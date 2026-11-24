package autismclient.mixin.accessor;

import java.util.Optional;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrialSpawnerStateData.class)
public interface AutismTrialSpawnerStateDataAccessor {
   @Accessor("nextSpawnData")
   Optional<SpawnData> autism$getNextSpawnData();
}
