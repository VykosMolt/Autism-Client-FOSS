package autismclient.gui.screens;

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
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = -7429889;
   private static final int MAX_PANEL_WIDTH = 530;
   private static final int read_I = 3;
   private static final int write_I = 70;
   private final Screen parent;
   private final List<autismclient.gui.ui.widgets.OverlayActionButton> buttons = new ArrayList<>();
   private final List<ProxiesScreen.Inner_StreamUtil> rows = new ArrayList<>();
   private final autismclient.gui.ui.widgets.ScrollOffsetController scroll = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private EditBox nameField;
   private EditBox addressField;
   private EditBox portField;
   private EditBox usernameField;
   private EditBox passwordField;
   private EditBox searchField;
   private ProxyProtocolType protocol = ProxyProtocolType.Socks5;
   private ProxiesScreen.Inner_DataUtil filter = ProxiesScreen.Inner_DataUtil.ALL;
   private bW editingProxy;
   private String searchQuery = "";
   private int update_I;
   private boolean init_Z;
   private int sync_I;
   private long seenRefreshRevision = -9223372036854775808L;
   private long seenImportRevision = -9223372036854775808L;
   private boolean importMenuOpen;
   private autismclient.gui.ui.widgets.OverlayActionButton fromFileButton;
   private autismclient.gui.ui.widgets.OverlayActionButton fromClipboardButton;
   private long cachedListRevision = -9223372036854775808L;
   private long cachedRefreshRevision = -9223372036854775808L;
   private String cachedQuery = null;
   private ProxiesScreen.Inner_DataUtil cachedFilter = null;
   private List<bW> cachedFiltered = List.of();
   private long activeRevision = -9223372036854775808L;
   private bW cachedActive;
   private List<bW> orderedSource;
   private bW orderedActive;
   private List<bW> cachedOrdered = List.of();

   public ProxiesScreen(Screen parent) {
      super(Component.literal("Proxies"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      int inputX = this.inputX();
      int inputW = this.inputWidth();
      int gap = 2;
      int portW = Math.max(36, Math.min(60, inputW / 6));
      int remaining = Math.max(2, inputW - portW - gap * 2);
      int nameW = Math.max(1, remaining * 2 / 5);
      int addressW = Math.max(1, remaining - nameW);
      int halfW = Math.max(1, (inputW - gap) / 2);
      this.nameField = this.createField(inputX, this.firstInputY(), nameW, "Name");
      this.addressField = this.createField(inputX + nameW + gap, this.firstInputY(), addressW, "Address");
      this.portField = this.createField(inputX + nameW + gap + addressW + gap, this.firstInputY(), portW, "Port");
      this.usernameField = this.createField(inputX, this.secondInputY(), halfW, "Username");
      this.passwordField = this.createField(inputX + halfW + gap, this.secondInputY(), Math.max(1, inputW - halfW - gap), "Password");
      this.searchField = this.createField(this.searchX(), this.searchY(), this.searchWidth(), "Search proxies");
      this.searchField.setHint(Component.literal("Search name/address..."));
      this.searchField.setResponder(value -> {
         this.searchQuery = trim(value);
         this.update_I = 0;
         this.scroll.jumpTo(0, 0);
         this.rebuildButtons();
      });
      this.setInputsVisible(!this.isCompact());
      ProxyListManager.getInstance().startGeoLookup(false);
      this.rebuildButtons();
   }

   private EditBox createField(int x, int y, int w, String hint) {
      EditBox field = new MsdfEditBox(this.font, x, y, w, this.controlHeight(), Component.literal(hint));
      field.setHint(Component.literal(hint));
      field.setMaxLength(256);
      this.addRenderableWidget(field);
      return field;
   }

   private void rebuildButtons() {
      this.buttons.clear();
      this.rows.clear();
      int ctl = this.controlHeight();
      int backWidth = this.buttonWidth("Back");
      this.buttons
         .add(
            autismclient.gui.ui.widgets.OverlayActionButton.create(
                  this.inputX(), this.searchY(), backWidth, ctl, Component.literal("Back"), b -> this.minecraft.gui.setScreen(this.parent)
               )
               .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
         );
      if (!this.isCompact()) {
         int actionGap = 2;
         int usableW = this.inputWidth();
         ProxyListManager manager = ProxyListManager.getInstance();
         ProxyListManager.Inner_TaskQueue refreshStatus = manager.getRefreshStatus();
         ProxyListManager.Inner_BufferHelper importStatus = manager.getImportStatus();
         boolean refreshRunning = refreshStatus.running();
         boolean importRunning = importStatus.running();
         String typeLabel = "Type: " + this.protocol;
         String addLabel = this.editingProxy == null ? "Add" : "Update";
         String clearLabel = this.editingProxy == null ? "Clear" : "Cancel";
         String refreshLabel = refreshRunning ? "Cancel" : "Refresh";
         String importLabel = importRunning ? "Cancel" : "Import";
         int actionY = this.actionRowY();
         int[] actionWidths = this.fitLabelWidths(usableW, actionGap, typeLabel, addLabel, clearLabel, refreshLabel, "Cleanup", importLabel, "Config");
         int actionX = this.inputX();
         this.buttons.add(autismclient.gui.ui.widgets.OverlayActionButton.create(actionX, actionY, actionWidths[0], ctl, Component.literal(typeLabel), b -> {
            this.protocol = this.protocol == ProxyProtocolType.Socks5 ? ProxyProtocolType.Socks4 : ProxyProtocolType.Socks5;
            this.rebuildButtons();
         }).setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY));
         actionX += actionWidths[0] + actionGap;
         this.buttons.add(autismclient.gui.ui.widgets.OverlayActionButton.create(actionX, actionY, actionWidths[1], ctl, Component.literal(addLabel), b -> {
            if (this.editingProxy == null) {
               this.addProxy();
            } else {
               this.updateProxy();
            }
         }).setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY));
         actionX += actionWidths[1] + actionGap;
         this.buttons
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.create(actionX, actionY, actionWidths[2], ctl, Component.literal(clearLabel), b -> this.resetForm())
                  .setTone(
                     this.editingProxy == null ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER
                  )
            );
         actionX += actionWidths[2] + actionGap;
         autismclient.gui.ui.widgets.OverlayActionButton refresh = autismclient.gui.ui.widgets.OverlayActionButton.create(
               actionX, actionY, actionWidths[3], ctl, Component.literal(refreshLabel), b -> this.refreshAll()
            )
            .setTone(
               refreshRunning ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY
            );
         refresh.active = !importRunning;
         this.buttons.add(refresh);
         actionX += actionWidths[3] + actionGap;
         autismclient.gui.ui.widgets.OverlayActionButton cleanup = autismclient.gui.ui.widgets.OverlayActionButton.create(
               actionX, actionY, actionWidths[4], ctl, Component.literal("Cleanup"), b -> this.cleanup()
            )
            .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY);
         cleanup.active = !refreshRunning && !importRunning;
         this.buttons.add(cleanup);
         actionX += actionWidths[4] + actionGap;
         this.buttons
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.create(actionX, actionY, actionWidths[5], ctl, Component.literal(importLabel), b -> this.open_V())
                  .setTone(
                     importRunning ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY
                  )
            );
         actionX += actionWidths[5] + actionGap;
         this.buttons
            .add(
               autismclient.gui.ui.widgets.OverlayActionButton.create(
                     actionX, actionY, actionWidths[6], ctl, Component.literal("Config"), b -> this.minecraft.gui.setScreen(new ProxyConfigScreen(this))
                  )
                  .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY)
            );
         int filterY = this.filterRowY();
         int filterX = this.inputX();
         int filterGap = 2;
         int[] filterWidths = this.fitLabelWidths(usableW, filterGap, "All", "Alive", "Dead", "Unchecked", "Enabled", "Sort", "Clear All");
         this.addFilterButton(filterX, filterY, filterWidths[0], ProxiesScreen.Inner_DataUtil.ALL, "All");
         filterX += filterWidths[0] + filterGap;
         this.addFilterButton(filterX, filterY, filterWidths[1], ProxiesScreen.Inner_DataUtil.ALIVE, "Alive");
         filterX += filterWidths[1] + filterGap;
         this.addFilterButton(filterX, filterY, filterWidths[2], ProxiesScreen.Inner_DataUtil.DEAD, "Dead");
         filterX += filterWidths[2] + filterGap;
         this.addFilterButton(filterX, filterY, filterWidths[3], ProxiesScreen.Inner_DataUtil.UNCHECKED, "Unchecked");
         filterX += filterWidths[3] + filterGap;
         this.addFilterButton(filterX, filterY, filterWidths[4], ProxiesScreen.Inner_DataUtil.ENABLED, "Enabled");
         filterX += filterWidths[4] + filterGap;
         autismclient.gui.ui.widgets.OverlayActionButton sort = autismclient.gui.ui.widgets.OverlayActionButton.create(
               filterX, filterY, filterWidths[5], ctl, Component.literal("Sort"), b -> this.sort()
            )
            .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY);
         sort.active = !refreshRunning && !importRunning && manager.size() > 1;
         this.buttons.add(sort);
         filterX += filterWidths[5] + filterGap;
         autismclient.gui.ui.widgets.OverlayActionButton clearAll = autismclient.gui.ui.widgets.OverlayActionButton.create(
               filterX, filterY, filterWidths[6], ctl, Component.literal("Clear All"), b -> this.clearAll()
            )
            .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER);
         clearAll.active = !refreshRunning && !importRunning && manager.size() > 0;
         this.buttons.add(clearAll);
         List<bW> proxies = this.orderedProxies();
         boolean hasActiveSection = proxies.size() > this.filteredProxies().size();
         int maxScroll = this.maxScroll(proxies.size());
         this.update_I = clampScroll(this.update_I, maxScroll);
         this.scroll.jumpTo(this.update_I, maxScroll);
         int pitch = this.rowPitch();
         int rowH = this.rowHeight();
         int top = this.listTop();
         int bottom = this.listBottom();
         int firstVisible = this.update_I / pitch;
         int rowY = top - this.update_I % pitch;

         for (int i = firstVisible; i < proxies.size() && rowY + rowH <= bottom; i++) {
            bW proxy = proxies.get(i);
            if (rowY + rowH <= top) {
               rowY += pitch;
            } else {
               this.rows.add(this.createRow(proxy, rowY, hasActiveSection && i == 0));
               rowY += pitch;
            }
         }
      }
   }

   private ProxiesScreen.Inner_StreamUtil createRow(bW proxy, int rowY, boolean activeSection) {
      int btnH = this.rowButtonHeight();
      int btnY = rowY + (this.rowHeight() - btnH) / 2;
      int toggleW = Math.max(this.buttonWidth("ON"), this.buttonWidth("OFF"));
      autismclient.gui.ui.widgets.OverlayActionButton toggle = autismclient.gui.ui.widgets.OverlayActionButton.create(
         this.rowLeft() + 4, btnY, toggleW, btnH, Component.literal(proxy.init_Z ? "ON" : "OFF"), b -> this.toggleProxy(proxy)
      );
      toggle.setTone(
            proxy.init_Z ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SUCCESS : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER
         )
         .setToggleState(proxy.init_Z)
         .setAnimationKey("proxy-enabled:" + proxy.read + ":" + proxy.init_I);
      int checkW = this.buttonWidth("Check");
      int deleteX = this.rowRight() - 1 - btnH;
      int checkX = deleteX - 2 - checkW;
      autismclient.gui.ui.widgets.OverlayActionButton check = autismclient.gui.ui.widgets.OverlayActionButton.create(
         checkX, btnY, checkW, btnH, Component.literal(proxy.init_bW_CachePool == bW.Inner_CachePool.CHECKING ? "..." : "Check"), b -> this.checkProxy(proxy)
      );
      check.setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.PRIMARY);
      check.active = proxy.init_bW_CachePool != bW.Inner_CachePool.CHECKING;
      autismclient.gui.ui.widgets.OverlayActionButton delete = autismclient.gui.ui.widgets.OverlayActionButton.create(deleteX, btnY, btnH, btnH, Component.empty(), b -> this.deleteProxy(proxy));
      delete.setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.DANGER).setIcon(GuiIconAtlas.DELETE);
      return new ProxiesScreen.Inner_StreamUtil(proxy, rowY, activeSection, toggle, check, delete);
   }

   private void addFilterButton(int x, int y, int width, ProxiesScreen.Inner_DataUtil value, String label) {
      autismclient.gui.ui.widgets.OverlayActionButton button = autismclient.gui.ui.widgets.OverlayActionButton.create(x, y, width, this.controlHeight(), Component.literal(label), b -> {
         this.filter = value;
         this.update_I = 0;
         this.scroll.jumpTo(0, 0);
         this.rebuildButtons();
      });
      button.setTone(
         this.filter == value ? autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.FILTER_ON : autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.FILTER_OFF
      );
      this.buttons.add(button);
   }

   @Override
   public void tick() {
      super.tick();
      int maxScroll = this.maxScroll(this.orderedProxies().size());
      this.update_I = clampScroll(this.update_I, maxScroll);
      this.scroll.jumpTo(this.update_I, maxScroll);
      ProxyListManager.Inner_TaskQueue status = ProxyListManager.getInstance().getRefreshStatus();
      if (status.revision() != this.seenRefreshRevision) {
         this.seenRefreshRevision = status.revision();
         this.rebuildButtons();
      }

      ProxyListManager.Inner_BufferHelper importStatus = ProxyListManager.getInstance().getImportStatus();
      if (importStatus.revision() != this.seenImportRevision) {
         this.seenImportRevision = importStatus.revision();
         this.rebuildButtons();
      }
   }

   private void addProxy() {
      bW proxy = this.proxyFromFields();
      if (proxy != null) {
         if (ProxyListManager.getInstance().addProxy(proxy)) {
            this.editingProxy = null;
            this.clearFields();
            this.toast("Added proxy " + proxy.read + ":" + proxy.init_I + ".", GuiColorPalette.open);
            this.checkProxy(proxy);
         } else {
            this.toast("Proxy already exists or is invalid.", GuiColorPalette.poll);
         }

         this.rebuildButtons();
      }
   }

   private void updateProxy() {
      if (this.editingProxy != null) {
         bW updated = this.proxyFromFields();
         if (updated != null) {
            if (ProxyListManager.getInstance().updateProxy(this.editingProxy, updated)) {
               this.toast("Updated " + this.editingProxy.init_Str() + ".", GuiColorPalette.open);
               this.resetForm();
            } else {
               this.toast("Proxy already exists or is invalid.", GuiColorPalette.poll);
               this.rebuildButtons();
            }
         }
      }
   }

   private void toggleProxy(bW proxy) {
      if (proxy != null) {
         int sectionsBefore = this.orderedProxies().size() - this.filteredProxies().size();
         ProxyListManager.getInstance().setProxyEnabled(proxy, !proxy.init_Z);
         this.init_V_I(sectionsBefore);
         this.toast(proxy.init_Z ? "Enabled " + proxy.init_Str() + "." : "Disabled proxy.", proxy.init_Z ? GuiColorPalette.open : GuiColorPalette.close);
         this.rebuildButtons();
      }
   }

   private void init_V_I(int sectionsBefore) {
      int sectionsAfter = this.orderedProxies().size() - this.filteredProxies().size();
      if (sectionsAfter != sectionsBefore) {
         int maxScroll = this.maxScroll(this.orderedProxies().size());
         this.update_I = clampScroll(this.update_I + (sectionsAfter - sectionsBefore) * this.rowPitch(), maxScroll);
         this.scroll.jumpTo(this.update_I, maxScroll);
      }
   }

   private void deleteProxy(bW proxy) {
      if (proxy != null) {
         ProxyListManager.getInstance().removeProxy(proxy);
         if (proxy.equals(this.editingProxy)) {
            this.editingProxy = null;
         }

         this.toast("Deleted " + proxy.init_Str() + ".", GuiColorPalette.close);
         this.rebuildButtons();
      }
   }

   private void checkProxy(bW proxy) {
      if (proxy != null && proxy.init_bW_CachePool != bW.Inner_CachePool.CHECKING) {
         proxy.init_bW_CachePool = bW.Inner_CachePool.CHECKING;
         proxy.init_J = 0L;
         this.rebuildButtons();
         autismclient.util.BackgroundTaskExecutor.submit("Autism-Proxy-Check", () -> {
            proxy.init_I_I(ProxyListManager.getInstance().getTimeoutMs());
            if (this.minecraft != null) {
               this.minecraft.execute(() -> {
                  this.toast(proxy.init_Str() + " is " + this.statusLabel(proxy) + ".", proxy.init_bW_CachePool.init_I());
                  this.rebuildButtons();
               });
            }
         });
      }
   }

   private void refreshAll() {
      ProxyListManager manager = ProxyListManager.getInstance();
      if (manager.getImportStatus().running()) {
         this.toast("Finish import before refreshing.", -14249);
      } else {
         ProxyListManager.Inner_TaskQueue currentStatus = manager.getRefreshStatus();
         if (currentStatus.running()) {
            if (manager.cancelRefresh()) {
               this.toast("Proxy refresh canceled.", -14249);
               this.rebuildButtons();
            }
         } else if (manager.size() == 0) {
            this.toast("No proxies to refresh.", -14249);
         } else if (!manager.startRefresh(true)) {
            this.toast("Proxy refresh could not start.", -14249);
         } else {
            long generation = manager.getRefreshStatus().generation();
            this.toast("Refreshing proxies...", -7429889);
            this.rebuildButtons();
            autismclient.util.BackgroundTaskExecutor.init("Autism-Proxy-Refresh-Watch", () -> !manager.getRefreshStatus().running(), () -> {
               if (this.minecraft != null) {
                  this.minecraft.execute(() -> {
                     ProxyListManager.Inner_TaskQueue finished = manager.getRefreshStatus();
                     if (finished.generation() == generation && finished.checkedCount() >= finished.totalCount()) {
                        this.toast("Proxy refresh finished.", GuiColorPalette.open);
                     }

                     this.rebuildButtons();
                  });
               }
            }, 100L);
         }
      }
   }

   private void cleanup() {
      ProxyListManager manager = ProxyListManager.getInstance();
      if (manager.getRefreshStatus().running()) {
         this.toast("Cancel refresh before cleanup.", -14249);
      } else if (manager.getImportStatus().running()) {
         this.toast("Finish import before cleanup.", -14249);
      } else {
         int before = manager.size();
         manager.pruneAndSort();
         int removed = Math.max(0, before - manager.size());
         this.toast(
            removed == 0 ? "Cleanup finished. Nothing removed." : "Cleanup removed " + removed + " proxies.", removed == 0 ? GuiColorPalette.close : GuiColorPalette.open
         );
         this.rebuildButtons();
      }
   }

   private void sort() {
      ProxyListManager manager = ProxyListManager.getInstance();
      if (manager.getRefreshStatus().running()) {
         this.toast("Cancel refresh before sorting proxies.", -14249);
      } else if (manager.getImportStatus().running()) {
         this.toast("Finish import before sorting proxies.", -14249);
      } else {
         if (manager.sortNow()) {
            this.toast("Sorted proxies by latency.", GuiColorPalette.open);
         } else {
            this.toast("Nothing to sort.", GuiColorPalette.close);
         }

         this.rebuildButtons();
      }
   }

   private void clearAll() {
      ProxyListManager manager = ProxyListManager.getInstance();
      if (manager.getRefreshStatus().running()) {
         this.toast("Cancel refresh before clearing proxies.", -14249);
      } else if (manager.getImportStatus().running()) {
         this.toast("Finish import before clearing proxies.", -14249);
      } else {
         int removed = manager.clearAllProxies();
         this.editingProxy = null;
         this.clearFields();
         this.update_I = 0;
         this.scroll.jumpTo(0, 0);
         this.toast(removed == 0 ? "Proxy list is already empty." : "Cleared " + removed + " proxies.", removed == 0 ? GuiColorPalette.close : GuiColorPalette.open);
         this.rebuildButtons();
      }
   }

   private void open_V() {
      ProxyListManager manager = ProxyListManager.getInstance();
      ProxyListManager.Inner_BufferHelper status = manager.getImportStatus();
      if (status.running()) {
         if (manager.cancelImport()) {
            this.toast("Proxy import canceled.", -14249);
            this.rebuildButtons();
         }
      } else if (manager.getRefreshStatus().running()) {
         this.toast("Cancel refresh before importing.", -14249);
      } else {
         this.importMenuOpen = true;
      }
   }

   private void importFromFile() {
      this.importMenuOpen = false;
      ProxyListManager manager = ProxyListManager.getInstance();
      PointerBuffer filters = BufferUtils.createPointerBuffer(1);
      ByteBuffer txtFilter = MemoryUtil.memASCII("*.txt");
      try {
         filters.put(txtFilter);
         filters.rewind();
         String selectedFile = TinyFileDialogs.tinyfd_openFileDialog("Import Proxies", null, filters, null, false);
         if (selectedFile != null) {
            File file = new File(selectedFile);
            if (!manager.importFromFileAsync(file)) {
               this.toast("Proxy import could not start.", -14249);
            } else {
               this.onImportStarted(manager, manager.getImportStatus().generation());
            }
         }
      } finally {
         MemoryUtil.memFree(txtFilter);
      }
   }

   private void importFromClipboard() {
      this.importMenuOpen = false;
      ProxyListManager manager = ProxyListManager.getInstance();
      String clipboard = this.minecraft != null ? this.minecraft.keyboardHandler.getClipboard() : null;
      if (clipboard == null || clipboard.isBlank()) {
         this.toast("Clipboard is empty.", -14249);
      } else if (!manager.importFromTextAsync(clipboard)) {
         this.toast("Proxy import could not start.", -14249);
      } else {
         this.onImportStarted(manager, manager.getImportStatus().generation());
      }
   }

   private void onImportStarted(ProxyListManager manager, long generation) {
      this.toast("Importing proxies...", -7429889);
      this.rebuildButtons();
      autismclient.util.BackgroundTaskExecutor.init("Autism-Proxy-Import-Watch", () -> !manager.getImportStatus().running(), () -> {
         if (this.minecraft != null) {
            this.minecraft.execute(() -> {
               ProxyListManager.Inner_BufferHelper finished = manager.getImportStatus();
               if (finished.generation() == generation && !finished.canceled()) {
                  this.toast("Imported " + finished.addedCount() + " proxies.", finished.addedCount() > 0 ? GuiColorPalette.open : -14249);
                  this.update_I = 0;
                  this.scroll.jumpTo(0, 0);
               }

               this.rebuildButtons();
            });
         }
      }, 100L);
   }

   private autismclient.gui.f.NetHelper[] importMenuRects() {
      int btnH = this.controlHeight();
      int btnW = Math.max(this.buttonWidth("From File"), this.buttonWidth("From Clipboard")) + 16;
      int cardW = btnW + 8;
      int titleH = 12;
      int cardH = 4 + titleH + 2 + btnH * 2 + 2 + 4;
      int cardX = (this.virtualWidth() - cardW) / 2;
      int cardY = (this.virtualHeight() - cardH) / 2;
      int btnX = cardX + 4;
      int fileY = cardY + 4 + titleH + 2;
      int clipY = fileY + btnH + 2;
      return new autismclient.gui.f.NetHelper[]{
         autismclient.gui.f.NetHelper.init(cardX, cardY, cardW, cardH),
         autismclient.gui.f.NetHelper.init(btnX, fileY, btnW, btnH),
         autismclient.gui.f.NetHelper.init(btnX, clipY, btnW, btnH)
      };
   }

   private void ensureImportButtons() {
      if (this.fromFileButton == null || this.fromFileButton.getHeight() != this.controlHeight()) {
         this.fromFileButton = autismclient.gui.ui.widgets.OverlayActionButton.create(0, 0, 1, this.controlHeight(), Component.literal("From File"), b -> this.importFromFile())
            .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY);
      }

      if (this.fromClipboardButton == null || this.fromClipboardButton.getHeight() != this.controlHeight()) {
         this.fromClipboardButton = autismclient.gui.ui.widgets.OverlayActionButton.create(0, 0, 1, this.controlHeight(), Component.literal("From Clipboard"), b -> this.importFromClipboard())
            .setTone(autismclient.gui.ui.widgets.OverlayActionButton.ButtonTone.SECONDARY);
      }
   }

   private void layoutImportButtons(autismclient.gui.f.NetHelper[] rects) {
      this.fromFileButton.setX(rects[1].read());
      this.fromFileButton.setY(rects[1].write());
      this.fromFileButton.setWidth(rects[1].update());
      this.fromClipboardButton.setX(rects[2].read());
      this.fromClipboardButton.setY(rects[2].write());
      this.fromClipboardButton.setWidth(rects[2].update());
   }

   private void renderImportMenu(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      this.ensureImportButtons();
      autismclient.gui.f.NetHelper[] rects = this.importMenuRects();
      this.layoutImportButtons(rects);
      autismclient.gui.f.NetHelper card = rects[0];
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -939524096);
      autismclient.gui.f.PacketUtil.load(graphics, card, 5, -401402080, outlineColor(GuiColorPalette.update));
      this.init_V_GuiGraphicsExtract(
         graphics, "Import proxies from", card.read() + card.update() / 2, this.centeredTextY(card.write() + 4, 12), GuiColorPalette.flush, true
      );
      autismclient.gui.ui.widgets.OverlayActionButton.render(graphics, this.font, this.fromFileButton, mouseX, mouseY);
      autismclient.gui.ui.widgets.OverlayActionButton.render(graphics, this.font, this.fromClipboardButton, mouseX, mouseY);
   }

   private void clearFields() {
      this.nameField.setValue("");
      this.addressField.setValue("");
      this.portField.setValue("");
      this.usernameField.setValue("");
      this.passwordField.setValue("");
   }

   private void resetForm() {
      this.editingProxy = null;
      this.clearFields();
      this.unfocusFields();
      this.rebuildButtons();
   }

   private bW proxyFromFields() {
      bW proxy = new bW();
      proxy.load_Str = trim(this.nameField.getValue());
      proxy.read = trim(this.addressField.getValue());
      proxy.write = trim(this.usernameField.getValue());
      proxy.update = this.passwordField.getValue();
      proxy.init_bZ = this.protocol;
      if (proxy.read.isBlank()) {
         this.toast("Enter a proxy address first.", -14249);
         return null;
      }

      try {
         proxy.init_I = Integer.parseInt(trim(this.portField.getValue()));
      } catch (NumberFormatException e) {
         this.toast("Invalid proxy port.", -14249);
         return null;
      }

      if (!proxy.init_Z()) {
         this.toast("Proxy must have a valid address and port.", -14249);
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
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -15987181);
         int panelX = this.panelX();
         this.drawCard(graphics, panelX, this.panelY(), this.panelWidth(), this.headerCardHeight(), -401402080);
         this.drawCard(graphics, this.listCardX(), this.listCardY(), this.listCardWidth(), this.listCardHeight(), -1206840035);
         int headerTextY = this.centeredTextY(this.searchY(), this.controlHeight());
         this.init_V_GuiGraphicsExtract(graphics, "Proxies", this.titleX(), headerTextY, GuiColorPalette.flush, false);
         if (this.isCompact()) {
            this.init_V_GuiGraphicsExtract(
               graphics, "Window too small.", this.inputX(), this.centeredTextY(this.firstInputY(), 12), GuiColorPalette.close, false, this.inputWidth()
            );
         }

         List<bW> proxies = this.filteredProxies();
         ProxyListManager manager = ProxyListManager.getInstance();
         int total = manager.size();
         int enabled = manager.countEnabledValidProxies();
         ProxyListManager.Inner_TaskQueue refreshStatus = manager.getRefreshStatus();
         ProxyListManager.Inner_BufferHelper importStatus = manager.getImportStatus();
         String summary = proxies.size() == total ? total + " proxies  enabled " + enabled : proxies.size() + " / " + total + " proxies  enabled " + enabled;
         String workLabel = this.importStatusLabel(importStatus);
         int workColor = importStatus.running() ? -7429889 : -14249;
         if (workLabel.isBlank()) {
            workLabel = this.refreshStatusLabel(refreshStatus);
            workColor = refreshStatus.canceling() ? -14249 : -7429889;
         }

         int listInnerW = this.listCardWidth() - 8;
         int workLabelW = workLabel.isBlank() ? 0 : Math.min(160, listInnerW / 2);
         int listHeaderY = this.centeredTextY(this.listCardY() + 4, 12);
         this.init_V_GuiGraphicsExtract(graphics, summary, this.listCardX() + 4, listHeaderY, GuiColorPalette.flush, false, listInnerW - workLabelW - 4);
         if (!workLabel.isBlank()) {
            this.init_V_GuiGraphicsExtract(graphics, workLabel, this.listCardX() + this.listCardWidth() - 4 - workLabelW, listHeaderY, workColor, false, workLabelW);
         }

         this.renderWorkStatus(graphics, refreshStatus, importStatus);
         autismclient.gui.ui.UiClipStack.getInstance()
            .push(
               graphics,
               autismclient.gui.f.NetHelper.init(this.rowLeft(), this.listTop(), Math.max(0, this.rowRight() - this.rowLeft()), Math.max(0, this.listBottom() - this.listTop()))
            );

         try {
            for (ProxiesScreen.Inner_StreamUtil row : this.rows) {
               this.renderRow(graphics, row, virtualMouseX, virtualMouseY);
            }
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(graphics);
         }

         if (total == 0) {
            this.init_V_GuiGraphicsExtract(
               graphics,
               "No proxies saved yet. Add one manually or import a .txt list.",
               this.rowLeft() + 4,
               this.centeredTextY(this.listTop(), this.rowHeight()),
               GuiColorPalette.close,
               false,
               this.rowWidth() - 4
            );
         } else if (proxies.isEmpty()) {
            this.init_V_GuiGraphicsExtract(
               graphics,
               "No proxies match the current search or filter.",
               this.rowLeft() + 4,
               this.centeredTextY(this.listTop(), this.rowHeight()),
               GuiColorPalette.close,
               false,
               this.rowWidth() - 4
            );
         }

         for (autismclient.gui.ui.widgets.OverlayActionButton button : this.buttons) {
            autismclient.gui.ui.widgets.OverlayActionButton.render(graphics, this.font, button, virtualMouseX, virtualMouseY);
         }

         if (!this.isCompact()) {
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.scrollbarMetrics(this.orderedProxies().size());
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(graphics, scrollbar, scrollbar.containsTrack(virtualMouseX, virtualMouseY), this.init_Z);
         }

         super.extractRenderState(graphics, virtualMouseX, virtualMouseY, delta);
         if (this.importMenuOpen) {
            this.renderImportMenu(graphics, virtualMouseX, virtualMouseY);
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   @Override
   public void removed() {
      super.removed();
   }

   private void renderRow(GuiGraphicsExtractor graphics, ProxiesScreen.Inner_StreamUtil row, int mouseX, int mouseY) {
      bW proxy = row.proxy;
      boolean selected = proxy.equals(this.editingProxy);
      int x = this.rowLeft();
      int y = row.y;
      int w = this.rowWidth();
      int h = this.rowHeight();
      int fill = proxy.init_Z ? successColor(858052714) : (selected ? outlineColor(606804509) : 403771667);
      autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), 4, fill);
      if (proxy.init_Z) {
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y + 2, 2, h - 4), 1, successColor(GuiColorPalette.sync));
      } else if (selected) {
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y + 2, 2, h - 4), 1, outlineColor(-10866632));
      }

      if (row.activeSection()) {
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(x, y + 2, 2, h - 4), 1, successColor(GuiColorPalette.sync));
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y + h, w, 1), outlineColor(GuiColorPalette.update));
      }

      String name = proxy.init_Str().isBlank() ? "(unnamed)" : proxy.init_Str();
      boolean hasAuth = proxy.write != null && !proxy.write.isBlank();
      String address = proxy.init_bZ + "  " + proxy.read + ":" + proxy.init_I + (hasAuth ? "  Auth" : "");
      String region = proxy.load();
      String status = this.statusLabel(proxy);
      int ty = this.centeredTextY(y, h);
      int nameX = row.toggleButton.getX() + row.toggleButton.getWidth() + 4;
      int metaRight = row.load.getX() - 4;
      int statusW = Math.min(this.textWidth("Unchecked"), Math.max(20, (metaRight - nameX) / 4));
      int statusX = metaRight - statusW;
      int regionW = Math.min(70, Math.max(20, (metaRight - nameX) / 4));
      int regionX = statusX - 4 - regionW;
      int textW = Math.max(20, regionX - 4 - nameX);
      int nameW = Math.min(this.textWidth(name), textW);
      this.init_V_GuiGraphicsExtract(graphics, name, nameX, ty, proxy.init_Z ? GuiColorPalette.open : GuiColorPalette.flush, false, nameW);
      int addressX = nameX + nameW + 8;
      int addressW = nameX + textW - addressX;
      if (addressW > 12) {
         this.init_V_GuiGraphicsExtract(graphics, address, addressX, ty, GuiColorPalette.close, false, addressW);
      }

      this.init_V_GuiGraphicsExtract(graphics, region, regionX, ty, proxy.init_I(), false, regionW);
      this.init_V_GuiGraphicsExtract(graphics, status, statusX, ty, proxy.init_bW_CachePool.init_I(), false, statusW);
      autismclient.gui.ui.widgets.OverlayActionButton.render(graphics, this.font, row.toggleButton, mouseX, mouseY);
      autismclient.gui.ui.widgets.OverlayActionButton.render(graphics, this.font, row.load, mouseX, mouseY);
      autismclient.gui.ui.widgets.OverlayActionButton.render(graphics, this.font, row.read, mouseX, mouseY);
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (this.importMenuOpen && Compat263.keyEventGlfwKey(input) == 256) {
         this.importMenuOpen = false;
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      MouseButtonEvent virtualEvent = toVirtual(event);
      if (this.importMenuOpen) {
         if (Compat263.mouseEventGlfwButton(virtualEvent) == 0) {
            this.ensureImportButtons();
            this.layoutImportButtons(this.importMenuRects());
            if (autismclient.gui.ui.widgets.OverlayActionButton.handleClick(this.fromFileButton, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
               return true;
            }

            if (autismclient.gui.ui.widgets.OverlayActionButton.handleClick(this.fromClipboardButton, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
               return true;
            }

            autismclient.gui.f.NetHelper card = this.importMenuRects()[0];
            if (!card.init_Z((int)virtualEvent.x(), (int)virtualEvent.y())) {
               this.importMenuOpen = false;
            }
         }

         return true;
      } else {
         if (Compat263.mouseEventGlfwButton(virtualEvent) != 0) {
            return super.mouseClicked(virtualEvent, doubleClick);
         }

         if (this.isCompact()) {
            return super.mouseClicked(virtualEvent, doubleClick);
         }

         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.scrollbarMetrics(this.orderedProxies().size());
         if (scrollbar.isScrollable() && scrollbar.containsTrack(virtualEvent.x(), virtualEvent.y())) {
            this.init_Z = true;
            this.sync_I = scrollbar.containsThumb(virtualEvent.x(), virtualEvent.y())
               ? Math.max(0, (int)Math.round(virtualEvent.y()) - scrollbar.thumbY())
               : scrollbar.thumbHeight() / 2;
            this.update_I = clampScroll(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbar, virtualEvent.y(), this.sync_I), scrollbar.maxScroll());
            this.scroll.jumpTo(this.update_I, scrollbar.maxScroll());
            this.rebuildButtons();
            this.unfocusFields();
            return true;
         }

         for (autismclient.gui.ui.widgets.OverlayActionButton button : this.buttons) {
            if (autismclient.gui.ui.widgets.OverlayActionButton.handleClick(button, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
               return true;
            }
         }

         for (ProxiesScreen.Inner_StreamUtil row : this.rows) {
            if (this.clickRow(row, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
               return true;
            }
         }

         this.unfocusFields();
         return super.mouseClicked(virtualEvent, doubleClick);
      }
   }

   private boolean clickRow(ProxiesScreen.Inner_StreamUtil row, double x, double y, int button) {
      if (row == null) {
         return false;
      }

      if (!this.isOver(row.toggleButton, x, y) && !this.isOver(row.load, x, y) && !this.isOver(row.read, x, y)) {
         if (x >= this.rowLeft() && x < this.rowRight() && y >= row.y && y < row.y + this.rowHeight()) {
            this.startEditing(row.proxy);
            return true;
         } else {
            return false;
         }
      } else {
         autismclient.gui.ui.widgets.OverlayActionButton.handleClick(row.toggleButton, x, y, button);
         autismclient.gui.ui.widgets.OverlayActionButton.handleClick(row.load, x, y, button);
         autismclient.gui.ui.widgets.OverlayActionButton.handleClick(row.read, x, y, button);
         return true;
      }
   }

   private boolean isOver(autismclient.gui.ui.widgets.OverlayActionButton button, double mouseX, double mouseY) {
      return button != null
         && button.visible
         && mouseX >= button.getX()
         && mouseX < button.getX() + button.getWidth()
         && mouseY >= button.getY()
         && mouseY < button.getY() + button.getHeight();
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.init_Z) {
         this.init_Z = false;
         return true;
      } else {
         return super.mouseReleased(toVirtual(event));
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      MouseButtonEvent virtualEvent = toVirtual(event);
      if (this.init_Z) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbar = this.scrollbarMetrics(this.orderedProxies().size());
         this.update_I = clampScroll(autismclient.gui.ui.widgets.ScrollbarMetricsHelper.scrollOffsetForMouseY(scrollbar, virtualEvent.y(), this.sync_I), scrollbar.maxScroll());
         this.scroll.jumpTo(this.update_I, scrollbar.maxScroll());
         this.rebuildButtons();
         return true;
      } else {
         return super.mouseDragged(virtualEvent, cA.init_D_D(dx), cA.init_D_D(dy));
      }
   }

   @Override
   public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
      x = cA.init_D_D(x);
      y = cA.init_D_D(y);
      if (this.isCompact()) {
         return super.mouseScrolled(x, y, scrollX, scrollY);
      }

      if (!(x < this.listCardX()) && !(x >= this.listCardX() + this.listCardWidth()) && !(y < this.listCardY()) && !(y >= this.listCardY() + this.listCardHeight())) {
         int maxScroll = this.maxScroll(this.orderedProxies().size());
         if (maxScroll <= 0) {
            return true;
         }

         int next = this.update_I - (int)Math.signum(scrollY) * this.rowPitch();
         this.update_I = clampScroll(next, maxScroll);
         this.scroll.setOffset(this.update_I, maxScroll);
         this.rebuildButtons();
         return true;
      } else {
         return super.mouseScrolled(x, y, scrollX, scrollY);
      }
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.parent);
   }

   private void startEditing(bW proxy) {
      this.editingProxy = proxy;
      if (proxy != null) {
         this.nameField.setValue(trim(proxy.load_Str));
         this.addressField.setValue(trim(proxy.read));
         this.portField.setValue(proxy.init_I <= 0 ? "" : Integer.toString(proxy.init_I));
         this.usernameField.setValue(trim(proxy.write));
         this.passwordField.setValue(proxy.update == null ? "" : proxy.update);
         this.protocol = proxy.init_bZ == null ? ProxyProtocolType.Socks5 : proxy.init_bZ;
      }

      this.rebuildButtons();
   }

   private List<bW> filteredProxies() {
      ProxyListManager manager = ProxyListManager.getInstance();
      long listRevision = manager.getListRevision();
      long refreshRevision = manager.getRefreshStatus().revision();
      String query = normalize(this.searchQuery);
      boolean statusSensitive = this.filter != ProxiesScreen.Inner_DataUtil.ALL || !query.isEmpty();
      boolean cacheValid = this.cachedFilter == this.filter
         && query.equals(this.cachedQuery)
         && this.cachedListRevision == listRevision
         && (!statusSensitive || this.cachedRefreshRevision == refreshRevision);
      if (cacheValid) {
         return this.cachedFiltered;
      }

      List<bW> proxies = manager.getItems();
      List<bW> filtered;
      if (query.isEmpty() && this.filter == ProxiesScreen.Inner_DataUtil.ALL) {
         filtered = proxies;
      } else {
         filtered = new ArrayList<>();

         for (bW proxy : proxies) {
            if (proxy != null && this.matchesFilter(proxy) && (query.isEmpty() || this.matchesQuery(proxy, query))) {
               filtered.add(proxy);
            }
         }
      }

      this.cachedFiltered = filtered;
      this.cachedQuery = query;
      this.cachedFilter = this.filter;
      this.cachedListRevision = listRevision;
      this.cachedRefreshRevision = refreshRevision;
      return this.cachedFiltered;
   }

   private bW activeProxy() {
      ProxyListManager manager = ProxyListManager.getInstance();
      long revision = manager.getListRevision();
      if (revision != this.activeRevision) {
         this.cachedActive = manager.getActiveProxy();
         this.activeRevision = revision;
      }

      return this.cachedActive;
   }

   private List<bW> orderedProxies() {
      List<bW> filtered = this.filteredProxies();
      bW active = this.activeProxy();
      if (filtered == this.orderedSource && active == this.orderedActive) {
         return this.cachedOrdered;
      }

      List<bW> display = filtered;
      if (active != null && filtered.indexOf(active) > 0) {
         display = new ArrayList<>(filtered.size() + 1);
         display.add(active);
         display.addAll(filtered);
      }

      this.orderedSource = filtered;
      this.orderedActive = active;
      this.cachedOrdered = display;
      return display;
   }

   private boolean matchesFilter(bW proxy) {
      boolean var10000;
      switch (this.filter) {
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

   private boolean matchesQuery(bW proxy, String query) {
      String haystack = normalize(
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
            + trim(proxy.write)
            + " "
            + proxy.read()
      );
      return haystack.contains(query);
   }

   private String statusLabel(bW proxy) {
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

   private String refreshStatusLabel(ProxyListManager.Inner_TaskQueue status) {
      if (status == null) {
         return "";
      } else if (status.running()) {
         return "Refreshing " + status.checkedCount() + "/" + status.totalCount();
      } else {
         return status.canceling() ? "Canceling" : "";
      }
   }

   private String importStatusLabel(ProxyListManager.Inner_BufferHelper status) {
      if (status == null) {
         return "";
      } else if (status.running()) {
         return "Importing " + status.scannedCount() + " +" + status.parsedCount();
      } else {
         return status.canceling() ? "Canceling import" : "";
      }
   }

   private void renderWorkStatus(GuiGraphicsExtractor graphics, ProxyListManager.Inner_TaskQueue refreshStatus, ProxyListManager.Inner_BufferHelper importStatus) {
      boolean importing = importStatus != null && importStatus.running();
      boolean refreshing = refreshStatus != null && refreshStatus.running();
      if (importing || refreshing) {
         int trackX = this.listCardX() + 4;
         int trackY = this.listCardY() + 4 + 12 + 1;
         int trackW = Math.max(1, this.listCardWidth() - 8);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(trackX, trackY, trackW, 2), outlineColor(1429414952));
         if (refreshing) {
            int total = Math.max(1, refreshStatus.totalCount());
            int fillW = Math.max(1, Math.min(trackW, Math.round((float)(trackW * Math.max(0, refreshStatus.checkedCount())) / total)));
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

   private int controlHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int rowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int rowPitch() {
      return this.rowHeight() + 2;
   }

   private int rowButtonHeight() {
      return this.rowHeight() - 2;
   }

   private int centeredTextY(int top, int height) {
      return autismclient.gui.f.UiHelper.init(this.font).centerTextY(autismclient.gui.f.NetHelper.init(0, top, 1, height));
   }

   private int textWidth(String text) {
      return k.init(this.font, text == null ? "" : text, THEME.init_Identifier_l(l.read), 0);
   }

   private int panelX() {
      return autismclient.gui.ui.layout.PanelLayoutMath.centerWithMargin(this.virtualWidth(), this.panelWidth(), 6);
   }

   private int panelWidth() {
      return autismclient.gui.ui.layout.PanelLayoutMath.clampSizeToMargin(this.virtualWidth(), 6, 530);
   }

   private int inputX() {
      return this.panelX() + 4;
   }

   private int inputWidth() {
      return Math.max(1, this.panelWidth() - 8);
   }

   private int panelY() {
      return 6;
   }

   private int searchY() {
      return this.panelY() + 4;
   }

   private int firstInputY() {
      return this.searchY() + this.controlHeight() + 2;
   }

   private int secondInputY() {
      return this.firstInputY() + this.controlHeight() + 2;
   }

   private int actionRowY() {
      return this.secondInputY() + this.controlHeight() + 2;
   }

   private int filterRowY() {
      return this.actionRowY() + this.controlHeight() + 2;
   }

   private int headerCardHeight() {
      return this.filterRowY() + this.controlHeight() + 4 - this.panelY();
   }

   private int titleX() {
      return this.inputX() + this.buttonWidth("Back") + 4;
   }

   private int searchX() {
      return this.titleX() + this.textWidth("Proxies") + 8;
   }

   private int searchWidth() {
      return Math.max(40, this.inputX() + this.inputWidth() - this.searchX());
   }

   private int listCardY() {
      return this.panelY() + this.headerCardHeight() + 4;
   }

   private int listCardX() {
      return this.panelX();
   }

   private int listCardWidth() {
      return Math.max(1, this.panelWidth());
   }

   private int listCardHeight() {
      int header = this.listTop() - this.listCardY();
      int cap = Math.max(3, this.virtualHeight() * 65 / 100 / this.rowPitch());
      int shown = Math.max(3, Math.min(cap, this.orderedProxies().size()));
      int content = header + shown * this.rowPitch() - 2 + 4;
      int available = Math.max(1, this.virtualHeight() - 6 - this.listCardY());
      return Math.min(available, content);
   }

   private int listTop() {
      return this.listCardY() + 4 + 12 + 4;
   }

   private int listBottom() {
      return this.listCardY() + this.listCardHeight() - 4;
   }

   private int listViewportHeight() {
      return Math.max(this.rowHeight(), Math.max(1, this.listBottom() - this.listTop()));
   }

   private int maxScroll(int rows) {
      return Math.max(0, rows * this.rowPitch() - 2 - this.listViewportHeight());
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics(int rows) {
      int contentPixels = Math.max(0, rows * this.rowPitch() - 2);
      int trackY = this.listTop();
      int trackHeight = this.listViewportHeight();
      int maxScroll = this.maxScroll(rows);
      this.update_I = clampScroll(this.update_I, maxScroll);
      this.scroll.jumpTo(this.update_I, maxScroll);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(contentPixels, this.listViewportHeight(), this.scrollbarX(), trackY, 6, trackHeight, this.update_I);
   }

   private int scrollbarX() {
      return this.listCardX() + this.listCardWidth() - 4 - 6;
   }

   private int rowLeft() {
      return this.listCardX() + 4;
   }

   private int rowRight() {
      return this.scrollbarX() - 2;
   }

   private int rowWidth() {
      return Math.max(1, this.rowRight() - this.rowLeft());
   }

   private boolean isCompact() {
      return this.panelWidth() < 300 || this.virtualHeight() < this.listTop() + this.rowPitch() * 2;
   }

   private void setInputsVisible(boolean visible) {
      if (this.nameField != null) {
         this.nameField.visible = visible;
      }

      if (this.addressField != null) {
         this.addressField.visible = visible;
      }

      if (this.portField != null) {
         this.portField.visible = visible;
      }

      if (this.usernameField != null) {
         this.usernameField.visible = visible;
      }

      if (this.passwordField != null) {
         this.passwordField.visible = visible;
      }

      if (this.searchField != null) {
         this.searchField.visible = visible;
      }
   }

   private int[] fitLabelWidths(int availableWidth, int gap, String... labels) {
      int[] widths = new int[labels.length];
      int total = Math.max(0, labels.length - 1) * gap;

      for (int i = 0; i < labels.length; i++) {
         widths[i] = this.buttonWidth(labels[i]);
         total += widths[i];
      }

      return total <= availableWidth ? widths : this.fitWidths(availableWidth, gap, widths);
   }

   private int[] fitWidths(int availableWidth, int gap, int... preferredWidths) {
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

   private void unfocusFields() {
      if (this.nameField != null) {
         this.nameField.setFocused(false);
      }

      if (this.addressField != null) {
         this.addressField.setFocused(false);
      }

      if (this.portField != null) {
         this.portField.setFocused(false);
      }

      if (this.usernameField != null) {
         this.usernameField.setFocused(false);
      }

      if (this.passwordField != null) {
         this.passwordField.setFocused(false);
      }

      if (this.searchField != null) {
         this.searchField.setFocused(false);
      }

      this.setFocused(null);
   }

   private int buttonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(k.init(this.font, label, THEME.init_Identifier_l(l.read), 0));
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean center) {
      this.init_V_GuiGraphicsExtract(graphics, text, x, y, color, center, 2147483647);
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, String text, int x, int y, int color, boolean center, int maxWidth) {
      Font renderer = this.font;
      Identifier font = THEME.init_Identifier_l(l.read);
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

   private static int clampScroll(int value, int maxScroll) {
      return Math.max(0, Math.min(maxScroll, value));
   }

   private static String normalize(String value) {
      return trim(value).toLowerCase(Locale.ROOT);
   }

   private static int outlineColor(int argb) {
      return ThemePaletteManager.recolor(argb, ThemePaletteManager.ColorRole.OUTLINE);
   }

   private static int successColor(int argb) {
      return ThemePaletteManager.recolor(argb, ThemePaletteManager.ColorRole.SUCCESS);
   }

   private enum Inner_DataUtil {
      ALL,
      ALIVE,
      DEAD,
      UNCHECKED,
      ENABLED;
   }

   private record Inner_StreamUtil(
      bW proxy,
      int y,
      boolean activeSection,
      autismclient.gui.ui.widgets.OverlayActionButton toggleButton,
      autismclient.gui.ui.widgets.OverlayActionButton load,
      autismclient.gui.ui.widgets.OverlayActionButton read
   ) {
   }
}
