package autismclient.mixin;

import autismclient.c.StreamUtil;
import autismclient.gui.d.ArrayUtil;
import autismclient.gui.d.ByteReader;
import autismclient.gui.d.MapStore;
import autismclient.gui.d.PathHelper;
import autismclient.gui.d.RingBuffer;
import autismclient.gui.d.TableIndex;
import autismclient.gui.d.a;
import autismclient.gui.f.RenderState;
import autismclient.gui.f.UiHelper;
import autismclient.gui.f.b.h;
import autismclient.modules.PoolEntry;
import autismclient.modules.aD;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.aA;
import autismclient.util.aN;
import autismclient.util.ProxyEntry;
import autismclient.util.ProxyManager;
import autismclient.util.cA;
import autismclient.util.g.PacketUtil;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList.Entry;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList.OnlineServerEntry;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = JoinMultiplayerScreen.class, priority = 2000)
public abstract class AutismMultiplayerScreenMixin extends Screen implements StreamUtil {
   @Unique
   private static final int BUTTON_HEIGHT = 20;
   @Unique
   private static final int BUTTON_WIDTH = 60;
   @Unique
   private static final int MACRO_BUTTON_WIDTH = 50;
   @Unique
   private static final int MULTI_BUTTON_WIDTH = 50;
   @Unique
   private static final int STACK_WIDTH = 104;
   @Unique
   private static final int MARGIN = 4;
   @Unique
   private static final int GAP = 3;
   @Unique
   private static final int TOP_BUTTON_HEIGHT = 16;
   @Unique
   private static final int EXTERNAL_NONE = 0;
   @Unique
   private static final int EXTERNAL_VIA_FABRIC_PLUS = 1;
   @Unique
   private static final int EXTERNAL_REPLAY_RECORD = 2;
   @Unique
   private static final int EXTERNAL_OPSEC = 3;
   @Unique
   private Button autism$accountsButton;
   @Unique
   private Button autism$joinMacroButton;
   @Unique
   private Button autism$multiButton;
   @Unique
   private Button autism$proxiesButton;
   @Unique
   private Button autism$spoofButton;
   @Unique
   private Button autism$packsButton;
   @Unique
   private Button autism$libraryButton;
   @Unique
   private Button autism$recordButton;
   @Unique
   private boolean autism$voicePromptChecked;
   @Unique
   private boolean autism$meteorUiConfigSuppressed;
   @Unique
   private int autism$topButtonsLeft = 4;
   @Shadow
   protected ServerSelectionList serverSelectionList;

