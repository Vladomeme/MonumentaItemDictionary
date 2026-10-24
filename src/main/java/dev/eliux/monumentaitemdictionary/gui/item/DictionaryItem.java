package dev.eliux.monumentaitemdictionary.gui.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.eliux.monumentaitemdictionary.util.ItemFactory;
import dev.eliux.monumentaitemdictionary.util.ItemFormatter;
import dev.eliux.monumentaitemdictionary.util.ItemStat;

import java.util.List;
import java.util.Objects;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import org.jetbrains.annotations.NotNull;

//Must be present: name, type, baseItem, lore, nbt, stats
public record DictionaryItem(String name, String type, String region, List<String> tier, String locationId,
                             int fishTier, boolean isFish, String baseItem, String lore, List<String> nbt,
                             List<List<ItemStat>> stats, List<String> effects, boolean hasMasterwork) implements Comparable<DictionaryItem> {

    public boolean hasRegion() {
        return !region.isEmpty();
    }

    public boolean hasTier() {
        return !tier.isEmpty();
    }

    public boolean hasLocation() {
        return !locationId.isEmpty();
    }

    public boolean hasEffects() {
        return !effects.isEmpty();
    }

    public boolean hasNbt() {
        for (String nbt : nbt()) {
            if (nbt != null && !nbt.isEmpty()) return true;
        }
        return false;
    }

    public void addMasterworkTier(String newTier, List<ItemStat> newStats, String newNbt, int level) {
        tier.set(level, newTier);
        stats.set(level, newStats);
        nbt.set(level, newNbt);
    }

    public List<ItemStat> getStatsNoMasterwork() {
        return stats.isEmpty() ? List.of() : stats.getFirst();
    }

    public List<ItemStat> getStatsFromMasterwork(int level) {
        return stats.get(level);
    }

    public String getNbtNoMasterwork() {
        return nbt.isEmpty() ? "{}" : nbt.getFirst();
    }

    public String getNbtFromMasterwork(int level) {
        return nbt.get(level);
    }

    public String getTierNoMasterwork() {
        return tier.isEmpty() ? "" : tier.getFirst();
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
        for (List<ItemStat> itemStatsList : stats) {
            if (itemStatsList == null) continue;

            for (ItemStat itemStat : itemStatsList) {
                if (itemStat.statName().equals(stat)) return true;
            }
        }
        return false;
    }

    public double getStat(String stat) {
        double highest = -1.0;
        for (List<ItemStat> itemStatsList : stats) {
            if (itemStatsList == null) continue;

            for (ItemStat itemStat : itemStatsList) {
                if (itemStat.statName().equals(stat) && itemStat.statValue() > highest)
                    highest = itemStat.statValue();
            }
        }
        return highest;
    }

    public NbtCompound getNbt() {
        for (String nbt : nbt()) {
            if (nbt != null && !nbt.isEmpty()) {
                try {
                    return StringNbtReader.parse(nbt);
                }
                catch (CommandSyntaxException ignored) {}
            }
        }
        return null;
    }

    public void openBook() {
        ItemStack stack = ItemFactory.fromIdWithStringNbt(baseItem().toLowerCase().replace(" ", "_"), nbt.getFirst());
        BookScreen.Contents contents = BookScreen.Contents.create(stack);
        if (contents != null) {
            MinecraftClient.getInstance().setScreen(new BookScreen(contents));
        }
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
