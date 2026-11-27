package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.util.BlockSelectionOutlineRenderer;

public final class WorldModule extends Module {
   private boolean darken;
   private String init_Str = "";
   private int darkness = -2147483648;
   private int tint = -2147483648;
   private String blocks = "";

   public WorldModule() {
      super("world", "World", ae.update, "World render tweaks.");
      this.add(new BoolSetting("darken-blocks", "Tint Blocks", true).description("Tint block textures.").group("Block Tint").build());
      this.add(
         new ColorSetting("tint-color", "Color", -16777216)
            .description("Tint color.")
            .group("Block Tint")
            .visibleWhen(() -> this.bool("darken-blocks"))
            .build()
      );
      this.add(
         new IntSetting("darkness", "Strength", 50, 0, 100, 1)
            .description("Tint strength, percent.")
            .group("Block Tint")
            .visibleWhen(() -> this.bool("darken-blocks"))
            .build()
      );
      this.add(
         new ChoiceSetting("mode", "Mode", "Blacklist", "Blacklist", "Whitelist")
            .description("List spares or targets.")
            .group("Block Tint")
            .visibleWhen(() -> this.bool("darken-blocks"))
            .build()
      );
      this.add(
         RegistryListSetting.blocks("blocks", "Blocks")
            .description("Blacklist spares, whitelist targets.")
            .group("Block Tint")
            .visibleWhen(() -> this.bool("darken-blocks"))
            .build()
      );
      this.add(new BoolSetting("block-highlight", "Block Highlight", false).description("Custom block outline.").group("Block Highlight").build());
      this.add(
         new ColorSetting("highlight-color", "Color", -1)
            .description("Outline color.")
            .group("Block Highlight")
            .visibleWhen(() -> this.bool("block-highlight"))
            .build()
      );
      this.add(
         new DoubleSetting("highlight-width", "Line Width", 2.0, 0.5, 6.0, 0.1)
            .description("Outline thickness.")
            .group("Block Highlight")
            .visibleWhen(() -> this.bool("block-highlight"))
            .build()
      );
      this.add(
         new ChoiceSetting("highlight-style", "Style", "Full", "Full", "Corners")
            .description("Full cube or corners.")
            .group("Block Highlight")
            .visibleWhen(() -> this.bool("block-highlight"))
            .build()
      );
      this.add(
         new BoolSetting("highlight-fill", "Fill", false)
            .description("Fill the box faintly.")
            .group("Block Highlight")
            .visibleWhen(() -> this.bool("block-highlight"))
            .build()
      );
      this.add(
         new BoolSetting("highlight-progress", "Show Progress", true)
            .description("Damage color shift.")
            .group("Block Highlight")
            .visibleWhen(() -> this.bool("block-highlight"))
            .build()
      );
      this.add(new BoolSetting("skybox", "Skybox", false).description("Replace the sky with the AUTISM panorama.").group("Skybox").build());
      this.add(
         new BoolSetting("skybox-spin", "Spin", true)
            .description("Slowly spin the skybox panorama.")
            .group("Skybox")
            .visibleWhen(() -> this.bool("skybox"))
            .build()
      );
      this.add(
         new DoubleSetting("skybox-speed", "Spin Speed", 1.0, 0.5, 3.0, 0.1)
            .description("Rotation speed.")
            .group("Skybox")
            .visibleWhen(() -> this.bool("skybox") && this.bool("skybox-spin"))
            .build()
      );
      this.add(
         new BoolSetting("skybox-recolor", "Theme Recolor", false)
            .description("Theme-recolor the skybox panorama.")
            .group("Skybox")
            .visibleWhen(() -> this.bool("skybox"))
            .build()
      );
      this.add(
         new ColorSetting("skybox-color", "Color", -50373)
            .description("Custom panorama color.")
            .group("Skybox")
            .visibleWhen(() -> this.bool("skybox") && !this.bool("skybox-recolor"))
            .build()
      );
   }

   @Override
   public void onEnable() {
      am.init_V();
      this.init();
   }

   @Override
   public void onDisable() {
      am.init_V();
      BlockSelectionOutlineRenderer.disable();
   }

   @Override
   public void tick() {
      boolean darken = this.bool("darken-blocks");
      String mode = this.choice("mode");
      int darkness = this.integer("darkness");
      int tint = am.init_I_Module(this, "tint-color", -16777216);
      String blocks = this.value("blocks");
      boolean switched = darken != this.darken || !mode.equals(this.init_Str) || !blocks.equals(this.blocks);
      if (switched || darkness != this.darkness || tint != this.tint) {
         this.darken = darken;
         this.init_Str = mode;
         this.darkness = darkness;
         this.tint = tint;
         this.blocks = blocks;
         if (switched) {
            am.init_V();
         } else {
            am.load_V();
         }
      }

      this.init();
   }

   private void init() {
      boolean active = this.isEnabled() && this.bool("block-highlight");
      int color = am.init_I_Module(this, "highlight-color", -1);
      float width = (float)this.decimal("highlight-width");
      boolean corners = "Corners".equals(this.choice("highlight-style"));
      boolean fill = this.bool("highlight-fill");
      int fillColor = color & 16777215 | 1073741824;
      BlockSelectionOutlineRenderer.init(active, color, width, corners, fill, fillColor);
      BlockSelectionOutlineRenderer.init(active && this.bool("highlight-progress"));
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      if (this.isEnabled()) {
         this.init();
      }
   }

   @Override
   public boolean ticksWhenDisabled() {
      return false;
   }
}
