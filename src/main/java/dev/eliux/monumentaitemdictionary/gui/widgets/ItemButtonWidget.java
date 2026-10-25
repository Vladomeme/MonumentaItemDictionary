package dev.eliux.monumentaitemdictionary.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.gui.item.ItemDictionaryGui;
import dev.eliux.monumentaitemdictionary.util.Colors;
import dev.eliux.monumentaitemdictionary.util.ItemColors;
import dev.eliux.monumentaitemdictionary.util.ItemFactory;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class ItemButtonWidget extends ClickableWidget {

    private final DictionaryItem item;
    private final ItemStack builtItem;
    private final Supplier<List<Text>> tooltipTextSupplier;

    public int shownMasterworkTier;

    private final ItemDictionaryGui gui;

    public ItemButtonWidget(int x, int y, int itemSize, Text message, DictionaryItem item,
                            Supplier<List<Text>> tooltipTextSupplier, ItemDictionaryGui gui) {
        super(x, y, itemSize, itemSize, message);
        this.tooltipTextSupplier = tooltipTextSupplier;
        this.item = item;
        shownMasterworkTier = item.getMinMasterwork();

        this.gui = gui;

        builtItem = ItemFactory.fromItem(item);
        NbtCompound plain = new NbtCompound();
        NbtCompound display = new NbtCompound();
        display.putString("Name", item.name().split("\\(")[0].trim());
        plain.put("display", display);
        builtItem.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                component -> component.apply(nbt -> nbt.put("plain", plain)));
    }

    public void setMinimumMasterwork() {
        shownMasterworkTier = item.getMinMasterwork();
    }

    public void setMaximumMasterwork() {
        for (int i = item.stats().size() - 1; i >= 0; i--) {
            if (item.stats().get(i) != null) {
                shownMasterworkTier = i;
                return;
            }
        }
        shownMasterworkTier = 0;
    }

    public void scrolled(double mouseX, double mouseY, double amount) {
        if (mouseX >= getX() && mouseX <= getX() + width && mouseY >= getY() - gui.getScrollPixels()
                && mouseY <= getY() + height - gui.getScrollPixels() && item.hasMasterwork()) {
            shownMasterworkTier += (int) amount;
            if (shownMasterworkTier < 0) shownMasterworkTier = 0;
            if (shownMasterworkTier > item.getMaxMasterwork() - 1) shownMasterworkTier = item.getMaxMasterwork() - 1;
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int yPixelOffset = -gui.getScrollPixels();

        int minX = getX();
        int minY = getY() + yPixelOffset;
        int maxX = minX + width;
        int maxY = minY + height;

        RenderSystem.enableDepthTest();

        boolean hovered = (mouseX >= minX) && (mouseX <= maxX) && (mouseY >= minY) && (mouseY <= maxY) && (mouseY > gui.labelMenuHeight);

        int outlineColor = hovered ? Colors.OUTLINE_HOVERED : Colors.WHITE;
        int fillOpacity = hovered ? Colors.OPACITY_HOVERED : Colors.OPACITY_DEFAULT;

        context.fill(minX, minY, maxX, maxY, fillOpacity | ItemColors.getColorForTier(
                item.hasMasterwork() ? item.getTierFromMasterwork(item.getMinMasterwork()) : item.getTierNoMasterwork()));
        context.drawBorder(minX, minY, width, height, outlineColor);

        context.enableScissor(0, gui.labelMenuHeight + 1, gui.width, gui.height);
        context.drawItem(builtItem, minX + (width / 2) - 7, minY + (height / 2) - 7);
        context.disableScissor();

        if (hovered) {
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltipTextSupplier.get(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!clicked(mouseX, mouseY)) return false;

        playDownSound(MinecraftClient.getInstance().getSoundManager());

        if (button == 0) {
            if (Screen.hasShiftDown() && Screen.hasControlDown()) {
                String wikiFormatted = item.name().replace(" ", "_").replace("'", "%27");
                Util.getOperatingSystem().open("https://monumenta.wiki.gg/wiki/" + wikiFormatted);
                return true;
            }
            else if (Mid.controller.itemGui.isGettingBuildItem && !Screen.hasAltDown()) {
                Mid.controller.itemGui.returnItem(item);
                return true;
            }

            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null && Screen.hasAltDown() && player.getAbilities().creativeMode)
                Mid.controller.setGeneratorScreen().setItem(item);
        }
        else if (button == 1) {
            NbtCompound nbt = item.getNbt();
            if (nbt != null && item.baseItem().equals("Written Book") && nbt.contains("pages")) {
                item.openBook();
            }
        }
        return true;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
