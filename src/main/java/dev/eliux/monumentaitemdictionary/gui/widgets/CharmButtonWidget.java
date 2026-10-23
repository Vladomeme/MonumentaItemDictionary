package dev.eliux.monumentaitemdictionary.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import dev.eliux.monumentaitemdictionary.gui.builder.BuilderGui;
import dev.eliux.monumentaitemdictionary.gui.charm.CharmDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
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
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class CharmButtonWidget extends ClickableWidget {

    private final DictionaryCharm charm;
    private final ItemStack builtItem;
    private final Supplier<List<Text>> tooltipTextSupplier;

    private final CharmDictionaryGui gui;

    public CharmButtonWidget(int x, int y, int charmSize, Text message, DictionaryCharm charm,
                             Supplier<List<Text>> tooltipTextSupplier, CharmDictionaryGui gui) {
        super(x, y, charmSize, charmSize, message);
        this.tooltipTextSupplier = tooltipTextSupplier;
        this.charm = charm;

        this.gui = gui;

        builtItem = ItemFactory.fromId(charm.baseItem().toLowerCase().replace(" ", "_"));

        NbtCompound monumenta = new NbtCompound();
        monumenta.putInt("CharmPower", charm.power());
        monumenta.putString("Tier", switch (charm.tier()) {
            case "Base" -> "charm";
            case "Rare" -> "rarecharm";
            case "Epic" -> "epiccharm";
            default -> "";
        });
        builtItem.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                component -> component.apply(nbt -> nbt.put("Monumenta", monumenta)));

        NbtCompound plain = new NbtCompound();
        NbtCompound display = new NbtCompound();
        NbtList lore = new NbtList();
        lore.add(0, NbtString.of("Charm Power :  - " + charm.className()));
        display.putString("Name", charm.name().split("\\(")[0].trim());
        display.put("Lore", lore);
        plain.put("display", display);
        builtItem.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                component -> component.apply(nbt -> nbt.put("plain", plain)));
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

        context.fill(minX, minY, maxX, maxY, fillOpacity + ItemColors.getColorForTier(charm.tier()));
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
            DictionaryController controller = Mid.controller;
            if (Screen.hasShiftDown() && Screen.hasControlDown()) {
                String wikiFormatted = charm.name().replace(" ", "_").replace("'", "%27");
                Util.getOperatingSystem().open("https://monumenta.wiki.gg/wiki/" + wikiFormatted);
            }
            else if (!Screen.hasAltDown() && controller.charmGui.isGettingBuildCharm
                    && charm.power() + controller.builderGui.getCharmsListWithPower().size() <= BuilderGui.MAX_CHARM_POWER && !Screen.hasAltDown())
                controller.charmGui.returnCharm(charm);

            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null && Screen.hasAltDown() && player.getAbilities().creativeMode)
                controller.setGeneratorScreen().setCharm(charm);
        }
        return true;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
