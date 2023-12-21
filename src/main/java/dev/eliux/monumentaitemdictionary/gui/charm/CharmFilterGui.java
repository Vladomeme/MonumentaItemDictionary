package dev.eliux.monumentaitemdictionary.gui.charm;

import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import dev.eliux.monumentaitemdictionary.gui.widgets.DropdownWidget;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemIconButtonWidget;
import dev.eliux.monumentaitemdictionary.util.Filter;
import dev.eliux.monumentaitemdictionary.util.ItemFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;

public class CharmFilterGui extends Screen {
    private final int labelMenuHeight = 30;

    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    private ItemIconButtonWidget backButton;
    private ButtonWidget addFilterButton;
    private ArrayList<DropdownWidget> filterListOption;
    private ArrayList<DropdownWidget> filterListValue;
    private ArrayList<ButtonWidget> filterListComparator;
    private ArrayList<TextFieldWidget> filterListConstant;
    private ArrayList<ItemIconButtonWidget> filterListDelete;
    //private ArrayList<ItemIconButtonWidget> filterListDuplicate;

    private ArrayList<Filter> charmFilters = new ArrayList<>();

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
        //filterListDuplicate = new ArrayList<>();
    }

    public void postInit() {
        backButton = new ItemIconButtonWidget(5, 5, 20, 20, Text.literal(""), (button) -> {
            controller.setCharmDictionaryScreen();
        }, Text.literal("Go Back"), "arrow", "");

        addFilterButton = ButtonWidget.builder(Text.literal("Add New Filter"), (button) -> {
            int index = filterListOption.size();

            Filter filter = new Filter();
            charmFilters.add(filter);

            ButtonWidget comparator = ButtonWidget.builder(Text.literal("Matches"), b -> {
                filter.incrementComparator();
                b.setMessage(Text.literal(
                        switch (filter.comparator) {
                            case 0: yield "Matches";
                            case 1: yield "Excludes";
                            case 2: yield ">=";
                            case 3: yield ">";
                            case 4: yield "=";
                            case 5: yield "<=";
                            case 6: yield "<";
                            default: yield "Error";
                        }
                ));

                updateFilterOutput();
            }).position(310, labelMenuHeight + 5 + index * 25).size(60, 20).tooltip(Tooltip.of(Text.literal("Click to cycle"))).build();

            DropdownWidget value = new DropdownWidget(textRenderer, 125, labelMenuHeight + 7 + index * 25, 180, Text.literal(""), "", Arrays.asList(), (v) -> {
                filter.value = v;
                updateFilterOutput();
            });
            TextFieldWidget constant = new TextFieldWidget(textRenderer, 375, labelMenuHeight + 8 + index * 25, 30, 14, Text.literal(""));
            constant.setText("0");
            constant.setChangedListener(c -> {
                try {
                    filter.constant = Double.parseDouble(c);
                } catch (Exception e) {
                    // error handling is stupid
                }
                updateFilterOutput();
            });
            DropdownWidget options = new DropdownWidget(textRenderer, 30, labelMenuHeight + 7 + index * 25, 90, Text.literal(""), "Select Sort Type", Arrays.asList("Tier", "Location", "Skill Modifier", "Class", "Charm Power", "Stat", "Base Item"), (v) -> {
                filter.setOption(v);

                switch (v) {
                    case "Tier" -> {
                        value.setChoices(controller.getAllCharmTiers());
                        value.setDefaultText("Select Tier");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Location" -> {
                        value.setChoices(controller.getAllCharmLocations());
                        value.setDefaultText("Select Location");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Skill Modifier" -> {
                        ArrayList<String> vc = new ArrayList<>();
                        for (String s : controller.getAllCharmSkillMods()) vc.add(ItemFormatter.formatCharmSkill(s));
                        value.setChoices(controller.getAllCharmSkillMods(), vc);
                        value.setDefaultText("Select Skill Modifiers");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Class" -> {
                        value.setChoices(controller.getAllCharmClasses());
                        value.setDefaultText("Select Class");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Charm Power" -> {
                        comparator.setMessage(Text.literal(">="));
                    }
                    case "Stat" -> {
                        ArrayList<String> vc = new ArrayList<>();
                        for (String s : controller.getAllCharmStats()) vc.add(ItemFormatter.formatCharmStat(s));
                        value.setChoices(controller.getAllCharmStats(), vc);
                        value.setDefaultText("Select Stat");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                    case "Base Item" -> {
                        value.setChoices(controller.getAllCharmBaseItems());
                        value.setDefaultText("Select Base Item");
                        comparator.setMessage(Text.literal("Matches"));
                    }
                }

                updateFilterOutput();
            });
            ItemIconButtonWidget delete = new ItemIconButtonWidget(5, labelMenuHeight + 5 + index * 25, 20, 20, Text.literal(""), b -> {
                removeIndex = filterListOption.indexOf(options);
            }, Text.literal("Delete").setStyle(Style.EMPTY.withColor(0xFF0000)), "orange_stained_glass_pane", "Cancel");
            /*
            ItemIconButtonWidget duplicate = new ItemIconButtonWidget(30, labelMenuHeight + 5 + index * 25, 20, 20, Text.literal(""), b -> {

            }, ((button1, matrices, mouseX, mouseY) -> {
                renderTooltip(context, Text.literal("Duplicate").setStyle(Style.EMPTY.withColor(0x4444FF)), mouseX, mouseY);
            }), "blue_stained_glass_pane", "");
             */

            filterListOption.add(options);
            filterListValue.add(value);
            filterListComparator.add(comparator);
            filterListConstant.add(constant);
            filterListDelete.add(delete);
            //filterListDuplicate.add(duplicate);

            updateFilterListPositions();
        }).position(5, labelMenuHeight + 5).size(80, 20).build();
    }

    public void clearFilters() {
        filterListOption.clear();
        filterListValue.clear();
        filterListComparator.clear();
        filterListConstant.clear();
        filterListDelete.clear();
        //filterListDuplicate.clear();
        charmFilters.clear();

        updateFilterOutput();
        updateFilterListPositions();
    }

    private void updateFilterListPositions() {
        if (controller.charmFilterGuiPreviouslyOpened) {
            addFilterButton.setY(labelMenuHeight + 5 + filterListOption.size() * 25);

            filterListOption.forEach(i -> i.setY(labelMenuHeight + 8 + filterListOption.indexOf(i) * 25));
            filterListValue.forEach(i -> i.setY(labelMenuHeight + 8 + filterListValue.indexOf(i) * 25));
            filterListComparator.forEach(i -> i.setY(labelMenuHeight + 5 + filterListComparator.indexOf(i) * 25));
            filterListConstant.forEach(i -> i.setY(labelMenuHeight + 8 + filterListConstant.indexOf(i) * 25));
            filterListDelete.forEach(i -> i.setY(labelMenuHeight + 5 + filterListDelete.indexOf(i) * 25));
            //filterListDuplicate.forEach(i -> i.y = labelMenuHeight + 5 + filterListDuplicate.indexOf(i) * 25);
        }
    }

    private void updateFilterOutput() {
        controller.updateCharmFilters(charmFilters);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        // draw filter buttons and stuff
        boolean anyOpen = false;
        for (DropdownWidget o : filterListOption) if (o.willClick(mouseX, mouseY)) anyOpen = true;
        if (anyOpen) {
            addFilterButton.render(context, 0, 0, delta); // funny band-aid fix for rendering white outline while in dropdown menu
        } else {
            addFilterButton.render(context, mouseX, mouseY, delta);
        }

        for (DropdownWidget o : filterListOption) {
            o.renderMain(context, mouseX, mouseY, delta);
        }
        for (DropdownWidget v : filterListValue) {
            if (!filterListOption.get(filterListValue.indexOf(v)).getLastChoice().equals("") && !filterListOption.get(filterListValue.indexOf(v)).getLastChoice().equals("Charm Power"))
                v.renderMain(context, mouseX, mouseY, delta);
        }
        for (TextFieldWidget c : filterListConstant) {
            if (filterListOption.get(filterListConstant.indexOf(c)).getLastChoice().equals("Stat") && !(charmFilters.get(filterListConstant.indexOf(c)).comparator < 2) || filterListOption.get(filterListConstant.indexOf(c)).getLastChoice().equals("Charm Power"))
                c.render(context, mouseX, mouseY, delta);
        }
        for (ButtonWidget c : filterListComparator) {
            if (!filterListOption.get(filterListComparator.indexOf(c)).getLastChoice().equals(""))
                c.render(context, mouseX, mouseY, delta);
        }
        filterListDelete.forEach(i -> i.render(context, mouseX, mouseY, delta));
        //filterListDuplicate.forEach(i -> i.render(context, mouseX, mouseY, delta));

        for (DropdownWidget o : filterListOption) {
            o.renderDropdown(context, mouseX, mouseY, delta);
        }
        for (DropdownWidget v : filterListValue) {
            if (!filterListOption.get(filterListValue.indexOf(v)).getLastChoice().equals(""))
                v.renderDropdown(context, mouseX, mouseY, delta);
        }

        // draw the label at the top
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        context.fill(0, 0, width, labelMenuHeight, 0xFF555555);
        context.drawHorizontalLine(0, width, labelMenuHeight, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Charm Filters").setStyle(Style.EMPTY.withBold(true)), width / 2, (labelMenuHeight - textRenderer.fontHeight) / 2, 0xFFFFAA00);
        context.getMatrices().pop();

        // draw gui elements
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        backButton.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();

        try {
            super.render(context, mouseX, mouseY, delta);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                if (!filterListOption.get(filterListValue.indexOf(v)).getLastChoice().equals("") && !filterListOption.get(filterListValue.indexOf(v)).getLastChoice().equals("Charm Power"))
                    v.mouseClicked(mouseX, mouseY, button);
                return false;
            }
            if (!filterListOption.get(filterListValue.indexOf(v)).getLastChoice().equals(""))
                v.mouseClicked(mouseX, mouseY, button);
        }
        for (ButtonWidget c : filterListComparator) {
            if (!filterListOption.get(filterListComparator.indexOf(c)).getLastChoice().equals(""))
                c.mouseClicked(mouseX, mouseY, button);
        }
        for (TextFieldWidget c : filterListConstant) {
            if (filterListOption.get(filterListConstant.indexOf(c)).getLastChoice().equals("Stat") && !(charmFilters.get(filterListConstant.indexOf(c)).comparator < 2) || filterListOption.get(filterListConstant.indexOf(c)).getLastChoice().equals("Charm Power"))
                c.mouseClicked(mouseX, mouseY, button);
        }
        filterListDelete.forEach(i -> i.mouseClicked(mouseX, mouseY, button));
        //filterListDuplicate.forEach(i -> i.mouseClicked(mouseX, mouseY, button));

        addFilterButton.mouseClicked(mouseX, mouseY, button);

        if (removeIndex != -1) {
            filterListOption.remove(removeIndex);
            filterListValue.remove(removeIndex);
            filterListComparator.remove(removeIndex);
            filterListConstant.remove(removeIndex);
            filterListDelete.remove(removeIndex);
            //filterListDuplicate.remove(removeIndex);
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
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);


    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        super.mouseScrolled(mouseX, mouseY, amount);

        filterListOption.forEach(i -> i.mouseScrolled(mouseX, mouseY, amount));
        filterListValue.forEach(i -> i.mouseScrolled(mouseX, mouseY, amount));

        return true;
    }
}
