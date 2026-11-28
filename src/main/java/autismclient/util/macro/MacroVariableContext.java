package autismclient.util.macro;

import autismclient.util.bi;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;

public final class MacroVariableContext {
   private static final ThreadLocal<MacroVariableStore> FALLBACK_STORE = ThreadLocal.withInitial(MacroVariableStore::new);

   private MacroVariableContext() {
   }

   public static void clear() {
      activeOrFallbackStore().clear();
   }

   public static void set(String name, Object value) {
      setValue(name, value instanceof MacroVarValue typed ? typed : MacroVarValue.ofText(value));
   }

   public static void setValue(String name, MacroVarValue value) {
      activeOrFallbackStore().set(name, value);
   }

   public static void setAll(Map<String, MacroVarValue> values) {
      activeOrFallbackStore().putAll(values);
   }

   public static void remove(String name) {
      activeOrFallbackStore().remove(name);
   }

   public static String resolveVariable(String name, Minecraft mc) {
      MacroTemplateResolver.ResolveResult resolution = MacroTemplateResolver.resolve("{" + name + "}", activeOrFallbackStore(), mc);
      return resolution.success() ? resolution.value() : "";
   }

   public static Optional<MacroVarValue> get(String name) {
      return activeOrFallbackStore().lookup(name);
   }

   public static String resolveString(String text, Minecraft mc) {
      MacroTemplateResolver.ResolveResult resolution = resolveTemplate(text, mc);
      return resolution.success() ? resolution.value() : "";
   }

   public static MacroTemplateResolver.ResolveResult resolveTemplate(String text, Minecraft mc) {
      return MacroTemplateResolver.resolve(text, activeOrFallbackStore(), mc);
   }

   public static boolean requireResolved(String label, MacroTemplateResolver.ResolveResult... resolutions) {
      LinkedHashSet<String> missing = new LinkedHashSet<>();
      boolean failed = false;

      for (MacroTemplateResolver.ResolveResult resolution : resolutions) {
         if (resolution != null && !resolution.success()) {
            failed = true;
            missing.addAll(resolution.missingVariables());
         }
      }

      if (!failed) {
         return true;
      }

      bi.write(missing.isEmpty() ? label + ": a value could not be resolved" : label + ": no value for {" + String.join("}, {", missing) + "}");
      return false;
   }

   public static MacroVariableStore currentStore() {
      return activeOrFallbackStore();
   }

   private static MacroVariableStore activeOrFallbackStore() {
      MacroVariableStore active = MacroExecutor.getCurrentVariableStore();
      return active == null ? FALLBACK_STORE.get() : active;
   }
}
