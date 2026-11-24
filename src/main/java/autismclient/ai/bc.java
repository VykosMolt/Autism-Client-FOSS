package autismclient.ai;

import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

final class bc {
   private bc() {
   }

   static autismclient.ai.b.TaskQueue init(final autismclient.ai.b.DataUtil env, final autismclient.ai.b.NetHelper spec) {
      final Minecraft mc = Minecraft.getInstance();
      final LocalPlayer originalPlayer = mc.player;
      return new autismclient.ai.b.TaskQueue(env, spec, new autismclient.ai.b.TaskQueue.Inner_DataUtil() {
         private Boolean init;

         @Override
         public autismclient.ai.b.BitSetUtil init(BlockPos at, BlockState source) {
            return bd.init_bd_DataUtil_bV_DataUtil(env, new bV.Inner_DataUtil(at, Map.of(), source));
         }

         @Override
         public autismclient.ai.b.BitSetUtil init(boolean powered) {
            return new bc.Inner_DataUtil(env, spec, powered);
         }

         @Override
         public autismclient.ai.b.BitSetUtil init(BlockPos at, BlockState source, Map<Integer, ItemStack> contents) {
            return bd.load(env, new bV.Inner_DataUtil(at, contents, source));
         }

         @Override
         public int init_I(ItemStack material) {
            return mc.player == originalPlayer && mc.player != null ? ae.init_I_Container_ItemStack_2(mc.player.getInventory(), material) : 0;
         }

         @Override
         public boolean init() {
            return env.update_Z();
         }

         @Override
         public boolean init_Z(ItemStack material) {
            return mc.player == originalPlayer && env.load(material);
         }

         @Override
         public boolean load(ItemStack material) {
            return mc.player == originalPlayer && mc.player != null && !BitSetUtil.init(mc.player.getInventory(), material, List.of()).isEmpty();
         }

         @Override
         public boolean load() {
            if (mc.player == originalPlayer && originalPlayer != null) {
               if (this.init == null) {
                  this.init = env.update_Z();
               }

               return this.init == env.update_Z();
            } else {
               return false;
            }
         }
      });
   }

   private static final class Inner_DataUtil implements autismclient.ai.b.BitSetUtil {
      final autismclient.ai.b.DataUtil init_DataUtil;
      final autismclient.ai.b.NetHelper init_NetHelper;
      final Object init_Obj;
      final Object load_Obj;
      final boolean init_Z;
      final boolean load_Z;
      final BlockState init_BlockState;
      final BlockState load_BlockState;
      autismclient.ai.b.BitSetUtil.Inner_DataUtil init_BitSetUtil_DataUti = autismclient.ai.b.BitSetUtil.Inner_DataUtil.init;
      long init_J;
      long load_J;
      String init_Str = "Preparing the source clock control";

      Inner_DataUtil(autismclient.ai.b.DataUtil env, autismclient.ai.b.NetHelper spec, boolean powered) {
         this.init_DataUtil = env;
         this.init_NetHelper = spec;
         this.init_Obj = env.init_Obj();
         this.load_Obj = Minecraft.getInstance().player;
         this.load_Z = env.update_Z();
         this.init_Z = powered;
         this.init_BlockState = spec.source().get(spec.lever());
         this.load_BlockState = this.init_BlockState.setValue(BlockStateProperties.POWERED, powered);
      }

      boolean init_Z() {
         return this.load_Obj != null
            && Minecraft.getInstance().player == this.load_Obj
            && this.init_DataUtil.init_Obj() == this.init_Obj
            && this.init_DataUtil.update_Z() == this.load_Z;
      }

      @Override
      public BlockPos position() {
         return this.init_NetHelper.lever();
      }

      @Override
      public Item item() {
         return Items.AIR;
      }

      @Override
      public String description() {
         return this.init_Z ? "clock hold control" : "clock control restoration";
      }

      @Override
      public String detail() {
         return this.init_Str;
      }

      @Override
      public boolean matchesSource(BlockState state) {
         return state == this.init_BlockState;
      }

