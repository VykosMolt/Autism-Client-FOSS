package autismclient.util;

import autismclient.AutismClientAddon;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.File;
import java.util.ArrayList;
import java.util.function.Supplier;

final class AuditConfigSafety {
   private static final ThreadLocal<ArrayList<Runnable>> deferredHooks = new ThreadLocal<>();
   private static final Gson gson = new Gson();

   private AuditConfigSafety() {
   }

   static AutismConfig initialize(Supplier<AutismConfig> var0) {
      ArrayList var1 = deferredHooks.get();
      if (var1 != null) {
         return var0.get();
      }

      ArrayList var2 = new ArrayList();
      deferredHooks.set(var2);

      AutismConfig var3;
      try {
         var3 = var0.get();
      } finally {
         deferredHooks.remove();
      }

      for (Runnable var5 : (java.util.List<Runnable>)var2) {
         try {
            var5.run();
         } catch (Throwable var10) {
            AutismClientAddon.LOG.error("Autism config afterSave hook failed", var10);
         }
      }

      return var3;
   }

   static Runnable deferHook(Runnable var0) {
      ArrayList var1 = deferredHooks.get();
      if (var0 != null && var1 != null) {
         var1.add(var0);
         return null;
      } else {
         return var0;
      }
   }

   static AutismConfig backupConfig(AutismConfig var0, File var1) {
      if (var0 != null && var1.isFile()) {
         return hasSettings(var0) ? var0 : null;
      } else {
         return var0;
      }
   }

   static AutismProfile normalizeBackupProfile(AutismProfile var0) {
      boolean var1 = hasText(var0.id)
         || hasText(var0.displayName)
         || var0.autoSave
         || var0.ownMacroLibrary
         || !var0.ownThemeColor
         || var0.serverPatterns != null && var0.serverPatterns.stream().anyMatch(AuditConfigSafety::hasText)
         || hasSettings(var0.snapshot);
      if (!var1) {
         return null;
      }

      var0.normalize();
      return var0;
   }

   private static boolean hasText(String var0) {
      return var0 != null && !var0.isBlank();
   }

   private static boolean hasSettings(AutismConfig var0) {
      if (var0 == null) {
         return false;
      } else {
         return var0.serverPluginScans != null && !var0.serverPluginScans.isEmpty() ? true : !gson.toJsonTree(var0).equals(AuditConfigSafety.Defaults.config);
      }
   }

   private static final class Defaults {
      private static final JsonElement config = AuditConfigSafety.gson.toJsonTree(new AutismConfig());
   }
}
