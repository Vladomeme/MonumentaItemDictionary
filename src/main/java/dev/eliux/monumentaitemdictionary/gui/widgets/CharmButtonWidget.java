package dev.eliux.monumentaitemdictionary.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.eliux.monumentaitemdictionary.gui.charm.CharmDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
import dev.eliux.monumentaitemdictionary.util.ItemColors;
import dev.eliux.monumentaitemdictionary.util.ItemFactory;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

public class CharmButtonWidget extends ButtonWidget {
    private final DictionaryCharm charm;
    private final ItemStack builtItem;
    public final int index;
    private final Supplier<List<Text>> tooltipTextSupplier;

    private final CharmDictionaryGui gui;

    public CharmButtonWidget(int x, int y, int charmSize, int index, Text message, PressAction onPress, DictionaryCharm charm, Supplier<List<Text>> tooltipTextSupplier, CharmDictionaryGui gui) {
        super(x, y, charmSize, charmSize, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.tooltipTextSupplier = tooltipTextSupplier;
        this.charm = charm;
        this.index = index;

        this.gui = gui;

        // dummy itemstack for rendering item icon
        builtItem = ItemFactory.fromEncoding(charm.baseItem.split("/")[0].trim().toLowerCase().replace(" ", "_"));
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
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);


    }
     public void scrolled (double mouseX, double mouseY, double amount) {
     }
    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
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

        context.fill(minX, minY, maxX, maxY, fillOpacity + ItemColors.getColorForTier(charm.tier));
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
