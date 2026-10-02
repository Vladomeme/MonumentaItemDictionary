package dev.eliux.monumentaitemdictionary.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                    NbtCompound headNbt = new NbtCompound();
                    headNbt.put("SkullOwner", source.get("SkullOwner").copy());
                    ItemStack converted = fromEncodingWithStringNbt(encoding.replace("minecraft:", ""), headNbt.toString());
                    ProfileComponent profile = converted.get(DataComponentTypes.PROFILE);
                    if (profile != null) stack.set(DataComponentTypes.PROFILE, profile);
                }
            } catch (CommandSyntaxException ignored) {
                // Missing or invalid profile data leaves the default head icon.
            }
        }
        return stack;
    }

    public static ItemStack fromEncoding(String encoding) {
        try {
            Item item = Registries.ITEM.get(Identifier.of(encoding));

            return new ItemStack(item, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ERROR_ITEM;
    }

    public static ItemStack fromEncodingWithStringNbt(String encoding, String nbt) {
        ClientPlayNetworkHandler nh = MinecraftClient.getInstance().getNetworkHandler();
        if (nh == null) return ERROR_ITEM;

        Logger logger = LoggerFactory.getLogger("MID");

        nbt = nbt.replaceAll("minecraft:sweeping", "minecraft:sweeping_edge");

        try {
            NbtCompound itemNbt = new NbtCompound();
            itemNbt.put("tag", StringNbtReader.parse(nbt));

            ItemStackComponentizationFix.StackData stackData = new ItemStackComponentizationFix.StackData(
                    "minecraft:" + encoding, 1, new Dynamic<>(nh.getRegistryManager().getOps(NbtOps.INSTANCE), itemNbt));
            ItemStackComponentizationFix.fixStack(stackData, stackData.nbt);

            DataResult<? extends Pair<ItemStack, ?>> dataResult = ItemStack.CODEC.decode(stackData.finalize());
            dataResult.ifError(error -> logger.error("[MID] {}", error.message()));

            return dataResult.result().isPresent() ? dataResult.result().get().getFirst() : ERROR_ITEM;
        }
        catch (Exception e) {
            logger.error(e.getMessage());
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
