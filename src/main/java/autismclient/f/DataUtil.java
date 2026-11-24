package autismclient.f;

import autismclient.compat263.Compat263;
import autismclient.modules.M;
import autismclient.modules.aD;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Cube;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.joml.Vector3f;

public final class DataUtil {
   private static final float init_F = 0.78F;
   private static final Set<PlayerModel> init_Set = Collections.newSetFromMap(new WeakHashMap<>());
   private static final Set<PlayerModel> load_Set = Collections.newSetFromMap(new WeakHashMap<>());
   private static final DataUtil.ConfigStore init_DataUtil_ConfigSto = DataUtil.ConfigStore.init(DataUtil.CachePool.Inner_DataUtil.init_DataUtil_CachePool);
   private static final DataUtil.ConfigStore load_DataUtil_ConfigSto = DataUtil.ConfigStore.init(DataUtil.CachePool.Inner_DataUtil.load_DataUtil_CachePool);
   private static final DataUtil.ConfigStore read_DataUtil_ConfigSto = DataUtil.ConfigStore.init(DataUtil.CachePool.Inner_DataUtil.read);
   private static final DataUtil.ConfigStore write = DataUtil.ConfigStore.init(DataUtil.CachePool.Inner_DataUtil.write);
   private static final double init_D = 36.0;
   private static final double load_D = 324.0;
   private static final double read_D = 1024.0;
   private static final int init_I = 24;
   private static final Map<Identifier, DataUtil.StreamUtil> init_Map = new HashMap<>();
   private static WeakReference<Object> auditLevelRef = new WeakReference<>(null);
   private static long init_J = -9223372036854775808L;
   private static int load_I;
   private static boolean init_Z;
   private static volatile boolean load_Z;

   private DataUtil() {
   }

