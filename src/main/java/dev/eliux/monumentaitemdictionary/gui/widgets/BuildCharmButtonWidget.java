package dev.eliux.monumentaitemdictionary.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.eliux.monumentaitemdictionary.gui.builder.BuilderGui;
import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
import dev.eliux.monumentaitemdictionary.util.ItemColors;
import dev.eliux.monumentaitemdictionary.util.ItemFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.Math.ceil;

public class BuildCharmButtonWidget extends ButtonWidget {
    private final Supplier<List<Text>> loreSupplier;
    private final DictionaryCharm charm;
    private final BuilderGui gui;
    private final ItemStack builtItem;
    private final float scale;

    public BuildCharmButtonWidget(int x, int y, int itemSize, Text message, PressAction onPress, @Nullable DictionaryCharm charm, Supplier<List<Text>> loreSupplier, BuilderGui gui) {
        super(x, y, itemSize, itemSize, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.loreSupplier = loreSupplier;
        this.charm = charm;
        this.gui = gui;
        this.scale = (float) width/18;

        builtItem = getItemStack(charm);
    }

    private ItemStack getItemStack(@Nullable DictionaryCharm charm) {
        if (charm == null) {
            ItemStack builtItem = ItemFactory.fromEncoding("barrier");
            NbtCompound baseNbt = builtItem.getOrCreateNbt();
            NbtCompound plain = new NbtCompound();
            NbtCompound display = new NbtCompound();
            display.putString("Name", "No Item");
            plain.put("display", display);
            baseNbt.put("plain", plain);
            builtItem.setNbt(baseNbt);

            return builtItem;
        }
        ItemStack builtItem = ItemFactory.fromEncoding(charm.baseItem.split("/")[0].trim().toLowerCase().replace(" ", "_"));
        NbtCompound baseNbt = builtItem.getOrCreateNbt();

        NbtCompound monumenta = new NbtCompound();
        monumenta.putInt("CharmPower", charm.power);
        monumenta.putString("Tier", switch(charm.tier) {
            case "Base": yield "charm";
            case "Rare": yield "rarecharm";
            case "Epic": yield "epiccharm";
            default: yield "";
        });
        baseNbt.put("Monumenta", monumenta);

        NbtCompound plain = new NbtCompound();
        NbtCompound display = new NbtCompound();
        NbtList lore = new NbtList();
        lore.add(0, NbtString.of("Charm Power :  - " + charm.className));
        display.putString("Name", charm.name.split("\\(")[0].trim());
        display.put("Lore", lore);
        plain.put("display", display);

        baseNbt.put("plain", plain);

        builtItem.setNbt(baseNbt);
        return builtItem;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.enableDepthTest();

        int minX = getX();
        int minY = getY();
        int maxX = minX + width;
        int maxY = minY + height;
        int itemSize = (int) (16*scale);

        boolean hovered = (mouseX >= minX) && (mouseX <= maxX) && (mouseY >= minY) && (mouseY <= maxY) && (mouseY > gui.labelMenuHeight);

        int outlineColor = hovered ? 0xFFC6C6C6 : 0xFFFFFFFF;
        int fillOpacity = hovered ? 0x6B000000 : 0x88000000;

        context.fill(minX, minY, maxX, maxY, fillOpacity + (charm != null ? ItemColors.getColorForTier(charm.tier) : 0x00000000));
        context.drawHorizontalLine(minX, maxX, minY, outlineColor);
        context.drawHorizontalLine(minX, maxX, maxY, outlineColor);
        context.drawVerticalLine(minX, minY, maxY, outlineColor);
        context.drawVerticalLine(maxX, minY, maxY, outlineColor);

        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, scale);
        context.drawItem(builtItem, (int) ceil((minX + (double) width/2 - ceil(
                (double) itemSize/2))/scale), (int) ceil((minY + (double) height/2 - ceil((double) itemSize/2))/scale));
        context.getMatrices().pop();

        if (hovered) {
            List<Text> lines = new ArrayList<>();
            lines.add(Text.literal("Click to add an item."));
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, (charm != null ? loreSupplier.get() : lines), mouseX, mouseY);
        }
    }
}
