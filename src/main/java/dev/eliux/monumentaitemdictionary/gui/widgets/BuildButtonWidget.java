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
            builtItem = ItemFactory.fromItem(displayingItem);
            NbtCompound plain = new NbtCompound();
            NbtCompound display = new NbtCompound();
            display.putString("Name", displayingItem.name().split("\\(")[0].trim());
            plain.put("display", display);
            builtItem.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                    component -> component.apply(nbt -> nbt.put("plain", plain)));
        }
        else builtItem = ItemFactory.fromId("barrier");
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

        RenderSystem.enableDepthTest();

        boolean hovered = (mouseX >= minX) && (mouseX <= maxX) && (mouseY >= minY) && (mouseY <= maxY) && (mouseY > gui.labelMenuHeight);

        int outlineColor = hovered ? Colors.OUTLINE_HOVERED : Colors.WHITE;
        int fillOpacity = hovered ? Colors.OPACITY_HOVERED : Colors.OPACITY_DEFAULT;

        context.fill(minX, minY, maxX, maxY, favorite ? Colors.FAVORITE
                : fillOpacity | (!Objects.equals(build.className, "No Class") ? ItemColors.getColorForClass(build.className) : Colors.CLEAR));
        context.drawBorder(minX, minY, width, height, outlineColor);

        context.drawItem(builtItem, minX + (width / 2) - 7, minY + (height / 2) - 7);

        if (hovered) {
            List<Text> lines = new ArrayList<>();
            lines.add(Text.literal(build.name).setStyle(Styles.BOLD));

            for (DictionaryItem item : build.allItems) {
                if (item == null) continue;
                String itemTier = item.hasMasterwork() ? item.getTierFromMasterwork(item.getMaxMasterwork() - 1) : item.getTierNoMasterwork();
                addNameLoreText(lines, item, itemTier);
            }

            lines.add(Text.literal("SHIFT + Click to toggle favorite on this build").setStyle(Styles.TEXT));
            lines.add(Text.literal("CTRL + SHIFT + Click to delete this build").setStyle(Styles.TEXT));
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, lines, mouseX, mouseY);
        }
    }

    public void addNameLoreText(List<Text> lines, DictionaryItem item, String itemTier) {
        if (MinecraftClient.getInstance().world == null) return;

        NbtCompound nbt = item.getNbt();

        if (nbt == null || !nbt.getCompound("display").contains("Name")) {
            //old method
            lines.add(Text.literal(item.name()).setStyle(Style.EMPTY
                    .withColor(Colors.BLACK + ItemColors.getColorForLocation(item.locationId()))
                    .withBold(ItemFormatter.shouldBold(itemTier))
                    .withUnderline(ItemFormatter.shouldUnderline(itemTier))));
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
