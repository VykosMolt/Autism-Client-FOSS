package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;

public final class bb extends Module {
   public bb() {
      super("viewmodel", "Viewmodel", ae.update, "Customize first-person hand position and animations.");
      this.add(new BoolSetting("main-hand", "Main Hand", false).description("Move the main hand item.").group("Main Hand").build());
      this.add(new DoubleSetting("main-hand-scale", "Item Scale", 0.0, -5.0, 5.0, 0.05).group("Main Hand").visibleWhen(() -> this.bool("main-hand")).build());
      this.add(new DoubleSetting("main-hand-x", "X", 0.0, -5.0, 5.0, 0.05).group("Main Hand").visibleWhen(() -> this.bool("main-hand")).build());
      this.add(new DoubleSetting("main-hand-y", "Y", 0.0, -5.0, 5.0, 0.05).group("Main Hand").visibleWhen(() -> this.bool("main-hand")).build());
      this.add(new DoubleSetting("main-hand-rot-x", "Rotation X", 0.0, -50.0, 50.0, 1.0).group("Main Hand").visibleWhen(() -> this.bool("main-hand")).build());
      this.add(new DoubleSetting("main-hand-rot-y", "Rotation Y", 0.0, -50.0, 50.0, 1.0).group("Main Hand").visibleWhen(() -> this.bool("main-hand")).build());
      this.add(new DoubleSetting("main-hand-rot-z", "Rotation Z", 0.0, -50.0, 50.0, 1.0).group("Main Hand").visibleWhen(() -> this.bool("main-hand")).build());
      this.add(new BoolSetting("off-hand", "Off Hand", false).description("Move the off hand item.").group("Off Hand").build());
      this.add(new DoubleSetting("off-hand-scale", "Item Scale", 0.0, -5.0, 5.0, 0.05).group("Off Hand").visibleWhen(() -> this.bool("off-hand")).build());
      this.add(new DoubleSetting("off-hand-x", "X", 0.0, -1.0, 1.0, 0.02).group("Off Hand").visibleWhen(() -> this.bool("off-hand")).build());
      this.add(new DoubleSetting("off-hand-y", "Y", 0.0, -1.0, 1.0, 0.02).group("Off Hand").visibleWhen(() -> this.bool("off-hand")).build());
      this.add(new DoubleSetting("off-hand-rot-x", "Rotation X", 0.0, -50.0, 50.0, 1.0).group("Off Hand").visibleWhen(() -> this.bool("off-hand")).build());
      this.add(new DoubleSetting("off-hand-rot-y", "Rotation Y", 0.0, -50.0, 50.0, 1.0).group("Off Hand").visibleWhen(() -> this.bool("off-hand")).build());
      this.add(new DoubleSetting("off-hand-rot-z", "Rotation Z", 0.0, -50.0, 50.0, 1.0).group("Off Hand").visibleWhen(() -> this.bool("off-hand")).build());
      this.add(new IntSetting("swing-duration", "Swing Duration", 6, 1, 20, 1).description("Attack animation length (ticks)."));
      this.add(
         new ChoiceSetting("blocking-animation", "Blocking Animation", "1.7", "1.7", "Pushdown")
            .description("Sword swing animation.")
            .group("Blocking Animation")
            .build()
      );
      this.add(
         new DoubleSetting("one-seven-y", "1.7 Y", 0.1, 0.05, 0.3, 0.01)
            .group("Blocking Animation")
            .visibleWhen(() -> "1.7".equals(this.choice("blocking-animation")))
            .build()
      );
      this.add(
         new DoubleSetting("one-seven-swing-scale", "1.7 Swing Scale", 0.9, 0.1, 1.0, 0.05)
            .group("Blocking Animation")
            .visibleWhen(() -> "1.7".equals(this.choice("blocking-animation")))
            .build()
      );
      this.add(new BoolSetting("equip-offset", "Equip Offset", true).description("Item lower/raise animation.").group("Equip Offset").build());
      this.add(
         new BoolSetting("ignore-blocking", "Ignore Blocking", true)
            .description("Skip the blocking offset.")
            .group("Equip Offset")
            .visibleWhen(() -> this.bool("equip-offset"))
            .build()
      );
      this.add(
         new BoolSetting("ignore-place", "Ignore Place", true)
            .description("Skip the place bump.")
            .group("Equip Offset")
            .visibleWhen(() -> this.bool("equip-offset"))
            .build()
      );
      this.add(
         new BoolSetting("ignore-amount", "Ignore Amount", false)
            .description("Skip on count change.")
            .group("Equip Offset")
            .visibleWhen(() -> this.bool("equip-offset"))
            .build()
      );
      this.add(new BoolSetting("air-walker", "Air Walker", false).description("Keep the walk bob in the air."));
   }

   @Override
   public void onEnable() {
      this.init();
   }

   @Override
   public void onDisable() {
      bc.load_V();
   }

   @Override
   public void tick() {
      this.init();
   }

   @Override
   public boolean ticksWhenDisabled() {
      return false;
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      if (this.isEnabled()) {
         this.init();
      }
   }

   private void init() {
      bc.init_Z = this.bool("main-hand");
      bc.init_F = (float)this.decimal("main-hand-scale");
      bc.load_F = (float)this.decimal("main-hand-x");
      bc.read_F = (float)this.decimal("main-hand-y");
      bc.write_F = (float)this.decimal("main-hand-rot-x");
      bc.update_F = (float)this.decimal("main-hand-rot-y");
      bc.sync_F = (float)this.decimal("main-hand-rot-z");
      bc.load_Z = this.bool("off-hand");
      bc.flush_F = (float)this.decimal("off-hand-scale");
      bc.close_F = (float)this.decimal("off-hand-x");
      bc.open = (float)this.decimal("off-hand-y");
      bc.poll = (float)this.decimal("off-hand-rot-x");
      bc.push = (float)this.decimal("off-hand-rot-y");
      bc.pop = (float)this.decimal("off-hand-rot-z");
      bc.init_I = this.integer("swing-duration");
      bc.load_I = "Pushdown".equals(this.choice("blocking-animation")) ? 1 : 0;
      bc.tick = (float)this.decimal("one-seven-y");
      bc.run = (float)this.decimal("one-seven-swing-scale");
      bc.read_Z = this.bool("equip-offset");
      bc.write_Z = this.bool("ignore-blocking");
      bc.update_Z = this.bool("ignore-place");
      bc.sync_Z = this.bool("ignore-amount");
      bc.flush_Z = this.bool("air-walker");
      bc.init_V();
   }
}
