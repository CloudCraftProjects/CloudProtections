package dev.booky.cloudprotections.config;
// Created by booky10 in CraftAttack (21:22 13.11.22)

import dev.booky.cloudcore.util.BlockBBox;
import dev.booky.cloudprotections.util.ProtectionFlag;
import dev.booky.cloudprotections.util.ProtectionRegion;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ProtectionRegionSerializer implements TypeSerializer<ProtectionRegion> {

    public static final ProtectionRegionSerializer INSTANCE = new ProtectionRegionSerializer();

    private ProtectionRegionSerializer() {
    }

    @Override
    public ProtectionRegion deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.virtual()) {
            return null;
        }

        BlockBBox box = Objects.requireNonNull(node.get(BlockBBox.class));

        ConfigurationNode flagsNode = node.node("flags");
        Set<ProtectionFlag> flags;
        if (flagsNode.virtual()) {
            flags = EnumSet.allOf(ProtectionFlag.class);
        } else {
            List<ProtectionFlag> list = flagsNode.getList(ProtectionFlag.class, List::of);
            if (list.isEmpty()) {
                // apparently doesn't get handled by EnumSet#copyOf...
                flags = EnumSet.noneOf(ProtectionFlag.class);
            } else {
                flags = EnumSet.copyOf(list);
            }
        }

        return new ProtectionRegion(box, flags);
    }

    @Override
    public void serialize(Type type, @Nullable ProtectionRegion obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
            return;
        }

        node.set(obj.getBox());

        // why the fuck does this EnumSerializer not work when serializing this?
        // this thing has worked in every other project I have used this in (more than 1), so why not here?
        node.node("flags").set(obj.getFlags().stream().map(ProtectionFlag::name).toList());
    }
}
