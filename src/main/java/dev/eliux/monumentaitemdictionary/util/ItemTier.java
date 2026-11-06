package dev.eliux.monumentaitemdictionary.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public enum ItemTier {
    //order matters, ordinal used for sorting (most to least interesting)
    //               Display name       Color     Bold   Underline
    LEGENDARY       ("Legendary",       0xFFD700, true,  true),
    EPIC            ("Epic",            0xB314E3, true,  true),
    ARTIFACT        ("Artifact",        0xD02E28, true,  false),
    RARE            ("Rare",            0x4AC2E5, true,  false),
    UNIQUE          ("Unique",          0xC8A2C8, true,  false),
    UNCOMMON        ("Uncommon",        0xC0C0C0, true,  false),
    PATRON          ("Patron",          0x82DB17, true,  false),
    EVENT           ("Event",           0x7FFFD4, false, false),
    TIER_5          ("Tier 5",          0x555555, false, false),
    TIER_4          ("Tier 4",          0x555555, false, false),
    TIER_3          ("Tier 3",          0x555555, false, false),
    TIER_2          ("Tier 2",          0x555555, false, false),
    TIER_1          ("Tier 1",          0x555555, false, false),
    TIER_0          ("Tier 0",          0x555555, false, false),
    BASE            ("Base",            0xFFFA75, false, false),
    OBFUSCATED      ("Obfuscated",      0x5D2D87, false, false),
    CURRENCY        ("Currency",        0xDCAE32, true,  false),
    EVENT_CURRENCY  ("Event Currency",  0xDCAE32, true,  false),
    TROPHY          ("Trophy",          0xCAFFFD, true,  false),
    KEY             ("Key",             0x47B6B5, true,  false),
    FISH            ("Fish",            0x1DCC9A, false, false),
    LEGACY          ("Legacy",          0xEEE6D6, false, false),
    DEFAULT         ("No Tier",         0xFFFFFF, false, false);
    
    public final String name;
    public final int color;
    public final boolean isBold;
    public final boolean isUnderlined;

    public final MutableText formattedName;
    public final MutableText formattedCharmName;

    public static final int MAX_FISH_TIER = 5;
    
    ItemTier(String name, int color, boolean isBold, boolean isUnderlined) {
        this.name = name;
        this.color = color;
        this.isBold = isBold;
        this.isUnderlined = isUnderlined;

        this.formattedName = Text.literal(name).setStyle(Style.EMPTY
                .withColor(color)
                .withBold(isUnderlined) //not a typo
                .withObfuscated(name.equals("Obfuscated")));

        this.formattedCharmName = Text.literal((name.equals("Base") ? "" : name + " ") + "Charm").setStyle(Style.EMPTY
                .withColor(color)
                .withBold(isUnderlined)); //not a typo
    }
    
    public static ItemTier of(String name) {
        return switch (formatTierPrimitive(name)) {
            case "Tier 0" -> TIER_0;
            case "Tier 1" -> TIER_1;
            case "Tier 2" -> TIER_2;
            case "Tier 3" -> TIER_3;
            case "Tier 4" -> TIER_4;
            case "Tier 5" -> TIER_5;
            case "Uncommon" -> UNCOMMON;
            case "Unique" -> UNIQUE;
            case "Rare" -> RARE;
            case "Artifact" -> ARTIFACT;
            case "Epic" -> EPIC;
            case "Legendary" -> LEGENDARY;
            case "Base" -> BASE;
            case "Event" -> EVENT;
            case "Patron" -> PATRON;
            case "Currency" -> CURRENCY;
            case "Event Currency" -> EVENT_CURRENCY;
            case "Trophy" -> TROPHY;
            case "Key" -> KEY;
            case "Fish" -> FISH;
            case "Legacy" -> LEGACY;
            case "Obfuscated" -> OBFUSCATED;
            default -> DEFAULT;
        };
    }

    // Fix for Event Currency tier (and maybe others in the future) being formatted as "Event_currency"
    public static String formatTierPrimitive(String tier) {
        if (tier.indexOf('_') == -1) return tier;

        StringBuilder builder = new StringBuilder();
        char[] chars = tier.toCharArray();

        for (int i = 0; i != chars.length; i++) {
            if (chars[i] == '_') {
                builder.append(' ');
                i++;
                builder.append(chars[i] < 'a' ? chars[i] : chars[i] - 32);
            }
        }
        return builder.toString();
    }

    public int getMasterwork() {
        return switch (this) {
            case RARE, ARTIFACT -> 4;
            case EPIC -> 6;
            default -> 0;
        };
    }
}
