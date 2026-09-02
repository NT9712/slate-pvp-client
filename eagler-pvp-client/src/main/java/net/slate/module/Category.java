package net.slate.module;

/** Top level grouping shown in the client menu. */
public enum Category {

	COMBAT("Combat"),
	HUD("HUD"),
	VISUAL("Visual"),
	PLAYER("Player"),
	PERFORMANCE("Performance"),
	MISC("Misc");

	public final String label;

	Category(String label) {
		this.label = label;
	}
}
