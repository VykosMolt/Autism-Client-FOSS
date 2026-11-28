package autismclient.gui.profiles;

import autismclient.gui.sdf.MsdfTextRenderer;
import autismclient.gui.ui.UiClipStack;
import autismclient.gui.ui.UiColorScheme;
import autismclient.gui.f.NetHelper;
import autismclient.gui.f.PacketUtil;
import autismclient.gui.ui.UiDensityMetrics;
import autismclient.gui.ui.widgets.TextInputWidget;
import autismclient.gui.f.b.BufferHelper;
import autismclient.gui.f.b.CachePool;
import autismclient.gui.f.b.CharEncoder;
import autismclient.gui.ui.widgets.ScrollbarMetricsHelper;
import autismclient.gui.ui.widgets.IconButtonRenderer;
import autismclient.gui.ui.widgets.ToggleSwitchRenderer;
import autismclient.util.AutismProfile;
import autismclient.util.AutismProfileManager;
import autismclient.util.bi;
import autismclient.util.GuiIconAtlas;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;

public final class ProfilesLibraryView {
   private static final int init_I = 4;
   private static final int load_I = 4;
   private final Font font;
   private final CharEncoder theme = new CharEncoder();
   private final AutismProfileManager profileManager = AutismProfileManager.getInstance();
   private final ProfileServerIconCache serverIconCache = new ProfileServerIconCache();
   private final List<ProfilesLibraryView.Inner_DataUtil> hitRegions = new ArrayList<>();
   private final List<TextInputWidget> inputs = new ArrayList<>();
   private final TextInputWidget searchInput = createInput("Find a profile or server", 80);
   private final TextInputWidget nameInput = createInput("Profile name", 48);
   private final TextInputWidget serverInput = createInput("Server address or *.domain", 80);
   private String selectedProfileId;
   private ProfilesLibraryView.Inner_StreamUtil mode = ProfilesLibraryView.Inner_StreamUtil.NONE;
   private AutismProfile pendingSwitch;
   private boolean autoSave = true;
   private boolean ownMacroLibrary = true;
   private boolean ownThemeColor = true;
   private UiColorScheme colors;
   private NetHelper area;
   private NetHelper listArea;
   private NetHelper clipArea;
   private boolean interactive;
   private int savedScroll;
   private int scroll;
   private ScrollbarMetricsHelper.Metrics scrollbarMetrics;
   private boolean draggingScrollbar;
   private int dragGrabOffset;
   private int sync;
   private int flush;
   private float partialTick;

   public ProfilesLibraryView(Font font) {
      this.font = font;
      this.serverInput.setOnSubmit(ignored -> this.addServerPattern());
      this.nameInput.setOnSubmit(ignored -> this.submitProfileName());
      this.searchInput.setOnChange(ignored -> this.scroll = 0);
   }

   private static TextInputWidget createInput(String placeholder, int maxLength) {
      return new TextInputWidget().setPlaceholder(placeholder).setMaxLength(maxLength);
   }

   public int preferredWidth() {
      return Math.round(320.0F * Math.max(1.0F, MsdfTextRenderer.fontScale()));
   }

   public int init(int width) {
      AutismProfile profile = this.profileManager.byId(this.selectedProfileId);
      int innerW = Math.max(0, width - 8);
      int body = profile == null
         ? Math.min(4 * flush_I(), gridHeight(this.profileManager.count(), columnsFor(innerW)))
         : Math.min(cardHeight(profile, innerW), 10 * sync_I());
      int modal = this.init_Z() ? (this.pendingSwitch != null ? read_I() : this.load_I()) : 0;
      return Math.max(modal + 8, 8 + inputHeight() + 4 + Math.max(2 * flush_I(), body));
   }

   public void render(autismclient.gui.f.SyncManager ctx, NetHelper area, float delta) {
      this.colors = ctx.init_TableIndex().colors();
      this.area = area;
      this.partialTick = delta;
      this.sync = ctx.read();
      this.flush = ctx.write();
      this.hitRegions.clear();
      this.inputs.clear();
      this.scrollbarMetrics = null;
      this.clipArea = this.area;
      boolean modal = this.init_Z();
      this.interactive = !modal;
      autismclient.gui.f.SyncManager page = modal ? ctx.load_SyncManager() : ctx;
      AutismProfile profile = this.profileManager.byId(this.selectedProfileId);
      if (profile == null && this.selectedProfileId != null) {
         this.closeProfileDetail();
      }

      NetHelper inner = this.area.init(4);
      NetHelper toolbar = NetHelper.init(inner.read(), inner.write(), inner.update(), inputHeight());
      this.renderHeaderBar(page, toolbar, profile);
      this.listArea = inner.load(0, inputHeight() + 4, 0, 0);
      if (this.listArea.sync() > 0 && this.listArea.update() > 0) {
         this.renderProfileGrid(page, profile);
      }

      if (modal) {
         this.interactive = true;
         this.clipArea = this.area;
         PacketUtil.init(ctx.init_GuiGraphicsExtract(), this.area, -1728053248);
         if (this.pendingSwitch != null) {
            this.renderUnsavedPrompt(ctx);
         } else {
            this.init(ctx);
         }
      }
   }

