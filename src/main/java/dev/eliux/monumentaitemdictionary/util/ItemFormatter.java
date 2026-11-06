package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.Mid;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemFormatter {

    public static final String[] charmStatExceptions = {"decay", "recoil", "jungle's_nourishment", "rage_of_the_keter",
            "devastation", "elemental_spirit", "rupture"};

    private static final Set<String> singleEnchants = new HashSet<>(List.of("gills", "weightless", "rage_of_the_keter", "jungle's_nourishment",
            "unbreakable", "adaptability", "multishot", "intuition", "radiant", "resurrection", "two_handed", "curse_of_irreparability", "mending", "silk_touch",
            "curse_of_corruption", "protection_of_the_depths", "infinity_bow", "infinity_tool", "infinity", "aqua_affinity", "ashes_of_eternity", "void_tether",
            "excavator", "darksight", "material", "alchemical_utensil", "broomstick", "clucking", "oinking", "throwing_knife", "liquid_courage",
            "intoxicating_warmth", "temporal_bender", "curse_of_ephemerality", "instant_drink", "divine_aura", "cumbersome", "persistence", "curse_of_instability",
            "snowy", "spiritshot", "oversized", "kinetic_loading"));

    private static final Set<String> curseEnchants = new HashSet<>(List.of("ineptitude", "curse_of_shrapnel", "curse_of_vanishing", "curse_of_corruption",
            "curse_of_crippling", "curse_of_irreparability", "two_handed", "fire_fragility", "melee_fragility", "blast_fragility", "projectile_fragility",
            "magic_fragility", "ailment_fragility", "fall_fragility", "curse_of_anemia", "cumbersome", "curse_of_ephemerality", "curse_of_instability",
            "curse_of_the_veil", "oversized", "curse_of_pestilence", "starvation"));

    private static final Set<String> stats = new HashSet<>(List.of("armor", "agility", "spell_power_base", "projectile_speed_percent",
            "knockback_resistance_flat", "attack_damage_base", "thorns_percent", "max_health_flat", "projectile_damage_base", "throw_rate_percent",
            "attack_speed_flat", "projectile_speed_base", "attack_damage_percent", "max_health_percent", "projectile_damage_percent", "magic_damage_percent",
            "attack_speed_percent", "thorns_flat", "speed_flat", "speed_percent", "attack_speed_base", "potion_damage_flat", "potion_radius_flat",
            "throw_rate_base", "potion_recharge_rate_percent"));

    private static final Set<String> baseStats = new HashSet<>(List.of("projectile_damage_base", "projectile_speed_base", "potion_damage_flat", "potion_radius_flat",
            "attack_speed_base", "attack_damage_base", "throw_rate_base", "potion_recharge_rate_percent"));

    private static final Set<String> hiddenStats = new HashSet<>(List.of("noglint", "hideenchants", "hideinfo", "utility_item_for_armory"));

    public static String buildStatString(String name, float value) {
        if (isStat(name))
            return (value < 0 ? "" : (isBaseStat(name) ? " " : "+")) + (name.equals("knockback_resistance_flat") ? 10 * value
                    : (name.equals("potion_recharge_rate_percent")) ? value / 100 : value) + (isPercentStat(name) ? "" : " ") + formatStat(name);
        else return formatStat(name) + " " + (isSingleEnchant(name) ? "" : (int) value);
    }

    public static boolean shouldBoldFish(int fishTier) {
        return (fishTier >= 3);
    }

    public static boolean shouldUnderlineFish(int fishTier) {
        return (fishTier >= 5);
    }

    public static Text getLocationText(String locationId) {
        Location location = Mid.controller.locationData.get(locationId);

        MutableText text;
        if (locationId.equals("twisted"))
            text = Text.literal("Twisted ").append(Text.literal("XXXXXXXX").setStyle(Styles.OBFUSCATED));
        else text = Text.literal(location.displayName());

        return text.setStyle(Style.EMPTY.withColor(location.color()));
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
        if (stat.endsWith("_base")) stat = stat.equals("spell_power_base") ? "%_" + stat.substring(0, stat.lastIndexOf("_base")) : stat.substring(0, stat.lastIndexOf("_base"));
        if (stat.endsWith("_flat")) stat = stat.equals("knockback_resistance_flat") ? "%_" + stat.substring(0, stat.lastIndexOf("_flat")) : stat.substring(0, stat.lastIndexOf("_flat"));
        if (stat.endsWith("_percent")) stat = stat.equals("potion_recharge_rate_percent") ? stat.substring(0, stat.lastIndexOf("_percent")) : "%_" + stat.substring(0, stat.lastIndexOf("_percent"));
        if (stat.endsWith("_bow")) stat = stat.substring(0, stat.lastIndexOf("_bow"));
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
    
    public static String formatEffect(String effect, String enchant) {
        return switch (effect) {
            case "damage", "Weakness" -> "Strength";
            case "MeleeWeakness" -> "Melee Damage";
            case "NegativeAttackSpeed" -> "Attack Speed";
            case "NegativeKnockbackResist", "KnockbackResist" -> "Knockback Resistance";
            case "AbilityCooldownDecrease", "AbilityCooldownIncrease" -> "Ability Cooldowns";

            case "AntiHeal", "Heal" -> "Healing Rate";
            case "HealOverTime" -> "Health Over Time";
            case "InstantHealthPercent" -> "Instant Health";
            case "MaxHealthDecrease", "MaxHealthIncrease" -> "Max Health";
            case "InstantDamagePercent" -> "Instant Damage";

            case "VanillaFireRes" -> "Fire Immunity";
            case "Vulnerability" -> "Resistance";
            case "FireVulnerability" -> "Fire Resistance";
            case "ProjectileVulnerability" -> "Projectile Resistance";
            case "BlastVulnerability" -> "Blast Resistance";
            case "FallVulnerability" -> "Fall Resistance";
            case "MeleeVulnerability" -> "Melee Resistance";
            case "MagicVulnerability" -> "Magic Resistance";

            case "SoulThreadReduction", "SoulThreadBonus" -> "Soul Thread Chance";
            case "ArrowSaving" -> "Arrow Save Chance";
            case "ExpLoss", "ExpBonus" -> "Experience";
            case "DurabilityLoss", "DurabilitySave" -> "Durability";
            case "WaterBreath" -> "Water Breathing";
            case "Slow" -> "Speed";

            case "FireDamageNegate" -> "Fire Hits Blocked";
            case "FallDamageNegate" -> "Falling Hits Blocked";
            case "MeleeDamageNegate" -> "Melee Hits Blocked";

            case "EnchantEffect" -> enchant + " Levels";

            default -> { //"AbilityCooldownDecrease" -> "Ability Cooldown Decrease"
                StringBuilder builder = new StringBuilder();
                char[] chars = effect.toCharArray();

                builder.append(chars[0]);
                for (int i = 1; i != chars.length; i++) {
                    if (chars[i] < 'a') builder.append(" ");
                    builder.append(chars[i]);
                }
                yield builder.toString();
            }
        };
    }

    public static String getSkillFromCharmStat(String stat) {
        for (String s : PlayerClass.allSkills) {
            if (stat.startsWith(s)) return s;
        }
        for (String s : charmStatExceptions) {
            if (stat.startsWith(s)) return s;
        }
        //unlucky
        for (String s : PlayerClass.allSkills) {
            if (stat.contains(s)) return s;
        }
        for (String s : charmStatExceptions) {
            if (stat.contains(s)) return s;
        }
        return "ERR";
    }

    public static String formatCharmStat(String inStat) {
        String stat = inStat;

        if (stat.endsWith("_flat")) stat = stat.substring(0, stat.lastIndexOf("_flat"));
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
