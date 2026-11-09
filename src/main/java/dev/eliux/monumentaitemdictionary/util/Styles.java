package dev.eliux.monumentaitemdictionary.util;

import net.minecraft.text.Style;

public class Styles {
    public static final int TEXT_COLOR = 0x555555;

    public static final Style BOLD = Style.EMPTY.withBold(true);
    public static final Style OBFUSCATED = Style.EMPTY.withObfuscated(true);
    public static final Style BOLD_UNDERLINE = Style.EMPTY.withBold(true).withUnderline(true);

    public static final Style WHITE = Style.EMPTY.withColor(0xFFFFFF);
    public static final Style RED = Style.EMPTY.withColor(0xFF0000);
    public static final Style GREEN = Style.EMPTY.withColor(0x00FF00);
    public static final Style BLUE = Style.EMPTY.withColor(0x00FFFF);
    public static final Style YELLOW = Style.EMPTY.withColor(0xFFFF00);
    public static final Style LIGHT_GRAY = Style.EMPTY.withColor(0xAAAAAA);
    public static final Style PURPLE = Style.EMPTY.withColor(0xAA00AA);

    public static final Style TEXT = Style.EMPTY.withColor(TEXT_COLOR);
    public static final Style BOLD_TEXT = Style.EMPTY.withColor(TEXT_COLOR).withBold(true);
    public static final Style RESET_BOLD_TEXT = Style.EMPTY.withColor(TEXT_COLOR).withBold(false);

    public static final Style CHARM_POWER = Style.EMPTY.withColor(0xFFFA75);
    public static final Style GITHUB_LINK = Style.EMPTY.withUnderline(true).withColor(0x5555FF);
    public static final Style MASTERWORK = Style.EMPTY.withColor(0xFFB43E);
    public static final Style FISH = Style.EMPTY.withColor(0x1DCC9A);
}
