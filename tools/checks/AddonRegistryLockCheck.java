package autismclient.b;

import autismclient.api.hud.HudElementProvider;
import autismclient.api.hud.HudElements;
import autismclient.api.macro.MacroActionEntry;
import autismclient.api.macro.MacroActionRegistry;
import autismclient.api.macro.MacroPresetRegistry;
import autismclient.util.macro.MacroAction;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.nbt.CompoundTag;

public class AddonRegistryLockCheck {
   private static boolean underLock;

   public static void main(String[] args) throws Exception {
      Class<?> ctxType = Class.forName("autismclient.b.DataUtil$BufferHelper");
      Constructor<?> ctor = ctxType.getDeclaredConstructor(String.class, String.class);
      ctor.setAccessible(true);
      Field current = DataUtil.class.getDeclaredField("AUDIT_CURRENT_ADDON");
      current.setAccessible(true);
      ((ThreadLocal<Object>)current.get(null)).set(ctor.newInstance("lockcheck", "Lock Check"));

      check(HudElements.registerDetailed(new HudElementProvider() {
         public String id() {
            return "lockcheck:hud";
         }

         public String label() {
            return "hud";
         }

         public int width() {
            underLock |= Thread.holdsLock(HudElements.class);
            return 10;
         }

         public int height() {
            return 10;
         }

         public void render(GuiGraphicsExtractor g, Font f, int x, int y, float d) {
         }
      }).accepted(), "hud element accepted");
      check(!underLock, "hud sizing ran outside the HudElements lock");

      check(MacroActionRegistry.registerDetailed(MacroActionEntry.builder("lockcheck:act", () -> {
         underLock |= Thread.holdsLock(MacroActionRegistry.class);
         return new Probe();
      }).build()).accepted(), "macro action accepted");
      check(!underLock, "action factory ran outside the MacroActionRegistry lock");
      check(MacroActionRegistry.factory("lockcheck:act") != null, "macro action inserted");

      check(MacroPresetRegistry.registerDetailed("cat", "preset", "", () -> {
         underLock |= Thread.holdsLock(MacroPresetRegistry.class);
         return List.of(new Probe());
      }).accepted(), "preset accepted");
      check(!underLock, "preset builder ran outside the MacroPresetRegistry lock");
      check(MacroPresetRegistry.entries().size() == 1, "preset inserted");
      System.out.println("PASS AddonRegistryLockCheck");
   }

   private static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL " + what);
         System.exit(1);
      }
   }

   static class Probe implements MacroAction {
      public void execute(Minecraft mc) {
      }

      public CompoundTag toTag() {
         underLock |= Thread.holdsLock(MacroActionRegistry.class);
         CompoundTag tag = new CompoundTag();
         tag.putString("type", "lockcheck:act");
         return tag;
      }

      public void fromTag(CompoundTag tag) {
      }

      public String getDisplayName() {
         return "probe";
      }

      public String getIcon() {
         return "P";
      }
   }
}
