package autismclient.gui.c;

import autismclient.gui.e.SyncManager;
import autismclient.gui.f.HashUtil;
import autismclient.gui.f.JsonUtil;
import autismclient.gui.f.NetHelper;
import autismclient.gui.f.PacketUtil;
import autismclient.gui.f.RenderState;
import autismclient.gui.f.b.BitSetUtil;
import autismclient.gui.f.b.BufferHelper;
import autismclient.gui.f.b.CachePool;
import autismclient.gui.f.b.CharEncoder;
import autismclient.gui.f.b.PoolEntry;
import autismclient.gui.f.b.RingBuffer;
import autismclient.gui.f.b.g;
import autismclient.util.AutismProfile;
import autismclient.util.AutismProfileManager;
import autismclient.util.bi;
import autismclient.util.cz;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;

public final class StreamUtil {
   private static final int init_I = 4;
   private static final int load_I = 4;
   private final Font init_Font;
   private final CharEncoder init_CharEncoder = new CharEncoder();
   private final AutismProfileManager init_bV = AutismProfileManager.init_bV();
   private final DataUtil init_DataUtil = new DataUtil();
   private final List<StreamUtil.Inner_DataUtil> init_List = new ArrayList<>();
   private final List<BitSetUtil> load_List = new ArrayList<>();
   private final BitSetUtil init_BitSetUtil = init_BitSetUtil_Str_I("Find a profile or server", 80);
   private final BitSetUtil load_BitSetUtil = init_BitSetUtil_Str_I("Profile name", 48);
   private final BitSetUtil read_BitSetUtil = init_BitSetUtil_Str_I("Server address or *.domain", 80);
   private String init_Str;
   private StreamUtil.Inner_StreamUtil init_StreamUtil_StreamU = StreamUtil.Inner_StreamUtil.NONE;
   private AutismProfile init_AutismProfile;
   private boolean init_Z = true;
   private boolean load_Z = true;
   private boolean read_Z = true;
   private JsonUtil init_JsonUtil;
   private NetHelper init_NetHelper;
   private NetHelper load_NetHelper;
   private NetHelper read_NetHelper;
   private boolean write_Z;
   private int read_I;
   private int write_I;
   private PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil;
   private boolean update_Z;
   private int update_I;
   private int sync;
   private int flush;
   private float init_F;

   public StreamUtil(Font font) {
      this.init_Font = font;
      this.read_BitSetUtil.load_BitSetUtil_Consumer(ignored -> this.sync_V());
      this.load_BitSetUtil.load_BitSetUtil_Consumer(ignored -> this.update_V());
      this.init_BitSetUtil.init_BitSetUtil_Consumer(ignored -> this.write_I = 0);
   }

   private static BitSetUtil init_BitSetUtil_Str_I(String placeholder, int maxLength) {
      return new BitSetUtil().load_BitSetUtil_Str(placeholder).init_BitSetUtil_I(maxLength);
   }

   public int init_I() {
      return Math.round(320.0F * Math.max(1.0F, SyncManager.init_F()));
   }

   public int init(int width) {
      AutismProfile profile = this.init_bV.init_AutismProfile_Str(this.init_Str);
      int innerW = Math.max(0, width - 8);
      int body = profile == null
         ? Math.min(4 * flush_I(), init_I_I_I(this.init_bV.init_I(), load_I_I(innerW)))
         : Math.min(init_I_AutismProfile_I(profile, innerW), 10 * sync_I());
      int modal = this.init_Z() ? (this.init_AutismProfile != null ? read_I() : this.load_I()) : 0;
      return Math.max(modal + 8, 8 + update_I() + 4 + Math.max(2 * flush_I(), body));
   }

   public void init_V_SyncManager(autismclient.gui.f.SyncManager ctx, NetHelper area, float delta) {
      this.init_JsonUtil = ctx.init_TableIndex().init_JsonUtil();
      this.init_NetHelper = area;
      this.init_F = delta;
      this.sync = ctx.read();
      this.flush = ctx.write();
      this.init_List.clear();
      this.load_List.clear();
      this.init_PoolEntry_DataUtil = null;
      this.read_NetHelper = this.init_NetHelper;
      boolean modal = this.init_Z();
      this.write_Z = !modal;
      autismclient.gui.f.SyncManager page = modal ? ctx.load_SyncManager() : ctx;
      AutismProfile profile = this.init_bV.init_AutismProfile_Str(this.init_Str);
      if (profile == null && this.init_Str != null) {
         this.read_V();
      }

      NetHelper inner = this.init_NetHelper.init(4);
      NetHelper toolbar = NetHelper.init(inner.read(), inner.write(), inner.update(), update_I());
      this.init_V_SyncManager_2(page, toolbar, profile);
      this.load_NetHelper = inner.load(0, update_I() + 4, 0, 0);
      if (this.load_NetHelper.sync() > 0 && this.load_NetHelper.update() > 0) {
         this.init_V_SyncManager_AutismProfile(page, profile);
      }

      if (modal) {
         this.write_Z = true;
         this.read_NetHelper = this.init_NetHelper;
         PacketUtil.init(ctx.init_GuiGraphicsExtract(), this.init_NetHelper, -1728053248);
         if (this.init_AutismProfile != null) {
            this.load_V_SyncManager(ctx);
         } else {
            this.init(ctx);
         }
      }
   }

