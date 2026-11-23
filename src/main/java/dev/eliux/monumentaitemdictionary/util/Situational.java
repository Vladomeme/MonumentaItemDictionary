package dev.eliux.monumentaitemdictionary.util;

import net.minecraft.text.Text;

public enum Situational {

    SHIELDING   ("shielding",   "Shielding"),
    POISE       ("poise",       "Poise"),
    INURE       ("inure",       "Inure"),
    STEADFAST   ("steadfast",   "Steadfast"),
    GUARD       ("guard",       "Guard"),
    ETHEREAL    ("ethereal",    "Ethereal"),
    REFLEXES    ("reflexes",    "Reflexes"),
    EVASION     ("evasion",     "Evasion"),
    TEMPO       ("tempo",       "Tempo"),
    CLOAKED     ("cloaked",     "Cloaked"),
    SECOND_WIND ("second_wind", "Second Wind");

    public final String key;
    public final Text display;

    Situational(String key, String display) {
        this.key = key;
        this.display = Text.of(display);
    }
}
