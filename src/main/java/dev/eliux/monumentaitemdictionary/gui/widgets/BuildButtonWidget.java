package dev.eliux.monumentaitemdictionary.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.eliux.monumentaitemdictionary.gui.builder.BuildDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.builder.DictionaryBuild;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.util.ItemColors;
import dev.eliux.monumentaitemdictionary.util.ItemFactory;
import dev.eliux.monumentaitemdictionary.util.ItemFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BuildButtonWidget extends ButtonWidget {
    private final DictionaryBuild build;
    private final BuildDictionaryGui gui;
    private final ItemStack builtItem;
    private boolean favorite;

    public BuildButtonWidget(int x, int y, int itemSize, Text message, PressAction onPress, DictionaryBuild build, BuildDictionaryGui gui) {
        super(x, y, itemSize, itemSize, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.build = build;
        this.gui = gui;
        this.favorite = build.favorite;

        DictionaryItem displayingItem = build.itemOnButton;

        if (displayingItem != null) {
            builtItem = ItemFactory.fromEncoding(displayingItem.baseItem.split("/")[0].trim().toLowerCase().replace(" ", "_"));
            NbtCompound baseNbt = builtItem.getOrCreateNbt();
            NbtCompound plain = new NbtCompound();
            NbtCompound display = new NbtCompound();
            display.putString("Name", displayingItem.name.split("\\(")[0].trim());
            plain.put("display", display);
            baseNbt.put("plain", plain);
            builtItem.setNbt(baseNbt);
        } else {
            builtItem = ItemFactory.fromEncoding("barrier");
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int minX = getX();
        int minY = getY();
        int maxX = minX + width;
        int maxY = minY + height;

        // rendering breaks if I do not use this, what is this, why do I have to use this, I don't know
        RenderSystem.enableDepthTest();

        boolean hovered = (mouseX >= minX) && (mouseX <= maxX) && (mouseY >= minY) && (mouseY <= maxY) && (mouseY > gui.labelMenuHeight);

        int outlineColor = hovered ? 0xFFC6C6C6 : 0xFFFFFFFF;
        int fillOpacity = hovered ? 0x6B000000 : 0x88000000;

        context.fill(minX, minY, maxX, maxY, favorite ? 0x88FFFF00 : fillOpacity | (!Objects.equals(build.className, "No Class") ? ItemColors.getColorForClass(build.className) : 0x00000000));
        context.drawHorizontalLine(minX, maxX, minY, outlineColor);
        context.drawHorizontalLine(minX, maxX, maxY, outlineColor);
        context.drawVerticalLine(minX, minY, maxY, outlineColor);
        context.drawVerticalLine(maxX, minY, maxY, outlineColor);

        context.drawItem(builtItem, minX + (width / 2) - 7, minY + (height / 2) - 7);

        if (hovered) {
            List<Text> lines = new ArrayList<>();
            lines.add(Text.literal(build.name).setStyle(Style.EMPTY.withBold(true)));

            for (DictionaryItem item : build.allItems) {
                if (item == null) continue;
                String itemTier = item.hasMasterwork ? item.getTierFromMasterwork(item.getMaxMasterwork() - 1) : item.getTierNoMasterwork();
                lines.add(Text.literal(item.name).setStyle(Style.EMPTY
                        .withColor(0xFF000000 + ItemColors.getColorForLocation(item.location))
                        .withBold(ItemFormatter.shouldBold(itemTier))
                        .withUnderline(ItemFormatter.shouldUnderline(itemTier))));
            }

            lines.add(Text.literal("SHIFT + Click to toggle favorite on this build").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR)));
            lines.add(Text.literal("CTRL + SHIFT + Click to delete this build").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR)));
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, lines, mouseX, mouseY);
        }
    }

    public void updateFavorite() {
        favorite = !favorite;
    }
}
