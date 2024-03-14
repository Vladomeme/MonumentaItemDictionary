package dev.eliux.monumentaitemdictionary.gui;

import com.google.gson.*;
import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.builder.BuildDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.builder.BuildFilterGui;
import dev.eliux.monumentaitemdictionary.gui.builder.BuilderGui;
import dev.eliux.monumentaitemdictionary.gui.builder.DictionaryBuild;
import dev.eliux.monumentaitemdictionary.gui.charm.CharmDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.charm.CharmFilterGui;
import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
import dev.eliux.monumentaitemdictionary.gui.generator.GeneratorGui;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.gui.item.ItemDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.item.ItemFilterGui;
import dev.eliux.monumentaitemdictionary.util.*;
import dev.eliux.monumentaitemdictionary.web.WebManager;

import java.nio.charset.StandardCharsets;
import java.util.*;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("CallToPrintStackTrace")
public class DictionaryController {
    private String itemNameFilter;
    private boolean hasItemNameFilter = false;
    private String charmNameFilter;
    private boolean hasCharmNameFilter = false;
    private String buildNameFilter;
    private boolean hasBuildNameFilter = false;
    private ArrayList<Filter> itemFilters = new ArrayList<>();
    private ArrayList<Filter> charmFilters = new ArrayList<>();
    private ArrayList<Filter> buildFilters = new ArrayList<>();

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
    private ArrayList<DictionaryBuild> builds;
    private ArrayList<DictionaryBuild> validBuilds;

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
    public BuildDictionaryGui buildDictionaryGui;
    public boolean buildFilterGuiPreviouslyOpened = false;
    public BuildFilterGui buildFilterGui;
    public boolean buildDictionaryGuiPreviouslyOpened = false;
    public BuilderGui builderGui;
    public boolean builderGuiPreviouslyOpened = false;

    public Screen lastOpenedScreen = null;

    public GeneratorGui generatorGui;

