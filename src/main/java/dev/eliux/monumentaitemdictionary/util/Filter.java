package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.gui.widgets.DropdownWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class Filter {

    private FilterType type;
    private FilterComparator comparator;
    private Object value;
    private double constant;

    public Filter() {
        type = FilterType.UNSET;
        comparator = FilterComparator.EXISTS;
        value = null;
        constant = 0.0;
    }

    public void update(FilterType type, DropdownWidget valueWidget, ButtonWidget comparatorWidget) {
        this.type = type;
        comparator = type.range.start;

        type.setChoices(valueWidget);
        valueWidget.setDefaultText(type == FilterType.UNSET ? "" : "Select " + type.name);
        comparatorWidget.setMessage(comparator.text);
    }

    public void setValue(String value) {
        if (value.isEmpty()) this.value = null;
        else {
            this.value = type.parseValue(value);
            if (this.value instanceof String s && s.isEmpty()) this.value = null;
        }
    }

    public void setConstant(String input) {
        try {
            constant = Double.parseDouble(input);
        }
        catch (Exception ignored) {}
    }

    public boolean isEmpty() {
        return value == null && type.hasOptions;
    }

    public boolean blocks(Object object) {
        return !type.test(object, comparator, value, constant);
    }

    public FilterType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public Text getComparatorText() {
        return comparator.text;
    }

    public boolean isNumeric() {
        return comparator.isNumeric();
    }

    public void nextComparator() {
        comparator = comparator.next();
        if (comparator.ordinal() < type.range.start.ordinal()
                || comparator.ordinal() > type.range.end.ordinal()) comparator = type.range.start;
    }
}
