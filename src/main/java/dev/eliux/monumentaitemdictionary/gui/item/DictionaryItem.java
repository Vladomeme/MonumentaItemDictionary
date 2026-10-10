package dev.eliux.monumentaitemdictionary.gui.item;

import dev.eliux.monumentaitemdictionary.util.ItemFormatter;
import dev.eliux.monumentaitemdictionary.util.ItemStat;

import java.util.ArrayList;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

//Must be present: name, type, baseItem, lore, nbt, stats
public record DictionaryItem(String name, String type, String region, ArrayList<String> tier, String locationId,
                             int fishTier, boolean isFish, String baseItem, String lore, ArrayList<String> nbt,
                             ArrayList<ArrayList<ItemStat>> stats, boolean hasMasterwork) implements Comparable<DictionaryItem> {

    public boolean hasRegion() {
        return !region.isEmpty();
    }

    public boolean hasTier() {
        return !tier.isEmpty();
    }

    public boolean hasLocation() {
        return !locationId.isEmpty();
    }

    public void addMasterworkTier(String newTier, ArrayList<ItemStat> newStats, String newNbt, int level) {
        tier.set(level, newTier);
        stats.set(level, newStats);
        nbt.set(level, newNbt);
    }

    public ArrayList<ItemStat> getStatsNoMasterwork() {
        return stats.getFirst();
    }

    public ArrayList<ItemStat> getStatsFromMasterwork(int level) {
        return stats.get(level);
    }

    public String getNbtNoMasterwork() {
        return nbt.getFirst();
    }

    public String getNbtFromMasterwork(int level) {
        return nbt.get(level);
    }

    public String getTierNoMasterwork() {
        return tier.getFirst();
    }

    public String getTierFromMasterwork(int level) {
        return tier.get(level);
    }

    public int getMinMasterwork() {
        if (!hasMasterwork) return 0;

        for (int i = 0; i < stats.size(); i++) {
            if (stats.get(i) != null) return i;
        }
        return 0;
    }

    public int getMaxMasterwork() {
        return hasMasterwork ? stats.size() : 0;
    }

    public boolean hasStat(String stat) {
        for (ArrayList<ItemStat> itemStatsList : stats) {
            if (itemStatsList == null) continue;

            for (ItemStat itemStat : itemStatsList) {
                if (itemStat.statName().equals(stat)) return true;
            }
        }
        return false;
    }

    public double getStat(String stat) {
        double highest = -1.0;
        for (ArrayList<ItemStat> itemStatsList : stats) {
            if (itemStatsList == null) continue;

            for (ItemStat itemStat : itemStatsList) {
                if (itemStat.statName().equals(stat) && itemStat.statValue() > highest)
                    highest = itemStat.statValue();
            }
        }
        return highest;
    }

    @Override
    public int compareTo(@NotNull DictionaryItem o) {
        int regionComparison = ItemFormatter.getNumberForRegion(o.region) - ItemFormatter.getNumberForRegion(this.region);
        if (regionComparison != 0) return regionComparison;

        int tierComparison = ItemFormatter.getNumberForTier(o.hasMasterwork ? o.getTierFromMasterwork(o.getMinMasterwork()) : o.getTierNoMasterwork()) - ItemFormatter.getNumberForTier(this.hasMasterwork ? this.getTierFromMasterwork(this.getMinMasterwork()) : this.getTierNoMasterwork());
        if (tierComparison != 0) return tierComparison;

        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DictionaryItem otherItem)) return false;

        return hasMasterwork == otherItem.hasMasterwork
                && Objects.equals(name, otherItem.name)
                && Objects.equals(region, otherItem.region)
                && Objects.equals(baseItem, otherItem.baseItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, region, baseItem, hasMasterwork);
    }
}
