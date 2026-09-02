package net.slate.module.setting;

import net.lax1dude.eaglercraft.HString;

public final class NumberSetting extends Setting {

	private double value;
	public final double min;
	public final double max;
	public final double step;
	public final double defaultValue;
	/** Text appended in the menu, e.g. "%" or "s". */
	public final String suffix;

	public NumberSetting(String name, double value, double min, double max, double step) {
		this(name, value, min, max, step, "");
	}

	public NumberSetting(String name, double value, double min, double max, double step, String suffix) {
		super(name);
		this.value = value;
		this.defaultValue = value;
		this.min = min;
		this.max = max;
		this.step = step;
		this.suffix = suffix;
	}

	public double get() {
		return value;
	}

	public float getFloat() {
		return (float) value;
	}

	public int getInt() {
		return (int) Math.round(value);
	}

	public void set(double v) {
		if (v < min) {
			v = min;
		}
		if (v > max) {
			v = max;
		}
		if (step > 0.0D) {
			v = Math.round(v / step) * step;
		}
		this.value = v;
	}

	/** 0..1 slider position. */
	public float fraction() {
		return (float) ((value - min) / (max - min));
	}

	public void setFraction(double f) {
		set(min + (max - min) * Math.max(0.0D, Math.min(1.0D, f)));
	}

	public String display() {
		String s;
		if (step >= 1.0D) {
			s = Integer.toString(getInt());
		} else if (step >= 0.1D) {
			s = HString.format("%.1f", value);
		} else {
			s = HString.format("%.2f", value);
		}
		return s + suffix;
	}

	public String write() {
		return Double.toString(value);
	}

	public void read(String v) {
		try {
			set(Double.parseDouble(v));
		} catch (NumberFormatException ignored) {
		}
	}

	public void reset() {
		set(defaultValue);
	}
}
