package net.slate.module.setting;

/** Base class for a single configurable value on a module. */
public abstract class Setting extends net.slate.module.Animated {

	public final String name;
	private Visibility visibility;

	protected Setting(String name) {
		this.name = name;
	}

	/** Only show this setting in the menu when the supplied condition holds. */
	public Setting visibleWhen(Visibility v) {
		this.visibility = v;
		return this;
	}

	public boolean isVisible() {
		return visibility == null || visibility.visible();
	}

	/** Serialised form, must round-trip through {@link #read(String)}. */
	public abstract String write();

	public abstract void read(String value);

	/** Puts the setting back to the value it shipped with. */
	public abstract void reset();

	public interface Visibility {
		boolean visible();
	}
}
