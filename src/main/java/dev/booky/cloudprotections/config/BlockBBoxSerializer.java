package dev.booky.cloudprotections.config;
// Created by booky10 in CraftAttack (13:15 05.10.22)

import dev.booky.cloudprotections.util.BlockBBox;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public class BlockBBoxSerializer implements TypeSerializer<BlockBBox> {

    public static final BlockBBoxSerializer INSTANCE = new BlockBBoxSerializer();

    private BlockBBoxSerializer() {
    }

    @Override
    public BlockBBox deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.virtual()) {
            return null;
        }

        World world = node.node("dimension").get(World.class);
        Objects.requireNonNull(world, "No dimension found for bounding box");

        BlockVector corner1 = node.node("corner1").get(BlockVector.class);
        Objects.requireNonNull(corner1, "No first corner found for bounding box");

        BlockVector corner2 = node.node("corner2").get(BlockVector.class);
        Objects.requireNonNull(corner2, "No second corner found for bounding box");

        return new BlockBBox(world, corner1, corner2);
    }

    @Override
    public void serialize(Type type, @Nullable BlockBBox obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
            return;
        }

        node.node("dimension").set(obj.getWorld());
        node.node("corner1").set(obj.getMin());
        node.node("corner2").set(obj.getMax());
    }
}
