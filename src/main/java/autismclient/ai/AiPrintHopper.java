package autismclient.ai;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;

final class AiPrintHopper extends AiPrintContainer {
   AiPrintHopper(autismclient.ai.print.LivePrintEnv env, AiSchematicMachinePlan.ContainerPlan saved) {
      super(
         env,
         saved.source() == null ? saved : new AiSchematicMachinePlan.ContainerPlan(saved.position(), saved.slots(), saved.source().setValue(BlockStateProperties.ENABLED, false)),
         false
      );
   }
}
