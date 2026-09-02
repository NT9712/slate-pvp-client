package net.slate.module.impl;

import net.slate.module.Category;
import net.slate.module.Module;
import net.slate.module.setting.BoolSetting;
import net.slate.module.setting.NumberSetting;

/** Quality of life for the chat box. */
public class ChatModule extends Module {

	private static ChatModule INSTANCE;

	private final NumberSetting history = add(new NumberSetting("History", 250.0D, 100.0D, 1000.0D, 50.0D));
	private final BoolSetting stackSpam = add(new BoolSetting("Stack Duplicates", true));
	private final BoolSetting keepDraft = add(new BoolSetting("Keep Draft", true));

	public ChatModule() {
		super("Chat", "Longer history, duplicate stacking and a saved draft.", Category.MISC);
		INSTANCE = this;
	}

	public static int historySize() {
		ChatModule m = INSTANCE;
		return m == null || !m.isEnabled() ? 100 : m.history.getInt();
	}

	public static boolean stacksDuplicates() {
		ChatModule m = INSTANCE;
		return m != null && m.isEnabled() && m.stackSpam.get();
	}

	public static boolean keepsDraft() {
		ChatModule m = INSTANCE;
		return m != null && m.isEnabled() && m.keepDraft.get();
	}
}
