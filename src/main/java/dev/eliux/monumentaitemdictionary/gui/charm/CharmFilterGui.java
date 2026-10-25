package dev.eliux.monumentaitemdictionary.gui.charm;

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
import java.util.List;

public class CharmFilterGui extends Screen {

    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    private final int labelMenuHeight = 30;

    private ItemIconButtonWidget backButton;
    private ButtonWidget addFilterButton;
    private final List<DropdownWidget> filterListOption;
    private final List<DropdownWidget> filterListValue;
    private final List<ButtonWidget> filterListComparator;
    private final List<TextFieldWidget> filterListConstant;
    private final List<ItemIconButtonWidget> filterListDelete;
    private final List<Filter> charmFilters = new ArrayList<>();
    private int removeIndex = -1;
    public final DictionaryController controller;

    public CharmFilterGui(Text title, DictionaryController controller) {
        super(title);
        this.controller = controller;

        filterListOption = new ArrayList<>();
        filterListValue = new ArrayList<>();
        filterListComparator = new ArrayList<>();
        filterListConstant = new ArrayList<>();
        filterListDelete = new ArrayList<>();
    }

    public void postInit() {
        backButton = new ItemIconButtonWidget(5, 5, 20, 20,
                (button) -> controller.setCharmDictionaryScreen(),
                Text.literal("Go Back"), "arrow", "");

        addFilterButton = ButtonWidget.builder(Text.literal("Add New Filter"), (button) -> {
            int index = filterListOption.size();

            Filter filter = new Filter();
            charmFilters.add(filter);

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
            }).position(310, labelMenuHeight + 5 + index * 25).size(60, 20).tooltip(Tooltip.of(Text.literal("Click to cycle"))).build();

            DropdownWidget value = new DropdownWidget(textRenderer, 125, labelMenuHeight + 7 + index * 25, 180,
                    Text.empty(), "", List.of(),
                    (v) -> {
                        filter.value = v;
                        updateFilterOutput();
            });
            TextFieldWidget constant = new TextFieldWidget(textRenderer, 375, labelMenuHeight + 8 + index * 25, 30, 14, Text.empty());
            constant.setText("0");
            constant.setChangedListener(c -> {
                try {
                    filter.constant = Double.parseDouble(c);
                }
                catch (Exception e) {
                    // error handling is stupid
                }
                updateFilterOutput();
            });
            DropdownWidget options = new DropdownWidget(textRenderer, 30, labelMenuHeight + 7 + index * 25, 90,
                    Text.empty(), "Select Sort Type",
                    Arrays.asList("Tier", "Location", "Skill Modifier", "Class", "Charm Power", "Stat", "Base Item"), (v) -> {
                filter.setOption(v);

                switch (v) {
                    case "Tier" -> {
                        value.setChoices(controller.allCharmTiers);
                        value.setDefaultText("Select Tier");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Location" -> {
                        value.setChoices(controller.allCharmLocations);
                        value.setDefaultText("Select Location");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Skill Modifier" -> {
                        List<String> vc = new ArrayList<>();
                        for (String s : controller.allCharmSkillMods) vc.add(ItemFormatter.formatCharmSkill(s));
                        value.setChoices(controller.allCharmSkillMods, vc);
                        value.setDefaultText("Select Skill Modifiers");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Class" -> {
                        value.setChoices(controller.allCharmClasses);
                        value.setDefaultText("Select Class");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Charm Power" -> comparator.setMessage(Text.literal(">="));
                    case "Stat" -> {
                        List<String> vc = new ArrayList<>();
                        for (String s : controller.allCharmStats) vc.add(ItemFormatter.formatCharmStat(s));
                        value.setChoices(controller.allCharmStats, vc);
                        value.setDefaultText("Select Stat");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Base Item" -> {
                        value.setChoices(controller.allCharmBaseItems);
                        value.setDefaultText("Select Base Item");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                }

                updateFilterOutput();
            });
            ItemIconButtonWidget delete = new ItemIconButtonWidget(5, labelMenuHeight + 5 + index * 25, 20, 20,
                    b -> removeIndex = filterListOption.indexOf(options),
                    Text.literal("Delete").setStyle(Styles.RED),
                    "orange_stained_glass_pane", "Cancel");

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
        charmFilters.clear();

        updateFilterOutput();
        updateFilterListPositions();
    }

    private void updateFilterListPositions() {
        if (controller.charmFilterGuiPreviouslyOpened) {
            addFilterButton.setY(labelMenuHeight + 5 + filterListOption.size() * 25);

            for (int i = 0; i != filterListOption.size(); i++) filterListOption.get(i).setY(labelMenuHeight + 8 + i * 25);
            for (int i = 0; i != filterListValue.size(); i++) filterListValue.get(i).setY(labelMenuHeight + 8 + i * 25);
            for (int i = 0; i != filterListComparator.size(); i++) filterListComparator.get(i).setY(labelMenuHeight + 5 + i * 25);
            for (int i = 0; i != filterListConstant.size(); i++) filterListConstant.get(i).setY(labelMenuHeight + 8 + i * 25);
            for (int i = 0; i != filterListDelete.size(); i++) filterListDelete.get(i).setY(labelMenuHeight + 5 + i * 25);
        }
    }

    private void updateFilterOutput() {
        controller.charmFilters = new ArrayList<>(charmFilters);
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

        for (DropdownWidget widget : filterListOption) widget.renderMain(context, mouseX, mouseY, delta);
        for (int i = 0; i != filterListValue.size(); i++) {
            if (!filterListOption.get(i).getLastChoice().isEmpty() && !filterListOption.get(i).getLastChoice().equals("Charm Power"))
                filterListValue.get(i).renderMain(context, mouseX, mouseY, delta);
        }
        for (int i = 0; i != filterListConstant.size(); i++) {
            if (filterListOption.get(i).getLastChoice().equals("Stat") && !(charmFilters.get(i).comparator < 2)
                    || filterListOption.get(i).getLastChoice().equals("Charm Power"))
                filterListConstant.get(i).render(context, mouseX, mouseY, delta);
        }
        for (int i = 0; i != filterListComparator.size(); i++) {
            if (!filterListOption.get(i).getLastChoice().isEmpty())
                filterListComparator.get(i).render(context, mouseX, mouseY, delta);
        }
        for (ItemIconButtonWidget widget : filterListDelete) widget.render(context, mouseX, mouseY, delta);
        for (DropdownWidget widget : filterListOption) widget.renderDropdown(context, mouseX, mouseY);
        for (int i = 0; i != filterListValue.size(); i++) {
            if (!filterListOption.get(i).getLastChoice().isEmpty())
                filterListValue.get(i).renderDropdown(context, mouseX, mouseY);
        }

        // draw the label at the top
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        context.fill(0, 0, width, labelMenuHeight, Colors.GRAY);
        context.drawHorizontalLine(0, width, labelMenuHeight, Colors.WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Charm Filters").setStyle(Styles.BOLD),
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

        for (DropdownWidget widget : filterListOption) {
            if (widget.willClick(mouseX, mouseY)) {
                widget.mouseClicked(mouseX, mouseY, button);
                return false;
            }
            widget.mouseClicked(mouseX, mouseY, button);
        }
        for (int i = 0; i < filterListValue.size(); i++) {
            DropdownWidget widget = filterListValue.get(i);
            if (widget.willClick(mouseX, mouseY)) {
                if (!filterListOption.get(i).getLastChoice().isEmpty() && !filterListOption.get(i).getLastChoice().equals("Charm Power"))
                    widget.mouseClicked(mouseX, mouseY, button);
                return false;
            }
            if (!filterListOption.get(i).getLastChoice().isEmpty())
                widget.mouseClicked(mouseX, mouseY, button);
        }
        for (int i = 0; i < filterListComparator.size(); i++) {
            if (!filterListOption.get(i).getLastChoice().isEmpty())
                filterListComparator.get(i).mouseClicked(mouseX, mouseY, button);
        }
        for (int i = 0; i < filterListConstant.size(); i++) {
            TextFieldWidget widget = filterListConstant.get(i);
            if (filterListOption.get(i).getLastChoice().equals("Stat") && !(charmFilters.get(i).comparator < 2)
                    || filterListOption.get(i).getLastChoice().equals("Charm Power"))
                widget.setFocused(widget.mouseClicked(mouseX, mouseY, button));
        }
        for (ItemIconButtonWidget widget : filterListDelete) widget.mouseClicked(mouseX, mouseY, button);

        addFilterButton.mouseClicked(mouseX, mouseY, button);

        if (removeIndex != -1) {
            filterListOption.remove(removeIndex);
            filterListValue.remove(removeIndex);
            filterListComparator.remove(removeIndex);
            filterListConstant.remove(removeIndex);
            filterListDelete.remove(removeIndex);
            charmFilters.remove(removeIndex);
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
