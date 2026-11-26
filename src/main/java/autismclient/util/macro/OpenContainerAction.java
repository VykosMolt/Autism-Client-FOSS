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
   private boolean init = true;

   @Override
   public boolean init_Z_3() {
      return this.raycast;
   }

   @Override
   public void init_V_Z(boolean value) {
      this.raycast = value;
   }

   @Override
   public AimTargetAction.Inner_DataUtil init_M_DataUtil_Minecraft(Minecraft mc) {
      if (this.targetMode == OpenContainerAction.Inner_DataUtil.BLOCK) {
         return AimTargetAction.Inner_DataUtil.init(this.blockPos);
      } else {
         autismclient.util.InteractionTargetRef target = this.targetMode == OpenContainerAction.Inner_DataUtil.LAST_TARGET ? AutismSessionState.init_ck().init_E() : this.init_E_2();
         if (target == null) {
            return null;
         } else {
            return target.init_Z() ? AimTargetAction.Inner_DataUtil.init(target.init_BlockPos()) : AimTargetAction.Inner_DataUtil.init(target.init_Entity_Minecraft(mc));
         }
      }
   }

   private autismclient.util.InteractionTargetRef init_E_2() {
      for (String ref : this.init_List_13()) {
         if (ref != null && !ref.isBlank()) {
            autismclient.util.InteractionTargetRef target = autismclient.util.InteractionTargetRef.init_E_Str(ref);
            if (target != null) {
               return target;
            }
         }
      }

      return null;
   }

   @Override
   public void execute(Minecraft mc) {
      this.init_Z_Minecraft_4(mc);
   }

   public boolean init_Z_Minecraft_4(Minecraft mc) {
      if (mc != null && mc.player != null) {
         if (this.targetMode == OpenContainerAction.Inner_DataUtil.ENTITY) {
            List<String> refs = this.init_List_13();
            boolean sentAny = false;

            for (String ref : refs) {
               if (ref != null && !ref.isBlank()) {
                  autismclient.util.InteractionTargetRef target = autismclient.util.InteractionTargetRef.init_E_Str(ref);
                  if (target != null) {
                     if (this.waitForTarget && !target.load_Z(mc)) {
                        return false;
                     }

                     sentAny |= target.init_Z_Minecraft(mc);
                  }
               }
            }

            return sentAny;
         } else {
            autismclient.util.InteractionTargetRef target = this.targetMode == OpenContainerAction.Inner_DataUtil.BLOCK ? autismclient.util.InteractionTargetRef.init_E_BlockPos(this.blockPos) : AutismSessionState.init_ck().init_E();
            return target != null && (!this.waitForTarget || target.load_Z(mc)) && target.init_Z_Minecraft(mc);
         }
      } else {
         return false;
      }
   }

   public boolean load(Minecraft mc) {
      if (mc == null || mc.player == null) {
         return false;
      }

      if (this.targetMode == OpenContainerAction.Inner_DataUtil.ENTITY) {
         List<String> refs = this.init_List_13();
         boolean hasTarget = false;

         for (String ref : refs) {
            if (ref != null && !ref.isBlank()) {
               autismclient.util.InteractionTargetRef target = autismclient.util.InteractionTargetRef.init_E_Str(ref);
               if (target == null || !target.load_Z(mc)) {
                  return false;
               }

               hasTarget = true;
            }
         }

         return hasTarget;
      } else {
         autismclient.util.InteractionTargetRef target = this.targetMode == OpenContainerAction.Inner_DataUtil.BLOCK ? autismclient.util.InteractionTargetRef.init_E_BlockPos(this.blockPos) : AutismSessionState.init_ck().init_E();
         return target != null && target.load_Z(mc);
      }
   }

   public void init_V_Minecraft_9(Minecraft mc) {
      if (mc != null) {
         if (mc.hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            this.targetMode = OpenContainerAction.Inner_DataUtil.ENTITY;
            this.entityTarget = autismclient.util.InteractionTargetRef.init_Str_Entity(entityHit.getEntity());
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

   public String load() {
      return init_Str_Str_10(this.entityTarget);
   }

   private static String init_Str_Str_10(String targetRef) {
      if (targetRef == null || targetRef.isBlank()) {
         return "(none)";
      } else if (targetRef.startsWith("~")) {
         String[] parts = targetRef.split("~", 4);
         String name = parts.length >= 4 ? parts[3] : "?";
         String type = parts.length >= 3 ? RegistryDisplayNameCache.read(parts[2]) : "?";
         return name.isBlank() ? type : name + " (" + type + ")";
      } else {
         return RegistryDisplayNameCache.read(targetRef);
      }
   }

   @Override
   public boolean read_Z() {
      return this.waitForGuiBefore;
   }

   @Override
   public void load(boolean v) {
      this.waitForGuiBefore = v;
   }

   @Override
   public boolean write_Z() {
      return this.waitForGuiAfter;
   }

   @Override
   public void read(boolean v) {
      this.waitForGuiAfter = v;
   }

   @Override
   public String init_Str_7() {
      return this.guiName;
   }

   @Override
   public void init_V_Str(String name) {
      this.guiName = name;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.OPEN_CONTAINER;
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
            List<String> refs = this.init_List_13();
            var10000 = refs.isEmpty() ? "(none)" : (refs.size() == 1 ? init_Str_Str_10(refs.get(0)) : refs.size() + " entities");
            break;
         case LAST_TARGET:
            var10000 = "Last Target";
            break;
         default:
            throw new MatchException(null, null);
      }

      String targetLabel = var10000;
      String base = "Open Container " + targetLabel;
      return base + GuiWaitAware.init_Str_aP(this);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "OPEN_CONTAINER");
      tag.putString("targetMode", this.targetMode.name());
      tag.putInt("x", this.blockPos.getX());
      tag.putInt("y", this.blockPos.getY());
      tag.putInt("z", this.blockPos.getZ());
      List<String> refs = this.init_List_13();
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
      tag.putBoolean("enabled", this.init);
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
      this.waitForGuiBefore = GuiWaitAware.init_Z_CompoundTag_Z(tag, false);
      this.waitForGuiAfter = GuiWaitAware.load_Z_CompoundTag_Z(tag, true);
      this.guiName = tag.getStringOr("guiName", "");
      this.raycast = tag.getBooleanOr("raycast", false);
      this.init = tag.getBooleanOr("enabled", true);
   }

   private List<String> init_List_13() {
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
