package autismclient.aggressivepvp;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

import java.util.function.Predicate;

import static autismclient.aggressivepvp.PlayerUtils.mc;

/** The parts of Meteor's EntityUtils that Crystal Aura and Auto Totem use. */
public final class EntityUtils {
    private EntityUtils() {}

    public static float getTotalHealth(LivingEntity target) {
        return target.getHealth() + target.getAbsorptionAmount();
    }

    public static String getName(Entity entity) {
        if (entity == null) return null;
        if (entity instanceof Player) return entity.getName().getString();
        return entity.getType().getDescription().getString();
    }

    public static GameType getGameMode(Player player) {
        if (player == null || mc.getConnection() == null) return null;
        PlayerInfo entry = mc.getConnection().getPlayerInfo(player.getUUID());
        return entry == null ? null : entry.getGameMode();
    }

    public static boolean isAboveWater(Entity entity) {
        BlockPos.MutableBlockPos blockPos = entity.blockPosition().mutable();
        int bottom = mc.level.getMinY();
        while (blockPos.getY() > bottom) {
            BlockState state = mc.level.getBlockState(blockPos);
            Block block = state.getBlock();
            if (block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING && state.isSolid()) break;
            Fluid fluid = state.getFluidState().getType();
            if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) return true;
            blockPos.move(0, -1, 0);
        }
        return false;
    }

    public static boolean intersectsWithEntity(AABB box, Predicate<Entity> predicate) {
        return !mc.level.getEntities((Entity) null, box, predicate).isEmpty();
    }
}
