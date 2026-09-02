package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.eymenwsmc.CompletableFuture;
import net.eymenwsmc.Util;
import net.eymenwsmc.gui.UpdateOverlay;
import net.eymenwsmc.network.NetworkHandler;
import net.eymenwsmc.socials.GuiSocialInfoScreen;
import net.eymenwsmc.socials.GuiSocialLoginScreen;
import net.lax1dude.eaglercraft.*;
import net.lax1dude.eaglercraft.profile.GuiScreenEditProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.RenderSkybox;
import net.minecraft.client.renderer.RenderSkyboxCube;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.resources.IResource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.StringUtils;
;import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.SaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executor;

@OnlyIn(Dist.CLIENT)
public class MainMenuScreen extends Screen {
    public static final RenderSkyboxCube PANORAMA_RESOURCES = new RenderSkyboxCube(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY_TEXTURES = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation ACCESSIBILITY_TEXTURES = new ResourceLocation("textures/gui/friends.png");
    private final boolean showTitleWronglySpelled;
    private static final ResourceLocation SPLASH_TEXTS = new ResourceLocation("texts/splashes.txt");
    private EaglercraftRandom random = new EaglercraftRandom();
    private String splashText;
    private Button buttonResetDemo;

    private MainMenuScreen.WarningDisplay openGLWarning1;
    private static final ResourceLocation MINECRAFT_TITLE_TEXTURES = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation MINECRAFT_TITLE_EDITION = new ResourceLocation("textures/gui/title/edition.png");
    private boolean hasCheckedForRealmsNotification;
    private int widthCopyright;
    private int widthCopyrightRest;
    private final RenderSkybox panorama = new RenderSkybox(PANORAMA_RESOURCES);
    private final boolean showFadeInAnimation;
    private long firstRenderTime;

    // === Update system ===
    private boolean updateAvailable = false;
    private String updateDownloadUrl = null;
    private UpdateOverlay updateOverlay;
    private boolean updateCheckRequested = false;

    public MainMenuScreen() {
        this(false);
    }

    public MainMenuScreen(boolean fadeIn) {
        super(new TranslationTextComponent("narrator.screen.title"));
        this.showFadeInAnimation = fadeIn;
        this.showTitleWronglySpelled = (double) (new Random()).nextFloat() < 1.0E-4D;
        if (!GLX.supportsOpenGL2()) {
            this.openGLWarning1 = new MainMenuScreen.WarningDisplay((new TranslationTextComponent("title.oldgl.eol.line1")).applyTextStyle(TextFormatting.RED).applyTextStyle(TextFormatting.BOLD), (new TranslationTextComponent("title.oldgl.eol.line2")).applyTextStyle(TextFormatting.RED).applyTextStyle(TextFormatting.BOLD), "https://help.mojang.com/customer/portal/articles/325948?ref=game");
        }

    }



    public void tick() {
        NetworkHandler.tick(); // Process incoming WebSocket messages (version check, etc.)

        if (!updateCheckRequested) {
            if (NetworkHandler.isConnected()) {
                // Already connected — request version info now
                NetworkHandler.requestVersionCheck();
                updateCheckRequested = true;
            } else if (!NetworkHandler.isConnecting) {
                // Not connected yet — start connecting so we can check version
                NetworkHandler.connect();
            }
        }

        // Sync overlay state from version info
        if (NetworkHandler.versionCheckDone) {
            boolean hasUpdate = Util.checkForUpdates();
            updateAvailable = hasUpdate;
            updateDownloadUrl = NetworkHandler.latestDownloadUrl;
            if (this.updateOverlay != null) {
                this.updateOverlay.setUpdateAvailable(hasUpdate);
            }
        }
    }

    public static CompletableFuture<Void> loadAsync(TextureManager texMngr, Executor backgroundExecutor) {
        return CompletableFuture.allOf(texMngr.loadAsync(MINECRAFT_TITLE_TEXTURES, backgroundExecutor), texMngr.loadAsync(MINECRAFT_TITLE_EDITION, backgroundExecutor), texMngr.loadAsync(PANORAMA_OVERLAY_TEXTURES, backgroundExecutor), PANORAMA_RESOURCES.loadAsync(texMngr, backgroundExecutor));
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected void init() {
        this.widthCopyright = this.font.getStringWidth("Copyright Mojang AB. Do not distribute!");
        this.widthCopyrightRest = this.width - this.widthCopyright - 2;

        int y = this.height / 2 + 6;
        this.addButton(new net.slate.ui.FlatButton(this.width / 2 - 100, y, 200, 22,
                I18n.format("menu.singleplayer"), false, (b) -> {
            this.mc.displayGuiScreen(new WorldSelectionScreen(this));
        }));
        this.addButton(new net.slate.ui.FlatButton(this.width / 2 - 100, y + 26, 200, 22,
                I18n.format("menu.multiplayer"), true, (b) -> {
            this.mc.displayGuiScreen(new MultiplayerScreen(this));
        }));
        this.addButton(new net.slate.ui.FlatButton(this.width / 2 - 100, y + 56, 98, 22,
                I18n.format("menu.options"), false, (b) -> {
            this.mc.displayGuiScreen(new OptionsScreen(this, this.mc.gameSettings));
        }));
        this.addButton(new net.slate.ui.FlatButton(this.width / 2 + 2, y + 56, 98, 22,
                "Profile", false, (b) -> {
            this.mc.displayGuiScreen(new GuiScreenEditProfile(new MainMenuScreen()));
        }));

        if (this.openGLWarning1 != null) {
            this.openGLWarning1.init(y);
        }
    }

    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        GlStateManager.enableAlphaTest();
        GlStateManager.enableBlend();
        if (this.firstRenderTime == 0L && this.showFadeInAnimation) {
            this.firstRenderTime = net.minecraft.util.Util.milliTime();
        }

        float f = this.showFadeInAnimation ? (float) (net.minecraft.util.Util.milliTime() - this.firstRenderTime) / 1000.0F : 1.0F;
        fill(0, 0, this.width, this.height, -1);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        this.panorama.render(p_render_3_, MathHelper.clamp(f, 0.0F, 1.0F));
        int i = 274;
        int j = this.width / 2 - 137;
        int k = 30;
        this.mc.getTextureManager().bindTexture(PANORAMA_OVERLAY_TEXTURES);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.showFadeInAnimation ? (float) MathHelper.ceil(MathHelper.clamp(f, 0.0F, 1.0F)) : 1.0F);
        blit(0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        float f1 = this.showFadeInAnimation ? MathHelper.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        int l = MathHelper.ceil(f1 * 255.0F) << 24;
        if ((l & -67108864) != 0) {
            // a soft scrim so the wordmark and buttons read cleanly over the panorama
            fill(0, 0, this.width, this.height, (MathHelper.ceil(f1 * 150.0F) << 24));

            float scale = this.width < 420 ? 2.0F : 3.0F;
            float wordmarkY = this.height / 2.0F - 52.0F;
            net.slate.Branding.drawWordmark(this.width / 2.0F, wordmarkY, scale);


            this.drawString(this.font, net.slate.Slate.NAME + " " + net.slate.Slate.VERSION, 2,
                    this.height - 10, 0x8A94A3 | l);
            this.drawString(this.font, "Minecraft " + SharedConstants.getVersion().getName(), 2,
                    this.height - 20, 0x666E7A | l);
            this.drawString(this.font, "Copyright Mojang AB. Do not distribute!", this.widthCopyrightRest, this.height - 10, 16777215 | l);
            if (p_render_1_ > this.widthCopyrightRest && p_render_1_ < this.widthCopyrightRest + this.widthCopyright && p_render_2_ > this.height - 10 && p_render_2_ < this.height) {
                fill(this.widthCopyrightRest, this.height - 1, this.widthCopyrightRest + this.widthCopyright, this.height, 16777215 | l);
            }

            if (this.openGLWarning1 != null) {
                this.openGLWarning1.render(l);
            }

            for (Widget widget : this.buttons) {
                widget.setAlpha(f1);
            }

            // Render update overlay if available
            if (this.updateOverlay != null) {
                this.updateOverlay.render(p_render_1_, p_render_2_, p_render_3_);
            }

            super.render(p_render_1_, p_render_2_, p_render_3_);

        }
    }

    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        if (super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
            return true;
        } else if (this.openGLWarning1 != null && this.openGLWarning1.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_)) {
            return true;
        } else {
            if (p_mouseClicked_1_ > (double) this.widthCopyrightRest && p_mouseClicked_1_ < (double) (this.widthCopyrightRest + this.widthCopyright) && p_mouseClicked_3_ > (double) (this.height - 10) && p_mouseClicked_3_ < (double) this.height) {
            }

            return false;
        }
    }

    public void removed() {

    }

    private void deleteDemoWorld(boolean p_213087_1_) {
        if (p_213087_1_) {
            SaveFormat saveformat = this.mc.getSaveLoader();
            saveformat.deleteWorldDirectory("Demo_World");
        }

        this.mc.displayGuiScreen(this);
    }

    @OnlyIn(Dist.CLIENT)
    class WarningDisplay {
        private int secondLineWidth;
        private int left;
        private int top;
        private int right;
        private int bottom;
        private final ITextComponent firstLine;
        private final ITextComponent secondLine;
        private final String onClickURL;

        public WarningDisplay(ITextComponent line1, ITextComponent line2, String url) {
            this.firstLine = line1;
            this.secondLine = line2;
            this.onClickURL = url;
        }

        public void init(int yIn) {
            int i = MainMenuScreen.this.font.getStringWidth(this.firstLine.getString());
            this.secondLineWidth = MainMenuScreen.this.font.getStringWidth(this.secondLine.getString());
            int j = Math.max(i, this.secondLineWidth);
            this.left = (MainMenuScreen.this.width - j) / 2;
            this.top = yIn - 24;
            this.right = this.left + j;
            this.bottom = this.top + 24;
        }

        public void render(int alpha) {
            AbstractGui.fill(this.left - 2, this.top - 2, this.right + 2, this.bottom - 1, 1428160512);
            MainMenuScreen.this.drawString(MainMenuScreen.this.font, this.firstLine.getFormattedText(), this.left, this.top, 16777215 | alpha);
            MainMenuScreen.this.drawString(MainMenuScreen.this.font, this.secondLine.getFormattedText(), (MainMenuScreen.this.width - this.secondLineWidth) / 2, this.top + 12, 16777215 | alpha);
        }

        public boolean mouseClicked(double mouseX, double p_223418_3_) {
            if (!StringUtils.isNullOrEmpty(this.onClickURL) && mouseX >= (double) this.left && mouseX <= (double) this.right && p_223418_3_ >= (double) this.top && p_223418_3_ <= (double) this.bottom) {
                MainMenuScreen.this.mc.displayGuiScreen(new ConfirmOpenLinkScreen((p_223421_1_) -> {
                    if (p_223421_1_) {
                        net.minecraft.util.Util.getOSType().openURI(this.onClickURL);
                    }

                    MainMenuScreen.this.mc.displayGuiScreen(MainMenuScreen.this);
                }, this.onClickURL, true));
                return true;
            } else {
                return false;
            }
        }
    }
}
