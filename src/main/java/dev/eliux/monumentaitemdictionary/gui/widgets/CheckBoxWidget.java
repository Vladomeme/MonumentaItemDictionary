package dev.eliux.monumentaitemdictionary.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.eliux.monumentaitemdictionary.gui.builder.BuilderGui;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class CheckBoxWidget extends CheckboxWidget {
    private final BuilderGui gui;
    private static final Identifier TEXTURE = new Identifier("textures/gui/checkbox.png");
    private boolean checked;
    private final boolean showMessage;
    public CheckBoxWidget(int x, int y, int width, int height, Text message, boolean checked, boolean showMessage, BuilderGui gui) {
        super(x, y, width, height, message, showMessage);
        this.checked = checked;
        this.showMessage = showMessage;
        this.gui = gui;
    }

    @Override
    public void onPress() {
        this.checked = !this.checked;
        gui.updateCheckBoxes();
        gui.updateStats();
    }
    @Override
    public boolean isChecked() {
        return this.checked;
    }
    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.getNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                builder.put(NarrationPart.USAGE, Text.translatable("narration.checkbox.usage.focused"));
            } else {
                builder.put(NarrationPart.USAGE, Text.translatable("narration.checkbox.usage.hovered"));
            }
        }

    }
    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableDepthTest();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        context.drawTexture(TEXTURE, this.getX(), this.getY(), this.isFocused() ? 20.0F : 0.0F, this.checked ? 20.0F : 0.0F, 20, this.height, 64, 64);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.showMessage) {
            context.drawTextWithShadow(textRenderer, this.getMessage(), this.getX() + 24, this.getY() + (this.height - 8) / 2, 14737632 | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }

    }
}
