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

import dev.eliux.monumentaitemdictionary.web.WebRequest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings({"CallToPrintStackTrace", "unused"})
public class DictionaryController {

    private String itemNameFilter;
    public boolean hasItemNameFilter = false;
    private String charmNameFilter;
    public boolean hasCharmNameFilter = false;
    private String buildNameFilter;
    public boolean hasBuildNameFilter = false;
    public List<Filter> itemFilters = new ArrayList<>();
    public List<Filter> charmFilters = new ArrayList<>();
    public List<Filter> buildFilters = new ArrayList<>();

    public List<String> allItemTypes;
    public List<String> allItemRegions;
    public List<String> allItemTiers;
    public Map<String, String> allItemLocations;
    public List<String> allItemStats;
    public List<String> allItemBaseItems;
    public List<String> allItemEffects;

    public List<String> allCharmRegions;
    public List<String> allCharmTiers;
    public Map<String, String> allCharmLocations;
    public List<String> allCharmSkillMods;
    public List<String> allCharmClasses;
    public List<String> allCharmStats;
    public List<String> allCharmBaseItems;

    public final List<DictionaryItem> items;
    public List<DictionaryItem> validItems;
    public final List<DictionaryCharm> charms;
    public List<DictionaryCharm> validCharms;
    public List<DictionaryBuild> builds;
    public List<DictionaryBuild> validBuilds;

    public Map<String, Location> locationData;

    public boolean isRequesting = false;

    public final ItemDictionaryGui itemGui;
    public final ItemFilterGui itemFilterGui;
    public final CharmDictionaryGui charmGui;
    public final CharmFilterGui charmFilterGui;
    public final BuildDictionaryGui buildDictionaryGui;
    public final BuildFilterGui buildFilterGui;
    public final BuilderGui builderGui;

    public boolean itemGuiPreviouslyOpened = false;
    public boolean itemFilterGuiPreviouslyOpened = false;
    public boolean charmGuiPreviouslyOpened = false;
    public boolean charmFilterGuiPreviouslyOpened = false;
    public boolean buildFilterGuiPreviouslyOpened = false;
    public boolean buildDictionaryGuiPreviouslyOpened = false;
    public boolean builderGuiPreviouslyOpened = false;

    public Screen lastOpenedScreen = null;

    public final GeneratorGui generatorGui;

