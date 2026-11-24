package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.phys.HitResult.Type;

public class DataUtil implements MacroAction {
   public DataUtil.Inner_DataUtil check = DataUtil.Inner_DataUtil.poll;
   public DataUtil.Inner_StreamUtil failureBehavior = DataUtil.Inner_StreamUtil.init;
   public String itemName = "";
   public String guiType = "ANY";
   public String entityId = "";
   public String message = "";

   @Override
   public void execute(Minecraft mc) {
      if (!this.init(mc)) {
         String out;
         if (this.message != null && !this.message.isBlank()) {
            j.Inner_CachePool resolved = n.init_j_CachePool_Str_Minecraft(this.message, mc);
            if (!resolved.init_Z()) {
               return;
            }

            out = resolved.init_Str();
         } else {
            out = "Macro assert failed: " + this.check;
         }

         AutismClientMessaging.sendPrefixed("§c" + out);
         if (this.failureBehavior == DataUtil.Inner_StreamUtil.init) {
            MacroExecutor.init_V();
         }
      }
   }

   public boolean init(Minecraft mc) {
      if (mc == null) {
         return false;
      }

      j.Inner_CachePool itemResolution = n.init_j_CachePool_Str_Minecraft(this.itemName, mc);
      j.Inner_CachePool entityResolution = n.init_j_CachePool_Str_Minecraft(this.entityId, mc);
      j.Inner_CachePool guiResolution = n.init_j_CachePool_Str_Minecraft(this.guiType, mc);
      if (itemResolution.init_Z() && entityResolution.init_Z() && guiResolution.init_Z()) {
         boolean var10000;
         switch (this.check) {
            case init:
               var10000 = mc.player != null
                  && ArrayUtil.update(itemResolution.init_Str()).init_I_ItemStack_I(mc.player.getMainHandItem(), mc.player.getInventory().getSelectedSlot())
                     >= 0;
               break;
            case load:
               var10000 = aK.init_Z_Minecraft_ArrayUtil(mc, ArrayUtil.update(itemResolution.init_Str()));
               break;
            case read:
               var10000 = f.init(mc.gui.screen(), guiResolution.init_Str(), "");
               break;
            case write:
               var10000 = mc.crosshairPickEntity != null && this.init(mc.crosshairPickEntity, entityResolution.init_Str());
               break;
            case update:
               var10000 = mc.crosshairPickEntity != null && this.init(mc.crosshairPickEntity, entityResolution.init_Str()) && this.init(mc.crosshairPickEntity);
               break;
            case sync:
               var10000 = mc.player != null && mc.player.getVehicle() != null && this.init(mc.player.getVehicle(), entityResolution.init_Str());
               break;
            case flush:
               var10000 = aK.init_Z_Minecraft_ArrayUtil(mc, ArrayUtil.update("minecraft:bundle"));
               break;
            case close:
               var10000 = this.load(mc);
               break;
            case open:
               var10000 = aK.init_Z_Minecraft_ArrayUtil(mc, ArrayUtil.update("minecraft:writable_book"));
               break;
            case poll:
               var10000 = mc.getConnection() != null;
               break;
            case push:
               var10000 = mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return false;
      }
   }

   private boolean load(Minecraft mc) {
      if (mc != null && mc.player != null && mc.player.containerMenu instanceof InventoryMenu) {
         ItemStack stack = mc.player.getInventory().getItem(0);
         if (stack != null && !stack.isEmpty() && stack.is(Items.BUNDLE)) {
            BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
            return contents != null && contents.size() == 1;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean init(Entity entity, String expected) {
      if (entity == null) {
         return false;
      } else if (expected != null && !expected.isBlank()) {
         String type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
         return type.equalsIgnoreCase(expected) || entity.getStringUUID().equalsIgnoreCase(expected) || entity.getName().getString().equalsIgnoreCase(expected);
      } else {
         return true;
      }
   }

   private boolean init(Entity entity) {
      if (entity == null) {
         return false;
      }

      String type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
      return type.contains("chest_boat")
         || type.contains("chest_minecart")
         || type.contains("hopper_minecart")
         || type.contains("furnace_minecart")
         || type.contains("llama")
         || type.contains("donkey")
         || type.contains("mule");
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_y_StreamGate;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "ASSERT");
      tag.putString("check", this.check.name());
      tag.putString("failureBehavior", this.failureBehavior.name());
      tag.putString("itemName", this.itemName);
      tag.putString("guiType", this.guiType);
      tag.putString("entityId", this.entityId);
      tag.putString("message", this.message);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.check = i.init(DataUtil.Inner_DataUtil.class, tag.getStringOr("check", "CONNECTION"), DataUtil.Inner_DataUtil.poll);
      this.failureBehavior = i.init(DataUtil.Inner_StreamUtil.class, tag.getStringOr("failureBehavior", "STOP_MACRO"), DataUtil.Inner_StreamUtil.init);
      this.itemName = tag.getStringOr("itemName", "");
      this.guiType = tag.getStringOr("guiType", "ANY");
      this.entityId = tag.getStringOr("entityId", "");
      this.message = tag.getStringOr("message", "");
   }

   @Override
   public String getDisplayName() {
      return "Assert " + this.check;
   }

   @Override
   public String getIcon() {
      return "!";
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open,
      poll,
      push;
   }

   public enum Inner_StreamUtil {
      init,
      load;
   }
}
