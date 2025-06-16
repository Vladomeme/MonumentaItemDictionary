package dev.eliux.monumentaitemdictionary.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.gui.item.ItemDictionaryGui;
import dev.eliux.monumentaitemdictionary.util.ItemColors;
import dev.eliux.monumentaitemdictionary.util.ItemFactory;
import dev.eliux.monumentaitemdictionary.util.ItemStat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class ItemButtonWidget extends ButtonWidget {
    private final int itemSize;
    private final DictionaryItem item;
    private final ItemStack builtItem;
    public final int index;
    private final Supplier<List<Text>> tooltipTextSupplier;

    public int shownMasterworkTier;

    private final ItemDictionaryGui gui;

    public ItemButtonWidget(int x, int y, int itemSize, int index, Text message, PressAction onPress, DictionaryItem item, Supplier<List<Text>> tooltipTextSupplier, ItemDictionaryGui gui) {
        super(x, y, itemSize, itemSize, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.tooltipTextSupplier = tooltipTextSupplier;
        this.itemSize = itemSize;
        this.item = item;
        this.index = index;
        shownMasterworkTier = item.getMinMasterwork();

        this.gui = gui;

        // dummy itemstack for rendering item icon
        builtItem = ItemFactory.fromEncoding(item.baseItem.split("/")[0].trim().toLowerCase().replace(" ", "_"));
        NbtCompound baseNbt = builtItem.getOrCreateNbt();
        NbtCompound plain = new NbtCompound();
        NbtCompound display = new NbtCompound();
        display.putString("Name", item.name.split("\\(")[0].trim());
        plain.put("display", display);
        baseNbt.put("plain", plain);
        builtItem.setNbt(baseNbt);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
    }

    public boolean isItem(DictionaryItem item) {
        return this.item == item;
    }

    public void setMinimumMasterwork() {
        shownMasterworkTier = item.getMinMasterwork();
    }

    public void setMaximumMasterwork() {
        // shownMasterworkTier = item.getMaxMasterwork();
        int max = 0;
        for (ArrayList<ItemStat> stats : item.stats) {
            if (stats != null) max = item.stats.indexOf(stats);
        }
        shownMasterworkTier = max;
    }

    public void scrolled(double mouseX, double mouseY, double amount) {
        if (mouseX >= getX() && mouseX <= getX() + width && mouseY >= getY() - gui.getScrollPixels() && mouseY <= getY() + height - gui.getScrollPixels() && item.hasMasterwork) {
            shownMasterworkTier += amount;
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

        // rendering breaks if I do not use this, what is this, why do I have to use this, I don't know
        RenderSystem.enableDepthTest();

        boolean hovered = (mouseX >= minX) && (mouseX <= maxX) && (mouseY >= minY) && (mouseY <= maxY) && (mouseY > gui.labelMenuHeight);

        int outlineColor = hovered ? 0xFFC6C6C6 : 0xFFFFFFFF;
        int fillOpacity = hovered ? 0x6B000000 : 0x88000000;

        context.fill(minX, minY, maxX, maxY, fillOpacity | ItemColors.getColorForTier(item.hasMasterwork ? item.getTierFromMasterwork(item.getMinMasterwork()) : item.getTierNoMasterwork()));
        context.drawHorizontalLine(minX, maxX, minY, outlineColor);
        context.drawHorizontalLine(minX, maxX, maxY, outlineColor);
        context.drawVerticalLine(minX, minY, maxY, outlineColor);
        context.drawVerticalLine(maxX, minY, maxY, outlineColor);

        context.drawItem(builtItem, minX + (width / 2) - 7, minY + (height / 2) - 7);

        if (hovered) {
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltipTextSupplier.get(), mouseX, mouseY);
        }
    }
}
