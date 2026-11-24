package autismclient.modules;

import autismclient.api.module.ActionSetting;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.StringSetting;
import java.util.Locale;

public final class n extends Module {
   private final ColorSetting init_ColorSetting;
   private final ColorSetting load;
   private final ColorSetting read;
   private final ColorSetting write;
   private String init_Str;
   private m init_m;

   public n() {
      super("crosshair", "Crosshair", ae.update, "Custom crosshair shapes and colors.");
      this.add(
         new ChoiceSetting("style", "Style", p.Inner_NetHelper.init_p_NetHelper.init_Str, p.Inner_NetHelper.init_StrArr())
            .description("Crosshair shape.")
            .group("Shape")
            .build()
      );
      this.add(
         new ActionSetting("edit", "Drawing", this::load_V_33)
            .buttonLabel("Edit")
            .description("Draw your own crosshair.")
            .group("Shape")
            .availableOffline()
            .visibleWhen(this::init_Z_50)
            .build()
      );
      this.add(new StringSetting("art", "Art", "").group("Shape").keepOnReset().visibleWhen(() -> false).build());
      this.add(
         new DoubleSetting("scale", "Scale", 1.0, 0.25, 4.0, 0.05).description("Size, sharp at any value.").group("Shape").formatter(n::init_Str_Str_4).build()
      );
      this.add(
         new DoubleSetting("thickness", "Thickness", 1.0, 0.5, 4.0, 0.25)
            .description("Line width.")
            .group("Shape")
            .visibleWhen(() -> !this.init_Z_50())
            .build()
      );
      this.add(
         new DoubleSetting("gap", "Gap", 2.0, 0.0, 8.0, 0.5)
            .description("Space around the dot.")
            .group("Shape")
            .visibleWhen(() -> this.init_p_NetHelper() == p.Inner_NetHelper.load)
            .build()
      );
      this.add(new BoolSetting("invert", "Invert", false).description("Negative, like vanilla's.").group("Look").build());
      this.init_ColorSetting = this.add(
         new ColorSetting("color", "Color", -1)
            .description("Crosshair color.")
            .group("Look")
            .visibleWhen(() -> !this.init_Z_50() && !this.bool("invert"))
            .build()
      );
      this.add(new DoubleSetting("opacity", "Opacity", 100.0, 10.0, 100.0, 5.0).unit("%").group("Look").build());
      this.add(new BoolSetting("outline", "Outline", true).description("Border around the shape.").group("Look").build());
      this.load = this.add(new ColorSetting("outline-color", "Outline Color", -16777216).group("Look").visibleWhen(() -> this.bool("outline")).build());
      this.add(new DoubleSetting("outline-width", "Outline Width", 0.5, 0.5, 3.0, 0.5).group("Look").visibleWhen(() -> this.bool("outline")).build());
      this.add(new BoolSetting("target-colors", "Target Colors", true).description("Red enemies, green mobs.").group("Targets").build());
      this.read = this.add(
         new ColorSetting("enemy-color", "Enemy Color", -50373)
            .description("Hostiles and players.")
            .group("Targets")
            .visibleWhen(() -> this.bool("target-colors"))
            .build()
      );
      this.write = this.add(
         new ColorSetting("mob-color", "Mob Color", -12845206)
            .description("Other mobs in reach.")
            .group("Targets")
            .visibleWhen(() -> this.bool("target-colors"))
            .build()
      );
      this.add(
         new BoolSetting("far-enemies", "Far Enemies", false)
            .description("Red past your reach too.")
            .group("Targets")
            .visibleWhen(() -> this.bool("target-colors"))
            .build()
      );
      this.add(
         new DoubleSetting("far-range", "Far Range", 48.0, 8.0, 128.0, 1.0)
            .unit("blocks")
            .group("Targets")
            .visibleWhen(() -> this.bool("target-colors") && this.bool("far-enemies"))
            .build()
      );
   }

   @Override
   public void onEnable() {
      this.init_V_50();
   }

   @Override
   public void onDisable() {
      q.init_V();
   }

   @Override
   public void tick() {
      this.init_V_50();
   }

   @Override
   public boolean ticksWhenDisabled() {
      return false;
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      if (this.isEnabled()) {
         this.init_V_50();
      }
   }

   public p.Inner_NetHelper init_p_NetHelper() {
      return p.Inner_NetHelper.init(this.choice("style"));
   }

   private boolean init_Z_50() {
      return this.init_p_NetHelper() == p.Inner_NetHelper.update;
   }

   public double init_D_14() {
      return this.decimal("thickness");
   }

   public double load_D_5() {
      return this.decimal("gap");
   }

   public m init_m() {
      String raw = this.text("art");
      if (this.init_m == null || !raw.equals(this.init_Str)) {
         m decoded = m.init(raw);
         this.init_m = decoded != null ? decoded : load_m();
         this.init_Str = raw;
      }

      return this.init_m;
   }

   public void init_V_m(m drawn) {
      if (drawn != null) {
         this.init_m = drawn;
         this.init_Str = drawn.init_Str();
         this.setValue("art", this.init_Str);
      }
   }

   public static m load_m() {
      return p.init(p.Inner_NetHelper.load, 16, 1.0, 2.0, -1);
   }

   public q.Inner_DataUtil init_q_DataUtil_m(m drawing) {
      return new q.Inner_DataUtil(
         this.init_p_NetHelper(),
         this.decimal("scale"),
         this.init_D_14(),
         this.load_D_5(),
         this.bool("invert"),
         this.init_ColorSetting.get(),
         (float)(this.decimal("opacity") / 100.0),
         this.bool("outline"),
         this.load.get(),
         this.decimal("outline-width"),
         this.bool("target-colors"),
         this.read.get(),
         this.write.get(),
         this.bool("far-enemies"),
         this.decimal("far-range"),
         drawing
      );
   }

   private void init_V_50() {
      q.init(this.init_q_DataUtil_m(this.init_m()));
   }

   private void load_V_33() {
      if (MC != null && MC.gui != null) {
         MC.gui.setScreen(new autismclient.gui.d.NetHelper(MC.gui.screen(), this));
      }
   }

   private static String init_Str_Str_4(String raw) {
      try {
         return String.format(Locale.ROOT, "%.2fx", Double.parseDouble(raw));
      } catch (NumberFormatException e) {
         return raw;
      }
   }
}
