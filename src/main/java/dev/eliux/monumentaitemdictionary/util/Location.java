package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.Mid;

public record Location(String displayName, int color, int loreColor) {

    public Location(String displayName, int color) {
        this(displayName, color, loreColor(color));
    }

    public static int colorOf(String locationId) {
        Location location = Mid.controller.locationData.get(locationId);
        return location != null ? location.color : 0xFFFFFF;
    }

    public static int loreColorOf(String locationId) {
        Location location = Mid.controller.locationData.get(locationId);
        return location != null ? location.loreColor : 0x555555;
    }

    private static int loreColor(int color) {
        int h1r = ItemColors.TEXT_COLOR / (256 * 256);
        int h1g = (ItemColors.TEXT_COLOR / 256) % 256;
        int h1b = ItemColors.TEXT_COLOR % 256;

        int h2r = color / (256 * 256);
        int h2g = (color / 256) % 256;
        int h2b = color % 256;

        int or = (int) (h1r * 0.67) + (int) (h2r * (1 - 0.67));
        int og = (int) (h1g * 0.67) + (int) (h2g * (1 - 0.67));
        int ob = (int) (h1b * 0.67) + (int) (h2b * (1 - 0.67));

        return (or * (256 * 256)) + (og * 256) + (ob);
    }
}
