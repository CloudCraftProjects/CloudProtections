package dev.booky.cloudprotections;
// Created by booky10 in CloudProtections (01:58 01.04.23)

import dev.booky.cloudcore.config.ConfigLoader;
import dev.booky.cloudprotections.util.ProtectionsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.function.Consumer;

public final class ProtectionsManager {

    private static final Component PREFIX = Component.text() // <gray>[<gradient:#4775ff:#9563ff>CloudProtections</gradient>] </gray>
            .append(Component.text("[", NamedTextColor.GRAY))
            .append(Component.text("C", TextColor.color(0x4775FF)))
            .append(Component.text("l", TextColor.color(0x4C74FF)))
            .append(Component.text("o", TextColor.color(0x5173FF)))
            .append(Component.text("u", TextColor.color(0x5672FF)))
            .append(Component.text("d", TextColor.color(0x5B71FF)))
            .append(Component.text("P", TextColor.color(0x5F6FFF)))
            .append(Component.text("r", TextColor.color(0x646EFF)))
            .append(Component.text("o", TextColor.color(0x696DFF)))
            .append(Component.text("t", TextColor.color(0x6E6CFF)))
            .append(Component.text("e", TextColor.color(0x736BFF)))
            .append(Component.text("c", TextColor.color(0x786AFF)))
            .append(Component.text("t", TextColor.color(0x7D69FF)))
            .append(Component.text("i", TextColor.color(0x8268FF)))
            .append(Component.text("o", TextColor.color(0x8666FF)))
            .append(Component.text("n", TextColor.color(0x8B65FF)))
            .append(Component.text("s", TextColor.color(0x9064FF)))
            .append(Component.text("] ", NamedTextColor.GRAY))
            .build().compact();

    private final Plugin plugin;

    private final Path configPath;
    private ProtectionsConfig config;

    public ProtectionsManager(Plugin plugin) {
        this.plugin = plugin;
        this.configPath = plugin.getDataFolder().toPath().resolve("config.yml");
    }

    public void reloadConfig() {
        this.config = ConfigLoader.loadObject(this.configPath, ProtectionsConfig.class);
    }

    public void saveConfig() {
        ConfigLoader.saveObject(this.configPath, this.config);
    }

    public synchronized void updateConfig(Consumer<ProtectionsConfig> consumer) {
        consumer.accept(this.config);
        this.saveConfig();
    }

    public Component getPrefix() {
        return PREFIX;
    }

    public ProtectionsConfig getConfig() {
        return this.config;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }
}
