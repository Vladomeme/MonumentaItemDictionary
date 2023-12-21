package dev.eliux.monumentaitemdictionary.gui.widgets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.function.Consumer;

public class ColorPickerWidget extends ButtonWidget {
    private boolean isOpen = false;

    private final int popupWidth;
    private final int popupHeight;

    private final Consumer<Integer> onSelect;

    private int displayColor = 0xFFFF0000;
    private float hue = 1;
    private float saturation = 1;
    private float brightness = 1;

    public ColorPickerWidget(int x, int y, int width, int height, int popupWidth, int popupHeight, Consumer<Integer> onSelect) {
        super(x, y, width, height, Text.literal(""), (b) -> {}, DEFAULT_NARRATION_SUPPLIER);
        this.onSelect = onSelect;
        this.popupWidth = popupWidth;
        this.popupHeight = popupHeight;
    }

    public void setColor(int newColor) {
        displayColor = newColor;
        float[] hsb = Color.RGBtoHSB((newColor >> 16)&0xFF, (newColor >> 8)&0xFF, newColor&0xFF, null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
    }

    public void renderMain(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        int borderColor = isOpen ? 0xFFFFFFFF : 0xFFAAAAAA;

        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), displayColor);
        context.drawHorizontalLine(getX() - 1, getX() + getWidth(), getY() - 1, borderColor);
        context.drawHorizontalLine(getX() - 1, getX() + getWidth(), getY() + getHeight(), borderColor);
        context.drawVerticalLine(getX() - 1, getY() - 1, getY() + getHeight(), borderColor);
        context.drawVerticalLine(getX() + getWidth(), getY() - 1, getY() + getHeight(), borderColor);
    }

    public void renderPopup(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible || !isOpen) return;

        // main box
        for (int x = 0; x < popupWidth; x++) {
            for (int y = 0; y < popupHeight; y++) {
                int color = Color.HSBtoRGB(hue, (float)x / popupWidth, 1f - ((float)y / popupHeight));
                context.fill(getX() + x, getY() + getHeight() + y, getX() + x + 1, getY() + getHeight() + y + 1, color);
            }
        }

        context.drawHorizontalLine(getX() - 1, getX() + popupWidth, getY() + getHeight(), 0xFFFFFFFF);
        context.drawHorizontalLine(getX() - 1, getX() + popupWidth, getY() + getHeight() + popupHeight, 0xFFFFFFFF);
        context.drawVerticalLine(getX() - 1, getY() + getHeight(), getY() + getHeight() + popupHeight, 0xFFFFFFFF);
        context.drawVerticalLine(getX() + popupWidth, getY() + getHeight(), getY() + getHeight() + popupHeight, 0xFFFFFFFF);

        // hue box
        for (int x = 0; x < popupWidth; x++) {
            int color = Color.HSBtoRGB((float)x / popupWidth, 1, 1);
            context.fill(getX() + x, getY() + getHeight() + popupHeight + 4, getX() + x + 1, getY() + getHeight() + popupHeight + 10, color);
        }

        context.drawHorizontalLine(getX() - 1, getX() + popupWidth, getY() + getHeight() + popupHeight + 4, 0xFFFFFFFF);
        context.drawHorizontalLine(getX() - 1, getX() + popupWidth, getY() + getHeight() + popupHeight + 10, 0xFFFFFFFF);
        context.drawVerticalLine(getX() - 1, getY() + getHeight() + popupHeight + 4, getY() + getHeight() + popupHeight + 10, 0xFFFFFFFF);
        context.drawVerticalLine(getX() + popupWidth, getY() + getHeight() + popupHeight + 4, getY() + getHeight() + popupHeight + 10, 0xFFFFFFFF);
    }

    public boolean willClick(double mouseX, double mouseY) {
        return (isOpen && visible && (mouseY >= getY() + getHeight() && mouseY <= getY() + getHeight() + popupHeight + 11 && mouseX > getX() && mouseX < getX() + popupWidth));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (!visible) return false;

        if (isOpen) {
            if (mouseX >= getX() && mouseX <= getX() + popupWidth && mouseY >= getY() + getHeight() && mouseY <= getY() + getHeight() + popupHeight) {
                saturation = (float)(mouseX - getX()) / popupWidth;
                brightness = 1f - (float)(mouseY - getY() - getHeight()) / popupHeight;

                this.playDownSound(MinecraftClient.getInstance().getSoundManager());

                int newColor = 0xFF000000 + Color.HSBtoRGB(hue, saturation, brightness);
                onSelect.accept(newColor);
                displayColor = newColor;
            } else if (mouseX >= getX() && mouseX <= getX() + popupWidth && mouseY >= getY() + getHeight() + popupHeight + 4 && mouseY <= getY() + getHeight() + popupHeight + 11) {
                hue = (float)(mouseX - getX()) / popupWidth;

                this.playDownSound(MinecraftClient.getInstance().getSoundManager());

                int newColor = 0xFF000000 + Color.HSBtoRGB(hue, saturation, brightness);
                onSelect.accept(newColor);
                displayColor = newColor;
            } else if (mouseX < getX() || mouseX > getX() + getWidth() || mouseY < getY() || mouseY > getY() + getHeight()) {
                isOpen = false;

                this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            }

        }

        return true;
    }

    @Override
    public void onPress() {
        super.onPress();

        isOpen = !isOpen;
    }
}
