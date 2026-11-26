package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.EnumSetting;

public final class NameTagsModule extends Module {
   static final int init = -1;
   static final int load = -5592406;
   static final int read = -1879048192;

   public NameTagsModule() {
      super("nametags", "Nametags", ae.update, "Labels above entities.");
      this.add(new BoolSetting("players", "Players", true).description("Tag players"));
      this.add(new BoolSetting("mobs", "Mobs", false).description("Tag mobs"));
      this.add(new BoolSetting("items", "Items", true).description("Tag dropped items"));
      this.add(new BoolSetting("hide-vanilla", "Hide Vanilla Nametags", true).description("Never show Minecraft's built-in floating name plates"));
      this.add(new BoolSetting("show-health", "Show Health", true).group("Info").description("Show health"));
      this.add(new BoolSetting("show-distance", "Show Distance", false).group("Info").description("Show distance"));
      this.add(new BoolSetting("show-armor", "Show Armor", true).group("Info").description("Armor icons"));
      this.add(new BoolSetting("show-held-item", "Show Held Item", true).group("Info").description("Main hand item"));
      this.add(new BoolSetting("show-enchantments", "Show Enchantments", true).group("Info").description("Armor enchantments"));
      this.add(
         new ColorSetting("distance-color", "Distance Color", -1)
            .group("Colors")
            .description("Distance text color and opacity")
            .visibleWhen(() -> this.bool("show-distance"))
      );
      this.add(
         new ColorSetting("enchantment-color", "Enchantment Color", -5592406)
            .group("Colors")
            .description("Enchantment text color and opacity")
            .visibleWhen(() -> this.bool("show-enchantments"))
      );
      this.add(
         new ColorSetting("background-color", "Background Color", -1879048192)
            .group("Colors")
            .description("Background color and opacity for every nametag row")
      );
      this.add(
         new EnumSetting<>("font", "Font", NameTagsModule.Inner_DataUtil.ONEST, NameTagsModule.Inner_DataUtil.values())
            .group("Display")
            .description("Onest or the vanilla Minecraft font")
      );
      this.add(
         new BoolSetting("pill-background", "Pill Background", true)
            .group("Display")
            .description("Smooth pill-shaped backgrounds; off uses square backgrounds")
      );
      this.add(new DoubleSetting("scale", "Scale", 1.25, 0.5, 4.0, 0.05).group("Display").description("Label size"));
      this.add(new BoolSetting("distance-scale", "Distance Scaling", true).group("Display").description("Gently shrink with distance; keep at least 80% size"));
      this.add(new DoubleSetting("max-distance", "Max Distance", 64.0, 0.0, 256.0, 1.0).group("Display").description("0 = unlimited"));
      this.add(new BoolSetting("group-items", "Group Items", true).group("Items").description("Merge nearby items").visibleWhen(() -> this.bool("items")));
      this.add(
         new DoubleSetting("group-radius", "Group Radius", 5.0, 0.5, 16.0, 0.5)
            .group("Items")
            .description("Item merge radius")
            .visibleWhen(() -> this.bool("items") && this.bool("group-items"))
      );
      this.add(
         new DoubleSetting("items-max-distance", "Items Max Distance", 256.0, 0.0, 256.0, 1.0)
            .group("Items")
            .description("Dropped item tag range; 0 = unlimited")
            .visibleWhen(() -> this.bool("items"))
      );
   }

   @Override
   public void onDisable() {
      NameTagsRenderer.init_V();
   }

   @Override
   public void onGameLeft() {
      NameTagsRenderer.init_V();
   }

   public enum Inner_DataUtil {
      ONEST,
      VANILLA;
   }
}
