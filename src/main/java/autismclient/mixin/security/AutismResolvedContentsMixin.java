package autismclient.mixin.security;

import autismclient.protector.PacketComponentOriginTracker;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ResolutionContext;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({TranslatableContents.class, NbtContents.class, SelectorContents.class, ScoreContents.class, ObjectContents.class})
public class AutismResolvedContentsMixin {
   @Inject(method = "resolve", at = @At("RETURN"))
   private void autism$propagateResolution(ResolutionContext context, int recursionDepth, CallbackInfoReturnable<MutableComponent> cir) {
      PacketComponentOriginTracker.propagateFromContents((ComponentContents)(Object)this, cir.getReturnValue());
   }
}
