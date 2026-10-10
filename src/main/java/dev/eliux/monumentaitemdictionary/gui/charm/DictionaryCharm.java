package dev.eliux.monumentaitemdictionary.gui.charm;

import dev.eliux.monumentaitemdictionary.util.CharmStat;

import java.util.ArrayList;
import java.util.Objects;

public class DictionaryCharm {
    public String name; // will exist
    public String region; // will exist
    public String locationId; // will exist
    public String tier; // will exist
    public int power; // will exist
    public String className; // will exist
    public String baseItem; // will exist
    public String nbt; // will exist
    public ArrayList<CharmStat> stats; // will exist

    public DictionaryCharm(String name, String region, String locationId, String tier, int power,
                           String className, String baseItem, String nbt, ArrayList<CharmStat> stats) {
        this.name = name;
        this.region = region;
        this.locationId = locationId;
        this.tier = tier;
        this.power = power;
        this.className = className;
        this.baseItem = baseItem;
        this.nbt = nbt;
        this.stats = stats;
    }

    public boolean hasStat(String stat) {
        for (CharmStat charmStat : stats) {
            if (charmStat.statNameFull.equals(stat)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasStatModifier(String statModifier) {
        for (CharmStat charmStat : stats) {
            if (charmStat.modifiedSkill.equals(statModifier)) {
                return true;
            }
        }
        return false;
    }

    public double getStat(String stat) {
        for (CharmStat charmStat : stats) {
            if (charmStat.statNameFull.equals(stat)) {
                return charmStat.statValue;
            }
        }
        return -1.0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DictionaryCharm otherCharm)) return false;

        return Objects.equals(name, otherCharm.name)
                && Objects.equals(region, otherCharm.region)
                && Objects.equals(baseItem, otherCharm.baseItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, region, baseItem);
    }
}
