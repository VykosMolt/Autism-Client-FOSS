package autismclient.aggressivepvp;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

import static autismclient.aggressivepvp.PlayerUtils.mc;

/** The parts of Meteor's InvUtils, FindItemResult and SlotUtils that Crystal Aura and Auto Totem use. */
public final class InvUtils {
    public static final int OFFHAND = 40;

    private InvUtils() {}

    public record FindItemResult(int slot, int count) {
        public boolean found() {
            return slot != -1;
        }

        public InteractionHand getHand() {
            if (slot == OFFHAND) return InteractionHand.OFF_HAND;
            if (slot == mc.player.getInventory().getSelectedSlot()) return InteractionHand.MAIN_HAND;
            return null;
        }

        public boolean isOffhand() {
            return getHand() == InteractionHand.OFF_HAND;
        }
    }

    private static Predicate<ItemStack> isOneOf(Item... items) {
        return stack -> {
            for (Item item : items) if (stack.getItem() == item) return true;
            return false;
        };
    }

    public static boolean testInHotbar(Item... items) {
        Predicate<ItemStack> predicate = isOneOf(items);
        if (predicate.test(mc.player.getMainHandItem()) || predicate.test(mc.player.getOffhandItem())) return true;
        for (int i = 0; i <= 8; i++) if (predicate.test(mc.player.getInventory().getItem(i))) return true;
        return false;
    }

    public static FindItemResult findInHotbar(Item... items) {
        return findInHotbar(isOneOf(items));
    }

    public static FindItemResult findInHotbar(Predicate<ItemStack> isGood) {
        if (isGood.test(mc.player.getOffhandItem())) return new FindItemResult(OFFHAND, mc.player.getOffhandItem().getCount());
        if (isGood.test(mc.player.getMainHandItem())) return new FindItemResult(mc.player.getInventory().getSelectedSlot(), mc.player.getMainHandItem().getCount());
        return find(isGood, 0, 8);
    }

    public static FindItemResult find(Item... items) {
        return find(isOneOf(items));
    }

    public static FindItemResult find(Predicate<ItemStack> isGood) {
        if (mc.player == null) return new FindItemResult(0, 0);
        return find(isGood, 0, mc.player.getInventory().getContainerSize());
    }

    public static FindItemResult find(Predicate<ItemStack> isGood, int start, int end) {
        if (mc.player == null) return new FindItemResult(0, 0);
        int slot = -1, count = 0;
        for (int i = start; i <= end; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (isGood.test(stack)) {
                if (slot == -1) slot = i;
                count += stack.getCount();
            }
        }
        return new FindItemResult(slot, count);
    }

    /** Selects a hotbar slot and tells the server at once (Meteor's InvUtils.swap without the swap-back memory). */
    public static boolean swap(int slot, boolean swapBack) {
        if (slot == OFFHAND) return true;
        if (slot < 0 || slot > 8) return false;
        mc.player.getInventory().setSelectedSlot(slot);
        Autism.syncSelected();
        return true;
    }

    /** Container slot id of an inventory index in the player's own menu (SlotUtils.indexToId, survival). */
    public static int indexToId(int i) {
        if (i >= 0 && i <= 8) return 36 + i;
        if (i >= 36 && i <= 39) return 5 + (i - 36);
        if (i == OFFHAND) return 45;
        return i;
    }
}
