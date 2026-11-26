package autismclient.protector;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class TooltipNbtGuard {
   static final int init = 512;

   private TooltipNbtGuard() {
   }

   public static boolean init_Z(List<Component> lines) {
      if (lines != null && lines.size() > 512) {
         int hidden = lines.size() - 511;
         lines.subList(511, lines.size()).clear();
         lines.add(Component.literal("... (huge NBT item: " + hidden + " tooltip lines hidden)").withStyle(ChatFormatting.DARK_GRAY));
         return true;
      } else {
         return false;
      }
   }

   public static int init_I(List<Component> lines) {
      if (lines != null && !lines.isEmpty()) {
         init_Z(lines);
         int replaced = 0;

         for (int i = 0; i < lines.size(); i++) {
            if (!ComponentSafetyValidator.init(lines.get(i))) {
               lines.set(i, Component.literal("[unsafe tooltip line removed]").withStyle(ChatFormatting.DARK_GRAY));
               replaced++;
            }
         }

         return replaced;
      } else {
         return 0;
      }
   }

   public static int init(ItemStack stack, RegistryAccess access) {
      if (stack != null && !stack.isEmpty() && access != null) {
         try {
            CompoundTag nbt = (CompoundTag)ItemStack.CODEC.encodeStart(access.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.write(nbt, new DataOutputStream(baos));
            return baos.size();
         } catch (Throwable t) {
            return -1;
         }
      } else {
         return -1;
      }
   }
}
