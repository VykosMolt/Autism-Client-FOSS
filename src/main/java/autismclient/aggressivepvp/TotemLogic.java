/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package autismclient.aggressivepvp;

import java.util.Random;

/** The decisions behind AutoTotem, kept free of Minecraft so a headless check can pin them down. */
public final class TotemLogic {
    private TotemLogic() {}

    public enum Path { SWAP, CLICK, WAIT }

    public enum ClickPolicy { Never, AfterPop, Always }

    /** How a move was made: the F-key swap, a click into the offhand, or a staging click into the hotbar. */
    public enum Kind { SWAP, CLICK, STAGE }

    public enum Verdict { NONE, SUCCESS, UNKNOWN, FAILURE }

    /** How a pending swap looks when its window ends. */
    public enum SwapOutcome { SUCCESS, FAILURE, UNREPORTED_WAIT, UNREPORTED_REVERT }

    /**
     * A swap counts only once the server reported a totem in the offhand slot. No report yet: wait one more window the
     * first time (slow server tick or round trip), revert the local mirror the second time. Reported: the inventory decides.
     */
    public static SwapOutcome swapOutcome(boolean reported, boolean holding, boolean extended) {
        if (reported) return holding ? SwapOutcome.SUCCESS : SwapOutcome.FAILURE;
        return extended ? SwapOutcome.UNREPORTED_REVERT : SwapOutcome.UNREPORTED_WAIT;
    }

    /** Ticks to wait for the server's answer at this round trip: at least VERDICT_TICKS, else two trips plus slack. */
    public static int windowFor(int latencyMs) {
        int latencyTicks = (Math.max(0, latencyMs) + 49) / 50;
        return Math.max(VERDICT_TICKS, 2 * latencyTicks + 4);
    }

    /** Ticks to wait for the server to report an offhand swap: two round trips plus slack, never under SWAP_TICKS. */
    public static int swapWindowFor(int latencyMs) {
        int latencyTicks = (Math.max(0, latencyMs) + 49) / 50;
        return Math.max(SWAP_TICKS, 2 * latencyTicks + 2);
    }

    public static final int SWAP_TICKS = 4;

    /** The first slot from..to (inclusive) that holds a totem and is not the one to avoid, -1 when there is none. */
    public static int pick(boolean[] totemAt, int from, int to, int avoid) {
        for (int slot = from; slot <= to && slot < totemAt.length; slot++) {
            if (totemAt[slot] && slot != avoid) return slot;
        }
        return -1;
    }

    /** Ticks to wait for the server's answer to a move before judging it; covers round trips up to ~500 ms. */
    public static final int VERDICT_TICKS = 10;
    /** A pop this recent lets the AfterPop click policy act. */
    public static final int RECENT_POP_TICKS = 40;

    public static Path path(boolean hotbarTotem, boolean still, boolean containerOk, ClickPolicy policy, boolean recentPop, boolean usingItem, boolean canSwap, boolean canClick) {
        boolean clickable = canClick && containerOk && (still || policy == ClickPolicy.Always || (policy == ClickPolicy.AfterPop && recentPop));
        boolean swappable = canSwap && hotbarTotem;
        if (swappable && !(usingItem && clickable && still)) return Path.SWAP;
        if (clickable) return Path.CLICK;
        if (swappable) return Path.SWAP;
        return Path.WAIT;
    }

    public static int stageTarget(boolean[] emptyHotbar, int selected, boolean hotbarTotem, int fallbackSlot, boolean fill) {
        if (fill || !hotbarTotem) {
            for (int slot = 0; slot < emptyHotbar.length; slot++) {
                if (emptyHotbar[slot] && (fill || slot == fallbackSlot)) return slot;
            }
        }
        if (!hotbarTotem && fallbackSlot != selected) return fallbackSlot;
        return -1;
    }

    public static int backoffTicks(int failures) {
        if (failures <= 0) return 0;
        return (int) Math.min(400, 20L << Math.min(failures - 1, 10));
    }

