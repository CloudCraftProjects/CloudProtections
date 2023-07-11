package dev.booky.cloudprotections.region.area;
// Created by booky10 in CloudProtections (13:56 11.07.23)

import org.bukkit.Location;
import org.bukkit.block.Block;

public interface IProtectionArea {

    boolean contains(Block block);

    Location getCenterLocation();
}
