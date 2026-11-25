package dev.eliux.monumentaitemdictionary.mixin;

import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.util.*;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Inject(method = "drawHoverEvent", at = @At("HEAD"), cancellable = true)
    private void mid$drawHoverEvent(TextRenderer textRenderer, Style style, int x, int y, CallbackInfo ci) {
        if (style == null) return;

        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent == null || hoverEvent.getAction() != HoverEvent.Action.SHOW_TEXT) return;

        Text text = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
        if (text == null) return;

        String[] lines = text.getString().split("\n");
        if (!lines[0].endsWith("'s Equipment")) return;

        //stats are calculated once per style instance
        if (((StyleDuck) style).mid$getStats() != null) {
            ((StyleDuck) style).mid$drawModifiedTooltip((DrawContext) (Object) this, textRenderer, x, y);
            ci.cancel();
            return;
        }

        //stat calculation
        List<DictionaryItem> items = new ArrayList<>();
        int[] itemOrder = new int[]{6, 5, 1, 2, 3, 4};
        boolean hasRingItems = false;
        for (int i : itemOrder) {
            if (lines[i].startsWith("No")) {
                items.add(null);
                continue;
            }

            DictionaryItem item = Mid.controller.getItemByName(lines[i].substring(1, lines[i].length() - 1), false);
            if (i != 6 && item.region() == ItemRegion.RING) hasRingItems = true;
            items.add(item);
        }

        //trying to fix exalted items
        if (hasRingItems) {
            for (int i = 0; i != 6; i++) {
                if (items.get(i) == null || items.get(i).region() == ItemRegion.RING) continue;

                DictionaryItem item = Mid.controller.getItemByName(items.get(i).name(), true);
                if (item != null) items.set(i, item);
            }
        }

        ItemRegion region = ItemRegion.RING;
        for (int i = 1; i != 6; i++) {
            if (items.get(i) != null) {
                region = items.get(i).region();
                break;
            }
        }

        ((StyleDuck) style).mid$setStats(new Stats(items, List.of(), List.of(), 100, region));
        ((StyleDuck) style).mid$drawModifiedTooltip((DrawContext) (Object) this, textRenderer, x, y);
        ci.cancel();
    }
}
