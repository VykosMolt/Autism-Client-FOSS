package autismclient.mixin.security;

import autismclient.protector.ConnectionPrivacyRegistry;
import autismclient.protector.ModNamespaceCatalog;
import autismclient.protector.PacketOriginDuck;
import autismclient.protector.PacketComponentOriginTracker;
import autismclient.protector.PacketDecodeOriginStack;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeybindContents.class)
public abstract class AutismProtectorKeybindContentsMixin implements PacketOriginDuck {
   @Shadow
   @Final
   private String name;
   @Unique
   private boolean autism$fromPacket;
   @Unique
   private ConnectionPrivacyRegistry.ConnectionSession autism$packetOrigin;
   @Unique
   private Component autism$displaySource;
   @Unique
   private Component autism$displayCopy;

   @Override
   public void autism$setFromPacket() {
      this.autism$setFromPacket(PacketDecodeOriginStack.init_BufferHelper_Strea());
   }

   @Override
   public void autism$setFromPacket(ConnectionPrivacyRegistry.ConnectionSession context) {
      if (!this.autism$fromPacket) {
         this.autism$packetOrigin = context;
      }

      this.autism$fromPacket = true;
   }

   @Override
   public ConnectionPrivacyRegistry.ConnectionSession autism$packetOrigin() {
      return this.autism$packetOrigin;
   }

   @Override
   public boolean autism$isFromPacket() {
      return this.autism$fromPacket;
   }

   @WrapOperation(method = "getNestedComponent", at = @At(value = "INVOKE", target = "Ljava/util/function/Supplier;get()Ljava/lang/Object;"))
   private Object autism$tagResolvedKeybind(Supplier<?> supplier, Operation<Object> original) {
      if (this.autism$fromPacket
         && (this.autism$packetOrigin == null || this.autism$packetOrigin.init_BufferHelper_Cache() == ConnectionPrivacyRegistry.PrivacyMode.VANILLA)
         && !ModNamespaceCatalog.init_Z(this.name)) {
         Component replacement = Component.translatable(this.name);
         PacketComponentOriginTracker.init(replacement, this.autism$packetOrigin);
         return replacement;
      }

      Object resolved = original.call(supplier);
      if (this.autism$fromPacket && resolved instanceof Component component) {
         if (component.getContents() instanceof TranslatableContents translation && translation.getArgs().length == 0 && component.getSiblings().isEmpty()) {
            if (this.autism$displaySource != component) {
               Component copy = Component.translatableWithFallback(translation.getKey(), translation.getFallback()).withStyle(component.getStyle());
               PacketComponentOriginTracker.init(copy, this.autism$packetOrigin);
               this.autism$displaySource = component;
               this.autism$displayCopy = copy;
            }

            return this.autism$displayCopy;
         }

         PacketComponentOriginTracker.init(component, this.autism$packetOrigin);
      }

      return resolved;
   }
}
