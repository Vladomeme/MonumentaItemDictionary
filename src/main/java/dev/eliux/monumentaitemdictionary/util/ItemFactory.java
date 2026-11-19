package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.tag.ItemTags;

public class ItemFactory {

    private static final ItemStack ERROR_ITEM = fromBase(Items.RED_CONCRETE);
    private static final Item[] LEATHER_ARMOR = new Item[] {Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS};
    public static final Item[] POTIONS = new Item[] {Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW};

    public static ItemStack fromBase(Item item) {
        return item != Items.AIR ? new ItemStack(item, 1) : ERROR_ITEM;
    }

    public static ItemStack fromItem(DictionaryItem item) {
        ItemStack stack = fromBase(item.baseItem());

        if (item.hasNbt()) {
            NbtCompound nbt = item.getNbt();
            if (nbt.isEmpty()) return stack;

            if (item.baseItem() == Items.PLAYER_HEAD) applySkullOwner(stack, nbt);
            if (arrayContains(LEATHER_ARMOR, item.baseItem())) applyColor(stack, nbt);
            if (arrayContains(POTIONS, item.baseItem())) applyPotionData(stack, nbt);
            if (item.baseItem() == Items.SHIELD) applyBlockEntityTag(stack, nbt);
            if (stack.isIn(ItemTags.BANNERS)) applyBlockEntityTag(stack, nbt);
        }
        return stack;
    }

    public static boolean arrayContains(Item[] array, Item item) {
        for (Item i : array) {
            if (i == item) return true;
        }
        return false;
    }

    private static void applySkullOwner(ItemStack stack, NbtCompound source) {
        if (source.contains("SkullOwner"))
            stack.getOrCreateNbt().put("SkullOwner", source.getCompound("SkullOwner").copy());
    }

    private static void applyColor(ItemStack stack, NbtCompound source) {
        if (source.getCompound("display").contains("color"))
            stack.getOrCreateSubNbt("display").putInt("color", source.getCompound("display").getInt("color"));
    }

    private static void applyPotionData(ItemStack stack, NbtCompound source) {
        if (source.contains("Potion"))
            stack.getOrCreateNbt().put("Potion", source.getCompound("Potion").copy());
        if (source.contains("CustomPotionColor"))
            stack.getOrCreateNbt().putInt("CustomPotionColor", source.getInt("CustomPotionColor"));
    }

    private static void applyBlockEntityTag(ItemStack stack, NbtCompound source) {
        if (source.contains("BlockEntityTag"))
            stack.getOrCreateNbt().put("BlockEntityTag", source.getCompound("BlockEntityTag").copy());
    }

    public static ItemStack fromBaseWithNbt(Item baseItem, NbtCompound nbt) {
        try {
            ItemStack stack = new ItemStack(baseItem, 1);
            stack.getOrCreateNbt();
            stack.setNbt(nbt);

            return stack;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return ERROR_ITEM;
    }

    public static void giveItemToClientPlayer(ItemStack item, int count) {
        if (MinecraftClient.getInstance().player != null) {
            ItemStack finalItem = item.copy();
            finalItem.setCount(count);
            MinecraftClient.getInstance().player.getInventory().insertStack(finalItem);
        }
    }
}
