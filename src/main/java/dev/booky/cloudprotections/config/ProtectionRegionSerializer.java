package dev.booky.cloudprotections.config;
// Created by booky10 in CraftAttack (21:22 13.11.22)

import dev.booky.cloudprotections.region.ProtectionFlag;
import dev.booky.cloudprotections.region.ProtectionRegion;
import dev.booky.cloudprotections.region.area.IProtectionArea;
import dev.booky.cloudprotections.region.exclusions.IProtectionExclusion;
import org.apache.commons.lang3.RandomStringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ProtectionRegionSerializer implements TypeSerializer<ProtectionRegion> {

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

        ConfigurationNode exclusionsNode = node.node("exclusions");
        List<IProtectionExclusion> exclusions = exclusionsNode.getList(IProtectionExclusion.class, List::of);

        ConfigurationNode flagsNode = node.node("flags");
        List<ProtectionFlag> flags = flagsNode.getList(ProtectionFlag.class, List::of);

        return new ProtectionRegion(id, area, Set.copyOf(exclusions), Set.copyOf(flags));
    }

    @Override
    public void serialize(Type type, @Nullable ProtectionRegion obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
            return;
        }

        node.set(obj.getArea());
        node.node("id").set(obj.getId());

        node.node("exclusions").setList(IProtectionExclusion.class, List.copyOf(obj.getExclusions()));
        node.node("flags").setList(ProtectionFlag.class, List.copyOf(obj.getFlags()));
    }
}
