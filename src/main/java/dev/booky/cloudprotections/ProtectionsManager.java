package dev.booky.cloudprotections;
// Created by booky10 in CloudProtections (01:58 01.04.23)

import dev.booky.cloudcore.config.ConfigLoader;
import dev.booky.cloudprotections.config.ProtectionRegionSerializer;
import dev.booky.cloudprotections.util.ProtectionFlag;
import dev.booky.cloudprotections.util.ProtectionRegion;
import dev.booky.cloudprotections.util.ProtectionsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

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

    private static final Consumer<TypeSerializerCollection.Builder> CONFIG_SERIALIZERS = builder -> builder
            .register(ProtectionRegion.class, ProtectionRegionSerializer.INSTANCE);

    private final Plugin plugin;

    private final Path configPath;
    private ProtectionsConfig config;

    public ProtectionsManager(Plugin plugin) {
        this.plugin = plugin;
        this.configPath = plugin.getDataFolder().toPath().resolve("config.yml");
    }

    public void reloadConfig() {
        this.config = ConfigLoader.loadObject(this.configPath, ProtectionsConfig.class, CONFIG_SERIALIZERS);
    }

    public void saveConfig() {
        ConfigLoader.saveObject(this.configPath, this.config, CONFIG_SERIALIZERS);
    }

    public synchronized void updateConfig(Consumer<ProtectionsConfig> consumer) {
        consumer.accept(this.config);
        this.saveConfig();
    }

    public final boolean isProtected(Block block, ProtectionFlag flag, @Nullable HumanEntity entity) {
        Location location = new Location(block.getWorld(), block.getX() + 0.5d, block.getY() + 0.5d, block.getZ() + 0.5d);
        return this.isProtected(location, flag, entity);
    }

    public final boolean isProtected(Location location, ProtectionFlag flag, @Nullable HumanEntity entity) {
        if (entity != null && entity.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        for (ProtectionRegion region : this.getConfig().getRegions()) {
            if (region.check(location, flag)) {
                return true;
            }
        }
        return false;
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
