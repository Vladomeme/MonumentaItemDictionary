package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.builder.DictionaryBuild;
import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.gui.item.ItemDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.widgets.DropdownWidget;

import java.util.ArrayList;
import java.util.List;

import static dev.eliux.monumentaitemdictionary.util.FilterComparator.Range.*;

//todo optimize for enum type comparisons instead of strings
public enum FilterType {

    UNSET ("", PRESENCE, false) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(List.of());
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            return true;
        }
    },
    REGION ("Region", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(ItemRegion.names());
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryItem item) return comparator.test(item.region().apiName.equals(fValue));
            else if (object instanceof DictionaryBuild build) return comparator.test(build.region.apiName.equals(fValue));
            return unexpectedType(this, object);
        }
    },

    ITEM_TIER ("Tier", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allItemTiers);
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryItem item) return comparator.test(item.hasTier(fValue));
            return unexpectedType(this, object);
        }
    },
    ITEM_LOCATION ("Location", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allItemLocations);
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryItem item) return comparator.test(item.hasLocation(fValue));
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

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryItem item) return comparator.test(item.type() == ItemType.of(fValue));
            return unexpectedType(this, object);
        }
    },
    ITEM_STAT ("Stat", FULL, true) {
        void setChoices(DropdownWidget widget) {
            List<String> list = new ArrayList<>(Mid.controller.allItemStats.size());
            for (String s : Mid.controller.allItemStats) list.add(ItemFormatter.fromSnakeCase(s));
            widget.setChoices(Mid.controller.allItemStats, list);
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryItem item) {
                if (comparator.isPresence()) return comparator.test(item.hasStat(fValue));
                return comparator.test(item.hasStat(fValue), item.getStat(fValue), fConstant);
            }
            return unexpectedType(this, object);
        }
    },
    ITEM_BASE_ITEM ("Base Item", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allItemBaseItems);
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryItem item) return comparator.test(item.baseItem().getName().getString().equals(fValue));
            return unexpectedType(this, object);
        }
    },
    ITEM_EFFECT ("Effect", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allItemEffects);
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryItem item) return comparator.test(item.hasEffects() && item.effects().contains(fValue));
            return unexpectedType(this, object);
        }
    },

    CHARM_TIER ("Charm Tier", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allCharmTiers);
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) return comparator.test(charm.tier().name.equals(fValue));
            return unexpectedType(this, object);
        }
    },
    CHARM_LOCATION ("Charm Location", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allCharmLocations);
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) return comparator.test(charm.hasLocation(fValue));
            return unexpectedType(this, object);
        }
    },
    SKILL_MODIFIER ("Skill Modifier", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            List<String> list = new ArrayList<>(Mid.controller.allCharmSkillMods.size());
            for (String s : Mid.controller.allCharmSkillMods) list.add(ItemFormatter.fromSnakeCase(s));
            widget.setChoices(Mid.controller.allCharmSkillMods, list);
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) return comparator.test(charm.hasStatModifier(fValue));
            return unexpectedType(this, object);
        }
    },
    CHARM_CLASS ("Class", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allCharmClasses);
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) return comparator.test(charm.itemClass().name.equals(fValue));
            return unexpectedType(this, object);
        }
    },
    CHARM_POWER ("Charm Power", NUMERIC, false) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(List.of());
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
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

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) {
                if (comparator.isPresence()) return comparator.test(charm.hasStat(fValue));
                return comparator.test(charm.hasStat(fValue), charm.getStat(fValue), fConstant);
            }
            return unexpectedType(this, object);
        }
    },
    CHARM_BASE_ITEM ("Charm Base Item", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allCharmBaseItems);
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryCharm charm) return comparator.test(charm.baseItem().getName().getString().equals(fValue));
            return unexpectedType(this, object);
        }
    },

    BUILD_CLASS ("Build Class", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Mid.controller.allCharmClasses);
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryBuild build) return comparator.test(build.className.name.equals(fValue));
            return unexpectedType(this, object);
        }
    },
    SPECIALIZATION ("Specialization", PRESENCE, true) {
        void setChoices(DropdownWidget widget) {
            widget.setChoices(Specialization.classSpecializations);
        }

        boolean test(Object object, FilterComparator comparator, String fValue, double fConstant) {
            if (object instanceof DictionaryBuild build) return comparator.test(build.specialization.name.equals(fValue));
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

    public static FilterType of(String filter) {
        return switch (filter) {
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
            default -> UNSET;
        };
    }

    abstract void setChoices(DropdownWidget widget);

    abstract boolean test(Object object, FilterComparator comparator, String fValue, double fConstant);

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