    public DictionaryController() {
        items = new ArrayList<>();
        validItems = new ArrayList<>();
        charms = new ArrayList<>();
        validCharms = new ArrayList<>();
        builds = new ArrayList<>();
        validBuilds = new ArrayList<>();

        loadLocations();
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

    public void open() {
        if (lastOpenedScreen == null || lastOpenedScreen instanceof ItemDictionaryGui) {
            itemGui.isGettingBuildItem = false;
            setItemDictionaryScreen();
        }
        else if (lastOpenedScreen instanceof CharmDictionaryGui) {
            itemGui.isGettingBuildItem = false;
            setCharmDictionaryScreen();
        }
        else {
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
        }
        else itemGui.updateGuiPositions();
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
        }
        else charmGui.updateGuiPositions();
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
        }
        else buildDictionaryGui.updateGuiPositions();
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
        }
        else builderGui.updateGuiPositions();
    }

    private String readStringData(String path) {
        try {
            return Files.readString(Path.of(path), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            Mid.LOGGER.warn("Monumenta Item Dictionary file not found: {}", path);
        }
        return "{}";
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") //mkdirs(), createNewFile()
    private void writeStringData(String writeData, String path) {
        try {
            File targetFile = new File(path);

            targetFile.getParentFile().mkdirs();
            targetFile.createNewFile();

            FileUtils.writeStringToFile(targetFile, writeData, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") //createNewFile()
    public String readJsonBuild() {
        try {
            File buildsFile = new File("config/mid/builds.json");
            buildsFile.createNewFile();
            return Files.readString(buildsFile.toPath(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void writeJsonBuild(JsonObject jsonBuild, int id) {
        try {
            File file = new File("config/mid/builds.json");

            JsonObject fileBuilds = JsonParser.parseString((readJsonBuild().isEmpty()) ? "{}" : readJsonBuild()).getAsJsonObject();
            fileBuilds.add(String.valueOf(id), jsonBuild);

            FileUtils.writeStringToFile(file, fileBuilds.toString(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestAndUpdate() {
        Mid.LOGGER.info("Started Data Request");

        if (isRequesting) return;
        isRequesting = true;

        WebRequest locationsRequest = new WebRequest("https://api.playmonumenta.com/locations");
        WebRequest itemsRequest = new WebRequest("https://api.playmonumenta.com/itemswithnbt");

        WebManager.manageRequestsAsynchronous(() -> {
            if (locationsRequest.isSuccess()) {
                writeStringData(locationsRequest.result(), "config/mid/locations.json");
            }
            if (itemsRequest.isSuccess() && (locationsRequest.isSuccess() || new File("config/mid/locations.json").exists())) {
                writeStringData(itemsRequest.result(), "config/mid/items.json");
                loadLocations();
                loadItems();
                itemGui.buildItemList();
                loadCharms();
                charmGui.buildCharmList();

                isRequesting = false;
                Mid.LOGGER.info("Finished Data Request - Success");
            }
        }, () -> {
            isRequesting = false;
            Mid.LOGGER.info("Finished Data Request - Failure");
        }, locationsRequest, itemsRequest);
    }

    public void loadBuilds() {
        try {
            List<DictionaryBuild> buildsInFile = new ArrayList<>();

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

                List<DictionaryItem> buildItems = new ArrayList<>();
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

                List<DictionaryCharm> buildCharms = new ArrayList<>();
                JsonArray rawCharms = buildData.get("charms").getAsJsonArray();
                for (JsonElement charm : rawCharms.asList()) {
                    buildCharms.add(getCharmByName(charm.getAsString()));
                }
                buildsInFile.add(new DictionaryBuild(buildName, buildItems, buildCharms, buildItemToShow, buildRegion, buildClass, buildSpecialization, buildFavorite, id));
            }
            builds = buildsInFile;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLocations() {
        locationData = new HashMap<>();

        JsonObject data = new Gson().fromJson(readStringData("config/mid/locations.json"), JsonObject.class);

        if (!data.has("locations")) return;
        JsonArray jsonArray = data.getAsJsonArray("locations");

        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = (JsonObject) jsonElement;

            if (!jsonObject.has("name")
                    || !jsonObject.has("displayName")
                    || !jsonObject.has("color")) continue;

            String name = jsonObject.get("name").getAsString();
            String displayName = jsonObject.get("displayName").getAsString();
            String color = jsonObject.get("color").getAsString();

            try {
                locationData.put(name, new Location(displayName, Integer.decode(color)));
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadItems() {
        Set<String> itemTypeSet = new HashSet<>();
        Set<String> itemRegionSet = new HashSet<>();
        Set<String> itemTierSet = new HashSet<>();
        Set<String> itemStatSet = new HashSet<>();
        Set<String> itemBaseSet = new HashSet<>();
        Set<String> itemEffectSet = new HashSet<>();
        allItemLocations = new HashMap<>();

        Map<String, DictionaryItem> masterworkItems = new HashMap<>();

        try {
            String rawData = readStringData("config/mid/items.json");

            items.clear();
            JsonObject data = new Gson().fromJson(rawData, JsonObject.class);
            for (Map.Entry<String, JsonElement> itemEntry : data.asMap().entrySet()) {
                JsonObject itemData = (JsonObject) itemEntry.getValue();

                if (!itemData.has("type")
                        || !itemData.has("base_item")) continue;

                // TYPE (must be present)
                String itemType = itemData.get("type").getAsString();
                if (itemType.equals("Charm")) continue;
                itemTypeSet.add(itemType);

                // BASE ITEM (must be present)
                String itemBaseItem = itemData.get("base_item").getAsString();
                itemBaseSet.add(itemBaseItem);

                // NAME (must be present; exception - written books)
                String itemName;
                if (itemData.has("name"))
                    itemName = itemData.get("name").getAsString();
                else if (itemData.has("base_item") && itemData.get("base_item").getAsString().equals("Written Book"))
                    itemName = itemEntry.getKey();
                else continue;

                // REGION
                String itemRegion = "";
                JsonPrimitive regionPrimitive = itemData.getAsJsonPrimitive("region");
                if (regionPrimitive != null) {
                    itemRegion = regionPrimitive.getAsString();
                    itemRegionSet.add(itemRegion);
                }

                // TIER
                String itemTier = "";
                JsonPrimitive tierPrimitive = itemData.getAsJsonPrimitive("tier");
                if (tierPrimitive != null) {
                    itemTier = ItemFormatter.formatTierPrimitive(tierPrimitive.getAsString());
                    itemTierSet.add(itemTier);
                }

                // LOCATION
                String itemLocationId = "";
                JsonPrimitive locationIdPrimitive = itemData.getAsJsonPrimitive("locationId");
                if (locationIdPrimitive != null) {
                    itemLocationId = locationIdPrimitive.getAsString();
                    Location location = locationData.get(itemLocationId);
                    if (location != null) allItemLocations.put(itemLocationId, location.displayName());
                }

                // FISH TIER
                int fishTier = -1;
                boolean isFish = false;
                JsonPrimitive fishQualityPrimitive = itemData.getAsJsonPrimitive("fish_quality");
                if (fishQualityPrimitive != null) {
                    fishTier = fishQualityPrimitive.getAsInt();
                    isFish = true;
                }

                // LORE
                String itemLore = "";
                JsonPrimitive lorePrimitive = itemData.getAsJsonPrimitive("lore");
                if (lorePrimitive != null) itemLore = lorePrimitive.getAsString();

                // STATS
                List<ItemStat> itemStats = new ArrayList<>();
                JsonObject statObject = itemData.get("stats").getAsJsonObject();
                for (Map.Entry<String, JsonElement> statEntry : statObject.entrySet()) {
                    String statName = statEntry.getKey();
                    if (ItemFormatter.isHiddenStat(statName)) continue;

                    itemStats.add(new ItemStat(statName, statEntry.getValue().getAsFloat()));
                    itemStatSet.add(statName);
                }
                if (itemStats.isEmpty()) itemStats = List.of();

                // EFFECTS
                List<String> itemEffects = new ArrayList<>();
                if (itemData.has("effects")) {
                    JsonArray effectArray = itemData.get("effects").getAsJsonArray();
                    for (JsonElement effectElement : effectArray) {
                        JsonObject effectObject = (JsonObject) effectElement;
                        if (effectObject.has("EffectType")) {
                            String effectType = effectObject.get("EffectType").getAsString();
                            String effectEnchant = null;
                            if (effectObject.has("EffectEnchant"))
                                effectEnchant = effectObject.get("EffectEnchant").getAsString();

                            String effectName = ItemFormatter.formatEffect(effectType, effectEnchant);

                            itemEffects.add(effectName);
                            itemEffectSet.add(effectName);
                        }
                    }
                }
                if (itemEffects.isEmpty()) itemEffects = List.of();

                // NBT
                String itemNbt = "";
                JsonPrimitive nbtPrimitive = itemData.getAsJsonPrimitive("nbt");
                if (nbtPrimitive != null) itemNbt = nbtPrimitive.getAsString();

                // ------------------
                // BUILDING THE ITEM
                JsonPrimitive masterworkPrimitive = itemData.getAsJsonPrimitive("masterwork");
                if (masterworkPrimitive == null) {
                    // Item doesn't have masterwork
                    List<String> totalTierList = itemTier.isEmpty() ? List.of() : List.of(itemTier);
                    List<List<ItemStat>> totalStatsList = itemStats.isEmpty() ? List.of() : List.of(itemStats);
                    List<String> totalNbtList = itemNbt.isEmpty() ? List.of() : List.of(itemNbt);

                    items.add(new DictionaryItem(itemName, itemType, itemRegion, totalTierList, itemLocationId,
                            fishTier, isFish, itemBaseItem, itemLore, totalNbtList, totalStatsList, itemEffects, false));
                }
                else {
                    // Item has masterwork
                    if (itemNbt.isEmpty()) itemNbt = "{}";
                    int maxLevel = ItemFormatter.getMasterworkForRarity(itemTier);
                    int level = masterworkPrimitive.getAsInt();

                    if (level > maxLevel) continue; // Masterwork level exceeds current cap

                    DictionaryItem masterworkItem = masterworkItems.get(itemName);
                    if (masterworkItem != null) {
                        // Item already exists, add a masterwork tier
                        masterworkItem.addMasterworkTier(itemTier, itemStats, itemNbt, level);
                    }
                    else {
                        // Item doesn't exist yet
                        List<String> totalTierList = Arrays.asList(new String[maxLevel + 1]);
                        @SuppressWarnings("unchecked")
                        List<List<ItemStat>> totalStatsList = Arrays.asList(new List[maxLevel + 1]);
                        List<String> totalNbtList = Arrays.asList(new String[maxLevel + 1]);

                        totalTierList.set(level, itemTier);
                        totalStatsList.set(level, itemStats);
                        totalNbtList.set(level, itemNbt);

                        items.add(new DictionaryItem(itemName, itemType, itemRegion, totalTierList, itemLocationId,
                                fishTier, isFish, itemBaseItem, itemLore, totalNbtList, totalStatsList, itemEffects, true));
                        masterworkItems.put(itemName, items.getLast());
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        items.sort(DictionaryItem::compareTo);

        allItemTypes = new ArrayList<>(itemTypeSet);
        allItemRegions = new ArrayList<>(itemRegionSet);
        allItemTiers = new ArrayList<>(itemTierSet);
        allItemStats = new ArrayList<>(itemStatSet);
        allItemBaseItems = new ArrayList<>(itemBaseSet);
        allItemEffects = new ArrayList<>(itemEffectSet);
    }

    public void loadCharms() {
        Set<String> charmTierSet = new HashSet<>();
        Set<String> charmSkillModSet = new HashSet<>();
        Set<String> charmClassSet = new HashSet<>();
        Set<String> charmStatSet = new HashSet<>();
        Set<String> charmBaseSet = new HashSet<>();
        allCharmRegions = List.of("Architect's Ring"); // Only one region (for now?)
        allCharmLocations = new HashMap<>();

        try {
            String rawData = readStringData("config/mid/items.json");

            charms.clear();
            JsonObject data = new Gson().fromJson(rawData, JsonObject.class);
            for (JsonElement charmElement : data.asMap().values()) {
                JsonObject charmData = (JsonObject) charmElement;

                if (!charmData.get("type").getAsString().equals("Charm")) continue;

                if (!charmData.has("name")
                        || !charmData.has("locationId")
                        || !charmData.has("tier")
                        || !charmData.has("power")
                        || !charmData.has("class_name")
                        || !charmData.has("base_item")) continue;

                // NAME (must be present)
                String charmName = charmData.get("name").getAsString();

                // LOCATION (must be present)
                String charmLocationId = charmData.get("locationId").getAsString();
                Location location = locationData.get(charmLocationId);
                if (location != null) allCharmLocations.put(charmLocationId, location.displayName());

                // TIER (must be present)
                String charmTier = charmData.get("tier").getAsString();
                charmTierSet.add(charmTier);

                // POWER (must be present)
                int charmPower = charmData.get("power").getAsInt();

                // CLASS (must be present)
                String charmClass = charmData.get("class_name").getAsString();
                charmClassSet.add(charmClass);

                // BASE ITEM (must be present)
                String charmBaseItem = charmData.get("base_item").getAsString();
                charmBaseSet.add(charmBaseItem);

                // NBT
                String charmNbt = "{}";
                if (charmData.has("nbt")) charmNbt = charmData.get("nbt").getAsString();

                List<CharmStat> charmStats = new ArrayList<>();
                JsonObject statsObject = charmData.get("stats").getAsJsonObject();
                for (Map.Entry<String, JsonElement> statEntry : statsObject.entrySet()) {
                    JsonElement statElement = statEntry.getValue();

                    String statName = statEntry.getKey();
                    String skillMod = ItemFormatter.getSkillFromCharmStat(statName);
                    charmSkillModSet.add(skillMod);
                    charmStatSet.add(statName);

                    if (statElement instanceof JsonPrimitive)
                        charmStats.add(new CharmStat(statName, skillMod, false, statElement.getAsFloat()));
                    else {
                        JsonObject statObject = (JsonObject) statElement;
                        charmStats.add(new CharmStat(statName, skillMod, statObject.get("locked").getAsBoolean(), statObject.get("value").getAsFloat()));
                    }
                }
                if (charmStats.isEmpty()) charmStats = List.of();

                charms.add(new DictionaryCharm(charmName, "Architect's Ring", charmLocationId, charmTier, charmPower, charmClass, charmBaseItem, charmNbt, charmStats));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        charms.sort((o1, o2) -> {
            if (!o1.tier().equals(o2.tier()))
                return -(ItemFormatter.getNumberForTier(o1.tier()) - ItemFormatter.getNumberForTier(o2.tier()));
            return 0;
        });

        allCharmTiers = new ArrayList<>(charmTierSet);
        allCharmSkillMods = new ArrayList<>(charmSkillModSet);
        allCharmClasses = new ArrayList<>(charmClassSet);
        allCharmStats = new ArrayList<>(charmStatSet);
        allCharmBaseItems = new ArrayList<>(charmBaseSet);
    }

    public void setItemNameFilter(String nameFilter) {
        this.itemNameFilter = nameFilter;
        hasItemNameFilter = true;
    }

    public void setBuildNameFilter(String nameFilter) {
        this.buildNameFilter = nameFilter;
        hasBuildNameFilter = true;
    }

    public List<String> getAllSpecializations() {
        return new ArrayList<>(Arrays.asList(
                "Arcanist",
                "Elementalist",
                "Berserker",
                "Guardian",
                "Seraph",
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
                "Soothsayer"
        ));
    }

    public void setCharmNameFilter(String nameFilter) {
        this.charmNameFilter = nameFilter;
        hasCharmNameFilter = true;
    }

    public void refreshItems() {
        validItems = new ArrayList<>();

        Set<String> mainhandTypes = new HashSet<>(List.of("Mainhand", "Mainhand Sword", "Mainhand Shield", "Wand",
                "Axe", "Pickaxe", "Trident", "Snowball", "Shovel", "Scythe", "Bow", "Crossbow"));
        Set<String> offhandTypes = new HashSet<>(List.of("Offhand", "Offhand Sword", "Offhand Shield"));

        itemLoop:
        for (DictionaryItem item : items) {
            for (Filter filter : itemFilters) {
                if (filter == null || filter.value.isEmpty()) continue;

                String value = filter.value;
                double constant = filter.constant;
                int comparator = filter.comparator;

                if (!switch (filter.getOption()) {
                    case "Stat" -> switch (comparator) {
                        case 0 -> item.hasStat(value);
                        case 1 -> !item.hasStat(value);
                        case 2 -> item.hasStat(value) && item.getStat(value) >= constant;
                        case 3 -> item.hasStat(value) && item.getStat(value) > constant;
                        case 4 -> item.hasStat(value) && item.getStat(value) == constant;
                        case 5 -> item.hasStat(value) && item.getStat(value) <= constant;
                        case 6 -> item.hasStat(value) && item.getStat(value) < constant;

                        default -> throw new AssertionError();
                    };
                    case "Tier" -> switch (comparator) {
                        case 0 -> item.hasTier() && item.tier().contains(value);
                        case 1 -> !item.hasTier() || !item.tier().contains(value);

                        default -> throw new AssertionError();
                    };
                    case "Region" -> switch (comparator) {
                        case 0 -> item.hasRegion() && item.region().equals(value);
                        case 1 -> !item.hasRegion() || !item.region().equals(value);

                        default -> throw new AssertionError();
                    };
                    case "Type" -> switch (comparator) {
                        case 0 -> item.type().equals(value);
                        case 1 -> !item.type().equals(value);

                        default -> throw new AssertionError();
                    };
                    case "Location" -> switch (comparator) {
                        case 0 -> item.hasLocation() && locationData.containsKey(item.locationId()) && locationData.get(item.locationId()).displayName().equals(value);
                        case 1 -> !item.hasLocation() || !locationData.containsKey(item.locationId()) || !locationData.get(item.locationId()).displayName().equals(value);

                        default -> throw new AssertionError();
                    };
                    case "Base Item" -> switch (comparator) {
                        case 0 -> item.baseItem().equals(value);
                        case 1 -> !item.baseItem().equals(value);

                        default -> throw new AssertionError();
                    };
                    case "Effect" -> switch (comparator) {
                        case 0 -> item.hasEffects() && item.effects().contains(value);
                        case 1 -> !item.hasEffects() || !item.effects().contains(value);

                        default -> throw new AssertionError();
                    };
                    default -> throw new AssertionError();
                }) continue itemLoop;
            }

            if (hasItemNameFilter && !StringUtils.containsIgnoreCase(item.name(), itemNameFilter)) continue;

            if (itemGui.isGettingBuildItem) {
                if (itemGui.itemTypeLookingFor.equals("Mainhand")) {
                    if (!mainhandTypes.contains(item.type())) continue;
                }
                else if (itemGui.itemTypeLookingFor.equals("Offhand")) {
                    if (!offhandTypes.contains(item.type())) continue;
                }
                else if (!item.type().equals(itemGui.itemTypeLookingFor)) continue;
            }

            validItems.add(item);
        }

        validItems.sort((o1, o2) -> {
            for (Filter f : itemFilters) {
                if (f.getOption().equals("Stat")) {
                    double val = o2.getStat(f.value) - o1.getStat(f.value);

                    if (val == 0) continue;
                    if (val > 0) return 1;
                    if (val < 0) return -1;
                }
            }
            return 0;
        });
    }

    public void refreshCharms() {
        validCharms = new ArrayList<>();

        charmLoop:
        for (DictionaryCharm charm : charms) {
            for (Filter filter : charmFilters) {
                if (filter == null || (filter.value.isEmpty() && !filter.getOption().equals("Charm Power"))) continue;

                String value = filter.value;
                double constant = filter.constant;
                int comparator = filter.comparator;

                if(!switch (filter.getOption()) {
                    case "Stat" -> switch (comparator) {
                        case 0 -> charm.hasStat(value);
                        case 1 -> !charm.hasStat(value);
                        case 2 -> charm.hasStat(value) && charm.getStat(value) >= constant;
                        case 3 -> charm.hasStat(value) && charm.getStat(value) > constant;
                        case 4 -> charm.hasStat(value) && charm.getStat(value) == constant;
                        case 5 -> charm.hasStat(value) && charm.getStat(value) <= constant;
                        case 6 -> charm.hasStat(value) && charm.getStat(value) < constant;

                        default -> throw new AssertionError();
                    };
                    case "Tier" -> switch (comparator) {
                        case 0 -> charm.tier().equals(value);
                        case 1 -> !charm.tier().equals(value);

                        default -> throw new AssertionError();
                    };
                    case "Class" -> switch (comparator) {
                        case 0 -> charm.className().equals(value);
                        case 1 -> !charm.className().equals(value);

                        default -> throw new AssertionError();
                    };
                    case "Skill Modifier" -> switch (comparator) {
                        case 0 -> charm.hasStatModifier(value);
                        case 1 -> !charm.hasStatModifier(value);

                        default -> throw new AssertionError();
                    };
                    case "Charm Power" -> switch (comparator) {
                        case 2 -> charm.power() >= constant;
                        case 3 -> charm.power() > constant;
                        case 4 -> charm.power() == constant;
                        case 5 -> charm.power() <= constant;
                        case 6 -> charm.power() < constant;

                        default -> throw new AssertionError();
                    };
                    case "Location" -> switch (comparator) {
                        case 0 -> locationData.containsKey(charm.locationId()) && locationData.get(charm.locationId()).displayName().equals(value);
                        case 1 -> !locationData.containsKey(charm.locationId()) || !locationData.get(charm.locationId()).displayName().equals(value);

                        default -> throw new AssertionError();
                    };
                    case "Base Item" -> switch (comparator) {
                        case 0 -> charm.baseItem().equals(value);
                        case 1 -> !charm.baseItem().equals(value);

                        default -> throw new AssertionError();
                    };
                    default -> throw new AssertionError();
                }) continue charmLoop;
            }

            if (hasCharmNameFilter && !StringUtils.containsIgnoreCase(charm.name(), charmNameFilter)) continue;

            validCharms.add(charm);
        }

        validCharms.sort((o1, o2) -> {
            for (Filter f : charmFilters) {
                if (f.getOption().equals("Stat")) {
                    float val = o2.getStat(f.value) - o1.getStat(f.value);

                    if (val == 0) continue;
                    if (val < 0) return -1;
                    if (val > 0) return 1;
                }
            }
            return 0;
        });
    }

    public void refreshBuilds() {
        validBuilds = new ArrayList<>();

        buildLoop:
        for (DictionaryBuild build : builds) {
            for (Filter filter : buildFilters) {
                if (filter == null || filter.value.isEmpty()) continue;

                String value = filter.value;
                int comparator = filter.comparator;

                if(!switch (filter.getOption()) {
                    case "Region" -> switch (comparator) {
                        case 0 -> build.region.equals(value);
                        case 1 -> !build.region.equals(value);

                        default -> throw new AssertionError();
                    };
                    case "Class" -> switch (comparator) {
                        case 0 -> build.className.equals(value);
                        case 1 -> !build.className.equals(value);

                        default -> throw new AssertionError();
                    };
                    case "Specialization" -> switch (comparator) {
                        case 0 -> build.specialization.equals(value);
                        case 1 -> !build.specialization.equals(value);

                        default -> throw new AssertionError();
                    };
                    default -> throw new AssertionError();
                }) continue buildLoop;
            }

            if (hasBuildNameFilter && !StringUtils.containsIgnoreCase(build.name, buildNameFilter)) continue;

            validBuilds.add(build);
        }

        validBuilds.sort((b1, b2) -> {
            if (b1.favorite == b2.favorite) return 0;
            else if (b1.favorite) return -1;
            else return 1;
        });
    }

    public DictionaryItem getItemByName(String itemName, boolean isExalted) {
        for (DictionaryItem item : items) {
            if (item.name().equals(itemName)
                    && (isExalted && item.region().equals("Ring") || (!isExalted && !item.region().equals("Ring")))) return item;
        }
        return null;
    }

    public DictionaryCharm getCharmByWeirdName(String rawCharm) {
        String[] rawCharmParts = rawCharm.split("-");

        String prefix = rawCharmParts[0].replace("_", " ");
        String suffix = rawCharmParts[1].replace("_", " ");
        int power = Integer.parseInt(rawCharmParts[2]);
        String classLetter = rawCharmParts[3];

        for (DictionaryCharm charm : charms) {
            String name = charm.name();
            if (name.substring(0, 3).equals(prefix) && name.contains(suffix) && charm.power() == power && charm.className().startsWith(classLetter))
                return charm;
        }
        return null;
    }

    public DictionaryCharm getCharmByName(String charmName) {
        for (DictionaryCharm charm : charms) {
            if (charm.name().equals(charmName)) return charm;
        }
        return null;
    }

    public void getItemFromDictionary(String itemType) {
        itemGui.postInit();
        itemGui.isGettingBuildItem = true;
        itemGui.itemTypeLookingFor = itemType;
        hasItemNameFilter = false;
        itemGui.clearSearchBar();
        itemFilterGui.clearFilters();
        setItemDictionaryScreen();
    }

    public void getCharmFromDictionary() {
        charmGui.postInit();
        charmGui.isGettingBuildCharm = true;
        if (charmGuiPreviouslyOpened) {
            hasCharmNameFilter = false;
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

            FileUtils.writeStringToFile(file, fileBuilds.toString(), StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean idExists(int id) {
        JsonElement fileBuildsElement = JsonParser.parseString(readJsonBuild());
        if (fileBuildsElement instanceof JsonNull) return false;
        JsonObject fileBuilds = fileBuildsElement.getAsJsonObject();

        for (String buildIdString : fileBuilds.asMap().keySet()) {
            if (Integer.parseInt(buildIdString) == id) return true;
        }
        return false;
    }

    public void deleteBuildFromJson(int id) {
        try {
            File file = new File("config/mid/builds.json");

            JsonObject fileBuilds = JsonParser.parseString(readJsonBuild()).getAsJsonObject();

            fileBuilds.remove(String.valueOf(id));

            FileUtils.writeStringToFile(file, fileBuilds.toString(), StandardCharsets.UTF_8);
            builds.removeIf(build -> build.id == id);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int generateNewId() {
        Random rand = new Random();

        int id = rand.nextInt(10000);
        while (idExists(id)) id = rand.nextInt(10000);

        return id;
    }
}
