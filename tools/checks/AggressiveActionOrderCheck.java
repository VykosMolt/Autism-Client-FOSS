import autismclient.aggressivepvp.ActionOrder;

public class AggressiveActionOrderCheck {
    static int checks;

    static void check(boolean ok, String what) {
        checks++;
        if (!ok) throw new AssertionError(what);
    }

    public static void main(String[] args) {
        ActionOrder o = new ActionOrder();
        check(o.mayAttack(7) && o.mayPlace() && o.maySwitchSlot() && !o.acted(), "a fresh tick allows everything");

        o.attacked(7);
        check(!o.mayPlace(), "no block click after a hit");
        check(o.mayAttack(7), "the same crystal may be hit again");
        check(!o.mayAttack(8), "no second entity in a tick");
        check(!o.maySwitchSlot(), "no slot change after a hit");
        check(o.acted(), "a hit is an action");

        o.reset();
        check(o.mayAttack(8) && o.mayPlace() && o.maySwitchSlot() && !o.acted(), "the next tick allows everything again");

        o.used();
        check(!o.mayAttack(7), "no hit after a block click");
        check(!o.mayPlace(), "one block click a tick");
        check(!o.maySwitchSlot(), "no slot change after a click");
        o.reset();

        o.dug();
        check(!o.mayPlace() && !o.mayAttack(7), "nothing in a tick that dug");
        check(o.maySwitchSlot(), "the slot may still change while digging");
        o.reset();

        o.released();
        check(!o.mayPlace() && !o.mayAttack(7) && !o.maySwitchSlot(), "nothing after releasing an item");
        o.reset();

        o.picked();
        check(!o.mayPlace() && !o.mayAttack(7), "nothing after pick block");
        o.reset();

        o.swapped();
        check(o.mayAttack(7) && o.mayPlace() && o.maySwitchSlot() && o.acted(), "a hit or a click may follow a hand swap");
        o.reset();

        o.ended();
        check(!o.mayAttack(7) && !o.mayPlace() && !o.maySwitchSlot(), "nothing after the movement");
        check(!o.acted(), "movement is not an action");
        o.reset();
        check(o.mayAttack(7) && o.mayPlace(), "and the next tick is open again");

        System.out.println("PASS: AggressiveActionOrderCheck (" + checks + " checks)");
    }
}
