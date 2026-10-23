package dev.eliux.monumentaitemdictionary.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.eliux.monumentaitemdictionary.gui.builder.BuilderGui;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class CheckBoxWidget extends CheckboxWidget {

    private final BuilderGui gui;
    private static final Identifier SELECTED_HIGHLIGHTED_TEXTURE = Identifier.of("widget/checkbox_selected_highlighted");
    private static final Identifier SELECTED_TEXTURE = Identifier.of("widget/checkbox_selected");
    private static final Identifier HIGHLIGHTED_TEXTURE = Identifier.of("widget/checkbox_highlighted");
    private static final Identifier TEXTURE = Identifier.of("widget/checkbox");
    private boolean checked;
    private final boolean showMessage;

    public CheckBoxWidget(int x, int y, Text message, boolean checked, boolean showMessage, BuilderGui gui) {
        super(x, y, 20, message, MinecraftClient.getInstance().textRenderer, checked, Callback.EMPTY);
        this.checked = checked;
        this.showMessage = showMessage;
        this.gui = gui;
    }

    @Override
    public void onPress() {
        checked = !checked;
        gui.updateCheckBoxes();
        gui.updateStats();
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        Identifier identifier;
        if (checked) identifier = isFocused() ? SELECTED_HIGHLIGHTED_TEXTURE : SELECTED_TEXTURE;
        else identifier = isFocused() ? HIGHLIGHTED_TEXTURE : TEXTURE;

        context.drawGuiTexture(identifier, getX(), getY(), 20, 20);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (showMessage) {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    getMessage(), getX() + 24, getY() + 8,
                    14737632 | MathHelper.ceil(alpha * 255.0F) << 24);
        }

    }
}
