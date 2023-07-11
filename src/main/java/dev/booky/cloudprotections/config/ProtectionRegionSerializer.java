package dev.booky.cloudprotections.config;
// Created by booky10 in CraftAttack (21:22 13.11.22)

import dev.booky.cloudprotections.region.ProtectionFlag;
import dev.booky.cloudprotections.region.ProtectionRegion;
import dev.booky.cloudprotections.region.area.IProtectionArea;
import org.apache.commons.lang3.RandomStringUtils;
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

        IProtectionArea area = Objects.requireNonNull(node.get(IProtectionArea.class));
        String id = node.node("id").getString(RandomStringUtils.randomAlphanumeric(8));

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

        return new ProtectionRegion(id, area, flags);
    }

    @Override
    public void serialize(Type type, @Nullable ProtectionRegion obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
            return;
        }

        node.set(obj.getArea());
        node.node("id").set(obj.getId());

        // why the fuck does this EnumSerializer not work when serializing this?
        // this thing has worked in every other project I have used this in (more than 1), so why not here?
        node.node("flags").set(obj.getFlags().stream().map(ProtectionFlag::name).toList());
    }
}
