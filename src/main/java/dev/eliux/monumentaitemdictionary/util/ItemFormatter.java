package dev.eliux.monumentaitemdictionary.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemFormatter {

    private static final String[] charmStatExceptions = {"decay", "recoil", "jungle's_nourishment", "rage_of_the_keter",
            "devastation", "elemental_spirit", "rupture"};

    private static final Set<String> singleEnchants = new HashSet<>();

    public static final Set<String> curseEnchants = new HashSet<>();

    public static final Set<String> stats = new HashSet<>(List.of("armor", "agility", "spell_power_base", "projectile_speed_percent",
            "knockback_resistance_flat", "attack_damage_base", "thorns_percent", "max_health_flat", "projectile_damage_base", "throw_rate_percent",
            "attack_speed_flat", "projectile_speed_base", "attack_damage_percent", "max_health_percent", "projectile_damage_percent", "magic_damage_percent",
            "attack_speed_percent", "thorns_flat", "speed_flat", "speed_percent", "attack_speed_base", "potion_damage_flat", "potion_radius_flat",
            "throw_rate_base", "potion_recharge_rate_percent"));

    public static final Set<String> baseStats = new HashSet<>(List.of("projectile_damage_base", "projectile_speed_base", "potion_damage_flat", "potion_radius_flat",
            "attack_speed_base", "attack_damage_base", "throw_rate_base", "potion_recharge_rate_percent"));

    public static final Set<String> hiddenStats = new HashSet<>(List.of("noglint", "hideenchants", "hideinfo", "utility_item_for_armory"));

    public static String buildStatString(String name, float value) {
        StringBuilder builder = new StringBuilder();
        if (stats.contains(name)) {
            if (value >= 0 && baseStats.contains(name)) builder.append("+");

            if (name.equals("knockback_resistance_flat")) value *= 10;
            else if (name.equals("potion_recharge_rate_percent")) value /= 100;
            builder.append(value);

            if (isPercentStat(name)) builder.append(" ");
            builder.append(fromSnakeCase(name));

            return builder.toString();
        }
        else {
            builder.append(fromSnakeCase(name));
            builder.append(" ");
            if (singleEnchants.contains(name)) builder.append((int) value);
        }
        return builder.toString();
    }

    public static boolean shouldBoldFish(int fishTier) {
        return (fishTier >= 3);
    }

    public static boolean shouldUnderlineFish(int fishTier) {
        return (fishTier >= 5);
    }

    public static boolean isPercentStat(String inStat) {
        return (inStat.endsWith("_percent") && !inStat.equals("potion_recharge_rate_percent")) ||
                inStat.equals("spell_power_base") ||
                inStat.equals("knockback_resistance_flat");
    }

    public static int getColorForStat(String itemStat, float value) {
        if (value < 0 || ItemFormatter.curseEnchants.contains(itemStat)) return 0xFF5555;
        if (itemStat.equals("armor") || itemStat.equals("agility")) return 0x33CCFF;
        if (ItemFormatter.baseStats.contains(itemStat)) return 0x00AA00;
        if (ItemFormatter.stats.contains(itemStat)) return 0x5555FF;

        return 0xAAAAAA;
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

    public static String fromSnakeCase(String s) {
        switch (s) { //exceptions
            case "jungles_nourishment" -> {return "Jungle's Nourishment";}
            case "spell_power_base" -> {return "% Spell Power";}
            case "knockback_resistance_flat" -> {return "% Knockback Resistance";}
            case "potion_recharge_rate_percent" -> {return "Potion Recharge Rate";}
        }

        if (s.endsWith("_base")) s = s.substring(0, s.length() - 5);
        if (s.endsWith("_flat")) s = s.substring(0, s.length() - 5);
        if (s.endsWith("_percent")) s = "% " + s.substring(0, s.length() - 8);

        return toUpperCase(s);
    }

    //replaces underscores, makes first letter of each word uppercase
    public static String toUpperCase(String s) {
        if (s.isEmpty()) return "";

        char[] chars = s.toCharArray();
        if (chars[0] >= 'a' && chars[0] <= 'z') chars[0] -= 32;
        for (int i = 1; i != chars.length; i++) {
            while (chars[i] != '_' && chars[i] != ' ') {
                i++;
                if (i == chars.length) return String.copyValueOf(chars);
            }
            chars[i] = ' ';
            if (i + 1 != chars.length) {
                if (chars[i + 1] >= 'a' && chars[i + 1] <= 'z') {
                    chars[i + 1] -= 32;
                    i++;
                }
            }
        }
        return String.copyValueOf(chars);
    }

    public static String toSnakeCase(String s) {
        StringBuilder builder = new StringBuilder();
        char[] chars = s.toCharArray();

        for (int i = 0; i != chars.length; i++) {
            char c = chars[i];
            if (c == ' ' || c == '_') builder.append("_");
            else if (c >= 'a' && c <= 'z') builder.append(c);
            else if (c >= 'A' && c <= 'Z') builder.append((char) (c + 32));
        }
        return builder.toString();
    }

    public static void extractEnchantInfo(Map<String, String> statMap) {
        singleEnchants.clear();
        curseEnchants.clear();

        for (Map.Entry<String, String> entry : statMap.entrySet()) {
            NbtCompound nbt;
            try {
                nbt = StringNbtReader.parse(entry.getValue());
            }
            catch (CommandSyntaxException ignored) {
                continue;
            }
            NbtList lore = nbt.getCompound("display").getList("Lore", 8);
            if (lore.isEmpty()) return;

            for (NbtElement nbtElement : lore) {
                JsonElement textElement = JsonParser.parseString(nbtElement.asString());
                if (!textElement.isJsonObject()) continue;

                JsonObject textObject = (JsonObject) textElement;
                if (!textObject.has("color") || !textObject.has("text")) continue;

                String text = textObject.get("text").getAsString();
                if (text.indexOf(':') != -1) break; //divider in region/location line

                text = toSnakeCase(text);
                String color = textObject.get("color").getAsString();
                boolean isCurse = color.equals("red");

                if (!color.equals("gray") && !isCurse) continue;

                String enchantKey = toSnakeCase(entry.getKey());
                if (text.equals(enchantKey)) {
                    singleEnchants.add(entry.getKey());
                    if (isCurse) curseEnchants.add(entry.getKey());
                }
                else if (isCurse && text.contains(enchantKey))
                    curseEnchants.add(entry.getKey());
            }
        }
    }
}
