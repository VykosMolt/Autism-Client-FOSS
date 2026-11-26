package autismclient.ai;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;

final class AiPrintHopper extends AiPrintContainer {
   AiPrintHopper(autismclient.ai.b.LivePrintEnv env, AiSchematicMachinePlan.Inner_DataUtil saved) {
      super(
         env,
         saved.source() == null ? saved : new AiSchematicMachinePlan.Inner_DataUtil(saved.position(), saved.slots(), saved.source().setValue(BlockStateProperties.ENABLED, false)),
         false
      );
   }
}
