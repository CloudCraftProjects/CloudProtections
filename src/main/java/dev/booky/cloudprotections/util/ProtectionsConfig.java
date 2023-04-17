package dev.booky.cloudprotections.util;
// Created by booky10 in CloudProtections (02:02 01.04.23)

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.LinkedHashSet;
import java.util.Set;

// Can't be final because of object mapping
@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public class ProtectionsConfig {

    private Set<ProtectionRegion> regions = new LinkedHashSet<>();

    private ProtectionsConfig() {
    }
    public Set<ProtectionRegion> getRegions() {
        return this.regions;
    }
}