   public static void init() {
      if (!init_Z) {
         init_Z = true;
         LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, renderer, helper, context) -> {
            if (renderer instanceof AvatarRenderer<?> avatarRenderer) {
               PlayerModel playerModel = avatarRenderer.getModel();
               init_Set.add(playerModel);
               helper.register(new DataUtil.Inner_DataUtil(avatarRenderer, context.getEquipmentRenderer()));
            }
         });
      }
   }

   public static void init(ArmorModelSet<?> modelSet) {
      if (modelSet != null) {
         init(modelSet.head());
         init(modelSet.chest());
         init(modelSet.legs());
         init(modelSet.feet());
      }
   }

   private static void init(Object model) {
      if (model instanceof PlayerModel playerModel) {
         load_Set.add(playerModel);
      }
   }

   public static void init(PlayerModel model, AvatarRenderState state) {
      if (model != null && state != null && load_Z && M.init(state.id) && !aD.read_Z()) {
         model.leftArm.xScale = 0.78F;
         model.rightArm.xScale = 0.78F;
         if (init_Set.contains(model)) {
            model.body.visible = false;
            model.jacket.visible = false;
         } else if (load_Set.contains(model)) {
            model.body.visible = false;
         }
      }
   }

   public static void init(AvatarRenderState state, PoseStack poseStack) {
      if (state != null && poseStack != null && M.init(state.id)) {
         poseStack.scale(1.2820513F, 1.0F, 1.0F);
      }
   }

   private static final class BufferHelper extends Model<AvatarRenderState> {
      private BufferHelper(ModelPart root) {
         super(root, RenderTypes::entityTranslucent);
      }

      @Override
      public void setupAnim(AvatarRenderState state) {
      }
   }

   private static final class CachePool {
      private static final int init_I = 29;
      private static final int[] init_IArr = new int[]{0, 1, 27, 28};
      private static final float[] init_FArr = new float[]{
         0.0F, 0.5F, 1.0F, 1.5F, 2.0F, 2.5F, 3.0F, 3.5F, 4.0F, 4.5F, 5.1F, 5.5F, 5.8F, 6.15F, 6.45F, 6.7F, 7.0F, 8.5F, 12.0F
      };
      private static final float[] load_FArr = new float[]{0.0F, 1.0F, 2.5F, 4.8F, 6.15F, 8.5F, 12.0F};
      private static final float[] read_FArr = new float[]{4.4F, 4.66F, 4.78F, 4.55F, 4.2F, 3.35F, 4.2F};
      private static final float[] write_FArr = new float[]{-2.02F, -2.05F, -2.08F, -2.03F, -2.0F, -1.94F, -2.05F};
      private static final float[] update_FArr = new float[]{2.0F, 2.02F, 2.04F, 2.02F, 2.0F, 1.94F, 2.05F};
      private static final float init_F = 1.55F;
      private static final float load_F = 2.55F;
      private static final float read_F = 2.65F;
      private static final float write_F = 3.4F;
      private static final float update_F = 2.85F;
      private static final float sync = 4.05F;
      private static final float flush = 2.7F;
      private static final double init_D = 6.0;

      private static DataUtil.BufferHelper init_DataUtil_BufferHel(DataUtil.TaskQueue uv, float shellOffset, DataUtil.CachePool.Inner_DataUtil detail) {
         return init(List.of(new DataUtil.CachePool.StreamUtil(init_List(uv, shellOffset, detail))));
      }

      private static DataUtil.BufferHelper init_DataUtil_BufferHel_DataUtil_TaskQueue(
         DataUtil.TaskQueue firstUv, float firstOffset, DataUtil.TaskQueue secondUv, float secondOffset, DataUtil.CachePool.Inner_DataUtil detail
      ) {
         return init(
            List.of(
               new DataUtil.CachePool.StreamUtil(init_List(firstUv, firstOffset, detail)),
               new DataUtil.CachePool.StreamUtil(init_List(secondUv, secondOffset, detail))
            )
         );
      }

      private static DataUtil.BufferHelper init(List<Cube> cubes) {
         return new DataUtil.BufferHelper(new ModelPart(cubes, Map.of()));
      }

      private static List<DataUtil.CachePool.BufferHelper> init_List(DataUtil.TaskQueue uv, float shellOffset, DataUtil.CachePool.Inner_DataUtil detail) {
         DataUtil.CachePool.ConfigStore[][] front = init(true, shellOffset);
         DataUtil.CachePool.ConfigStore[][] back = init(false, shellOffset);
         List<DataUtil.CachePool.BufferHelper> quads = new ArrayList<>(640);

         for (int segment = 0; segment < detail.load_IArr.length - 1; segment++) {
            int topRow = detail.load_IArr[segment];
            int bottomRow = detail.load_IArr[segment + 1];
            int[] frontColumns = init_Z(init_FArr[topRow], init_FArr[bottomRow]) ? detail.init_IArr : init_IArr;
            init(quads, front, topRow, bottomRow, frontColumns, uv);
            init_V_List(quads, back, topRow, bottomRow, uv);
            init(quads, front, back, topRow, bottomRow, false, uv);
            init(quads, front, back, topRow, bottomRow, true, uv);
         }

         init(quads, front, back, detail.load_IArr[0], false, detail.init_IArr, uv);
         init(quads, front, back, detail.load_IArr[detail.load_IArr.length - 1], true, init_IArr, uv);
         return List.copyOf(quads);
      }

      private static void init(
         List<DataUtil.CachePool.BufferHelper> quads,
         DataUtil.CachePool.ConfigStore[][] surface,
         int topRow,
         int bottomRow,
         int[] columns,
         DataUtil.TaskQueue uv
      ) {
         for (int segment = 0; segment < columns.length - 1; segment++) {
            int left = columns[segment];
            int right = columns[segment + 1];
            quads.add(
               init(
                  init(surface[topRow][right], load(right), write(topRow), uv),
                  init(surface[topRow][left], load(left), write(topRow), uv),
                  init(surface[bottomRow][left], load(left), write(bottomRow), uv),
                  init(surface[bottomRow][right], load(right), write(bottomRow), uv)
               )
            );
         }
      }

      private static void init_V_List(
         List<DataUtil.CachePool.BufferHelper> quads, DataUtil.CachePool.ConfigStore[][] surface, int topRow, int bottomRow, DataUtil.TaskQueue uv
      ) {
         for (int segment = 0; segment < init_IArr.length - 1; segment++) {
            int left = init_IArr[segment];
            int right = init_IArr[segment + 1];
            quads.add(
               init(
                  init(surface[topRow][left], read(left), write(topRow), uv),
                  init(surface[topRow][right], read(right), write(topRow), uv),
                  init(surface[bottomRow][right], read(right), write(bottomRow), uv),
                  init(surface[bottomRow][left], read(left), write(bottomRow), uv)
               )
            );
         }
      }

      private static boolean init_Z(float topY, float bottomY) {
         return bottomY > -0.29999995F && topY < 6.6000004F;
      }

      private static DataUtil.CachePool.ConfigStore[][] init(boolean frontSurface, float shellOffset) {
         DataUtil.CachePool.ConfigStore[][] base = new DataUtil.CachePool.ConfigStore[init_FArr.length][29];

         for (int row = 0; row < init_FArr.length; row++) {
            float y = init_FArr[row];
            float halfWidth = init(y, read_FArr);
            float baseDepth = init(y, frontSurface ? write_FArr : update_FArr);

            for (int column = 0; column < 29; column++) {
               float factor = init_F(column);
               float x = factor * halfWidth;
               float z = frontSurface ? baseDepth - init_F(x, y) : baseDepth;
               base[row][column] = new DataUtil.CachePool.ConfigStore(x, y, z, new Vector3f());
            }
         }

         init(base, frontSurface);
         if (shellOffset == 0.0F) {
            return base;
         }

         DataUtil.CachePool.ConfigStore[][] expanded = new DataUtil.CachePool.ConfigStore[init_FArr.length][29];

         for (int row = 0; row < init_FArr.length; row++) {
            for (int column = 0; column < 29; column++) {
               DataUtil.CachePool.ConfigStore point = base[row][column];
               Vector3f normal = point.init_Vector3f;
               expanded[row][column] = new DataUtil.CachePool.ConfigStore(
                  point.init_F + normal.x() * shellOffset, point.load + normal.y() * shellOffset, point.read + normal.z() * shellOffset, new Vector3f(normal)
               );
            }
         }

         return expanded;
      }

      private static void init(DataUtil.CachePool.ConfigStore[][] surface, boolean frontSurface) {
         int lastRow = surface.length - 1;
         int lastColumn = surface[0].length - 1;

         for (int row = 0; row <= lastRow; row++) {
            int previousRow = Math.max(0, row - 1);
            int nextRow = Math.min(lastRow, row + 1);

            for (int column = 0; column <= lastColumn; column++) {
               int previousColumn = Math.max(0, column - 1);
               int nextColumn = Math.min(lastColumn, column + 1);
               Vector3f horizontal = init(surface[row][nextColumn], surface[row][previousColumn]);
               Vector3f vertical = init(surface[nextRow][column], surface[previousRow][column]);
               Vector3f normal = frontSurface ? vertical.cross(horizontal) : horizontal.cross(vertical);
               if (normal.lengthSquared() < 1.0E-6F) {
                  normal.set(0.0F, 0.0F, frontSurface ? -1.0F : 1.0F);
               } else {
                  normal.normalize();
               }

               surface[row][column].init_Vector3f.set(normal);
            }
         }
      }

      private static float init_F(float x, float y) {
         float leftDx = x + 1.55F;
         float rightDx = x - 1.55F;
         float left = init(leftDx, y - 2.55F, leftDx < 0.0F ? 3.4F : 2.65F);
         float right = init(rightDx, y - 2.55F, rightDx > 0.0F ? 3.4F : 2.65F);
         if (left <= 0.0F) {
            return right * 2.7F;
         }

         if (right <= 0.0F) {
            return left * 2.7F;
         }

         double blended = Math.pow(Math.pow(left, 6.0) + Math.pow(right, 6.0), 0.16666666666666666);
         return (float)blended * 2.7F;
      }

      private static float init(float dx, float dy, float radiusX) {
         float nx = dx / radiusX;
         float ny = dy / (dy < 0.0F ? 2.85F : 4.05F);
         float radiusSquared = nx * nx + ny * ny;
         if (radiusSquared >= 1.0F) {
            return 0.0F;
         }

         float inside = 1.0F - radiusSquared;
         return inside * inside * (3.0F - 2.0F * inside);
      }

      private static void init(
         List<DataUtil.CachePool.BufferHelper> quads,
         DataUtil.CachePool.ConfigStore[][] front,
         DataUtil.CachePool.ConfigStore[][] back,
         int topRow,
         int bottomRow,
         boolean east,
         DataUtil.TaskQueue uv
      ) {
         int column = east ? 28 : 0;
         DataUtil.CachePool.ConfigStore frontTop = front[topRow][column];
         DataUtil.CachePool.ConfigStore backTop = back[topRow][column];
         DataUtil.CachePool.ConfigStore frontBottom = front[bottomRow][column];
         DataUtil.CachePool.ConfigStore backBottom = back[bottomRow][column];
         float frontU = east ? 28.0F : 20.0F;
         float backU = east ? 32.0F : 16.0F;
         Vector3f normal = init(east ? backTop : frontTop, east ? frontTop : backTop, east ? frontBottom : backBottom);
         if (east) {
            quads.add(
               init(
                  init_DataUtil_CachePool_DataUtil_CachePool(backTop, backU, write(topRow), normal, uv),
                  init_DataUtil_CachePool_DataUtil_CachePool(frontTop, frontU, write(topRow), normal, uv),
                  init_DataUtil_CachePool_DataUtil_CachePool(frontBottom, frontU, write(bottomRow), normal, uv),
                  init_DataUtil_CachePool_DataUtil_CachePool(backBottom, backU, write(bottomRow), normal, uv)
               )
            );
         } else {
            quads.add(
               init(
                  init_DataUtil_CachePool_DataUtil_CachePool(frontTop, frontU, write(topRow), normal, uv),
                  init_DataUtil_CachePool_DataUtil_CachePool(backTop, backU, write(topRow), normal, uv),
                  init_DataUtil_CachePool_DataUtil_CachePool(backBottom, backU, write(bottomRow), normal, uv),
                  init_DataUtil_CachePool_DataUtil_CachePool(frontBottom, frontU, write(bottomRow), normal, uv)
               )
            );
         }
      }

      private static void init(
         List<DataUtil.CachePool.BufferHelper> quads,
         DataUtil.CachePool.ConfigStore[][] front,
         DataUtil.CachePool.ConfigStore[][] back,
         int row,
         boolean bottom,
         int[] columns,
         DataUtil.TaskQueue uv
      ) {
         Vector3f normal = new Vector3f(0.0F, bottom ? 1.0F : -1.0F, 0.0F);

         for (int segment = 0; segment < columns.length - 1; segment++) {
            int left = columns[segment];
            int right = columns[segment + 1];
            DataUtil.CachePool.ConfigStore frontLeft = front[row][left];
            DataUtil.CachePool.ConfigStore frontRight = front[row][right];
            DataUtil.CachePool.ConfigStore backLeft = back[row][left];
            DataUtil.CachePool.ConfigStore backRight = back[row][right];
            float uLeft = (bottom ? 32.0F : 24.0F) + init_F(left) * 4.0F;
            float uRight = (bottom ? 32.0F : 24.0F) + init_F(right) * 4.0F;
            float frontCapV = uv.write + 4.0F;
            float backCapV = uv.write;
            if (bottom) {
               quads.add(
                  init(
                     load(frontRight, uRight, frontCapV, normal, uv),
                     load(frontLeft, uLeft, frontCapV, normal, uv),
                     load(backLeft, uLeft, backCapV, normal, uv),
                     load(backRight, uRight, backCapV, normal, uv)
                  )
               );
            } else {
               quads.add(
                  init(
                     load(backRight, uRight, backCapV, normal, uv),
                     load(backLeft, uLeft, backCapV, normal, uv),
                     load(frontLeft, uLeft, frontCapV, normal, uv),
                     load(frontRight, uRight, frontCapV, normal, uv)
                  )
               );
            }
         }
      }

      private static int[] init_IArr(int length) {
         int[] values = new int[length];
         int index = 0;

         while (index < length) {
            values[index] = index++;
         }

         return values;
      }

      private static float init(float y, float[] values) {
         if (y <= load_FArr[0]) {
            return values[0];
         }

         for (int i = 1; i < load_FArr.length; i++) {
            if (y <= load_FArr[i]) {
               float progress = (y - load_FArr[i - 1]) / (load_FArr[i] - load_FArr[i - 1]);
               return values[i - 1] + (values[i] - values[i - 1]) * progress;
            }
         }

         return values[values.length - 1];
      }

      private static float init_F(int column) {
         return -1.0F + 2.0F * column / 28.0F;
      }

      private static float load(int column) {
         return 24.0F + init_F(column) * 4.0F;
      }

      private static float read(int column) {
         return 36.0F - init_F(column) * 4.0F;
      }

      private static float write(int row) {
         return init_FArr[row];
      }

      private static Vector3f init(DataUtil.CachePool.ConfigStore a, DataUtil.CachePool.ConfigStore b) {
         return new Vector3f(a.init_F - b.init_F, a.load - b.load, a.read - b.read);
      }

      private static Vector3f init(DataUtil.CachePool.ConfigStore a, DataUtil.CachePool.ConfigStore b, DataUtil.CachePool.ConfigStore c) {
         Vector3f ab = init(b, a);
         Vector3f ac = init(c, a);
         Vector3f normal = ab.cross(ac);
         return normal.lengthSquared() < 1.0E-6F ? new Vector3f(0.0F, 0.0F, -1.0F) : normal.normalize();
      }

      private static DataUtil.CachePool.Inner_CachePool init(DataUtil.CachePool.ConfigStore point, float u, float v, DataUtil.TaskQueue uv) {
         return init_DataUtil_CachePool_DataUtil_CachePool(point, u, v, point.init_Vector3f, uv);
      }

      private static DataUtil.CachePool.Inner_CachePool init_DataUtil_CachePool_DataUtil_CachePool(
         DataUtil.CachePool.ConfigStore point, float u, float v, Vector3f normal, DataUtil.TaskQueue uv
      ) {
         return new DataUtil.CachePool.Inner_CachePool(
            point.init_F, point.load, point.read, u / uv.init_F, (uv.read_F + v) / uv.load_F, normal.x(), normal.y(), normal.z()
         );
      }

      private static DataUtil.CachePool.Inner_CachePool load(DataUtil.CachePool.ConfigStore point, float u, float v, Vector3f normal, DataUtil.TaskQueue uv) {
         return new DataUtil.CachePool.Inner_CachePool(point.init_F, point.load, point.read, u / uv.init_F, v / uv.load_F, normal.x(), normal.y(), normal.z());
      }

      private static DataUtil.CachePool.BufferHelper init(
         DataUtil.CachePool.Inner_CachePool a, DataUtil.CachePool.Inner_CachePool b, DataUtil.CachePool.Inner_CachePool c, DataUtil.CachePool.Inner_CachePool d
      ) {
         return new DataUtil.CachePool.BufferHelper(a, b, c, d);
      }

      private static final class BufferHelper {
         private final DataUtil.CachePool.Inner_CachePool[] init;

         private BufferHelper(
            DataUtil.CachePool.Inner_CachePool a,
            DataUtil.CachePool.Inner_CachePool b,
            DataUtil.CachePool.Inner_CachePool c,
            DataUtil.CachePool.Inner_CachePool d
         ) {
            this.init = new DataUtil.CachePool.Inner_CachePool[]{a, b, c, d};
         }
      }

      private static final class ConfigStore {
         private final float init_F;
         private final float load;
         private final float read;
         private final Vector3f init_Vector3f;

         private ConfigStore(float x, float y, float z, Vector3f normal) {
            this.init_F = x;
            this.load = y;
            this.read = z;
            this.init_Vector3f = normal;
         }
      }

      private record Inner_CachePool(float init, float load, float read, float write, float update, float sync, float flush, float close) {
      }

      private record Inner_DataUtil(int[] init_IArr, int[] load_IArr) {
         private static final DataUtil.CachePool.Inner_DataUtil init_DataUtil_CachePool = new DataUtil.CachePool.Inner_DataUtil(
            DataUtil.CachePool.init_IArr(29), DataUtil.CachePool.init_IArr(DataUtil.CachePool.init_FArr.length)
         );
         private static final DataUtil.CachePool.Inner_DataUtil load_DataUtil_CachePool = new DataUtil.CachePool.Inner_DataUtil(
            new int[]{0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28}, new int[]{0, 1, 3, 5, 7, 9, 10, 12, 14, 16, 17, 18}
         );
         private static final DataUtil.CachePool.Inner_DataUtil read = new DataUtil.CachePool.Inner_DataUtil(
            new int[]{0, 4, 8, 12, 16, 20, 24, 28}, new int[]{0, 2, 5, 8, 10, 13, 16, 17, 18}
         );
         private static final DataUtil.CachePool.Inner_DataUtil write = new DataUtil.CachePool.Inner_DataUtil(
            new int[]{0, 7, 10, 14, 18, 21, 28}, new int[]{0, 5, 8, 10, 13, 16, 18}
         );
      }

      private static final class StreamUtil extends Cube {
         private final DataUtil.CachePool.Inner_CachePool[] init_DataUtil_CachePoolArr;
         private final Vector3f init_Vector3f = new Vector3f();
         private final Vector3f load = new Vector3f();

         private StreamUtil(List<DataUtil.CachePool.BufferHelper> quads) {
            super(0, 0, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, false, 64.0F, 64.0F, Set.of());
            this.init_DataUtil_CachePoolArr = new DataUtil.CachePool.Inner_CachePool[quads.size() * 4];
            int index = 0;

            for (DataUtil.CachePool.BufferHelper quad : quads) {
               for (DataUtil.CachePool.Inner_CachePool point : quad.init) {
                  this.init_DataUtil_CachePoolArr[index++] = point;
               }
            }
         }

         @Override
         public void compile(Pose pose, VertexConsumer output, int lightCoords, int overlayCoords, int color) {
            for (int index = 0; index < this.init_DataUtil_CachePoolArr.length; index++) {
               DataUtil.CachePool.Inner_CachePool point = this.init_DataUtil_CachePoolArr[index];
               pose.transformNormal(point.sync, point.flush, point.close, this.init_Vector3f);
               pose.pose().transformPosition(point.init / 16.0F, point.load / 16.0F, point.read / 16.0F, this.load);
               output.addVertex(
                  this.load.x(),
                  this.load.y(),
                  this.load.z(),
                  color,
                  point.write,
                  point.update,
                  overlayCoords,
                  lightCoords,
                  this.init_Vector3f.x(),
                  this.init_Vector3f.y(),
                  this.init_Vector3f.z()
               );
            }
         }
      }
   }

   private record ConfigStore(
      DataUtil.BufferHelper init, DataUtil.BufferHelper load, DataUtil.BufferHelper read, DataUtil.BufferHelper write, DataUtil.BufferHelper update
   ) {
      private static DataUtil.ConfigStore init(DataUtil.CachePool.Inner_DataUtil detail) {
         return new DataUtil.ConfigStore(
            DataUtil.CachePool.init_DataUtil_BufferHel(DataUtil.TaskQueue.init_DataUtil_TaskQueue, 0.0F, detail),
            DataUtil.CachePool.init_DataUtil_BufferHel(DataUtil.TaskQueue.load_DataUtil_TaskQueue, 0.25F, detail),
            DataUtil.CachePool.init_DataUtil_BufferHel_DataUtil_TaskQueue(
               DataUtil.TaskQueue.init_DataUtil_TaskQueue, 0.0F, DataUtil.TaskQueue.load_DataUtil_TaskQueue, 0.25F, detail
            ),
            DataUtil.CachePool.init_DataUtil_BufferHel(DataUtil.TaskQueue.read_DataUtil_TaskQueue, 0.58F, detail),
            DataUtil.CachePool.init_DataUtil_BufferHel(DataUtil.TaskQueue.read_DataUtil_TaskQueue, 0.32F, detail)
         );
      }
   }

   private static final class Inner_DataUtil extends RenderLayer<AvatarRenderState, PlayerModel> {
      private final EquipmentLayerRenderer init;

      private Inner_DataUtil(RenderLayerParent<AvatarRenderState, PlayerModel> renderer, EquipmentLayerRenderer equipmentRenderer) {
         super(renderer);
         this.init = equipmentRenderer;
      }

      public void submit(PoseStack poseStack, SubmitNodeCollector output, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
         this.init(poseStack, output, lightCoords, state, yRot, xRot);
      }

      public void init(PoseStack poseStack, SubmitNodeCollector output, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
         if (M.init(state.id) && !aD.read_Z() && !state.isSpectator) {
            DataUtil.load_Z = true;
            poseStack.pushPose();
            try {
               PlayerModel parent = this.getParentModel();
               parent.root().translateAndRotate(poseStack);
               parent.body.translateAndRotate(poseStack);
               DataUtil.ConfigStore meshes = init(state);
               init(meshes, state, poseStack, output, lightCoords);
               this.init(state.chestEquipment, EquipmentSlot.CHEST, LayerType.HUMANOID, meshes.write, state, poseStack, output, lightCoords, 3);
               this.init(state.legsEquipment, EquipmentSlot.LEGS, LayerType.HUMANOID_LEGGINGS, meshes.update, state, poseStack, output, lightCoords, 8);
            } finally {
               poseStack.popPose();
            }
         }
      }

      private static void init(DataUtil.ConfigStore meshes, AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector output, int lightCoords) {
         Identifier texture = state.skin.body().texturePath();
         int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
         DataUtil.StreamUtil jacketAlpha = state.showJacket ? init(texture) : DataUtil.StreamUtil.load;
         if (!state.isInvisible) {
            if (meshes == DataUtil.write) {
               DataUtil.BufferHelper crowdMesh = jacketAlpha == DataUtil.StreamUtil.load ? meshes.init : meshes.read;
               Compat263.submitModel(
                  output.order(0),
                  crowdMesh,
                  state,
                  poseStack,
                  RenderTypes.entityTranslucent(texture),
                  lightCoords,
                  overlay,
                  -1,
                  null,
                  state.outlineColor,
                  null
               );
            } else {
               Compat263.submitModel(
                  output.order(0), meshes.init, state, poseStack, RenderTypes.entitySolid(texture), lightCoords, overlay, -1, null, state.outlineColor, null
               );
               if (jacketAlpha != DataUtil.StreamUtil.load) {
                  RenderType var10000;
                  switch (jacketAlpha) {
                     case init:
                     case update:
                        var10000 = net.minecraft.client.renderer.rendertype.CachePool.read(texture);
                        break;
                     case load:
                        throw new IllegalStateException("Empty jacket submitted");
                     case read:
                        var10000 = RenderTypes.entitySolid(texture);
                        break;
                     case write:
                        var10000 = RenderTypes.entityCutoutCull(texture);
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  RenderType jacketRenderType = var10000;
                  Compat263.submitModel(
                     output.order(0), meshes.load, state, poseStack, jacketRenderType, lightCoords, overlay, -1, null, state.outlineColor, null
                  );
               }
            }
         } else {
            DataUtil.BufferHelper visibleMesh = jacketAlpha == DataUtil.StreamUtil.load ? meshes.init : meshes.read;
            if (!state.isInvisibleToPlayer) {
               Compat263.submitModel(
                  output.order(0),
                  visibleMesh,
                  state,
                  poseStack,
                  net.minecraft.client.renderer.rendertype.CachePool.read(texture),
                  lightCoords,
                  overlay,
                  654311423,
                  null,
                  state.outlineColor,
                  null
               );
            } else if (state.appearsGlowing()) {
               Compat263.submitModel(
                  output.order(0), visibleMesh, state, poseStack, RenderTypes.outline(texture), lightCoords, overlay, -1, null, state.outlineColor, null
               );
            }
         }
      }

      private static DataUtil.ConfigStore init(AvatarRenderState state) {
         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft.player != null && minecraft.player.getId() != state.id && init(minecraft) >= 24) {
            return DataUtil.write;
         } else {
            double distanceToCameraSq = state.distanceToCameraSq;
            if (distanceToCameraSq <= 36.0) {
               return DataUtil.init_DataUtil_ConfigSto;
            } else {
               return distanceToCameraSq <= 324.0 ? DataUtil.load_DataUtil_ConfigSto : DataUtil.read_DataUtil_ConfigSto;
            }
         }
      }

      private static int init(Minecraft minecraft) {
         if (minecraft.level != null && minecraft.player != null) {
            long tick = minecraft.level.getGameTime();
            if (minecraft.level == DataUtil.auditLevelRef.get() && tick == DataUtil.init_J) {
               return DataUtil.load_I;
            }

            DataUtil.auditLevelRef = new WeakReference<>(minecraft.level);
            DataUtil.init_J = tick;
            int count = 0;

            for (AbstractClientPlayer player : minecraft.level.players()) {
               if (player.distanceToSqr(minecraft.player) <= 1024.0) {
                  count++;
               }
            }

            DataUtil.load_I = count;
            return count;
         } else {
            return 0;
         }
      }

      private static DataUtil.StreamUtil init(Identifier textureId) {
         DataUtil.StreamUtil cached = DataUtil.init_Map.get(textureId);
         if (cached != null) {
            return cached;
         } else if (!(Minecraft.getInstance().getTextureManager().getTexture(textureId) instanceof DynamicTexture dynamicTexture)) {
            if (DataUtil.init_Map.size() >= 1024) {
               DataUtil.init_Map.clear();
            }
            DataUtil.init_Map.put(textureId, DataUtil.StreamUtil.init);
            return DataUtil.StreamUtil.init;
         } else {
            NativeImage pixels = dynamicTexture.getPixels();
            if (pixels != null && !pixels.isClosed() && pixels.getWidth() >= 64 && pixels.getHeight() >= 64) {
               boolean hasTransparent = false;
               boolean hasVisible = false;
               boolean hasPartial = false;

               for (int y = 32; y < 48; y++) {
                  for (int x = 16; x < 40; x++) {
                     int alpha = ARGB.alpha(pixels.getPixel(x, y));
                     hasTransparent |= alpha == 0;
                     hasVisible |= alpha != 0;
                     hasPartial |= alpha > 0 && alpha < 255;
                  }
               }

               DataUtil.StreamUtil result;
               if (!hasVisible) {
                  result = DataUtil.StreamUtil.load;
               } else if (hasPartial) {
                  result = DataUtil.StreamUtil.update;
               } else if (hasTransparent) {
                  result = DataUtil.StreamUtil.write;
               } else {
                  result = DataUtil.StreamUtil.read;
               }

               if (DataUtil.init_Map.size() >= 1024) {
                  DataUtil.init_Map.clear();
               }
               DataUtil.init_Map.put(textureId, result);
               return result;
            } else {
               return DataUtil.StreamUtil.init;
            }
         }
      }

      private void init(
         ItemStack stack,
         EquipmentSlot expectedSlot,
         LayerType layerType,
         DataUtil.BufferHelper model,
         AvatarRenderState state,
         PoseStack poseStack,
         SubmitNodeCollector output,
         int lightCoords,
         int order
      ) {
         if (stack != null && !stack.isEmpty()) {
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
            if (equippable != null && equippable.slot() == expectedSlot && !equippable.assetId().isEmpty()) {
               this.init
                  .renderLayers(
                     layerType,
                     equippable.assetId().orElseThrow(),
                     model,
                     state,
                     stack,
                     poseStack,
                     output,
                     lightCoords,
                     state.skin.body().texturePath(),
                     state.outlineColor,
                     order
                  );
            }
         }
      }
   }

   private enum StreamUtil {
      init,
      load,
      read,
      write,
      update;
   }

   private record TaskQueue(float init_F, float load_F, float read_F, float write) {
      private static final DataUtil.TaskQueue init_DataUtil_TaskQueue = new DataUtil.TaskQueue(64.0F, 64.0F, 20.0F, 16.0F);
      private static final DataUtil.TaskQueue load_DataUtil_TaskQueue = new DataUtil.TaskQueue(64.0F, 64.0F, 36.0F, 32.0F);
      private static final DataUtil.TaskQueue read_DataUtil_TaskQueue = new DataUtil.TaskQueue(64.0F, 32.0F, 20.0F, 16.0F);
   }
}
