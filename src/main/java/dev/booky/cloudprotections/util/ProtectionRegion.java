package dev.booky.cloudprotections.util;
// Created by booky10 in CraftAttack (21:22 13.11.22)

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class ProtectionRegion {

    private final BlockBBox box;
    private final Set<ProtectionFlag> flags;

    public ProtectionRegion(BlockBBox box) {
        this(box, EnumSet.allOf(ProtectionFlag.class));
    }

    public ProtectionRegion(BlockBBox box, ProtectionFlag... flags) {
        this(box, EnumSet.copyOf(List.of(flags)));
    }

    public ProtectionRegion(BlockBBox box, Set<ProtectionFlag> flags) {
        this.box = box.clone();
        this.flags = EnumSet.copyOf(flags);
    }

    public boolean addFlag(ProtectionFlag flag) {
        return this.flags.add(flag);
    }

    public boolean removeFlag(ProtectionFlag flag) {
        return this.flags.remove(flag);
    }

    public boolean hasFlag(ProtectionFlag flag) {
        return this.flags.contains(flag);
    }

    public BlockBBox getBox() {
        return this.box.clone();
    }

    public Set<ProtectionFlag> getFlags() {
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
        return "ProtectionRegion{box=" + this.box + ", flags=" + this.flags + '}';
    }
}
