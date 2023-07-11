package dev.booky.cloudprotections.region.exclusions;
// Created by booky10 in CloudProtections (15:32 11.07.23)

import org.bukkit.entity.Player;

import java.util.UUID;

public final class UniqueIdExclusion implements IProtectionExclusion {

    private final UUID playerId;

    public UniqueIdExclusion(UUID playerId) {
        this.playerId = playerId;
    }

    @Override
    public boolean isExcluded(Player player) {
        return player.getUniqueId().equals(this.playerId);
    }

    public UUID getPlayerId() {
        return this.playerId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UniqueIdExclusion that)) return false;
        return this.playerId.equals(that.playerId);
    }

    @Override
    public int hashCode() {
        return this.playerId.hashCode();
    }

    @Override
    public String toString() {
        return "UniqueIdExclusion{playerId=" + this.playerId + '}';
    }
}
