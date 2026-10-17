package dev.eliux.monumentaitemdictionary.util;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.UUID;

public class ItemFactory {
    private static final ItemStack ERROR_ITEM = fromId("minecraft:red_concrete");

    public static ItemStack fromId(String baseItem) {
        Item item = Registries.ITEM.get(Identifier.of(baseItem.toLowerCase().replace(" ", "_")));
        return item != Items.AIR ? new ItemStack(item, 1) : ERROR_ITEM;
    }

    public static ItemStack fromItem(DictionaryItem item) {
        ItemStack stack = fromId(item.baseItem());
        if (!stack.isOf(Items.PLAYER_HEAD)) return stack;

        for (String nbt : item.nbt()) {
            if (nbt != null && !nbt.isEmpty()) {
                ProfileComponent component = createProfileComponent(nbt);
                if (component != null) stack.set(DataComponentTypes.PROFILE, component);
            }
        }
        return stack;
    }

    public static ProfileComponent createProfileComponent(String stringNbt) {
        NbtCompound nbt;
        try {
            nbt = StringNbtReader.parse(stringNbt).getCompound("SkullOwner");
        }
        catch (CommandSyntaxException ignored) {
            return null;
        }

        Optional<String> name = nbt.contains("Name") ? Optional.of(nbt.getString("Name")) : Optional.empty();
        Optional<UUID> id = nbt.containsUuid("Id") ? Optional.of(nbt.getUuid("Id")) : Optional.empty();
        PropertyMap properties = new PropertyMap();
        properties.put("textures", new Property("textures", nbt.getCompound("Properties")
                .getList("textures", 10).getCompound(0).getString("Value")));

        return new ProfileComponent(name, id, properties);
    }

    public static ItemStack fromIdWithStringNbt(String baseItem, String nbt) {
        ClientPlayNetworkHandler nh = MinecraftClient.getInstance().getNetworkHandler();
        if (nh == null) return ERROR_ITEM;

        nbt = nbt.replaceAll("minecraft:sweeping", "minecraft:sweeping_edge");

        try {
            NbtCompound itemNbt = new NbtCompound();
            itemNbt.put("tag", StringNbtReader.parse(nbt));

            ItemStackComponentizationFix.StackData stackData = new ItemStackComponentizationFix.StackData(
                    "minecraft:" + baseItem, 1, new Dynamic<>(nh.getRegistryManager().getOps(NbtOps.INSTANCE), itemNbt));
            ItemStackComponentizationFix.fixStack(stackData, stackData.nbt);

            DataResult<? extends Pair<ItemStack, ?>> dataResult = ItemStack.CODEC.decode(stackData.finalize());
            dataResult.ifError(error -> Mid.LOGGER.error("[MID] {}", error.message()));

            return dataResult.result().isPresent() ? dataResult.result().get().getFirst() : ERROR_ITEM;
        }
        catch (Exception e) {
            Mid.LOGGER.error(e.getMessage());
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
