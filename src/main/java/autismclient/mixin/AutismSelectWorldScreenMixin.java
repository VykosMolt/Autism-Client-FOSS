package autismclient.mixin;

import autismclient.c.StreamUtil;
import autismclient.gui.d.ByteReader;
import autismclient.modules.aD;
import autismclient.util.aA;
import autismclient.util.aN;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SelectWorldScreen.class, priority = 2000)
public abstract class AutismSelectWorldScreenMixin extends Screen implements StreamUtil {
   @Unique
   private static final int BUTTON_HEIGHT = 20;
   @Unique
   private static final int BUTTON_WIDTH = 104;
   @Unique
   private static final int MACRO_BUTTON_WIDTH = 50;
   @Unique
   private static final int BUTTON_GAP = 3;
   @Unique
   private Button autism$macroButton;
   @Unique
   private Button autism$recordButton;

   protected AutismSelectWorldScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "repositionElements", at = @At("TAIL"))
   private void autism$repositionElements(CallbackInfo ci) {
      this.autism$layoutReplayButton();
   }

   @Unique
   private void autism$layoutReplayButton() {
      AbstractWidget back = null;

      for (GuiEventListener child : this.children()) {
         if (child instanceof AbstractWidget widget && widget != this.autism$recordButton && widget != this.autism$macroButton) {
            if (this.autism$isReplayRecordButton(widget)) {
               widget.visible = false;
               widget.active = false;
            } else if (this.autism$isBackButton(widget)) {
               back = widget;
            }
         }
      }

      if (aD.init_Z()) {
         if (this.autism$macroButton != null) {
            this.autism$macroButton.visible = false;
            this.autism$macroButton.active = false;
         }

         if (this.autism$recordButton != null) {
            this.autism$recordButton.visible = false;
            this.autism$recordButton.active = false;
         }
      } else {
         if (this.autism$macroButton == null) {
            this.autism$macroButton = this.addRenderableWidget(
               Button.builder(Component.literal("Macro"), ignored -> this.minecraft.gui.setScreen(new ByteReader(this))).bounds(0, 0, 50, 20).build()
            );
         }

         if (this.autism$recordButton == null) {
            this.autism$recordButton = this.addRenderableWidget(Button.builder(this.autism$replaySingleplayerLabel(), ignored -> {
               this.autism$toggleReplaySingleplayerRecording();
               this.autism$recordButton.setMessage(this.autism$replaySingleplayerLabel());
            }).bounds(0, 0, 104, 20).build());
         }

         int macroX = back != null ? back.getRight() + 3 : this.width / 2 + 104;
         int y = back != null ? back.getY() : this.height - 28;
         if (macroX + 50 > this.width - 4) {
            macroX = Math.max(4, this.width - 4 - 50);
            y = Math.max(4, y - 20 - 3);
         }

         this.autism$macroButton.setX(macroX);
         this.autism$macroButton.setY(y);
         this.autism$macroButton.setSize(50, 20);
         this.autism$macroButton.visible = true;
         this.autism$macroButton.active = true;
         if (!FabricLoader.getInstance().isModLoaded("replaymod")) {
            this.autism$recordButton.visible = false;
            this.autism$recordButton.active = false;
         } else {
            int width = Math.min(104, Math.max(60, this.width - 8));
            int recordX = macroX + 50 + 3;
            int recordY = y;
            if (recordX + width > this.width - 4) {
               recordX = Math.max(4, this.width - 4 - width);
               recordY = Math.max(4, y - 20 - 3);
            }

            this.autism$recordButton.setX(recordX);
            this.autism$recordButton.setY(recordY);
            this.autism$recordButton.setSize(width, 20);
            this.autism$recordButton.setMessage(this.autism$replaySingleplayerLabel());
            this.autism$recordButton.visible = true;
            this.autism$recordButton.active = true;
         }
      }
   }

   @Unique
   private boolean autism$isBackButton(AbstractWidget widget) {
      String label = widget.getMessage().getString();
      return label.equals(Component.translatable("gui.back").getString()) || "back".equalsIgnoreCase(label);
   }

   @Unique
   private boolean autism$isReplayRecordButton(AbstractWidget widget) {
      if (!FabricLoader.getInstance().isModLoaded("replaymod")) {
         return false;
      }

      String className = widget.getClass().getName().toLowerCase(Locale.ROOT);
      String normalizedLabel = widget.getMessage().getString().toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "").replace(".", "");
      return className.contains("replaymod")
         || normalizedLabel.contains("recordsingleplayer")
         || normalizedLabel.contains("replaymodguisettingsrecordsingleplayer");
   }

   @Unique
   private Component autism$replaySingleplayerLabel() {
      boolean enabled = autism$getReplayBoolean("RECORD_SINGLEPLAYER", true);
      return Component.literal(enabled ? "Replay: On" : "Replay: Off");
   }

   @Unique
   private void autism$toggleReplaySingleplayerRecording() {
      boolean enabled = autism$getReplayBoolean("RECORD_SINGLEPLAYER", true);
      autism$setReplayBoolean("RECORD_SINGLEPLAYER", !enabled);
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

   @Override
   public void autism$renderExternalButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
      this.autism$layoutReplayButton();
      if (!aD.init_Z()) {
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

         graphics.text(this.font, this.autism$fitPlain(macroLabel, 220), 4, 4, macroColor, false);
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