   private void renderHeaderBar(autismclient.gui.f.SyncManager ctx, NetHelper bar, AutismProfile profile) {
      AutismProfile active = this.profileManager.active();
      boolean dirty = active != null && !active.autoSave && this.profileManager.hasUnsavedChanges();
      int right = bar.init();
      if (dirty) {
         int saveW = buttonWidthFor(ctx, "Save active profile");
         right -= saveW;
         this.init(
            ctx,
            NetHelper.init(right, bar.write(), saveW, bar.sync()),
            "Save active profile",
            null,
            CachePool.Inner_DataUtil.write,
            () -> this.profileManager.saveSnapshot(active)
         );
         right -= 4;
      }

      if (profile != null) {
         int backW = buttonWidthFor(ctx, "Library");
         this.init(ctx, NetHelper.init(bar.read(), bar.write(), backW, bar.sync()), "Library", null, CachePool.Inner_DataUtil.load, this::closeProfileDetail);
         int x = bar.read() + backW + 4;
         ctx.init_PoolEntry()
            .drawEllipsized(
               ctx.init_GuiGraphicsExtract(),
               profile.displayName,
               x,
               ctx.init_PoolEntry().centerTextY(bar),
               right - x,
               profile.id.equals(this.profileManager.activeId()) ? this.colors.success : this.colors.textPrimary
            );
      } else {
         int newW = buttonWidthFor(ctx, "New profile") + 16;
         right -= newW;
         this.init(
            ctx,
            NetHelper.init(right, bar.write(), newW, bar.sync()),
            "New profile",
            GuiIconAtlas.PLUS,
            CachePool.Inner_DataUtil.read,
            () -> this.init(ProfilesLibraryView.Inner_StreamUtil.CREATE)
         );
         this.renderInput(ctx, this.searchInput, NetHelper.init(bar.read(), bar.write(), right - 4 - bar.read(), bar.sync()));
      }
   }

   private void renderProfileGrid(autismclient.gui.f.SyncManager ctx, AutismProfile profile) {
      List<AutismProfile> entries = profile == null ? this.filteredProfiles() : List.of();
      int contentH = profile == null
         ? gridHeight(entries.size(), columnsFor(this.listArea.update()))
         : cardHeight(profile, this.listArea.update());
      boolean reserveScrollbar = contentH > this.listArea.sync();
      int w = this.listArea.update() - (reserveScrollbar ? 8 : 0);
      int cols = columnsFor(w);
      contentH = profile == null ? gridHeight(entries.size(), cols) : cardHeight(profile, w);
      int maxScroll = Math.max(0, contentH - this.listArea.sync());
      this.scroll = Math.min(this.scroll, maxScroll);
      int top = this.listArea.write() - this.scroll;
      this.clipArea = this.listArea;
      autismclient.gui.f.SyncManager content = this.listArea.init_Z(ctx.read(), ctx.write()) ? ctx : ctx.load_SyncManager();
      UiClipStack.getInstance().push(ctx.init_GuiGraphicsExtract(), this.listArea);

      try {
         if (profile == null) {
            this.init(content, entries, this.listArea.read(), top, w, cols);
         } else {
            this.renderProfileCard(content, profile, this.listArea.read(), top, w);
         }
      } finally {
         UiClipStack.getInstance().pop(ctx.init_GuiGraphicsExtract());
      }

      if (maxScroll > 0) {
         int gutter = 6;
         ScrollbarMetricsHelper.Metrics metrics = ScrollbarMetricsHelper.compute(
            contentH,
            this.listArea.sync(),
            this.listArea.init() - gutter,
            this.listArea.write(),
            gutter,
            this.listArea.sync(),
            this.scroll
         );
         ScrollbarMetricsHelper.render(ctx.init_GuiGraphicsExtract(), metrics, metrics.containsTrack(ctx.read(), ctx.write()), this.draggingScrollbar);
         if (this.interactive) {
            this.scrollbarMetrics = metrics;
         }
      }
   }

