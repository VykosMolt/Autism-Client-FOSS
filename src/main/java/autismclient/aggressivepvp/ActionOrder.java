/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package autismclient.aggressivepvp;

/**
 * What one client tick has sent so far, and what may still follow in the order the game itself sends things.
 * <p>
 * The game handles a tick's clicks in a fixed order (slot keys, swap hands, drop, attack, use, pick, then the player
 * moves and the movement goes out, then the tick-end packet), and both clicks act on the one thing the crosshair is
 * on. A server that knows this (Grim's PacketOrder, MultiPlace and MultiInteract checks) drops what could not have
 * come from that order: a hit after a click on a block, a click on a block after a hit, a second block or a second
 * entity in the same tick, a slot change or a hand swap after either, anything after the movement. On 2026-11-11 that
 * cost crystals their hits, the user their own obsidian, and Auto Totem a swap.
 * <p>
 * No Minecraft types: {@link ActionWindow} feeds it the packets, ActionOrderCheck runs it.
 */
public final class ActionOrder {
    private boolean attacked, used, dug, released, swapped, picked, ended;
    private int attackedId;

    /** The tick-end packet went out: the next tick starts clean. */
    public void reset() {
        attacked = used = dug = released = swapped = picked = ended = false;
        attackedId = 0;
    }

    public void attacked(int entityId) {
        attacked = true;
        attackedId = entityId;
    }

    /** A click on a block, on an entity, or the use of an item. */
    public void used() {
        used = true;
    }

    /** Started, aborted or finished breaking a block. */
    public void dug() {
        dug = true;
    }

    /** Let go of an item in use. */
    public void released() {
        released = true;
    }

    /** Swapped hands or dropped an item. */
    public void swapped() {
        swapped = true;
    }

    public void picked() {
        picked = true;
    }

    /** The end of the tick has begun: input, sprint state or movement went out. */
    public void ended() {
        ended = true;
    }

    /** A hit on this entity: not after any click or dig, and not on a second entity. */
    public boolean mayAttack(int entityId) {
        return !ended && !used && !dug && !released && !picked && (!attacked || attackedId == entityId);
    }

    /** A click on a block: one a tick, and not in a tick that hit something or dug. */
    public boolean mayPlace() {
        return !ended && !attacked && !used && !dug && !released && !picked;
    }

    /** A change of the held slot: before the tick's hit or click, never after. */
    public boolean maySwitchSlot() {
        return !ended && !attacked && !used && !released;
    }

    /** Whether anything a player does by hand has gone out this tick. */
    public boolean acted() {
        return attacked || used || dug || released || swapped || picked;
    }
}
