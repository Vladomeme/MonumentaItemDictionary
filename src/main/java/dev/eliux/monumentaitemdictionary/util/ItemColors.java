package dev.eliux.monumentaitemdictionary.util;

public class ItemColors {

    public static final int TEXT_COLOR = 0x555555;
    static final int TEXT_ENCHANT_COLOR = 0xAAAAAA;
    static final int TEXT_STAT_COLOR = 0x5555FF;
    static final int TEXT_BASE_STAT_COLOR = 0x00AA00;
    static final int TEXT_DEFENSE_COLOR = 0x33CCFF;
    static final int TEXT_NEGATIVE_COLOR = 0xFF5555;
    static final int TEXT_MASTERWORK_COLOR = 0xFFB43E;
    static final int TEXT_CHARM_POWER_COLOR = 0xFFFA75;
    static final int TEXT_POSITIVE_CHARM_COLOR = 0x4AC2E5;
    static final int TEXT_NEGATIVE_CHARM_COLOR = 0xD02E28;

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

    public static int getColorForStat(String itemStat, float value) {
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
