package dev.eliux.monumentaitemdictionary.gui;

import com.google.gson.*;
import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.charm.CharmDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.charm.CharmFilterGui;
import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
import dev.eliux.monumentaitemdictionary.gui.generator.GeneratorGui;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.gui.item.ItemDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.item.ItemFilterGui;
import dev.eliux.monumentaitemdictionary.util.CharmStat;
import dev.eliux.monumentaitemdictionary.util.Filter;
import dev.eliux.monumentaitemdictionary.util.ItemFormatter;
import dev.eliux.monumentaitemdictionary.util.ItemStat;
import dev.eliux.monumentaitemdictionary.web.WebManager;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DictionaryController {
    private String itemNameFilter;
    private boolean hasItemNameFilter = false;
    private String charmNameFilter;
    private boolean hasCharmNameFilter = false;
    private ArrayList<Filter> itemFilters = new ArrayList<>();
    private ArrayList<Filter> charmFilters = new ArrayList<>();

    private ArrayList<String> allItemTypes;
    private ArrayList<String> allItemRegions;
    private ArrayList<String> allItemTiers;
    private ArrayList<String> allItemLocations;
    private ArrayList<String> allItemStats;
    private ArrayList<String> allItemBaseItems;

    private ArrayList<String> allCharmRegions;
    private ArrayList<String> allCharmTiers;
    private ArrayList<String> allCharmLocations;
    private ArrayList<String> allCharmSkillMods;
    private ArrayList<String> allCharmClasses;
    private ArrayList<String> allCharmStats;
    private ArrayList<String> allCharmBaseItems;

    private final ArrayList<DictionaryItem> items;
    private ArrayList<DictionaryItem> validItems;
    private final ArrayList<DictionaryCharm> charms;
    private ArrayList<DictionaryCharm> validCharms;

    // private @Nullable CompletableFuture<ItemApiResponse> itemResponseFuture = null;

    public boolean itemLoadFailed = false;
    public boolean charmLoadFailed = false;

    public boolean isRequesting = false;

    public ItemDictionaryGui itemGui;
    public boolean itemGuiPreviouslyOpened = false;
    public ItemFilterGui itemFilterGui;
    public boolean itemFilterGuiPreviouslyOpened = false;
    public CharmDictionaryGui charmGui;
    public boolean charmGuiPreviouslyOpened = false;
    public CharmFilterGui charmFilterGui;
    public boolean charmFilterGuiPreviouslyOpened = false;

    public Screen lastOpenedScreen = null;

    public GeneratorGui generatorGui;

    public DictionaryController() {
        items = new ArrayList<>();
        validItems = new ArrayList<>();
        charms = new ArrayList<>();
        validCharms = new ArrayList<>();

        loadItems();
        loadCharms();

        itemGui = new ItemDictionaryGui(Text.literal("Monumenta Item Dictionary"), this);
        itemFilterGui = new ItemFilterGui(Text.literal("Item Filter Menu"), this);
        charmGui = new CharmDictionaryGui(Text.literal("Monumenta Charm Dictionary"), this);
        charmFilterGui = new CharmFilterGui(Text.literal("Charm Filter Menu"), this);

        generatorGui = new GeneratorGui(Text.literal("Item Generator Options"), this);
    }

    public void tick() {
        /*if (itemResponseFuture != null) {
            try {
                // Process remaining item API response code on main thread
                ItemApiResponse response = itemResponseFuture.join();
                itemResponseFuture = null;

                loadItems();
                itemGui.buildItemList();
                loadCharms();
                charmGui.buildCharmList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    public void open() {
        if (lastOpenedScreen instanceof ItemFilterGui)
            setItemFilterScreen();
        else if (lastOpenedScreen instanceof CharmDictionaryGui)
            setCharmDictionaryScreen();
        else if (lastOpenedScreen instanceof CharmFilterGui)
            setCharmFilterScreen();
        else setItemDictionaryScreen();
    }

    public void setItemDictionaryScreen() {
        lastOpenedScreen = MinecraftClient.getInstance().currentScreen;

        MinecraftClient.getInstance().setScreen(itemGui);
        if (!itemGuiPreviouslyOpened) {
            itemGui.postInit();
            itemGuiPreviouslyOpened = true;
        } else {
            itemGui.updateGuiPositions();
        }
    }

    public void setItemFilterScreen() {
        lastOpenedScreen = MinecraftClient.getInstance().currentScreen;

        MinecraftClient.getInstance().setScreen(itemFilterGui);
        if (!itemFilterGuiPreviouslyOpened) {
            itemFilterGui.postInit();
            itemFilterGuiPreviouslyOpened = true;
        } else {
            //filterGui.updateGuiPositions();
        }
    }

    public void setCharmDictionaryScreen() {
        lastOpenedScreen = MinecraftClient.getInstance().currentScreen;

        MinecraftClient.getInstance().setScreen(charmGui);
        if (!charmGuiPreviouslyOpened) {
            charmGui.postInit();
            charmGuiPreviouslyOpened = true;
        } else {
            charmGui.updateGuiPositions();
        }
    }

    public void setCharmFilterScreen() {
        lastOpenedScreen = MinecraftClient.getInstance().currentScreen;

        MinecraftClient.getInstance().setScreen(charmFilterGui);
        if (!charmFilterGuiPreviouslyOpened) {
            charmFilterGui.postInit();
            charmFilterGuiPreviouslyOpened = true;
        } else {
            //charmFilterGui.updateGuiPositions();
        }
    }

    public GeneratorGui setGeneratorScreen() {
        lastOpenedScreen = MinecraftClient.getInstance().currentScreen;

        MinecraftClient.getInstance().setScreen(generatorGui);
        generatorGui.postInit();
        return generatorGui;
    }

    private String readItemData() {
        try {
            return Files.readString(Path.of("config/mid/items.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "{}";
    }

    private void writeItemData(String writeData) {
        try {
            File targetFile = new File("config/mid/items.json");

            targetFile.getParentFile().mkdirs();
            targetFile.createNewFile();

            FileUtils.writeStringToFile(targetFile, writeData, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestAndUpdate() {
        if (isRequesting) return;
        isRequesting = true;
        WebManager.manageRequestAsynchronous("https://api.playmonumenta.com/itemswithnbt", (data) -> {
            writeItemData(data);
            loadItems();
            itemGui.buildItemList();
            loadCharms();
            charmGui.buildCharmList();
            isRequesting = false;
        }, () -> {
            isRequesting = false;
        });
    }

    public void loadItems() {
        allItemTypes = new ArrayList<>();
        allItemRegions = new ArrayList<>();
        allItemTiers = new ArrayList<>();
        allItemLocations = new ArrayList<>();
        allItemStats = new ArrayList<>();
        allItemBaseItems = new ArrayList<>();

        try {
            String rawData = readItemData();

            items.clear();
            JsonObject data = new Gson().fromJson(rawData, JsonObject.class);
            for (JsonElement itemElement : data.asMap().values()) {
                JsonObject itemData = (JsonObject) itemElement;

                // Construct item information
                if (itemData.get("type").getAsString().equals("Charm"))
                    continue;

                if (!itemData.has("name")) continue; // if this element is not present, skip this item
                String itemName = itemData.get("name").getAsString();

                if (!itemData.has("type")) continue; // if this element is not present, skip this item
                String itemType = itemData.get("type").getAsString();
                if (itemType.equals("Charm"))
                    continue;
                if (!allItemTypes.contains(itemType))
                    allItemTypes.add(itemType);

                String itemRegion = "";
                JsonPrimitive regionPrimitive = itemData.getAsJsonPrimitive("region");
                if (regionPrimitive != null) {
                    itemRegion = regionPrimitive.getAsString();

                    if (!allItemRegions.contains(itemRegion))
                        allItemRegions.add(itemRegion);
                }

                String itemTier = "";
                JsonPrimitive tierPrimitive = itemData.getAsJsonPrimitive("tier");
                if (tierPrimitive != null) {
                    List<String> plainSplit = Arrays.asList(tierPrimitive.getAsString().replace("_", " ").split(" ")); // janky code to patch Event Currency appearing as Event_currency and other future similar events
                    StringBuilder formattedSplit = new StringBuilder();
                    for (String s : plainSplit) {
                        if (s.length() > 0)
                            formattedSplit.append(s.substring(0, 1).toUpperCase()).append(s.substring(1).toLowerCase());
                        if (plainSplit.indexOf(s) != plainSplit.size() - 1)
                            formattedSplit.append(" ");
                    }
                    itemTier = formattedSplit.toString();

                    if (!allItemTiers.contains(itemTier))
                        allItemTiers.add(itemTier);
                }

                String itemLocation = "";
                JsonPrimitive locationPrimitive = itemData.getAsJsonPrimitive("location");
                if (locationPrimitive != null) {
                    itemLocation = locationPrimitive.getAsString();

                    if (!allItemLocations.contains(itemLocation))
                        allItemLocations.add(itemLocation);
                }

                int fishTier = -1;
                boolean isFish = false;
                JsonPrimitive fishQualityPrimitive = itemData.getAsJsonPrimitive("fish_quality");
                if (fishQualityPrimitive != null) {
                    fishTier = fishQualityPrimitive.getAsInt();
                    isFish = true;
                }

                if (!itemData.has("base_item")) continue; // if this element is not present, skip this item
                String itemBaseItem = itemData.get("base_item").getAsString();
                if (!allItemBaseItems.contains(itemBaseItem))
                    allItemBaseItems.add(itemBaseItem);

                String itemLore = "";
                JsonPrimitive lorePrimitive = itemData.getAsJsonPrimitive("lore");
                if (lorePrimitive != null) {
                    itemLore = lorePrimitive.getAsString();
                }

                ArrayList<ItemStat> itemStats = new ArrayList<>();
                JsonObject statObject = itemData.get("stats").getAsJsonObject();
                for (Map.Entry<String, JsonElement> statEntry : statObject.entrySet()) {
                    String statKey = statEntry.getKey();
                    if (ItemFormatter.isHiddenStat(statKey)) continue;

                    itemStats.add(new ItemStat(statKey, statEntry.getValue().getAsDouble()));

                    if (!allItemStats.contains(statKey))
                        allItemStats.add(statKey);
                }

                String itemNbt = "";
                JsonPrimitive nbtPrimitive = itemData.getAsJsonPrimitive("nbt");
                if (nbtPrimitive != null) {
                    itemNbt = nbtPrimitive.getAsString();
                }

                // Build the item
                JsonPrimitive masterworkPrimitive = itemData.getAsJsonPrimitive("masterwork");
                if (masterworkPrimitive != null) {
                    boolean hasItem = false;
                    for (DictionaryItem dictionaryItem : items) {
                        if (dictionaryItem.name.equals(itemName)) {
                            hasItem = true;
                            dictionaryItem.addMasterworkTier(itemStats, itemNbt, masterworkPrimitive.getAsInt());
                        }
                    }
                    if (!hasItem) {
                        ArrayList<ArrayList<ItemStat>> totalStatsList = new ArrayList<>();
                        ArrayList<String> totalNbtList = new ArrayList<>();
                        for (int i = 0; i < ItemFormatter.getMasterworkForRarity(itemTier) + 1; i++) {
                            totalStatsList.add(null);
                            totalNbtList.add(null);
                        }
                        totalStatsList.set(masterworkPrimitive.getAsInt(), itemStats);
                        totalNbtList.set(itemData.get("masterwork").getAsInt(), itemNbt);
                        items.add(new DictionaryItem(itemName, itemType, itemRegion, itemTier, itemLocation, fishTier, isFish, itemBaseItem, itemLore, totalNbtList, totalStatsList, true));
                    }
                } else {
                    ArrayList<ArrayList<ItemStat>> totalStatsList = new ArrayList<>();
                    totalStatsList.add(itemStats);
                    ArrayList<String> totalNbtList = new ArrayList<>();
                    totalNbtList.add(itemNbt);
                    items.add(new DictionaryItem(itemName, itemType, itemRegion, itemTier, itemLocation, fishTier, isFish, itemBaseItem, itemLore, totalNbtList, totalStatsList, false));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            itemLoadFailed = true;
        }

        items.sort(DictionaryItem::compareTo);
    }

    public void loadCharms() {
        allCharmRegions = new ArrayList<>();
        allCharmTiers = new ArrayList<>();
        allCharmLocations = new ArrayList<>();
        allCharmSkillMods = new ArrayList<>();
        allCharmClasses = new ArrayList<>();
        allCharmStats = new ArrayList<>();
        allCharmBaseItems = new ArrayList<>();

        try {
            String rawData = readItemData();

            charms.clear();
            JsonObject data = new Gson().fromJson(rawData, JsonObject.class);
            for (JsonElement charmElement : data.asMap().values()) {
                JsonObject charmData = (JsonObject) charmElement;

                // Construct charm information
                if (!charmData.get("type").getAsString().equals("Charm"))
                    continue;

                if (!charmData.has("name")) continue; // if this element is not present, skip this item
                String charmName = charmData.get("name").getAsString();

                // only one region for charms, for now
                String charmRegion = "Architect's Ring";
                if (!allCharmRegions.contains(charmRegion))
                    allCharmRegions.add(charmRegion);

                if (!charmData.has("location")) continue; // if this element is not present, skip this item
                String charmLocation = charmData.get("location").getAsString();
                if (!allCharmLocations.contains(charmLocation))
                    allCharmLocations.add(charmLocation);

                if (!charmData.has("tier")) continue; // if this element is not present, skip this item
                String charmTier = charmData.get("tier").getAsString().replace("_", " ");
                if (!allCharmTiers.contains(charmTier))
                    allCharmTiers.add(charmTier);

                if (!charmData.has("power")) continue; // if this element is not present, skip this item
                int charmPower = charmData.get("power").getAsInt();


                if (!charmData.has("class_name")) continue; // if this element is not present, skip this item
                String charmClass = charmData.get("class_name").getAsString();
                if (!allCharmClasses.contains(charmClass))
                    allCharmClasses.add(charmClass);

                if (!charmData.has("base_item")) continue; // if this element is not present, skip this item
                String charmBaseItem = charmData.get("base_item").getAsString();
                if (!allCharmBaseItems.contains(charmBaseItem))
                    allCharmBaseItems.add(charmBaseItem);

                String charmNbt = "";
                if (charmData.has("nbt")) {
                    charmNbt = charmData.get("nbt").getAsString();
                }

                ArrayList<CharmStat> charmStats = new ArrayList<>();
                JsonObject statObject = charmData.get("stats").getAsJsonObject();
                for (Map.Entry<String, JsonElement> statEntry : statObject.entrySet()) {
                    String statKey = statEntry.getKey();
                    String skillMod = ItemFormatter.getSkillFromCharmStat(statKey);
                    if (!allCharmSkillMods.contains(skillMod))
                        allCharmSkillMods.add(skillMod);

                    charmStats.add(new CharmStat(statKey, skillMod, statEntry.getValue().getAsDouble()));

                    if (!allCharmStats.contains(statKey))
                        allCharmStats.add(statKey);
                }

                charms.add(new DictionaryCharm(charmName, charmRegion, charmLocation, charmTier, charmPower, charmClass, charmBaseItem, charmNbt, charmStats));
            }
        } catch (Exception e) {
            e.printStackTrace();
            charmLoadFailed = true;
        }

        charms.sort((o1, o2) -> {
            if (!o1.tier.equals(o2.tier)) {
                return -(ItemFormatter.getNumberForTier(o1.tier) - ItemFormatter.getNumberForTier(o2.tier));
            }
            return 0;
        });
    }

    public ArrayList<String> getAllItemTypes() {
        return allItemTypes;
    }

    public ArrayList<String> getAllItemRegions() {
        return allItemRegions;
    }

    public ArrayList<String> getAllItemTiers() {
        return allItemTiers;
    }

    public ArrayList<String> getAllItemLocations() {
        return allItemLocations;
    }

    public ArrayList<String> getAllItemStats() {
        return allItemStats;
    }

    public ArrayList<String> getAllItemBaseItems() {
        return allItemBaseItems;
    }

    public void setItemNameFilter(String nameFilter) {
        this.itemNameFilter = nameFilter;
        hasItemNameFilter = true;
    }

    public void clearItemNameFilter() {
        hasItemNameFilter = false;
    }

    public void updateItemFilters(ArrayList<Filter> filters) {
        itemFilters = new ArrayList<>(filters);
    }

    public void resetItemFilters() {
        itemFilters = new ArrayList<>();
    }

    public ArrayList<String> getAllCharmRegions() {
        return allCharmRegions;
    }

    public ArrayList<String> getAllCharmTiers() {
        return allCharmTiers;
    }

    public ArrayList<String> getAllCharmLocations() {
        return allCharmLocations;
    }

    public ArrayList<String> getAllCharmSkillMods() {
        return allCharmSkillMods;
    }

    public ArrayList<String> getAllCharmClasses() {
        return allCharmClasses;
    }

    public ArrayList<String> getAllCharmStats() {
        return allCharmStats;
    }

    public ArrayList<String> getAllCharmBaseItems() {
        return allCharmBaseItems;
    }

    public void setCharmNameFilter(String nameFilter) {
        this.charmNameFilter = nameFilter;
        hasCharmNameFilter = true;
    }

    public void clearCharmNameFilter() {
        hasCharmNameFilter = false;
    }

    public void updateCharmFilters(ArrayList<Filter> filters) {
        charmFilters = new ArrayList<>(filters);
    }

    public void resetCharmFilters() {
        charmFilters = new ArrayList<>();
    }

    public void refreshItems() {
        ArrayList<DictionaryItem> filteredItems = new ArrayList<>(items);

        for (Filter filter : itemFilters) {
            if (filter != null) {
                if (filter.getOption().equals("Stat")) {
                    if (!filter.value.equals("")) {
                        switch (filter.comparator) {
                            case 0 -> filteredItems.removeIf(i -> !i.hasStat(filter.value));
                            case 1 -> filteredItems.removeIf(i -> i.hasStat(filter.value));
                            case 2 -> filteredItems.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) >= filter.constant));
                            case 3 -> filteredItems.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) > filter.constant));
                            case 4 -> filteredItems.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) == filter.constant));
                            case 5 -> filteredItems.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) <= filter.constant));
                            case 6 -> filteredItems.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) < filter.constant));
                        }
                    }
                } else if (filter.getOption().equals("Tier")) {
                    if (!filter.value.equals("")) {
                        switch (filter.comparator) {
                            case 0 -> filteredItems.removeIf(i -> !i.hasTier() || !i.tier.equals(filter.value));
                            case 1 -> filteredItems.removeIf(i -> i.hasTier() && i.tier.equals(filter.value));
                        }
                    }
                } else if (filter.getOption().equals("Region")) {
                    if (!filter.value.equals("")) {
                        switch (filter.comparator) {
                            case 0 -> filteredItems.removeIf(i -> !i.hasRegion() || !i.region.equals(filter.value));
                            case 1 -> filteredItems.removeIf(i -> i.hasRegion() && i.region.equals(filter.value));
                        }
                    }
                } else if (filter.getOption().equals("Type")) {
                    if (!filter.value.equals("")) {
                        switch (filter.comparator) {
                            case 0 -> filteredItems.removeIf(i -> !i.type.equals(filter.value));
                            case 1 -> filteredItems.removeIf(i -> i.type.equals(filter.value));
                        }
                    }
                } else if (filter.getOption().equals("Location")) {
                    if (!filter.value.equals("")) {
                        switch (filter.comparator) {
                            case 0 -> filteredItems.removeIf(i -> !i.hasLocation() || !i.location.equals(filter.value));
                            case 1 -> filteredItems.removeIf(i -> i.hasLocation() && i.location.equals(filter.value));
                        }
                    }
                } else if (filter.getOption().equals("Base Item")) {
                    if (!filter.value.equals("")) {
                        switch (filter.comparator) {
                            case 0 -> filteredItems.removeIf(i -> !i.baseItem.equals(filter.value));
                            case 1 -> filteredItems.removeIf(i -> i.baseItem.equals(filter.value));
                        }
                    }
                }
            }
        }

        if (hasItemNameFilter)
            filteredItems.removeIf(i -> !i.name.toLowerCase().contains(itemNameFilter.toLowerCase()));

        filteredItems.sort((o1, o2) -> {
            for (Filter f : itemFilters) {
                if (f.getOption().equals("Stat")) {
                    double val = o2.getStat(f.value) - o1.getStat(f.value);

                    if (val == 0)
                        continue;

                    if (val > 0) return 1;
                    if (val < 0) return -1;
                }
            }
            return 0;
        });

        validItems = filteredItems;
    }

    public void refreshCharms() {
        ArrayList<DictionaryCharm> filteredCharms = new ArrayList<>(charms);

        for (Filter filter : charmFilters) {
            if (filter != null) {
                if (filter.getOption().equals("Stat")) {
                    if (!filter.value.equals("")) {
                        switch (filter.comparator) {
                            case 0 -> filteredCharms.removeIf(i -> !i.hasStat(filter.value));
                            case 1 -> filteredCharms.removeIf(i -> i.hasStat(filter.value));
                            case 2 -> filteredCharms.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) >= filter.constant));
                            case 3 -> filteredCharms.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) > filter.constant));
                            case 4 -> filteredCharms.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) == filter.constant));
                            case 5 -> filteredCharms.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) <= filter.constant));
                            case 6 -> filteredCharms.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) < filter.constant));
                        }
                    }
                } else if (filter.getOption().equals("Tier")) {
                    if (!filter.value.equals("")) {
                        switch (filter.comparator) {
                            case 0 -> filteredCharms.removeIf(i -> !i.tier.equals(filter.value));
                            case 1 -> filteredCharms.removeIf(i -> i.tier.equals(filter.value));
                        }
                    }
                } else if (filter.getOption().equals("Class")) {
                    if (!filter.value.equals("")) {
                        switch (filter.comparator) {
                            case 0 -> filteredCharms.removeIf(i -> !i.className.equals(filter.value));
                            case 1 -> filteredCharms.removeIf(i -> i.className.equals(filter.value));
                        }
                    }
                } else if (filter.getOption().equals("Skill Modifier")) {
                    if (!filter.value.equals("")) {
                        switch (filter.comparator) {
                            case 0 -> filteredCharms.removeIf(i -> !i.hasStatModifier(filter.value));
                            case 1 -> filteredCharms.removeIf(i -> i.hasStatModifier(filter.value));
                        }
                    }
                } else if (filter.getOption().equals("Charm Power")) {
                    switch (filter.comparator) {
                        case 2 -> filteredCharms.removeIf(i -> !(i.power >= filter.constant));
                        case 3 -> filteredCharms.removeIf(i -> !(i.power > filter.constant));
                        case 4 -> filteredCharms.removeIf(i -> !(i.power == filter.constant));
                        case 5 -> filteredCharms.removeIf(i -> !(i.power < filter.constant));
                        case 6 -> filteredCharms.removeIf(i -> !(i.power <= filter.constant));
                    }
                } else if (filter.getOption().equals("Location")) {
                    if (!filter.value.equals("")) {
                        switch (filter.comparator) {
                            case 0 -> filteredCharms.removeIf(i -> !i.location.equals(filter.value));
                            case 1 -> filteredCharms.removeIf(i -> i.location.equals(filter.value));
                        }
                    }
                } else if (filter.getOption().equals("Base Item")) {
                    if (!filter.value.equals("")) {
                        switch (filter.comparator) {
                            case 0 -> filteredCharms.removeIf(i -> !i.baseItem.equals(filter.value));
                            case 1 -> filteredCharms.removeIf(i -> i.baseItem.equals(filter.value));
                        }
                    }
                }
            }
        }

        if (hasCharmNameFilter)
            filteredCharms.removeIf(i -> !i.name.toLowerCase().contains(charmNameFilter.toLowerCase()));

        filteredCharms.sort((o1, o2) -> {
            for (Filter f : charmFilters) {
                if (f.getOption().equals("Stat")) {
                    double val = o2.getStat(f.value) - o1.getStat(f.value);

                    if (val == 0)
                        continue;

                    if (val < 0) return -1;
                    if (val > 0) return 1;
                }
            }
            return 0;
        });

        validCharms = filteredCharms;
    }

    public ArrayList<DictionaryItem> getItems() {
        return validItems;
    }

    public ArrayList<DictionaryCharm> getCharms() {
        return validCharms;
    }

    public boolean anyItems() {
        return items.size() == 0;
    }

    public boolean anyCharms() {
        return charms.size() == 0;
    }
}
