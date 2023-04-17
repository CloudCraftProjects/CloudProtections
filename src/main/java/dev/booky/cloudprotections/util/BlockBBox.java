package dev.booky.cloudprotections.util;
// Created by booky10 in CraftAttack (13:27 05.10.22)

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Objects;

public final class BlockBBox extends BoundingBox {

    private final WeakReference<World> world;

    public BlockBBox(Location corner1, Location corner2) {
        this(corner1.getWorld(), corner1.getX(), corner1.getY(), corner1.getZ(),
                corner2.getX(), corner2.getY(), corner2.getZ());
        if (corner1.getWorld() != corner2.getWorld()) {
            throw new IllegalStateException("Worlds mismatch between corners: corner1=" + corner1 + ", corner2=" + corner2);
        }
    }

    public BlockBBox(World world, Vector corner1, Vector corner2) {
        this(world, corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(), corner2.getZ());
    }

    public BlockBBox(World world, double x1, double y1, double z1, double x2, double y2, double z2) {
        this(world, NumberConversions.floor(x1), NumberConversions.floor(y1), NumberConversions.floor(z1),
                NumberConversions.ceil(x2), NumberConversions.ceil(y2), NumberConversions.ceil(z2));
    }

    public BlockBBox(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        super(x1, y1, z1, x2, y2, z2);
        this.world = new WeakReference<>(world);
    }

    public boolean contains(Location location) {
        if (this.getWorld() != location.getWorld()) {
            return false;
        }
        return super.contains(location.getX(), location.getY(), location.getZ());
    }

    public int getBlockMinX() {
        return NumberConversions.floor(super.getMinX());
    }

    public int getBlockMinY() {
        return NumberConversions.floor(super.getMinY());
    }

    public int getBlockMinZ() {
        return NumberConversions.floor(super.getMinZ());
    }

    public int getBlockMaxX() {
        return NumberConversions.floor(super.getMaxX());
    }

    public int getBlockMaxY() {
        return NumberConversions.floor(super.getMaxY());
    }

    public int getBlockMaxZ() {
        return NumberConversions.floor(super.getMaxZ());
    }

    public Block getMinBlock() {
        return this.getWorld().getBlockAt(this.getBlockMinX(), this.getBlockMinY(), this.getBlockMinZ());
    }

    public Block getMaxBlock() {
        return this.getWorld().getBlockAt(this.getBlockMaxX(), this.getBlockMaxY(), this.getBlockMaxZ());
    }

    public Location getMinPos() {
        return new Location(this.getWorld(), super.getMinX(), super.getMinY(), super.getMinZ());
    }

    public Location getMaxPos() {
        return new Location(this.getWorld(), super.getMaxX(), super.getMaxY(), super.getMaxZ());
    }

    @Override
    public @NotNull BlockVector getMin() {
        return super.getMin().toBlockVector();
    }

    @Override
    public @NotNull BlockVector getMax() {
        return super.getMax().toBlockVector();
    }

    public World getWorld() {
        return Objects.requireNonNull(this.world.get(), "World has been unloaded");
    }

    @Override
    public @NotNull BlockBBox clone() {
        return (BlockBBox) super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BlockBBox that)) return false;
        if (!super.equals(obj)) return false;
        return this.getWorld().equals(that.getWorld());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.getWorld().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BlockBBox{world=" + this.getWorld() + ",parent={" + super.toString() + "}}";
    }
}
