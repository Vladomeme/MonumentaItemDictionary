package dev.eliux.monumentaitemdictionary.util;

public class Percentage {
    public double perc;
    public double val;
    public Percentage(double value, boolean perc) {
        if (perc) {
            this.perc = value;
            this.val = value / 100;
        }
        else {
            this.perc = value * 100;
            this.val = value;
        }
    }

    public Percentage mulP(Percentage percentage) {
        this.perc *= percentage.val;
        this.val *= percentage.val;
        return this;
    }

    public Percentage add(double value, boolean percent) {
        if (percent) {
            this.perc += value;
            this.val += value / 100;
        }
        else {
            this.perc += value*100;
            this.val += value;
        }
        return this;
    }

    public Percentage mul(double value, boolean percent) {
        if (percent) {
            this.perc *= value/100;
            this.val *= value/100;
        }
        else {
            this.perc *= value;
            this.val *= value;
        }
        return this;
    }

    public Percentage duplicate() {
        return new Percentage(this.perc, true);
    }
}
