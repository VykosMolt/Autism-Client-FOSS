package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import java.util.List;

public final class AntiBotModule extends Module {
   static final String init_Str = "Conservative";
   static final String load_Str = "Aggressive";
   static final String read_Str = "Custom";
   final ChoiceSetting init_ChoiceSetting = this.add(
      new ChoiceSetting("mode", "Mode", "Conservative", "Conservative", "Aggressive", "Custom").description("Detection strictness preset").build()
   );
   final BoolSetting floatingGround = this.add(
      this.init_BoolSetting(new BoolSetting("floating-ground", "Floating Ground", true)).description("On ground over air")
   );
   final IntSetting init_IntSetting = this.add(
      new IntSetting("ground-flags", "Ground Flags", 10, 1, 50, 1)
         .group("Custom")
         .description("Flags before hiding")
         .visibleWhen(() -> this.isCustomMode() && this.floatingGround.get())
   );
   final BoolSetting noAttributes = this.add(
      this.init_BoolSetting(new BoolSetting("no-attributes", "No Attributes", true)).description("Server never sent attributes")
   );
   final BoolSetting noGameMode = this.add(
      this.init_BoolSetting(new BoolSetting("no-game-mode", "No Game Mode", false)).description("Tab entry lacks game mode")
   );
   final BoolSetting minAge = this.add(this.init_BoolSetting(new BoolSetting("min-age", "Min Age", false)).description("Hide just-spawned players"));
   final IntSetting ageTicks = this.add(
      new IntSetting("age-ticks", "Age", 20, 0, 120, 1)
         .group("Custom")
         .unit("ticks")
         .description("Ticks before shown")
         .visibleWhen(() -> this.isCustomMode() && this.minAge.get())
   );
   final BoolSetting alwaysNear = this.add(
      this.init_BoolSetting(new BoolSetting("always-near", "Always Near", false)).description("Never left your radius")
   );
   final DoubleSetting nearRange = this.add(
      new DoubleSetting("near-range", "Near Range", 20.0, 5.0, 30.0, 1.0)
         .group("Custom")
         .description("Radius in blocks")
         .visibleWhen(() -> this.isCustomMode() && this.alwaysNear.get())
   );
   final BoolSetting sync = this.add(this.init_BoolSetting(new BoolSetting("need-hit", "Need Hit", false)).description("Hidden until you hit"));
   final BoolSetting flush = this.add(this.init_BoolSetting(new BoolSetting("need-swing", "Need Swing", false)).description("Hidden until it swings"));
   final BoolSetting close = this.add(this.init_BoolSetting(new BoolSetting("need-crit", "Need Crit", false)).description("Hidden until crit-hit"));
   final BoolSetting open = this.add(this.init_BoolSetting(new BoolSetting("odd-scale", "Odd Scale", false)).description("Scale is not normal"));
   final BoolSetting poll = this.add(this.init_BoolSetting(new BoolSetting("armor", "Armor", false)).description("Real players wear this"));
   final ChoiceSetting armorHead = this.add(this.init("armor-head", "Helmet", ArmorFilterMatcher.HEAD_OPTIONS));
   final ChoiceSetting armorChest = this.add(this.init("armor-chest", "Chestplate", ArmorFilterMatcher.CHEST_OPTIONS));
   final ChoiceSetting armorLegs = this.add(this.init("armor-legs", "Leggings", ArmorFilterMatcher.BASE_OPTIONS));
   final ChoiceSetting armorFeet = this.add(this.init("armor-feet", "Boots", ArmorFilterMatcher.BASE_OPTIONS));

   public AntiBotModule() {
      super("antibot", "AntiBot", ae.write, "Ignores fake/bot players.");
   }

   private BoolSetting init_BoolSetting(BoolSetting setting) {
      return setting.group("Custom").visibleWhen(this::isCustomMode);
   }

   private ChoiceSetting init(String id, String title, List<String> rules) {
      return new ChoiceSetting(id, title, "Any", rules)
         .group("Custom")
         .description("Any = not checked")
         .visibleWhen(() -> this.isCustomMode() && this.poll.get())
         .build();
   }

   boolean isCustomMode() {
      return "Custom".equals(this.init_ChoiceSetting.get());
   }

   boolean load() {
      return "Aggressive".equals(this.init_ChoiceSetting.get());
   }

   boolean isCheckEnabled(BoolSetting check) {
      return this.isCustomMode() ? check.get() : this.load() && (check == this.floatingGround || check == this.noAttributes);
   }

   int minAgeTicks() {
      return this.isCustomMode() ? this.init_IntSetting.get() : 10;
   }

   @Override
   public void tick() {
      AntiBotTracker.tick();
   }

   @Override
   public void onDisable() {
      AntiBotTracker.resetAll();
   }

   @Override
   public void onGameJoin() {
      AntiBotTracker.resetAll();
   }

   @Override
   public void onGameLeft() {
      AntiBotTracker.resetAll();
   }
}
