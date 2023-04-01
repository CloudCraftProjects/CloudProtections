package dev.booky.cloudprotections.util;
// Created by booky10 in CloudProtections (02:02 01.04.23)

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

// Can't be final because of object mapping
@SuppressWarnings("FieldMayBeFinal")
@ConfigSerializable
public class ProtectionsConfig {

    private ProtectionsConfig() {
    }
}
