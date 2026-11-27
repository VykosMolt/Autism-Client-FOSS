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
   private static final MapNonEmptyCache nonEmptyCache = new MapNonEmptyCache();
   private static final MapRenderState RENDER_STATE = new MapRenderState();
   private static MapId cachedId;
   private static int read = -2147483648;
   private static MapItemSavedData cachedData;
   private static Object cachedLevel;
   private static MapTooltipPreview.Inner_StreamUtil cachedTooltip;
   private static MapTooltipPreview.Inner_DataUtil cachedComponent;

   private MapTooltipPreview() {
   }

   public static void register() {
      ClientTooltipComponentCallback.EVENT.register(data -> {
         if (!(data instanceof MapTooltipPreview.Inner_StreamUtil tooltip)) {
            return null;
         } else {
            if (cachedComponent == null || !tooltip.equals(cachedTooltip)) {
               cachedTooltip = tooltip;
               cachedComponent = new MapTooltipPreview.Inner_DataUtil(tooltip.init());
            }

            return cachedComponent;
         }
      });
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> load());
   }

   public static Optional<TooltipComponent> init(ItemStack stack, Optional<TooltipComponent> original) {
      if (!original.isPresent() && stack != null && !stack.isEmpty() && autismclient.modules.InventoryTweaksModule.isMapPreviewEnabled()) {
         MapId id = stack.get(DataComponents.MAP_ID);
         if (mapData(id) == null) {
            return original;
         }

         MapTooltipPreview.Inner_StreamUtil cached = cachedTooltip;
         return Optional.of(cached != null && cached.init().equals(id) ? cached : new MapTooltipPreview.Inner_StreamUtil(id));
      } else {
         return original;
      }
   }

   private static int previewSize() {
      Minecraft mc = Minecraft.getInstance();
      int height = mc == null ? 0 : mc.getWindow().getGuiScaledHeight();
      return height * 2 >= 640 ? 128 : 64;
   }

   private static MapItemSavedData mapData(MapId id) {
      Minecraft mc = Minecraft.getInstance();
      if (id != null && mc != null && mc.level != null && autismclient.modules.InventoryTweaksModule.isMapPreviewEnabled()) {
         MapItemSavedData data = MapItem.getSavedData(id, mc.level);
         return nonEmptyCache.init(mc.level, id, data, AutismSessionState.getInstance().getClientTick()) ? data : null;
      } else {
         return null;
      }
   }

   public static void invalidate(MapId id) {
      nonEmptyCache.init(id);
      if (id != null && id.equals(cachedId)) {
         cachedData = null;
      }
   }

   private static void load() {
      nonEmptyCache.init();
      cachedId = null;
      cachedData = null;
      cachedLevel = null;
      read = -2147483648;
      RENDER_STATE.texture = null;
      RENDER_STATE.decorations.clear();
      cachedTooltip = null;
      cachedComponent = null;
   }

   private static MapItemSavedData load(MapId id) {
      Minecraft mc = Minecraft.getInstance();
      MapItemSavedData data = mapData(id);
      if (data == null) {
         return null;
      }

      int tick = AutismSessionState.getInstance().getClientTick();
      if (id.equals(cachedId) && tick == read && cachedLevel == mc.level && cachedData == data) {
         return data;
      }

      mc.getMapRenderer().extractRenderState(id, data, RENDER_STATE);
      cachedLevel = mc.level;
      cachedId = id;
      read = tick;
      cachedData = data;
      return data;
   }

   static final class Inner_DataUtil implements ClientTooltipComponent {
      private final MapId init;

      Inner_DataUtil(MapId id) {
         this.init = id;
      }

      @Override
      public int getHeight(Font font) {
         return MapTooltipPreview.mapData(this.init) == null ? 0 : MapTooltipPreview.previewSize() + 4;
      }

      @Override
      public int getWidth(Font font) {
         return MapTooltipPreview.mapData(this.init) == null ? 0 : MapTooltipPreview.previewSize();
      }

      @Override
      public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
         try {
            if (MapTooltipPreview.load(this.init) == null) {
               return;
            }

            float scale = MapTooltipPreview.previewSize() / 128.0F;
            graphics.pose().pushMatrix();

            try {
               graphics.pose().translate(x, y + 2);
               graphics.pose().scale(scale, scale);
               graphics.map(MapTooltipPreview.RENDER_STATE);
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
