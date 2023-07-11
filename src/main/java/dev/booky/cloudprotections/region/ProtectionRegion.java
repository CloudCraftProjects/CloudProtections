package dev.booky.cloudprotections.region;
// Created by booky10 in CraftAttack (21:22 13.11.22)

import dev.booky.cloudprotections.region.area.IProtectionArea;
import dev.booky.cloudprotections.region.exclusions.IProtectionExclusion;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public final class ProtectionRegion {

    private final String id;
    private final IProtectionArea area;
    private final Set<IProtectionExclusion> exclusions;
    private final Set<ProtectionFlag> flags;

    public ProtectionRegion(String id, IProtectionArea area) {
        this(id, area, EnumSet.allOf(ProtectionFlag.class));
    }

    public ProtectionRegion(String id, IProtectionArea area, ProtectionFlag... flags) {
        this(id, area, Set.of(flags));
    }

    public ProtectionRegion(String id, IProtectionArea area, Set<ProtectionFlag> flags) {
        this(id, area, Set.of(), flags);
    }

    public ProtectionRegion(String id, IProtectionArea area, Set<IProtectionExclusion> exclusions, Set<ProtectionFlag> flags) {
        this.id = id;
        this.area = area;
        this.exclusions = new HashSet<>(exclusions);
        this.flags = EnumSet.copyOf(flags);
    }

    public final boolean check(Block block, ProtectionFlag flag) {
        return this.flags.contains(flag) && this.area.contains(block);
    }

    public final boolean isExcluded(Player player) {
        for (IProtectionExclusion exclusion : this.exclusions) {
            if (exclusion.isExcluded(player)) {
                return true;
            }
        }
        return false;
    }

    public final boolean addFlag(ProtectionFlag flag) {
        return this.flags.add(flag);
    }

    public final boolean removeFlag(ProtectionFlag flag) {
        return this.flags.remove(flag);
    }

    public final boolean hasFlag(ProtectionFlag flag) {
        return this.flags.contains(flag);
    }

    public final boolean addExclusion(IProtectionExclusion exclusion) {
        return this.exclusions.add(exclusion);
    }

    public final boolean removeExclusion(IProtectionExclusion exclusion) {
        return this.exclusions.remove(exclusion);
    }

    public final String getId() {
        return this.id;
    }

    public final IProtectionArea getArea() {
        return this.area;
    }

    public final Set<IProtectionExclusion> getExclusions() {
        return Collections.unmodifiableSet(this.exclusions);
    }

    public final Set<ProtectionFlag> getFlags() {
        return Collections.unmodifiableSet(this.flags);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ProtectionRegion that)) return false;
        return this.area.equals(that.area);
    }

    @Override
    public int hashCode() {
        return this.area.hashCode();
    }

    @Override
    public String toString() {
        return "ProtectionRegion{id='" + this.id + '\'' + ", area=" + this.area + ", flags=" + this.flags + '}';
    }
}