   private List<AutismProfile> filteredProfiles() {
      String query = this.searchInput.getValue().strip().toLowerCase(Locale.ROOT);
      List<AutismProfile> profiles = this.profileManager.profiles();
      return query.isEmpty()
         ? profiles
         : profiles.stream()
            .filter(
               p -> p.displayName.toLowerCase(Locale.ROOT).contains(query)
                  || p.serverPatterns.stream().anyMatch(pattern -> this.serverPatternMatches(pattern, query))
            )
            .toList();
   }

   private boolean serverPatternMatches(String pattern, String query) {
      if (pattern == null) {
         return false;
      }

      if (pattern.toLowerCase(Locale.ROOT).contains(query)) {
         return true;
      }

      ServerData server = this.serverIconCache.serverFor(pattern);
      return server != null
         && (server.name != null && server.name.toLowerCase(Locale.ROOT).contains(query) || server.ip.toLowerCase(Locale.ROOT).contains(query));
   }

   private void init(autismclient.gui.f.SyncManager ctx, List<AutismProfile> profiles, int x, int y, int w, int cols) {
      if (profiles.isEmpty()) {
         ctx.init_PoolEntry().drawCentered(ctx.init_GuiGraphicsExtract(), "No matching profiles", NetHelper.init(x, y + 4, w, sync_I()), this.colors.textSecondary);
      } else {
         String activeId = this.profileManager.activeId();
         int cardW = (w - (cols - 1) * 4) / cols;
         int cardH = flush_I() - 4;

         for (int i = 0; i < profiles.size(); i++) {
            NetHelper card = NetHelper.init(x + i % cols * (cardW + 4), y + i / cols * flush_I(), cardW, cardH);
            if (card.load() > this.listArea.write() && card.write() < this.listArea.load()) {
               AutismProfile profile = profiles.get(i);
               boolean active = profile.id.equals(activeId);
               BufferHelper.init(ctx, card, "profile:" + profile.id, active, this.isHovered(ctx, card));
               List<String> patterns = profile.serverPatterns;
               String pattern = patterns.isEmpty() ? null : patterns.getFirst();
               ServerData server = this.serverIconCache.serverFor(pattern);
               if (!this.serverIconCache.hasIcon(server)) {
                  for (int rule = 1; rule < patterns.size(); rule++) {
                     ServerData candidate = this.serverIconCache.serverFor(patterns.get(rule));
                     if (this.serverIconCache.hasIcon(candidate)) {
                        pattern = patterns.get(rule);
                        server = candidate;
                        break;
                     }
                  }
               }

               int iconSize = 18;
               this.serverIconCache
                  .init(
                     ctx,
                     server,
                     pattern == null ? autismclient.gui.f.a.DataUtil.add : autismclient.gui.f.a.DataUtil.push,
                     card.read() + 4,
                     card.write() + (cardH - iconSize) / 2,
                     iconSize
                  );
               int tx = card.read() + iconSize + 8;
               String activeLabel = "Active";
               int badgeW = active ? ctx.init_PoolEntry().measureWidth(activeLabel) + 4 : 0;
               int textY = card.write() + (cardH - 2 * write_I()) / 2;
               if (active) {
                  ctx.init_PoolEntry()
                     .draw(ctx.init_GuiGraphicsExtract(), activeLabel, card.init() - badgeW, textY, this.colors.success);
               }

               ctx.init_PoolEntry().drawEllipsized(ctx.init_GuiGraphicsExtract(), profile.displayName, tx, textY, card.init() - 4 - badgeW - tx, this.colors.textPrimary);
               String subtitle = describeServerPattern(pattern, server);
               if (patterns.size() > 1) {
                  subtitle = subtitle + "  +" + (patterns.size() - 1);
               }

               ctx.init_PoolEntry().drawEllipsized(ctx.init_GuiGraphicsExtract(), subtitle, tx, textY + write_I(), card.init() - 4 - tx, this.colors.textSecondary);
               this.addHitRegion(card, () -> this.init(profile));
            }
         }
      }
   }

