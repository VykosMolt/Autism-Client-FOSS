package autismclient.api;

import autismclient.api.ai.AiCapabilities;
import autismclient.api.ai.AiCapabilityProvider;
import autismclient.api.custommenu.CustomMenuAdapter;
import autismclient.api.custommenu.CustomMenuAdapterRegistry;
import autismclient.api.event.AddonEvents;
import autismclient.api.hud.HudElementProvider;
import autismclient.api.hud.HudElements;
import autismclient.api.macro.MacroActionEntry;
import autismclient.api.macro.MacroActionRegistry;
import autismclient.api.macro.MacroPresetRegistry;
import autismclient.b.DataUtil;
import autismclient.commands.Command;
import autismclient.modules.Module;
import autismclient.modules.ae;
import autismclient.modules.al;
import autismclient.util.macro.MacroAction;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;

public final class AutismAddons {
   private AutismAddons() {
   }

   public static AutismAddons.Ai ai() {
      return AutismAddons.Ai.INSTANCE;
   }

   public static AutismAddons.Modules modules() {
      return AutismAddons.Modules.INSTANCE;
   }

   public static AutismAddons.Commands commands() {
      return AutismAddons.Commands.INSTANCE;
   }

   public static AutismAddons.MacroActions macroActions() {
      return AutismAddons.MacroActions.INSTANCE;
   }

   public static AutismAddons.Presets presets() {
      return AutismAddons.Presets.INSTANCE;
   }

   public static AutismAddons.Hud hud() {
      return AutismAddons.Hud.INSTANCE;
   }

   public static AutismAddons.Events events() {
      return AutismAddons.Events.INSTANCE;
   }

   public static AutismAddons.CustomMenus customMenus() {
      return AutismAddons.CustomMenus.INSTANCE;
   }

   public static List<AutismAddon> list() {
      return DataUtil.init_List();
   }

   public static String id(String localId) {
      return DataUtil.init_Str(localId);
   }

   public static int apiVersion() {
      return ApiVersion.CURRENT;
   }

   public static final class Ai {
      private static final AutismAddons.Ai INSTANCE = new AutismAddons.Ai();

      private Ai() {
      }

      public boolean register(AiCapabilityProvider provider) {
         return AiCapabilities.register(provider);
      }

      public AddonRegistrationResult registerDetailed(AiCapabilityProvider provider) {
         return AiCapabilities.registerDetailed(provider);
      }
   }

   public static final class Commands {
      static final AutismAddons.Commands INSTANCE = new AutismAddons.Commands();

      private Commands() {
      }

      public void register(Command command) {
         autismclient.commands.DataUtil.init_V(command, DataUtil.init_Str());
      }

      public AddonRegistrationResult registerDetailed(Command command) {
         return autismclient.commands.DataUtil.init_AddonRegistrationR(command, DataUtil.init_Str());
      }
   }

   public static final class CustomMenus {
      static final AutismAddons.CustomMenus INSTANCE = new AutismAddons.CustomMenus();

      private CustomMenus() {
      }

      public boolean register(CustomMenuAdapter adapter) {
         return CustomMenuAdapterRegistry.register(adapter);
      }
   }

   public static final class Events {
      static final AutismAddons.Events INSTANCE = new AutismAddons.Events();

      private Events() {
      }

      public void onTick(Consumer<Minecraft> listener) {
         AddonEvents.onTick(listener);
      }

      public void onPacketSend(Predicate<Packet<?>> listener) {
         AddonEvents.onPacketSend(listener);
      }

      public void onPacketReceive(Consumer<Packet<?>> listener) {
         AddonEvents.onPacketReceive(listener);
      }

      public void onGameJoin(Runnable listener) {
         AddonEvents.onGameJoin(listener);
      }

      public void onGameLeft(Runnable listener) {
         AddonEvents.onGameLeft(listener);
      }
   }

   public static final class Hud {
      static final AutismAddons.Hud INSTANCE = new AutismAddons.Hud();

      private Hud() {
      }

      public boolean register(HudElementProvider provider) {
         return HudElements.register(provider);
      }

      public AddonRegistrationResult registerDetailed(HudElementProvider provider) {
         return HudElements.registerDetailed(provider);
      }
   }

   public static final class MacroActions {
      static final AutismAddons.MacroActions INSTANCE = new AutismAddons.MacroActions();

      private MacroActions() {
      }

      public boolean register(MacroActionEntry entry) {
         return MacroActionRegistry.register(entry);
      }

      public AddonRegistrationResult registerDetailed(MacroActionEntry entry) {
         return MacroActionRegistry.registerDetailed(entry);
      }

      public void registerCategory(String id, String label, int color) {
         MacroActionRegistry.registerScopedCategory(id, label, color);
      }
   }

   public static final class Modules {
      static final AutismAddons.Modules INSTANCE = new AutismAddons.Modules();

      private Modules() {
      }

      public boolean register(Module module) {
         return al.init_Z_Module_Str(module, DataUtil.init_Str());
      }

      public AddonRegistrationResult registerDetailed(Module module) {
         return al.init_AddonRegistrationR_Module_Str(module, DataUtil.init_Str());
      }

      public ae autoCategory() {
         return ae.init(DataUtil.init_Str(), DataUtil.load_Str(null));
      }

      public ae registerCategory(String label) {
         return ae.init(DataUtil.init_Str(), DataUtil.load_Str(label));
      }
   }

   public static final class Presets {
      static final AutismAddons.Presets INSTANCE = new AutismAddons.Presets();

      private Presets() {
      }

      public void registerCategory(String id, String label) {
         MacroPresetRegistry.registerScopedCategory(id, label);
      }

      public void register(String label, String tip, Supplier<List<MacroAction>> builder) {
         MacroPresetRegistry.register(MacroPresetRegistry.ensureScopedCategory(null), label, tip, builder);
      }

      public AddonRegistrationResult registerDetailed(String label, String tip, Supplier<List<MacroAction>> builder) {
         return MacroPresetRegistry.registerDetailed(MacroPresetRegistry.ensureScopedCategory(null), label, tip, builder);
      }

      public void register(String categoryId, String label, String tip, Supplier<List<MacroAction>> builder) {
         MacroPresetRegistry.register(MacroPresetRegistry.ensureScopedCategory(categoryId), label, tip, builder);
      }

      public AddonRegistrationResult registerDetailed(String categoryId, String label, String tip, Supplier<List<MacroAction>> builder) {
         return MacroPresetRegistry.registerDetailed(MacroPresetRegistry.ensureScopedCategory(categoryId), label, tip, builder);
      }
   }
}
