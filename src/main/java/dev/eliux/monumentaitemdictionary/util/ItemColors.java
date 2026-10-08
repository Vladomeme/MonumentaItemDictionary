package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.Mid;

public class ItemColors {

    public static final int DEFAULT_COLOR = 0xFFFFFF;

    public static final int TEXT_COLOR = 0x555555;
    public static final int TEXT_ENCHANT_COLOR = 0xAAAAAA;
    public static final int TEXT_STAT_COLOR = 0x5555FF;
    public static final int TEXT_BASE_STAT_COLOR = 0x00AA00;
    public static final int TEXT_DEFENSE_COLOR = 0x33CCFF;
    public static final int TEXT_NEGATIVE_COLOR = 0xFF5555;
    public static final int TEXT_MASTERWORK_COLOR = 0xFFB43E;
    public static final int TEXT_CHARM_POWER_COLOR = 0xFFFA75;
    @SuppressWarnings("unused")
    public static final int TEXT_LORE_COLOR = 0xAA00AA;
    public static final int TEXT_POSITIVE_CHARM_COLOR = 0x4AC2E5;
    public static final int TEXT_NEGATIVE_CHARM_COLOR = 0xD02E28;

    public static final int LEGENDARY_COLOR = 0xFFD700;
    public static final int EPIC_COLOR = 0xB314E3;
    public static final int ARTIFACT_COLOR = 0xD02E28;
    public static final int RARE_COLOR = 0x4AC2E5;
    public static final int BASE_COLOR = 0xFFFA75;
    public static final int UNIQUE_COLOR = 0xC8A2C8;
    public static final int PATRON_COLOR = 0x82DB17;
    public static final int EVENT_COLOR = 0x7FFFD4;
    public static final int EVENT_CURRENCY_COLOR = 0xDCAE32;
    public static final int CURRENCY_COLOR = 0xDCAE32;
    public static final int TROPHY_COLOR = 0xCAFFFD;
    public static final int KEY_COLOR = 0x47B6B5;
    public static final int LEGACY_COLOR = 0xEEE6D6;
    public static final int OBFUSCATED_COLOR = 0x5D2D87;
    public static final int FISH_COLOR = 0x1DCC9A;

    public static final int UNCOMMON_COLOR = 0xC0C0C0;
    public static final int TIER5_COLOR = 0x555555;
    public static final int TIER4_COLOR = 0x555555;
    public static final int TIER3_COLOR = 0x555555;
    public static final int TIER2_COLOR = 0x555555;
    public static final int TIER1_COLOR = 0x555555;
    public static final int TIER0_COLOR = 0x555555;

    public static final int ALCHEMIST_COLOR = 0x81D434;
    public static final int WARRIOR_COLOR = 0xD32818;
    public static final int CLERIC_COLOR = 0xFFC644;
    public static final int ROGUE_COLOR = 0x36393D;
    public static final int MAGE_COLOR = 0xA129D3;
    public static final int SCOUT_COLOR = 0x59B4EB;
    public static final int WARLOCK_COLOR = 0xF0489E;
    public static final int SHAMAN_COLOR = 0x009900;
    public static final int GENERALIST_COLOR = 0x9F8F91;

    public static int getColorForTier(String itemTier) {
        return switch (itemTier) {
            case "Legendary" -> LEGENDARY_COLOR;
            case "Epic" -> EPIC_COLOR;
            case "Artifact" -> ARTIFACT_COLOR;
            case "Rare" -> RARE_COLOR;
            case "Base" -> BASE_COLOR;
            case "Unique" -> UNIQUE_COLOR;
            case "Event" -> EVENT_COLOR;
            case "Patron" -> PATRON_COLOR;
            case "Event Currency" -> CURRENCY_COLOR;
            case "Currency" -> EVENT_CURRENCY_COLOR;
            case "Trophy" -> TROPHY_COLOR;
            case "Key" -> KEY_COLOR;
            case "Fish" -> FISH_COLOR;
            case "Legacy" -> LEGACY_COLOR;
            case "Obfuscated" -> OBFUSCATED_COLOR;
            case "Uncommon" -> UNCOMMON_COLOR;
            case "Tier 5" -> TIER5_COLOR;
            case "Tier 4" -> TIER4_COLOR;
            case "Tier 3" -> TIER3_COLOR;
            case "Tier 2" -> TIER2_COLOR;
            case "Tier 1" -> TIER1_COLOR;
            case "Tier 0" -> TIER0_COLOR;
            default -> DEFAULT_COLOR;
        };
    }

