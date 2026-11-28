package autismclient.util.macro;

import autismclient.util.RegistryDisplayNameCache;
import autismclient.util.AutismSessionState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class OpenContainerAction implements AimTargetAction, MacroAction, GuiWaitAware {
   public OpenContainerAction.Inner_DataUtil targetMode = OpenContainerAction.Inner_DataUtil.BLOCK;
   public BlockPos blockPos = BlockPos.ZERO;
   public String entityTarget = "";
   public List<String> entityTargets = new ArrayList<>();
   public boolean waitForTarget = true;
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = true;
   public String guiName = "";
   public boolean raycast = false;
   private boolean enabled = true;

   @Override
   public boolean isRaycast() {
      return this.raycast;
   }

   @Override
   public void setRaycast(boolean value) {
      this.raycast = value;
   }

   @Override
   public AimTargetAction.AimTarget getAimTarget(Minecraft mc) {
      if (this.targetMode == OpenContainerAction.Inner_DataUtil.BLOCK) {
         return AimTargetAction.AimTarget.ofBlock(this.blockPos);
      } else {
         autismclient.util.InteractionTargetRef target = this.targetMode == OpenContainerAction.Inner_DataUtil.LAST_TARGET ? AutismSessionState.getInstance().getLastInteractionTarget() : this.resolveTarget();
         if (target == null) {
            return null;
         } else {
            return target.isBlock() ? AimTargetAction.AimTarget.ofBlock(target.blockPos()) : AimTargetAction.AimTarget.ofEntity(target.resolveEntity(mc));
         }
      }
   }

   private autismclient.util.InteractionTargetRef resolveTarget() {
      for (String ref : this.targetRefs()) {
         if (ref != null && !ref.isBlank()) {
            autismclient.util.InteractionTargetRef target = autismclient.util.InteractionTargetRef.ofEntityRef(ref);
            if (target != null) {
               return target;
            }
         }
      }

      return null;
   }

   @Override
   public void execute(Minecraft mc) {
      this.openTarget(mc);
   }

   public boolean openTarget(Minecraft mc) {
      if (mc != null && mc.player != null) {
         if (this.targetMode == OpenContainerAction.Inner_DataUtil.ENTITY) {
            List<String> refs = this.targetRefs();
            boolean sentAny = false;

            for (String ref : refs) {
               if (ref != null && !ref.isBlank()) {
                  autismclient.util.InteractionTargetRef target = autismclient.util.InteractionTargetRef.ofEntityRef(ref);
                  if (target != null) {
                     if (this.waitForTarget && !target.isPresent(mc)) {
                        return false;
                     }

                     sentAny |= target.interact(mc);
                  }
               }
            }

            return sentAny;
         } else {
            autismclient.util.InteractionTargetRef target = this.targetMode == OpenContainerAction.Inner_DataUtil.BLOCK ? autismclient.util.InteractionTargetRef.ofBlock(this.blockPos) : AutismSessionState.getInstance().getLastInteractionTarget();
            return target != null && (!this.waitForTarget || target.isPresent(mc)) && target.interact(mc);
         }
      } else {
         return false;
      }
   }

   public boolean open(Minecraft mc) {
      if (mc == null || mc.player == null) {
         return false;
      }

      if (this.targetMode == OpenContainerAction.Inner_DataUtil.ENTITY) {
         List<String> refs = this.targetRefs();
         boolean hasTarget = false;

         for (String ref : refs) {
            if (ref != null && !ref.isBlank()) {
               autismclient.util.InteractionTargetRef target = autismclient.util.InteractionTargetRef.ofEntityRef(ref);
               if (target == null || !target.isPresent(mc)) {
                  return false;
               }

               hasTarget = true;
            }
         }

         return hasTarget;
      } else {
         autismclient.util.InteractionTargetRef target = this.targetMode == OpenContainerAction.Inner_DataUtil.BLOCK ? autismclient.util.InteractionTargetRef.ofBlock(this.blockPos) : AutismSessionState.getInstance().getLastInteractionTarget();
         return target != null && target.isPresent(mc);
      }
   }

   public void captureCrosshairTarget(Minecraft mc) {
      if (mc != null) {
         if (mc.hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            this.targetMode = OpenContainerAction.Inner_DataUtil.ENTITY;
            this.entityTarget = autismclient.util.InteractionTargetRef.refFor(entityHit.getEntity());
            if (!this.entityTarget.isBlank() && !this.entityTargets.contains(this.entityTarget)) {
               this.entityTargets.add(this.entityTarget);
            }
         } else if (mc.hitResult instanceof BlockHitResult blockHit) {
            this.targetMode = OpenContainerAction.Inner_DataUtil.BLOCK;
            this.blockPos = blockHit.getBlockPos();
            this.entityTarget = "";
            this.entityTargets.clear();
         }
      }
   }

   public String describe() {
      return describeTarget(this.entityTarget);
   }

   private static String describeTarget(String targetRef) {
      if (targetRef == null || targetRef.isBlank()) {
         return "(none)";
      } else if (targetRef.startsWith("~")) {
         String[] parts = targetRef.split("~", 4);
         String name = parts.length >= 4 ? parts[3] : "?";
         String type = parts.length >= 3 ? RegistryDisplayNameCache.entityName(parts[2]) : "?";
         return name.isBlank() ? type : name + " (" + type + ")";
      } else {
         return RegistryDisplayNameCache.entityName(targetRef);
      }
   }

   @Override
   public boolean waitsForGuiBefore() {
      return this.waitForGuiBefore;
   }

   @Override
   public void setWaitForGuiBefore(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean waitsForGuiAfter() {
      return this.waitForGuiAfter;
   }

   @Override
   public void setWaitForGuiAfter(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String getGuiName() {
      return this.guiName;
   }

   @Override
   public void setGuiName(String name) {
      this.guiName = name;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.OPEN_CONTAINER;
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   @Override
   public String getIcon() {
      return "OC";
   }

   @Override
   public String getDisplayName() {
      String var10000;
      switch (this.targetMode) {
         case BLOCK:
            var10000 = this.blockPos.getX() + "," + this.blockPos.getY() + "," + this.blockPos.getZ();
            break;
         case ENTITY:
            List<String> refs = this.targetRefs();
            var10000 = refs.isEmpty() ? "(none)" : (refs.size() == 1 ? describeTarget(refs.get(0)) : refs.size() + " entities");
            break;
         case LAST_TARGET:
            var10000 = "Last Target";
            break;
         default:
            throw new MatchException(null, null);
      }

      String targetLabel = var10000;
      String base = "Open Container " + targetLabel;
      return base + GuiWaitAware.waitSuffix(this);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "OPEN_CONTAINER");
      tag.putString("targetMode", this.targetMode.name());
      tag.putInt("x", this.blockPos.getX());
      tag.putInt("y", this.blockPos.getY());
      tag.putInt("z", this.blockPos.getZ());
      List<String> refs = this.targetRefs();
      tag.putString("entityTarget", refs.isEmpty() ? "" : refs.get(0));
      ListTag entityTargets = new ListTag();

      for (String ref : refs) {
         if (ref != null && !ref.isBlank()) {
            entityTargets.add(StringTag.valueOf(ref));
         }
      }

      tag.put("entityTargets", entityTargets);
      tag.putBoolean("waitForTarget", this.waitForTarget);
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putString("guiName", this.guiName);
      tag.putBoolean("raycast", this.raycast);
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      int x = tag.getIntOr("x", 0);
      int y = tag.getIntOr("y", 0);
      int z = tag.getIntOr("z", 0);
      this.blockPos = new BlockPos(x, y, z);

      try {
         this.targetMode = OpenContainerAction.Inner_DataUtil.valueOf(tag.getStringOr("targetMode", "BLOCK"));
      } catch (IllegalArgumentException ignored) {
         this.targetMode = OpenContainerAction.Inner_DataUtil.BLOCK;
      }

      this.entityTargets.clear();
      if (tag.contains("entityTargets")) {
         for (Tag element : tag.getList("entityTargets").orElse(new ListTag())) {
            String value = element.asString().orElse("");
            if (value != null && !value.isBlank() && !this.entityTargets.contains(value)) {
               this.entityTargets.add(value);
            }
         }
      }

      this.entityTarget = tag.getStringOr("entityTarget", "");
      if (this.entityTargets.isEmpty() && this.entityTarget != null && !this.entityTarget.isBlank()) {
         this.entityTargets.add(this.entityTarget);
      } else if (!this.entityTargets.isEmpty()) {
         this.entityTarget = this.entityTargets.get(0);
      }

      this.waitForTarget = tag.getBooleanOr("waitForTarget", true);
      this.waitForGuiBefore = GuiWaitAware.readWaitForGuiBefore(tag, false);
      this.waitForGuiAfter = GuiWaitAware.readWaitForGuiAfter(tag, true);
      this.guiName = tag.getStringOr("guiName", "");
      this.raycast = tag.getBooleanOr("raycast", false);
      this.enabled = tag.getBooleanOr("enabled", true);
   }

   private List<String> targetRefs() {
      if (!this.entityTargets.isEmpty()) {
         return this.entityTargets;
      } else {
         return this.entityTarget != null && !this.entityTarget.isBlank() ? List.of(this.entityTarget) : List.of();
      }
   }

   public enum Inner_DataUtil {
      BLOCK,
      ENTITY,
      LAST_TARGET;
   }
}
