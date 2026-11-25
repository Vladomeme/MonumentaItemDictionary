package dev.eliux.monumentaitemdictionary.mixin;

import dev.eliux.monumentaitemdictionary.Mid;
import dev.eliux.monumentaitemdictionary.util.*;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Mixin(Style.class)
public abstract class StyleMixin implements StyleDuck {

    @Shadow
    public abstract @Nullable HoverEvent getHoverEvent();

    @Unique private Stats mid$stats;
    @Unique private final DecimalFormat formatter = new DecimalFormat("0.##") {{setRoundingMode(RoundingMode.DOWN);}};

    @Override
    public void mid$setStats(Stats stats) {
        this.mid$stats = stats;
    }

    @Override
    public Stats mid$getStats() {
        return mid$stats;
    }

    @Override
    public void mid$drawModifiedTooltip(DrawContext context, TextRenderer tr, int x, int y) {
        Mid.controller.lastTooltipSource = (Style) (Object) this;

        assert getHoverEvent() != null;
        Text sourceText = getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);

        List<OrderedText> text = new ArrayList<>(tr.wrapLines(sourceText, Math.max(context.getScaledWindowWidth() / 2, 200)));
        if (!Screen.hasShiftDown()) {
            text.add(OrderedText.EMPTY);
            text.add(Text.literal("Hold [Shift] to display stats").setStyle(Styles.RESET_BOLD_TEXT).asOrderedText());
            text.add(Text.literal("[Shift] + Click to open the Builder").setStyle(Styles.RESET_BOLD_TEXT).asOrderedText());
        }
        else {
            text.add(OrderedText.EMPTY);
            text.add(mid$agilityArmor());

            text.addAll(mid$health());

            text.addAll(mid$misc());

            text.add(OrderedText.EMPTY);
            text.add(Text.literal("Effective HP").setStyle(Styles.LIGHT_GRAY.withUnderline(true))
                    .append(Text.literal(" (Damage Reduction)").setStyle(Styles.TEXT.withUnderline(false))).asOrderedText());
            text.addAll(mid$defenceStats());

            text.addAll(mid$meleeLines());
            text.addAll(mid$projectileLines());
            text.addAll(mid$magicLines());

            text.add(OrderedText.EMPTY);

        }
        context.drawOrderedTooltip(tr, text, x, y);
    }

    @Unique
    private OrderedText mid$agilityArmor() {
        StringBuilder builder = new StringBuilder();

        if (mid$stats.agility > 0) {
            builder.append(mid$format(mid$stats.agility)).append(" Agility");
            if (mid$stats.armor > 0) builder.append(" - ").append(mid$format(mid$stats.armor)).append(" Armor");
        }
        else if (mid$stats.armor > 0) {
            builder.append(mid$format(mid$stats.armor)).append(" Armor");
        }
        else builder.append("0 Agility/Armor");

        return Text.literal(builder.toString()).setStyle(Styles.LIGHT_GRAY).asOrderedText();
    }

    //Health, Healing Rate
    //Regeneration, Life Drain
    @Unique
    private List<OrderedText> mid$health() {
        List<OrderedText> list = new ArrayList<>();
        list.add(OrderedText.EMPTY);

        MutableText text = Text.literal(mid$format(mid$stats.healthFinal)).setStyle(Styles.LIGHT_GRAY)
                .append(Text.literal(" Health").setStyle(Styles.RED));
        if (mid$stats.healingRate.perc != 100)
            text.append(Text.literal(", " + mid$format(mid$stats.healingRate.perc) + "% Healing Rate").setStyle(Styles.LIGHT_GRAY));
        list.add(text.asOrderedText());

        if (mid$stats.regenPerSec > 0) {
            MutableText text2 = Text.literal("+" + mid$format(mid$stats.regenPerSec)).setStyle(Styles.LIGHT_GRAY)
                    .append(Text.literal(" ♥").setStyle(Styles.RED))
                    .append(Text.literal("/s").setStyle(Styles.LIGHT_GRAY));
            if (mid$stats.lifeDrainOnCrit > 0) {
                text2.append(Text.literal(", +" + mid$format(mid$stats.lifeDrainOnCrit)).setStyle(Styles.LIGHT_GRAY)
                        .append(Text.literal(" ♥").setStyle(Styles.RED))
                        .append(Text.literal(" Life Drain/Crit").setStyle(Styles.LIGHT_GRAY)));
            }
            list.add(text2.asOrderedText());
        }
        else {
            if (mid$stats.lifeDrainOnCrit > 0) {
                list.add(Text.literal(mid$format(mid$stats.lifeDrainOnCrit)).setStyle(Styles.LIGHT_GRAY)
                        .append(Text.literal(" ♥").setStyle(Styles.RED))
                        .append(Text.literal(" Life Drain / Crit").setStyle(Styles.LIGHT_GRAY)).asOrderedText());
            }
        }
        return list;
    }

    //Speed, Knockback Resistance
    //Thorns
    //Fire Tick Damage
    @Unique
    private List<OrderedText> mid$misc() {
        List<OrderedText> list = new ArrayList<>();
        list.add(OrderedText.EMPTY);
        list.add(Text.literal(mid$format(mid$stats.speedPercent.perc) + "% ").setStyle(Styles.LIGHT_GRAY)
                .append(Text.literal("Speed").setStyle(Styles.BLUE))
                .append(Text.literal(", " + mid$format(mid$stats.knockbackRes.perc) + "% ").setStyle(Styles.LIGHT_GRAY))
                .append(Text.literal("Knockback Resistance").setStyle(Styles.BROWN)).asOrderedText());
        if (mid$stats.thorns > 0)
            list.add(Text.literal(mid$format(mid$stats.thorns)).setStyle(Styles.LIGHT_GRAY)
                    .append(Text.literal(" Thorns Damage").setStyle(Styles.YELLOW)).asOrderedText());
        if (mid$stats.fireTickDamage > 1)
            list.add(Text.literal(mid$format(mid$stats.fireTickDamage)).setStyle(Styles.LIGHT_GRAY)
                    .append(Text.literal(" Fire Tick Damage").setStyle(Styles.YELLOW)).asOrderedText());
        return list;
    }

    @Unique
    private List<OrderedText> mid$defenceStats() {
        List<OrderedText> list = new ArrayList<>();
        list.add(mid$defenceLine("Melee",   mid$stats.meleeEHP,      mid$stats.meleeDR));
        list.add(mid$defenceLine("Proj",    mid$stats.projectileEHP, mid$stats.projectileDR));
        list.add(mid$defenceLine("Magic",   mid$stats.magicEHP,      mid$stats.magicDR));
        list.add(mid$defenceLine("Blast",   mid$stats.blastEHP,      mid$stats.blastDR));
        list.add(mid$defenceLine("Fire",    mid$stats.fireEHP,       mid$stats.fireDR));
        list.add(mid$defenceLine("Fall",    mid$stats.fallEHP,       mid$stats.fallDR));
        list.add(mid$defenceLine("Ailment", mid$stats.ailmentEHP,    mid$stats.ailmentDR));
        return list;
    }

    @Unique
    private OrderedText mid$defenceLine(String type, double ehp, Percentage dr) {
        return Text.literal(type + ": ").setStyle(Styles.LIGHT_GRAY)
                .append(Text.literal(mid$format(ehp) + " ").setStyle(Styles.TEXT))
                .append(Text.literal("(" + mid$format(dr.perc) + "%)").setStyle(Styles.TEXT)).asOrderedText();
    }

    @Unique
    private List<OrderedText> mid$meleeLines() {
        if (mid$stats.attackDamage == 1) return List.of();

        List<OrderedText> list = new ArrayList<>();
        list.add(OrderedText.EMPTY);
        list.add(Text.literal("\uD83E\uDE93 ").setStyle(Styles.RED)
                .append(Text.literal(mid$format(mid$stats.attackDamage) + " Melee Damage").setStyle(Styles.LIGHT_GRAY))
                .append(Text.literal(" (+" + mid$format(mid$stats.attackDamagePercent.perc - 100) + "%)").setStyle(Styles.TEXT)).asOrderedText());
        list.add(Text.literal("\uD83E\uDE93 ").setStyle(Styles.RED)
                .append(Text.literal(mid$format(mid$stats.attackSpeed) + " Attack Speed").setStyle(Styles.LIGHT_GRAY)).asOrderedText());
        return list;
    }

    @Unique
    private List<OrderedText> mid$projectileLines() {
        if (mid$stats.projectileDamage == 0) return List.of();

        List<OrderedText> list = new ArrayList<>();
        list.add(OrderedText.EMPTY);
        list.add(Text.literal("\uD83C\uDFF9 ").setStyle(Styles.BLUE)
                .append(Text.literal(mid$format(mid$stats.projectileDamage) + " Projectile Damage").setStyle(Styles.LIGHT_GRAY))
                .append(Text.literal(" (+" + mid$format(mid$stats.projectileDamagePercent.perc - 100) + "%)").setStyle(Styles.TEXT)).asOrderedText());
        if (mid$stats.throwRate > 0)
            list.add(Text.literal("\uD83C\uDFF9 ").setStyle(Styles.BLUE)
                    .append(Text.literal(mid$format(mid$stats.throwRate) + " Throw Rate").setStyle(Styles.LIGHT_GRAY)).asOrderedText());
        return list;
    }

    @Unique
    private List<OrderedText> mid$magicLines() {
        if (mid$stats.spellPowerPercent.perc == 100 && mid$stats.magicDamagePercent.perc == 100 && mid$stats.potionDamage == 0) return List.of();

        List<OrderedText> list = new ArrayList<>();
        list.add(OrderedText.EMPTY);
        if (mid$stats.spellPowerPercent.perc != 100 || mid$stats.magicDamagePercent.perc != 100) {
            list.add(Text.literal("✨ ").setStyle(Styles.YELLOW)
                    .append(Text.literal(mid$format(mid$stats.spellDamage.perc) + "% Spell Damage").setStyle(Styles.LIGHT_GRAY))
                    .append(Text.literal(" (+" + mid$format(mid$stats.magicDamagePercent.perc - 100) + "%)").setStyle(Styles.TEXT)).asOrderedText());
        }
        if (mid$stats.spellCooldownPercent.perc != 100)
            list.add(Text.literal("✨ ").setStyle(Styles.YELLOW)
                    .append(Text.literal(mid$format(mid$stats.spellCooldownPercent.perc) + "% Cooldown Duration").setStyle(Styles.LIGHT_GRAY)).asOrderedText());
        if (mid$stats.potionDamage > 0)
            list.add(Text.literal("✨ ").setStyle(Styles.YELLOW)
                    .append(Text.literal(mid$format(mid$stats.potionDamage) + " Potion Damage").setStyle(Styles.LIGHT_GRAY)).asOrderedText());
        return list;
    }

    @Unique
    private String mid$format(double d) {
        return formatter.format(d);
    }
}
