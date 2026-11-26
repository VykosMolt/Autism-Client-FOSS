package autismclient.util;

import autismclient.gui.f.b.g;
import autismclient.gui.f.b.h;
import autismclient.gui.f.b.j;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AutismKeybindOverlay extends bj {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final String init_Str_2 = "Settings";
   private static final String load_Str = "Fire keybinds while a container screen is open. Blocked in chat/sign/book editors and focused text fields.";
   private static final String read_Str = "Use our custom main menu. Turn off for the normal vanilla/Fabric title screen (mods stay visible).";
   private static final int flush_I = 120;
   private static final int close_I = 14;
   private static final int open = 4;
   private static final int poll = 10;
   private static final int push = 4;
   private static final int pop = 22;
   private static final int tick = 47;
   private static final int run = 34;
   private static final float init_F_2 = 3.0F;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.f.c.CharEncoder init_CharEncoder_2 = new autismclient.gui.f.c.CharEncoder("Settings").init_CharEncoder(true);
   private final autismclient.gui.f.c.TextFormat init_TextFormat = new autismclient.gui.f.c.TextFormat(this.init_CharEncoder, this.init_CharEncoder_2);
   private final List<AutismKeybindOverlay.Inner_CachePool> init_List = new ArrayList<>();
   private final List<AutismKeybindOverlay.Inner_BufferHelper> load_List = new ArrayList<>();
   private boolean write_Z = false;
   private float load_F;
   private float read_F;
   private float write_F;
   private float update_F;
   private int get;
   private int set;
   private boolean update_Z = false;
   private int put = -1;
   private String write_Str = null;
   private float sync_F;
   private float flush_F;
   private int add = -1;
   private int clear = -1;
   private float close_F = 0.0F / 0.0F;
   private boolean sync_Z;
   private int reset;
   private autismclient.gui.f.b.RefQueue init_RefQueue;

   public AutismKeybindOverlay() {
      super("autism-keybinds", 120, 142);
      this.init_I_2 = 160;
      this.load_I = 46;
      if (!AutismLiteVariant.enabled()) {
         this.init_List
            .add(
               new AutismKeybindOverlay.Inner_CachePool(
                  "Module Menu",
                  "Opens the Autism module click GUI",
                  () -> this.init_AutismConfig().keybindModuleMenu,
                  v -> this.init_AutismConfig().keybindModuleMenu = v
               )
            );
         this.init_List
            .add(
               new AutismKeybindOverlay.Inner_CachePool(
                  "AI Assistant",
                  "Opens the AI Assistant utility window",
                  () -> this.init_AutismConfig().keybindAiAssistant,
                  v -> this.init_AutismConfig().keybindAiAssistant = v
               )
            );
      }

      this.init_List
         .add(
            new AutismKeybindOverlay.Inner_CachePool(
               "Load GUI",
               "Restores the last saved GUI screen and handler",
               () -> this.init_AutismConfig().keybindLoadGui,
               v -> this.init_AutismConfig().keybindLoadGui = v
            )
         );
      this.init_List
         .add(
            new AutismKeybindOverlay.Inner_CachePool(
               "Flush Queue",
               "Sends all queued packets to the server",
               () -> this.init_AutismConfig().keybindFlushQueue,
               v -> this.init_AutismConfig().keybindFlushQueue = v
            )
         );
      this.init_List
         .add(
            new AutismKeybindOverlay.Inner_CachePool(
               "Clear Queue",
               "Discards all queued packets",
               () -> this.init_AutismConfig().keybindClearQueue,
               v -> this.init_AutismConfig().keybindClearQueue = v
            )
         );
      this.init_List
         .add(
            new AutismKeybindOverlay.Inner_CachePool(
               "Toggle Logger",
               "Shows or hides the packet logger overlay",
               () -> this.init_AutismConfig().keybindToggleLogger,
               v -> this.init_AutismConfig().keybindToggleLogger = v
            )
         );
      this.init_List
         .add(
            new AutismKeybindOverlay.Inner_CachePool(
               "Toggle Send",
               "Turns packet sending on or off",
               () -> this.init_AutismConfig().keybindToggleSend,
               v -> this.init_AutismConfig().keybindToggleSend = v
            )
         );
      this.init_List
         .add(
            new AutismKeybindOverlay.Inner_CachePool(
               "Toggle Delay",
               "Turns packet delay on or off",
               () -> this.init_AutismConfig().keybindToggleDelay,
               v -> this.init_AutismConfig().keybindToggleDelay = v
            )
         );
      this.write_V_9();
   }

   private void write_V_9() {
      this.init_CharEncoder_2.flush(false);
      this.init_CharEncoder_2.init(autismclient.gui.f.b.l.load);
      this.init_CharEncoder_2.init_CharEncoder_Z_Z(true, true);
      this.init_CharEncoder_2.init_CharEncoder_I_I(this.get_I_7() + 2, this.get_I_7() + this.add_I_7() * 2 + 8);
      this.init_CharEncoder_2.init_PacketUtil().init_V_2();
      this.init_CharEncoder_2.init_PacketUtil().init(this.set_I_7()).init(autismclient.gui.f.c.IoWorker.init(this.get_I_7()));
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new AutismKeybindOverlay.Inner_JsonUtil(
               "Inside GUI",
               "Fire keybinds while a container screen is open. Blocked in chat/sign/book editors and focused text fields.",
               () -> this.init_AutismConfig().keybindInsideGui,
               v -> this.init_AutismConfig().keybindInsideGui = v
            )
         );
      if (!AutismLiteVariant.enabled()) {
         this.init_CharEncoder_2
            .init_PacketUtil()
            .init_TableIndex_TableIndex(
               new AutismKeybindOverlay.Inner_JsonUtil(
                  "Custom Menu",
                  "Use our custom main menu. Turn off for the normal vanilla/Fabric title screen (mods stay visible).",
                  () -> this.init_AutismConfig().customMainMenu,
                  v -> {
                     AutismConfig config = this.init_AutismConfig();
                     config.customMainMenu = v;
                     config.save();
                  }
               )
            );
      }

      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(new AutismKeybindOverlay.Inner_NetHelper());
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(new AutismKeybindOverlay.Inner_DataUtil());
      if (!AutismLiteVariant.enabled()) {
         this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(new AutismKeybindOverlay.Inner_ConfigStore());
      }

      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new AutismKeybindOverlay.Inner_JsonUtil("Auto Probe", "Auto-Scans on join.", () -> this.init_AutismConfig().autoProbePlugins, v -> {
               AutismConfig config = this.init_AutismConfig();
               config.autoProbePlugins = v;
               config.save();
            })
         );
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new AutismKeybindOverlay.Inner_JsonUtil("InfiniChat", "Remove chat length limit.", () -> this.init_AutismConfig().infiniChat, v -> {
               AutismConfig config = this.init_AutismConfig();
               config.infiniChat = v;
               config.save();
            })
         );
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new AutismKeybindOverlay.Inner_JsonUtil(
               "Chat Feedback", "Show module enable/disable messages in chat.", () -> this.init_AutismConfig().chatFeedback, v -> {
                  AutismConfig config = this.init_AutismConfig();
                  config.chatFeedback = v;
                  config.save();
               }
            )
         );
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new AutismKeybindOverlay.Inner_JsonUtil("Stop On Leave", "Stop macros on disconnect.", () -> this.init_AutismConfig().stopMacroOnLeave, v -> {
               AutismConfig config = this.init_AutismConfig();
               config.stopMacroOnLeave = v;
               config.save();
            })
         );
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(new AutismKeybindOverlay.Inner_TaskQueue());
      this.load_List.clear();

      for (int i = 0; i < this.init_List.size(); i++) {
         AutismKeybindOverlay.Inner_BufferHelper row = new AutismKeybindOverlay.Inner_BufferHelper(i, this.init_List.get(i));
         this.load_List.add(row);
         this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(row);
      }

      if (!AutismLiteVariant.enabled()) {
         autismclient.gui.f.c.StreamUtil settingsPages = new autismclient.gui.f.c.StreamUtil()
            .init(0)
            .init(autismclient.gui.f.c.IoWorker.init_IoWorker)
            .init_StreamUtil(false);
         settingsPages.init_TableIndex_Z(true);
         settingsPages.init_TableIndex_TableIndex(
            new autismclient.gui.f.c.ByteReader("Theme Color", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.PRIMARY, this::update_V_7)
               .read_ByteReader_Z(true)
               .init_ByteReader_Z(true)
               .init_ByteReader_MethodUtil_DataUti(autismclient.gui.f.b.MethodUtil.load)
               .load_ByteReader_I(this.state())
         );
         settingsPages.init_TableIndex_TableIndex(
            new autismclient.gui.f.c.ByteReader("Profiles", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.PRIMARY, this::sync_V_6)
               .read_ByteReader_Z(true)
               .init_ByteReader_Z(true)
               .init_ByteReader_MethodUtil_DataUti(autismclient.gui.f.b.MethodUtil.read)
               .load_ByteReader_I(this.state())
         );
         this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(settingsPages);
      }
   }

   private void update_V_7() {
      init_Minecraft.gui.setScreen(new autismclient.gui.d.ThemeColorScreen(init_Minecraft.gui.screen()));
   }

   private void sync_V_6() {
      if (!AutismLiteVariant.enabled()) {
         autismclient.modules.PoolEntry module = autismclient.modules.PoolEntry.init_PoolEntry();
         cX profiles = module == null ? null : module.load_cX();
         if (profiles != null) {
            ((AutismProfilesOverlay)profiles).load(false);
            profiles.init_V_Z(true);
            bk manager = bk.init_bk();
            manager.init_V_cX_cX_DataUtil(profiles, cX.Inner_DataUtil.HOST_SCREEN);
            manager.update_V(profiles);
         }
      }
   }

   private AutismConfig init_AutismConfig() {
      return AutismConfig.getGlobal();
   }

   @Override
   public int init_I() {
      return this.tick_I_9();
   }

   @Override
   public int load_I() {
      return this.init_CharEncoder.load() + this.put();
   }

   @Override
   public boolean read_Z() {
      return true;
   }

   @Override
   protected int a__I() {
      return this.init_CharEncoder.load() + this.get_I_7() * 2 + 4 * (autismclient.gui.f.RenderState.init(14) + 2);
   }

   @Override
   public cH init_cH() {
      return new cH(this.init_I_2, this.load_I, this.read_I, this.write, this.init_Z, this.load_Z);
   }

   @Override
   public void init_V_cH(cH bounds) {
      cH clamped = this.init_cH_cH(bounds);
      this.init_I_2 = clamped.init_I;
      this.load_I = clamped.load_I;
      this.read_I = clamped.read;
      this.write = clamped.write;
      this.init_Z = clamped.init_Z;
      this.load_Z = clamped.load_Z;
      this.init_CharEncoder_2.write(!this.load_Z);
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
      if (this.init_Z || this.a__Z()) {
         this.write_Str = null;
         autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
         float uiMouseX = viewport.init_F_D(mouseX);
         float uiMouseY = viewport.load(mouseY);
         int contentWidth = this.tick_I_9();
         this.read_I = this.init_I_I_I(contentWidth, contentWidth);
         boolean active = bk.init_bk().update_Z(this) || bk.init_bk().write_Z(this);
         boolean headerHovered = uiMouseX >= this.init_I_2
            && uiMouseX < this.init_I_2 + this.read_I
            && uiMouseY >= this.load_I
            && uiMouseY < this.load_I + this.init_CharEncoder.load();
         autismclient.gui.f.c.SyncManager metrics = new autismclient.gui.f.c.SyncManager(
            context, init_Minecraft.font, viewport, this.init_CharEncoder, uiMouseX, uiMouseY, delta
         );
         this.init_CharEncoder_2.load_CharEncoder(!this.load_Z);
         this.init_CharEncoder_2.update(active);
         this.init_CharEncoder_2.sync(headerHovered);
         int contentHeight = Math.max(this.init_CharEncoder.load(), Math.round(this.init_CharEncoder_2.init_F_SyncManager_F(metrics, this.read_I)));
         this.write = this.load_Z ? contentHeight : Math.min(contentHeight, this.load_I_I_I(contentHeight, this.a__I()));
         cH clamped = this.init_cH_cH(new cH(this.init_I_2, this.load_I, this.read_I, this.write, this.init_Z, this.load_Z));
         if (!this.load_Z && clamped.write < contentHeight && this.read_I < contentWidth + 6) {
            this.read_I = contentWidth + 6;
            clamped = this.init_cH_cH(new cH(this.init_I_2, this.load_I, this.read_I, this.write, this.init_Z, this.load_Z));
         }

         this.init_I_2 = clamped.init_I;
         this.load_I = clamped.load_I;
         this.read_I = clamped.read;
         this.write = clamped.write;
         this.init_CharEncoder_2.init_TableIndex_F(this.init_I_2, this.load_I, this.read_I, this.write);
         this.init_TextFormat.init(context, mouseX, mouseY, delta);
         if (!this.load_Z && this.init_RefQueue != null && this.init_RefQueue.init_Z_2()) {
            context.nextStratum();
            viewport.init_V_GuiGraphicsExtract(context);

            try {
               this.init_RefQueue
                  .init_V_SyncManager(autismclient.gui.f.UiHelper.init(context, init_Minecraft.font, Math.round(uiMouseX), Math.round(uiMouseY)));
            } finally {
               viewport.load(context);
            }
         }

         if (!this.load_Z && this.write_Str != null) {
            context.nextStratum();
            this.init_V_GuiGraphicsExtract_2(context, viewport, this.write_Str, this.sync_F, this.flush_F);
         }
      }
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor context, autismclient.gui.f.c.RingBuffer viewport, String text, float uiX, float uiY) {
      viewport.init_V_GuiGraphicsExtract(context);

      try {
         h.init(
            autismclient.gui.f.UiHelper.init(context, init_Minecraft.font, Math.round(uiX), Math.round(uiY)),
            text,
            Math.round(uiX),
            Math.round(uiY),
            this.reset()
         );
      } finally {
         viewport.load(context);
      }
   }

   public static String init_Str_I_3(int keyCode) {
      return ValueSink.init_Str(keyCode);
   }

   @Override
   public boolean init_Z_D_3(double mouseX, double mouseY, int button) {
      if (!this.init_Z) {
         return false;
      }

      autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.init_F_D(mouseX);
      float uiMouseY = viewport.load(mouseY);
      if (this.init_RefQueue != null && this.init_RefQueue.init_Z_2()) {
         autismclient.gui.f.EventBus result = this.init_RefQueue.init_EventBus_I_2(Math.round(uiMouseX), Math.round(uiMouseY), button);
         if (!this.init_RefQueue.init_Z_2()) {
            this.init_RefQueue = null;
         }

         return result == autismclient.gui.f.EventBus.HANDLED;
      } else if (this.read_Z_F_F(uiMouseX, uiMouseY)) {
         this.init_Z = false;
         this.put = -1;
         this.write_Z = false;
         this.update_Z = false;
         return true;
      } else if (button == 0 && this.init_Z_F_F_2(uiMouseX, uiMouseY)) {
         this.write_Z = true;
         this.update_Z = false;
         this.load_F = uiMouseX - this.init_I_2;
         this.read_F = uiMouseY - this.load_I;
         this.write_F = uiMouseX;
         this.update_F = uiMouseY;
         this.get = this.init_I_2;
         this.set = this.load_I;
         return true;
      } else if (this.load_Z) {
         return this.init_Z_D_D(mouseX, mouseY);
      } else if (this.put >= 0 && ValueSink.load_Z(button)) {
         this.init_V_I_4(ValueSink.init_I(button));
         return true;
      } else {
         autismclient.gui.f.b.PoolEntry.Inner_DataUtil bodyBar = this.init_CharEncoder_2.init_PoolEntry_DataUtil();
         if (button == 0 && bodyBar != null && bodyBar.init(uiMouseX, uiMouseY)) {
            this.sync_Z = true;
            this.init_CharEncoder_2.init_V(true);
            this.reset = bodyBar.load(uiMouseX, uiMouseY) ? Math.round(uiMouseY) - bodyBar.update() : bodyBar.sync() / 2;
            this.init_CharEncoder_2.init(autismclient.gui.f.b.PoolEntry.init(bodyBar, uiMouseY, this.reset));
            return true;
         } else {
            return this.init_TextFormat.init_Z_D_2(mouseX, mouseY, button) ? true : this.init_Z_D_D(mouseX, mouseY);
         }
      }
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      if (!this.init_Z || this.load_Z) {
         return false;
      } else if (this.init_RefQueue != null && this.init_RefQueue.init_Z_2() && keyCode == 256) {
         this.init_RefQueue.load_V_2();
         this.init_RefQueue = null;
         return true;
      } else if (this.init_TextFormat.init_Z_I(keyCode, scanCode, modifiers)) {
         return true;
      } else if (this.init_Z && this.put >= 0) {
         this.init_V_I_4(autismclient.gui.f.b.EventBus.init(keyCode));
         return true;
      } else {
         return false;
      }
   }

   private void init_V_I_4(int bindCode) {
      for (int i = 0; i < this.init_List.size(); i++) {
         if (i != this.put && this.init_List.get(i).init_IntSupplier.getAsInt() == bindCode) {
            this.init_List.get(i).init_IntConsumer.accept(-1);
         }
      }

      this.init_List.get(this.put).init_IntConsumer.accept(bindCode);
      this.init_AutismConfig().save();
      this.put = -1;
   }

   @Override
   public boolean load_Z_D_3(double mouseX, double mouseY, int button) {
      if (this.init_RefQueue != null && this.init_RefQueue.init_Z_2()) {
         autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
         return this.init_RefQueue.load(Math.round(viewport.init_F_D(mouseX)), Math.round(viewport.load(mouseY)), button) == autismclient.gui.f.EventBus.HANDLED;
      } else if (this.write_Z) {
         this.write_Z = false;
         this.a__V();
         return true;
      } else if (this.sync_Z) {
         this.sync_Z = false;
         this.init_CharEncoder_2.init_V(false);
         return true;
      } else {
         return this.load_Z ? false : this.init_TextFormat.load(mouseX, mouseY, button);
      }
   }

   @Override
   public boolean init_Z_D(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.init_RefQueue != null && this.init_RefQueue.init_Z_2()) {
         autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
         return this.init_RefQueue.init_EventBus_I(Math.round(viewport.init_F_D(mouseX)), Math.round(viewport.load(mouseY)), button, deltaX, deltaY)
            == autismclient.gui.f.EventBus.HANDLED;
      }

      if (!this.write_Z) {
         if (this.sync_Z) {
            autismclient.gui.f.b.PoolEntry.Inner_DataUtil bodyBar = this.init_CharEncoder_2.init_PoolEntry_DataUtil();
            if (bodyBar != null) {
               this.init_CharEncoder_2.init(autismclient.gui.f.b.PoolEntry.init(bodyBar, this.init_TextFormat.init_RingBuffer().load(mouseY), this.reset));
            }

            return true;
         } else {
            return this.load_Z ? false : this.init_TextFormat.init(mouseX, mouseY, button, deltaX, deltaY);
         }
      } else {
         autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
         float uiMouseX = viewport.init_F_D(mouseX);
         float uiMouseY = viewport.load(mouseY);
         cH clamped = this.init_cH_cH(
            new cH(Math.round(uiMouseX - this.load_F), Math.round(uiMouseY - this.read_F), this.read_I, this.write, this.init_Z, this.load_Z)
         );
         this.init_I_2 = clamped.init_I;
         this.load_I = clamped.load_I;
         this.update_Z = this.update_Z
            || Math.abs(uiMouseX - this.write_F) >= 3.0F
            || Math.abs(uiMouseY - this.update_F) >= 3.0F
            || this.init_I_2 != this.get
            || this.load_I != this.set;
         return true;
      }
   }

   @Override
   public boolean init_Z_D_2(double mouseX, double mouseY, double amount) {
      if (this.load_Z) {
         return false;
      } else if (this.init_RefQueue != null && this.init_RefQueue.init_Z_2()) {
         autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
         return this.init_RefQueue.init_EventBus_I(Math.round(viewport.init_F_D(mouseX)), Math.round(viewport.load(mouseY)), amount)
            == autismclient.gui.f.EventBus.HANDLED;
      } else if (this.init_TextFormat.init_Z_D(mouseX, mouseY, amount)) {
         return true;
      } else if (this.init_CharEncoder_2.init_F_2() > 0.0F && this.init_Z_D_D(mouseX, mouseY)) {
         this.init_CharEncoder_2.init(this.init_CharEncoder_2.sync_F() - (float)amount * (autismclient.gui.f.RenderState.init(14) + 2));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      return this.init_Z && !this.load_Z ? this.init_TextFormat.init(chr, modifiers) : false;
   }

   @Override
   public boolean sync_Z() {
      return this.init_Z;
   }

   @Override
   public void init_V_Z(boolean v) {
      this.init_Z = v;
      if (!v) {
         this.put = -1;
         this.write_Z = false;
         this.update_Z = false;
         this.flush_V_6();
      } else {
         this.init_CharEncoder_2.write(!this.load_Z);
         bk.init_bk().update_V(this);
      }
   }

   public void load_V_9() {
      this.init_V_Z(!this.init_Z);
   }

   @Override
   public boolean init_Z_D_D(double mouseX, double mouseY) {
      if (!this.init_Z) {
         return false;
      }

      autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
      float uiX = viewport.init_F_D(mouseX);
      float uiY = viewport.load(mouseY);
      return uiX >= this.init_I_2 && uiX < this.init_I_2 + this.read_I && uiY >= this.load_I && uiY < this.load_I + this.write;
   }

   @Override
   public boolean load_Z_D_D(double mouseX, double mouseY) {
      if (!this.init_Z) {
         return false;
      }

      autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
      return this.init_Z_F_F_2(viewport.init_F_D(mouseX), viewport.load(mouseY));
   }

   private boolean init_Z_F_F_2(float uiMouseX, float uiMouseY) {
      return autismclient.gui.f.b.ClassCache.init(this.init_NetHelper_3(), this.init_CharEncoder.load(), false, true, uiMouseX, uiMouseY);
   }

   private boolean load_Z_F_F_2(float uiMouseX, float uiMouseY) {
      return autismclient.gui.f.b.ClassCache.read(this.init_NetHelper_3(), this.init_CharEncoder.load(), uiMouseX, uiMouseY);
   }

   @Override
   public boolean flush_Z() {
      return this.load_Z;
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public void write_V_Z(boolean c) {
      if (this.load_Z != c) {
         this.load_Z = c;
         this.init_CharEncoder_2.write(!this.load_Z);
         if (c) {
            this.put = -1;
            this.flush_V_6();
            this.get_V();
         }

         this.a__V();
      }
   }

   @Override
   public boolean write_Z() {
      return this.init_TextFormat.init_Z();
   }

   @Override
   public boolean e_() {
      return this.init_Z && this.put >= 0;
   }

   @Override
   public void push_V() {
      this.flush_V_6();
      this.init_TextFormat.load();
   }

   private boolean read_Z_F_F(float uiMouseX, float uiMouseY) {
      return autismclient.gui.f.b.ClassCache.init(this.init_NetHelper_3(), this.init_CharEncoder.load(), uiMouseX, uiMouseY);
   }

   private autismclient.gui.f.NetHelper init_NetHelper_3() {
      return autismclient.gui.f.NetHelper.init(this.init_I_2, this.load_I, this.read_I, Math.max(this.init_CharEncoder.load(), this.write));
   }

   private cH init_cH_cH(cH bounds) {
      autismclient.gui.f.c.RingBuffer viewport = this.init_TextFormat.init_RingBuffer();
      int margin = 4;
      int viewportW = Math.round(viewport.init());
      int viewportH = Math.round(viewport.load());
      int availableW = Math.max(1, viewportW - margin * 2);
      int availableH = Math.max(this.init_CharEncoder.load(), viewportH - margin * 2);
      int width = Math.max(Math.min(this.init_I(), availableW), Math.min(bounds.read, availableW));
      int minHeight = bounds.load_Z ? this.init_CharEncoder.load() : this.load_I();
      int height = Math.max(Math.min(minHeight, availableH), Math.min(bounds.write, availableH));
      int renderedHeight = bounds.load_Z ? this.init_CharEncoder.load() : height;
      int x = Math.max(margin, Math.min(bounds.init_I, Math.max(margin, viewportW - margin - width)));
      int y = Math.max(margin, Math.min(bounds.load_I, Math.max(margin, viewportH - margin - renderedHeight)));
      return new cH(x, y, width, height, bounds.init_Z, bounds.load_Z);
   }

   private int push_I_11() {
      if (init_Minecraft.font == null) {
         return this.prev();
      }

      if (this.add > 0) {
         return this.add;
      }

      List<String> labels = new ArrayList<>(this.init_List.size() + 9);
      labels.add("Inside GUI");
      labels.add("Custom Menu");
      labels.add("Overlay Scale");
      labels.add("Font Scale");
      labels.add("Menu Style");
      labels.add("Auto Probe");
      labels.add("InfiniChat");
      labels.add("Stop On Leave");
      labels.add("Command Prefix");

      for (AutismKeybindOverlay.Inner_CachePool entry : this.init_List) {
         labels.add(entry.init_Str);
      }

      this.add = Math.max(
         this.valid(),
         j.init(
               init_Minecraft.font,
               this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
               this.init_CharEncoder.load(autismclient.gui.f.b.l.read),
               labels
            )
            + this.size()
      );
      return this.add;
   }

   private int pop_I_11() {
      if (init_Minecraft.font == null) {
         return 112;
      }

      int keybindWidth = this.push_I_11() + this.data() + this.value();
      if (!AutismLiteVariant.enabled()) {
         int styleWidth = this.init_I_Str_I_3("DEFAULT", 47) + 2 + this.init_I_Str_I_3("CSGO", 34);
         keybindWidth = Math.max(keybindWidth, this.push_I_11() + this.data() + styleWidth);
      }

      return keybindWidth;
   }

   private int tick_I_9() {
      if (init_Minecraft.font == null) {
         return 120;
      }

      float fontScale = autismclient.gui.e.SyncManager.init_F();
      if (fontScale != this.close_F) {
         this.close_F = fontScale;
         this.clear = 0;
      }

      if (this.clear > 0) {
         return this.clear;
      }

      int headerReserve = this.add_I_7() * 2 + this.get_I_7() * 2 + 14;
      int titleWidth = autismclient.gui.f.b.k.init(
         init_Minecraft.font,
         "Settings",
         this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.init),
         this.init_CharEncoder.load(autismclient.gui.f.b.l.init)
      );
      this.clear = Math.max(this.run_I_9(), Math.max(this.pop_I_11() + this.get_I_7() * 2, titleWidth + headerReserve + this.get_I_7()));
      return this.clear;
   }

   private void init_V_Str_5(String requested) {
      String selected = z.init_Str(requested);
      autismclient.modules.PoolEntry module = autismclient.modules.PoolEntry.init_PoolEntry();
      if (module != null) {
         module.init_V_Str(selected);
      } else {
         this.init_AutismConfig().commandPrefix = selected;
         this.init_AutismConfig().save();
      }
   }

   private void flush_V_6() {
      if (this.init_RefQueue != null) {
         this.init_RefQueue.load_V_2();
      }

      this.init_RefQueue = null;
   }

   private int run_I_9() {
      return 120;
   }

   private int get_I_7() {
      return 4;
   }

   private int set_I_7() {
      return 0;
   }

   private int put() {
      return 20;
   }

   private int add_I_7() {
      return 10;
   }

   private int clear() {
      return 2;
   }

   private int reset() {
      return 170;
   }

   private int start_I_7() {
      return 3;
   }

   private int stop() {
      return 8;
   }

   private int next() {
      return 2;
   }

   private int prev() {
      return 82;
   }

   private int valid() {
      return 68;
   }

   private int size() {
      return 0;
   }

   private int data() {
      return 4;
   }

   private int value() {
      return 34;
   }

   private int cache() {
      return 2;
   }

   private int state() {
      return autismclient.gui.f.RenderState.init(13);
   }

   private static int queue() {
      return entry() - 2;
   }

   private static int buffer() {
      return Math.min(queue(), 11);
   }

   private int init_I_Str_I_3(String label, int fallback) {
      return init_Minecraft.font == null
         ? fallback
         : autismclient.gui.f.RenderState.load(
            autismclient.gui.f.b.k.init(
               init_Minecraft.font,
               label,
               this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
               this.init_CharEncoder.load(autismclient.gui.f.b.l.read)
            )
         );
   }

   private static int entry() {
      return autismclient.gui.f.RenderState.init(14);
   }

   private final class Inner_BufferHelper extends autismclient.gui.f.c.CachePool {
      private final int init_I_2;
      private final AutismKeybindOverlay.Inner_CachePool init_AutismKeybindOverl;
      private final autismclient.gui.f.c.PoolEntry init_PoolEntry;

      private Inner_BufferHelper(int index, AutismKeybindOverlay.Inner_CachePool entry) {
         super(
            new autismclient.gui.f.c.PoolEntry(entry.init_Str, autismclient.gui.f.b.l.read).load_PoolEntry(true),
            new AutismKeybindOverlay.Inner_StreamUtil(
               entry.init_IntSupplier,
               () -> AutismKeybindOverlay.this.put == index,
               () -> AutismKeybindOverlay.this.put = AutismKeybindOverlay.this.put == index ? -1 : index
            )
         );
         this.init_I_2 = index;
         this.init_AutismKeybindOverl = entry;
         this.write = 14.0F;
         this.read_Z = true;
         this.init_PoolEntry = (autismclient.gui.f.c.PoolEntry)this.init_TableIndex();
         this.init_CachePool_F(AutismKeybindOverlay.this.push_I_11());
         this.init_CachePool_I(AutismKeybindOverlay.this.data());
         this.init_CachePool_Z(true);
         this.init_CachePool_IoWorker(autismclient.gui.f.c.IoWorker.init_IoWorker);
      }

      @Override
      public float init_F_SyncManager(autismclient.gui.f.c.SyncManager context) {
         return AutismKeybindOverlay.this.read_I - AutismKeybindOverlay.this.get_I_7() * 2;
      }

      @Override
      public float init_F_SyncManager_F(autismclient.gui.f.c.SyncManager context, float availableWidth) {
         return AutismKeybindOverlay.entry();
      }

      @Override
      public void init_V_SyncManager(autismclient.gui.f.c.SyncManager context) {
         this.init_PoolEntry.init(autismclient.gui.f.b.l.read);
         super.init_V_SyncManager(context);
         if (this.init_PoolEntry.init_Z_F_F(context.init_F(), context.load())) {
            AutismKeybindOverlay.this.write_Str = this.init_AutismKeybindOverl.load;
            AutismKeybindOverlay.this.sync_F = context.init_F() + AutismKeybindOverlay.this.stop();
            AutismKeybindOverlay.this.flush_F = context.load() - AutismKeybindOverlay.this.next();
         }
      }

      @Override
      public boolean init_Z_SyncManager_2(autismclient.gui.f.c.SyncManager context, float mouseX, float mouseY, int button) {
         return super.init_Z_SyncManager_2(context, mouseX, mouseY, button);
      }
   }

   private static class Inner_CachePool {
      final String init_Str;
      final String load;
      final IntSupplier init_IntSupplier;
      final IntConsumer init_IntConsumer;

      Inner_CachePool(String label, String tooltip, IntSupplier getter, IntConsumer setter) {
         this.init_Str = label;
         this.load = tooltip;
         this.init_IntSupplier = getter;
         this.init_IntConsumer = setter;
      }
   }

   private final class Inner_ConfigStore extends autismclient.gui.f.c.CachePool {
      private final autismclient.gui.f.c.ByteReader init_ByteReader;
      private final autismclient.gui.f.c.ByteReader load_ByteReader;

      private Inner_ConfigStore() {
         super(
            new autismclient.gui.f.c.PoolEntry("Menu Style", autismclient.gui.f.b.l.read).load_PoolEntry(true),
            new autismclient.gui.f.c.StreamUtil().init(2).init(autismclient.gui.f.c.IoWorker.init_IoWorker)
         );
         autismclient.gui.f.c.StreamUtil choices = (autismclient.gui.f.c.StreamUtil)this.load_TableIndex();
         this.init_ByteReader = this.init("DEFAULT", "DEFAULT", AutismKeybindOverlay.this.init_I_Str_I_3("DEFAULT", 47));
         this.load_ByteReader = this.init("CSGO", "CSGO", AutismKeybindOverlay.this.init_I_Str_I_3("CSGO", 34));
         choices.init_TableIndex_TableIndex(this.init_ByteReader);
         choices.init_TableIndex_TableIndex(this.load_ByteReader);
         this.write = 14.0F;
         this.read_Z = true;
         this.init_CachePool_F(AutismKeybindOverlay.this.push_I_11());
         this.init_CachePool_I(AutismKeybindOverlay.this.data());
         this.init_CachePool_Z(true);
         this.init_CachePool_IoWorker(autismclient.gui.f.c.IoWorker.init_IoWorker);
      }

      private autismclient.gui.f.c.ByteReader init(String label, String style, int width) {
         return new autismclient.gui.f.c.ByteReader(label, autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY, () -> {
            AutismKeybindOverlay.this.init_AutismConfig().moduleMenuStyle = style;
            AutismKeybindOverlay.this.init_AutismConfig().save();
         }).read_ByteReader_F(width).init_ByteReader_F(width).load_ByteReader_F(width).init_ByteReader_I(5).load_ByteReader_I(AutismKeybindOverlay.queue());
      }

      @Override
      public float init_F_SyncManager_F(autismclient.gui.f.c.SyncManager context, float availableWidth) {
         return AutismKeybindOverlay.entry();
      }

      @Override
      public void init_V_SyncManager(autismclient.gui.f.c.SyncManager context) {
         boolean csgo = "CSGO".equals(AutismKeybindOverlay.this.init_AutismConfig().moduleMenuStyle);
         this.init_ByteReader
            .init_ByteReader_ByteReader_StreamU(
               csgo ? autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY : autismclient.gui.f.c.ByteReader.Inner_StreamUtil.PRIMARY
            );
         this.load_ByteReader
            .init_ByteReader_ByteReader_StreamU(
               csgo ? autismclient.gui.f.c.ByteReader.Inner_StreamUtil.PRIMARY : autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY
            );
         super.init_V_SyncManager(context);
         if (this.init_Z_F_F(context.init_F(), context.load())) {
            AutismKeybindOverlay.this.write_Str = "DEFAULT: movable category panels. CSGO: one window with a category sidebar. Changes apply immediately.";
            AutismKeybindOverlay.this.sync_F = context.init_F() + AutismKeybindOverlay.this.stop();
            AutismKeybindOverlay.this.flush_F = context.load() - AutismKeybindOverlay.this.next();
         }
      }
   }

   private final class Inner_DataUtil extends autismclient.gui.f.c.CachePool {
      private static final double[] init_DArr = new double[]{0.75, 0.85, 1.0, 1.15, 1.3};
      private final autismclient.gui.f.c.PoolEntry init_PoolEntry;
      private final autismclient.gui.f.c.ByteReader init_ByteReader;
      private final String init_Str = "Scales the custom UI font only (layout stays on Overlay Scale). Left-click increases, right-click decreases.";

      private Inner_DataUtil() {
         super(
            new autismclient.gui.f.c.PoolEntry("Font Scale", autismclient.gui.f.b.l.read).load_PoolEntry(true),
            new autismclient.gui.f.c.ByteReader("1x", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY, null)
               .read_ByteReader_F(AutismKeybindOverlay.this.value())
               .init_ByteReader_F(AutismKeybindOverlay.this.value())
               .load_ByteReader_F(AutismKeybindOverlay.this.value())
               .init_ByteReader_I(AutismKeybindOverlay.this.cache())
               .load_ByteReader_I(AutismKeybindOverlay.queue())
               .init_ByteReader_MethodUtil_DataUti(autismclient.gui.f.b.MethodUtil.init)
               .read_ByteReader_I(0)
         );
         this.init_PoolEntry = (autismclient.gui.f.c.PoolEntry)this.init_TableIndex();
         this.init_ByteReader = (autismclient.gui.f.c.ByteReader)this.load_TableIndex();
         this.init_ByteReader.init_ByteReader_Runnable(() -> this.init_V_I_2(1));
         this.write = 14.0F;
         this.read_Z = true;
         this.init_CachePool_F(AutismKeybindOverlay.this.push_I_11());
         this.init_CachePool_I(AutismKeybindOverlay.this.data());
         this.init_CachePool_Z(true);
         this.init_CachePool_IoWorker(autismclient.gui.f.c.IoWorker.init_IoWorker);
      }

      private void init_V_I_2(int direction) {
         double current = AutismKeybindOverlay.this.init_AutismConfig().uiFontScale;
         int index = 0;
         double best = 1.7976931348623157E308;

         for (int i = 0; i < init_DArr.length; i++) {
            double distance = Math.abs(init_DArr[i] - current);
            if (distance < best) {
               best = distance;
               index = i;
            }
         }

         index = Math.floorMod(index + direction, init_DArr.length);
         AutismKeybindOverlay.this.init_AutismConfig().uiFontScale = init_DArr[index];
         AutismKeybindOverlay.this.init_AutismConfig().save();
         AutismKeybindOverlay.this.init_TextFormat.init_V();
      }

      @Override
      public float init_F_SyncManager(autismclient.gui.f.c.SyncManager context) {
         return AutismKeybindOverlay.this.read_I - AutismKeybindOverlay.this.get_I_7() * 2;
      }

      @Override
      public float init_F_SyncManager_F(autismclient.gui.f.c.SyncManager context, float availableWidth) {
         return AutismKeybindOverlay.entry();
      }

      @Override
      public void init_V_SyncManager(autismclient.gui.f.c.SyncManager context) {
         double scale = AutismKeybindOverlay.this.init_AutismConfig().uiFontScale;
         this.init_ByteReader.init_ByteReader_Str(scale == 1.0 ? "1x" : String.format(Locale.ROOT, "%.2fx", scale).replaceAll("0+x$", "x"));
         this.init_ByteReader
            .init_ByteReader_ByteReader_StreamU(
               scale != 1.0 ? autismclient.gui.f.c.ByteReader.Inner_StreamUtil.PRIMARY : autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY
            );
         super.init_V_SyncManager(context);
         if (this.init_PoolEntry.init_Z_F_F(context.init_F(), context.load())) {
            AutismKeybindOverlay.this.write_Str = "Scales the custom UI font only (layout stays on Overlay Scale). Left-click increases, right-click decreases.";
            AutismKeybindOverlay.this.sync_F = context.init_F() + AutismKeybindOverlay.this.stop();
            AutismKeybindOverlay.this.flush_F = context.load() - AutismKeybindOverlay.this.next();
         }
      }

      @Override
      public boolean init_Z_SyncManager_2(autismclient.gui.f.c.SyncManager context, float mouseX, float mouseY, int button) {
         if ((button == 0 || button == 1) && this.init_ByteReader.init_Z_F_F(mouseX, mouseY)) {
            this.init_V_I_2(button == 1 ? -1 : 1);
            return true;
         } else {
            return super.init_Z_SyncManager_2(context, mouseX, mouseY, button);
         }
      }
   }

   private final class Inner_JsonUtil extends autismclient.gui.f.c.CachePool {
      private final BooleanSupplier init_BooleanSupplier;
      private final Consumer<Boolean> init_Consumer;
      private final String init_Str;
      private final autismclient.gui.f.c.PoolEntry init_PoolEntry;
      private final autismclient.gui.f.c.ByteReader init_ByteReader;

      private Inner_JsonUtil(String label, String tooltip, BooleanSupplier getter, Consumer<Boolean> setter) {
         super(
            new autismclient.gui.f.c.PoolEntry(label, autismclient.gui.f.b.l.read).load_PoolEntry(true),
            new autismclient.gui.f.c.ByteReader("OFF", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY, null)
               .load_ByteReader_Z(true)
               .read_ByteReader_F(22.0F)
               .init_ByteReader_F(22.0F)
               .load_ByteReader_F(22.0F)
               .init_ByteReader_I(AutismKeybindOverlay.this.cache())
               .load_ByteReader_I(AutismKeybindOverlay.buffer())
               .init_ByteReader_MethodUtil_DataUti(autismclient.gui.f.b.MethodUtil.init)
               .read_ByteReader_I(0)
         );
         this.init_BooleanSupplier = getter;
         this.init_Consumer = setter;
         this.init_Str = tooltip;
         this.init_PoolEntry = (autismclient.gui.f.c.PoolEntry)this.init_TableIndex();
         this.init_ByteReader = (autismclient.gui.f.c.ByteReader)this.load_TableIndex();
         this.init_ByteReader.init_ByteReader_Runnable(() -> {
            setter.accept(!getter.getAsBoolean());
            AutismKeybindOverlay.this.init_AutismConfig().save();
         });
         this.write = 14.0F;
         this.read_Z = true;
         this.init_CachePool_F(AutismKeybindOverlay.this.push_I_11());
         this.init_CachePool_I(AutismKeybindOverlay.this.data());
         this.init_CachePool_Z(true);
         this.init_CachePool_IoWorker(autismclient.gui.f.c.IoWorker.init_IoWorker);
      }

      @Override
      public float init_F_SyncManager(autismclient.gui.f.c.SyncManager context) {
         return AutismKeybindOverlay.this.read_I - AutismKeybindOverlay.this.get_I_7() * 2;
      }

      @Override
      public float init_F_SyncManager_F(autismclient.gui.f.c.SyncManager context, float availableWidth) {
         return AutismKeybindOverlay.entry();
      }

      @Override
      public void init_V_SyncManager(autismclient.gui.f.c.SyncManager context) {
         boolean on = this.init_BooleanSupplier.getAsBoolean();
         this.init_PoolEntry.init_V_SyncManager(context);
         autismclient.gui.f.SyncManager ui = autismclient.gui.f.UiHelper.init(
            context.init_GuiGraphicsExtract(), context.init_Font(), Math.round(context.init_F()), Math.round(context.load())
         );
         autismclient.gui.f.NetHelper pillBounds = autismclient.gui.f.NetHelper.init(
            Math.round(this.init_ByteReader.load_F()),
            Math.round(this.init_ByteReader.read_F()),
            Math.round(this.init_ByteReader.write_F()),
            Math.round(this.init_ByteReader.update_F())
         );
         g.init(ui, pillBounds, on, pillBounds.init_Z(ui.read(), ui.write()), "keybind:" + this.init_Str);
         if (this.init_PoolEntry.init_Z_F_F(context.init_F(), context.load())) {
            AutismKeybindOverlay.this.write_Str = this.init_Str;
            AutismKeybindOverlay.this.sync_F = context.init_F() + AutismKeybindOverlay.this.stop();
            AutismKeybindOverlay.this.flush_F = context.load() - AutismKeybindOverlay.this.next();
         }
      }
   }

   private final class Inner_NetHelper extends autismclient.gui.f.c.CachePool {
      private final autismclient.gui.f.c.PoolEntry init_PoolEntry;
      private final autismclient.gui.f.c.ByteReader init_ByteReader;
      private final String init_Str = "Scales every Autism overlay/window. Left-click increases, right-click decreases. 1x is default; 1.5x/2x are for 4K/high-DPI screens.";

      private Inner_NetHelper() {
         super(
            new autismclient.gui.f.c.PoolEntry("Overlay Scale", autismclient.gui.f.b.l.read).load_PoolEntry(true),
            new autismclient.gui.f.c.ByteReader("1x", autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY, null)
               .read_ByteReader_F(AutismKeybindOverlay.this.value())
               .init_ByteReader_F(AutismKeybindOverlay.this.value())
               .load_ByteReader_F(AutismKeybindOverlay.this.value())
               .init_ByteReader_I(AutismKeybindOverlay.this.cache())
               .load_ByteReader_I(AutismKeybindOverlay.queue())
               .init_ByteReader_MethodUtil_DataUti(autismclient.gui.f.b.MethodUtil.init)
               .read_ByteReader_I(0)
         );
         this.init_PoolEntry = (autismclient.gui.f.c.PoolEntry)this.init_TableIndex();
         this.init_ByteReader = (autismclient.gui.f.c.ByteReader)this.load_TableIndex();
         this.init_ByteReader.init_ByteReader_Runnable(() -> {
            cA.init_V_D(cA.load_D());
            AutismKeybindOverlay.this.init_TextFormat.init_V();
         });
         this.write = 14.0F;
         this.read_Z = true;
         this.init_CachePool_F(AutismKeybindOverlay.this.push_I_11());
         this.init_CachePool_I(AutismKeybindOverlay.this.data());
         this.init_CachePool_Z(true);
         this.init_CachePool_IoWorker(autismclient.gui.f.c.IoWorker.init_IoWorker);
      }

      @Override
      public float init_F_SyncManager(autismclient.gui.f.c.SyncManager context) {
         return AutismKeybindOverlay.this.read_I - AutismKeybindOverlay.this.get_I_7() * 2;
      }

      @Override
      public float init_F_SyncManager_F(autismclient.gui.f.c.SyncManager context, float availableWidth) {
         return AutismKeybindOverlay.entry();
      }

      @Override
      public void init_V_SyncManager(autismclient.gui.f.c.SyncManager context) {
         double scale = cA.init_D();
         this.init_ByteReader.init_ByteReader_Str(cA.init_Str_D(scale));
         this.init_ByteReader
            .init_ByteReader_ByteReader_StreamU(
               scale > 1.0 ? autismclient.gui.f.c.ByteReader.Inner_StreamUtil.PRIMARY : autismclient.gui.f.c.ByteReader.Inner_StreamUtil.SECONDARY
            );
         super.init_V_SyncManager(context);
         if (this.init_PoolEntry.init_Z_F_F(context.init_F(), context.load())) {
            AutismKeybindOverlay.this.write_Str = "Scales every Autism overlay/window. Left-click increases, right-click decreases. 1x is default; 1.5x/2x are for 4K/high-DPI screens.";
            AutismKeybindOverlay.this.sync_F = context.init_F() + AutismKeybindOverlay.this.stop();
            AutismKeybindOverlay.this.flush_F = context.load() - AutismKeybindOverlay.this.next();
         }
      }

      @Override
      public boolean init_Z_SyncManager_2(autismclient.gui.f.c.SyncManager context, float mouseX, float mouseY, int button) {
         if ((button == 0 || button == 1) && this.init_ByteReader.init_Z_F_F(mouseX, mouseY)) {
            cA.init_V_D(button == 1 ? cA.read() : cA.load_D());
            AutismKeybindOverlay.this.init_TextFormat.init_V();
            return true;
         } else {
            return super.init_Z_SyncManager_2(context, mouseX, mouseY, button);
         }
      }
   }

   private static final class Inner_StreamUtil extends autismclient.gui.f.c.TableIndex {
      private final IntSupplier init_IntSupplier;
      private final BooleanSupplier init_BooleanSupplier;
      private final Runnable init_Runnable;

      private Inner_StreamUtil(IntSupplier bindCode, BooleanSupplier capturing, Runnable onPress) {
         this.init_IntSupplier = bindCode;
         this.init_BooleanSupplier = capturing;
         this.init_Runnable = onPress;
         this.read_F = 34.0F;
         this.write = AutismKeybindOverlay.queue();
      }

      @Override
      public float init_F_SyncManager(autismclient.gui.f.c.SyncManager context) {
         return 34.0F;
      }

      @Override
      public float init_F_SyncManager_F(autismclient.gui.f.c.SyncManager context, float availableWidth) {
         return AutismKeybindOverlay.queue();
      }

      @Override
      public void init_V_SyncManager(autismclient.gui.f.c.SyncManager context) {
         if (this.init_Z) {
            autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(
               Math.round(this.init_F), Math.round(this.load_F), Math.round(this.read_F), Math.round(this.write)
            );
            boolean hovered = this.load_Z && this.init_Z_F_F(context.init_F(), context.load());
            autismclient.gui.f.b.EventBus.init(
               autismclient.gui.f.UiHelper.init(
                  context.init_GuiGraphicsExtract(), context.init_Font(), Math.round(context.init_F()), Math.round(context.load())
               ),
               bounds,
               this.init_IntSupplier.getAsInt(),
               this.init_BooleanSupplier.getAsBoolean(),
               hovered
            );
         }
      }

      @Override
      public boolean init_Z_SyncManager_2(autismclient.gui.f.c.SyncManager context, float mouseX, float mouseY, int button) {
         if (this.load_Z && this.init_Z_F_F(mouseX, mouseY) && button == 0) {
            if (this.init_Runnable != null) {
               this.init_Runnable.run();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   private final class Inner_TaskQueue extends autismclient.gui.f.c.TableIndex {
      private autismclient.gui.f.NetHelper init_NetHelper = autismclient.gui.f.NetHelper.init(0, 0, 1, 1);

      private Inner_TaskQueue() {
         this.write = 14.0F;
         this.read_Z = true;
      }

      @Override
      public float init_F_SyncManager(autismclient.gui.f.c.SyncManager context) {
         return AutismKeybindOverlay.this.read_I - AutismKeybindOverlay.this.get_I_7() * 2;
      }

      @Override
      public float init_F_SyncManager_F(autismclient.gui.f.c.SyncManager context, float availableWidth) {
         return AutismKeybindOverlay.entry();
      }

      @Override
      public void init_V_SyncManager(autismclient.gui.f.c.SyncManager context) {
         int rowX = Math.round(this.init_F);
         int rowY = Math.round(this.load_F);
         int rowW = Math.round(this.read_F);
         int rowH = Math.round(this.write);
         int controlW = AutismKeybindOverlay.this.value();
         int controlH = Math.min(rowH, AutismKeybindOverlay.queue());
         this.init_NetHelper = autismclient.gui.f.NetHelper.init(rowX + Math.max(0, rowW - controlW), rowY + (rowH - controlH) / 2, controlW, controlH);
         autismclient.gui.f.b.k.init(
            context.init_GuiGraphicsExtract(),
            context.init_Font(),
            "Command Prefix",
            AutismKeybindOverlay.this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
            AutismKeybindOverlay.this.init_CharEncoder.load(autismclient.gui.f.b.l.read),
            rowX,
            j.init(
               rowY, rowH, AutismKeybindOverlay.this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), AutismKeybindOverlay.this.init_CharEncoder.flush()
            ),
            Math.max(1, rowW - controlW - AutismKeybindOverlay.this.data()),
            false
         );
         String value = z.init_Str();
         autismclient.gui.f.b.UiHelper.init(
            autismclient.gui.f.UiHelper.init(context.init_GuiGraphicsExtract(), context.init_Font(), Math.round(context.init_F()), Math.round(context.load())),
            this.init_NetHelper,
            value,
            this.init_NetHelper.init_Z(Math.round(context.init_F()), Math.round(context.load())),
            AutismKeybindOverlay.this.init_RefQueue != null && AutismKeybindOverlay.this.init_RefQueue.init_Z_2()
         );
      }

      @Override
      public boolean init_Z_SyncManager_2(autismclient.gui.f.c.SyncManager context, float mouseX, float mouseY, int button) {
         if (button == 0 && this.init_NetHelper.init_Z(Math.round(mouseX), Math.round(mouseY))) {
            AutismKeybindOverlay.this.init_RefQueue = new autismclient.gui.f.b.RefQueue(
               this.init_NetHelper, z.init_List, z.init_Str(), AutismKeybindOverlay.this::init_V_Str_5
            );
            AutismKeybindOverlay.this.init_RefQueue.init_V_2();
            return true;
         } else {
            return false;
         }
      }
   }
}
