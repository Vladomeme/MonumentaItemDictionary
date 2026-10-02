package dev.eliux.monumentaitemdictionary.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class DropdownWidget extends TextFieldWidget {
    private final TextRenderer textRenderer;

    private List<String> choices;
    private List<String> visualChoices;
    private ArrayList<String> validChoices;
    private ArrayList<String> visualValidChoices;
    private String lastChoice;
    private String visualLastChoice;
    private String defaultText;
    private int maxShown;
    private int scrollAmount;

    private final Consumer<String> onUpdate;

    public DropdownWidget(TextRenderer textRenderer, int x, int y, int width, Text text, String defaultText, List<String> choices, Consumer<String> onUpdate) {
        super(textRenderer, x, y, width, 14, text);
        this.textRenderer = textRenderer;
        this.onUpdate = onUpdate;

        this.choices = choices;
        this.visualChoices = choices;
        this.defaultText = defaultText;
        lastChoice = "";
        visualLastChoice = "";
        setText(visualLastChoice);
        validChoices = new ArrayList<>(choices);
        visualValidChoices = new ArrayList<>(visualChoices);
        updateMaxShown();
    }

    public DropdownWidget(TextRenderer textRenderer, int x, int y, int width, Text text, String defaultText, List<String> choices, List<String> visualChoices, Consumer<String> onUpdate) {
        super(textRenderer, x, y, width, 14, text);
        this.textRenderer = textRenderer;
        this.onUpdate = onUpdate;

        this.choices = choices;
        this.visualChoices = visualChoices;
        this.defaultText = defaultText;
        lastChoice = "";
        visualLastChoice = "";
        setText(visualLastChoice);
        validChoices = new ArrayList<>(choices);
        visualValidChoices = new ArrayList<>(visualChoices);
        updateMaxShown();
    }

    public void setChoices(List<String> newChoices) {
        setChoices(newChoices, newChoices);
    }

    public void setChoices(List<String> newChoices, List<String> newVisualChoices) {
        if (newChoices.size() != newVisualChoices.size()) throw new IllegalArgumentException("Dropdown labels must match choices");
        choices = List.copyOf(newChoices);
        visualChoices = List.copyOf(newVisualChoices);
        lastChoice = "";
        visualLastChoice = "";
        setText("");
        validChoices = new ArrayList<>(choices);
        visualValidChoices = new ArrayList<>(visualChoices);
        scrollAmount = 0;
        updateMaxShown();
    }

    public void setDefaultText(String newDefaultText) {
        defaultText = newDefaultText;
    }

    public String getLastChoice() {
        return lastChoice;
    }

    private void updateShownChoices() {
        validChoices.clear();
        visualValidChoices.clear();
        if (isFocused()) {
            if (getText().isEmpty()) {
                validChoices = new ArrayList<>(choices);
                visualValidChoices = new ArrayList<>(visualChoices);
            } else {
                for (int i = 0; i < visualChoices.size(); i++) {
                    String choice = visualChoices.get(i);
                    if (choice.toLowerCase(java.util.Locale.ROOT).contains(getText().toLowerCase(java.util.Locale.ROOT))) {
                        validChoices.add(choices.get(i));
                        visualValidChoices.add(choice);
                    }
                }
            }
        }
        updateScrollLimits();
    }

    private void updateMaxShown() {
        if (MinecraftClient.getInstance().currentScreen == null) return;
        maxShown = Math.max(0, (MinecraftClient.getInstance().currentScreen.height - getY() - height - 1) / (height + 1));
    }

    private void updateScrollLimits() {
        if (scrollAmount > validChoices.size() - maxShown) scrollAmount = validChoices.size() - maxShown;
        if (scrollAmount < 0) scrollAmount = 0;
    }

    // must be called manually
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        updateMaxShown();
        updateScrollLimits();
        if (willClick(mouseX, mouseY)) {
            int index = (int) ((mouseY - getY() - height) / (height + 1)) + scrollAmount;
            lastChoice = validChoices.get(index);
            visualLastChoice = visualValidChoices.get(index);
            setFocused(false);
            setText(visualLastChoice);
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            onUpdate.accept(lastChoice);
            return true;
        }
        if (isMouseOver(mouseX, mouseY)) {
            if (!isFocused()) {
                setFocused(true);
                setText("");
                scrollAmount = 0;
                updateShownChoices();
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        setFocused(false);
        setText(visualLastChoice);
        return false;
    }

    public boolean willClick(double mouseX, double mouseY) {
        return isFocused() && mouseX >= getX() && mouseX < getX() + width
                && mouseY >= getY() + height
                && mouseY < getY() + height + (height + 1) * Math.min(validChoices.size(), maxShown);
    }

    // must be called manually
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        updateShownChoices();
        return true;
    }

    // must be called manually
    @Override
    public boolean charTyped(char chr, int modifiers) {
        super.charTyped(chr, modifiers);
        updateShownChoices();
        return true;
    }

    // must be called manually
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);

        if (mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= this.getY() && mouseY <= this.getY() + ((this.height + 1) * (Math.min(validChoices.size(), maxShown) + 1))) {
            // mouse is in scroll area
            if (validChoices.size() > maxShown) {
                // should be able to scroll
                scrollAmount -= vAmount;
                updateScrollLimits();
            }
        }

        return true;
    }

    // must be called manually
    public void resize(MinecraftClient client, int width, int height) {
        updateMaxShown();
        updateScrollLimits();
    }

    // must be called manually
    // called at normal render
    public void renderMain(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (lastChoice.isEmpty() && !isFocused()) context.drawTextWithShadow(textRenderer, defaultText, this.getX() + 3, this.getY() + 3, 0x666666);
    }

    // must be called manually
    // called after other render calls
    public void renderDropdown(DrawContext context, int mouseX, int mouseY, float delta) {
        updateMaxShown();
        updateScrollLimits();
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 200);
        if (this.isFocused() && !validChoices.isEmpty() && maxShown > 0) {
            context.fill(this.getX() - 1, this.getY() + this.height, this.getX() + this.width + 1, this.getY() + this.height + ((this.height + 1) * Math.min(validChoices.size(), maxShown)) + 1, 0xFFA0A0A0);
            context.fill(this.getX(), this.getY() + this.height + 1, this.getX() + this.width, this.getY() + this.height + ((this.height + 1) * Math.min(validChoices.size(), maxShown)), 0xFF000000);

            // draw highlight under mouse
            for (int i = 0; i < Math.min(validChoices.size(), maxShown); i ++) {
                if (mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= this.getY() + this.height + ((this.height + 1) * i) && mouseY < this.getY() + this.height + ((this.height + 1) * (i + 1))) {
                    context.fill(this.getX(), this.getY() + this.height + ((this.height + 1) * i) + 1, this.getX() + this.width, this.getY() + this.height + ((this.height + 1) * (i + 1)), 0xFF212121);
                }
            }

            // draw dividing lines
            for (int i = 0; i < Math.min(validChoices.size(), maxShown) - 1; i ++) {
                context.drawHorizontalLine(this.getX() + 3, this.getX() + this.width - 4, this.getY() + this.height + ((this.height + 1) * (i + 1)), 0xFFA0A0A0);
            }

            // draw choice text
            for (int i = 0; i < Math.min(visualValidChoices.size(), maxShown); i ++) {
                String finalText;
                if (textRenderer.getWidth(visualValidChoices.get(i + scrollAmount)) > this.width - 8) {
                    finalText = textRenderer.trimToWidth(visualValidChoices.get(i + scrollAmount), this.width - 14) + "...";
                } else {
                    finalText = visualValidChoices.get(i + scrollAmount);
                }
                //String finalText = textRenderer.trimToWidth(validChoices.get(i), this.width - 8);
                context.drawTextWithShadow(textRenderer, finalText, this.getX() + 4, this.getY() + this.height + ((this.height + 1) * i) + 4, 0xFFFFFFFF);
            }

            // draw scroll bar if needed
            if (validChoices.size() > maxShown) {
                context.drawVerticalLine(this.getX() + this.width - 1, this.getY() + this.height,  this.getY() + ((this.height + 1) * (maxShown + 1)) - 1, 0xFF303030);
                int scrollBarPixels = ((this.height + 1) * (maxShown + 1)) - 1 - this.height;
                context.drawVerticalLine(this.getX() + this.width - 1, this.getY() + this.height + (int)(scrollBarPixels * ((double)scrollAmount / validChoices.size())), this.getY() + this.height + (int)(scrollBarPixels * ((double)(scrollAmount + maxShown) / validChoices.size())), 0xFF505050);
            }
        }
        context.getMatrices().pop();
    }
}
