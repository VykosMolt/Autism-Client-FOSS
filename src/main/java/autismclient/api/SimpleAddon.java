package autismclient.api;

import autismclient.api.macro.MacroActionEntry;
import autismclient.api.macro.SimpleAction;
import autismclient.api.macro.SimpleCondition;
import autismclient.modules.Module;
import autismclient.util.macro.MacroAction;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;

public abstract class SimpleAddon extends AutismAddon {
   private final int apiVersion;
   private final String rootPackage;

   protected SimpleAddon(int apiVersion, String rootPackage) {
      this.apiVersion = apiVersion;
      this.rootPackage = rootPackage == null ? "" : rootPackage;
   }

   @Override
   public final int apiVersion() {
      return this.apiVersion;
   }

   @Override
   public final String getPackage() {
      return this.rootPackage;
   }

   @Override
   public final void onInitialize() {
      this.initialize();
   }

   protected abstract void initialize();

   protected final String id(String localId) {
      return AutismAddons.id(localId);
   }

   protected final AddonRegistrationResult registerModule(Module module) {
      return AutismAddons.modules().registerDetailed(module);
   }

   protected final AddonRegistrationResult registerAction(MacroActionEntry entry) {
      return AutismAddons.macroActions().registerDetailed(entry);
   }

   protected final AddonRegistrationResult registerPreset(String label, String tip, Supplier<List<MacroAction>> builder) {
      return AutismAddons.presets().registerDetailed(label, tip, builder);
   }

   protected final AddonRegistrationResult registerSimpleAction(String localId, String label, String tip, String icon, Consumer<Minecraft> runner) {
      return this.registerAction(this.simpleAction(localId, label, tip, icon, runner));
   }

   protected final AddonRegistrationResult registerSimpleCondition(
      String localId, String label, String tip, String status, String icon, Predicate<Minecraft> predicate
   ) {
      return this.registerAction(this.simpleCondition(localId, label, tip, status, icon, predicate));
   }

   protected final MacroActionEntry simpleAction(String localId, String label, String tip, String icon, Consumer<Minecraft> runner) {
      String typeId = this.id(localId);
      return MacroActionEntry.builder(typeId, () -> new SimpleAction(typeId, label, icon, runner)).picker(label, tip).build();
   }

   protected final MacroActionEntry simpleCondition(String localId, String label, String tip, String status, String icon, Predicate<Minecraft> predicate) {
      String typeId = this.id(localId);
      return MacroActionEntry.builder(typeId, () -> new SimpleCondition(typeId, label, status, icon, predicate)).condition(label, tip).build();
   }
}
