package autismclient.ai;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;

final class be extends bd {
   be(autismclient.ai.b.DataUtil env, bV.Inner_DataUtil saved) {
      super(
         env,
         saved.source() == null ? saved : new bV.Inner_DataUtil(saved.position(), saved.slots(), saved.source().setValue(BlockStateProperties.ENABLED, false)),
         false
      );
   }
}
