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
   private static final Set<String> hiddenOverlayIds = new LinkedHashSet<>();
   private final Screen parent;
   private final AutismModulesScreen.ScreenMode mode;
   private autismclient.gui.ui.menu.ModulesMenuController menuController;
   private AutismPacketSelectorOverlay packetSelectorOverlay;
   private AutismMacroListOverlay macroListOverlay;
   private AutismFabricatorOverlay fabricatorOverlay;
   private AutismLANSyncOverlay lanSyncOverlay;
   private AutismQueueEditorOverlay queueEditorOverlay;
   private AutismCustomFilterOverlay customFilterOverlay;
   private AutismKeybindOverlay keybindOverlay;
   private AutismAdminToolsOverlay adminToolsOverlay;
   private String selectedModuleId;
   private final Set<String> stashedOverlayIds = new LinkedHashSet<>();
   private boolean menuClosed;
   private long closeAnimStart;

   public AutismModulesScreen(Screen parent) {
      this(parent, AutismModulesScreen.ScreenMode.IN_GAME);
   }

   public AutismModulesScreen(Screen parent, AutismModulesScreen.ScreenMode mode) {
      super(Component.literal("Autism Modules"));
      this.parent = parent;
      this.mode = mode == null ? AutismModulesScreen.ScreenMode.IN_GAME : mode;
   }

   public boolean isTitleSetup() {
      return this.mode == AutismModulesScreen.ScreenMode.TITLE_SETUP;
   }

   public autismclient.gui.ui.menu.ModulesMenuController menuController() {
      return this.menuController;
   }

   public void runMenuAction(String id) {
      this.openTool(id);
   }

   @Override
   protected void init() {
      if (this.menuClosed) {
         this.closeAnimStart = 0L;
         if (this.menuController != null) {
            this.menuController.resumeMenu();
         }
      }

      this.menuClosed = false;
      this.setupOverlays();
      if (this.isTitleSetup()) {
         this.stashForeignOverlays();
      } else {
         this.restoreHiddenOverlays();
      }

      if (this.menuController == null) {
         this.menuController = new autismclient.gui.ui.menu.ModulesMenuController(new AutismModulesScreen.MenuHost());
      }

      this.menuController.restoreState();
      if (this.selectedModuleId != null && !this.selectedModuleId.isBlank()) {
         this.menuController.openModule(this.selectedModuleId);
         this.selectedModuleId = null;
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   public boolean hasOpenOverlay() {
      return this.menuController != null && this.menuController.isCapturingInput() || this.packetSelectorOverlay != null && this.packetSelectorOverlay.isVisible();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      if (this.closeAnimStart != 0L && System.nanoTime() - this.closeAnimStart >= 180000000L) {
         this.closeToParent();
      } else {
         if (this.menuController == null) {
            this.menuController = new autismclient.gui.ui.menu.ModulesMenuController(new AutismModulesScreen.MenuHost());
            this.menuController.restoreState();
         }

         int mx = cA.init_I_D(mouseX);
         int my = cA.init_I_D(mouseY);
         boolean selectorOpen = this.packetSelectorOverlay != null && this.packetSelectorOverlay.isVisible();
         this.setOverlaysHidden(selectorOpen || this.menuController.hasPopup());
         boolean overlayBlocksMenuHover = !selectorOpen && !this.menuController.hasPopup() && OverlayManager.getInstance().isMouseOverCached(mouseX, mouseY);
         int menuMouseX = overlayBlocksMenuHover ? -10000 : mx;
         int menuMouseY = overlayBlocksMenuHover ? -10000 : my;
         cA.init_V_GuiGraphicsExtract(graphics);

         try {
            if (selectorOpen) {
               autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.virtualWidth(), this.virtualHeight()), -1442511609);
               this.packetSelectorOverlay.render(graphics, mx, my, delta);
            } else {
               this.menuController.render(graphics, menuMouseX, menuMouseY, delta, this.virtualWidth(), this.virtualHeight());
            }
         } finally {
            cA.load_V_GuiGraphicsExtract(graphics);
         }

         if (!aD.init_Z() && !selectorOpen && !this.menuController.isModuleOpen() && !this.menuController.hasPopup()) {
            OverlayManager.getInstance().render(graphics, mouseX, mouseY, delta);
         }
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (this.closeAnimStart != 0L) {
         return true;
      } else {
         int mx = cA.init_I_D(event.x());
         int my = cA.init_I_D(event.y());
         if (this.packetSelectorOverlay != null && this.packetSelectorOverlay.isVisible()) {
            return this.packetSelectorOverlay.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event));
         } else {
            return this.menuController != null
                  && !this.menuController.hasPopup()
                  && OverlayManager.getInstance().mouseClicked(event.x(), event.y(), Compat263.mouseEventGlfwButton(event))
               ? true
               : this.menuController == null
                  || this.menuController.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event), Compat263.mouseEventGlfwModifiers(event));
         }
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      if (this.closeAnimStart != 0L) {
         return true;
      } else {
         int mx = cA.init_I_D(event.x());
         int my = cA.init_I_D(event.y());
         if (this.packetSelectorOverlay != null
            && this.packetSelectorOverlay.isVisible()
            && this.packetSelectorOverlay.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event))) {
            return true;
         } else {
            return this.menuController != null
                  && !this.menuController.hasPopup()
                  && OverlayManager.getInstance().mouseReleased(event.x(), event.y(), Compat263.mouseEventGlfwButton(event))
               ? true
               : this.menuController == null || this.menuController.mouseReleased(mx, my, Compat263.mouseEventGlfwButton(event));
         }
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      if (this.closeAnimStart != 0L) {
         return true;
      } else {
         int mx = cA.init_I_D(event.x());
         int my = cA.init_I_D(event.y());
         if (this.packetSelectorOverlay != null
            && this.packetSelectorOverlay.isVisible()
            && this.packetSelectorOverlay.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy)) {
            return true;
         } else {
            return this.menuController != null
                  && !this.menuController.hasPopup()
                  && OverlayManager.getInstance().mouseDragged(event.x(), event.y(), Compat263.mouseEventGlfwButton(event), dx, dy)
               ? true
               : this.menuController == null || this.menuController.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), dx, dy);
         }
      }
   }

   @Override
   public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
      if (this.closeAnimStart != 0L) {
         return true;
      } else {
         int mx = cA.init_I_D(x);
         int my = cA.init_I_D(y);
         if (this.packetSelectorOverlay != null && this.packetSelectorOverlay.isVisible()) {
            return this.packetSelectorOverlay.mouseScrolled(mx, my, scrollY);
         } else {
            return this.menuController != null && !this.menuController.hasPopup() && OverlayManager.getInstance().mouseScrolled(x, y, scrollY)
               ? true
               : this.menuController == null || this.menuController.mouseScrolled(mx, my, scrollY);
         }
      }
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (this.closeAnimStart != 0L) {
         return true;
      } else if (this.packetSelectorOverlay != null
         && this.packetSelectorOverlay.isVisible()
         && this.packetSelectorOverlay.keyPressed(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))) {
         return true;
      } else if (this.menuController != null
         && !this.menuController.hasPopup()
         && OverlayManager.getInstance().keyPressed(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))) {
         return true;
      } else if (this.menuController != null
         && this.menuController.keyPressed(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))) {
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
      if (this.closeAnimStart != 0L) {
         return true;
      } else {
         char chr = (char)input.codepoint();
         if (this.packetSelectorOverlay != null && this.packetSelectorOverlay.isVisible() && this.packetSelectorOverlay.charTyped(chr, 0)) {
            return true;
         } else if (this.menuController != null && !this.menuController.hasPopup() && OverlayManager.getInstance().charTyped(chr, 0)) {
            return true;
         } else {
            return this.menuController != null && this.menuController.charTyped(chr) ? true : super.charTyped(input);
         }
      }
   }

   @Override
   public void onClose() {
      if (this.menuController != null) {
         this.menuController.onClose();
      }

      this.closeToParent();
   }

   private void closeToParent() {
      this.closeMenu();
      if (this.minecraft != null) {
         this.minecraft.gui.setScreen(this.parent);
      }
   }

   @Override
   public void removed() {
      this.closeMenu();
   }

   private void closeMenu() {
      if (!this.menuClosed) {
         this.menuClosed = true;
         if (this.menuController != null) {
            this.menuController.closeProfilesView();
         }

         OverlayManager.getInstance().restoreStashedIfFits();
         this.saveOverlayStates();
         this.hideTransientOverlays();
         this.restoreStashedOverlays();
         this.leaveConfigurationMode();
      }
   }

   private void init(Module module, Setting<?, ?> option) {
      if (module != null && option != null) {
         if (this.packetSelectorOverlay == null) {
            this.packetSelectorOverlay = new AutismPacketSelectorOverlay(this.font);
         }

         boolean c2s = PacketFilterListCodec.isC2sOption(option.id());
         Set<Class<? extends Packet<?>>> selected = PacketFilterListCodec.parse(module.value(option.id()), c2s);
         if (c2s) {
            this.packetSelectorOverlay
               .openC2sMultiSelect((packetClass, enabled) -> this.togglePacketSelection(module, option, true, packetClass, enabled), selected);
         } else {
            this.packetSelectorOverlay
               .openS2cMultiSelect((packetClass, enabled) -> this.togglePacketSelection(module, option, false, packetClass, enabled), selected);
         }
      }
   }

   private void togglePacketSelection(Module module, Setting<?, ?> option, boolean c2s, Class<? extends Packet<?>> packetClass, boolean selected) {
      Set<Class<? extends Packet<?>>> packets = new LinkedHashSet<>(PacketFilterListCodec.parse(module.value(option.id()), c2s));
      if (selected) {
         packets.add(packetClass);
      } else {
         packets.remove(packetClass);
      }

      String encoded = PacketFilterListCodec.encode(packets);
      if (this.isTitleSetup()) {
         module.setConfiguredValue(option.id(), encoded);
      } else {
         module.setValue(option.id(), encoded);
      }
   }

   private void setupOverlays() {
      OverlayManager manager = OverlayManager.getInstance();
      if (!AutismLiteVariant.enabled()) {
         autismclient.ai.AiAssistant.getInstance().restoreOverlay();
         SchematicWorkspace.restoreWindow();
      }

      this.macroListOverlay = this.init(AutismMacroListOverlay.class, null);
      if (this.macroListOverlay == null) {
         this.macroListOverlay = new AutismMacroListOverlay(this.font);
         this.macroListOverlay.restoreState();
      }

      manager.register(this.macroListOverlay);
      AutismMacroEditorOverlay macroEditor = AutismMacroEditorOverlay.getInstance();
      this.macroListOverlay.setConfigurationOnly(this.isTitleSetup());
      if (macroEditor != null) {
         macroEditor.setConfigurationOnly(this.isTitleSetup());
      }

      if (this.isTitleSetup()) {
         this.lanSyncOverlay = AutismLANSyncOverlay.getOrCreate(this.font);
         this.lanSyncOverlay.restoreState();
         this.lanSyncOverlay.setConfigurationOnly(true);
         manager.register(this.lanSyncOverlay);
         this.queueEditorOverlay = this.init(AutismQueueEditorOverlay.class, null);
         if (this.queueEditorOverlay == null) {
            this.queueEditorOverlay = new AutismQueueEditorOverlay(this.font);
            this.queueEditorOverlay.restoreState();
         }

         this.queueEditorOverlay.setConfigurationOnly(true);
         manager.register(this.queueEditorOverlay);
         this.customFilterOverlay = this.init(AutismCustomFilterOverlay.class, null);
         if (this.customFilterOverlay == null) {
            OverlayBounds filterLayout = AutismSessionState.getInstance().getOverlayBounds("autism-custom-filter");
            if (filterLayout != null && filterLayout.visible) {
               this.ensureCustomFilterOverlay(manager);
            }
         } else {
            manager.register(this.customFilterOverlay);
            if (this.customFilterOverlay.getPresetOverlay() != null) {
               if (this.init(AutismCustomFilterPresetOverlay.class, null) == null) {
                  this.customFilterOverlay.getPresetOverlay().loadBounds();
               }

               manager.register(this.customFilterOverlay.getPresetOverlay());
            }
         }

         this.keybindOverlay = this.init(AutismKeybindOverlay.class, null);
         if (this.keybindOverlay == null) {
            this.keybindOverlay = new AutismKeybindOverlay();
            this.keybindOverlay.loadBounds();
         }

         manager.register(this.keybindOverlay);
         manager.setHidden(this.macroListOverlay, false);
         manager.setHidden(this.keybindOverlay, false);
         if (macroEditor != null) {
            if (this.init(AutismMacroEditorOverlay.class, null) == null) {
               macroEditor.restoreState();
            }

            manager.register(macroEditor);
            manager.setHidden(macroEditor, false);
         }

         ActionEditorOverlay actionEditor = ActionEditorOverlay.getInstance();
         actionEditor.setConfigurationOnly(true);
         manager.register(actionEditor);
         manager.setHidden(actionEditor, false);
         autismclient.modules.ModuleRuntimeManager global = autismclient.modules.ModuleRuntimeManager.getInstance();
         if (global != null) {
            AutismPacketLoggerOverlay logger = global.getOrCreatePacketLoggerOverlay();
            if (logger != null) {
               logger.restoreState();
               logger.setConfigurationOnly(true);
               manager.register(logger);
            }

            AutismServerInfoOverlay serverInfo = global.getOrCreateServerInfoOverlay();
            if (serverInfo != null) {
               serverInfo.restoreState();
               serverInfo.setConfigurationOnly(true);
               manager.register(serverInfo);
            }
         }
      } else {
         if (this.parent instanceof AbstractContainerScreen<?> handledScreen) {
            this.fabricatorOverlay = this.init(AutismFabricatorOverlay.class, null);
            if (this.fabricatorOverlay == null) {
               this.fabricatorOverlay = AutismFabricatorOverlay.getOrCreate(handledScreen);
               this.fabricatorOverlay.restoreState();
            }

            manager.register(this.fabricatorOverlay);
         }

         this.lanSyncOverlay = this.init(AutismLANSyncOverlay.class, null);
         if (this.lanSyncOverlay == null) {
            this.lanSyncOverlay = AutismLANSyncOverlay.getOrCreate(this.font);
            this.lanSyncOverlay.restoreState();
         }

         this.lanSyncOverlay.setConfigurationOnly(false);
         manager.register(this.lanSyncOverlay);
         this.queueEditorOverlay = this.init(AutismQueueEditorOverlay.class, null);
         if (this.queueEditorOverlay == null) {
            this.queueEditorOverlay = new AutismQueueEditorOverlay(this.font);
            this.queueEditorOverlay.restoreState();
         }

         this.queueEditorOverlay.setConfigurationOnly(false);
         manager.register(this.queueEditorOverlay);
         this.customFilterOverlay = this.init(AutismCustomFilterOverlay.class, null);
         if (this.customFilterOverlay == null) {
            OverlayBounds filterLayout = AutismSessionState.getInstance().getOverlayBounds("autism-custom-filter");
            if (filterLayout != null && filterLayout.visible) {
               this.ensureCustomFilterOverlay(manager);
            }
         } else {
            manager.register(this.customFilterOverlay);
            if (this.customFilterOverlay.getPresetOverlay() != null) {
               if (this.init(AutismCustomFilterPresetOverlay.class, null) == null) {
                  this.customFilterOverlay.getPresetOverlay().loadBounds();
               }

               manager.register(this.customFilterOverlay.getPresetOverlay());
            }
         }

         this.keybindOverlay = this.init(AutismKeybindOverlay.class, null);
         if (this.keybindOverlay == null) {
            this.keybindOverlay = new AutismKeybindOverlay();
            this.keybindOverlay.loadBounds();
         }

         manager.register(this.keybindOverlay);
         this.adminToolsOverlay = this.init(AutismAdminToolsOverlay.class, null);
         if (this.adminToolsOverlay == null) {
            this.adminToolsOverlay = AutismAdminToolsOverlay.getInstance();
            this.adminToolsOverlay.loadBounds();
         }

         manager.register(this.adminToolsOverlay);
         if (macroEditor != null) {
            if (this.init(AutismMacroEditorOverlay.class, null) == null) {
               macroEditor.restoreState();
            }

            manager.register(macroEditor);
         }

         ActionEditorOverlay actionEditor = ActionEditorOverlay.getInstance();
         actionEditor.setConfigurationOnly(false);
         manager.register(actionEditor);
         autismclient.modules.ModuleRuntimeManager global = autismclient.modules.ModuleRuntimeManager.getInstance();
         if (global != null) {
            AutismPacketLoggerOverlay logger = global.getOrCreatePacketLoggerOverlay();
            if (logger != null) {
               logger.restoreState();
               logger.setConfigurationOnly(false);
               manager.register(logger);
            }

            AutismServerInfoOverlay serverInfo = global.getOrCreateServerInfoOverlay();
            if (serverInfo != null) {
               serverInfo.restoreState();
               serverInfo.setConfigurationOnly(false);
               manager.register(serverInfo);
            }
         }
      }
   }

   private void setOverlaysHidden(boolean hidden) {
      if (hidden) {
         this.hideTransientOverlays();
      } else {
         this.restoreHiddenOverlays();
      }
   }

   private void hideTransientOverlays() {
      hiddenOverlayIds.clear();
      this.hideTransientOverlay(this.macroListOverlay);
      this.hideTransientOverlay(this.fabricatorOverlay);
      this.hideTransientOverlay(this.lanSyncOverlay);
      this.hideTransientOverlay(this.queueEditorOverlay);
      this.hideTransientOverlay(this.customFilterOverlay);
      this.hideTransientOverlay(this.keybindOverlay);
      this.hideTransientOverlay(this.adminToolsOverlay);
      if (!AutismLiteVariant.enabled()) {
         AutismOverlay assistant = autismclient.ai.AiAssistant.getInstance().getOverlay();
         if (assistant != null && !assistant.isPersistent()) {
            this.hideTransientOverlay(assistant);
         }
      }

      autismclient.modules.ModuleRuntimeManager global = autismclient.modules.ModuleRuntimeManager.getInstance();
      if (global != null) {
         this.hideTransientOverlay(global.getPacketLoggerOverlay());
         this.hideTransientOverlay(global.getServerInfoOverlay());
      }

      this.hideTransientOverlay(AutismMacroEditorOverlay.getInstance());
      this.hideTransientOverlay(ActionEditorOverlay.getInstance());
      if (this.customFilterOverlay != null) {
         this.hideTransientOverlay(this.customFilterOverlay.getPresetOverlay());
      }
   }

   private void hideTransientOverlay(AutismOverlay overlay) {
      if (overlay != null && overlay.isVisible() && !overlay.isPersistent()) {
         hiddenOverlayIds.add(overlay.getOverlayId());
         OverlayManager.getInstance().setHidden(overlay, true);
      }
   }

   private void restoreHiddenOverlays() {
      if (!hiddenOverlayIds.isEmpty()) {
         Set<String> restoreIds = new LinkedHashSet<>(hiddenOverlayIds);
         hiddenOverlayIds.clear();

         for (AutismOverlay overlay : OverlayManager.getInstance().getOverlays()) {
            if (overlay != null && restoreIds.contains(overlay.getOverlayId())) {
               OverlayManager.getInstance().setHidden(overlay, false);
            }
         }
      }
   }

   private void stashForeignOverlays() {
      OverlayManager manager = OverlayManager.getInstance();
      this.stashedOverlayIds.clear();

      for (AutismOverlay overlay : manager.getOverlays()) {
         if (overlay != null && overlay.isVisible() && !this.isOwnedOverlay(overlay)) {
            this.stashedOverlayIds.add(overlay.getOverlayId());
            manager.setHidden(overlay, true);
         }
      }
   }

   private boolean isOwnedOverlay(AutismOverlay overlay) {
      return "ai-assistant".equals(overlay.getOverlayId())
         || overlay == this.macroListOverlay
         || overlay == this.lanSyncOverlay
         || overlay == this.queueEditorOverlay
         || overlay == this.customFilterOverlay
         || this.customFilterOverlay != null && overlay == this.customFilterOverlay.getPresetOverlay()
         || overlay == this.keybindOverlay
         || autismclient.modules.ModuleRuntimeManager.getInstance() != null && overlay == autismclient.modules.ModuleRuntimeManager.getInstance().getPacketLoggerOverlay()
         || autismclient.modules.ModuleRuntimeManager.getInstance() != null && overlay == autismclient.modules.ModuleRuntimeManager.getInstance().getServerInfoOverlay()
         || overlay == AutismMacroEditorOverlay.getInstance()
         || overlay == ActionEditorOverlay.getInstance();
   }

   private void restoreStashedOverlays() {
      if (!this.stashedOverlayIds.isEmpty()) {
         OverlayManager manager = OverlayManager.getInstance();

         for (AutismOverlay overlay : manager.getOverlays()) {
            if (overlay != null && this.stashedOverlayIds.contains(overlay.getOverlayId())) {
               manager.setHidden(overlay, false);
            }
         }

         this.stashedOverlayIds.clear();
      }
   }

   private <T extends AutismOverlay> T init(Class<T> type, String overlayId) {
      for (AutismOverlay overlay : OverlayManager.getInstance().getOverlays()) {
         if (overlay != null && type.isInstance(overlay) && (overlayId == null || overlayId.equals(overlay.getOverlayId()))) {
            return type.cast(overlay);
         }
      }

      return null;
   }

   private AutismCustomFilterOverlay ensureCustomFilterOverlay(OverlayManager manager) {
      if (this.customFilterOverlay == null) {
         this.customFilterOverlay = this.init(AutismCustomFilterOverlay.class, null);
         if (this.customFilterOverlay == null) {
            this.customFilterOverlay = new AutismCustomFilterOverlay(this.font);
            this.customFilterOverlay.loadBounds();
         }

         manager.register(this.customFilterOverlay);
         if (this.customFilterOverlay.getPresetOverlay() != null) {
            if (this.init(AutismCustomFilterPresetOverlay.class, null) == null) {
               this.customFilterOverlay.getPresetOverlay().loadBounds();
            }

            manager.register(this.customFilterOverlay.getPresetOverlay());
         }
      }

      return this.customFilterOverlay;
   }

   private void openTool(String id) {
      if ("schematics".equals(id)) {
         if (!AutismLiteVariant.enabled()) {
            SchematicWorkspace.toggleWindow();
         }
      } else if ("ai".equals(id)) {
         if (!AutismLiteVariant.enabled()) {
            autismclient.ai.AiAssistant.getInstance().toggle();
         }
      } else if ("macros".equals(id)) {
         this.toggleMacroEditor();
      } else if ("keys".equals(id)) {
         this.toggleOverlay(this.keybindOverlay);
      } else if (this.isTitleSetup()) {
         switch (id) {
            case "lan":
               this.toggleOverlay(this.lanSyncOverlay);
               break;
            case "queue":
               this.toggleOverlay(this.queueEditorOverlay);
               break;
            case "packets":
               this.toggleOverlay(this.ensureCustomFilterOverlay(OverlayManager.getInstance()));
               break;
            case "logger":
               autismclient.modules.ModuleRuntimeManager globalxx = autismclient.modules.ModuleRuntimeManager.getInstance();
               if (globalxx != null) {
                  this.toggleOverlay(globalxx.getOrCreatePacketLoggerOverlay());
               }
               break;
            case "server":
               autismclient.modules.ModuleRuntimeManager globalx = autismclient.modules.ModuleRuntimeManager.getInstance();
               if (globalx != null) {
                  this.toggleOverlay(globalx.getOrCreateServerInfoOverlay());
               }
               break;
         }
      } else {
         autismclient.modules.ModuleRuntimeManager global = autismclient.modules.ModuleRuntimeManager.getInstance();
         if (global != null) {
            switch (id) {
               case "admin":
                  this.toggleOverlay(this.adminToolsOverlay);
                  break;
               case "lan":
                  this.toggleOverlay(this.lanSyncOverlay);
                  break;
               case "queue":
                  this.toggleOverlay(this.queueEditorOverlay);
                  break;
               case "logger":
                  AutismPacketLoggerOverlay logger = global.getOrCreatePacketLoggerOverlay();
                  if (logger != null) {
                     logger.loadBounds();
                     OverlayManager.getInstance().register(logger);
                     this.toggleOverlay(logger);
                  }
                  break;
               case "packets":
                  this.toggleOverlay(this.ensureCustomFilterOverlay(OverlayManager.getInstance()));
                  break;
               case "server":
                  AutismServerInfoOverlay serverInfo = global.getOrCreateServerInfoOverlay();
                  if (serverInfo != null) {
                     OverlayManager.getInstance().register(serverInfo);
                     this.toggleOverlay(serverInfo);
                  }
                  break;
               case "send":
                  LauncherQuickActions.toggleSendPackets();
                  break;
               case "delay":
                  LauncherQuickActions.toggleDelayPackets();
                  break;
               case "flush":
                  LauncherQuickActions.flushQueue();
                  break;
               case "clear":
                  LauncherQuickActions.clearQueue();
            }
         }
      }
   }

   private void toggleMacroEditor() {
      AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.getInstance();
      if (editor != null) {
         OverlayManager.getInstance().register(editor);
      }

      if (editor != null && editor.isVisible()) {
         if (this.macroListOverlay != null) {
            this.macroListOverlay.setVisible(false);
         }

         OverlayManager.getInstance().bringToFront(editor);
      } else {
         this.toggleOverlay(this.macroListOverlay);
      }
   }

   private void toggleOverlay(AutismOverlay overlay) {
      if (overlay != null) {
         OverlayManager.getInstance().register(overlay);
         overlay.setVisible(!overlay.isVisible());
         if (overlay.isVisible()) {
            OverlayManager.getInstance().bringToFront(overlay);
         }
      }
   }

   private AutismFabricatorOverlay fabricatorOverlay() {
      if (this.parent instanceof AbstractContainerScreen<?> handledScreen) {
         this.fabricatorOverlay = AutismFabricatorOverlay.getOrCreate(handledScreen);
         this.fabricatorOverlay.restoreState();
         OverlayManager.getInstance().register(this.fabricatorOverlay);
         return this.fabricatorOverlay;
      } else {
         return null;
      }
   }

   private void addModuleToMacro(Module module) {
      if (module != null) {
         AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.getInstance();
         if (editor != null) {
            editor.setConfigurationOnly(this.isTitleSetup());
            OverlayManager manager = OverlayManager.getInstance();
            manager.register(editor);
            manager.setHidden(editor, false);
            if (editor.isVisible() && AutismSessionState.getInstance().getEditingMacro() != null) {
               editor.setVisible(true);
               manager.bringToFront(editor);
            } else {
               editor.openReturningToList(null, true);
            }

            if (this.macroListOverlay != null) {
               this.macroListOverlay.setVisible(false);
            }

            editor.appendAction(new ak(module.name()));
         }
      }
   }

   private void leaveConfigurationMode() {
      if (this.macroListOverlay != null) {
         this.macroListOverlay.setConfigurationOnly(false);
      }

      if (this.lanSyncOverlay != null) {
         this.lanSyncOverlay.setConfigurationOnly(false);
      }

      if (this.queueEditorOverlay != null) {
         this.queueEditorOverlay.setConfigurationOnly(false);
      }

      ActionEditorOverlay actionEditor = ActionEditorOverlay.getExisting();
      if (actionEditor != null) {
         actionEditor.setConfigurationOnly(false);
      }

      autismclient.modules.ModuleRuntimeManager global = autismclient.modules.ModuleRuntimeManager.getInstance();
      if (global != null) {
         AutismPacketLoggerOverlay logger = global.getPacketLoggerOverlay();
         if (logger != null) {
            logger.setConfigurationOnly(false);
         }

         AutismServerInfoOverlay serverInfo = global.getServerInfoOverlay();
         if (serverInfo != null) {
            serverInfo.setConfigurationOnly(false);
         }
      }

      AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.getInstance();
      if (editor != null) {
         editor.setConfigurationOnly(false);
      }
   }

   private void saveOverlayStates() {
      this.saveOverlayState(this.macroListOverlay);
      this.saveOverlayState(this.fabricatorOverlay);
      this.saveOverlayState(this.lanSyncOverlay);
      this.saveOverlayState(this.queueEditorOverlay);
      this.saveOverlayState(this.customFilterOverlay);
      this.saveOverlayState(this.keybindOverlay);
      this.saveOverlayState(this.adminToolsOverlay);
      autismclient.modules.ModuleRuntimeManager global = autismclient.modules.ModuleRuntimeManager.getInstance();
      if (global != null) {
         this.saveOverlayState(global.getPacketLoggerOverlay());
         this.saveOverlayState(global.getServerInfoOverlay());
      }

      this.saveOverlayState(AutismMacroEditorOverlay.getInstance());
      this.saveOverlayState(ActionEditorOverlay.getInstance());
      if (this.customFilterOverlay != null) {
         this.saveOverlayState(this.customFilterOverlay.getPresetOverlay());
      }
   }

   private void saveOverlayState(AutismOverlay overlay) {
      if (overlay != null) {
         if (overlay instanceof AutismMacroListOverlay macroList) {
            macroList.saveState();
         } else if (overlay instanceof AutismFabricatorOverlay fabricator) {
            fabricator.saveState();
         } else if (overlay instanceof AutismLANSyncOverlay lanSync) {
            lanSync.saveState();
         } else if (overlay instanceof AutismQueueEditorOverlay queue) {
            queue.saveState();
         } else if (overlay instanceof AutismCustomFilterOverlay filter) {
            filter.saveBounds();
         } else if (overlay instanceof AutismKeybindOverlay keybind) {
            keybind.saveBounds();
         } else if (overlay instanceof AutismPacketLoggerOverlay logger) {
            logger.saveState();
         } else if (overlay instanceof AutismServerInfoOverlay serverInfo) {
            serverInfo.saveState();
         } else if (overlay instanceof AutismMacroEditorOverlay macroEditor) {
            macroEditor.saveState();
         } else {
            overlay.saveBounds();
         }
      }
   }

   private boolean init(KeyEvent input, boolean down) {
      if (this.hasOpenOverlay()) {
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

   public enum ScreenMode {
      IN_GAME,
      TITLE_SETUP;
   }

   private final class MenuHost implements autismclient.gui.ui.menu.ModulesMenuController.ControllerHost {
      @Override
      public boolean isTitleSetup() {
         return AutismModulesScreen.this.isTitleSetup();
      }

      @Override
      public Screen screen() {
         return AutismModulesScreen.this;
      }

      @Override
      public Font font() {
         return AutismModulesScreen.this.font;
      }

      @Override
      public void closeScreen() {
         AutismModulesScreen.this.onClose();
      }

      @Override
      public void saveConfig() {
         AutismConfig.getGlobal().save();
      }

      @Override
      public void openPacketSelector(Module module, Setting<?, ?> option) {
         AutismModulesScreen.this.init(module, option);
      }

      @Override
      public void openStringListEditor(Module module, Setting<?, ?> option) {
         if (AutismModulesScreen.this.minecraft != null) {
            AutismModulesScreen.this.selectedModuleId = module == null ? null : module.id();
            AutismModulesScreen.this.minecraft.gui.setScreen(new StringListEditorScreen(AutismModulesScreen.this, module, option));
         }
      }

      @Override
      public void openRegistrySelector(Module module, Setting<?, ?> option) {
         if (AutismModulesScreen.this.minecraft != null) {
            AutismModulesScreen.this.selectedModuleId = module == null ? null : module.id();
            AutismModulesScreen.this.minecraft.gui.setScreen(new RegistrySelectorScreen(AutismModulesScreen.this, module, option));
         }
      }

      @Override
      public void openMacroEditor(Module module, Setting<?, ?> option) {
         this.openMacroEditor(module, option, null);
      }

      @Override
      public void openMacroEditor(Module module, Setting<?, ?> option, AutismMacro macro) {
         AutismMacroEditorOverlay editor = AutismMacroEditorOverlay.getInstance();
         if (editor != null) {
            editor.setConfigurationOnly(AutismModulesScreen.this.isTitleSetup());
            OverlayManager manager = OverlayManager.getInstance();
            manager.register(editor);
            manager.setHidden(editor, false);
            editor.openReturningToList(macro, true);
            editor.setVisible(true);
            manager.bringToFront(editor);
            if (AutismModulesScreen.this.macroListOverlay != null) {
               AutismModulesScreen.this.macroListOverlay.setVisible(false);
            }
         }
      }

      @Override
      public void openToolById(String id) {
         AutismModulesScreen.this.openTool(id);
      }

      @Override
      public void addToMacro(Module module) {
         AutismModulesScreen.this.addModuleToMacro(module);
      }
   }
}
