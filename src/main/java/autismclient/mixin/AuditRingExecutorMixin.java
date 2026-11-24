package autismclient.mixin;

import java.util.concurrent.Executor;
import net.minecraft.TracingExecutor;
import net.minecraft.util.Util;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkGeneratorStructureState.class)
abstract class AuditRingExecutorMixin {
   @Redirect(
      method = "generateRingPositions",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/TracingExecutor;forName(Ljava/lang/String;)Ljava/util/concurrent/Executor;")
   )
   private Executor audit$ringExecutor(TracingExecutor var1, String var2) {
      return (var1 == null ? Util.backgroundExecutor() : var1).forName(var2);
   }
}