   private void init_V_SyncManager_2(autismclient.gui.f.SyncManager ctx, NetHelper bar, AutismProfile profile) {
      AutismProfile active = this.init_bV.init_AutismProfile();
      boolean dirty = active != null && !active.autoSave && this.init_bV.init_Z();
      int right = bar.init();
      if (dirty) {
         int saveW = init_I_SyncManager_Str(ctx, "Save active profile");
         right -= saveW;
         this.init(
            ctx,
            NetHelper.init(right, bar.write(), saveW, bar.sync()),
            "Save active profile",
            null,
            CachePool.Inner_DataUtil.write,
            () -> this.init_bV.init_V_AutismProfile(active)
         );
         right -= 4;
      }

      if (profile != null) {
         int backW = init_I_SyncManager_Str(ctx, "Library");
         this.init(ctx, NetHelper.init(bar.read(), bar.write(), backW, bar.sync()), "Library", null, CachePool.Inner_DataUtil.load, this::read_V);
         int x = bar.read() + backW + 4;
         ctx.init_PoolEntry()
            .read(
               ctx.init_GuiGraphicsExtract(),
               profile.displayName,
               x,
               ctx.init_PoolEntry().init_I(bar),
               right - x,
               profile.id.equals(this.init_bV.init_Str()) ? this.init_JsonUtil.stop : this.init_JsonUtil.prev
            );
      } else {
         int newW = init_I_SyncManager_Str(ctx, "New profile") + 16;
         right -= newW;
         this.init(
            ctx,
            NetHelper.init(right, bar.write(), newW, bar.sync()),
            "New profile",
            cz.pop,
            CachePool.Inner_DataUtil.read,
            () -> this.init(StreamUtil.Inner_StreamUtil.CREATE)
         );
         this.init_V_SyncManager_3(ctx, this.init_BitSetUtil, NetHelper.init(bar.read(), bar.write(), right - 4 - bar.read(), bar.sync()));
      }
   }

   private void init_V_SyncManager_AutismProfile(autismclient.gui.f.SyncManager ctx, AutismProfile profile) {
      List<AutismProfile> entries = profile == null ? this.init_List() : List.of();
      int contentH = profile == null
         ? init_I_I_I(entries.size(), load_I_I(this.load_NetHelper.update()))
         : init_I_AutismProfile_I(profile, this.load_NetHelper.update());
      boolean reserveScrollbar = contentH > this.load_NetHelper.sync();
      int w = this.load_NetHelper.update() - (reserveScrollbar ? 8 : 0);
      int cols = load_I_I(w);
      contentH = profile == null ? init_I_I_I(entries.size(), cols) : init_I_AutismProfile_I(profile, w);
      int maxScroll = Math.max(0, contentH - this.load_NetHelper.sync());
      this.write_I = Math.min(this.write_I, maxScroll);
      int top = this.load_NetHelper.write() - this.write_I;
      this.read_NetHelper = this.load_NetHelper;
      autismclient.gui.f.SyncManager content = this.load_NetHelper.init_Z(ctx.read(), ctx.write()) ? ctx : ctx.load_SyncManager();
      HashUtil.init_HashUtil().init(ctx.init_GuiGraphicsExtract(), this.load_NetHelper);

      try {
         if (profile == null) {
            this.init(content, entries, this.load_NetHelper.read(), top, w, cols);
         } else {
            this.init_V_SyncManager(content, profile, this.load_NetHelper.read(), top, w);
         }
      } finally {
         HashUtil.init_HashUtil().init(ctx.init_GuiGraphicsExtract());
      }

      if (maxScroll > 0) {
         int gutter = 6;
         PoolEntry.Inner_DataUtil metrics = PoolEntry.init_PoolEntry_DataUtil_I_2(
            contentH,
            this.load_NetHelper.sync(),
            this.load_NetHelper.init() - gutter,
            this.load_NetHelper.write(),
            gutter,
            this.load_NetHelper.sync(),
            this.write_I
         );
         PoolEntry.init(ctx.init_GuiGraphicsExtract(), metrics, metrics.init(ctx.read(), ctx.write()), this.update_Z);
         if (this.write_Z) {
            this.init_PoolEntry_DataUtil = metrics;
         }
      }
   }

