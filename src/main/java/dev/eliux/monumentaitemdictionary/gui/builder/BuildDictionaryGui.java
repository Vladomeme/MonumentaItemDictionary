package dev.eliux.monumentaitemdictionary.gui.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.eliux.monumentaitemdictionary.gui.DictionaryController;
import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;
import dev.eliux.monumentaitemdictionary.gui.widgets.BuildButtonWidget;
import dev.eliux.monumentaitemdictionary.gui.widgets.ItemIconButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.*;

public class BuildDictionaryGui extends Screen {
    private ItemIconButtonWidget addBuildButton;
    private ItemIconButtonWidget showItemsButton;
    private ItemIconButtonWidget showCharmsButton;
    public final int sideMenuWidth = 40;
    public final int labelMenuHeight = 30;
    public final int itemPadding = 10;
    public final int itemSize = 50;
    private long lastAltPressed = 0;
    public final DictionaryController controller;
    public ArrayList<DictionaryBuild> buildsList;
    public HashMap<DictionaryBuild, BuildButtonWidget> buildsButtons = new HashMap<>();
    private TextFieldWidget searchBar;
    private ItemIconButtonWidget filterButton;
    public final List<String> itemTypesIndex = Arrays.asList("Mainhand", "Offhand", "Helmet", "Chestplate", "Leggings", "Boots");

    public BuildDictionaryGui(Text title, DictionaryController controller) {
        super(title);
        this.controller = controller;
    }

    public void postInit() {
        searchBar = new TextFieldWidget(textRenderer, width / 2 + 90, 7, width / 2 - 100, 15, Text.literal("Search"));
        searchBar.setChangedListener(t -> {
            controller.setBuildNameFilter(searchBar.getText());
            if (searchBar.getText().isEmpty())
                controller.clearBuildNameFilter();

            buildBuildsList();
        });
        searchBar.setFocused(true);

        buildsList = new ArrayList<>();

        addBuildButton = new ItemIconButtonWidget(
                5, 5, 20, 20,
                Text.literal(""),
                (button) -> {
                    controller.setBuilderScreen();
                    controller.builderGui.resetBuild();
                    },
                Text.literal("Add Build"),
                "paper", "");

        showItemsButton = new ItemIconButtonWidget(
                width - sideMenuWidth + 10,labelMenuHeight + 10, 20, 20,
                Text.literal(""),
                (button) -> controller.setItemDictionaryScreen(),
                Text.literal("Item Data").setStyle(Style.EMPTY.withColor(0xFF00FFFF)),
                "iron_chestplate", "");

        showCharmsButton = new ItemIconButtonWidget(
                width - sideMenuWidth + 10, labelMenuHeight + 38, 20, 20,
                Text.literal(""),
                (button) -> controller.setCharmDictionaryScreen(),
                Text.literal("Charm Data").setStyle(Style.EMPTY.withColor(0xFFFFFF00)),
                "glowstone_dust", "");

        filterButton = new ItemIconButtonWidget(
                width - sideMenuWidth + 10, height - 30, 20, 20,
                Text.literal(""),
                button -> controller.setBuildFilterScreen(),
                Text.literal("Filter"), "chest", "");

        buildBuildsList();
    }

    public void buildBuildsList()
    {
        controller.loadBuilds();
        controller.refreshBuilds();
        buildsList = controller.getBuilds();

        buildsButtons.clear();
        for (DictionaryBuild build : buildsList) {
            BuildButtonWidget button = getBuildButtonWidget(build);
            buildsButtons.put(build, button);
        }
    }

    private BuildButtonWidget getBuildButtonWidget(DictionaryBuild build) {
        int index = buildsList.indexOf(build);
        int row = index / ((width - sideMenuWidth - 5) / (itemSize + itemPadding));
        int col = index % ((width - sideMenuWidth - 5) / (itemSize + itemPadding));

        int x = (col + 1) * itemPadding + col * itemSize;
        int y = labelMenuHeight + (row + 1) * itemPadding + row * itemSize;

        return new BuildButtonWidget(x, y, itemSize, Text.literal(build.name), b -> buildButtonClicked(build), build,
                this);
    }

    private void buildButtonClicked(DictionaryBuild build) {
        if (hasShiftDown() && hasControlDown()) {
            controller.deleteBuildFromJson(build.id);
            buildsList.remove(build);
        } else if (hasShiftDown()){
            toggleFavorite(build);
            buildsButtons.get(build).updateFavorite();
            controller.refreshBuilds();
        }else {
            controller.setBuilderScreen();
            controller.builderGui.loadItems(build);
        }
    }

