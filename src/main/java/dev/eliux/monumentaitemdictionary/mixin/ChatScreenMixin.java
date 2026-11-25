package dev.eliux.monumentaitemdictionary.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.util.StyleDuck;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawHoverEvent(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Style;II)V"))
    private void mid$render$wrap(DrawContext context, TextRenderer textRenderer, Style style, int x, int y, Operation<Void> original) {
        if (Mid.controller.lastTooltipSource == null) original.call(context, textRenderer, style, x, y);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void mid$render(DrawContext context, int x, int y, float delta, CallbackInfo ci) {
        if (Mid.controller.lastTooltipSource == null) return;

        if (Screen.hasShiftDown())
            ((StyleDuck) Mid.controller.lastTooltipSource).mid$drawModifiedTooltip(context, MinecraftClient.getInstance().textRenderer, x, y);
        else Mid.controller.lastTooltipSource = null;
    }
}
