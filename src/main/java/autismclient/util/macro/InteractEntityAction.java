package autismclient.util.macro;

import autismclient.util.RegistryDisplayNameCache;
import autismclient.util.AutismSessionState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.EntityHitResult;

public class InteractEntityAction implements PacketOrderAware, AimTargetAction, MacroAction, GuiWaitAware {
   public InteractEntityAction.TargetMode targetMode = InteractEntityAction.TargetMode.ENTITY;
   public List<String> entityTargets = new ArrayList<>();
   public boolean waitForTarget = true;
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = false;
   public String guiName = "";
   public boolean raycast = false;
   public volatile PacketOrderMode packetOrder = PacketOrderMode.INSTANT;
   private boolean init = true;

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
      autismclient.util.InteractionTargetRef target = this.resolveTarget();
      if (target == null) {
         return null;
      } else {
         return target.isBlock() ? AimTargetAction.AimTarget.init(target.blockPos()) : AimTargetAction.AimTarget.init(target.resolveEntity(mc));
      }
   }

   private autismclient.util.InteractionTargetRef resolveTarget() {
      if (this.targetMode == InteractEntityAction.TargetMode.LAST_TARGET) {
         return AutismSessionState.getInstance().getLastInteractionTarget();
      }

      for (String ref : this.entityTargets) {
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
      this.interact(mc);
   }

   public boolean interact(Minecraft mc) {
      if (mc != null && mc.player != null) {
         if (this.targetMode != InteractEntityAction.TargetMode.LAST_TARGET) {
            boolean sentAny = false;

            for (String ref : this.entityTargets) {
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
            autismclient.util.InteractionTargetRef target = AutismSessionState.getInstance().getLastInteractionTarget();
            return target != null && (!this.waitForTarget || target.isPresent(mc)) && target.interact(mc);
         }
      } else {
         return false;
      }
   }

   public boolean load(Minecraft mc) {
      if (mc == null || mc.player == null) {
         return false;
      }

      if (this.targetMode == InteractEntityAction.TargetMode.LAST_TARGET) {
         autismclient.util.InteractionTargetRef target = AutismSessionState.getInstance().getLastInteractionTarget();
         return target != null && target.isPresent(mc);
      }

      boolean hasTarget = false;

      for (String ref : this.entityTargets) {
         if (ref != null && !ref.isBlank()) {
            autismclient.util.InteractionTargetRef target = autismclient.util.InteractionTargetRef.ofEntityRef(ref);
            if (target == null || !target.isPresent(mc)) {
               return false;
            }

            hasTarget = true;
         }
      }

      return hasTarget;
   }

   public void captureCrosshairTarget(Minecraft mc) {
      if (mc != null && mc.hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
         this.targetMode = InteractEntityAction.TargetMode.ENTITY;
         String ref = autismclient.util.InteractionTargetRef.refFor(entityHit.getEntity());
         if (!ref.isBlank() && !this.entityTargets.contains(ref)) {
            this.entityTargets.add(ref);
         }
      }
   }

   private String describeRef(String ref) {
      if (ref == null || ref.isBlank()) {
         return "(none)";
      } else if (ref.startsWith("~")) {
         String[] parts = ref.split("~", 4);
         String name = parts.length >= 4 ? parts[3] : "?";
         String type = parts.length >= 3 ? RegistryDisplayNameCache.read(parts[2]) : "?";
         return name.isBlank() ? type : name + " (" + type + ")";
      } else {
         return RegistryDisplayNameCache.read(ref);
      }
   }

   @Override
   public boolean waitsForGuiBefore() {
      return this.waitForGuiBefore;
   }

   @Override
   public void load(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean waitsForGuiAfter() {
      return this.waitForGuiAfter;
   }

   @Override
   public void read(boolean v) {
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
   public PacketOrderMode getPacketOrder() {
      return this.packetOrder;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.INTERACT_ENTITY;
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   @Override
   public String getIcon() {
      return "IE";
   }

   @Override
   public String getDisplayName() {
      String targetLabel;
      if (this.targetMode == InteractEntityAction.TargetMode.LAST_TARGET) {
         targetLabel = "Last Target";
      } else if (this.entityTargets.isEmpty()) {
         targetLabel = "(none)";
      } else if (this.entityTargets.size() == 1) {
         targetLabel = this.describeRef(this.entityTargets.get(0));
      } else {
         targetLabel = this.entityTargets.size() + " entities";
      }

      return "Interact Entity " + targetLabel + GuiWaitAware.waitSuffix(this);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "INTERACT_ENTITY");
      tag.putString("targetMode", this.targetMode.name());
      ListTag list = new ListTag();

      for (String ref : this.entityTargets) {
         if (ref != null && !ref.isBlank()) {
            list.add(StringTag.valueOf(ref));
         }
      }

      tag.put("entityTargets", list);
      tag.putString("entityTarget", this.entityTargets.isEmpty() ? "" : this.entityTargets.get(0));
      tag.putBoolean("waitForTarget", this.waitForTarget);
      tag.putBoolean("waitForGuiBefore", this.waitForGuiBefore);
      tag.putBoolean("waitForGuiAfter", this.waitForGuiAfter);
      tag.putString("guiName", this.guiName);
      tag.putBoolean("raycast", this.raycast);
      tag.putBoolean("enabled", this.init);
      PacketOrderAware.writePacketOrder(tag, this.packetOrder);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      String mode = tag.getStringOr("targetMode", "ENTITY");
      if ("BLOCK".equals(mode)) {
         mode = "ENTITY";
      }

      try {
         this.targetMode = InteractEntityAction.TargetMode.valueOf(mode);
      } catch (IllegalArgumentException ignored) {
         this.targetMode = InteractEntityAction.TargetMode.ENTITY;
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

      if (this.entityTargets.isEmpty()) {
         String single = tag.getStringOr("entityTarget", "");
         if (single != null && !single.isBlank()) {
            this.entityTargets.add(single);
         }
      }

      this.waitForTarget = tag.getBooleanOr("waitForTarget", true);
      this.waitForGuiBefore = GuiWaitAware.readWaitForGuiBefore(tag, false);
      this.waitForGuiAfter = GuiWaitAware.readWaitForGuiAfter(tag, false);
      this.guiName = tag.getStringOr("guiName", "");
      this.raycast = tag.getBooleanOr("raycast", false);
      this.init = tag.getBooleanOr("enabled", true);
      this.packetOrder = PacketOrderAware.readPacketOrder(tag);
   }

   public enum TargetMode {
      ENTITY,
      LAST_TARGET;
   }
}