    public static int getColorForClass(String charmClass) {
        return switch (charmClass) {
            case "Alchemist" -> ALCHEMIST_COLOR;
            case "Warrior" -> WARRIOR_COLOR;
            case "Cleric" -> CLERIC_COLOR;
            case "Rogue" -> ROGUE_COLOR;
            case "Mage" -> MAGE_COLOR;
            case "Scout" -> SCOUT_COLOR;
            case "Warlock" -> WARLOCK_COLOR;
            case "Shaman" -> SHAMAN_COLOR;
            case "Generalist" -> GENERALIST_COLOR;
            default -> DEFAULT_COLOR;
        };
    }

    public static int getColorForCharmStat(CharmStat charmStat) {
        boolean positive = charmStat.statValue() >= 0;
        boolean inverted = (charmStat.statNameFull().contains("cooldown")
                && !(charmStat.statNameFull().contains("cooldown_reduction")
                    || charmStat.statNameFull().contains("cooldown_cap")
                    || charmStat.statNameFull().contains("cooldown_recharge_rate")
                    || charmStat.statNameFull().contains("cooldown_refund") ))
                || charmStat.statNameFull().contains("self_damage")
                || charmStat.statNameFull().contains("requirement")
                || charmStat.statNameFull().contains("penalty")
                || charmStat.statNameFull().contains("delay")
                || charmStat.statNameFull().contains("price")
                || charmStat.statNameFull().contains("received_damage")
                || charmStat.statNameFull().contains("cost")
                || charmStat.statNameFull().contains("priming_duration")
                /* Hardcode Affected Charm Note
                    Silver Codex, Focused/Greater/Lesser Executioner's Charm: Coup de Grace health threshold
                    Psychosis: Locked Amplifying Hex max debuffs */
                || (charmStat.statNameFull().contains("threshold") && !(charmStat.statNameFull().contains("coup_de_grace")))
                || (charmStat.statLocked() && charmStat.statNameFull().equals("amplifying_hex_max_debuffs_flat"));
        return (positive ^ inverted) ? TEXT_POSITIVE_CHARM_COLOR : TEXT_NEGATIVE_CHARM_COLOR;
    }

    public static int getColorForLocation(String locationId) {
        Location location = Mid.controller.locationData.get(locationId);
        return location != null ? location.color() : DEFAULT_COLOR;
    }

    public static int getColorForStat(String itemStat, double value) {
        if (value < 0 || ItemFormatter.isCurseEnchant(itemStat))
            return TEXT_NEGATIVE_COLOR;
        if (itemStat.equals("armor") || itemStat.equals("agility"))
            return TEXT_DEFENSE_COLOR;
        if (ItemFormatter.isBaseStat(itemStat))
            return TEXT_BASE_STAT_COLOR;
        if (ItemFormatter.isStat(itemStat))
            return TEXT_STAT_COLOR;

        return TEXT_ENCHANT_COLOR;
    }

    public static int mixHexes(int h1, int h2, double p) {
        int h1r = h1 / (256 * 256);
        int h1g = (h1 / 256) % 256;
        int h1b = h1 % 256;

        int h2r = h2 / (256 * 256);
        int h2g = (h2 / 256) % 256;
        int h2b = h2 % 256;

        int or = (int) (h1r * p) + (int) (h2r * (1 - p));
        int og = (int) (h1g * p) + (int) (h2g * (1 - p));
        int ob = (int) (h1b * p) + (int) (h2b * (1 - p));

        return (or * (256 * 256)) + (og * 256) + (ob);
    }
}
