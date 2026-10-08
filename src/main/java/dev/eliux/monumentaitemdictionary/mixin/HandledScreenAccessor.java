package dev.eliux.monumentaitemdictionary.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("y")
    int mid$getY();

    @Accessor("x")
    int mid$getX();

    @Accessor("backgroundWidth")
    int mid$getBackGroundWidth();

    @Accessor("handler")
    ScreenHandler mid$getHandler();

}