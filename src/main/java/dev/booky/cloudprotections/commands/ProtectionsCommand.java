package dev.booky.cloudprotections.commands;
// Created by booky10 in CloudProtections (21:49 17.04.23)

import dev.booky.cloudprotections.ProtectionsManager;
import dev.booky.cloudprotections.util.ProtectionFlag;
import dev.booky.cloudprotections.util.ProtectionRegion;
import dev.jorel.commandapi.CommandAPI;
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
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class ProtectionsCommand {

    private ProtectionsCommand() {
    }

    public static void register(ProtectionsManager manager) {
        CommandAPI.unregister("cloudprotections");
        CommandAPI.unregister("cprotections");
        CommandAPI.unregister("cprots");

        Supplier<ListArgument<ProtectionFlag>> flagsListArg = () -> new ListArgumentBuilder<ProtectionFlag>(
                "flags").withList(ProtectionFlag.values()).withStringMapper().buildGreedy();

        Supplier<Argument<ProtectionRegion>> regionArgument = () -> new CustomArgument<>(
                new StringArgument("region"), info -> {
            ProtectionRegion region = manager.getRegion(info.currentInput());
            if (region != null) {
                return region;
            }

            // apparently this is not translatable :(
            CustomArgument.MessageBuilder errorMsg = new CustomArgument.MessageBuilder("Invalid region: ").appendArgInput().appendHere();
            throw new CustomArgument.CustomArgumentException(errorMsg);
        }).replaceSuggestions(ArgumentSuggestions.stringsAsync(info -> CompletableFuture.supplyAsync(() ->
                manager.getConfig().getRegions().stream().map(ProtectionRegion::getId).toArray(String[]::new))));

        new CommandTree("cloudprotections")
                .withPermission("cloudprotections.command")
                .withAliases("cprotections", "cprots")
                .then(new LiteralArgument("create")
                        .then(new StringArgument("id")
                                .then(new LocationArgument("corner1", LocationType.BLOCK_POSITION)
                                        .then(new LocationArgument("corner2", LocationType.BLOCK_POSITION)
                                                .executes((CommandSender sender, CommandArguments args) -> { /**/ })
                                                .then(new WorldArgument("dimension")
                                                        .executes((CommandSender sender, CommandArguments args) -> { /**/ }))))))
                .then(new LiteralArgument("delete")
                        .then(regionArgument.get()
                                .executes((CommandSender sender, CommandArguments args) -> { /**/ })))
                .then(new LiteralArgument("list")
                        .executes((CommandSender sender, CommandArguments args) -> { /**/ }))
                .then(new LiteralArgument("flags")
                        .then(regionArgument.get()
                                .then(new LiteralArgument("add")
                                        .then(flagsListArg.get()
                                                .executes((CommandSender sender, CommandArguments args) -> { /**/ })))
                                .then(new LiteralArgument("remove")
                                        .then(flagsListArg.get()
                                                .executes((CommandSender sender, CommandArguments args) -> { /**/ })))
                                .then(new LiteralArgument("list")
                                        .executes((CommandSender sender, CommandArguments args) -> { /**/ }))))
                .register();
    }
}
