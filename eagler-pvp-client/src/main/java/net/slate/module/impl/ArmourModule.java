package net.slate.module.impl;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.slate.hud.HudDraw;
import net.slate.module.HudModule;
import net.slate.module.HudModule.HAlign;
import net.slate.module.HudModule.VAlign;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.ModeSetting;
import net.slate.ui.Draw;
import net.slate.ui.Theme;

/** Armour pieces and the held item with their remaining durability. */
public final class ArmourModule extends HudModule {

	private static final int SLOT = 20;
	private static final int WIDE_SLOT = 26;

	private final ModeSetting show = add(new ModeSetting("Show", 0, "Percent", "Value", "None"));
	private final BoolSetting heldItem = add(new BoolSetting("Held Item", true));
	private final BoolSetting durabilityBar = add(new BoolSetting("Durability Bar", true));

	private final ItemStack[] stacks = new ItemStack[5];

	public ArmourModule() {
		super("Durability", "Durability of your armour and weapon; full items stay quiet.", HAlign.RIGHT, VAlign.BOTTOM, 6, 6);
	}


	@Override
	public void updateLayout() {
		int slots = heldItem.get() ? 5 : 4;
		int pitch = pitch();
		setSize(slots * pitch - (pitch - 16), 16 + (show.is("None") ? 0 : 8));
	}

	@Override
	public void render(float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}
		ItemStack[] stacks = this.stacks;
		int count = heldItem.get() ? 5 : 4;
		for (int i = 0; i < 4; i++) {
			stacks[i] = mc.player.inventory.armorInventory.get(3 - i);
		}
		stacks[4] = heldItem.get() ? mc.player.getHeldItemMainhand() : ItemStack.EMPTY;

		ItemRenderer ir = mc.getItemRenderer();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		RenderHelper.enableGUIStandardItemLighting();
		ir.zLevel = 100.0F;
		for (int i = 0; i < count; i++) {
			ItemStack stack = stacks[i];
			if (stack == null || stack.isEmpty()) {
				continue;
			}
			ir.renderItemAndEffectIntoGUI(mc.player, stack, i * pitch(), 0);
			if (durabilityBar.get()) {
				ir.renderItemOverlays(mc.fontRenderer, stack, i * pitch(), 0);
			}
		}
		ir.zLevel = 0.0F;
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableDepthTest();
		GlStateManager.enableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.enableTexture();
		GlStateManager.color4f(1f, 1f, 1f, 1f);

		if (show.is("None")) {
			return;
		}
		for (int i = 0; i < count; i++) {
			ItemStack stack = stacks[i];
			if (stack == null || stack.isEmpty() || stack.getMaxDamage() <= 0) {
				continue;
			}
			int max = stack.getMaxDamage();
			int left = max - stack.getDamage();
			int percent = (int) (left * 100L / max);
			if (percent >= 100) {
				continue;
			}
			String text = show.is("Percent") ? (percent + "%") : Integer.toString(left);
			Draw.textSmallOutlined(text, i * pitch() + 8f - Draw.smallWidth(text) * 0.5f, 17f, colour(percent));
		}
	}

	/** Small text fits under a 16px icon; when it cannot be scaled the slots spread out instead. */
	private static int pitch() {
		return Draw.smallHeight() < Draw.TEXT_H ? SLOT : WIDE_SLOT;
	}

	/** Full items recede, the one that is about to break stands out. */
	private static int colour(int percent) {
		if (percent > 50) {
			return net.slate.hud.HudDraw.LABEL;
		}
		return percent > 20 ? Theme.WARN : Theme.BAD;
	}
}
