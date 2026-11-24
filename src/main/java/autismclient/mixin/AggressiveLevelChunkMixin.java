package autismclient.mixin;

import autismclient.aggressivepvp.Versions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Obsidian or bedrock appearing (instant place). */
@Mixin(LevelChunk.class)
public abstract class AggressiveLevelChunkMixin {
    @Shadow
    @Final
    private Level level;

    @Inject(
        method = "setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At("TAIL")
    )
    private void ap$blockSet(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        if (level.isClientSide() && level == Minecraft.getInstance().level) Versions.onBlockUpdate(pos, state);
    }
}
