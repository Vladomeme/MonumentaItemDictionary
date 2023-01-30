package dev.eliux.monumentaitemdictionary.gui.charm;

import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import dev.eliux.monumentaitemdictionary.gui.widgets.CharmButtonWidget;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemIconButtonWidget;
import dev.eliux.monumentaitemdictionary.util.CharmStat;
import dev.eliux.monumentaitemdictionary.util.ItemColors;
import dev.eliux.monumentaitemdictionary.util.ItemFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CharmDictionaryGui extends Screen {
    public final int sideMenuWidth = 40;
    public final int labelMenuHeight = 30;
    public final int itemPadding = 7;
    public final int itemSize = 25;
    private int scrollPixels = 0;

    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    private ArrayList<CharmButtonWidget> charmButtons = new ArrayList<>();

    private TextFieldWidget searchBar;
    private ItemIconButtonWidget reloadCharmsButton;
    private ItemIconButtonWidget showItemsButton;
    private ItemIconButtonWidget filterButton;
    private ItemIconButtonWidget resetFilterButton;
    private ItemIconButtonWidget tipsMasterworkButton;

    public final DictionaryController controller;

    public CharmDictionaryGui(Text title, DictionaryController controller) {
        super(title);
        this.controller = controller;
    }

    public void postInit() {
        buildCharmList();

        searchBar = new TextFieldWidget(textRenderer, width / 2 + 90, 7, width / 2 - 100, 15, new LiteralText("Search"));
        searchBar.setChangedListener(t -> {
            controller.setCharmNameFilter(searchBar.getText());
            if (searchBar.getText().equals(""))
                controller.clearCharmNameFilter();

            buildCharmList();
            updateScrollLimits();
        });
        searchBar.setTextFieldFocused(true);

        reloadCharmsButton = new ItemIconButtonWidget(5, 5, 20, 20, new LiteralText(""), (button) -> {
            controller.requestAndUpdate();
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, new LiteralText("Reload All Data"), mouseX, mouseY);
        }, "globe_banner_pattern", "");

        showItemsButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, labelMenuHeight + 10, 20, 20, new LiteralText(""), (button) -> {
            controller.setItemDictionaryScreen();
        }, ((button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, new LiteralText("Item Data").setStyle(Style.EMPTY.withColor(0xFF00FFFF)), mouseX, mouseY);
        }), "iron_chestplate", "");

        filterButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 30, 20, 20, new LiteralText(""), (button) -> {
            controller.setCharmFilterScreen();
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, new LiteralText("Filter"), mouseX, mouseY);
        }, "chest", "");

        resetFilterButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 60, 20, 20, new LiteralText(""), (button) -> {
            controller.charmFilterGui.clearFilters();
            buildCharmList();
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, new LiteralText("Reset Filters").setStyle(Style.EMPTY.withColor(0xFFFF0000)), mouseX, mouseY);
        }, "barrier", "");

        tipsMasterworkButton = new ItemIconButtonWidget(30, 5, 20, 20, new LiteralText(""), (button) -> {
            Util.getOperatingSystem().open("https://github.com/Ilyiux/MonumentaItemDictionary");
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Arrays.asList(
                    new LiteralText("Tips").setStyle(Style.EMPTY.withColor(0xFFFFFFFF)),
                    new LiteralText(""),
                    new LiteralText("Click to go to the MID Github page!").setStyle(Style.EMPTY.withUnderline(true).withColor(0xFF5555FF))
            ), mouseX, mouseY);
        }, "oak_sign", "");
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        // draw the scroll bar
        int totalRows = (int) Math.ceil((double)charmButtons.size() / (double)((width - sideMenuWidth - 5) / (itemSize + itemPadding)));
        int totalPixelHeight = totalRows * itemSize + (totalRows + 1) * itemPadding;
        double bottomPercent = (double)scrollPixels / totalPixelHeight;
        double screenPercent = (double)(height - labelMenuHeight) / totalPixelHeight;
        drawVerticalLine(matrices, width - sideMenuWidth - 1, labelMenuHeight, height, 0x77AAAAAA); // called twice to make the scroll bar render wider (janky, but I don't really care)
        drawVerticalLine(matrices, width - sideMenuWidth - 2, labelMenuHeight, height, 0x77AAAAAA);
        drawVerticalLine(matrices, width - sideMenuWidth - 1, (int) (labelMenuHeight + (height - labelMenuHeight) * bottomPercent), (int) (labelMenuHeight + (height - labelMenuHeight) * (bottomPercent + screenPercent)), 0xFFC3C3C3);
        drawVerticalLine(matrices, width - sideMenuWidth - 2, (int) (labelMenuHeight + (height - labelMenuHeight) * bottomPercent), (int) (labelMenuHeight + (height - labelMenuHeight) * (bottomPercent + screenPercent)), 0xFFC3C3C3);

        // draw the sort menu
        drawVerticalLine(matrices, width - sideMenuWidth, labelMenuHeight, height, 0xFFFFFFFF);

        // draw item buttons
        charmButtons.forEach(b -> {
            if (b.y - scrollPixels + itemSize >= labelMenuHeight && b.y - scrollPixels <= height) {
                b.renderButton(matrices, mouseX, mouseY, delta);
            }
        });

        if (charmButtons.size() == 0) {
            drawCenteredText(matrices, textRenderer, "Found No Charms", width / 2, labelMenuHeight + 10, 0xFF2222);
        }

        // draw the label at the top
        matrices.push();
        matrices.translate(0, 0, 110);
        fill(matrices, 0, 0, width, labelMenuHeight, 0xFF555555);
        drawHorizontalLine(matrices, 0, width, labelMenuHeight, 0xFFFFFFFF);
        drawCenteredText(matrices, textRenderer, new LiteralText("Monumenta Charm Dictionary").setStyle(Style.EMPTY.withBold(true)), width / 2, (labelMenuHeight - textRenderer.fontHeight) / 2, 0xFFd8b427);
        matrices.pop();

        // draw gui elements
        matrices.push();
        matrices.translate(0, 0, 110);
        searchBar.render(matrices, mouseX, mouseY, delta);
        reloadCharmsButton.render(matrices, mouseX, mouseY, delta);
        showItemsButton.render(matrices, mouseX, mouseY, delta);
        filterButton.render(matrices, mouseX, mouseY, delta);
        resetFilterButton.render(matrices, mouseX, mouseY, delta);
        tipsMasterworkButton.render(matrices, mouseX, mouseY, delta);
        matrices.pop();

        try {
            super.render(matrices, mouseX, mouseY, delta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buildCharmList() {
        controller.refreshCharms();
        ArrayList<DictionaryCharm> toBuildCharms = controller.getCharms();

        charmButtons.clear();
        for (DictionaryCharm charm : toBuildCharms) {
            int index = toBuildCharms.indexOf(charm);
            int row = index / ((width - sideMenuWidth - 5) / (itemSize + itemPadding));
            int col = index % ((width - sideMenuWidth - 5) / (itemSize + itemPadding));

            int x = (col + 1) * itemPadding + col * itemSize;
            int y = labelMenuHeight + (row + 1) * itemPadding + row * itemSize;

            CharmButtonWidget button = new CharmButtonWidget(x, y, itemSize, index, new LiteralText(charm.name), (b) -> {

            }, charm, (b, matrices, mouseX, mouseY) -> {
                renderTooltip(matrices, generateCharmLoreText(charm), mouseX, mouseY);
            }, this);

            charmButtons.add(button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);

        searchBar.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == 258) { // tab key pressed
            searchBar.setTextFieldFocused(!searchBar.isFocused());
        }

        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        super.charTyped(chr, modifiers);

        searchBar.charTyped(chr, modifiers);

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        charmButtons.forEach((b) -> b.mouseClicked(mouseX, mouseY, button));

        searchBar.mouseClicked(mouseX, mouseY, button);
        reloadCharmsButton.mouseClicked(mouseX, mouseY, button);
        showItemsButton.mouseClicked(mouseX, mouseY, button);
        filterButton.mouseClicked(mouseX, mouseY, button);
        resetFilterButton.mouseClicked(mouseX, mouseY, button);
        tipsMasterworkButton.mouseClicked(mouseX, mouseY, button);

        return true;
    }

    private List<Text> generateCharmLoreText(DictionaryCharm charm) {
        List<Text> lines = new ArrayList<>();

        lines.add(new LiteralText(charm.name).setStyle(Style.EMPTY
                .withColor(0xFF000000 + ItemColors.getColorForLocation(charm.location))
                .withBold(ItemFormatter.shouldBold(charm.tier))
                .withUnderline(ItemFormatter.shouldUnderline(charm.tier))));

        MutableText region = new LiteralText(charm.region + " : ").setStyle(Style.EMPTY
                .withColor(ItemColors.TEXT_COLOR));
        MutableText tier = new LiteralText(ItemFormatter.formatCharmTier(charm.tier)).setStyle(Style.EMPTY
                .withColor(ItemColors.getColorForTier(charm.tier))
                .withBold(ItemFormatter.shouldUnderline(charm.tier)));
        lines.add(region.append(tier));

        MutableText charmPowerDesc = new LiteralText("Charm Power : ").setStyle(Style.EMPTY
                .withColor(ItemColors.TEXT_COLOR));
        MutableText charmPower = new LiteralText("").setStyle(Style.EMPTY
                .withColor(ItemColors.TEXT_CHARM_POWER_COLOR));
        for (int i = 0; i < charm.power; i++) charmPower.append("★");
        MutableText divider = new LiteralText(" - ").setStyle(Style.EMPTY
                .withColor(ItemColors.TEXT_COLOR));
        MutableText classText = new LiteralText(charm.className).setStyle(Style.EMPTY
                        .withColor(ItemColors.getColorForClass(charm.className)));
        lines.add(charmPowerDesc.append(charmPower).append(divider).append(classText));

        lines.add(new LiteralText(charm.location).setStyle(Style.EMPTY.withColor(ItemColors.getColorForLocation(charm.location))));

        lines.add(new LiteralText(""));

        lines.add(new LiteralText("When in Charm Slot:").setStyle(Style.EMPTY.withColor(0xAAAAAA)));
        for (CharmStat stat : charm.stats) {
            lines.add(new LiteralText((stat.statValue >= 0 ? "+" : "") + stat.statValue + (stat.statNameFull.endsWith("percent") ? "" : " ") + ItemFormatter.formatCharmStat(stat.statNameFull)).setStyle(Style.EMPTY
                    .withColor(ItemColors.getColorForCharmStat(stat))));
        }

        lines.add(new LiteralText(""));

        lines.add(new LiteralText(charm.baseItem).setStyle(Style.EMPTY
                .withColor(ItemColors.TEXT_COLOR)));

        return lines;
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);

        updateGuiPositions();
    }

    public void updateGuiPositions() {
        buildCharmList();
        updateScrollLimits();

        searchBar.setX(width / 2 + 90);
        searchBar.setWidth(width / 2 - 100);

        showItemsButton.x = width - sideMenuWidth + 10;
        showItemsButton.y = labelMenuHeight + 10;

        filterButton.x = width - sideMenuWidth + 10;
        filterButton.y = height - 30;
        resetFilterButton.x = width - sideMenuWidth + 10;
        resetFilterButton.y = height - 60;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        super.mouseScrolled(mouseX, mouseY, amount);

        if (Screen.hasControlDown()) {
            charmButtons.forEach((b) -> b.scrolled(mouseX, mouseY, amount));
        } else {
            if (mouseX >= 0 && mouseX < width - sideMenuWidth && mouseY >= labelMenuHeight && mouseY < height) {
                scrollPixels += -amount * 12; // scaled

                updateScrollLimits();
            }
        }

        return true;
    }

    private void updateScrollLimits() {
        int rows = (int) Math.ceil((double)charmButtons.size() / (double)((width - sideMenuWidth - 5) / (itemSize + itemPadding)));
        int maxScroll = rows * itemSize + (rows + 1) * itemPadding - height + labelMenuHeight;
        if (scrollPixels > maxScroll) scrollPixels = maxScroll;

        if (scrollPixels < 0) scrollPixels = 0;
    }

    public int getScrollPixels() {
        return scrollPixels;
    }
}
