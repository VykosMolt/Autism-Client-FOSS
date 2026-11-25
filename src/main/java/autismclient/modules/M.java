package autismclient.modules;

import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismItemStackRenderStateAccessor;
import autismclient.util.AutismLiteVariant;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class M extends Module {
   public static final int init_I = -11702;
   private static volatile boolean init_Z;

   public M() {
      super("golden-lever", "GoldenLever", ae.write, "Renames and recolors vanilla levers.");
      init_Z = this.isEnabledRaw();
   }

   @Override
   public void onEnable() {
      init_Z = true;
      am.init_V();
   }

   @Override
   public void onDisable() {
      init_Z = false;
      am.init_V();
   }

   public static boolean init_Z_11() {
      return read();
   }

   public static boolean init(ItemStack stack) {
      return read() && stack != null && stack.is(Items.LEVER);
   }

   public static boolean init(BlockState state) {
      return read() && state != null && state.is(Blocks.LEVER);
   }

   public static Component init_Component() {
      return Component.literal("Golden Lever").withStyle(ChatFormatting.GOLD);
   }

   public static void init(ItemStackRenderState output) {
      if (output != null) {
         AutismItemStackRenderStateAccessor accessor = (AutismItemStackRenderStateAccessor)output;
         LayerRenderState[] layers = accessor.autism$getLayers();
         int count = Math.min(accessor.autism$getActiveLayerCount(), layers.length);

         for (int i = 0; i < count; i++) {
            init(layers[i]);
         }
      }
   }

   private static void init(LayerRenderState layer) {
      if (layer != null) {
         List<BakedQuad> quads = Compat263.prepareQuadList(layer);

         for (int i = 0; i < quads.size(); i++) {
            quads.set(i, init(quads.get(i)));
         }

         IntList tints = layer.tintLayers();
         tints.clear();
         tints.add(-11702);
      }
   }

   private static BakedQuad init(BakedQuad quad) {
      MaterialInfo materialInfo = quad.materialInfo();
      if (materialInfo.tintIndex() == 0) {
         return quad;
      }

      MaterialInfo tintedInfo = Compat263.newMaterialInfo(
         materialInfo.sprite(), materialInfo.layer(), materialInfo.itemRenderType(), 0, Compat263.materialInfoShade(materialInfo), materialInfo.lightEmission()
      );
      return new BakedQuad(
         quad.position0(),
         quad.position1(),
         quad.position2(),
         quad.position3(),
         quad.packedUV0(),
         quad.packedUV1(),
         quad.packedUV2(),
         quad.packedUV3(),
         quad.direction(),
         tintedInfo
      );
   }

   private static boolean read() {
      return !AutismLiteVariant.enabled() && !autismclient.e.ListPool.init() ? false : init_Z;
   }
}
