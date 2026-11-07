package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.gui.widgets.DropdownWidget;
import net.minecraft.client.gui.widget.ButtonWidget;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Filter {

    public static final List<String> mainhandTypes = List.of("Mainhand", "Mainhand Sword", "Mainhand Shield",
            "Wand", "Axe", "Pickaxe", "Trident", "Snowball", "Shovel", "Scythe", "Bow", "Crossbow");
    public static final Set<String> offhandTypes = new HashSet<>(List.of("Offhand", "Offhand Sword", "Offhand Shield"));

    private FilterType type;
    public FilterComparator comparator;
    public String value;
    public double constant;

    public Filter() {
        type = FilterType.UNSET;
        comparator = FilterComparator.EXISTS;
        value = "";
        constant = 0.0;
    }

    public void update(FilterType type, DropdownWidget valueWidget, ButtonWidget comparatorWidget) {
        this.type = type;
        comparator = type.range.start;

        type.setChoices(valueWidget);
        valueWidget.setDefaultText(type == FilterType.UNSET ? "" : "Select " + type.name);
        comparatorWidget.setMessage(comparator.text);
    }

    public void setConstant(String input) {
        try {
            constant = Double.parseDouble(input);
        }
        catch (Exception ignored) {}
    }

    public boolean isEmpty() {
        return value.isEmpty() && type.hasOptions;
    }

    public boolean blocks(Object object) {
        return !type.test(object, comparator, value, constant);
    }

    public FilterType getType() {
        return type;
    }

    public void nextComparator() {
        comparator = comparator.next();
        if (comparator.ordinal() < type.range.start.ordinal()
                || comparator.ordinal() > type.range.end.ordinal()) comparator = type.range.start;
    }
}