    private void toggleFavorite(DictionaryBuild build) {
        build.favorite = !build.favorite;

        controller.toggleJsonBuildFavorite(build.id);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        context.fill(0, 0, width, labelMenuHeight, 0xFF555555);
        context.drawHorizontalLine(0, width, labelMenuHeight, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Build Dictionary").setStyle(Style.EMPTY.withBold(true)), width / 2, (labelMenuHeight - textRenderer.fontHeight) / 2, 0xFF2ca9d3);
        context.getMatrices().pop();
        context.drawVerticalLine(width - sideMenuWidth - 1, labelMenuHeight, height, 0x77AAAAAA); // called twice to make the scroll bar render wider (janky, but I don't really care)
        context.drawVerticalLine(width - sideMenuWidth - 2, labelMenuHeight, height, 0x77AAAAAA);

        buildsButtons.forEach((build, button) -> button.renderButton(context, mouseX, mouseY, delta));

        if (buildsButtons.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, "Found No Builds", width / 2, labelMenuHeight + 10, 0xFF2222);
        }

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 110);
        addBuildButton.render(context, mouseX, mouseY, delta);
        showCharmsButton.render(context, mouseX, mouseY, delta);
        showItemsButton.render(context, mouseX, mouseY, delta);
        filterButton.render(context, mouseX, mouseY, delta);
        searchBar.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();

        try {
            super.render(context, mouseX, mouseY, delta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        updateGuiPositions();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        buildsButtons.forEach((build, b) -> b.mouseClicked(mouseX, mouseY, button));
        buildBuildsList();

        addBuildButton.mouseClicked(mouseX, mouseY, button);
        showCharmsButton.mouseClicked(mouseX, mouseY, button);
        showItemsButton.mouseClicked(mouseX, mouseY, button);
        filterButton.mouseClicked(mouseX, mouseY, button);
        searchBar.mouseClicked(mouseX, mouseY, button);

        return true;
    }

    public void updateGuiPositions() {
        buildBuildsList();
        showItemsButton.setX(width - sideMenuWidth + 10);
        showItemsButton.setY(labelMenuHeight + 10);

        showCharmsButton.setX(width - sideMenuWidth + 10);
        showCharmsButton.setY(labelMenuHeight + 38);

        filterButton.setX(width - sideMenuWidth + 10);
        filterButton.setY(height - 30);
    }

    public void addBuild(String name, List<DictionaryItem> items, List<DictionaryCharm> charms, DictionaryItem itemOnBuildButton, String region, String className, String specialization) {
        if (name.isEmpty()) name = "No Name";
        int id = controller.generateNewId();

        DictionaryBuild build = new DictionaryBuild(name, items, charms, itemOnBuildButton, region, className, specialization, false, id);
        JsonObject jsonBuild = getBuildAsJSON(build);

        controller.writeJsonBuild(jsonBuild, build.id);

        controller.addBuild(build);
    }

    private JsonObject getBuildAsJSON(DictionaryBuild build) {
        JsonObject jsonBuild = new JsonObject();

        JsonObject itemsJson = new JsonObject();
        int i = 0;
        for (DictionaryItem item : build.allItems) {
            if (item != null) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("name", item.name);
                itemJson.addProperty("exalted", item.region.equals("Ring"));

                itemsJson.add(item.type, itemJson);
            } else {
                itemsJson.add(itemTypesIndex.get(i), new JsonObject());
            }
            i++;
        }

        ArrayList<DictionaryCharm> charmsWithoutDuplicates = new ArrayList<>(new HashSet<>(build.charms));
        JsonArray charmsArray = new JsonArray();
        for (DictionaryCharm charm : charmsWithoutDuplicates) {
            charmsArray.add(charm.name);
        }

        jsonBuild.addProperty("name", build.name);
        jsonBuild.add("items", itemsJson);
        jsonBuild.add("charms", charmsArray);
        jsonBuild.addProperty("region", build.region);
        jsonBuild.addProperty("class", build.className);
        jsonBuild.addProperty("specialization", build.specialization);

        JsonObject itemToShowJson = new JsonObject();
        itemToShowJson.addProperty("name", build.itemOnButton.name);
        itemToShowJson.addProperty("exalted", build.itemOnButton.region.equals("Ring"));
        jsonBuild.add("item_to_show", itemToShowJson);
        jsonBuild.addProperty("favorite", build.favorite);

        return jsonBuild;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);

        searchBar.keyPressed(keyCode, scanCode, modifiers);
        if (keyCode == 258) { // tab key pressed
            searchBar.setFocused(!searchBar.isFocused());
        }

        // reset filters shortcut
        if (keyCode == 342 || keyCode == 346) { // left or right alt pressed
            long lastAltPressed = 0;
            if (System.currentTimeMillis() - lastAltPressed < 1000) {
                controller.itemFilterGui.clearFilters();
                searchBar.setText("");
                buildBuildsList();
            }
        }

        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        super.keyReleased(keyCode, scanCode, modifiers);

        if (keyCode == 342 || keyCode == 346) { // left or right alt pressed
            lastAltPressed = System.currentTimeMillis();
        }

        return true;
    }



    @Override
    public boolean charTyped(char chr, int modifiers) {
        super.charTyped(chr, modifiers);

        searchBar.charTyped(chr, modifiers);

        return true;
    }

}

