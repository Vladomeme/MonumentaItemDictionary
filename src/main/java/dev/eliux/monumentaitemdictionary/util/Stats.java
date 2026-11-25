package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;

import java.util.*;

import static java.lang.Math.*;
import static dev.eliux.monumentaitemdictionary.util.Infusion.*;

public class Stats {

    // Misc Stats 0-6
    public double armor;
    public double agility;
    public Percentage speedPercent;
    public Percentage knockbackRes;
    public int thorns;
    public double fireTickDamage;

    // Health Stats 6-14
    public double healthFinal;
    public double currentHealth;
    public Percentage effHealingRate;
    public Percentage healingRate;
    public double regenPerSec;
    public Percentage regenPerSecPercent;
    public double lifeDrainOnCrit;
    public Percentage lifeDrainOnCritPercent;

    // DR Stats 14-21
    public Percentage meleeDR;
    public Percentage projectileDR;
    public Percentage magicDR;
    public Percentage blastDR;
    public Percentage fireDR;
    public Percentage fallDR;
    public Percentage ailmentDR;

    // Health Normalized DR Stats 21-28
    public Percentage meleeHNDR;
    public Percentage projectileHNDR;
    public Percentage magicHNDR;
    public Percentage blastHNDR;
    public Percentage fireHNDR;
    public Percentage fallHNDR;
    public Percentage ailmentHNDR;

    // EHP Stats 28-35
    public double meleeEHP;
    public double projectileEHP;
    public double magicEHP;
    public double blastEHP;
    public double fallEHP;
    public double fireEHP;
    public double ailmentEHP;

    // Melee Stats 35-42
    public Percentage attackSpeedPercent;
    public double attackSpeed;
    public Percentage attackDamagePercent;
    public double attackDamage;
    public double attackDamageCrit;
    public double iframeDPS;
    public double iframeCritDPS;

    // Projectile Stats 42-48
    public Percentage projectileDamagePercent;
    public double projectileDamage;
    public Percentage projectileSpeedPercent;
    public double projectileSpeed;
    public Percentage throwRatePercent;
    public double throwRate;

    // Magic Stats 48-53
    public Percentage magicDamagePercent;
    public Percentage spellPowerPercent;
    public Percentage spellDamage;
    public Percentage spellCooldownPercent;
    public double potionDamage;

    private final ItemRegion currentRegion;
    private final Percentage currentHealthPercent;
    private final List<Situational> situationals;
    private final List<Infusion> infusions;
    private final Map<String, Double> allItemStats = new HashMap<>();

    public Stats(List<DictionaryItem> items, List<Situational> situationals, List<Infusion> infusions,
                 double currentHealthPercent, ItemRegion currentRegion) {
        this.situationals = situationals;
        this.infusions = infusions;
        this.currentHealthPercent = new Percentage(currentHealthPercent, true);
        this.currentRegion = currentRegion;

        for (int i = 0; i < ItemType.equipmentList.size(); i++) {
            if (items.get(i) != null) {
                DictionaryItem item = items.get(i);
                List<ItemStat> itemStats = item.hasMasterwork() ? item.getStatsFromMasterwork(item.getMaxMasterwork() - 1) : item.getStatsNoMasterwork();
                for (ItemStat stat : itemStats) {
                    allItemStats.merge(stat.statName(), (double) stat.statValue(), Double::sum);
                }
            }
        }

        sumAllStats();
        calculateOffenseStats();
        calculateDefenseStats();
    }

