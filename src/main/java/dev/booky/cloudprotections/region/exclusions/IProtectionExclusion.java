package dev.booky.cloudprotections.region.exclusions;
// Created by booky10 in CloudProtections (15:28 11.07.23)

import org.bukkit.entity.Player;

public interface IProtectionExclusion {

    boolean isExcluded(Player player);
}
