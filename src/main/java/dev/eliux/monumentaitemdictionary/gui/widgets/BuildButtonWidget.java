package dev.eliux.monumentaitemdictionary.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.eliux.monumentaitemdictionary.gui.builder.BuildDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.builder.DictionaryBuild;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BuildButtonWidget extends ButtonWidget {

    private final DictionaryBuild build;
    private final BuildDictionaryGui gui;
    private final ItemStack builtItem;
    private boolean favorite;

    public BuildButtonWidget(int x, int y, Text message, PressAction onPress, DictionaryBuild build, BuildDictionaryGui gui) {
        super(x, y, gui.itemSize, gui.itemSize, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.build = build;
        this.gui = gui;
        this.favorite = build.favorite;

        DictionaryItem displayingItem = build.itemOnButton;

        if (displayingItem != null) {
            builtItem = ItemFactory.fromItem(displayingItem);
            NbtCompound plain = new NbtCompound();
            NbtCompound display = new NbtCompound();
            display.putString("Name", displayingItem.name().split("\\(")[0].trim());
            plain.put("display", display);
            builtItem.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                    component -> component.apply(nbt -> nbt.put("plain", plain)));
        }
        else builtItem = ItemFactory.fromBase(Items.BARRIER);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int minX = getX();
        int minY = getY();
        int maxX = minX + width;
        int maxY = minY + height;

        RenderSystem.enableDepthTest();

        boolean hovered = (mouseX >= minX) && (mouseX <= maxX) && (mouseY >= minY) && (mouseY <= maxY) && (mouseY > gui.labelMenuHeight);

        int outlineColor;
        int fillOpacity;
        if (hovered) {
            outlineColor = Colors.OUTLINE_HOVERED;
            fillOpacity = Colors.OPACITY_HOVERED;
        }
        else {
            outlineColor = Colors.WHITE;
            fillOpacity = Colors.OPACITY_DEFAULT;
        }

        context.fill(minX, minY, maxX, maxY, favorite ? Colors.FAVORITE
                : fillOpacity | (build.className != PlayerClass.NO_CLASS ? build.className.color : Colors.CLEAR));
        context.drawBorder(minX, minY, width, height, outlineColor);

        //(minX + (width / 2) - 7, minY + (height / 2) - 7), change if w/h becomes non-constant
        context.drawItem(builtItem, minX + 18, minY + 18);

        if (hovered) {
            List<Text> lines = new ArrayList<>();
            lines.add(Text.literal(build.name).setStyle(Styles.BOLD));

            for (DictionaryItem item : build.allItems) {
                if (item == null) continue;
                addNameLoreText(lines, item);
            }

            lines.add(Text.literal("SHIFT + Click to toggle favorite on this build").setStyle(Styles.TEXT));
            lines.add(Text.literal("CTRL + SHIFT + Click to delete this build").setStyle(Styles.TEXT));
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, lines, mouseX, mouseY);
        }
    }

    public void addNameLoreText(List<Text> lines, DictionaryItem item) {
        if (MinecraftClient.getInstance().world == null) return;

        NbtCompound nbt = item.getNbt();

        if (nbt == null || !nbt.getCompound("display").contains("Name")) {
            //old method
            ItemTier itemTier = item.getTierAtMaxMasterwork();
            lines.add(Text.literal(item.name()).setStyle(Style.EMPTY
                    .withColor(Colors.BLACK + item.location().color())
                    .withBold(itemTier.isBold)
                    .withUnderline(itemTier.isUnderlined)));
            return;
        }

        String nameJson = nbt.getCompound("display").getString("Name");
        Text text = Text.Serialization.fromJson(nameJson, MinecraftClient.getInstance().world.getRegistryManager());
        lines.add(text);
    }

    public void updateFavorite() {
        favorite = !favorite;
    }
}