    private void sumAllStats() {
        agility = statValue("agility");
        armor = statValue("armor");

        speedPercent = statValue("speed_percent", 100, true)
                .mul((0.1 + statValue("speed_flat")) / 0.1, false)
                .mul(((currentHealthPercent.perc <= 50) ? 1 - 0.1 * statValue("curse_of_crippling") : 1), false);
        knockbackRes = new Percentage(Math.min(statValue("knockback_resistance_flat", 0, false).val, 10) * 10, true);
        thorns = (int) statValue("thorns_flat");
        fireTickDamage = 1 + statValue("inferno");

        Percentage healthPercent = statValue("max_health_percent", 100, true);
        healingRate = statValue("curse_of_anemia", 100, -10, true);
        statValue("sustenance", healingRate, 10, true);
        regenPerSec = 0.33 * sqrt(statValue("regeneration")) * healingRate.val;
        healthFinal = (20 + statValue("max_health_flat")) * healthPercent.val * (1 + 0.01 * infusionValue(VITALITY));
        currentHealth = healthFinal * currentHealthPercent.val;
        effHealingRate = new Percentage(((20 / healthFinal) * healingRate.val), false);
        regenPerSecPercent = new Percentage((regenPerSec / healthFinal), false);

        attackDamagePercent = statValue("attack_damage_percent", 100, true)
                .add(infusionValue(VIGOR) * currentRegion.damageBonus, false);
        attackSpeedPercent = statValue("attack_speed_percent", 100, true);

        projectileDamagePercent = statValue("projectile_damage_percent", 100, true)
                .add(infusionValue(FOCUS) * currentRegion.damageBonus, false);
        projectileSpeedPercent = statValue("projectile_speed_percent", 100, true);

        magicDamagePercent = statValue("magic_damage_percent", 100, true)
                .add(infusionValue(PERSPICACITY) * currentRegion.damageBonus, false);
        potionDamage = statValue("potion_damage_flat");
        throwRatePercent = statValue("throw_rate_percent", 100, true);
    }

    private void calculateOffenseStats() {
        attackDamage = statValue("attack_damage_base", 1) * attackDamagePercent.val
                * ((currentHealthPercent.perc <= 50) ? 1 - 0.1 * statValue("curse_of_crippling") : 1);
        attackSpeed = (statValue("attack_speed_base", 4) + statValue("attack_speed_flat")) * attackSpeedPercent.val;
        attackDamageCrit = (attackDamage * 1.5);
        iframeDPS = ((attackSpeed >= 2) ? attackDamage * 2 : attackDamage * attackSpeed);
        iframeCritDPS = ((attackSpeed >= 2) ? attackDamageCrit * 2 : attackDamageCrit * attackSpeed);

        projectileDamage = statValue("projectile_damage_base") * projectileDamagePercent.val;
        projectileSpeed = statValue("projectile_speed_base") * projectileSpeedPercent.val;
        throwRate = statValue("throw_rate_base") * throwRatePercent.val;

        potionDamage *= magicDamagePercent.val;
        spellPowerPercent = statValue("spell_power_base", 100, true);
        spellDamage = spellPowerPercent.duplicate().mulP(magicDamagePercent);
        spellCooldownPercent = new Percentage(100, true).mul(pow(0.95, statValue("aptitude") - statValue("ineptitude")), false);

        lifeDrainOnCrit = sqrt(statValue("life_drain")) * 1.5 / (pow(clamp(attackSpeed, 0.5, 1.6), 1.5)) * healingRate.val;
        lifeDrainOnCritPercent = new Percentage((lifeDrainOnCrit / healthFinal), false);
    }

