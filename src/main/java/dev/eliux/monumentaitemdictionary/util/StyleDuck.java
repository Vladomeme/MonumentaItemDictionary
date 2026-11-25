package dev.eliux.monumentaitemdictionary.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public interface StyleDuck {

    void mid$setStats(Stats stats);
    Stats mid$getStats();
    void mid$drawModifiedTooltip(DrawContext context, TextRenderer tr, int x, int y);
}
