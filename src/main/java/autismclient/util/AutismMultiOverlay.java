package autismclient.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.Packet;

public final class AutismMultiOverlay extends bj implements autismclient.gui.b.StateCache.Inner_CachePool {
   private static final String init_Str_2 = "autism-multi";
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final Font init_Font;
   private final autismclient.gui.b.StateCache init_StateCache;
   private final AutismPacketSelectorOverlay init_AutismPacketSelect;
   private final Map<String, AutismMultiGuiOverlay> init_Map = new LinkedHashMap<>();
   private AutismMultiGuiOverlay init_AutismMultiGuiOver;
   private boolean write_Z;
   private boolean update_Z;
   private Screen init_Screen;
   private double init_D;
   private double load_D;
   private static volatile AutismMultiOverlay init_AutismMultiOverlay;

   public static void read_V_Str(String accountId) {
      AutismMultiOverlay live = init_AutismMultiOverlay;
      if (live != null && accountId != null) {
         live.init_V(accountId);
      }
   }

   public static void write_V_Str(String accountId) {
      AutismMultiOverlay live = init_AutismMultiOverlay;
      if (live != null && accountId != null && !live.init_Z(accountId)) {
         live.init_V(accountId);
      }
   }

   public static boolean write_Z_Str(String accountId) {
      AutismMultiOverlay live = init_AutismMultiOverlay;
      return live != null && accountId != null && live.init_Z(accountId);
   }

   public static void update_V_Str(String accountId) {
      AutismMultiOverlay live = init_AutismMultiOverlay;
      if (live != null && accountId != null && !accountId.isBlank()) {
         live.close(accountId);
         AutismMultiGuiOverlay viewer = live.init_Map.get(accountId);
         Minecraft client = Minecraft.getInstance();
         if (viewer != null && client != null && !(client.gui.screen() instanceof autismclient.gui.d.TaskTimer)) {
            client.gui.setScreen(new autismclient.gui.d.TaskTimer(viewer, null, false, true).init_TaskTimer());
         }
      }
   }

   public static void sync_V_Str(String accountId) {
      AutismMultiOverlay live = init_AutismMultiOverlay;
      if (live != null && accountId != null) {
         AutismMultiGuiOverlay viewer = live.init_Map.get(accountId);
         if (viewer != null) {
            viewer.init_V_Z(false);
         }
      }
   }

   private void close(String accountId) {
      AutismMultiGuiOverlay viewer = this.init_Map.get(accountId);
      if (viewer == null) {
         viewer = new AutismMultiGuiOverlay(this.init_Font, accountId);
         viewer.b__V();
         this.init_Map.put(accountId, viewer);
      }

      bk manager = bk.init_bk();
      manager.init_V_cX_cX_DataUtil(viewer, cX.Inner_DataUtil.write);
      if (!viewer.init_Z_Str_5(accountId)) {
         viewer.poll_Z_10();
      }

      manager.update_V(viewer);
   }