    private void calculateDefenseStats() {
        double armor = Math.max(this.armor, 0);
        double agility = Math.max(this.agility, 0);

        boolean noDefence = armor == 0 && agility == 0;

        double eArmor = min(armor, currentRegion.defStatLimit);
        double eAgility = min(agility, currentRegion.defStatLimit);

        if (statValue("adaptability") > 0) {
            if (eArmor > eAgility) eAgility = eArmor;
            else eArmor = eAgility;
        }

        double situationalArmorMulti = min(eArmor / currentRegion.defStatLimit, 1) * currentRegion.ehpMulti;
        double situationalAgilityMulti = min(eAgility / currentRegion.defStatLimit, 1) * currentRegion.ehpMulti;

        double armorMulti = 1;
        double agilityMulti = 1;

        double poiseEffective = currentHealthPercent.val >= 0.9 ? 1 : currentHealthPercent.val >= 0.7 ? 0.5 : 0;
        double steadfastEffective = min((1 - currentHealthPercent.val) / 0.6, 1);
        armorMulti += situationalArmorMulti * situationalValue(Situational.SHIELDING);
        armorMulti += situationalArmorMulti * situationalValue(Situational.INURE);
        armorMulti += situationalArmorMulti * situationalValue(Situational.GUARD);
        armorMulti += situationalArmorMulti * situationalValue(Situational.POISE) * poiseEffective;
        armorMulti += situationalArmorMulti * situationalValue(Situational.STEADFAST) * steadfastEffective;

        agilityMulti += situationalAgilityMulti * situationalValue(Situational.EVASION);
        agilityMulti += situationalAgilityMulti * situationalValue(Situational.REFLEXES);
        agilityMulti += situationalAgilityMulti * situationalValue(Situational.ETHEREAL);
        agilityMulti += situationalAgilityMulti * situationalValue(Situational.TEMPO);
        agilityMulti += situationalAgilityMulti * situationalValue(Situational.CLOAKED);

        double totalArmor = armor + log(1 / armorMulti) / log(0.96);
        double totalAgility = agility + log(1 / agilityMulti) / log(0.96);

        double meleeProt =      statValue("melee_protection");
        double projectileProt = statValue("projectile_protection");
        double magicProt =      statValue("magic_protection");
        double blastProt =      statValue("blast_protection");
        double fireProt =       statValue("fire_protection");
        double fallProt =       statValue("feather_falling");
        double ailmentProt =    statValue("ailment_protection");

        double meleeFragility =      statValue("melee_fragility");
        double projectileFragility = statValue("projectile_fragility");
        double magicFragility =      statValue("magic_fragility");
        double blastFragility =      statValue("blast_fragility");
        double fireFragility =       statValue("fire_fragility");
        double fallFragility =       statValue("fall_fragility");
        double ailmentFragility =    statValue("ailment_fragility");

        double meleeBaseDR =      damageReduction(noDefence, meleeProt,      meleeFragility,      2, totalArmor,     totalAgility);
        double projectileBaseDR = damageReduction(noDefence, projectileProt, projectileFragility, 2, totalArmor,     totalAgility);
        double magicBaseDR =      damageReduction(noDefence, magicProt,      magicFragility,      2, totalArmor,     totalAgility);
        double blastBaseDR =      damageReduction(noDefence, blastProt,      blastFragility,      2, totalArmor,     totalAgility);
        double fireBaseDR =       damageReduction(noDefence, fireProt,       fireFragility,       2, totalArmor / 2, totalAgility / 2);
        double fallBaseDR =       damageReduction(noDefence, fallProt,       fallFragility,       3, totalArmor / 2, totalAgility / 2);
        double ailmentBaseDR =    damageReduction(true,      ailmentProt,    ailmentFragility,    2, 0,              0);

        double meleeSecondWindDR =      damageReductionSW(noDefence, meleeProt,      meleeFragility,      2, totalArmor,     totalAgility);
        double projectileSecondWindDR = damageReductionSW(noDefence, projectileProt, projectileFragility, 2, totalArmor,     totalAgility);
        double magicSecondWindDR =      damageReductionSW(noDefence, magicProt,      magicFragility,      2, totalArmor,     totalAgility);
        double blastSecondWindDR =      damageReductionSW(noDefence, blastProt,      blastFragility,      2, totalArmor,     totalAgility);
        double fireSecondWindDR =       damageReductionSW(noDefence, fireProt,       fireFragility,       2, totalArmor / 2, totalAgility / 2);
        double fallSecondWindDR =       damageReductionSW(noDefence, fallProt,       fallFragility,       3, totalArmor / 2, totalAgility / 2);
        double ailmentSecondWindDR =    damageReductionSW(true,      ailmentProt,    ailmentFragility,    2, 0,              0);

        meleeEHP =      effectiveHealth(meleeBaseDR,      meleeSecondWindDR);
        projectileEHP = effectiveHealth(projectileBaseDR, projectileSecondWindDR);
        magicEHP =      effectiveHealth(magicBaseDR,      magicSecondWindDR);
        blastEHP =      effectiveHealth(blastBaseDR,      blastSecondWindDR);
        fireEHP =       effectiveHealth(fireBaseDR,       fireSecondWindDR);
        fallEHP =       effectiveHealth(fallBaseDR,       fallSecondWindDR);
        ailmentEHP =    effectiveHealth(ailmentBaseDR,    ailmentSecondWindDR);

        meleeDR =      damageReductionPercentage(meleeBaseDR,      meleeSecondWindDR);
        projectileDR = damageReductionPercentage(projectileBaseDR, projectileSecondWindDR);
        magicDR =      damageReductionPercentage(magicBaseDR,      magicSecondWindDR);
        blastDR =      damageReductionPercentage(blastBaseDR,      blastSecondWindDR);
        fireDR =       damageReductionPercentage(fireBaseDR,       fireSecondWindDR);
        fallDR =       damageReductionPercentage(fallBaseDR,       fallSecondWindDR);
        ailmentDR =    damageReductionPercentage(ailmentBaseDR,    ailmentSecondWindDR);

        meleeHNDR =      damageReductionPercentageHN(meleeDR);
        projectileHNDR = damageReductionPercentageHN(projectileDR);
        magicHNDR =      damageReductionPercentageHN(magicDR);
        blastHNDR =      damageReductionPercentageHN(blastDR);
        fireHNDR =       damageReductionPercentageHN(fireDR);
        fallHNDR =       damageReductionPercentageHN(fallDR);
        ailmentHNDR =    damageReductionPercentageHN(ailmentDR);
    }

