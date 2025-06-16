package dev.eliux.monumentaitemdictionary.util;

import java.util.TreeMap;

public class ItemFormatter {
    public static String[] modifiableSkills = {"alchemist_potion", "gruesome_alchemy", "iron_tincture", "empowering_odor", "energizing_elixir", "brutal_alchemy", "alchemical_artillery", "unstable_amalgam", "bezoar", "taboo", "scorched_earth", "esoteric_enhancements", "panacea", "transmutation_ring", "warding_remedy",
            "rejuvenation", "celestial_blessing", "divine_justice", "heavenly_boon", "illuminate", "cleansing_rain", "hand_of_light", "crusade", "sanctified_armor", "holy_javelin", "choir_bells", "luminous_infusion", "enchanted_prayer", "thurible_procession", "hallowed_beam",
            "arcane_strike", "frost_nova", "mana_lance", "thunder_step", "elemental_arrows", "magma_shield", "spellshock", "prismatic_shield", "astral_omen", "cosmic_moonblade", "sage's_insight", "blizzard", "elemental_spirits", "starfall",
            "advancing_shadows", "dagger_throw", "escape_death", "smokescreen", "by_my_blade", "dodging", "skirmisher", "vicious_combos", "blade_dance", "deadly_ronde", "wind_walk", "bodkin_blitz", "cloak_and_dagger", "coup_de_grace",
            "hunting_companion", "wind_bomb", "swift_cuts", "swiftness", "eagle_eye", "sharpshooter", "quickdraw", "volley", "rending_razor", "whirling_blade", "tactical_maneuver", "pinning_shot", "split_arrow", "predator_strike",
            "cleansing_totem", "flame_totem", "lightning_totem", "earthen_tremor", "totemic_projection", "interconnected_havoc", "chain_lightning", "crystalline_combos", "sanctuary", "whirlwind_totem", "chain_healing_wave", "desecrating_shot", "decayed_totem", "devastation",
            "amplifying_hex", "choleric_flames", "melancholic_lament", "sanguine_harvest", "phlegmatic_resolve", "cursed_wound", "grasping_claws", "soul_rend", "dark_pact", "judgement_chain", "voodoo_bonds", "haunting_shades", "restless_souls", "withering_gaze",
            "brute_force", "defensive_line", "riposte", "toughness", "counter_strike", "frenzy", "shield_bash", "weapon_mastery", "glorious_battle", "meteor_slam", "rampage", "bodyguard", "challenge", "shield_wall",
            "decay", "inferno", "recoil", "jungle's_nourishment", "rage_of_the_keter", "hex_eater", "sapper", "life_drain", "regicide", "quake", "eruption", "smite", "slayer", "duelist", "regeneration", "thunder_aspect"};

    public static boolean shouldBold(String inTier) {
        return inTier.equals("Patron") ||
                inTier.equals("Key") ||
                inTier.equals("Currency") ||
                inTier.equals("Trophy") ||
                inTier.equals("Uncommon") ||
                inTier.equals("Unique") ||
                inTier.equals("Rare") ||
                inTier.equals("Artifact") ||
                inTier.equals("Epic") ||
                inTier.equals("Legendary");
    }

    public static boolean shouldUnderline(String inTier) {
        return inTier.equals("Epic") ||
                inTier.equals("Legendary");
    }

    public static String formatRegion(String inRegion) {
        return switch (inRegion) {
            case "Valley":
                yield "King's Valley";
            case "Isles":
                yield "Celsian Isles";
            case "Ring":
                yield "Architect's Ring";
            default:
                yield inRegion;
        };
    }

