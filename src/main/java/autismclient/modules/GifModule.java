package autismclient.modules;

import autismclient.api.module.ActionSetting;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.StringListSetting;
import autismclient.api.module.StringSetting;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GifModule extends Module {
   public GifModule() {
      super("gif", "GIF", ae.update, "Custom animated images on capes, shields and the HUD.");
      this.add(new BoolSetting("cape-enabled", "Custom Cape", false).description("Custom image instead of the cape.").group("Cape").build());
      this.add(
         new StringSetting("cape-image", "Image", "")
            .description("GIF, PNG or JPEG.")
            .group("Cape")
            .formatter(GifModule::read)
            .filePicker("cape-pick")
            .visibleWhen(() -> this.bool("cape-enabled"))
            .build()
      );
      this.add(
         new ActionSetting("cape-pick", "Pick File", this::pickCapeImage)
            .description("Choose image file.")
            .group("Cape")
            .availableOffline()
            .visibleWhen(() -> false)
            .build()
      );
      this.add(
         new ChoiceSetting("cape-target", "Show On", "Self", "Self", "Everyone", "Others")
            .description("Who wears the image.")
            .group("Cape")
            .visibleWhen(() -> this.bool("cape-enabled"))
            .build()
      );
      this.add(
         new StringListSetting("cape-custom", "Custom Players", "")
            .description("name=image path per player.")
            .group("Cape")
            .playerNameList()
            .visibleWhen(() -> this.bool("cape-enabled"))
            .build()
      );
      this.add(
         new DoubleSetting("cape-fps", "Framerate (FPS)", 0.0, 0.0, 60.0, 0.5)
            .description("Animation speed; Original keeps the file's own timing.")
            .group("Cape")
            .formatter(GifModule::load)
            .visibleWhen(() -> this.bool("cape-enabled"))
            .build()
      );
      this.add(
         new IntSetting("cape-quality", "Quality", 4, 1, 4, 1)
            .description("Render resolution; Original is the file's own.")
            .group("Cape")
            .formatter(GifModule::init_Str)
            .visibleWhen(() -> this.bool("cape-enabled"))
            .build()
      );
      this.add(new BoolSetting("shield-enabled", "Custom Shield", false).description("Custom image instead of the shield.").group("Shield").build());
      this.add(
         new StringSetting("shield-image", "Image", "")
            .description("GIF, PNG or JPEG.")
            .group("Shield")
            .formatter(GifModule::read)
            .filePicker("shield-pick")
            .visibleWhen(() -> this.bool("shield-enabled"))
            .build()
      );
      this.add(
         new ActionSetting("shield-pick", "Pick File", this::load)
            .description("Choose image file.")
            .group("Shield")
            .availableOffline()
            .visibleWhen(() -> false)
            .build()
      );
      this.add(
         new ChoiceSetting("shield-target", "Show On", "Self", "Self", "Everyone", "Others")
            .description("Who wears the image.")
            .group("Shield")
            .visibleWhen(() -> this.bool("shield-enabled"))
            .build()
      );
      this.add(
         new StringListSetting("shield-custom", "Custom Players", "")
            .description("name=image path per player.")
            .group("Shield")
            .playerNameList()
            .visibleWhen(() -> this.bool("shield-enabled"))
            .build()
      );
      this.add(
         new DoubleSetting("shield-fps", "Framerate (FPS)", 0.0, 0.0, 60.0, 0.5)
            .description("Animation speed; Original keeps the file's own timing.")
            .group("Shield")
            .formatter(GifModule::load)
            .visibleWhen(() -> this.bool("shield-enabled"))
            .build()
      );
      this.add(
         new IntSetting("shield-quality", "Quality", 4, 1, 4, 1)
            .description("Render resolution; Original is the file's own.")
            .group("Shield")
            .formatter(GifModule::init_Str)
            .visibleWhen(() -> this.bool("shield-enabled"))
            .build()
      );
      this.add(
         new BoolSetting("cape-keep-aspect", "Cape: Preserve Aspect", false)
            .description("Fit instead of stretching.")
            .group("Advanced")
            .visibleWhen(() -> this.bool("cape-enabled"))
            .build()
      );
      this.add(
         new BoolSetting("shield-keep-aspect", "Shield: Preserve Aspect", false)
            .description("Fit instead of stretching.")
            .group("Advanced")
            .visibleWhen(() -> this.bool("shield-enabled"))
            .build()
      );
      this.add(
         new ActionSetting("reload-images", "Reload Images", () -> autismclient.util.gif.GifTextureManager.reloadAll())
            .description("Drop every cached image so files are re-read.")
            .group("Advanced")
            .availableOffline()
            .buttonLabel("Reload")
            .build()
      );
   }

   @Override
   public boolean settingsShareable() {
      return false;
   }

   @Override
   public void onEnable() {
      this.read();
   }

   @Override
   public void onDisable() {
      this.read();
   }

   @Override
   public void tick() {
      this.read();
   }

   @Override
   public void onGameJoin() {
      this.read();
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      this.read();
   }

   @Override
   protected void onSettingsReset() {
      this.read();
   }

   private void pickCapeImage() {
      autismclient.util.NativeImageFilePicker.pickImage(this.text("cape-image").trim(), selected -> this.setValue("cape-image", selected));
   }

   private void load() {
      autismclient.util.NativeImageFilePicker.pickImage(this.text("shield-image").trim(), selected -> this.setValue("shield-image", selected));
   }

   private void read() {
      GifOverlayState.init(
         new GifOverlayState.OverlaySnapshot(
            this.isEnabledRaw(),
            this.bool("cape-enabled"),
            this.bool("shield-enabled"),
            this.text("cape-image").trim(),
            this.text("shield-image").trim(),
            this.choice("cape-target"),
            this.choice("shield-target"),
            this.decimal("cape-fps"),
            this.decimal("shield-fps"),
            this.bool("cape-keep-aspect"),
            this.bool("shield-keep-aspect"),
            init(this.integer("cape-quality")),
            init(this.integer("shield-quality")),
            init(this.list("cape-custom")),
            init(this.list("shield-custom")),
            this.init_Str_3()
         )
      );
   }

   private String init_Str_3() {
      if (MC != null && MC.getUser() != null) {
         String name = MC.getUser().getName();
         if (name != null && !name.isBlank()) {
            return name;
         }
      }

      return GifOverlayState.init_Str();
   }

   private static Map<String, String> init(List<String> entries) {
      Map<String, String> out = new LinkedHashMap<>();

      for (String entry : entries) {
         int split = init_I(entry);
         if (split > 0) {
            String name = entry.substring(0, split).trim();
            String path = entry.substring(split + 1).trim();
            if (!name.isEmpty() && !path.isEmpty()) {
               out.put(name.toLowerCase(Locale.ROOT), path);
            }
         }
      }

      return Collections.unmodifiableMap(out);
   }

   private static int init_I(String entry) {
      int split = entry.indexOf(61);
      return split >= 0 ? split : entry.indexOf(58);
   }

   private static double init(int step) {
      double var10000;
      switch (step) {
         case 1:
            var10000 = 0.25;
            break;
         case 2:
            var10000 = 0.5;
            break;
         case 3:
            var10000 = 0.75;
            break;
         default:
            var10000 = 1.0;
      }

      return var10000;
   }

   private static String init_Str(String raw) {
      String var10000;
      switch (raw == null ? "" : raw.trim()) {
         case "1":
            var10000 = "Low";
            break;
         case "2":
            var10000 = "Medium";
            break;
         case "3":
            var10000 = "High";
            break;
         default:
            var10000 = "Original";
      }

      return var10000;
   }

   private static String load(String raw) {
      try {
         double value = Double.parseDouble(raw.trim());
         if (value <= 0.0) {
            return "Original";
         } else {
            return value == Math.floor(value) ? Integer.toString((int)value) : raw;
         }
      } catch (Exception e) {
         return raw;
      }
   }

   private static String read(String value) {
      if (value != null && !value.isBlank()) {
         try {
            Path name = Paths.get(value).getFileName();
            return name == null ? value : name.toString();
         } catch (Exception ignored) {
            return value;
         }
      } else {
         return "No file selected";
      }
   }
}
