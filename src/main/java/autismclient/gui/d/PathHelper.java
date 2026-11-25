package autismclient.gui.d;

import autismclient.compat263.Compat263;
import autismclient.gui.f.b.j;
import autismclient.gui.f.b.l;
import autismclient.util.bW;
import autismclient.util.bY;
import autismclient.util.cA;
import autismclient.util.ct;
import autismclient.util.k;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class PathHelper extends KeyHolder {
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final int init_I = 6;
   private static final int load_I = 4;
   private static final int read_I = 176;
   private static final int write_I = 360;
   private static final int update = 4;
   private static final int sync = 3;
   private static final int[] init_IArr = new int[]{50, 100, 200, 300, 500, 1000};
   private static final List<String> init_List = List.of("Gentle", "Balanced", "Fast", "Immediate", "Custom");
   private static final List<String> load_List = List.of("Off", "Auto", "Manual");
   private final Screen init_Screen;
   private final List<autismclient.gui.f.b.SyncManager> read_List = new ArrayList<>();
   private final String init_Str;
   private final boolean init_Z;
   private final EnumSet<autismclient.util.MethodUtil> init_EnumSet = EnumSet.allOf(autismclient.util.MethodUtil.class);
   private autismclient.util.g.FieldCache init_FieldCache;
   private String load_Str;
   private boolean load_Z;
   private long init_J;
   private long load_J;
   private EditBox init_EditBox;
   private EditBox load_EditBox;
   private EditBox read_EditBox;
   private EditBox write_EditBox;
   private k init_k;
   private int flush;
   private int close;
   private String read_Str = "";
   private int open = 176;
   private int poll = 1;
   private int push = 3;
   private int pop = 3;

   public PathHelper(Screen parent, String prefillServer) {
      this(parent, prefillServer, false);
   }

   public PathHelper(Screen parent, String prefillServer, boolean openedFromActiveConsole) {
      super(Component.literal("Multi"));
      this.init_Screen = parent;
      this.init_Str = prefillServer == null ? "" : prefillServer.trim();
      this.init_Z = openedFromActiveConsole;
      List<autismclient.util.g.FieldCache> profiles = autismclient.util.g.TaskTimer.init_TaskTimer().init_List();
      autismclient.util.g.FieldCache active = autismclient.util.g.PacketUtil.init_PacketUtil().init_FieldCache();
      autismclient.util.g.FieldCache shared = active == null
         ? autismclient.util.g.TaskTimer.init_TaskTimer().init_FieldCache_Str(autismclient.util.g.TaskTimer.init_TaskTimer().read_Str())
         : null;
      this.init_FieldCache = active != null
         ? active
         : (shared != null ? shared : (profiles.isEmpty() ? this.load_FieldCache() : new autismclient.util.g.FieldCache(profiles.getFirst())));
      this.load_Str = active == null && profiles.isEmpty() ? null : this.init_FieldCache.read_Str;
      if (this.load_Str != null) {
         autismclient.util.g.TaskTimer.init_TaskTimer().init_V_Str(this.load_Str);
      }
   }

   @Override
   protected void init() {
      this.read_Str = this.init_Str_7();
      this.load_V_18();
   }

   @Override
   public void tick() {
      super.tick();
      if (this.load_Z && System.currentTimeMillis() - this.init_J >= 500L) {
         this.open_V_3();
      }

      String key = this.init_Str_7();
      if (!key.equals(this.read_Str) && !(this.getFocused() instanceof EditBox)) {
         this.flush_V_6();
         this.read_Str = key;
         this.load_V_18();
      }
   }

   private String init_Str_7() {
      autismclient.util.g.PacketUtil manager = autismclient.util.g.PacketUtil.init_PacketUtil();
      return Boolean.toString(manager.load_Z());
   }

   private void load_V_18() {
      this.clearWidgets();
      this.read_List.clear();
      this.read_V_11();
      int ix = this.data();
      int innerW = this.value();
      int ctl = add_I();
      int gap = 2;
      int half = (innerW - gap) / 2;
      boolean custom = this.init_FieldCache.init_FieldCache_StreamU == autismclient.util.g.FieldCache.Inner_StreamUtil.update;
      boolean auto = this.init_FieldCache.init_FieldCache_CachePo == autismclient.util.g.FieldCache.Inner_CachePool.load;
      boolean locked = this.init_Z_Str_3(this.init_FieldCache.read_Str);
      this.init_EditBox = this.load_EditBox = this.read_EditBox = this.write_EditBox = null;
      if (locked) {
         this.load(ix, innerW);
      }

      if (!locked) {
         this.init_EditBox = this.init(ix, this.init_I_I_11(0), half, "Profile name", this.init_FieldCache.write_Str, 64);
         this.load_EditBox = this.init(ix + half + gap, this.init_I_I_11(0), innerW - half - gap, "host:port", this.init_FieldCache.update_Str, 255);
         int dropY = this.init_I_I_11(1);
         if (auto) {
            int third = (innerW - 2 * gap) / 3;
            this.load(ix, dropY, third);
            this.read(ix + third + gap, dropY, third);
            int pingX = ix + 2 * (third + gap);
            this.read_List
               .add(
                  new autismclient.gui.f.b.SyncManager(
                        pingX, dropY, innerW - 2 * (third + gap), ctl, init_List_9(), load_I_I_3(this.init_FieldCache.update_I), index -> {
                           this.flush_V_6();
                           if (index >= 0 && index < init_IArr.length) {
                              this.init_FieldCache.update_I = init_IArr[index];
                           }

                           this.open_V_3();
                           this.load_V_18();
                        }
                     )
                     .init_SyncManager_Str("<=" + this.init_FieldCache.update_I + "ms")
               );
         } else {
            this.load(ix, dropY, half);
            this.read(ix + half + gap, dropY, innerW - half - gap);
         }

         if (custom) {
            this.read_EditBox = this.init(ix, this.init_I_I_11(2), half, "Accounts 1-500", Integer.toString(this.init_FieldCache.read_I), 4);
            this.write_EditBox = this.init(
               ix + half + gap, this.init_I_I_11(2), innerW - half - gap, "Delay 0-5000ms", Integer.toString(this.init_FieldCache.write_I), 6
            );
         } else {
            this.read_EditBox = null;
            this.write_EditBox = null;
         }

         int macroY = this.init_I_I_11(this.sync_I_7());
         String allLabel = this.init_FieldCache.sync.isBlank() ? "Macro (all): none" : "Macro (all): " + this.init_FieldCache.sync;
         this.init(
            ix,
            macroY,
            innerW,
            ctl,
            allLabel,
            this.init_FieldCache.sync.isBlank() ? autismclient.gui.f.b.CachePool.Inner_DataUtil.init : autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
            b -> this.put_V()
         );
         int formY = this.init_I_I_11(this.sync_I_7() + 1);
         this.init(ix, formY, innerW, ctl, "Passwords (login)", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, b -> this.set_V_2());
         if (!this.load_Z_3()) {
            this.init(ix, this.poll_I_2(), innerW);
         }
      }

      int footerY = this.buffer();
      int profileFooterY = this.queue();
      int leftX = this.next() + 4;
      int newW = this.init_I_Str_14("New");
      int duplicateW = this.init_I_Str_14("Duplicate");
      this.init(leftX, profileFooterY, newW, ctl, "New", autismclient.gui.f.b.CachePool.Inner_DataUtil.read, b -> this.pop_V_2());
      this.init(leftX + newW + gap, profileFooterY, duplicateW, ctl, "Duplicate", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, b -> this.run_V_2());
      if (autismclient.util.g.PacketUtil.init_PacketUtil().load_Z()) {
         int activeX = leftX + newW + gap + duplicateW + gap;
         int activeMaxRight = this.next() + this.size() - 4;
         int activeW = Math.min(this.read_I_11(), Math.max(1, activeMaxRight - activeX));
         StreamGate activeButton = new StreamGate(
            activeX,
            profileFooterY,
            activeW,
            ctl,
            Component.literal("Active Multi"),
            autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
            () -> "Active " + autismclient.util.g.PacketUtil.init_PacketUtil().read_Str(),
            b -> this.push_V_2()
         );
         this.addRenderableWidget(activeButton);
      }

      int footerX = ix;
      int footerW = Math.max(1, innerW);
      int footerGap = gap;
      boolean compactActiveReturn = this.init_Z && autismclient.util.g.PacketUtil.init_PacketUtil().load_Z();
      int gaps = compactActiveReturn ? 1 : 2;
      int advancedW = compactActiveReturn ? 0 : this.init_I_Str_14("Advanced");
      int connectW = this.init_I_Str_14("Connect");
      int backW = this.init_I_Str_14("Back");
      if (advancedW + connectW + backW + gaps * footerGap > footerW) {
         int footerEach = Math.max(1, (footerW - footerGap * gaps) / (gaps + 1));
         advancedW = compactActiveReturn ? 0 : footerEach;
         connectW = footerEach;
         backW = Math.max(1, footerW - gaps * (footerEach + footerGap));
      }

      int backX = footerX + footerW - backW;
      int connectX = backX - footerGap - connectW;
      if (!compactActiveReturn) {
         this.init(
            connectX - footerGap - advancedW, footerY, advancedW, ctl, "Advanced", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, b -> this.get_V_2()
         );
      }

      this.init(connectX, footerY, connectW, ctl, "Connect", autismclient.gui.f.b.CachePool.Inner_DataUtil.write, b -> this.poll_V_2());
      this.init(backX, footerY, backW, ctl, "Back", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, b -> this.onClose());
      this.init(
         ix + innerW - this.update_I_8(),
         this.open_I_3(),
         this.update_I_8(),
         ctl,
         "Accounts...",
         autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         b -> this.write_V_9()
      );
      if (this.init_Z_9()) {
         String commonProxy = this.read_Str_2();
         String label = commonProxy == null ? "Set All Proxies (Mixed)..." : "Set All Proxies: " + this.load_Str_Str_5(commonProxy);
         int bulkW = Math.min(innerW, this.init_I_Str_14(label));
         this.init(
            ix,
            this.flush_I_6(),
            bulkW,
            ctl,
            label,
            autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
            button -> this.init("Proxy for all selected accounts", commonProxy, this::load_V_Str)
         );
      }

      this.update_V_8();
      this.sync_V_7();
   }

   private void read_V_11() {
      int ctl = add_I();
      int gap = 2;
      int want = autismclient.gui.f.b.k.init(this.font, "Profiles", init_CharEncoder.init_Identifier_l(l.read), 0);
      int footerW = this.init_I_Str_14("New") + gap + this.init_I_Str_14("Duplicate");
      if (autismclient.util.g.PacketUtil.init_PacketUtil().load_Z()) {
         footerW += gap + this.read_I_11();
      }

      want = Math.max(want, footerW);
      List<autismclient.util.g.FieldCache> profiles = autismclient.util.g.TaskTimer.init_TaskTimer().init_List();

      for (autismclient.util.g.FieldCache profile : profiles) {
         want = Math.max(want, this.init_I_Str_14("> " + profile.write_Str) + gap + this.write_I_9());
      }

      int room = this.init_I() - 12 - 4 - 180;
      this.open = Math.max(90, Math.min(Math.min(176, want + 8), room));
      this.poll = Math.max(1, Math.min(360, this.init_I() - 12 - 4 - this.open));
      int pitch = clear_I();
      int reserve = 4 + ctl + 4 + 6 - gap;
      int profileFit = Math.max(1, (this.load_I() - reserve - this.stop()) / pitch);
      this.push = Math.max(Math.min(3, profileFit), Math.min(profiles.size(), profileFit));
      int accountFit = Math.max(1, (this.load_I() - reserve - this.push_I_2()) / pitch);
      int accountCount = this.init_List_Z(false).size();
      this.pop = Math.max(Math.min(3, accountFit), Math.min(accountCount, accountFit));
   }

   private int init_I_Str_14(String text) {
      return autismclient.gui.f.RenderState.load(
         autismclient.gui.f.b.k.init(this.font, autismclient.util.g.PacketUtil.update(text, 72), init_CharEncoder.init_Identifier_l(l.read), 0)
      );
   }

   private int read_I_11() {
      return Math.max(
         40,
         autismclient.gui.f.b.k.init(
               this.font, "Active " + autismclient.util.g.PacketUtil.init_PacketUtil().read_Str(), init_CharEncoder.init_Identifier_l(l.read), 0
            )
            + 24
      );
   }

   private int write_I_9() {
      return Math.max(add_I(), autismclient.gui.f.b.k.init(this.font, "X", init_CharEncoder.init_Identifier_l(l.read), 0) + 8);
   }

   private void write_V_9() {
      this.flush_V_6();
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(new StreamUtil(this));
      }
   }

   private void load(int ix, int innerW) {
      this.init(ix, this.init_I_I_11(0), innerW, "Profile: " + this.init_FieldCache.write_Str);
      this.init(ix, this.init_I_I_11(1), innerW, "Server: " + (this.init_FieldCache.update_Str.isBlank() ? "-" : this.init_FieldCache.update_Str));
      this.init(ix, this.init_I_I_11(2), innerW, this.init_FieldCache.init_List.size() + " accounts");
      StreamGate note = this.init(
         ix, this.init_I_I_11(3), innerW, add_I(), "Batch running - Disconnect to edit", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, b -> {}
      );
      note.active = false;
   }

   private void init(int x, int y, int w, String text) {
      StreamGate label = this.init(x, y, w, add_I(), text, autismclient.gui.f.b.CachePool.Inner_DataUtil.init, b -> {});
      label.active = false;
   }

   private EditBox init(int x, int y, int w, String hint, String value, int max) {
      EditBox box = new StateCache(this.font, x, y, Math.max(1, w), add_I(), Component.literal(hint));
      box.setMaxLength(max);
      box.setValue(value);
      box.setHint(Component.literal(hint));
      box.setResponder(v -> this.close_V_4());
      this.addRenderableWidget(box);
      return box;
   }

   private void init(int ix, int y, int innerW) {
      String[] labels = new String[]{"Cracked", "Session", "Microsoft", "TheAltening"};
      autismclient.util.MethodUtil[] types = new autismclient.util.MethodUtil[]{
         autismclient.util.MethodUtil.init, autismclient.util.MethodUtil.load, autismclient.util.MethodUtil.read, autismclient.util.MethodUtil.write
      };
      int gap = 2;
      int chipW = (innerW - gap * (labels.length - 1)) / labels.length;

      for (int i = 0; i < labels.length; i++) {
         autismclient.util.MethodUtil type = types[i];
         autismclient.gui.f.b.CachePool.Inner_DataUtil tone = this.init_EnumSet.contains(type)
            ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write
            : autismclient.gui.f.b.CachePool.Inner_DataUtil.init;
         StreamGate filter = new StreamGate(
            ix + i * (chipW + gap), y, Math.max(1, chipW), add_I(), Component.literal(this.init_Str_Str_I(labels[i], chipW - 6)), tone, b -> {
               this.flush_V_6();
               if (!this.init_EnumSet.add(type)) {
                  this.init_EnumSet.remove(type);
               }

               this.close = 0;
               this.load_V_18();
            }
         );
         filter.init(this.init_EnumSet.contains(type));
         this.addRenderableWidget(filter);
      }
   }

   private void update_V_8() {
      List<autismclient.util.g.FieldCache> profiles = autismclient.util.g.TaskTimer.init_TaskTimer().init_List();
      int visible = Math.max(1, this.push);
      this.flush = Math.max(0, Math.min(this.flush, Math.max(0, profiles.size() - visible)));
      int end = Math.min(profiles.size(), this.flush + visible);
      int y = this.stop();
      int x = this.next() + 4;
      int rowH = add_I();
      int delW = this.write_I_9();
      int nameW = Math.max(24, this.size() - 8 - 2 - delW);

      for (int i = this.flush; i < end; i++) {
         autismclient.util.g.FieldCache profile = profiles.get(i);
         boolean picked = profile.read_Str.equals(this.load_Str);
         autismclient.gui.f.b.CachePool.Inner_DataUtil tone = picked
            ? autismclient.gui.f.b.CachePool.Inner_DataUtil.write
            : autismclient.gui.f.b.CachePool.Inner_DataUtil.init;
         String mark = picked ? "> " : "";
         this.init(x, y, nameW, rowH, mark + profile.write_Str, tone, b -> this.read(profile)).init(picked);
         StreamGate delete = this.init(
            x + nameW + 2, y, delW, rowH, "X", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, b -> this.init_V_FieldCache(profile)
         );
         delete.active = !this.init_Z_Str_3(profile.read_Str);
         y += clear_I();
      }
   }

   private void sync_V_7() {
      List<PathHelper.Inner_DataUtil> choices = this.init_List_Z(true);
      boolean manual = this.init_FieldCache.init_FieldCache_CachePo == autismclient.util.g.FieldCache.Inner_CachePool.read;
      boolean locked = this.init_Z_Str_3(this.init_FieldCache.read_Str);
      int visible = Math.max(1, this.pop);
      this.close = Math.max(0, Math.min(this.close, Math.max(0, choices.size() - visible)));
      int end = Math.min(choices.size(), this.close + visible);
      int x = this.data();
      int w = this.value();
      int y = this.push_I_2();
      int rowH = add_I();
      int gap = 2;

      for (int i = this.close; i < end; i++) {
         PathHelper.Inner_DataUtil choice = choices.get(i);
         boolean current = choice.init_Z();
         autismclient.util.g.FieldCache.Inner_BufferHelper selected = current ? null : this.init_FieldCache_BufferH_Str(choice.init_Str());
         autismclient.gui.f.b.CachePool.Inner_DataUtil tone = current
            ? autismclient.gui.f.b.CachePool.Inner_DataUtil.update
            : (selected == null ? autismclient.gui.f.b.CachePool.Inner_DataUtil.init : autismclient.gui.f.b.CachePool.Inner_DataUtil.write);
         boolean canToggle = !current;
         String accountLabel = choice.load() + (current ? " (Current)" : "");
         if (manual) {
            int proxyW = Math.max(1, Math.min(128, Math.max(36, w / 3)));
            if (proxyW + gap >= w) {
               proxyW = Math.max(1, w / 2);
            }

            int toggleW = Math.max(1, w - proxyW - gap);
            StreamGate toggle = this.init(x, y, toggleW, rowH, accountLabel, tone, b -> this.init_V_Str_7(choice.init_Str()));
            toggle.active = canToggle && !locked;
            toggle.init(selected != null || current);
            if (selected == null) {
               StreamGate proxyButton = this.init(x + toggleW + gap, y, proxyW, rowH, "-", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, b -> {});
               proxyButton.active = false;
            } else {
               StreamGate proxyButton = this.init(
                  x + toggleW + gap,
                  y,
                  proxyW,
                  rowH,
                  this.load_Str_Str_5(selected.load_Str()),
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.read,
                  button -> this.init("Proxy for " + choice.load(), selected.load_Str(), proxyId -> {
                     autismclient.util.g.FieldCache.Inner_BufferHelper spec = this.init_FieldCache_BufferH_Str(choice.init_Str());
                     if (spec != null) {
                        this.init_V_FieldCache_BufferH_Str(spec, proxyId);
                     }
                  })
               );
               proxyButton.active = !choice.init_Z() && !locked;
            }
         } else {
            StreamGate toggle = this.init(x, y, w, rowH, accountLabel, tone, b -> this.init_V_Str_7(choice.init_Str()));
            toggle.active = canToggle && !locked;
            toggle.init(selected != null || current);
         }

         y += clear_I();
      }
   }

   private void flush_V_6() {
      if (this.init_EditBox != null) {
         this.init_FieldCache.write_Str = init_Str_Str(this.init_EditBox.getValue());
      }

      if (this.load_EditBox != null) {
         this.init_FieldCache.update_Str = init_Str_Str(this.load_EditBox.getValue());
      }

      if (this.read_EditBox != null) {
         this.init_FieldCache.read_I = init_I_Str_I_2(this.read_EditBox.getValue(), this.init_FieldCache.read_I);
      }

      if (this.write_EditBox != null) {
         this.init_FieldCache.write_I = init_I_Str_I_2(this.write_EditBox.getValue(), this.init_FieldCache.write_I);
      }

      this.init_FieldCache.init_V();
   }

   private void close_V_4() {
      this.load_Z = true;
      this.init_J = System.currentTimeMillis();
   }

   private void open_V_3() {
      this.flush_V_6();
      autismclient.util.g.TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
      this.load_Str = this.init_FieldCache.read_Str;
      autismclient.util.g.TaskTimer.init_TaskTimer().init_V_Str(this.init_FieldCache.read_Str);
      boolean wasDirty = this.load_Z;
      this.load_Z = false;
      if (wasDirty) {
         this.load_J = System.currentTimeMillis();
      }
   }

   private void poll_V_2() {
      this.flush_V_6();
      this.init_FieldCache.write_Str = autismclient.util.g.TaskTimer.init_TaskTimer().init_Str(this.init_FieldCache.write_Str, this.init_FieldCache.read_Str);
      autismclient.util.g.PacketUtil.Inner_TextFormat result = autismclient.util.g.PacketUtil.init_PacketUtil()
         .init_PacketUtil_TextFor_FieldCache(this.init_FieldCache);
      if (!result.init_Z()) {
         this.init_V_Str_I(autismclient.util.g.PacketUtil.update(result.init_Str(), 90), ValueSink.poll);
      } else {
         autismclient.util.g.TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
         this.load_Str = this.init_FieldCache.read_Str;
         this.load_Z = false;
         this.minecraft.gui.setScreen(new TableIndex(this.init_Screen));
      }
   }

   private void push_V_2() {
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(new TableIndex(this.init_Screen));
      }
   }

   private void pop_V_2() {
      if (this.load_Z) {
         this.open_V_3();
      }

      this.init_FieldCache = this.load_FieldCache();
      autismclient.util.g.TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
      this.load_Str = this.init_FieldCache.read_Str;
      autismclient.util.g.TaskTimer.init_TaskTimer().init_V_Str(this.init_FieldCache.read_Str);
      this.load_Z = false;
      this.flush = 0;
      this.close = 0;
      this.init_V_Str_I("New profile created.", ValueSink.open);
      this.load_V_18();
   }

   private void run_V_2() {
      if (this.load_Z) {
         this.open_V_3();
      }

      autismclient.util.g.FieldCache copy = new autismclient.util.g.FieldCache(this.init_FieldCache);
      copy.read_Str = UUID.randomUUID().toString();
      copy.write_Str = autismclient.util.g.TaskTimer.init_TaskTimer().init_Str(copy.write_Str, copy.read_Str);
      this.init_FieldCache = copy;
      autismclient.util.g.TaskTimer.init_TaskTimer().init_V_FieldCache(this.init_FieldCache);
      this.load_Str = this.init_FieldCache.read_Str;
      this.load_Z = false;
      this.init_V_Str_I("Duplicated.", ValueSink.open);
      this.load_V_18();
   }

   private void init_V_FieldCache(autismclient.util.g.FieldCache profile) {
      this.flush_V_6();
      if (profile != null && this.init_Z_Str_3(profile.read_Str)) {
         this.init_V_Str_I("Disconnect the active profile first.", ValueSink.poll);
      } else {
         autismclient.util.g.TaskTimer.init_TaskTimer().load(profile.read_Str);
         if (profile.read_Str.equals(this.load_Str) || this.init_FieldCache.read_Str.equals(profile.read_Str)) {
            List<autismclient.util.g.FieldCache> remaining = autismclient.util.g.TaskTimer.init_TaskTimer().init_List();
            this.init_FieldCache = remaining.isEmpty() ? this.load_FieldCache() : new autismclient.util.g.FieldCache(remaining.getFirst());
            this.load_Str = remaining.isEmpty() ? null : this.init_FieldCache.read_Str;
            this.load_Z = false;
         }

         this.init_V_Str_I("Deleted " + autismclient.util.g.PacketUtil.update(profile.write_Str, 40) + ".", ValueSink.close);
         this.load_V_18();
      }
   }

   private void get_V_2() {
      this.flush_V_6();
      this.minecraft.gui.setScreen(new CharEncoder(this, this.init_FieldCache.init_IoWorker, policy -> {
         this.init_FieldCache.init_IoWorker = policy;
         this.open_V_3();
      }, this.init_FieldCache.init_CachePool, cfg -> {
         this.init_FieldCache.init_CachePool = cfg;
         this.open_V_3();
      }, false));
   }

   private autismclient.util.g.FieldCache init_FieldCache() {
      return new autismclient.util.g.FieldCache(this.init_FieldCache);
   }

   private autismclient.util.g.DataUtil init_DataUtil_Set(Set<String> accountIds) {
      return autismclient.util.g.PathHelper.init(this.init_FieldCache(), accountIds);
   }

   private void set_V_2() {
      this.flush_V_6();
      Set<String> accounts = new LinkedHashSet<>();

      for (autismclient.util.g.FieldCache.Inner_BufferHelper spec : this.init_FieldCache.init_List) {
         accounts.add(spec.init_Str());
      }

      this.minecraft.gui.setScreen(new UiHelper(this, this.init_DataUtil_Set(accounts), accounts, updated -> {
         this.init_FieldCache = autismclient.util.g.PathHelper.init_FieldCache(updated, this.init_FieldCache());
         this.open_V_3();
         this.load_V_18();
      }, false));
   }

   private void load_V_FieldCache(autismclient.util.g.FieldCache profile) {
      if (this.load_Z) {
         this.open_V_3();
      }

      this.init_FieldCache = new autismclient.util.g.FieldCache(profile);
      this.load_Str = this.init_FieldCache.read_Str;
      autismclient.util.g.TaskTimer.init_TaskTimer().init_V_Str(this.init_FieldCache.read_Str);
      this.load_Z = false;
      this.close = 0;
      this.load_V_18();
   }

   private void read(autismclient.util.g.FieldCache profile) {
      if (profile != null && !profile.read_Str.equals(this.load_Str)) {
         this.load_V_FieldCache(profile);
      }
   }

   private void load(int x, int y, int width) {
      this.read_List
         .add(new autismclient.gui.f.b.SyncManager(x, y, width, add_I(), init_List, this.init_FieldCache.init_FieldCache_StreamU.ordinal(), index -> {
            this.flush_V_6();
            if (index >= 0 && index < autismclient.util.g.FieldCache.Inner_StreamUtil.values().length) {
               this.init_FieldCache.init_FieldCache_StreamU = autismclient.util.g.FieldCache.Inner_StreamUtil.values()[index];
               this.open_V_3();
               this.load_V_18();
            }
         }).init_SyncManager_Str("Join speed: " + this.init_FieldCache.init_FieldCache_StreamU.name()));
   }

   private void read(int x, int y, int width) {
      this.read_List
         .add(new autismclient.gui.f.b.SyncManager(x, y, width, add_I(), load_List, this.init_FieldCache.init_FieldCache_CachePo.ordinal(), index -> {
            this.flush_V_6();
            if (index >= 0 && index < autismclient.util.g.FieldCache.Inner_CachePool.values().length) {
               this.init_FieldCache.init_FieldCache_CachePo = autismclient.util.g.FieldCache.Inner_CachePool.values()[index];
               this.open_V_3();
               this.load_V_18();
            }
         }).init_SyncManager_Str("Proxy: " + this.load_Str_4()));
   }

   private void put_V() {
      this.flush_V_6();
      this.minecraft.gui.setScreen(new BitSetUtil(this, this.init_FieldCache.sync, name -> {
         this.init_FieldCache.sync = name == null ? "" : name;
         this.open_V_3();
      }));
   }

   private void init_V_Str_7(String accountId) {
      if (!this.init_Z_Str_3(this.init_FieldCache.read_Str)) {
         if (autismclient.util.g.PacketUtil.init_Z_Str(accountId)) {
            this.init_V_Str_I("Current account cannot join Multi.", ValueSink.poll);
         } else {
            this.flush_V_6();
            autismclient.util.g.FieldCache.Inner_BufferHelper existing = this.init_FieldCache_BufferH_Str(accountId);
            if (existing != null) {
               this.init_FieldCache.init_List.remove(existing);
            } else if (this.init_FieldCache.init_List.size() < 500) {
               this.init_FieldCache.init_List.add(new autismclient.util.g.FieldCache.Inner_BufferHelper(accountId, ""));
            } else {
               this.init_V_Str_I("Maximum 500 accounts.", ValueSink.poll);
            }

            this.open_V_3();
            this.load_V_18();
         }
      }
   }

   private void init_V_FieldCache_BufferH_Str(autismclient.util.g.FieldCache.Inner_BufferHelper existing, String proxyId) {
      if (!this.init_Z_Str_3(this.init_FieldCache.read_Str)) {
         int index = this.init_FieldCache.init_List.indexOf(existing);
         if (index >= 0) {
            this.init_FieldCache.init_List.set(index, new autismclient.util.g.FieldCache.Inner_BufferHelper(existing.init_Str(), proxyId, existing.read()));
         }

         this.open_V_3();
         this.load_V_18();
      }
   }

   private void init(String title, String selectedProxyId, Consumer<String> onPick) {
      this.flush_V_6();
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(new FieldCache(this, title, this.init_FieldCache.update_Str, selectedProxyId, this.init_Map_2(), onPick));
      }
   }

   private Map<String, Integer> init_Map_2() {
      Map<String, Integer> usage = new LinkedHashMap<>();

      for (autismclient.util.g.FieldCache.Inner_BufferHelper spec : this.init_FieldCache.init_List) {
         if (!spec.init_Z() && !spec.load_Z()) {
            usage.merge(spec.load_Str(), 1, Integer::sum);
         }
      }

      return Map.copyOf(usage);
   }

   private void load_V_Str(String proxyId) {
      if (!this.init_Z_Str_3(this.init_FieldCache.read_Str)) {
         if (this.init_FieldCache.init_List.isEmpty()) {
            this.init_V_Str_I("Select some accounts first.", ValueSink.poll);
         } else {
            String id = proxyId == null ? "" : proxyId;

            for (int i = 0; i < this.init_FieldCache.init_List.size(); i++) {
               autismclient.util.g.FieldCache.Inner_BufferHelper spec = this.init_FieldCache.init_List.get(i);
               this.init_FieldCache.init_List.set(i, new autismclient.util.g.FieldCache.Inner_BufferHelper(spec.init_Str(), id, spec.read()));
            }

            this.open_V_3();
            this.load_V_18();
            this.init_V_Str_I(
               "Set proxy on " + this.init_FieldCache.init_List.size() + " account" + (this.init_FieldCache.init_List.size() == 1 ? "" : "s") + ".",
               ValueSink.open
            );
         }
      }
   }

   private autismclient.util.g.FieldCache.Inner_BufferHelper init_FieldCache_BufferH_Str(String accountId) {
      for (autismclient.util.g.FieldCache.Inner_BufferHelper spec : this.init_FieldCache.init_List) {
         if (spec.init_Str().equals(accountId)) {
            return spec;
         }
      }

      return null;
   }

   private String load_Str_Str_5(String proxyId) {
      if (proxyId == null || proxyId.isBlank()) {
         return "Proxy Off";
      }

      if ("best".equals(proxyId)) {
         return "Best Proxy";
      }

      bW proxy = bY.init_bY().init_bW_Str(proxyId);
      return proxy == null ? "Missing" : proxy.init_Str();
   }

   private String load_Str_4() {
      String var10000;
      switch (this.init_FieldCache.init_FieldCache_CachePo) {
         case init:
            var10000 = "Off";
            break;
         case load:
            var10000 = "Auto";
            break;
         case read:
            var10000 = "Manual";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private String read_Str_2() {
      if (this.init_FieldCache.init_List.isEmpty()) {
         return "";
      }

      String common = this.init_FieldCache.init_List.getFirst().load_Str();

      for (int i = 1; i < this.init_FieldCache.init_List.size(); i++) {
         if (!Objects.equals(common, this.init_FieldCache.init_List.get(i).load_Str())) {
            return null;
         }
      }

      return common == null ? "" : common;
   }

   private List<PathHelper.Inner_DataUtil> init_List_Z(boolean filtered) {
      List<PathHelper.Inner_DataUtil> choices = new ArrayList<>();
      Set<String> known = new HashSet<>();
      choices.add(
         new PathHelper.Inner_DataUtil(
            "default", autismclient.util.FieldCache.init_User().getName() + " (Default)", autismclient.util.g.PacketUtil.init_Z_Str("default")
         )
      );
      known.add("default");
      String query = filtered ? this.write_Str_2().toLowerCase(Locale.ROOT) : "";

      for (autismclient.util.BitSetUtil account : autismclient.util.TokenStore.init_TokenStore().init_List()) {
         known.add(account.write_Str());
         autismclient.util.MethodUtil type = account.init_MethodUtil == null ? autismclient.util.MethodUtil.init : account.init_MethodUtil;
         if (!filtered || this.init_EnumSet.contains(type)) {
            String label = account.init_Str() + " (" + type.name() + ")";
            if (query.isEmpty() || label.toLowerCase(Locale.ROOT).contains(query)) {
               choices.add(new PathHelper.Inner_DataUtil(account.write_Str(), label, autismclient.util.g.PacketUtil.init_Z_Str(account.write_Str())));
            }
         }
      }

      for (autismclient.util.g.FieldCache.Inner_BufferHelper spec : this.init_FieldCache.init_List) {
         if (known.add(spec.init_Str())) {
            choices.add(new PathHelper.Inner_DataUtil(spec.init_Str(), "Missing account: " + spec.init_Str(), false));
         }
      }

      return choices;
   }

   @Override
   public boolean charTyped(CharacterEvent event) {
      return this.init_k != null && this.init_k.init_Z_CharacterEvent(event) ? true : super.charTyped(event);
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (Compat263.keyEventGlfwKey(event) == 256 && autismclient.gui.f.b.SyncManager.read(this.read_List)) {
         return true;
      } else {
         return this.init_k != null && this.init_k.init_Z_KeyEvent(event) ? true : super.keyPressed(event);
      }
   }

   private void add_V() {
      if (this.init_k == null) {
         this.init_k = new k(Minecraft.getInstance(), this.font, 0, 0, 20, add_I(), false);
         this.init_k.init_V_Component(Component.literal("Search accounts..."));
         this.init_k.load(48);
         this.init_k.init_V_Consumer(value -> {
            this.close = 0;
            this.load_V_18();
         });
      }
   }

   private String write_Str_2() {
      return this.init_k == null ? "" : this.init_k.load_Str();
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      double vx = cA.init_D_D(mouseX);
      double vy = cA.init_D_D(mouseY);
      if (autismclient.gui.f.b.SyncManager.init_Z_List(this.read_List, vx, vy, vertical)) {
         return true;
      } else if (vx < this.next() + this.size()) {
         this.flush_V_6();
         this.flush = Math.max(0, this.flush + (vertical < 0.0 ? 1 : -1));
         this.load_V_18();
         return true;
      } else if (vx >= this.prev() && vy >= this.push_I_2() - 4) {
         this.flush_V_6();
         this.close = Math.max(0, this.close + (vertical < 0.0 ? 1 : -1));
         this.load_V_18();
         return true;
      } else {
         return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (autismclient.gui.f.b.SyncManager.init_Z_List_2(this.read_List, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
         return true;
      } else if (this.init_k != null && this.init_k.init_Z_D_2(virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
         this.flush_V_6();
         this.clear_V();
         return true;
      } else {
         return super.mouseClicked(virtualEvent, doubled);
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (autismclient.gui.f.b.SyncManager.write(this.read_List)) {
         return true;
      }

      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (this.init_k != null) {
         this.init_k.load(virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent));
      }

      return super.mouseReleased(virtualEvent);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      MouseButtonEvent virtualEvent = init_MouseButtonEvent_MouseButtonEvent(event);
      if (autismclient.gui.f.b.SyncManager.load(this.read_List, virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent))) {
         return true;
      }

      double vdx = cA.init_D_D(dragX);
      double vdy = cA.init_D_D(dragY);
      return this.init_k != null && this.init_k.init(virtualEvent.x(), virtualEvent.y(), Compat263.mouseEventGlfwButton(virtualEvent), vdx, vdy)
         ? true
         : super.mouseDragged(virtualEvent, vdx, vdy);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int virtualMouseX = cA.init_I_D(mouseX);
      int virtualMouseY = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), pop_I_2());
         int leftPanelHeight = Math.max(1, this.queue() + add_I() + 4 - 6);
         int rightPanelHeight = Math.max(1, this.buffer() + add_I() + 4 - 6);
         autismclient.gui.f.PacketUtil.load(graphics, autismclient.gui.f.NetHelper.init(this.next(), 6, this.size(), leftPanelHeight), 6, run_I_2(), get_I_2());
         autismclient.gui.f.PacketUtil.load(
            graphics, autismclient.gui.f.NetHelper.init(this.prev(), 6, this.valid(), rightPanelHeight), 6, tick_I_12(), get_I_2()
         );
         int headerTextY = this.init_I_I_I_7(6, reset());
         this.init(graphics, "Profiles", this.next() + 4, headerTextY, set_I_2());
         this.init(graphics, this.init_Str_Str_I(this.update_Str_2(), this.value()), this.data(), headerTextY, set_I_2());
         int ix = this.data();
         int innerW = this.value();
         int groupTextY = this.init_I_I_I_7(this.close_I_4(), start());
         this.init(graphics, "Accounts", ix, groupTextY, set_I_2());
         long selectedCount = this.init_FieldCache.init_List.stream().filter(s -> !autismclient.util.g.PacketUtil.init_Z_Str(s.init_Str())).count();
         this.load(graphics, selectedCount + " selected", ix + innerW, groupTextY, put_I());
         this.init(graphics, virtualMouseX, virtualMouseY, delta);
         boolean menuOpen = autismclient.gui.f.b.SyncManager.init_Z_List(this.read_List);
         int mx = menuOpen ? -2147483648 : virtualMouseX;
         int my = menuOpen ? -2147483648 : virtualMouseY;
         super.extractRenderState(graphics, mx, my, delta);
         autismclient.gui.f.b.SyncManager.init(graphics, this.font, this.read_List, virtualMouseX, virtualMouseY);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      autismclient.gui.f.NetHelper box = this.init_NetHelper_3();
      this.add_V();
      this.init_k.read(box.read());
      this.init_k.write(box.write());
      this.init_k.update(box.update());
      this.init_k.sync(box.sync());
      this.init_k.init_V(graphics, mouseX, mouseY, delta);
   }

   private int update_I_8() {
      return this.init_I_Str_14("Accounts...");
   }

   private autismclient.gui.f.NetHelper init_NetHelper_3() {
      return autismclient.gui.f.NetHelper.init(this.data(), this.open_I_3(), Math.max(20, this.value() - this.update_I_8() - 2), add_I());
   }

   private String update_Str_2() {
      if (this.load_Str == null) {
         return "New profile";
      } else {
         String name = autismclient.util.g.PacketUtil.update(this.init_FieldCache.write_Str, 28);
         if (this.load_Z) {
            return "Editing " + name + " (changed)";
         } else {
            return System.currentTimeMillis() - this.load_J < 1500L ? "Editing " + name + " (saved)" : "Editing " + name;
         }
      }
   }

   private int sync_I_7() {
      return this.init_FieldCache.init_FieldCache_StreamU == autismclient.util.g.FieldCache.Inner_StreamUtil.update ? 3 : 2;
   }

   private int init_I_I_11(int row) {
      return this.stop() + row * clear_I();
   }

   private int flush_I_6() {
      return this.init_I_I_11(this.sync_I_7() + 2) - 2 + 4;
   }

   private int close_I_4() {
      return this.flush_I_6() + (this.init_Z_9() ? clear_I() : 0);
   }

   private int open_I_3() {
      return this.close_I_4() + start();
   }

   private boolean init_Z_9() {
      return this.init_FieldCache.init_FieldCache_CachePo == autismclient.util.g.FieldCache.Inner_CachePool.read
         && !this.init_Z_Str_3(this.init_FieldCache.read_Str);
   }

   private int poll_I_2() {
      return this.open_I_3() + clear_I();
   }

   private int push_I_2() {
      return this.open_I_3() + clear_I() * (this.load_Z_3() ? 1 : 2);
   }

   @Override
   public void onClose() {
      if (this.load_Z) {
         this.open_V_3();
      }

      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   private StreamGate init(int x, int y, int w, int h, String text, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, OnPress press) {
      autismclient.gui.f.b.CachePool.Inner_DataUtil interactiveTone = tone == autismclient.gui.f.b.CachePool.Inner_DataUtil.init
         ? autismclient.gui.f.b.CachePool.Inner_DataUtil.load
         : tone;
      StreamGate button = new StreamGate(x, y, Math.max(1, w), h, Component.literal(this.init_Str_Str_I(text, w - 6)), interactiveTone, press);
      this.addRenderableWidget(button);
      return button;
   }

   private String init_Str_Str_I(String text, int width) {
      return autismclient.gui.f.b.k.load(
         this.font, autismclient.util.g.PacketUtil.update(text, 72), Math.max(1, width), init_CharEncoder.init_Identifier_l(l.read), set_I_2()
      );
   }

   private void clear_V() {
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

      this.setFocused(null);
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
      Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
      autismclient.gui.f.b.k.init(graphics, this.font, autismclient.util.g.PacketUtil.update(text, 120), fontId, color, x, y, false);
   }

   private void load(GuiGraphicsExtractor graphics, String text, int right, int y, int color) {
      Identifier fontId = init_CharEncoder.init_Identifier_l(l.read);
      int w = autismclient.gui.f.b.k.init(this.font, text, fontId, color);
      autismclient.gui.f.b.k.init(graphics, this.font, text, fontId, color, right - w, y, false);
   }

   private static int pop_I_2() {
      return ct.init_I_I_ct_DataUtil(-15987181, ct.Inner_DataUtil.update);
   }

   private static int tick_I_12() {
      return ct.init_I_I_ct_DataUtil(-401402080, ct.Inner_DataUtil.close);
   }

   private static int run_I_2() {
      return ct.init_I_I_ct_DataUtil(-1206840035, ct.Inner_DataUtil.close);
   }

   private static int get_I_2() {
      return ct.init_I_I_ct_DataUtil(ValueSink.update, ct.Inner_DataUtil.load);
   }

   private static int set_I_2() {
      return ct.init_I_I_ct_DataUtil(ValueSink.flush, ct.Inner_DataUtil.read);
   }

   private static int put_I() {
      return ct.init_I_I_ct_DataUtil(ValueSink.close, ct.Inner_DataUtil.read);
   }

   private autismclient.util.g.FieldCache load_FieldCache() {
      autismclient.util.g.FieldCache profile = new autismclient.util.g.FieldCache();
      profile.write_Str = autismclient.util.g.TaskTimer.init_TaskTimer().init_Str("New profile", profile.read_Str);
      profile.update_Str = this.init_Str;
      if (!autismclient.util.g.PacketUtil.init_Z_Str("default")) {
         profile.init_List.add(new autismclient.util.g.FieldCache.Inner_BufferHelper("default", ""));
      } else {
         for (autismclient.util.BitSetUtil account : autismclient.util.TokenStore.init_TokenStore().init_List()) {
            if (!autismclient.util.g.PacketUtil.init_Z_Str(account.write_Str())) {
               profile.init_List.add(new autismclient.util.g.FieldCache.Inner_BufferHelper(account.write_Str(), ""));
               break;
            }
         }
      }

      return profile;
   }

   private boolean init_Z_Str_3(String profileId) {
      if (profileId != null && autismclient.util.g.PacketUtil.init_PacketUtil().load_Z()) {
         autismclient.util.g.FieldCache active = autismclient.util.g.PacketUtil.init_PacketUtil().init_FieldCache();
         return active != null && profileId.equals(active.read_Str);
      } else {
         return false;
      }
   }

   private static int add_I() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private static int clear_I() {
      return add_I() + 2;
   }

   private static int reset() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private static int start() {
      return autismclient.gui.f.RenderState.init(12);
   }

   private int init_I_I_I_7(int y, int h) {
      return j.init(y, h, autismclient.gui.f.b.k.init(init_CharEncoder.init_Identifier_l(l.read)), 0);
   }

   private int stop() {
      return 6 + reset() + 2;
   }

   private int next() {
      int total = this.open + 4 + this.poll;
      return Math.max(6, (this.init_I() - total) / 2);
   }

   private int prev() {
      return this.next() + this.size() + 4;
   }

   private int valid() {
      return this.poll;
   }

   private int size() {
      return this.open;
   }

   private int data() {
      return this.prev() + 4;
   }

   private int value() {
      return Math.max(1, this.valid() - 8);
   }

   private boolean load_Z_3() {
      return this.load_I() < 300;
   }

   private int cache() {
      return this.stop() + this.push * clear_I() - 2;
   }

   private int state() {
      return this.push_I_2() + this.pop * clear_I() - 2;
   }

   private int queue() {
      return this.cache() + 4;
   }

   private int buffer() {
      return this.state() + 4;
   }

   private static List<String> init_List_9() {
      List<String> labels = new ArrayList<>(init_IArr.length);

      for (int option : init_IArr) {
         labels.add(option + "ms");
      }

      return labels;
   }

   private static int load_I_I_3(int value) {
      int index = 0;
      int best = 2147483647;

      for (int i = 0; i < init_IArr.length; i++) {
         int distance = Math.abs(init_IArr[i] - value);
         if (distance < best) {
            best = distance;
            index = i;
         }
      }

      return index;
   }

   private static int init_I_Str_I_2(String value, int fallback) {
      try {
         return Integer.parseInt(value.trim());
      } catch (RuntimeException ignored) {
         return fallback;
      }
   }

   private record Inner_DataUtil(String init_Str, String load, boolean init_Z) {
   }
}
