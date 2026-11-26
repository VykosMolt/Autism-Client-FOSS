package autismclient.util;

import autismclient.gui.f.b.h;
import autismclient.gui.f.b.j;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AutismQueueEditorOverlay extends bj {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private int flush_I = 100;
   private int close_I = 100;
   private int open_I = 248;
   private int poll_I = 176;
   private boolean write_Z = false;
   private double init_D = 0.0;
   private double load_D = 0.0;
   private int push = 0;
   private boolean update_Z = false;
   private boolean sync_Z = false;
   private boolean flush_Z = false;
   private int pop = -1;
   private boolean close_Z = false;
   private long init_J = 0L;
   private String init_Str_2 = "";
   private boolean open_Z = false;
   private boolean poll_Z = false;
   private int tick = 0;
   private final Font init_Font;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.f.b.CacheEntry init_CacheEntry = new autismclient.gui.f.b.CacheEntry();
   private List<ck.Inner_CachePool> init_List = new ArrayList<>();
   private final List<AutismQueueEditorOverlay.Inner_DataUtil> load_List = new ArrayList<>();
   private final List<AutismQueueEditorOverlay.Inner_StreamUtil> read_List = new ArrayList<>();
   private String load_Str = null;
   private int run = 0;
   private int get = 0;

   public AutismQueueEditorOverlay(Font textRenderer) {
      this.init_Font = textRenderer;
      this.open_I = this.add_I_16();
      this.poll_I = this.clear();
   }

   @Override
   protected boolean d_() {
      return true;
   }

   @Override
   public int init_I() {
      return this.add_I_16();
   }

   @Override
   public int load_I() {
      return this.reset();
   }

   @Override
   public boolean read_Z() {
      return true;
   }

   @Override
   protected int a__I() {
      return this.pop_I_23() + 3 * this.start_I_14() + this.queue();
   }

   @Override
   protected void c_() {
      if (!this.set_Z()) {
         this.open_I = this.add_I_16();
      }
   }

   @Override
   public cH init_cH() {
      return new cH(this.flush_I, this.close_I, this.open_I, this.poll_I, this.sync_Z, this.update_Z);
   }

   @Override
   public void init_V_cH(cH bounds) {
      if (bounds != null) {
         cH clamped = this.init_cH_cX_cH(this, bounds);
         this.flush_I = clamped.init_I;
         this.close_I = clamped.load_I;
         this.open_I = clamped.read;
         this.poll_I = clamped.write;
         this.sync_Z = clamped.init_Z;
         this.update_Z = clamped.load_Z;
      }
   }

   public boolean poll_Z_15() {
      return ck.init_ck().chunk();
   }

   @Override
   public void init_V_Z(boolean visible) {
      this.sync_Z = visible;
      if (visible) {
         this.push = 0;
         this.init_CacheEntry.load(0, 0);
         bk.init_bk().update_V(this);
      }

      this.update_V_20();
   }

   public void write_V_23() {
      this.init_V_Z(!this.sync_Z);
   }

   public void load(boolean configurationOnly) {
      this.flush_Z = configurationOnly;
   }

   @Override
   public boolean sync_Z() {
      return this.sync_Z;
   }

   @Override
   public boolean flush_Z() {
      return this.update_Z;
   }

   @Override
   public void write_V_Z(boolean collapsed) {
      if (this.update_Z != collapsed) {
         this.update_Z = collapsed;
         if (collapsed) {
            this.get_V();
         }

         this.a__V();
      }
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public boolean init_Z_D_D(double mouseX, double mouseY) {
      if (!this.sync_Z) {
         return false;
      }

      List<ck.Inner_CachePool> displayQueue = this.init_List.isEmpty() ? this.init_List_12() : this.init_List;
      int panelHeight = this.update_Z
         ? 14
         : this.init_cH_cX_cH(this, new cH(this.flush_I, this.close_I, this.open_I, this.init_I(displayQueue), this.sync_Z, this.update_Z)).write;
      return mouseX >= this.flush_I && mouseX <= this.flush_I + this.open_I && mouseY >= this.close_I && mouseY <= this.close_I + panelHeight;
   }

   @Override
   public boolean load_Z_D_D(double mouseX, double mouseY) {
      if (!this.sync_Z) {
         return false;
      }

      cH bounds = this.init_cH();
      return mouseX >= this.flush_I
         && mouseX <= this.flush_I + this.open_I
         && mouseY >= this.close_I
         && mouseY <= this.close_I + 14
         && !this.read_Z_D_2(mouseX, mouseY, bounds);
   }

   public void update_V_20() {
      ck shared = ck.init_ck();
      shared.start(this.sync_Z);
      shared.clear(this.flush_I);
      shared.reset(this.close_I);
      this.a__V();
   }

   public void sync_V_17() {
      ck shared = ck.init_ck();
      this.b__V();
      this.sync_Z = shared.value_Z();
      this.flush_I = shared.start_I();
      this.close_I = shared.stop_I();
   }

   private int push_I_23() {
      return this.close_I + this.pop_I_23();
   }

   private int pop_I_23() {
      int toolbarRows = aE.init_aE().write_Z() ? 4 : 3;
      return this.cache() + toolbarRows * this.next() + (toolbarRows - 1) * this.value() + this.state();
   }

   private int tick_I_21() {
      return this.flush_I + this.data();
   }

   private int run_I_21() {
      return autismclient.gui.f.c.NetHelper.init(this.open_I, this.data());
   }

   private int init_I(boolean hasScrollbar) {
      return Math.max(40, autismclient.gui.f.c.NetHelper.init(this.run_I_21(), hasScrollbar, this.size()));
   }

   private int get_I_19() {
      return this.run_I_21();
   }

   private int init_I_Str_5(String label, int minWidth, int maxWidth) {
      return autismclient.gui.f.c.NetHelper.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, label, 5, minWidth, maxWidth);
   }

   private int init_I(int panelHeight) {
      return Math.max(this.start_I_14() + 4, panelHeight - this.pop_I_23() - this.data());
   }

   private int load(int panelHeight) {
      return this.sync_I(Math.max(0, this.init_I(panelHeight) - 2), this.start_I_14());
   }

   private int set_I_19() {
      return this.push_I_23() - 1;
   }

   private int read(int panelHeight) {
      return this.set_I_19() + this.load(panelHeight);
   }

   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil(int panelHeight) {
      int visibleHeight = this.load(panelHeight);
      int contentHeight = this.init_List.size() * this.start_I_14();
      int maxScroll = Math.max(0, contentHeight - visibleHeight);
      return autismclient.gui.f.b.PoolEntry.init_PoolEntry_DataUtil_I_2(
         contentHeight,
         Math.max(1, visibleHeight),
         this.tick_I_21() + this.run_I_21() - 5,
         this.set_I_19(),
         4,
         Math.max(1, this.init_I(panelHeight) - 2),
         this.init_CacheEntry.init_I_F_I(0.0F, maxScroll)
      );
   }

   private int flush(int rowX, int rowWidth) {
      return rowX + rowWidth - this.stop() - this.data();
   }

   private int put() {
      return this.valid();
   }

   private int close(int rowX, int rowWidth) {
      return this.flush(rowX, rowWidth) - 4 - this.put();
   }

   private int open(int rowX, int rowWidth) {
      return Math.max(40, this.close(rowX, rowWidth) - 14 - 2 - (rowX + 8) - 6);
   }

   private int init_I(List<ck.Inner_CachePool> displayQueue) {
      int size = displayQueue.size();
      int rows = size == 0 ? 1 : Math.min(size, this.prev());
      return this.load_I_I_I(Math.max(this.load_I(), this.pop_I_23() + rows * this.start_I_14() + this.queue()), this.a__I());
   }

   private ck.Inner_CachePool init_ck_CachePool(List<ck.Inner_CachePool> queue) {
      if (this.pop == -1) {
         return null;
      }

      for (ck.Inner_CachePool packet : queue) {
         if (packet.init_I() == this.pop) {
            return packet;
         }
      }

      this.pop = -1;
      return null;
   }

   private void flush_V_16() {
      this.pop = -1;
   }

   private List<ck.Inner_CachePool> init_List_12() {
      ck shared = ck.init_ck();
      List<ck.Inner_CachePool> queue = shared.read_List();
      if (queue.isEmpty()) {
         queue = shared.load_List();
      }

      return queue;
   }

   private int init(GuiGraphicsExtractor context, int x, int y, int h, String label, boolean active, int mouseX, int mouseY, String tip, Runnable action) {
      int width = autismclient.gui.f.c.NetHelper.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, label, 5, 24, 96);
      autismclient.gui.f.b.PacketUtil.init(context, this.init_Font, x, y, width, h, label, active, "queue:auto:" + label + ":" + x + ":" + y, mouseX, mouseY);
      this.load_List.add(new AutismQueueEditorOverlay.Inner_DataUtil(x, y, width, h, action));
      this.init(x, y, width, h, mouseX, mouseY, tip);
      return x + width;
   }

   private int init(GuiGraphicsExtractor context, int x, int y, int h, String label, int mouseX, int mouseY, String tip, Runnable action) {
      int width = autismclient.gui.f.c.NetHelper.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, label, 5, 24, 96);
      autismclient.gui.f.b.PacketUtil.init(
         context, this.init_Font, x, y, width, h, label, autismclient.gui.f.b.ByteReader.Inner_StreamUtil.GHOST, true, mouseX, mouseY
      );
      this.load_List.add(new AutismQueueEditorOverlay.Inner_DataUtil(x, y, width, h, action));
      this.init(x, y, width, h, mouseX, mouseY, tip);
      return x + width;
   }

   private int load(GuiGraphicsExtractor context, int x, int y, int h, String label, boolean active, int mouseX, int mouseY, String tip, Runnable action) {
      int width = autismclient.gui.f.c.NetHelper.init(this.init_Font, this.init_CharEncoder, autismclient.gui.f.b.l.read, label, 5, 24, 112);
      autismclient.gui.f.b.PacketUtil.init(context, this.init_Font, x, y, width, h, label, active, "queue:status:" + label + ":" + x + ":" + y, mouseX, mouseY);
      this.load_List.add(new AutismQueueEditorOverlay.Inner_DataUtil(x, y, width, h, action));
      this.init(x, y, width, h, mouseX, mouseY, tip);
      return x + width;
   }

   private void init(
      GuiGraphicsExtractor context, int x, int y, int w, int h, String label, boolean active, int mouseX, int mouseY, String tip, Runnable action
   ) {
      this.init(context, x, y, w, h, label, autismclient.gui.f.b.ByteReader.Inner_StreamUtil.SECONDARY, active, mouseX, mouseY, tip, action);
   }

   private void init(
      GuiGraphicsExtractor context,
      int x,
      int y,
      int w,
      int h,
      String label,
      autismclient.gui.f.b.ByteReader.Inner_StreamUtil variant,
      boolean active,
      int mouseX,
      int mouseY,
      String tip,
      Runnable action
   ) {
      autismclient.gui.f.b.PacketUtil.init(context, this.init_Font, x, y, w, h, label, variant, active, mouseX, mouseY);
      if (active) {
         this.load_List.add(new AutismQueueEditorOverlay.Inner_DataUtil(x, y, w, h, action));
      }

      this.init(x, y, w, h, mouseX, mouseY, tip);
   }

   private void init(GuiGraphicsExtractor context, int x, int y, int w, int h, String label, int mouseX, int mouseY, String tip, Runnable action) {
      this.init(context, x, y, w, h, label, false, mouseX, mouseY, tip, action);
   }

   private void load(
      GuiGraphicsExtractor context, int x, int y, int w, int h, String label, boolean enabled, int mouseX, int mouseY, String tip, Runnable action
   ) {
      autismclient.gui.f.b.PacketUtil.init(context, this.init_Font, x, y, w, h, label, enabled, "queue:state:" + label + ":" + x + ":" + y, mouseX, mouseY);
      this.load_List.add(new AutismQueueEditorOverlay.Inner_DataUtil(x, y, w, h, action));
      this.init(x, y, w, h, mouseX, mouseY, tip);
   }

   private boolean init_Z(List<ck.Inner_CachePool> queue) {
      for (int i = 1; i < queue.size(); i++) {
         ck.Inner_CachePool previous = queue.get(i - 1);
         ck.Inner_CachePool current = queue.get(i);
         if (previous.load_I() > current.load_I()) {
            return false;
         }

         if (previous.load_I() == current.load_I() && previous.init_I() > current.init_I()) {
            return false;
         }
      }

      return true;
   }

   private String init_Str(List<ck.Inner_CachePool> queue) {
      if (queue.isEmpty()) {
         return "";
      }

      StringBuilder signature = new StringBuilder(queue.size() * 16);

      for (ck.Inner_CachePool packet : queue) {
         signature.append(packet.init_I()).append(':').append(packet.load_I()).append(';');
      }

      return signature.toString();
   }

   private void init_V(List<ck.Inner_CachePool> queue) {
      String signature = this.init_Str(queue);
      if (!signature.equals(this.init_Str_2)) {
         this.init_Str_2 = signature;
         this.init_J = System.currentTimeMillis();
         this.open_Z = true;
      }
   }

   private void init(ck shared) {
      List<ck.Inner_CachePool> delayedQueue = shared.load_List();
      this.init_V(delayedQueue);
      if (delayedQueue.size() >= 2 && this.open_Z && System.currentTimeMillis() - this.init_J >= 800L) {
         if (this.init_Z(delayedQueue)) {
            this.open_Z = false;
         } else {
            shared.write_V();
            this.init_Str_2 = this.init_Str(shared.load_List());
            this.open_Z = false;
            this.close_Z = true;
         }
      }
   }

   private void init_V_List_3(List<ck.Inner_CachePool> queue, ck.Inner_CachePool selectedPacket, int panelHeight) {
      if (selectedPacket != null && !queue.isEmpty()) {
         int selectedIndex = -1;

         for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).init_I() == selectedPacket.init_I()) {
               selectedIndex = i;
               break;
            }
         }

         if (selectedIndex >= 0) {
            int visibleHeight = this.load(panelHeight);
            int maxScroll = Math.max(0, queue.size() * this.start_I_14() - visibleHeight);
            int rowTop = selectedIndex * this.start_I_14();
            int rowBottom = rowTop + this.start_I_14();
            if (rowTop < this.push) {
               this.push = rowTop;
            } else if (rowBottom > this.push + visibleHeight) {
               this.push = rowBottom - visibleHeight;
            }

            this.push = this.init_I_I_3(this.push, this.start_I_14(), maxScroll);
         }
      }
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.sync_Z || this.a__Z()) {
         ck shared = ck.init_ck();
         List<ck.Inner_CachePool> staggeredQueue = shared.read_List();
         if (staggeredQueue.isEmpty()) {
            this.init(shared);
            this.init_List = shared.load_List();
         } else {
            this.init_List = new ArrayList<>(staggeredQueue);
         }

         ck.Inner_CachePool selectedPacket = this.init_ck_CachePool(this.init_List);
         cH clamped = this.init_cH_cX_cH(
            this, new cH(this.flush_I, this.close_I, this.init_I_I_I(this.open_I, this.init_I()), this.init_I(this.init_List), this.sync_Z, this.update_Z)
         );
         this.flush_I = clamped.init_I;
         this.close_I = clamped.load_I;
         this.open_I = clamped.read;
         int panelHeight = clamped.write;
         this.poll_I = panelHeight;
         if (!this.update_Z && this.close_Z) {
            this.init_V_List_3(this.init_List, selectedPacket, panelHeight);
            this.close_Z = false;
         }

         this.load_List.clear();
         this.read_List.clear();
         this.load_Str = null;
         String title = "Queue (" + this.init_List.size() + ")";
         cH bounds = new cH(this.flush_I, this.close_I, this.open_I, panelHeight, this.sync_Z, this.update_Z);
         this.init_V_GuiGraphicsExtract_6(context, mouseX, mouseY, bounds, title, this.update_Z, this.write_Z);
         this.init_V_GuiGraphicsExtract_13(context, mouseX, mouseY, bounds);
         boolean clipBody = this.init_Z_GuiGraphicsExtract(context, bounds, this.update_Z);
         if (!clipBody) {
            this.init_V_GuiGraphicsExtract_14(context, bounds, this.update_Z, this.write_Z);
         } else {
            try {
               int gap = this.value();
               int row1Y = this.close_I + this.cache();
               int row2Y = row1Y + this.next() + gap;
               int row3Y = row2Y + this.next() + gap;
               int row4Y = row3Y + this.next() + gap;
               int leftX = this.flush_I + this.data();
               int rowWidth = this.get_I_19();
               int rowRight = leftX + rowWidth;
               int bx = leftX;
               bx = this.init(context, bx, row1Y, this.next(), "Copy", mouseX, mouseY, "Copy queue to clipboard (serialized)", () -> {
                  MacroClipboardCodec.init_V(this.init_List_12());
                  bi.init("Queue copied.");
               }) + gap;
               bx = this.init(context, bx, row1Y, this.next(), "Paste", mouseX, mouseY, "Replace queue from clipboard", () -> {
                  if (ck.init_ck().read_List().isEmpty()) {
                     ck.Inner_CachePool.load_V();
                  }

                  List<ck.Inner_CachePool> newQueue = MacroClipboardCodec.read();
                  if (newQueue != null) {
                     ck.init_ck().load_V_List(newQueue);
                     this.flush_V_16();
                     this.push = 0;
                     AutismClientMessaging.sendPrefixed("§aQueue pasted!");
                  }
               }) + gap;
               bx = this.init(context, bx, row1Y, this.next(), "Dupe", mouseX, mouseY, "Duplicate all packets in the queue", () -> {
                  List<ck.Inner_CachePool> currentQueue = ck.init_ck().load_List();
                  if (!currentQueue.isEmpty()) {
                     List<ck.Inner_CachePool> duplicatedQueue = new ArrayList<>(currentQueue);

                     for (ck.Inner_CachePool packetx : currentQueue) {
                        duplicatedQueue.add(new ck.Inner_CachePool(packetx.init_Packet, packetx.load_I(), packetx.init_ck_BufferHelper()));
                     }

                     ck.init_ck().load_V_List(duplicatedQueue);
                     this.flush_V_16();
                     AutismClientMessaging.sendPrefixed("§aDuplicated " + currentQueue.size() + " packet(s).");
                  }
               }) + gap;
               bx = this.init(context, bx, row1Y, this.next(), "Ins", mouseX, mouseY, "Insert clipboard packets at the end of the queue", () -> {
                  List<ck.Inner_CachePool> newPackets = MacroClipboardCodec.read();
                  if (newPackets != null && !newPackets.isEmpty()) {
                     List<ck.Inner_CachePool> combinedQueue = new ArrayList<>(ck.init_ck().load_List());
                     combinedQueue.addAll(newPackets);
                     ck.init_ck().load_V_List(combinedQueue);
                     this.flush_V_16();
                     AutismClientMessaging.sendPrefixed("§aInserted " + newPackets.size() + " packet(s).");
                  }
               }) + gap;
               int replayWidth = Math.max(this.init_I_Str_5("Replay", 48, 84), rowRight - bx);
               this.init(
                  context,
                  bx,
                  row1Y,
                  replayWidth,
                  this.next(),
                  "Replay",
                  autismclient.gui.f.b.ByteReader.Inner_StreamUtil.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Restore the last flushed queue back into the editor",
                  () -> {
                     if (ck.init_ck().poll_Z()) {
                        this.flush_V_16();
                        this.push = 0;
                        AutismClientMessaging.sendPrefixed("§aRestored - packets will regenerate on send");
                     } else {
                        AutismClientMessaging.sendPrefixed("§cNo history");
                     }
                  }
               );
               boolean flushOnDelay = autismclient.modules.ModuleRuntimeManager.init_PoolEntry().stop_Z();
               boolean captureAsExact = autismclient.modules.ModuleRuntimeManager.init_PoolEntry().next_Z();
               int clearWidth = this.init_I_Str_5("Clear", 42, 58);
               int captureModeBtnWidth = this.next();
               int flushWidth = Math.max(this.init_I_Str_5("FLUSH ON DELAY", 110, 150), rowWidth - clearWidth - captureModeBtnWidth - gap - gap);
               this.load(
                  context,
                  leftX,
                  row2Y,
                  flushWidth,
                  this.next(),
                  "FLUSH ON DELAY",
                  flushOnDelay,
                  mouseX,
                  mouseY,
                  flushOnDelay
                     ? "When delay turns off, queued packets auto-flush"
                     : "When delay turns off, keep the queued packets until you flush or clear them",
                  () -> {
                     autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
                     boolean newValue = !module.stop_Z();
                     module.flush(newValue);
                     AutismClientMessaging.sendPrefixed(newValue ? "§aFlush on Delay ON" : "§eFlush on Delay OFF");
                  }
               );
               this.init(
                  context,
                  leftX + flushWidth + gap,
                  row2Y,
                  captureModeBtnWidth,
                  this.next(),
                  captureAsExact ? "E" : "R",
                  captureAsExact ? autismclient.gui.f.b.ByteReader.Inner_StreamUtil.DANGER : autismclient.gui.f.b.ByteReader.Inner_StreamUtil.SUCCESS,
                  true,
                  mouseX,
                  mouseY,
                  captureAsExact ? "Capture as Exact: new packets stored verbatim" : "Capture as Regenerate: new packets rewritten on send",
                  () -> {
                     autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
                     boolean nowExact = !module.next_Z();
                     module.close(nowExact);
                     ck.Inner_BufferHelper bulkMode = nowExact ? ck.Inner_BufferHelper.EXACT : ck.Inner_BufferHelper.REGENERATE;
                     int updated = 0;

                     for (ck.Inner_CachePool qp : ck.init_ck().load_List()) {
                        if (qp.init_ck_BufferHelper() != bulkMode) {
                           qp.init(bulkMode);
                           updated++;
                        }
                     }

                     AutismClientMessaging.sendPrefixed(
                        nowExact
                           ? "§cCapture mode: EXACT" + (updated > 0 ? " (" + updated + " queued updated)" : "")
                           : "§aCapture mode: REGENERATE" + (updated > 0 ? " (" + updated + " queued updated)" : "")
                     );
                  }
               );
               this.init(
                  context,
                  leftX + flushWidth + gap + captureModeBtnWidth + gap,
                  row2Y,
                  clearWidth,
                  this.next(),
                  "Clear",
                  autismclient.gui.f.b.ByteReader.Inner_StreamUtil.DANGER,
                  true,
                  mouseX,
                  mouseY,
                  "Remove all packets from the queue",
                  () -> {
                     int cleared = ck.init_ck().write_I();
                     this.flush_V_16();
                     this.push = 0;
                     autismclient.modules.ModuleRuntimeManager.init_PoolEntry().read(cleared);
                  }
               );
               boolean captureModeOn = ck.init_ck().chunk();
               String delayModeLabel = ck.init_ck().init_ck_DataUtil() == ck.Inner_DataUtil.TICKS ? "Ticks" : "Ms";
               int modeWidth = this.init_I_Str_5(delayModeLabel, 34, 44);
               int captureWidth = this.init_I_Str_5("Capture", 56, 72);
               int plus1Width = this.init_I_Str_5("+1", 22, 28);
               int plus10Width = this.init_I_Str_5("+10", 26, 34);
               int plus20Width = this.init_I_Str_5("+20", 26, 34);
               int sendWidth = Math.max(
                  this.init_I_Str_5("Send Q", 54, 82), rowWidth - modeWidth - captureWidth - plus1Width - plus10Width - plus20Width - gap * 5
               );
               this.init(
                  context,
                  leftX,
                  row3Y,
                  modeWidth,
                  this.next(),
                  delayModeLabel,
                  autismclient.gui.f.b.ByteReader.Inner_StreamUtil.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Toggle delay unit: Ticks or milliseconds",
                  () -> {
                     ck.init_ck().flush_V();
                     String newMode = ck.init_ck().init_ck_DataUtil() == ck.Inner_DataUtil.TICKS ? "Ticks" : "Ms";
                     AutismClientMessaging.sendPrefixed("§aDelay mode: " + newMode);
                  }
               );
               this.load(
                  context,
                  leftX + modeWidth + gap,
                  row3Y,
                  captureWidth,
                  this.next(),
                  "Capture",
                  captureModeOn,
                  mouseX,
                  mouseY,
                  "Record real timing between captured packets",
                  () -> {
                     boolean newCapture = !ck.init_ck().chunk();
                     ck.init_ck().cache(newCapture);
                     AutismClientMessaging.sendPrefixed(newCapture ? "§aCapture Real Delays ON" : "§eCapture Real Delays OFF");
                  }
               );
               int adjustX = leftX + modeWidth + gap + captureWidth + gap;
               this.init(
                  context,
                  adjustX,
                  row3Y,
                  plus1Width,
                  this.next(),
                  "+1",
                  autismclient.gui.f.b.ByteReader.Inner_StreamUtil.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Add +1 to the selected packet, or apply a +1 incremental pattern to the whole queue",
                  () -> this.init_V_2(1)
               );
               this.init(
                  context,
                  adjustX + plus1Width + gap,
                  row3Y,
                  plus10Width,
                  this.next(),
                  "+10",
                  autismclient.gui.f.b.ByteReader.Inner_StreamUtil.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Add +10 to the selected packet, or apply a +10 incremental pattern to the whole queue",
                  () -> this.init_V_2(10)
               );
               this.init(
                  context,
                  adjustX + plus1Width + gap + plus10Width + gap,
                  row3Y,
                  plus20Width,
                  this.next(),
                  "+20",
                  autismclient.gui.f.b.ByteReader.Inner_StreamUtil.GHOST,
                  true,
                  mouseX,
                  mouseY,
                  "Add +20 to the selected packet, or apply a +20 incremental pattern to the whole queue",
                  () -> this.init_V_2(20)
               );
               this.init(
                  context,
                  rowRight - sendWidth,
                  row3Y,
                  sendWidth,
                  this.next(),
                  this.flush_Z ? "In Game" : "Send Q",
                  autismclient.gui.f.b.ByteReader.Inner_StreamUtil.SUCCESS,
                  !this.flush_Z,
                  mouseX,
                  mouseY,
                  this.flush_Z ? "Join a world to send queued packets" : "Send all queued packets to the server",
                  () -> {
                     if (!this.flush_Z && init_Minecraft.getConnection() != null) {
                        int count = ck.init_ck().init_I_ClientPacketListen(init_Minecraft.getConnection());
                        autismclient.modules.ModuleRuntimeManager.init_PoolEntry().load(count);
                     }
                  }
               );
               boolean inLanSession = aE.init_aE().write_Z();
               if (inLanSession) {
                  int syncWidth = this.init_I_Str_5("Sync", 46, 58);
                  int syncExecWidth = Math.max(this.init_I_Str_5("Sync Exec", 92, 118), rowWidth - syncWidth - gap);
                  this.init(
                     context,
                     leftX,
                     row4Y,
                     syncWidth,
                     this.next(),
                     "Sync",
                     autismclient.gui.f.b.ByteReader.Inner_StreamUtil.GHOST,
                     !this.flush_Z,
                     mouseX,
                     mouseY,
                     this.flush_Z ? "Join a world to share packet queues" : "Broadcast this queue to all LAN peers",
                     this::close_V_14
                  );
                  this.init(
                     context,
                     leftX + syncWidth + gap,
                     row4Y,
                     syncExecWidth,
                     this.next(),
                     "Sync Exec",
                     autismclient.gui.f.b.ByteReader.Inner_StreamUtil.SUCCESS,
                     !this.flush_Z,
                     mouseX,
                     mouseY,
                     this.flush_Z ? "Join a world to execute packet queues" : "Tell LAN peers to execute their queues together",
                     () -> aE.init_aE().flush_V()
                  );
               }

               if (this.init_List.isEmpty()) {
                  cs.init(
                     context,
                     this.init_Font,
                     "Queue empty",
                     cs.Inner_DataUtil.MUTED,
                     this.flush_I + 10,
                     j.init(this.push_I_23(), this.start_I_14(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.write), this.init_CharEncoder.flush()),
                     false
                  );
               } else {
                  int visibleHeight = this.load(panelHeight);
                  int totalContentHeight = this.init_List.size() * this.start_I_14();
                  int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
                  this.push = this.init_I_I_3(this.push, this.start_I_14(), maxScroll);
                  this.init_CacheEntry.init_V_I_I(this.push, maxScroll);
                  int drawScroll = this.init_CacheEntry.init_I_F_I(delta, maxScroll);
                  int contentStartY = this.push_I_23();
                  int listX = this.tick_I_21();
                  int listY = contentStartY - 2;
                  int listWidth = this.run_I_21();
                  int listHeight = this.init_I(panelHeight);
                  int clipTop = this.set_I_19();
                  int clipBottom = this.read(panelHeight);
                  autismclient.gui.f.b.FileCache.init(context, listX, listY, listWidth, listHeight, selectedPacket != null);
                  autismclient.gui.f.b.PoolEntry.Inner_DataUtil scrollbarMetrics = this.init_PoolEntry_DataUtil(panelHeight);
                  int listContentWidth = this.init_I(scrollbarMetrics.init_Z());
                  autismclient.gui.f.HashUtil.init_HashUtil()
                     .init(context, autismclient.gui.f.NetHelper.init(listX + 1, clipTop, Math.max(0, listWidth - 2), Math.max(0, clipBottom - clipTop)));
                  int baseY = contentStartY - drawScroll;
                  int groupColorIndex = 0;
                  int lastDelay = -2147483648;

                  for (int i = 0; i < this.init_List.size(); i++) {
                     ck.Inner_CachePool packet = this.init_List.get(i);
                     int itemY = baseY + i * this.start_I_14();
                     int rowY = itemY - 1;
                     if (rowY + this.start_I_14() > clipTop && rowY < clipBottom) {
                        boolean grouped = false;
                        if (i > 0 && this.init_List.get(i - 1).load_I() == packet.load_I()) {
                           grouped = true;
                        }

                        if (i < this.init_List.size() - 1 && this.init_List.get(i + 1).load_I() == packet.load_I()) {
                           grouped = true;
                        }

                        int textColor = p.scan();
                        if (grouped) {
                           if (packet.load_I() != lastDelay) {
                              groupColorIndex++;
                           }

                           textColor = groupColorIndex % 2 == 0 ? -12268289 : -21948;
                        }

                        lastDelay = packet.load_I();
                        int rowX = listX + 1;
                        int rowW = Math.max(36, listContentWidth - 2);
                        boolean selected = selectedPacket != null && selectedPacket.init_I() == packet.init_I();
                        boolean hovered = mouseX >= rowX && mouseX < rowX + rowW && mouseY >= rowY && mouseY < rowY + this.start_I_14();
                        autismclient.gui.f.b.FileCache.init(
                           context,
                           this.init_Font,
                           "",
                           rowX,
                           rowY,
                           rowW,
                           this.start_I_14(),
                           hovered,
                           selected,
                           grouped ? autismclient.gui.f.b.FileCache.Inner_DataUtil.WARNING : autismclient.gui.f.b.FileCache.Inner_DataUtil.NORMAL
                        );
                        String label = "#" + packet.init_I() + " " + PacketNameResolver.init_Str_Packet(packet.init_Packet);
                        String delayText = String.valueOf(packet.load_I());
                        int labelMaxWidth = this.open(rowX, rowW);
                        String trimmed = autismclient.gui.f.b.k.init(
                                 this.init_Font, label, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), textColor
                              )
                              > labelMaxWidth
                           ? autismclient.gui.f.b.k.init(
                              this.init_Font,
                              label,
                              Math.max(1, labelMaxWidth),
                              this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                              textColor
                           )
                           : label;
                        int rowTextY = j.init(
                           rowY, this.start_I_14(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.flush()
                        );
                        autismclient.gui.f.b.k.init(
                           context,
                           this.init_Font,
                           trimmed,
                           this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                           selected ? p.table() : textColor,
                           rowX + 7,
                           rowTextY,
                           false
                        );
                        int delayBtnWidth = this.put();
                        int delayBtnX = this.close(rowX, rowW);
                        int buttonY = rowY + Math.max(0, (this.start_I_14() - this.stop()) / 2);
                        int modeBtnW = 14;
                        int modeBtnX = delayBtnX - modeBtnW - 2;
                        boolean exact = packet.init_Z();
                        autismclient.gui.f.b.PacketUtil.init(
                           context,
                           this.init_Font,
                           modeBtnX,
                           buttonY,
                           modeBtnW,
                           this.stop(),
                           exact ? "E" : "R",
                           exact ? autismclient.gui.f.b.ByteReader.Inner_StreamUtil.DANGER : autismclient.gui.f.b.ByteReader.Inner_StreamUtil.SUCCESS,
                           true,
                           mouseX,
                           mouseY
                        );
                        autismclient.gui.f.b.PacketUtil.init(
                           context,
                           this.init_Font,
                           delayBtnX,
                           buttonY,
                           delayBtnWidth,
                           this.stop(),
                           delayText,
                           autismclient.gui.f.b.ByteReader.Inner_StreamUtil.PRIMARY,
                           true,
                           mouseX,
                           mouseY
                        );
                        int removeBtnX = this.flush(rowX, rowW);
                        boolean removeHovered = mouseX >= removeBtnX
                           && mouseX < removeBtnX + this.stop()
                           && mouseY >= buttonY
                           && mouseY < buttonY + this.stop();
                        autismclient.gui.f.b.FileCache.init(context, removeBtnX, buttonY, this.stop(), removeHovered);
                        autismclient.gui.f.b.FileCache.init(context, rowX, rowY + this.start_I_14(), rowW);
                        this.read_List
                           .add(
                              new AutismQueueEditorOverlay.Inner_StreamUtil(
                                 packet, rowX, rowY, rowW, this.start_I_14(), delayBtnX, delayBtnWidth, removeBtnX, this.stop(), modeBtnX, modeBtnW
                              )
                           );
                     }
                  }

                  autismclient.gui.f.HashUtil.init_HashUtil().init(context);
                  autismclient.gui.f.b.PoolEntry.init(context, scrollbarMetrics, scrollbarMetrics.init(mouseX, mouseY), this.poll_Z);
               }
            } finally {
               this.init_V_GuiGraphicsExtract_Z(context, clipBody);
               this.init_V_GuiGraphicsExtract_14(context, bounds, this.update_Z, this.write_Z);
            }

            if (!this.update_Z && this.load_Str != null) {
               context.nextStratum();
               this.init_V_GuiGraphicsExtract_12(context, this.load_Str, this.run, this.get);
            }
         }
      }
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.sync_Z) {
         return false;
      }

      ck shared = ck.init_ck();
      List<ck.Inner_CachePool> displayQueue = this.init_List.isEmpty() ? this.init_List_12() : this.init_List;
      int panelHeight = this.update_Z
         ? 14
         : this.init_cH_cX_cH(this, new cH(this.flush_I, this.close_I, this.open_I, this.init_I(displayQueue), this.sync_Z, this.update_Z)).write;
      if (button == 0 && mouseX >= this.flush_I && mouseX <= this.flush_I + this.open_I && mouseY >= this.close_I && mouseY <= this.close_I + 14) {
         cH bounds = new cH(this.flush_I, this.close_I, this.open_I, panelHeight, this.sync_Z, this.update_Z);
         if (this.load_Z_D_2(mouseX, mouseY, bounds)) {
            this.pop_V();
            return true;
         } else if (this.write(mouseX, mouseY, bounds)) {
            this.init_V_Z(false);
            return true;
         } else {
            this.write_Z = true;
            this.init_D = mouseX - this.flush_I;
            this.load_D = mouseY - this.close_I;
            return true;
         }
      } else {
         if (this.update_Z) {
            return false;
         }

         if (button == 0) {
            for (AutismQueueEditorOverlay.Inner_DataUtil region : this.load_List) {
               if (region.init(mouseX, mouseY)) {
                  region.init_Runnable.run();
                  return true;
               }
            }
         }

         autismclient.gui.f.b.PoolEntry.Inner_DataUtil scrollbarMetrics = this.init_PoolEntry_DataUtil(panelHeight);
         if (button == 0 && scrollbarMetrics.init_Z() && scrollbarMetrics.init(mouseX, mouseY)) {
            this.poll_Z = true;
            this.tick = scrollbarMetrics.load(mouseX, mouseY) ? (int)Math.round(mouseY) - scrollbarMetrics.update() : scrollbarMetrics.sync() / 2;
            this.push = this.init_I_I_3(autismclient.gui.f.b.PoolEntry.init(scrollbarMetrics, mouseY, this.tick), this.start_I_14(), scrollbarMetrics.flush());
            this.init_CacheEntry.load(this.push, scrollbarMetrics.flush());
            return true;
         }

         for (AutismQueueEditorOverlay.Inner_StreamUtil region : this.read_List) {
            if (region.init(mouseX, mouseY)) {
               if (region.read(mouseX, mouseY) && button == 0) {
                  shared.init_V_ck_CachePool(region.init_ck_CachePool);
                  if (this.pop == region.init_ck_CachePool.init_I()) {
                     this.flush_V_16();
                  }

                  return true;
               }

               if (region.write(mouseX, mouseY) && button == 0) {
                  ck.Inner_BufferHelper next = region.init_ck_CachePool.init_Z() ? ck.Inner_BufferHelper.REGENERATE : ck.Inner_BufferHelper.EXACT;
                  region.init_ck_CachePool.init(next);
                  return true;
               }

               if (region.load(mouseX, mouseY)) {
                  if (button == 0) {
                     shared.init_V_ck_CachePool_I(region.init_ck_CachePool, region.init_ck_CachePool.load_I() + 1);
                     return true;
                  }

                  if (button == 1) {
                     shared.init_V_ck_CachePool_I(region.init_ck_CachePool, Math.max(0, region.init_ck_CachePool.load_I() - 1));
                     return true;
                  }
               }

               if (button == 0) {
                  this.pop = this.pop == region.init_ck_CachePool.init_I() ? -1 : region.init_ck_CachePool.init_I();
                  return true;
               }
            }
         }

         return this.init_Z_D_D(mouseX, mouseY);
      }
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.write_Z && button == 0) {
         cH nextBounds = this.init_cH_cX_cH(
            this, new cH((int)(mouseX - this.init_D), (int)(mouseY - this.load_D), this.open_I, this.poll_I, this.sync_Z, this.update_Z)
         );
         this.flush_I = nextBounds.init_I;
         this.close_I = nextBounds.load_I;
         return true;
      } else if (this.poll_Z && button == 0) {
         List<ck.Inner_CachePool> displayQueue = this.init_List.isEmpty() ? this.init_List_12() : this.init_List;
         int panelHeight = this.update_Z
            ? 14
            : this.init_cH_cX_cH(this, new cH(this.flush_I, this.close_I, this.open_I, this.init_I(displayQueue), this.sync_Z, this.update_Z)).write;
         autismclient.gui.f.b.PoolEntry.Inner_DataUtil scrollbarMetrics = this.init_PoolEntry_DataUtil(panelHeight);
         this.push = this.init_I_I_3(autismclient.gui.f.b.PoolEntry.init(scrollbarMetrics, mouseY, this.tick), this.start_I_14(), scrollbarMetrics.flush());
         this.init_CacheEntry.load(this.push, scrollbarMetrics.flush());
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      if (button == 0 && this.write_Z) {
         this.write_Z = false;
         this.update_V_20();
         return true;
      } else if (button == 0 && this.poll_Z) {
         this.poll_Z = false;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      if (this.sync_Z && !this.update_Z && this.init_Z_D_D(mouseX, mouseY)) {
         List<ck.Inner_CachePool> displayQueue = this.init_List.isEmpty() ? this.init_List_12() : this.init_List;
         int panelHeight = this.init_cH_cX_cH(this, new cH(this.flush_I, this.close_I, this.open_I, this.init_I(displayQueue), this.sync_Z, this.update_Z)).write;
         int visibleHeight = this.load(panelHeight);
         int maxScroll = Math.max(0, displayQueue.size() * this.start_I_14() - visibleHeight);
         if (maxScroll <= 0) {
            return false;
         }

         this.push = this.init_I_I_3(this.push - (int)(Math.signum(amount) * this.start_I_14()), this.start_I_14(), maxScroll);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      return false;
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      return false;
   }

   private void close_V_14() {
      if (!this.flush_Z) {
         aE sync = aE.init_aE();
         if (!sync.write_Z()) {
            AutismClientMessaging.sendPrefixed("§cNot in LAN session");
         } else {
            List<ck.Inner_CachePool> queue = this.init_List_12();
            if (queue.isEmpty()) {
               AutismClientMessaging.sendPrefixed("§cQueue is empty");
            } else {
               String queueData = MacroClipboardCodec.init_Str(queue);
               if (queueData == null) {
                  AutismClientMessaging.sendPrefixed("§cFailed to serialize queue");
               } else {
                  sync.write(queueData);
                  AutismClientMessaging.sendPrefixed("§aQueue sent to " + sync.write_I() + " clients");
               }
            }
         }
      }
   }

   private void init(int x, int y, int w, int h, int mx, int my, String tip) {
      if (mx >= x && mx < x + w && my >= y && my < y + h) {
         this.load_Str = tip;
         this.run = mx + 8;
         this.get = my + 12;
      }
   }

   private void init_V_GuiGraphicsExtract_12(GuiGraphicsExtractor ctx, String text, int x, int y) {
      h.init(autismclient.gui.f.UiHelper.init(ctx, this.init_Font, x, y), text, x, y, 200);
   }

   private void init_V_2(int increment) {
      List<ck.Inner_CachePool> queue = this.init_List_12();
      if (queue.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§cQueue is empty");
      } else if (this.pop != -1) {
         for (ck.Inner_CachePool packet : queue) {
            if (packet.init_I() == this.pop) {
               packet.init(packet.load_I() + increment);
               String modeStr = ck.init_ck().load_ck_DataUtil() == ck.Inner_DataUtil.TICKS ? "ticks" : "ms";
               AutismClientMessaging.sendPrefixed("§aPacket #" + packet.init_I() + " delay: " + packet.load_I() + " " + modeStr);
               return;
            }
         }

         this.flush_V_16();
         AutismClientMessaging.sendPrefixed("§cSelected packet not found");
      } else {
         for (int i = 0; i < queue.size(); i++) {
            queue.get(i).init(i * increment);
         }

         AutismClientMessaging.sendPrefixed("§aApplied +" + increment + " incremental delays");
      }
   }

   private int add_I_16() {
      return 248;
   }

   private int clear() {
      int toolbar = this.cache() + 3 * this.next() + 2 * this.value() + this.state();
      return toolbar + 3 * this.start_I_14() + this.queue();
   }

   private int reset() {
      return this.pop_I_23() + this.start_I_14() + this.queue();
   }

   private int start_I_14() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private int stop() {
      return 12;
   }

   private int next() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int prev() {
      return 12;
   }

   private int valid() {
      return 40;
   }

   private int size() {
      return 12;
   }

   private int data() {
      return 4;
   }

   private int value() {
      return 2;
   }

   private int cache() {
      return 18;
   }

   private int state() {
      return 4;
   }

   private int queue() {
      return 6;
   }

   private static class Inner_DataUtil {
      final int init_I;
      final int load;
      final int read;
      final int write;
      final Runnable init_Runnable;

      private Inner_DataUtil(int x, int y, int width, int height, Runnable action) {
         this.init_I = x;
         this.load = y;
         this.read = width;
         this.write = height;
         this.init_Runnable = action;
      }

      private boolean init(double mouseX, double mouseY) {
         return mouseX >= this.init_I && mouseX < this.init_I + this.read && mouseY >= this.load && mouseY < this.load + this.write;
      }
   }

   private static class Inner_StreamUtil {
      final ck.Inner_CachePool init_ck_CachePool;
      final int init_I;
      final int load;
      final int read;
      final int write;
      final int update;
      final int sync;
      final int flush;
      final int close;
      final int open;
      final int poll;

      private Inner_StreamUtil(
         ck.Inner_CachePool packet, int x, int y, int width, int height, int delayX, int delayWidth, int removeX, int removeWidth, int modeX, int modeWidth
      ) {
         this.init_ck_CachePool = packet;
         this.init_I = x;
         this.load = y;
         this.read = width;
         this.write = height;
         this.update = delayX;
         this.sync = delayWidth;
         this.flush = removeX;
         this.close = removeWidth;
         this.open = modeX;
         this.poll = modeWidth;
      }

      private boolean init(double mouseX, double mouseY) {
         return mouseX >= this.init_I && mouseX < this.init_I + this.read && mouseY >= this.load && mouseY < this.load + this.write;
      }

      private boolean load(double mouseX, double mouseY) {
         return this.init(mouseX, mouseY) && mouseX >= this.update && mouseX < this.update + this.sync;
      }

      private boolean read(double mouseX, double mouseY) {
         return this.init(mouseX, mouseY) && mouseX >= this.flush && mouseX < this.flush + this.close;
      }

      private boolean write(double mouseX, double mouseY) {
         return this.init(mouseX, mouseY) && mouseX >= this.open && mouseX < this.open + this.poll;
      }
   }
}
