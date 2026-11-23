package dev.eliux.monumentaitemdictionary.util;

public enum Infusion {

    VIGOR         ("Vigor"),
    FOCUS         ("Focus"),
    TENACITY      ("Tenacity"),
    VITALITY      ("Vitality"),
    PERSPICACITY  ("Perspicacity"),
    UNDERSTANDING ("Understanding");

    public final String display;

    Infusion(String display) {
        this.display = display;
    }
}
