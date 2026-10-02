package dev.eliux.monumentaitemdictionary.gui.builder;

import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.gui.widgets.BuildCharmButtonWidget;
import dev.eliux.monumentaitemdictionary.gui.widgets.BuildItemButtonWidget;
import dev.eliux.monumentaitemdictionary.gui.widgets.CheckBoxWidget;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemIconButtonWidget;
import dev.eliux.monumentaitemdictionary.util.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.Clipboard;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.floor;
import static java.lang.Math.max;

public class BuilderGui extends Screen {
    public static final int MAX_CHARM_POWER = 12;
    private final DictionaryController controller;
    public final List<String> itemTypesIndex = Arrays.asList("Mainhand", "Offhand", "Helmet", "Chestplate", "Leggings", "Boots");
    public final List<String> situationals = Arrays.asList("Shielding", "Poise", "Inure", "Steadfast", "Guard", "Second Wind", "Ethereal", "Reflexes", "Evasion", "Tempo", "Cloaked", "Adaptability", "Versatile");
    public final List<String> infusions = Arrays.asList("Vigor", "Focus", "Tenacity", "Vitality", "Perspicacity");
    private final List<BuildItemButtonWidget> buildItemButtons = new ArrayList<>();
    private final List<BuildCharmButtonWidget> buildCharmButtons = new ArrayList<>();
    public List<DictionaryCharm> charms = new ArrayList<>();
    public List<DictionaryItem> buildItems = Arrays.asList(null, null, null, null, null, null);
    private Regions region = Regions.NO_REGION;
    private ClassName className = ClassName.NO_CLASS;
    private Specializations specialization = Specializations.NO_SPECIALIZATION;
    public DictionaryItem itemOnBuildButton;
    private BuildCharmButtonWidget charmsButton;
    private Stats buildStats;
    private final List<String> statsToRender = new ArrayList<>();
    public int sideMenuWidth = 40;

    public int labelMenuHeight = 30;
    public int itemPadding = 5;
    public int buttonSize = 50;
    public int checkBoxSize = 20;
    public int statsY = 260 + itemPadding*2;
    public int statsRow = 350;
    public int statsColumn = 170;
    public int charmsY = labelMenuHeight + itemPadding + (buttonSize + itemPadding) * 3;
    public int charmsButtonY;
    public int charmsX = 2*statsColumn + itemPadding;
    public int textTimeOffset = 1;
    public float deltaTicks = 0;
    private int halfWidth;
    private static final boolean SHOW_ALL_SITUATIONALS = false;
    private static final int CONTENT_TOP_GAP = 8;
    private static final int STATS_PADDING = 8;
    private static final int STATS_ROW_HEIGHT = 13;
    private static final int STATS_HEADING_HEIGHT = 18;
    private static final int STATS_SECTION_GAP = 16;
    private int equipmentY;
    private int situationalsY;
    private int situationalSectionY;
    private int charmGridX;
    private int infusionsY;
    private int checkboxColumns;
    private int checkboxColumnWidth;
    private TextFieldWidget nameBar;
    private SliderWidget currentHealthSlider;
    private boolean draggingHealthSlider;
    private ItemIconButtonWidget showBuildDictionaryButton;
    private ItemIconButtonWidget buildClipboard;
    private ItemIconButtonWidget addBuildButton;
    private CyclingButtonWidget<Regions> regionButton;
    private Map<String, Boolean> enabledSituationals;
    private HashMap<String, Boolean> enabledInfusions;
    private final List<CheckBoxWidget> situationalCheckBoxList = new ArrayList<>();
    private final List<CheckBoxWidget> infusionsCheckBoxList = new ArrayList<>();
    private double currentHealthPercent = 100;
    private int scrollPixels = 0;
    private int statusY;
    private Text statusText = Text.literal("");
    private Text hoveredStat;
    private CyclingButtonWidget<ClassName> classButton;
    private CyclingButtonWidget<Specializations> specializationButton;

    public BuilderGui(Text title, DictionaryController controller) {
        super(title);
        this.controller = controller;

    }

