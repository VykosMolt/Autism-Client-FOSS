import autismclient.aggressivepvp.TotemLogic;
import autismclient.aggressivepvp.TotemLogic.ClickPolicy;
import autismclient.aggressivepvp.TotemLogic.Kind;
import autismclient.aggressivepvp.TotemLogic.Path;
import autismclient.aggressivepvp.TotemLogic.Tracker;
import autismclient.aggressivepvp.TotemLogic.Verdict;

import java.util.Random;

public class AggressiveAutoTotemCheck {
    static int failures;

    static void check(String what, boolean ok) {
        if (!ok) {
            System.out.println("FAIL: " + what);
            failures++;
        }
    }

    static Path path(boolean hotbar, boolean still, boolean ok, ClickPolicy p, boolean recentPop) {
        return TotemLogic.path(hotbar, still, ok, p, recentPop, false, true, true);
    }

    public static void main(String[] args) {
        for (boolean still : new boolean[]{true, false}) for (boolean ok : new boolean[]{true, false}) for (ClickPolicy p : ClickPolicy.values())
            check("hotbar totem -> SWAP (still=" + still + " container=" + ok + " " + p + ")", path(true, still, ok, p, false) == Path.SWAP);
        for (ClickPolicy p : ClickPolicy.values()) check("foreign container -> WAIT (" + p + ")", path(false, true, false, p, true) == Path.WAIT);
        check("still -> CLICK", path(false, true, true, ClickPolicy.Never, false) == Path.CLICK);
        check("moving, Never -> WAIT", path(false, false, true, ClickPolicy.Never, true) == Path.WAIT);
        check("moving, AfterPop, no pop -> WAIT", path(false, false, true, ClickPolicy.AfterPop, false) == Path.WAIT);
        check("moving, AfterPop, pop -> CLICK", path(false, false, true, ClickPolicy.AfterPop, true) == Path.CLICK);
        check("moving, Always -> CLICK", path(false, false, true, ClickPolicy.Always, false) == Path.CLICK);
        check("eating, still, hotbar totem -> CLICK", TotemLogic.path(true, true, true, ClickPolicy.Never, false, true, true, true) == Path.CLICK);
        check("eating while moving, hotbar totem -> SWAP", TotemLogic.path(true, false, true, ClickPolicy.Never, false, true, true, true) == Path.SWAP);
        check("eating while moving right after a pop, hotbar totem -> SWAP", TotemLogic.path(true, false, true, ClickPolicy.AfterPop, true, true, true, true) == Path.SWAP);
        check("eating while moving, hotbar totem -> SWAP (Always)", TotemLogic.path(true, false, true, ClickPolicy.Always, false, true, true, true) == Path.SWAP);
        check("swap backed off, still -> CLICK", TotemLogic.path(true, true, true, ClickPolicy.Never, false, false, false, true) == Path.CLICK);
        check("swap backed off, moving -> WAIT", TotemLogic.path(true, false, true, ClickPolicy.Never, false, false, false, true) == Path.WAIT);
        check("click backed off, hotbar totem -> SWAP", TotemLogic.path(true, true, true, ClickPolicy.Never, false, false, true, false) == Path.SWAP);
        check("click backed off, no hotbar totem -> WAIT", TotemLogic.path(false, true, true, ClickPolicy.Always, true, false, true, false) == Path.WAIT);

        check("backoff 0", TotemLogic.backoffTicks(0) == 0 && TotemLogic.backoffTicks(-3) == 0);
        check("backoff 1..5 = 20,40,80,160,320", TotemLogic.backoffTicks(1) == 20 && TotemLogic.backoffTicks(2) == 40 && TotemLogic.backoffTicks(3) == 80 && TotemLogic.backoffTicks(4) == 160 && TotemLogic.backoffTicks(5) == 320);
        check("backoff capped at 400", TotemLogic.backoffTicks(6) == 400 && TotemLogic.backoffTicks(40) == 400 && TotemLogic.backoffTicks(Integer.MAX_VALUE) == 400);

        Tracker t = new Tracker();
        t.tick();
        check("fresh: no pending, no recent pop, both paths open", t.pending() == null && !t.recentPop() && t.ticksSincePop() == -1 && t.canSwap() && t.canClick());

        t.attempted(Kind.CLICK);
        for (int i = 0; i < TotemLogic.VERDICT_TICKS - 1; i++) {
            t.tick();
            check("tick " + i + ": still pending, no verdict", t.pending() == Kind.CLICK && t.verdict(false, true, true) == Verdict.NONE);
        }
        t.tick();
        check("window over: FAILURE", t.verdictDue() && t.verdict(false, true, true) == Verdict.FAILURE && t.failures() == 1 && t.lastBackoff() == 20);
        check("click path quiet, swap path open", !t.canClick() && t.canSwap() && t.pending() == null);
        check("popWaitOver", t.popWaitOver());
        for (int i = 0; i < 20; i++) t.tick();
        check("click path open again after 20 ticks", t.canClick());

        t.attempted(Kind.CLICK);
        for (int i = 0; i < TotemLogic.VERDICT_TICKS; i++) t.tick();
        check("second failure backs off 40", t.verdict(false, true, true) == Verdict.FAILURE && t.lastBackoff() == 40 && t.failures() == 2);
        for (int i = 0; i < 40; i++) t.tick();
        t.attempted(Kind.CLICK);
        for (int i = 0; i < TotemLogic.VERDICT_TICKS; i++) t.tick();
        check("success resets the count", t.verdict(true, true, true) == Verdict.SUCCESS && t.failures() == 0);

        t.attempted(Kind.SWAP);
        for (int i = 0; i < TotemLogic.VERDICT_TICKS; i++) t.tick();
        check("swap failure: swap quiet, click open", t.verdict(false, true, true) == Verdict.FAILURE && !t.canSwap() && t.canClick());
        for (int i = 0; i < 20; i++) t.tick();

        t.attempted(Kind.CLICK);
        for (int i = 0; i < TotemLogic.VERDICT_TICKS; i++) t.tick();
        check("backoff off: FAILURE without quiet", t.verdict(false, true, false) == Verdict.FAILURE && t.lastBackoff() == 0 && t.canClick() && t.canSwap());

        int before = t.failures();
        t.attempted(Kind.CLICK);
        for (int i = 0; i < TotemLogic.VERDICT_TICKS; i++) t.tick();
        check("not wanted any more: UNKNOWN", t.verdict(false, false, true) == Verdict.UNKNOWN && t.failures() == before && t.canClick());

        Tracker p2 = new Tracker();
        p2.tick();
        p2.attempted(Kind.SWAP);
        p2.tick(); p2.tick(); p2.tick();
        p2.popped(4);
        check("pop clears the pending move", p2.pending() == null && !p2.verdictDue() && p2.recentPop() && p2.ticksSincePop() == 0);
        for (int i = 0; i < TotemLogic.VERDICT_TICKS; i++) p2.tick();
        check("no failure after the pop", p2.verdict(false, true, true) == Verdict.NONE && p2.failures() == 0 && p2.canSwap() && p2.canClick());

        Tracker p3 = new Tracker();
        p3.tick();
        p3.popped(4);
        boolean quiet = true;
        for (int i = 0; i < 4; i++) { quiet &= !p3.canSwap() && !p3.canClick() && !p3.popWaitOver(); p3.tick(); }
        check("pop delay holds both paths for 4 ticks", quiet && p3.canSwap() && p3.canClick() && p3.popWaitOver());
        for (int i = 0; i < TotemLogic.RECENT_POP_TICKS - 4; i++) p3.tick();
        check("recent pop at the edge", p3.recentPop());
        p3.tick();
        check("recent pop expired", !p3.recentPop());

        Tracker p4 = new Tracker();
        for (int i = 0; i < 5; i++) p4.tick();
        check("no pop ever: not recent", !p4.recentPop());

        Tracker t5 = new Tracker();
        t5.tick();
        check("attempt accepted", t5.attempted(Kind.CLICK, 30) && t5.pending() == Kind.CLICK);
        t5.tick(); t5.tick();
        check("attempt while pending refused", !t5.attempted(Kind.SWAP) && t5.pending() == Kind.CLICK);
        for (int i = 0; i < 27; i++) t5.tick();
        check("30-tick window not over at 29", !t5.verdictDue());
        t5.tick();
        check("30-tick window over at 30", t5.verdictDue());
        check("extend once", t5.extend() && t5.extended() && !t5.verdictDue());
        check("no second extension", !t5.extend());
        for (int i = 0; i < 30; i++) t5.tick();
        check("extended window over", t5.verdictDue() && t5.verdict(true, true, true) == Verdict.SUCCESS && t5.pending() == null);
        check("window never below VERDICT_TICKS", TotemLogic.windowFor(0) == TotemLogic.VERDICT_TICKS && TotemLogic.windowFor(100) == TotemLogic.VERDICT_TICKS);
        check("window for 400 ms = 20", TotemLogic.windowFor(400) == 20);
        check("window for 1 s = 44", TotemLogic.windowFor(1000) == 44);

        check("reported + holding = SUCCESS", TotemLogic.swapOutcome(true, true, false) == TotemLogic.SwapOutcome.SUCCESS);
        check("reported + not holding = FAILURE", TotemLogic.swapOutcome(true, false, true) == TotemLogic.SwapOutcome.FAILURE);
        check("unreported first time = WAIT", TotemLogic.swapOutcome(false, true, false) == TotemLogic.SwapOutcome.UNREPORTED_WAIT);
        check("unreported after extension = REVERT", TotemLogic.swapOutcome(false, true, true) == TotemLogic.SwapOutcome.UNREPORTED_REVERT);

        Random r = new Random(7);
        boolean inRange = true, sawLo = false, sawHi = false;
        for (int i = 0; i < 2000; i++) {
            int d = TotemLogic.popDelay(r, 3, 6);
            inRange &= d >= 3 && d <= 6;
            sawLo |= d == 3;
            sawHi |= d == 6;
        }
        check("pop delay 3..6", inRange && sawLo && sawHi);
        check("pop delay equal bounds", TotemLogic.popDelay(r, 4, 4) == 4);
        boolean swappedOk = true;
        for (int i = 0; i < 200; i++) { int d = TotemLogic.popDelay(r, 6, 3); swappedOk &= d >= 3 && d <= 6; }
        check("pop delay swapped bounds", swappedOk);
        check("pop delay negative bounds", TotemLogic.popDelay(r, -5, -1) == 0);

        check("mace: <= 1.5 nothing", TotemLogic.maceBonus(1.0) == 0 && TotemLogic.maceBonus(1.5) == 0);
        check("mace: 2 = 8", TotemLogic.maceBonus(2.0) == 8f);
        check("mace: 3 = 12", TotemLogic.maceBonus(3.0) == 12f);
        check("mace: 5 = 16", TotemLogic.maceBonus(5.0) == 16f);
        check("mace: 8 = 22", TotemLogic.maceBonus(8.0) == 22f);
        check("mace: 12 = 26", TotemLogic.maceBonus(12.0) == 26f);
        check("mace: continuous", Math.abs(TotemLogic.maceBonus(3.0001) - 12f) < 0.01 && Math.abs(TotemLogic.maceBonus(8.0001) - 22f) < 0.01);

        TotemLogic.Tracker busy = new TotemLogic.Tracker();
        busy.tick();
        busy.attempted(Kind.STAGE, 10);
        check("pending stage abandoned", busy.abandonStage() && busy.pending() == null && busy.failures() == 0 && busy.attempted(Kind.SWAP, 10));
        check("pending swap not abandoned", !busy.abandonStage() && busy.pending() == Kind.SWAP);

        boolean[] none = new boolean[9], some = new boolean[9];
        some[2] = some[5] = some[8] = true;
        check("stage all: lowest empty", TotemLogic.stageTarget(some, 0, false, 8, true) == 2);
        check("stage all: empty slot in hand filled", TotemLogic.stageTarget(some, 2, true, 8, true) == 2);
        check("stage one: empty fallback in hand filled", TotemLogic.stageTarget(new boolean[]{false, false, false, false, true, false, false, false, false}, 4, false, 4, false) == 4);
        check("stage one: non-empty fallback in hand not displaced", TotemLogic.stageTarget(none, 4, false, 4, false) == -1);
        check("stage all: goes on with hotbar totem", TotemLogic.stageTarget(some, 0, true, 8, true) == 2);
        check("stage all: full hotbar with totem left alone", TotemLogic.stageTarget(none, 0, true, 8, true) == -1);
        check("stage all: full hotbar without totem uses fallback", TotemLogic.stageTarget(none, 0, false, 8, true) == 8);
        check("stage all: never displaces hand", TotemLogic.stageTarget(none, 8, false, 8, true) == -1);
        check("stage one: fallback only when no totem", TotemLogic.stageTarget(some, 0, false, 8, false) == 8 && TotemLogic.stageTarget(some, 0, true, 8, false) == -1);
        check("stage one: fallback even when non-empty", TotemLogic.stageTarget(none, 0, false, 4, false) == 4);

        check("swap window 4 ticks", TotemLogic.swapWindowFor(0) == 4 && TotemLogic.swapWindowFor(30) == 4 && TotemLogic.swapWindowFor(50) == 4);
        check("swap window scaled", TotemLogic.swapWindowFor(120) == 8 && TotemLogic.swapWindowFor(400) == 18);
        Tracker fast = new Tracker();
        check("swap attempt accepted", fast.attemptedSwap(TotemLogic.swapWindowFor(27)) && fast.pending() == Kind.SWAP);
        for (int i = 0; i < 3; i++) fast.tick();
        check("swap: no verdict after 3 ticks", !fast.verdictDue());
        fast.tick();
        check("swap: verdict due after 4 ticks", fast.verdictDue());
        check("swap: extended once", fast.extend() && !fast.verdictDue());
        for (int i = 0; i < 4; i++) fast.tick();
        check("swap: given up after 8 ticks", fast.verdictDue() && fast.verdict(false, true, false) == Verdict.FAILURE && fast.lastBackoff() == 0 && fast.failures() == 1 && fast.canSwap());
        check("swap: no second while pending", fast.attemptedSwap(4) && !fast.attemptedSwap(4));
        for (int i = 0; i < 4; i++) fast.tick();
        check("swap: second failure backs off swap only", fast.verdict(false, true, true) == Verdict.FAILURE && fast.lastBackoff() == 40 && !fast.canSwap() && fast.canClick());

        Tracker dead = new Tracker();
        for (int i = 0; i < 2; i++) {
            dead.attemptedSwap(4);
            while (!dead.verdictDue()) dead.tick();
            dead.verdict(false, true, true);
        }
        dead.attempted(Kind.CLICK, 10);
        boolean stuck = dead.pending() == Kind.CLICK && !dead.canSwap() && dead.failures() == 2;
        dead.forget();
        check("forget clears swap state", stuck && dead.pending() == null && dead.canSwap() && dead.canClick() && dead.failures() == 0 && !dead.recentPop() && dead.attemptedSwap(4));

        Tracker clicks = new Tracker();
        for (int i = 0; i < 2; i++) {
            clicks.attempted(Kind.CLICK, 10);
            while (!clicks.verdictDue()) clicks.tick();
            clicks.verdict(false, true, true);
        }
        clicks.popped(40);
        boolean clickStuck = !clicks.canClick() && clicks.recentPop() && clicks.ticksSincePop() == 0;
        clicks.forget();
        check("forget clears click state", clickStuck && clicks.canClick() && clicks.canSwap() && !clicks.recentPop() && clicks.ticksSincePop() == -1);
        check("click window not shortened", new Tracker().attempted(Kind.CLICK, 2) && TotemLogic.windowFor(27) == TotemLogic.VERDICT_TICKS);

        boolean[] inv = new boolean[36];
        inv[6] = inv[8] = inv[20] = true;
        check("pick first", TotemLogic.pick(inv, 0, 8, -1) == 6 && TotemLogic.pick(inv, 9, 35, -1) == 20);
        check("pick avoid", TotemLogic.pick(inv, 0, 8, 6) == 8 && TotemLogic.pick(inv, 0, 35, 6) == 8);
        check("pick none", TotemLogic.pick(inv, 0, 7, 6) == -1 && TotemLogic.pick(new boolean[36], 0, 35, -1) == -1);

        if (failures > 0) {
            System.out.println("FAIL: AggressiveAutoTotemCheck (" + failures + " failures)");
            System.exit(1);
        }
        System.out.println("PASS: AggressiveAutoTotemCheck");
    }
}
