import autismclient.aggressivepvp.CrystalLogic;

public class AggressiveTradeTotemCheck {
    static int checks;

    static void check(boolean ok, String what) {
        checks++;
        if (!ok) throw new AssertionError(what);
    }

    public static void main(String[] args) {
        CrystalLogic.Rules rules = new CrystalLogic.Rules(2, 8, true, 1, 1);
        CrystalLogic.Rules lifted = CrystalLogic.traded(rules);
        double none = CrystalLogic.NONE;

        check(CrystalLogic.score(rules, 30, 25, 40, 20, false, true) == none, "with the limit, 25 to us is refused");
        check(CrystalLogic.score(lifted, 30, 25, 40, 20, false, false) == none, "spare ready: pop us without popping target refused");
        check(CrystalLogic.score(lifted, 30, 25, 25, 20, false, false) > none, "spare ready: pop for pop goes");
        check(CrystalLogic.score(lifted, 2.4, 22, 20, 20, false, false) == none, "spare ready: 2.4 for a pop refused");
        check("would pop you without popping the target (trade-totem)".equals(CrystalLogic.why(lifted, 2.4, 22, 20, 20, false, false)), "refusal named");
        check(CrystalLogic.score(rules, 60, 48, 20, 5, false, true) == none, "limit refuses kill us");
        check(CrystalLogic.score(lifted, 60, 48, 20, 5, false, false) > none, "limit lifted pop+kill goes");
        check(CrystalLogic.score(lifted, 3, 6, 20, 20, false, false) > none, "spare ready: hurts us more goes");
        check(CrystalLogic.score(rules, 3, 6, 20, 20, false, false) == none, "no spare: ratio refuses");
        check(CrystalLogic.score(lifted, 1, 0.5, 20, 20, false, false) == none, "under min damage refused");
        check(CrystalLogic.score(lifted, 60, 10, 80, 20, false, false) > CrystalLogic.score(lifted, 30, 12, 80, 20, false, false), "better trade higher");

        CrystalLogic.Rules night = new CrystalLogic.Rules(1.5, 16, true, 1, 0.9);
        check(CrystalLogic.score(CrystalLogic.traded(night), 11.4, 34.2, 4.0, 7.0, false, false) > none, "totem for totem goes");
        double judged = CrystalLogic.judgedHealth(7.0, true);
        check(judged == 7.0, "judgedHealth <= current");
        check(CrystalLogic.score(night, 11.4, 34.2, 4.0, judged, false, true) == none, "pop on its way refused");
        check(CrystalLogic.judgedHealth(20, true) == CrystalLogic.POST_POP_HEALTH && CrystalLogic.judgedHealth(20, false) == 20, "POST_POP_HEALTH bar");
        check(CrystalLogic.score(night, 14, 12, 20, 20, false, true) > none, "12 at 20 goes");
        check(CrystalLogic.score(night, 14, 12, 20, CrystalLogic.judgedHealth(20, true), false, true) == none, "12 after pop refused");

        check(CrystalLogic.score(CrystalLogic.traded(night), 2.6, 7.0, 2.0, 5.3, false, false) > none, "raw 2.0 went");
        double him = CrystalLogic.targetHealth(2.0, 0, 7);
        check(him == 2.0 + CrystalLogic.POP_ABSORPTION, "hidden absorption added");
        check(CrystalLogic.score(CrystalLogic.traded(night), 2.6, 7.0, him, 5.3, false, false) == none, "hidden absorption prevents bad trade");
        check(CrystalLogic.targetHealth(2.0, 0, 101) == 2.0 && CrystalLogic.targetHealth(2.0, 0, -1) == 2.0, "expired or no pop");
        check(CrystalLogic.targetHealth(10.0, 8.0, 7) == 10.0, "visible absorption not doubled");

        check(CrystalLogic.popBar(9.8, 0) == 9.8, "margin 0");
        check(CrystalLogic.score(CrystalLogic.traded(night), 2.9, 8.1, 12.1, CrystalLogic.popBar(9.8, 0), false, false) > none, "without margin went");
        check(CrystalLogic.score(CrystalLogic.traded(night), 2.9, 8.1, 12.1, CrystalLogic.popBar(9.8, 0.2), false, false) == none, "with 20% refused");
        check(CrystalLogic.score(CrystalLogic.traded(night), 2.7, 3.0, 3.3, CrystalLogic.popBar(3.7, 0.2), false, false) == none, "3.0 of 3.7 refused");
        check(CrystalLogic.score(CrystalLogic.traded(night), 25, 18, 20, CrystalLogic.popBar(20, 0.2), false, false) > none, "kill goes with margin");
        check(CrystalLogic.score(CrystalLogic.traded(night), 3, 6, 20, CrystalLogic.popBar(20, 0.2), false, false) > none, "6 of 20 unchanged");

        System.out.println("PASS: AggressiveTradeTotemCheck (" + checks + " checks)");
    }
}