    private double damageReduction(boolean noDefence, double prot, double fragility, double modifier, double effArmor, double effAgility) {
        double defense = (noDefence || effArmor + effAgility == 0) ? 0 : effArmor + effAgility - 0.5 * effArmor * effAgility / (effArmor + effAgility);
        double baseDmg = (1 - statValue("worldly_protection") * 0.1) * pow(0.96, (prot - fragility) * modifier + defense) * (1 - infusionValue(TENACITY) * 0.005);
        return 1 - baseDmg;
    }

    private double damageReductionSW(boolean noDefence, double prot, double fragility, double modifier, double effArmor, double effAgility) {
        double defense = (noDefence || effArmor + effAgility == 0) ? 0 : effArmor + effAgility - 0.5 * effArmor * effAgility / (effArmor + effAgility);
        double baseDmg = (1 - statValue("worldly_protection") * 0.1) * pow(0.96, (prot - fragility) * modifier + defense) * (1 - infusionValue(TENACITY) * 0.005);

        if (situationals.contains(Situational.SECOND_WIND)) {
            double swLevel = situationalValue(Situational.SECOND_WIND);
            if (swLevel > 0) return 1 - baseDmg * pow(0.9, swLevel);
        }
        return 1 - baseDmg;
    }

    private double effectiveHealth(double damageReductionBase, double damageReductionSecondWind) {
        if (situationals.contains(Situational.SECOND_WIND)) {
            double swLevel = situationalValue(Situational.SECOND_WIND);
            if (swLevel > 0) {
                double hpNoSecondWind = max(0, (currentHealth - healthFinal * 0.5));
                double hpSecondWind = min(currentHealth, healthFinal * 0.5);

                return (hpNoSecondWind / (1 - damageReductionBase) + hpSecondWind / (1 - damageReductionSecondWind));
            }
        }
        return healthFinal * currentHealthPercent.val / (1 - damageReductionBase);
    }

    private Percentage damageReductionPercentage(double damageReductionBase, double damageReductionSecondWind) {
        if (situationals.contains(Situational.SECOND_WIND)) {
            double swLevel = situationalValue(Situational.SECOND_WIND);
            if (swLevel > 0 && currentHealthPercent.val <= 0.5) return new Percentage(damageReductionSecondWind, false);
        }
        return new Percentage(damageReductionBase, false);
    }

    //health normalized
    private Percentage damageReductionPercentageHN(Percentage damageReduction) {
        return new Percentage(1 - (1 - damageReduction.val) / (healthFinal / 20), false);
    }

    private int infusionValue(Infusion infusion) {
        return infusions.contains(infusion) ? (infusions.contains(UNDERSTANDING) ? 30 : 24) : 0;
    }

    private double situationalValue(Situational situational) {
        return situationals.contains(situational) ? statValue(situational.key) : 0;
    }

    private double statValue(String s) {
        if (allItemStats.containsKey(s)) return allItemStats.get(s);
        return 0;
    }

    private double statValue(String s, double defaultValue) {
        if (allItemStats.containsKey(s)) return allItemStats.get(s);
        return defaultValue;
    }

    private Percentage statValue(String s, double initialValue, boolean percent) {
        Percentage percentage = new Percentage(initialValue, percent);
        if (allItemStats.containsKey(s)) percentage.add(allItemStats.get(s), percent);
        return percentage;
    }

    @SuppressWarnings("SameParameterValue")
    private Percentage statValue(String s, double initialValue, double multiplier, boolean percent) {
        Percentage percentage = new Percentage(initialValue, percent);
        if (allItemStats.containsKey(s)) percentage.add(allItemStats.get(s) * multiplier, percent);
        return percentage;
    }

    @SuppressWarnings("SameParameterValue")
    private void statValue(String s, Percentage percentage, double multiplier, boolean percent) {
        if (allItemStats.containsKey(s)) percentage.add(allItemStats.get(s) * multiplier, percent);
    }
}
