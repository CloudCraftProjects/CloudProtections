package dev.booky.cloudprotections.config;
// Created by booky10 in CloudProtections (14:07 11.07.23)

import dev.booky.cloudcore.util.BlockBBox;
import dev.booky.cloudprotections.region.area.BoxProtectionArea;
import dev.booky.cloudprotections.region.area.IProtectionArea;
import dev.booky.cloudprotections.region.area.SphericalProtectionArea;
import org.bukkit.block.Block;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public final class ProtectionAreaSerializer implements TypeSerializer<IProtectionArea> {

    public static final ProtectionAreaSerializer INSTANCE = new ProtectionAreaSerializer();

    private ProtectionAreaSerializer() {
    }

    @Override
    public IProtectionArea deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.virtual()) {
            return null;
        }

        if (node.hasChild("radius")) {
            // no box has some kind of radius, this is spherical area
            Block centerBlock = Objects.requireNonNull(node.node("center").get(Block.class));
            double radius = node.node("radius").getDouble();
            return new SphericalProtectionArea(centerBlock, radius);
        }

        BlockBBox box = Objects.requireNonNull(node.get(BlockBBox.class));
        return new BoxProtectionArea(box);
    }

    @Override
    public void serialize(Type type, @Nullable IProtectionArea obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
            return;
        }

        if (obj instanceof SphericalProtectionArea area) {
            node.node("center").set(area.getCenterBlock());
            node.node("radius").set(area.getRadius());
            return;
        }

        if (obj instanceof BoxProtectionArea area) {
            node.set(area.getBox());
            return;
        }

        throw new UnsupportedOperationException("Unsupported protection area implementation: " + obj);
    }
}
