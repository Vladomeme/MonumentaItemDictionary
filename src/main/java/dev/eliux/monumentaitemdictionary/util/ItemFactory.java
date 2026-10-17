package dev.eliux.monumentaitemdictionary.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ItemFactory {
    private static final ItemStack ERROR_ITEM = fromId("minecraft:red_concrete");

    public static ItemStack fromId(String baseItem) {
        Item item = Registries.ITEM.get(new Identifier(baseItem.toLowerCase().replace(" ", "_")));
        return item != Items.AIR ? new ItemStack(item, 1) : ERROR_ITEM;
    }

    public static ItemStack fromItem(DictionaryItem item) {
        ItemStack stack = fromId(item.baseItem);
        if (!stack.isOf(Items.PLAYER_HEAD)) return stack;

        for (String nbt : item.nbt) {
            if (nbt != null && !nbt.isEmpty()) {
                NbtCompound stackNbt = new NbtCompound();
                try {
                    stackNbt.put("SkullOwner", StringNbtReader.parse(nbt).getCompound("SkullOwner").copy());
                }
                catch (CommandSyntaxException ignored) {
                    return stack;
                }
                stack.setNbt(stackNbt);
                break;
            }
        }
        return stack;
    }

    public static ItemStack fromEncodingWithNbt(String encoding, NbtCompound nbt) {
        try {
            Item item = Registries.ITEM.get(new Identifier(encoding));
            ItemStack stack = new ItemStack(item, 1);
            stack.getOrCreateNbt();
            stack.setNbt(nbt);

            return stack;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ERROR_ITEM;
    }

    public static ItemStack fromIdWithStringNbt(String baseItem, String nbt) {
        NbtCompound compound;
        try {
            compound = StringNbtReader.parse(nbt);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();

            return ERROR_ITEM;
        }

        return fromEncodingWithNbt(baseItem, compound);
    }

    public static void giveItemToClientPlayer(ItemStack item, int count) {
        if (MinecraftClient.getInstance().player != null) {
            ItemStack finalItem = item.copy();
            finalItem.setCount(count);
            MinecraftClient.getInstance().player.getInventory().insertStack(finalItem);
        }
    }
}
