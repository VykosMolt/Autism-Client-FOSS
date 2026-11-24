package autismclient.ai;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

final class az extends aR {
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final Object init_Obj = this.init_Minecraft.level;
   private final boolean init_Z;
   private final boolean load;
   private final autismclient.ai.a.j.Inner_DataUtil init_j_DataUtil = new autismclient.ai.a.j.Inner_DataUtil();
   private autismclient.ai.a.j.Inner_StreamUtil init_j_StreamUtil;

   az(boolean parkour, boolean assumeNoFallDamage) {
      this.init_Z = parkour;
      this.load = assumeNoFallDamage;
   }

   autismclient.ai.a.j.Inner_StreamUtil init_j_StreamUtil() {
      return this.init_j_StreamUtil;
   }

   void write() {
      BlockPos feet = this.init_j_StreamUtil.stance();
      JsonObject args = ag.init_JsonObject_ObjArr("x", feet.getX(), "y", feet.getY(), "z", feet.getZ(), "radius", 0.6);
      if (this.init_Z) {
         args = cy.init_JsonObject_JsonObject_Z(args, this.load);
         args.addProperty("allowInventory", false);
      }

      o.init_V("game:goto", args);
   }

   @Override
   boolean load_Z() {
      return this.init_j_StreamUtil != null && this.init_j_StreamUtil.valid();
   }

   @Override
   boolean read_Z() {
      return this.init_Minecraft.level != null && this.init_Minecraft.level == this.init_Obj && this.init_Minecraft.player != null;
   }

   @Override
   BlockPos init_BlockPos() {
      return this.load_Z() ? this.init_j_StreamUtil.stance() : BlockPos.ZERO;
   }

   @Override
   int init_I() {
      return 0;
   }

   @Override
   String read_Str() {
      return "none";
   }

   @Override
   void init_V_Z(boolean surface) {
      this.load(false);
   }

   void update() {
      this.load(true);
      if (!this.load_Z()) {
         autismclient.util.bi.write("Aim at a safe standing surface");
      }
   }

   private void load(boolean fresh) {
      if (this.read_Z()) {
         this.init_j_StreamUtil = this.init_j_DataUtil
            .init(this.init_Minecraft, cU.init_Vec3_Minecraft(this.init_Minecraft), cU.load_Vec3_Minecraft(this.init_Minecraft), 250.0, false, fresh);
      }
   }

   @Override
   void init(double scroll, double x, double y, double width, double height) {
   }

   @Override
   void load_V() {
   }

   @Override
   void init(int x, int y, int z) {
   }

   @Override
   void init_V_I(int steps) {
   }

   @Override
   void init_V() {
   }

   @Override
   void init(BlockPos position, int rotation, String mirror) {
   }

   @Override
   void read_V() {
   }
}
