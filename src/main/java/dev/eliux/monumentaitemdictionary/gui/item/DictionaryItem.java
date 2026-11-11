package dev.eliux.monumentaitemdictionary.gui.item;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.util.*;

import java.util.List;
import java.util.Objects;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import org.jetbrains.annotations.NotNull;

public record DictionaryItem(String name, ItemType type, ItemRegion region, List<ItemTier> tier, String locationId,
                             int fishTier, boolean isFish, Item baseItem, String lore, List<String> nbt,
                             List<List<ItemStat>> stats, List<String> effects, boolean hasMasterwork) implements Comparable<DictionaryItem> {

    public boolean hasRegion() {
        return region != ItemRegion.DEFAULT;
    }

    public boolean hasTier() {
        return !tier.isEmpty();
    }

    public boolean hasTier(String tier) {
        if (this.tier.isEmpty()) return tier.equals(ItemTier.DEFAULT.name);

        for (ItemTier itemTier : this.tier) {
            if (itemTier != null && itemTier.name.equals(tier)) return true;
        }
        return false;
    }

    public boolean hasLocation() {
        return !locationId.isEmpty();
    }

    public boolean hasLocation(String location) {
        return !locationId.isEmpty() && Mid.controller.locationData.containsKey(locationId)
                && Mid.controller.locationData.get(locationId).displayName().equals(location);
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

    public void addMasterworkTier(ItemTier newTier, List<ItemStat> newStats, String newNbt, int level) {
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

    public ItemTier getTier() {
        if (hasMasterwork) return tier.get(getMinMasterwork());
        return tier.isEmpty() ? ItemTier.DEFAULT : tier.getFirst();
    }

    public ItemTier getTierAtMasterwork(int level) {
        if (hasMasterwork) return tier.get(Math.max(level, getMinMasterwork()));
        return tier.isEmpty() ? ItemTier.DEFAULT : tier.getFirst();
    }

    public ItemTier getTierAtMaxMasterwork() {
        if (hasMasterwork) return tier.get(getMaxMasterwork() - 1);
        return tier.isEmpty() ? ItemTier.DEFAULT : tier.getFirst();
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
        for (int i = stats.size() - 1; i >= 0; i--) {
            List<ItemStat> statList = stats.get(i);
            if (statList == null) continue;

            for (ItemStat itemStat : statList) {
                if (itemStat.statName().equals(stat)) return true;
            }
        }
        return false;
    }

    public float getStat(String stat) {
        for (int i = stats.size() - 1; i >= 0; i--) {
            List<ItemStat> statList = stats.get(i);
            if (statList == null) continue;

            for (ItemStat itemStat : statList) {
                if (itemStat.statName().equals(stat)) return itemStat.statValue();
            }
        }
        return -1.0f;
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
        ItemStack stack = ItemFactory.fromBaseWithStringNbt(baseItem, nbt.getFirst());
        BookScreen.Contents contents = BookScreen.Contents.create(stack);
        if (contents != null) {
            MinecraftClient.getInstance().setScreen(new BookScreen(contents));
        }
    }

    @Override
    public int compareTo(@NotNull DictionaryItem o) {
        int value = o.region.ordinal() - region.ordinal();
        if (value != 0) return value;

        value = getTier().ordinal() - o.getTier().ordinal();
        if (value != 0) return value;

        if (isFish && o.isFish) {
            value = o.fishTier - fishTier;
            if (value != 0) return value;
        }

        value = o.type.ordinal() - type.ordinal();
        if (value != 0) return value;

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
