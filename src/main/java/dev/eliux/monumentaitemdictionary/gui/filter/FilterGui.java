package dev.eliux.monumentaitemdictionary.gui.filter;

import dev.eliux.monumentaitemdictionary.gui.LazyScreen;
import dev.eliux.monumentaitemdictionary.gui.widgets.DropdownWidget;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemIconButtonWidget;
import dev.eliux.monumentaitemdictionary.util.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FilterGui extends Screen implements LazyScreen {

    private final int labelMenuHeight = 30;

    private ItemIconButtonWidget backButton;
    private ButtonWidget addFilterButton;
    private final List<DropdownWidget> filterListOption;
    private final List<DropdownWidget> filterListValue;
    private final List<ButtonWidget> filterListComparator;
    private final List<TextFieldWidget> filterListConstant;
    private final List<ItemIconButtonWidget> filterListDelete;

    public final List<Filter> filters = new ArrayList<>();

    public final List<FilterType> filterTypes;
    public final Runnable returnFunction;
    public final Consumer<List<Filter>> filterOutput;
    public final int valueFieldWidth;
    public final String label;

    private boolean initialized = false;

    public FilterGui(String title, String label, int valueFieldWidth, List<FilterType> filterTypes,
                     Runnable returnFunction, Consumer<List<Filter>> filterOutput) {
        super(Text.of(title));
        this.filterTypes = filterTypes;
        this.valueFieldWidth = valueFieldWidth;
        this.label = label;
        this.returnFunction = returnFunction;
        this.filterOutput = filterOutput;

        filterListOption = new ArrayList<>();
        filterListValue = new ArrayList<>();
        filterListComparator = new ArrayList<>();
        filterListConstant = new ArrayList<>();
        filterListDelete = new ArrayList<>();
    }

    public void postInit() {
        if (initialized) return;
        initialized = true;

        backButton = new ItemIconButtonWidget(5, 5, button -> returnFunction.run(),
                Text.literal("Go Back"), Items.ARROW, "");

        addFilterButton = ButtonWidget.builder(Text.literal("Add New Filter"), filterButton -> {
            int index = filterListOption.size();

            Filter filter = new Filter();
            filters.add(filter);

            ButtonWidget comparatorWidget = ButtonWidget.builder(FilterComparator.EXISTS.text, button -> {
                filter.nextComparator();
                button.setMessage(filter.getComparatorText());
                filterOutput.accept(filters);
            }).position(130 + valueFieldWidth, labelMenuHeight + 5 + index * 25).size(60, 20)
                    .tooltip(Tooltip.of(Text.literal("Click to cycle"))).build();

            DropdownWidget valueWidget = new DropdownWidget(textRenderer, 125, labelMenuHeight + 7 + index * 25, valueFieldWidth,
                    Text.empty(), "", List.of(), value -> {
                filter.setValue(value);
                filterOutput.accept(filters);
            });
            TextFieldWidget constantWidget = new TextFieldWidget(textRenderer, 195 + valueFieldWidth, labelMenuHeight + 8 + index * 25, 30, 14, Text.empty());
            constantWidget.setText("0");
            constantWidget.setChangedListener(constant -> {
                filter.setConstant(constant);
                filterOutput.accept(filters);
            });
            DropdownWidget optionsWidget = new DropdownWidget(textRenderer, 30, labelMenuHeight + 7 + index * 25, 90,
                    Text.empty(), "Select Sort Type", FilterType.namesOf(filterTypes), value -> {
                filter.update(FilterType.of(value), valueWidget, comparatorWidget);
                filterOutput.accept(filters);
            });
            ItemIconButtonWidget deleteWidget = new ItemIconButtonWidget(5, labelMenuHeight + 5 + index * 25,
                    button -> remove(filterListOption.indexOf(optionsWidget)),
                    Text.literal("Delete").setStyle(Styles.RED), Items.ORANGE_STAINED_GLASS_PANE, "Cancel");

            filterListOption.add(optionsWidget);
            filterListValue.add(valueWidget);
            filterListComparator.add(comparatorWidget);
            filterListConstant.add(constantWidget);
            filterListDelete.add(deleteWidget);

            updateFilterListPositions();
        }).position(5, labelMenuHeight + 5).size(80, 20).build();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        boolean anyOpen = false;
        for (DropdownWidget widget : filterListOption) {
            if (widget.willClick(mouseX, mouseY)) {
                anyOpen = true;
                break;
            }
        }

        if (anyOpen) addFilterButton.render(context, 0, 0, delta);
        else addFilterButton.render(context, mouseX, mouseY, delta);

        for (int i = 0; i < filterListOption.size(); i++) {
            DropdownWidget option = filterListOption.get(i);
            option.renderMain(context, mouseX, mouseY, delta);
            option.renderDropdown(context, mouseX, mouseY);

            if (!option.getLastChoice().isEmpty()) {
                DropdownWidget value = filterListValue.get(i);
                if (FilterType.of(filterListOption.get(i).getLastChoice()).hasOptions)
                    value.renderMain(context, mouseX, mouseY, delta);
                value.renderDropdown(context, mouseX, mouseY);

                filterListComparator.get(i).render(context, mouseX, mouseY, delta);
            }
            if (filters.get(i).isNumeric())
                filterListConstant.get(i).render(context, mouseX, mouseY, delta);
            filterListDelete.get(i).render(context, mouseX, mouseY, delta);
        }

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        context.fill(0, 0, width, labelMenuHeight, Colors.GRAY);
        context.drawHorizontalLine(0, width, labelMenuHeight, Colors.WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(label).setStyle(Styles.BOLD),
                width / 2, (labelMenuHeight - textRenderer.fontHeight) / 2, Colors.FILTER_TITLE);
        context.getMatrices().pop();

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        backButton.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();

        children().forEach(element -> ((Drawable) element).render(context, mouseX, mouseY, delta));
    }

    private void updateFilterListPositions() {
        int offset = labelMenuHeight;
        if (!filterListOption.isEmpty()) {
            for (int i = 0; i != filterListOption.size(); i++) {
                filterListOption.get(i).setY(offset + 8);
                filterListValue.get(i).setY(offset + 8);
                filterListComparator.get(i).setY(offset + 5);
                filterListConstant.get(i).setY(offset + 8);
                filterListDelete.get(i).setY(offset + 5);
                offset += 25;
            }
        }
        if (addFilterButton != null) addFilterButton.setY(offset + 5);
    }

    public void clearFilters() {
        filterListOption.clear();
        filterListValue.clear();
        filterListComparator.clear();
        filterListConstant.clear();
        filterListDelete.clear();
        filters.clear();

        filterOutput.accept(filters);
        updateFilterListPositions();
    }

    public void remove(int i) {
        filterListOption.remove(i);
        filterListValue.remove(i);
        filterListComparator.remove(i);
        filterListConstant.remove(i);
        filterListDelete.remove(i);
        filters.remove(i);

        filterOutput.accept(filters);
        updateFilterListPositions();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        backButton.mouseClicked(mouseX, mouseY, button);

        for (int i = 0; i < filterListOption.size(); i++) {
            filterListDelete.get(i).mouseClicked(mouseX, mouseY, button);

            DropdownWidget option = filterListOption.get(i);
            DropdownWidget value = filterListValue.get(i);
            TextFieldWidget constant = filterListConstant.get(i);

            if (option.mouseClicked(mouseX, mouseY, button)) {
                mouseX = -1000;
                value.setText("");
                value.setFocused(false);
                constant.setText("");
                constant.setFocused(false);
            }

            if (!option.getLastChoice().isEmpty()) {
                if (FilterType.of(filterListOption.get(i).getLastChoice()).hasOptions) {
                    if (value.mouseClicked(mouseX, mouseY, button)) mouseX = -1000;
                }
                if (filterListComparator.get(i).mouseClicked(mouseX, mouseY, button)) mouseX = -1000;
            }
            if (filters.get(i).isNumeric())
                constant.setFocused(constant.mouseClicked(mouseX, mouseY, button));
        }
        addFilterButton.mouseClicked(mouseX, mouseY, button);

        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        super.charTyped(chr, modifiers);

        for (DropdownWidget widget : filterListOption) widget.charTyped(chr, modifiers);
        for (DropdownWidget widget : filterListValue) widget.charTyped(chr, modifiers);
        for (TextFieldWidget widget : filterListConstant) widget.charTyped(chr, modifiers);

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);

        for (DropdownWidget widget : filterListOption) widget.keyPressed(keyCode, scanCode, modifiers);
        for (DropdownWidget widget : filterListValue) widget.keyPressed(keyCode, scanCode, modifiers);
        for (TextFieldWidget widget : filterListConstant) widget.keyPressed(keyCode, scanCode, modifiers);

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);

        for (DropdownWidget widget : filterListOption) widget.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
        for (DropdownWidget widget : filterListValue) widget.mouseScrolled(mouseX, mouseY, hAmount, vAmount);

        return true;
    }
}
