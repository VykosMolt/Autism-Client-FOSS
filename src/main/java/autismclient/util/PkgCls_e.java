package autismclient.util;

import autismclient.mixin.AutismRenderTypeStateAccessor;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

public final class PkgCls_e {
   public static final int init = 15728880;

   private PkgCls_e() {
   }

   public static RenderType init_RenderType(RenderType original) {
      Identifier texture = init_Identifier(original);
      return texture == null ? null : net.minecraft.client.renderer.rendertype.DataUtil.init(texture, PkgCls_i.init_RenderPipeline());
   }

   public static RenderType load(RenderType original) {
      Identifier texture = init_Identifier(original);
      return texture == null ? null : net.minecraft.client.renderer.rendertype.DataUtil.load(texture, PkgCls_i.load());
   }

   private static Identifier init_Identifier(RenderType original) {
      try {
         RenderSetup setup = ((AutismRenderTypeStateAccessor)original).autism$getState();
         return setup == null ? null : net.minecraft.client.renderer.rendertype.DataUtil.init(setup);
      } catch (Throwable t) {
         return null;
      }
   }
}
