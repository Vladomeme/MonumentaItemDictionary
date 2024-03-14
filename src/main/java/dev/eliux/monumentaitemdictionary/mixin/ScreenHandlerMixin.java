package dev.eliux.monumentaitemdictionary.mixin;

import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import dev.eliux.monumentaitemdictionary.gui.builder.DictionaryBuild;
import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemIconButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Mixin(Screen.class)
public abstract class ScreenHandlerMixin {
    @Shadow protected abstract <T extends Element & Selectable> T addSelectableChild(T child);

    @Shadow protected abstract <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement);

    @Shadow public abstract void close();

    @Unique private ButtonWidget buildFromInventoryButton = null;
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen currentScreen = client.currentScreen;
        if (currentScreen instanceof GenericContainerScreen && buildFromInventoryButton != null) {
            buildFromInventoryButton.active = !isOnArmoryMainscreen(
                    currentScreen.getTitle().getString(),
                    ((HandledScreenAccessor) currentScreen).getHandler().slots);
            buildFromInventoryButton.renderButton(matrices, mouseX, mouseY, delta);
        }
    }

    @Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("TAIL"))
    private void init(MinecraftClient client, int width, int height, CallbackInfo ci) {
        String inventoryTitle = Objects.requireNonNull(client.currentScreen).getTitle().getString();
        if (client.currentScreen instanceof GenericContainerScreen && (inventoryTitle.equals("Player Stats Calculator") || inventoryTitle.equals("Mechanical Armory"))) {
            int x = ((HandledScreenAccessor) client.currentScreen).getX() + ((HandledScreenAccessor) client.currentScreen).getBackGroundWidth() - 20;
            int y = ((HandledScreenAccessor) client.currentScreen).getY() - 20;

            buildFromInventoryButton =
                    new ItemIconButtonWidget(x, y, 20, 20, Text.literal(""), button -> {
                        DefaultedList<Slot> slots = ((HandledScreenAccessor) client.currentScreen).getHandler().slots;
                        DictionaryController controller = new DictionaryController();

                        DictionaryBuild buildFromInventory =
                                (inventoryTitle.equals("Mechanical Armory")) ? getBuildFromMechanicalArmory(client,
                                        slots, controller) : getBuildFromPlayerStats(client, slots, controller);
                        this.close();
                        controller.setBuilderScreen();
                        controller.builderGui.loadItems(buildFromInventory);
                    }, Text.literal("Add build from current inventory"), "name_tag", "");

            addSelectableChild(buildFromInventoryButton);
            addDrawableChild(buildFromInventoryButton);
        }
    }

    @Unique
    private boolean isOnArmoryMainscreen(String inventoryTitle, DefaultedList<Slot> slots) {
        return inventoryTitle.equals("Mechanical Armory") && slots.get(4).getStack().getName().getString().equals("Mechanical Armory Info");
    }

    @Unique
    private DictionaryBuild getBuildFromMechanicalArmory(MinecraftClient client, DefaultedList<Slot> slots, DictionaryController controller) {
        List<String> slotItemNames = slots.stream().map(slot -> slot.getStack().getName().getString()).toList();
        List<DictionaryItem> itemsFromBuild = new ArrayList<>();
        List<Integer> itemsInOrderOfBuild = Arrays.asList(18, 15, 11, 12, 13, 14);
        List<DictionaryCharm> charms = new ArrayList<>();
        String className = "";
        String specializationName = "";

        String name = slotItemNames.get(4);

        for (int slot : itemsInOrderOfBuild) {
            String itemName = slotItemNames.get(slot);
            boolean isExalted =
                    slots.get(slot).getStack().getTooltip(client.player, TooltipContext.BASIC).stream()
                            .anyMatch(text -> text.getString().contains("Exalted version"));
            itemsFromBuild.add(controller.getItemByName(itemName, isExalted));
        }

        if (!slotItemNames.get(36).equals("Charms Excluded")) {
            List<Integer> charmSlots = Arrays.asList(38, 39, 40, 41, 42, 43, 44);
            for (int slot : charmSlots) {
                String charmName = slotItemNames.get(slot);
                if (charmName.isEmpty()) break;
                charms.add(controller.getCharmByName(slotItemNames.get(slot)));
            }
        }

        if(!slotItemNames.get(45).equals("Class Excluded")) {
            String classInfo = slotItemNames.get(47);
            className = classInfo.substring(0, classInfo.indexOf(" "));
            specializationName = classInfo.substring(classInfo.indexOf("(")+1, classInfo.indexOf(")"));
        }

        int id = controller.generateNewId();
        return new DictionaryBuild(name, itemsFromBuild, charms, null, "", className, specializationName, false, id);
    }

    @Unique
    private static DictionaryBuild getBuildFromPlayerStats(MinecraftClient client, DefaultedList<Slot> slots, DictionaryController controller) {
        List<DictionaryItem> itemsFromBuild;
        String region = slots.get(4).getStack().getName().getString();
        if (region.contains("Ring")) region = "Ring";
        else if (region.contains("Isles")) region = "Isles";
        else if (region.contains("Valley")) region = "Valley";

        List<Integer> slotsInOrderOfBuildRightSide = Arrays.asList(52, 25, 26, 35, 44, 53);
        List<Integer> slotsInOrderOfBuildLeftSide = Arrays.asList(46, 19, 18, 27, 36, 45);
        itemsFromBuild = getItemsFromOneSide(client, slots, controller, slotsInOrderOfBuildRightSide);
        if (itemsFromBuild.stream().allMatch(Objects::isNull)) itemsFromBuild = getItemsFromOneSide(client, slots, controller, slotsInOrderOfBuildLeftSide);

        int id = controller.generateNewId();
        return new DictionaryBuild("", itemsFromBuild, List.of(), null, region, "", "", false, id);
    }

    @Unique
    private static List<DictionaryItem> getItemsFromOneSide(MinecraftClient client, DefaultedList<Slot> slots, DictionaryController controller, List<Integer> slotsInOrderOfBuild) {
        List<DictionaryItem> itemsFromBuild = new ArrayList<>();

        for (int slot : slotsInOrderOfBuild) {
            String itemName = slots.get(slot).getStack().getName().getString();
            boolean isExalted =
                    slots.get(slot).getStack().getTooltip(client.player, TooltipContext.BASIC).stream()
                            .anyMatch(text -> text.getString().contains("Ring"));
            itemsFromBuild.add(controller.getItemByName(itemName, isExalted));
        }

        return itemsFromBuild;
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        buildFromInventoryButton = null;
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void onResize(MinecraftClient client, int width, int height, CallbackInfo ci) {
        if (buildFromInventoryButton != null && client.currentScreen != null) {
            int x = ((HandledScreenAccessor) client.currentScreen).getX() + ((HandledScreenAccessor) client.currentScreen).getBackGroundWidth() - 20;
            int y = ((HandledScreenAccessor) client.currentScreen).getY() - 20;
            buildFromInventoryButton.setX(x);
            buildFromInventoryButton.setY(y);
            addSelectableChild(buildFromInventoryButton);
            addDrawableChild(buildFromInventoryButton);
        }
    }
}
