package autismclient.util;

import autismclient.gui.f.b.j;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AutismPacketInspectOverlay extends bj {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final int flush_I = 0;
   private static final int close_I = 8;
   private final Font init_Font;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private boolean write_Z;
   private boolean update_Z;
   private boolean sync_Z;
   private double init_D;
   private double load_D;
   private int open = 120;
   private int poll = 50;
   private int push = 220;
   private int pop = 173;
   private bu.Inner_JsonUtil init_bu_JsonUtil;
   private AutismPacketLoggerOverlay.Inner_JsonUtil init_AutismPacketLogger;
   private List<AutismPacketInspectOverlay.Inner_BufferHelper> init_List = Collections.emptyList();
   private boolean flush_Z = true;
   private int tick = -1;
   private autismclient.gui.f.c.EventBus init_EventBus = null;
   private String init_Str_2 = "";
   private int run = 0;
   private AtomicReference<bu.Inner_SyncManager> init_AtomicReference;
   private bu.Inner_SyncManager init_bu_SyncManager;
   private CompletableFuture<?> init_CompletableFuture;
   private String load_Str = "";
   private boolean close_Z;
   private static final int get = 4;
   private static final int set = 128;
   private static final Map<String, Integer> init_Map = new LinkedHashMap<String, Integer>() {
      @Override
      protected boolean removeEldestEntry(Entry<String, Integer> eldest) {
         return this.size() > 128;
      }
   };

   public AutismPacketInspectOverlay(Font textRenderer) {
      this.init_Font = textRenderer;
      this.push = this.run_I_16();
      this.pop = this.get_I_14();
   }

   public void init_V_bu_JsonUtil(bu.Inner_JsonUtil inspection, AutismPacketLoggerOverlay.Inner_JsonUtil sourceEntry, int anchorX, int anchorY) {
      if (inspection != null) {
         bk manager = bk.init_bk();
         manager.init_V(this);
         this.open_V_9();
         this.update_V_15();
         this.init_bu_JsonUtil = inspection;
         this.init_AutismPacketLogger = sourceEntry;
         this.close_Z = false;
         this.write_Z = true;
         this.update_Z = false;
         this.init_Str_2 = this.init_Str_bu_JsonUtil_AutismPacketLogger(inspection, sourceEntry);
         this.run = init_Map.getOrDefault(this.init_Str_2, 0);
         this.init_EventBus = null;
         this.flush_Z = true;
         this.sync_V_13();
         if (sourceEntry != null && this.init_bu_SyncManager != null) {
            this.init_bu_JsonUtil = this.init_bu_JsonUtil();
         }

         this.read_V_I_I_3(anchorX, anchorY);
         manager.update_V(this);
      }
   }

   public void init_V_AutismPacketLogger(AutismPacketLoggerOverlay.Inner_JsonUtil sourceEntry, int anchorX, int anchorY) {
      if (sourceEntry != null) {
         bk manager = bk.init_bk();
         manager.init_V(this);
         this.open_V_9();
         this.update_V_15();
         this.init_AutismPacketLogger = sourceEntry;
         this.close_Z = false;
         this.write_Z = true;
         this.update_Z = false;
         this.init_Str_2 = this.init_Str_bu_JsonUtil_AutismPacketLogger(null, sourceEntry);
         this.run = init_Map.getOrDefault(this.init_Str_2, 0);
         this.init_EventBus = null;
         this.flush_Z = true;
         this.sync_V_13();
         this.init_bu_JsonUtil = this.init_bu_JsonUtil();
         this.read_V_I_I_3(anchorX, anchorY);
         manager.update_V(this);
      }
   }

   public void write_V_17() {
      this.open_V_9();
      this.write_Z = false;
      this.sync_Z = false;
      this.init_AutismPacketLogger = null;
      this.update_V_15();
   }

   private void update_V_15() {
      this.load_Str = "";
      this.init_AtomicReference = null;
      this.init_bu_SyncManager = null;
      this.close_Z = false;
      if (this.init_CompletableFuture != null) {
         this.init_CompletableFuture.cancel(true);
         this.init_CompletableFuture = null;
      }
   }

   private void sync_V_13() {
      if (this.init_AutismPacketLogger != null) {
         AutismPayloadSupport.Inner_StreamUtil snapshot = AutismPayloadSupport.init_AutismPayloadSuppo(this.init_AutismPacketLogger);
         if (snapshot != null && (!this.close_Z || this.init_CompletableFuture == null || this.init_CompletableFuture.isDone())) {
            this.close_Z = true;
            String baseKey = this.init_Str_bu_JsonUtil_AutismPacketLogger(null, this.init_AutismPacketLogger);
            this.init_bu_SyncManager = bu.init_bu_SyncManager_AutismPayloadSuppo(snapshot);
            this.init_AtomicReference = new AtomicReference<>(this.init_bu_SyncManager);
            this.load_Str = baseKey;
            this.init_V_AutismPayloadSuppo_Str(snapshot, baseKey);
         }
      }
   }

   private void init_V_AutismPayloadSuppo_Str(AutismPayloadSupport.Inner_StreamUtil snapshot, String analysisKey) {
      if (snapshot != null && analysisKey != null && !analysisKey.isBlank()) {
         this.init_CompletableFuture = CompletableFuture.runAsync(
            () -> {
               try {
                  bu.Inner_SyncManager result = bu.init_bu_SyncManager_AutismPayloadSuppo_Consumer(
                     snapshot, view -> this.init_V_Str_bu_SyncManager(analysisKey, view)
                  );
                  this.init_V_Str_bu_SyncManager(analysisKey, result);
               } catch (Throwable t) {
                  this.init_V_Str_bu_SyncManager(analysisKey, bu.init_bu_SyncManager_Str(AutismPayloadSupport.init(t)));
               }
            }
         );
      }
   }

   private void init_V_Str_bu_SyncManager(String analysisKey, bu.Inner_SyncManager view) {
      AtomicReference<bu.Inner_SyncManager> ref = this.init_AtomicReference;
      if (ref != null && view != null && analysisKey.equals(this.load_Str)) {
         ref.set(view);
      }
   }

   private void flush_V_12() {
      if (this.init_AtomicReference != null && this.init_AutismPacketLogger != null) {
         bu.Inner_SyncManager next = this.init_AtomicReference.get();
         if (next != null && next != this.init_bu_SyncManager) {
            int oldScroll = this.init_EventBus != null ? this.init_EventBus.open() : this.run;
            this.init_bu_SyncManager = next;
            this.init_bu_JsonUtil = this.init_bu_JsonUtil();
            this.run = Math.max(0, oldScroll);
            this.flush_Z = true;
            if (this.init_EventBus != null) {
               this.init_EventBus.load_V(this.run);
            }
         }
      }
   }

   private bu.Inner_JsonUtil init_bu_JsonUtil() {
      return this.init_AutismPacketLogger == null
         ? this.init_bu_JsonUtil
         : bu.init_bu_JsonUtil_AutismPacketLogger(this.init_AutismPacketLogger, this.init_bu_SyncManager, true);
   }

   private void load(boolean keepScroll) {
      this.open_V_9();
      int oldScroll = keepScroll && this.init_EventBus != null ? this.init_EventBus.open() : 0;
      this.init_Str_2 = this.init_Str_bu_JsonUtil_AutismPacketLogger(null, this.init_AutismPacketLogger);
      this.run = keepScroll ? Math.max(0, oldScroll) : init_Map.getOrDefault(this.init_Str_2, 0);
      this.init_EventBus = null;
      this.init_List = Collections.emptyList();
      this.flush_Z = true;
      this.init_bu_JsonUtil = this.init_bu_JsonUtil();
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.write_Z || this.a__Z()) {
         this.flush_V_12();
         cH bounds = this.init_cH();
         String title = this.init_bu_JsonUtil == null ? "Packet Inspect" : this.init_bu_JsonUtil.init_Str();
         this.init_V_GuiGraphicsExtract_6(context, mouseX, mouseY, bounds, title, this.update_Z, this.sync_Z);
         boolean clipBody = this.init_Z_GuiGraphicsExtract(context, bounds, this.update_Z);
         if (!clipBody) {
            this.init_V_GuiGraphicsExtract_14(context, bounds, this.update_Z, this.sync_Z);
         } else {
            try {
               int listX = this.open + this.put();
               int tabsHeight = this.value();
               int tabsY = this.poll + 14 + this.size();
               if (tabsHeight > 0) {
                  this.init(context, listX, tabsY, this.push - this.put() * 2, mouseX, mouseY);
               }

               int progressHeight = this.data();
               int progressY = tabsY + tabsHeight;
               if (progressHeight > 0) {
                  this.init(context, listX, progressY, this.push - this.put() * 2, progressHeight, this.init_bu_SyncManager);
               }

               int listY = progressY + progressHeight;
               int listWidth = this.push - this.put() * 2;
               int listHeight = Math.max(1, this.pop - 14 - this.set_I_14() - this.state() - tabsHeight - progressHeight);
               int innerX = listX + this.add_I_12();
               int innerY = listY + this.add_I_12();
               int innerWidth = autismclient.gui.f.b.MapStore.init(listWidth, this.add_I_12());
               int innerHeight = listHeight - this.add_I_12() * 2;
               int lineH = this.valid();
               if (!bk.init_bk().update_Z(this) && !bk.init_bk().write_Z(this)) {
                  boolean var32 = false;
               } else {
                  boolean footerY = true;
               }

               this.init_V(innerWidth);
               if (this.init_List.isEmpty()) {
                  autismclient.gui.f.b.FileCache.init(context, this.init_Font, "No inspection data.", listX, listY, listWidth);
               } else {
                  int contentHeight = this.init_List.size() * lineH;
                  if (this.init_EventBus == null
                     || this.init_EventBus.init_I() != listX
                     || this.init_EventBus.load_I() != listY
                     || this.init_EventBus.read_I() != listWidth
                     || this.init_EventBus.write_I() != listHeight) {
                     int previousScrollOffset = this.init_EventBus != null ? this.init_EventBus.open() : this.run;
                     this.init_EventBus = new autismclient.gui.f.c.EventBus(listX, listY, listWidth, listHeight, lineH, 4);
                     this.init_EventBus.init_V(contentHeight);
                     this.init_EventBus.load_V(previousScrollOffset);
                     this.run = this.init_EventBus.open();
                  }

                  this.init_EventBus.init_V(contentHeight);
                  if (this.run > 0 && this.init_EventBus.open() == 0) {
                     this.init_EventBus.load_V(this.run);
                  }

                  this.init_EventBus.init_V_GuiGraphicsExtract_2(context, this.init_CharEncoder.set(), this.init_CharEncoder.reset());

                  try {
                     this.init_EventBus
                        .init_V_GuiGraphicsExtract_3(
                           context,
                           this.init_List.size(),
                           (idx, bnd) -> {
                              AutismPacketInspectOverlay.Inner_BufferHelper ln = this.init_List.get(idx);
                              this.init_V_GuiGraphicsExtract_8(
                                 context,
                                 ln,
                                 innerX,
                                 j.init(bnd.load, lineH, this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.flush())
                              );
                           }
                        );
                  } finally {
                     this.init_EventBus.init(context);
                  }

                  this.init_EventBus.init_V_GuiGraphicsExtract(context, mouseX, mouseY);
               }

               int footerY = this.poll + this.pop - this.set_I_14() + this.queue();
               autismclient.gui.f.b.k.init(
                  context,
                  this.init_Font,
                  this.init_List.size() + " lines",
                  this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                  this.init_CharEncoder.load(autismclient.gui.f.b.l.write),
                  this.open + this.put() + 5,
                  j.init(footerY, this.reset(), this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.write), this.init_CharEncoder.flush()),
                  false
               );

               for (AutismPacketInspectOverlay.Inner_CachePool button : this.init_List(footerY)) {
                  autismclient.gui.f.b.ByteReader.Inner_StreamUtil var10000;
                  switch (button.init_AutismPacketInspec()) {
                     case WAIT:
                     case QUEUE:
                     case ADD_PAYLOAD:
                        var10000 = autismclient.gui.f.b.ByteReader.Inner_StreamUtil.SECONDARY;
                        break;
                     case SEND:
                        var10000 = autismclient.gui.f.b.ByteReader.Inner_StreamUtil.SUCCESS;
                        break;
                     case EDIT_PAYLOAD:
                        var10000 = autismclient.gui.f.b.ByteReader.Inner_StreamUtil.PRIMARY;
                        break;
                     case COPY_RAW:
                        var10000 = autismclient.gui.f.b.ByteReader.Inner_StreamUtil.PRIMARY;
                        break;
                     case COPY:
                        var10000 = autismclient.gui.f.b.ByteReader.Inner_StreamUtil.GHOST;
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  autismclient.gui.f.b.ByteReader.Inner_StreamUtil variant = var10000;
                  autismclient.gui.f.b.PacketUtil.init(
                     context, this.init_Font, button.init_I(), button.load(), button.read(), this.reset(), button.init_Str(), variant, true, mouseX, mouseY
                  );
               }
            } finally {
               this.init_V_GuiGraphicsExtract_Z(context, clipBody);
               this.init_V_GuiGraphicsExtract_14(context, bounds, this.update_Z, this.sync_Z);
            }
         }
      }
   }

   private void init_V(int innerWidth) {
      if (this.flush_Z || this.tick != innerWidth) {
         if (this.init_bu_JsonUtil == null) {
            this.init_List = Collections.emptyList();
            this.flush_Z = false;
            this.tick = innerWidth;
         } else {
            List<AutismPacketInspectOverlay.Inner_BufferHelper> wrapped = new ArrayList<>();

            for (bu.Inner_TaskQueue line : this.init_bu_JsonUtil.init_List()) {
               this.init_V_bu_TaskQueue(line, innerWidth, wrapped);
            }

            this.init_List = wrapped;
            this.flush_Z = false;
            this.tick = innerWidth;
         }
      }
   }

   private void init_V_bu_TaskQueue(bu.Inner_TaskQueue line, int maxWidth, List<AutismPacketInspectOverlay.Inner_BufferHelper> target) {
      String text = line.init_Str();
      if (text == null || text.isEmpty()) {
         target.add(AutismPacketInspectOverlay.Inner_BufferHelper.init("", 0, line.init_I()));
      } else if (this.init_Z_2(text)) {
         this.init_V_Str_4(text, 0, line.init_I(), maxWidth, target);
      } else {
         int leadingSpaces = this.init_I_3(text);
         String indentComponent = " ".repeat(leadingSpaces);
         int indentWidth = this.load(indentComponent);
         int colonIndex = text.indexOf(58, leadingSpaces);
         if (colonIndex > leadingSpaces && colonIndex < text.length() - 1) {
            String label = text.substring(leadingSpaces, colonIndex + 1).trim();
            String value = text.substring(colonIndex + 1).stripLeading();
            String prefix = indentComponent + label + " ";
            int prefixWidth = this.load(prefix);
            int continuationOffset = Math.min(prefixWidth, indentWidth + this.start_I_11());
            this.init(prefix, value, line.init_I(), prefixWidth, continuationOffset, maxWidth, target);
         } else {
            this.init_V_Str_4(text.substring(leadingSpaces), indentWidth, line.init_I(), maxWidth, target);
         }
      }
   }

   private void init(
      String prefix,
      String value,
      int valueColor,
      int prefixWidth,
      int continuationOffset,
      int maxWidth,
      List<AutismPacketInspectOverlay.Inner_BufferHelper> target
   ) {
      int safeContinuationOffset = autismclient.gui.f.b.MapStore.load(maxWidth, continuationOffset);
      if (prefixWidth >= maxWidth) {
         this.init_V_Str_4(prefix.stripTrailing(), 0, p.pool(), maxWidth, target);
         this.init_V_Str_4(value, safeContinuationOffset, valueColor, maxWidth, target);
      } else if (value != null && !value.isEmpty()) {
         String remaining = value;

         for (boolean firstLine = true; !remaining.isEmpty(); firstLine = false) {
            int offset = firstLine ? prefixWidth : safeContinuationOffset;
            int availableWidth = autismclient.gui.f.b.MapStore.read(maxWidth, offset);
            String current = remaining;
            int split = autismclient.gui.f.ConfigStore.init(
               current, 0, current.length(), availableWidth, (start, end) -> this.load(current.substring(start, end))
            );
            String part = remaining.substring(0, split).stripTrailing();
            target.add(new AutismPacketInspectOverlay.Inner_BufferHelper(firstLine ? prefix : null, p.pool(), part, valueColor, offset));
            remaining = remaining.substring(split).stripLeading();
         }
      } else {
         target.add(new AutismPacketInspectOverlay.Inner_BufferHelper(prefix, p.pool(), "", valueColor, prefixWidth));
      }
   }

   private void init_V_Str_4(String text, int offset, int color, int maxWidth, List<AutismPacketInspectOverlay.Inner_BufferHelper> target) {
      if (text != null) {
         if (text.isEmpty()) {
            target.add(AutismPacketInspectOverlay.Inner_BufferHelper.init("", offset, color));
         } else {
            int safeOffset = autismclient.gui.f.b.MapStore.load(maxWidth, offset);
            String remaining = text;

            while (!remaining.isEmpty()) {
               int availableWidth = autismclient.gui.f.b.MapStore.read(maxWidth, safeOffset);
               String current = remaining;
               int split = autismclient.gui.f.ConfigStore.init(
                  current, 0, current.length(), availableWidth, (start, end) -> this.load(current.substring(start, end))
               );
               String part = remaining.substring(0, split).stripTrailing();
               target.add(AutismPacketInspectOverlay.Inner_BufferHelper.init(part, safeOffset, color));
               remaining = remaining.substring(split).stripLeading();
            }
         }
      }
   }

   private void init_V_GuiGraphicsExtract_8(GuiGraphicsExtractor context, AutismPacketInspectOverlay.Inner_BufferHelper line, int x, int y) {
      if (line != null && line.load_Str() != null) {
         if (line.init_Str() != null && !line.init_Str().isEmpty()) {
            autismclient.gui.f.b.k.init(
               context, this.init_Font, line.init_Str(), this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), line.init_I(), x, y, false
            );
         }

         int valueX = x + line.read();
         autismclient.gui.f.b.k.init(
            context, this.init_Font, line.load_Str(), this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), line.load_I(), valueX, y, false
         );
      }
   }

   private void init(GuiGraphicsExtractor context, int x, int y, int width, int height, bu.Inner_SyncManager view) {
      if (view != null && width > 0 && height > 0) {
         int fill = -1441722090;
         autismclient.gui.f.PacketUtil.init(context, autismclient.gui.f.NetHelper.init(x, y, width, height - 2), fill, -2007870113);
         String status = view.init_Str() != null && !view.init_Str().isBlank() ? view.init_Str() : "Decoding";
         int percent = Math.max(0, Math.min(100, (int)Math.round(view.init_D() * 100.0)));
         String label = "Analyzer: " + status + " - progress " + percent + "%";
         autismclient.gui.f.b.k.init(
            context,
            this.init_Font,
            label,
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
            view.load() ? p.valid() : p.parse(),
            x + 4,
            y + 2,
            false
         );
         int barX = x + 4;
         int barY = y + height - 8;
         int barWidth = Math.max(1, width - 8);
         int barHeight = 4;
         autismclient.gui.f.b.NodeWalker.init(
            autismclient.gui.f.UiHelper.init(context, this.init_Font, -1, -1),
            autismclient.gui.f.NetHelper.init(barX, barY, barWidth, barHeight),
            view.init_D()
         );
      }
   }

   private void init(GuiGraphicsExtractor context, int x, int y, int width, int mouseX, int mouseY) {
   }

   private boolean init_Z_2(String text) {
      String trimmed = text == null ? "" : text.trim();
      return trimmed.startsWith("[") && trimmed.endsWith("]");
   }

   private int init_I_3(String text) {
      int count = 0;

      while (count < text.length() && text.charAt(count) == ' ') {
         count++;
      }

      return count;
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.write_Z) {
         return false;
      }

      cH bounds = this.init_cH();
      if (button == 0 && mouseY >= this.poll && mouseY <= this.poll + 14 && mouseX >= this.open && mouseX <= this.open + this.push) {
         if (this.write(mouseX, mouseY, bounds)) {
            this.write_V_17();
            return true;
         } else {
            this.sync_Z = true;
            this.init_D = mouseX - this.open;
            this.load_D = mouseY - this.poll;
            return true;
         }
      } else {
         if (this.update_Z) {
            return true;
         }

         int tabsHeight = this.value();
         int progressHeight = this.data();
         int listY = this.poll + 14 + this.size() + tabsHeight + progressHeight;
         int listHeight = Math.max(1, this.pop - 14 - this.set_I_14() - this.state() - tabsHeight - progressHeight);
         int listX = this.open + this.put();
         int listWidth = this.push - this.put() * 2;
         if (button == 0
            && this.init_EventBus != null
            && this.init_EventBus.init_Z_D_D(mouseX, mouseY)
            && this.init_EventBus.init_Z_D_2(mouseX, mouseY, button)) {
            return true;
         }

         int footerY = this.poll + this.pop - this.set_I_14() + this.queue();
         if (button == 0) {
            for (AutismPacketInspectOverlay.Inner_CachePool footerButton : this.init_List(footerY)) {
               if (mouseX >= footerButton.init_I()
                  && mouseX < footerButton.init_I() + footerButton.read()
                  && mouseY >= footerButton.load()
                  && mouseY < footerButton.load() + this.reset()) {
                  this.init_V(footerButton.init_AutismPacketInspec());
                  return true;
               }
            }
         }

         return this.init_Z_D_D(mouseX, mouseY);
      }
   }

   @Override
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      if (button == 0 && this.sync_Z) {
         this.sync_Z = false;
         this.open_V_9();
         this.a__V();
         return true;
      } else if (button == 0 && this.init_EventBus != null) {
         this.init_EventBus.read_V();
         this.open_V_9();
         return true;
      } else {
         this.sync_Z = false;
         return false;
      }
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.init_EventBus != null) {
         this.init_EventBus.init_V_D_D(mouseX, mouseY);
         this.open_V_9();
      }

      if (this.write_Z && button == 0 && this.sync_Z) {
         this.init_V_cH(new cH((int)Math.round(mouseX - this.init_D), (int)Math.round(mouseY - this.load_D), this.push, this.pop, this.write_Z, this.update_Z));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      if (this.write_Z && !this.update_Z) {
         int tabsHeight = this.value();
         int progressHeight = this.data();
         int listY = this.poll + 14 + this.size() + tabsHeight + progressHeight;
         int listHeight = Math.max(1, this.pop - 14 - this.set_I_14() - this.state() - tabsHeight - progressHeight);
         int listX = this.open + this.put();
         int listWidth = this.push - this.put() * 2;
         if (mouseX < listX || mouseX > listX + listWidth || mouseY < listY || mouseY > listY + listHeight) {
            return false;
         } else if (this.init_EventBus != null) {
            boolean handled = this.init_EventBus.init_Z_D(mouseX, mouseY, amount);
            this.open_V_9();
            return handled;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (!this.write_Z) {
         return false;
      } else if (keyCode == 256) {
         this.write_V_17();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      return false;
   }

   @Override
   public boolean sync_Z() {
      return this.write_Z;
   }

   @Override
   public void init_V_Z(boolean visible) {
      this.write_Z = visible;
      if (!visible) {
         this.write_V_17();
      }
   }

   @Override
   public boolean init_Z_D_D(double mouseX, double mouseY) {
      if (!this.write_Z) {
         return false;
      }

      int height = this.update_Z ? 14 : this.pop;
      return mouseX >= this.open && mouseX <= this.open + this.push && mouseY >= this.poll && mouseY <= this.poll + height;
   }

   @Override
   public boolean load_Z_D_D(double mouseX, double mouseY) {
      return !this.write_Z
         ? false
         : mouseX >= this.open
            && mouseX <= this.open + this.push
            && mouseY >= this.poll
            && mouseY <= this.poll + 14
            && !this.read_Z_D_2(mouseX, mouseY, this.init_cH());
   }

   @Override
   public int b__I() {
      return 12;
   }

   @Override
   public boolean flush_Z() {
      return this.update_Z;
   }

   @Override
   public void write_V_Z(boolean collapsed) {
      if (this.update_Z != collapsed) {
         this.update_Z = collapsed;
         this.sync_Z = false;
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
   public cH init_cH() {
      return new cH(this.open, this.poll, this.push, this.pop, this.write_Z, this.update_Z);
   }

   @Override
   public void init_V_cH(cH bounds) {
      if (bounds != null) {
         cH clamped = this.init_cH_cX_cH(this, bounds);
         boolean sizeChanged = this.push != clamped.read || this.pop != clamped.write;
         this.open = clamped.init_I;
         this.poll = clamped.load_I;
         this.push = clamped.read;
         this.pop = clamped.write;
         this.write_Z = clamped.init_Z;
         this.update_Z = clamped.load_Z;
         if (sizeChanged) {
            this.flush_Z = true;
         }
      }
   }

   @Override
   public int init_I() {
      return this.pop_I_18();
   }

   @Override
   public int load_I() {
      return this.tick_I_16();
   }

   private void init_V(AutismPacketInspectOverlay.Inner_StreamUtil action) {
      if (action != null) {
         switch (action) {
            case WAIT:
               bs.poll(this.init_AutismPacketLogger);
               break;
            case SEND:
               this.init_V(this.init_AutismPacketLogger);
               break;
            case QUEUE:
               bs.close(this.init_AutismPacketLogger);
               break;
            case EDIT_PAYLOAD:
               bs.push(this.init_AutismPacketLogger);
               break;
            case ADD_PAYLOAD:
               bs.pop(this.init_AutismPacketLogger);
               break;
            case COPY_RAW:
               this.close_V_11();
               break;
            case COPY:
               if (this.init_bu_JsonUtil != null) {
                  init_Minecraft.keyboardHandler.setClipboard(this.init_bu_JsonUtil.load());
                  bi.init("Copied packet inspection.");
               }
         }
      }
   }

   private void close_V_11() {
      String export = this.init_Str(this.init_AutismPacketLogger);
      if (export != null && !export.isBlank()) {
         init_Minecraft.keyboardHandler.setClipboard(export);
         bi.init("Copied raw packet bytes.");
      } else {
         bi.read("No raw packet bytes were captured.");
      }
   }

   private String init_Str(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      if (entry == null) {
         return "";
      }

      AutismPayloadSupport.Inner_StreamUtil payload = AutismPayloadSupport.init_AutismPayloadSuppo(entry);
      bn.Inner_StreamUtil packet = bn.init(entry.init_Packet);
      byte[] payloadBytes = payload == null ? new byte[0] : payload.init_BArr();
      byte[] packetBytes = packet == null ? new byte[0] : packet.init_BArr();
      if (payloadBytes.length == 0 && packetBytes.length == 0) {
         return "";
      }

      StringBuilder out = new StringBuilder(Math.max(256, (payloadBytes.length + packetBytes.length) * 4));
      out.append("# AUTISM Packet Raw Export\n");
      out.append("Name: ").append(entry.load_Str).append('\n');
      out.append("Direction: ").append(entry.init_Str).append('\n');
      out.append("Class: ").append(entry.init_Class == null ? "unknown" : entry.init_Class.getName()).append('\n');
      out.append("Tick: ").append(entry.init_I).append('\n');
      out.append("Time: ").append(Instant.ofEpochMilli(entry.load_J)).append('\n');
      if (payload != null) {
         out.append("Payload Channel: ").append(payload.load_Str()).append('\n');
         out.append("Payload Protocol: ").append(payload.update()).append('\n');
         out.append("Payload Class: ").append(payload.write()).append('\n');
         out.append("Payload Packet ID: ").append(payload.load_I()).append('\n');
      }

      if (packet != null) {
         out.append("Captured Protocol: ").append(packet.load()).append('\n');
         out.append("Captured Packet ID: ").append(packet.init_I()).append('\n');
         out.append("Captured Packet Type: ").append(packet.write()).append('\n');
      }

      String decoded = bh.init_Str_AutismPacketLogger(entry);
      if (decoded != null && !decoded.isBlank()) {
         out.append('\n').append(decoded).append('\n');
      }

      this.init_V_StringBuilder(out, "Payload Body", payloadBytes);
      this.init_V_StringBuilder(out, "Plaintext Packet", packetBytes);
      return out.toString();
   }

   private void init_V_StringBuilder(StringBuilder out, String title, byte[] bytes) {
      if (out != null && bytes != null && bytes.length != 0) {
         out.append('\n').append('[').append(title).append("]\n");
         out.append("Length: ").append(bytes.length).append(" bytes\n");
         out.append("Hex: ").append(this.init_Str_BArr_Z(bytes, false)).append('\n');
         out.append("Hex Spaced:\n").append(this.init_Str_BArr_I(bytes, 32)).append('\n');
         out.append("Base64: ").append(Base64.getEncoder().encodeToString(bytes)).append('\n');
         String utf8 = AutismPayloadSupport.write(bytes);
         if (utf8 != null && !utf8.isBlank()) {
            out.append("UTF-8 Escaped:\n").append(this.init_Str(utf8)).append('\n');
         }
      }
   }

   private String init_Str(String text) {
      if (text != null && !text.isEmpty()) {
         StringBuilder sb = new StringBuilder(text.length() + 16);

         for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);
            switch (chr) {
               case '\u0000':
                  sb.append("\\0\n");
                  break;
               case '\t':
                  sb.append("\\t");
                  break;
               case '\n':
                  sb.append('\n');
                  break;
               case '\r':
                  if (i + 1 >= text.length() || text.charAt(i + 1) != '\n') {
                     sb.append('\n');
                  }
                  break;
               default:
                  if (Character.isISOControl(chr)) {
                     sb.append(String.format("\\u%04X", Integer.valueOf(chr)));
                  } else {
                     sb.append(chr);
                  }
            }
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private String init_Str_BArr_Z(byte[] bytes, boolean spaced) {
      if (bytes != null && bytes.length != 0) {
         StringBuilder sb = new StringBuilder(bytes.length * (spaced ? 3 : 2));

         for (int i = 0; i < bytes.length; i++) {
            if (spaced && i > 0) {
               sb.append(' ');
            }

            this.init_V_StringBuilder_I(sb, bytes[i] & 255);
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private String init_Str_BArr_I(byte[] bytes, int bytesPerLine) {
      if (bytes != null && bytes.length != 0) {
         int perLine = Math.max(1, bytesPerLine);
         StringBuilder sb = new StringBuilder(bytes.length * 3 + bytes.length / perLine + 8);

         for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
               sb.append((char)(i % perLine == 0 ? '\n' : ' '));
            }

            this.init_V_StringBuilder_I(sb, bytes[i] & 255);
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private void init_V_StringBuilder_I(StringBuilder sb, int value) {
      char[] digits = "0123456789ABCDEF".toCharArray();
      sb.append(digits[value >>> 4 & 15]);
      sb.append(digits[value & 15]);
   }

   private void init_V(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      if (entry != null && entry.init_Packet != null && "C2S".equalsIgnoreCase(entry.init_Str)) {
         bs.read(entry);
      } else {
         AutismClientMessaging.sendPrefixed("§cOnly C2S packets can be sent.");
      }
   }

   private void read_V_I_I_3(int anchorX, int anchorY) {
      int screenWidth = init_Minecraft.getWindow() != null ? cA.init_I() : 600;
      int screenHeight = init_Minecraft.getWindow() != null ? cA.load_I() : 400;
      int lineHeight = this.valid();
      int lineCount = this.init_bu_JsonUtil == null ? 0 : this.init_bu_JsonUtil.init_List().size();
      int footerInfoWidth = this.load(lineCount + " lines") + this.put() + 15;
      int footerButtonsWidth = this.push_I_18();
      int titleWidth = this.load(this.init_bu_JsonUtil == null ? "Packet Inspect" : this.init_bu_JsonUtil.init_Str()) + this.entry();
      int minWidth = Math.max(this.pop_I_18(), Math.max(titleWidth, footerInfoWidth + footerButtonsWidth + this.node()));
      int maxWidth = Math.max(minWidth, Math.min(this.stop(), screenWidth - 12));
      int maxHeight = Math.max(this.tick_I_16(), Math.min(this.next(), screenHeight - 12));
      int desiredWidth = this.flush(minWidth, maxWidth);
      AutismPacketInspectOverlay.Inner_DataUtil fit = this.init_AutismPacketInspec(desiredWidth);
      int desiredVisibleLines = Math.max(3, Math.min(fit.load(), this.prev()));
      int desiredListHeight = Math.max(this.task(), desiredVisibleLines * lineHeight + this.add_I_12() * 2 + 2);
      int desiredHeight = 14 + this.size() + this.value() + this.data() + desiredListHeight + this.set_I_14() + this.buffer();
      desiredHeight = Math.min(maxHeight, Math.max(this.tick_I_16(), desiredHeight));
      this.init_V_cH(new cH(anchorX, anchorY, desiredWidth, desiredHeight, true, false));
   }

   private void open_V_9() {
      if (this.init_Str_2 != null && !this.init_Str_2.isEmpty()) {
         int offset = this.init_EventBus != null ? this.init_EventBus.open() : this.run;
         this.run = Math.max(0, offset);
         init_Map.put(this.init_Str_2, this.run);
      }
   }

   private String init_Str_bu_JsonUtil_AutismPacketLogger(bu.Inner_JsonUtil inspection, AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      if (entry != null) {
         String className = entry.init_Class == null ? "" : entry.init_Class.getName();
         return entry.init_Str + "|" + entry.load_Str + "|" + className + "|" + entry.load_J + "|" + entry.init_I;
      } else {
         return inspection == null ? "" : inspection.init_Str();
      }
   }

   private int flush(int minPanelWidth, int maxPanelWidth) {
      int rawLineCount = this.init_bu_JsonUtil == null ? 0 : this.init_bu_JsonUtil.init_List().size();
      int bestWidth = minPanelWidth;
      long bestScore = 9223372036854775807L;

      for (int width = minPanelWidth; width <= maxPanelWidth; width += 8) {
         long score = this.init_J_I_I(width, rawLineCount);
         if (score < bestScore || score == bestScore && width < bestWidth) {
            bestScore = score;
            bestWidth = width;
         }
      }

      if (bestWidth != maxPanelWidth) {
         long score = this.init_J_I_I(maxPanelWidth, rawLineCount);
         if (score < bestScore || score == bestScore && maxPanelWidth < bestWidth) {
            bestWidth = maxPanelWidth;
         }
      }

      return bestWidth;
   }

   private long init_J_I_I(int panelWidthCandidate, int rawLineCount) {
      AutismPacketInspectOverlay.Inner_DataUtil fit = this.init_AutismPacketInspec(panelWidthCandidate);
      int extraLines = Math.max(0, fit.load() - rawLineCount);
      int wastedPixels = Math.max(0, fit.init() - fit.read());
      return panelWidthCandidate / 6L + extraLines * 52L + wastedPixels * 4L;
   }

   private AutismPacketInspectOverlay.Inner_DataUtil init_AutismPacketInspec(int panelWidthCandidate) {
      int listWidth = Math.max(32, panelWidthCandidate - this.put() * 2);
      int contentWidth = autismclient.gui.f.b.MapStore.init(listWidth, this.add_I_12());
      List<AutismPacketInspectOverlay.Inner_BufferHelper> measured = new ArrayList<>();
      if (this.init_bu_JsonUtil != null) {
         for (bu.Inner_TaskQueue line : this.init_bu_JsonUtil.init_List()) {
            this.init_V_bu_TaskQueue(line, contentWidth, measured);
         }
      }

      int longestWrappedLineWidth = 0;

      for (AutismPacketInspectOverlay.Inner_BufferHelper line : measured) {
         longestWrappedLineWidth = Math.max(longestWrappedLineWidth, line.init(this.init_Font));
      }

      return new AutismPacketInspectOverlay.Inner_DataUtil(contentWidth, measured.size(), longestWrappedLineWidth);
   }

   private List<AutismPacketInspectOverlay.Inner_CachePool> init_List(int footerY) {
      List<AutismPacketInspectOverlay.Inner_CachePool> buttons = new ArrayList<>();
      int cursorX = this.open + this.push - this.put();

      for (AutismPacketInspectOverlay.Inner_StreamUtil action : this.init_List_9()) {
         cursorX = this.init_I_List(buttons, cursorX, footerY, action);
      }

      return buttons;
   }

   private List<AutismPacketInspectOverlay.Inner_StreamUtil> init_List_9() {
      List<AutismPacketInspectOverlay.Inner_StreamUtil> actions = new ArrayList<>();
      actions.add(AutismPacketInspectOverlay.Inner_StreamUtil.COPY);
      if (this.init_Z(this.init_AutismPacketLogger)) {
         actions.add(AutismPacketInspectOverlay.Inner_StreamUtil.COPY_RAW);
      }

      if (bs.init(this.init_AutismPacketLogger)) {
         actions.add(AutismPacketInspectOverlay.Inner_StreamUtil.QUEUE);
      }

      if (bs.sync(this.init_AutismPacketLogger)) {
         actions.add(AutismPacketInspectOverlay.Inner_StreamUtil.EDIT_PAYLOAD);
      }

      if (bs.flush(this.init_AutismPacketLogger)) {
         actions.add(AutismPacketInspectOverlay.Inner_StreamUtil.ADD_PAYLOAD);
      }

      if (bs.write(this.init_AutismPacketLogger)) {
         actions.add(AutismPacketInspectOverlay.Inner_StreamUtil.SEND);
      }

      if (bs.update(this.init_AutismPacketLogger)) {
         actions.add(AutismPacketInspectOverlay.Inner_StreamUtil.WAIT);
      }

      return actions;
   }

   private boolean init_Z(AutismPacketLoggerOverlay.Inner_JsonUtil entry) {
      if (entry == null) {
         return false;
      }

      AutismPayloadSupport.Inner_StreamUtil payload = AutismPayloadSupport.init_AutismPayloadSuppo(entry);
      if (payload != null && payload.init_BArr().length > 0) {
         return true;
      }

      bn.Inner_StreamUtil packet = bn.init(entry.init_Packet);
      return packet != null && packet.init_BArr().length > 0;
   }

   private int push_I_18() {
      int width = 0;

      for (AutismPacketInspectOverlay.Inner_StreamUtil action : this.init_List_9()) {
         if (width > 0) {
            width += this.clear();
         }

         width += this.init_I(action);
      }

      return width;
   }

   private int init_I_List(
      List<AutismPacketInspectOverlay.Inner_CachePool> buttons, int cursorX, int footerY, AutismPacketInspectOverlay.Inner_StreamUtil action
   ) {
      int width = this.init_I(action);
      int x = cursorX - width;
      buttons.add(new AutismPacketInspectOverlay.Inner_CachePool(action, x, footerY, width));
      return x - this.clear();
   }

   private int init_I(AutismPacketInspectOverlay.Inner_StreamUtil action) {
      return Math.min(
         68,
         autismclient.gui.f.RenderState.load(
            autismclient.gui.f.b.k.init(this.init_Font, action.init_Str, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), 0)
         )
      );
   }

   private int load(String text) {
      return text != null && !text.isEmpty()
         ? autismclient.gui.f.b.k.init(this.init_Font, text, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), p.task())
         : 0;
   }

   private int pop_I_18() {
      return 176;
   }

   private int tick_I_16() {
      return 14 + this.size() + this.task() + this.set_I_14() + this.buffer();
   }

   private int run_I_16() {
      return 220;
   }

   private int get_I_14() {
      return 173;
   }

   private int set_I_14() {
      return this.queue() + this.reset() + 4;
   }

   private int put() {
      return 4;
   }

   private int add_I_12() {
      return 3;
   }

   private int clear() {
      return 2;
   }

   private int reset() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private int start_I_11() {
      return 32;
   }

   private int stop() {
      return 392;
   }

   private int next() {
      return 300;
   }

   private int prev() {
      return 13;
   }

   private int valid() {
      return Math.max(autismclient.gui.f.RenderState.init(12), this.init_CharEncoder.init_I_l_I(autismclient.gui.f.b.l.read, 2));
   }

   private int size() {
      return 4;
   }

   private int data() {
      return this.init_bu_SyncManager != null && !this.init_bu_SyncManager.init_Z() ? 22 : 0;
   }

   private int value() {
      return 0;
   }

   private int cache() {
      return 16;
   }

   private boolean poll_Z_12() {
      return this.init_AutismPacketLogger != null && AutismPayloadSupport.init_AutismPayloadSuppo(this.init_AutismPacketLogger) != null;
   }

   private int state() {
      return this.size() + this.buffer();
   }

   private int queue() {
      return 2;
   }

   private int buffer() {
      return 0;
   }

   private int entry() {
      return 54;
   }

   private int node() {
      return 16;
   }

   private int task() {
      return 3 * this.valid() + this.add_I_12() * 2 + 2;
   }

   private record Inner_BufferHelper(String init_Str, int init_I, String load_Str, int load_I, int read) {
      static AutismPacketInspectOverlay.Inner_BufferHelper init(String text, int offset, int color) {
         return new AutismPacketInspectOverlay.Inner_BufferHelper(null, color, text, color, offset);
      }

      int init(Font textRenderer) {
         int width = this.read
            + autismclient.gui.f.b.k.init(
               textRenderer, this.load_Str, new autismclient.gui.f.b.CharEncoder().init_Identifier_l(autismclient.gui.f.b.l.read), p.task()
            );
         if (this.init_Str != null && !this.init_Str.isEmpty()) {
            width = Math.max(
               width,
               autismclient.gui.f.b.k.init(
                  textRenderer, this.init_Str, new autismclient.gui.f.b.CharEncoder().init_Identifier_l(autismclient.gui.f.b.l.read), p.task()
               )
            );
         }

         return width;
      }
   }

   private record Inner_CachePool(AutismPacketInspectOverlay.Inner_StreamUtil init_AutismPacketInspec, int init_I, int load, int read) {
      private String init_Str() {
         return this.init_AutismPacketInspec.init_Str;
      }
   }

   private record Inner_DataUtil(int init, int load, int read) {
   }

   private enum Inner_StreamUtil {
      WAIT("Wait"),
      SEND("Send"),
      QUEUE("Queue"),
      EDIT_PAYLOAD("Edit Payload"),
      ADD_PAYLOAD("+Payload"),
      COPY_RAW("Raw Hex"),
      COPY("Copy");

      private final String init_Str;

      Inner_StreamUtil(String label) {
         this.init_Str = label;
      }
   }
}