   private List<AutismProfile> init_List() {
      String query = this.init_BitSetUtil.init_Str().strip().toLowerCase(Locale.ROOT);
      List<AutismProfile> profiles = this.init_bV.init_List();
      return query.isEmpty()
         ? profiles
         : profiles.stream()
            .filter(
               p -> p.displayName.toLowerCase(Locale.ROOT).contains(query)
                  || p.serverPatterns.stream().anyMatch(pattern -> this.init_Z_Str_Str(pattern, query))
            )
            .toList();
   }

   private boolean init_Z_Str_Str(String pattern, String query) {
      if (pattern == null) {
         return false;
      }

      if (pattern.toLowerCase(Locale.ROOT).contains(query)) {
         return true;
      }

      ServerData server = this.init_DataUtil.init_ServerData_Str(pattern);
      return server != null
         && (server.name != null && server.name.toLowerCase(Locale.ROOT).contains(query) || server.ip.toLowerCase(Locale.ROOT).contains(query));
   }

   private void init(autismclient.gui.f.SyncManager ctx, List<AutismProfile> profiles, int x, int y, int w, int cols) {
      if (profiles.isEmpty()) {
         ctx.init_PoolEntry().load(ctx.init_GuiGraphicsExtract(), "No matching profiles", NetHelper.init(x, y + 4, w, sync_I()), this.init_JsonUtil.valid);
      } else {
         String activeId = this.init_bV.init_Str();
         int cardW = (w - (cols - 1) * 4) / cols;
         int cardH = flush_I() - 4;

         for (int i = 0; i < profiles.size(); i++) {
            NetHelper card = NetHelper.init(x + i % cols * (cardW + 4), y + i / cols * flush_I(), cardW, cardH);
            if (card.load() > this.load_NetHelper.write() && card.write() < this.load_NetHelper.load()) {
               AutismProfile profile = profiles.get(i);
               boolean active = profile.id.equals(activeId);
               BufferHelper.init(ctx, card, "profile:" + profile.id, active, this.init_Z_SyncManager_NetHelper(ctx, card));
               List<String> patterns = profile.serverPatterns;
               String pattern = patterns.isEmpty() ? null : patterns.getFirst();
               ServerData server = this.init_DataUtil.init_ServerData_Str(pattern);
               if (!this.init_DataUtil.init_Z_ServerData(server)) {
                  for (int rule = 1; rule < patterns.size(); rule++) {
                     ServerData candidate = this.init_DataUtil.init_ServerData_Str(patterns.get(rule));
                     if (this.init_DataUtil.init_Z_ServerData(candidate)) {
                        pattern = patterns.get(rule);
                        server = candidate;
                        break;
                     }
                  }
               }

               int iconSize = 18;
               this.init_DataUtil
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
               int badgeW = active ? ctx.init_PoolEntry().init(activeLabel) + 4 : 0;
               int textY = card.write() + (cardH - 2 * write_I()) / 2;
               if (active) {
                  ctx.init_PoolEntry()
                     .init_V_GuiGraphicsExtract_2(ctx.init_GuiGraphicsExtract(), activeLabel, card.init() - badgeW, textY, this.init_JsonUtil.stop);
               }

               ctx.init_PoolEntry().read(ctx.init_GuiGraphicsExtract(), profile.displayName, tx, textY, card.init() - 4 - badgeW - tx, this.init_JsonUtil.prev);
               String subtitle = init_Str_Str_ServerData(pattern, server);
               if (patterns.size() > 1) {
                  subtitle = subtitle + "  +" + (patterns.size() - 1);
               }

               ctx.init_PoolEntry().read(ctx.init_GuiGraphicsExtract(), subtitle, tx, textY + write_I(), card.init() - 4 - tx, this.init_JsonUtil.valid);
               this.init_V_NetHelper_Runnable(card, () -> this.init(profile));
            }
         }
      }
   }

