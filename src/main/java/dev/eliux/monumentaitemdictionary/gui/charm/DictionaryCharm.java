package dev.eliux.monumentaitemdictionary.gui.charm;

import dev.eliux.monumentaitemdictionary.util.*;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record DictionaryCharm(String name, Location location, ItemTier tier, int power, PlayerClass itemClass,
                              Item baseItem, List<Text> lore, NbtCompound nbt, List<CharmStat> stats) implements Comparable<DictionaryCharm> {

    @SuppressWarnings("SameReturnValue")
    public ItemRegion region() {
        return ItemRegion.RING;
    }

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

    public float getStat(String stat) {
        for (CharmStat charmStat : stats) {
            if (charmStat.statNameFull().equals(stat)) return charmStat.statValue();
        }
        return -1.0f;
    }

    public NbtCompound getNbt() {
        return (nbt == null || nbt.isEmpty()) ? new NbtCompound() : nbt;
    }

    @Override
    public int compareTo(@NotNull DictionaryCharm o) {
        int value = tier.ordinal() - o.tier.ordinal();
        if (value != 0) return value;

        value = itemClass.ordinal() - o.itemClass.ordinal();
        if (value != 0) return value;

        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DictionaryCharm otherCharm)) return false;

        return Objects.equals(name, otherCharm.name)
                && Objects.equals(baseItem, otherCharm.baseItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, baseItem);
    }
}
