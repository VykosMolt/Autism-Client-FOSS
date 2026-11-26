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
   final BoolSetting init_BoolSetting = this.add(
      this.init_BoolSetting(new BoolSetting("floating-ground", "Floating Ground", true)).description("On ground over air")
   );
   final IntSetting init_IntSetting = this.add(
      new IntSetting("ground-flags", "Ground Flags", 10, 1, 50, 1)
         .group("Custom")
         .description("Flags before hiding")
         .visibleWhen(() -> this.init_Z_13() && this.init_BoolSetting.get())
   );
   final BoolSetting load_BoolSetting = this.add(
      this.init_BoolSetting(new BoolSetting("no-attributes", "No Attributes", true)).description("Server never sent attributes")
   );
   final BoolSetting read_BoolSetting = this.add(
      this.init_BoolSetting(new BoolSetting("no-game-mode", "No Game Mode", false)).description("Tab entry lacks game mode")
   );
   final BoolSetting write_BoolSetting = this.add(this.init_BoolSetting(new BoolSetting("min-age", "Min Age", false)).description("Hide just-spawned players"));
   final IntSetting load_IntSetting = this.add(
      new IntSetting("age-ticks", "Age", 20, 0, 120, 1)
         .group("Custom")
         .unit("ticks")
         .description("Ticks before shown")
         .visibleWhen(() -> this.init_Z_13() && this.write_BoolSetting.get())
   );
   final BoolSetting update_BoolSetting = this.add(
      this.init_BoolSetting(new BoolSetting("always-near", "Always Near", false)).description("Never left your radius")
   );
   final DoubleSetting init_DoubleSetting = this.add(
      new DoubleSetting("near-range", "Near Range", 20.0, 5.0, 30.0, 1.0)
         .group("Custom")
         .description("Radius in blocks")
         .visibleWhen(() -> this.init_Z_13() && this.update_BoolSetting.get())
   );
   final BoolSetting sync = this.add(this.init_BoolSetting(new BoolSetting("need-hit", "Need Hit", false)).description("Hidden until you hit"));
   final BoolSetting flush = this.add(this.init_BoolSetting(new BoolSetting("need-swing", "Need Swing", false)).description("Hidden until it swings"));
   final BoolSetting close = this.add(this.init_BoolSetting(new BoolSetting("need-crit", "Need Crit", false)).description("Hidden until crit-hit"));
   final BoolSetting open = this.add(this.init_BoolSetting(new BoolSetting("odd-scale", "Odd Scale", false)).description("Scale is not normal"));
   final BoolSetting poll = this.add(this.init_BoolSetting(new BoolSetting("armor", "Armor", false)).description("Real players wear this"));
   final ChoiceSetting load_ChoiceSetting = this.add(this.init("armor-head", "Helmet", ArmorFilterMatcher.load_List));
   final ChoiceSetting read_ChoiceSetting = this.add(this.init("armor-chest", "Chestplate", ArmorFilterMatcher.read_List));
   final ChoiceSetting write_ChoiceSetting = this.add(this.init("armor-legs", "Leggings", ArmorFilterMatcher.init_List));
   final ChoiceSetting update_ChoiceSetting = this.add(this.init("armor-feet", "Boots", ArmorFilterMatcher.init_List));

   public AntiBotModule() {
      super("antibot", "AntiBot", ae.write, "Ignores fake/bot players.");
   }

   private BoolSetting init_BoolSetting(BoolSetting setting) {
      return setting.group("Custom").visibleWhen(this::init_Z_13);
   }

   private ChoiceSetting init(String id, String title, List<String> rules) {
      return new ChoiceSetting(id, title, "Any", rules)
         .group("Custom")
         .description("Any = not checked")
         .visibleWhen(() -> this.init_Z_13() && this.poll.get())
         .build();
   }

   boolean init_Z_13() {
      return "Custom".equals(this.init_ChoiceSetting.get());
   }

   boolean load() {
      return "Aggressive".equals(this.init_ChoiceSetting.get());
   }

   boolean init_Z(BoolSetting check) {
      return this.init_Z_13() ? check.get() : this.load() && (check == this.init_BoolSetting || check == this.load_BoolSetting);
   }

   int init_I_8() {
      return this.init_Z_13() ? this.init_IntSetting.get() : 10;
   }

   @Override
   public void tick() {
      AntiBotTracker.init_V();
   }

   @Override
   public void onDisable() {
      AntiBotTracker.load();
   }

   @Override
   public void onGameJoin() {
      AntiBotTracker.load();
   }

   @Override
   public void onGameLeft() {
      AntiBotTracker.load();
   }
}
