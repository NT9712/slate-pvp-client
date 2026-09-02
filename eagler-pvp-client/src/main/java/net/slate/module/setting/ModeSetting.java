package net.slate.module.setting;

public final class ModeSetting extends Setting {

	private final String[] modes;
	private int index;
	public final int defaultIndex;

	public ModeSetting(String name, int index, String... modes) {
		super(name);
		this.modes = modes;
		this.index = index;
		this.defaultIndex = index;
	}

	public String get() {
		return modes[index];
	}

	public int index() {
		return index;
	}

	public String[] modes() {
		return modes;
	}

	public boolean is(String mode) {
		return modes[index].equals(mode);
	}

	public void cycle(int dir) {
		index = (index + dir + modes.length) % modes.length;
	}

	public void set(int i) {
		if (i >= 0 && i < modes.length) {
			index = i;
		}
	}

	public String write() {
		return modes[index];
	}

	public void read(String v) {
		for (int i = 0; i < modes.length; i++) {
			if (modes[i].equals(v)) {
				index = i;
				return;
			}
		}
	}

	public void reset() {
		index = defaultIndex;
	}
}
