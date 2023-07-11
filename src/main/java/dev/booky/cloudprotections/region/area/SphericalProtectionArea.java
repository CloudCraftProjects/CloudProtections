package dev.booky.cloudprotections.region.area;
// Created by booky10 in CloudProtections (13:59 11.07.23)

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class SphericalProtectionArea implements IProtectionArea {

    private final World world;
    private final BlockPosition centerBlock;
    private final double radius;
    private transient final double radiusSq;

    public SphericalProtectionArea(Block centerBlock, double radius) {
        this(centerBlock.getWorld(), Position.block(centerBlock.getX(), centerBlock.getY(), centerBlock.getZ()), radius);
    }

    public SphericalProtectionArea(World world, BlockPosition centerBlock, double radius) {
        this.world = world;
        this.centerBlock = centerBlock;
        this.radius = radius;
        this.radiusSq = radius * radius;
    }

    @Override
    public boolean contains(Block block) {
        if (this.world != block.getWorld()) {
            return false;
        }
        BlockPosition centerBlock = this.centerBlock;

        double diffX = centerBlock.blockX() - block.getX();
        double diffY = centerBlock.blockY() - block.getY();
        double diffZ = centerBlock.blockZ() - block.getZ();
        double distSq = diffX * diffX + diffY * diffY + diffZ * diffZ;
        return distSq <= this.radiusSq;
    }

    public Block getCenterBlock() {
        BlockPosition centerBlock = this.centerBlock;
        return this.world.getBlockAt(centerBlock.blockX(), centerBlock.blockY(), centerBlock.blockZ());
    }

    public World getWorld() {
        return this.world;
    }

    public BlockPosition getCenterBlockPos() {
        return this.centerBlock;
    }

    public double getRadius() {
        return this.radius;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SphericalProtectionArea that)) return false;
        if (Double.compare(that.radius, this.radius) != 0) return false;
        if (!this.world.equals(that.world)) return false;
        return this.centerBlock.equals(that.centerBlock);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = this.world.hashCode();
        result = 31 * result + this.centerBlock.hashCode();
        temp = Double.doubleToLongBits(this.radius);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