   private void renderProfileCard(autismclient.gui.f.SyncManager ctx, AutismProfile p, int x, int y, int w) {
      boolean active = p.id.equals(this.profileManager.activeId());
      boolean canDelete = !AutismProfileManager.isDefault(p);
      int slots = read_I_I(w);
      int cellW = (w - (slots - 1) * 2) / slots;
      String[] labels = new String[]{active ? "Active" : "Load", "Rename", "Duplicate", "Delete"};
      CachePool.Inner_DataUtil[] tones = new CachePool.Inner_DataUtil[]{
         CachePool.Inner_DataUtil.write, CachePool.Inner_DataUtil.load, CachePool.Inner_DataUtil.load, CachePool.Inner_DataUtil.update
      };
      Runnable[] actions = new Runnable[]{
         active ? null : () -> this.switchToProfile(p),
         () -> this.init(ProfilesLibraryView.Inner_StreamUtil.RENAME),
         () -> this.init(ProfilesLibraryView.Inner_StreamUtil.DUPLICATE),
         canDelete ? () -> this.deleteProfile(p) : null
      };

      for (int i = 0; i < labels.length; i++) {
         NetHelper cell = NetHelper.init(x + i % slots * (cellW + 2), y + i / slots * (inputHeight() + 2), cellW, inputHeight());
         this.init(ctx, cell, labels[i], null, tones[i], actions[i]);
      }

      int cy = y + write(w) + 4;
      cy = this.init(ctx, x, cy, w, "Auto-save changes", p.autoSave, () -> this.profileManager.setAutoSave(p, !p.autoSave));
      cy = this.init(ctx, x, cy, w, "Own macro library", p.ownMacroLibrary, () -> this.profileManager.setOwnMacroLibrary(p, !p.ownMacroLibrary));
      cy = this.init(ctx, x, cy, w, "Own theme color", p.ownThemeColor, () -> this.profileManager.setOwnThemeColor(p, !p.ownThemeColor));
      cy += 2;
      String serverHeading = "Auto-apply on servers";
      ctx.init_PoolEntry()
         .drawEllipsized(ctx.init_GuiGraphicsExtract(), serverHeading, x, ctx.init_PoolEntry().centerTextY(NetHelper.init(x, cy, w, sync_I())), w, this.colors.textSecondary);
      cy += sync_I() + 2;
      int useW = buttonWidthFor(ctx, "Use current");
      int addW = buttonWidthFor(ctx, "Add");
      int fieldW = w - useW - addW - 4;
      this.renderInput(ctx, this.serverInput, NetHelper.init(x, cy, fieldW, inputHeight()));
      this.init(ctx, NetHelper.init(x + fieldW + 2, cy, addW, inputHeight()), "Add", null, CachePool.Inner_DataUtil.read, this::addServerPattern);
      this.init(ctx, NetHelper.init(x + w - useW, cy, useW, inputHeight()), "Use current", null, CachePool.Inner_DataUtil.load, this::fillCurrentServer);
      cy += inputHeight() + 4;
      if (p.serverPatterns.isEmpty()) {
         this.init(ctx, "No server rules", x, cy, w, this.colors.textSecondary);
      } else {
         for (String pattern : p.serverPatterns) {
            NetHelper rule = NetHelper.init(x, cy, w, sync_I());
            cy += sync_I() + 2;
            if (rule.load() > this.listArea.write() && rule.write() < this.listArea.load()) {
               PacketUtil.load(ctx.init_GuiGraphicsExtract(), rule, 3, this.colors.cardSurface);
               ServerData server = this.serverIconCache.serverFor(pattern);
               int iconSize = sync_I() - 4;
               this.serverIconCache.init(ctx, server, autismclient.gui.f.a.DataUtil.push, rule.read() + 4, rule.write() + 2, iconSize);
               int tx = rule.read() + iconSize + 8;
               NetHelper remove = NetHelper.init(rule.init() - sync_I(), rule.write() + 1, sync_I() - 2, sync_I() - 2);
               String label = describeServerPattern(pattern, null);
               if (server != null && server.name != null && !server.name.isBlank() && !server.name.equalsIgnoreCase(label)) {
                  label = server.name + "  |  " + label;
               }

               this.init(ctx, label, tx, rule.write(), remove.read() - tx - 4, this.colors.textPrimary);
               IconButtonRenderer.init(
                  ctx.init_GuiGraphicsExtract(), this.font, remove, GuiIconAtlas.CLOSE, this.isHovered(ctx, remove), true, true
               );
               this.addHitRegion(remove, () -> this.removeServerPattern(p, pattern));
            }
         }
      }
   }

   private static String describeServerPattern(String pattern, ServerData server) {
      if (pattern == null || pattern.isBlank()) {
         return "No server rules";
      } else if (pattern.equals("*")) {
         return "All servers";
      } else {
         return server != null && server.name != null && !server.name.isBlank() && !server.name.equalsIgnoreCase(pattern) ? server.name : pattern;
      }
   }

