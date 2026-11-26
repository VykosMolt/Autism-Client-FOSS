package autismclient.gui.f.c;

import autismclient.gui.f.b.MethodUtil;
import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemeTextureRecolorer;
import net.minecraft.resources.Identifier;

public final class ConfigStore extends TableIndex {
   private String init_Str;
   private Identifier init_Identifier;
   private int init_I = 12;
   private int load_I = 5;
   private boolean write_Z;

   public ConfigStore(String text, Identifier icon) {
      this.init_Str = text == null ? "" : text;
      this.init_Identifier = icon;
      this.write = 14.0F;
   }

   public ConfigStore init(int iconSize) {
      this.init_I = Math.max(1, iconSize);
      this.load_V();
      return this;
   }

   public ConfigStore init_ConfigStore(boolean connectedStyle) {
      this.write_Z = connectedStyle;
      return this;
   }

   @Override
   public float init_F_SyncManager(SyncManager context) {
      int textWidth = autismclient.gui.f.UiHelper.init(context.init_Font()).init(this.init_Str);
      return textWidth + (this.init_Identifier != null ? this.init_I + this.load_I : 0);
   }

   @Override
   public float init_F_SyncManager_F(SyncManager context, float availableWidth) {
      return this.write_Z ? autismclient.gui.f.RenderState.init(14) : Math.max(11, this.init_I + 1);
   }

   @Override
   public void init_V_SyncManager(SyncManager context) {
      if (this.init_Z) {
         int drawX = Math.round(this.init_F);
         int drawY = Math.round(this.load_F);
         int drawH = Math.round(this.write);
         if (this.write_Z) {
            MethodUtil.init(
               autismclient.gui.f.UiHelper.init(
                  context.init_GuiGraphicsExtract(), context.init_Font(), Math.round(context.init_F()), Math.round(context.load())
               ),
               autismclient.gui.f.NetHelper.init(drawX, drawY, Math.round(this.read_F), drawH),
               this.init_Str,
               this.init_Identifier
            );
         } else {
            int contentX = drawX;
            if (this.init_Identifier != null) {
               int iconY = drawY + Math.max(1, (drawH - this.init_I) / 2);
               context.init(ThemeTextureRecolorer.init(this.init_Identifier, ThemePaletteManager.Inner_DataUtil.ACCENT), drawX, iconY, drawX + this.init_I, iconY + this.init_I);
               contentX += this.init_I + this.load_I;
            }

            autismclient.gui.f.UiHelper.init(context.init_Font())
               .init_V_GuiGraphicsExtract_2(
                  context.init_GuiGraphicsExtract(),
                  this.init_Str,
                  contentX,
                  drawY + Math.max(1, (drawH - 9) / 2),
                  context.init_I_I(ThemePaletteManager.init_I_I_ct_DataUtil(-791321, ThemePaletteManager.Inner_DataUtil.TEXT))
               );
         }
      }
   }
}
