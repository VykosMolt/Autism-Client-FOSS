package autismclient.util;

import java.util.Optional;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public final class MapTooltipPreview {
   private static final int init_I = 128;
   private static final int load = 2;
   private static final MapNonEmptyCache init_da = new MapNonEmptyCache();
   private static final MapRenderState init_MapRenderState = new MapRenderState();
   private static MapId init_MapId;
   private static int read = -2147483648;
   private static MapItemSavedData init_MapItemSavedData;
   private static Object init_Obj;
   private static MapTooltipPreview.Inner_StreamUtil init_aQ_StreamUtil;
   private static MapTooltipPreview.Inner_DataUtil init_aQ_DataUtil;

   private MapTooltipPreview() {
   }

   public static void init_V() {
      ClientTooltipComponentCallback.EVENT.register(data -> {
         if (!(data instanceof MapTooltipPreview.Inner_StreamUtil tooltip)) {
            return null;
         } else {
            if (init_aQ_DataUtil == null || !tooltip.equals(init_aQ_StreamUtil)) {
               init_aQ_StreamUtil = tooltip;
               init_aQ_DataUtil = new MapTooltipPreview.Inner_DataUtil(tooltip.init());
            }

            return init_aQ_DataUtil;
         }
      });
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> load());
   }

   public static Optional<TooltipComponent> init(ItemStack stack, Optional<TooltipComponent> original) {
      if (!original.isPresent() && stack != null && !stack.isEmpty() && autismclient.modules.InventoryTweaksModule.isMapPreviewEnabled()) {
         MapId id = stack.get(DataComponents.MAP_ID);
         if (init_MapItemSavedData(id) == null) {
            return original;
         }

         MapTooltipPreview.Inner_StreamUtil cached = init_aQ_StreamUtil;
         return Optional.of(cached != null && cached.init().equals(id) ? cached : new MapTooltipPreview.Inner_StreamUtil(id));
      } else {
         return original;
      }
   }

   private static int init_I() {
      Minecraft mc = Minecraft.getInstance();
      int height = mc == null ? 0 : mc.getWindow().getGuiScaledHeight();
      return height * 2 >= 640 ? 128 : 64;
   }

   private static MapItemSavedData init_MapItemSavedData(MapId id) {
      Minecraft mc = Minecraft.getInstance();
      if (id != null && mc != null && mc.level != null && autismclient.modules.InventoryTweaksModule.isMapPreviewEnabled()) {
         MapItemSavedData data = MapItem.getSavedData(id, mc.level);
         return init_da.init(mc.level, id, data, AutismSessionState.getInstance().getClientTick()) ? data : null;
      } else {
         return null;
      }
   }

   public static void init_V(MapId id) {
      init_da.init(id);
      if (id != null && id.equals(init_MapId)) {
         init_MapItemSavedData = null;
      }
   }

   private static void load() {
      init_da.init();
      init_MapId = null;
      init_MapItemSavedData = null;
      init_Obj = null;
      read = -2147483648;
      init_MapRenderState.texture = null;
      init_MapRenderState.decorations.clear();
      init_aQ_StreamUtil = null;
      init_aQ_DataUtil = null;
   }

   private static MapItemSavedData load(MapId id) {
      Minecraft mc = Minecraft.getInstance();
      MapItemSavedData data = init_MapItemSavedData(id);
      if (data == null) {
         return null;
      }

      int tick = AutismSessionState.getInstance().getClientTick();
      if (id.equals(init_MapId) && tick == read && init_Obj == mc.level && init_MapItemSavedData == data) {
         return data;
      }

      mc.getMapRenderer().extractRenderState(id, data, init_MapRenderState);
      init_Obj = mc.level;
      init_MapId = id;
      read = tick;
      init_MapItemSavedData = data;
      return data;
   }

   static final class Inner_DataUtil implements ClientTooltipComponent {
      private final MapId init;

      Inner_DataUtil(MapId id) {
         this.init = id;
      }

      @Override
      public int getHeight(Font font) {
         return MapTooltipPreview.init_MapItemSavedData(this.init) == null ? 0 : MapTooltipPreview.init_I() + 4;
      }

      @Override
      public int getWidth(Font font) {
         return MapTooltipPreview.init_MapItemSavedData(this.init) == null ? 0 : MapTooltipPreview.init_I();
      }

      @Override
      public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
         try {
            if (MapTooltipPreview.load(this.init) == null) {
               return;
            }

            float scale = MapTooltipPreview.init_I() / 128.0F;
            graphics.pose().pushMatrix();

            try {
               graphics.pose().translate(x, y + 2);
               graphics.pose().scale(scale, scale);
               graphics.map(MapTooltipPreview.init_MapRenderState);
            } finally {
               graphics.pose().popMatrix();
            }
         } catch (Throwable var12) {
         }
      }
   }

   public record Inner_StreamUtil(MapId init) implements TooltipComponent {
   }
}
