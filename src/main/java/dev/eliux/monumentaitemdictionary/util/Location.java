package dev.eliux.monumentaitemdictionary.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.Objects;

public record Location(String displayName, Text text, int color, int loreColor) {

    public static final Location EMPTY = new Location("", Text.empty(), 0xFFFFFF, 0x555555);

    public Location(String key, String displayName, int color) {
        this(displayName, locationText(displayName, key, color), color, loreColor(color));
    }

    private static int loreColor(int color) {
        int h1r = Styles.TEXT_COLOR / (256 * 256);
        int h1g = (Styles.TEXT_COLOR / 256) % 256;
        int h1b = Styles.TEXT_COLOR % 256;

        int h2r = color / (256 * 256);
        int h2g = (color / 256) % 256;
        int h2b = color % 256;

        int or = (int) (h1r * 0.67) + (int) (h2r * (1 - 0.67));
        int og = (int) (h1g * 0.67) + (int) (h2g * (1 - 0.67));
        int ob = (int) (h1b * 0.67) + (int) (h2b * (1 - 0.67));

        return (or * (256 * 256)) + (og * 256) + (ob);
    }

    private static Text locationText(String displayName, String key, int color) {
        MutableText text;
        if (key.equals("twisted"))
            text = Text.literal("Twisted ").append(Text.literal("XXXXXXXX").setStyle(Styles.OBFUSCATED));
        else text = Text.literal(displayName);

        return text.setStyle(Style.EMPTY.withColor(color));
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Location location)) return false;
        if (this == object) return true;
        return color == location.color && loreColor == location.loreColor && Objects.equals(displayName, location.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, color, loreColor);
    }
}