   private void init(autismclient.gui.f.SyncManager ctx) {
      boolean create = this.mode == ProfilesLibraryView.Inner_StreamUtil.CREATE;
      NetHelper box = this.centeredModalBox(220, this.load_I());
      this.drawModalSurface(ctx, box);
      String var10000;
      switch (this.mode) {
         case NONE:
            var10000 = "";
            break;
         case CREATE:
            var10000 = "New profile";
            break;
         case RENAME:
            var10000 = "Rename profile";
            break;
         case DUPLICATE:
            var10000 = "Duplicate profile";
            break;
         default:
            throw new MatchException(null, null);
      }

      String title = var10000;
      NetHelper inner = box.init(6);
      int x = inner.read();
      int w = inner.update();
      this.init(ctx, title, x, inner.write(), w, this.colors.textPrimary);
      int cy = inner.write() + sync_I() + 4;
      this.renderInput(ctx, this.nameInput, NetHelper.init(x, cy, w, inputHeight()));
      cy += inputHeight() + 4;
      if (create) {
         cy = this.init(ctx, x, cy, w, "Auto-save changes", this.autoSave, () -> this.autoSave = !this.autoSave);
         cy = this.init(ctx, x, cy, w, "Own macro library", this.ownMacroLibrary, () -> this.ownMacroLibrary = !this.ownMacroLibrary);
         cy = this.init(ctx, x, cy, w, "Own theme color", this.ownThemeColor, () -> this.ownThemeColor = !this.ownThemeColor);
         cy += 2;
      }

      int okW = buttonWidthFor(ctx, create ? "Create" : "Save");
      int cancelW = buttonWidthFor(ctx, "Cancel");
      int start = inner.init() - okW - cancelW - 4;
      this.init(ctx, NetHelper.init(start, cy, okW, inputHeight()), create ? "Create" : "Save", null, CachePool.Inner_DataUtil.read, this::submitProfileName);
      this.init(ctx, NetHelper.init(start + okW + 4, cy, cancelW, inputHeight()), "Cancel", null, CachePool.Inner_DataUtil.load, this::cancelCreate);
   }

   private void renderUnsavedPrompt(autismclient.gui.f.SyncManager ctx) {
      NetHelper box = this.centeredModalBox(240, read_I());
      this.drawModalSurface(ctx, box);
      NetHelper inner = box.init(6);
      this.init(ctx, "Unsaved changes", inner.read(), inner.write(), inner.update(), this.colors.textPrimary);
      AutismProfile active = this.profileManager.active();
      String name = active == null ? "the active profile" : active.displayName;
      this.init(ctx, "Save " + name + " before switching?", inner.read(), inner.write() + sync_I(), inner.update(), this.colors.textSecondary);
      int cy = inner.write() + sync_I() * 2 + 4;
      int w = (inner.update() - 8) / 3;
      this.init(ctx, NetHelper.init(inner.read(), cy, w, inputHeight()), "Save", null, CachePool.Inner_DataUtil.write, () -> this.init(true));
      this.init(ctx, NetHelper.init(inner.read() + w + 4, cy, w, inputHeight()), "Discard", null, CachePool.Inner_DataUtil.update, () -> this.init(false));
      this.init(
         ctx,
         NetHelper.init(inner.read() + (w + 4) * 2, cy, w, inputHeight()),
         "Cancel",
         null,
         CachePool.Inner_DataUtil.load,
         () -> this.pendingSwitch = null
      );
   }

   private NetHelper centeredModalBox(int idealWidth, int height) {
      int w = Math.min(this.area.update() - 8, Math.round(idealWidth * Math.max(1.0F, MsdfTextRenderer.fontScale())));
      return NetHelper.init(
         this.area.read() + (this.area.update() - w) / 2,
         this.area.write() + Math.max(0, (this.area.sync() - height) / 2),
         w,
         height
      );
   }

   private void drawModalSurface(autismclient.gui.f.SyncManager ctx, NetHelper box) {
      PacketUtil.load(ctx.init_GuiGraphicsExtract(), box, 8, this.colors.popupSurface, this.colors.borderStrong);
   }

   private static int gridHeight(int count, int cols) {
      return Math.max(sync_I() * 2, (count + cols - 1) / cols * flush_I() - 4);
   }

   private static int columnsFor(int width) {
      return width >= 224 ? 2 : 1;
   }

   private static int read_I_I(int width) {
      return width >= 200 ? 4 : 2;
   }

   private static int write(int width) {
      int rows = 4 / read_I_I(width);
      return rows * (inputHeight() + 2) - 2;
   }

   private static int cardHeight(AutismProfile profile, int width) {
      return write(width) + 12 + 5 * sync_I() + 6 + inputHeight() + Math.max(0, profile.serverPatterns.size() - 1) * (sync_I() + 2);
   }

   private int load_I() {
      int h = 12 + sync_I() + 8 + 2 * inputHeight();
      if (this.mode == ProfilesLibraryView.Inner_StreamUtil.CREATE) {
         h += 3 * sync_I() + 4 + 4;
      }

      return h;
   }

   private static int read_I() {
      return 12 + 2 * sync_I() + 4 + inputHeight();
   }

