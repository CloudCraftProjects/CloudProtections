package dev.booky.cloudprotections.region.exclusions;
// Created by booky10 in CloudProtections (16:02 11.07.23)

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public final class UniqueIdsExclusion implements IProtectionExclusion {

    private final Collection<UUID> playerIds;

    public UniqueIdsExclusion(Collection<UUID> playerIds) {
        this.playerIds = Set.copyOf(playerIds);
    }

    @Override
    public boolean isExcluded(Player player) {
        return this.playerIds.contains(player.getUniqueId());
    }

    public Collection<UUID> getPlayerIds() {
        return this.playerIds;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UniqueIdsExclusion that)) return false;
        return this.playerIds.equals(that.playerIds);
    }

    @Override
    public int hashCode() {
        return this.playerIds.hashCode();
    }

    @Override
    public String toString() {
        return "UniqueIdExclusion{playerIds=" + this.playerIds + '}';
    }
}
