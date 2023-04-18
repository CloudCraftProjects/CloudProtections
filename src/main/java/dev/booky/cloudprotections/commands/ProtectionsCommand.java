package dev.booky.cloudprotections.commands;
// Created by booky10 in CloudProtections (21:49 17.04.23)

import dev.booky.cloudcore.util.BlockBBox;
import dev.booky.cloudprotections.ProtectionsManager;
import dev.booky.cloudprotections.util.ProtectionFlag;
import dev.booky.cloudprotections.util.ProtectionRegion;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.ListArgument;
import dev.jorel.commandapi.arguments.ListArgumentBuilder;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.WorldArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ProtectionsCommand {

    private final ProtectionsManager manager;

    private ProtectionsCommand(ProtectionsManager manager) {
        this.manager = manager;
    }

    public static void create(ProtectionsManager manager) {
        ProtectionsCommand command = new ProtectionsCommand(manager);
        command.unregister();
        command.register();
    }

    private WrapperCommandSyntaxException fail(Component message) {
        return CommandAPIBukkit.failWithAdventureComponent(this.manager.getPrefix()
                .append(message.colorIfAbsent(NamedTextColor.RED)));
    }

    private void fail(CommandSender sender, Component message) {
        sender.sendMessage(this.manager.getPrefix()
                .append(message.colorIfAbsent(NamedTextColor.RED)));
    }

    private void success(CommandSender sender, Component message) {
        sender.sendMessage(this.manager.getPrefix()
                .append(message.colorIfAbsent(NamedTextColor.YELLOW)));
    }

    private void unregister() {
        CommandAPI.unregister("cloudprotections");
        CommandAPI.unregister("cprotections");
        CommandAPI.unregister("cprots");
    }

    private void register() {
        Supplier<ListArgument<ProtectionFlag>> flagsListArg = () -> new ListArgumentBuilder<ProtectionFlag>(
                "flags").withList(ProtectionFlag.values()).withStringMapper().buildGreedy();

        Supplier<Argument<ProtectionRegion>> regionArgument = () -> new CustomArgument<>(
                new StringArgument("region"), info -> {
            ProtectionRegion region = this.manager.getRegion(info.currentInput());
            if (region != null) {
                return region;
            }

            // apparently this is not translatable :(
            CustomArgument.MessageBuilder errorMsg = new CustomArgument.MessageBuilder("Invalid region: ").appendArgInput().appendHere();
            throw new CustomArgument.CustomArgumentException(errorMsg);
        }).replaceSuggestions(ArgumentSuggestions.strings(info ->
                this.manager.getRegionIds().toArray(String[]::new)));

        new CommandTree("cloudprotections")
                .withPermission("cloudprotections.command")
                .withAliases("cprotections", "cprots")
                .then(new LiteralArgument("create")
                        .then(new StringArgument("id")
                                .then(new LocationArgument("corner1", LocationType.BLOCK_POSITION)
                                        .then(new LocationArgument("corner2", LocationType.BLOCK_POSITION)
                                                .then(new WorldArgument("dimension").setOptional(true)
                                                        .executesNative(this::createRegion))))))
                .then(new LiteralArgument("delete")
                        .then(regionArgument.get()
                                .executesNative(this::deleteRegion)))
                .then(new LiteralArgument("list")
                        .executesNative(this::listRegions))
                .then(new LiteralArgument("flags")
                        .then(regionArgument.get()
                                .then(new LiteralArgument("add")
                                        .then(flagsListArg.get()
                                                .executesNative(this::addRegionFlag)))
                                .then(new LiteralArgument("remove")
                                        .then(flagsListArg.get()
                                                .executesNative(this::removeRegionFlag)))
                                .then(new LiteralArgument("list")
                                        .executesNative(this::listRegionFlags))))
                .register();
    }

    private void createRegion(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        String id = Objects.requireNonNull(args.getUnchecked("id"));
        if (this.manager.getRegion(id) != null) {
            throw this.fail(Component.translatable("protections.command.create.already-exists", Component.text(id, NamedTextColor.WHITE)));
        }

        World dimension = args.getOrDefaultUnchecked("dimension", sender::getWorld);
        Vector corner1 = Objects.requireNonNull(args.<Location>getUnchecked("corner1")).toVector();
        Vector corner2 = Objects.requireNonNull(args.<Location>getUnchecked("corner2")).toVector();

        BlockBBox box = new BlockBBox(dimension, corner1, corner2);
        ProtectionRegion region = new ProtectionRegion(id, box, EnumSet.allOf(ProtectionFlag.class));

        this.manager.updateRegions(regions -> regions.putIfAbsent(id, region));
        this.success(sender, Component.translatable("protections.command.create.success", Component.text(id, NamedTextColor.WHITE)));
    }

    private void deleteRegion(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        ProtectionRegion region = Objects.requireNonNull(args.getUnchecked("region"));
        throw this.fail(Component.translatable("protections.command.delete.confirmation-required",
                Component.text(region.getId(), NamedTextColor.WHITE),
                Component.translatable("protections.command.delete.confirmation-button")
                        .clickEvent(ClickEvent.callback(clicker -> {
                            if (clicker != sender.getCaller()) {
                                return;
                            }

                            if (this.manager.getRegion(region.getId()) != region) {
                                // can't throw exceptions here
                                this.fail(sender, Component.translatable("protections.command.delete.invalid-region",
                                        Component.text(region.getId(), NamedTextColor.WHITE)));
                                return;
                            }

                            this.manager.updateRegions(regions -> regions.remove(region.getId(), region));
                            this.success(sender, Component.translatable("protections.command.delete.success",
                                    Component.text(region.getId(), NamedTextColor.WHITE)));
                        }))));
    }

    private void listRegions(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        Collection<ProtectionRegion> regions = this.manager.getRegions();
        if (regions.isEmpty()) {
            throw this.fail(Component.translatable("protections.command.list.none"));
        }

        ComponentBuilder<?, ?> msg = Component.translatable()
                .key("protections.command.list.header")
                .args(Component.text(regions.size(), NamedTextColor.WHITE));

        for (ProtectionRegion region : regions) {
            BlockBBox box = region.getBox();
            String minStr = box.getMinX() + ":" + box.getMinY() + ":" + box.getMinZ();
            String maxStr = box.getMaxX() + ":" + box.getMaxY() + ":" + box.getMaxZ();

            msg.appendNewline().appendSpace();
            msg.append(Component.translatable("protections.command.list.entry",
                    Component.text(region.getId(), NamedTextColor.WHITE),
                    Component.text(minStr, NamedTextColor.WHITE),
                    Component.text(maxStr, NamedTextColor.WHITE),
                    Component.text(box.getWorld().key().asString(), NamedTextColor.WHITE),
                    Component.text(region.getFlags().size(), NamedTextColor.WHITE)));
        }

        success(sender, msg.build());
    }

    private void addRegionFlag(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        ProtectionRegion region = Objects.requireNonNull(args.getUnchecked("region"));
        List<ProtectionFlag> flags = Objects.requireNonNull(args.getUnchecked("flags"));
        flags.removeIf(region::hasFlag);

        if (flags.isEmpty()) {
            throw fail(Component.translatable("protections.command.flags.add.nothing-changed"));
        }

        ComponentBuilder<?, ?> msg = Component.translatable()
                .key(flags.size() == 1
                        ? "protections.command.flags.add.success.singular"
                        : "protections.command.flags.add.success.plural")
                .args(Component.text(flags.size(), NamedTextColor.WHITE));

        for (ProtectionFlag flag : flags) {
            region.addFlag(flag);

            if (msg.children().isEmpty()) {
                msg.appendSpace();
            } else {
                msg.append(Component.text(", "));
            }
            msg.append(flag.getName().colorIfAbsent(NamedTextColor.WHITE));
        }

        success(sender, msg.build());
    }

    private void removeRegionFlag(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        ProtectionRegion region = Objects.requireNonNull(args.getUnchecked("region"));
        List<ProtectionFlag> flags = Objects.requireNonNull(args.getUnchecked("flags"));
        flags.removeIf(Predicate.not(region::hasFlag));

        if (flags.isEmpty()) {
            throw fail(Component.translatable("protections.command.flags.remove.nothing-changed"));
        }

        ComponentBuilder<?, ?> msg = Component.translatable()
                .key(flags.size() == 1
                        ? "protections.command.flags.remove.success.singular"
                        : "protections.command.flags.remove.success.plural")
                .args(Component.text(flags.size(), NamedTextColor.WHITE));

        for (ProtectionFlag flag : flags) {
            region.removeFlag(flag);

            if (msg.children().isEmpty()) {
                msg.appendSpace();
            } else {
                msg.append(Component.text(", "));
            }
            msg.append(flag.getName().colorIfAbsent(NamedTextColor.WHITE));
        }

        success(sender, msg.build());
    }

    private void listRegionFlags(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        throw this.fail(Component.text("Unsupported"));
    }
}
