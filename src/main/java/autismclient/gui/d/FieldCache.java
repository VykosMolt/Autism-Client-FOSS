package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.bW;
import autismclient.util.bY;
import autismclient.util.cA;
import autismclient.util.ct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class FieldCache extends KeyHolder {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 6;
   private static final int load_I = 4;
   private static final int read_I = 520;
   private static final int write_I = 3;
   private static final int update_I = -14249;
   private final Screen init_Screen;
   private final String init_Str;
   private final String load_Str;
   private final int sync;
   private final String read_Str;
   private final Map<String, Integer> init_Map;
   private final Consumer<String> init_Consumer;
   private final List<autismclient.gui.f.b.ByteReader> init_List = new ArrayList<>();
   private final List<FieldCache.Inner_StreamUtil> load_List = new ArrayList<>();
   private EditBox init_EditBox;
   private String write_Str = "";
   private int flush;
   private boolean init_Z;
   private int close;
   private long init_J = -9223372036854775808L;
   private long load_J = -9223372036854775808L;
   private String update_Str = null;
   private List<FieldCache.Inner_DataUtil> read_List = List.of();
   private boolean load_Z;
   private long read_J = -9223372036854775808L;
   private int open = 6;
   private int poll = 3;

   public FieldCache(
      Screen parent, String contextTitle, String serverAddress, String selectedProxyId, Map<String, Integer> profileUsage, Consumer<String> onPick
   ) {
      super(Component.literal("Choose Proxy"));
      this.init_Screen = parent;
      this.init_Str = init_Str_Str(contextTitle).isBlank() ? "Choose a manual proxy" : init_Str_Str(contextTitle);
      this.read_Str = selectedProxyId;
      this.init_Map = profileUsage == null ? Map.of() : Map.copyOf(profileUsage);
      this.init_Consumer = onPick;
      ServerAddress parsed = null;

      try {
         if (serverAddress != null && !serverAddress.isBlank()) {
            parsed = ServerAddress.parseString(serverAddress.trim());
         }
      } catch (RuntimeException var9) {
      }

      this.load_Str = parsed == null ? "" : parsed.getHost();
      this.sync = parsed == null ? 0 : parsed.getPort();
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   protected void init() {
      this.init_EditBox = null;
      this.read_V_7();
      this.init_EditBox = new StateCache(
         this.font, this.push() + 4, this.open(), Math.max(40, this.run() - 8), write_I_6(), Component.literal("Search proxies")
      );
      this.init_EditBox.setHint(Component.literal("Search name, address, type, or region..."));
      this.init_EditBox.setMaxLength(160);
      this.init_EditBox.setValue(this.write_Str);
      this.init_EditBox.setResponder(value -> {
         this.write_Str = init_Str_Str(value);
         this.flush = 0;
         this.load_V_10();
      });
      this.addRenderableWidget(this.init_EditBox);
      bY manager = bY.init_bY();
      manager.init_Z_Z(false);
      bY.Inner_TaskQueue refresh = manager.init_bY_TaskQueue();
      this.load_Z = refresh.init_Z();
      this.read_J = refresh.init_J();
      this.load_V_10();
   }

   @Override
   public void tick() {
      super.tick();
      bY manager = bY.init_bY();
      long listRevision = manager.init_J();
      bY.Inner_TaskQueue refresh = manager.init_bY_TaskQueue();
      long statusRevision = refresh.load_J();
      if (this.load_Z && !refresh.init_Z() && refresh.init_J() == this.read_J) {
         if (refresh.init_I() >= refresh.load_I()) {
            manager.write_Z();
            this.init_V_Str_I("Proxy refresh finished and sorted best to worst.", ValueSink.open);
         }

         this.flush = 0;
         this.update_V_6();
      }

      this.load_Z = refresh.init_Z();
      this.read_J = refresh.init_J();
      if (listRevision != this.init_J || statusRevision != this.load_J) {
         this.load_V_10();
      }
   }

   private void load_V_10() {
      this.read_V_7();
      this.init_List.clear();
      this.load_List.clear();
      int ctl = write_I_6();
      int gap = 2;
      int headerY = this.open + 4;
      int backW = this.init_I_Str_9("Back");
      int manageW = this.init_I_Str_9("Manage Proxies");
      int refreshW = Math.max(this.init_I_Str_9("Refresh"), this.init_I_Str_9("Cancel"));
      int backX = this.push() + this.run() - 4 - backW;
      int manageX = backX - gap - manageW;
      autismclient.gui.f.b.ByteReader back = autismclient.gui.f.b.ByteReader.init(
            backX, headerY, backW, ctl, Component.literal("Back"), button -> this.onClose()
         )
         .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.init);
      autismclient.gui.f.b.ByteReader manage = autismclient.gui.f.b.ByteReader.init(
            manageX, headerY, manageW, ctl, Component.literal("Manage Proxies"), button -> this.sync_V_5()
         )
         .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.write);
      bY.Inner_TaskQueue refreshStatus = bY.init_bY().init_bY_TaskQueue();
      autismclient.gui.f.b.ByteReader refresh = autismclient.gui.f.b.ByteReader.init(
            manageX - gap - refreshW, headerY, refreshW, ctl, Component.literal(refreshStatus.init_Z() ? "Cancel" : "Refresh"), button -> this.write_V_6()
         )
         .init_ByteReader_ByteReader_StreamU(
            refreshStatus.init_Z() ? autismclient.gui.f.b.ByteReader.Inner_StreamUtil.load : autismclient.gui.f.b.ByteReader.Inner_StreamUtil.read
         );
      this.init_List.add(back);
      this.init_List.add(manage);
      this.init_List.add(refresh);
      List<FieldCache.Inner_DataUtil> choices = this.init_List_5();
      int visibleCount = this.tick_I_8();
      this.flush = init(this.flush, choices.size(), visibleCount);
      int y = this.clear();
      int end = Math.min(choices.size(), this.flush + visibleCount);
      int checkW = this.init_I_Str_9("Check");
      int checkY = (update_I_6() - ctl) / 2;

      for (int i = this.flush; i < end; i++) {
         FieldCache.Inner_DataUtil choice = choices.get(i);
         autismclient.gui.f.b.ByteReader check = null;
         if (choice.init_bW() != null) {
            bW proxy = choice.init_bW();
            check = autismclient.gui.f.b.ByteReader.init(
                  this.add() - checkW - 4,
                  y + checkY,
                  checkW,
                  ctl,
                  Component.literal(proxy.init_bW_CachePool == bW.Inner_CachePool.load ? "..." : "Check"),
                  button -> this.init_V_bW_2(proxy)
               )
               .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.ByteReader.Inner_StreamUtil.write);
            check.init_Z = !refreshStatus.init_Z() && proxy.init_bW_CachePool != bW.Inner_CachePool.load;
         }

         this.load_List.add(new FieldCache.Inner_StreamUtil(choice, y, check));
         y += sync_I_5();
      }
   }

   private void read_V_7() {
      int valid = 0;

      for (bW proxy : bY.init_bY().init_List()) {
         if (proxy != null && proxy.init_Z()) {
            valid++;
         }
      }

      int total = 2 + valid;
      int fixed = this.read_I_7();
      int fit = Math.max(1, (this.load_I() - 12 - fixed + 2) / sync_I_5());
      this.poll = Math.max(Math.min(3, fit), Math.min(total, fit));
      int panelH = fixed + this.poll * sync_I_5() - 2;
      this.open = Math.max(6, (this.load_I() - panelH) / 2);
      if (this.init_EditBox != null) {
         this.init_EditBox.setX(this.push() + 4);
         this.init_EditBox.setY(this.open());
         this.init_EditBox.setWidth(Math.max(40, this.run() - 8));
      }
   }

   private int read_I_7() {
      return 4 + write_I_6() + 2 + write_I_6() + 4 + close() + 4 + 4;
   }

   private static int write_I_6() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private static int update_I_6() {
      return autismclient.gui.f.RenderState.init(12) * 2;
   }

   private static int sync_I_5() {
      return update_I_6() + 2;
   }

   private static int flush() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private static int close() {
      return 2 + flush() + 4;
   }

   private int init_I_Str_9(String text) {
      return autismclient.gui.f.RenderState.load(k.init(this.font, text, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private int init_I_3(int y, int h) {
      return j.init(y, h, k.init(init_CharEncoder.init_Identifier_l(l.read)), 0);
   }

   private int open() {
      return this.open + 4 + write_I_6() + 2;
   }

   private int poll() {
      return this.open() + write_I_6() + 4;
   }

   private int push() {
      return Math.max(6, (this.init_I() - this.run()) / 2);
   }

   private void write_V_6() {
      bY manager = bY.init_bY();
      bY.Inner_TaskQueue current = manager.init_bY_TaskQueue();
      if (current.init_Z()) {
         if (manager.update_Z()) {
            this.init_V_Str_I("Proxy refresh canceled.", -14249);
         }

         this.update_V_6();
         this.load_V_10();
      } else if (this.load_Str.isBlank() || this.sync <= 0) {
         this.init_V_Str_I("Set a valid profile server before checking proxies.", -14249);
      } else if (!manager.init(true, this.load_Str, this.sync)) {
         this.init_V_Str_I(manager.close_I() == 0 ? "No proxies to refresh." : "Proxy refresh could not start.", -14249);
      } else {
         bY.Inner_TaskQueue started = manager.init_bY_TaskQueue();
         this.load_Z = true;
         this.read_J = started.init_J();
         this.update_V_6();
         this.load_V_10();
         this.init_V_Str_I("Checking proxies against " + this.load_Str + ":" + this.sync + "...", ValueSink.open);
      }
   }

   private void init_V_bW_2(bW proxy) {
      if (proxy != null && !bY.init_bY().init_bY_TaskQueue().init_Z()) {
         if (!this.load_Str.isBlank() && this.sync > 0) {
            proxy.init_bW_CachePool = bW.Inner_CachePool.load;
            proxy.init_J = 0L;
            this.update_V_6();
            this.load_V_10();
            autismclient.util.ClassCache.init_Future_Str_Runnable(
               "Autism-Multi-Proxy-Check",
               () -> {
                  int attempts = Math.max(1, bY.init_bY().update_I() + 1);
                  autismclient.util.g.RefQueue.Inner_DataUtil verified = new autismclient.util.g.RefQueue.Inner_DataUtil(false, 0L);

                  for (int attempt = 0; attempt < attempts && !verified.init_Z(); attempt++) {
                     verified = autismclient.util.g.RefQueue.init(proxy, this.load_Str, this.sync, bY.init_bY().read_I());
                  }

                  bW.Inner_DataUtil result = verified.init_Z()
                     ? new bW.Inner_DataUtil(bW.Inner_CachePool.write, verified.init_J(), 1)
                     : new bW.Inner_DataUtil(bW.Inner_CachePool.read, 0L, 2);
                  autismclient.util.g.RefQueue.Inner_DataUtil completed = verified;
                  if (this.minecraft != null) {
                     this.minecraft.execute(() -> {
                        if (bY.init_bY().init(proxy, result, completed.init_bZ())) {
                           this.init_V_Str_I(proxy.init_Str() + " is " + init_Str_bW_2(proxy) + ".", load(proxy));
                        }

                        this.update_V_6();
                        this.load_V_10();
                     });
                  }
               }
            );
         } else {
            this.init_V_Str_I("Set a valid profile server before checking this proxy.", -14249);
         }
      }
   }

   private void update_V_6() {
      this.init_J = -9223372036854775808L;
      this.load_J = -9223372036854775808L;
      this.update_Str = null;
   }

   private void sync_V_5() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(new ArrayUtil(this));
      }
   }

   private void init_V_Str_3(String proxyId) {
      if (this.init_Consumer != null) {
         this.init_Consumer.accept(proxyId == null ? "" : proxyId);
      }

      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private List<FieldCache.Inner_DataUtil> init_List_5() {
      bY manager = bY.init_bY();
      long listRevision = manager.init_J();
      long statusRevision = manager.init_bY_TaskQueue().load_J();
      String query = this.write_Str.toLowerCase(Locale.ROOT);
      if (listRevision == this.init_J && statusRevision == this.load_J && query.equals(this.update_Str)) {
         return this.read_List;
      }

      List<bW> proxies = manager.init_List()
         .stream()
         .filter(proxyx -> proxyx != null && proxyx.init_Z())
         .sorted(
            Comparator.comparingInt(FieldCache::init_I_bW)
               .thenComparingLong(proxyx -> proxyx.init_bW_CachePool == bW.Inner_CachePool.write && proxyx.init_J > 0L ? proxyx.init_J : 9223372036854775807L)
               .thenComparing(bW::init_Str, String.CASE_INSENSITIVE_ORDER)
               .thenComparing(proxyx -> init_Str_Str(proxyx.read), String.CASE_INSENSITIVE_ORDER)
               .thenComparingInt(proxyx -> proxyx.init_I)
         )
         .toList();
      List<FieldCache.Inner_DataUtil> choices = new ArrayList<>(proxies.size() + 2);
      init(choices, new FieldCache.Inner_DataUtil("", "Proxy Off", "Connect directly without a proxy", null), query);
      if (proxies.stream().anyMatch(proxyx -> proxyx.init_bW_CachePool != bW.Inner_CachePool.read)) {
         init(choices, new FieldCache.Inner_DataUtil("best", "Best Proxy", "Balance usable proxies across Best Proxy accounts", null), query);
      }

      for (bW proxy : proxies) {
         String auth = proxy.write != null && !proxy.write.isBlank() ? "  Auth" : "";
         FieldCache.Inner_DataUtil choice = new FieldCache.Inner_DataUtil(
            proxy.write(), proxy.init_Str(), proxy.init_bZ + "  " + proxy.read + ":" + proxy.init_I + auth, proxy
         );
         init(choices, choice, query);
      }

      this.init_J = listRevision;
      this.load_J = statusRevision;
      this.update_Str = query;
      this.read_List = List.copyOf(choices);
      return this.read_List;
   }

   private static void init(List<FieldCache.Inner_DataUtil> choices, FieldCache.Inner_DataUtil choice, String query) {
      if (query.isBlank() || choice.write().contains(query)) {
         choices.add(choice);
      }
   }

   private static int init_I_bW(bW proxy) {
      if (proxy != null && proxy.init_bW_CachePool != null) {
         byte var10000;
         switch (proxy.init_bW_CachePool) {
            case write:
               var10000 = 0;
               break;
            case init:
               var10000 = 1;
               break;
            case load:
               var10000 = 2;
               break;
            case read:
               var10000 = 3;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return 2;
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (Compat263.mouseEventGlfwButton(virtualEvent) == 0) {
         autismclient.gui.f.b.PoolEntry.Inner_DataUtil scrollbar = this.init_PoolEntry_DataUtil_4();
         if (scrollbar.init_Z() && scrollbar.init(virtualEvent.x(), virtualEvent.y())) {
            this.init_Z = true;
            this.close = scrollbar.load(virtualEvent.x(), virtualEvent.y())
               ? Math.max(0, (int)Math.round(virtualEvent.y()) - scrollbar.update())
               : scrollbar.sync() / 2;
            this.init_V_I_5(autismclient.gui.f.b.PoolEntry.init(scrollbar, virtualEvent.y(), this.close));
            return true;
         }

         for (autismclient.gui.f.b.ByteReader button : this.init_List) {
            if (autismclient.gui.f.b.ByteReader.init(button, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
               return true;
            }
         }

         for (FieldCache.Inner_StreamUtil row : this.load_List) {
            autismclient.gui.f.b.ByteReader check = row.init_ByteReader();
            if (check != null
               && virtualEvent.x() >= check.init_I()
               && virtualEvent.x() < check.init_I() + check.read()
               && virtualEvent.y() >= check.load()
               && virtualEvent.y() < check.load() + check.write()) {
               autismclient.gui.f.b.ByteReader.init(check, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent));
               return true;
            }
         }

         for (FieldCache.Inner_StreamUtil row : this.load_List) {
            if (virtualEvent.x() >= this.put()
               && virtualEvent.x() < this.add()
               && virtualEvent.y() >= row.init_I()
               && virtualEvent.y() < row.init_I() + update_I_6()) {
               this.init_V_Str_3(row.init_FieldCache_DataUti().init_Str());
               return true;
            }
         }
      }

      return super.mouseClicked(virtualEvent, doubleClick);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.init_Z) {
         autismclient.gui.f.b.PoolEntry.Inner_DataUtil scrollbar = this.init_PoolEntry_DataUtil_4();
         this.init_V_I_5(autismclient.gui.f.b.PoolEntry.init(scrollbar, virtualEvent.y(), this.close));
         return true;
      } else {
         return super.mouseDragged(virtualEvent, cA.init_D_D(dragX), cA.init_D_D(dragY));
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.init_Z) {
         this.init_Z = false;
         return true;
      } else {
         return super.mouseReleased(virtualEvent);
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      int x = cA.init_I_D(mouseX);
      int y = cA.init_I_D(mouseY);
      if (x < this.put() || x >= this.add() + 2 + 6 || y < this.clear() || y >= this.reset()) {
         return super.mouseScrolled(x, y, horizontal, vertical);
      }

      if (vertical == 0.0) {
         return true;
      }

      this.flush = init(this.flush + (vertical < 0.0 ? 2 : -2), this.init_List_5().size(), this.tick_I_8());
      this.load_V_10();
      return true;
   }

   private void init_V_I_5(int pixels) {
      this.flush = init(Math.round((float)pixels / sync_I_5()), this.init_List_5().size(), this.tick_I_8());
      this.load_V_10();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), start());
         int listBottom = this.reset() + 4;
         autismclient.gui.f.PacketUtil.load(
            graphics, autismclient.gui.f.NetHelper.init(this.push(), this.open, this.run(), listBottom + 4 - this.open), 6, stop(), next()
         );
         autismclient.gui.f.PacketUtil.load(
            graphics,
            autismclient.gui.f.NetHelper.init(this.get(), this.poll(), this.set(), listBottom - this.poll()),
            6,
            ct.init_I_I_ct_DataUtil(-1206840035, ct.Inner_DataUtil.close),
            next()
         );
         int titleX = this.push() + 4;
         int firstButtonX = this.init_List.stream().mapToInt(autismclient.gui.f.b.ByteReader::init_I).min().orElse(this.push() + this.run() - 4);
         int titleMaxWidth = Math.max(1, firstButtonX - titleX - 4);
         this.init(graphics, this.init_Str, titleX, this.init_I_3(this.open + 4, write_I_6()), titleMaxWidth, prev());
         this.init_V_GuiGraphicsExtract_2(graphics);

         for (FieldCache.Inner_StreamUtil row : this.load_List) {
            this.init(graphics, row, virtualMouseX, virtualMouseY);
         }

         for (autismclient.gui.f.b.ByteReader button : this.init_List) {
            autismclient.gui.f.b.ByteReader.init(graphics, this.font, button, virtualMouseX, virtualMouseY);
         }

         autismclient.gui.f.b.PoolEntry.Inner_DataUtil scrollbar = this.init_PoolEntry_DataUtil_4();
         if (scrollbar.init_Z()) {
            autismclient.gui.f.b.PoolEntry.init(graphics, scrollbar, scrollbar.init(virtualMouseX, virtualMouseY), this.init_Z);
         }

         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics) {
      List<FieldCache.Inner_DataUtil> choices = this.init_List_5();
      bY.Inner_TaskQueue refresh = bY.init_bY().init_bY_TaskQueue();
      int totalUsable = (int)bY.init_bY()
         .init_List()
         .stream()
         .filter(proxy -> proxy != null && proxy.init_Z() && proxy.init_bW_CachePool != bW.Inner_CachePool.read)
         .count();
      bW selectedProxy = this.read_Str != null && !this.read_Str.isBlank() && !"best".equals(this.read_Str) ? bY.init_bY().init_bW_Str(this.read_Str) : null;
      boolean selectedVisible = this.read_Str == null
         || this.read_Str.isBlank()
         || "best".equals(this.read_Str) && totalUsable > 0
         || selectedProxy != null && selectedProxy.init_Z() && selectedProxy.init_bW_CachePool != bW.Inner_CachePool.read;
      int headerTextY = this.init_I_3(this.poll() + 2, flush());
      if (refresh.init_Z()) {
         String progress = "Refreshing " + refresh.init_I() + "/" + refresh.load_I() + " against " + this.load_Str + ":" + this.sync;
         this.init(graphics, progress, this.put(), headerTextY, Math.max(20, this.add() - this.put()), prev());
      } else if (!selectedVisible) {
         this.init(graphics, "Current proxy is missing; choose a replacement", this.put(), headerTextY, Math.max(20, this.add() - this.put()), data());
      } else if (selectedProxy != null && selectedProxy.init_bW_CachePool == bW.Inner_CachePool.read) {
         this.init(
            graphics,
            "Selected proxy failed its last check; manual selection is still preserved",
            this.put(),
            headerTextY,
            Math.max(20, this.add() - this.put()),
            data()
         );
      } else {
         String summary = choices.size() + " choices  " + totalUsable + " usable proxies";
         this.init(graphics, summary, this.put(), headerTextY, Math.max(20, this.add() - this.put()), prev());
      }

      if (choices.isEmpty()) {
         this.init(graphics, "No proxy choices match this search.", this.put() + 4, this.init_I_3(this.clear(), update_I_6()), valid());
      }

      if (refresh.init_Z()) {
         int trackX = this.put();
         int trackY = this.poll() + 2 + flush() + 1;
         int trackW = Math.max(1, this.add() - this.put());
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(trackX, trackY, trackW, 2), next());
         int total = Math.max(1, refresh.load_I());
         int fillW = refresh.init_I() <= 0 ? 0 : Math.max(1, Math.min(trackW, Math.round((float)(trackW * refresh.init_I()) / total)));
         if (fillW > 0) {
            autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(trackX, trackY, fillW, 2), size());
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, FieldCache.Inner_StreamUtil row, int mouseX, int mouseY) {
      FieldCache.Inner_DataUtil choice = row.init_FieldCache_DataUti();
      boolean selected = Objects.equals(this.read_Str, choice.init_Str());
      int usedCount = this.init_Map.getOrDefault(choice.init_Str(), 0);
      boolean used = usedCount > 0;
      boolean hovered = mouseX >= this.put() && mouseX < this.add() && mouseY >= row.init_I() && mouseY < row.init_I() + update_I_6();
      int fill = used
         ? 858989064
         : (
            selected
               ? ct.init_I_I_ct_DataUtil(858052714, ct.Inner_DataUtil.sync)
               : (hovered ? ct.init_I_I_ct_DataUtil(707467805, ct.Inner_DataUtil.init) : ct.init_I_I_ct_DataUtil(403771667, ct.Inner_DataUtil.close))
         );
      int frameH = update_I_6();
      int line = frameH / 2;
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(this.put(), row.init_I(), this.add() - this.put(), frameH), fill);
      if (selected) {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(this.put(), row.init_I(), 2, frameH), size());
      }

      if (used) {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(this.put(), row.init_I(), 2, frameH), -14249);
      }

      bW proxy = choice.init_bW();
      int checkX = row.init_ByteReader() == null ? this.add() - 4 : row.init_ByteReader().init_I();
      int metaWidth = proxy == null ? 84 : Math.min(86, Math.max(48, (this.add() - this.put()) / 4));
      int metaX = checkX - metaWidth - 4;
      int textX = this.put() + 2 + 4;
      int textWidth = Math.max(24, metaX - textX - 4);
      int line1 = this.init_I_3(row.init_I(), line);
      int line2 = this.init_I_3(row.init_I() + line, line);
      this.init(graphics, choice.load(), textX, line1, textWidth, used ? -14249 : (selected ? size() : prev()));
      this.init(graphics, choice.read(), textX, line2, textWidth, valid());
      if (proxy == null) {
         String status = choice.init_Str().isBlank() ? "Direct" : this.pop() + " available";
         this.load(graphics, status, metaX, this.init_I_3(row.init_I(), frameH), metaWidth, choice.init_Str().isBlank() ? valid() : size());
      } else {
         this.load(graphics, used ? "Used" + (usedCount > 1 ? " x" + usedCount : "") : proxy.load(), metaX, line1, metaWidth, used ? -14249 : proxy.init_I());
         this.load(graphics, init_Str_bW_2(proxy), metaX, line2, metaWidth, load(proxy));
         if (row.init_ByteReader() != null) {
            autismclient.gui.f.b.ByteReader.init(graphics, this.font, row.init_ByteReader(), mouseX, mouseY);
         }
      }
   }

   private int pop() {
      int count = 0;

      for (bW proxy : bY.init_bY().init_List()) {
         if (proxy != null && proxy.init_Z() && proxy.init_bW_CachePool != bW.Inner_CachePool.read) {
            count++;
         }
      }

      return count;
   }

   private static String init_Str_bW_2(bW proxy) {
      if (proxy != null && proxy.init_bW_CachePool != null) {
         String var10000;
         switch (proxy.init_bW_CachePool) {
            case write:
               var10000 = proxy.init_J > 0L ? proxy.init_J + "ms" : "Alive";
               break;
            case init:
               var10000 = "Unchecked";
               break;
            case load:
               var10000 = "Checking";
               break;
            case read:
               var10000 = "Dead";
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return "Unchecked";
      }
   }

   private static int load(bW proxy) {
      if (proxy != null && proxy.init_bW_CachePool != null) {
         int var10000;
         switch (proxy.init_bW_CachePool) {
            case write:
               var10000 = ValueSink.open;
               break;
            case init:
            case load:
               var10000 = ValueSink.close;
               break;
            case read:
               var10000 = ValueSink.poll;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return ValueSink.close;
      }
   }

   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil_4() {
      int viewport = Math.max(1, this.reset() - this.clear());
      int trackX = this.add() + 2;
      return autismclient.gui.f.b.PoolEntry.init_PoolEntry_DataUtil_I_2(
         this.init_List_5().size() * sync_I_5() - 2, viewport, trackX, this.clear(), 6, viewport, this.flush * sync_I_5()
      );
   }

   private int tick_I_8() {
      return Math.max(1, this.poll);
   }

   private static int init(int offset, int total, int visible) {
      return Math.max(0, Math.min(offset, Math.max(0, total - visible)));
   }

   @Override
   public void onClose() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private int run() {
      return Math.max(1, Math.min(520, this.init_I() - 12));
   }

   private int get() {
      return this.push() + 4;
   }

   private int set() {
      return Math.max(1, this.run() - 8);
   }

   private int put() {
      return this.get() + 4;
   }

   private int add() {
      return Math.max(this.put() + 40, this.get() + this.set() - 4 - 6 - 2);
   }

   private int clear() {
      return this.poll() + close();
   }

   private int reset() {
      return this.clear() + this.poll * sync_I_5() - 2;
   }

   private void init(GuiGraphicsExtractor graphics, String value, int x, int y, int color) {
      k.init(graphics, this.font, value == null ? "" : value, init_CharEncoder.init_Identifier_l(l.read), color, x, y, false);
   }

   private void init(GuiGraphicsExtractor graphics, String value, int x, int y, int maxWidth, int color) {
      Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
      String fitted = k.load(this.font, value == null ? "" : value, Math.max(1, maxWidth), fontId, color);
      k.init(graphics, this.font, fitted, fontId, color, x, y, false);
   }

   private void load(GuiGraphicsExtractor graphics, String value, int x, int y, int maxWidth, int color) {
      Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
      String fitted = k.load(this.font, value == null ? "" : value, Math.max(1, maxWidth), fontId, color);
      k.init(graphics, this.font, fitted, fontId, color, x + maxWidth - this.font.width(fitted), y, false);
   }

   private static int start() {
      return ct.init_I_I_ct_DataUtil(-15987181, ct.Inner_DataUtil.update);
   }

   private static int stop() {
      return ct.init_I_I_ct_DataUtil(-401402080, ct.Inner_DataUtil.close);
   }

   private static int next() {
      return ct.init_I_I_ct_DataUtil(ValueSink.update, ct.Inner_DataUtil.load);
   }

   private static int prev() {
      return ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read);
   }

   private static int valid() {
      return ValueSink.close;
   }

   private static int size() {
      return ct.init_I_I_ct_DataUtil(ValueSink.sync, ct.Inner_DataUtil.sync);
   }

   private static int data() {
      return -14249;
   }

   private record Inner_DataUtil(String init_Str, String load, String read, bW init_bW) {
      private String write() {
         String proxyText = this.init_bW == null ? "" : this.init_bW.read() + " " + this.init_bW.init_bW_CachePool;
         return (KeyHolder.init_Str_Str(this.load) + " " + KeyHolder.init_Str_Str(this.read) + " " + proxyText).toLowerCase(Locale.ROOT);
      }
   }

   private record Inner_StreamUtil(FieldCache.Inner_DataUtil init_FieldCache_DataUti, int init_I, autismclient.gui.f.b.ByteReader init_ByteReader) {
   }
}