    public void postInit() {
        this.charmsButtonY = (int) (getCharmsListWithPower().size()/getCharmColumns())*
                (buttonSize + itemPadding) + buttonSize + itemPadding;
        this.halfWidth = width/2;
        this.scrollPixels = 0;
        this.statusText = Text.literal("");

        nameBar = new TextFieldWidget(textRenderer,
                190 + textRenderer.getWidth(Text.literal("Monumenta Builder").setStyle(Style.EMPTY.withBold(true))),
                itemPadding,
                width - itemPadding - (185 + textRenderer.getWidth(Text.literal("Monumenta Builder").setStyle(Style.EMPTY.withBold(true)))),
                20, Text.literal("Build Name"));
        nameBar.setPlaceholder(Text.literal("Build Name: "));
        nameBar.setChangedListener(text -> statusText = Text.literal(""));
        nameBar.setText("");

        currentHealthSlider = new SliderWidget(
                charmsX,
                charmsY + charmsButtonY,
                (width - sideMenuWidth) - charmsX - itemPadding,
                20,
                Text.literal("Current Health: 100%"),
                1) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal("Current Health: " + Math.round(this.value * 100) + "%"));
            }

            @Override
            protected void applyValue() {
                currentHealthPercent = Math.round(this.value * 100);
                this.value = currentHealthPercent / 100.0;
                updateStats();
            }
        };

        buildClipboard = new ItemIconButtonWidget(30, 5, 20, 20, Text.literal(""), (button) -> buildClipboardButtonClicked(),
                Arrays.asList(Text.literal("Build Clipboard").setStyle(Style.EMPTY.withColor(0xFFFFFFFF)),
                        Text.literal("Click").setStyle(Style.EMPTY.withBold(true).withColor(ItemColors.TEXT_COLOR)).append(Text.literal(" to set a build from your clipboard").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR).withBold(false))),
                        Text.literal(""),
                        Text.literal("SHIFT + Click").setStyle(Style.EMPTY.withBold(true).withColor(ItemColors.TEXT_COLOR)).append(Text.literal(" to copy the build url of the current build").setStyle(Style.EMPTY.withColor(ItemColors.TEXT_COLOR).withBold(false)))),
                "name_tag", "");
        showBuildDictionaryButton = new ItemIconButtonWidget(
                width - sideMenuWidth + 10, labelMenuHeight + 10, 20, 20,
                Text.literal(""),
                (button) -> controller.setBuildDictionaryScreen(),
                Text.literal("Builds Data"),
                "diamond_chestplate", "");

        addBuildButton = new ItemIconButtonWidget(
                5, 5, 20, 20,
                Text.literal(""),
                (button) -> {
                    if (itemOnBuildButton == null) statusText = Text.literal("Please select an item to appear on your Build Icon").setStyle(Style.EMPTY.withColor(0xFFFF0000));
                    else if (nameBar.getText().isBlank()) statusText = Text.literal("Please put a name to your Build").setStyle(Style.EMPTY.withColor(0xFFFF0000));
                    else {
                        controller.buildDictionaryGui.addBuild(nameBar.getText(), buildItems, charms,
                                itemOnBuildButton, switch (region) {
                                    case ARCHITECTS_RING -> "Ring";
                                    case CELSIAN_ISLES -> "Isles";
                                    case KINGS_VALLEY -> "Valley";
                                    default -> "No Region";
                                }, className.getText().getString(), specialization.getText().getString());
                        controller.setBuildDictionaryScreen();
                    }
                },
                Text.literal("Add Build To Dictionary"), "writable_book", "");

        regionButton = CyclingButtonWidget.builder(Regions::getText)
                .values(Regions.values())
                .initially(Regions.NO_REGION)
                .build(55, 5, 125, 20, Text.literal("Region"),
                        (button, region) -> {
                            this.region = region;
                            updateStats();
                        });

        classButton = CyclingButtonWidget.builder(ClassName::getText)
                .values(ClassName.values())
                .initially(ClassName.NO_CLASS)
                .build(itemPadding,
                        labelMenuHeight + itemPadding + (itemPadding+buttonSize)*2,
                        Math.max(20, halfWidth - 2 * itemPadding), 20,
                        Text.literal("Class"),
                        (button, className) -> {
                            this.className = className;
                            specialization = Specializations.NO_SPECIALIZATION;
                            updateSpecializations();
                        });

        updateSpecializations();
        enabledSituationals = new HashMap<>() {{
            put("shielding", false);
            put("poise", false);
            put("inure", false);
            put("steadfast", false);
            put("guard", false);
            put("ethereal", false);
            put("reflexes", false);
            put("evasion", false);
            put("tempo", false);
            put("cloaked", false);
            put("adaptability", false);
            put("second_wind", false);
            put("versatile", false);
        }};
        enabledInfusions = new HashMap<>() {{
            put("vigor", false);
            put("focus", false);
            put("tenacity", false);
            put("vitality", false);
            put("perspicacity", false);
        }};

        this.buildStats = new Stats(buildItems, enabledSituationals, enabledInfusions, currentHealthPercent, getStatsRegion());
        updateButtons();
        updateGuiPositions();
    }

    private void buildClipboardButtonClicked() {
        if (hasShiftDown()) {
            getBuildUrl();
        } else {
            String buildUrl = new Clipboard().getClipboard(0, (e, d) -> Mid.LOGGER.info("Failed to get Clipboard"));
            if (verifyUrl(buildUrl)) {
                getBuildFromUrl(buildUrl);
            }
        }
    }

    private void getBuildUrl() {
        StringBuilder baseUrl = new StringBuilder("https://odetomisery.vercel.app/builder/");
        for (int i = 0; i < itemTypesIndex.size(); i++) {
            DictionaryItem item  = buildItems.get(i);
            baseUrl.append(itemTypesIndex.get(i).substring(0, 1).toLowerCase()).append("=");
            if (item != null) {
                baseUrl.append(getUrlItemName(item, controller.getAllItems())).append("&");
            } else baseUrl.append("None&");
        }

        baseUrl.append("charm=");
        if (!charms.isEmpty()) {
            baseUrl.append(charms.stream().map(this::getWeirdCharmName).collect(Collectors.joining(",")));
        } else baseUrl.append("None");

        if (!nameBar.getText().isEmpty()) {
            baseUrl.append("&name=").append(nameBar.getText().replace(" ", "%20"));
        }

        Clipboard clipboard = new Clipboard();
        clipboard.setClipboard(0, baseUrl.toString());
        statusText = Text.literal("Build Url copied to your clipboard!").setStyle(Style.EMPTY.withColor(0xFF00FF00));
    }

    private static String getUrlItemName(DictionaryItem item, List<DictionaryItem> dictionaryItems) {
        String name = item.name.equals("Carcano 91/38") ? "Carcano 9138" : item.name;
        if (item.hasMasterwork) {
            boolean isExalted = item.region.equals("Ring") && dictionaryItems.stream()
                    .anyMatch(other -> other.name.equals(item.name) && !other.hasMasterwork);
            if (isExalted) name = "EX " + name;
            name += "-" + (item.getMaxMasterwork() - 1);
        }
        return name.replace(" ", "%20");
    }

    private ArrayList<String> extractRelevantLetters(String charmName, int n) {
        ArrayList<String> parts = new ArrayList<>();
        parts.add(charmName.substring(0, 3).replace(" ", "_"));
        parts.add((charmName.length() - 3 < n) ? charmName.substring(3).replace(" ", "_") : charmName.substring(charmName.length() - n).replace(" ", "_"));

        return parts;
    }

    private String getWeirdCharmName(DictionaryCharm charm) {
        ArrayList<String> relevantLetters = extractRelevantLetters(charm.name.replace(" Charm", ""), 6);
        return String.format("%s-%s-%d-%s", relevantLetters.get(0), relevantLetters.get(1), charm.power, charm.className.charAt(0));
    }



    private void updateSpecializations() {
        if (specialization == Specializations.NO_CLASS) specialization = Specializations.NO_SPECIALIZATION;
        specializationButton = CyclingButtonWidget.builder(Specializations::getText)
                .values(switch (className) {
                    case NO_CLASS -> List.of(Specializations.NO_SPECIALIZATION);
                    case MAGE -> Arrays.asList(Specializations.NO_SPECIALIZATION, Specializations.ARCANIST, Specializations.ELEMENTALIST);
                    case SCOUT -> Arrays.asList(Specializations.NO_SPECIALIZATION, Specializations.HUNTER, Specializations.RANGER);
                    case ROGUE -> Arrays.asList(Specializations.NO_SPECIALIZATION, Specializations.SWORDSAGE, Specializations.ASSASSIN);
                    case WARRIOR -> Arrays.asList(Specializations.NO_SPECIALIZATION, Specializations.BERSERKER, Specializations.GUARDIAN);
                    case ALCHEMIST -> Arrays.asList(Specializations.NO_SPECIALIZATION, Specializations.HARBINGER, Specializations.APOTHECARY);
                    case WARLOCK -> Arrays.asList(Specializations.NO_SPECIALIZATION, Specializations.TENEBRIST, Specializations.REAPER);
                    case SHAMAN -> Arrays.asList(Specializations.NO_SPECIALIZATION, Specializations.HEXBREAKER, Specializations.SOOTHSAYER);
                    case CLERIC -> Arrays.asList(Specializations.NO_SPECIALIZATION, Specializations.PALADIN, Specializations.SERAPH);
                    case DD_ZENITH -> Specializations.getDDZenithClasses();
                })
                .initially(specialization)
                .build(charmsX,
                        labelMenuHeight + itemPadding + (itemPadding+buttonSize)*2 + 25,
                        Math.max(20, halfWidth - 2 * itemPadding), 20,
                        Text.literal("Spec"),
                        (button, specialization) -> this.specialization = specialization);
    }

    private void getBuildFromUrl(String buildUrl) {
        updateUserOptions();
        buildUrl = buildUrl.substring(buildUrl.indexOf("m="));
        DictionaryItem item;

        for (String rawItem : buildUrl.split("&")) {
            boolean isExalted = false;

            String itemName = String.join(" ", rawItem.substring(2).split("%20"));
            if (itemName.charAt(itemName.length()-2) == '-') {
                itemName = itemName.substring(0, itemName.length()-2);
            }

            if (itemName.contains("EX")) {
                itemName = itemName.replace("EX ", "");
                isExalted = true;
            }

            String itemType = rawItem.substring(0, 1);

            if (itemName.equals("Carcano 9138")) item = controller.getItemByName("Carcano 91/38", isExalted); //CARCANO HARDCODE
            else item = controller.getItemByName(itemName, isExalted);
            if (item == null) { continue; }

            switch (itemType) {
                case "m" -> buildItems.set(0, item);
                case "o" -> buildItems.set(1, item);
                case "h" -> buildItems.set(2, item);
                case "c" -> buildItems.set(3, item);
                case "l" -> buildItems.set(4, item);
                case "b" -> buildItems.set(5, item);
                default -> {}
            }
        }

        charms.clear();
        String[] rawCharms = buildUrl.substring(
                buildUrl.indexOf("charm=") + 6,
                buildUrl.contains("name=") ? buildUrl.indexOf("name=") - 1 : buildUrl.length())
                .split(",");
        if (!rawCharms[0].equals("None")) {
            for (String charm : rawCharms) {
                DictionaryCharm charmToAdd = controller.getCharmByWeirdName(charm);
                charms.add(charmToAdd);
            }
        }

        if (buildUrl.contains("name=")) {
           nameBar.setText(buildUrl.substring(buildUrl.indexOf("name=") + 5).replace("%20", " "));
        }

        updateButtons();
        updateStats();
    }

    private boolean verifyUrl(String buildUrl) {
        return buildUrl.contains("ohthemisery.tk/builder") || buildUrl.contains("ohthemisery.vercel.app/builder") || buildUrl.contains("ohthemisery-psi.vercel.app/builder") || buildUrl.contains("odetomisery.vercel.app/builder");
    }

    private BuildCharmButtonWidget getCharmButtonWidget(int i, @Nullable DictionaryCharm charm) {
        charmsButton = new BuildCharmButtonWidget(
                charmGridX + (int) (i % getCharmColumns()) * (buttonSize + itemPadding),
                charmsY + (int) (i / getCharmColumns()) * (buttonSize + itemPadding) - scrollPixels,
                buttonSize,
                Text.literal(""),
                (b) -> charmButtonClicked(charm, hasShiftDown(), hasControlDown()), charm,  () -> (charm != null) ? controller.charmGui.generateCharmLoreText(charm) : null,
                this);
        buildCharmButtons.add(charmsButton);

        return charmsButton;
    }

    private void charmButtonClicked(@Nullable DictionaryCharm charm, boolean shiftDown, boolean ctrlDown) {
        if (charm == null) controller.getCharmFromDictionary();
        else if (!shiftDown && !ctrlDown) {
            charms.remove(charm);
            controller.getCharmFromDictionary();
        } else if (shiftDown) {
            charms.remove(charm);
            updateStats();
        } else {
            String wikiFormatted = charm.name.replace(" ", "_").replace("'", "%27");
            Util.getOperatingSystem().open("https://monumenta.wiki.gg/wiki/" + wikiFormatted);
        }
    }

    private BuildItemButtonWidget getBuildItemButtonWidget(int i) {
        DictionaryItem item = buildItems.get(i);
        String itemType = itemTypesIndex.get(i);
        return new BuildItemButtonWidget(
                getEquipmentX(i),
                getEquipmentY(i) - scrollPixels,
                buttonSize,
                Text.literal(""),
                (b) -> itemButtonClicked(item, itemType, hasShiftDown(), hasControlDown()),
                item,
                () -> controller.itemGui.generateItemLoreText(item),
                this
        );
    }

    private void itemButtonClicked(@Nullable DictionaryItem item, String itemType, boolean shiftDown, boolean controlDown) {
        if (!shiftDown && !controlDown) controller.getItemFromDictionary(itemType);
        else if (shiftDown && !controlDown) {
            buildItems.set(itemTypesIndex.indexOf(itemType), null);
            updateStats();
        } else if (!shiftDown && item != null) {
            itemOnBuildButton = item;
            statusText = Text.literal("");
        } else if (item != null) {
            String wikiFormatted = item.name.replace(" ", "_").replace("'", "%27");
            Util.getOperatingSystem().open("https://monumenta.wiki.gg/wiki/" + wikiFormatted);
        }
    }

    public void updateGuiPositions() {
        if (nameBar == null || classButton == null || specializationButton == null) return;
        int contentWidth = width - sideMenuWidth;
        halfWidth = contentWidth / 2;
        buttonSize = Math.max(16, width / 18);
        itemPadding = Math.max(2, buttonSize / 10);
        int top = labelMenuHeight + CONTENT_TOP_GAP;
        equipmentY = top + 40;
        charmsX = halfWidth + itemPadding;
        charmGridX = charmsX;
        charmsY = getEquipmentY(2) + buttonSize + itemPadding;
        int charmSlots = Math.max(1, getCharmsListWithPower().size() + (getCharmsListWithPower().size() < MAX_CHARM_POWER ? 1 : 0));
        int charmRows = (charmSlots + getCharmColumns() - 1) / getCharmColumns();
        charmsButtonY = charmRows * (buttonSize + itemPadding) + itemPadding;

        int widestCheckbox = java.util.stream.Stream.concat(situationals.stream(), infusions.stream())
                .mapToInt(textRenderer::getWidth).max().orElse(0) + 32;
        checkboxColumns = Math.max(1, Math.min(4, (contentWidth - 2 * itemPadding) / widestCheckbox));
        checkboxColumnWidth = widestCheckbox;
        Set<String> equippedSituationals = getEquippedSituationals(buildItems);
        int visibleSituationals = SHOW_ALL_SITUATIONALS ? situationals.size() : (int) situationals.stream()
                .filter(name -> !name.equals("Versatile") && equippedSituationals
                        .contains(name.replace(" ", "_").toLowerCase(Locale.ROOT))).count();
        situationalSectionY = Math.max(getEquipmentY(5) + buttonSize, charmsY + charmsButtonY + 14) + 22;
        situationalsY = situationalSectionY + (visibleSituationals > 0 ? 16 : 0);
        infusionsY = visibleSituationals == 0 ? situationalSectionY : situationalsY
                + ((visibleSituationals + checkboxColumns - 1) / checkboxColumns) * (checkBoxSize + 5) + 18;
        int controlsBottom = infusionsY + ((infusions.size() + checkboxColumns - 1) / checkboxColumns) * (checkBoxSize + 5);
        int healthY = Math.max(controlsBottom, charmsY + charmsButtonY + 14) + 10;
        statsY = healthY + 20 + (statusText.getString().isEmpty() ? 8 : 24);
        statusY = statsY - 14;

        showBuildDictionaryButton.setX(width - sideMenuWidth + 10);
        showBuildDictionaryButton.setY(labelMenuHeight + 10);
        nameBar.setWidth(Math.max(1, width - 2 * itemPadding - (190 + textRenderer.getWidth(Text.literal("Monumenta Builder").setStyle(Style.EMPTY.withBold(true))))));
        classButton.setPosition(itemPadding, top - scrollPixels);
        classButton.setWidth(Math.max(20, halfWidth - 2 * itemPadding));
        specializationButton.setPosition(charmsX, top - scrollPixels);
        specializationButton.setWidth(Math.max(20, halfWidth - 2 * itemPadding));
        currentHealthSlider.setX(itemPadding + STATS_PADDING);
        currentHealthSlider.setY(healthY - scrollPixels);
        currentHealthSlider.setWidth(Math.max(20, contentWidth - 2 * itemPadding - 2 * STATS_PADDING));
    }

    private int getEquipmentY(int index) {
        if (index < 2) return equipmentY;
        return equipmentY + (index - 1) * (buttonSize + itemPadding) + 12;
    }

    private int getEquipmentX(int index) {
        return index == 1 ? charmsX : itemPadding;
    }

    private int getEquipmentRight(int index) {
        return index == 1 ? width - sideMenuWidth - itemPadding : halfWidth - itemPadding;
    }

    public boolean isEquipmentRowHovered(BuildItemButtonWidget widget, double mouseX, double mouseY) {
        int right = widget.getX() >= halfWidth ? width - sideMenuWidth - itemPadding : halfWidth - itemPadding;
        return isInContentViewport(mouseX, mouseY) && mouseX >= widget.getX() && mouseX < right
                && mouseY >= widget.getY() && mouseY < widget.getY() + widget.getHeight();
    }

    private void drawSectionLabel(DrawContext context, String label, int x, int y, int right) {
        int screenY = y - scrollPixels;
        Text text = Text.literal(label).setStyle(Style.EMPTY.withBold(true));
        context.drawTextWithShadow(textRenderer, text, x, screenY, 0xFFAAAAAA);
        int lineStart = x + textRenderer.getWidth(text) + 8;
        if (lineStart < right) context.drawHorizontalLine(lineStart, right, screenY + 4, 0x55777777);
    }

    private void drawControlSections(DrawContext context, int mouseX, int mouseY) {
        drawSectionLabel(context, "Equipment & Charms", itemPadding, equipmentY - 16, width - sideMenuWidth - 2 * itemPadding);
        if (situationalCheckBoxList.stream().anyMatch(checkbox -> checkbox.visible)) {
            drawSectionLabel(context, "Situational Effects", itemPadding, situationalSectionY, width - sideMenuWidth - 2 * itemPadding);
        }
        drawSectionLabel(context, "Infusions", itemPadding, infusionsY - 16, width - sideMenuWidth - 2 * itemPadding);
        for (BuildItemButtonWidget widget : buildItemButtons) {
            if (isEquipmentRowHovered(widget, mouseX, mouseY)) {
                int right = widget.getX() >= halfWidth ? width - sideMenuWidth - itemPadding : halfWidth - itemPadding;
                context.fill(widget.getX(), widget.getY(), right, widget.getY() + widget.getHeight(), 0x22000000);
            }
        }
    }

    public void updateButtons() {
        updateGuiPositions();
        situationalCheckBoxList.clear();
        for (int i = 0; i < situationals.size(); i++) {
            String situational = situationals.get(i);

            CheckBoxWidget situationalCheckBox = new CheckBoxWidget(
                    itemPadding + (i % checkboxColumns) * checkboxColumnWidth,
                    situationalsY + (i / checkboxColumns) * (checkBoxSize + 5) - scrollPixels,
                    Text.literal(situational),
                    enabledSituationals.get(situational.replace(" ", "_").toLowerCase()),
                    true,
                    this);
            situationalCheckBoxList.add(situationalCheckBox);
        }
        updateSituationalVisibility();

        infusionsCheckBoxList.clear();
        for (int i = 0; i < infusions.size() ; i++) {
            String infusion = infusions.get(i);

            CheckBoxWidget infusionCheckBox = new CheckBoxWidget(
                    itemPadding + (i % checkboxColumns) * checkboxColumnWidth,
                    infusionsY + (i / checkboxColumns) * (checkBoxSize + 5) - scrollPixels,
                    Text.literal(infusion),
                    enabledInfusions.get(infusions.get(i).toLowerCase()),
                    true,
                    this);
            infusionsCheckBoxList.add(infusionCheckBox);
        }


        buildItemButtons.clear();
        for (int i = 0; i < 6; i++) {
            BuildItemButtonWidget itemButton = getBuildItemButtonWidget(i);
            buildItemButtons.add(itemButton);
        }
        buildCharmButtons.clear();
        if (charms.isEmpty()) {
            charmsButton = getCharmButtonWidget(0, null);
        } else {
            for (int i = 0; i < getCharmsListWithPower().size(); i++) {
                charmsButton = getCharmButtonWidget(i, getCharmsListWithPower().get(i));
            }
            if (getCharmsListWithPower().size() < MAX_CHARM_POWER) {
                charmsButton = getCharmButtonWidget(getCharmsListWithPower().size(), null);
            }
        }

        updateGuiPositions();
    }

    public List<DictionaryCharm> getCharmsListWithPower() {
        List<DictionaryCharm> charmsListWithPower = new ArrayList<>();
        for (DictionaryCharm charm : charms) {
            for (int j = 0; j < charm.power; j++) {
                charmsListWithPower.add(charm);
            }
        }
        return charmsListWithPower;
    }

    public void updateUserOptions() {
    }

    public void updateCheckBoxes() {
        Iterator<CheckBoxWidget> checkBox = situationalCheckBoxList.iterator();
        Iterator<String> text = situationals.iterator();
        while (checkBox.hasNext() && text.hasNext()) {
            enabledSituationals.put(text.next().replace(" ", "_").toLowerCase(), checkBox.next().isChecked());
        }

        checkBox = infusionsCheckBoxList.iterator();
        text = infusions.iterator();
        while (checkBox.hasNext() && text.hasNext()) {
            enabledInfusions.put(text.next().toLowerCase(), checkBox.next().isChecked());
        }
    }

    private int getStatsRegion() {
        return switch (region) {
            case KINGS_VALLEY -> 1;
            case CELSIAN_ISLES -> 2;
            case ARCHITECTS_RING -> 3;
            default -> getEquipmentRegion();
        };
    }

    private int getEquipmentRegion() {
        int highestRegion = 1;
        for (DictionaryItem item : buildItems) {
            if (item == null) continue;
            int itemRegion = switch (item.region) {
                case "Ring" -> 3;
                case "Isles" -> 2;
                default -> 1;
            };
            highestRegion = Math.max(highestRegion, itemRegion);
        }
        return highestRegion;
    }

    private static Set<String> getEquippedSituationals(List<DictionaryItem> items) {
        Set<String> equippedSituationals = new HashSet<>();
        for (DictionaryItem item : items) {
            if (item == null) continue;
            List<ItemStat> stats = item.hasMasterwork ? item.getStatsFromMasterwork(item.getMaxMasterwork() - 1) : item.getStatsNoMasterwork();
            if (stats == null) continue;
            for (ItemStat stat : stats) {
                if (stat.statValue > 0) equippedSituationals.add(stat.statName);
            }
        }
        return equippedSituationals;
    }

    private void updateSituationalVisibility() {
        Set<String> equippedSituationals = getEquippedSituationals(buildItems);
        int visibleIndex = 0;
        for (CheckBoxWidget checkbox : situationalCheckBoxList) {
            String name = checkbox.getMessage().getString().replace(" ", "_").toLowerCase();
            checkbox.visible = SHOW_ALL_SITUATIONALS || (!name.equals("versatile") && equippedSituationals.contains(name));
            checkbox.active = checkbox.visible;
            if (checkbox.visible) {
                checkbox.setX(itemPadding + (visibleIndex % checkboxColumns) * checkboxColumnWidth);
                checkbox.setY(situationalsY + (visibleIndex / checkboxColumns) * (checkBoxSize + 5) - scrollPixels);
                visibleIndex++;
            }
        }
    }

    public void updateStats() {
        updateCheckBoxes();
        updateSituationalVisibility();
        buildStats = new Stats(buildItems, enabledSituationals, enabledInfusions, currentHealthPercent, getStatsRegion());
        statsToRender.clear();
        Map<String, String> statFormatter = StatsFormats.getStatFormats();
        Field[] allFields = Stats.class.getFields();
        List<Field> fields = Arrays.stream(allFields).filter(field -> Modifier.isPublic(field.getModifiers())).toList();

        for (Field field : fields) {
            try {
                String statName = field.getName();
                if (statName.contains("EHP")) statName = statName.substring(0, statName.indexOf("EHP"));
                else if (statName.contains("HNDR")) statName = statName.substring(0, statName.indexOf("HNDR"));
                else if (statName.contains("DR")) statName = statName.substring(0, statName.indexOf("DR"));
                String formattedStatName = statFormatter.get(statName);
                String formattedStat = getFormattedStat(field, formattedStatName);
                statsToRender.add(formattedStat);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFormattedStat(Field field, String formattedStatName) throws IllegalAccessException {
        int intStatValue;
        double doubleStatValue;
        String formattedStatValue;
        if (field.get(buildStats) instanceof Percentage) {
            doubleStatValue = (((Percentage) field.get(buildStats)).perc);
            formattedStatValue = String.format("%.2f", doubleStatValue) + "%";
        } else if (field.get(buildStats) instanceof Integer) {
            intStatValue = field.getInt(buildStats);
            formattedStatValue = String.valueOf(intStatValue);
        } else {
            doubleStatValue = field.getDouble(buildStats);
            formattedStatValue = String.format("%.2f", doubleStatValue);
        }

        return formattedStatName + formattedStatValue;
    }

    public void loadItems(DictionaryBuild build) {
        buildItems = new ArrayList<>(build.allItems);
        charms = new ArrayList<>(build.charms);
        itemOnBuildButton = build.itemOnButton;

        region = region.getRegion(build.region);
        regionButton.setValue(region);
        className = className.getClass(build.className);
        specialization = specialization.getSpecialization(build.specialization);
        classButton.setValue(className);
        updateSpecializations();


        nameBar.setText(build.name);

        updateButtons();
        updateCheckBoxes();
        updateStats();
    }

    public void resetBuild() {
        buildItems = Arrays.asList(null, null, null, null, null, null);
        nameBar.setText("");
        charms.clear();
        className = ClassName.NO_CLASS;
        specialization = Specializations.NO_SPECIALIZATION;
        classButton.setValue(className);
        updateSpecializations();
        region = Regions.NO_REGION;
        regionButton.setValue(region);

        itemOnBuildButton = null;
        buildStats = new Stats(buildItems, enabledSituationals, enabledInfusions, currentHealthPercent, getStatsRegion());
        scrollPixels = 0;
        updateStats();
        updateCheckBoxes();
        updateButtons();
    }

    private void drawButtons(DrawContext context, int mouseX, int mouseY, float delta) {
        buildItemButtons.forEach((b) -> b.renderWidget(context, mouseX, mouseY, delta));
        buildCharmButtons.forEach((b) -> b.renderWidget(context, mouseX, mouseY, delta));
        situationalCheckBoxList.stream().filter(b -> b.visible).forEach((b) -> b.renderWidget(context, mouseX, mouseY, delta));
        infusionsCheckBoxList.forEach((b) -> b.renderWidget(context, mouseX, mouseY, delta));
    }

    private void drawItemText(DrawContext context, int mouseX, int mouseY) {
        for (int i = 0;i < buildItems.size(); i++) {
            DictionaryItem item = buildItems.get(i);
            if (item != null) {
                int x = getEquipmentX(i) + buttonSize + 2*itemPadding;
                int y = 10 + getEquipmentY(i) + Math.max(0, (buttonSize - 20) / 2) - scrollPixels;
                String tier = item.hasMasterwork ? item.getTierFromMasterwork(item.getMaxMasterwork() - 1) : item.getTierNoMasterwork();
                if (tier == null) tier = item.getTierFromMasterwork(item.getMinMasterwork());
                boolean bold = item.isFish ? ItemFormatter.shouldBoldFish(item.fishTier) : ItemFormatter.shouldBold(tier);
                boolean underline = item.isFish ? ItemFormatter.shouldUnderlineFish(item.fishTier) : ItemFormatter.shouldUnderline(tier);
                boolean hovered = isEquipmentRowHovered(buildItemButtons.get(i), mouseX, mouseY);
                String itemText = getEquipmentText(item.name, x, getEquipmentRight(i), bold, hovered);
                context.drawTextWithShadow(textRenderer,
                        Text.literal(itemText).setStyle(Style.EMPTY.withBold(bold).withUnderline(underline)),
                        x,
                        y,
                        0xFF000000 + ItemColors.getColorForLocation(item.location));
            }
        }

        for (int i = 0; i < buildItemButtons.size(); i++) {
            int x = getEquipmentX(i) + buttonSize + 2*itemPadding;
            int y = getEquipmentY(i) + Math.max(0, (buttonSize - 20) / 2) - scrollPixels;
            String text = getEquipmentText(itemTypesIndex.get(i), x, getEquipmentRight(i), true, false);
            context.drawTextWithShadow(textRenderer,
                    Text.literal(text).setStyle(Style.EMPTY.withBold(true)),
                    x,
                    y,
                    0xFFFFFFFF);
            if (buildItems.get(i) == null) {
                context.drawTextWithShadow(textRenderer, getEquipmentText("Click to add...", x, getEquipmentRight(i), false, false),
                        x, y + 10, 0xFFAAAAAA);
            }
        }

        String stars = getCharmsListWithPower().size() + "/" + MAX_CHARM_POWER;
        context.drawTextWithShadow(textRenderer, "Charms", charmsX, getEquipmentY(2) - scrollPixels, 0xFFAAAAAA);
        int power = Math.min(MAX_CHARM_POWER, getCharmsListWithPower().size());
        Text powerStars = Text.literal("★".repeat(power)).setStyle(Style.EMPTY.withColor(0xFFFF00))
                .append(Text.literal("☆".repeat(MAX_CHARM_POWER - power)).setStyle(Style.EMPTY.withColor(0xAAAAAA)));
        int starsX = charmsX + textRenderer.getWidth("Charms") + 6;
        int available = Math.max(1, width - sideMenuWidth - itemPadding - starsX);
        float starsScale = Math.min(1.0f, (float) available / Math.max(1, textRenderer.getWidth(powerStars)));
        context.getMatrices().push();
        context.getMatrices().translate(starsX, getEquipmentY(2) - scrollPixels, 0);
        context.getMatrices().scale(starsScale, starsScale, 1.0f);
        context.drawTextWithShadow(textRenderer, powerStars, 0, 0, 0xFFFFFF00);
        context.getMatrices().pop();
        context.drawTextWithShadow(textRenderer,
                Text.literal(stars),
                charmsX, getEquipmentY(2) + 10 - scrollPixels, 0xFFFFFF00);
        if (getCharmsListWithPower().size() >= MAX_CHARM_POWER) {
            context.drawTextWithShadow(textRenderer,
                    Text.literal(getSlidingText("Full Charms", charmsX, width - sideMenuWidth, true)).setStyle(Style.EMPTY.withBold(true).withUnderline(true)),
                    charmsX, charmsY + charmsButtonY + itemPadding - scrollPixels, 0xFFFF0000);
        } else {
            context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth("Click an empty slot to add a charm.",
                            Math.max(1, width - sideMenuWidth - charmsX - 2 * itemPadding)),
                    charmsX, charmsY + charmsButtonY + itemPadding - scrollPixels, 0xFFAAAAAA);
        }

        if (!statusText.getString().isEmpty()) {
            context.drawTextWithShadow(textRenderer, statusText, itemPadding, statusY - scrollPixels, 0xFFFF0000);
        }
    }

    private String getSlidingText(String text, int xi, int xf, boolean bold) {
        int textWidth = xf - xi - 10;
        if (textWidth <= 0 || text.isEmpty()) return "";
        if (textWidth >= textRenderer.getWidth(Text.literal(text).setStyle(Style.EMPTY.withBold(bold))) + 20) return text;
        int charWidth = textRenderer.getWidth(Text.literal("M").setStyle(Style.EMPTY.withBold(bold)));
        int textLength = (int) floor((double) textWidth/charWidth);
        if (textLength <= 0) return "";


        int start = textTimeOffset % textLength;

        return (text + " " + text + " " + text).substring(start, start + textLength);
    }

    private String getEquipmentText(String text, int x, int right, boolean bold, boolean hovered) {
        int available = Math.max(0, right - x - 4);
        Text styled = Text.literal(text).setStyle(Style.EMPTY.withBold(bold));
        if (textRenderer.getWidth(styled) <= available) return text;
        if (hovered) return getSlidingText(text, x, right, bold);
        int ellipsisWidth = textRenderer.getWidth(Text.literal("...").setStyle(styled.getStyle()));
        if (available <= ellipsisWidth) return "";
        return textRenderer.trimToWidth(styled, available - ellipsisWidth).getString() + "...";
    }

    private void drawStats(DrawContext context, int mouseX, int mouseY) {
        hoveredStat = null;
        if (statsToRender.isEmpty()) return;
        List<String> statsTypes = new ArrayList<>(Arrays.asList("Misc Stats", "Health Stats", "DR Stats", "HP Normalized DR Stats", "EHP Stats", "Melee Stats", "Projectile Stats", "Magic Stats"));
        List<List<String>> statsByType = new ArrayList<>();

        statsByType.add(statsToRender.subList(0 ,  7)); // Misc Stats
        statsByType.add(statsToRender.subList(7 , 15)); // Health Stats
        statsByType.add(statsToRender.subList(15, 22)); // Damage Reduction Stats
        statsByType.add(statsToRender.subList(22, 29)); // Health Normalized Damage Reduction Stats
        statsByType.add(statsToRender.subList(29, 36)); // EHP Stats
        statsByType.add(statsToRender.subList(36, 43)); // Melee Stats
        statsByType.add(statsToRender.subList(43, 49)); // Projectile Stats
        statsByType.add(statsToRender.subList(49, 53)); // Magic Stats

        int i = 0;
        int j = 0;
        int leftColumnHeight = statsByType.subList(0, 4).stream()
                .mapToInt(stats -> STATS_HEADING_HEIGHT + stats.size() * STATS_ROW_HEIGHT + STATS_SECTION_GAP).sum();
        int rightColumnHeight = statsByType.subList(4, 8).stream()
                .mapToInt(stats -> STATS_HEADING_HEIGHT + stats.size() * STATS_ROW_HEIGHT + STATS_SECTION_GAP).sum();
        statsRow = STATS_PADDING + Math.max(leftColumnHeight, rightColumnHeight);
        for (List<String> stats : statsByType) {
            if (i == 4) j = 0;
            int x = (i < 4 ? itemPadding : charmsX) + STATS_PADDING;
            int right = (i < 4 ? halfWidth - itemPadding : width - sideMenuWidth - itemPadding) - STATS_PADDING;
            int y = statsY + STATS_PADDING + j - scrollPixels;
            String heading = getEquipmentText(statsTypes.get(i), x, right, true, false);
            context.drawTextWithShadow(textRenderer, Text.literal(heading).setStyle(Style.EMPTY.withBold(true)), x, y, 0xFF92BDA3);
            if (!heading.equals(statsTypes.get(i)) && isInStatRow(mouseX, mouseY, x, right, y)) hoveredStat = Text.literal(statsTypes.get(i));
            j += STATS_HEADING_HEIGHT;
            for (String stat : stats) {
                drawStatRow(context, stat, x, right, statsY + STATS_PADDING + j - scrollPixels, mouseX, mouseY);
                j += STATS_ROW_HEIGHT;
            }
            j += STATS_SECTION_GAP;
            i++;
        }
    }

    private boolean isInStatRow(int mouseX, int mouseY, int x, int right, int y) {
        return isInContentViewport(mouseX, mouseY) && mouseX >= x && mouseX < right && mouseY >= y && mouseY < y + STATS_ROW_HEIGHT;
    }

    private void drawStatRow(DrawContext context, String stat, int x, int right, int y, int mouseX, int mouseY) {
        int separator = stat.lastIndexOf(": ");
        String label = separator < 0 ? stat : stat.substring(0, separator);
        String value = separator < 0 ? "" : stat.substring(separator + 2);
        int valueX = Math.max(x, right - textRenderer.getWidth(value));
        String visibleLabel = getEquipmentText(label, x, valueX - 4, false, false);
        context.drawTextWithShadow(textRenderer, visibleLabel, x, y, 0xFFA1BA89);
        context.drawTextWithShadow(textRenderer, value, valueX, y, 0xFFA1BA89);
        if (!visibleLabel.equals(label) && isInStatRow(mouseX, mouseY, x, right, y)) hoveredStat = Text.literal(stat);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        deltaTicks += delta;
        textTimeOffset += (deltaTicks >= 20) ? 1 : 0;
        deltaTicks = (deltaTicks >= 20) ? 0 : deltaTicks;
        textTimeOffset = (textTimeOffset >= 100) ? 1 : textTimeOffset;

        updateGuiPositions();
        int previousScroll = scrollPixels;
        updateScrollLimits();
        if (previousScroll != scrollPixels) updateGuiPositions();
        updateButtons();
        context.enableScissor(0, labelMenuHeight, width - sideMenuWidth - 2, height);
        drawControlSections(context, mouseX, mouseY);
        classButton.render(context, mouseX, mouseY, delta);
        specializationButton.render(context, mouseX, mouseY, delta);
        currentHealthSlider.render(context, mouseX, mouseY, delta);
        drawButtons(context, mouseX, mouseY, delta);
        drawItemText(context, mouseX, mouseY);
        drawStats(context, mouseX, mouseY);
        context.disableScissor();

        int viewportHeight = Math.max(1, height - labelMenuHeight);
        int maxScroll = getMaxScrollPixels();
        int scrollX = width - sideMenuWidth - 2;
        context.fill(scrollX, labelMenuHeight, scrollX + 2, height, 0x77AAAAAA);
        if (maxScroll > 0) {
            int thumbHeight = Math.max(8, viewportHeight * viewportHeight / (viewportHeight + maxScroll));
            int thumbY = labelMenuHeight + scrollPixels * (viewportHeight - thumbHeight) / maxScroll;
            context.fill(scrollX, thumbY, scrollX + 2, thumbY + thumbHeight, 0xFFC3C3C3);
        }

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 440);
        context.fill(0, 0, width, labelMenuHeight, 0xFF555555);
        context.drawHorizontalLine(0, width, labelMenuHeight, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("Monumenta Builder").setStyle(Style.EMPTY.withBold(true)), 185, (labelMenuHeight - textRenderer.fontHeight) / 2, 0xFF2ca9d3);
        context.getMatrices().pop();

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 500);
        regionButton.render(context, mouseX, mouseY, delta);
        nameBar.render(context, mouseX, mouseY, delta);
        addBuildButton.render(context, mouseX, mouseY, delta);
        buildClipboard.render(context, mouseX, mouseY, delta);
        showBuildDictionaryButton.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();

        if (isInContentViewport(mouseX, mouseY)) {
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 500);
            buildItemButtons.forEach(b -> b.renderItemTooltip(context, mouseX, mouseY));
            buildCharmButtons.forEach(b -> b.renderCharmTooltip(context, mouseX, mouseY));
            if (hoveredStat != null) context.drawTooltip(textRenderer, hoveredStat, mouseX, mouseY);
            context.getMatrices().pop();
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);

        updateGuiPositions();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        draggingHealthSlider = false;
        boolean overName = mouseY >= 0 && mouseY < labelMenuHeight && nameBar.visible && nameBar.isMouseOver(mouseX, mouseY);
        nameBar.setFocused(overName);
        if (mouseY >= 0 && mouseY < labelMenuHeight) {
            if (overName) return nameBar.mouseClicked(mouseX, mouseY, button);
            if (regionButton.mouseClicked(mouseX, mouseY, button)) return true;
            if (addBuildButton.mouseClicked(mouseX, mouseY, button)) return true;
            if (buildClipboard.mouseClicked(mouseX, mouseY, button)) return true;
            return true;
        }
        if (showBuildDictionaryButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (!isInContentViewport(mouseX, mouseY)) return false;
        if (classButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (specializationButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (currentHealthSlider.mouseClicked(mouseX, mouseY, button)) {
            draggingHealthSlider = button == 0;
            return true;
        }
        for (CheckBoxWidget checkbox : situationalCheckBoxList) {
            if (checkbox.visible && checkbox.mouseClicked(mouseX, mouseY, button)) return true;
        }
        for (CheckBoxWidget checkbox : infusionsCheckBoxList) {
            if (checkbox.mouseClicked(mouseX, mouseY, button)) return true;
        }
        for (BuildItemButtonWidget item : buildItemButtons) {
            if (item.mouseClicked(mouseX, mouseY, button)) return true;
        }
        for (BuildCharmButtonWidget charm : buildCharmButtons) {
            if (charm.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return true;
    }

    public boolean isInContentViewport(double mouseX, double mouseY) {
        return mouseX >= 0 && mouseX < width - sideMenuWidth - 2 && mouseY >= labelMenuHeight && mouseY < height;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        if (!isInContentViewport(mouseX, mouseY)) return false;
        if (classButton.isMouseOver(mouseX, mouseY)) return classButton.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
        if (specializationButton.isMouseOver(mouseX, mouseY)) return specializationButton.mouseScrolled(mouseX, mouseY, hAmount, vAmount);
        scrollPixels += (int) (-vAmount * 22);
        updateScrollLimits();
        updateGuiPositions();
        updateButtons();
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingHealthSlider && button == 0) {
            if (isInContentViewport(mouseX, mouseY)) currentHealthSlider.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingHealthSlider && button == 0) {
            draggingHealthSlider = false;
            currentHealthSlider.mouseReleased(mouseX, mouseY, button);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private int getCharmColumns() {
        return Math.max(1, Math.min(5, (width - sideMenuWidth - charmGridX - itemPadding) / Math.max(1, buttonSize + itemPadding)));
    }

    private int getMaxScrollPixels() {
        int contentBottom = Math.max(statsY + statsRow, charmsY + charmsButtonY + 14);
        return Math.max(0, contentBottom + itemPadding - height);
    }

    private void updateScrollLimits () {
        int maxScroll = getMaxScrollPixels();
        if (scrollPixels > maxScroll) scrollPixels = maxScroll;

        if (scrollPixels < 0) scrollPixels = 0;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);

        nameBar.keyPressed(keyCode, scanCode, modifiers);

        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        super.charTyped(chr, modifiers);

        nameBar.charTyped(chr, modifiers);

        return true;
    }

    enum Regions {
        NO_REGION(Text.literal("Auto")),
        KINGS_VALLEY(Text.literal("King's Valley")),
        CELSIAN_ISLES(Text.literal("Celsian Isles")),
        ARCHITECTS_RING(Text.literal("Architect's Ring"));

        private final Text text;
        Regions(Text text) {
            this.text = text;
        }

        public Text getText() {
            return this.text;
        }

        public Regions getRegion(String region) {
            return switch (region) {
                case "Valley" -> KINGS_VALLEY;
                case "Isles" -> CELSIAN_ISLES;
                case "Ring" -> ARCHITECTS_RING;
                default -> NO_REGION;
            };
        }
    }

    enum ClassName {
        NO_CLASS(Text.literal("No Class")),
        MAGE(Text.literal("Mage")),
        SCOUT(Text.literal("Scout")),
        ROGUE(Text.literal("Rogue")),
        WARRIOR(Text.literal("Warrior")),
        ALCHEMIST(Text.literal("Alchemist")),
        WARLOCK(Text.literal("Warlock")),
        SHAMAN(Text.literal("Shaman")),
        CLERIC(Text.literal("Cleric")),
        DD_ZENITH(Text.literal("DD/Zenith"));

        private final Text text;
        ClassName(Text text) {
            this.text = text;
        }

        public Text getText() {
            return this.text;
        }
        public ClassName getClass(String className) {
            for (ClassName classNames : ClassName.values()) {
                if (classNames.text.getString().equals(className)) return classNames;
            }
            return NO_CLASS;
        }
    }

    enum Specializations {
        NO_SPECIALIZATION(Text.literal("No Specialization")),
        ARCANIST(Text.literal("Arcanist")),
        ELEMENTALIST(Text.literal("Elementalist")),
        RANGER(Text.literal("Ranger")),
        HUNTER(Text.literal("Hunter")),
        SWORDSAGE(Text.literal("Swordsage")),
        ASSASSIN(Text.literal("Assassin")),
        BERSERKER(Text.literal("Berserker")),
        GUARDIAN(Text.literal("Guardian")),
        HARBINGER(Text.literal("Harbinger")),
        APOTHECARY(Text.literal("Apothecary")),
        REAPER(Text.literal("Reaper")),
        TENEBRIST(Text.literal("Tenebrist")),
        SOOTHSAYER(Text.literal("Soothsayer")),
        HEXBREAKER(Text.literal("Hexbreaker")),
        PALADIN(Text.literal("Paladin")),
        SERAPH(Text.literal("Seraph")),

        NO_CLASS(Text.literal("No class")),
        DAWNBRINGER(Text.literal("Dawnbringer")),
        EARTHBOUND(Text.literal("Earthbound")),
        FLAMECALLER(Text.literal("Flamecaller")),
        FROSTBORN(Text.literal("Frostborn")),
        SHADOWDANCER(Text.literal("Shadowdancer")),
        STEELSAGE(Text.literal("Steelsage")),
        WINDWALKER(Text.literal("Windwalker"));

        private final Text text;
        Specializations(Text text) {
            this.text = text;
        }

        public Text getText() {
            return this.text;
        }

        public Specializations getSpecialization(String specialization) {
            for (Specializations specializations : Specializations.values()) {
                if (specializations.text.getString().equals(specialization)) return specializations;
            }
            return NO_SPECIALIZATION;
        }

        public static List<Specializations> getDDZenithClasses() {
            return Arrays.asList(NO_SPECIALIZATION, DAWNBRINGER, EARTHBOUND, FLAMECALLER, FROSTBORN, SHADOWDANCER, STEELSAGE, WINDWALKER);
        }
    }
}
