package dev.eliux.monumentaitemdictionary.util;

public class CharmStat {
    public String statNameFull;
    public String modifiedSkill;
    public boolean statLocked;
    public double statValue;

    public CharmStat(String statNameFull, String modifiedSkill, boolean statLocked, double statValue) {
        this.statNameFull = statNameFull;
        this.modifiedSkill = modifiedSkill;
        this.statLocked = statLocked;
        this.statValue = statValue;
    }
}
