package net.slate.module;

/** Anything the menu animates keeps its own state, so no per-frame map lookups or boxing. */
public abstract class Animated {

	/** 0..1, driven by the menu. */
	public float anim;
}
