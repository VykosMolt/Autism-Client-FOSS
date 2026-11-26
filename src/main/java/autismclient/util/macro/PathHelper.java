package autismclient.util.macro;

import autismclient.util.cc;
import autismclient.util.ck;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.EntityHitResult;

public class PathHelper implements E, M, MacroAction, aP {
   public PathHelper.Inner_DataUtil targetMode = PathHelper.Inner_DataUtil.ENTITY;
   public List<String> entityTargets = new ArrayList<>();
   public boolean waitForTarget = true;
   public boolean waitForGuiBefore = false;
   public boolean waitForGuiAfter = false;
   public String guiName = "";
   public boolean raycast = false;
   public volatile D packetOrder = D.INSTANT;
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
   public M.Inner_DataUtil init_M_DataUtil_Minecraft(Minecraft mc) {
      autismclient.util.E target = this.init_E();
      if (target == null) {
         return null;
      } else {
         return target.init_Z() ? M.Inner_DataUtil.init(target.init_BlockPos()) : M.Inner_DataUtil.init(target.init_Entity_Minecraft(mc));
      }
   }

   private autismclient.util.E init_E() {
      if (this.targetMode == PathHelper.Inner_DataUtil.LAST_TARGET) {
         return ck.init_ck().init_E();
      }

      for (String ref : this.entityTargets) {
         if (ref != null && !ref.isBlank()) {
            autismclient.util.E target = autismclient.util.E.init_E_Str(ref);
            if (target != null) {
               return target;
            }
         }
      }

      return null;
   }

   @Override
   public void execute(Minecraft mc) {
      this.init_Z_Minecraft(mc);
   }

   public boolean init_Z_Minecraft(Minecraft mc) {
      if (mc != null && mc.player != null) {
         if (this.targetMode != PathHelper.Inner_DataUtil.LAST_TARGET) {
            boolean sentAny = false;

            for (String ref : this.entityTargets) {
               if (ref != null && !ref.isBlank()) {
                  autismclient.util.E target = autismclient.util.E.init_E_Str(ref);
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
            autismclient.util.E target = ck.init_ck().init_E();
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

      if (this.targetMode == PathHelper.Inner_DataUtil.LAST_TARGET) {
         autismclient.util.E target = ck.init_ck().init_E();
         return target != null && target.load_Z(mc);
      }

      boolean hasTarget = false;

      for (String ref : this.entityTargets) {
         if (ref != null && !ref.isBlank()) {
            autismclient.util.E target = autismclient.util.E.init_E_Str(ref);
            if (target == null || !target.load_Z(mc)) {
               return false;
            }

            hasTarget = true;
         }
      }

      return hasTarget;
   }

   public void init_V_Minecraft_3(Minecraft mc) {
      if (mc != null && mc.hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
         this.targetMode = PathHelper.Inner_DataUtil.ENTITY;
         String ref = autismclient.util.E.init_Str_Entity(entityHit.getEntity());
         if (!ref.isBlank() && !this.entityTargets.contains(ref)) {
            this.entityTargets.add(ref);
         }
      }
   }

   private String init_Str_Str_4(String ref) {
      if (ref == null || ref.isBlank()) {
         return "(none)";
      } else if (ref.startsWith("~")) {
         String[] parts = ref.split("~", 4);
         String name = parts.length >= 4 ? parts[3] : "?";
         String type = parts.length >= 3 ? cc.read(parts[2]) : "?";
         return name.isBlank() ? type : name + " (" + type + ")";
      } else {
         return cc.read(ref);
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
   public D init_D() {
      return this.packetOrder;
   }

   @Override
   public StreamGate getType() {
      return StreamGate.INTERACT_ENTITY;
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
      if (this.targetMode == PathHelper.Inner_DataUtil.LAST_TARGET) {
         targetLabel = "Last Target";
      } else if (this.entityTargets.isEmpty()) {
         targetLabel = "(none)";
      } else if (this.entityTargets.size() == 1) {
         targetLabel = this.init_Str_Str_4(this.entityTargets.get(0));
      } else {
         targetLabel = this.entityTargets.size() + " entities";
      }

      return "Interact Entity " + targetLabel + aP.init_Str_aP(this);
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
      E.init_V_CompoundTag_D(tag, this.packetOrder);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      String mode = tag.getStringOr("targetMode", "ENTITY");
      if ("BLOCK".equals(mode)) {
         mode = "ENTITY";
      }

      try {
         this.targetMode = PathHelper.Inner_DataUtil.valueOf(mode);
      } catch (IllegalArgumentException ignored) {
         this.targetMode = PathHelper.Inner_DataUtil.ENTITY;
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
      this.waitForGuiBefore = aP.init_Z_CompoundTag_Z(tag, false);
      this.waitForGuiAfter = aP.load_Z_CompoundTag_Z(tag, false);
      this.guiName = tag.getStringOr("guiName", "");
      this.raycast = tag.getBooleanOr("raycast", false);
      this.init = tag.getBooleanOr("enabled", true);
      this.packetOrder = E.init_D_CompoundTag(tag);
   }

   public enum Inner_DataUtil {
      ENTITY,
      LAST_TARGET;
   }
}