    public DictionaryController() {
        items = new ArrayList<>();
        validItems = new ArrayList<>();
        charms = new ArrayList<>();
        validCharms = new ArrayList<>();
        builds = new ArrayList<>();
        validBuilds = new ArrayList<>();

        loadItems();
        loadCharms();
        loadBuilds();

        itemGui = new ItemDictionaryGui(Text.literal("Monumenta Item Dictionary"), this);
        itemFilterGui = new ItemFilterGui(Text.literal("Item Filter Menu"), this);
        charmGui = new CharmDictionaryGui(Text.literal("Monumenta Charm Dictionary"), this);
        charmFilterGui = new CharmFilterGui(Text.literal("Charm Filter Menu"), this);
        buildDictionaryGui = new BuildDictionaryGui(Text.literal("Build Dictionary Menu"), this);
        builderGui = new BuilderGui(Text.literal("Builder Menu"), this);
        buildFilterGui = new BuildFilterGui(Text.literal("Build Filter Menu"), this);

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
        if (lastOpenedScreen == null || lastOpenedScreen instanceof ItemDictionaryGui) {
            itemGui.isGettingBuildItem = false;
            setItemDictionaryScreen();
        } else if (lastOpenedScreen instanceof CharmDictionaryGui) {
            itemGui.isGettingBuildItem = false;
            setCharmDictionaryScreen();
        } else {
            itemGui.isGettingBuildItem = false;
            setItemDictionaryScreen();
        }
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
        }
    }

    public GeneratorGui setGeneratorScreen() {
        lastOpenedScreen = MinecraftClient.getInstance().currentScreen;

        MinecraftClient.getInstance().setScreen(generatorGui);
        generatorGui.postInit();
        return generatorGui;
    }

    public void setBuildDictionaryScreen() {
        lastOpenedScreen = MinecraftClient.getInstance().currentScreen;

        MinecraftClient.getInstance().setScreen(buildDictionaryGui);
        if (!buildDictionaryGuiPreviouslyOpened) {
            buildDictionaryGui.postInit();
            buildDictionaryGuiPreviouslyOpened = true;
        } else {
            buildDictionaryGui.updateGuiPositions();
        }
    }

    public void setBuildFilterScreen() {
        lastOpenedScreen = MinecraftClient.getInstance().currentScreen;

        MinecraftClient.getInstance().setScreen(buildFilterGui);
        if (!buildFilterGuiPreviouslyOpened) {
            buildFilterGui.postInit();
            buildFilterGuiPreviouslyOpened = true;
        }
    }

    public void setBuilderScreen() {
        lastOpenedScreen = MinecraftClient.getInstance().currentScreen;

        MinecraftClient.getInstance().setScreen(builderGui);
        if (!builderGuiPreviouslyOpened) {
            builderGui.postInit();
            builderGuiPreviouslyOpened = true;
        } else {
            builderGui.updateGuiPositions();
        }
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

    public String readJsonBuild() {
        try {
            File buildsFile = new File("config/mid/builds.json");
            buildsFile.createNewFile();
            return Files.readString(buildsFile.toPath(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void writeJsonBuild(JsonObject jsonBuild, int id) {
        try {
            File file = new File("config/mid/builds.json");

            JsonObject fileBuilds = JsonParser.parseString((readJsonBuild().isEmpty()) ? "{}" : readJsonBuild()).getAsJsonObject();
            fileBuilds.add(String.valueOf(id), jsonBuild);

            FileUtils.writeStringToFile(file, fileBuilds.toString(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestAndUpdate() {
        Mid.LOGGER.info("Started Data Request");

        if (isRequesting) return;
        isRequesting = true;
        WebManager.manageRequestAsynchronous("https://api.playmonumenta.com/itemswithnbt", (data) -> {
            writeItemData(data);
            loadItems();
            itemGui.buildItemList();
            loadCharms();
            charmGui.buildCharmList();
            isRequesting = false;

            Mid.LOGGER.info("Finished Data Request - Success");
        }, () -> {
            isRequesting = false;

            Mid.LOGGER.info("Finished Data Request - Failure");
        });
    }

    public void loadBuilds() {
        try {
            ArrayList<DictionaryBuild> buildsInFile = new ArrayList<>();

            JsonElement jsonBuilds = JsonParser.parseString(readJsonBuild());
            if (jsonBuilds instanceof JsonNull) return;
            JsonObject data = new Gson().fromJson(jsonBuilds, JsonObject.class);
            for (Map.Entry<String, JsonElement> buildElement : data.asMap().entrySet()) {
                int id = Integer.parseInt(buildElement.getKey());
                JsonObject buildData = (JsonObject) buildElement.getValue();

                String buildName = buildData.get("name").getAsString();
                String buildRegion = buildData.get("region").getAsString();
                String buildClass = buildData.get("class").getAsString();
                String buildSpecialization = buildData.get("specialization").getAsString();
                boolean buildFavorite = buildData.get("favorite").getAsBoolean();

                JsonObject itemToShow = buildData.get("item_to_show").getAsJsonObject();
                String itemToShowName = itemToShow.get("name").getAsString();
                boolean itemToShowIsExalted = itemToShow.get("exalted").getAsBoolean();

                DictionaryItem buildItemToShow = getItemByName(itemToShowName, itemToShowIsExalted);

                ArrayList<DictionaryItem> buildItems = new ArrayList<>();
                JsonObject rawItems = buildData.get("items").getAsJsonObject();
                for (String item : rawItems.asMap().keySet()) {
                    JsonObject itemJsonObject = rawItems.get(item).getAsJsonObject();
                    if (!itemJsonObject.has("name")) {
                        buildItems.add(null);
                        continue;
                    }
                    boolean isExalted = itemJsonObject.get("exalted").getAsBoolean();
                    String itemName = itemJsonObject.get("name").getAsString();

                    buildItems.add(getItemByName(itemName, isExalted));
                }

                ArrayList<DictionaryCharm> buildCharms = new ArrayList<>();
                JsonArray rawCharms = buildData.get("charms").getAsJsonArray();
                for (JsonElement charm : rawCharms.asList()) {
                    buildCharms.add(getCharmByName(charm.getAsString()));
                }
                 buildsInFile.add(new DictionaryBuild(buildName, buildItems, buildCharms, buildItemToShow, buildRegion, buildClass, buildSpecialization, buildFavorite, id));
            }
            builds = buildsInFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        if (!s.isEmpty())
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
                    // if the item has masterwork
                    // attempt to add a tier to the item
                    boolean hasItem = false;
                    for (DictionaryItem dictionaryItem : items) {
                        if (dictionaryItem.name.equals(itemName) && masterworkPrimitive.getAsInt() <= ItemFormatter.getMasterworkForRarity(itemTier)) {
                            // if the item already exists
                            hasItem = true;
                            dictionaryItem.addMasterworkTier(itemTier, itemStats, itemNbt, masterworkPrimitive.getAsInt());
                        }
                    }
                    if (!hasItem) {
                        // if the item does not already exist
                        ArrayList<String> totalTierList = new ArrayList<>();
                        ArrayList<ArrayList<ItemStat>> totalStatsList = new ArrayList<>();
                        ArrayList<String> totalNbtList = new ArrayList<>();
                        for (int i = 0; i < ItemFormatter.getMasterworkForRarity(itemTier) + 1; i++) {
                            totalTierList.add(null);
                            totalStatsList.add(null);
                            totalNbtList.add(null);
                        }
                        int level = masterworkPrimitive.getAsInt();
                        if (level <= ItemFormatter.getMasterworkForRarity(itemTier)) { // prevent adding a tier above the believed cap
                            totalTierList.set(level, itemTier);
                            totalStatsList.set(level, itemStats);
                            totalNbtList.set(level, itemNbt);
                            items.add(new DictionaryItem(itemName, itemType, itemRegion, totalTierList, itemLocation, fishTier, isFish, itemBaseItem, itemLore, totalNbtList, totalStatsList, true));
                        }
                    }
                } else {
                    // if the item does not have masterwork
                    ArrayList<String> totalTierList = new ArrayList<>();
                    totalTierList.add(itemTier);
                    ArrayList<ArrayList<ItemStat>> totalStatsList = new ArrayList<>();
                    totalStatsList.add(itemStats);
                    ArrayList<String> totalNbtList = new ArrayList<>();
                    totalNbtList.add(itemNbt);
                    items.add(new DictionaryItem(itemName, itemType, itemRegion, totalTierList, itemLocation, fishTier, isFish, itemBaseItem, itemLore, totalNbtList, totalStatsList, false));
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

    public void setBuildNameFilter(String nameFilter) {
        this.buildNameFilter = nameFilter;
        hasBuildNameFilter = true;
    }

    public void clearBuildNameFilter() { hasBuildNameFilter = false; }

    public void clearItemNameFilter() {
        hasItemNameFilter = false;
    }

    public void updateItemFilters(ArrayList<Filter> filters) {
        itemFilters = new ArrayList<>(filters);
    }
    public void updateBuildFilters(ArrayList<Filter> filters){
        buildFilters = new ArrayList<>(filters);
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

    public ArrayList<String> getAllSpecializations() {
        return new ArrayList<>(Arrays.asList(
                "Arcanist",
                "Elementalist",
                "Berskerker",
                "Guardian",
                "Hierophant",
                "Paladin",
                "Assassin",
                "Swordsage",
                "Apothecary",
                "Harbinger",
                "Hunter",
                "Ranger",
                "Reaper",
                "Tenebrist",
                "Hexbreaker",
                "Soothslayer"
        ));
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
    public void refreshItems() {
        ArrayList<DictionaryItem> filteredItems = new ArrayList<>(items);

        for (Filter filter : itemFilters) {
            if (filter != null) {
                switch (filter.getOption()) {
                    case "Stat" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredItems.removeIf(i -> !i.hasStat(filter.value));
                                case 1 -> filteredItems.removeIf(i -> i.hasStat(filter.value));
                                case 2 ->
                                        filteredItems.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) >= filter.constant));
                                case 3 ->
                                        filteredItems.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) > filter.constant));
                                case 4 ->
                                        filteredItems.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) == filter.constant));
                                case 5 ->
                                        filteredItems.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) <= filter.constant));
                                case 6 ->
                                        filteredItems.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) < filter.constant));
                            }
                        }
                    }
                    case "Tier" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredItems.removeIf(i -> !i.hasTier() || !i.tier.contains(filter.value));
                                case 1 -> filteredItems.removeIf(i -> i.hasTier() && i.tier.contains(filter.value));
                            }
                        }
                    }
                    case "Region" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredItems.removeIf(i -> !i.hasRegion() || !i.region.equals(filter.value));
                                case 1 -> filteredItems.removeIf(i -> i.hasRegion() && i.region.equals(filter.value));
                            }
                        }
                    }
                    case "Type" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredItems.removeIf(i -> !i.type.equals(filter.value));
                                case 1 -> filteredItems.removeIf(i -> i.type.equals(filter.value));
                            }
                        }
                    }
                    case "Location" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 ->
                                        filteredItems.removeIf(i -> !i.hasLocation() || !i.location.equals(filter.value));
                                case 1 ->
                                        filteredItems.removeIf(i -> i.hasLocation() && i.location.equals(filter.value));
                            }
                        }
                    }
                    case "Base Item" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredItems.removeIf(i -> !i.baseItem.equals(filter.value));
                                case 1 -> filteredItems.removeIf(i -> i.baseItem.equals(filter.value));
                            }
                        }
                    }
                }
            }
        }

        if (hasItemNameFilter)
            filteredItems.removeIf(i -> !i.name.toLowerCase().contains(itemNameFilter.toLowerCase()));


        if (itemGui.isGettingBuildItem) {
            if (itemGui.itemTypeLookingFor.equals("Mainhand")) {
                filteredItems.removeIf(i -> !i.type.equals("Mainhand") && !i.type.equals("Mainhand Sword") && !i.type.equals("Mainhand Shield") && !i.type.equals("Wand") && !i.type.equals("Axe") && !i.type.equals("Pickaxe") && !i.type.equals("Trident") && !i.type.equals("Snowball") && !i.type.equals("Shovel") && !i.type.equals("Scythe") && !i.type.equals("Bow") && !i.type.equals("Crossbow"));
            } else if (itemGui.itemTypeLookingFor.equals("Offhand")) {
                filteredItems.removeIf(i -> !i.type.equals("Offhand") && !i.type.equals("Offhand Sword") && !i.type.equals("Offhand Shield"));
            } else {
                filteredItems.removeIf(i -> !i.type.equals(itemGui.itemTypeLookingFor));
            }
        }

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
                switch (filter.getOption()) {
                    case "Stat" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredCharms.removeIf(i -> !i.hasStat(filter.value));
                                case 1 -> filteredCharms.removeIf(i -> i.hasStat(filter.value));
                                case 2 ->
                                        filteredCharms.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) >= filter.constant));
                                case 3 ->
                                        filteredCharms.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) > filter.constant));
                                case 4 ->
                                        filteredCharms.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) == filter.constant));
                                case 5 ->
                                        filteredCharms.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) <= filter.constant));
                                case 6 ->
                                        filteredCharms.removeIf(i -> !i.hasStat(filter.value) || !(i.getStat(filter.value) < filter.constant));
                            }
                        }
                    }
                    case "Tier" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredCharms.removeIf(i -> !i.tier.equals(filter.value));
                                case 1 -> filteredCharms.removeIf(i -> i.tier.equals(filter.value));
                            }
                        }
                    }
                    case "Class" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredCharms.removeIf(i -> !i.className.equals(filter.value));
                                case 1 -> filteredCharms.removeIf(i -> i.className.equals(filter.value));
                            }
                        }
                    }
                    case "Skill Modifier" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredCharms.removeIf(i -> !i.hasStatModifier(filter.value));
                                case 1 -> filteredCharms.removeIf(i -> i.hasStatModifier(filter.value));
                            }
                        }
                    }
                    case "Charm Power" -> {
                        switch (filter.comparator) {
                            case 2 -> filteredCharms.removeIf(i -> !(i.power >= filter.constant));
                            case 3 -> filteredCharms.removeIf(i -> !(i.power > filter.constant));
                            case 4 -> filteredCharms.removeIf(i -> !(i.power == filter.constant));
                            case 5 -> filteredCharms.removeIf(i -> !(i.power <= filter.constant));
                            case 6 -> filteredCharms.removeIf(i -> !(i.power < filter.constant));
                        }
                    }
                    case "Location" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredCharms.removeIf(i -> !i.location.equals(filter.value));
                                case 1 -> filteredCharms.removeIf(i -> i.location.equals(filter.value));
                            }
                        }
                    }
                    case "Base Item" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredCharms.removeIf(i -> !i.baseItem.equals(filter.value));
                                case 1 -> filteredCharms.removeIf(i -> i.baseItem.equals(filter.value));
                            }
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

    public void refreshBuilds() {
        ArrayList<DictionaryBuild> filteredBuilds = new ArrayList<>(builds);

        for (Filter filter : buildFilters) {
            if (filter != null) {
                switch (filter.getOption()) {
                    case "Region" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredBuilds.removeIf(build -> !build.region.equals(filter.value));
                                case 1 -> filteredBuilds.removeIf(build -> build.region.equals(filter.value));
                            }
                        }
                    }
                    case "Class" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredBuilds.removeIf(build -> !build.className.equals(filter.value));
                                case 1 -> filteredBuilds.removeIf(build -> build.className.equals(filter.value));
                            }
                        }
                    }
                    case "Specialization" -> {
                        if (!filter.value.isEmpty()) {
                            switch (filter.comparator) {
                                case 0 -> filteredBuilds.removeIf(build -> !build.specialization.equals(filter.value));
                                case 1 -> filteredBuilds.removeIf(build -> build.specialization.equals(filter.value));
                            }
                        }
                    }
                }
            }
        }

        if (hasBuildNameFilter) filteredBuilds.removeIf(build -> !build.name.toLowerCase().contains(buildNameFilter.toLowerCase()));

        filteredBuilds.sort((b1, b2) -> {
            if (b1.favorite && b2.favorite) return 0;
            else if (b1.favorite) return -1;
            else return 1;
        });

        validBuilds = filteredBuilds;
    }

    public ArrayList<DictionaryItem> getItems() {
        return validItems;
    }

    public ArrayList<DictionaryCharm> getCharms() {
        return validCharms;
    }

    public ArrayList<DictionaryBuild> getBuilds() {
        return validBuilds;
    }
    public void addBuild(DictionaryBuild build) {
        builds.add(build);
    }

    public boolean anyItems() {
        return items.isEmpty();
    }

    public boolean anyCharms() {
        return charms.isEmpty();
    }

    public DictionaryItem getItemByName(String itemName, boolean isExalted) {
        List<DictionaryItem> possibleItems = new ArrayList<>();
        for (DictionaryItem item : items) {
            if (item.name.equals(itemName)) {
                possibleItems.add(item);
            }
        }

        if (possibleItems.size() == 1) {
            return possibleItems.get(0);
        } else if (possibleItems.size() > 1) {
            for (DictionaryItem item : possibleItems) {
                if (isExalted && item.region.equals("Ring")) {
                    return item;
                } else if (!isExalted && !item.region.equals("Ring")) {
                    return item;
                }
            }
        }
        return null;
    }
    public DictionaryCharm getCharmByWeirdName(String rawCharm) {
        String[] rawCharmParts = rawCharm.split("-");

        String preffix = rawCharmParts[0].replace("_", " ");
        String suffix = rawCharmParts[1].replace("_", " ");
        int power = Integer.parseInt(rawCharmParts[2]);
        String classLetter = rawCharmParts[3];

        for (DictionaryCharm charm : charms) {
            String name = charm.name;
            if (name.substring(0, 3).equals(preffix) && name.contains(suffix) && charm.power == power && charm.className.startsWith(classLetter)) {
                return charm;
            }
        }
        return null;
    }

    public DictionaryCharm getCharmByName(String charmName) {
        for (DictionaryCharm charm : charms) {
            String name = charm.name;
            if (name.equals(charmName)) {
                return charm;
            }
        }
        return null;
    }

    public void getItemFromDictionary(String itemType) {
        itemGui.postInit();
        itemGui.isGettingBuildItem = true;
        itemGui.itemTypeLookingFor = itemType;
        clearItemNameFilter();
        itemGui.clearSearchBar();
        itemFilterGui.clearFilters();
        setItemDictionaryScreen();
    }

    public void getCharmFromDictionary() {
        charmGui.postInit();
        charmGui.isGettingBuildCharm = true;
        if (charmGuiPreviouslyOpened) {
            clearCharmNameFilter();
            charmGui.clearSearchBar();
            charmFilterGui.clearFilters();
        }
        setCharmDictionaryScreen();
    }

    public void toggleJsonBuildFavorite(int id) {
        try {
            File file = new File("config/mid/builds.json");

            JsonObject fileBuilds = JsonParser.parseString(readJsonBuild()).getAsJsonObject();

            for (String buildIdString : fileBuilds.asMap().keySet()) {
                int buildId = Integer.parseInt(buildIdString);
                if (buildId == id) {
                    JsonObject build = fileBuilds.get(String.valueOf(id)).getAsJsonObject();
                    boolean favoriteStatus = build.get("favorite").getAsBoolean();
                    build.addProperty("favorite", !favoriteStatus);

                    fileBuilds.add(String.valueOf(id), build);
                }
            }

            FileUtils.writeStringToFile(file, fileBuilds.toString(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean idExists(int id) {
        JsonElement fileBuildsElement = JsonParser.parseString(readJsonBuild());
        if (fileBuildsElement instanceof JsonNull) return false;
        JsonObject fileBuilds = fileBuildsElement.getAsJsonObject();

        for (String buildIdString : fileBuilds.asMap().keySet()) if (Integer.parseInt(buildIdString) == id) return true;
        return false;
    }

    public void deleteBuildFromJson(int id) {
        try {
            File file = new File("config/mid/builds.json");

            JsonObject fileBuilds = JsonParser.parseString(readJsonBuild()).getAsJsonObject();

            fileBuilds.remove(String.valueOf(id));

            FileUtils.writeStringToFile(file, fileBuilds.toString(), Charset.defaultCharset());
            builds.removeIf(build -> build.id == id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int generateNewId() {
        Random rand = new Random();
        int id = rand.nextInt(10000);

        while (idExists(id)) {
            id = rand.nextInt(10000);
        }
        return id;
    }
}
