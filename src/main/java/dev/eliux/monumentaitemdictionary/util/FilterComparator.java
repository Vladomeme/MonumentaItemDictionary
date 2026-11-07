package dev.eliux.monumentaitemdictionary.util;

import net.minecraft.text.Text;

public enum FilterComparator {

    EXISTS ("Matches") {
        boolean test(boolean present, double constant, double fConstant) {
            return present;
        }
    },
    NOT_EXISTS ("Excludes") {
        boolean test(boolean present, double constant, double fConstant) {
            return !present;
        }
    },
    HIGHER_OR_EQUAL (">=") {
        boolean test(boolean present, double constant, double fConstant) {
            return present && constant >= fConstant;
        }
    },
    HIGHER (">") {
        boolean test(boolean present, double constant, double fConstant) {
            return present && constant > fConstant;
        }
    },
    EQUAL ("=") {
        boolean test(boolean present, double constant, double fConstant) {
            return present && constant == fConstant;
        }
    },
    LOWER_OR_EQUAL ("<=") {
        boolean test(boolean present, double constant, double fConstant) {
            return present && constant <= fConstant;
        }
    },
    LOWER ("<") {
        boolean test(boolean present, double constant, double fConstant) {
            return present && constant < fConstant;
        }
    };

    public final String display;

    public final Text text;

    private static final FilterComparator[] values = FilterComparator.values();

    FilterComparator(String display) {
        this.display = display;

        this.text = Text.of(display);
    }

    public boolean isPresence() {
        return ordinal() < 2;
    }

    public boolean isNumeric() {
        return ordinal() >= 2;
    }

    abstract boolean test(boolean present, double constant, double fConstant);

    public boolean test(boolean present) {
        return test(present, 0, 0);
    }

    public boolean test(double constant, double fConstant) {
        return test(true, constant, fConstant);
    }

    public enum Range {
        PRESENCE  (EXISTS, NOT_EXISTS),
        NUMERIC   (HIGHER_OR_EQUAL, LOWER),
        FULL      (EXISTS, LOWER);

        final FilterComparator start;
        final FilterComparator end;

        Range(FilterComparator start, FilterComparator end) {
            this.start = start;
            this.end = end;
        }
    }

    public FilterComparator next() {
        if (ordinal() == values.length - 1) return values[0];
        return values[ordinal() + 1];
    }
}
