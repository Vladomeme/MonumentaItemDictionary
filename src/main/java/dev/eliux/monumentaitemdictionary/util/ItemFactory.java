package dev.eliux.monumentaitemdictionary.util;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.datafixer.fix.BannerPatternFormatFix;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.potion.Potion;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.*;

public class ItemFactory {

    private static final ItemStack ERROR_ITEM = fromId("minecraft:red_concrete");
    private static final Item[] LEATHER_ARMOR = new Item[] {Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS};
    public static final Item[] POTIONS = new Item[] {Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW};

    public static ItemStack fromId(String baseItem) {
        Item item = Registries.ITEM.get(Identifier.of(baseItem.toLowerCase().replace(" ", "_")));
        return item != Items.AIR ? new ItemStack(item, 1) : ERROR_ITEM;
    }

    public static ItemStack fromItem(DictionaryItem item) {
        ItemStack stack = fromId(item.baseItem());

        Item baseItem = stack.getItem();

        if (baseItem == Items.PLAYER_HEAD)           applyProfileComponent(item, stack);
        if (arrayContains(LEATHER_ARMOR, baseItem))  applyColorComponent(item, stack);
        if (arrayContains(POTIONS, baseItem))        applyPotionContentsComponent(item, stack);
        if (baseItem == Items.SHIELD)                applyPatternComponents(item, stack);
        if (stack.isIn(ItemTags.BANNERS))            applyPatternComponents(item, stack);

        return stack;
    }

    public static NbtCompound getItemNbt(DictionaryItem item) {
        for (String nbt : item.nbt()) {
            if (nbt != null && !nbt.isEmpty()) {
                try {
                    return StringNbtReader.parse(nbt);
                }
                catch (CommandSyntaxException ignored) {}
            }
        }
        return null;
    }

    public static boolean arrayContains(Item[] array, Item item) {
        for (Item i : array) {
            if (i == item) return true;
        }
        return false;
    }

    private static void applyProfileComponent(DictionaryItem item, ItemStack stack) {
        NbtCompound nbt = getItemNbt(item);
        if (nbt == null) return;
        nbt = nbt.getCompound("SkullOwner");

        Optional<String> name = nbt.contains("Name") ? Optional.of(nbt.getString("Name")) : Optional.empty();
        Optional<UUID> id = nbt.containsUuid("Id") ? Optional.of(nbt.getUuid("Id")) : Optional.empty();
        PropertyMap properties = new PropertyMap();
        properties.put("textures", new Property("textures", nbt.getCompound("Properties")
                .getList("textures", 10).getCompound(0).getString("Value")));

        stack.set(DataComponentTypes.PROFILE, new ProfileComponent(name, id, properties));
    }

    private static void applyColorComponent(DictionaryItem item, ItemStack stack) {
        NbtCompound nbt = getItemNbt(item);
        if (nbt != null) {
            nbt = nbt.getCompound("display");
            if (nbt.contains("color"))
                stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(nbt.getInt("color"), false));
        }
    }

    private static void applyPotionContentsComponent(DictionaryItem item, ItemStack stack) {
        NbtCompound nbt = getItemNbt(item);
        if (nbt != null) {
            Optional<RegistryEntry<Potion>> potion;
            if (nbt.contains("Potion"))
                potion = Optional.ofNullable(Registries.POTION.getEntry(Identifier.of(nbt.getString("Potion"))).orElse(null));
            else
                potion = Optional.empty();

            Optional<Integer> color = nbt.contains("CustomPotionColor") ? Optional.of(nbt.getInt("CustomPotionColor")) : Optional.empty();

            stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(potion, color, List.of()));
        }
    }

    private static void applyPatternComponents(DictionaryItem item, ItemStack stack) {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return;

        NbtCompound nbt = getItemNbt(item);
        if (nbt == null || !nbt.contains("BlockEntityTag")) return;
        nbt = nbt.getCompound("BlockEntityTag");

        if (nbt.contains("Base"))
            stack.set(DataComponentTypes.BASE_COLOR, DyeColor.byId(nbt.getInt("Base")));

        if (nbt.contains("Patterns")) {
            Optional<RegistryWrapper.Impl<BannerPattern>> registryLookup = world.getRegistryManager().getOptionalWrapper(RegistryKeys.BANNER_PATTERN);

            if (registryLookup.isPresent()) {
                List<BannerPatternsComponent.Layer> layers = new ArrayList<>();

                for (NbtElement nbtElement : nbt.getList("Patterns", 10)) {
                    NbtCompound layer = (NbtCompound) nbtElement;
                    RegistryKey<BannerPattern> patternKey = RegistryKey.of(RegistryKeys.BANNER_PATTERN,
                            Identifier.of(BannerPatternFormatFix.OLD_TO_NEW_PATTERNS.get(layer.getString("Pattern"))));

                    Optional<RegistryEntry.Reference<BannerPattern>> layerPattern = registryLookup.get().getOptional(patternKey);

                    if (layerPattern.isPresent()) {
                        DyeColor layerColor = DyeColor.byId(layer.getInt("Color"));
                        layers.add(new BannerPatternsComponent.Layer(layerPattern.get(), layerColor));
                    }
                }
                stack.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(layers));
            }
        }
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
