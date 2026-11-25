package dev.eliux.monumentaitemdictionary.mixin;

import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import dev.eliux.monumentaitemdictionary.gui.builder.DictionaryBuild;
import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemIconButtonWidget;
import dev.eliux.monumentaitemdictionary.util.PlayerClass;
import dev.eliux.monumentaitemdictionary.util.ItemRegion;
import dev.eliux.monumentaitemdictionary.util.Specialization;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @SuppressWarnings("UnusedReturnValue")
    @Shadow protected abstract <T extends Element & Selectable> T addSelectableChild(T child);

    @SuppressWarnings("UnusedReturnValue")
    @Shadow protected abstract <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement);

    @Shadow public abstract void close();

    @Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("TAIL"))
    private void mid$init(MinecraftClient client, int width, int height, CallbackInfo ci) {
        if (client.currentScreen == null) return;
        String inventoryTitle = client.currentScreen.getTitle().getString();
        if (client.currentScreen instanceof GenericContainerScreen && (inventoryTitle.equals("Player Stats Calculator") || inventoryTitle.equals("Mechanical Armory"))) {
            int x = ((HandledScreenAccessor) client.currentScreen).mid$getX() + ((HandledScreenAccessor) client.currentScreen).mid$getBackGroundWidth() - 20;
            int y = ((HandledScreenAccessor) client.currentScreen).mid$getY() - 20;

            buildFromInventoryButton = new ItemIconButtonWidget(x, y, button -> {
                DefaultedList<Slot> slots = ((HandledScreenAccessor) client.currentScreen).mid$getHandler().slots;
                DictionaryController controller = Mid.controller;

                DictionaryBuild buildFromInventory =
                        (inventoryTitle.equals("Mechanical Armory")) ? getBuildFromMechanicalArmory(client,
                                slots, controller) : getBuildFromPlayerStats(client, slots, controller);
                close();
                controller.open(controller.builderGui);
                controller.builderGui.loadItems(buildFromInventory);
            }, Text.literal("Add build from current inventory"), Items.NAME_TAG, "");

            addSelectableChild(buildFromInventoryButton);
            addDrawableChild(buildFromInventoryButton);
        }
    }

    @Unique private ButtonWidget buildFromInventoryButton = null;
    @Inject(method = "render", at = @At("HEAD"))
    private void mid$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen currentScreen = client.currentScreen;
        if (currentScreen instanceof GenericContainerScreen && buildFromInventoryButton != null) {
            buildFromInventoryButton.active = !isOnArmoryMainscreen(
                    currentScreen.getTitle().getString(),
                    ((HandledScreenAccessor) currentScreen).mid$getHandler().slots);
            buildFromInventoryButton.render(context, mouseX, mouseY, delta);
        }
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void mid$resize(MinecraftClient client, int width, int height, CallbackInfo ci) {
        if (buildFromInventoryButton != null && client.currentScreen != null) {
            int x = ((HandledScreenAccessor) client.currentScreen).mid$getX() + ((HandledScreenAccessor) client.currentScreen).mid$getBackGroundWidth() - 20;
            int y = ((HandledScreenAccessor) client.currentScreen).mid$getY() - 20;
            buildFromInventoryButton.setX(x);
            buildFromInventoryButton.setY(y);
            addSelectableChild(buildFromInventoryButton);
            addDrawableChild(buildFromInventoryButton);
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void mid$close(CallbackInfo ci) {
        buildFromInventoryButton = null;
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
        PlayerClass buildClass = PlayerClass.NO_CLASS;
        Specialization specializationName = Specialization.NO_SPECIALIZATION;

        String name = slotItemNames.get(4);

        for (int slot : itemsInOrderOfBuild) {
            String itemName = slotItemNames.get(slot);
            boolean isExalted =
                    slots.get(slot).getStack().getTooltip(Item.TooltipContext.DEFAULT, client.player, TooltipType.BASIC).stream()
                            .anyMatch(text -> text.getString().contains("Exalted version"));
            itemsFromBuild.add(controller.getItemByName(itemName, isExalted));
        }

        if (!slotItemNames.get(36).equals("Charms Excluded")) {
            List<Integer> charmSlots = Arrays.asList(38, 39, 40, 41, 42, 43, 44);
            for (int slot : charmSlots) {
                String charmName = slotItemNames.get(slot);
                if (charmName.isEmpty()) break;
                DictionaryCharm charm = controller.getCharmByName(slotItemNames.get(slot));
                if (charm != null) charms.add(charm);
            }
        }

        if (!slotItemNames.get(45).equals("Class Excluded")) {
            String classInfo = slotItemNames.get(47);
            if (classInfo != null && !classInfo.isEmpty()) {
                buildClass = PlayerClass.of(classInfo.substring(0, classInfo.indexOf(" ")));
                specializationName = Specialization.of(classInfo.substring(classInfo.indexOf("(") + 1, classInfo.indexOf(")")));
            }
        }

        int id = controller.generateNewId();
        return new DictionaryBuild(name, itemsFromBuild, charms, null, ItemRegion.DEFAULT, buildClass, specializationName, false, id);
    }

    @Unique
    private static DictionaryBuild getBuildFromPlayerStats(MinecraftClient client, DefaultedList<Slot> slots, DictionaryController controller) {
        List<DictionaryItem> itemsFromBuild;
        String regionName = slots.get(4).getStack().getName().getString();
        ItemRegion region = ItemRegion.DEFAULT;
        if (regionName.contains("Ring")) region = ItemRegion.RING;
        else if (regionName.contains("Isles")) region = ItemRegion.ISLES;
        else if (regionName.contains("Valley")) region = ItemRegion.VALLEY;

        List<Integer> slotsInOrderOfBuildRightSide = Arrays.asList(52, 25, 26, 35, 44, 53);
        List<Integer> slotsInOrderOfBuildLeftSide = Arrays.asList(46, 19, 18, 27, 36, 45);
        itemsFromBuild = getItemsFromOneSide(client, slots, controller, slotsInOrderOfBuildLeftSide);
        if (itemsFromBuild.stream().allMatch(Objects::isNull)) itemsFromBuild = getItemsFromOneSide(client, slots, controller, slotsInOrderOfBuildRightSide);

        int id = controller.generateNewId();
        return new DictionaryBuild("", itemsFromBuild, List.of(), null, region, PlayerClass.NO_CLASS, Specialization.NO_SPECIALIZATION, false, id);
    }

    @Unique
    private static List<DictionaryItem> getItemsFromOneSide(MinecraftClient client, DefaultedList<Slot> slots, DictionaryController controller, List<Integer> slotsInOrderOfBuild) {
        List<DictionaryItem> itemsFromBuild = new ArrayList<>();

        for (int slot : slotsInOrderOfBuild) {
            String itemName = slots.get(slot).getStack().getName().getString();
            boolean isExalted =
                    slots.get(slot).getStack().getTooltip(Item.TooltipContext.DEFAULT, client.player, TooltipType.BASIC).stream()
                            .anyMatch(text -> text.getString().contains("Ring"));
            itemsFromBuild.add(controller.getItemByName(itemName, isExalted));
        }

        return itemsFromBuild;
    }

    @Inject(method = "handleTextClick", at = @At("HEAD"))
    private void mid$handleTextClick(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (!Screen.hasShiftDown() || style == null) return;

        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent == null || hoverEvent.getAction() != HoverEvent.Action.SHOW_TEXT) return;

        Text text = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
        if (text == null) return;

        String[] lines = text.getString().split("\n");
        if (!lines[0].endsWith("'s Equipment")) return;

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

        int id = Mid.controller.generateNewId();
        DictionaryBuild build = new DictionaryBuild(lines[0], items, List.of(), items.getFirst(), region,
                PlayerClass.NO_CLASS,Specialization.NO_SPECIALIZATION, false, id);

        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        close();
        Mid.controller.open(Mid.controller.builderGui);
        Mid.controller.builderGui.loadItems(build);
    }
}