   private void init_V_SyncManager(autismclient.gui.f.SyncManager ctx, AutismProfile p, int x, int y, int w) {
      boolean active = p.id.equals(this.init_bV.init_Str());
      boolean canDelete = !AutismProfileManager.init_Z_AutismProfile(p);
      int slots = read_I_I(w);
      int cellW = (w - (slots - 1) * 2) / slots;
      String[] labels = new String[]{active ? "Active" : "Load", "Rename", "Duplicate", "Delete"};
      CachePool.Inner_DataUtil[] tones = new CachePool.Inner_DataUtil[]{
         CachePool.Inner_DataUtil.write, CachePool.Inner_DataUtil.load, CachePool.Inner_DataUtil.load, CachePool.Inner_DataUtil.update
      };
      Runnable[] actions = new Runnable[]{
         active ? null : () -> this.load_V_AutismProfile(p),
         () -> this.init(StreamUtil.Inner_StreamUtil.RENAME),
         () -> this.init(StreamUtil.Inner_StreamUtil.DUPLICATE),
         canDelete ? () -> this.read_V_AutismProfile(p) : null
      };

      for (int i = 0; i < labels.length; i++) {
         NetHelper cell = NetHelper.init(x + i % slots * (cellW + 2), y + i / slots * (update_I() + 2), cellW, update_I());
         this.init(ctx, cell, labels[i], null, tones[i], actions[i]);
      }

      int cy = y + write(w) + 4;
      cy = this.init(ctx, x, cy, w, "Auto-save changes", p.autoSave, () -> this.init_bV.init_V_AutismProfile_Z(p, !p.autoSave));
      cy = this.init(ctx, x, cy, w, "Own macro library", p.ownMacroLibrary, () -> this.init_bV.load_V_AutismProfile_Z(p, !p.ownMacroLibrary));
      cy = this.init(ctx, x, cy, w, "Own theme color", p.ownThemeColor, () -> this.init_bV.read(p, !p.ownThemeColor));
      cy += 2;
      String serverHeading = "Auto-apply on servers";
      ctx.init_PoolEntry()
         .read(ctx.init_GuiGraphicsExtract(), serverHeading, x, ctx.init_PoolEntry().init_I(NetHelper.init(x, cy, w, sync_I())), w, this.init_JsonUtil.valid);
      cy += sync_I() + 2;
      int useW = init_I_SyncManager_Str(ctx, "Use current");
      int addW = init_I_SyncManager_Str(ctx, "Add");
      int fieldW = w - useW - addW - 4;
      this.init_V_SyncManager_3(ctx, this.read_BitSetUtil, NetHelper.init(x, cy, fieldW, update_I()));
      this.init(ctx, NetHelper.init(x + fieldW + 2, cy, addW, update_I()), "Add", null, CachePool.Inner_DataUtil.read, this::sync_V);
      this.init(ctx, NetHelper.init(x + w - useW, cy, useW, update_I()), "Use current", null, CachePool.Inner_DataUtil.load, this::flush_V);
      cy += update_I() + 4;
      if (p.serverPatterns.isEmpty()) {
         this.init(ctx, "No server rules", x, cy, w, this.init_JsonUtil.valid);
      } else {
         for (String pattern : p.serverPatterns) {
            NetHelper rule = NetHelper.init(x, cy, w, sync_I());
            cy += sync_I() + 2;
            if (rule.load() > this.load_NetHelper.write() && rule.write() < this.load_NetHelper.load()) {
               PacketUtil.load(ctx.init_GuiGraphicsExtract(), rule, 3, this.init_JsonUtil.open);
               ServerData server = this.init_DataUtil.init_ServerData_Str(pattern);
               int iconSize = sync_I() - 4;
               this.init_DataUtil.init(ctx, server, autismclient.gui.f.a.DataUtil.push, rule.read() + 4, rule.write() + 2, iconSize);
               int tx = rule.read() + iconSize + 8;
               NetHelper remove = NetHelper.init(rule.init() - sync_I(), rule.write() + 1, sync_I() - 2, sync_I() - 2);
               String label = init_Str_Str_ServerData(pattern, null);
               if (server != null && server.name != null && !server.name.isBlank() && !server.name.equalsIgnoreCase(label)) {
                  label = server.name + "  |  " + label;
               }

               this.init(ctx, label, tx, rule.write(), remove.read() - tx - 4, this.init_JsonUtil.prev);
               RingBuffer.init(
                  ctx.init_GuiGraphicsExtract(), this.init_Font, remove, cz.init_Identifier, this.init_Z_SyncManager_NetHelper(ctx, remove), true, true
               );
               this.init_V_NetHelper_Runnable(remove, () -> this.init_V_AutismProfile_Str(p, pattern));
            }
         }
      }
   }

   private static String init_Str_Str_ServerData(String pattern, ServerData server) {
      if (pattern == null || pattern.isBlank()) {
         return "No server rules";
      } else if (pattern.equals("*")) {
         return "All servers";
      } else {
         return server != null && server.name != null && !server.name.isBlank() && !server.name.equalsIgnoreCase(pattern) ? server.name : pattern;
      }
   }

