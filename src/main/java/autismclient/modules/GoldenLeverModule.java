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

public final class GoldenLeverModule extends Module {
   public static final int init_I = -11702;
   private static volatile boolean enabledCache;

   public GoldenLeverModule() {
      super("golden-lever", "GoldenLever", ae.write, "Renames and recolors vanilla levers.");
      enabledCache = this.isEnabledRaw();
   }

   @Override
   public void onEnable() {
      enabledCache = true;
      am.init_V();
   }

   @Override
   public void onDisable() {
      enabledCache = false;
      am.init_V();
   }

   public static boolean isActive() {
      return tintActive();
   }

   public static boolean isGoldenLever(ItemStack stack) {
      return tintActive() && stack != null && stack.is(Items.LEVER);
   }

   public static boolean isGoldenLeverBlock(BlockState state) {
      return tintActive() && state != null && state.is(Blocks.LEVER);
   }

   public static Component label() {
      return Component.literal("Golden Lever").withStyle(ChatFormatting.GOLD);
   }

   public static void tintLayers(ItemStackRenderState output) {
      if (output != null) {
         AutismItemStackRenderStateAccessor accessor = (AutismItemStackRenderStateAccessor)output;
         LayerRenderState[] layers = accessor.autism$getLayers();
         int count = Math.min(accessor.autism$getActiveLayerCount(), layers.length);

         for (int i = 0; i < count; i++) {
            tintLayer(layers[i]);
         }
      }
   }

   private static void tintLayer(LayerRenderState layer) {
      if (layer != null) {
         List<BakedQuad> quads = Compat263.prepareQuadList(layer);

         for (int i = 0; i < quads.size(); i++) {
            quads.set(i, tintQuad(quads.get(i)));
         }

         IntList tints = layer.tintLayers();
         tints.clear();
         tints.add(-11702);
      }
   }

   private static BakedQuad tintQuad(BakedQuad quad) {
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

   private static boolean tintActive() {
      return !AutismLiteVariant.enabled() && !autismclient.e.ListPool.init() ? false : enabledCache;
   }
}
