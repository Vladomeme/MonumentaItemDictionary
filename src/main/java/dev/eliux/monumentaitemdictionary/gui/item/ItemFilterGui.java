package dev.eliux.monumentaitemdictionary.gui.item;

import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import dev.eliux.monumentaitemdictionary.gui.widgets.DropdownWidget;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemIconButtonWidget;
import dev.eliux.monumentaitemdictionary.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
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

public class ItemFilterGui extends Screen {

    private final int labelMenuHeight = 30;

    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    private ItemIconButtonWidget backButton;
    private ButtonWidget addFilterButton;
    private final List<DropdownWidget> filterListOption;
    private final List<DropdownWidget> filterListValue;
    private final List<ButtonWidget> filterListComparator;
    private final List<TextFieldWidget> filterListConstant;
    private final List<ItemIconButtonWidget> filterListDelete;

    private final List<FilterType> filterTypes = List.of(FilterType.ITEM_TIER, FilterType.REGION, FilterType.ITEM_LOCATION,
            FilterType.ITEM_TYPE, FilterType.ITEM_STAT, FilterType.ITEM_BASE_ITEM, FilterType.ITEM_EFFECT);
    private final List<FilterType> filterTypesNoStat = new ArrayList<>(filterTypes) {{remove(FilterType.ITEM_STAT);}};
    private final List<Filter> itemFilters = new ArrayList<>();

    private int removeIndex = -1;

    public final DictionaryController controller;
    public final ItemDictionaryGui itemGui;

    public ItemFilterGui(Text title, DictionaryController controller) {
        super(title);
        this.controller = controller;
        this.itemGui = controller.itemGui;

        filterListOption = new ArrayList<>();
        filterListValue = new ArrayList<>();
        filterListComparator = new ArrayList<>();
        filterListConstant = new ArrayList<>();
        filterListDelete = new ArrayList<>();
    }

    public void postInit() {
        backButton = new ItemIconButtonWidget(5, 5,
                (button) -> controller.setItemDictionaryScreen(),
                Text.literal("Go Back"), Items.ARROW, "");

        addFilterButton = ButtonWidget.builder(Text.literal("Add New Filter"), (button) -> {
            int index = filterListOption.size();

            Filter filter = new Filter();
            itemFilters.add(filter);

            ButtonWidget comparator = ButtonWidget.builder(FilterComparator.EXISTS.text, b -> {
                filter.nextComparator();
                b.setMessage(filter.comparator.text);
                updateFilterOutput();
            }).position(280, labelMenuHeight + 5 + index * 25).size(60, 20).tooltip(Tooltip.of(Text.literal("Click to cycle"))).build();

            DropdownWidget value = new DropdownWidget(textRenderer, 125, labelMenuHeight + 7 + index * 25, 150,
                    Text.empty(), "", List.of(), (v) -> {
                filter.value = v;
                updateFilterOutput();
            });
            TextFieldWidget constant = new TextFieldWidget(textRenderer, 345, labelMenuHeight + 8 + index * 25, 30, 14, Text.empty());
            constant.setText("0");
            constant.setChangedListener(c -> {
                filter.setConstant(c);
                updateFilterOutput();
            });
            DropdownWidget options = new DropdownWidget(textRenderer, 30, labelMenuHeight + 7 + index * 25, 90,
                    Text.empty(), "Select Sort Type", FilterType.namesOf(filterTypes), (v) -> {
                filter.update(FilterType.of(v), value, comparator);
                updateFilterOutput();
            });
            ItemIconButtonWidget delete = new ItemIconButtonWidget(5, labelMenuHeight + 5 + index * 25,
                    b -> removeIndex = filterListOption.indexOf(options),
                    Text.literal("Delete").setStyle(Styles.RED), Items.ORANGE_STAINED_GLASS_PANE, "Cancel");

            if (itemGui.isGettingBuildItem && !(itemGui.itemTypeLookingFor == ItemType.MAINHAND)
                    || itemGui.itemTypeLookingFor == ItemType.OFFHAND)
                options.setChoices(FilterType.namesOf(filterTypesNoStat));

            filterListOption.add(options);
            filterListValue.add(value);
            filterListComparator.add(comparator);
            filterListConstant.add(constant);
            filterListDelete.add(delete);

            updateFilterListPositions();
        }).position(5, labelMenuHeight + 5).size(80, 20).build();
    }

    public void clearFilters() {
        filterListOption.clear();
        filterListValue.clear();
        filterListComparator.clear();
        filterListConstant.clear();
        filterListDelete.clear();
        itemFilters.clear();

        updateFilterOutput();
        updateFilterListPositions();
    }

    private void updateFilterListPositions() {
        if (controller.itemFilterGuiPreviouslyOpened) {
            int offset = labelMenuHeight;
            for (int i = 0; i != filterListOption.size(); i++) {
                filterListOption.get(i).setY(offset + 8);
                filterListValue.get(i).setY(offset + 8);
                filterListComparator.get(i).setY(offset + 5);
                filterListConstant.get(i).setY(offset + 8);
                filterListDelete.get(i).setY(offset + 5);
                offset += 25;
            }
            addFilterButton.setY(offset + 5);
        }
    }

    private void updateFilterOutput() {
        controller.itemFilters = new ArrayList<>(itemFilters);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        // draw filter buttons and stuff
        boolean anyOpen = false;
        for (DropdownWidget widget : filterListOption) {
            if (widget.willClick(mouseX, mouseY)) {
                anyOpen = true;
                break;
            }
        }

        if (anyOpen) addFilterButton.render(context, 0, 0, delta); // funny band-aid fix for rendering white outline while in dropdown menu
        else addFilterButton.render(context, mouseX, mouseY, delta);

        for (int i = 0; i < filterListOption.size(); i++) {
            DropdownWidget option = filterListOption.get(i);
            option.renderMain(context, mouseX, mouseY, delta);
            option.renderDropdown(context, mouseX, mouseY);

            if (!option.getLastChoice().isEmpty()) {
                DropdownWidget value = filterListValue.get(i);
                value.renderMain(context, mouseX, mouseY, delta);
                value.renderDropdown(context, mouseX, mouseY);

                filterListComparator.get(i).render(context, mouseX, mouseY, delta);
            }
            if (itemFilters.get(i).comparator.isNumeric())
                filterListConstant.get(i).render(context, mouseX, mouseY, delta);
            filterListDelete.get(i).render(context, mouseX, mouseY, delta);
        }

        // draw the label at the top
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        context.fill(0, 0, width, labelMenuHeight, Colors.GRAY);
        context.drawHorizontalLine(0, width, labelMenuHeight, Colors.WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Item Filters").setStyle(Styles.BOLD),
                width / 2, (labelMenuHeight - textRenderer.fontHeight) / 2, Colors.FILTER_TITLE);
        context.getMatrices().pop();

        // draw gui elements
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        backButton.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();

        children().forEach(element -> ((Drawable) element).render(context, mouseX, mouseY, delta));
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
            if (itemFilters.get(i).comparator.isNumeric())
                constant.setFocused(constant.mouseClicked(mouseX, mouseY, button));
        }

        addFilterButton.mouseClicked(mouseX, mouseY, button);

        if (removeIndex != -1) {
            filterListOption.remove(removeIndex);
            filterListValue.remove(removeIndex);
            filterListComparator.remove(removeIndex);
            filterListConstant.remove(removeIndex);
            filterListDelete.remove(removeIndex);
            itemFilters.remove(removeIndex);
            removeIndex = -1;

            updateFilterListPositions();
            updateFilterOutput();
        }

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
