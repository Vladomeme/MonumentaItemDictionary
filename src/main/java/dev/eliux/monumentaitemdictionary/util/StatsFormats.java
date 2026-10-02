package dev.eliux.monumentaitemdictionary.util;

import java.util.HashMap;
import java.util.Map;

public class StatsFormats {
    public static Map<String, String> statFormats = new HashMap<>() {{
       put("armor", "Armor: ");
       put("agility", "Agility: ");
       put("speedPercent", "% Speed: ");
       put("knockbackRes", "% KB Resistance: ");
       put("thorns", "Thorns Damage: ");
       put("fireTickDamage", "Fire Tick Damage: ");
       put("healthFinal", "Max Health: ");
       put("currentHealth", "Current Health: ");
       put("effHealingRate", "Eff. Healing Rate: ");
       put("healingRate", "Healing Rate: ");
       put("regenPerSec", "Regen / Sec: ");
       put("regenPerSecPercent", "% HP Regen / Sec: ");
       put("lifeDrainOnCrit", "Life Drain Crit: ");
       put("lifeDrainOnCritPercent", "Life Drain % HP Crit: ");
       put("attackSpeedPercent", "Attack Speed: ");
       put("attackSpeed", "Weapon Attack Speed: ");
       put("attackDamagePercent", "Damage: ");
       put("attackDamage", "Weapon Damage: ");
       put("attackDamageCrit", "Weapon Crit Damage: ");
       put("iframeDPS", "IFrame-Capped DPS: ");
       put("iframeCritDPS", "IFrame-Capped Crit DPS: ");
       put("projectileDamagePercent", "Proj Damage: ");
       put("projectileDamage", "Weapon Proj Damage: ");
       put("projectileSpeedPercent", "Proj Speed: ");
       put("projectileSpeed", "Weapon Proj Speed: ");
       put("throwRatePercent", "Throw Rate: ");
       put("throwRate", "Weapon Throw Rate: ");
       put("magicDamagePercent", "Magic Damage: ");
       put("spellPowerPercent", "Spell Power: ");
       put("spellDamage", "Total Magic Damage: ");
       put("spellCooldownPercent", "Cooldown Duration: ");
       put("potionDamage", "Potion Damage: ");
       put("melee", "Melee: ");
       put("projectile", "Projectile: ");
       put("magic", "Magic: ");
       put("blast", "Blast: ");
       put("fire", "Fire: ");
       put("fall", "Fall: ");
       put("ailment", "Ailment: ");
    }};

    public static Map<String, String> getStatFormats() {
        return statFormats;
    }
}
