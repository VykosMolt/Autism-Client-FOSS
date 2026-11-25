package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.StringListSetting;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismItemStackRenderStateAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class M extends Module {
   public static final int init_I = -11702;
   private static volatile boolean init_Z;
   private static volatile boolean load;
   private static volatile boolean read;
   private static volatile boolean write;
   private static volatile boolean update;
   private static volatile boolean sync;
   private static volatile Set<String> init_Set = Set.of();

   public M() {
      super("golden-lever", "GoldenLever", ae.write, "Renames and recolors vanilla levers.");
      this.add(new BoolSetting("female-body", "Female Body", false).group("Appearance").description("Shape player bodies."));
      this.add(
         new BoolSetting("female-body-self-only", "Self Only", true)
            .group("Appearance")
            .description("Only affect you.")
            .visibleWhen(() -> Boolean.parseBoolean(this.value("female-body")))
      );
      this.add(
         new BoolSetting("female-body-only-others", "Only Others", false)
            .group("Appearance")
            .description("Only affect others.")
            .visibleWhen(
               () -> Boolean.parseBoolean(this.value("female-body"))
                  && !Boolean.parseBoolean(this.value("female-body-self-only"))
                  && !Boolean.parseBoolean(this.value("female-body-custom-players"))
            )
      );
      this.add(
         new BoolSetting("female-body-custom-players", "Custom Players", false)
            .group("Appearance")
            .description("Use selected players.")
            .visibleWhen(() -> Boolean.parseBoolean(this.value("female-body")) && !Boolean.parseBoolean(this.value("female-body-self-only")))
      );
      this.add(
         new BoolSetting("female-body-custom-include-self", "Include Self", true)
            .group("Appearance")
            .description("Also include you.")
            .visibleWhen(
               () -> Boolean.parseBoolean(this.value("female-body"))
                  && !Boolean.parseBoolean(this.value("female-body-self-only"))
                  && Boolean.parseBoolean(this.value("female-body-custom-players"))
            )
      );
      this.add(
         new StringListSetting("female-body-player-names", "Players", "")
            .group("Appearance")
            .description("Choose player names.")
            .playerNameList()
            .visibleWhen(
               () -> Boolean.parseBoolean(this.value("female-body"))
                  && !Boolean.parseBoolean(this.value("female-body-self-only"))
                  && Boolean.parseBoolean(this.value("female-body-custom-players"))
            )
      );
      init_Z = this.isEnabledRaw();
      this.init_V_10();
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

   @Override
   protected void onOptionValueChanged(String optionId) {
      if (optionId != null && optionId.startsWith("female-body")) {
         this.init_V_10();
      }
   }

   @Override
   protected void onSettingsReset() {
      load = false;
      read = true;
      write = false;
      update = false;
      sync = true;
      init_Set = Set.of();
   }

   public static boolean init_Z_11() {
      return read();
   }

   public static boolean load() {
      return read() && load && !aD.read_Z();
   }

   public static boolean init(int entityId) {
      if (!load()) {
         return false;
      }

      if (MC.player == null) {
         return false;
      }

      boolean self = MC.player.getId() == entityId;
      if (read) {
         return self;
      }

      if (update) {
         if (self) {
            return sync;
         } else if (!init_Set.isEmpty() && MC.level != null) {
            return MC.level.getEntity(entityId) instanceof Player player && player.getGameProfile() != null && player.getGameProfile().name() != null
               ? init_Set.contains(init(player.getGameProfile().name()))
               : false;
         } else {
            return false;
         }
      } else {
         return !write || !self;
      }
   }

   private void init_V_10() {
      load = this.bool("female-body");
      read = this.bool("female-body-self-only");
      write = this.bool("female-body-only-others");
      update = this.bool("female-body-custom-players");
      sync = this.bool("female-body-custom-include-self");
      LinkedHashSet<String> names = new LinkedHashSet<>();

      for (String entry : this.list("female-body-player-names")) {
         String normalized = init(entry);
         if (!normalized.isEmpty()) {
            names.add(normalized);
         }
      }

      init_Set = Set.copyOf(names);
   }

   private static String init(String name) {
      return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
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
      return init_Z;
   }
}
