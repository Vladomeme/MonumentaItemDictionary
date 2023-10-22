package dev.eliux.monumentaitemdictionary.gui.charm;

import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import dev.eliux.monumentaitemdictionary.gui.widgets.CharmButtonWidget;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemIconButtonWidget;
import dev.eliux.monumentaitemdictionary.util.CharmStat;
import dev.eliux.monumentaitemdictionary.util.ItemColors;
import dev.eliux.monumentaitemdictionary.util.ItemFormatter;
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

public class CharmDictionaryGui extends Screen {
    public final int sideMenuWidth = 40;
    public final int labelMenuHeight = 30;
    public final int itemPadding = 7;
    public final int itemSize = 25;
    private int scrollPixels = 0;

    private long lastAltPressed = 0;

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

        searchBar = new TextFieldWidget(textRenderer, width / 2 + 90, 7, width / 2 - 100, 15, Text.literal("Search"));
        searchBar.setChangedListener(t -> {
            controller.setCharmNameFilter(searchBar.getText());
            if (searchBar.getText().equals(""))
                controller.clearCharmNameFilter();

            buildCharmList();
            updateScrollLimits();
        });
        searchBar.setFocused(true);

        reloadCharmsButton = new ItemIconButtonWidget(5, 5, 20, 20, Text.literal(""), (button) -> {
            controller.requestAndUpdate();
        }, Text.literal("Reload All Data"), "globe_banner_pattern", "");

        showItemsButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, labelMenuHeight + 10, 20, 20, Text.literal(""), (button) -> {
            controller.setItemDictionaryScreen();
        }, Text.literal("Item Data").setStyle(Style.EMPTY.withColor(0xFF00FFFF)), "iron_chestplate", "");

        filterButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 30, 20, 20, Text.literal(""), (button) -> {
            controller.setCharmFilterScreen();
        }, Text.literal("Filter"), "chest", "");

        resetFilterButton = new ItemIconButtonWidget(width - sideMenuWidth + 10, height - 60, 20, 20, Text.literal(""), (button) -> {
            controller.charmFilterGui.clearFilters();
            searchBar.setText("");
            buildCharmList();
        }, Text.literal("Reset Filters").setStyle(Style.EMPTY.withColor(0xFFFF0000)), "barrier", "");

        tipsMasterworkButton = new ItemIconButtonWidget(30, 5, 20, 20, Text.literal(""), (button) -> {
            Util.getOperatingSystem().open("https://github.com/Ilyiux/MonumentaItemDictionary");
        }, Arrays.asList(
                    Text.literal("Tips").setStyle(Style.EMPTY.withColor(0xFFFFFFFF)),
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
        int totalRows = (int) Math.ceil((double)charmButtons.size() / (double)((width - sideMenuWidth - 5) / (itemSize + itemPadding)));
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
            charmButtons.forEach(b -> {
                if (b.getY() - scrollPixels + itemSize >= labelMenuHeight && b.getY() - scrollPixels <= height) {
                    b.renderButton(context, mouseX, mouseY, delta);
                }
            });

            if (charmButtons.size() == 0) {
                context.drawCenteredTextWithShadow(textRenderer, "Found No Charms", width / 2, labelMenuHeight + 10, 0xFF2222);

                if (controller.anyCharms()) {
                    context.drawCenteredTextWithShadow(textRenderer, "It seems like there were no charms to begin with...", width / 2, labelMenuHeight + 30, 0xFF2222);
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
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Monumenta Charm Dictionary").setStyle(Style.EMPTY.withBold(true)), width / 2, (labelMenuHeight - textRenderer.fontHeight) / 2, 0xFFd8b427);
        context.getMatrices().pop();

        // draw gui elements
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        searchBar.render(context, mouseX, mouseY, delta);
        reloadCharmsButton.render(context, mouseX, mouseY, delta);
        showItemsButton.render(context, mouseX, mouseY, delta);
        filterButton.render(context, mouseX, mouseY, delta);
        resetFilterButton.render(context, mouseX, mouseY, delta);
        tipsMasterworkButton.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();

        try {
            super.render(context, mouseX, mouseY, delta);
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

            CharmButtonWidget button = new CharmButtonWidget(x, y, itemSize, index, Text.literal(charm.name), (b) -> {
                if (hasShiftDown() && hasControlDown()) {
                    String wikiFormatted = charm.name.replace(" ", "_").replace("'", "%27");
                    Util.getOperatingSystem().open("https://monumenta.wiki.gg/wiki/" + wikiFormatted);
                }

                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null && hasAltDown() && player.getAbilities().creativeMode) {
                    //ItemGenerator.giveItemToClientPlayer(charm.name);
                    controller.setGeneratorScreen().setCharm(charm);
                }
            }, charm, () -> generateCharmLoreText(charm), this);

            charmButtons.add(button);
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

        charmButtons.forEach((b) -> b.mouseClicked(mouseX, mouseY + scrollPixels, button));

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

        lines.add(Text.literal(charm.name).setStyle(Style.EMPTY
                .withColor(0xFF000000 + ItemColors.getColorForLocation(charm.location))
                .withBold(ItemFormatter.shouldBold(charm.tier))
                .withUnderline(ItemFormatter.shouldUnderline(charm.tier))));

        MutableText region = Text.literal(charm.region + " : ").setStyle(Style.EMPTY
                .withColor(ItemColors.TEXT_COLOR));
        MutableText tier = Text.literal(ItemFormatter.formatCharmTier(charm.tier)).setStyle(Style.EMPTY
                .withColor(ItemColors.getColorForTier(charm.tier))
                .withBold(ItemFormatter.shouldUnderline(charm.tier)));
        lines.add(region.append(tier));

        MutableText charmPowerDesc = Text.literal("Charm Power : ").setStyle(Style.EMPTY
                .withColor(ItemColors.TEXT_COLOR));
        MutableText charmPower = Text.literal("").setStyle(Style.EMPTY
                .withColor(ItemColors.TEXT_CHARM_POWER_COLOR));
        for (int i = 0; i < charm.power; i++) charmPower.append("★");
        MutableText divider = Text.literal(" - ").setStyle(Style.EMPTY
                .withColor(ItemColors.TEXT_COLOR));
        MutableText classText = Text.literal(charm.className).setStyle(Style.EMPTY
                        .withColor(ItemColors.getColorForClass(charm.className)));
        lines.add(charmPowerDesc.append(charmPower).append(divider).append(classText));

        lines.add(Text.literal(charm.location).setStyle(Style.EMPTY.withColor(ItemColors.getColorForLocation(charm.location))));

        lines.add(Text.literal(""));

        lines.add(Text.literal("When in Charm Slot:").setStyle(Style.EMPTY.withColor(0xAAAAAA)));
        for (CharmStat stat : charm.stats) {
            lines.add(Text.literal((stat.statValue >= 0 ? "+" : "") + stat.statValue + (stat.statNameFull.endsWith("percent") ? "" : " ") + ItemFormatter.formatCharmStat(stat.statNameFull)).setStyle(Style.EMPTY
                    .withColor(ItemColors.getColorForCharmStat(stat))));
        }

        lines.add(Text.literal(""));

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.getAbilities().creativeMode) {
            lines.add(Text.literal("[CTRL] [ALT] + Click to generate this item").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR)));
        }
        lines.add(Text.literal("[CTRL] [SHIFT] + Click to open in the wiki").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR)));
        lines.add(Text.literal(charm.baseItem).setStyle(Style.EMPTY
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

        showItemsButton.setX(width - sideMenuWidth + 10);
        showItemsButton.setY(labelMenuHeight + 10);

        filterButton.setX(width - sideMenuWidth + 10);
        filterButton.setY(height - 30);
        resetFilterButton.setX(width - sideMenuWidth + 10);
        resetFilterButton.setY(height - 60);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        super.mouseScrolled(mouseX, mouseY, amount);

        if (Screen.hasControlDown()) {
            charmButtons.forEach((b) -> b.scrolled(mouseX, mouseY, amount));
        } else {
            if (mouseX >= 0 && mouseX < width - sideMenuWidth && mouseY >= labelMenuHeight && mouseY < height) {
                scrollPixels += -amount * 22; // scaled

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
