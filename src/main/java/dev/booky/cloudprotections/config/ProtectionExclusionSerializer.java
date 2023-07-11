package dev.booky.cloudprotections.config;
// Created by booky10 in CloudProtections (16:15 11.07.23)

import dev.booky.cloudprotections.region.exclusions.IProtectionExclusion;
import dev.booky.cloudprotections.region.exclusions.UniqueIdExclusion;
import dev.booky.cloudprotections.region.exclusions.UniqueIdsExclusion;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public final class ProtectionExclusionSerializer implements TypeSerializer<IProtectionExclusion> {

    public static final ProtectionExclusionSerializer INSTANCE = new ProtectionExclusionSerializer();

    private ProtectionExclusionSerializer() {
    }

    @Override
    public IProtectionExclusion deserialize(Type type, ConfigurationNode node) throws SerializationException {
        List<UUID> uniqueIds = node.getList(UUID.class, List::of);
        if (uniqueIds.isEmpty()) {
            return null;
        }
        if (uniqueIds.size() > 1) {
            return new UniqueIdsExclusion(uniqueIds);
        }

        UUID uniqueId = uniqueIds.get(0);
        return new UniqueIdExclusion(uniqueId);
    }

    @Override
    public void serialize(Type type, @Nullable IProtectionExclusion obj, ConfigurationNode node) throws SerializationException {
        if (obj instanceof UniqueIdsExclusion) {
            node.set(((UniqueIdsExclusion) obj).getPlayerIds());
            return;
        }
        if (obj instanceof UniqueIdExclusion) {
            node.setList(UUID.class, List.of(((UniqueIdExclusion) obj).getPlayerId()));
            return;
        }
        if (obj == null) {
            node.set(null);
            return;
        }

        throw new UnsupportedOperationException("Unsupported exclusion implementation: " + obj);
    }
}
