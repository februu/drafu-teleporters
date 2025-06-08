package dev.febru.drafuteleporters.client.gui;

import dev.febru.drafuteleporters.manager.TeleporterDataManager;
import dev.febru.drafuteleporters.payloads.TeleportRequestPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TPScreen extends Screen {
    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 16;
    private static final int DROPDOWN_HEIGHT = 20;
    private static final int SPACING = 30;

    // Enhanced options list - you can add many more here

    private List<TeleporterDataManager.TeleporterData> OPTIONS = new ArrayList<>();
    private TeleporterDataManager.TeleporterData selectedOption = null; // Changed: Initialize to null
    private boolean dropdownOpen = false;
    private int scrollOffset = 0;
    private final int maxVisibleItems = 6;

    private DropdownButton dropdownButton;

    public TPScreen(Text title, List<TeleporterDataManager.TeleporterData> teleporters) {
        super(title);
        OPTIONS = teleporters;
        // Set selectedOption safely
        if (!OPTIONS.isEmpty()) {
            selectedOption = OPTIONS.get(0);
        }
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;

        // Create custom dropdown button
        String buttonText = selectedOption != null ? selectedOption.name : "No teleporters available";
        this.dropdownButton = new DropdownButton(
                centerX - BUTTON_WIDTH / 2,
                startY,
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                Text.literal(buttonText)
        );

        // Create action button with enhanced styling
        ButtonWidget actionButton = ButtonWidget.builder(
                Text.literal("Teleport"),
                button -> onActionButtonPressed()
        ).dimensions(
                centerX - BUTTON_WIDTH / 2,
                startY + SPACING,
                BUTTON_WIDTH,
                BUTTON_HEIGHT + 4
        ).build();

        // Disable teleport button if no teleporters available
        if (selectedOption == null) {
            actionButton.active = false;
        }

        // Create cancel button
        ButtonWidget cancelButton = ButtonWidget.builder(
                Text.literal("Cancel"),
                button -> onCloseButtonPressed()
        ).dimensions(
                centerX - BUTTON_WIDTH / 2,
                startY + SPACING * 2,
                BUTTON_WIDTH,
                BUTTON_HEIGHT + 4
        ).build();

        this.addDrawableChild(actionButton);
        this.addDrawableChild(cancelButton);
        this.addDrawableChild(this.dropdownButton);
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        // Title
        Text title = Text.literal("Teleporter Hub");
        int titleX = (this.width - this.textRenderer.getWidth(title)) / 2;
        int titleY = this.height / 2 - 90;
        context.drawText(this.textRenderer, title, titleX, titleY, 0xFFFFFF, true);

        if (dropdownOpen) {
            super.renderBackground(context, mouseX, mouseY, delta);
            this.dropdownButton.render(context, mouseX, mouseY, delta);
            renderDropdown(context, mouseX, mouseY);
        } else {
            super.render(context, mouseX, mouseY, delta);
        }
    }

    private void renderDropdown(DrawContext context, int mouseX, int mouseY) {
        if (OPTIONS.isEmpty()) return; // Don't render dropdown if no options

        int dropdownX = this.dropdownButton.getX();
        int dropdownY = this.dropdownButton.getY() + this.dropdownButton.getHeight();
        int dropdownWidth = this.dropdownButton.getWidth();

        int visibleItems = Math.min(maxVisibleItems, OPTIONS.size());
        int actualDropdownHeight = visibleItems * DROPDOWN_HEIGHT;

        // Dropdown shadow
        context.fill(dropdownX + 1, dropdownY + 1, dropdownX + dropdownWidth + 1, dropdownY + actualDropdownHeight + 1, 0x80000000);

        // Dropdown background
        context.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + actualDropdownHeight, 0xF0333333);
        context.drawBorder(dropdownX, dropdownY, dropdownWidth, actualDropdownHeight, 0xFF666666);

        // Render visible options
        for (int i = 0; i < visibleItems; i++) {
            int optionIndex = i + scrollOffset;
            if (optionIndex >= OPTIONS.size()) break;

            TeleporterDataManager.TeleporterData option = OPTIONS.get(optionIndex);
            int optionY = dropdownY + i * DROPDOWN_HEIGHT;

            // Highlight hovered option
            if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth &&
                    mouseY >= optionY && mouseY <= optionY + DROPDOWN_HEIGHT) {
                context.fill(dropdownX, optionY, dropdownX + dropdownWidth, optionY + DROPDOWN_HEIGHT, 0x80555555);
            }

            // Highlight selected option
            if (option.equals(selectedOption)) {
                context.fill(dropdownX, optionY, dropdownX + dropdownWidth, optionY + DROPDOWN_HEIGHT, 0x4000FF00);
            }

            // Draw option text
            int textY = optionY + (DROPDOWN_HEIGHT - this.textRenderer.fontHeight) / 2;
            context.drawText(this.textRenderer, option.name, dropdownX + 6, textY+1, 0xFFFFFFFF, false);
        }

        // Draw scrollbar if needed
        if (OPTIONS.size() > maxVisibleItems) {
            int scrollbarX = dropdownX + dropdownWidth - 5;
            int scrollbarThumbHeight = Math.max(10, actualDropdownHeight * maxVisibleItems / OPTIONS.size());
            int scrollbarThumbY = dropdownY + (actualDropdownHeight - scrollbarThumbHeight) * scrollOffset / Math.max(1, OPTIONS.size() - maxVisibleItems);

            // Scrollbar track
            context.fill(scrollbarX, dropdownY + 1, scrollbarX + 4, dropdownY + actualDropdownHeight - 1, 0xFF222222);
            // Scrollbar thumb
            context.fill(scrollbarX, scrollbarThumbY, scrollbarX + 4, scrollbarThumbY + scrollbarThumbHeight, 0xFF888888);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            if (dropdownOpen && !OPTIONS.isEmpty()) {
                // Check if clicking on dropdown options
                int dropdownX = this.dropdownButton.getX();
                int dropdownY = this.dropdownButton.getY() + this.dropdownButton.getHeight();
                int dropdownWidth = this.dropdownButton.getWidth();
                int visibleItems = Math.min(maxVisibleItems, OPTIONS.size());
                int actualDropdownHeight = visibleItems * DROPDOWN_HEIGHT;

                if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth &&
                        mouseY >= dropdownY && mouseY <= dropdownY + actualDropdownHeight) {

                    int clickedIndex = (int) ((mouseY - dropdownY) / DROPDOWN_HEIGHT) + scrollOffset;
                    if (clickedIndex >= 0 && clickedIndex < OPTIONS.size()) {
                        selectedOption = OPTIONS.get(clickedIndex);
                        dropdownButton.setMessage(Text.literal(selectedOption.name));
                        onOptionChanged(selectedOption.name);
                    }
                }
                dropdownOpen = false;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (dropdownOpen && !OPTIONS.isEmpty()) {
            int dropdownX = this.dropdownButton.getX();
            int dropdownY = this.dropdownButton.getY() + this.dropdownButton.getHeight();
            int dropdownWidth = this.dropdownButton.getWidth();
            int visibleItems = Math.min(maxVisibleItems, OPTIONS.size());
            int actualDropdownHeight = visibleItems * DROPDOWN_HEIGHT;

            if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth &&
                    mouseY >= dropdownY && mouseY <= dropdownY + actualDropdownHeight) {

                scrollOffset = MathHelper.clamp(scrollOffset - (int) verticalAmount, 0, Math.max(0, OPTIONS.size() - maxVisibleItems));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void onOptionChanged(String newOption) {
    }

    private void onActionButtonPressed() {
        if (selectedOption == null) return; // Safety check

        System.out.println("Sending packet to server" + selectedOption);
        UUID playerUuid = MinecraftClient.getInstance().player.getUuid();
        ClientPlayNetworking.send(new TeleportRequestPayload(playerUuid, selectedOption, false));
        MinecraftClient.getInstance().player.setPitch(0.0F);
        this.close();
    }

    private void onCloseButtonPressed() {
        UUID playerUuid = MinecraftClient.getInstance().player.getUuid();
        ClientPlayNetworking.send(new TeleportRequestPayload(playerUuid, null, true));
        this.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // Custom dropdown button class
    private class DropdownButton extends ButtonWidget {
        public DropdownButton(int x, int y, int width, int height, Text message) {
            super(x, y, width, height, message, button -> {
                if (!OPTIONS.isEmpty()) { // Only open dropdown if there are options
                    dropdownOpen = !dropdownOpen;
                    scrollOffset = 0;
                }
            }, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            // Enhanced button rendering
            int color = this.isHovered() ? 0xFF4A4A4A : 0xFF333333;

            // Button background
            context.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), color);

            // Button border
            int borderColor = this.isHovered() ? 0xFF666666 : 0xFF555555;
            context.drawBorder(this.getX(), this.getY(), this.getWidth(), this.getHeight(), borderColor);

            // Button text
            int textColor = this.isHovered() ? 0xFFFFFFFF : 0xFFCCCCCC;
            int textX = this.getX() + 8;
            int textY = this.getY() + (this.getHeight() - 8) / 2 + 1;
            context.drawText(textRenderer, this.getMessage(), textX, textY, textColor, false);

            // Dropdown arrow (only show if there are options)
            if (!OPTIONS.isEmpty()) {
                String arrow = dropdownOpen ? "▲" : "▼";
                int arrowX = this.getX() + this.getWidth() - 16;
                context.drawText(textRenderer, arrow, arrowX, textY, textColor, false);
            }
        }
    }
}