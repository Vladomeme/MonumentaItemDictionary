package dev.eliux.monumentaitemdictionary.gui.charm;

import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.util.CharmStat;
import dev.eliux.monumentaitemdictionary.util.PlayerClass;
import dev.eliux.monumentaitemdictionary.util.ItemRegion;
import dev.eliux.monumentaitemdictionary.util.ItemTier;
import net.minecraft.item.Item;

import java.util.List;
import java.util.Objects;

public record DictionaryCharm(String name, String locationId, ItemTier tier, int power, PlayerClass itemClass,
                              Item baseItem, String lore, String nbt, List<CharmStat> stats) {

    @SuppressWarnings("SameReturnValue")
    public ItemRegion region() {
        return ItemRegion.RING;
    }

    public boolean hasLocation(String location) {
        return Mid.controller.locationData.containsKey(locationId) && Mid.controller.locationData.get(locationId).displayName().equals(location);
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
