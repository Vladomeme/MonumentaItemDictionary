package dev.eliux.monumentaitemdictionary.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("y")
    int getY();

    @Accessor("x")
    int getX();

    @Accessor("backgroundWidth")
    int getBackGroundWidth();

    @Accessor("handler")
    ScreenHandler getHandler();

}