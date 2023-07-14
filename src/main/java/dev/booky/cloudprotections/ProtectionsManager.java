package dev.booky.cloudprotections;
// Created by booky10 in CloudProtections (01:58 01.04.23)

import dev.booky.cloudcore.config.ConfigLoader;
import dev.booky.cloudprotections.config.ProtectionAreaSerializer;
import dev.booky.cloudprotections.config.ProtectionRegionSerializer;
import dev.booky.cloudprotections.region.ProtectionFlag;
import dev.booky.cloudprotections.region.ProtectionRegion;
import dev.booky.cloudprotections.region.area.IProtectionArea;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            .register(ProtectionRegion.class, ProtectionRegionSerializer.INSTANCE)
            .register(IProtectionArea.class, ProtectionAreaSerializer.INSTANCE);
    private static final TypeToken<List<ProtectionRegion>> REGIONS_TOKEN = new TypeToken<>() {};

    private final Plugin plugin;
    private final Path regionsPath;
    private Map<String, ProtectionRegion> regions = Map.of();

    public ProtectionsManager(Plugin plugin) {
        this.plugin = plugin;
        this.regionsPath = plugin.getDataFolder().toPath().resolve("regions.yml");
    }

    public void reloadRegions() {
        this.regions = ConfigLoader.loadObject(this.regionsPath, REGIONS_TOKEN, List::of, CONFIG_SERIALIZERS)
                .stream().collect(Collectors.toUnmodifiableMap(ProtectionRegion::getId, Function.identity()));
    }

    public void saveRegions() {
        ConfigLoader.saveObject(this.regionsPath, REGIONS_TOKEN, List.copyOf(this.regions.values()), CONFIG_SERIALIZERS);
    }

    public synchronized void updateRegions(Consumer<Map<String, ProtectionRegion>> consumer) {
        Map<String, ProtectionRegion> mutRegions = new HashMap<>(this.regions);
        consumer.accept(mutRegions);

        if (!this.regions.equals(mutRegions)) {
            this.regions = mutRegions;
            this.saveRegions();
        }
    }

    public final boolean isProtected(Location location, ProtectionFlag flag, @Nullable Player player) {
        return this.isProtected(location.getBlock(), flag, player);
    }

    public final boolean isProtected(Block block, ProtectionFlag flag, @Nullable Player player) {
        if (player != null && player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        for (ProtectionRegion region : this.getRegions()) {
            if (player != null && region.isExcluded(player)) {
                continue;
            }
            if (region.check(block, flag)) {
                return true;
            }
        }
        return false;
    }

    public @Nullable ProtectionRegion getRegion(String id) {
        return this.regions.get(id);
    }

    public Set<String> getRegionIds() {
        return Collections.unmodifiableSet(this.regions.keySet());
    }

    public Collection<ProtectionRegion> getRegions() {
        return Collections.unmodifiableCollection(this.regions.values());
    }

    public Component getPrefix() {
        return PREFIX;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }
}