   private static int write_I() {
      return UiDensityMetrics.scaledHeight(12);
   }

   private static int inputHeight() {
      return UiDensityMetrics.scaledHeight(13);
   }

   private static int sync_I() {
      return UiDensityMetrics.scaledHeight(14);
   }

   private static int flush_I() {
      return 2 * write_I() + 6 + 4;
   }

   private static int buttonWidthFor(autismclient.gui.f.SyncManager ctx, String label) {
      return UiDensityMetrics.buttonWidthForText(ctx.init_PoolEntry().measureWidth(label));
   }

   private void init(AutismProfile profile) {
      if (this.selectedProfileId == null) {
         this.savedScroll = this.scroll;
      }

      this.selectedProfileId = profile.id;
      this.scroll = 0;
      this.serverInput.setValue("");
      this.blurInputs();
   }

   private void closeProfileDetail() {
      this.selectedProfileId = null;
      this.serverInput.setValue("");
      this.scroll = this.savedScroll;
      this.blurInputs();
   }

   public boolean init_Z() {
      return this.mode != ProfilesLibraryView.Inner_StreamUtil.NONE || this.pendingSwitch != null;
   }

   public void reset() {
      this.cancelCreate();
      this.pendingSwitch = null;
      this.closeProfileDetail();
      this.searchInput.setValue("");
      this.savedScroll = this.scroll = 0;
      this.draggingScrollbar = false;
      this.area = null;
      this.hitRegions.clear();
      this.inputs.clear();
      this.serverIconCache.init();
   }

   private void switchToProfile(AutismProfile target) {
      if (target != null) {
         AutismProfile cur = this.profileManager.active();
         if (cur == null || !target.id.equals(cur.id)) {
            if (cur != null && !cur.autoSave && this.profileManager.hasUnsavedChanges()) {
               this.pendingSwitch = target;
            } else {
               this.profileManager.switchToAsync(target);
               this.selectedProfileId = target.id;
            }
         }
      }
   }

   private void init(boolean save) {
      AutismProfile target = this.pendingSwitch;
      this.pendingSwitch = null;
      if (target != null) {
         if (save) {
            this.profileManager.saveSnapshot(this.profileManager.active());
         }

         this.profileManager.switchToAsync(target);
         this.selectedProfileId = target.id;
      }
   }

   private void deleteProfile(AutismProfile p) {
      if (p != null && this.profileManager.delete(p)) {
         this.closeProfileDetail();
         bi.write("Deleted profile: " + p.displayName);
      }
   }

   private void init(ProfilesLibraryView.Inner_StreamUtil mode) {
      this.blurInputs();
      this.mode = mode;
      if (mode == ProfilesLibraryView.Inner_StreamUtil.CREATE) {
         this.autoSave = true;
         this.ownMacroLibrary = true;
         this.ownThemeColor = true;
      }

      AutismProfile sel = this.profileManager.byId(this.selectedProfileId);
      this.nameInput
         .setValue(
            mode == ProfilesLibraryView.Inner_StreamUtil.RENAME && sel != null
               ? sel.displayName
               : (mode == ProfilesLibraryView.Inner_StreamUtil.DUPLICATE && sel != null ? sel.displayName + " copy" : "")
         );
      this.nameInput.setFocused(true);
   }

   private void cancelCreate() {
      this.mode = ProfilesLibraryView.Inner_StreamUtil.NONE;
      this.nameInput.setFocused(false);
      this.nameInput.setValue("");
   }

   private void submitProfileName() {
      String name = this.nameInput.getValue() == null ? "" : this.nameInput.getValue().strip();
      if (name.isEmpty()) {
         bi.read("Enter a name.");
      } else {
         String exclude = this.mode == ProfilesLibraryView.Inner_StreamUtil.RENAME ? this.selectedProfileId : null;
         if (this.profileManager.isNameTaken(name, exclude)) {
            bi.read("Name already in use.");
         } else {
            switch (this.mode) {
               case CREATE:
                  AutismProfile p = this.profileManager.create(name, this.autoSave, this.ownMacroLibrary, this.ownThemeColor);
                  if (p != null) {
                     this.init(p);
                     this.profileManager.switchToAsync(p);
                  }
                  break;
               case RENAME:
                  AutismProfile selx = this.profileManager.byId(this.selectedProfileId);
                  if (selx != null) {
                     this.profileManager.rename(selx, name);
                  }
                  break;
               case DUPLICATE:
                  AutismProfile sel = this.profileManager.byId(this.selectedProfileId);
                  AutismProfile d = this.profileManager.duplicate(sel, name);
                  if (d != null) {
                     this.init(d);
                  }
            }

            this.cancelCreate();
         }
      }
   }

