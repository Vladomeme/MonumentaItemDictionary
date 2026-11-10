package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.Mid;

import java.util.ArrayList;
import java.util.List;

public enum ItemType {

    //               Name             Mainhand Offhand  Use line
    HELMET          ("Helmet",          false,  false,  "When on Head:"),
    CHESTPLATE      ("Chestplate",      false,  false,  "When on Chest:"),
    LEGGINGS        ("Leggings",        false,  false,  "When on Legs:"),
    BOOTS           ("Boots",           false,  false,  "When on Feet:"),

    MAINHAND        ("Mainhand",        true,   false,  "When in Main Hand:"),
    MAINHAND_SWORD  ("Mainhand Sword",  true,   false,  "When in Main Hand:"),
    MAINHAND_SHIELD ("Mainhand Shield", true,   false,  "When in Main Hand:"),

    OFFHAND         ("Offhand",         false,  true,   "When in Offhand:"),
    OFFHAND_SWORD   ("Offhand Sword",   false,  true,   "When in Offhand:"),
    OFFHAND_SHIELD  ("Offhand Shield",  false,  true,   "When in Offhand:"),

    WAND            ("Wand",            true,   false,  "When in Main Hand:"),
    AXE             ("Axe",             true,   false,  "When in Main Hand:"),
    SCYTHE          ("Scythe",          true,   false,  "When in Main Hand:"),

    PICKAXE         ("Pickaxe",         true,   false,  "When in Main Hand:"),
    SHOVEL          ("Shovel",          true,   false,  "When in Main Hand:"),

    BOW             ("Bow",             true,   false,  "When in Main Hand:"),
    CROSSBOW        ("Crossbow",        true,   false,  "When in Main Hand:"),
    TRIDENT         ("Trident",         true,   false,  "When in Main Hand:"),
    SNOWBALL        ("Snowball",        true,   false,  "When in Main Hand:"),

    CHARM           ("Charm",           false,  false,  "When in Charm Slot:"),
    PROJECTILE      ("Projectile",      false,  false,  "When Shot:"),
    CONSUMABLE      ("Consumable",      false,  false,  "When Consumed:"),
    WRITTEN_BOOK    ("Written Book",    false,  false,  "When Used:"),
    MISC            ("Misc",            false,  false,  "When Used:");

    public final String name;
    public final boolean isMainhand;
    public final boolean isOffhand;
    public final String useLine;

    public static final List<ItemType> equipmentList = List.of(ItemType.MAINHAND, ItemType.OFFHAND,
            ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS);

    ItemType(String name, boolean isMainhand, boolean isOffhand, String useLine) {
        this.name = name;
        this.isMainhand = isMainhand;
        this.isOffhand = isOffhand;
        this.useLine = useLine;
    }

    public static ItemType of(String type) {
        return switch (type) {
            case "Helmet" -> HELMET;
            case "Chestplate" -> CHESTPLATE;
            case "Leggings" -> LEGGINGS;
            case "Boots" -> BOOTS;

            case "Mainhand" -> MAINHAND;
            case "Mainhand Sword" -> MAINHAND_SWORD;
            case "Mainhand Shield" -> MAINHAND_SHIELD;

            case "Offhand" -> OFFHAND;
            case "Offhand Sword" -> OFFHAND_SWORD;
            case "Offhand Shield" -> OFFHAND_SHIELD;

            case "Wand" -> WAND;
            case "Axe" -> AXE;
            case "Scythe" -> SCYTHE;

            case "Pickaxe" -> PICKAXE;
            case "Shovel" -> SHOVEL;

            case "Bow" -> BOW;
            case "Crossbow" -> CROSSBOW;
            case "Trident" -> TRIDENT;
            case "Snowball" -> SNOWBALL;
            case "Projectile" -> PROJECTILE;

            case "Charm" -> CHARM;
            case "Consumable" -> CONSUMABLE;
            case "Written Book" -> WRITTEN_BOOK;
            case "Misc" -> MISC;
            default -> {
                Mid.LOGGER.info("Unknown item type: {}", type);
                yield MISC;
            }
        };
    }

    public static List<String> mainhandTypes() {
        List<String> types = new ArrayList<>();
        for (ItemType type : values()) {
            if (type.isMainhand) types.add(type.name);
        }
        return types;
    }

    public static List<String> offhandTypes() {
        List<String> types = new ArrayList<>();
        for (ItemType type : values()) {
            if (type.isOffhand) types.add(type.name);
        }
        return types;
    }
}
