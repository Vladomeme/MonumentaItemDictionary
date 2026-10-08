package dev.eliux.monumentaitemdictionary.gui.builder;

import dev.eliux.monumentaitemdictionary.gui.charm.DictionaryCharm;
import dev.eliux.monumentaitemdictionary.gui.item.DictionaryItem;

import java.util.Arrays;
import java.util.List;

public class DictionaryBuild {
    public final int id;
    public final DictionaryItem itemOnButton;
    public final String className;
    public final String specialization;
    public final String region;
    public final String name;
    public final DictionaryItem mainhand;
    public final DictionaryItem offhand;
    public final DictionaryItem head;
    public final DictionaryItem chestplate;
    public final DictionaryItem leggings;
    public final DictionaryItem boots;
    public final List<DictionaryCharm> charms;
    public final List<DictionaryItem> allItems;
    public boolean favorite;

    public DictionaryBuild(String name, List<DictionaryItem> items, List<DictionaryCharm> charms, DictionaryItem itemOnBuildButton, String region, String className, String specialization, boolean favorite, int id) {
        this.name = name;
        this.mainhand = items.get(0);
        this.offhand = items.get(1);
        this.head = items.get(2);
        this.chestplate = items.get(3);
        this.leggings = items.get(4);
        this.boots = items.get(5);
        this.charms = charms;
        this.itemOnButton = itemOnBuildButton;
        this.region = region;
        this.className = className;
        this.specialization = specialization;
        this.favorite = favorite;
        this.id = id;

        allItems = Arrays.asList(mainhand, offhand, head, chestplate, leggings, boots);
    }
}
