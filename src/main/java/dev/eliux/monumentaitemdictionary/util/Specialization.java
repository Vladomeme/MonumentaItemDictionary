package dev.eliux.monumentaitemdictionary.util;

import dev.eliux.monumentaitemdictionary.Mid;
import net.minecraft.text.Text;

import java.util.List;

public enum Specialization {

    NO_SPECIALIZATION   ("No Specialization"),

    ARCANIST      ("Arcanist"),
    ELEMENTALIST  ("Elementalist"),
    RANGER        ("Ranger"),
    HUNTER        ("Hunter"),
    SWORDSAGE     ("Swordsage"),
    ASSASSIN      ("Assassin"),
    BERSERKER     ("Berserker"),
    GUARDIAN      ("Guardian"),
    HARBINGER     ("Harbinger"),
    APOTHECARY    ("Apothecary"),
    REAPER        ("Reaper"),
    TENEBRIST     ("Tenebrist"),
    EARTHCARVER   ("Earthcarver"),
    WINDWEAVER    ("Windweaver"),
    PALADIN       ("Paladin"),
    SERAPH        ("Seraph"),

    NO_CLASS      ("No class"),
    DAWNBRINGER   ("Dawnbringer"),
    EARTHBOUND    ("Earthbound"),
    FLAMECALLER   ("Flamecaller"),
    FROSTBORN     ("Frostborn"),
    SHADOWDANCER  ("Shadowdancer"),
    STEELSAGE     ("Steelsage"),
    WINDWALKER    ("Windwalker");

    public final String name;

    public final Text text;

    public static final List<String> classSpecializations = List.of(ARCANIST.name, ELEMENTALIST.name, RANGER.name, HUNTER.name,
            SWORDSAGE.name, ASSASSIN.name, BERSERKER.name, GUARDIAN.name, HARBINGER.name, APOTHECARY.name, REAPER.name, TENEBRIST.name,
            EARTHCARVER.name, WINDWEAVER.name, PALADIN.name, SERAPH.name);

    Specialization(String name) {
        this.name = name;

        this.text = Text.of(name);
    }

    public static Specialization of(String name) {
        return switch (name) {
            case "No Specialization" -> NO_SPECIALIZATION;

            case "Arcanist" -> ARCANIST;
            case "Elementalist" -> ELEMENTALIST;
            case "Ranger" -> RANGER;
            case "Hunter" -> HUNTER;
            case "Swordsage" -> SWORDSAGE;
            case "Assassin" -> ASSASSIN;
            case "Berserker" -> BERSERKER;
            case "Guardian" -> GUARDIAN;
            case "Harbinger" -> HARBINGER;
            case "Apothecary" -> APOTHECARY;
            case "Reaper" -> REAPER;
            case "Tenebrist" -> TENEBRIST;
            case "Earthcarver" -> EARTHCARVER;
            case "Windweaver" -> WINDWEAVER;
            case "Paladin" -> PALADIN;
            case "Seraph" -> SERAPH;

            case "No class" -> NO_CLASS;
            case "Dawnbringer" -> DAWNBRINGER;
            case "Earthbound" -> EARTHBOUND;
            case "Flamecaller" -> FLAMECALLER;
            case "Frostborn" -> FROSTBORN;
            case "Shadowdancer" -> SHADOWDANCER;
            case "Steelsage" -> STEELSAGE;
            case "Windwalker" -> WINDWALKER;

            default -> {
                Mid.LOGGER.info("Unknown specialization: {}", name);
                yield NO_SPECIALIZATION;
            }
        };
    }

    public Text text() {
        return text;
    }
}
