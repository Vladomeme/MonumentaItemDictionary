package dev.eliux.monumentaitemdictionary.gui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import dev.eliux.monumentaitemdictionary.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class DropdownWidget extends TextFieldWidget {

    private final TextRenderer textRenderer;

    private ChoiceProvider choiceProvider;
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

        this.choiceProvider = new ListBasedChoiceProvider(choices);
        this.defaultText = defaultText;
        updateMaxShown();
    }

    @SuppressWarnings("unchecked")
    public void setChoices(Object newChoices) {
        if (newChoices instanceof List) {
            if (choiceProvider instanceof MapBasedChoiceProvider)
                choiceProvider = new ListBasedChoiceProvider((List<String>) newChoices);
            else
                choiceProvider.setChoices(newChoices);
        }
        else if (newChoices instanceof Map) {
            if (choiceProvider instanceof ListBasedChoiceProvider)
                choiceProvider = new MapBasedChoiceProvider((Map<String, String>) newChoices);
            else
                choiceProvider.setChoices(newChoices);
        }
    }

    public void setChoices(List<String> newChoices, List<String> newVisualChoices) {
        choiceProvider.setChoices(newChoices, newVisualChoices);
    }

    public void setDefaultText(String newDefaultText) {
        defaultText = newDefaultText;
    }

    public String getLastChoice() {
        return lastChoice;
    }

    private void updateMaxShown() {
        if (MinecraftClient.getInstance().currentScreen == null) return;
        maxShown = (int) ((double) (MinecraftClient.getInstance().currentScreen.height - (getY() + height)) / (double) (height + 1));
    }

    private void updateScrollLimits() {
        if (scrollAmount > validChoices.size() - maxShown) scrollAmount = validChoices.size() - maxShown;
        if (scrollAmount < 0) scrollAmount = 0;
    }

    // must be called manually
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) || isFocused()) {
            setFocused(true);
            for (int i = 0; i < Math.min(validChoices.size(), maxShown); i ++) {
                if (mouseX >= getX() && mouseX <= getX() + width && mouseY >= getY() + height + ((height + 1) * i) && mouseY < getY() + height + ((height + 1) * (i + 1))) {
                    lastChoice = validChoices.get(i + scrollAmount);
                    visualLastChoice = visualValidChoices.get(i + scrollAmount);
                    onUpdate.accept(lastChoice);
                    setFocused(false);

                    playDownSound(MinecraftClient.getInstance().getSoundManager());
                }
            }
        }

        if (isFocused()) {
            setText("");
            choiceProvider.resetValidChoices();
            visualValidChoices = new ArrayList<>(visualChoices);
        }
        else setText(visualLastChoice);

        if (!isFocused() || !(mouseX >= getX()) || !(mouseX <= getX() + width) || !(mouseY >= getY()) || !(mouseY <= getY() + height)) {
            super.mouseClicked(mouseX, mouseY, button);
        }

        return true;
    }

    public boolean willClick(double mouseX, double mouseY) {
        return isFocused() && (mouseX >= getX() && mouseX <= getX() + width && mouseY >= getX() + height) && mouseY < getY() + height + ((height + 1) * (validChoices.size() + 1));
    }

    // must be called manually
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        choiceProvider.updateShownChoices();
        return true;
    }

    // must be called manually
    @Override
    public boolean charTyped(char chr, int modifiers) {
        super.charTyped(chr, modifiers);
        choiceProvider.updateShownChoices();
        return true;
    }

    // must be called manually
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        super.mouseScrolled(mouseX, mouseY, hAmount, vAmount);

        if (mouseX >= getX() && mouseX <= getX() + width && mouseY >= getY() && mouseY <= getY() + ((height + 1) * (Math.min(validChoices.size(), maxShown) + 1))) {
            // mouse is in scroll area
            if (validChoices.size() > maxShown) {
                // should be able to scroll
                scrollAmount -= (int) vAmount;
                updateScrollLimits();
            }
        }

        return true;
    }

    // must be called manually
    // called at normal render
    public void renderMain(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (lastChoice.isEmpty() && !isFocused()) context.drawTextWithShadow(textRenderer, defaultText, getX() + 3, getY() + 3, Colors.DROPDOWN_DEFAULT_TEXT);
    }

    // must be called manually
    // called after other render calls
    public void renderDropdown(DrawContext context, int mouseX, int mouseY) {
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 200);
        if (isFocused() && !validChoices.isEmpty()) {
            context.fill(getX() - 1, getY() + height, getX() + width + 1, getY() + height + ((height + 1) * Math.min(validChoices.size(), maxShown)) + 1, Colors.DROPDOWN_BORDER);
            context.fill(getX(), getY() + height + 1, getX() + width, getY() + height + ((height + 1) * Math.min(validChoices.size(), maxShown)), Colors.BLACK);

            // draw highlight under mouse
            for (int i = 0; i < Math.min(validChoices.size(), maxShown); i ++) {
                if (mouseX >= getX() && mouseX <= getX() + width && mouseY >= getY() + height + ((height + 1) * i) && mouseY < getY() + height + ((height + 1) * (i + 1))) {
                    context.fill(getX(), getY() + height + ((height + 1) * i) + 1, getX() + width, getY() + height + ((height + 1) * (i + 1)), Colors.DROPDOWN_HIGHLIGHT);
                }
            }

            // draw dividing lines
            for (int i = 0; i < Math.min(validChoices.size(), maxShown) - 1; i ++) {
                context.drawHorizontalLine(getX() + 3, getX() + width - 4, getY() + height + ((height + 1) * (i + 1)), Colors.DROPDOWN_DIVIDER);
            }

            // draw choice text
            for (int i = 0; i < Math.min(visualValidChoices.size(), maxShown); i ++) {
                String finalText;
                if (textRenderer.getWidth(visualValidChoices.get(i + scrollAmount)) > width - 8)
                    finalText = textRenderer.trimToWidth(visualValidChoices.get(i + scrollAmount), width - 14) + "...";
                else
                    finalText = visualValidChoices.get(i + scrollAmount);

                context.drawTextWithShadow(textRenderer, finalText, getX() + 4, getY() + height + ((height + 1) * i) + 4, Colors.WHITE);
            }

            // draw scroll bar if needed
            if (validChoices.size() > maxShown) {
                context.drawVerticalLine(getX() + width - 1, getY() + height,  getY() + ((height + 1) * (maxShown + 1)) - 1, Colors.DROPDOWN_SCROLLBAR_BG);
                int scrollBarPixels = ((height + 1) * (maxShown + 1)) - 1 - height;
                context.drawVerticalLine(getX() + width - 1, getY() + height + (int) (scrollBarPixels * ((double) scrollAmount / validChoices.size())),
                        getY() + height + (int) (scrollBarPixels * ((double) (scrollAmount + maxShown) / validChoices.size())), Colors.DROPDOWN_SCROLLBAR);
            }
        }
        context.getMatrices().pop();
    }

    interface ChoiceProvider {
        void setChoices(Object source);
        void setChoices(List<String> newChoices, List<String> newVisualChoices);
        void resetValidChoices();
        void updateShownChoices();
    }

    class ListBasedChoiceProvider implements ChoiceProvider {

        List<String> choices;

        ListBasedChoiceProvider(List<String> source) {
            choices = source;
            visualChoices = source;
            lastChoice = "";
            visualLastChoice = "";
            validChoices = new ArrayList<>(choices);
            visualValidChoices = new ArrayList<>(visualChoices);
            setText(visualLastChoice);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void setChoices(Object source) {
            List<String> newChoices = (List<String>) source;
            setChoices(newChoices, newChoices);
        }

        @Override
        public void setChoices(List<String> newChoices, List<String> newVisualChoices) {
            choices = newChoices;
            visualChoices = newVisualChoices;
            lastChoice = "";
            visualLastChoice = "";
            setText(visualLastChoice);
        }

        @Override
        public void resetValidChoices() {
            validChoices = new ArrayList<>(choices);
        }

        @Override
        public void updateShownChoices() {
            validChoices.clear();
            visualValidChoices.clear();
            if (isFocused()) {
                if (getText().isEmpty()) {
                    validChoices = new ArrayList<>(choices);
                    visualValidChoices = new ArrayList<>(visualChoices);
                }
                else {
                    for (String choice : visualChoices) {
                        if (choice.toLowerCase().contains(getText().toLowerCase())) {
                            validChoices.add(choices.get(visualChoices.indexOf(choice)));
                            visualValidChoices.add(choice);
                        }
                    }
                }
            }
            updateScrollLimits();
        }
    }

    class MapBasedChoiceProvider implements ChoiceProvider {

        Map<String, String> choices;

        MapBasedChoiceProvider(Map<String, String> source) {
            choices = source;
            visualChoices = new ArrayList<>(source.values());
            lastChoice = "";
            visualLastChoice = "";
            validChoices = new ArrayList<>(source.values());
            visualValidChoices = new ArrayList<>(visualChoices);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void setChoices(Object source) {
            Map<String, String> newChoices = (Map<String, String>) source;
            choices = newChoices;
            visualChoices = new ArrayList<>(newChoices.values());
            lastChoice = "";
            visualLastChoice = "";
            setText(visualLastChoice);
        }

        //never used
        @Override
        public void setChoices(List<String> newChoices, List<String> newVisualChoices) {

        }

        @Override
        public void resetValidChoices() {
            validChoices = new ArrayList<>(choices.values());
        }

        @Override
        public void updateShownChoices() {
            validChoices.clear();
            visualValidChoices.clear();
            if (isFocused()) {
                if (getText().isEmpty()) {
                    validChoices = new ArrayList<>(choices.values());
                    visualValidChoices = new ArrayList<>(visualChoices);
                }
                else {
                    for (Map.Entry<String, String> entry : choices.entrySet()) {
                        String input = getText().toLowerCase();
                        if (entry.getKey().toLowerCase().contains(input) || entry.getValue().toLowerCase().contains(input)) {
                            validChoices.add(entry.getValue());
                            visualValidChoices.add(entry.getValue());
                        }
                    }
                }
            }
            updateScrollLimits();
        }
    }
}
