package dev.eliux.monumentaitemdictionary.util;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static dev.eliux.monumentaitemdictionary.util.Specialization.*;

public enum PlayerClass {

    NO_CLASS    ("No Class",    0xFFFFFF,
            new Specialization[]{NO_SPECIALIZATION}),
    MAGE        ("Mage",        0xA129D3,
            new Specialization[]{NO_SPECIALIZATION, ARCANIST, ELEMENTALIST}),
    SCOUT       ("Scout",       0x59B4EB,
            new Specialization[]{NO_SPECIALIZATION, HUNTER, RANGER}),
    ROGUE       ("Rogue",       0x36393D,
            new Specialization[]{NO_SPECIALIZATION, SWORDSAGE, ASSASSIN}),
    WARRIOR     ("Warrior",     0xD32818,
            new Specialization[]{NO_SPECIALIZATION, BERSERKER, GUARDIAN}),
    ALCHEMIST   ("Alchemist",   0x81D434,
            new Specialization[]{NO_SPECIALIZATION, HARBINGER, APOTHECARY}),
    WARLOCK     ("Warlock",     0xF0489E,
            new Specialization[]{NO_SPECIALIZATION, TENEBRIST, REAPER}),
    SHAMAN      ("Shaman",      0x009900,
            new Specialization[]{NO_SPECIALIZATION, WINDWEAVER, EARTHCARVER}),
    CLERIC      ("Cleric",      0xFFC644,
            new Specialization[]{NO_SPECIALIZATION, PALADIN, SERAPH}),
    GENERALIST  ("Generalist",  0x9F8F91,
            new Specialization[]{NO_SPECIALIZATION}),
    DD_ZENITH   ("DD/Zenith",   0x5d2d87,
            new Specialization[]{Specialization.NO_CLASS, DAWNBRINGER, EARTHBOUND, FLAMECALLER, FROSTBORN, SHADOWDANCER, STEELSAGE, WINDWALKER});

    public final String name;
    public final int color;
    public final Specialization[] specializations;

    public final Text text;

    public static final List<String> allSkills = new ArrayList<>();

    PlayerClass(String name, int color, Specialization[] specializations) {
        this.name = name;
        this.color = color;

        this.text = Text.of(name);
        this.specializations = specializations;
    }
    
    public static PlayerClass of(String name) {
        return switch (name) {
            case "Alchemist" -> ALCHEMIST;
            case "Warrior" -> WARRIOR;
            case "Cleric" -> CLERIC;
            case "Rogue" -> ROGUE;
            case "Mage" -> MAGE;
            case "Scout" -> SCOUT;
            case "Warlock" -> WARLOCK;
            case "Shaman" -> SHAMAN;
            case "Generalist" -> GENERALIST;
            case "DD/Zenith" -> DD_ZENITH;
            default -> NO_CLASS;
        };
    }

    public Text text() {
        return text;
    }

    public static void addSkill(String name) {
        StringBuilder builder = new StringBuilder();
        char[] chars = name.toCharArray();

        for (int i = 0; i != chars.length; i++) {
            if (chars[i] == ' ') {
                builder.append("_");
                continue;
            }
            if (chars[i] >= 'A' && chars[i] <= 'Z') {
                builder.append((char) (chars[i] + 32));
                continue;
            }
            builder.append(chars[i]);
        }
        allSkills.add(builder.toString());
    }
}