    /** Random re-equip delay after a pop, inclusive bounds in ticks; swapped bounds are tolerated. */
    public static int popDelay(Random random, int min, int max) {
        int lo = Math.max(0, Math.min(min, max)), hi = Math.max(0, Math.max(min, max));
        return lo + random.nextInt(hi - lo + 1);
    }

    /**
     * MaceItem.getAttackDamageBonus in 26.3 without the density enchantment: the smash bonus for an attacker that fell
     * this far, before armour. Zero below the 1.5-block threshold (MaceItem.canSmashAttack).
     */
    public static float maceBonus(double fallDistance) {
        if (fallDistance <= 1.5) return 0;
        if (fallDistance <= 3) return (float) (4 * fallDistance);
        if (fallDistance <= 8) return (float) (12 + 2 * (fallDistance - 3));
        return (float) (22 + fallDistance - 8);
    }

    public static final class Tracker {
        private long tick;
        private long attemptTick = -1;
        private int window = VERDICT_TICKS;
        private boolean extended;
        private Kind pending;
        private long lastPopTick = -1;
        private long quietUntil, clickQuietUntil, swapQuietUntil;
        private int failures;
        private int lastBackoff;

        public void tick() {
            tick++;
        }

        public void forget() {
            attemptTick = -1;
            pending = null;
            extended = false;
            lastPopTick = -1;
            quietUntil = clickQuietUntil = swapQuietUntil = tick;
            failures = 0;
            lastBackoff = 0;
        }

        public long now() {
            return tick;
        }

        public void popped(int delay) {
            lastPopTick = tick;
            quietUntil = Math.max(quietUntil, tick + delay);
            attemptTick = -1;
            pending = null;
        }

        public boolean recentPop() {
            return lastPopTick >= 0 && tick - lastPopTick <= RECENT_POP_TICKS;
        }

        public long ticksSincePop() {
            return lastPopTick < 0 ? -1 : tick - lastPopTick;
        }

        public boolean attempted(Kind kind, int windowTicks) {
            if (pending != null) return false;
            attemptTick = tick;
            pending = kind;
            window = Math.max(VERDICT_TICKS, windowTicks);
            extended = false;
            return true;
        }

        public boolean attempted(Kind kind) {
            return attempted(kind, VERDICT_TICKS);
        }

        public boolean attemptedSwap(int windowTicks) {
            if (!attempted(Kind.SWAP)) return false;
            window = Math.max(SWAP_TICKS, windowTicks);
            return true;
        }

        public boolean abandonStage() {
            if (pending != Kind.STAGE) return false;
            pending = null;
            attemptTick = -1;
            return true;
        }

        public boolean extend() {
            if (pending == null || extended) return false;
            extended = true;
            attemptTick = tick;
            return true;
        }

        public boolean extended() {
            return extended;
        }

        public Kind pending() {
            return pending;
        }

        public boolean verdictDue() {
            return pending != null && tick >= attemptTick + window;
        }

        public boolean canSwap() {
            return tick >= quietUntil && tick >= swapQuietUntil;
        }

        public boolean canClick() {
            return tick >= quietUntil && tick >= clickQuietUntil;
        }

        public boolean popWaitOver() {
            return tick >= quietUntil;
        }

        public int failures() {
            return failures;
        }

        public int lastBackoff() {
            return lastBackoff;
        }

        public Verdict verdict(boolean achieved, boolean stillWanted, boolean applyBackoff) {
            if (!verdictDue()) return Verdict.NONE;
            Kind kind = pending;
            pending = null;
            attemptTick = -1;
            if (achieved) {
                failures = 0;
                return Verdict.SUCCESS;
            }
            if (!stillWanted) return Verdict.UNKNOWN;
            failures++;
            lastBackoff = applyBackoff ? backoffTicks(failures) : 0;
            if (kind == Kind.SWAP) swapQuietUntil = Math.max(swapQuietUntil, tick + lastBackoff);
            else clickQuietUntil = Math.max(clickQuietUntil, tick + lastBackoff);
            return Verdict.FAILURE;
        }
    }
}
