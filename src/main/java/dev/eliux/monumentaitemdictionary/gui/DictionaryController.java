package dev.eliux.monumentaitemdictionary.gui;

import com.google.gson.*;
import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.builder.BuildDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.builder.BuilderGui;
import dev.eliux.monumentaitemdictionary.gui.builder.DictionaryBuild;
import dev.eliux.monumentaitemdictionary.gui.charm.CharmDictionaryGui;
import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
import dev.eliux.monumentaitemdictionary.gui.filter.FilterGui;
import dev.eliux.monumentaitemdictionary.gui.generator.GeneratorGui;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.gui.item.ItemDictionaryGui;
import dev.eliux.monumentaitemdictionary.util.*;
import dev.eliux.monumentaitemdictionary.web.WebManager;

import java.nio.charset.StandardCharsets;
import java.util.*;

import dev.eliux.monumentaitemdictionary.web.WebRequest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import oshi.util.tuples.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("CallToPrintStackTrace")
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
    public List<String> allItemTiers;
    public Map<String, String> allItemLocations;
    public List<String> allItemStats;
    public List<String> allItemBaseItems;
    public List<String> allItemEffects;

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
    public final CharmDictionaryGui charmGui;
    public final BuildDictionaryGui buildDictionaryGui;
    public final BuilderGui builderGui;
    public final GeneratorGui generatorGui;
    public final FilterGui itemFilterGui;
    public final FilterGui charmFilterGui;
    public final FilterGui buildFilterGui;

    public Screen lastOpenedScreen = null;

    public DictionaryController() {
        items = new ArrayList<>();
        validItems = new ArrayList<>();
        charms = new ArrayList<>();
        validCharms = new ArrayList<>();
        builds = new ArrayList<>();
        validBuilds = new ArrayList<>();

        loadLocations();
        loadSkills();
        loadItems();
        loadCharms();
        loadBuilds();

        itemGui = new ItemDictionaryGui(Text.literal("Monumenta Item Dictionary"), this);
        charmGui = new CharmDictionaryGui(Text.literal("Monumenta Charm Dictionary"), this);
        buildDictionaryGui = new BuildDictionaryGui(Text.literal("Build Dictionary Menu"), this);
        builderGui = new BuilderGui(Text.literal("Builder Menu"), this);

        List<FilterType> itemFilterTypes = List.of(FilterType.ITEM_TIER, FilterType.REGION, FilterType.ITEM_LOCATION,
                FilterType.ITEM_TYPE, FilterType.ITEM_STAT, FilterType.ITEM_BASE_ITEM, FilterType.ITEM_EFFECT);
        List<FilterType> charmFilterTypes = List.of(FilterType.CHARM_TIER, FilterType.CHARM_LOCATION, FilterType.SKILL_MODIFIER,
                FilterType.CHARM_CLASS, FilterType.CHARM_POWER, FilterType.CHARM_STAT, FilterType.CHARM_BASE_ITEM);
        List<FilterType> buildFilterTypes = List.of(FilterType.REGION, FilterType.BUILD_CLASS, FilterType.SPECIALIZATION);

        itemFilterGui = new FilterGui("Item Filter Menu", "Item Filters", 150,
                itemFilterTypes, () -> open(itemGui), filters -> itemFilters = filters);
        charmFilterGui = new FilterGui("Charm Filter Menu", "Charm Filters", 180,
                charmFilterTypes, () -> open(charmGui), filters -> charmFilters = filters);
        buildFilterGui = new FilterGui("Build Filter Menu", "Build Filters", 150,
                buildFilterTypes, () -> open(buildDictionaryGui), filters -> buildFilters = filters);


        generatorGui = new GeneratorGui(Text.literal("Item Generator Options"), this);
    }

    public void open() {
        itemGui.isGettingBuildItem = false;
        charmGui.isGettingBuildCharm = false;

        if (lastOpenedScreen == null || lastOpenedScreen instanceof ItemDictionaryGui)
            open(itemGui);
        else if (lastOpenedScreen instanceof CharmDictionaryGui)
            open(charmGui);
        else
            open(itemGui);
    }

    public void open(Screen screen) {
        if (screen instanceof ItemDictionaryGui || screen instanceof CharmDictionaryGui)
            lastOpenedScreen = screen;

        MinecraftClient.getInstance().setScreen(screen);
        if (screen instanceof LazyScreen ls) ls.postInit();
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
        WebRequest skillsRequest = new WebRequest("https://api.playmonumenta.com/skills");
        WebRequest itemsRequest = new WebRequest("https://api.playmonumenta.com/itemswithnbt");

        WebManager.manageRequestsAsynchronous(() -> {
            if (locationsRequest.isSuccess()) {
                writeStringData(locationsRequest.result(), "config/mid/locations.json");
            }
            if (skillsRequest.isSuccess()) {
                writeStringData(skillsRequest.result(), "config/mid/skills.json");
            }
            if (itemsRequest.isSuccess() && (locationsRequest.isSuccess() || new File("config/mid/locations.json").exists())) {
                writeStringData(itemsRequest.result(), "config/mid/items.json");
                loadLocations();
                loadSkills();
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
        }, locationsRequest, skillsRequest, itemsRequest);
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
                ItemRegion buildRegion = ItemRegion.of(buildData.get("region").getAsString());
                PlayerClass buildClass = PlayerClass.of(buildData.get("class").getAsString());
                Specialization buildSpecialization = Specialization.of(buildData.get("specialization").getAsString());
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
                for (JsonElement charm : rawCharms) {
                    DictionaryCharm dictionaryCharm = getCharmByName(charm.getAsString());
                    if (dictionaryCharm != null) buildCharms.add(dictionaryCharm);
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
                locationData.put(name, new Location(name, displayName, Integer.decode(color)));
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadSkills() {
        PlayerClass.allSkills.clear();

        JsonObject data = new Gson().fromJson(readStringData("config/mid/skills.json"), JsonObject.class);

        if (!data.has("classes")) return;
        JsonArray jsonArray = data.get("classes").getAsJsonArray();

        for (JsonElement classElement : jsonArray) {
            JsonObject classObject = (JsonObject) classElement;

            if (classObject.has("classPassive")) {
                JsonObject passiveObject = classObject.get("classPassive").getAsJsonObject();
                if (passiveObject.has("name"))
                    PlayerClass.addSkill(passiveObject.get("name").getAsString());
                else if (passiveObject.has("displayName"))
                    PlayerClass.addSkill(passiveObject.get("displayName").getAsString());
            }

            if (classObject.has("skills")) {
                JsonArray skillsArray = classObject.get("skills").getAsJsonArray();
                for (JsonElement skillElement : skillsArray) {
                    JsonObject skillObject = (JsonObject) skillElement;
                    if (skillObject.has("name"))
                        PlayerClass.addSkill(skillObject.get("name").getAsString());
                    else if (skillObject.has("displayName"))
                        PlayerClass.addSkill(skillObject.get("displayName").getAsString());
                }
            }

            if (classObject.has("specs")) {
                JsonArray specsArray = classObject.get("specs").getAsJsonArray();
                for (JsonElement specElement : specsArray) {
                    JsonObject specObject = (JsonObject) specElement;
                    if (specObject.has("specPassive")) {
                        JsonObject passiveObject = specObject.get("specPassive").getAsJsonObject();
                        if (passiveObject.has("name"))
                            PlayerClass.addSkill(passiveObject.get("name").getAsString());
                        else if (passiveObject.has("displayName"))
                            PlayerClass.addSkill(passiveObject.get("displayName").getAsString());
                    }

                    if (!specObject.has("specSkills")) continue;
                    JsonArray skillsArray = specObject.get("specSkills").getAsJsonArray();
                    for (JsonElement skillElement : skillsArray) {
                        JsonObject skillObject = (JsonObject) skillElement;
                        if (skillObject.has("name"))
                            PlayerClass.addSkill(skillObject.get("name").getAsString());
                        else if (skillObject.has("displayName"))
                            PlayerClass.addSkill(skillObject.get("displayName").getAsString());
                    }
                }
            }
        }
    }

    public void loadItems() {
        Set<String> itemTypeSet = new HashSet<>();
        Set<String> itemTierSet = new HashSet<>();
        Set<String> itemStatSet = new HashSet<>();
        Set<String> itemBaseSet = new HashSet<>();
        Set<String> itemEffectSet = new HashSet<>();
        List<Pair<String, String>> itemLocations = new ArrayList<>();

        Map<String, DictionaryItem> masterworkItems = new HashMap<>();
        Map<String, String> statMap = new HashMap<>();

        try {
            String rawData = readStringData("config/mid/items.json");

            items.clear();
            JsonObject data = new Gson().fromJson(rawData, JsonObject.class);
            for (Map.Entry<String, JsonElement> itemEntry : data.asMap().entrySet()) {
                JsonObject itemData = (JsonObject) itemEntry.getValue();

                if (!itemData.has("type")
                        || !itemData.has("base_item")) continue;

                // TYPE (must be present)
                ItemType itemType = ItemType.of(itemData.get("type").getAsString());
                if (itemType == ItemType.CHARM) continue;
                itemTypeSet.add(itemType.name);

                // BASE ITEM (must be present)
                String baseItem = itemData.get("base_item").getAsString();
                Item itemBaseItem = Registries.ITEM.get(Identifier.of(baseItem.toLowerCase().replace(" ", "_")));
                itemBaseSet.add(baseItem);

                // NAME (must be present; exception - written books)
                String itemName;
                if (itemData.has("name"))
                    itemName = itemData.get("name").getAsString();
                else if (itemData.has("base_item") && itemData.get("base_item").getAsString().equals("Written Book"))
                    itemName = itemEntry.getKey();
                else continue;

                // REGION
                ItemRegion itemRegion = ItemRegion.DEFAULT;
                JsonPrimitive regionPrimitive = itemData.getAsJsonPrimitive("region");
                if (regionPrimitive != null) itemRegion = ItemRegion.of(regionPrimitive.getAsString());

                // TIER
                ItemTier itemTier = ItemTier.DEFAULT;
                JsonPrimitive tierPrimitive = itemData.getAsJsonPrimitive("tier");
                if (tierPrimitive != null) {
                    String tierValue = tierPrimitive.getAsString();
                    itemTier = ItemTier.of(tierValue);
                    itemTierSet.add(itemTier.name);
                }

                // LOCATION
                Location itemLocation = Location.EMPTY;
                JsonPrimitive locationIdPrimitive = itemData.getAsJsonPrimitive("locationId");
                if (locationIdPrimitive != null) {
                    String itemLocationId = locationIdPrimitive.getAsString();
                    itemLocation = locationData.get(itemLocationId);
                    if (itemLocation == null) itemLocation = Location.EMPTY;
                    else if (itemLocation != Location.EMPTY) itemLocations.add(new Pair<>(itemLocationId, itemLocation.displayName()));
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
                List<Text> itemLore = new ArrayList<>();
                JsonPrimitive lorePrimitive = itemData.getAsJsonPrimitive("lore");
                if (lorePrimitive != null) {
                    String[] lines = lorePrimitive.getAsString().split("\n");
                    for (String line : lines) {
                        itemLore.add(Text.literal(line).setStyle(Style.EMPTY.withColor(itemLocation.loreColor())));
                    }
                }
                if (itemLore.isEmpty()) itemLore = List.of();

                // NBT
                String itemNbt = "";
                JsonPrimitive nbtPrimitive = itemData.getAsJsonPrimitive("nbt");
                if (nbtPrimitive != null) itemNbt = nbtPrimitive.getAsString();

                // STATS
                List<ItemStat> itemStats = new ArrayList<>();
                JsonObject statObject = itemData.get("stats").getAsJsonObject();
                for (Map.Entry<String, JsonElement> statEntry : statObject.entrySet()) {
                    String statName = statEntry.getKey();
                    if (ItemFormatter.hiddenStats.contains(statName)) continue;

                    if (!statMap.containsKey(statName) && !itemNbt.isEmpty()) statMap.put(statName, itemNbt);
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

                // ------------------
                // BUILDING THE ITEM
                JsonPrimitive masterworkPrimitive = itemData.getAsJsonPrimitive("masterwork");
                if (masterworkPrimitive == null) {
                    // Item doesn't have masterwork
                    List<ItemTier> totalTierList = itemTier == ItemTier.DEFAULT ? List.of() : List.of(itemTier);
                    List<List<ItemStat>> totalStatsList = itemStats.isEmpty() ? List.of() : List.of(itemStats);
                    List<String> totalNbtList = itemNbt.isEmpty() ? List.of() : List.of(itemNbt);

                    items.add(new DictionaryItem(itemName, itemType, itemRegion, totalTierList, itemLocation,
                            fishTier, isFish, itemBaseItem, itemLore, totalNbtList, totalStatsList, itemEffects, false));
                }
                else {
                    // Item has masterwork
                    if (itemNbt.isEmpty()) itemNbt = "{}";
                    int maxLevel = itemTier.getMasterwork();
                    int level = masterworkPrimitive.getAsInt();

                    if (level > maxLevel) continue; // Masterwork level exceeds current cap

                    DictionaryItem masterworkItem = masterworkItems.get(itemName);
                    if (masterworkItem != null) {
                        // Item already exists, add a masterwork tier
                        masterworkItem.addMasterworkTier(itemTier, itemStats, itemNbt, level);
                    }
                    else {
                        // Item doesn't exist yet
                        List<ItemTier> totalTierList = Arrays.asList(new ItemTier[maxLevel + 1]);
                        @SuppressWarnings("unchecked")
                        List<List<ItemStat>> totalStatsList = Arrays.asList(new List[maxLevel + 1]);
                        List<String> totalNbtList = Arrays.asList(new String[maxLevel + 1]);

                        totalTierList.set(level, itemTier);
                        totalStatsList.set(level, itemStats);
                        totalNbtList.set(level, itemNbt);

                        items.add(new DictionaryItem(itemName, itemType, itemRegion, totalTierList, itemLocation,
                                fishTier, isFish, itemBaseItem, itemLore, totalNbtList, totalStatsList, itemEffects, true));
                        masterworkItems.put(itemName, items.getLast());
                    }
                }
            }
            ItemFormatter.extractEnchantInfo(statMap);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        items.sort(DictionaryItem::compareTo);

        allItemTypes = new ArrayList<>(itemTypeSet);
        allItemTiers = new ArrayList<>(itemTierSet);
        allItemStats = new ArrayList<>(itemStatSet);
        allItemBaseItems = new ArrayList<>(itemBaseSet);
        allItemEffects = new ArrayList<>(itemEffectSet);
        allItemTypes.sort(null);
        allItemTiers.sort(null);
        allItemStats.sort(null);
        allItemBaseItems.sort(null);
        allItemEffects.sort(null);

        itemLocations.sort(Comparator.comparing(Pair::getB));
        allItemLocations = new LinkedHashMap<>();
        for (Pair<String, String> pair : itemLocations) allItemLocations.put(pair.getA(), pair.getB());
    }

    public void loadCharms() {
        Set<String> charmTierSet = new HashSet<>();
        Set<String> charmSkillModSet = new HashSet<>();
        Set<String> charmClassSet = new HashSet<>();
        Set<String> charmStatSet = new HashSet<>();
        Set<String> charmBaseSet = new HashSet<>();
        allCharmLocations = new HashMap<>();

        try {
            String rawData = readStringData("config/mid/items.json");

            charms.clear();
            JsonObject data = new Gson().fromJson(rawData, JsonObject.class);
            for (JsonElement charmElement : data.asMap().values()) {
                JsonObject charmData = (JsonObject) charmElement;

                if (!charmData.has("type") || !charmData.get("type").getAsString().equals("Charm")) continue;

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
                Location charmLocation = locationData.get(charmLocationId);
                if (charmLocation == null) charmLocation = Location.EMPTY;
                else if (charmLocation != Location.EMPTY) allCharmLocations.put(charmLocationId, charmLocation.displayName());

                // TIER (must be present)
                ItemTier charmTier = ItemTier.of(charmData.get("tier").getAsString());
                charmTierSet.add(charmTier.name);

                // POWER (must be present)
                int charmPower = charmData.get("power").getAsInt();

                // CLASS (must be present)
                PlayerClass charmClass = PlayerClass.of(charmData.get("class_name").getAsString());
                charmClassSet.add(charmClass.name);

                // BASE ITEM (must be present)
                String baseItem = charmData.get("base_item").getAsString();
                Item charmBaseItem = Registries.ITEM.get(Identifier.of(baseItem.toLowerCase().replace(" ", "_")));
                charmBaseSet.add(baseItem);

                // LORE
                List<Text> charmLore = new ArrayList<>();
                JsonPrimitive lorePrimitive = charmData.getAsJsonPrimitive("lore");
                if (lorePrimitive != null) {
                    String[] lines = lorePrimitive.getAsString().split("\n");
                    for (String line : lines) {
                        charmLore.add(Text.literal(line).setStyle(Style.EMPTY.withColor(charmLocation.loreColor())));
                    }
                }
                if (charmLore.isEmpty()) charmLore = List.of();

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

                charms.add(new DictionaryCharm(charmName, charmLocation, charmTier, charmPower, charmClass, charmBaseItem, charmLore, charmNbt, charmStats));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        charms.sort(Comparator.comparingInt(o -> o.tier().ordinal()));

        allCharmTiers = new ArrayList<>(charmTierSet);
        allCharmSkillMods = new ArrayList<>(charmSkillModSet);
        allCharmClasses = new ArrayList<>(charmClassSet);
        allCharmStats = new ArrayList<>(charmStatSet);
        allCharmBaseItems = new ArrayList<>(charmBaseSet);
        allCharmTiers.sort(null);
        allCharmSkillMods.sort(null);
        allCharmClasses.sort(null);
        allCharmStats.sort(null);
        allCharmBaseItems.sort(null);
    }

    public void setItemNameFilter(String nameFilter) {
        this.itemNameFilter = nameFilter;
        hasItemNameFilter = true;
    }

    public void setBuildNameFilter(String nameFilter) {
        this.buildNameFilter = nameFilter;
        hasBuildNameFilter = true;
    }

    public void setCharmNameFilter(String nameFilter) {
        this.charmNameFilter = nameFilter;
        hasCharmNameFilter = true;
    }

    public void refreshItems() {
        validItems = new ArrayList<>();

        itemLoop:
        for (DictionaryItem item : items) {
            for (Filter filter : itemFilters) {
                if (filter == null || filter.isEmpty()) continue;
                if (filter.blocks(item)) continue itemLoop;
            }
            if (hasItemNameFilter && !StringUtils.containsIgnoreCase(item.name(), itemNameFilter)) continue;

            if (itemGui.isGettingBuildItem) {
                if (itemGui.itemTypeLookingFor == ItemType.MAINHAND) {
                    if (!item.type().isMainhand) continue;
                }
                else if (itemGui.itemTypeLookingFor == ItemType.OFFHAND) {
                    if (!item.type().isOffhand) continue;
                }
                else if (!item.type().equals(itemGui.itemTypeLookingFor)) continue;
            }

            validItems.add(item);
        }

        validItems.sort((o1, o2) -> {
            for (Filter filter : itemFilters) {
                if (filter.getType() == FilterType.ITEM_STAT) {
                    double val = o2.getStat((String) filter.getValue()) - o1.getStat((String) filter.getValue());

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
                if (filter == null || filter.isEmpty()) continue;
                if (filter.blocks(charm)) continue charmLoop;
            }
            if (hasCharmNameFilter && !StringUtils.containsIgnoreCase(charm.name(), charmNameFilter)) continue;

            validCharms.add(charm);
        }

        validCharms.sort((o1, o2) -> {
            for (Filter filter : charmFilters) {
                if (filter.getType() == FilterType.CHARM_STAT) {
                    float val = o2.getStat((String) filter.getValue()) - o1.getStat((String) filter.getValue());

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
                if (filter == null || filter.isEmpty()) continue;
                if (filter.blocks(build)) continue buildLoop;
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
                    && ((isExalted && (item.region() == ItemRegion.RING)) || (!isExalted && (item.region() != ItemRegion.RING)))) return item;
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
            if (name.substring(0, 3).equals(prefix) && name.contains(suffix) && charm.power() == power && charm.itemClass().name.startsWith(classLetter))
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

    public void getItemFromDictionary(ItemType itemType) {
        itemGui.postInit();
        itemGui.isGettingBuildItem = true;
        itemGui.itemTypeLookingFor = itemType;
        hasItemNameFilter = false;
        itemGui.clearSearchBar();
        itemFilterGui.clearFilters();
        open(itemGui);
    }

    public void getCharmFromDictionary() {
        charmGui.postInit();
        charmGui.isGettingBuildCharm = true;
        hasCharmNameFilter = false;
        charmGui.clearSearchBar();
        charmFilterGui.clearFilters();
        open(charmGui);
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
