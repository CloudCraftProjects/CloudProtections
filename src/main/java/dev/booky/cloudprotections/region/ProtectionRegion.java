package dev.booky.cloudprotections.region;
// Created by booky10 in CraftAttack (21:22 13.11.22)

import dev.booky.cloudprotections.region.area.IProtectionArea;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class ProtectionRegion {

    private final String id;
    private final IProtectionArea area;
    private final Set<ProtectionFlag> flags;

    public ProtectionRegion(String id, IProtectionArea area) {
        this(id, area, EnumSet.allOf(ProtectionFlag.class));
    }

    public ProtectionRegion(String id, IProtectionArea area, ProtectionFlag... flags) {
        this(id, area, EnumSet.copyOf(List.of(flags)));
    }

    public ProtectionRegion(String id, IProtectionArea area, Set<ProtectionFlag> flags) {
        this.id = id;
        this.area = area;
        this.flags = EnumSet.copyOf(flags);
    }

    public final boolean check(Block block, ProtectionFlag flag) {
        return this.flags.contains(flag) && this.area.contains(block);
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

    public String getId() {
        return this.id;
    }

    public final IProtectionArea getArea() {
        return this.area;
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
