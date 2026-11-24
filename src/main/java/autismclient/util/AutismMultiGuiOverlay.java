package autismclient.util;

import autismclient.compat263.CompatGLFW;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class AutismMultiGuiOverlay extends bj {
   private static final int flush_I = 18;
   private static final int close_I = 7;
   private static final int open = 12;
   private static final int poll = 12;
   private static final int push = 3;
   private static final int pop = -3750202;
   private static final int tick = -7631989;
   private static final int run = -13158601;
   private static final int get = -1;
   private static final int set = -2130706433;
   private static final int put = -855310;
   private static final int add = -12174784;
   private static final int clear = -13248397;
   private static final int reset = -42149;
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final Font init_Font;
   private final List<AutismMultiGuiOverlay.Inner_DataUtil> init_List = new ArrayList<>();
   private final List<AutismMultiGuiOverlay.Inner_StreamUtil> load_List = new ArrayList<>();
   private final List<autismclient.gui.b.JsonUtil.Inner_TaskQueue> read_List = new ArrayList<>();
   private final autismclient.gui.b.NetHelper init_NetHelper = new autismclient.gui.b.NetHelper();
   private String init_Str_2 = "";
   private String load_Str = "";
   private final boolean write_Z;
   private String read_Str = "";
   private List<autismclient.util.g.NodeWalker.Inner_DataUtil> write_List = List.of();
   private autismclient.util.g.ClassCache.Inner_ByteReader init_ClassCache_ByteRea;
   private String write_Str = "";
   private long init_J = -9223372036854775808L;
   private int start = 18;
   private int stop = 18;
   private int next;
   private int prev;
   private int valid;
   private int size;
   private int data;
   private boolean update_Z;
   private boolean sync_Z;
   private int value;
   private double init_D;
   private double load_D;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private int cache = -1;
   private int state = -1;
   private int queue;
   private int buffer;
   private int[] init_IArr;
   private int entry = -1;
   private int node = -1;
   private int task = -1;

   public AutismMultiGuiOverlay(Font font, String accountId) {
      this(font, accountId, false);
   }

   public static AutismMultiGuiOverlay init_AutismMultiGuiOver_Font(Font font) {
      return new AutismMultiGuiOverlay(font, "@shared", true);
   }

   private AutismMultiGuiOverlay(Font font, String accountId, boolean shared) {
      super("autism-multi-gui-" + (shared ? "shared" : (accountId == null ? "" : accountId)), 230, shared ? 258 : 238);
      this.init_Font = font;
      this.write_Z = shared;
      this.init_Str_2 = !shared && accountId != null ? accountId : "";
      this.init_I_2 = 548;
      this.load_I = 30 + (shared ? 0 : Math.floorMod(this.init_Str_2.hashCode(), 8) * 14);
   }

   public boolean poll_Z_10() {
      if (this.write_Z) {
         if (!autismclient.util.g.PacketUtil.init_PacketUtil().load_Z()) {
            bi.init("No active Multi batch", -42149);
            return false;
         }

         this.write_List = autismclient.util.g.NodeWalker.init();
         this.write_V_15();
      } else {
         autismclient.util.g.ClassCache.Inner_FieldCache snapshot = this.init_ClassCache_FieldCa_Str(this.init_Str_2);
         if (snapshot == null) {
            bi.init("Multi session is unavailable", -42149);
            return false;
         }

         this.load_Str = snapshot.load_Str();
      }

      this.init_J = -9223372036854775808L;
      this.next = 0;
      this.sync_V_11();
      this.flush_V_11();
      this.write_V_Z(false);
      this.init_V_Z(true);
      return true;
   }

   public boolean init_Z_Str_5(String targetAccountId) {
      return this.init_Z && !this.write_Z && targetAccountId != null && targetAccountId.equals(this.init_Str_2);
   }

   void load(boolean closeHostedViewer) {
      this.init_ClassCache_ByteRea = null;
      this.write_Str = "";
      this.init_J = -9223372036854775808L;
      this.load_List.clear();
      this.init_ItemStack = ItemStack.EMPTY;
      this.cache = -1;
      this.next = 0;
      if (closeHostedViewer) {
         this.init_V_Z(false);
      }
   }

   private autismclient.util.g.NodeWalker.Inner_DataUtil init_NodeWalker_DataUti() {
      return autismclient.util.g.NodeWalker.init(this.write_List, this.read_Str);
   }

   private List<String> init_List_8() {
      autismclient.util.g.NodeWalker.Inner_DataUtil group = this.init_NodeWalker_DataUti();
      return group == null ? List.of() : group.init_List();
   }

   private void write_V_15() {
      autismclient.util.g.NodeWalker.Inner_DataUtil group = this.init_NodeWalker_DataUti();
      this.init_Str_2 = group == null ? "" : group.init_Str();
      this.load_Str = group == null ? "Shared GUI" : "Shared GUI - " + group.init_I() + (group.init_I() == 1 ? " bot" : " bots");
   }

   private void update_V_13() {
      if (this.write_List.size() >= 2) {
         autismclient.util.g.NodeWalker.Inner_DataUtil current = this.init_NodeWalker_DataUti();
         int index = 0;

         for (int i = 0; i < this.write_List.size(); i++) {
            if (current != null && this.write_List.get(i).load().equals(current.load())) {
               index = i;
               break;
            }
         }

         this.read_Str = this.write_List.get((index + 1) % this.write_List.size()).load();
         this.next = 0;
      }
   }

   @Override
   public void init_V_Z(boolean value) {
      if (!value) {
         this.update_Z = false;
         this.sync_Z = false;
         this.init_ItemStack = ItemStack.EMPTY;
         this.cache = -1;
      }

      super.init_V_Z(value);
   }

   @Override
   public int init_I() {
      return 128;
   }

   @Override
   public int load_I() {
      return this.push_I_17() + 18;
   }

   @Override
   public cX.Inner_DataUtil init_cX_DataUtil() {
      return cX.Inner_DataUtil.write;
   }

   @Override
   public boolean f_() {
      return true;
   }

   @Override
   public boolean e_() {
      return this.init_Z && !this.load_Z;
   }

   @Override
   public void b__V() {
      cH layout = ck.init_ck().init_cH_Str(this.init_Str());
      if (layout != null) {
         this.init_V_cH(new cH(layout.init_I, layout.load_I, layout.read, layout.write, this.init_Z, this.load_Z));
      }
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      if (this.init_Z || this.a__Z()) {
         autismclient.util.g.PacketUtil manager = autismclient.util.g.PacketUtil.load_PacketUtil();
         if (manager != null && manager.load_Z() && (this.write_Z || this.init_ClassCache_FieldCa_Str(this.init_Str_2) != null)) {
            if (this.init_Minecraft != null && this.init_Minecraft.gui.screen() != null) {
               if (this.write_Z) {
                  this.write_List = autismclient.util.g.NodeWalker.init();
                  this.write_V_15();
               } else {
                  autismclient.util.g.ClassCache.Inner_FieldCache snapshot = this.init_ClassCache_FieldCa_Str(this.init_Str_2);
                  if (snapshot == null) {
                     this.init_V_Z(false);
                     return;
                  }

                  this.load_Str = snapshot.load_Str();
               }

               this.sync_V_11();
               cH bounds = this.init_cH_cX(this);
               this.init_I_2 = bounds.init_I;
               this.load_I = bounds.load_I;
               this.read_I = bounds.read;
               this.write = bounds.write;
               this.load_V_GuiGraphicsExtract(graphics, mouseX, mouseY);
               if (!this.init_Z_Z(this.load_Z)) {
                  this.init_List.clear();
                  this.load_List.clear();
                  this.read_List.clear();
                  int hoverForInfo = this.cache;
                  this.init_ItemStack = ItemStack.EMPTY;
                  this.cache = -1;
                  this.state = -1;
                  boolean clipped = this.init_Z_GuiGraphicsExtract(graphics, this.init_cH(), this.load_Z);
                  int innerX = this.init_I_2 + 7;
                  int innerWidth = Math.max(1, this.read_I - 14);
                  int titleY = this.load_I + 14 + 3;
                  Component title = this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.init_Component() != null
                     ? this.init_ClassCache_ByteRea.init_Component()
                     : Component.literal(this.write_Z ? "Waiting for a bot GUI..." : "GUI");
                  autismclient.gui.f.HashUtil.init_HashUtil().init(graphics, autismclient.gui.f.NetHelper.init(innerX, titleY, innerWidth, 12));
                  graphics.text(this.init_Font, title.getVisualOrderText(), innerX, titleY, this.run_I_15(), false);
                  autismclient.gui.f.HashUtil.init_HashUtil().init(graphics);
                  int toolbarY = titleY + 12;
                  if (this.init_ClassCache_ByteRea != null) {
                     autismclient.gui.f.HashUtil.init_HashUtil().init(graphics, autismclient.gui.f.NetHelper.init(innerX, toolbarY, innerWidth, 10));
                     int keyColor = ct.init_I_I_ct_DataUtil(-4743522, ct.Inner_DataUtil.read);
                     int mx2 = innerX;
                     mx2 = this.init(
                        graphics,
                        mx2,
                        toolbarY,
                        "Rev: ",
                        Integer.toString(this.init_ClassCache_ByteRea.load_I()),
                        keyColor,
                        ct.init_I_I_ct_DataUtil(-46518, ct.Inner_DataUtil.init)
                     );
                     mx2 = this.init(
                        graphics,
                        mx2 + 8,
                        toolbarY,
                        "SyncID: ",
                        Integer.toString(this.init_ClassCache_ByteRea.init_I()),
                        keyColor,
                        ct.init_I_I_ct_DataUtil(-791321, ct.Inner_DataUtil.read)
                     );
                     int visibleSlot = hoverForInfo >= 0 ? autismclient.util.g.PacketUtil.init_PacketUtil().load_I(this.init_Str_2, hoverForInfo) : -1;
                     this.init(
                        graphics,
                        mx2 + 8,
                        toolbarY,
                        "Slot: ",
                        visibleSlot >= 0 ? Integer.toString(visibleSlot) : "--",
                        keyColor,
                        ct.init_I_I_ct_DataUtil(-7350273, ct.Inner_DataUtil.init)
                     );
                     autismclient.gui.f.HashUtil.init_HashUtil().init(graphics);
                     toolbarY += 12;
                  }

                  if (this.write_Z) {
                     autismclient.util.g.NodeWalker.Inner_DataUtil group = this.init_NodeWalker_DataUti();
                     String pickerLabel = group == null ? "No GUIs open yet" : "GUI: " + group.read() + (this.write_List.size() > 1 ? "  >" : "");
                     this.init(
                        graphics,
                        innerX,
                        toolbarY,
                        innerWidth,
                        pickerLabel,
                        this.write_List.size() > 1 ? this.set_I_13() : this.get_I_13(),
                        mouseX,
                        mouseY,
                        this::update_V_13
                     );
                     toolbarY += pop_I_17() + 2;
                  }

                  int gap = 2;
                  int firstWidth = Math.max(1, (innerWidth - gap) / 2);
                  this.init_IArr = new int[]{innerX, toolbarY, firstWidth, pop_I_17()};
                  this.init(graphics, innerX, toolbarY, firstWidth, "Close W/O Pkt", this.get_I_13(), mouseX, mouseY, () -> this.read(true));
                  this.init(
                     graphics,
                     innerX + firstWidth + gap,
                     toolbarY,
                     innerWidth - firstWidth - gap,
                     "Close",
                     this.get_I_13(),
                     mouseX,
                     mouseY,
                     () -> this.read(false)
                  );
                  int areaTop = toolbarY + pop_I_17() + 3 + 2;
                  int availableHeight = Math.max(18, this.load_I + this.write - 7 - 3 - areaTop);
                  boolean scroll = this.stop > availableHeight;
                  int scrollbarSpace = scroll ? 6 : 0;
                  this.size = Math.max(1, Math.min(this.start, innerWidth - scrollbarSpace));
                  this.data = Math.min(this.stop, availableHeight);
                  this.prev = innerX + Math.max(0, (innerWidth - this.size - scrollbarSpace) / 2);
                  this.valid = areaTop;
                  this.next = load_I_I_3(this.next, 0, this.tick_I_15());
                  this.init_V_GuiGraphicsExtract_5(graphics, mouseX, mouseY);
                  autismclient.gui.f.b.PoolEntry.Inner_DataUtil scrollbar = this.init_PoolEntry_DataUtil_6();
                  autismclient.gui.f.b.PoolEntry.init(graphics, scrollbar, scrollbar.init(mouseX, mouseY), this.sync_Z);
                  this.init_V_GuiGraphicsExtract_Z(graphics, clipped);
                  ItemStack carried = this.init_ClassCache_ByteRea == null ? ItemStack.EMPTY : this.init_ClassCache_ByteRea.init_ItemStack();
                  if (carried != null && !carried.isEmpty()) {
                     graphics.nextStratum();
                     graphics.item(carried, mouseX - 8, mouseY - 8);
                     graphics.itemDecorations(this.init_Font, carried, mouseX - 8, mouseY - 8);
                  } else if (!this.init_ItemStack.isEmpty()) {
                     this.init_V_GuiGraphicsExtract_5(graphics, this.init_ItemStack, this.queue, this.buffer, this.state >= 0);
                  } else if (AutismConfig.getGlobal().multiShowTooltips
                     && this.init_IArr != null
                     && autismclient.gui.b.UiHelper.init(this.init_IArr[0], this.init_IArr[1], this.init_IArr[2], this.init_IArr[3], mouseX, mouseY)) {
                     autismclient.gui.b.UiHelper.init(graphics, this.init_Font, "Hide GUI locally, keep it open", mouseX, mouseY);
                  }
               }
            }
         } else {
            this.init_V_Z(false);
         }
      }
   }

   private void init_V_GuiGraphicsExtract_5(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      autismclient.gui.f.NetHelper area = autismclient.gui.f.NetHelper.init(this.prev - 3, this.valid - 3, this.size + 6, this.data + 6);
      autismclient.gui.f.PacketUtil.init(graphics, area, -3750202);
      int selectedHandler = autismclient.util.g.PacketUtil.init_PacketUtil().init_I_Str(this.init_Str_2);
      autismclient.gui.f.HashUtil.init_HashUtil().init(graphics, autismclient.gui.f.NetHelper.init(this.prev, this.valid, this.size, this.data));
      if (this.init_ClassCache_ByteRea != null) {
         for (autismclient.util.g.ClassCache.Inner_MapStore slot : this.init_ClassCache_ByteRea.init_List()) {
            int x = this.prev + slot.init_I();
            int y = this.valid + slot.load() - this.next;
            if (x + 18 > this.prev && x < this.prev + this.size && y + 18 > this.valid && y < this.valid + this.data) {
               this.read_V_GuiGraphicsExtract(graphics, x, y);
               if (selectedHandler >= 0 && slot.read() == selectedHandler) {
                  autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, 18, 18), 452935744, -1325449152);
               }

               ItemStack stack = slot.init_ItemStack();
               if (stack != null && !stack.isEmpty()) {
                  try {
                     graphics.item(stack, x + 1, y + 1);
                     graphics.itemDecorations(this.init_Font, stack, x + 1, y + 1);
                  } catch (Throwable var12) {
                  }
               }

               this.load_List.add(new AutismMultiGuiOverlay.Inner_StreamUtil(x, y, slot.read(), stack == null ? ItemStack.EMPTY : stack));
               if (mouseX >= x
                  && mouseX < x + 18
                  && mouseY >= y
                  && mouseY < y + 18
                  && mouseX >= this.prev
                  && mouseX < this.prev + this.size
                  && mouseY >= this.valid
                  && mouseY < this.valid + this.data) {
                  autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x + 1, y + 1, 16, 16), -2130706433);
                  this.init_ItemStack = stack == null ? ItemStack.EMPTY : stack;
                  this.cache = slot.read();
                  this.state = autismclient.util.g.PacketUtil.init_PacketUtil().init_I_Str_I(this.init_Str_2, slot.read());
                  this.queue = mouseX;
                  this.buffer = mouseY;
               }
            }
         }

         this.init_NetHelper.init(this.load_Str_8());
         autismclient.gui.b.JsonUtil.init(
            graphics, this.init_Font, this.init_ClassCache_ByteRea, this.prev, this.valid, this.next, mouseX, mouseY, this.read_List, this.init_NetHelper
         );
      }

      autismclient.gui.f.HashUtil.init_HashUtil().init(graphics);
   }

   private void load_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      boolean active = this.update_Z || bk.init_bk().update_Z(this) || bk.init_bk().write_Z(this);
      autismclient.gui.f.b.FieldCache.init(
         autismclient.gui.f.UiHelper.init(graphics, this.init_Font, mouseX, mouseY),
         autismclient.gui.f.NetHelper.init(this.init_I_2, this.load_I, this.read_I, this.init_I_cH_Z(this.init_cH(), this.load_Z)),
         this.write_Z ? this.load_Str : "GUI - " + this.load_Str,
         this.load_Z,
         true,
         true,
         mouseY >= this.load_I && mouseY < this.load_I + 14,
         active,
         4,
         4,
         14
      );
   }

   @Override
   protected boolean init_Z_D(double mouseX, double mouseY, cH bounds) {
      return false;
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   private void read_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, int x, int y) {
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, 18, 18), -7631989);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, 18, 1), -13158601);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, 1, 18), -13158601);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y + 18 - 1, 18, 1), -1);
      autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x + 18 - 1, y, 1, 18), -1);
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int width, String label, int outline, int mouseX, int mouseY, Runnable callback) {
      int h = pop_I_17();
      boolean hover = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + h;
      autismclient.gui.f.b.CachePool.init(
         autismclient.gui.f.UiHelper.init(graphics, this.init_Font, mouseX, mouseY),
         autismclient.gui.f.NetHelper.init(x, y, width, h),
         label,
         autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         hover,
         false
      );
      this.init_List.add(new AutismMultiGuiOverlay.Inner_DataUtil(x, y, width, h, callback));
   }

   private void sync_V_11() {
      long revision = autismclient.util.g.PacketUtil.init_PacketUtil().init_J_Str(this.init_Str_2);
      if (revision == this.init_J && this.init_ClassCache_ByteRea != null && this.init_Str_2.equals(this.write_Str)) {
         if (cA.load_I() != this.task) {
            this.flush_V_11();
         }
      } else {
         this.init_J = revision;
         this.write_Str = this.init_Str_2;
         this.init_ClassCache_ByteRea = autismclient.util.g.PacketUtil.init_PacketUtil().init_ClassCache_ByteRea_Str(this.init_Str_2);
         int[] cs = autismclient.gui.b.JsonUtil.init_IArr_ClassCache_ByteRea(this.init_ClassCache_ByteRea);
         this.start = Math.max(18, cs[0]);
         this.stop = Math.max(18, cs[1]);
         this.init_NetHelper.init(this.load_Str_8());
         if (this.start != this.entry || this.stop != this.node || cA.load_I() != this.task) {
            this.flush_V_11();
         }

         this.next = load_I_I_3(this.next, 0, this.tick_I_15());
      }
   }

   private int push_I_17() {
      int info = this.init_ClassCache_ByteRea != null ? 12 : 0;
      return 29 + info + (this.write_Z ? pop_I_17() + 2 : 0) + pop_I_17() + 3 + 2 + 3 + 7;
   }

   private static int pop_I_17() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private void flush_V_11() {
      this.entry = this.start;
      this.node = this.stop;
      this.task = cA.load_I();
      int screenH = Math.max(120, this.task);
      int maxGridH = Math.max(18, screenH - this.push_I_17() - 20);
      int gridH = Math.min(this.stop, maxGridH);
      boolean needsScroll = this.stop > gridH;
      int wantedWidth = Math.max(this.init_I(), this.start + 14 + (needsScroll ? 6 : 0));
      int wantedHeight = Math.max(this.load_I(), this.push_I_17() + gridH);
      this.init_V_cH(new cH(this.init_I_2, this.load_I, wantedWidth, wantedHeight, true, false));
   }

   private autismclient.util.g.ClassCache.Inner_FieldCache init_ClassCache_FieldCa_Str(String id) {
      if (id != null && !id.isBlank()) {
         for (autismclient.util.g.ClassCache.Inner_FieldCache snapshot : autismclient.util.g.PacketUtil.init_PacketUtil().load_List()) {
            if (id.equals(snapshot.init_Str())) {
               return snapshot;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private int tick_I_15() {
      return Math.max(0, this.stop - Math.max(18, this.data));
   }

   private autismclient.gui.f.b.PoolEntry.Inner_DataUtil init_PoolEntry_DataUtil_6() {
      return autismclient.gui.f.b.PoolEntry.init_PoolEntry_DataUtil_I_2(
         this.stop, Math.max(18, this.data), this.prev + this.size + 2, this.valid, 3, Math.max(18, this.data), this.next
      );
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.init_Z) {
         return false;
      }

      cH bounds = this.init_cH();
      if (this.write(mouseX, mouseY, bounds)) {
         this.init_V_Z(false);
         return true;
      }

      if (button == 0 && this.init_PoolEntry_DataUtil_6().load(mouseX, mouseY)) {
         this.sync_Z = true;
         this.value = (int)Math.round(mouseY) - this.init_PoolEntry_DataUtil_6().update();
         return true;
      }

      if (button == 0 && this.load_Z_D_D(mouseX, mouseY)) {
         this.update_Z = true;
         this.init_D = mouseX - this.init_I_2;
         this.load_D = mouseY - this.load_I;
         return true;
      }

      if (this.load_Z) {
         return false;
      }

      if (button == 0) {
         for (AutismMultiGuiOverlay.Inner_DataUtil action : this.init_List) {
            if (action.init(mouseX, mouseY)) {
               action.init_Runnable().run();
               return true;
            }
         }
      }

      boolean insideGrid = mouseX >= this.prev && mouseX < this.prev + this.size && mouseY >= this.valid && mouseY < this.valid + this.data;
      if (insideGrid && button == 0) {
         for (autismclient.gui.b.JsonUtil.Inner_TaskQueue hit : this.read_List) {
            if (!(mouseX < hit.init_I()) && !(mouseX >= hit.init_I() + hit.read()) && !(mouseY < hit.load()) && !(mouseY >= hit.load() + hit.write())) {
               this.init_V_JsonUtil_ConfigSto(hit.init_JsonUtil_ConfigSto());
               return true;
            }
         }
      }

      if (insideGrid) {
         for (AutismMultiGuiOverlay.Inner_StreamUtil slot : this.load_List) {
            if (slot.init(mouseX, mouseY)) {
               if (button == 1 && this.push_Z_9() && this.pop_Z_6()) {
                  if (!slot.init_ItemStack().isEmpty()) {
                     this.init_V_ItemStack_2(slot.init_ItemStack(), (int)mouseX, (int)mouseY);
                  }

                  return true;
               }

               if (slot.read() < 0) {
                  return true;
               }

               if (this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.init_Z()) {
                  autismclient.util.g.BufferHelper.Inner_DataUtil spec = autismclient.util.g.BufferHelper.init(button, this.pop_Z_6(), this.push_Z_9());
                  if (this.write_Z) {
                     this.load(autismclient.util.g.PacketUtil.init_PacketUtil().init_PacketUtil_CachePo_List(this.init_List_8(), slot.read(), spec));
                  } else {
                     this.init_V_Str_8(autismclient.util.g.PacketUtil.init_PacketUtil().init_Str_Str_2(this.init_Str_2, slot.read(), spec));
                  }

                  return true;
               }

               bi.init(
                  this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.load_Z()
                     ? "Inventory is waiting for a server update"
                     : "Inventory is synchronizing",
                  this.put()
               );
               return true;
            }
         }
      }

      return insideGrid ? true : this.init_Z_D_D(mouseX, mouseY);
   }

   @Override
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      if (this.sync_Z) {
         this.sync_Z = false;
         return true;
      } else if (this.update_Z) {
         this.update_Z = false;
         this.a__V();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.sync_Z) {
         this.next = autismclient.gui.f.b.PoolEntry.init(this.init_PoolEntry_DataUtil_6(), mouseY, this.value);
         return true;
      } else if (this.update_Z) {
         cH next = this.init_cH_cX_cH(
            this, new cH((int)Math.round(mouseX - this.init_D), (int)Math.round(mouseY - this.load_D), this.read_I, this.write, this.init_Z, this.load_Z)
         );
         this.init_I_2 = next.init_I;
         this.load_I = next.load_I;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      if (this.init_Z && !this.load_Z && this.init_Z_D_D(mouseX, mouseY)) {
         this.next = load_I_I_3(this.next - (int)Math.signum(amount) * 18, 0, this.tick_I_15());
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (!this.init_Z) {
         return false;
      }

      if (this.init_NetHelper.init_SyncManager.init_Z() && this.init_NetHelper.init_SyncManager.init(keyCode)) {
         this.close_V_10();
         return true;
      }

      if (bk.init_bk().update_Z()) {
         return false;
      }

      if (!this.load_Z && keyCode == 81 && this.cache >= 0 && this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.init_Z()) {
         boolean wholeStack = (modifiers & 2) != 0 || this.push_Z_9();
         autismclient.util.g.BufferHelper.Inner_DataUtil spec = autismclient.util.g.BufferHelper.init(wholeStack);
         if (this.write_Z) {
            this.load(autismclient.util.g.PacketUtil.init_PacketUtil().init_PacketUtil_CachePo_List(this.init_List_8(), this.cache, spec));
         } else {
            this.init_V_Str_8(autismclient.util.g.PacketUtil.init_PacketUtil().init_Str_Str_2(this.init_Str_2, this.cache, spec));
         }

         return true;
      } else if (!this.load_Z
         && this.state >= 0
         && this.init_ClassCache_ByteRea != null
         && this.init_ClassCache_ByteRea.init_Z()
         && (keyCode == 85 || keyCode == 73)) {
         boolean use = keyCode == 73;
         if (this.write_Z) {
            this.load(
               use
                  ? autismclient.util.g.PacketUtil.init_PacketUtil().load(this.init_List_8(), this.state)
                  : autismclient.util.g.PacketUtil.init_PacketUtil().init_PacketUtil_CachePo_List_I(this.init_List_8(), this.state)
            );
         } else {
            this.init_V_Str_8(
               use
                  ? autismclient.util.g.PacketUtil.init_PacketUtil().load_Str(this.init_Str_2, this.state)
                  : autismclient.util.g.PacketUtil.init_PacketUtil().init_Str_Str_I(this.init_Str_2, this.state)
            );
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      if (this.init_NetHelper.init_SyncManager.init_Z() && this.init_NetHelper.init_SyncManager.init(chr)) {
         this.close_V_10();
         return true;
      } else {
         return false;
      }
   }

   private void close_V_10() {
      if (this.write_Z) {
         autismclient.util.g.PacketUtil.init_PacketUtil()
            .init_PacketUtil_CachePo_List_4(this.init_List_8(), this.init_NetHelper.init_SyncManager.init_Str(), this.load_Str_8());
      } else {
         autismclient.util.g.PacketUtil.init_PacketUtil().load_Str(this.init_Str_2, this.init_NetHelper.init_SyncManager.init_Str());
      }
   }

   private String load_Str_8() {
      return this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.init_ClassCache_FileCac() != null
         ? this.init_ClassCache_ByteRea.init_ClassCache_FileCac().init_Str()
         : "";
   }

   private void init_V_JsonUtil_ConfigSto(autismclient.gui.b.JsonUtil.Inner_ConfigStore action) {
      if (action instanceof autismclient.gui.b.JsonUtil.Inner_JsonUtil) {
         this.init_NetHelper.init_SyncManager.init_V();
         this.init_NetHelper.init_SyncManager.init("");
      } else if (action instanceof autismclient.gui.b.JsonUtil.Inner_StreamUtil p) {
         if (p.init_Z()) {
            this.init_NetHelper.load = p.init_I();
         } else {
            this.init_NetHelper.init_I = p.init_I();
         }
      } else if (this.init_ClassCache_ByteRea != null && this.init_ClassCache_ByteRea.init_Z()) {
         autismclient.util.g.PacketUtil mgr = autismclient.util.g.PacketUtil.init_PacketUtil();
         String type = this.load_Str_8();
         if (action instanceof autismclient.gui.b.JsonUtil.Inner_CachePool b) {
            if (this.write_Z) {
               this.load(mgr.init_PacketUtil_CachePo_List_2(this.init_List_8(), b.init(), type));
            } else {
               this.init_V_Str_8(mgr.read_Str_Str_I(this.init_Str_2, b.init()));
            }
         } else if (action instanceof autismclient.gui.b.JsonUtil.Inner_StateCache t) {
            if (this.write_Z) {
               this.load(mgr.load(this.init_List_8(), t.init(), type));
            } else {
               this.init_V_Str_8(mgr.write(this.init_Str_2, t.init()));
            }
         } else if (action instanceof autismclient.gui.b.JsonUtil.Inner_DataUtil be) {
            if (this.write_Z) {
               this.load(mgr.init_PacketUtil_CachePo_List(this.init_List_8(), be.init(), be.load(), type));
            } else {
               this.init_V_Str_8(mgr.init_Str_Str(this.init_Str_2, be.init(), be.load()));
            }
         } else if (action instanceof autismclient.gui.b.JsonUtil.Inner_NetHelper rs) {
            this.init_NetHelper.read = Math.max(0, this.init_NetHelper.read + rs.init());
            if (this.write_Z) {
               this.load(mgr.init_PacketUtil_CachePo_List_2(this.init_List_8(), this.init_NetHelper.read, type));
            } else {
               this.init_V_Str_8(mgr.read_Str_Str_I(this.init_Str_2, this.init_NetHelper.read));
            }
         }
      } else {
         bi.init("Inventory is synchronizing", this.put());
      }
   }

   private void init_V_PacketUtil_CachePo(autismclient.util.g.PacketUtil.Inner_CachePool result) {
      if (result == null || result.read() > 0) {
         bi.init("GUI action failed", this.put());
      } else if (result.init_I() == 0 && result.load() > 0) {
         bi.init("GUI action skipped", this.get_I_13());
      }
   }

   private void init_V_Str_8(String result) {
      if (!"Sent".equals(result)) {
         String message = result != null && !result.isBlank() ? autismclient.util.g.PacketUtil.update(result, 80) : "GUI action failed";
         bi.init(message, this.put());
      }
   }

   private void load(autismclient.util.g.PacketUtil.Inner_CachePool result) {
      if (result != null) {
         int missed = result.read() + result.load();
         if (result.init_I() <= 0 || missed != 0) {
            bi.init(
               result.init_I() == 0 ? "No bot took the click" : "Sent " + result.init_I() + ", missed " + missed,
               result.init_I() == 0 ? this.put() : this.get_I_13()
            );
         }
      }
   }

   private void read(boolean silent) {
      Set<String> scope = this.write_Z ? new LinkedHashSet<>(this.init_List_8()) : Set.of(this.init_Str_2);
      if (!this.write_Z || !scope.isEmpty()) {
         this.init_V_PacketUtil_CachePo(
            silent
               ? autismclient.util.g.PacketUtil.init_PacketUtil().close_PacketUtil_CachePo_Set(scope)
               : autismclient.util.g.PacketUtil.init_PacketUtil().flush_PacketUtil_CachePo_Set(scope)
         );
      }
   }

   private void init_V_ItemStack_2(ItemStack stack, int mouseX, int mouseY) {
      AutismItemNbtInspectOverlay overlay = AutismItemNbtInspectOverlay.init_AutismItemNbtInspe_Font(this.init_Font);
      if (overlay != null) {
         overlay.init_V_ItemStack(stack, mouseX + 8, mouseY);
         bk.init_bk().init_V_cX_cX_DataUtil(overlay, cX.Inner_DataUtil.write);
         bk.init_bk().update_V(overlay);
      }
   }

   private void init_V_GuiGraphicsExtract_5(GuiGraphicsExtractor graphics, ItemStack stack, int mouseX, int mouseY, boolean hotbar) {
      try {
         List<Component> base = Screen.getTooltipFromItem(this.init_Minecraft, stack);
         if (base == null || base.isEmpty()) {
            return;
         }

         List<Component> lines = base;
         if (hotbar) {
            lines = new ArrayList<>(base);
            lines.add(Component.literal("[U] switch to slot"));
            lines.add(Component.literal("[I] switch + use item"));
         }

         int width = 0;

         for (Component line : lines) {
            width = Math.max(width, this.init_Font.width(line));
         }

         int height = lines.size() == 1 ? 8 : lines.size() * 10 - 2;
         int screenWidth = cA.init_I();
         int screenHeight = cA.load_I();
         int x = mouseX + 12;
         int y = mouseY - 12;
         if (x + width + 4 > screenWidth) {
            x = Math.max(4, mouseX - width - 16);
         }

         if (y + height + 4 > screenHeight) {
            y = screenHeight - height - 4;
         }

         if (y < 4) {
            y = 4;
         }

         graphics.nextStratum();
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x - 3, y - 3, width + 6, height + 6), -267386864);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x - 3, y - 3, width + 6, height + 6), 0, 1347420320);
         int lineY = y;

         for (Component line : lines) {
            graphics.text(this.init_Font, line.getVisualOrderText(), x, lineY, -1, true);
            lineY += 10;
         }
      } catch (Throwable var17) {
      }
   }

   private String init_Str_Str_I_3(String value, int width) {
      String safe = value == null ? "" : value;
      return this.init_Font.width(safe) <= width ? safe : this.init_Font.plainSubstrByWidth(safe, Math.max(1, width - this.init_Font.width("..."))) + "...";
   }

   private boolean push_Z_9() {
      if (this.init_Minecraft != null && this.init_Minecraft.getWindow() != null) {
         long window = this.init_Minecraft.getWindow().handle();
         return CompatGLFW.glfwGetKey(window, 341) == 1 || CompatGLFW.glfwGetKey(window, 345) == 1;
      } else {
         return false;
      }
   }

   private boolean pop_Z_6() {
      if (this.init_Minecraft != null && this.init_Minecraft.getWindow() != null) {
         long window = this.init_Minecraft.getWindow().handle();
         return CompatGLFW.glfwGetKey(window, 340) == 1 || CompatGLFW.glfwGetKey(window, 344) == 1;
      } else {
         return false;
      }
   }

   private int run_I_15() {
      return ct.init_I_I_ct_DataUtil(-855310, ct.Inner_DataUtil.read);
   }

   private int get_I_13() {
      return ct.init_I_I_ct_DataUtil(-12174784, ct.Inner_DataUtil.load);
   }

   private int init(GuiGraphicsExtractor graphics, int x, int y, String key, String value, int keyColor, int valueColor) {
      graphics.text(this.init_Font, Component.literal(key).getVisualOrderText(), x, y, keyColor, false);
      int vx = x + this.init_Font.width(key);
      graphics.text(this.init_Font, Component.literal(value).getVisualOrderText(), vx, y, valueColor, false);
      return vx + this.init_Font.width(value);
   }

   private int set_I_13() {
      return ct.init_I_I_ct_DataUtil(-13248397, ct.Inner_DataUtil.sync);
   }

   private int put() {
      return ct.init_I_I_ct_DataUtil(-42149, ct.Inner_DataUtil.flush);
   }

   private static int flush(int color, int alpha) {
      return alpha << 24 | color & 16777215;
   }

   private static int load_I_I_3(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   private record Inner_DataUtil(int init_I, int load, int read, int write, Runnable init_Runnable) {
      boolean init(double mouseX, double mouseY) {
         return mouseX >= this.init_I && mouseX < this.init_I + this.read && mouseY >= this.load && mouseY < this.load + this.write;
      }
   }

   private record Inner_StreamUtil(int init_I, int load, int read, ItemStack init_ItemStack) {
      boolean init(double mouseX, double mouseY) {
         return mouseX >= this.init_I && mouseX < this.init_I + 18 && mouseY >= this.load && mouseY < this.load + 18;
      }
   }
}