   private void init(autismclient.gui.f.SyncManager ctx) {
      boolean create = this.init_StreamUtil_StreamU == StreamUtil.Inner_StreamUtil.CREATE;
      NetHelper box = this.init_NetHelper_I_I(220, this.load_I());
      this.init_V_SyncManager_NetHelper(ctx, box);
      String var10000;
      switch (this.init_StreamUtil_StreamU) {
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
      this.init(ctx, title, x, inner.write(), w, this.init_JsonUtil.prev);
      int cy = inner.write() + sync_I() + 4;
      this.init_V_SyncManager_3(ctx, this.load_BitSetUtil, NetHelper.init(x, cy, w, update_I()));
      cy += update_I() + 4;
      if (create) {
         cy = this.init(ctx, x, cy, w, "Auto-save changes", this.init_Z, () -> this.init_Z = !this.init_Z);
         cy = this.init(ctx, x, cy, w, "Own macro library", this.load_Z, () -> this.load_Z = !this.load_Z);
         cy = this.init(ctx, x, cy, w, "Own theme color", this.read_Z, () -> this.read_Z = !this.read_Z);
         cy += 2;
      }

      int okW = init_I_SyncManager_Str(ctx, create ? "Create" : "Save");
      int cancelW = init_I_SyncManager_Str(ctx, "Cancel");
      int start = inner.init() - okW - cancelW - 4;
      this.init(ctx, NetHelper.init(start, cy, okW, update_I()), create ? "Create" : "Save", null, CachePool.Inner_DataUtil.read, this::update_V);
      this.init(ctx, NetHelper.init(start + okW + 4, cy, cancelW, update_I()), "Cancel", null, CachePool.Inner_DataUtil.load, this::write_V);
   }

   private void load_V_SyncManager(autismclient.gui.f.SyncManager ctx) {
      NetHelper box = this.init_NetHelper_I_I(240, read_I());
      this.init_V_SyncManager_NetHelper(ctx, box);
      NetHelper inner = box.init(6);
      this.init(ctx, "Unsaved changes", inner.read(), inner.write(), inner.update(), this.init_JsonUtil.prev);
      AutismProfile active = this.init_bV.init_AutismProfile();
      String name = active == null ? "the active profile" : active.displayName;
      this.init(ctx, "Save " + name + " before switching?", inner.read(), inner.write() + sync_I(), inner.update(), this.init_JsonUtil.valid);
      int cy = inner.write() + sync_I() * 2 + 4;
      int w = (inner.update() - 8) / 3;
      this.init(ctx, NetHelper.init(inner.read(), cy, w, update_I()), "Save", null, CachePool.Inner_DataUtil.write, () -> this.init(true));
      this.init(ctx, NetHelper.init(inner.read() + w + 4, cy, w, update_I()), "Discard", null, CachePool.Inner_DataUtil.update, () -> this.init(false));
      this.init(
         ctx,
         NetHelper.init(inner.read() + (w + 4) * 2, cy, w, update_I()),
         "Cancel",
         null,
         CachePool.Inner_DataUtil.load,
         () -> this.init_AutismProfile = null
      );
   }

   private NetHelper init_NetHelper_I_I(int idealWidth, int height) {
      int w = Math.min(this.init_NetHelper.update() - 8, Math.round(idealWidth * Math.max(1.0F, SyncManager.init_F())));
      return NetHelper.init(
         this.init_NetHelper.read() + (this.init_NetHelper.update() - w) / 2,
         this.init_NetHelper.write() + Math.max(0, (this.init_NetHelper.sync() - height) / 2),
         w,
         height
      );
   }

   private void init_V_SyncManager_NetHelper(autismclient.gui.f.SyncManager ctx, NetHelper box) {
      PacketUtil.load(ctx.init_GuiGraphicsExtract(), box, 8, this.init_JsonUtil.sync, this.init_JsonUtil.run);
   }

   private static int init_I_I_I(int count, int cols) {
      return Math.max(sync_I() * 2, (count + cols - 1) / cols * flush_I() - 4);
   }

   private static int load_I_I(int width) {
      return width >= 224 ? 2 : 1;
   }

   private static int read_I_I(int width) {
      return width >= 200 ? 4 : 2;
   }

   private static int write(int width) {
      int rows = 4 / read_I_I(width);
      return rows * (update_I() + 2) - 2;
   }

   private static int init_I_AutismProfile_I(AutismProfile profile, int width) {
      return write(width) + 12 + 5 * sync_I() + 6 + update_I() + Math.max(0, profile.serverPatterns.size() - 1) * (sync_I() + 2);
   }

   private int load_I() {
      int h = 12 + sync_I() + 8 + 2 * update_I();
      if (this.init_StreamUtil_StreamU == StreamUtil.Inner_StreamUtil.CREATE) {
         h += 3 * sync_I() + 4 + 4;
      }

      return h;
   }

   private static int read_I() {
      return 12 + 2 * sync_I() + 4 + update_I();
   }

   private static int write_I() {
      return RenderState.init(12);
   }

   private static int update_I() {
      return RenderState.init(13);
   }

   private static int sync_I() {
      return RenderState.init(14);
   }

   private static int flush_I() {
      return 2 * write_I() + 6 + 4;
   }

   private static int init_I_SyncManager_Str(autismclient.gui.f.SyncManager ctx, String label) {
      return RenderState.load(ctx.init_PoolEntry().init(label));
   }

   private void init(AutismProfile profile) {
      if (this.init_Str == null) {
         this.read_I = this.write_I;
      }

      this.init_Str = profile.id;
      this.write_I = 0;
      this.read_BitSetUtil.init_BitSetUtil_Str("");
      this.load_V();
   }

   private void read_V() {
      this.init_Str = null;
      this.read_BitSetUtil.init_BitSetUtil_Str("");
      this.write_I = this.read_I;
      this.load_V();
   }

   public boolean init_Z() {
      return this.init_StreamUtil_StreamU != StreamUtil.Inner_StreamUtil.NONE || this.init_AutismProfile != null;
   }

   public void init_V() {
      this.write_V();
      this.init_AutismProfile = null;
      this.read_V();
      this.init_BitSetUtil.init_BitSetUtil_Str("");
      this.read_I = this.write_I = 0;
      this.update_Z = false;
      this.init_NetHelper = null;
      this.init_List.clear();
      this.load_List.clear();
      this.init_DataUtil.init();
   }

   private void load_V_AutismProfile(AutismProfile target) {
      if (target != null) {
         AutismProfile cur = this.init_bV.init_AutismProfile();
         if (cur == null || !target.id.equals(cur.id)) {
            if (cur != null && !cur.autoSave && this.init_bV.init_Z()) {
               this.init_AutismProfile = target;
            } else {
               this.init_bV.read(target);
               this.init_Str = target.id;
            }
         }
      }
   }

   private void init(boolean save) {
      AutismProfile target = this.init_AutismProfile;
      this.init_AutismProfile = null;
      if (target != null) {
         if (save) {
            this.init_bV.init_V_AutismProfile(this.init_bV.init_AutismProfile());
         }

         this.init_bV.read(target);
         this.init_Str = target.id;
      }
   }

   private void read_V_AutismProfile(AutismProfile p) {
      if (p != null && this.init_bV.load_Z(p)) {
         this.read_V();
         bi.write("Deleted profile: " + p.displayName);
      }
   }

   private void init(StreamUtil.Inner_StreamUtil mode) {
      this.load_V();
      this.init_StreamUtil_StreamU = mode;
      if (mode == StreamUtil.Inner_StreamUtil.CREATE) {
         this.init_Z = true;
         this.load_Z = true;
         this.read_Z = true;
      }

      AutismProfile sel = this.init_bV.init_AutismProfile_Str(this.init_Str);
      this.load_BitSetUtil
         .init_BitSetUtil_Str(
            mode == StreamUtil.Inner_StreamUtil.RENAME && sel != null
               ? sel.displayName
               : (mode == StreamUtil.Inner_StreamUtil.DUPLICATE && sel != null ? sel.displayName + " copy" : "")
         );
      this.load_BitSetUtil.init_V_Z(true);
   }

   private void write_V() {
      this.init_StreamUtil_StreamU = StreamUtil.Inner_StreamUtil.NONE;
      this.load_BitSetUtil.init_V_Z(false);
      this.load_BitSetUtil.init_BitSetUtil_Str("");
   }

   private void update_V() {
      String name = this.load_BitSetUtil.init_Str() == null ? "" : this.load_BitSetUtil.init_Str().strip();
      if (name.isEmpty()) {
         bi.read("Enter a name.");
      } else {
         String exclude = this.init_StreamUtil_StreamU == StreamUtil.Inner_StreamUtil.RENAME ? this.init_Str : null;
         if (this.init_bV.init_Z_Str_Str(name, exclude)) {
            bi.read("Name already in use.");
         } else {
            switch (this.init_StreamUtil_StreamU) {
               case CREATE:
                  AutismProfile p = this.init_bV.init(name, this.init_Z, this.load_Z, this.read_Z);
                  if (p != null) {
                     this.init(p);
                     this.init_bV.read(p);
                  }
                  break;
               case RENAME:
                  AutismProfile selx = this.init_bV.init_AutismProfile_Str(this.init_Str);
                  if (selx != null) {
                     this.init_bV.init_Z_AutismProfile_Str(selx, name);
                  }
                  break;
               case DUPLICATE:
                  AutismProfile sel = this.init_bV.init_AutismProfile_Str(this.init_Str);
                  AutismProfile d = this.init_bV.init_AutismProfile_AutismProfile_Str(sel, name);
                  if (d != null) {
                     this.init(d);
                  }
            }

            this.write_V();
         }
      }
   }

   private void sync_V() {
      AutismProfile sel = this.init_bV.init_AutismProfile_Str(this.init_Str);
      if (sel != null) {
         String pat = this.read_BitSetUtil.init_Str() == null ? "" : this.read_BitSetUtil.init_Str().strip().toLowerCase(Locale.ROOT);
         if (!pat.isEmpty()) {
            List<String> next = new ArrayList<>(sel.serverPatterns);
            if (!next.contains(pat)) {
               next.add(pat);
            }

            this.init_bV.init_V_AutismProfile_List(sel, next);
            this.read_BitSetUtil.init_BitSetUtil_Str("");
         }
      }
   }

   private void init_V_AutismProfile_Str(AutismProfile p, String pattern) {
      if (p != null) {
         List<String> next = new ArrayList<>(p.serverPatterns);
         next.remove(pattern);
         this.init_bV.init_V_AutismProfile_List(p, next);
      }
   }

   private void flush_V() {
      Minecraft mc = Minecraft.getInstance();
      ServerData sd = mc == null ? null : mc.getCurrentServer();
      if (sd != null && sd.ip != null && !sd.ip.isBlank()) {
         this.read_BitSetUtil.init_BitSetUtil_Str(sd.ip.strip().toLowerCase(Locale.ROOT));
         this.read_BitSetUtil.init_V_Z(true);
      } else {
         bi.read("Not connected to a server.");
      }
   }

   public boolean init_Z_I_2(int mx, int my, int button) {
      if (this.init_NetHelper != null && this.init_NetHelper.init_Z(mx, my)) {
         if (button != 0) {
            return true;
         }

         if (this.init_PoolEntry_DataUtil != null && !this.init_Z() && this.init_PoolEntry_DataUtil.init(mx, my)) {
            this.update_Z = true;
            this.update_I = this.init_PoolEntry_DataUtil.load(mx, my) ? my - this.init_PoolEntry_DataUtil.update() : this.init_PoolEntry_DataUtil.sync() / 2;
            this.write_I = PoolEntry.init(this.init_PoolEntry_DataUtil, my, this.update_I);
            this.load_V();
            return true;
         }

         autismclient.gui.f.c.SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, mx, my);

         for (BitSetUtil input : this.load_List) {
            if ((input != this.read_BitSetUtil || this.load_NetHelper.init_Z(mx, my)) && input.init_Z_SyncManager_2(ctx, mx, my, button)) {
               for (BitSetUtil other : this.load_List) {
                  if (other != input) {
                     other.init_V_Z(false);
                  }
               }

               return true;
            }
         }

         this.load_V();

         for (StreamUtil.Inner_DataUtil hit : this.init_List) {
            if (hit.init_NetHelper().init_Z(mx, my)) {
               hit.init_Runnable().run();
               return true;
            }
         }

         return true;
      } else {
         this.load_V();
         return this.init_Z();
      }
   }