   public AutismMultiOverlay(Font font) {
      super("autism-multi", 430, 250);
      this.init_Font = font;
      this.init_I_2 = 70;
      this.load_I = 30;
      this.init_StateCache = new autismclient.gui.b.StateCache(this, font);
      init_AutismMultiOverlay = this;
      this.init_AutismPacketSelect = new AutismPacketSelectorOverlay(font);
      autismclient.util.g.PacketUtil.init_V_PacketUtil_PacketU(new autismclient.util.g.PacketUtil.Inner_PacketUtil() {
         @Override
         public void init() {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
               client.execute(AutismMultiOverlay.this::sync_V_12);
            }
         }

         @Override
         public void init(String accountId) {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
               client.execute(() -> AutismMultiOverlay.this.poll(accountId));
            }
         }

         @Override
         public void load(String accountId) {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
               client.execute(() -> AutismMultiOverlay.this.open(accountId));
            }
         }
      });
   }

   private void open(String accountId) {
      AutismMultiGuiOverlay viewer = this.init_Map.get(accountId);
      Minecraft client = Minecraft.getInstance();
      boolean hosted = viewer != null && client != null && client.gui.screen() instanceof autismclient.gui.d.TaskTimer host && host.init(viewer);
      if (viewer != null) {
         viewer.load(hosted);
      }

      if (this.init_AutismMultiGuiOver != null) {
         this.init_AutismMultiGuiOver.load(false);
      }
   }

   private void poll(String accountId) {
      AutismMultiGuiOverlay viewer = this.init_Map.remove(accountId);
      if (viewer != null) {
         viewer.init_V_Z(false);
         bk.init_bk().read_V(viewer);
      }
   }

   private void sync_V_12() {
      for (AutismMultiGuiOverlay viewer : this.init_Map.values()) {
         viewer.init_V_Z(false);
         bk.init_bk().read_V(viewer);
      }

      this.init_Map.clear();
      if (this.init_AutismMultiGuiOver != null) {
         this.init_AutismMultiGuiOver.init_V_Z(false);
         bk.init_bk().read_V(this.init_AutismMultiGuiOver);
         this.init_AutismMultiGuiOver = null;
      }
   }

   public void write_V_16() {
      this.init_V_Z(!this.init_Z);
      if (this.init_Z) {
         this.init_StateCache.init_V();
         bk.init_bk().update_V(this);
      }
   }

   public void flush(String name) {
      this.init_StateCache.init_V_Str(name);
   }

   public void update_V_14() {
      this.init_V_Z(true);
      this.init_StateCache.init_V();
   }

   @Override
   public void init_V_Z(boolean value) {
      if (!value) {
         this.init_StateCache.load_V();
         this.init_AutismPacketSelect.write_V_21();
         this.update_Z = false;
         this.init_Screen = null;
      }

      super.init_V_Z(value);
   }

   @Override
   public int init_I() {
      return 370;
   }

   @Override
   public int load_I() {
      return 190;
   }

   @Override
   public cX.Inner_DataUtil init_cX_DataUtil() {
      return cX.Inner_DataUtil.write;
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public boolean write_Z() {
      return this.init_StateCache.init_Z() || this.init_AutismPacketSelect.write_Z();
   }

   @Override
   public void push_V() {
      this.init_StateCache.read_V();
      this.init_AutismPacketSelect.push_V();
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if ((this.init_Z || this.a__Z()) && !this.poll_Z_11()) {
         cH bounds = this.init_cH_cX(this);
         this.init_I_2 = bounds.init_I;
         this.load_I = bounds.load_I;
         this.read_I = bounds.read;
         this.write = bounds.write;
         this.init_V_GuiGraphicsExtract_6(
            context,
            mouseX,
            mouseY,
            this.init_cH(),
            autismclient.util.g.PacketUtil.init_PacketUtil().load_Z() ? "Multi " + autismclient.util.g.PacketUtil.init_PacketUtil().read_Str() : "Multi",
            this.load_Z,
            this.write_Z
         );
         if (!this.init_Z_Z(this.load_Z)) {
            boolean clipped = this.init_Z_GuiGraphicsExtract(context, this.init_cH(), this.load_Z);
            this.init_StateCache
               .init_V_GuiGraphicsExtract(context, this.init_I_2 + 2, this.load_I + 14 + 1, this.read_I - 4, this.write - 14 - 3, mouseX, mouseY, delta);
            this.init_V_GuiGraphicsExtract_Z(context, clipped);
         }

         if (this.init_AutismPacketSelect.sync_Z()) {
            this.init_AutismPacketSelect.init_V_GuiGraphicsExtract_15(context, mouseX, mouseY, delta);
         }
      }
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.init_Z || this.poll_Z_11()) {
         return false;
      } else if (this.init_AutismPacketSelect.sync_Z()) {
         this.init_AutismPacketSelect.init_Z_D_3(mouseX, mouseY, button);
         return true;
      } else {
         cH bounds = this.init_cH();
         if (this.write(mouseX, mouseY, bounds)) {
            this.init_V_Z(false);
            this.write_Z = false;
            return true;
         } else if (button == 0 && this.load_Z_D_D(mouseX, mouseY)) {
            this.write_Z = true;
            this.init_D = mouseX - this.init_I_2;
            this.load_D = mouseY - this.load_I;
            return true;
         } else if (this.load_Z) {
            return false;
         } else {
            return this.init_StateCache.init_Z_I_2((int)Math.round(mouseX), (int)Math.round(mouseY), button) ? true : this.init_Z_D_D(mouseX, mouseY);
         }
      }
   }

   @Override
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.init_Z || this.poll_Z_11()) {
         return false;
      } else if (this.init_AutismPacketSelect.sync_Z()) {
         this.init_AutismPacketSelect.load_Z_D_3(mouseX, mouseY, button);
         return true;
      } else if (this.write_Z) {
         this.write_Z = false;
         this.a__V();
         return true;
      } else {
         return this.init_StateCache.load((int)Math.round(mouseX), (int)Math.round(mouseY), button);
      }
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (!this.init_Z || this.poll_Z_11()) {
         return false;
      } else if (this.init_AutismPacketSelect.sync_Z()) {
         this.init_AutismPacketSelect.init_Z_D(mouseX, mouseY, button, deltaX, deltaY);
         return true;
      } else if (this.write_Z) {
         cH next = this.init_cH_cX_cH(
            this, new cH((int)Math.round(mouseX - this.init_D), (int)Math.round(mouseY - this.load_D), this.read_I, this.write, this.init_Z, this.load_Z)
         );
         this.init_I_2 = next.init_I;
         this.load_I = next.load_I;
         return true;
      } else {
         return !this.load_Z && this.init_StateCache.init_Z_I((int)Math.round(mouseX), (int)Math.round(mouseY), button, deltaX, deltaY);
      }
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      if (!this.init_Z || this.poll_Z_11()) {
         return false;
      } else if (this.init_AutismPacketSelect.sync_Z()) {
         this.init_AutismPacketSelect.init_Z_D_2(mouseX, mouseY, amount);
         return true;
      } else {
         return !this.load_Z && this.init_Z_D_D(mouseX, mouseY) && this.init_StateCache.init_Z_I((int)Math.round(mouseX), (int)Math.round(mouseY), amount);
      }
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (!this.init_Z || this.poll_Z_11()) {
         return false;
      } else if (this.init_AutismPacketSelect.sync_Z()) {
         this.init_AutismPacketSelect.init_Z_I(keyCode, scanCode, modifiers);
         return true;
      } else {
         return !this.load_Z && this.init_StateCache.read(keyCode, scanCode, modifiers);
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      if (!this.init_Z || this.poll_Z_11()) {
         return false;
      } else if (this.init_AutismPacketSelect.sync_Z()) {
         this.init_AutismPacketSelect.init_Z_C_I(chr, modifiers);
         return true;
      } else {
         return !this.load_Z && this.init_StateCache.init_Z_C_I(chr, modifiers);
      }
   }

   @Override
   public boolean init_Z_D_D(double mouseX, double mouseY) {
      return this.init_Z && !this.poll_Z_11()
         ? this.init_AutismPacketSelect.sync_Z() && this.init_AutismPacketSelect.init_Z_D_D(mouseX, mouseY) || super.init_Z_D_D(mouseX, mouseY)
         : false;
   }

   @Override
   public void init_V() {
      if (this.init_Minecraft != null) {
         Screen parent = this.init_Minecraft.gui.screen();
         this.update_Z = true;
         this.init_Screen = parent;
         this.init_StateCache.read_V();
         this.init_Minecraft.gui.setScreen(new autismclient.gui.d.StreamUtil(parent));
      }
   }

   @Override
   public void init_V_Str(String title, String serverAddress, String selectedProxyId, Map<String, Integer> profileUsage, Consumer<String> callback) {
      if (this.init_Minecraft != null) {
         Screen parent = this.init_Minecraft.gui.screen();
         this.update_Z = true;
         this.init_Screen = parent;
         this.init_StateCache.read_V();
         this.init_Minecraft.gui.setScreen(new autismclient.gui.d.FieldCache(parent, title, serverAddress, selectedProxyId, profileUsage, callback));
      }
   }

   @Override
   public void init_V(String accountId) {
      if (accountId != null && !accountId.isBlank()) {
         AutismMultiGuiOverlay viewer = this.init_Map.get(accountId);
         if (viewer != null && viewer.init_Z_Str_5(accountId)) {
            viewer.init_V_Z(false);
         } else {
            if (viewer == null) {
               viewer = new AutismMultiGuiOverlay(this.init_Font, accountId);
               viewer.b__V();
               this.init_Map.put(accountId, viewer);
            }

            bk manager = bk.init_bk();
            manager.init_V_cX_cX_DataUtil(viewer, cX.Inner_DataUtil.write);
            if (viewer.poll_Z_10()) {
               manager.update_V(viewer);
            }
         }
      }
   }

   @Override
   public boolean init_Z(String accountId) {
      AutismMultiGuiOverlay viewer = this.init_Map.get(accountId);
      return viewer != null && viewer.init_Z_Str_5(accountId);
   }

   @Override
   public void load_V(String accountId) {
      autismclient.util.g.ValueSink.init(accountId, this.init_Minecraft == null ? null : this.init_Minecraft.gui.screen());
   }

   @Override
   public boolean load_Z(String accountId) {
      return autismclient.util.g.ValueSink.init_Z(accountId);
   }

   @Override
   public boolean read_Z_Str(String accountId) {
      return autismclient.util.g.ValueSink.load_Z(accountId);
   }

   @Override
   public void load_V_2() {
      if (this.init_AutismMultiGuiOver != null && this.init_AutismMultiGuiOver.sync_Z()) {
         this.init_AutismMultiGuiOver.init_V_Z(false);
      } else {
         if (this.init_AutismMultiGuiOver == null) {
            this.init_AutismMultiGuiOver = AutismMultiGuiOverlay.init_AutismMultiGuiOver_Font(this.init_Font);
            this.init_AutismMultiGuiOver.b__V();
         }

         bk manager = bk.init_bk();
         manager.init_V_cX_cX_DataUtil(this.init_AutismMultiGuiOver, cX.Inner_DataUtil.write);
         if (this.init_AutismMultiGuiOver.poll_Z_10()) {
            manager.update_V(this.init_AutismMultiGuiOver);
         }
      }
   }

   @Override
   public boolean init_Z() {
      return this.init_AutismMultiGuiOver != null && this.init_AutismMultiGuiOver.sync_Z();
   }

   private boolean poll_Z_11() {
      if (!this.update_Z) {
         return false;
      }

      Screen current = this.init_Minecraft != null && this.init_Minecraft.gui != null ? this.init_Minecraft.gui.screen() : null;
      if (current != this.init_Screen) {
         return true;
      }

      this.update_Z = false;
      this.init_Screen = null;
      return false;
   }

   @Override
   public void init(Consumer<Class<? extends Packet<?>>> callback) {
      this.init_AutismPacketSelect.init_V_Consumer(callback, autismclient.util.g.HashUtil.init(), true);
   }

   @Override
   public void init_V_IoWorker_DataUtil(
      autismclient.util.g.IoWorker.Inner_DataUtil direction,
      Collection<Class<? extends Packet<?>>> selected,
      BiConsumer<Class<? extends Packet<?>>, Boolean> callback
   ) {
      if (direction == autismclient.util.g.IoWorker.Inner_DataUtil.load) {
         this.init_AutismPacketSelect.load_V_BiConsumer(callback, selected, autismclient.util.g.IoWorker.init_Set_IoWorker_DataUtil(direction));
      } else {
         this.init_AutismPacketSelect.init_V_BiConsumer(callback, selected, autismclient.util.g.IoWorker.init_Set_IoWorker_DataUtil(direction));
      }
   }

   @Override
   public void init_V_aM_Consumer(aM macro, Consumer<aM> onSaved) {
      AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
      if (editor != null) {
         bk.init_bk().init_V(editor);
         boolean inWorld = this.init_Minecraft != null && this.init_Minecraft.player != null && this.init_Minecraft.level != null;
         editor.load_V_Z_3(!inWorld);
         editor.load_V_aM_Consumer(macro, onSaved);
         bk.init_bk().update_V(editor);
      }
   }

   @Override
   public void init_V_FieldCache(autismclient.util.g.FieldCache profile, Set<String> selectedAccounts, Consumer<autismclient.util.g.FieldCache> onSaved) {
      if (this.init_Minecraft != null) {
         Screen parent = this.init_Minecraft.gui.screen();
         this.update_Z = true;
         this.init_Screen = parent;
         this.init_StateCache.read_V();
         autismclient.util.g.DataUtil draft = autismclient.util.g.PathHelper.init(profile, selectedAccounts);
         this.init_Minecraft
            .gui
            .setScreen(
               new autismclient.gui.d.UiHelper(
                  parent, draft, selectedAccounts, updated -> onSaved.accept(autismclient.util.g.PathHelper.init_FieldCache(updated, profile)), false
               )
            );
      }
   }

   @Override
   public void init_V_CachePool(autismclient.util.g.CachePool config, Consumer<autismclient.util.g.CachePool> onSaved, boolean live) {
      if (this.init_Minecraft != null) {
         Screen parent = this.init_Minecraft.gui.screen();
         this.update_Z = true;
         this.init_Screen = parent;
         this.init_StateCache.read_V();
         this.init_Minecraft.gui.setScreen(new autismclient.gui.d.PoolEntry(parent, config, onSaved, live));
      }
   }
}
