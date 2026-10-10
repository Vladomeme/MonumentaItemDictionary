package dev.eliux.monumentaitemdictionary.gui.charm;

import dev.eliux.monumentaitemdictionary.util.CharmStat;

import java.util.ArrayList;
import java.util.Objects;

//All values must be present
public record DictionaryCharm(String name, String region, String locationId, String tier, int power, String className,
                              String baseItem, String nbt, ArrayList<CharmStat> stats) {

    public boolean hasStat(String stat) {
        for (CharmStat charmStat : stats) {
            if (charmStat.statNameFull().equals(stat)) return true;
        }
        return false;
    }

    public boolean hasStatModifier(String statModifier) {
        for (CharmStat charmStat : stats) {
            if (charmStat.modifiedSkill().equals(statModifier)) return true;
        }
        return false;
    }

    public double getStat(String stat) {
        for (CharmStat charmStat : stats) {
            if (charmStat.statNameFull().equals(stat)) return charmStat.statValue();
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
