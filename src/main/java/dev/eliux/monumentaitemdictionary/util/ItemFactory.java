package dev.eliux.monumentaitemdictionary.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ItemFactory {
    private static final ItemStack ERROR_ITEM = fromEncoding("minecraft:red_concrete");

    public static ItemStack fromItemIcon(DictionaryItem item) {
        String nbt = "";
        for (String tierNbt : item.nbt) {
            if (tierNbt != null && !tierNbt.isBlank()) {
                nbt = tierNbt;
                break;
            }
        }
        return fromIcon(item.baseItem, nbt);
    }

    public static ItemStack fromIcon(String baseItem, String nbt) {
        String encoding = baseItem.split("/")[0].trim().toLowerCase().replace(" ", "_");
        ItemStack stack = fromEncoding(encoding);
        if ((encoding.equals("player_head") || encoding.equals("minecraft:player_head")) && nbt != null && !nbt.isBlank()) {
            try {
                NbtCompound source = StringNbtReader.parse(nbt);
                if (source.contains("tag", 10)) source = source.getCompound("tag");
                if (source.contains("SkullOwner")) {
                    stack.getOrCreateNbt().put("SkullOwner", source.get("SkullOwner").copy());
                }
            } catch (CommandSyntaxException ignored) {
                // Missing or invalid profile data leaves the default head icon.
            }
        }
        return stack;
    }

    public static ItemStack fromEncoding(String encoding) {
        try {
            Item item = Registries.ITEM.get(new Identifier(encoding));

            return new ItemStack(item, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ERROR_ITEM;
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

    public static ItemStack fromEncodingWithStringNbt(String encoding, String nbt) {
        NbtCompound compound;
        try {
            compound = StringNbtReader.parse(nbt);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();

            return ERROR_ITEM;
        }

        return fromEncodingWithNbt(encoding, compound);
    }

    public static void giveItemToClientPlayer(ItemStack item, int count) {
        if (MinecraftClient.getInstance().player != null) {
            ItemStack finalItem = item.copy();
            finalItem.setCount(count);
            MinecraftClient.getInstance().player.getInventory().insertStack(finalItem);
        }
    }
}
