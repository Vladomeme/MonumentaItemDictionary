package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.builder.DictionaryBuild;
import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.gui.item.ItemDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.widgets.DropdownWidget;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.eliux.monumentaitemdictionary.util.FilterComparator.Range.*;

//todo optimize for enum type comparisons instead of strings
public enum FilterType {

    UNSET ("", PRESENCE, false) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(List.of());
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            return true;
        }
    },
    REGION ("Region", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(ItemRegion.names());
        }

        Object parseValue(String value) {
            return ItemRegion.of(value);
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryItem item) return comparator.test(item.region().equals(fValue));
            else if (object instanceof DictionaryBuild build) return comparator.test(build.region.equals(fValue));
            return unexpectedType(this, object);
        }
    },

    ITEM_TIER ("Tier", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allItemTiers);
        }

        Object parseValue(String value) {
            return ItemTier.of(value);
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryItem item) return comparator.test(item.hasTier((ItemTier) fValue));
            return unexpectedType(this, object);
        }
    },
    ITEM_LOCATION ("Location", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allItemLocations);
        }

        Object parseValue(String value) {
            for (Map.Entry<String, Location> entry : Mid.controller.locationData.entrySet()) {
                if (entry.getValue().displayName().equals(value)) return entry.getKey();
            }
            return "";
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryItem item) return comparator.test(item.hasLocation((String) fValue));
            return unexpectedType(this, object);
        }
    },
    ITEM_TYPE ("Type", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            ItemDictionaryGui itemGui = Mid.controller.itemGui;

            if (itemGui.isGettingBuildItem) {
                if (itemGui.itemTypeLookingFor == ItemType.MAINHAND) widget.setChoices(ItemType.mainhandTypes());
                else if (itemGui.itemTypeLookingFor == ItemType.OFFHAND) widget.setChoices(ItemType.offhandTypes());
                else widget.setChoices(List.of(itemGui.itemTypeLookingFor));
            }
            widget.setChoices(Mid.controller.allItemTypes);
        }

        Object parseValue(String value) {
            return ItemType.of(value);
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryItem item) return comparator.test(item.type().equals(fValue));
            return unexpectedType(this, object);
        }
    },
    ITEM_STAT ("Stat", FULL, true) {
        void setChoices(DropdownWidget widget) {
            List<String> list = new ArrayList<>(Mid.controller.allItemStats.size());
            for (String s : Mid.controller.allItemStats) list.add(ItemFormatter.fromSnakeCase(s));
            widget.setChoices(Mid.controller.allItemStats, list);
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryItem item) {
                String value = (String) fValue;
                if (comparator.isPresence()) return comparator.test(item.hasStat(value));
                return comparator.test(item.hasStat(value), item.getStat(value), fConstant);
            }
            return unexpectedType(this, object);
        }
    },
    ITEM_BASE_ITEM ("Base Item", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allItemBaseItems);
        }

        Object parseValue(String value) {
            return Registries.ITEM.get(Identifier.of(value.toLowerCase().replace(" ", "_")));
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryItem item) return comparator.test(item.baseItem().equals(fValue));
            return unexpectedType(this, object);
        }
    },
    ITEM_EFFECT ("Effect", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allItemEffects);
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryItem item) return comparator.test(item.hasEffect((String) fValue));
            return unexpectedType(this, object);
        }
    },

    CHARM_TIER ("Charm Tier", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allCharmTiers);
        }

        Object parseValue(String value) {
            return ItemTier.of(value);
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) return comparator.test(charm.tier().equals(fValue));
            return unexpectedType(this, object);
        }
    },
    CHARM_LOCATION ("Charm Location", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allCharmLocations);
        }

        Object parseValue(String value) {
            for (Map.Entry<String, Location> entry : Mid.controller.locationData.entrySet()) {
                if (entry.getValue().displayName().equals(value)) return entry.getKey();
            }
            return "";
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) return comparator.test(charm.locationId().equals(fValue));
            return unexpectedType(this, object);
        }
    },
    SKILL_MODIFIER ("Skill Modifier", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            List<String> list = new ArrayList<>(Mid.controller.allCharmSkillMods.size());
            for (String s : Mid.controller.allCharmSkillMods) list.add(ItemFormatter.fromSnakeCase(s));
            widget.setChoices(Mid.controller.allCharmSkillMods, list);
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) return comparator.test(charm.hasStatModifier((String) fValue));
            return unexpectedType(this, object);
        }
    },
    CHARM_CLASS ("Class", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allCharmClasses);
        }

        Object parseValue(String value) {
            return PlayerClass.of(value);
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) return comparator.test(charm.itemClass().equals(fValue));
            return unexpectedType(this, object);
        }
    },
    CHARM_POWER ("Charm Power", NUMERIC, false) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(List.of());
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) return comparator.test(charm.power(), fConstant);
            return unexpectedType(this, object);
        }
    },
    CHARM_STAT ("Charm Stat", FULL, true) {
        void setChoices(DropdownWidget widget) {
            List<String> list = new ArrayList<>(Mid.controller.allCharmStats.size());
            for (String s : Mid.controller.allCharmStats) list.add(ItemFormatter.fromSnakeCase(s));
            widget.setChoices(Mid.controller.allCharmStats, list);
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) {
                String value = (String) fValue;
                if (comparator.isPresence()) return comparator.test(charm.hasStat(value));
                return comparator.test(charm.hasStat(value), charm.getStat(value), fConstant);
            }
            return unexpectedType(this, object);
        }
    },
    CHARM_BASE_ITEM ("Charm Base Item", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allCharmBaseItems);
        }

        Object parseValue(String value) {
            return Registries.ITEM.get(Identifier.of(value.toLowerCase().replace(" ", "_")));
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) return comparator.test(charm.baseItem().equals(fValue));
            return unexpectedType(this, object);
        }
    },

    BUILD_CLASS ("Build Class", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allCharmClasses);
        }

        Object parseValue(String value) {
            return PlayerClass.of(value);
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryBuild build) return comparator.test(build.className.equals(fValue));
            return unexpectedType(this, object);
        }
    },
    SPECIALIZATION ("Specialization", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Specialization.classSpecializations);
        }

        Object parseValue(String value) {
            return Specialization.of(value);
        }

        boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant) {
            if (object instanceof DictionaryBuild build) return comparator.test(build.specialization.equals(fValue));
            return unexpectedType(this, object);
        }
    };

    public final String name;
    public final FilterComparator.Range range;
    public final boolean hasOptions;

    FilterType(String name, FilterComparator.Range range, boolean hasOptions) {
        this.name = name;
        this.range = range;
        this.hasOptions = hasOptions;
    }

    public static FilterType of(String name) {
        return switch (name) {
            case "" -> UNSET;
            case "Region" -> REGION;

            case "Tier" -> ITEM_TIER;
            case "Location" -> ITEM_LOCATION;
            case "Type" -> ITEM_TYPE;
            case "Stat" -> ITEM_STAT;
            case "Base Item" -> ITEM_BASE_ITEM;
            case "Effect" -> ITEM_EFFECT;

            case "Charm Tier" -> CHARM_TIER;
            case "Charm Location" -> CHARM_LOCATION;
            case "Skill Modifier" -> SKILL_MODIFIER;
            case "Class" -> CHARM_CLASS;
            case "Charm Power" -> CHARM_POWER;
            case "Charm Stat" -> CHARM_STAT;
            case "Charm Base Item" -> CHARM_BASE_ITEM;

            case "Build Class" -> BUILD_CLASS;
            case "Specialization" -> SPECIALIZATION;
            default -> {
                Mid.LOGGER.info("Unknown filter: {}", name);
                yield UNSET;
            }
        };
    }

    abstract void setChoices(DropdownWidget widget);

    Object parseValue(String value) {
        return value;
    }

    abstract boolean test(Object object, FilterComparator comparator, Object fValue, double fConstant);

    @SuppressWarnings("SameReturnValue")
    private static boolean unexpectedType(FilterType type, Object object) {
        Mid.LOGGER.warn("Unexpected object type {} passed to {} filter", object.getClass(), type.name);
        return true;
    }

    public static List<String> namesOf(List<FilterType> types) {
        List<String> list = new ArrayList<>(types.size());
        for (FilterType type : types) list.add(type.name);
        return list;
    }
}