   protected AutismMultiplayerScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "repositionElements", at = @At("TAIL"))
   private void autism$repositionElements(CallbackInfo ci) {
      this.autism$layoutButtons();
      this.autism$maybeShowVoiceChatPrompt();
   }

   @Inject(method = "tick", at = @At("TAIL"))
   private void autism$refreshMultiButton(CallbackInfo ci) {
      if (this.autism$multiButton != null) {
         String label = autism$multiLabel();
         if (!label.equals(this.autism$multiButton.getMessage().getString())) {
            this.autism$multiButton.setMessage(Component.literal(label));
         }
      }
   }

   @Unique
   private static String autism$multiLabel() {
      if (AutismLiteVariant.enabled()) {
         return "Multi";
      }

      PacketUtil manager = PacketUtil.init_PacketUtil();
      return manager.load_Z() ? "Multi " + manager.read_Str() : "Multi";
   }

   @Unique
   private void autism$maybeShowVoiceChatPrompt() {
      if (!this.autism$voicePromptChecked) {
         this.autism$voicePromptChecked = true;
         if (!aD.init_Z() && FabricLoader.getInstance().isModLoaded("voicechat")) {
            PoolEntry module = PoolEntry.init_PoolEntry();
            if (module != null && module.tick_Z()) {
               AutismConfig config = AutismConfig.getGlobal();
               if (config != null && !config.voiceChatModdedPromptShown) {
                  config.voiceChatModdedPromptShown = true;
                  config.save();
                  Screen parent = this;
                  this.minecraft.execute(() -> {
                     if (this.minecraft.gui.screen() == parent) {
                        this.minecraft.gui.setScreen(new a(parent));
                     }
                  });
               }
            }
         }
      }
   }

   @Unique
   private void autism$layoutButtons() {
      this.autism$suppressMeteorWidgets();
      AbstractWidget via = null;
      AbstractWidget opsec = null;

      for (GuiEventListener child : this.children()) {
         if (child instanceof AbstractWidget widget && !this.autism$isOwned(widget)) {
            int kind = this.autism$externalKind(widget);
            if (kind == 2) {
               autism$setVisible(widget, false);
            } else if (kind == 1) {
               via = widget;
            } else if (kind == 3) {
               opsec = widget;
            }
         }
      }

      boolean hidden = aD.init_Z();
      if (hidden) {
         autism$setVisible(this.autism$accountsButton, false);
         autism$setVisible(this.autism$joinMacroButton, false);
         autism$setVisible(this.autism$multiButton, false);
         autism$setVisible(this.autism$proxiesButton, false);
         autism$setVisible(this.autism$spoofButton, false);
         autism$setVisible(this.autism$packsButton, false);
         autism$setVisible(this.autism$libraryButton, false);
         autism$setVisible(this.autism$recordButton, false);
         autism$setVisible(via, false);
         autism$setVisible(opsec, false);
      } else {
         this.autism$ensureOwnedButtons();
         int topRight = this.width - 4;
         int topAvailable = Math.max(4, this.width - 8);
         int topGap = 2;
         boolean lite = AutismLiteVariant.enabled();
         int topCell = lite ? Math.max(1, (topAvailable - topGap * 3) / 4) : Math.max(1, (topAvailable - topGap * 4) / 5);
         int accountsW = Math.min(this.autism$hugWidth("Accounts"), topCell);
         int proxiesW = Math.min(this.autism$hugWidth("Proxies"), topCell);
         int libraryW = Math.min(this.autism$hugWidth("Library"), topCell);
         int macroW = Math.min(this.autism$hugWidth("Macro"), topCell);
         int cursor = topRight;
         cursor -= accountsW;
         autism$place(this.autism$accountsButton, cursor, 4, accountsW, 16);
         cursor -= topGap + proxiesW;
         autism$place(this.autism$proxiesButton, cursor, 4, proxiesW, 16);
         cursor -= topGap + libraryW;
         this.autism$libraryButton.setMessage(this.autism$fitLabel(libraryW, "Library"));
         autism$place(this.autism$libraryButton, cursor, 4, libraryW, 16);
         cursor -= topGap + macroW;
         autism$place(this.autism$joinMacroButton, cursor, 4, macroW, 16);
         if (!lite) {
            String multiLabel = autism$multiLabel();
            int multiW = Math.min(this.autism$hugWidth(multiLabel), topCell);
            cursor -= topGap + multiW;
            autism$place(this.autism$multiButton, cursor, 4, multiW, 16);
            if (!multiLabel.equals(this.autism$multiButton.getMessage().getString())) {
               this.autism$multiButton.setMessage(Component.literal(multiLabel));
            }
         }

         this.autism$topButtonsLeft = Math.max(4, cursor);
         int footerRight = this.width / 2 - 154 - 3;
         int footerWidth = Math.max(60, Math.min(104, footerRight - 4));
         int footerX = Math.max(4, footerRight - footerWidth);
         int footerY = Math.max(4, this.height - 51);
         this.autism$spoofButton.setMessage(this.autism$spoofClientLabel(footerWidth));
         this.autism$packsButton.setMessage(this.autism$bypassPacksLabel(footerWidth));
         autism$place(this.autism$spoofButton, footerX, footerY, footerWidth, 20);
         autism$place(this.autism$packsButton, footerX, footerY + 20 + 3, footerWidth, 20);
         int rightX = Math.min(this.width - 4 - 104, this.width / 2 + 154 + 3);
         int rightWidth = Math.max(60, Math.min(104, this.width - 4 - rightX));
         int count = (FabricLoader.getInstance().isModLoaded("replaymod") ? 1 : 0) + (via != null ? 1 : 0) + (opsec != null ? 1 : 0);
         int rightY = Math.max(4, this.height - 8 - Math.max(0, count * 20 + Math.max(0, count - 1) * 3));
         int slot = 0;
         if (FabricLoader.getInstance().isModLoaded("replaymod")) {
            this.autism$recordButton.setMessage(this.autism$replayServerLabel(rightWidth));
            autism$place(this.autism$recordButton, rightX, rightY + slot++ * 23, rightWidth, 20);
         } else {
            autism$setVisible(this.autism$recordButton, false);
         }

         if (via != null) {
            autism$place(via, rightX, rightY + slot++ * 23, rightWidth, 20);
         }

         if (opsec != null) {
            autism$place(opsec, rightX, rightY + slot * 23, rightWidth, 20);
         }
      }
   }

   @Unique
   private void autism$ensureOwnedButtons() {
      if (this.autism$accountsButton == null) {
         this.autism$accountsButton = this.addRenderableWidget(
            Button.builder(Component.literal("Accounts"), ignored -> this.minecraft.gui.setScreen(new autismclient.gui.d.StreamUtil(this)))
               .bounds(0, 0, 60, 20)
               .build()
         );
      }

      if (this.autism$joinMacroButton == null) {
         this.autism$joinMacroButton = this.addRenderableWidget(
            Button.builder(Component.literal("Macro"), ignored -> this.minecraft.gui.setScreen(new ByteReader(this))).bounds(0, 0, 50, 20).build()
         );
      }

      if (this.autism$multiButton == null && !AutismLiteVariant.enabled()) {
         this.autism$multiButton = this.addRenderableWidget(
            Button.builder(Component.literal("Multi"), ignored -> this.autism$openMulti()).bounds(0, 0, 50, 20).build()
         );
      }

      if (this.autism$proxiesButton == null) {
         this.autism$proxiesButton = this.addRenderableWidget(
            Button.builder(Component.literal("Proxies"), ignored -> this.minecraft.gui.setScreen(new ArrayUtil(this))).bounds(0, 0, 60, 20).build()
         );
      }

      if (this.autism$spoofButton == null) {
         this.autism$spoofButton = this.addRenderableWidget(Button.builder(Component.literal("Client"), ignored -> {
            PoolEntry module = PoolEntry.init_PoolEntry();
            if (module != null) {
               module.update(!module.tick_Z());
            }

            this.autism$layoutButtons();
         }).bounds(0, 0, 104, 20).build());
      }

      if (this.autism$packsButton == null) {
         this.autism$packsButton = this.addRenderableWidget(Button.builder(Component.literal("Packs"), ignored -> {
            PoolEntry module = PoolEntry.init_PoolEntry();
            if (module != null) {
               module.write(!module.pop_Z());
            }

            this.autism$layoutButtons();
         }).bounds(0, 0, 104, 20).build());
      }

      if (this.autism$libraryButton == null) {
         this.autism$libraryButton = this.addRenderableWidget(
            Button.builder(Component.literal("Library"), ignored -> this.autism$openPluginLibrary()).bounds(0, 0, 60, 20).build()
         );
      }

      if (this.autism$recordButton == null) {
         this.autism$recordButton = this.addRenderableWidget(Button.builder(Component.literal("Replay"), ignored -> {
            this.autism$toggleReplayServerRecording();
            this.autism$layoutButtons();
         }).bounds(0, 0, 104, 20).build());
      }
   }

   @Unique
   private void autism$openPluginLibrary() {
      this.minecraft.gui.setScreen(new MapStore(this));
   }

   @Unique
   private void autism$openMulti() {
      if (!AutismLiteVariant.enabled()) {
         ServerData selected = this.autism$selectedServerData();
         String address = selected == null ? "" : selected.ip;
         Runnable proceed = () -> {
            if (PacketUtil.init_PacketUtil().load_Z()) {
               this.minecraft.gui.setScreen(new TableIndex(this));
            } else {
               PacketUtil.init_PacketUtil().init_V_ServerData(selected);
               this.minecraft.gui.setScreen(new PathHelper(this, address));
            }
         };
         RingBuffer.init(this.minecraft, this, proceed);
      }
   }

   @Unique
   private boolean autism$isOwned(AbstractWidget widget) {
      return widget == this.autism$accountsButton
         || widget == this.autism$joinMacroButton
         || widget == this.autism$multiButton
         || widget == this.autism$proxiesButton
         || widget == this.autism$spoofButton
         || widget == this.autism$packsButton
         || widget == this.autism$libraryButton
         || widget == this.autism$recordButton;
   }

   @Unique
   private ServerData autism$selectedServerData() {
      if (this.serverSelectionList == null) {
         return null;
      }

      Entry selected = this.serverSelectionList.getSelected();
      return selected instanceof OnlineServerEntry online ? online.getServerData() : null;
   }

   @Unique
   private static void autism$place(AbstractWidget widget, int x, int y, int width, int height) {
      if (widget != null) {
         widget.setX(Math.max(4, x));
         widget.setY(Math.max(4, y));
         widget.setSize(Math.max(1, width), Math.max(1, height));
         autism$setVisible(widget, true);
      }
   }

   @Unique
   private static void autism$setVisible(AbstractWidget widget, boolean visible) {
      if (widget != null) {
         widget.visible = visible;
         widget.active = visible;
      }
   }

   @Unique
   private Component autism$spoofClientLabel(int width) {
      PoolEntry module = PoolEntry.init_PoolEntry();
      boolean enabled = module != null && module.tick_Z();
      return this.autism$fitLabel(width, enabled ? "Client: Vanilla" : "Client: Modded", enabled ? "Vanilla" : "Modded", "Client");
   }

   @Unique
   private Component autism$bypassPacksLabel(int width) {
      PoolEntry module = PoolEntry.init_PoolEntry();
      boolean vanilla = module != null && module.tick_Z();
      boolean bypass = module != null && module.pop_Z();
      String label = autismclient.g.PacketUtil.init(vanilla, bypass);
      return this.autism$fitLabel(width, label, bypass ? "Bypass" : "Normal", "Packs");
   }

   @Unique
   private Component autism$replayServerLabel(int width) {
      boolean enabled = autism$getReplayBoolean("RECORD_SERVER", true);
      return this.autism$fitLabel(width, enabled ? "Replay: On" : "Replay: Off", enabled ? "Rec: On" : "Rec: Off", "Replay");
   }

   @Unique
   private int autism$hugWidth(String label) {
      return RenderState.load(this.font.width(label));
   }

   @Unique
   private Component autism$fitLabel(int width, String... candidates) {
      int available = Math.max(1, width - 8);

      for (String candidate : candidates) {
         if (this.font.width(candidate) <= available) {
            return Component.literal(candidate);
         }
      }

      return Component.literal(candidates[candidates.length - 1]);
   }

   @Unique
   private int autism$externalKind(AbstractWidget widget) {
      String label = widget.getMessage().getString();
      String normalized = label.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "").replace(".", "");
      String className = widget.getClass().getName().toLowerCase(Locale.ROOT);
      if (FabricLoader.getInstance().isModLoaded("viafabricplus") && "ViaFabricPlus".equals(label)) {
         return 1;
      } else if (!FabricLoader.getInstance().isModLoaded("replaymod")
         || !className.contains("replaymod") && !normalized.contains("recordserver") && !normalized.contains("replaymodguisettingsrecordserver")) {
         return !className.contains("opsec") && !normalized.contains("opsec") ? 0 : 3;
      } else {
         return 2;
      }
   }

   @Unique
   private void autism$suppressMeteorWidgets() {
      if (FabricLoader.getInstance().isModLoaded("meteor-client")) {
         if (!this.autism$meteorUiConfigSuppressed) {
            this.autism$meteorUiConfigSuppressed = true;
            autism$disableMeteorMultiplayerUiConfig();
         }

         for (GuiEventListener child : this.children()) {
            if (child instanceof Button button && !this.autism$isOwned(button)) {
               String label = button.getMessage().getString();
               if ("Accounts".equals(label) || "Proxies".equals(label)) {
                  autism$setVisible(button, false);
               }
            }
         }
      }
   }

   @Unique
   private void autism$toggleReplayServerRecording() {
      boolean enabled = autism$getReplayBoolean("RECORD_SERVER", true);
      autism$setReplayBoolean("RECORD_SERVER", !enabled);
   }

   @Unique
   private static boolean autism$getReplayBoolean(String settingField, boolean fallback) {
      try {
         Object settings = autism$replaySettingsRegistry();
         Object key = Class.forName("com.replaymod.recording.Setting").getField(settingField).get(null);
         return settings.getClass().getMethod("get", Class.forName("com.replaymod.core.SettingsRegistry$SettingKey")).invoke(settings, key) instanceof Boolean bool
            ? bool
            : fallback;
      } catch (ReflectiveOperationException | LinkageError ignored) {
         return fallback;
      }
   }

   @Unique
   private static void autism$setReplayBoolean(String settingField, boolean value) {
      try {
         Object settings = autism$replaySettingsRegistry();
         Object key = Class.forName("com.replaymod.recording.Setting").getField(settingField).get(null);
         Class<?> settingKeyClass = Class.forName("com.replaymod.core.SettingsRegistry$SettingKey");
         settings.getClass().getMethod("set", settingKeyClass, Object.class).invoke(settings, key, value);
         settings.getClass().getMethod("save").invoke(settings);
      } catch (ReflectiveOperationException | LinkageError var5) {
      }
   }

   @Unique
   private static Object autism$replaySettingsRegistry() throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
      Object replayMod = Class.forName("com.replaymod.core.ReplayMod").getField("instance").get(null);
      return replayMod.getClass().getMethod("getSettingsRegistry").invoke(replayMod);
   }

   @Unique
   private static void autism$disableMeteorMultiplayerUiConfig() {
      try {
         Class<?> configClass = Class.forName("meteordevelopment.meteorclient.systems.config.Config");
         Object config = configClass.getMethod("get").invoke(null);
         Class<?> buttonPositionClass = Class.forName("meteordevelopment.meteorclient.systems.config.Config$ButtonPosition");
         Object hidden = Enum.valueOf(buttonPositionClass.asSubclass(Enum.class), "Hidden");
         autism$setMeteorSetting(configClass.getField("accountButtonAnchor").get(config), hidden);
         autism$setMeteorSetting(configClass.getField("proxiesButtonAnchor").get(config), hidden);
         autism$setMeteorSetting(configClass.getField("showAccountStatus").get(config), false);
         autism$setMeteorSetting(configClass.getField("showProxiesStatus").get(config), false);
      } catch (ReflectiveOperationException var4) {
      }
   }

   @Unique
   private static void autism$setMeteorSetting(Object setting, Object value) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
      setting.getClass().getSuperclass().getMethod("set", Object.class).invoke(setting, value);
   }

   @Override
   public void autism$renderExternalButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
      this.autism$layoutButtons();
      if (!aD.init_Z()) {
         int leftTextWidth = Math.max(0, this.autism$topButtonsLeft - 4 - 3);
         String username = this.minecraft.getUser().getName();
         if (leftTextWidth > 8) {
            graphics.text(this.font, this.autism$fitPlain("Logged in as " + username, leftTextWidth), 4, 4, -1, false);
         }

         ProxyEntry proxy = ProxyManager.init_bY().init_bW();
         int statusY = 16;
         if (proxy != null) {
            String proxyLabel = "Using proxy " + proxy.read + ":" + proxy.init_I;
            if (leftTextWidth > 8) {
               graphics.text(this.font, this.autism$fitPlain(proxyLabel, leftTextWidth), 4, statusY, -5263441, false);
            }

            statusY += 12;
         }

         String macroName = aA.init_Str();
         String macroLabel;
         int macroColor;
         if (macroName.isBlank()) {
            macroLabel = "Join Macro: none";
            macroColor = -7370102;
         } else if (aN.init_aN().init_aM_Str(macroName) == null) {
            macroLabel = "Join Macro missing: " + macroName;
            macroColor = -38037;
         } else {
            macroLabel = "Join Macro: " + macroName + " - " + aA.load_Str();
            macroColor = -10035062;
         }

         if (leftTextWidth > 8) {
            graphics.text(this.font, this.autism$fitPlain(macroLabel, leftTextWidth), 4, statusY, macroColor, false);
         }

         this.autism$renderButtonGuidance(graphics, mouseX, mouseY);
      }
   }

   @Unique
   private void autism$renderButtonGuidance(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      PoolEntry module = PoolEntry.init_PoolEntry();
      boolean vanilla = module != null && module.tick_Z();
      boolean bypass = module != null && module.pop_Z();
      String guidance = null;
      if (this.autism$spoofButton != null && this.autism$spoofButton.isMouseOver(mouseX, mouseY)) {
         guidance = autismclient.g.PacketUtil.init(vanilla);
      } else if (this.autism$packsButton != null && this.autism$packsButton.isMouseOver(mouseX, mouseY)) {
         guidance = autismclient.g.PacketUtil.load(vanilla, bypass);
      }

      if (guidance != null) {
         int virtualMouseX = cA.init_I_D(mouseX);
         int virtualMouseY = cA.init_I_D(mouseY);
         cA.init_V_GuiGraphicsExtract(graphics);

         try {
            graphics.nextStratum();
            h.init(UiHelper.init(graphics, this.font, virtualMouseX, virtualMouseY), guidance, virtualMouseX, virtualMouseY, 240);
         } finally {
            cA.load_V_GuiGraphicsExtract(graphics);
         }
      }
   }

   @Unique
   private String autism$fitPlain(String label, int maxWidth) {
      if (label == null) {
         return "";
      } else {
         return this.font.width(label) <= maxWidth ? label : this.font.plainSubstrByWidth(label, Math.max(1, maxWidth - 4));
      }
   }
}
