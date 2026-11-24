package autismclient.mixin.security;

import autismclient.g.MethodUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.function.BooleanSupplier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.KeyMapping.Category;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Options.class)
public class AutismProtectorOptionsMixin {
   @WrapOperation(
      method = "<init>",
      at = @At(value = "NEW", target = "(Ljava/lang/String;ILnet/minecraft/client/KeyMapping$Category;)Lnet/minecraft/client/KeyMapping;")
   )
   private KeyMapping autism$keyMapping(String name, int key, Category category, Operation<KeyMapping> original) {
      MethodUtil.load_V(name);
      return original.call(name, key, category);
   }

   @WrapOperation(
      method = "<init>",
      at = @At(
         value = "NEW",
         target = "(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILnet/minecraft/client/KeyMapping$Category;)Lnet/minecraft/client/KeyMapping;"
      )
   )
   private KeyMapping autism$keyMapping(String name, Type type, int key, Category category, Operation<KeyMapping> original) {
      MethodUtil.load_V(name);
      return original.call(name, type, key, category);
   }

   @WrapOperation(
      method = "<init>",
      at = @At(
         value = "NEW",
         target = "(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILnet/minecraft/client/KeyMapping$Category;I)Lnet/minecraft/client/KeyMapping;"
      )
   )
   private KeyMapping autism$keyMapping(String name, Type type, int key, Category category, int order, Operation<KeyMapping> original) {
      MethodUtil.load_V(name);
      return original.call(name, type, key, category, order);
   }

   @WrapOperation(
      method = "<init>",
      at = @At(
         value = "NEW",
         target = "(Ljava/lang/String;ILnet/minecraft/client/KeyMapping$Category;Ljava/util/function/BooleanSupplier;Z)Lnet/minecraft/client/ToggleKeyMapping;"
      )
   )
   private ToggleKeyMapping autism$toggleKeyMapping(
      String name, int key, Category category, BooleanSupplier needsToggle, boolean shouldRestore, Operation<ToggleKeyMapping> original
   ) {
      MethodUtil.load_V(name);
      return original.call(name, key, category, needsToggle, shouldRestore);
   }

   @WrapOperation(
      method = "<init>",
      at = @At(
         value = "NEW",
         target = "(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILnet/minecraft/client/KeyMapping$Category;Ljava/util/function/BooleanSupplier;Z)Lnet/minecraft/client/ToggleKeyMapping;"
      )
   )
   private ToggleKeyMapping autism$toggleKeyMapping(
      String name, Type type, int key, Category category, BooleanSupplier needsToggle, boolean shouldRestore, Operation<ToggleKeyMapping> original
   ) {
      MethodUtil.load_V(name);
      return original.call(name, type, key, category, needsToggle, shouldRestore);
   }
}
