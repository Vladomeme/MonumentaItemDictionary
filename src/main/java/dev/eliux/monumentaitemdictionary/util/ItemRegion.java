package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.Mid;
import net.minecraft.text.Text;

import java.util.List;

public enum ItemRegion {

    //order matters, used for sorting
    //       API name     Display name        EHP multi   Stat limit
    DEFAULT ("No Region", "No Region",        0.3,        36),
    VALLEY  ("Valley",    "King's Valley",    0.2,        20),
    ISLES   ("Isles",     "Celsian Isles",    0.25,       30),
    RING    ("Ring",      "Architect's Ring", 0.3,        36);

    public final String apiName;
    public final String name;
    public final double ehpMulti;
    public final double defStatLimit;

    public final Text text;

    ItemRegion(String apiName, String name, double ehpMulti, double defStatLimit) {
        this.apiName = apiName;
        this.name = name;
        this.ehpMulti = ehpMulti;
        this.defStatLimit = defStatLimit;

        this.text = Text.of(name);
    }

    public static ItemRegion of(String name) {
        return switch (name) {
            case "Valley" -> VALLEY;
            case "Isles" -> ISLES;
            case "Ring" -> RING;
            case "No Region" -> DEFAULT;
            default -> {
                Mid.LOGGER.info("Unknown region: {}", name);
                yield DEFAULT;
            }
        };
    }

    public static ItemRegion[] regions() {
        return new ItemRegion[]{RING, ISLES, VALLEY};
    }

    public static List<String> names() {
        return List.of(DEFAULT.apiName, RING.apiName, ISLES.apiName, VALLEY.apiName);
    }

    public Text text() {
        return text;
    }
}