   private void addServerPattern() {
      AutismProfile sel = this.profileManager.byId(this.selectedProfileId);
      if (sel != null) {
         String pat = this.serverInput.getValue() == null ? "" : this.serverInput.getValue().strip().toLowerCase(Locale.ROOT);
         if (!pat.isEmpty()) {
            List<String> next = new ArrayList<>(sel.serverPatterns);
            if (!next.contains(pat)) {
               next.add(pat);
            }

            this.profileManager.setServerPatterns(sel, next);
            this.serverInput.setValue("");
         }
      }
   }

   private void removeServerPattern(AutismProfile p, String pattern) {
      if (p != null) {
         List<String> next = new ArrayList<>(p.serverPatterns);
         next.remove(pattern);
         this.profileManager.setServerPatterns(p, next);
      }
   }

   private void fillCurrentServer() {
      Minecraft mc = Minecraft.getInstance();
      ServerData sd = mc == null ? null : mc.getCurrentServer();
      if (sd != null && sd.ip != null && !sd.ip.isBlank()) {
         this.serverInput.setValue(sd.ip.strip().toLowerCase(Locale.ROOT));
         this.serverInput.setFocused(true);
      } else {
         bi.read("Not connected to a server.");
      }
   }

   public boolean mouseClicked(int mx, int my, int button) {
      if (this.area != null && this.area.init_Z(mx, my)) {
         if (button != 0) {
            return true;
         }

         if (this.scrollbarMetrics != null && !this.init_Z() && this.scrollbarMetrics.containsTrack(mx, my)) {
            this.draggingScrollbar = true;
            this.dragGrabOffset = this.scrollbarMetrics.containsThumb(mx, my) ? my - this.scrollbarMetrics.thumbY() : this.scrollbarMetrics.thumbHeight() / 2;
            this.scroll = ScrollbarMetricsHelper.scrollOffsetForMouseY(this.scrollbarMetrics, my, this.dragGrabOffset);
            this.blurInputs();
            return true;
         }

         autismclient.gui.ui.layout.LayoutContext ctx = this.layoutContext(null, mx, my);

         for (TextInputWidget input : this.inputs) {
            if ((input != this.serverInput || this.listArea.init_Z(mx, my)) && input.mouseClicked(ctx, mx, my, button)) {
               for (TextInputWidget other : this.inputs) {
                  if (other != input) {
                     other.setFocused(false);
                  }
               }

               return true;
            }
         }

         this.blurInputs();

         for (ProfilesLibraryView.Inner_DataUtil hit : this.hitRegions) {
            if (hit.bounds().init_Z(mx, my)) {
               hit.action().run();
               return true;
            }
         }

         return true;
      } else {
         this.blurInputs();
         return this.init_Z();
      }
   }

   public boolean load(int mx, int my, int button) {
      if (this.draggingScrollbar) {
         this.draggingScrollbar = false;
         return true;
      }

      autismclient.gui.ui.layout.LayoutContext ctx = this.layoutContext(null, mx, my);

      for (TextInputWidget input : this.inputs) {
         if (input.mouseReleased(ctx, mx, my, button)) {
            return true;
         }
      }

      return false;
   }

   public boolean mouseDragged(int mx, int my, int button, double dx, double dy) {
      if (this.draggingScrollbar && this.scrollbarMetrics != null) {
         this.scroll = ScrollbarMetricsHelper.scrollOffsetForMouseY(this.scrollbarMetrics, my, this.dragGrabOffset);
         return true;
      }

      autismclient.gui.ui.layout.LayoutContext ctx = this.layoutContext(null, mx, my);

      for (TextInputWidget input : this.inputs) {
         if (input.mouseDragged(ctx, mx, my, button, (float)dx, (float)dy)) {
            return true;
         }
      }

      return false;
   }

   public boolean mouseScrolled(int mx, int my, double amount) {
      if (this.init_Z()) {
         return true;
      } else if (this.scrollbarMetrics != null && this.listArea.init_Z(mx, my)) {
         int pitch = this.selectedProfileId == null ? flush_I() : sync_I() + 2;
         this.scroll = Math.clamp(this.scroll - (int)Math.round(amount * pitch), 0, this.scrollbarMetrics.maxScroll());
         return true;
      } else {
         return false;
      }
   }