   public boolean load(int mx, int my, int button) {
      if (this.update_Z) {
         this.update_Z = false;
         return true;
      }

      autismclient.gui.f.c.SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, mx, my);

      for (BitSetUtil input : this.load_List) {
         if (input.load_Z_SyncManager(ctx, mx, my, button)) {
            return true;
         }
      }

      return false;
   }

   public boolean init_Z_I(int mx, int my, int button, double dx, double dy) {
      if (this.update_Z && this.init_PoolEntry_DataUtil != null) {
         this.write_I = PoolEntry.init(this.init_PoolEntry_DataUtil, my, this.update_I);
         return true;
      }

      autismclient.gui.f.c.SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, mx, my);

      for (BitSetUtil input : this.load_List) {
         if (input.init_Z_SyncManager(ctx, mx, my, button, (float)dx, (float)dy)) {
            return true;
         }
      }

      return false;
   }

   public boolean init_Z_I(int mx, int my, double amount) {
      if (this.init_Z()) {
         return true;
      } else if (this.init_PoolEntry_DataUtil != null && this.load_NetHelper.init_Z(mx, my)) {
         int pitch = this.init_Str == null ? flush_I() : sync_I() + 2;
         this.write_I = Math.clamp(this.write_I - (int)Math.round(amount * pitch), 0, this.init_PoolEntry_DataUtil.flush());
         return true;
      } else {
         return false;
      }
   }

   public boolean read(int key, int scanCode, int modifiers) {
      if (key == 256) {
         if (this.init_Z()) {
            this.write_V();
            this.init_AutismProfile = null;
            return true;
         } else if (this.load_Z()) {
            this.load_V();
            return true;
         } else if (this.init_Str != null) {
            this.read_V();
            return true;
         } else {
            return false;
         }
      } else {
         if (!this.init_Z() && this.init_Str == null && key == 70 && (modifiers & 10) != 0) {
            this.init_BitSetUtil.init_V_Z(true);
            return true;
         }

         autismclient.gui.f.c.SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, this.sync, this.flush);

         for (BitSetUtil input : this.load_List) {
            if (input.load_Z() && input.init_Z_SyncManager_3(ctx, key, scanCode, modifiers)) {
               return true;
            }
         }

         return this.init_Z();
      }
   }

   public boolean init_Z_C_I(char chr, int modifiers) {
      autismclient.gui.f.c.SyncManager ctx = this.init_SyncManager_GuiGraphicsExtract(null, this.sync, this.flush);

      for (BitSetUtil input : this.load_List) {
         if (input.load_Z() && input.init_Z_SyncManager_2(ctx, chr, modifiers)) {
            return true;
         }
      }

      return this.init_Z();
   }

   public boolean load_Z() {
      for (BitSetUtil input : this.load_List) {
         if (input.load_Z()) {
            return true;
         }
      }

      return false;
   }

   public void load_V() {
      this.load_BitSetUtil.init_V_Z(false);
      this.read_BitSetUtil.init_V_Z(false);
      this.init_BitSetUtil.init_V_Z(false);
   }

   private autismclient.gui.f.c.SyncManager init_SyncManager_GuiGraphicsExtract(GuiGraphicsExtractor g, int mx, int my) {
      return new autismclient.gui.f.c.SyncManager(
         g, this.init_Font, autismclient.gui.f.c.RingBuffer.init_RingBuffer_F(1.0F), this.init_CharEncoder, mx, my, this.init_F
      );
   }

   private void init_V_SyncManager_3(autismclient.gui.f.SyncManager ctx, BitSetUtil field, NetHelper box) {
      if (box.update() >= 10 && box.load() > this.read_NetHelper.write() && box.write() < this.read_NetHelper.load()) {
         field.init_TableIndex_F(box.read(), box.write(), box.update(), box.sync());
         field.init_V_SyncManager(this.init_SyncManager_GuiGraphicsExtract(ctx.init_GuiGraphicsExtract(), ctx.read(), ctx.write()));
         if (this.write_Z) {
            this.load_List.add(field);
         }
      }
   }

   private void init(autismclient.gui.f.SyncManager ctx, NetHelper box, String label, Identifier icon, CachePool.Inner_DataUtil tone, Runnable action) {
      boolean hovered = action != null && this.init_Z_SyncManager_NetHelper(ctx, box);
      if (icon == null) {
         CachePool.init(ctx, box, label, tone, hovered, false);
      } else {
         CachePool.init(ctx, box, label, icon, tone, hovered, false, 0.0F);
      }

      if (action != null) {
         this.init_V_NetHelper_Runnable(box, action);
      } else if (!"Active".equals(label)) {
         PacketUtil.load(ctx.init_GuiGraphicsExtract(), box, 3, 1711276032);
      }
   }

   private int init(autismclient.gui.f.SyncManager ctx, int x, int y, int w, String label, boolean value, Runnable action) {
      NetHelper row = NetHelper.init(x, y, w, sync_I());
      boolean hovered = this.init_Z_SyncManager_NetHelper(ctx, row);
      PacketUtil.load(ctx.init_GuiGraphicsExtract(), row, 3, hovered ? this.init_JsonUtil.push : this.init_JsonUtil.open);
      g.init(ctx, row, label, value, hovered, "profiles:" + label);
      this.init_V_NetHelper_Runnable(row, action);
      return row.load() + 2;
   }

   private void init(autismclient.gui.f.SyncManager ctx, String text, int x, int y, int w, int color) {
      ctx.init_PoolEntry().read(ctx.init_GuiGraphicsExtract(), text, x, ctx.init_PoolEntry().init_I(NetHelper.init(x, y, w, sync_I())), w, color);
   }

   private boolean init_Z_SyncManager_NetHelper(autismclient.gui.f.SyncManager ctx, NetHelper box) {
      return this.write_Z && box.init_Z(ctx.read(), ctx.write()) && this.read_NetHelper.init_Z(ctx.read(), ctx.write());
   }

   private void init_V_NetHelper_Runnable(NetHelper box, Runnable action) {
      if (this.write_Z) {
         int x = Math.max(box.read(), this.read_NetHelper.read());
         int y = Math.max(box.write(), this.read_NetHelper.write());
         NetHelper clipped = NetHelper.init(x, y, Math.min(box.init(), this.read_NetHelper.init()) - x, Math.min(box.load(), this.read_NetHelper.load()) - y);
         if (clipped.update() > 0 && clipped.sync() > 0) {
            this.init_List.add(new StreamUtil.Inner_DataUtil(clipped, action));
         }
      }
   }

   private record Inner_DataUtil(NetHelper init_NetHelper, Runnable init_Runnable) {
   }

   private enum Inner_StreamUtil {
      NONE,
      CREATE,
      RENAME,
      DUPLICATE;
   }
}
