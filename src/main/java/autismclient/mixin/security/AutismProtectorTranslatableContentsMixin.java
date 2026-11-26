package autismclient.mixin.security;

import autismclient.g.BufferHelper;
import autismclient.g.ClassCache;
import autismclient.g.HashUtil;
import autismclient.g.NetHelper;
import autismclient.g.StreamUtil;
import autismclient.g.TokenStore;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TranslatableContents.class)
public abstract class AutismProtectorTranslatableContentsMixin implements NetHelper {
   @Shadow
   private Language decomposedWith;
   @Unique
   private boolean autism$fromPacket;
   @Unique
   private BufferHelper.Inner_StreamUtil autism$packetOrigin;
   @Unique
   private boolean autism$silent;
   @Unique
   private static final String AUTISM_ALLOW = "\u0000__autism_allow__";

   @Override
   public void autism$setFromPacket() {
      this.autism$setFromPacket(TokenStore.init_BufferHelper_Strea());
   }

   @Override
   public void autism$setFromPacket(BufferHelper.Inner_StreamUtil context) {
      if (!this.autism$fromPacket) {
         this.autism$packetOrigin = context;
      }

      this.autism$fromPacket = true;
      this.decomposedWith = null;
   }

   @Override
   public BufferHelper.Inner_StreamUtil autism$packetOrigin() {
      return this.autism$packetOrigin;
   }

   @Override
   public boolean autism$isFromPacket() {
      return this.autism$fromPacket;
   }

   @Override
   public void autism$setSilent() {
      this.autism$silent = true;
   }

   @WrapOperation(
      method = "decompose",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/locale/Language;getOrDefault(Ljava/lang/String;)Ljava/lang/String;")
   )
   private String autism$wrapGetOrDefault(Language instance, String id, Operation<String> original) {
      String result = this.autism$handle(instance, id, id);
      return result == "\u0000__autism_allow__" ? original.call(instance, id) : result;
   }

   @WrapOperation(
      method = "decompose",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/locale/Language;getOrDefault(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")
   )
   private String autism$wrapGetOrDefaultFallback(Language instance, String idArg, String defaultValue, Operation<String> original) {
      String result = this.autism$handle(instance, idArg, defaultValue);
      return result == "\u0000__autism_allow__" ? original.call(instance, idArg, defaultValue) : result;
   }

   @Unique
   private String autism$handle(Language language, String translationKey, String defaultValue) {
      if (!this.autism$fromPacket) {
         return "\u0000__autism_allow__";
      } else {
         BufferHelper.Inner_StreamUtil origin = this.autism$packetOrigin;
         if (origin != null && origin.init_BufferHelper_Cache() == BufferHelper.Inner_CachePool.MODDED && !HashUtil.close() && !StreamUtil.init_Z()) {
            return "\u0000__autism_allow__";
         } else {
            return origin != null && !origin.init_Z() && BufferHelper.init_Z(origin) && origin.init_Connection().isMemoryConnection()
               ? "\u0000__autism_allow__"
               : ClassCache.init(language, translationKey, defaultValue, origin);
         }
      }
   }
}
