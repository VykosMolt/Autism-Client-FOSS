package autismclient.aggressivepvp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import static autismclient.aggressivepvp.PlayerUtils.mc;

/** The parts of Meteor's BlockUtils and SwingMode that Crystal Aura uses, adapted for MC 26.3. */
public final class BlockUtils {
    private BlockUtils() {}

    public static boolean canPlace(BlockPos blockPos, boolean checkEntities) {
        if (blockPos == null || !Level.isInSpawnableBounds(blockPos)) return false;
        if (!mc.level.getBlockState(blockPos).canBeReplaced()) return false;
        return !checkEntities || mc.level.isUnobstructed(Blocks.OBSIDIAN.defaultBlockState(), blockPos, CollisionContext.empty());
    }

    public static Direction getPlaceSide(BlockPos blockPos) {
        Vec3 lookVec = Vec3.atCenterOf(blockPos).subtract(mc.player.getEyePosition());
        double bestRelevancy = -Double.MAX_VALUE;
        Direction bestSide = null;
        for (Direction side : Direction.values()) {
            BlockState state = mc.level.getBlockState(blockPos.relative(side));
            if (state.isAir() || isClickable(state.getBlock())) continue;
            if (!state.getFluidState().isEmpty()) continue;
            double relevancy = side.getAxis().choose(lookVec.x(), lookVec.y(), lookVec.z()) * side.getAxisDirection().getStep();
            if (relevancy > bestRelevancy) {
                bestRelevancy = relevancy;
                bestSide = side;
            }
        }
        return bestSide;
    }

    public static InteractionResult interact(BlockHitResult blockHitResult, InteractionHand hand, SwingMode swing) {
        boolean wasSneaking = mc.player.isShiftKeyDown();
        mc.player.setShiftKeyDown(false);
        InteractionResult result = mc.gameMode.useItemOn(mc.player, hand, blockHitResult);
        if (result instanceof InteractionResult.Success success && success.swingSource() == InteractionResult.SwingSource.PREDICTED) {
            swing.interactSwing(hand);
        }
        mc.player.setShiftKeyDown(wasSneaking);
        return result;
    }

    public static boolean isClickable(Block block) {
        return block instanceof CraftingTableBlock || block instanceof AnvilBlock || block instanceof LoomBlock || block instanceof CartographyTableBlock
            || block instanceof GrindstoneBlock || block instanceof StonecutterBlock || block instanceof ButtonBlock || block instanceof BasePressurePlateBlock
            || block instanceof BaseEntityBlock || block instanceof BedBlock || block instanceof FenceGateBlock || block instanceof DoorBlock
            || block instanceof NoteBlock || block instanceof TrapDoorBlock;
    }

    public enum SwingMode {
        Both,
        Packet,
        Client,
        None;

        public boolean client() {
            return this == Client || this == Both;
        }

        public boolean server() {
            return this == Packet || this == Both;
        }

        /** The arm after an attack. The punch packet itself always goes out with the attack (see CrystalEngine.attackCrystal). */
        public void attackAnimation(InteractionHand hand) {
            if (client()) mc.player.swing(hand, mc.player.getItemInHand(hand).getAttackAnimation(), false);
        }

        /**
         * As 26.3 does after a block click the client predicted: local arm animation only.
         * ServerboundSwingPacket was removed in 26.3; the server triggers the interaction swing from UseItemOn itself.
         */
        public void interactSwing(InteractionHand hand) {
            if (client()) mc.player.swing(hand, mc.player.getItemInHand(hand).getInteractAnimation(), false);
        }
    }
}
