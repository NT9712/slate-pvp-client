package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import net.lax1dude.eaglercraft.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.slate.ui.FlatButton;
import net.slate.ui.Draw;
import net.slate.ui.Theme;

@OnlyIn(Dist.CLIENT)
public class IngameMenuScreen extends Screen {
    private static final ResourceLocation FRIENDS_TEX = new ResourceLocation("textures/gui/friends.png");
    private final boolean isFullMenu;
    private float panelAnim = 0f;

    public IngameMenuScreen(boolean p_i51519_1_) {
        super(p_i51519_1_ ? new TranslationTextComponent("menu.game") : new TranslationTextComponent("menu.paused"));
        this.isFullMenu = p_i51519_1_;
    }

    protected void init() {
        if (this.isFullMenu) {
            this.addButtons();
        }
    }

    private void addButtons() {
        int centerX = this.width / 2;
        int btnW = 240;
        int btnH = 40;
        int spacing = 12;
        int startY = this.height / 2 - (7 * (btnH + spacing)) / 2;
        
        this.addButton(new FlatButton(centerX - btnW / 2, startY + 0 * (btnH + spacing), btnW, btnH,
            I18n.format("menu.returnToGame"), false, (b) -> {
                this.mc.displayGuiScreen((Screen) null);
                this.mc.setIngameFocus();
            }));
        
        this.addButton(new FlatButton(centerX - btnW / 2, startY + 1 * (btnH + spacing), btnW, btnH,
            I18n.format("gui.advancements"), false, (b) -> {
                this.mc.displayGuiScreen(new AdvancementsScreen(this.mc.player.connection.getAdvancementManager()));
            }));
        
        this.addButton(new FlatButton(centerX - btnW / 2, startY + 2 * (btnH + spacing), btnW, btnH,
            I18n.format("gui.stats"), false, (b) -> {
                this.mc.displayGuiScreen(new StatsScreen(this, this.mc.player.getStats()));
            }));

        if (net.lax1dude.eaglercraft.PauseMenuCustomizeState.serverInfoMode != net.lax1dude.eaglercraft.PauseMenuCustomizeState.SERVER_INFO_MODE_NONE) {
            this.addButton(new FlatButton(centerX - btnW / 2, startY + 3 * (btnH + spacing), btnW, btnH,
                net.lax1dude.eaglercraft.PauseMenuCustomizeState.serverInfoButtonText, false, (b) -> {
                    if (net.lax1dude.eaglercraft.PauseMenuCustomizeState.serverInfoMode == net.lax1dude.eaglercraft.PauseMenuCustomizeState.SERVER_INFO_MODE_EXTERNAL_URL && 
                        net.lax1dude.eaglercraft.PauseMenuCustomizeState.serverInfoURL != null) {
                        net.lax1dude.eaglercraft.EagRuntime.openLink(net.lax1dude.eaglercraft.PauseMenuCustomizeState.serverInfoURL);
                    }
                }));
        }

        this.addButton(new FlatButton(centerX - btnW / 2, startY + 4 * (btnH + spacing), btnW, btnH,
            I18n.format("menu.options"), false, (b) -> {
                this.mc.displayGuiScreen(new OptionsScreen(this, this.mc.gameSettings));
            }));

        Button discordBtn = this.addButton(new FlatButton(centerX - btnW / 2, startY + 5 * (btnH + spacing), btnW, btnH,
            I18n.format("Invite"), false, (b) -> {
                if (net.lax1dude.eaglercraft.PauseMenuCustomizeState.discordButtonMode == net.lax1dude.eaglercraft.PauseMenuCustomizeState.DISCORD_MODE_INVITE_URL && 
                    net.lax1dude.eaglercraft.PauseMenuCustomizeState.discordInviteURL != null) {
                    net.lax1dude.eaglercraft.EagRuntime.openLink(net.lax1dude.eaglercraft.PauseMenuCustomizeState.discordInviteURL);
                } else {
                    mc.displayGuiScreen(new ShareToLanScreen(this));
                }
            }));

        if (net.lax1dude.eaglercraft.PauseMenuCustomizeState.discordButtonMode != net.lax1dude.eaglercraft.PauseMenuCustomizeState.DISCORD_MODE_NONE) {
            discordBtn.setMessage(net.lax1dude.eaglercraft.PauseMenuCustomizeState.discordButtonText);
        } else {
            discordBtn.active = this.mc.isSingleplayer() && !(this.mc.getIntegratedServer() != null && this.mc.getIntegratedServer().getPublic());
        }

        this.addButton(new FlatButton(centerX - btnW / 2, startY + 6 * (btnH + spacing), btnW, btnH,
            I18n.format(this.mc.isIntegratedServerRunning() ? "menu.returnToMenu" : "menu.disconnect"), false, true, (b) -> {
                boolean flag = this.mc.isIntegratedServerRunning();
                b.active = false;
                if (flag) {
                    this.mc.scheduleWorldUnload(new DirtMessageScreen(new TranslationTextComponent("menu.savingLevel")), new MainMenuScreen());
                    this.mc.world.sendQuittingDisconnectingPacket();
                    if (flag) {
                        this.mc.shutdownIntegratedServer(new MainMenuScreen());
                    } else {
                        this.mc.shutdownIntegratedServer(new MultiplayerScreen(new MainMenuScreen()));
                    }
                } else {
                    this.mc.world.sendQuittingDisconnectingPacket();
                    this.mc.func_213254_o();
                    this.mc.displayGuiScreen(new MultiplayerScreen(new MainMenuScreen()));
                }
            }));

        if (mc.gameSettings.socialFeatures && mc.isSingleplayer()) {
            this.addButton(new ImageButton(this.width / 2 - 10, startY + 7 * (btnH + spacing) + 4, 20, 20,
                0, 0, 0, FRIENDS_TEX, 16, 16,
                (b) -> {
                    this.mc.displayGuiScreen(new net.eymenwsmc.friends.FriendsOverlayScreen(this));
                }, I18n.format("socials.friends")) {
                @Override
                public void renderButton(int mouseX, int mouseY, float partialTicks) {
                    Minecraft mc = Minecraft.getInstance();
                    mc.getTextureManager().bindTexture(Button.WIDGETS_LOCATION);
                    int i = this.getYImage(this.isHovered());
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.alpha);
                    GlStateManager.enableBlend();
                    GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                    this.blit(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
                    this.blit(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);

                    mc.getTextureManager().bindTexture(FRIENDS_TEX);
                    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    this.blit(this.x + 2, this.y + 2, 0, 0, 16, 16, 16, 16);

                    if (net.eymenwsmc.network.NetworkHandler.pendingRequests.size() > 0 || net.eymenwsmc.network.NetworkHandler.pendingJoinRequests.size() > 0) {
                        int dotX = this.x + this.width - 5;
                        int dotY = this.y + 1;
                        Draw.rect(dotX, dotY, dotX + 5, dotY + 5, 0xFFFF3333);
                        Draw.rect(dotX + 1, dotY + 1, dotX + 4, dotY + 4, 0xFFFF5555);
                    }

                    if (this.isHovered()) {
                        IngameMenuScreen.this.renderTooltip(I18n.format("socials.friends"), mouseX, mouseY);
                    }
                }
            });
        }
    }

