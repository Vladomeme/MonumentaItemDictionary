package dev.eliux.monumentaitemdictionary.gui.item;

import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemButtonWidget;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemIconButtonWidget;
import dev.eliux.monumentaitemdictionary.util.ItemColors;
import dev.eliux.monumentaitemdictionary.util.ItemFormatter;
import dev.eliux.monumentaitemdictionary.util.ItemStat;
import java.util.HashMap;
import java.util.TreeMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemDictionaryGui extends Screen {
    public final int sideMenuWidth = 40;
    public final int labelMenuHeight = 30;
    public final int itemPadding = 7;
    public final int itemSize = 25;
    private int scrollPixels = 0;

    private long lastAltPressed = 0;

    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    private final TreeMap<Integer, ArrayList<ItemButtonWidget>> itemButtons = new TreeMap<>();
    private final HashMap<DictionaryItem, ItemButtonWidget> widgetByItem = new HashMap<>();

    private TextFieldWidget searchBar;
    private ItemIconButtonWidget reloadItemsButton;
    private ItemIconButtonWidget showCharmsButton;
    private ItemIconButtonWidget filterButton;
    private ItemIconButtonWidget resetFilterButton;
    private ItemIconButtonWidget minMasterworkButton;
    private ItemIconButtonWidget maxMasterworkButton;
    private ItemIconButtonWidget tipsMasterworkButton;

    public final DictionaryController controller;

    public ItemDictionaryGui(Text title, DictionaryController controller) {
        super(title);
        this.controller = controller;
    }

    public void postInit() {
        buildItemList();

        searchBar = new TextFieldWidget(textRenderer, width / 2 + 90, 7, width / 2 - 100, 15, Text.literal("Search"));
        searchBar.setChangedListener(t -> {
            controller.setItemNameFilter(searchBar.getText());
            if (searchBar.getText().equals(""))
                controller.clearItemNameFilter();

            buildItemList();
            updateScrollLimits();
        });
        searchBar.setFocused(true);

        reloadItemsButton = new ItemIconButtonWidget(5, 5, 20, 20, Text.literal(""), (button) -> {
            controller.requestAndUpdate();
        }, Text.literal("Reload All Data"), "globe_banner_pattern", "");

        showCharmsButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, labelMenuHeight + 10, 20, 20, Text.literal(""), (button) -> {
            controller.setCharmDictionaryScreen();
        }, Text.literal("Charm Data").setStyle(Style.EMPTY.withColor(0xFFFFFF00)), "glowstone_dust", "");

        filterButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 30, 20, 20, Text.literal(""), (button) -> {
            controller.setItemFilterScreen();
        }, Text.literal("Filter"), "chest", "");

        resetFilterButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 60, 20, 20, Text.literal(""), (button) -> {
            controller.itemFilterGui.clearFilters();
            searchBar.setText("");
            buildItemList();
        }, Text.literal("Reset Filters").setStyle(Style.EMPTY.withColor(0xFFFF0000)), "barrier", "");

        minMasterworkButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 120, 20, 20, Text.literal(""), (button) -> {
            for (List<ItemButtonWidget> row : itemButtons.values()) {
                row.forEach(ItemButtonWidget::setMinimumMasterwork);
            }
        }, Text.literal("Show Minimum Masterwork").setStyle(Style.EMPTY.withColor(0xFFAA00AA)), "netherite_scrap", "");

        maxMasterworkButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 90, 20, 20, Text.literal(""), (button) -> {
            for (List<ItemButtonWidget> row : itemButtons.values()) {
                row.forEach(ItemButtonWidget::setMaximumMasterwork);
            }
        }, Arrays.asList(Text.literal("Show Maximum Masterwork").setStyle(Style.EMPTY.withColor(0xFFAA00AA)), Text.literal("(Only counts tiers with data)").setStyle(Style.EMPTY.withColor(0xFFAAAAAA))), "netherite_ingot", "");

        tipsMasterworkButton = new ItemIconButtonWidget(30, 5, 20, 20, Text.literal(""), (button) -> {
            Util.getOperatingSystem().open("https://github.com/Ilyiux/MonumentaItemDictionary");
        }, Arrays.asList(
                    Text.literal("Tips").setStyle(Style.EMPTY.withColor(0xFFFFFFFF)),
                    Text.literal(""),
                    Text.literal("Ctrl + Scroll").setStyle(Style.EMPTY.withBold(true).withColor(ItemColors.TEXT_COLOR)).append(Text.literal(" to increase/decrease individual masterwork tiers").setStyle(Style.EMPTY.withBold(false).withColor(ItemColors.TEXT_COLOR))),
                    Text.literal(""),
                    Text.literal("Shift").setStyle(Style.EMPTY.withBold(true).withColor(ItemColors.TEXT_COLOR)).append(Text.literal(" to show an item's lore").setStyle(Style.EMPTY.withBold(false).withColor(ItemColors.TEXT_COLOR))),
                    Text.literal(""),
                    Text.literal("Double Tap Alt").setStyle(Style.EMPTY.withBold(true).withColor(ItemColors.TEXT_COLOR)).append(Text.literal(" to quickly reset search and filters").setStyle(Style.EMPTY.withBold(false).withColor(ItemColors.TEXT_COLOR))),
                    Text.literal(""),
                    Text.literal("Ctrl Shift + Click").setStyle(Style.EMPTY.withBold(true).withColor(ItemColors.TEXT_COLOR)).append(Text.literal(" to open an item in the wiki").setStyle(Style.EMPTY.withBold(false).withColor(ItemColors.TEXT_COLOR))),
                    Text.literal(""),
                    Text.literal("Click to go to the MID Github page!").setStyle(Style.EMPTY.withUnderline(true).withColor(0xFF5555FF))
            ), "oak_sign", "");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        // draw the scroll bar
        int totalRows = itemButtons.size();
        int totalPixelHeight = totalRows * itemSize + (totalRows + 1) * itemPadding;
        double bottomPercent = (double)scrollPixels / totalPixelHeight;
        double screenPercent = (double)(height - labelMenuHeight) / totalPixelHeight;
        context.drawVerticalLine(width - sideMenuWidth - 1, labelMenuHeight, height, 0x77AAAAAA); // called twice to make the scroll bar render wider (janky, but I don't really care)
        context.drawVerticalLine(width - sideMenuWidth - 2, labelMenuHeight, height, 0x77AAAAAA);
        context.drawVerticalLine(width - sideMenuWidth - 1, (int) (labelMenuHeight + (height - labelMenuHeight) * bottomPercent), (int) (labelMenuHeight + (height - labelMenuHeight) * (bottomPercent + screenPercent)), 0xFFC3C3C3);
        context.drawVerticalLine(width - sideMenuWidth - 2, (int) (labelMenuHeight + (height - labelMenuHeight) * bottomPercent), (int) (labelMenuHeight + (height - labelMenuHeight) * (bottomPercent + screenPercent)), 0xFFC3C3C3);

        // draw the sort menu
        context.drawVerticalLine(width - sideMenuWidth, labelMenuHeight, height, 0xFFFFFFFF);

        // draw item buttons
        if (!controller.isRequesting) {
            for (List<ItemButtonWidget> row : itemButtons
                    .subMap(labelMenuHeight + scrollPixels - itemSize, true,
                            height + scrollPixels, true)
                    .values()) {
                row.forEach(b -> b.renderButton(context, mouseX, mouseY, delta));
            }

            if (itemButtons.isEmpty()) {
                context.drawCenteredTextWithShadow(textRenderer, "Found No Items", width / 2, labelMenuHeight + 10, 0xFF2222);

                if (controller.anyItems()) {
                    context.drawCenteredTextWithShadow(textRenderer, "It seems like there were no items to begin with...", width / 2, labelMenuHeight + 30, 0xFF2222);
                    context.drawCenteredTextWithShadow(textRenderer, "Try clicking the Reload All Data button in the top left", width / 2, labelMenuHeight + 45, 0xFF2222);
                }
            }
        }

        if (controller.isRequesting) {
            context.drawCenteredTextWithShadow(textRenderer, "Requesting item data...", width / 2, labelMenuHeight + 10, 0xFF2222);
        }

        // draw the label at the top
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        context.fill(0, 0, width, labelMenuHeight, 0xFF555555);
        context.drawHorizontalLine(0, width, labelMenuHeight, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Monumenta Item Dictionary").setStyle(Style.EMPTY.withBold(true)), width / 2, (labelMenuHeight - textRenderer.fontHeight) / 2, 0xFF2ca9d3);
        context.getMatrices().pop();

        // draw gui elements
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        searchBar.render(context, mouseX, mouseY, delta);
        reloadItemsButton.render(context, mouseX, mouseY, delta);
        showCharmsButton.render(context, mouseX, mouseY, delta);
        filterButton.render(context, mouseX, mouseY, delta);
        resetFilterButton.render(context, mouseX, mouseY, delta);
        minMasterworkButton.render(context, mouseX, mouseY, delta);
        maxMasterworkButton.render(context, mouseX, mouseY, delta);
        tipsMasterworkButton.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();

        try {
            super.render(context, mouseX, mouseY, delta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buildItemList() {
        controller.refreshItems();
        ArrayList<DictionaryItem> toBuildItems = controller.getItems();

        itemButtons.clear();
        widgetByItem.clear();
        for (DictionaryItem item : toBuildItems) {
            int index = toBuildItems.indexOf(item);
            int row = index / ((width - sideMenuWidth - 5) / (itemSize + itemPadding));
            int col = index % ((width - sideMenuWidth - 5) / (itemSize + itemPadding));

            int x = (col + 1) * itemPadding + col * itemSize;
            int y = labelMenuHeight + (row + 1) * itemPadding + row * itemSize;

            ItemButtonWidget button = new ItemButtonWidget(x, y, itemSize, index, Text.literal(item.name), (b) -> {
                if (hasShiftDown() && hasControlDown()) {
                    String wikiFormatted = item.name.replace(" ", "_").replace("'", "%27");
                    Util.getOperatingSystem().open("https://monumenta.wiki.gg/wiki/" + wikiFormatted);
                }

                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null && hasAltDown() && player.getAbilities().creativeMode) {
                    //ItemGenerator.giveItemToClientPlayer(item.name + (item.hasMasterwork ? "-" + button.shownMasterworkTier : ""));
                    controller.setGeneratorScreen().setItem(item);
                }
            }, item, () -> generateItemLoreText(item), this);

            itemButtons.computeIfAbsent(y, k -> new ArrayList<>())
                    .add(button);
            widgetByItem.put(item, button);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);

        searchBar.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == 258) { // tab key pressed
            searchBar.setFocused(!searchBar.isFocused());
        }

        // reset filters shortcut
        if (keyCode == 342 || keyCode == 346) { // left or right alt pressed
            if (System.currentTimeMillis() - lastAltPressed < 1000) {
                controller.itemFilterGui.clearFilters();
                searchBar.setText("");
                buildItemList();
            }
        }

        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        super.keyReleased(keyCode, scanCode, modifiers);

        if (keyCode == 342 || keyCode == 346) { // left or right alt pressed
            lastAltPressed = System.currentTimeMillis();
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

        for (List<ItemButtonWidget> row : itemButtons
                .subMap((int) mouseY + scrollPixels - itemSize, true,
                        (int) mouseY + scrollPixels, true)
                .values()) {
            row.forEach(b -> b.mouseClicked(mouseX, mouseY + scrollPixels, button));
        }

        searchBar.mouseClicked(mouseX, mouseY, button);
        reloadItemsButton.mouseClicked(mouseX, mouseY, button);
        showCharmsButton.mouseClicked(mouseX, mouseY, button);
        filterButton.mouseClicked(mouseX, mouseY, button);
        resetFilterButton.mouseClicked(mouseX, mouseY, button);
        minMasterworkButton.mouseClicked(mouseX, mouseY, button);
        maxMasterworkButton.mouseClicked(mouseX, mouseY, button);
        tipsMasterworkButton.mouseClicked(mouseX, mouseY, button);

        return true;
    }

    private List<Text> generateItemLoreText(DictionaryItem item) {
        // this is scuffed
        int masterworkTier = 0;
        ItemButtonWidget itemButton = widgetByItem.get(item);
        if (itemButton != null) {
            masterworkTier = itemButton.shownMasterworkTier;
        }
        String itemTier = item.hasMasterwork ? item.getTierFromMasterwork(Math.max(masterworkTier, item.getMinMasterwork())) : item.getTierNoMasterwork();

        List<Text> lines = new ArrayList<>();

        lines.add(Text.literal(item.name).setStyle(Style.EMPTY
                .withColor(0xFF000000 + ItemColors.getColorForLocation(item.location))
                .withBold(!item.isFish ? ItemFormatter.shouldBold(itemTier) : ItemFormatter.shouldBoldFish(item.fishTier))
                .withUnderline(!item.isFish ? ItemFormatter.shouldUnderline(itemTier) : ItemFormatter.shouldUnderlineFish(item.fishTier))));

        ArrayList<Text> enchants = new ArrayList<>();
        ArrayList<Text> basestats = new ArrayList<>();
        ArrayList<Text> stats = new ArrayList<>();

        ArrayList<ItemStat> showStats = item.hasMasterwork ? item.getStatsFromMasterwork(masterworkTier) : item.getStatsNoMasterwork();

        if (showStats == null) {
            if (masterworkTier > item.getMinMasterwork()) {
                lines.add(Text.literal("The data for this tier is missing.").setStyle(Style.EMPTY.withColor(0xFFFF0000)));
            } else if (masterworkTier < item.getMinMasterwork()) {
                lines.add(Text.literal("This masterwork tier does not exist.").setStyle(Style.EMPTY.withColor(0xFFFF0000)));
            }
        }

        if (showStats != null) {
            for (ItemStat stat : showStats) {
                Text line = Text.literal(ItemFormatter.buildStatString(stat.statName, stat.statValue)).setStyle(Style.EMPTY
                        .withColor(ItemColors.getColorForStat(stat.statName, stat.statValue)));
                if (ItemFormatter.isStat(stat.statName)) {
                    if (ItemFormatter.isBaseStat(stat.statName)) {
                        basestats.add(line);
                    } else {
                        stats.add(line);
                    }
                } else {
                    enchants.add(line);
                }
            }
        }

        if (showStats != null) {
            lines.addAll(enchants);
        }

        if (item.hasRegion() || item.hasTier()) {
            MutableText regionText = Text.literal(item.hasRegion() ? ItemFormatter.formatRegion(item.region) + (item.hasTier() ? " : " : "") : "")
                    .setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR));
            MutableText tierText = Text.literal(item.hasTier() ? itemTier : "").setStyle(Style.EMPTY
                    .withColor(ItemColors.getColorForTier(itemTier))
                    .withBold(ItemFormatter.shouldUnderline(itemTier)));

            lines.add(regionText.append(tierText));
        }

        if (item.hasMasterwork) {
            MutableText baseText = Text.literal("Masterwork : ").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR));
            for (int i = 0; i < ItemFormatter.getMasterworkForRarity(itemTier); i ++) {
                if (i < masterworkTier) {
                    baseText.append(Text.literal("★").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_MASTERWORK_COLOR)));
                } else {
                    baseText.append(Text.literal("☆").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR)));
                }
            }
            lines.add(baseText);
        }

        if (item.isFish) {
            MutableText baseText = Text.literal("Fish Quality : ").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR));
            for (int i = 0; i < ItemFormatter.getMaxFishTier(); i ++) {
                if (i < item.fishTier) {
                    baseText.append(Text.literal("★").setStyle(Style.EMPTY.withColor(ItemColors.FISH_COLOR)));
                } else {
                    baseText.append(Text.literal("☆").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR)));
                }
            }
            lines.add(baseText);
        }

        if (item.hasLocation()) {
            lines.add(Text.literal(item.location).setStyle(Style.EMPTY
                    .withColor(ItemColors.getColorForLocation(item.location))));
        }

        if (!item.lore.equals("")) {
            if (hasShiftDown()) {
                for (String line : item.lore.split("\n")) {
                    lines.add(Text.literal(line).setStyle(Style.EMPTY.withColor(ItemColors.mixHexes(ItemColors.TEXT_COLOR, ItemColors.getColorForLocation(item.location), 0.67))));
                }
            } else {
                lines.add(Text.literal("Press [SHIFT] to show lore.").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR)));
            }
        }

        if (showStats != null) {
            if (stats.size() > 0 || basestats.size() > 0) {
                lines.add(Text.literal(""));
                lines.add(Text.literal(ItemFormatter.formatUseLine(item.type)).setStyle(Style.EMPTY.withColor(0xAAAAAA)));
            }

            lines.addAll(basestats);
            lines.addAll(stats);
        }

        lines.add(Text.literal(""));


        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.getAbilities().creativeMode) {
            lines.add(Text.literal("[ALT] + Click to generate this item").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR)));
        }
        lines.add(Text.literal("[CTRL] [SHIFT] + Click to open in the wiki").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR)));
        lines.add(Text.literal(item.type + " - " + item.baseItem).setStyle(Style.EMPTY
                .withColor(ItemColors.TEXT_COLOR)));

        return lines;
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);

        updateGuiPositions();
    }

    public void updateGuiPositions() {
        buildItemList();
        updateScrollLimits();

        searchBar.setX(width / 2 + 90);
        searchBar.setWidth(width / 2 - 100);

        showCharmsButton.setX(width - sideMenuWidth + 10);
        showCharmsButton.setY(labelMenuHeight + 10);

        filterButton.setX(width - sideMenuWidth + 10);
        filterButton.setY(height - 30);
        resetFilterButton.setX(width - sideMenuWidth + 10);
        resetFilterButton.setY(height - 60);

        minMasterworkButton.setX(width - sideMenuWidth + 10);
        minMasterworkButton.setY(height - 120);
        maxMasterworkButton.setX(width - sideMenuWidth + 10);
        maxMasterworkButton.setY(height - 90);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        super.mouseScrolled(mouseX, mouseY, amount);

        if (Screen.hasControlDown()) {
            for (List<ItemButtonWidget> row : itemButtons
                    .subMap(labelMenuHeight + scrollPixels - itemSize, true,
                            height + scrollPixels, true)
                    .values()) {
                row.forEach(b -> b.scrolled(mouseX, mouseY, amount));
            }
        } else {
            if (mouseX >= 0 && mouseX < width - sideMenuWidth && mouseY >= labelMenuHeight && mouseY < height) {
                scrollPixels += -amount * 22; // scaled

                updateScrollLimits();
            }
        }

        return true;
    }

    private void updateScrollLimits() {
        int rows = itemButtons.size();
        int maxScroll = rows * itemSize + (rows + 1) * itemPadding - height + labelMenuHeight;
        if (scrollPixels > maxScroll) scrollPixels = maxScroll;

        if (scrollPixels < 0) scrollPixels = 0;
    }

    public int getScrollPixels() {
        return scrollPixels;
    }
}
