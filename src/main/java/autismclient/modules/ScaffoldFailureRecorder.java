package autismclient.modules;

import autismclient.util.RotationMath;
import com.google.gson.GsonBuilder;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

final class ScaffoldFailureRecorder {
   static final int init_I = 192;
   private final ArrayDeque<ScaffoldFailureRecorder.PlacementFact> init_ArrayDeque = new ArrayDeque<>();
   private final Map<BlockPos, Integer> init_Map = new HashMap<>();
   private long init_J = -9223372036854775808L;
   private boolean init_Z;

   synchronized void init(ScaffoldFailureRecorder.PlacementFact fact) {
      this.init_ArrayDeque.addLast(fact);
      if ("use".equals(fact.init_Str()) && fact.init_BlockPos() != null) {
         this.init_Map.merge(fact.init_BlockPos(), 1, Integer::sum);
      }

      while (this.init_ArrayDeque.size() > 192) {
         ScaffoldFailureRecorder.PlacementFact removed = this.init_ArrayDeque.removeFirst();
         if ("use".equals(removed.init_Str()) && removed.init_BlockPos() != null) {
            this.init_Map.computeIfPresent(removed.init_BlockPos(), (cell, count) -> count <= 1 ? null : count - 1);
         }
      }
   }

   synchronized boolean init(BlockPos cell) {
      return cell != null && this.init_Map.containsKey(cell);
   }

   synchronized ScaffoldFailureRecorder.FailureCapture init(String reason, BlockPos cell, int sequence, int tick, long now) {
      if (this.init(cell) && (!this.init_Z || now - this.init_J >= 4000000000L)) {
         ScaffoldFailureRecorder.FailureCapture capture = new ScaffoldFailureRecorder.FailureCapture(!this.init_Z, reason, cell.immutable(), sequence, tick, List.copyOf(this.init_ArrayDeque));
         this.init_Z = true;
         this.init_J = now;
         return capture;
      } else {
         return null;
      }
   }

   synchronized void init() {
      this.init_ArrayDeque.clear();
      this.init_Map.clear();
      this.init_J = -9223372036854775808L;
      this.init_Z = false;
   }

   record FailureCapture(boolean init_Z, String init_Str, BlockPos init_BlockPos, int init_I, int load, List<ScaffoldFailureRecorder.PlacementFact> init_List) {
      String toJson() {
         return new GsonBuilder().setPrettyPrinting().create().toJson(this);
      }

      public String load_Str() {
         return this.init_Str;
      }

      public int load_I() {
         return this.load;
      }
   }

   record PlacementFact(
      String init_Str,
      int init_I,
      int load_I,
      BlockPos init_BlockPos,
      BlockPos load_BlockPos,
      Direction init_Direction,
      Vec3 init_Vec3,
      Vec3 load_Vec3,
      RotationMath.Rotation init_ce_DataUtil,
      RotationMath.Rotation load_ce_DataUtil,
      Input init_Input,
      String load_Str
   ) {
      PlacementFact {
         init_BlockPos = init_BlockPos == null ? null : init_BlockPos.immutable();
         load_BlockPos = load_BlockPos == null ? null : load_BlockPos.immutable();
      }
   }
}
