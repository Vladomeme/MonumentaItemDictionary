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
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        backButton = new ItemIconButtonWidget(5, 5, 20, 20,
                (button) -> controller.setItemDictionaryScreen(), Text.literal("Go Back"), "arrow", "");

        addFilterButton = ButtonWidget.builder(Text.literal("Add New Filter"), (button) -> {
            int index = filterListOption.size();

            Filter filter = new Filter();
            itemFilters.add(filter);

            ButtonWidget comparator = ButtonWidget.builder(Text.literal("Matches"), b -> {
                filter.incrementComparator();
                b.setMessage(Text.literal(
                        switch (filter.comparator) {
                            case 0 -> "Matches";
                            case 1 -> "Excludes";
                            case 2 -> ">=";
                            case 3 -> ">";
                            case 4 -> "=";
                            case 5 -> "<=";
                            case 6 -> "<";
                            default -> "Error";
                        }
                ));

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
                try {
                    filter.constant = Double.parseDouble(c);
                } catch (Exception e) {
                    // error handling is stupid
                }
                updateFilterOutput();
            });
            DropdownWidget options = new DropdownWidget(textRenderer, 30, labelMenuHeight + 7 + index * 25, 90,
                    Text.empty(), "Select Sort Type", Arrays.asList("Tier", "Region", "Location", "Type", "Stat", "Base Item", "Effect"), (v) -> {
                filter.setOption(v);
                    switch (v) {
                    case "Tier" -> {
                        value.setChoices(controller.allItemTiers);
                        value.setDefaultText("Select Tier");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Region" -> {
                        value.setChoices(controller.allItemRegions);
                        value.setDefaultText("Select Region");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Location" -> {
                        value.setChoices(controller.allItemLocations);
                        value.setDefaultText("Select Location");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Stat" -> {
                        List<String> vc = new ArrayList<>();
                        for (String s : controller.allItemStats) vc.add(ItemFormatter.formatStat(s));
                        value.setChoices(controller.allItemStats, vc);
                        value.setDefaultText("Select Stat");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Base Item" -> {
                        value.setChoices(controller.allItemBaseItems);
                        value.setDefaultText("Select Base Item");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Type" -> {
                        List<String> allTypes = controller.allItemTypes;
                        List<String> mainhandTypes = new ArrayList<>(Arrays.asList("Mainhand",
                                "Mainhand Sword", "Mainhand Shield", "Wand", "Axe", "Pickaxe", "Trident",
                                "Snowball", "Shovel", "Scythe", "Bow", "Crossbow"));
                        List<String> offhandTypes = new ArrayList<>(Arrays.asList("Offhand", "Offhand Sword", "Offhand Shield"));
                        List<String> oneType = new ArrayList<>(Collections.singletonList(itemGui.itemTypeLookingFor));
                        if (itemGui.isGettingBuildItem && itemGui.itemTypeLookingFor.equals("Mainhand")) value.setChoices(mainhandTypes);
                        else if (itemGui.isGettingBuildItem && itemGui.itemTypeLookingFor.equals("Offhand")) value.setChoices(offhandTypes);
                        else if (itemGui.isGettingBuildItem) value.setChoices(oneType);
                        else value.setChoices(allTypes);
                        value.setDefaultText("Select Type");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Effect" -> {
                        value.setChoices(controller.allItemEffects);
                        value.setDefaultText("Select Effect");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                }

                updateFilterOutput();
            });
            ItemIconButtonWidget delete = new ItemIconButtonWidget(5, labelMenuHeight + 5 + index * 25, 20, 20,
                    b -> removeIndex = filterListOption.indexOf(options),
                    Text.literal("Delete").setStyle(Styles.RED),
                    "orange_stained_glass_pane", "Cancel");

            if (itemGui.isGettingBuildItem && !(itemGui.itemTypeLookingFor.equals("Mainhand")
                    || itemGui.itemTypeLookingFor.equals("Offhand")))
                options.setChoices(Arrays.asList("Tier", "Region", "Location", "Stat", "Base Item", "Effect"));

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
            addFilterButton.setY(labelMenuHeight + 5 + filterListOption.size() * 25);

            filterListOption.forEach(i -> i.setY(labelMenuHeight + 8 + filterListOption.indexOf(i) * 25));
            filterListValue.forEach(i -> i.setY(labelMenuHeight + 8 + filterListValue.indexOf(i) * 25));
            filterListComparator.forEach(i -> i.setY(labelMenuHeight + 5 + filterListComparator.indexOf(i) * 25));
            filterListConstant.forEach(i -> i.setY(labelMenuHeight + 8 + filterListConstant.indexOf(i) * 25));
            filterListDelete.forEach(i -> i.setY(labelMenuHeight + 5 + filterListDelete.indexOf(i) * 25));
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
        for (DropdownWidget o : filterListOption) if (o.willClick(mouseX, mouseY)) anyOpen = true;

        if (anyOpen) addFilterButton.render(context, 0, 0, delta); // funny band-aid fix for rendering white outline while in dropdown menu
        else addFilterButton.render(context, mouseX, mouseY, delta);

        for (DropdownWidget o : filterListOption) o.renderMain(context, mouseX, mouseY, delta);
        for (DropdownWidget v : filterListValue) {
            if (!filterListOption.get(filterListValue.indexOf(v)).getLastChoice().isEmpty())
                v.renderMain(context, mouseX, mouseY, delta);
        }
        for (TextFieldWidget c : filterListConstant) {
            if (filterListOption.get(filterListConstant.indexOf(c)).getLastChoice().equals("Stat") && !(itemFilters.get(filterListConstant.indexOf(c)).comparator < 2))
                c.render(context, mouseX, mouseY, delta);
        }
        for (ButtonWidget c : filterListComparator) {
            if (!filterListOption.get(filterListComparator.indexOf(c)).getLastChoice().isEmpty())
                c.render(context, mouseX, mouseY, delta);
        }
        filterListDelete.forEach(i -> i.render(context, mouseX, mouseY, delta));

        for (DropdownWidget o : filterListOption) o.renderDropdown(context, mouseX, mouseY);
        for (DropdownWidget v : filterListValue) {
            if (!filterListOption.get(filterListValue.indexOf(v)).getLastChoice().isEmpty())
                v.renderDropdown(context, mouseX, mouseY);
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

        for (DropdownWidget o : filterListOption) {
            if (o.willClick(mouseX, mouseY)) {
                o.mouseClicked(mouseX, mouseY, button);
                return false;
            }
            o.mouseClicked(mouseX, mouseY, button);
        }
        for (DropdownWidget v : filterListValue) {
            if (v.willClick(mouseX, mouseY)) {
                if (!filterListOption.get(filterListValue.indexOf(v)).getLastChoice().isEmpty())
                    v.mouseClicked(mouseX, mouseY, button);
                return false;
            }
            if (!filterListOption.get(filterListValue.indexOf(v)).getLastChoice().isEmpty())
                v.mouseClicked(mouseX, mouseY, button);
        }
        for (ButtonWidget c : filterListComparator) {
            if (!filterListOption.get(filterListComparator.indexOf(c)).getLastChoice().isEmpty())
                c.mouseClicked(mouseX, mouseY, button);
        }
        for (TextFieldWidget c : filterListConstant) {
            if (filterListOption.get(filterListConstant.indexOf(c)).getLastChoice().equals("Stat") && !(itemFilters.get(filterListConstant.indexOf(c)).comparator < 2))
                c.setFocused(c.mouseClicked(mouseX, mouseY, button));
        }
        filterListDelete.forEach(i -> i.mouseClicked(mouseX, mouseY, button));

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

        filterListOption.forEach(i -> i.charTyped(chr, modifiers));
        filterListValue.forEach(i -> i.charTyped(chr, modifiers));
        filterListConstant.forEach(i -> i.charTyped(chr, modifiers));

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);

        filterListOption.forEach(i -> i.keyPressed(keyCode, scanCode, modifiers));
        filterListValue.forEach(i -> i.keyPressed(keyCode, scanCode, modifiers));
        filterListConstant.forEach(i -> i.keyPressed(keyCode, scanCode, modifiers));

        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);

        filterListOption.forEach(i -> i.mouseScrolled(mouseX, mouseY, hAmount, vAmount));
        filterListValue.forEach(i -> i.mouseScrolled(mouseX, mouseY, hAmount, vAmount));

        return true;
    }
}
