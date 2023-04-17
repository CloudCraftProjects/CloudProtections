package dev.booky.cloudprotections.util;
// Created by booky10 in CraftAttack (21:22 13.11.22)

import dev.booky.cloudcore.util.BlockBBox;
import org.bukkit.Location;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class ProtectionRegion {

    private final String id;
    private final BlockBBox box;
    private final Set<ProtectionFlag> flags;

    public ProtectionRegion(String id, BlockBBox box) {
        this(id, box, EnumSet.allOf(ProtectionFlag.class));
    }

    public ProtectionRegion(String id, BlockBBox box, ProtectionFlag... flags) {
        this(id, box, EnumSet.copyOf(List.of(flags)));
    }

    public ProtectionRegion(String id, BlockBBox box, Set<ProtectionFlag> flags) {
        this.id = id;
        this.box = box.clone();
        this.flags = EnumSet.copyOf(flags);
    }

    public final boolean check(Location location, ProtectionFlag flag) {
        return this.flags.contains(flag) && this.box.containsLoc(location);
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

    public final BlockBBox getBox() {
        return this.box.clone();
    }

    public final Set<ProtectionFlag> getFlags() {
        return Collections.unmodifiableSet(this.flags);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ProtectionRegion that)) return false;
        return this.box.equals(that.box);
    }

    @Override
    public int hashCode() {
        return this.box.hashCode();
    }

    @Override
    public String toString() {
        return "ProtectionRegion{id='" + this.id + '\'' + ", box=" + this.box + ", flags=" + this.flags + '}';
    }
}
