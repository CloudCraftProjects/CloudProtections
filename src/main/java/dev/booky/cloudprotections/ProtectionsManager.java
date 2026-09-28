package dev.booky.cloudprotections;
// Created by booky10 in CloudProtections (01:58 01.04.23)

import dev.booky.cloudcore.config.ConfigurateLoader;
import dev.booky.cloudprotections.config.ProtectionAreaSerializer;
import dev.booky.cloudprotections.config.ProtectionRegionSerializer;
import dev.booky.cloudprotections.region.ProtectionFlag;
import dev.booky.cloudprotections.region.ProtectionRegion;
import dev.booky.cloudprotections.region.area.IProtectionArea;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class ProtectionsManager {

    private static final Component PREFIX = MiniMessage.miniMessage()
            .deserialize("<gray>[<gradient:#4775ff:#9563ff>CloudProtections</gradient>] </gray>")
            .compact();

    private static final TypeToken<List<ProtectionRegion>> REGIONS_TOKEN = new TypeToken<>() {
    };
    private static final ConfigurateLoader<?, ?> CONFIG_LOADER = ConfigurateLoader.yamlLoader()
            .withAllDefaultSerializers()
            .withSerializers(builder -> builder
                    .register(ProtectionRegion.class, ProtectionRegionSerializer.INSTANCE)
                    .register(IProtectionArea.class, ProtectionAreaSerializer.INSTANCE))
            .build();

    private final Plugin plugin;
    private final Path regionsPath;

    private final Object regionsLock = new Object();
    private RegionSnapshot regions = new RegionSnapshot(Map.of(), List.of());

    public ProtectionsManager(Plugin plugin) {
        this.plugin = plugin;
        this.regionsPath = plugin.getDataFolder().toPath().resolve("regions.yml");
    }

    public void reloadRegions() {
        List<ProtectionRegion> regions = CONFIG_LOADER.loadObject(
                this.regionsPath, REGIONS_TOKEN, List::of);
        this.replaceRegions(regions);
    }

    public void saveRegions() {
        synchronized (this.regionsLock) {
            this.saveRegions0();
        }
    }

    private void saveRegions0() {
        CONFIG_LOADER.saveObject(this.regionsPath, this.regions.sorted(), REGIONS_TOKEN);
    }

    public void updateRegions(Consumer<Map<String, ProtectionRegion>> consumer) {
        synchronized (this.regionsLock) {
            Map<String, ProtectionRegion> mutRegions = new LinkedHashMap<>(this.regions.byId());
            consumer.accept(mutRegions);

            this.replaceRegions0(mutRegions.values());
            this.saveRegions0();
        }
    }

    private void replaceRegions(Collection<ProtectionRegion> regions) {
        synchronized (this.regionsLock) {
            this.replaceRegions0(regions);
        }
    }

    private void replaceRegions0(Collection<ProtectionRegion> regions) {
        List<ProtectionRegion> regionList = new ArrayList<>(regions);
        regionList.sort(Comparator.comparingInt(ProtectionRegion::getPriority).reversed());

        Map<String, ProtectionRegion> newRegions = new LinkedHashMap<>(regionList.size());
        for (ProtectionRegion region : regionList) {
            newRegions.put(region.getId(), region);
        }

        this.regions = new RegionSnapshot(
                Collections.unmodifiableMap(newRegions),
                List.copyOf(regionList)
        );
    }

    public final boolean isProtected(Location location, ProtectionFlag flag, @Nullable Player player) {
        return this.isProtected(location.getBlock(), flag, player);
    }

    public final boolean isProtected(Block block, ProtectionFlag flag, @Nullable Player player) {
        if (player != null && player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }

        for (ProtectionRegion region : this.getRegions()) {
            if (region.check(block, flag)) {
                return player == null || !region.isExcluded(player);
            }
        }
        return false;
    }

    public @Nullable ProtectionRegion getRegion(String id) {
        return this.regions.byId().get(id);
    }

    public Set<String> getRegionIds() {
        return this.regions.byId().keySet();
    }

    public Collection<ProtectionRegion> getRegions() {
        return this.regions.sorted();
    }

    public Component getPrefix() {
        return PREFIX;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    private record RegionSnapshot(
            Map<String, ProtectionRegion> byId,
            List<ProtectionRegion> sorted
    ) {
    }
}
