package dev.eliux.monumentaitemdictionary.gui.item;

import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import dev.eliux.monumentaitemdictionary.gui.builder.BuilderGui;
import dev.eliux.monumentaitemdictionary.gui.charm.CharmDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemButtonWidget;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemIconButtonWidget;
import dev.eliux.monumentaitemdictionary.util.*;

import java.util.*;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class ItemDictionaryGui extends Screen {

    public final int sideMenuWidth = 40;
    public final int labelMenuHeight = 30;
    public final int itemPadding = 7;
    public final int itemSize = 25;
    public String itemTypeLookingFor;
    private int scrollPixels = 0;

    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    private final TreeMap<Integer, List<ItemButtonWidget>> itemButtons = new TreeMap<>();
    private final Map<DictionaryItem, ItemButtonWidget> widgetByItem = new HashMap<>();

    private TextFieldWidget searchBar;
    private ItemIconButtonWidget reloadItemsButton;
    private ItemIconButtonWidget builderGuiButton;
    private ItemIconButtonWidget showCharmsButton;
    private ItemIconButtonWidget filterButton;
    private ItemIconButtonWidget resetFilterButton;
    private ItemIconButtonWidget minMasterworkButton;
    private ItemIconButtonWidget maxMasterworkButton;
    private ItemIconButtonWidget tipsMasterworkButton;
    public boolean isGettingBuildItem = false;
    private long lastAltPressed = 0;

    public final DictionaryController controller;
    private ItemIconButtonWidget builderButton;

    public ItemDictionaryGui(Text title, DictionaryController controller) {
        super(title);
        this.controller = controller;
    }

    public void postInit() {
        buildItemList();

        searchBar = new TextFieldWidget(textRenderer, width / 2 + 90, 7, width / 2 - 100, 15, Text.literal("Search"));
        searchBar.setChangedListener(t -> {
            scrollPixels = 0;
            controller.setItemNameFilter(searchBar.getText());
            if (searchBar.getText().isEmpty())
                controller.hasItemNameFilter = false;

            buildItemList();
            updateScrollLimits();
        });
        searchBar.setPlaceholder(Text.of("Search..."));
        searchBar.setFocused(true);

        reloadItemsButton = new ItemIconButtonWidget(5, 5, 20, 20,
                (button) -> controller.requestAndUpdate(),
                Text.literal("Reload All Data"),
                "globe_banner_pattern", "");

        builderGuiButton = new ItemIconButtonWidget(55, 5, 20, 20,
                (button) -> controller.setBuildDictionaryScreen(),
                Text.literal("Open Builder GUI"),
                "iron_chestplate", "");

        builderButton = new ItemIconButtonWidget(55, 5, 20, 20,
                (button) -> {
                    isGettingBuildItem = false;
                    controller.setBuilderScreen();
                }, Text.literal("Go Back To Builder"),
                "arrow", "");

        showCharmsButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, labelMenuHeight + 10, 20, 20,
                (button) -> controller.setCharmDictionaryScreen(),
                Text.literal("Charm Data").setStyle(Styles.YELLOW),
                "glowstone_dust", "");

        filterButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 30, 20, 20,
                (button) -> controller.setItemFilterScreen(),
                Text.literal("Filter"),
                "chest", "");

        resetFilterButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 60, 20, 20,
                (button) -> {
                    controller.itemFilterGui.clearFilters();
                    searchBar.setText("");
                    buildItemList();
                }, Text.literal("Reset Filters").setStyle(Styles.RED),
                "barrier", "");

        minMasterworkButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 120, 20, 20,
                (button) -> {
                    for (List<ItemButtonWidget> row : itemButtons.values()) {
                        row.forEach(ItemButtonWidget::setMinimumMasterwork);
                    }
                }, Text.literal("Show Minimum Masterwork").setStyle(Styles.PURPLE),
                "netherite_scrap", "");

        maxMasterworkButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 90, 20, 20,
                (button) -> {
                    for (List<ItemButtonWidget> row : itemButtons.values()) {
                        row.forEach(ItemButtonWidget::setMaximumMasterwork);
                    }
                },
                Arrays.asList(
                        Text.literal("Show Maximum Masterwork").setStyle(Styles.PURPLE),
                        Text.literal("(Only counts tiers with data)").setStyle(Styles.LIGHT_GRAY)),
                "netherite_ingot", "");

        tipsMasterworkButton = new ItemIconButtonWidget(
                30, 5, 20, 20,
                (button) -> Util.getOperatingSystem().open("https://github.com/Vladomeme/MonumentaItemDictionary"),
                Arrays.asList(
                        Text.literal("Tips").setStyle(Styles.WHITE),
                        Text.empty(),
                        Text.literal("Ctrl + Scroll").setStyle(Styles.BOLD_TEXT)
                                .append(Text.literal(" to increase/decrease individual masterwork tiers").setStyle(Styles.RESET_BOLD_TEXT)),
                        Text.empty(),
                        Text.literal("Shift").setStyle(Styles.BOLD_TEXT)
                                .append(Text.literal(" to show an item's lore").setStyle(Styles.RESET_BOLD_TEXT)),
                        Text.empty(),
                        Text.literal("Double Tap Alt").setStyle(Styles.BOLD_TEXT)
                                .append(Text.literal(" to quickly reset search and filters").setStyle(Styles.RESET_BOLD_TEXT)),
                        Text.empty(),
                        Text.literal("Ctrl Shift + Click").setStyle(Styles.BOLD_TEXT)
                                .append(Text.literal(" to open an item in the wiki").setStyle(Styles.RESET_BOLD_TEXT)),
                        Text.empty(),
                        Text.literal("Click to go to the MID Github page!").setStyle(Styles.GITHUB_LINK)
                ), "oak_sign", "");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        // draw the scroll bar
        int totalRows = itemButtons.size();
        int totalPixelHeight = totalRows * itemSize + (totalRows + 1) * itemPadding;
        double bottomPercent = (double) scrollPixels / totalPixelHeight;
        double screenPercent = (double) (height - labelMenuHeight) / totalPixelHeight;

        context.fill(width - sideMenuWidth - 2, labelMenuHeight, width - sideMenuWidth, height, Colors.SCROLLBAR_BG);
        context.fill(width - sideMenuWidth - 2, (int) (labelMenuHeight + (height - labelMenuHeight) * bottomPercent),
                width - sideMenuWidth, (int) (labelMenuHeight + (height - labelMenuHeight) * (bottomPercent + screenPercent)), Colors.SCROLLBAR);

        // draw the sort menu
        context.drawVerticalLine(width - sideMenuWidth, labelMenuHeight, height, Colors.WHITE);

        // draw item buttons
        if (!controller.isRequesting) {
            if (!itemButtons.isEmpty()) {
                for (List<ItemButtonWidget> row : itemButtons
                        .subMap(labelMenuHeight + scrollPixels - itemSize, true, height + scrollPixels, true)
                        .values()) {
                    row.forEach(b -> b.renderWidget(context, mouseX, mouseY, delta));
                }
            }
            else {
                context.drawCenteredTextWithShadow(textRenderer, "Found No Items", width / 2, labelMenuHeight + 10, Colors.BRIGHT_RED);

                if (controller.items.isEmpty()) {
                    context.drawCenteredTextWithShadow(textRenderer, "It seems like there were no items to begin with...",
                            width / 2, labelMenuHeight + 30, Colors.BRIGHT_RED);
                    context.drawCenteredTextWithShadow(textRenderer, "Try clicking the Reload All Data button in the top left",
                            width / 2, labelMenuHeight + 45, Colors.BRIGHT_RED);
                }
            }
        }

        if (controller.isRequesting)
            context.drawCenteredTextWithShadow(textRenderer, "Requesting item data...", width / 2, labelMenuHeight + 10, Colors.BRIGHT_RED);

        // draw the label at the top
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        context.fill(0, 0, width, labelMenuHeight, Colors.GRAY);
        context.drawHorizontalLine(0, width, labelMenuHeight, Colors.WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Monumenta Item Dictionary").setStyle(Styles.BOLD),
                width / 2, (labelMenuHeight - textRenderer.fontHeight) / 2, Colors.TITLE);
        context.getMatrices().pop();

        // draw gui elements
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        searchBar.render(context, mouseX, mouseY, delta);
        reloadItemsButton.render(context, mouseX, mouseY, delta);
        filterButton.render(context, mouseX, mouseY, delta);
        resetFilterButton.render(context, mouseX, mouseY, delta);
        minMasterworkButton.render(context, mouseX, mouseY, delta);
        maxMasterworkButton.render(context, mouseX, mouseY, delta);
        tipsMasterworkButton.render(context, mouseX, mouseY, delta);

        if (!isGettingBuildItem) {
            builderGuiButton.render(context, mouseX, mouseY, delta);
            showCharmsButton.render(context, mouseX, mouseY, delta);
        }
        else builderButton.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();

        children().forEach(element -> ((Drawable) element).render(context, mouseX, mouseY, delta));
    }

    public void buildItemList() {
        controller.refreshItems();
        List<DictionaryItem> toBuildItems = controller.validItems;

        itemButtons.clear();
        widgetByItem.clear();
        int columns = (width - sideMenuWidth - 5) / (itemSize + itemPadding);
        for (int i = 0; i != toBuildItems.size(); i++) {
            DictionaryItem item = toBuildItems.get(i);
            int row = i / columns;
            int col = i % columns;

            int x = (col + 1) * itemPadding + col * itemSize;
            int y = labelMenuHeight + (row + 1) * itemPadding + row * itemSize;

            ItemButtonWidget button = new ItemButtonWidget(x, y, itemSize, Text.literal(item.name()), item, () -> generateItemLoreText(item), this);

            itemButtons.computeIfAbsent(y, k -> new ArrayList<>()).add(button);
            widgetByItem.put(item, button);
        }

        if (isGettingBuildItem) {
            for (List<ItemButtonWidget> row : itemButtons.values()) {
                row.forEach(ItemButtonWidget::setMaximumMasterwork);
            }
        }
    }

    public void returnItem(DictionaryItem item) {
        BuilderGui builderGui = controller.builderGui;

        int index = builderGui.itemTypesIndex.indexOf(itemTypeLookingFor);
        builderGui.buildItems.set(index, item);

        builderGui.updateButtons();
        builderGui.updateStats();
        controller.itemFilterGui.clearFilters();
        controller.setBuilderScreen();

        isGettingBuildItem = false;
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

        if (searchBar.mouseClicked(mouseX, mouseY, button)) searchBar.setFocused(true);
        reloadItemsButton.mouseClicked(mouseX, mouseY, button);
        filterButton.mouseClicked(mouseX, mouseY, button);
        resetFilterButton.mouseClicked(mouseX, mouseY, button);
        minMasterworkButton.mouseClicked(mouseX, mouseY, button);
        maxMasterworkButton.mouseClicked(mouseX, mouseY, button);
        tipsMasterworkButton.mouseClicked(mouseX, mouseY, button);

        if (!isGettingBuildItem) {
            builderGuiButton.mouseClicked(mouseX, mouseY, button);
            showCharmsButton.mouseClicked(mouseX, mouseY, button);
        }
        else builderButton.mouseClicked(mouseX, mouseY, button);

        if (mouseY > labelMenuHeight) {
            for (List<ItemButtonWidget> row : itemButtons
                    .subMap((int) mouseY + scrollPixels - itemSize, true,
                            (int) mouseY + scrollPixels, true).values()) {
                row.forEach(b -> b.mouseClicked(mouseX, mouseY + scrollPixels, button));
            }
        }

        return true;
    }

    public List<Text> generateItemLoreText(DictionaryItem item) {
        if (item == null) return new ArrayList<>(List.of(Text.literal("Click to add an item."))); //empty item slot in BuilderGUI

        int masterworkTier = item.getMaxMasterwork() - 1;
        ItemButtonWidget itemButton = widgetByItem.get(item);
        if (itemButton != null && !(MinecraftClient.getInstance().currentScreen instanceof BuilderGui)) {
            masterworkTier = itemButton.shownMasterworkTier;
        }

        String itemTier = item.hasMasterwork() ? item.getTierFromMasterwork(Math.max(masterworkTier, item.getMinMasterwork())) : item.getTierNoMasterwork();
        NbtCompound nbt = item.getNbt();

        List<Text> lines = new ArrayList<>();

        addNameLoreText(lines, item, nbt, itemTier);
        addBookAuthorLoreText(lines, item, nbt);

        List<Text> enchants = new ArrayList<>();
        List<Text> baseStats = new ArrayList<>();
        List<Text> stats = new ArrayList<>();

        List<ItemStat> showStats = item.hasMasterwork() ? item.getStatsFromMasterwork(masterworkTier) : item.getStatsNoMasterwork();

        if (showStats == null) {
            if (masterworkTier > item.getMinMasterwork())
                lines.add(Text.literal("The data for this tier is missing.").setStyle(Styles.RED));
            else if (masterworkTier < item.getMinMasterwork())
                lines.add(Text.literal("This masterwork tier does not exist.").setStyle(Styles.RED));
        }

        boolean isAlchemicalUtensil = false;
        if (showStats != null) {
            for (ItemStat stat : showStats) {
                if (stat.statName().equals("alchemical_utensil") && stat.statValue() > 0) {
                    isAlchemicalUtensil = true;
                    continue;
                }

                Text line = Text.literal(ItemFormatter.buildStatString(stat.statName(), stat.statValue()))
                        .setStyle(Style.EMPTY.withColor(ItemColors.getColorForStat(stat.statName(), stat.statValue())));
                if (ItemFormatter.isStat(stat.statName())) {
                    if (ItemFormatter.isBaseStat(stat.statName())) baseStats.add(line);
                    else stats.add(line);
                }
                else enchants.add(line);
            }
        }

        if (showStats != null) lines.addAll(enchants);

        if (item.type().equals("Wand"))
            lines.add(Text.literal("* Magic Wand *").setStyle(Styles.TEXT));
        if (isAlchemicalUtensil)
            lines.add(Text.literal("* Alchemical Utensil *").setStyle(Styles.TEXT));

        if (item.hasRegion() && item.hasTier()) {
            MutableText regionText = Text.literal(ItemFormatter.formatRegion(item.region()) + " : ").setStyle(Styles.TEXT);
            MutableText tierText = Text.literal(itemTier)
                    .setStyle(Style.EMPTY
                            .withColor(ItemColors.getColorForTier(itemTier))
                            .withBold(ItemFormatter.shouldUnderline(itemTier))
                            .withObfuscated(itemTier.equals("Obfuscated")));

            lines.add(regionText.append(tierText));
        }

        if (item.hasMasterwork()) {
            MutableText baseText = Text.literal("Masterwork : ").setStyle(Styles.TEXT);
            for (int i = 0; i < ItemFormatter.getMasterworkForRarity(itemTier); i ++) {
                if (i < masterworkTier)
                    baseText.append(Text.literal("★").setStyle(Styles.MASTERWORK));
                else
                    baseText.append(Text.literal("☆").setStyle(Styles.TEXT));
            }
            lines.add(baseText);
        }

        if (item.isFish()) {
            MutableText baseText = Text.literal("Fish Quality : ").setStyle(Styles.TEXT);
            for (int i = 0; i < ItemFormatter.getMaxFishTier(); i ++) {
                if (i < item.fishTier())
                    baseText.append(Text.literal("★").setStyle(Styles.FISH));
                else
                    baseText.append(Text.literal("☆").setStyle(Styles.TEXT));
            }
            lines.add(baseText);
        }

        if (item.hasLocation() && Mid.controller.locationData.containsKey(item.locationId())) {
            lines.add(ItemFormatter.getLocationText(item.locationId()));
        }

        if (!item.lore().isEmpty()) {
            if (hasShiftDown()) {
                for (String line : item.lore().split("\n")) {
                    lines.add(Text.literal(line).setStyle(Style.EMPTY.withColor(
                            ItemColors.mixHexes(ItemColors.TEXT_COLOR, ItemColors.getColorForLocation(item.locationId()), 0.67))));
                }
            }
            else lines.add(Text.literal("Press [SHIFT] to show lore.").setStyle(Styles.TEXT));
        }

        addPotionEffectLoreText(lines, nbt);

        if (showStats != null) {
            if (!stats.isEmpty() || !baseStats.isEmpty()) {
                lines.add(Text.empty());
                lines.add(Text.literal(ItemFormatter.formatUseLine(item.type())).setStyle(Styles.LIGHT_GRAY));
            }

            lines.addAll(baseStats);
            lines.addAll(stats);
        }

        lines.add(Text.empty());

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.getAbilities().creativeMode && !(MinecraftClient.getInstance().currentScreen instanceof BuilderGui))
            lines.add(Text.literal("[ALT] + Click to generate this item").setStyle(Styles.TEXT));

        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentScreen instanceof ItemDictionaryGui || currentScreen instanceof CharmDictionaryGui || currentScreen instanceof BuilderGui) {
            lines.add(Text.literal("[CTRL] [SHIFT] + Click to open in the wiki").setStyle(Styles.TEXT));

            if (nbt != null && item.baseItem().equals("Written Book") && nbt.contains("pages"))
                lines.add(Text.literal("Right Click to open the book").setStyle(Styles.TEXT));

            if (currentScreen instanceof BuilderGui) {
                lines.add(Text.literal("[SHIFT] + Click to delete item").setStyle(Styles.TEXT));
                lines.add(Text.literal("[CTRL] + Click to set the item as Build Icon").setStyle(Styles.TEXT));
            }
            lines.add(Text.literal(item.type() + " - " + item.baseItem()).setStyle(Styles.TEXT));
        }
        return lines;
    }

    private void addNameLoreText(List<Text> lines, DictionaryItem item, NbtCompound nbt, String itemTier) {
        if (MinecraftClient.getInstance().world == null) return;

        if (nbt == null || !nbt.getCompound("display").contains("Name")) {
            //old method
            lines.add(Text.literal(item.name()).setStyle(Style.EMPTY
                    .withColor(Colors.BLACK + ItemColors.getColorForLocation(item.locationId()))
                    .withBold(!item.isFish() ? ItemFormatter.shouldBold(itemTier) : ItemFormatter.shouldBoldFish(item.fishTier()))
                    .withUnderline(!item.isFish() ? ItemFormatter.shouldUnderline(itemTier) : ItemFormatter.shouldUnderlineFish(item.fishTier()))));
            return;
        }

        String nameJson = nbt.getCompound("display").getString("Name");
        Text text = Text.Serialization.fromJson(nameJson, MinecraftClient.getInstance().world.getRegistryManager());
        lines.add(text);
    }

    private void addBookAuthorLoreText(List<Text> lines, DictionaryItem item, NbtCompound nbt) {
        if (nbt != null && item.baseItem().equals("Written Book") && nbt.contains("author"))
            lines.add(Text.literal("by " + nbt.getString("author")).setStyle(Styles.LIGHT_GRAY));
    }

    private void addPotionEffectLoreText(List<Text> lines, NbtCompound nbt) {
        if (MinecraftClient.getInstance().world == null || nbt == null
                || !nbt.getCompound("Monumenta").getCompound("Stock").contains("Effects")) return;

        NbtList lore = nbt.getCompound("display").getList("Lore", 8);
        if (lore.isEmpty()) return;

        boolean addedNewline = false;
        boolean adding = false;
        for (NbtElement nbtElement : lore) {
            Text text = Text.Serialization.fromJson(nbtElement.asString(), MinecraftClient.getInstance().world.getRegistryManager());
            if (text == null) continue;

            String line = text.getString();
            if (line.equals("When Consumed:")) adding = true;
            else if (adding && line.isEmpty()) return;

            if (adding) {
                if (!addedNewline) {
                    lines.add(Text.empty());
                    addedNewline = true;
                }
                lines.add(text);
            }
        }
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
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);

        if (Screen.hasControlDown() && !isGettingBuildItem) {
            for (List<ItemButtonWidget> row : itemButtons
                    .subMap(labelMenuHeight + scrollPixels - itemSize, true,
                            height + scrollPixels, true)
                    .values()) {
                row.forEach(b -> b.scrolled(mouseX, mouseY, vAmount));
            }
        }
        else {
            if (mouseX >= 0 && mouseX < width - sideMenuWidth && mouseY >= labelMenuHeight && mouseY < height) {
                scrollPixels += (int) (-vAmount * 22); // scaled

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

    public void clearSearchBar() {
        searchBar.setText("");
    }
}
