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

public final class OreGhostModelCache {
   public static final int init_I = 5;
   private static final RandomSource RANDOM = RandomSource.create(42L);
   private static final Map<BlockState, OreGhostModelCache.Inner_StreamUtil> MODELS = new LinkedHashMap<BlockState, OreGhostModelCache.Inner_StreamUtil>(128, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Entry<BlockState, OreGhostModelCache.Inner_StreamUtil> eldest) {
         return this.size() > 4096;
      }
   };
   private static final List<BlockStateModelPart> PARTS = new ArrayList<>();
   private static Object modelSet;

   private OreGhostModelCache() {
   }

   public static void init() {
      MODELS.clear();
      modelSet = null;
   }

   public static OreGhostModelCache.Inner_StreamUtil init(BlockState state) {
      if (state == null) {
         return OreGhostModelCache.Inner_StreamUtil.EMPTY;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc == null) {
         return OreGhostModelCache.Inner_StreamUtil.EMPTY;
      }

      BlockStateModelSet set = mc.getModelManager().getBlockStateModelSet();
      if (set == null) {
         return OreGhostModelCache.Inner_StreamUtil.EMPTY;
      }

      if (set != modelSet) {
         MODELS.clear();
         modelSet = set;
      }

      OreGhostModelCache.Inner_StreamUtil cached = MODELS.get(state);
      if (cached != null) {
         return cached;
      }

      OreGhostModelCache.Inner_StreamUtil built;
      try {
         built = init(set, state);
      } catch (Throwable error) {
         AutismClientAddon.LOG.debug("OreSim ghost model build failed", error);
         built = OreGhostModelCache.Inner_StreamUtil.EMPTY;
      }

      MODELS.put(state, built);
      return built;
   }

   private static OreGhostModelCache.Inner_StreamUtil init(BlockStateModelSet set, BlockState state) {
      PARTS.clear();
      RANDOM.setSeed(42L);
      set.get(state).collectParts(RANDOM, PARTS);
      List<OreGhostModelCache.Inner_DataUtil> faces = new ArrayList<>();

      for (BlockStateModelPart part : PARTS) {
         for (Direction direction : Direction.values()) {
            init(part.getQuads(direction), direction, faces);
         }

         init(part.getQuads(null), null, faces);
      }

      PARTS.clear();
      return faces.isEmpty() ? OreGhostModelCache.Inner_StreamUtil.EMPTY : new OreGhostModelCache.Inner_StreamUtil(List.copyOf(faces));
   }

   private static void init(List<BakedQuad> quads, Direction cull, List<OreGhostModelCache.Inner_DataUtil> out) {
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
            out.add(new OreGhostModelCache.Inner_DataUtil(cull, facing, data, quad.materialInfo().tintIndex()));
         }
      }
   }

   public record Inner_DataUtil(Direction cullFace, Direction load, float[] vertices, int tintIndex) {
   }

   public record Inner_StreamUtil(List<OreGhostModelCache.Inner_DataUtil> faces) {
      static final OreGhostModelCache.Inner_StreamUtil EMPTY = new OreGhostModelCache.Inner_StreamUtil(List.of());
   }
}
