package autismclient.modules;

import autismclient.api.module.ActionSetting;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.StringSetting;
import java.util.Locale;

public final class CrosshairModule extends Module {
   private final ColorSetting colorSetting;
   private final ColorSetting outlineColor;
   private final ColorSetting enemyColor;
   private final ColorSetting mobColor;
   private String raw;
   private CrosshairPixelArt drawn;

   public CrosshairModule() {
      super("crosshair", "Crosshair", ae.update, "Custom crosshair shapes and colors.");
      this.add(
         new ChoiceSetting("style", "Style", CrosshairPresetGeometry.Inner_NetHelper.CROSS.init_Str, CrosshairPresetGeometry.Inner_NetHelper.ids())
            .description("Crosshair shape.")
            .group("Shape")
            .build()
      );
      this.add(
         new ActionSetting("edit", "Drawing", this::openEditor)
            .buttonLabel("Edit")
            .description("Draw your own crosshair.")
            .group("Shape")
            .availableOffline()
            .visibleWhen(this::isCustomStyle)
            .build()
      );
      this.add(new StringSetting("art", "Art", "").group("Shape").keepOnReset().visibleWhen(() -> false).build());
      this.add(
         new DoubleSetting("scale", "Scale", 1.0, 0.25, 4.0, 0.05).description("Size, sharp at any value.").group("Shape").formatter(CrosshairModule::formatScale).build()
      );
      this.add(
         new DoubleSetting("thickness", "Thickness", 1.0, 0.5, 4.0, 0.25)
            .description("Line width.")
            .group("Shape")
            .visibleWhen(() -> !this.isCustomStyle())
            .build()
      );
      this.add(
         new DoubleSetting("gap", "Gap", 2.0, 0.0, 8.0, 0.5)
            .description("Space around the dot.")
            .group("Shape")
            .visibleWhen(() -> this.getStyle() == CrosshairPresetGeometry.Inner_NetHelper.TACTICAL)
            .build()
      );
      this.add(new BoolSetting("invert", "Invert", false).description("Negative, like vanilla's.").group("Look").build());
      this.colorSetting = this.add(
         new ColorSetting("color", "Color", -1)
            .description("Crosshair color.")
            .group("Look")
            .visibleWhen(() -> !this.isCustomStyle() && !this.bool("invert"))
            .build()
      );
      this.add(new DoubleSetting("opacity", "Opacity", 100.0, 10.0, 100.0, 5.0).unit("%").group("Look").build());
      this.add(new BoolSetting("outline", "Outline", true).description("Border around the shape.").group("Look").build());
      this.outlineColor = this.add(new ColorSetting("outline-color", "Outline Color", -16777216).group("Look").visibleWhen(() -> this.bool("outline")).build());
      this.add(new DoubleSetting("outline-width", "Outline Width", 0.5, 0.5, 3.0, 0.5).group("Look").visibleWhen(() -> this.bool("outline")).build());
      this.add(new BoolSetting("target-colors", "Target Colors", true).description("Red enemies, green mobs.").group("Targets").build());
      this.enemyColor = this.add(
         new ColorSetting("enemy-color", "Enemy Color", -50373)
            .description("Hostiles and players.")
            .group("Targets")
            .visibleWhen(() -> this.bool("target-colors"))
            .build()
      );
      this.mobColor = this.add(
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
      CrosshairRenderState.clear();
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

   public CrosshairPresetGeometry.Inner_NetHelper getStyle() {
      return CrosshairPresetGeometry.Inner_NetHelper.init(this.choice("style"));
   }

   private boolean isCustomStyle() {
      return this.getStyle() == CrosshairPresetGeometry.Inner_NetHelper.CUSTOM;
   }

   public double thickness() {
      return this.decimal("thickness");
   }

   public double gap() {
      return this.decimal("gap");
   }

   public CrosshairPixelArt art() {
      String raw = this.text("art");
      if (this.drawn == null || !raw.equals(this.raw)) {
         CrosshairPixelArt decoded = CrosshairPixelArt.init(raw);
         this.drawn = decoded != null ? decoded : defaultArt();
         this.raw = raw;
      }

      return this.drawn;
   }

   public void setArt(CrosshairPixelArt drawn) {
      if (drawn != null) {
         this.drawn = drawn;
         this.raw = drawn.encode();
         this.setValue("art", this.raw);
      }
   }

   public static CrosshairPixelArt defaultArt() {
      return CrosshairPresetGeometry.init(CrosshairPresetGeometry.Inner_NetHelper.TACTICAL, 16, 1.0, 2.0, -1);
   }

   public CrosshairRenderState.Inner_DataUtil init_q_DataUtil_m(CrosshairPixelArt drawing) {
      return new CrosshairRenderState.Inner_DataUtil(
         this.getStyle(),
         this.decimal("scale"),
         this.thickness(),
         this.gap(),
         this.bool("invert"),
         this.colorSetting.get(),
         (float)(this.decimal("opacity") / 100.0),
         this.bool("outline"),
         this.outlineColor.get(),
         this.decimal("outline-width"),
         this.bool("target-colors"),
         this.enemyColor.get(),
         this.mobColor.get(),
         this.bool("far-enemies"),
         this.decimal("far-range"),
         drawing
      );
   }

   private void init_V_50() {
      CrosshairRenderState.init(this.init_q_DataUtil_m(this.art()));
   }

   private void openEditor() {
      if (MC != null && MC.gui != null) {
         MC.gui.setScreen(new autismclient.gui.screens.CrosshairEditorScreen(MC.gui.screen(), this));
      }
   }

   private static String formatScale(String raw) {
      try {
         return String.format(Locale.ROOT, "%.2fx", Double.parseDouble(raw));
      } catch (NumberFormatException e) {
         return raw;
      }
   }
}
