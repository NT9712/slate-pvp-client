package net.slate.module.setting;

public final class BoolSetting extends Setting {

	private boolean value;
	public final boolean defaultValue;

	public BoolSetting(String name, boolean value) {
		super(name);
		this.value = value;
		this.defaultValue = value;
	}

	public boolean get() {
		return value;
	}

	public void set(boolean v) {
		this.value = v;
	}

	public void toggle() {
		this.value = !this.value;
	}

	public String write() {
		return value ? "true" : "false";
	}

	public void read(String v) {
		this.value = "true".equals(v);
	}

	public void reset() {
		this.value = defaultValue;
	}
}