      @Override
      public autismclient.ai.b.BitSetUtil.Inner_DataUtil prepare() {
         if (this.init_BitSetUtil_DataUti != autismclient.ai.b.BitSetUtil.Inner_DataUtil.init
            && this.init_BitSetUtil_DataUti != autismclient.ai.b.BitSetUtil.Inner_DataUtil.load) {
            return this.init_BitSetUtil_DataUti;
         }

         if (this.init_Z() && this.init_DataUtil.load_Z(this.position())) {
            BlockState actual = this.init_DataUtil.init_BlockState_BlockPos(this.position());
            if (actual != this.init_BlockState && actual != this.init_BlockState.setValue(BlockStateProperties.POWERED, true)) {
               return this.init_BitSetUtil_DataUti = autismclient.ai.b.BitSetUtil.Inner_DataUtil.flush;
            } else if (actual == this.load_BlockState) {
               this.init_Str = "Clock control changed without this action's receipt";
               return this.init_BitSetUtil_DataUti = autismclient.ai.b.BitSetUtil.Inner_DataUtil.flush;
            } else {
               Minecraft mc = Minecraft.getInstance();
               return mc.player != null
                     && mc.gui.screen() == null
                     && mc.player.containerMenu == mc.player.inventoryMenu
                     && mc.player.inventoryMenu.getCarried().isEmpty()
                  ? (this.init_BitSetUtil_DataUti = autismclient.ai.b.BitSetUtil.Inner_DataUtil.load)
                  : (this.init_BitSetUtil_DataUti = autismclient.ai.b.BitSetUtil.Inner_DataUtil.init);
            }
         } else {
            this.init_Str = "Clock control world, player or game mode changed";
            return this.init_BitSetUtil_DataUti = autismclient.ai.b.BitSetUtil.Inner_DataUtil.flush;
         }
      }

      @Override
      public boolean beginDispatch() {
         if (this.prepare() == autismclient.ai.b.BitSetUtil.Inner_DataUtil.load
            && !this.init_DataUtil.load_Z()
            && !this.init_NetHelper.sweptVolume().intersects(this.init_DataUtil.init_AABB())) {
            this.init_J = ca.init_J_BlockPos(this.position());
            this.load_J = this.init_DataUtil.load_J();
            this.init_BitSetUtil_DataUti = autismclient.ai.b.BitSetUtil.Inner_DataUtil.read;
            this.init_Str = "Waiting for the clock lever's native server receipt";
            return true;
         } else {
            return false;
         }
      }

      @Override
      public autismclient.ai.b.BitSetUtil.Inner_DataUtil poll_BitSetUtil_DataUti() {
         if (this.init_BitSetUtil_DataUti != autismclient.ai.b.BitSetUtil.Inner_DataUtil.read) {
            return this.init_BitSetUtil_DataUti;
         }

         if (!this.init_Z()) {
            this.init_Str = "Clock control world, player or game mode changed after dispatch";
            return this.init_BitSetUtil_DataUti = autismclient.ai.b.BitSetUtil.Inner_DataUtil.close;
         }

         ca.Inner_DataUtil reply = ca.init_ca_DataUtil_BlockPos(this.position());
         if (reply == null
            || reply.revision() <= this.init_J
            || reply.state() != this.load_BlockState
            || this.init_DataUtil.init_BlockState_BlockPos(this.position()) != this.load_BlockState
            || this.init_Z && !this.init_NetHelper.holding(this.init_DataUtil::init_BlockState_BlockPos)) {
            if (this.init_DataUtil.load_J() - this.load_J >= 5000000000L) {
               this.init_Str = "Clock control was not acknowledged; do not repeat the toggle";
               return this.init_BitSetUtil_DataUti = autismclient.ai.b.BitSetUtil.Inner_DataUtil.close;
            } else {
               return this.init_BitSetUtil_DataUti;
            }
         } else {
            this.init_Str = "Native clock control and holding geometry acknowledged";
            return this.init_BitSetUtil_DataUti = autismclient.ai.b.BitSetUtil.Inner_DataUtil.update;
         }
      }

      @Override
      public void cancel() {
         if (this.init_BitSetUtil_DataUti == autismclient.ai.b.BitSetUtil.Inner_DataUtil.read) {
            this.init_BitSetUtil_DataUti = autismclient.ai.b.BitSetUtil.Inner_DataUtil.close;
            this.init_Str = "Clock control dispatch interrupted without a receipt";
         }
      }
   }
}
