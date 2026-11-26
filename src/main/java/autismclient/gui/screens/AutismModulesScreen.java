package autismclient.gui.screens;

import autismclient.ai.SchematicWorkspace;
import autismclient.api.module.Setting;
import autismclient.compat263.Compat263;
import autismclient.gui.macro.editor.ActionEditorOverlay;
import autismclient.modules.Module;
import autismclient.modules.aD;
import autismclient.util.AutismAdminToolsOverlay;
import autismclient.util.AutismConfig;
import autismclient.util.AutismCustomFilterOverlay;
import autismclient.util.AutismCustomFilterPresetOverlay;
import autismclient.util.AutismFabricatorOverlay;
import autismclient.util.AutismKeybindOverlay;
import autismclient.util.AutismLANSyncOverlay;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismMacroEditorOverlay;
import autismclient.util.AutismMacroListOverlay;
import autismclient.util.AutismPacketLoggerOverlay;
import autismclient.util.AutismPacketSelectorOverlay;
import autismclient.util.AutismQueueEditorOverlay;
import autismclient.util.AutismServerInfoOverlay;
import autismclient.util.LauncherQuickActions;
import autismclient.util.AutismMacro;
import autismclient.util.OverlayManager;
import autismclient.util.cA;
import autismclient.util.OverlayBounds;
import autismclient.util.AutismOverlay;
import autismclient.util.AutismSessionState;
import autismclient.util.PacketFilterListCodec;
import autismclient.util.macro.ak;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class AutismModulesScreen extends VirtualScaledScreen implements autismclient.gui.ui.RetainedForegroundScreen {
   private static final Set<String> init_Set = new LinkedHashSet<>();
   private final Screen init_Screen;
   private final AutismModulesScreen.Inner_DataUtil init_HashUtil_DataUtil;
   private autismclient.gui.ui.menu.ModulesMenuController init_ConfigStore;
   private AutismPacketSelectorOverlay init_AutismPacketSelect;
   private AutismMacroListOverlay init_AutismMacroListOve;
   private AutismFabricatorOverlay init_AutismFabricatorOv;
   private AutismLANSyncOverlay init_AutismLANSyncOverl;
   private AutismQueueEditorOverlay init_AutismQueueEditorO;
   private AutismCustomFilterOverlay init_AutismCustomFilter;
   private AutismKeybindOverlay init_AutismKeybindOverl;
   private AutismAdminToolsOverlay init_AutismAdminToolsOv;
   private String init_Str;
   private final Set<String> load = new LinkedHashSet<>();
   private boolean init_Z;
   private long init_J;

   public AutismModulesScreen(Screen parent) {
      this(parent, AutismModulesScreen.Inner_DataUtil.IN_GAME);
   }

   public AutismModulesScreen(Screen parent, AutismModulesScreen.Inner_DataUtil mode) {
      super(Component.literal("Autism Modules"));
      this.init_Screen = parent;
      this.init_HashUtil_DataUtil = mode == null ? AutismModulesScreen.Inner_DataUtil.IN_GAME : mode;
   }

   public boolean init_Z_5() {
      return this.init_HashUtil_DataUtil == AutismModulesScreen.Inner_DataUtil.TITLE_SETUP;
   }

   public autismclient.gui.ui.menu.ModulesMenuController init_ConfigStore() {
      return this.init_ConfigStore;
   }

   public void init_V_Str_4(String id) {
      this.load(id);
   }

   @Override
   protected void init() {
      if (this.init_Z) {
         this.init_J = 0L;
         if (this.init_ConfigStore != null) {
            this.init_ConfigStore.read_V();
         }
      }

      this.init_Z = false;
      this.write();
      if (this.init_Z_5()) {
         this.flush();
      } else {
         this.sync();
      }

      if (this.init_ConfigStore == null) {
         this.init_ConfigStore = new autismclient.gui.ui.menu.ModulesMenuController(new AutismModulesScreen.Inner_StreamUtil());
      }

      this.init_ConfigStore.init_V();
      if (this.init_Str != null && !this.init_Str.isBlank()) {
         this.init_ConfigStore.init_V_Str(this.init_Str);
         this.init_Str = null;
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   public boolean load_Z_2() {
      return this.init_ConfigStore != null && this.init_ConfigStore.init_Z() || this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      if (this.init_J != 0L && System.nanoTime() - this.init_J >= 180000000L) {
         this.load_V_11();
      } else {
         if (this.init_ConfigStore == null) {
            this.init_ConfigStore = new autismclient.gui.ui.menu.ModulesMenuController(new AutismModulesScreen.Inner_StreamUtil());
            this.init_ConfigStore.init_V();
         }

         int mx = cA.init_I_D(mouseX);
         int my = cA.init_I_D(mouseY);
         boolean selectorOpen = this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z();
         this.init_V_Z_2(selectorOpen || this.init_ConfigStore.load_Z());
         boolean overlayBlocksMenuHover = !selectorOpen && !this.init_ConfigStore.load_Z() && OverlayManager.init_bk().load(mouseX, mouseY);
         int menuMouseX = overlayBlocksMenuHover ? -10000 : mx;
         int menuMouseY = overlayBlocksMenuHover ? -10000 : my;
         cA.init_V_GuiGraphicsExtract(graphics);

         try {
            if (selectorOpen) {
               autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.init_I(), this.load_I()), -1442511609);
               this.init_AutismPacketSelect.init_V_GuiGraphicsExtract_15(graphics, mx, my, delta);
            } else {
               this.init_ConfigStore.init_V_GuiGraphicsExtract(graphics, menuMouseX, menuMouseY, delta, this.init_I(), this.load_I());
            }
         } finally {
            cA.load_V_GuiGraphicsExtract(graphics);
         }

         if (!aD.init_Z() && !selectorOpen && !this.init_ConfigStore.read_Z() && !this.init_ConfigStore.load_Z()) {
            OverlayManager.init_bk().init(graphics, mouseX, mouseY, delta);
         }
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.init_J != 0L) {
         return true;
      } else {
         int mx = cA.init_I_D(event.x());
         int my = cA.init_I_D(event.y());
         if (this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z()) {
            return this.init_AutismPacketSelect.init_Z_D_3(mx, my, Compat263.mouseEventGlfwButton(event));
         } else {
            return this.init_ConfigStore != null
                  && !this.init_ConfigStore.load_Z()
                  && OverlayManager.init_bk().init_Z_D_2(event.x(), event.y(), Compat263.mouseEventGlfwButton(event))
               ? true
               : this.init_ConfigStore == null
                  || this.init_ConfigStore.init_Z_I(mx, my, Compat263.mouseEventGlfwButton(event), Compat263.mouseEventGlfwModifiers(event));
         }
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.init_J != 0L) {
         return true;
      } else {
         int mx = cA.init_I_D(event.x());
         int my = cA.init_I_D(event.y());
         if (this.init_AutismPacketSelect != null
            && this.init_AutismPacketSelect.sync_Z()
            && this.init_AutismPacketSelect.load_Z_D_3(mx, my, Compat263.mouseEventGlfwButton(event))) {
            return true;
         } else {
            return this.init_ConfigStore != null
                  && !this.init_ConfigStore.load_Z()
                  && OverlayManager.init_bk().load(event.x(), event.y(), Compat263.mouseEventGlfwButton(event))
               ? true
               : this.init_ConfigStore == null || this.init_ConfigStore.load_Z_I(mx, my, Compat263.mouseEventGlfwButton(event));
         }
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      if (this.init_J != 0L) {
         return true;
      } else {
         int mx = cA.init_I_D(event.x());
         int my = cA.init_I_D(event.y());
         if (this.init_AutismPacketSelect != null
            && this.init_AutismPacketSelect.sync_Z()
            && this.init_AutismPacketSelect.init_Z_D(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
            return true;
         } else {
            return this.init_ConfigStore != null
                  && !this.init_ConfigStore.load_Z()
                  && OverlayManager.init_bk().init(event.x(), event.y(), Compat263.mouseEventGlfwButton(event), dx, dy)
               ? true
               : this.init_ConfigStore == null || this.init_ConfigStore.init_Z_I(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy);
         }
      }
   }

   @Override
   public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
      if (this.init_J != 0L) {
         return true;
      } else {
         int mx = cA.init_I_D(x);
         int my = cA.init_I_D(y);
         if (this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z()) {
            return this.init_AutismPacketSelect.init_Z_D_2(mx, my, scrollY);
         } else {
            return this.init_ConfigStore != null && !this.init_ConfigStore.load_Z() && OverlayManager.init_bk().init_Z_D(x, y, scrollY)
               ? true
               : this.init_ConfigStore == null || this.init_ConfigStore.init_Z_I(mx, my, scrollY);
         }
      }
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (this.init_J != 0L) {
         return true;
      } else if (this.init_AutismPacketSelect != null
         && this.init_AutismPacketSelect.sync_Z()
         && this.init_AutismPacketSelect.init_Z_I(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))) {
         return true;
      } else if (this.init_ConfigStore != null
         && !this.init_ConfigStore.load_Z()
         && OverlayManager.init_bk().init_Z_I(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))) {
         return true;
      } else if (this.init_ConfigStore != null
         && this.init_ConfigStore.read_Z_I(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))) {
         return true;
      } else {
         return this.init(input, true) ? false : super.keyPressed(input);
      }
   }

   @Override
   public boolean keyReleased(KeyEvent input) {
      return this.init(input, false) ? false : super.keyReleased(input);
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      if (this.init_J != 0L) {
         return true;
      } else {
         char chr = (char)input.codepoint();
         if (this.init_AutismPacketSelect != null && this.init_AutismPacketSelect.sync_Z() && this.init_AutismPacketSelect.init_Z_C_I(chr, 0)) {
            return true;
         } else if (this.init_ConfigStore != null && !this.init_ConfigStore.load_Z() && OverlayManager.init_bk().init_Z_C_I(chr, 0)) {
            return true;
         } else {
            return this.init_ConfigStore != null && this.init_ConfigStore.init_Z_C(chr) ? true : super.charTyped(input);
         }
      }
   }

   @Override
   public void onClose() {
      if (this.init_ConfigStore != null) {
         this.init_ConfigStore.load_V();
      }

      this.load_V_11();
   }

   private void load_V_11() {
      this.read();
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.init_Screen);
      }
   }

   @Override
   public void removed() {
      this.read();
   }

   private void read() {
      if (!this.init_Z) {
         this.init_Z = true;
         if (this.init_ConfigStore != null) {
            this.init_ConfigStore.close();
         }

         OverlayManager.init_bk().close();
         this.push();
         this.update();
         this.close();
         this.poll();
      }
   }

   private void init(Module module, Setting<?, ?> option) {
      if (module != null && option != null) {
         if (this.init_AutismPacketSelect == null) {
            this.init_AutismPacketSelect = new AutismPacketSelectorOverlay(this.font);
         }

         boolean c2s = PacketFilterListCodec.init_Z(option.id());
         Set<Class<? extends Packet<?>>> selected = PacketFilterListCodec.init_Set(module.value(option.id()), c2s);
         if (c2s) {
            this.init_AutismPacketSelect
               .init_V_BiConsumer_Collection((packetClass, enabled) -> this.init(module, option, true, packetClass, enabled), selected);
         } else {
            this.init_AutismPacketSelect
               .load_V_BiConsumer_Collection((packetClass, enabled) -> this.init(module, option, false, packetClass, enabled), selected);
         }
      }
   }

   private void init(Module module, Setting<?, ?> option, boolean c2s, Class<? extends Packet<?>> packetClass, boolean selected) {
      Set<Class<? extends Packet<?>>> packets = new LinkedHashSet<>(PacketFilterListCodec.init_Set(module.value(option.id()), c2s));
      if (selected) {
         packets.add(packetClass);
      } else {
         packets.remove(packetClass);
      }

      String encoded = PacketFilterListCodec.init(packets);
      if (this.init_Z_5()) {
         module.setConfiguredValue(option.id(), encoded);
      } else {
         module.setValue(option.id(), encoded);
      }
   }

   private void write() {
      OverlayManager manager = OverlayManager.init_bk();
      if (!AutismLiteVariant.enabled()) {
         autismclient.ai.AiAssistant.init_ConfigStore().load_V();
         SchematicWorkspace.read_V();
      }

      this.init_AutismMacroListOve = this.init(AutismMacroListOverlay.class, null);
      if (this.init_AutismMacroListOve == null) {
         this.init_AutismMacroListOve = new AutismMacroListOverlay(this.font);
         this.init_AutismMacroListOve.update_V_11();
      }

      manager.init_V(this.init_AutismMacroListOve);
      AutismMacroEditorOverlay macroEditor = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
      this.init_AutismMacroListOve.load_V_Z_4(this.init_Z_5());
      if (macroEditor != null) {
         macroEditor.load_V_Z_3(this.init_Z_5());
      }

      if (this.init_Z_5()) {
         this.init_AutismLANSyncOverl = AutismLANSyncOverlay.init_AutismLANSyncOverl_Font(this.font);
         this.init_AutismLANSyncOverl.write_V_10();
         this.init_AutismLANSyncOverl.load(true);
         manager.init_V(this.init_AutismLANSyncOverl);
         this.init_AutismQueueEditorO = this.init(AutismQueueEditorOverlay.class, null);
         if (this.init_AutismQueueEditorO == null) {
            this.init_AutismQueueEditorO = new AutismQueueEditorOverlay(this.font);
            this.init_AutismQueueEditorO.sync_V_17();
         }

         this.init_AutismQueueEditorO.load(true);
         manager.init_V(this.init_AutismQueueEditorO);
         this.init_AutismCustomFilter = this.init(AutismCustomFilterOverlay.class, null);
         if (this.init_AutismCustomFilter == null) {
            OverlayBounds filterLayout = AutismSessionState.init_ck().init_cH_Str("autism-custom-filter");
            if (filterLayout != null && filterLayout.init_Z) {
               this.ensureCustomFilterOverlay(manager);
            }
         } else {
            manager.init_V(this.init_AutismCustomFilter);
            if (this.init_AutismCustomFilter.init_AutismCustomFilter() != null) {
               if (this.init(AutismCustomFilterPresetOverlay.class, null) == null) {
                  this.init_AutismCustomFilter.init_AutismCustomFilter().b__V();
               }

               manager.init_V(this.init_AutismCustomFilter.init_AutismCustomFilter());
            }
         }

         this.init_AutismKeybindOverl = this.init(AutismKeybindOverlay.class, null);
         if (this.init_AutismKeybindOverl == null) {
            this.init_AutismKeybindOverl = new AutismKeybindOverlay();
            this.init_AutismKeybindOverl.b__V();
         }

         manager.init_V(this.init_AutismKeybindOverl);
         manager.init_V_cX_Z(this.init_AutismMacroListOve, false);
         manager.init_V_cX_Z(this.init_AutismKeybindOverl, false);
         if (macroEditor != null) {
            if (this.init(AutismMacroEditorOverlay.class, null) == null) {
               macroEditor.poll_V_5();
            }

            manager.init_V(macroEditor);
            manager.init_V_cX_Z(macroEditor, false);
         }

         ActionEditorOverlay actionEditor = ActionEditorOverlay.init_ActionEditorOverla_2();
         actionEditor.read_V(true);
         manager.init_V(actionEditor);
         manager.init_V_cX_Z(actionEditor, false);
         autismclient.modules.ModuleRuntimeManager global = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
         if (global != null) {
            AutismPacketLoggerOverlay logger = global.init_AutismPacketLogger();
            if (logger != null) {
               logger.flush_V_13();
               logger.load(true);
               manager.init_V(logger);
            }

            AutismServerInfoOverlay serverInfo = global.init_AutismServerInfoOv();
            if (serverInfo != null) {
               serverInfo.update_V_21();
               serverInfo.load(true);
               manager.init_V(serverInfo);
            }
         }
      } else {
         if (this.init_Screen instanceof AbstractContainerScreen<?> handledScreen) {
            this.init_AutismFabricatorOv = this.init(AutismFabricatorOverlay.class, null);
            if (this.init_AutismFabricatorOv == null) {
               this.init_AutismFabricatorOv = AutismFabricatorOverlay.init_AutismFabricatorOv_AbstractContainerS(handledScreen);
               this.init_AutismFabricatorOv.write_V_8();
            }

            manager.init_V(this.init_AutismFabricatorOv);
         }

         this.init_AutismLANSyncOverl = this.init(AutismLANSyncOverlay.class, null);
         if (this.init_AutismLANSyncOverl == null) {
            this.init_AutismLANSyncOverl = AutismLANSyncOverlay.init_AutismLANSyncOverl_Font(this.font);
            this.init_AutismLANSyncOverl.write_V_10();
         }

         this.init_AutismLANSyncOverl.load(false);
         manager.init_V(this.init_AutismLANSyncOverl);
         this.init_AutismQueueEditorO = this.init(AutismQueueEditorOverlay.class, null);
         if (this.init_AutismQueueEditorO == null) {
            this.init_AutismQueueEditorO = new AutismQueueEditorOverlay(this.font);
            this.init_AutismQueueEditorO.sync_V_17();
         }

         this.init_AutismQueueEditorO.load(false);
         manager.init_V(this.init_AutismQueueEditorO);
         this.init_AutismCustomFilter = this.init(AutismCustomFilterOverlay.class, null);
         if (this.init_AutismCustomFilter == null) {
            OverlayBounds filterLayout = AutismSessionState.init_ck().init_cH_Str("autism-custom-filter");
            if (filterLayout != null && filterLayout.init_Z) {
               this.ensureCustomFilterOverlay(manager);
            }
         } else {
            manager.init_V(this.init_AutismCustomFilter);
            if (this.init_AutismCustomFilter.init_AutismCustomFilter() != null) {
               if (this.init(AutismCustomFilterPresetOverlay.class, null) == null) {
                  this.init_AutismCustomFilter.init_AutismCustomFilter().b__V();
               }

               manager.init_V(this.init_AutismCustomFilter.init_AutismCustomFilter());
            }
         }

         this.init_AutismKeybindOverl = this.init(AutismKeybindOverlay.class, null);
         if (this.init_AutismKeybindOverl == null) {
            this.init_AutismKeybindOverl = new AutismKeybindOverlay();
            this.init_AutismKeybindOverl.b__V();
         }

         manager.init_V(this.init_AutismKeybindOverl);
         this.init_AutismAdminToolsOv = this.init(AutismAdminToolsOverlay.class, null);
         if (this.init_AutismAdminToolsOv == null) {
            this.init_AutismAdminToolsOv = AutismAdminToolsOverlay.init_AutismAdminToolsOv();
            this.init_AutismAdminToolsOv.b__V();
         }

         manager.init_V(this.init_AutismAdminToolsOv);
         if (macroEditor != null) {
            if (this.init(AutismMacroEditorOverlay.class, null) == null) {
               macroEditor.poll_V_5();
            }

            manager.init_V(macroEditor);
         }

         ActionEditorOverlay actionEditor = ActionEditorOverlay.init_ActionEditorOverla_2();
         actionEditor.read_V(false);
         manager.init_V(actionEditor);
         autismclient.modules.ModuleRuntimeManager global = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
         if (global != null) {
            AutismPacketLoggerOverlay logger = global.init_AutismPacketLogger();
            if (logger != null) {
               logger.flush_V_13();
               logger.load(false);
               manager.init_V(logger);
            }

            AutismServerInfoOverlay serverInfo = global.init_AutismServerInfoOv();
            if (serverInfo != null) {
               serverInfo.update_V_21();
               serverInfo.load(false);
               manager.init_V(serverInfo);
            }
         }
      }
   }

   private void init_V_Z_2(boolean hidden) {
      if (hidden) {
         this.update();
      } else {
         this.sync();
      }
   }

   private void update() {
      init_Set.clear();
      this.init_V_cX(this.init_AutismMacroListOve);
      this.init_V_cX(this.init_AutismFabricatorOv);
      this.init_V_cX(this.init_AutismLANSyncOverl);
      this.init_V_cX(this.init_AutismQueueEditorO);
      this.init_V_cX(this.init_AutismCustomFilter);
      this.init_V_cX(this.init_AutismKeybindOverl);
      this.init_V_cX(this.init_AutismAdminToolsOv);
      if (!AutismLiteVariant.enabled()) {
         AutismOverlay assistant = autismclient.ai.AiAssistant.init_ConfigStore().init_cX();
         if (assistant != null && !assistant.f_()) {
            this.init_V_cX(assistant);
         }
      }

      autismclient.modules.ModuleRuntimeManager global = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
      if (global != null) {
         this.init_V_cX(global.load_AutismPacketLogger());
         this.init_V_cX(global.load_AutismServerInfoOv());
      }

      this.init_V_cX(AutismMacroEditorOverlay.init_AutismMacroEditorO_2());
      this.init_V_cX(ActionEditorOverlay.init_ActionEditorOverla_2());
      if (this.init_AutismCustomFilter != null) {
         this.init_V_cX(this.init_AutismCustomFilter.init_AutismCustomFilter());
      }
   }

   private void init_V_cX(AutismOverlay overlay) {
      if (overlay != null && overlay.sync_Z() && !overlay.f_()) {
         init_Set.add(overlay.init_Str());
         OverlayManager.init_bk().init_V_cX_Z(overlay, true);
      }
   }

   private void sync() {
      if (!init_Set.isEmpty()) {
         Set<String> restoreIds = new LinkedHashSet<>(init_Set);
         init_Set.clear();

         for (AutismOverlay overlay : OverlayManager.init_bk().init_List()) {
            if (overlay != null && restoreIds.contains(overlay.init_Str())) {
               OverlayManager.init_bk().init_V_cX_Z(overlay, false);
            }
         }
      }
   }

   private void flush() {
      OverlayManager manager = OverlayManager.init_bk();
      this.load.clear();

      for (AutismOverlay overlay : manager.init_List()) {
         if (overlay != null && overlay.sync_Z() && !this.init_Z_cX(overlay)) {
            this.load.add(overlay.init_Str());
            manager.init_V_cX_Z(overlay, true);
         }
      }
   }

   private boolean init_Z_cX(AutismOverlay overlay) {
      return "ai-assistant".equals(overlay.init_Str())
         || overlay == this.init_AutismMacroListOve
         || overlay == this.init_AutismLANSyncOverl
         || overlay == this.init_AutismQueueEditorO
         || overlay == this.init_AutismCustomFilter
         || this.init_AutismCustomFilter != null && overlay == this.init_AutismCustomFilter.init_AutismCustomFilter()
         || overlay == this.init_AutismKeybindOverl
         || autismclient.modules.ModuleRuntimeManager.init_PoolEntry() != null && overlay == autismclient.modules.ModuleRuntimeManager.init_PoolEntry().load_AutismPacketLogger()
         || autismclient.modules.ModuleRuntimeManager.init_PoolEntry() != null && overlay == autismclient.modules.ModuleRuntimeManager.init_PoolEntry().load_AutismServerInfoOv()
         || overlay == AutismMacroEditorOverlay.init_AutismMacroEditorO_2()
         || overlay == ActionEditorOverlay.init_ActionEditorOverla_2();
   }

   private void close() {
      if (!this.load.isEmpty()) {
         OverlayManager manager = OverlayManager.init_bk();

         for (AutismOverlay overlay : manager.init_List()) {
            if (overlay != null && this.load.contains(overlay.init_Str())) {
               manager.init_V_cX_Z(overlay, false);
            }
         }

         this.load.clear();
      }
   }

   private <T extends AutismOverlay> T init(Class<T> type, String overlayId) {
      for (AutismOverlay overlay : OverlayManager.init_bk().init_List()) {
         if (overlay != null && type.isInstance(overlay) && (overlayId == null || overlayId.equals(overlay.init_Str()))) {
            return type.cast(overlay);
         }
      }

      return null;
   }

   private AutismCustomFilterOverlay ensureCustomFilterOverlay(OverlayManager manager) {
      if (this.init_AutismCustomFilter == null) {
         this.init_AutismCustomFilter = this.init(AutismCustomFilterOverlay.class, null);
         if (this.init_AutismCustomFilter == null) {
            this.init_AutismCustomFilter = new AutismCustomFilterOverlay(this.font);
            this.init_AutismCustomFilter.b__V();
         }

         manager.init_V(this.init_AutismCustomFilter);
         if (this.init_AutismCustomFilter.init_AutismCustomFilter() != null) {
            if (this.init(AutismCustomFilterPresetOverlay.class, null) == null) {
               this.init_AutismCustomFilter.init_AutismCustomFilter().b__V();
            }

            manager.init_V(this.init_AutismCustomFilter.init_AutismCustomFilter());
         }
      }

      return this.init_AutismCustomFilter;
   }

   private void load(String id) {
      if ("schematics".equals(id)) {
         if (!AutismLiteVariant.enabled()) {
            SchematicWorkspace.load_V();
         }
      } else if ("ai".equals(id)) {
         if (!AutismLiteVariant.enabled()) {
            autismclient.ai.AiAssistant.init_ConfigStore().read();
         }
      } else if ("macros".equals(id)) {
         this.open();
      } else if ("keys".equals(id)) {
         this.load(this.init_AutismKeybindOverl);
      } else if (this.init_Z_5()) {
         switch (id) {
            case "lan":
               this.load(this.init_AutismLANSyncOverl);
               break;
            case "queue":
               this.load(this.init_AutismQueueEditorO);
               break;
            case "packets":
               this.load(this.ensureCustomFilterOverlay(OverlayManager.init_bk()));
               break;
            case "logger":
               autismclient.modules.ModuleRuntimeManager globalxx = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
               if (globalxx != null) {
                  this.load(globalxx.init_AutismPacketLogger());
               }
               break;
            case "server":
               autismclient.modules.ModuleRuntimeManager globalx = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
               if (globalx != null) {
                  this.load(globalx.init_AutismServerInfoOv());
               }
               break;
         }
      } else {
         autismclient.modules.ModuleRuntimeManager global = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
         if (global != null) {
            switch (id) {
               case "admin":
                  this.load(this.init_AutismAdminToolsOv);
                  break;
               case "lan":
                  this.load(this.init_AutismLANSyncOverl);
                  break;
               case "queue":
                  this.load(this.init_AutismQueueEditorO);
                  break;
               case "logger":
                  AutismPacketLoggerOverlay logger = global.init_AutismPacketLogger();
                  if (logger != null) {
                     logger.b__V();
                     OverlayManager.init_bk().init_V(logger);
                     this.load(logger);
                  }
                  break;
               case "packets":
                  this.load(this.ensureCustomFilterOverlay(OverlayManager.init_bk()));
                  break;
               case "server":
                  AutismServerInfoOverlay serverInfo = global.init_AutismServerInfoOv();
                  if (serverInfo != null) {
                     OverlayManager.init_bk().init_V(serverInfo);
                     this.load(serverInfo);
                  }
                  break;
               case "send":
                  LauncherQuickActions.init_V();
                  break;
               case "delay":
                  LauncherQuickActions.load_V();
                  break;
               case "flush":
                  LauncherQuickActions.read();
                  break;
               case "clear":
                  LauncherQuickActions.write();
            }
         }
      }
   }

   private void open() {
      AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
      if (editor != null) {
         OverlayManager.init_bk().init_V(editor);
      }

      if (editor != null && editor.sync_Z()) {
         if (this.init_AutismMacroListOve != null) {
            this.init_AutismMacroListOve.init_V_Z(false);
         }

         OverlayManager.init_bk().update_V(editor);
      } else {
         this.load(this.init_AutismMacroListOve);
      }
   }

   private void load(AutismOverlay overlay) {
      if (overlay != null) {
         OverlayManager.init_bk().init_V(overlay);
         overlay.init_V_Z(!overlay.sync_Z());
         if (overlay.sync_Z()) {
            OverlayManager.init_bk().update_V(overlay);
         }
      }
   }

   private AutismFabricatorOverlay init_AutismFabricatorOv() {
      if (this.init_Screen instanceof AbstractContainerScreen<?> handledScreen) {
         this.init_AutismFabricatorOv = AutismFabricatorOverlay.init_AutismFabricatorOv_AbstractContainerS(handledScreen);
         this.init_AutismFabricatorOv.write_V_8();
         OverlayManager.init_bk().init_V(this.init_AutismFabricatorOv);
         return this.init_AutismFabricatorOv;
      } else {
         return null;
      }
   }

   private void init_V_Module(Module module) {
      if (module != null) {
         AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
         if (editor != null) {
            editor.load_V_Z_3(this.init_Z_5());
            OverlayManager manager = OverlayManager.init_bk();
            manager.init_V(editor);
            manager.init_V_cX_Z(editor, false);
            if (editor.sync_Z() && AutismSessionState.init_ck().init_aM() != null) {
               editor.init_V_Z(true);
               manager.update_V(editor);
            } else {
               editor.init_V_aM_Z(null, true);
            }

            if (this.init_AutismMacroListOve != null) {
               this.init_AutismMacroListOve.init_V_Z(false);
            }

            editor.init_V_MacroAction_2(new ak(module.name()));
         }
      }
   }

   private void poll() {
      if (this.init_AutismMacroListOve != null) {
         this.init_AutismMacroListOve.load_V_Z_4(false);
      }

      if (this.init_AutismLANSyncOverl != null) {
         this.init_AutismLANSyncOverl.load(false);
      }

      if (this.init_AutismQueueEditorO != null) {
         this.init_AutismQueueEditorO.load(false);
      }

      ActionEditorOverlay actionEditor = ActionEditorOverlay.load_ActionEditorOverla();
      if (actionEditor != null) {
         actionEditor.read_V(false);
      }

      autismclient.modules.ModuleRuntimeManager global = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
      if (global != null) {
         AutismPacketLoggerOverlay logger = global.load_AutismPacketLogger();
         if (logger != null) {
            logger.load(false);
         }

         AutismServerInfoOverlay serverInfo = global.load_AutismServerInfoOv();
         if (serverInfo != null) {
            serverInfo.load(false);
         }
      }

      AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
      if (editor != null) {
         editor.load_V_Z_3(false);
      }
   }

   private void push() {
      this.read(this.init_AutismMacroListOve);
      this.read(this.init_AutismFabricatorOv);
      this.read(this.init_AutismLANSyncOverl);
      this.read(this.init_AutismQueueEditorO);
      this.read(this.init_AutismCustomFilter);
      this.read(this.init_AutismKeybindOverl);
      this.read(this.init_AutismAdminToolsOv);
      autismclient.modules.ModuleRuntimeManager global = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
      if (global != null) {
         this.read(global.load_AutismPacketLogger());
         this.read(global.load_AutismServerInfoOv());
      }

      this.read(AutismMacroEditorOverlay.init_AutismMacroEditorO_2());
      this.read(ActionEditorOverlay.init_ActionEditorOverla_2());
      if (this.init_AutismCustomFilter != null) {
         this.read(this.init_AutismCustomFilter.init_AutismCustomFilter());
      }
   }

   private void read(AutismOverlay overlay) {
      if (overlay != null) {
         if (overlay instanceof AutismMacroListOverlay macroList) {
            macroList.write_V_13();
         } else if (overlay instanceof AutismFabricatorOverlay fabricator) {
            fabricator.read_V_3();
         } else if (overlay instanceof AutismLANSyncOverlay lanSync) {
            lanSync.read_V_5();
         } else if (overlay instanceof AutismQueueEditorOverlay queue) {
            queue.update_V_20();
         } else if (overlay instanceof AutismCustomFilterOverlay filter) {
            filter.a__V();
         } else if (overlay instanceof AutismKeybindOverlay keybind) {
            keybind.a__V();
         } else if (overlay instanceof AutismPacketLoggerOverlay logger) {
            logger.write_V_18();
         } else if (overlay instanceof AutismServerInfoOverlay serverInfo) {
            serverInfo.write_V_24();
         } else if (overlay instanceof AutismMacroEditorOverlay macroEditor) {
            macroEditor.open_V_7();
         } else {
            overlay.a__V();
         }
      }
   }

   private boolean init(KeyEvent input, boolean down) {
      if (this.load_Z_2()) {
         return false;
      }

      if (this.minecraft != null && this.minecraft.options != null) {
         KeyMapping[] movement = new KeyMapping[]{
            this.minecraft.options.keyUp,
            this.minecraft.options.keyDown,
            this.minecraft.options.keyLeft,
            this.minecraft.options.keyRight,
            this.minecraft.options.keyJump,
            this.minecraft.options.keyShift,
            this.minecraft.options.keySprint
         };

         for (KeyMapping key : movement) {
            if (key != null && key.matches(input)) {
               key.setDown(down);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public enum Inner_DataUtil {
      IN_GAME,
      TITLE_SETUP;
   }

   private final class Inner_StreamUtil implements autismclient.gui.ui.menu.ModulesMenuController.Inner_BufferHelper {
      @Override
      public boolean init_Z() {
         return AutismModulesScreen.this.init_Z_5();
      }

      @Override
      public Screen init_Screen() {
         return AutismModulesScreen.this;
      }

      @Override
      public Font init_Font() {
         return AutismModulesScreen.this.font;
      }

      @Override
      public void init_V() {
         AutismModulesScreen.this.onClose();
      }

      @Override
      public void load() {
         AutismConfig.getGlobal().save();
      }

      @Override
      public void init(Module module, Setting<?, ?> option) {
         AutismModulesScreen.this.init(module, option);
      }

      @Override
      public void load(Module module, Setting<?, ?> option) {
         if (AutismModulesScreen.this.minecraft != null) {
            AutismModulesScreen.this.init_Str = module == null ? null : module.id();
            AutismModulesScreen.this.minecraft.gui.setScreen(new StringListEditorScreen(AutismModulesScreen.this, module, option));
         }
      }

      @Override
      public void read(Module module, Setting<?, ?> option) {
         if (AutismModulesScreen.this.minecraft != null) {
            AutismModulesScreen.this.init_Str = module == null ? null : module.id();
            AutismModulesScreen.this.minecraft.gui.setScreen(new RegistrySelectorScreen(AutismModulesScreen.this, module, option));
         }
      }

      @Override
      public void write(Module module, Setting<?, ?> option) {
         this.init(module, option, null);
      }

      @Override
      public void init(Module module, Setting<?, ?> option, AutismMacro macro) {
         AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
         if (editor != null) {
            editor.load_V_Z_3(AutismModulesScreen.this.init_Z_5());
            OverlayManager manager = OverlayManager.init_bk();
            manager.init_V(editor);
            manager.init_V_cX_Z(editor, false);
            editor.init_V_aM_Z(macro, true);
            editor.init_V_Z(true);
            manager.update_V(editor);
            if (AutismModulesScreen.this.init_AutismMacroListOve != null) {
               AutismModulesScreen.this.init_AutismMacroListOve.init_V_Z(false);
            }
         }
      }

      @Override
      public void init(String id) {
         AutismModulesScreen.this.load(id);
      }

      @Override
      public void init(Module module) {
         AutismModulesScreen.this.init_V_Module(module);
      }
   }
}
