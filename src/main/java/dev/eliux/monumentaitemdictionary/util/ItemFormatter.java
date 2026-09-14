package dev.eliux.monumentaitemdictionary.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class ItemFormatter {
    public static String[] modifiableSkills = {"alchemist_potions", "gruesome_alchemy", "iron_tincture", "volatile_reaction", "energizing_elixir", "brutal_alchemy", "alchemical_artillery", "unstable_amalgam", "bezoar", "taboo", "scorched_earth", "esoteric_enhancements", "panacea", "transmutation_ring", "warding_remedy",
            "crusade", "celestial_blessing", "divine_justice", "heavenly_boon", "illuminate", "cleansing_rain", "hand_of_light", "touch_of_radiance", "sanctified_armor", "unwavering", "holy_javelin", "choir_bells", "luminous_infusion", "rejuvenation", "ethereal_ascension", "hallowed_beam", "keeper_virtue",
            "channeling", "arcane_strike", "frost_nova", "mana_lance", "thunder_step", "elemental_arrows", "magma_shield", "spellshock", "prismatic_shield", "astral_omen", "cosmic_moonblade", "sage's_insight", "blizzard", "elemental_spirits", "starfall",
            "dethroner", "advancing_shadows", "dagger_throw", "escape_death", "smokescreen", "by_my_blade", "dodging", "skirmisher", "vicious_combos", "blade_dance", "deadly_ronde", "wind_walk", "bodkin_blitz", "cloak_and_dagger", "coup_de_grace",
            "versatile", "hunting_companion", "wind_bomb", "steel_trap", "swiftness", "shrapnel_bomb", "sharpshooter", "parting_shot", "volley", "rending_razor", "gale_shot", "tactical_maneuver", "lockdown", "quiver_storm", "predator_strike",
            "totemic_projection", "cleansing_totem", "flame_totem", "lightning_totem", "earthen_tremor", "spiritualism", "interconnected_havoc", "chain_lightning", "ignition_drive", "spiritual_combos", "whirlwind_totem", "totemic_consecration", "spiritcatcher_orbs", "decayed_totem", "devastation",
            "culling", "amplifying_hex", "choleric_flames", "melancholic_lament", "sanguine_harvest", "phlegmatic_resolve", "cursed_wound", "grasping_claws", "soul_rend", "dark_pact", "judgement_chain", "voodoo_bonds", "haunting_shades", "restless_souls", "withering_gaze",
            "formidable", "brute_force", "defensive_line", "riposte", "toughness", "counter_strike", "frenzy", "shield_bash", "weapon_mastery", "bloodlust", "glorious_battle", "meteor_slam", "rampage", "bodyguard", "challenge", "shield_wall",
            "decay", "inferno", "recoil", "jungle's_nourishment", "rage_of_the_keter", "hex_eater", "sapper", "life_drain", "regicide", "quake", "eruption", "smite", "slayer", "duelist", "regeneration", "thunder_aspect"};

    private static final Set<String> boldTiers = new HashSet<>(List.of("Patron", "Key", "Currency", "Trophy", "Uncommon", "Unique", "Rare", "Artifact", "Epic", "Legendary"));

    private static final Set<String> underlineTiers = new HashSet<>(List.of("Epic", "Legendary"));

    private static final Set<String> singleEnchants = new HashSet<>(List.of("gills", "weightless", "rage_of_the_keter", "jungle's_nourishment",
            "unbreakable", "adaptability", "multishot", "intuition", "radiant", "resurrection", "two_handed", "curse_of_irreparability", "mending", "silk_touch",
            "curse_of_corruption", "protection_of_the_depths", "infinity_bow", "infinity_tool", "infinity", "aqua_affinity", "ashes_of_eternity", "void_tether",
            "excavator", "darksight", "material", "alchemical_utensil", "broomstick", "clucking", "oinking", "throwing_knife", "liquid_courage",
            "intoxicating_warmth", "temporal_bender", "curse_of_ephemerality", "instant_drink", "divine_aura", "cumbersome", "persistence", "curse_of_instability",
            "snowy", "spiritshot", "oversized", "kinetic_loading"));

    private static final Set<String> curseEnchants = new HashSet<>(List.of("ineptitude", "curse_of_shrapnel", "curse_of_vanishing", "curse_of_corruption",
            "curse_of_crippling", "curse_of_irreparability", "two_handed", "fire_fragility", "melee_fragility", "blast_fragility", "projectile_fragility",
            "magic_fragility", "ailment_fragility", "curse_of_anemia", "cumbersome", "curse_of_ephemerality", "curse_of_instability", "curse_of_the_veil",
            "oversized", "curse_of_pestilence", "starvation"));

    private static final Set<String> stats = new HashSet<>(List.of("armor", "agility", "spell_power_base", "projectile_speed_percent",
            "knockback_resistance_flat", "attack_damage_base", "thorns_percent", "max_health_flat", "projectile_damage_base", "throw_rate_percent",
            "attack_speed_flat", "projectile_speed_base", "attack_damage_percent", "max_health_percent", "projectile_damage_percent", "magic_damage_percent",
            "attack_speed_percent", "thorns_flat", "speed_flat", "speed_percent", "attack_speed_base", "potion_damage_flat", "potion_radius_flat",
            "throw_rate_base", "potion_recharge_rate_percent"));

    private static final Set<String> baseStats = new HashSet<>(List.of("projectile_damage_base", "projectile_speed_base", "potion_damage_flat", "potion_radius_flat",
            "attack_speed_base", "attack_damage_base", "throw_rate_base", "potion_recharge_rate_percent"));

    private static final Set<String> hiddenStats = new HashSet<>(List.of("noglint", "hideenchants", "hideinfo"));

    public static boolean shouldBold(String inTier) {
        return boldTiers.contains(inTier);
    }

    public static boolean shouldUnderline(String inTier) {
        return underlineTiers.contains(inTier);
    }

    public static String formatRegion(String inRegion) {
        return switch (inRegion) {
            case "Valley" -> "King's Valley";
            case "Isles" -> "Celsian Isles";
            case "Ring" -> "Architect's Ring";
            default -> inRegion;
        };
    }

    public static String buildStatString(String name, double value) {
        if (isStat(name)) {
            return (value < 0 ? "" : (isBaseStat(name) ? " " : "+")) + (name.equals("knockback_resistance_flat") ? 10 * value : (name.equals("potion_recharge_rate_percent")) ? value / 100 : value) + (isPercentStat(name) ? "" : " ") + formatStat(name);
        } else {
            return formatStat(name) + " " + (isSingleEnchant(name) ? "" : (int)value);
        }
    }

    public static String buildStatStringWithRoman(String name, double value) {
        if (isStat(name)) {
            return (value < 0 ? "" : (isBaseStat(name) ? " " : "+")) + value + (isPercentStat(name) ? "" : " ") + formatStat(name);
        } else {
            return formatStat(name) + " " + (isSingleEnchant(name) ? "" : toRoman((int)value));
        }
    }

    public static String toRoman(int number) {
        TreeMap<Integer, String> romanMap = new TreeMap<>();

        romanMap.put(1000, "M");
        romanMap.put(900, "CM");
        romanMap.put(500, "D");
        romanMap.put(400, "CD");
        romanMap.put(100, "C");
        romanMap.put(90, "XC");
        romanMap.put(50, "L");
        romanMap.put(40, "XL");
        romanMap.put(10, "X");
        romanMap.put(9, "IX");
        romanMap.put(5, "V");
        romanMap.put(4, "IV");
        romanMap.put(1, "I");

        Integer l = romanMap.floorKey(number);
        if (l == null) {
            return "" + number;
        }
        if (l == number) {
            return "" + romanMap.get(l);
        }
        return romanMap.get(l) + toRoman(number - l);
    }

    public static int getMasterworkForRarity(String rarity) {
        return switch (rarity) {
            case "Rare", "Artifact" -> 4;
            case "Epic" -> 6;
            default -> 0;
        };
    }

    @SuppressWarnings("SameReturnValue")
    public static int getMaxFishTier() {
        return 5;
    }

    public static boolean shouldBoldFish(int fishTier) {
        return (fishTier >= 3);
    }

    public static boolean shouldUnderlineFish(int fishTier) {
        return (fishTier >= 5);
    }

    public static int getNumberForTier(String inTier) {
        return switch (inTier) {
            case "Legendary" -> 20;
            case "Epic" -> 19;
            case "Artifact" -> 18;
            case "Rare" -> 17;
            case "Base" -> 16;
            case "Unique" -> 15;
            case "Patron" -> 14;
            case "Uncommon" -> 13;
            case "Tier 5" -> 12;
            case "Tier 4" -> 11;
            case "Tier 3" -> 10;
            case "Tier 2" -> 9;
            case "Tier 1" -> 8;
            case "Tier 0" -> 7;
            case "Obfuscated" -> 6;
            case "Currency" -> 5;
            case "Event Currency" -> 4;
            case "Key" -> 3;
            case "Event" -> 2;
            case "Trophy" -> 1;
            default -> 0;
        };
    }

    public static int getNumberForRegion(String inRegion) {
        return switch (inRegion) {
            case "Ring" -> 3;
            case "Isles" -> 2;
            case "Valley" -> 1;
            default -> 0;
        };
    }

    public static String formatUseLine(String inType) {
        return switch (inType) {
            case "Helmet" -> "When on Head:";
            case "Chestplate" -> "When on Chest:";
            case "Leggings" -> "When on Legs:";
            case "Boots" -> "When on Feet:";
            case "Wand", "Axe", "Pickaxe", "Mainhand Sword", "Mainhand Shield", "Bow", "Trident", "Snowball", "Shovel",
                 "Mainhand", "Scythe", "Crossbow" -> "When in Main Hand:";
            case "Projectile" -> "When Shot:";
            case "Offhand", "Offhand Sword", "Offhand Shield" -> "When in Offhand";
            default -> "When Used:";
        };
    }

    public static String formatStat(String inStat) {
        String stat = inStat;
        stat = stat.replace("_s_", "'s_");
        if (stat.endsWith("_prot")) stat = stat.substring(0, stat.lastIndexOf("_prot")) + "_protection";
        if (stat.endsWith("_base")) stat = stat.equals("spell_power_base") ? "%_" + stat.substring(0, stat.lastIndexOf("_base")) : stat.substring(0, stat.lastIndexOf("_base")) + "";
        if (stat.endsWith("_flat")) stat = stat.equals("knockback_resistance_flat") ? "%_" + stat.substring(0, stat.lastIndexOf("_flat")) : stat.substring(0, stat.lastIndexOf("_flat")) + "";
        if (stat.endsWith("_percent")) stat = stat.equals("potion_recharge_rate_percent") ? stat.substring(0, stat.lastIndexOf("_percent")) : "%_" + stat.substring(0, stat.lastIndexOf("_percent")) + "";
        if (stat.endsWith("_bow")) stat = stat.substring(0, stat.lastIndexOf("_bow")) + "";
        if (stat.endsWith("_tool")) stat = stat.substring(0, stat.lastIndexOf("_tool")) + "_food";
        if (stat.endsWith("_m")) stat = stat.substring(0, stat.lastIndexOf("_m")) + "_melee";
        if (stat.endsWith("_p")) stat = stat.substring(0, stat.lastIndexOf("_p")) + "_ranged";
        stat = stat.replace("_", " ");

        if (stat.length() > 1) stat = stat.substring(0, 1).toUpperCase() + stat.substring(1);
        for (int i = 0; i < stat.length() - 1; i++) {
            if (stat.charAt(i) == ' ') {
                stat = stat.substring(0, i + 1) + stat.substring(i + 1, i + 2).toUpperCase() + stat.substring(i + 2);
            }
        }
        return stat;
    }

    public static String getSkillFromCharmStat(String stat) {
        for (String s : modifiableSkills) {
            if (stat.startsWith(s)) return s;
        }
        return "ERR";
    }

    public static String formatCharmTier(String tier) {
        return (tier.equals("Base") ? "" : tier + " ") + "Charm";
    }

    public static String formatCharmStat(String inStat) {
        String stat = inStat;

        if (stat.endsWith("_flat")) stat = stat.substring(0, stat.lastIndexOf("_flat")) + "";
        if (stat.endsWith("_percent")) stat = "%_" + stat.substring(0, stat.lastIndexOf("_percent"));
        stat = stat.replace("_", " ");

        if (stat.length() > 1) stat = stat.substring(0, 1).toUpperCase() + stat.substring(1);
        for (int i = 0; i < stat.length() - 1; i++) {
            if (stat.charAt(i) == ' ') {
                stat = stat.substring(0, i + 1) + stat.substring(i + 1, i + 2).toUpperCase() + stat.substring(i + 2);
            }
        }

        return stat;
    }

    public static String formatCharmSkill(String inSkill) {
        String skill = inSkill;

        skill = skill.replace("_", " ");

        if (skill.length() > 1) skill = skill.substring(0, 1).toUpperCase() + skill.substring(1);
        for (int i = 0; i < skill.length() - 1; i++) {
            if (skill.charAt(i) == ' ') {
                skill = skill.substring(0, i + 1) + skill.substring(i + 1, i + 2).toUpperCase() + skill.substring(i + 2);
            }
        }

        return skill;
    }

    public static boolean isSingleEnchant(String inEnchant) {
        return singleEnchants.contains(inEnchant);
    }

    public static boolean isCurseEnchant(String inEnchant) {
        return curseEnchants.contains(inEnchant);
    }

    public static boolean isStat(String inStat) {
        return stats.contains(inStat);
    }

    public static boolean isPercentStat(String inStat) {
        return (inStat.endsWith("_percent") && !inStat.equals("potion_recharge_rate_percent")) ||
                inStat.equals("spell_power_base") ||
                inStat.equals("knockback_resistance_flat");
    }

    public static boolean isBaseStat(String inStat) {
        return baseStats.contains(inStat);
    }

    public static boolean isHiddenStat(String inStat) {
        return hiddenStats.contains(inStat);
    }
}
