package autismclient.modules;

import autismclient.api.module.Setting;
import autismclient.api.module.SettingOwner;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class Module implements SettingOwner {
   protected static final Minecraft MC = Minecraft.getInstance();
   private final String id;
   private final String name;
   private ae category;
   private final String description;
   private final List<Setting<?, ?>> settings = new ArrayList<>();
   private String replacementToggleMessage;
   private boolean addon;
   private final Map<String, Long> controlRevisions = new ConcurrentHashMap<>();
   private boolean configuredWrite;
   private final ScopedToggleOverride temporaryActivation = new ScopedToggleOverride(() -> this.state().enabled, enabled -> {
      try {
         if (enabled) {
            this.onEnable();
         } else {
            this.onDisable();
         }
      } finally {
         al.load_V();
      }
   });

   public final long controlRevision(String key) {
      return this.controlRevisions.getOrDefault(key, 0L);
   }

   protected Module(String id, String name, ae category, String description) {
      this.id = id;
      this.name = name;
      this.category = category;
      this.description = description;
   }

   protected Module(String id, String name, String description) {
      this(id, name, null, description);
   }

   final void assignCategory(ae category) {
      if (this.category == null) {
         this.category = category;
      }
   }

   public final String id() {
      return this.id;
   }

   public final boolean isAddon() {
      return this.addon;
   }

   final void markAddon() {
      this.addon = true;
   }

   public final String name() {
      return this.name;
   }

   public final ae category() {
      return this.category;
   }

   public final String description() {
      return this.description;
   }

   public final boolean isEnabled() {
      return !AutismLiteVariant.enabled() && !autismclient.e.RefQueue.init() ? false : this.temporaryActivation.isEnabled();
   }

   final boolean isEnabledRaw() {
      return this.state().enabled;
   }

   public final ScopedToggleOverride.OverrideHandle borrowEnabled(boolean enabled) {
      return enabled && aD.load_Z(this) ? () -> {} : this.temporaryActivation.push(enabled);
   }

   public final void setEnabled(boolean enabled) {
      this.controlRevisions.merge("enabled", 1L, Long::sum);
      if (!enabled || !aD.load_Z(this)) {
         boolean wasEnabled = this.temporaryActivation.isEnabled();
         boolean configuredChanged = this.state().enabled != enabled;
         this.temporaryActivation.clear();
         if (wasEnabled == enabled && !configuredChanged) {
            this.replacementToggleMessage = null;
         } else {
            this.state().enabled = enabled;
            if (wasEnabled != enabled) {
               if (enabled) {
                  this.onEnable();
               } else {
                  this.onDisable();
               }
            }

            al.load_V();
            this.save();
            String message = this.replacementToggleMessage;
            this.replacementToggleMessage = null;
            if (this.emitsToggleMessage() && AutismConfig.getGlobal().chatFeedback && !aD.load_Z()) {
               AutismClientMessaging.sendPrefixed(
                  message != null && !message.isBlank() ? message : this.name + ": " + (this.isEnabledRaw() ? "enabled" : "disabled")
               );
            }
         }
      }
   }

   public final void setConfiguredEnabled(boolean enabled) {
      this.controlRevisions.merge("enabled", 1L, Long::sum);
      if (!enabled || !aD.load_Z(this)) {
         boolean wasEnabled = this.isEnabledRaw();
         boolean configuredChanged = this.state().enabled != enabled;
         this.temporaryActivation.clear();
         if (wasEnabled != enabled || configuredChanged) {
            this.state().enabled = enabled;
            al.init_V_Module_4(this, wasEnabled, enabled);
            al.load_V();
            this.save();
         }
      }
   }

   protected final void setEnabledSilently(boolean enabled) {
      this.controlRevisions.merge("enabled", 1L, Long::sum);
      if (!enabled || !aD.load_Z(this)) {
         boolean wasEnabled = this.temporaryActivation.isEnabled();
         boolean configuredChanged = this.state().enabled != enabled;
         this.temporaryActivation.clear();
         if (wasEnabled != enabled || configuredChanged) {
            this.state().enabled = enabled;
            if (wasEnabled != enabled) {
               if (enabled) {
                  this.onEnable();
               } else {
                  this.onDisable();
               }
            }

            al.load_V();
            this.save();
         }
      }
   }

   protected final void replaceNextToggleMessage(String message) {
      this.replacementToggleMessage = message;
   }

   protected final void disableSilentlyWithToggleMessage(String message) {
      this.replaceNextToggleMessage(message);
      this.setEnabledSilently(false);
   }

   protected final void disableWithToggleMessage(String message) {
      this.replaceNextToggleMessage(message);
      this.setEnabled(false);
   }

   public final void toggle() {
      this.setEnabled(!this.isEnabled());
   }

   protected int defaultKeybind() {
      return -1;
   }

   public final int keybind() {
      return this.state().keybind;
   }

   public final void setKeybind(int keybind) {
      this.state().keybind = keybind;
      al.init_V();
      this.save();
   }

   public final List<Setting<?, ?>> settings() {
      return Collections.unmodifiableList(this.settings);
   }

   public final List<Setting<?, ?>> visibleSettings() {
      List<Setting<?, ?>> visible = new ArrayList<>();

      for (Setting<?, ?> setting : this.settings) {
         if (setting.isVisible()) {
            visible.add(setting);
         }
      }

      return visible;
   }

   protected final <T, S extends Setting<T, S>> S add(S setting) {
      if (setting == null) {
         throw new IllegalArgumentException("Module setting cannot be null");
      }

      if (setting.id() == null || setting.id().isBlank()) {
         throw new IllegalArgumentException("Module setting id cannot be blank for " + this.id);
      }

      if (this.setting(setting.id()) != null) {
         throw new IllegalStateException("Duplicate setting '" + setting.id() + "' in module '" + this.id + "'");
      }

      setting.attach(this);
      this.settings.add(setting);
      this.state().settings.putIfAbsent(setting.id(), setting.defaultValue());
      al.init_V();
      return setting;
   }

   public final <T, S extends Setting<T, S>> S addSetting(S setting) {
      return this.add(setting);
   }

   public final void moveSettingToTop(Setting<?, ?> setting) {
      if (setting != null && this.settings.remove(setting)) {
         this.settings.add(0, setting);
         al.init_V();
      }
   }

   public final void moveSettingsToEnd(List<Setting<?, ?>> tail) {
      if (tail != null && !tail.isEmpty()) {
         this.settings.removeAll(tail);
         this.settings.addAll(tail);
         al.init_V();
      }
   }

   public String info() {
      return "";
   }

   public String settingsHint() {
      return "";
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void tick() {
   }

   public boolean ticksWhenDisabled() {
      return false;
   }

   public boolean hasDisabledTickWork() {
      return this.ticksWhenDisabled();
   }

   public boolean opensSettingsOnClick() {
      return false;
   }

   public boolean hasActivationToggle() {
      return true;
   }

   public boolean holdToActivate() {
      return false;
   }

   public boolean showInModuleMenu() {
      return true;
   }

   public boolean settingsShareable() {
      return true;
   }

   public boolean showInArrayList() {
      return true;
   }

   public boolean emitsToggleMessage() {
      return true;
   }

   public void preMovementTick() {
   }

   public void onNetworkMovementTickPre() {
   }

   public void onPacketProcessFrame() {
   }

   public void onInputFrame() {
   }

   public boolean shouldCancelPlayerTick() {
      return false;
   }

   public void onRenderLevel(float partialTick) {
   }

   public void onMouseRotation(double deltaYaw, double deltaPitch) {
   }

   public Vec3 onPlayerMove(MoverType type, Vec3 movement) {
      return movement;
   }

   public boolean shouldApplySpeedTimer() {
      return false;
   }

   public float speedTimerMultiplier() {
      return 1.0F;
   }

   public void onGameJoin() {
   }

   public void onGameLeft() {
   }

   public boolean onPacketSend(Packet<?> packet) {
      return false;
   }

   public boolean onPacketReceive(Packet<?> packet) {
      return false;
   }

   public void onSoundPacket(ClientboundSoundPacket packet) {
   }

   public void appendTooltip(ItemStack stack, List<?> lines) {
   }

   public boolean shouldCancelAttack(HitResult hitResult) {
      return false;
   }

   public boolean shouldCancelUse(HitResult hitResult, InteractionHand hand) {
      return false;
   }

   public void onStartBreakingBlock(BlockPos pos, Direction direction) {
   }

   public boolean onStartDestroyBlock(BlockPos pos, Direction direction) {
      return false;
   }

   public void onBlockBreakingProgress(BlockPos pos, Direction direction) {
   }

   public boolean shouldCancelStartBreakingBlock(BlockPos pos, Direction direction) {
      return false;
   }

   public boolean shouldTraceEntity(Entity entity) {
      return false;
   }

   public int traceColor(Entity entity) {
      return -2130706433;
   }

   protected final boolean bool(String settingId) {
      return Boolean.parseBoolean(this.value(settingId));
   }

   protected final int integer(String settingId) {
      try {
         return Integer.parseInt(this.value(settingId));
      } catch (NumberFormatException ignored) {
         Setting<?, ?> setting = this.setting(settingId);

         try {
            return setting == null ? 0 : Integer.parseInt(setting.defaultValue());
         } catch (NumberFormatException stillBad) {
            return 0;
         }
      }
   }

   protected final double decimal(String settingId) {
      try {
         return Double.parseDouble(this.value(settingId));
      } catch (NumberFormatException ignored) {
         Setting<?, ?> setting = this.setting(settingId);

         try {
            return setting == null ? 0.0 : Double.parseDouble(setting.defaultValue());
         } catch (NumberFormatException stillBad) {
            return 0.0;
         }
      }
   }

   protected final String text(String settingId) {
      return this.value(settingId);
   }

   protected final String choice(String settingId) {
      return this.value(settingId);
   }

   protected final List<String> list(String settingId) {
      String value = this.value(settingId);
      if (value != null && !value.isBlank()) {
         List<String> out = new ArrayList<>();

         for (String raw : value.split("\\|")) {
            String item = raw.trim();
            if (!item.isEmpty()) {
               out.add(item);
            }
         }

         return out;
      } else {
         return List.of();
      }
   }

   public final String value(String settingId) {
      String external = this.externalSettingValue(settingId);
      if (external != null) {
         return external;
      }

      Setting<?, ?> setting = this.setting(settingId);
      String fallback = setting == null ? "" : setting.defaultValue();
      String raw = this.state().settings.getOrDefault(settingId, fallback);
      return setting != null && setting.hasDynamicMax() ? setting.sanitizeUiString(raw) : raw;
   }

   @Override
   public final String settingValue(String name) {
      String external = this.externalSettingValue(name);
      return external != null ? external : this.state().settings.get(name);
   }

   @Override
   public final void putSettingValue(String name, String value) {
      this.setValue(name, value);
   }

   public final void setValue(String settingId, String value) {
      this.setValueTransient(settingId, value);
      this.save();
   }

   public final void setValueTransient(String settingId, String value) {
      this.controlRevisions.merge(settingId, 1L, Long::sum);
      String sanitized = sanitize(this.setting(settingId), value);
      if (!this.setExternalSettingValue(settingId, sanitized)) {
         this.state().settings.put(settingId, sanitized);
      }

      this.onOptionValueChanged(settingId);
      al.init_V();
   }

   public final void setConfiguredValue(String settingId, String value) {
      this.setConfiguredValueTransient(settingId, value);
      this.save();
   }

   public final void setConfiguredValueTransient(String settingId, String value) {
      this.controlRevisions.merge(settingId, 1L, Long::sum);
      String sanitized = sanitize(this.setting(settingId), value);
      if (!this.setConfiguredExternal(settingId, sanitized)) {
         this.state().settings.put(settingId, sanitized);
      }

      al.init_V_Module_3(this, settingId, false);
      al.init_V();
   }

   public final void persistConfiguredState() {
      this.save();
   }

   public final void resetValue(String settingId) {
      Setting<?, ?> setting = this.setting(settingId);
      if (setting != null) {
         if (!this.setExternalSettingValue(settingId, setting.defaultValue())) {
            this.state().settings.put(settingId, setting.defaultValue());
         }

         this.onOptionValueChanged(settingId);
         al.init_V();
         this.save();
      }
   }

   public final void resetConfiguredValue(String settingId) {
      Setting<?, ?> setting = this.setting(settingId);
      if (setting != null) {
         if (!this.setConfiguredExternal(settingId, setting.defaultValue())) {
            this.state().settings.put(settingId, setting.defaultValue());
         }

         al.init_V_Module_3(this, settingId, false);
         al.init_V();
         this.save();
      }
   }

   public final void resetSettings() {
      for (Setting<?, ?> setting : this.settings) {
         if (!setting.isKeptOnReset() && !this.setExternalSettingValue(setting.id(), setting.defaultValue())) {
            this.state().settings.put(setting.id(), setting.defaultValue());
         }
      }

      this.onSettingsReset();
      al.init_V();
      this.save();
   }

   public final void resetConfiguredSettings() {
      for (Setting<?, ?> setting : this.settings) {
         if (!setting.isKeptOnReset() && !this.setConfiguredExternal(setting.id(), setting.defaultValue())) {
            this.state().settings.put(setting.id(), setting.defaultValue());
         }
      }

      al.init_V_Module_3(this, "", true);
      al.init_V();
      this.save();
   }

   private boolean setConfiguredExternal(String settingId, String value) {
      boolean outer = this.configuredWrite;
      this.configuredWrite = true;

      try {
         return this.setExternalSettingValue(settingId, value);
      } finally {
         this.configuredWrite = outer;
      }
   }

   protected final void setLinkedValue(String settingId, String value) {
      if (this.configuredWrite) {
         this.setConfiguredValueTransient(settingId, value);
      } else {
         this.setValueTransient(settingId, value);
      }
   }

   final void applyConfiguredSettings(Set<String> settingIds, boolean reset) {
      if (reset) {
         this.onSettingsReset();
      }

      if (settingIds != null) {
         for (String settingId : settingIds) {
            if (settingId != null && !settingId.isBlank()) {
               this.onOptionValueChanged(settingId);
            }
         }
      }
   }

   protected void onSettingsReset() {
   }

   protected void onOptionValueChanged(String settingId) {
   }

   protected String externalSettingValue(String settingId) {
      return null;
   }

   protected boolean setExternalSettingValue(String settingId, String value) {
      return false;
   }

   public final void adjustOption(Setting<?, ?> setting, int direction) {
      if (setting != null) {
         switch (setting.kind()) {
            case BOOLEAN:
               this.setValue(setting.id(), Boolean.toString(!this.bool(setting.id())));
               break;
            case INTEGER: {
               int value = this.integer(setting.id());
               int adjusted = (int)clamp(value + (int)setting.step() * direction, setting.min(), setting.max());
               this.setValue(setting.id(), Integer.toString(adjusted));
               break;
            }
            case DOUBLE: {
               double value = this.decimal(setting.id());
               double adjusted = clamp(value + setting.step() * direction, setting.min(), setting.max());
               this.setValue(setting.id(), String.format(Locale.ROOT, "%.2f", adjusted));
               break;
            }
            case ENUM:
               List<String> choices = setting.choices();
               if (!choices.isEmpty()) {
                  int index = choices.indexOf(this.value(setting.id()));
                  if (index < 0) {
                     index = 0;
                  }

                  int next = Math.floorMod(index + direction, choices.size());
                  this.setValue(setting.id(), choices.get(next));
               }
               break;
            case ACTION:
               if (setting.action() != null) {
                  setting.action().run();
               }
         }
      }
   }

   public final void adjustConfiguredOption(Setting<?, ?> setting, int direction) {
      if (setting != null) {
         switch (setting.kind()) {
            case BOOLEAN:
               this.setConfiguredValue(setting.id(), Boolean.toString(!this.bool(setting.id())));
               break;
            case INTEGER: {
               int adjusted = (int)clamp(this.integer(setting.id()) + (int)setting.step() * direction, setting.min(), setting.max());
               this.setConfiguredValue(setting.id(), Integer.toString(adjusted));
               break;
            }
            case DOUBLE: {
               double adjusted = clamp(this.decimal(setting.id()) + setting.step() * direction, setting.min(), setting.max());
               this.setConfiguredValue(setting.id(), String.format(Locale.ROOT, "%.2f", adjusted));
               break;
            }
            case ENUM:
               List<String> choices = setting.choices();
               if (!choices.isEmpty()) {
                  int index = Math.max(0, choices.indexOf(this.value(setting.id())));
                  this.setConfiguredValue(setting.id(), choices.get(Math.floorMod(index + direction, choices.size())));
               }
               break;
            case ACTION:
               if (setting.isAvailable(false, false) && setting.action() != null) {
                  setting.action().run();
               }
         }
      }
   }

   public final String displayValue(Setting<?, ?> setting) {
      if (setting == null) {
         return "";
      }

      String override = this.displayValueOverride(setting);
      if (override != null) {
         return override;
      }

      String var10000;
      switch (setting.kind()) {
         case BOOLEAN:
            var10000 = this.bool(setting.id()) ? "ON" : "OFF";
            break;
         case ACTION:
            var10000 = "RUN";
            break;
         default:
            var10000 = setting.format(this.value(setting.id()));
      }

      return var10000;
   }

   protected String displayValueOverride(Setting<?, ?> setting) {
      return null;
   }

   private static String sanitize(Setting<?, ?> setting, String value) {
      if (setting == null) {
         return value == null ? "" : value;
      } else {
         return setting.sanitizeUiString(value);
      }
   }

   public final Setting<?, ?> setting(String settingId) {
      for (Setting<?, ?> setting : this.settings) {
         if (setting.id().equals(settingId)) {
            return setting;
         }
      }

      return null;
   }

   protected final boolean isAdminContext() {
      return MC != null && MC.player != null && MC.player.canUseGameMasterBlocks();
   }

   protected final boolean isCreativeContext() {
      return MC != null && MC.player != null && MC.player.hasInfiniteMaterials();
   }

   protected final void sendCommand(String command) {
      if (!aD.read_Z() && MC != null && MC.getConnection() != null && command != null && !command.isBlank()) {
         String normalized = command.startsWith("/") ? command.substring(1) : command;
         MC.getConnection().sendCommand(normalized);
      }
   }

   private AutismConfig.ModuleState state() {
      AutismConfig config = AutismConfig.getGlobal();
      return config.modules.computeIfAbsent(this.id, ignored -> {
         AutismConfig.ModuleState created = new AutismConfig.ModuleState();
         created.keybind = this.defaultKeybind();
         return created;
      });
   }

   private void save() {
      AutismConfig.getGlobal().save();
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }
}
