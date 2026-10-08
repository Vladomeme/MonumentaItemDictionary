package dev.eliux.monumentaitemdictionary.util;

public class Filter {

    private String option;
    public String value;
    public int comparator;
    public double constant;

    // comparator info:
    // 0 -> Exists
    // 1 -> Not Exists
    // 2 -> >=
    // 3 -> >
    // 4 -> =
    // 5 -> <=
    // 6 -> <

    public Filter() {
        this.option = "";
        this.value = "";
        this.comparator = 0;
        this.constant = 0.0;
    }

    public void setOption(String option) {
        this.option = option;
        if (option.equals("Charm Power") && comparator < 2) comparator = 2;
        else comparator = 0;
    }

    public String getOption() {
        return option;
    }

    public void incrementComparator() {
        comparator++;
        if (option.equals("Stat")) {
            if (comparator > 6) comparator = 0;
        }
        else if (option.equals("Charm Power")) {
            if (comparator > 6) comparator = 0;
            if (comparator < 2) comparator = 2;
        }
        else {
            if (comparator > 1) comparator = 0;
        }
    }
}
