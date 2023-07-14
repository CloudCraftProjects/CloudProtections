package dev.booky.cloudprotections.commands;
// Created by booky10 in CloudProtections (21:49 17.04.23)

import dev.booky.cloudcore.util.BlockBBox;
import dev.booky.cloudprotections.ProtectionsManager;
import dev.booky.cloudprotections.region.ProtectionFlag;
import dev.booky.cloudprotections.region.ProtectionRegion;
import dev.booky.cloudprotections.region.area.BoxProtectionArea;
import dev.booky.cloudprotections.region.area.IProtectionArea;
import dev.booky.cloudprotections.region.area.SphericalProtectionArea;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
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
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Function;
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
        return CommandAPIBukkit.failWithAdventureComponent(this.failMsg(message));
    }

    private void fail(CommandSender sender, Component message) {
        sender.sendMessage(this.failMsg(message));
    }

    private Component failMsg(Component message) {
        return this.manager.getPrefix().append(message.colorIfAbsent(NamedTextColor.RED));
    }

    private void success(CommandSender sender, Component message) {
        sender.sendMessage(this.successMsg(message));
    }

    private Component successMsg(Component message) {
        return this.manager.getPrefix().append(message.colorIfAbsent(NamedTextColor.YELLOW));
    }

    private void unregister() {
        CommandAPI.unregister("cloudprotections");
        CommandAPI.unregister("cprotections");
        CommandAPI.unregister("cprots");
    }

    private void register() {
        Function<BiPredicate<SuggestionInfo<CommandSender>, ProtectionFlag>, ListArgument<ProtectionFlag>> flagsListArg =
                filter -> new ListArgumentBuilder<ProtectionFlag>("flags")
                        .withList(info -> Arrays.stream(ProtectionFlag.values())
                                .filter(flag -> filter.test(info, flag)).toList())
                        .withMapper(flag -> flag.name().toLowerCase(Locale.ROOT))
                        .buildGreedy();

        Supplier<Argument<ProtectionRegion>> regionArgument = () -> new CustomArgument<>(
                new StringArgument("region"), info -> {
            ProtectionRegion region = this.manager.getRegion(info.currentInput());
            if (region != null) {
                return region;
            }

            throw CustomArgument.CustomArgumentException.fromAdventureComponent(failMsg(
                    Component.translatable("protections.command.invalid-region",
                            Component.text(info.currentInput(), NamedTextColor.WHITE))));
        }).replaceSuggestions(ArgumentSuggestions.strings(info ->
                this.manager.getRegionIds().toArray(String[]::new)));

        BiPredicate<SuggestionInfo<CommandSender>, ProtectionFlag> flagRemoveFilter = (info, flag) -> {
            ProtectionRegion region = Objects.requireNonNull(info.previousArgs().getUnchecked("region"));
            return region.hasFlag(flag);
        };
        BiPredicate<SuggestionInfo<CommandSender>, ProtectionFlag> flagAddFilter = flagRemoveFilter.negate();

        LiteralArgument createArgument = new LiteralArgument("create");
        for (AreaType areaType : AreaType.values()) {
            createArgument.then(new LiteralArgument(areaType.name().toLowerCase(Locale.ROOT))
                    .then(new StringArgument("id")
                            .then(areaType.provideArgs(this))));
        }

        new CommandTree("cloudprotections")
                .withPermission("cloudprotections.command")
                .withAliases("cprotections", "cprots")
                .then(createArgument
                        .withPermission("cloudprotections.command.create"))
                .then(new LiteralArgument("list")
                        .withPermission("cloudprotections.command.list")
                        .executesNative(this::listRegions))
                .then(new LiteralArgument("modify")
                        .then(regionArgument.get()
                                .then(new LiteralArgument("delete")
                                        .withPermission("cloudprotections.command.delete")
                                        .executesNative(this::deleteRegion))
                                .then(new LiteralArgument("rename")
                                        .withPermission("cloudprotections.command.rename")
                                        .then(new StringArgument("id")
                                                .executesNative(this::renameRegion)))
                                .then(new LiteralArgument("flags")
                                        .withPermission("cloudprotections.command.flags")
                                        .executesNative(this::listRegionFlags)
                                        .then(new LiteralArgument("add")
                                                .withPermission("cloudprotections.command.flags.add")
                                                .then(flagsListArg.apply(flagAddFilter)
                                                        .executesNative(this::addRegionFlag)))
                                        .then(new LiteralArgument("remove")
                                                .withPermission("cloudprotections.command.flags.remove")
                                                .then(flagsListArg.apply(flagRemoveFilter)
                                                        .executesNative(this::removeRegionFlag)))
                                        .then(new LiteralArgument("list")
                                                .executesNative(this::listRegionFlags)))
                                .then(new LiteralArgument("exclusions")
                                        .withPermission("cloudprotections.command.exclusions")
                                        .executesNative(this::listRegionExclusions)
                                        .then(new LiteralArgument("add")
                                                .withPermission("cloudprotections.command.exclusions.add")
                                                .then(new GreedyStringArgument("uuid")
                                                        .executesNative(this::addRegionExclusion)))
                                        .then(new LiteralArgument("remove")
                                                .withPermission("cloudprotections.command.exclusions.remove")
                                                .then(new GreedyStringArgument("uuid")
                                                        .executesNative(this::removeRegionExclusion)))
                                        .then(new LiteralArgument("list")
                                                .executesNative(this::listRegionExclusions)))))
                .register();
    }

    private void createRegion(NativeProxyCommandSender sender, CommandArguments args, AreaType areaType) throws WrapperCommandSyntaxException {
        String id = Objects.requireNonNull(args.getUnchecked("id"));
        if (this.manager.getRegion(id) != null) {
            throw this.fail(Component.translatable("protections.command.create.already-exists", Component.text(id, NamedTextColor.WHITE)));
        }

        IProtectionArea area = areaType.create(sender, args);
        ProtectionRegion region = new ProtectionRegion(id, area, EnumSet.allOf(ProtectionFlag.class));

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

    private void renameRegion(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        ProtectionRegion region = Objects.requireNonNull(args.getUnchecked("region"));
        String newId = Objects.requireNonNull(args.getUnchecked("id"));
        ProtectionRegion newRegion = new ProtectionRegion(newId, region.getArea(), region.getFlags());

        if (this.manager.getRegion(newId) != null) {
            throw this.fail(Component.translatable("protections.command.rename.already-exists",
                    Component.text(newId, NamedTextColor.WHITE)));
        }

        this.manager.updateRegions(regions -> {
            regions.remove(region.getId());
            regions.put(newId, newRegion);
        });
        this.success(sender, Component.translatable("protections.command.rename.success",
                Component.text(region.getId(), NamedTextColor.WHITE), Component.text(newId, NamedTextColor.WHITE)));
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
            AreaType areaType = AreaType.get(region.getArea());
            Component description = areaType.getDescription(region.getArea());
            Location centerLocation = region.getArea().getCenterLocation();

            msg.appendNewline().appendSpace();
            msg.append(Component.translatable("protections.command.list.entry",
                            Component.text(region.getId(), NamedTextColor.WHITE), description,
                            Component.text(region.getFlags().size(), NamedTextColor.WHITE))
                    .clickEvent(ClickEvent.callback(clicker -> {
                        if (clicker == sender.getCaller() && clicker instanceof Entity entity) {
                            entity.teleportAsync(centerLocation, PlayerTeleportEvent.TeleportCause.COMMAND);
                        }
                    }, opts -> opts.uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofMinutes(10)))));
        }

        this.success(sender, msg.build());
    }

    private void addRegionFlag(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        ProtectionRegion region = Objects.requireNonNull(args.getUnchecked("region"));
        List<ProtectionFlag> flags = Objects.requireNonNull(args.getUnchecked("flags"));
        flags.removeIf(region::hasFlag);

        if (flags.isEmpty()) {
            throw fail(Component.translatable("protections.command.flags.add.nothing-changed",
                    Component.text(region.getId(), NamedTextColor.WHITE)));
        }

        ComponentBuilder<?, ?> msg = Component.translatable()
                .key(flags.size() == 1
                        ? "protections.command.flags.add.success.singular"
                        : "protections.command.flags.add.success.plural")
                .args(Component.text(flags.size(), NamedTextColor.WHITE),
                        Component.text(region.getId(), NamedTextColor.WHITE));

        for (ProtectionFlag flag : flags) {
            region.addFlag(flag);

            if (msg.children().isEmpty()) {
                msg.appendSpace();
            } else {
                msg.append(Component.text(", "));
            }
            msg.append(flag.getName().colorIfAbsent(NamedTextColor.WHITE));
        }
        this.manager.saveRegions();

        this.success(sender, msg.build());
    }

    private void removeRegionFlag(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        ProtectionRegion region = Objects.requireNonNull(args.getUnchecked("region"));
        List<ProtectionFlag> flags = Objects.requireNonNull(args.getUnchecked("flags"));
        flags.removeIf(Predicate.not(region::hasFlag));

        if (flags.isEmpty()) {
            throw fail(Component.translatable("protections.command.flags.remove.nothing-changed",
                    Component.text(region.getId(), NamedTextColor.WHITE)));
        }

        ComponentBuilder<?, ?> msg = Component.translatable()
                .key(flags.size() == 1
                        ? "protections.command.flags.remove.success.singular"
                        : "protections.command.flags.remove.success.plural")
                .args(Component.text(flags.size(), NamedTextColor.WHITE),
                        Component.text(region.getId(), NamedTextColor.WHITE));

        for (ProtectionFlag flag : flags) {
            region.removeFlag(flag);

            if (msg.children().isEmpty()) {
                msg.appendSpace();
            } else {
                msg.append(Component.text(", "));
            }
            msg.append(flag.getName().colorIfAbsent(NamedTextColor.WHITE));
        }
        this.manager.saveRegions();

        this.success(sender, msg.build());
    }

    private void listRegionFlags(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        ProtectionRegion region = Objects.requireNonNull(args.getUnchecked("region"));
        Set<ProtectionFlag> flags = Set.copyOf(region.getFlags());
        if (flags.isEmpty()) {
            throw this.fail(Component.translatable("protections.command.flags.list.none",
                    Component.text(region.getId(), NamedTextColor.WHITE)));
        }

        ComponentBuilder<?, ?> msg = Component.translatable()
                .key(flags.size() == 1
                        ? "protections.command.flags.list.info.singular"
                        : "protections.command.flags.list.info.plural")
                .args(Component.text(flags.size(), NamedTextColor.WHITE),
                        Component.text(region.getId(), NamedTextColor.WHITE));

        for (ProtectionFlag flag : flags) {
            if (msg.children().isEmpty()) {
                msg.appendSpace();
            } else {
                msg.append(Component.text(", "));
            }
            msg.append(flag.getName().colorIfAbsent(NamedTextColor.WHITE));
        }

        this.success(sender, msg.build());
    }

    private void addRegionExclusion(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        ProtectionRegion region = Objects.requireNonNull(args.getUnchecked("region"));
        UUID uuid = args.<String>getOptionalUnchecked("uuid").map(UUID::fromString).orElseThrow();

        if (!region.addExclusion(uuid)) {
            throw fail(Component.translatable("protections.command.exclusions.add.nothing-changed",
                    Component.text(region.getId(), NamedTextColor.WHITE)));
        }
        this.manager.saveRegions();

        this.success(sender, Component.translatable("protections.command.exclusions.add.success",
                Component.text(region.getId(), NamedTextColor.WHITE),
                Component.text(uuid.toString(), NamedTextColor.WHITE)));
    }

    private void removeRegionExclusion(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        ProtectionRegion region = Objects.requireNonNull(args.getUnchecked("region"));
        UUID uuid = args.<String>getOptionalUnchecked("uuid").map(UUID::fromString).orElseThrow();

        if (!region.removeExclusion(uuid)) {
            throw fail(Component.translatable("protections.command.exclusions.remove.nothing-changed",
                    Component.text(region.getId(), NamedTextColor.WHITE)));
        }
        this.manager.saveRegions();

        this.success(sender, Component.translatable("protections.command.exclusions.remove.success",
                Component.text(region.getId(), NamedTextColor.WHITE),
                Component.text(uuid.toString(), NamedTextColor.WHITE)));
    }

    private void listRegionExclusions(NativeProxyCommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        ProtectionRegion region = Objects.requireNonNull(args.getUnchecked("region"));
        Set<UUID> exclusions = Set.copyOf(region.getExcludedPlayerIds());
        if (exclusions.isEmpty()) {
            throw this.fail(Component.translatable("protections.command.exclusions.list.none",
                    Component.text(region.getId(), NamedTextColor.WHITE)));
        }

        ComponentBuilder<?, ?> msg = Component.translatable()
                .key(exclusions.size() == 1
                        ? "protections.command.exclusions.list.info.singular"
                        : "protections.command.exclusions.list.info.plural")
                .args(Component.text(exclusions.size(), NamedTextColor.WHITE),
                        Component.text(region.getId(), NamedTextColor.WHITE));

        for (UUID exclusion : exclusions) {
            msg.appendNewline();
            msg.append(Component.text(" - "));
            msg.append(Component.text(exclusion.toString(), NamedTextColor.WHITE));
        }

        this.success(sender, msg.build());
    }

    private enum AreaType {
        BOX {
            @Override
            public Argument<?> provideArgs(ProtectionsCommand command) {
                return new LocationArgument("corner1", LocationType.BLOCK_POSITION)
                        .then(new LocationArgument("corner2", LocationType.BLOCK_POSITION)
                                .then(new WorldArgument("dimension").setOptional(true)
                                        .executesNative((NativeProxyCommandSender sender, CommandArguments args)
                                                -> command.createRegion(sender, args, this))));
            }

            @Override
            public IProtectionArea create(NativeProxyCommandSender sender, CommandArguments args) {
                World dimension = args.<World>getOptionalUnchecked("dimension").orElseGet(sender::getWorld);
                Vector corner1 = Objects.requireNonNull(args.<Location>getUnchecked("corner1")).toVector();
                Vector corner2 = Objects.requireNonNull(args.<Location>getUnchecked("corner2")).toVector();

                BlockBBox box = new BlockBBox(dimension, corner1, corner2);
                return new BoxProtectionArea(box);
            }

            @Override
            protected boolean matches(IProtectionArea instance) {
                return instance instanceof BoxProtectionArea;
            }

            @Override
            public Component getDescription(IProtectionArea instance) {
                BlockBBox box = ((BoxProtectionArea) instance).getBox();
                String dimensionStr = box.getWorld().getKey().asString();
                String minStr = box.getMinX() + ":" + box.getMinY() + ":" + box.getMinZ();
                String maxStr = box.getMaxX() + ":" + box.getMaxY() + ":" + box.getMaxZ();
                String centerStr = box.getBlockCenterX() + ":" + box.getBlockCenterY() + ":" + box.getBlockCenterZ();
                return Component.translatable("protections.command.list.box",
                                Component.text(minStr, NamedTextColor.WHITE),
                                Component.text(maxStr, NamedTextColor.WHITE),
                                Component.text(dimensionStr, NamedTextColor.WHITE))
                        .hoverEvent(Component.translatable("protections.command.list.hover",
                                Component.text(centerStr, NamedTextColor.WHITE)));
            }
        },
        SPHERE {
            @Override
            public Argument<?> provideArgs(ProtectionsCommand command) {
                return new LocationArgument("center", LocationType.BLOCK_POSITION)
                        .then(new DoubleArgument("radius", Vector.getEpsilon())
                                .then(new WorldArgument("dimension").setOptional(true)
                                        .executesNative((NativeProxyCommandSender sender, CommandArguments args)
                                                -> command.createRegion(sender, args, this))));
            }

            @Override
            public IProtectionArea create(NativeProxyCommandSender sender, CommandArguments args) {
                World dimension = args.<World>getOptionalUnchecked("dimension").orElseGet(sender::getWorld);
                double radius = Objects.requireNonNull(args.<Double>getUnchecked("radius"));

                Block centerBlock = Objects.requireNonNull(args.<Location>getUnchecked("center")).getBlock();
                BlockPosition centerBlockPos = Position.block(centerBlock.getX(), centerBlock.getY(), centerBlock.getZ());

                return new SphericalProtectionArea(dimension, centerBlockPos, radius);
            }

            @Override
            protected boolean matches(IProtectionArea instance) {
                return instance instanceof SphericalProtectionArea;
            }

            @Override
            public Component getDescription(IProtectionArea instance) {
                SphericalProtectionArea area = ((SphericalProtectionArea) instance);
                BlockPosition centerBlock = area.getCenterBlockPos();

                String dimensionStr = area.getWorld().getKey().asString();
                String centerStr = centerBlock.blockX() + ":" + centerBlock.blockY() + ":" + centerBlock.blockZ();
                String radiusStr = "%,.2f".formatted(area.getRadius());
                return Component.translatable("protections.command.list.spherical",
                                Component.text(radiusStr, NamedTextColor.WHITE),
                                Component.text(centerStr, NamedTextColor.WHITE),
                                Component.text(dimensionStr, NamedTextColor.WHITE))
                        .hoverEvent(Component.translatable("protections.command.list.hover",
                                Component.text(centerStr, NamedTextColor.WHITE)));
            }
        };

        public static AreaType get(IProtectionArea instance) {
            for (AreaType areaType : values()) {
                if (areaType.matches(instance)) {
                    return areaType;
                }
            }
            throw new UnsupportedOperationException("Unsupported area implementation: " + instance);
        }

        public abstract Argument<?> provideArgs(ProtectionsCommand command);

        public abstract IProtectionArea create(NativeProxyCommandSender sender, CommandArguments args);

        public abstract Component getDescription(IProtectionArea instance);

        protected abstract boolean matches(IProtectionArea instance);
    }
}