    public static String buildStatString(String name, double value) {
        if (isStat(name)) {
            return (value < 0 ? "" : (isBaseStat(name) ? " " : "+")) + value + (isPercentStat(name) ? "" : " ") + formatStat(name);
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
            case "Rare":
            case "Artifact":
                yield 4;
            case "Epic":
                yield 6;
            default:
                yield 0;
        };
    }

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
            case "Legendary": yield 20;
            case "Epic": yield 19;
            case "Artifact": yield 18;
            case "Rare": yield 17;
            case "Base": yield 16;
            case "Unique": yield 15;
            case "Patron": yield 14;
            case "Uncommon": yield 13;
            case "Tier 5": yield 12;
            case "Tier 4": yield 11;
            case "Tier 3": yield 10;
            case "Tier 2": yield 9;
            case "Tier 1": yield 8;
            case "Tier 0": yield 7;
            case "Obfuscated": yield 6;
            case "Currency": yield 5;
            case "Event Currency": yield 4;
            case "Key": yield 3;
            case "Event": yield 2;
            case "Trophy": yield 1;
            default: yield 0;
        };
    }

    public static int getNumberForRegion(String inRegion) {
        return switch (inRegion) {
            case "Ring": yield 3;
            case "Isles": yield 2;
            case "Valley": yield 1;
            default: yield 0;
        };
    }

    public static String formatUseLine(String inType) {
        return switch (inType) {
            case "Helmet": yield "When on Head:";
            case "Chestplate": yield "When on Chest:";
            case "Leggings": yield "When on Legs:";
            case "Boots": yield "When on Feet:";
            case "Wand":
            case "Axe":
            case "Pickaxe":
            case "Mainhand Sword":
            case "Mainhand Shield":
            case "Bow":
            case "Trident":
            case "Snowball":
            case "Shovel":
            case "Mainhand":
            case "Scythe":
            case "Crossbow":
                yield "When in Main Hand:";
            case "Projectile":
                yield "When Shot:";
            case "Offhand":
            case "Offhand Sword":
            case "Offhand Shield":
                yield "When in Offhand";
            default:
                yield "When Used:";
        };
    }

    public static String formatStat(String inStat) {
        String stat = inStat;
        stat = stat.replace("_s_", "'s_");
        if (stat.endsWith("_prot")) stat = stat.substring(0, stat.lastIndexOf("_prot")) + "_protection";
        if (stat.endsWith("_base")) stat = stat.substring(0, stat.lastIndexOf("_base")) + "";
        if (stat.endsWith("_flat")) stat = stat.substring(0, stat.lastIndexOf("_flat")) + "";
        if (stat.endsWith("_percent")) stat = "%_" + stat.substring(0, stat.lastIndexOf("_percent"));
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
        return inEnchant.equals("gills") ||
                inEnchant.equals("weightless") ||
                inEnchant.equals("rage_of_the_keter") ||
                inEnchant.equals("jungle_s_nourishment") ||
                inEnchant.equals("unbreakable") ||
                inEnchant.equals("adaptability") ||
                inEnchant.equals("multishot") ||
                inEnchant.equals("intuition") ||
                inEnchant.equals("radiant") ||
                inEnchant.equals("resurrection") ||
                inEnchant.equals("two_handed") ||
                inEnchant.equals("curse_of_irreparability") ||
                inEnchant.equals("mending") ||
                inEnchant.equals("silk_touch") ||
                inEnchant.equals("curse_of_corruption") ||
                inEnchant.equals("protection_of_the_depths") ||
                inEnchant.equals("infinity_bow") ||
                inEnchant.equals("infinity_tool") ||
                inEnchant.equals("infinity") ||
                inEnchant.equals("aqua_affinity") ||
                inEnchant.equals("ashes_of_eternity") ||
                inEnchant.equals("void_tether") ||
                inEnchant.equals("excavator") ||
                inEnchant.equals("darksight") ||
                inEnchant.equals("material") ||
                inEnchant.equals("alchemical_utensil") ||
                inEnchant.equals("broomstick") ||
                inEnchant.equals("clucking") ||
                inEnchant.equals("oinking") ||
                inEnchant.equals("throwing_knife") ||
                inEnchant.equals("liquid_courage") ||
                inEnchant.equals("intoxicating_warmth") ||
                inEnchant.equals("temporal_bender") ||
                inEnchant.equals("curse_of_ephemerality") ||
                inEnchant.equals("instant_drink") ||
                inEnchant.equals("divine_aura") ||
                inEnchant.equals("cumbersome") ||
                inEnchant.equals("persistence");
    }

    public static boolean isCurseEnchant(String inEnchant) {
        return inEnchant.equals("ineptitude") ||
                inEnchant.equals("curse_of_shrapnel") ||
                inEnchant.equals("curse_of_vanishing") ||
                inEnchant.equals("curse_of_corruption") ||
                inEnchant.equals("curse_of_crippling") ||
                inEnchant.equals("curse_of_irreparability") ||
                inEnchant.equals("two_handed") ||
                inEnchant.equals("fire_fragility") ||
                inEnchant.equals("melee_fragility") ||
                inEnchant.equals("blast_fragility") ||
                inEnchant.equals("projectile_fragility") ||
                inEnchant.equals("magic_fragility") ||
                inEnchant.equals("curse_of_anemia") ||
                inEnchant.equals("cumbersome") ||
                inEnchant.equals("curse_of_ephemerality");
    }

    public static boolean isStat(String inStat) {
        return inStat.equals("armor") ||
                inStat.equals("agility") ||
                inStat.equals("spell_power_base") ||
                inStat.equals("projectile_speed_percent") ||
                inStat.equals("knockback_resistance_flat") ||
                inStat.equals("attack_damage_base") ||
                inStat.equals("thorns_percent") ||
                inStat.equals("max_health_flat") ||
                inStat.equals("projectile_damage_base") ||
                inStat.equals("throw_rate_percent") ||
                inStat.equals("attack_speed_flat") ||
                inStat.equals("projectile_speed_base") ||
                inStat.equals("attack_damage_percent") ||
                inStat.equals("max_health_percent") ||
                inStat.equals("projectile_damage_percent") ||
                inStat.equals("magic_damage_percent") ||
                inStat.equals("attack_speed_percent") ||
                inStat.equals("thorns_flat") ||
                inStat.equals("speed_flat") ||
                inStat.equals("speed_percent") ||
                inStat.equals("attack_speed_base") ||
                inStat.equals("potion_damage_flat") ||
                inStat.equals("potion_radius_flat") ||
                inStat.equals("throw_rate_base");
    }

    public static boolean isPercentStat(String inStat) {
        return inStat.endsWith("_percent");
    }

    public static boolean isBaseStat(String inStat) {
        return inStat.equals("projectile_damage_base") ||
                inStat.equals("projectile_speed_base") ||
                inStat.equals("potion_damage_flat") ||
                inStat.equals("potion_radius_flat") ||
                inStat.equals("attack_speed_base") ||
                inStat.equals("attack_damage_base") ||
                inStat.equals("throw_rate_base");
    }

    public static boolean isHiddenStat(String inStat) {
        return inStat.equals("noglint") ||
                inStat.equals("hideenchants") ||
                inStat.equals("hideinfo");
    }
}
