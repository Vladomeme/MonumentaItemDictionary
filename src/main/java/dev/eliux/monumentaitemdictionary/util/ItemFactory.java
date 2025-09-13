package dev.eliux.monumentaitemdictionary.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
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
