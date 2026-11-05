package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.Mid;

public record Location(String displayName, int color) {

    public static int colorOf(String locationId) {
        Location location = Mid.controller.locationData.get(locationId);
        return location != null ? location.color() : 0xFFFFFF;
    }

}
