package dev.booky.cloudprotections.region.area;
// Created by booky10 in CloudProtections (13:58 11.07.23)

import dev.booky.cloudcore.util.BlockBBox;
import org.bukkit.Location;
import org.bukkit.block.Block;

public final class BoxProtectionArea implements IProtectionArea{

    private final BlockBBox box;

    public BoxProtectionArea(BlockBBox box) {
        this.box = box;
    }

    @Override
    public boolean contains(Block block) {
        return this.box.contains(block);
    }

    @Override
    public Location getCenterLocation() {
        return this.box.getCenterLocation();
    }

    public BlockBBox getBox() {
        return this.box;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BoxProtectionArea that)) return false;
        return this.box.equals(that.box);
    }

    @Override
    public int hashCode() {
        return this.box.hashCode();
    }

    @Override
    public String toString() {
        return "BoxProtectionArea{box=" + this.box + '}';
    }
}
