package net.minecraft.client.gui.screen;

import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.AccessibilityScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.LockIconButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.AbstractOption;
import net.lax1dude.eaglercraft.recording.GuiScreenRecordingNote;
import net.lax1dude.eaglercraft.recording.GuiScreenRecordingSettings;
import net.lax1dude.eaglercraft.recording.ScreenRecordingController;
import net.minecraft.network.play.client.CLockDifficultyPacket;
import net.minecraft.network.play.client.CSetDifficultyPacket;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.slate.ui.FlatButton;
import net.slate.ui.Draw;
import net.slate.ui.Theme;
import net.slate.Slate;
import com.mojang.blaze3d.platform.GlStateManager;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class OptionsScreen extends Screen {
    private static final AbstractOption[] SCREEN_OPTIONS = new AbstractOption[]{AbstractOption.FOV};
    
    private final Screen lastScreen;
    private final GameSettings settings;
    private Button difficultyButton;
    private LockIconButton lockButton;
    private Difficulty field_213062_f;
    private Button recordingButton;
    
    // CS2-style sidebar
    private int selectedCategory = 0;
    private final List<Category> categories = new ArrayList<>();
    private float sidebarAnim = 0f;
    private float contentAnim = 0f;
    
    private static class Category {
        final String name;
        final String key;
        final Runnable onSelect;
        
        Category(String name, String key, Runnable onSelect) {
            this.name = name;
            this.key = key;
            this.onSelect = onSelect;
        }
    }

    public OptionsScreen(Screen p_i1046_1_, GameSettings p_i1046_2_) {
        super(new TranslationTextComponent("options.title"));
        this.lastScreen = p_i1046_1_;
        this.settings = p_i1046_2_;
    }

    protected void init() {
        setupCategories();
        
        // If we were in a sub-screen, return to last selected category
        if (lastScreen instanceof OptionsScreen) {
            this.selectedCategory = ((OptionsScreen) lastScreen).selectedCategory;
        }
    }
    
    private void setupCategories() {
        categories.clear();
        
        categories.add(new Category(I18n.format("options.video"), "video", () -> {
            this.mc.displayGuiScreen(new VideoSettingsScreen(this, this.settings));
        }));
        
        categories.add(new Category(I18n.format("options.controls"), "controls", () -> {
            this.mc.displayGuiScreen(new ControlsScreen(this, this.settings));
        }));
        
        categories.add(new Category(I18n.format("options.sounds"), "audio", () -> {
            this.mc.displayGuiScreen(new OptionsSoundsScreen(this, this.settings));
        }));
        
        categories.add(new Category("Performance", "performance", () -> {
            this.mc.displayGuiScreen(new PerformanceSettingsScreen(this, this.settings));
        }));
        
        categories.add(new Category(I18n.format("options.skinCustomisation"), "skin", () -> {
            this.mc.displayGuiScreen(new CustomizeSkinScreen(this));
        }));
        
        categories.add(new Category(I18n.format("options.language"), "language", () -> {
            this.mc.displayGuiScreen(new LanguageScreen(this, this.settings, this.mc.getLanguageManager()));
        }));
        
        categories.add(new Category(I18n.format("options.chat.title"), "chat", () -> {
            this.mc.displayGuiScreen(new ChatOptionsScreen(this, this.settings));
        }));
        
        categories.add(new Category(I18n.format("options.resourcepack"), "resourcepacks", () -> {
            this.mc.displayGuiScreen(new ResourcePacksScreen(this));
        }));
        
        categories.add(new Category(I18n.format("options.accessibility.title"), "accessibility", () -> {
            this.mc.displayGuiScreen(new AccessibilityScreen(this, this.settings));
        }));
        
        // Difficulty category (only in world)
        if (this.mc.world != null) {
            categories.add(0, new Category("Difficulty", "difficulty", null));
        }
    }

    @Override
    public void tick() {
        sidebarAnim = Draw.approach(sidebarAnim, 1f, 0.2f, Slate.delta());
        contentAnim = Draw.approach(contentAnim, 1f, 0.15f, Slate.delta());
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        // Render backdrop
        Draw.rect(0, 0, width, height, Theme.BACKDROP);
        
        // Layout constants
        final int sidebarW = 220;
        final int pad = Theme.PAD;
        final int topBarH = 56;
        final int contentX = sidebarW;
        final int contentY = topBarH;
        final int contentW = width - sidebarW;
        final int contentH = height - topBarH;
        
        // Top bar
        Draw.rect(0, 0, width, topBarH, Theme.PANEL);
        Draw.rect(0, topBarH - 1, width, 1, Theme.DIVIDER);
        
        // Title
        Draw.text(this.title.getFormattedText(), pad, (topBarH - Draw.TEXT_H) / 2f, Theme.TEXT);
        
        // Close button (top right)
        int closeBtnSize = 32;
        int closeX = width - closeBtnSize - pad;
        int closeY = (topBarH - closeBtnSize) / 2;
        boolean closeHover = mouseX >= closeX && mouseX <= closeX + closeBtnSize && 
                           mouseY >= closeY && mouseY <= closeY + closeBtnSize;
        if (closeHover) {
            Draw.rect(closeX, closeY, closeBtnSize, closeBtnSize, Theme.ROW_HOVER);
        }
        Draw.textCentred("×", closeX + closeBtnSize / 2f, closeY + (closeBtnSize - Draw.TEXT_H) / 2f + 2f, 
                        closeHover ? Theme.ACCENT : Theme.TEXT_DIM);
        
        // Sidebar background
        Draw.rect(0, topBarH, sidebarW, height - topBarH, Theme.PANEL);
        Draw.rect(sidebarW - 1, topBarH, 1, height - topBarH, Theme.DIVIDER);
        
        // Sidebar categories
        int btnH = 40;
        int btnY = topBarH + pad;
        for (int i = 0; i < categories.size(); i++) {
            Category cat = categories.get(i);
            boolean selected = i == selectedCategory;
            boolean hovered = !selected && mouseX >= pad && mouseX <= sidebarW - pad && 
                            mouseY >= btnY && mouseY <= btnY + btnH;
            
            // Background
            int bgColor;
            if (selected) {
                bgColor = Theme.ACCENT_MUTED;
            } else if (hovered) {
                bgColor = Theme.ROW_HOVER;
            } else {
                bgColor = 0x00000000;
            }
            
            if (bgColor != 0x00000000) {
                Draw.roundRect(pad, btnY, sidebarW - 2 * pad, btnH, Theme.RADIUS_SM, bgColor);
            }
            
            // Selection indicator
            if (selected) {
                Draw.rect(pad, btnY, 3, btnH, Theme.ACCENT);
            }
            
            // Text
            Draw.text(cat.name, pad + 12 + (selected ? 3 : 0), btnY + (btnH - Draw.TEXT_H) / 2f, 
                     selected ? Theme.ACCENT : (hovered ? Theme.TEXT : Theme.TEXT_DIM));
            
            btnY += btnH + 4;
        }
        
        // Done button at bottom of sidebar
        int doneBtnY = height - pad - 40;
        boolean doneHover = mouseX >= pad && mouseX <= sidebarW - pad && 
                          mouseY >= doneBtnY && mouseY <= doneBtnY + 36;
        Draw.roundRect(pad, doneBtnY, sidebarW - 2 * pad, 36, Theme.RADIUS_SM, 
                      doneHover ? Theme.ROW_HOVER : 0x00000000);
        if (doneHover) {
            Draw.outline(pad, doneBtnY, sidebarW - 2 * pad, 36, Theme.RADIUS_SM, Theme.BORDER);
        }
        Draw.textCentred(I18n.format("gui.done"), sidebarW / 2f, doneBtnY + (36 - Draw.TEXT_H) / 2f + 1f,
                        doneHover ? Theme.ACCENT : Theme.TEXT);
        
        // Content panel
        Draw.rect(contentX, contentY, contentW, contentH, Theme.PANEL_LIGHT);
        
        // Render selected category content
        renderCategoryContent(categories.get(selectedCategory), contentX, contentY, contentW, contentH, mouseX, mouseY, partialTicks);
        
        // Handle click detection for sidebar
        if (mouseX < sidebarW && mouseY > topBarH) {
            // Sidebar clicks handled in mouseClicked
        }
        
        super.render(mouseX, mouseY, partialTicks);
    }
    
    private void renderCategoryContent(Category cat, int x, int y, int w, int h, int mouseX, int mouseY, float partialTicks) {
        if ("difficulty".equals(cat.key)) {
            renderDifficultyPanel(x, y, w, h, mouseX, mouseY);
        } else if ("performance".equals(cat.key)) {
            renderPerformancePanel(x, y, w, h, mouseX, mouseY);
        } else {
            // Placeholder for categories that open sub-screens
            renderPlaceholderPanel(x, y, w, h, cat.name);
        }
    }
    
    private void renderDifficultyPanel(int x, int y, int w, int h, int mouseX, int mouseY) {
        int pad = Theme.PAD;
        int contentX = x + pad;
        int contentY = y + pad;
        int contentW = w - 2 * pad;
        
        // Section title
        Draw.text("World Difficulty", contentX, contentY, Theme.TEXT);
        contentY += Draw.TEXT_H + 12;
        
        // Difficulty buttons
        String[] difficulties = {"Peaceful", "Easy", "Normal", "Hard"};
        int btnW = (contentW - 3 * 8) / 4;
        int btnH = 36;
        
        for (int i = 0; i < difficulties.length; i++) {
            int btnX = contentX + i * (btnW + 8);
            boolean selected = field_213062_f != null && field_213062_f.getId() == i;
            boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && 
                            mouseY >= contentY && mouseY <= contentY + btnH;
            boolean canChange = this.mc.world != null && !this.mc.world.getWorldInfo().isHardcore();
            
            int bgColor = selected ? Theme.ACCENT_MUTED : (hovered && canChange ? Theme.ROW_HOVER : Theme.OFF);
            Draw.roundRect(btnX, contentY, btnW, btnH, Theme.RADIUS_SM, bgColor);
            
            if (selected) {
                Draw.outline(btnX, contentY, btnW, btnH, Theme.RADIUS_SM, Theme.ACCENT);
            } else if (hovered && canChange) {
                Draw.outline(btnX, contentY, btnW, btnH, Theme.RADIUS_SM, Theme.BORDER_LIGHT);
            }
            
            Draw.textCentred(difficulties[i], btnX + btnW / 2f, contentY + (btnH - Draw.TEXT_H) / 2f + 1f,
                           selected ? Theme.ACCENT : (hovered ? Theme.TEXT : Theme.TEXT_DIM));
        }
        
        // Lock button
        if (lockButton != null) {
            // Position lock button next to selected difficulty
        }
        
        contentY += btnH + 20;
        
        // Hardcore warning
        if (this.mc.world != null && this.mc.world.getWorldInfo().isHardcore()) {
            Draw.text("Difficulty is locked on Hardcore mode", contentX, contentY, Theme.TEXT_MUTED);
        } else if (!canChangeDifficulty()) {
            Draw.text("Difficulty cannot be changed on this server", contentX, contentY, Theme.TEXT_MUTED);
        }
    }
    
    private void renderPerformancePanel(int x, int y, int w, int h, int mouseX, int mouseY) {
        int pad = Theme.PAD;
        int contentX = x + pad;
        int contentY = y + pad;
        int contentW = w - 2 * pad;
        
        Draw.text("Performance Settings", contentX, contentY, Theme.TEXT);
        contentY += Draw.TEXT_H + 16;
        
        // Button to open full performance settings
        int btnW = 200;
        int btnH = 36;
        int btnX = contentX;
        boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && 
                        mouseY >= contentY && mouseY <= contentY + btnH;
        
        Draw.roundRect(btnX, contentY, btnW, btnH, Theme.RADIUS_SM, 
                      hovered ? Theme.ACCENT_MUTED : Theme.OFF);
        if (hovered) {
            Draw.outline(btnX, contentY, btnW, btnH, Theme.RADIUS_SM, Theme.ACCENT);
        }
        Draw.textCentred("Open Performance Settings", btnX + btnW / 2f, contentY + (btnH - Draw.TEXT_H) / 2f + 1f,
                        hovered ? Theme.ACCENT : Theme.TEXT);
    }
    
    private void renderPlaceholderPanel(int x, int y, int w, int h, String title) {
        int pad = Theme.PAD;
        int contentX = x + pad;
        int contentY = y + pad;
        
        Draw.text(title, contentX, contentY, Theme.TEXT);
        contentY += Draw.TEXT_H + 16;
        
        Draw.text("Configure this in the dedicated settings screen.", contentX, contentY, Theme.TEXT_DIM);
        contentY += Draw.TEXT_H + 8;
        
        // Button to open sub-screen
        Category cat = categories.get(selectedCategory);
        if (cat.onSelect != null) {
            int btnW = 200;
            int btnH = 36;
            int btnX = contentX;
            // Note: hover detection would need mouseX/mouseY passed
            Draw.roundRect(btnX, contentY, btnW, btnH, Theme.RADIUS_SM, Theme.ACCENT_MUTED);
            Draw.textCentred("Open " + title + " Settings", btnX + btnW / 2f, contentY + (btnH - Draw.TEXT_H) / 2f + 1f,
                           Theme.ACCENT);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        
        final int sidebarW = 220;
        final int topBarH = 56;
        final int pad = Theme.PAD;
        
        // Close button
        int closeBtnSize = 32;
        int closeX = width - closeBtnSize - pad;
        int closeY = (topBarH - closeBtnSize) / 2;
        if (mouseX >= closeX && mouseX <= closeX + closeBtnSize && 
            mouseY >= closeY && mouseY <= closeY + closeBtnSize) {
            this.mc.displayGuiScreen(this.lastScreen);
            return true;
        }
        
        // Sidebar category clicks
        int btnH = 40;
        int btnY = topBarH + pad;
        for (int i = 0; i < categories.size(); i++) {
            if (mouseX >= pad && mouseX <= sidebarW - pad && 
                mouseY >= btnY && mouseY <= btnY + btnH) {
                this.selectedCategory = i;
                if (categories.get(i).onSelect != null) {
                    // Don't auto-navigate, let user click button in content
                }
                return true;
            }
            btnY += btnH + 4;
        }
        
        // Done button
        int doneBtnY = height - pad - 40;
        if (mouseX >= pad && mouseX <= sidebarW - pad && 
            mouseY >= doneBtnY && mouseY <= doneBtnY + 36) {
            this.mc.displayGuiScreen(this.lastScreen);
            return true;
        }
        
        // Content area clicks
        if (mouseX >= sidebarW) {
            Category cat = categories.get(selectedCategory);
            if ("difficulty".equals(cat.key)) {
                handleDifficultyClick(mouseX, mouseY, sidebarW);
            } else if ("performance".equals(cat.key)) {
                handlePerformanceClick(mouseX, mouseY, sidebarW);
            } else if (cat.onSelect != null) {
                handleSubScreenClick(mouseX, mouseY, sidebarW, cat);
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void handleDifficultyClick(double mouseX, double mouseY, int sidebarW) {
        if (!canChangeDifficulty()) return;
        
        int pad = Theme.PAD;
        int contentX = sidebarW + pad;
        int contentY = 56 + pad + Draw.TEXT_H + 12; // After title
        int btnW = (width - sidebarW - 2 * pad - 3 * 8) / 4;
        int btnH = 36;
        
        for (int i = 0; i < 4; i++) {
            int btnX = contentX + i * (btnW + 8);
            if (mouseX >= btnX && mouseX <= btnX + btnW && 
                mouseY >= contentY && mouseY <= contentY + btnH) {
                this.field_213062_f = Difficulty.byId(i);
                if (this.mc.getConnection() != null) {
                    this.mc.getConnection().sendPacket(new CSetDifficultyPacket(this.field_213062_f));
                }
                if (difficultyButton != null) {
                    difficultyButton.setMessage(getDifficultyText(this.field_213062_f));
                }
                return;
            }
        }
    }
    
    private void handlePerformanceClick(double mouseX, double mouseY, int sidebarW) {
        int pad = Theme.PAD;
        int contentX = sidebarW + pad;
        int contentY = 56 + pad + Draw.TEXT_H + 16;
        int btnW = 200;
        int btnH = 36;
        int btnX = contentX;
        
        if (mouseX >= btnX && mouseX <= btnX + btnW && 
            mouseY >= contentY && mouseY <= contentY + btnH) {
            this.mc.displayGuiScreen(new PerformanceSettingsScreen(this, this.settings));
        }
    }
    
    private void handleSubScreenClick(double mouseX, double mouseY, int sidebarW, Category cat) {
        int pad = Theme.PAD;
        int contentX = sidebarW + pad;
        int contentY = 56 + pad + Draw.TEXT_H + 16 + Draw.TEXT_H + 8;
        int btnW = 200;
        int btnH = 36;
        int btnX = contentX;
        
        if (mouseX >= btnX && mouseX <= btnX + btnW && 
            mouseY >= contentY && mouseY <= contentY + btnH) {
            cat.onSelect.run();
        }
    }
    
    private boolean canChangeDifficulty() {
        return this.mc.world != null && 
               (this.mc.isSingleplayer() && !this.mc.world.getWorldInfo().isHardcore()) &&
               this.mc.getConnection() != null;
    }

    public String getDifficultyText(Difficulty p_175355_1_) {
        return (new TranslationTextComponent("options.difficulty")).appendText(": ").appendSibling(p_175355_1_.getDisplayName()).getFormattedText();
    }

    private void func_213050_a(boolean p_213050_1_) {
        this.mc.displayGuiScreen(this);
        if (p_213050_1_ && this.mc.world != null) {
            this.mc.getConnection().sendPacket(new CLockDifficultyPacket(true));
            this.lockButton.setLocked(true);
            this.lockButton.active = false;
            this.difficultyButton.active = false;
        }
    }

    @Override
    public void removed() {
        this.settings.saveOptions();
    }
    
    @Override
    public void renderBackground() {
        // Override to prevent vanilla background
    }
}