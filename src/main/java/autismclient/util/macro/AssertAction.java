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

public class AssertAction implements MacroAction {
   public AssertAction.CheckKind check = AssertAction.CheckKind.CONNECTION;
   public AssertAction.FailureBehavior failureBehavior = AssertAction.FailureBehavior.STOP_MACRO;
   public String itemName = "";
   public String guiType = "ANY";
   public String entityId = "";
   public String message = "";

   @Override
   public void execute(Minecraft mc) {
      if (!this.init(mc)) {
         String out;
         if (this.message != null && !this.message.isBlank()) {
            MacroTemplateResolver.ResolveResult resolved = MacroVariableContext.resolveTemplate(this.message, mc);
            if (!resolved.success()) {
               return;
            }

            out = resolved.value();
         } else {
            out = "Macro assert failed: " + this.check;
         }

         AutismClientMessaging.sendPrefixed("§c" + out);
         if (this.failureBehavior == AssertAction.FailureBehavior.STOP_MACRO) {
            MacroExecutor.stopCurrentRun();
         }
      }
   }

   public boolean init(Minecraft mc) {
      if (mc == null) {
         return false;
      }

      MacroTemplateResolver.ResolveResult itemResolution = MacroVariableContext.resolveTemplate(this.itemName, mc);
      MacroTemplateResolver.ResolveResult entityResolution = MacroVariableContext.resolveTemplate(this.entityId, mc);
      MacroTemplateResolver.ResolveResult guiResolution = MacroVariableContext.resolveTemplate(this.guiType, mc);
      if (itemResolution.success() && entityResolution.success() && guiResolution.success()) {
         boolean var10000;
         switch (this.check) {
            case HELD_ITEM:
               var10000 = mc.player != null
                  && MacroItemTarget.parse(itemResolution.value()).matchScore(mc.player.getMainHandItem(), mc.player.getInventory().getSelectedSlot())
                     >= 0;
               break;
            case INVENTORY_ITEM:
               var10000 = WaitInventoryPredicateAction.hasItem(mc, MacroItemTarget.parse(itemResolution.value()));
               break;
            case GUI_TYPE:
               var10000 = GuiScreenMatcher.init(mc.gui.screen(), guiResolution.value(), "");
               break;
            case LOOKING_AT_ENTITY:
               var10000 = mc.crosshairPickEntity != null && this.init(mc.crosshairPickEntity, entityResolution.value());
               break;
            case LOOKING_AT_CONTAINER_ENTITY:
               var10000 = mc.crosshairPickEntity != null && this.init(mc.crosshairPickEntity, entityResolution.value()) && this.init(mc.crosshairPickEntity);
               break;
            case MOUNTED_ENTITY:
               var10000 = mc.player != null && mc.player.getVehicle() != null && this.init(mc.player.getVehicle(), entityResolution.value());
               break;
            case HAS_BUNDLE:
               var10000 = WaitInventoryPredicateAction.hasItem(mc, MacroItemTarget.parse("minecraft:bundle"));
               break;
            case BUNDLE_V2_READY:
               var10000 = this.load(mc);
               break;
            case HAS_WRITABLE_BOOK:
               var10000 = WaitInventoryPredicateAction.hasItem(mc, MacroItemTarget.parse("minecraft:writable_book"));
               break;
            case CONNECTION:
               var10000 = mc.getConnection() != null;
               break;
            case LOOKING_AT_BLOCK:
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
   public MacroActionType getType() {
      return MacroActionType.ASSERT;
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
      this.check = MacroNbtCodecHelper.parseEnum(AssertAction.CheckKind.class, tag.getStringOr("check", "CONNECTION"), AssertAction.CheckKind.CONNECTION);
      this.failureBehavior = MacroNbtCodecHelper.parseEnum(AssertAction.FailureBehavior.class, tag.getStringOr("failureBehavior", "STOP_MACRO"), AssertAction.FailureBehavior.STOP_MACRO);
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

   public enum CheckKind {
      HELD_ITEM,
      INVENTORY_ITEM,
      GUI_TYPE,
      LOOKING_AT_ENTITY,
      LOOKING_AT_CONTAINER_ENTITY,
      MOUNTED_ENTITY,
      HAS_BUNDLE,
      BUNDLE_V2_READY,
      HAS_WRITABLE_BOOK,
      CONNECTION,
      LOOKING_AT_BLOCK;
   }

   public enum FailureBehavior {
      STOP_MACRO,
      WARN_ONLY;
   }
}
