package autismclient.util.oresim;

import autismclient.AutismClientAddon;
import autismclient.compat263.Compat263;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

public final class DataUtil {
   public static final int init_I = 5;
   private static final RandomSource init_RandomSource = RandomSource.create(42L);
   private static final Map<BlockState, DataUtil.Inner_StreamUtil> init_Map = new LinkedHashMap<BlockState, DataUtil.Inner_StreamUtil>(128, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<BlockState, DataUtil.Inner_StreamUtil> eldest) {
         return this.size() > 4096;
      }
   };
   private static final List<BlockStateModelPart> init_List = new ArrayList<>();
   private static Object init_Obj;

   private DataUtil() {
   }

   public static void init() {
      init_Map.clear();
      init_Obj = null;
   }

   public static DataUtil.Inner_StreamUtil init(BlockState state) {
      if (state == null) {
         return DataUtil.Inner_StreamUtil.init_DataUtil_StreamUti;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc == null) {
         return DataUtil.Inner_StreamUtil.init_DataUtil_StreamUti;
      }

      BlockStateModelSet set = mc.getModelManager().getBlockStateModelSet();
      if (set == null) {
         return DataUtil.Inner_StreamUtil.init_DataUtil_StreamUti;
      }

      if (set != init_Obj) {
         init_Map.clear();
         init_Obj = set;
      }

      DataUtil.Inner_StreamUtil cached = init_Map.get(state);
      if (cached != null) {
         return cached;
      }

      DataUtil.Inner_StreamUtil built;
      try {
         built = init(set, state);
      } catch (Throwable error) {
         AutismClientAddon.LOG.debug("OreSim ghost model build failed", error);
         built = DataUtil.Inner_StreamUtil.init_DataUtil_StreamUti;
      }

      init_Map.put(state, built);
      return built;
   }

   private static DataUtil.Inner_StreamUtil init(BlockStateModelSet set, BlockState state) {
      init_List.clear();
      init_RandomSource.setSeed(42L);
      set.get(state).collectParts(init_RandomSource, init_List);
      List<DataUtil.Inner_DataUtil> faces = new ArrayList<>();

      for (BlockStateModelPart part : init_List) {
         for (Direction direction : Direction.values()) {
            init(part.getQuads(direction), direction, faces);
         }

         init(part.getQuads(null), null, faces);
      }

      init_List.clear();
      return faces.isEmpty() ? DataUtil.Inner_StreamUtil.init_DataUtil_StreamUti : new DataUtil.Inner_StreamUtil(List.copyOf(faces));
   }

   private static void init(List<BakedQuad> quads, Direction cull, List<DataUtil.Inner_DataUtil> out) {
      if (quads != null && !quads.isEmpty()) {
         for (BakedQuad quad : quads) {
            float[] data = new float[20];

            for (int i = 0; i < 4; i++) {
               Vector3fc position = quad.position(i);
               long uv = quad.packedUV(i);
               int base = i * 5;
               data[base] = position.x();
               data[base + 1] = position.y();
               data[base + 2] = position.z();
               data[base + 3] = UVPair.unpackU(uv);
               data[base + 4] = UVPair.unpackV(uv);
            }

            Direction facing = Compat263.materialInfoShade(quad.materialInfo()) ? quad.direction() : null;
            out.add(new DataUtil.Inner_DataUtil(cull, facing, data, quad.materialInfo().tintIndex()));
         }
      }
   }

   public record Inner_DataUtil(Direction init_Direction, Direction load, float[] init_FArr, int init_I) {
   }

   public record Inner_StreamUtil(List<DataUtil.Inner_DataUtil> init_List) {
      static final DataUtil.Inner_StreamUtil init_DataUtil_StreamUti = new DataUtil.Inner_StreamUtil(List.of());
   }
}
