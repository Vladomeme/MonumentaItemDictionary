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
}