   public boolean read(int key, int scanCode, int modifiers) {
      if (key == 256) {
         if (this.init_Z()) {
            this.cancelCreate();
            this.pendingSwitch = null;
            return true;
         } else if (this.hasFocusedInput()) {
            this.blurInputs();
            return true;
         } else if (this.selectedProfileId != null) {
            this.closeProfileDetail();
            return true;
         } else {
            return false;
         }
      } else {
         if (!this.init_Z() && this.selectedProfileId == null && key == 70 && (modifiers & 10) != 0) {
            this.searchInput.setFocused(true);
            return true;
         }

         autismclient.gui.ui.layout.LayoutContext ctx = this.layoutContext(null, this.sync, this.flush);

         for (TextInputWidget input : this.inputs) {
            if (input.isFocused() && input.keyPressed(ctx, key, scanCode, modifiers)) {
               return true;
            }
         }

         return this.init_Z();
      }
   }

   public boolean charTyped(char chr, int modifiers) {
      autismclient.gui.ui.layout.LayoutContext ctx = this.layoutContext(null, this.sync, this.flush);

      for (TextInputWidget input : this.inputs) {
         if (input.isFocused() && input.charTyped(ctx, chr, modifiers)) {
            return true;
         }
      }

      return this.init_Z();
   }

   public boolean hasFocusedInput() {
      for (TextInputWidget input : this.inputs) {
         if (input.isFocused()) {
            return true;
         }
      }

      return false;
   }

   public void blurInputs() {
      this.nameInput.setFocused(false);
      this.serverInput.setFocused(false);
      this.searchInput.setFocused(false);
   }

   private autismclient.gui.ui.layout.LayoutContext layoutContext(GuiGraphicsExtractor g, int mx, int my) {
      return new autismclient.gui.ui.layout.LayoutContext(
         g, this.font, autismclient.gui.ui.layout.LayoutViewport.create(1.0F), this.theme, mx, my, this.partialTick
      );
   }

   private void renderInput(autismclient.gui.f.SyncManager ctx, TextInputWidget field, NetHelper box) {
      if (box.update() >= 10 && box.load() > this.clipArea.write() && box.write() < this.clipArea.load()) {
         field.setBounds(box.read(), box.write(), box.update(), box.sync());
         field.render(this.layoutContext(ctx.init_GuiGraphicsExtract(), ctx.read(), ctx.write()));
         if (this.interactive) {
            this.inputs.add(field);
         }
      }
   }

   private void init(autismclient.gui.f.SyncManager ctx, NetHelper box, String label, Identifier icon, CachePool.Inner_DataUtil tone, Runnable action) {
      boolean hovered = action != null && this.isHovered(ctx, box);
      if (icon == null) {
         CachePool.init(ctx, box, label, tone, hovered, false);
      } else {
         CachePool.init(ctx, box, label, icon, tone, hovered, false, 0.0F);
      }

      if (action != null) {
         this.addHitRegion(box, action);
      } else if (!"Active".equals(label)) {
         PacketUtil.load(ctx.init_GuiGraphicsExtract(), box, 3, 1711276032);
      }
   }

   private int init(autismclient.gui.f.SyncManager ctx, int x, int y, int w, String label, boolean value, Runnable action) {
      NetHelper row = NetHelper.init(x, y, w, sync_I());
      boolean hovered = this.isHovered(ctx, row);
      PacketUtil.load(ctx.init_GuiGraphicsExtract(), row, 3, hovered ? this.colors.cardSurfaceElevated : this.colors.cardSurface);
      ToggleSwitchRenderer.init(ctx, row, label, value, hovered, "profiles:" + label);
      this.addHitRegion(row, action);
      return row.load() + 2;
   }

   private void init(autismclient.gui.f.SyncManager ctx, String text, int x, int y, int w, int color) {
      ctx.init_PoolEntry().drawEllipsized(ctx.init_GuiGraphicsExtract(), text, x, ctx.init_PoolEntry().centerTextY(NetHelper.init(x, y, w, sync_I())), w, color);
   }

   private boolean isHovered(autismclient.gui.f.SyncManager ctx, NetHelper box) {
      return this.interactive && box.init_Z(ctx.read(), ctx.write()) && this.clipArea.init_Z(ctx.read(), ctx.write());
   }

   private void addHitRegion(NetHelper box, Runnable action) {
      if (this.interactive) {
         int x = Math.max(box.read(), this.clipArea.read());
         int y = Math.max(box.write(), this.clipArea.write());
         NetHelper clipped = NetHelper.init(x, y, Math.min(box.init(), this.clipArea.init()) - x, Math.min(box.load(), this.clipArea.load()) - y);
         if (clipped.update() > 0 && clipped.sync() > 0) {
            this.hitRegions.add(new ProfilesLibraryView.Inner_DataUtil(clipped, action));
         }
      }
   }

   private record Inner_DataUtil(NetHelper bounds, Runnable action) {
   }

   private enum Inner_StreamUtil {
      NONE,
      CREATE,
      RENAME,
      DUPLICATE;
   }
}