    public void tick() {
        net.eymenwsmc.network.NetworkHandler.tick();
        super.tick();
        panelAnim = Draw.approach(panelAnim, 1f, 0.25f, net.slate.Slate.delta());
        if (org.lwjgl.input.Mouse.isActuallyGrabbed()) {
            org.lwjgl.input.Mouse.setGrabbed(false);
            this.mc.mouseHelper.ungrabMouse();
        }
    }

    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        // Dark backdrop
        Draw.rect(0, 0, this.width, this.height, Theme.BACKDROP);
        
        if (this.isFullMenu) {
            // Centered panel
            int panelW = 400;
            int panelH = 440;
            int panelX = (this.width - panelW) / 2;
            int panelY = (this.height - panelH) / 2;
            
            // Panel background with subtle animation
            float anim = panelAnim;
            int drawX = panelX;
            int drawY = panelY + (int) ((1f - anim) * 20);
            int alpha = (int) (anim * 255);
            
            Draw.roundRect(drawX, drawY, panelW, panelH, Theme.RADIUS_LG, Theme.alpha(Theme.PANEL, alpha / 255f));
            Draw.outline(drawX, drawY, panelW, panelH, Theme.RADIUS_LG, Theme.alpha(Theme.BORDER, alpha / 255f));
            
            // Title
            Draw.textCentred(this.title.getFormattedText(), this.width / 2f, drawY + 24, Theme.alpha(Theme.TEXT, alpha / 255f));
            
            // Render buttons
            for (int i = 0; i < this.buttons.size(); ++i) {
                this.buttons.get(i).render(p_render_1_, p_render_2_, p_render_3_);
            }
        } else {
            // Minimal pause overlay
            Draw.textCentred(this.title.getFormattedText(), this.width / 2f, 20, Theme.TEXT);
            Draw.textCentred("Press Escape to resume", this.width / 2f, 40, Theme.TEXT_DIM);
        }
        
        super.render(p_render_1_, p_render_2_, p_render_3_);
    }
}