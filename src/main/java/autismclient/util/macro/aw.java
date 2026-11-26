package autismclient.util.macro;

import autismclient.util.cc;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class aw implements CacheEntry, MacroAction {
   public List<String> entityIds = new ArrayList<>();
   public boolean mustBeLookingAt = false;
   public aw.Inner_DataUtil checkMode = aw.Inner_DataUtil.RADIUS;
   public boolean centerOnPlayer = true;
   public double radius = 6.0;
   public boolean containerEntitiesOnly = false;
   public int timeoutMs = 0;
   public double f_x_D = 0.0;
   public double f_y_D = 0.0;
   public double f_z_D = 0.0;
   public boolean listenDuringPreviousAction = false;
   public String saveAs = "";
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      ListTag list = new ListTag();

      for (String id : this.entityIds) {
         list.add(StringTag.valueOf(id));
      }

      tag.put("entityIds", list);
      tag.putBoolean("mustBeLookingAt", this.mustBeLookingAt);
      tag.putBoolean("centerOnPlayer", this.centerOnPlayer);
      tag.putDouble("radius", this.radius);
      tag.putDouble("x", this.f_x_D);
      tag.putDouble("y", this.f_y_D);
      tag.putDouble("z", this.f_z_D);
      tag.putBoolean("containerEntitiesOnly", this.containerEntitiesOnly);
      tag.putInt("timeoutMs", this.timeoutMs);
      tag.putBoolean("enabled", this.init);
      tag.putString("checkMode", this.checkMode.name());
      p.init(tag, this);
      tag.putString("saveAs", this.saveAs);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.entityIds.clear();
      if (tag.contains("entityIds")) {
         for (Tag el : tag.getList("entityIds").orElse(new ListTag())) {
            String s = el.asString().orElse("");
            if (!s.isEmpty()) {
               this.entityIds.add(s);
            }
         }
      } else if (tag.contains("entityName")) {
         String oldName = tag.getStringOr("entityName", "");
         if (!oldName.isEmpty()) {
            String tryId = "minecraft:" + oldName.toLowerCase().replace(" ", "_");

            try {
               Identifier id = Identifier.parse(tryId);
               if (BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                  this.entityIds.add(tryId);
               }
            } catch (Exception var6) {
            }
         }
      }

      this.mustBeLookingAt = tag.getBooleanOr("mustBeLookingAt", false);
      if (tag.contains("checkMode")) {
         try {
            this.checkMode = aw.Inner_DataUtil.valueOf(tag.getStringOr("checkMode", "RADIUS"));
         } catch (Exception e) {
            this.checkMode = aw.Inner_DataUtil.RADIUS;
         }
      } else {
         this.checkMode = this.mustBeLookingAt ? aw.Inner_DataUtil.LOOKING_AT : aw.Inner_DataUtil.RADIUS;
      }

      this.centerOnPlayer = tag.getBooleanOr("centerOnPlayer", true);
      this.radius = tag.getDoubleOr("radius", 6.0);
      this.containerEntitiesOnly = tag.getBooleanOr("containerEntitiesOnly", false);
      this.timeoutMs = tag.getIntOr("timeoutMs", 0);
      this.f_x_D = tag.getDoubleOr("x", 0.0);
      this.f_y_D = tag.getDoubleOr("y", 0.0);
      this.f_z_D = tag.getDoubleOr("z", 0.0);
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      p.load(tag, this);
      this.saveAs = tag.getStringOr("saveAs", "");
   }

   public void init(CompoundTag tag) {
      this.entityIds.clear();
      String entityId = tag.getStringOr("entityId", "");
      if (!entityId.isBlank()) {
         this.entityIds.add(entityId);
      }

      String condition = tag.getStringOr("condition", "LOOKING_AT");
      aw.Inner_DataUtil var10001;
      switch (condition) {
         case "WITHIN_REACH":
            var10001 = aw.Inner_DataUtil.WITHIN_REACH;
            break;
         case "MOUNTED_IN":
            var10001 = aw.Inner_DataUtil.MOUNTED_IN;
            break;
         case "NEARBY":
            var10001 = aw.Inner_DataUtil.NEARBY;
            break;
         default:
            var10001 = aw.Inner_DataUtil.LOOKING_AT;
      }

      this.checkMode = var10001;
      this.radius = tag.getDoubleOr("range", 5.0);
      this.centerOnPlayer = true;
      this.containerEntitiesOnly = tag.getBooleanOr("containerEntitiesOnly", false);
      this.timeoutMs = tag.getIntOr("timeoutMs", 0);
      p.load(tag, this);
   }

   public boolean init_Z_5(Minecraft mc) {
      if (mc != null && mc.player != null && mc.level != null) {
         boolean var10000;
         switch (this.checkMode) {
            case RADIUS:
            case NEARBY:
               Vec3 center = this.centerOnPlayer
                  ? new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ())
                  : new Vec3(this.f_x_D, this.f_y_D, this.f_z_D);
               double radiusSq = this.radius * this.radius;
               boolean found = false;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity != mc.player && !(entity.distanceToSqr(center) > radiusSq) && this.init(entity)) {
                     found = true;
                     break;
                  }
               }

               var10000 = found;
               break;
            case LOOKING_AT:
               var10000 = this.init(mc.crosshairPickEntity);
               break;
            case WITHIN_REACH: {
               double reachSq = mc.player.blockInteractionRange() * mc.player.blockInteractionRange();
               boolean found2 = false;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity != mc.player && !(entity.distanceToSqr(mc.player) > reachSq) && this.init(entity)) {
                     found2 = true;
                     break;
                  }
               }

               var10000 = found2;
               break;
            }
            case MOUNTED_IN:
               var10000 = this.init(mc.player.getVehicle());
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return false;
      }
   }

   public boolean init(Entity entity) {
      if (entity == null) {
         return false;
      }

      String type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
      if (this.containerEntitiesOnly && !type.contains("boat") && !type.contains("minecart") && !type.contains("llama") && !type.contains("chest")) {
         return false;
      }

      if (this.entityIds.isEmpty()) {
         return true;
      }

      for (String entry : this.entityIds) {
         if (entry != null && !entry.isBlank()) {
            if (!entry.startsWith("~")) {
               if (type.equalsIgnoreCase(entry) || entity.getStringUUID().equalsIgnoreCase(entry) || entity.getName().getString().equalsIgnoreCase(entry)) {
                  return true;
               }
            } else {
               String[] parts = entry.split("~", 4);
               if (parts.length >= 2 && entity.getStringUUID().equalsIgnoreCase(parts[1])) {
                  return true;
               }

               if (parts.length >= 3 && type.equalsIgnoreCase(parts[2])) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public Entity init_Entity(Minecraft mc) {
      if (mc != null && mc.player != null && mc.level != null) {
         if (this.checkMode == aw.Inner_DataUtil.LOOKING_AT) {
            return this.init(mc.crosshairPickEntity) ? mc.crosshairPickEntity : null;
         }

         if (this.checkMode == aw.Inner_DataUtil.MOUNTED_IN) {
            return this.init(mc.player.getVehicle()) ? mc.player.getVehicle() : null;
         }

         Vec3 center = this.centerOnPlayer ? mc.player.position() : new Vec3(this.f_x_D, this.f_y_D, this.f_z_D);
         double maxDistance = this.checkMode == aw.Inner_DataUtil.WITHIN_REACH ? mc.player.blockInteractionRange() : this.radius;
         double maxDistanceSq = maxDistance * maxDistance;
         Entity nearest = null;
         double nearestDistance = 1.7976931348623157E308;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity != mc.player && this.init(entity)) {
               double distance = entity.distanceToSqr(center);
               if (distance <= maxDistanceSq && distance < nearestDistance) {
                  nearest = entity;
                  nearestDistance = distance;
               }
            }
         }

         return nearest;
      } else {
         return null;
      }
   }

   @Override
   public StreamGate getType() {
      return StreamGate.WAIT_ENTITY;
   }

   @Override
   public String getDisplayName() {
      String first;
      if (this.entityIds.isEmpty()) {
         first = "any";
      } else {
         String e0 = this.entityIds.get(0);
         if (e0.startsWith("~")) {
            String[] p = e0.split("~", 4);
            String display = p.length >= 4 ? p[3] : "";
            String type = p.length >= 3 ? cc.read(p[2]) : "?";
            first = "SPEC " + (display != null && !display.isBlank() ? display : type);
         } else {
            first = cc.read(e0);
         }

         if (this.entityIds.size() > 1) {
            first = first + " (+" + (this.entityIds.size() - 1) + ")";
         }
      }

      String var10000;
      switch (this.checkMode) {
         case RADIUS:
            var10000 = "r=" + (int)this.radius;
            break;
         case LOOKING_AT:
            var10000 = "look";
            break;
         case WITHIN_REACH:
            var10000 = "reach";
            break;
         case MOUNTED_IN:
            var10000 = "mounted";
            break;
         case NEARBY:
            var10000 = "nearby " + (int)this.radius;
            break;
         default:
            throw new MatchException(null, null);
      }

      String modeStr = var10000;
      return "Wait Entity: " + first + " (" + modeStr + ")";
   }

   @Override
   public String getIcon() {
      return "ENT";
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
   public String load() {
      return this.saveAs;
   }

   @Override
   public void a_(String name) {
      this.saveAs = name == null ? "" : name;
   }

   public enum Inner_DataUtil {
      RADIUS,
      LOOKING_AT,
      WITHIN_REACH,
      MOUNTED_IN,
      NEARBY;
   }
}
