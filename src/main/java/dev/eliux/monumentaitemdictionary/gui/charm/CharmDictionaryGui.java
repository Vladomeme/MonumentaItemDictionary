package dev.eliux.monumentaitemdictionary.gui.charm;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import dev.eliux.monumentaitemdictionary.gui.LazyScreen;
import dev.eliux.monumentaitemdictionary.gui.builder.BuilderGui;
import dev.eliux.monumentaitemdictionary.gui.widgets.CharmButtonWidget;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemIconButtonWidget;
import dev.eliux.monumentaitemdictionary.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.*;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class CharmDictionaryGui extends Screen implements LazyScreen {

    public final int sideMenuWidth = 40;
    public final int labelMenuHeight = 30;
    public final int itemPadding = 7;
    public final int itemSize = 25;
    private int scrollPixels = 0;

    private long lastAltPressed = 0;

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final TextRenderer textRenderer = client.textRenderer;

    private final List<CharmButtonWidget> charmButtons = new ArrayList<>();

    public TextFieldWidget searchBar;
    private ItemIconButtonWidget reloadCharmsButton;
    private ItemIconButtonWidget showItemsButton;
    private ItemIconButtonWidget filterButton;
    private ItemIconButtonWidget resetFilterButton;
    private ItemIconButtonWidget tipsMasterworkButton;

    public final DictionaryController controller;
    private ItemIconButtonWidget buildDictionaryButton;
    public boolean isGettingBuildCharm = false;
    private ItemIconButtonWidget builderButton;

    private boolean initialized = false;

    public CharmDictionaryGui(Text title, DictionaryController controller) {
        super(title);
        this.controller = controller;
    }

    public void postInit() {
        if (initialized) {
            updateGuiPositions();
            return;
        }
        initialized = true;

        buildCharmList();

        searchBar = new TextFieldWidget(textRenderer, width / 2 + 90, 7, width / 2 - 100, 15, Text.literal("Search"));
        searchBar.setChangedListener(t -> {
            scrollPixels = 0;
            buildCharmList();
            updateScrollLimits();
        });
        searchBar.setPlaceholder(Text.of("Search..."));
        searchBar.setFocused(true);

        reloadCharmsButton = new ItemIconButtonWidget(5, 5,
                (button) -> controller.requestAndUpdate(),
                Text.literal("Reload All Data"), Items.GLOBE_BANNER_PATTERN, "");

        showItemsButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, labelMenuHeight + 10,
                (button) -> controller.open(controller.itemGui),
                Text.literal("Item Data").setStyle(Styles.BLUE), Items.IRON_CHESTPLATE, "");

        buildDictionaryButton = new ItemIconButtonWidget(55, 5,
                (button) -> controller.open(controller.buildDictionaryGui),
                Text.literal("Open Builder GUI"), Items.IRON_CHESTPLATE, "");

        builderButton = new ItemIconButtonWidget(55, 5,
                (button) -> {
                    isGettingBuildCharm = false;
                    controller.open(controller.builderGui);
                },
                Text.literal("Go Back To Builder"), Items.ARROW, "");
        
        filterButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 30,
                (button) -> controller.open(controller.charmFilterGui),
                Text.literal("Filter"), Items.CHEST, "");

        resetFilterButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 60,
                (button) -> {
                    controller.charmFilterGui.clearFilters();
                    searchBar.setText("");
                    buildCharmList();
                }, Text.literal("Reset Filters").setStyle(Styles.RED), Items.BARRIER, "");

        tipsMasterworkButton = new ItemIconButtonWidget(30, 5,
                (button) -> Util.getOperatingSystem().open("https://github.com/Vladomeme/MonumentaItemDictionary"),
                List.of(
                        Text.literal("Tips").setStyle(Styles.WHITE),
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
                ), Items.OAK_SIGN, "");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        // draw the scroll bar
        int totalRows = (int) Math.ceil((double) charmButtons.size() / (double) ((width - sideMenuWidth - 5) / (itemSize + itemPadding)));
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
            if (!charmButtons.isEmpty()) {
                charmButtons.forEach(b -> {
                    if (b.getY() - scrollPixels + itemSize >= labelMenuHeight && b.getY() - scrollPixels <= height) {
                        b.renderWidget(context, mouseX, mouseY, delta);
                    }
                });
            }
            else {
                context.drawCenteredTextWithShadow(textRenderer, "Found No Charms", width / 2, labelMenuHeight + 10, Colors.BRIGHT_RED);

                if (controller.charms.isEmpty()) {
                    context.drawCenteredTextWithShadow(textRenderer, "It seems like there were no charms to begin with...",
                            width / 2, labelMenuHeight + 30, Colors.BRIGHT_RED);
                    context.drawCenteredTextWithShadow(textRenderer, "Try clicking the Reload All Data button in the top left",
                            width / 2, labelMenuHeight + 45, Colors.BRIGHT_RED);
                }
            }
        }

        if (controller.isRequesting) {
            context.drawCenteredTextWithShadow(textRenderer, "Requesting item data...", width / 2, labelMenuHeight + 10, Colors.BRIGHT_RED);
        }

        // draw the label at the top
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        context.fill(0, 0, width, labelMenuHeight, Colors.GRAY);
        context.drawHorizontalLine(0, width, labelMenuHeight, Colors.WHITE);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Monumenta Charm Dictionary").setStyle(Styles.BOLD),
                width / 2, (labelMenuHeight - textRenderer.fontHeight) / 2, Colors.CHARM_TITLE);
        context.getMatrices().pop();

        // draw gui elements
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        searchBar.render(context, mouseX, mouseY, delta);
        reloadCharmsButton.render(context, mouseX, mouseY, delta);
        filterButton.render(context, mouseX, mouseY, delta);
        resetFilterButton.render(context, mouseX, mouseY, delta);
        tipsMasterworkButton.render(context, mouseX, mouseY, delta);

        if (!isGettingBuildCharm) {
            showItemsButton.render(context, mouseX, mouseY, delta);
            buildDictionaryButton.render(context, mouseX, mouseY, delta);
        }
        else builderButton.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();

        children().forEach(element -> ((Drawable) element).render(context, mouseX, mouseY, delta));
    }

    public void buildCharmList() {
        controller.refreshCharms();

        charmButtons.clear();
        int columns = (width - sideMenuWidth - 5) / (itemSize + itemPadding);
        for (int i = 0; i != controller.validCharms.size(); i++) {
            DictionaryCharm charm = controller.validCharms.get(i);
            int row = i / columns;
            int col = i % columns;

            int x = (col + 1) * itemPadding + col * itemSize;
            int y = labelMenuHeight + (row + 1) * itemPadding + row * itemSize;

            charmButtons.add(new CharmButtonWidget(x, y, Text.literal(charm.name()), charm, this));
        }
    }

    public void returnCharm(DictionaryCharm charm) {
        BuilderGui builderGui = controller.builderGui;

        builderGui.charms.add(charm);

        controller.open(builderGui);
        builderGui.updateButtons();

        isGettingBuildCharm = false;
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
                controller.charmFilterGui.clearFilters();
                searchBar.setText("");
                buildCharmList();
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
        reloadCharmsButton.mouseClicked(mouseX, mouseY, button);
        filterButton.mouseClicked(mouseX, mouseY, button);
        resetFilterButton.mouseClicked(mouseX, mouseY, button);
        tipsMasterworkButton.mouseClicked(mouseX, mouseY, button);

        if (!isGettingBuildCharm) {
            showItemsButton.mouseClicked(mouseX, mouseY, button);
            buildDictionaryButton.mouseClicked(mouseX, mouseY, button);
        }
        else builderButton.mouseClicked(mouseX, mouseY, button);

        if (mouseY > labelMenuHeight)
            charmButtons.forEach((b) -> b.mouseClicked(mouseX, mouseY + scrollPixels, button));

        return true;
    }

    public List<Text> generateCharmLoreText(DictionaryCharm charm) {
        List<Text> lines = new ArrayList<>();

        NbtCompound nbt;
        try {
            nbt = StringNbtReader.parse(charm.nbt());
        }
        catch (CommandSyntaxException ignored) {
            nbt = null;
        }

        addNameLoreText(lines, charm, nbt);

        MutableText region = Text.literal(charm.region().name + " : ").setStyle(Styles.TEXT);
        MutableText tier = charm.tier().formattedCharmName;
        lines.add(region.append(tier));

        MutableText charmPowerDesc = Text.literal("Charm Power : ").setStyle(Styles.TEXT);
        MutableText charmPower = Text.literal("★".repeat(charm.power())).setStyle(Styles.CHARM_POWER);
        MutableText divider = Text.literal(" - ").setStyle(Styles.TEXT);
        MutableText classText = Text.literal(charm.itemClass().name).setStyle(charm.itemClass().style);
        lines.add(charmPowerDesc.append(charmPower).append(divider).append(classText));

        if (charm.location() != Location.EMPTY) lines.add(charm.location().text());

        if (!charm.lore().isEmpty()) {
            if (hasShiftDown()) lines.addAll(charm.lore());
            else lines.add(Text.literal("Press [SHIFT] to show lore.").setStyle(Styles.TEXT));
        }

        if (!charm.stats().isEmpty()) addStatsLoreText(lines, nbt);

        lines.add(Text.empty());

        ClientPlayerEntity player = client.player;
        if (player != null && player.getAbilities().creativeMode && !(client.currentScreen instanceof BuilderGui)) {
            lines.add(Text.literal("[ALT] + Click to generate this item").setStyle(Styles.TEXT));
        }
        lines.add(Text.literal("[CTRL] [SHIFT] + Click to open in the wiki").setStyle(Styles.TEXT));
        if (client.currentScreen instanceof BuilderGui) {
            lines.add(Text.literal("[SHIFT] + Click to delete item").setStyle(Styles.TEXT));
        }

        lines.add(Text.literal(charm.baseItem().getName().getString()).setStyle(Styles.TEXT));

        return lines;
    }

    private void addNameLoreText(List<Text> lines, DictionaryCharm charm, NbtCompound nbt) {
        if (client.world == null) return;

        if (nbt == null || !nbt.getCompound("display").contains("Name")) {
            //old method
            lines.add(Text.literal(charm.name()).setStyle(Style.EMPTY
                    .withColor(Colors.BLACK + charm.location().color())
                    .withBold(charm.tier().isBold)
                    .withUnderline(charm.tier().isUnderlined)));
            return;
        }

        String nameJson = nbt.getCompound("display").getString("Name");
        Text text = Text.Serialization.fromJson(nameJson, client.world.getRegistryManager());
        lines.add(text);
    }

    private void addStatsLoreText(List<Text> lines, NbtCompound nbt) {
        if (client.world == null || nbt == null) return;

        NbtList lore = nbt.getCompound("display").getList("Lore", 8);
        if (lore.isEmpty()) return;

        boolean addedNewline = false;
        boolean adding = false;
        for (NbtElement nbtElement : lore) {
            MutableText text = Text.Serialization.fromJson(nbtElement.asString(), client.world.getRegistryManager());
            if (text == null) continue;

            String line = text.getString();
            if (line.equals("When in Charm Slot:")) adding = true;
            else if (adding && line.isEmpty()) return;

            if (adding) {
                if (!addedNewline) {
                    lines.add(Text.empty());
                    addedNewline = true;
                }

                //reconstruct the text to replace the Locked symbol
                if (line.charAt(0) == '#')
                    text = Text.literal("\uD83D\uDD12" + text.getString().substring(1)).setStyle(text.getStyle());

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
        buildCharmList();
        updateScrollLimits();

        searchBar.setX(width / 2 + 90);
        searchBar.setWidth(width / 2 - 100);

        showItemsButton.setX(width - sideMenuWidth + 10);
        showItemsButton.setY(labelMenuHeight + 10);

        filterButton.setX(width - sideMenuWidth + 10);
        filterButton.setY(height - 30);
        resetFilterButton.setX(width - sideMenuWidth + 10);
        resetFilterButton.setY(height - 60);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);

        if (!Screen.hasControlDown()) {
            if (mouseX >= 0 && mouseX < width - sideMenuWidth && mouseY >= labelMenuHeight && mouseY < height) {
                scrollPixels += (int) (-vAmount * 22); // scaled

                updateScrollLimits();
            }
        }

        return true;
    }

    private void updateScrollLimits() {
        int rows = (int) Math.ceil((double) charmButtons.size() / (double) ((width - sideMenuWidth - 5) / (itemSize + itemPadding)));
        int maxScroll = rows * itemSize + (rows + 1) * itemPadding - height + labelMenuHeight;
        if (scrollPixels > maxScroll) scrollPixels = maxScroll;

        if (scrollPixels < 0) scrollPixels = 0;
    }

    public int getScrollPixels() {
        return scrollPixels;
    }
}
