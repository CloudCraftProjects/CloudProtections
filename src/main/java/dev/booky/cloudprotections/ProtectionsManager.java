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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private static final TypeToken<List<ProtectionRegion>> REGIONS_TOKEN = new TypeToken<>() {};
    private static final ConfigurateLoader<?, ?> CONFIG_LOADER = ConfigurateLoader.yamlLoader()
            .withAllDefaultSerializers()
            .withSerializers(builder -> builder
                    .register(ProtectionRegion.class, ProtectionRegionSerializer.INSTANCE)
                    .register(IProtectionArea.class, ProtectionAreaSerializer.INSTANCE))
            .build();

    private final Plugin plugin;
    private final Path regionsPath;
    private final Map<String, ProtectionRegion> regions = new LinkedHashMap<>();

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
        CONFIG_LOADER.saveObject(this.regionsPath, List.copyOf(this.regions.values()), REGIONS_TOKEN);
    }

    public synchronized void updateRegions(Consumer<Map<String, ProtectionRegion>> consumer) {
        Map<String, ProtectionRegion> mutRegions = new HashMap<>(this.regions);
        consumer.accept(mutRegions);

        this.replaceRegions(mutRegions.values());
        this.saveRegions();
    }

    private void replaceRegions(Collection<ProtectionRegion> regions) {
        List<ProtectionRegion> regionList = new ArrayList<>(regions);
        regionList.sort(Comparator.comparingInt(ProtectionRegion::getPriority).reversed());

        synchronized (this.regions) {
            this.regions.clear();
            for (ProtectionRegion region : regionList) {
                this.regions.put(region.getId(), region);
            }
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
            if (region.check(block, flag)) {
                return player == null || !region.isExcluded(player);
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
