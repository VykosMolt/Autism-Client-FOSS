package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.util.bW;
import autismclient.util.ProxyListManager;
import autismclient.util.ProxyProtocolType;
import autismclient.util.cA;
import autismclient.util.ThemePaletteManager;
import autismclient.util.GuiIconAtlas;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class ProxiesScreen extends VirtualScaledScreen {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = -7429889;
   private static final int load_I = 530;
   private static final int read_I = 3;
   private static final int write_I = 70;
   private final Screen init_Screen;
   private final List<autismclient.gui.f.b.OverlayActionButton> init_List = new ArrayList<>();
   private final List<ProxiesScreen.Inner_StreamUtil> load_List = new ArrayList<>();
   private final autismclient.gui.f.b.ScrollOffsetController init_CacheEntry = new autismclient.gui.f.b.ScrollOffsetController();
   private EditBox init_EditBox;
   private EditBox load_EditBox;
   private EditBox read_EditBox;
   private EditBox write_EditBox;
   private EditBox update_EditBox;
   private EditBox sync_EditBox;
   private ProxyProtocolType init_bZ = ProxyProtocolType.Socks5;
   private ProxiesScreen.Inner_DataUtil init_ArrayUtil_DataUtil = ProxiesScreen.Inner_DataUtil.ALL;
   private bW init_bW;
   private String init_Str = "";
   private int update_I;
   private boolean init_Z;
   private int sync_I;
   private long init_J = -9223372036854775808L;
   private long load_J = -9223372036854775808L;
   private boolean load_Z;
   private autismclient.gui.f.b.OverlayActionButton init_ByteReader;
   private autismclient.gui.f.b.OverlayActionButton load_ByteReader;
   private long read_J = -9223372036854775808L;
   private long write_J = -9223372036854775808L;
   private String load_Str = null;
   private ProxiesScreen.Inner_DataUtil load_ArrayUtil_DataUtil = null;
   private List<bW> read_List = List.of();
   private long update_J = -9223372036854775808L;
   private bW load_bW;
   private List<bW> write_List;
   private bW read_bW;
   private List<bW> update_List = List.of();

   public ProxiesScreen(Screen parent) {
      super(Component.literal("Proxies"));
      this.init_Screen = parent;
   }

   @Override
   protected void init() {
      int inputX = this.open_I();
      int inputW = this.poll_I();
      int gap = 2;
      int portW = Math.max(36, Math.min(60, inputW / 6));
      int remaining = Math.max(2, inputW - portW - gap * 2);
      int nameW = Math.max(1, remaining * 2 / 5);
      int addressW = Math.max(1, remaining - nameW);
      int halfW = Math.max(1, (inputW - gap) / 2);
      this.init_EditBox = this.init_EditBox_I(inputX, this.tick_I(), nameW, "Name");
      this.load_EditBox = this.init_EditBox_I(inputX + nameW + gap, this.tick_I(), addressW, "Address");
      this.read_EditBox = this.init_EditBox_I(inputX + nameW + gap + addressW + gap, this.tick_I(), portW, "Port");
      this.write_EditBox = this.init_EditBox_I(inputX, this.run_I(), halfW, "Username");
      this.update_EditBox = this.init_EditBox_I(inputX + halfW + gap, this.run_I(), Math.max(1, inputW - halfW - gap), "Password");
      this.sync_EditBox = this.init_EditBox_I(this.clear(), this.pop_I(), this.reset(), "Search proxies");
      this.sync_EditBox.setHint(Component.literal("Search name/address..."));
      this.sync_EditBox.setResponder(value -> {
         this.init_Str = init_Str_Str(value);
         this.update_I = 0;
         this.init_CacheEntry.load(0, 0);
         this.load_V();
      });
      this.init_V_Z(!this.init_Z());
      ProxyListManager.init_bY().init_Z_Z(false);
      this.load_V();
   }

   private EditBox init_EditBox_I(int x, int y, int w, String hint) {
      EditBox field = new MsdfEditBox(this.font, x, y, w, this.read_I(), Component.literal(hint));
      field.setHint(Component.literal(hint));
      field.setMaxLength(256);
      this.addRenderableWidget(field);
      return field;
   }

   private void load_V() {
      this.init_List.clear();
      this.load_List.clear();
      int ctl = this.read_I();
      int backWidth = this.load_I_Str("Back");
      this.init_List
         .add(
            autismclient.gui.f.b.OverlayActionButton.init(
                  this.open_I(), this.pop_I(), backWidth, ctl, Component.literal("Back"), b -> this.minecraft.gui.setScreen(this.init_Screen)
               )
               .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY)
         );
      if (!this.init_Z()) {
         int actionGap = 2;
         int usableW = this.poll_I();
         ProxyListManager manager = ProxyListManager.init_bY();
         ProxyListManager.Inner_TaskQueue refreshStatus = manager.init_bY_TaskQueue();
         ProxyListManager.Inner_BufferHelper importStatus = manager.init_bY_BufferHelper();
         boolean refreshRunning = refreshStatus.init_Z();
         boolean importRunning = importStatus.init_Z();
         String typeLabel = "Type: " + this.init_bZ;
         String addLabel = this.init_bW == null ? "Add" : "Update";
         String clearLabel = this.init_bW == null ? "Clear" : "Cancel";
         String refreshLabel = refreshRunning ? "Cancel" : "Refresh";
         String importLabel = importRunning ? "Cancel" : "Import";
         int actionY = this.get_I();
         int[] actionWidths = this.init_IArr_I_2(usableW, actionGap, typeLabel, addLabel, clearLabel, refreshLabel, "Cleanup", importLabel, "Config");
         int actionX = this.open_I();
         this.init_List.add(autismclient.gui.f.b.OverlayActionButton.init(actionX, actionY, actionWidths[0], ctl, Component.literal(typeLabel), b -> {
            this.init_bZ = this.init_bZ == ProxyProtocolType.Socks5 ? ProxyProtocolType.Socks4 : ProxyProtocolType.Socks5;
            this.load_V();
         }).init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY));
         actionX += actionWidths[0] + actionGap;
         this.init_List.add(autismclient.gui.f.b.OverlayActionButton.init(actionX, actionY, actionWidths[1], ctl, Component.literal(addLabel), b -> {
            if (this.init_bW == null) {
               this.read_V();
            } else {
               this.write_V();
            }
         }).init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.PRIMARY));
         actionX += actionWidths[1] + actionGap;
         this.init_List
            .add(
               autismclient.gui.f.b.OverlayActionButton.init(actionX, actionY, actionWidths[2], ctl, Component.literal(clearLabel), b -> this.get_V())
                  .init_ByteReader_ByteReader_StreamU(
                     this.init_bW == null ? autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY : autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.DANGER
                  )
            );
         actionX += actionWidths[2] + actionGap;
         autismclient.gui.f.b.OverlayActionButton refresh = autismclient.gui.f.b.OverlayActionButton.init(
               actionX, actionY, actionWidths[3], ctl, Component.literal(refreshLabel), b -> this.update_V()
            )
            .init_ByteReader_ByteReader_StreamU(
               refreshRunning ? autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.DANGER : autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY
            );
         refresh.init_Z = !importRunning;
         this.init_List.add(refresh);
         actionX += actionWidths[3] + actionGap;
         autismclient.gui.f.b.OverlayActionButton cleanup = autismclient.gui.f.b.OverlayActionButton.init(
               actionX, actionY, actionWidths[4], ctl, Component.literal("Cleanup"), b -> this.sync_V()
            )
            .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY);
         cleanup.init_Z = !refreshRunning && !importRunning;
         this.init_List.add(cleanup);
         actionX += actionWidths[4] + actionGap;
         this.init_List
            .add(
               autismclient.gui.f.b.OverlayActionButton.init(actionX, actionY, actionWidths[5], ctl, Component.literal(importLabel), b -> this.open_V())
                  .init_ByteReader_ByteReader_StreamU(
                     importRunning ? autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.DANGER : autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY
                  )
            );
         actionX += actionWidths[5] + actionGap;
         this.init_List
            .add(
               autismclient.gui.f.b.OverlayActionButton.init(
                     actionX, actionY, actionWidths[6], ctl, Component.literal("Config"), b -> this.minecraft.gui.setScreen(new ProxyConfigScreen(this))
                  )
                  .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY)
            );
         int filterY = this.set_I();
         int filterX = this.open_I();
         int filterGap = 2;
         int[] filterWidths = this.init_IArr_I_2(usableW, filterGap, "All", "Alive", "Dead", "Unchecked", "Enabled", "Sort", "Clear All");
         this.init_V_I(filterX, filterY, filterWidths[0], ProxiesScreen.Inner_DataUtil.ALL, "All");
         filterX += filterWidths[0] + filterGap;
         this.init_V_I(filterX, filterY, filterWidths[1], ProxiesScreen.Inner_DataUtil.ALIVE, "Alive");
         filterX += filterWidths[1] + filterGap;
         this.init_V_I(filterX, filterY, filterWidths[2], ProxiesScreen.Inner_DataUtil.DEAD, "Dead");
         filterX += filterWidths[2] + filterGap;
         this.init_V_I(filterX, filterY, filterWidths[3], ProxiesScreen.Inner_DataUtil.UNCHECKED, "Unchecked");
         filterX += filterWidths[3] + filterGap;
         this.init_V_I(filterX, filterY, filterWidths[4], ProxiesScreen.Inner_DataUtil.ENABLED, "Enabled");
         filterX += filterWidths[4] + filterGap;
         autismclient.gui.f.b.OverlayActionButton sort = autismclient.gui.f.b.OverlayActionButton.init(
               filterX, filterY, filterWidths[5], ctl, Component.literal("Sort"), b -> this.flush_V()
            )
            .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY);
         sort.init_Z = !refreshRunning && !importRunning && manager.close_I() > 1;
         this.init_List.add(sort);
         filterX += filterWidths[5] + filterGap;
         autismclient.gui.f.b.OverlayActionButton clearAll = autismclient.gui.f.b.OverlayActionButton.init(
               filterX, filterY, filterWidths[6], ctl, Component.literal("Clear All"), b -> this.close_V()
            )
            .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.DANGER);
         clearAll.init_Z = !refreshRunning && !importRunning && manager.close_I() > 0;
         this.init_List.add(clearAll);
         List<bW> proxies = this.load_List();
         boolean hasActiveSection = proxies.size() > this.init_List().size();
         int maxScroll = this.init_I_I(proxies.size());
         this.update_I = load(this.update_I, maxScroll);
         this.init_CacheEntry.load(this.update_I, maxScroll);
         int pitch = this.update_I();
         int rowH = this.write_I();
         int top = this.valid();
         int bottom = this.size();
         int firstVisible = this.update_I / pitch;
         int rowY = top - this.update_I % pitch;

         for (int i = firstVisible; i < proxies.size() && rowY + rowH <= bottom; i++) {
            bW proxy = proxies.get(i);
            if (rowY + rowH <= top) {
               rowY += pitch;
            } else {
               this.load_List.add(this.init_ArrayUtil_StreamUt_bW(proxy, rowY, hasActiveSection && i == 0));
               rowY += pitch;
            }
         }
      }
   }

   private ProxiesScreen.Inner_StreamUtil init_ArrayUtil_StreamUt_bW(bW proxy, int rowY, boolean activeSection) {
      int btnH = this.sync_I();
      int btnY = rowY + (this.write_I() - btnH) / 2;
      int toggleW = Math.max(this.load_I_Str("ON"), this.load_I_Str("OFF"));
      autismclient.gui.f.b.OverlayActionButton toggle = autismclient.gui.f.b.OverlayActionButton.init(
         this.cache() + 4, btnY, toggleW, btnH, Component.literal(proxy.init_Z ? "ON" : "OFF"), b -> this.init_V_bW(proxy)
      );
      toggle.init_ByteReader_ByteReader_StreamU(
            proxy.init_Z ? autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SUCCESS : autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.DANGER
         )
         .init_ByteReader_Z(proxy.init_Z)
         .init_ByteReader_Str("proxy-enabled:" + proxy.read + ":" + proxy.init_I);
      int checkW = this.load_I_Str("Check");
      int deleteX = this.state() - 1 - btnH;
      int checkX = deleteX - 2 - checkW;
      autismclient.gui.f.b.OverlayActionButton check = autismclient.gui.f.b.OverlayActionButton.init(
         checkX, btnY, checkW, btnH, Component.literal(proxy.init_bW_CachePool == bW.Inner_CachePool.CHECKING ? "..." : "Check"), b -> this.read_V_bW(proxy)
      );
      check.init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.PRIMARY);
      check.init_Z = proxy.init_bW_CachePool != bW.Inner_CachePool.CHECKING;
      autismclient.gui.f.b.OverlayActionButton delete = autismclient.gui.f.b.OverlayActionButton.init(deleteX, btnY, btnH, btnH, Component.empty(), b -> this.load_V_bW(proxy));
      delete.init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.DANGER).init_ByteReader_Identifier(GuiIconAtlas.open);
      return new ProxiesScreen.Inner_StreamUtil(proxy, rowY, activeSection, toggle, check, delete);
   }

   private void init_V_I(int x, int y, int width, ProxiesScreen.Inner_DataUtil value, String label) {
      autismclient.gui.f.b.OverlayActionButton button = autismclient.gui.f.b.OverlayActionButton.init(x, y, width, this.read_I(), Component.literal(label), b -> {
         this.init_ArrayUtil_DataUtil = value;
         this.update_I = 0;
         this.init_CacheEntry.load(0, 0);
         this.load_V();
      });
      button.init_ByteReader_ByteReader_StreamU(
         this.init_ArrayUtil_DataUtil == value ? autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.FILTER_ON : autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.FILTER_OFF
      );
      this.init_List.add(button);
   }

   @Override
   public void tick() {
      super.tick();
      int maxScroll = this.init_I_I(this.load_List().size());
      this.update_I = load(this.update_I, maxScroll);
      this.init_CacheEntry.load(this.update_I, maxScroll);
      ProxyListManager.Inner_TaskQueue status = ProxyListManager.init_bY().init_bY_TaskQueue();
      if (status.load_J() != this.init_J) {
         this.init_J = status.load_J();
         this.load_V();
      }

      ProxyListManager.Inner_BufferHelper importStatus = ProxyListManager.init_bY().init_bY_BufferHelper();
      if (importStatus.load_J() != this.load_J) {
         this.load_J = importStatus.load_J();
         this.load_V();
      }
   }

   private void read_V() {
      bW proxy = this.init_bW();
      if (proxy != null) {
         if (ProxyListManager.init_bY().init_Z_bW(proxy)) {
            this.init_bW = null;
            this.run_V();
            this.init_V_Str_I("Added proxy " + proxy.read + ":" + proxy.init_I + ".", GuiColorPalette.open);
            this.read_V_bW(proxy);
         } else {
            this.init_V_Str_I("Proxy already exists or is invalid.", GuiColorPalette.poll);
         }

         this.load_V();
      }
   }

   private void write_V() {
      if (this.init_bW != null) {
         bW updated = this.init_bW();
         if (updated != null) {
            if (ProxyListManager.init_bY().init_Z_bW_bW(this.init_bW, updated)) {
               this.init_V_Str_I("Updated " + this.init_bW.init_Str() + ".", GuiColorPalette.open);
               this.get_V();
            } else {
               this.init_V_Str_I("Proxy already exists or is invalid.", GuiColorPalette.poll);
               this.load_V();
            }
         }
      }
   }

   private void init_V_bW(bW proxy) {
      if (proxy != null) {
         int sectionsBefore = this.load_List().size() - this.init_List().size();
         ProxyListManager.init_bY().init_V_bW_Z(proxy, !proxy.init_Z);
         this.init_V_I(sectionsBefore);
         this.init_V_Str_I(proxy.init_Z ? "Enabled " + proxy.init_Str() + "." : "Disabled proxy.", proxy.init_Z ? GuiColorPalette.open : GuiColorPalette.close);
         this.load_V();
      }
   }

   private void init_V_I(int sectionsBefore) {
      int sectionsAfter = this.load_List().size() - this.init_List().size();
      if (sectionsAfter != sectionsBefore) {
         int maxScroll = this.init_I_I(this.load_List().size());
         this.update_I = load(this.update_I + (sectionsAfter - sectionsBefore) * this.update_I(), maxScroll);
         this.init_CacheEntry.load(this.update_I, maxScroll);
      }
   }

   private void load_V_bW(bW proxy) {
      if (proxy != null) {
         ProxyListManager.init_bY().init_V_bW(proxy);
         if (proxy.equals(this.init_bW)) {
            this.init_bW = null;
         }

         this.init_V_Str_I("Deleted " + proxy.init_Str() + ".", GuiColorPalette.close);
         this.load_V();
      }
   }

   private void read_V_bW(bW proxy) {
      if (proxy != null && proxy.init_bW_CachePool != bW.Inner_CachePool.CHECKING) {
         proxy.init_bW_CachePool = bW.Inner_CachePool.CHECKING;
         proxy.init_J = 0L;
         this.load_V();
         autismclient.util.BackgroundTaskExecutor.init_Future_Str_Runnable("Autism-Proxy-Check", () -> {
            proxy.init_I_I(ProxyListManager.init_bY().read_I());
            if (this.minecraft != null) {
               this.minecraft.execute(() -> {
                  this.init_V_Str_I(proxy.init_Str() + " is " + this.init_Str_bW(proxy) + ".", proxy.init_bW_CachePool.init_I());
                  this.load_V();
               });
            }
         });
      }
   }

   private void update_V() {
      ProxyListManager manager = ProxyListManager.init_bY();
      if (manager.init_bY_BufferHelper().init_Z()) {
         this.init_V_Str_I("Finish import before refreshing.", -14249);
      } else {
         ProxyListManager.Inner_TaskQueue currentStatus = manager.init_bY_TaskQueue();
         if (currentStatus.init_Z()) {
            if (manager.update_Z()) {
               this.init_V_Str_I("Proxy refresh canceled.", -14249);
               this.load_V();
            }
         } else if (manager.close_I() == 0) {
            this.init_V_Str_I("No proxies to refresh.", -14249);
         } else if (!manager.load_Z_Z(true)) {
            this.init_V_Str_I("Proxy refresh could not start.", -14249);
         } else {
            long generation = manager.init_bY_TaskQueue().init_J();
            this.init_V_Str_I("Refreshing proxies...", -7429889);
            this.load_V();
            autismclient.util.BackgroundTaskExecutor.init("Autism-Proxy-Refresh-Watch", () -> !manager.init_bY_TaskQueue().init_Z(), () -> {
               if (this.minecraft != null) {
                  this.minecraft.execute(() -> {
                     ProxyListManager.Inner_TaskQueue finished = manager.init_bY_TaskQueue();
                     if (finished.init_J() == generation && finished.init_I() >= finished.load_I()) {
                        this.init_V_Str_I("Proxy refresh finished.", GuiColorPalette.open);
                     }

                     this.load_V();
                  });
               }
            }, 100L);
         }
      }
   }

   private void sync_V() {
      ProxyListManager manager = ProxyListManager.init_bY();
      if (manager.init_bY_TaskQueue().init_Z()) {
         this.init_V_Str_I("Cancel refresh before cleanup.", -14249);
      } else if (manager.init_bY_BufferHelper().init_Z()) {
         this.init_V_Str_I("Finish import before cleanup.", -14249);
      } else {
         int before = manager.close_I();
         manager.init_V_2();
         int removed = Math.max(0, before - manager.close_I());
         this.init_V_Str_I(
            removed == 0 ? "Cleanup finished. Nothing removed." : "Cleanup removed " + removed + " proxies.", removed == 0 ? GuiColorPalette.close : GuiColorPalette.open
         );
         this.load_V();
      }
   }

   private void flush_V() {
      ProxyListManager manager = ProxyListManager.init_bY();
      if (manager.init_bY_TaskQueue().init_Z()) {
         this.init_V_Str_I("Cancel refresh before sorting proxies.", -14249);
      } else if (manager.init_bY_BufferHelper().init_Z()) {
         this.init_V_Str_I("Finish import before sorting proxies.", -14249);
      } else {
         if (manager.write_Z()) {
            this.init_V_Str_I("Sorted proxies by latency.", GuiColorPalette.open);
         } else {
            this.init_V_Str_I("Nothing to sort.", GuiColorPalette.close);
         }

         this.load_V();
      }
   }

   private void close_V() {
      ProxyListManager manager = ProxyListManager.init_bY();
      if (manager.init_bY_TaskQueue().init_Z()) {
         this.init_V_Str_I("Cancel refresh before clearing proxies.", -14249);
      } else if (manager.init_bY_BufferHelper().init_Z()) {
         this.init_V_Str_I("Finish import before clearing proxies.", -14249);
      } else {
         int removed = manager.load_I();
         this.init_bW = null;
         this.run_V();
         this.update_I = 0;
         this.init_CacheEntry.load(0, 0);
         this.init_V_Str_I(removed == 0 ? "Proxy list is already empty." : "Cleared " + removed + " proxies.", removed == 0 ? GuiColorPalette.close : GuiColorPalette.open);
         this.load_V();
      }
   }

   private void open_V() {
      ProxyListManager manager = ProxyListManager.init_bY();
      ProxyListManager.Inner_BufferHelper status = manager.init_bY_BufferHelper();
      if (status.init_Z()) {
         if (manager.sync_Z()) {
            this.init_V_Str_I("Proxy import canceled.", -14249);
            this.load_V();
         }
      } else if (manager.init_bY_TaskQueue().init_Z()) {
         this.init_V_Str_I("Cancel refresh before importing.", -14249);
      } else {
         this.load_Z = true;
      }
   }

   private void poll_V() {
      this.load_Z = false;
      ProxyListManager manager = ProxyListManager.init_bY();
      PointerBuffer filters = BufferUtils.createPointerBuffer(1);
      ByteBuffer txtFilter = MemoryUtil.memASCII("*.txt");
      try {
         filters.put(txtFilter);
         filters.rewind();
         String selectedFile = TinyFileDialogs.tinyfd_openFileDialog("Import Proxies", null, filters, null, false);
         if (selectedFile != null) {
            File file = new File(selectedFile);
            if (!manager.init_Z_File(file)) {
               this.init_V_Str_I("Proxy import could not start.", -14249);
            } else {
               this.init_V_bY_J(manager, manager.init_bY_BufferHelper().init_J());
            }
         }
      } finally {
         MemoryUtil.memFree(txtFilter);
      }
   }

   private void push_V() {
      this.load_Z = false;
      ProxyListManager manager = ProxyListManager.init_bY();
      String clipboard = this.minecraft != null ? this.minecraft.keyboardHandler.getClipboard() : null;
      if (clipboard == null || clipboard.isBlank()) {
         this.init_V_Str_I("Clipboard is empty.", -14249);
      } else if (!manager.init_Z_Str(clipboard)) {
         this.init_V_Str_I("Proxy import could not start.", -14249);
      } else {
         this.init_V_bY_J(manager, manager.init_bY_BufferHelper().init_J());
      }
   }

   private void init_V_bY_J(ProxyListManager manager, long generation) {
      this.init_V_Str_I("Importing proxies...", -7429889);
      this.load_V();
      autismclient.util.BackgroundTaskExecutor.init("Autism-Proxy-Import-Watch", () -> !manager.init_bY_BufferHelper().init_Z(), () -> {
         if (this.minecraft != null) {
            this.minecraft.execute(() -> {
               ProxyListManager.Inner_BufferHelper finished = manager.init_bY_BufferHelper();
               if (finished.init_J() == generation && !finished.read_Z()) {
                  this.init_V_Str_I("Imported " + finished.read_I() + " proxies.", finished.read_I() > 0 ? GuiColorPalette.open : -14249);
                  this.update_I = 0;
                  this.init_CacheEntry.load(0, 0);
               }

               this.load_V();
            });
         }
      }, 100L);
   }

   private autismclient.gui.f.NetHelper[] init_NetHelperArr() {
      int btnH = this.read_I();
      int btnW = Math.max(this.load_I_Str("From File"), this.load_I_Str("From Clipboard")) + 16;
      int cardW = btnW + 8;
      int titleH = 12;
      int cardH = 4 + titleH + 2 + btnH * 2 + 2 + 4;
      int cardX = (this.init_I() - cardW) / 2;
      int cardY = (this.load_I() - cardH) / 2;
      int btnX = cardX + 4;
      int fileY = cardY + 4 + titleH + 2;
      int clipY = fileY + btnH + 2;
      return new autismclient.gui.f.NetHelper[]{
         autismclient.gui.f.NetHelper.init(cardX, cardY, cardW, cardH),
         autismclient.gui.f.NetHelper.init(btnX, fileY, btnW, btnH),
         autismclient.gui.f.NetHelper.init(btnX, clipY, btnW, btnH)
      };
   }

   private void pop_V() {
      if (this.init_ByteReader == null || this.init_ByteReader.write() != this.read_I()) {
         this.init_ByteReader = autismclient.gui.f.b.OverlayActionButton.init(0, 0, 1, this.read_I(), Component.literal("From File"), b -> this.poll_V())
            .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY);
      }

      if (this.load_ByteReader == null || this.load_ByteReader.write() != this.read_I()) {
         this.load_ByteReader = autismclient.gui.f.b.OverlayActionButton.init(0, 0, 1, this.read_I(), Component.literal("From Clipboard"), b -> this.push_V())
            .init_ByteReader_ByteReader_StreamU(autismclient.gui.f.b.OverlayActionButton.Inner_StreamUtil.SECONDARY);
      }
   }

   private void init_V_NetHelperArr(autismclient.gui.f.NetHelper[] rects) {
      this.init_ByteReader.init_V_I(rects[1].read());
      this.init_ByteReader.load(rects[1].write());
      this.init_ByteReader.read(rects[1].update());
      this.load_ByteReader.init_V_I(rects[2].read());
      this.load_ByteReader.load(rects[2].write());
      this.load_ByteReader.read(rects[2].update());
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      this.pop_V();
      autismclient.gui.f.NetHelper[] rects = this.init_NetHelperArr();
      this.init_V_NetHelperArr(rects);
      autismclient.gui.f.NetHelper card = rects[0];
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), -939524096);
      autismclient.gui.f.PacketUtil.load(graphics, card, 5, -401402080, load_I_I(GuiColorPalette.update));
      this.init_V_GuiGraphicsExtract(
         graphics, "Import proxies from", card.read() + card.update() / 2, this.init_I_I_I(card.write() + 4, 12), GuiColorPalette.flush, true
      );
      autismclient.gui.f.b.OverlayActionButton.init(graphics, this.font, this.init_ByteReader, mouseX, mouseY);
      autismclient.gui.f.b.OverlayActionButton.init(graphics, this.font, this.load_ByteReader, mouseX, mouseY);
   }

   private void run_V() {
      this.init_EditBox.setValue("");
      this.load_EditBox.setValue("");
      this.read_EditBox.setValue("");
      this.write_EditBox.setValue("");
      this.update_EditBox.setValue("");
   }

   private void get_V() {
      this.init_bW = null;
      this.run_V();
      this.set_V();
      this.load_V();
   }

   private bW init_bW() {
      bW proxy = new bW();
      proxy.load_Str = init_Str_Str(this.init_EditBox.getValue());
      proxy.read = init_Str_Str(this.load_EditBox.getValue());
      proxy.write = init_Str_Str(this.write_EditBox.getValue());
      proxy.update = this.update_EditBox.getValue();
      proxy.init_bZ = this.init_bZ;
      if (proxy.read.isBlank()) {
         this.init_V_Str_I("Enter a proxy address first.", -14249);
         return null;
      }

      try {
         proxy.init_I = Integer.parseInt(init_Str_Str(this.read_EditBox.getValue()));
      } catch (NumberFormatException e) {
         this.init_V_Str_I("Invalid proxy port.", -14249);
         return null;
      }

      if (!proxy.init_Z()) {
         this.init_V_Str_I("Proxy must have a valid address and port.", -14249);
         return null;
      } else {
         return proxy;
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), -15987181);
         int panelX = this.flush_I();
         this.init_V_GuiGraphicsExtract_2(graphics, panelX, this.push_I(), this.close_I(), this.put(), -401402080);
         this.init_V_GuiGraphicsExtract_2(graphics, this.stop(), this.start(), this.next(), this.prev(), -1206840035);
         int headerTextY = this.init_I_I_I(this.pop_I(), this.read_I());
         this.init_V_GuiGraphicsExtract(graphics, "Proxies", this.add(), headerTextY, GuiColorPalette.flush, false);
         if (this.init_Z()) {
            this.init_V_GuiGraphicsExtract(
               graphics, "Window too small.", this.open_I(), this.init_I_I_I(this.tick_I(), 12), GuiColorPalette.close, false, this.poll_I()
            );
         }

         List<bW> proxies = this.init_List();
         ProxyListManager manager = ProxyListManager.init_bY();
         int total = manager.close_I();
         int enabled = manager.init_I_2();
         ProxyListManager.Inner_TaskQueue refreshStatus = manager.init_bY_TaskQueue();
         ProxyListManager.Inner_BufferHelper importStatus = manager.init_bY_BufferHelper();
         String summary = proxies.size() == total ? total + " proxies  enabled " + enabled : proxies.size() + " / " + total + " proxies  enabled " + enabled;
         String workLabel = this.init_Str_bY_BufferHelper(importStatus);
         int workColor = importStatus.init_Z() ? -7429889 : -14249;
         if (workLabel.isBlank()) {
            workLabel = this.init_Str_bY_TaskQueue(refreshStatus);
            workColor = refreshStatus.load_Z() ? -14249 : -7429889;
         }

         int listInnerW = this.next() - 8;
         int workLabelW = workLabel.isBlank() ? 0 : Math.min(160, listInnerW / 2);
         int listHeaderY = this.init_I_I_I(this.start() + 4, 12);
         this.init_V_GuiGraphicsExtract(graphics, summary, this.stop() + 4, listHeaderY, GuiColorPalette.flush, false, listInnerW - workLabelW - 4);
         if (!workLabel.isBlank()) {
            this.init_V_GuiGraphicsExtract(graphics, workLabel, this.stop() + this.next() - 4 - workLabelW, listHeaderY, workColor, false, workLabelW);
         }

         this.init_V_GuiGraphicsExtract_2(graphics, refreshStatus, importStatus);
         autismclient.gui.f.UiClipStack.init_HashUtil()
            .init(
               graphics,
               autismclient.gui.f.NetHelper.init(this.cache(), this.valid(), Math.max(0, this.state() - this.cache()), Math.max(0, this.size() - this.valid()))
            );

         try {
            for (ProxiesScreen.Inner_StreamUtil row : this.load_List) {
               this.init_V_GuiGraphicsExtract(graphics, row, virtualMouseX, virtualMouseY);
            }
         } finally {
            autismclient.gui.f.UiClipStack.init_HashUtil().init(graphics);
         }

         if (total == 0) {
            this.init_V_GuiGraphicsExtract(
               graphics,
               "No proxies saved yet. Add one manually or import a .txt list.",
               this.cache() + 4,
               this.init_I_I_I(this.valid(), this.write_I()),
               GuiColorPalette.close,
               false,
               this.queue() - 4
            );
         } else if (proxies.isEmpty()) {
            this.init_V_GuiGraphicsExtract(
               graphics,
               "No proxies match the current search or filter.",
               this.cache() + 4,
               this.init_I_I_I(this.valid(), this.write_I()),
               GuiColorPalette.close,
               false,
               this.queue() - 4
            );
         }

         for (autismclient.gui.f.b.OverlayActionButton button : this.init_List) {
            autismclient.gui.f.b.OverlayActionButton.init(graphics, this.font, button, virtualMouseX, virtualMouseY);
         }

         if (!this.init_Z()) {
            autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil scrollbar = this.init_PoolEntry_DataUtil_I(this.load_List().size());
            autismclient.gui.f.b.ScrollbarMetricsHelper.init(graphics, scrollbar, scrollbar.init(virtualMouseX, virtualMouseY), this.init_Z);
         }

         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
         if (this.load_Z) {
            this.init_V_GuiGraphicsExtract(graphics, virtualMouseX, virtualMouseY);
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   @Override
   public void removed() {
      super.removed();
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, ProxiesScreen.Inner_StreamUtil row, int mouseX, int mouseY) {
      bW proxy = row.init_bW;
      boolean selected = proxy.equals(this.init_bW);
      int x = this.cache();
      int y = row.init_I;
      int w = this.queue();
      int h = this.write_I();
      int fill = proxy.init_Z ? read_I_I(858052714) : (selected ? load_I_I(606804509) : 403771667);
      autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), 4, fill);
      if (proxy.init_Z) {
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y + 2, 2, h - 4), 1, read_I_I(GuiColorPalette.sync));
      } else if (selected) {
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y + 2, 2, h - 4), 1, load_I_I(-10866632));
      }

      if (row.init_Z()) {
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y + 2, 2, h - 4), 1, read_I_I(GuiColorPalette.sync));
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y + h, w, 1), load_I_I(GuiColorPalette.update));
      }

      String name = proxy.init_Str().isBlank() ? "(unnamed)" : proxy.init_Str();
      boolean hasAuth = proxy.write != null && !proxy.write.isBlank();
      String address = proxy.init_bZ + "  " + proxy.read + ":" + proxy.init_I + (hasAuth ? "  Auth" : "");
      String region = proxy.load();
      String status = this.init_Str_bW(proxy);
      int ty = this.init_I_I_I(y, h);
      int nameX = row.init_ByteReader.init_I() + row.init_ByteReader.read() + 4;
      int metaRight = row.load.init_I() - 4;
      int statusW = Math.min(this.init_I_Str("Unchecked"), Math.max(20, (metaRight - nameX) / 4));
      int statusX = metaRight - statusW;
      int regionW = Math.min(70, Math.max(20, (metaRight - nameX) / 4));
      int regionX = statusX - 4 - regionW;
      int textW = Math.max(20, regionX - 4 - nameX);
      int nameW = Math.min(this.init_I_Str(name), textW);
      this.init_V_GuiGraphicsExtract(graphics, name, nameX, ty, proxy.init_Z ? GuiColorPalette.open : GuiColorPalette.flush, false, nameW);
      int addressX = nameX + nameW + 8;
      int addressW = nameX + textW - addressX;
      if (addressW > 12) {
         this.init_V_GuiGraphicsExtract(graphics, address, addressX, ty, GuiColorPalette.close, false, addressW);
      }

      this.init_V_GuiGraphicsExtract(graphics, region, regionX, ty, proxy.init_I(), false, regionW);
      this.init_V_GuiGraphicsExtract(graphics, status, statusX, ty, proxy.init_bW_CachePool.init_I(), false, statusW);
      autismclient.gui.f.b.OverlayActionButton.init(graphics, this.font, row.init_ByteReader, mouseX, mouseY);
      autismclient.gui.f.b.OverlayActionButton.init(graphics, this.font, row.load, mouseX, mouseY);
      autismclient.gui.f.b.OverlayActionButton.init(graphics, this.font, row.read, mouseX, mouseY);
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (this.load_Z && Compat263.keyEventGlfwKey(input) == 256) {
         this.load_Z = false;
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.load_Z) {
         if (Compat263.mouseEventGlfwButton(virtualEvent) == 0) {
            this.pop_V();
            this.init_V_NetHelperArr(this.init_NetHelperArr());
            if (autismclient.gui.f.b.OverlayActionButton.init(this.init_ByteReader, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
               return true;
            }

            if (autismclient.gui.f.b.OverlayActionButton.init(this.load_ByteReader, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
               return true;
            }

            autismclient.gui.f.NetHelper card = this.init_NetHelperArr()[0];
            if (!card.init_Z((int)virtualEvent.x(), (int)virtualEvent.y())) {
               this.load_Z = false;
            }
         }

         return true;
      } else {
         if (Compat263.mouseEventGlfwButton(virtualEvent) != 0) {
            return super.mouseClicked(virtualEvent, doubleClick);
         }

         if (this.init_Z()) {
            return super.mouseClicked(virtualEvent, doubleClick);
         }

         autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil scrollbar = this.init_PoolEntry_DataUtil_I(this.load_List().size());
         if (scrollbar.init_Z() && scrollbar.init(virtualEvent.x(), virtualEvent.y())) {
            this.init_Z = true;
            this.sync_I = scrollbar.load(virtualEvent.x(), virtualEvent.y())
               ? Math.max(0, (int)Math.round(virtualEvent.y()) - scrollbar.update())
               : scrollbar.sync() / 2;
            this.update_I = load(autismclient.gui.f.b.ScrollbarMetricsHelper.init(scrollbar, virtualEvent.y(), this.sync_I), scrollbar.flush());
            this.init_CacheEntry.load(this.update_I, scrollbar.flush());
            this.load_V();
            this.set_V();
            return true;
         }

         for (autismclient.gui.f.b.OverlayActionButton button : this.init_List) {
            if (autismclient.gui.f.b.OverlayActionButton.init(button, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
               return true;
            }
         }

         for (ProxiesScreen.Inner_StreamUtil row : this.load_List) {
            if (this.init_Z_ArrayUtil_StreamUt(row, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
               return true;
            }
         }

         this.set_V();
         return super.mouseClicked(virtualEvent, doubleClick);
      }
   }

   private boolean init_Z_ArrayUtil_StreamUt(ProxiesScreen.Inner_StreamUtil row, double x, double y, int button) {
      if (row == null) {
         return false;
      }

      if (!this.init_Z_ByteReader(row.init_ByteReader, x, y) && !this.init_Z_ByteReader(row.load, x, y) && !this.init_Z_ByteReader(row.read, x, y)) {
         if (x >= this.cache() && x < this.state() && y >= row.init_I && y < row.init_I + this.write_I()) {
            this.write(row.init_bW);
            return true;
         } else {
            return false;
         }
      } else {
         autismclient.gui.f.b.OverlayActionButton.init(row.init_ByteReader, x, y, button);
         autismclient.gui.f.b.OverlayActionButton.init(row.load, x, y, button);
         autismclient.gui.f.b.OverlayActionButton.init(row.read, x, y, button);
         return true;
      }
   }

   private boolean init_Z_ByteReader(autismclient.gui.f.b.OverlayActionButton button, double mouseX, double mouseY) {
      return button != null
         && button.load_Z
         && mouseX >= button.init_I()
         && mouseX < button.init_I() + button.read()
         && mouseY >= button.load()
         && mouseY < button.load() + button.write();
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.init_Z) {
         this.init_Z = false;
         return true;
      } else {
         return super.mouseReleased(init_MouseButtonEvent_MouseButtonEvent(event));
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.init_Z) {
         autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil scrollbar = this.init_PoolEntry_DataUtil_I(this.load_List().size());
         this.update_I = load(autismclient.gui.f.b.ScrollbarMetricsHelper.init(scrollbar, virtualEvent.y(), this.sync_I), scrollbar.flush());
         this.init_CacheEntry.load(this.update_I, scrollbar.flush());
         this.load_V();
         return true;
      } else {
         return super.mouseDragged(virtualEvent, cA.init_D_D(dx), cA.init_D_D(dy));
      }
   }

   @Override
   public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
      x = cA.init_D_D(x);
      y = cA.init_D_D(y);
      if (this.init_Z()) {
         return super.mouseScrolled(x, y, scrollX, scrollY);
      }

      if (!(x < this.stop()) && !(x >= this.stop() + this.next()) && !(y < this.start()) && !(y >= this.start() + this.prev())) {
         int maxScroll = this.init_I_I(this.load_List().size());
         if (maxScroll <= 0) {
            return true;
         }

         int next = this.update_I - (int)Math.signum(scrollY) * this.update_I();
         this.update_I = load(next, maxScroll);
         this.init_CacheEntry.init_V_I_I(this.update_I, maxScroll);
         this.load_V();
         return true;
      } else {
         return super.mouseScrolled(x, y, scrollX, scrollY);
      }
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.init_Screen);
   }

   private void write(bW proxy) {
      this.init_bW = proxy;
      if (proxy != null) {
         this.init_EditBox.setValue(init_Str_Str(proxy.load_Str));
         this.load_EditBox.setValue(init_Str_Str(proxy.read));
         this.read_EditBox.setValue(proxy.init_I <= 0 ? "" : Integer.toString(proxy.init_I));
         this.write_EditBox.setValue(init_Str_Str(proxy.write));
         this.update_EditBox.setValue(proxy.update == null ? "" : proxy.update);
         this.init_bZ = proxy.init_bZ == null ? ProxyProtocolType.Socks5 : proxy.init_bZ;
      }

      this.load_V();
   }

   private List<bW> init_List() {
      ProxyListManager manager = ProxyListManager.init_bY();
      long listRevision = manager.init_J();
      long refreshRevision = manager.init_bY_TaskQueue().load_J();
      String query = load_Str_Str(this.init_Str);
      boolean statusSensitive = this.init_ArrayUtil_DataUtil != ProxiesScreen.Inner_DataUtil.ALL || !query.isEmpty();
      boolean cacheValid = this.load_ArrayUtil_DataUtil == this.init_ArrayUtil_DataUtil
         && query.equals(this.load_Str)
         && this.read_J == listRevision
         && (!statusSensitive || this.write_J == refreshRevision);
      if (cacheValid) {
         return this.read_List;
      }

      List<bW> proxies = manager.init_List();
      List<bW> filtered;
      if (query.isEmpty() && this.init_ArrayUtil_DataUtil == ProxiesScreen.Inner_DataUtil.ALL) {
         filtered = proxies;
      } else {
         filtered = new ArrayList<>();

         for (bW proxy : proxies) {
            if (proxy != null && this.init_Z_bW(proxy) && (query.isEmpty() || this.init_Z_bW_Str(proxy, query))) {
               filtered.add(proxy);
            }
         }
      }

      this.read_List = filtered;
      this.load_Str = query;
      this.load_ArrayUtil_DataUtil = this.init_ArrayUtil_DataUtil;
      this.read_J = listRevision;
      this.write_J = refreshRevision;
      return this.read_List;
   }

   private bW load_bW() {
      ProxyListManager manager = ProxyListManager.init_bY();
      long revision = manager.init_J();
      if (revision != this.update_J) {
         this.load_bW = manager.init_bW();
         this.update_J = revision;
      }

      return this.load_bW;
   }

   private List<bW> load_List() {
      List<bW> filtered = this.init_List();
      bW active = this.load_bW();
      if (filtered == this.write_List && active == this.read_bW) {
         return this.update_List;
      }

      List<bW> display = filtered;
      if (active != null && filtered.indexOf(active) > 0) {
         display = new ArrayList<>(filtered.size() + 1);
         display.add(active);
         display.addAll(filtered);
      }

      this.write_List = filtered;
      this.read_bW = active;
      this.update_List = display;
      return display;
   }

   private boolean init_Z_bW(bW proxy) {
      boolean var10000;
      switch (this.init_ArrayUtil_DataUtil) {
         case ALL:
            var10000 = true;
            break;
         case ALIVE:
            var10000 = proxy.init_bW_CachePool == bW.Inner_CachePool.ALIVE;
            break;
         case DEAD:
            var10000 = proxy.init_bW_CachePool == bW.Inner_CachePool.DEAD;
            break;
         case UNCHECKED:
            var10000 = proxy.init_bW_CachePool == bW.Inner_CachePool.UNCHECKED || proxy.init_bW_CachePool == bW.Inner_CachePool.CHECKING;
            break;
         case ENABLED:
            var10000 = proxy.init_Z;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private boolean init_Z_bW_Str(bW proxy, String query) {
      String haystack = load_Str_Str(
         proxy.init_Str()
            + " "
            + proxy.read
            + " "
            + proxy.init_I
            + " "
            + proxy.init_bZ
            + " "
            + proxy.init_bW_CachePool
            + " "
            + init_Str_Str(proxy.write)
            + " "
            + proxy.read()
      );
      return haystack.contains(query);
   }

   private String init_Str_bW(bW proxy) {
      if (proxy == null) {
         return "";
      }

      String var10000;
      switch (proxy.init_bW_CachePool) {
         case ALIVE:
            var10000 = proxy.init_J + "ms";
            break;
         case DEAD:
            var10000 = "Dead";
            break;
         case CHECKING:
            var10000 = "Checking";
            break;
         case UNCHECKED:
            var10000 = "Unchecked";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private String init_Str_bY_TaskQueue(ProxyListManager.Inner_TaskQueue status) {
      if (status == null) {
         return "";
      } else if (status.init_Z()) {
         return "Refreshing " + status.init_I() + "/" + status.load_I();
      } else {
         return status.load_Z() ? "Canceling" : "";
      }
   }

   private String init_Str_bY_BufferHelper(ProxyListManager.Inner_BufferHelper status) {
      if (status == null) {
         return "";
      } else if (status.init_Z()) {
         return "Importing " + status.init_I() + " +" + status.load_I();
      } else {
         return status.load_Z() ? "Canceling import" : "";
      }
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, ProxyListManager.Inner_TaskQueue refreshStatus, ProxyListManager.Inner_BufferHelper importStatus) {
      boolean importing = importStatus != null && importStatus.init_Z();
      boolean refreshing = refreshStatus != null && refreshStatus.init_Z();
      if (importing || refreshing) {
         int trackX = this.stop() + 4;
         int trackY = this.start() + 4 + 12 + 1;
         int trackW = Math.max(1, this.next() - 8);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(trackX, trackY, trackW, 2), load_I_I(1429414952));
         if (refreshing) {
            int total = Math.max(1, refreshStatus.load_I());
            int fillW = Math.max(1, Math.min(trackW, Math.round((float)(trackW * Math.max(0, refreshStatus.init_I())) / total)));
            autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(trackX, trackY, fillW, 2), -7429889);
         } else {
            int sweepW = Math.max(24, trackW / 4);
            int range = Math.max(1, trackW + sweepW);
            int offset = (int)(System.currentTimeMillis() / 8L % range) - sweepW;
            int fillX = Math.max(trackX, trackX + offset);
            int fillRight = Math.min(trackX + trackW, trackX + offset + sweepW);
            if (fillRight > fillX) {
               autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(fillX, trackY, fillRight - fillX, 2), -7429889);
            }
         }
      }
   }

   private int read_I() {
      return autismclient.gui.f.UiDensityMetrics.init(13);
   }

   private int write_I() {
      return autismclient.gui.f.UiDensityMetrics.init(14);
   }

   private int update_I() {
      return this.write_I() + 2;
   }

   private int sync_I() {
      return this.write_I() - 2;
   }

   private int init_I_I_I(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).init_I(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private int init_I_Str(String text) {
      return k.init(this.font, text == null ? "" : text, init_CharEncoder.init_Identifier_l(l.read), 0);
   }

   private int flush_I() {
      return autismclient.gui.f.c.PanelLayoutMath.load(this.init_I(), this.close_I(), 6);
   }

   private int close_I() {
      return autismclient.gui.f.c.PanelLayoutMath.init(this.init_I(), 6, 530);
   }

   private int open_I() {
      return this.flush_I() + 4;
   }

   private int poll_I() {
      return Math.max(1, this.close_I() - 8);
   }

   private int push_I() {
      return 6;
   }

   private int pop_I() {
      return this.push_I() + 4;
   }

   private int tick_I() {
      return this.pop_I() + this.read_I() + 2;
   }

   private int run_I() {
      return this.tick_I() + this.read_I() + 2;
   }

   private int get_I() {
      return this.run_I() + this.read_I() + 2;
   }

   private int set_I() {
      return this.get_I() + this.read_I() + 2;
   }

   private int put() {
      return this.set_I() + this.read_I() + 4 - this.push_I();
   }

   private int add() {
      return this.open_I() + this.load_I_Str("Back") + 4;
   }

   private int clear() {
      return this.add() + this.init_I_Str("Proxies") + 8;
   }

   private int reset() {
      return Math.max(40, this.open_I() + this.poll_I() - this.clear());
   }

   private int start() {
      return this.push_I() + this.put() + 4;
   }

   private int stop() {
      return this.flush_I();
   }

   private int next() {
      return Math.max(1, this.close_I());
   }

   private int prev() {
      int header = this.valid() - this.start();
      int cap = Math.max(3, this.load_I() * 65 / 100 / this.update_I());
      int shown = Math.max(3, Math.min(cap, this.load_List().size()));
      int content = header + shown * this.update_I() - 2 + 4;
      int available = Math.max(1, this.load_I() - 6 - this.start());
      return Math.min(available, content);
   }

   private int valid() {
      return this.start() + 4 + 12 + 4;
   }

   private int size() {
      return this.start() + this.prev() - 4;
   }

   private int data() {
      return Math.max(this.write_I(), Math.max(1, this.size() - this.valid()));
   }

   private int init_I_I(int rows) {
      return Math.max(0, rows * this.update_I() - 2 - this.data());
   }

   private autismclient.gui.f.b.ScrollbarMetricsHelper.Inner_DataUtil init_PoolEntry_DataUtil_I(int rows) {
      int contentPixels = Math.max(0, rows * this.update_I() - 2);
      int trackY = this.valid();
      int trackHeight = this.data();
      int maxScroll = this.init_I_I(rows);
      this.update_I = load(this.update_I, maxScroll);
      this.init_CacheEntry.load(this.update_I, maxScroll);
      return autismclient.gui.f.b.ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(contentPixels, this.data(), this.value(), trackY, 6, trackHeight, this.update_I);
   }

   private int value() {
      return this.stop() + this.next() - 4 - 6;
   }

   private int cache() {
      return this.stop() + 4;
   }

   private int state() {
      return this.value() - 2;
   }

   private int queue() {
      return Math.max(1, this.state() - this.cache());
   }

   private boolean init_Z() {
      return this.close_I() < 300 || this.load_I() < this.valid() + this.update_I() * 2;
   }

   private void init_V_Z(boolean visible) {
      if (this.init_EditBox != null) {
         this.init_EditBox.visible = visible;
      }

      if (this.load_EditBox != null) {
         this.load_EditBox.visible = visible;
      }

      if (this.read_EditBox != null) {
         this.read_EditBox.visible = visible;
      }

      if (this.write_EditBox != null) {
         this.write_EditBox.visible = visible;
      }

      if (this.update_EditBox != null) {
         this.update_EditBox.visible = visible;
      }

      if (this.sync_EditBox != null) {
         this.sync_EditBox.visible = visible;
      }
   }

   private int[] init_IArr_I_2(int availableWidth, int gap, String... labels) {
      int[] widths = new int[labels.length];
      int total = Math.max(0, labels.length - 1) * gap;

      for (int i = 0; i < labels.length; i++) {
         widths[i] = this.load_I_Str(labels[i]);
         total += widths[i];
      }

      return total <= availableWidth ? widths : this.init_IArr_I(availableWidth, gap, widths);
   }

   private int[] init_IArr_I(int availableWidth, int gap, int... preferredWidths) {
      int[] widths = new int[preferredWidths.length];
      int gaps = Math.max(0, preferredWidths.length - 1) * Math.max(0, gap);
      int preferredTotal = 0;

      for (int preferred : preferredWidths) {
         preferredTotal += Math.max(1, preferred);
      }

      int usable = Math.max(preferredWidths.length, availableWidth - gaps);
      int assigned = 0;

      for (int i = 0; i < preferredWidths.length; i++) {
         widths[i] = i == preferredWidths.length - 1
            ? Math.max(1, usable - assigned)
            : Math.max(1, Math.round((float)(Math.max(1, preferredWidths[i]) * usable) / preferredTotal));
         assigned += widths[i];
      }

      return widths;
   }

   private void set_V() {
      if (this.init_EditBox != null) {
         this.init_EditBox.setFocused(false);
      }

      if (this.load_EditBox != null) {
         this.load_EditBox.setFocused(false);
      }

      if (this.read_EditBox != null) {
         this.read_EditBox.setFocused(false);
      }

      if (this.write_EditBox != null) {
         this.write_EditBox.setFocused(false);
      }

      if (this.update_EditBox != null) {
         this.update_EditBox.setFocused(false);
      }

      if (this.sync_EditBox != null) {
         this.sync_EditBox.setFocused(false);
      }

      this.setFocused(null);
   }

   private int load_I_Str(String label) {
      return autismclient.gui.f.UiDensityMetrics.load(k.init(this.font, label, init_CharEncoder.init_Identifier_l(l.read), 0));
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean center) {
      this.init_V_GuiGraphicsExtract(graphics, text, x, y, color, center, 2147483647);
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean center, int maxWidth) {
      Font renderer = this.font;
      Identifier font = init_CharEncoder.init_Identifier_l(l.read);
      String value = text == null ? "" : text;
      if (maxWidth != 2147483647 && !center) {
         k.init(graphics, renderer, value, font, color, x, y, Math.max(1, maxWidth), false);
      } else {
         if (maxWidth != 2147483647) {
            value = k.init(renderer, value, maxWidth, font, color);
         }

         int w = k.init(renderer, value, font, color);
         int drawX = center ? x - w / 2 : x;
         k.init(graphics, renderer, value, font, color, drawX, y, false);
      }
   }

   private static int load(int value, int maxScroll) {
      return Math.max(0, Math.min(maxScroll, value));
   }

   private static String load_Str_Str(String value) {
      return init_Str_Str(value).toLowerCase(Locale.ROOT);
   }

   private static int load_I_I(int argb) {
      return ThemePaletteManager.init_I_I_ct_DataUtil(argb, ThemePaletteManager.Inner_DataUtil.OUTLINE);
   }

   private static int read_I_I(int argb) {
      return ThemePaletteManager.init_I_I_ct_DataUtil(argb, ThemePaletteManager.Inner_DataUtil.SUCCESS);
   }

   private enum Inner_DataUtil {
      ALL,
      ALIVE,
      DEAD,
      UNCHECKED,
      ENABLED;
   }

   private record Inner_StreamUtil(
      bW init_bW,
      int init_I,
      boolean init_Z,
      autismclient.gui.f.b.OverlayActionButton init_ByteReader,
      autismclient.gui.f.b.OverlayActionButton load,
      autismclient.gui.f.b.OverlayActionButton read
   ) {
   }
}
