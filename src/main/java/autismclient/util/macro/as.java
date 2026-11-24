package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class as implements MacroAction {
   public as.Inner_DataUtil condition = as.Inner_DataUtil.init;
   public String entityId = "";
   public double range = 5.0;
   public boolean containerEntitiesOnly = false;
   public int timeoutMs = 0;
   public boolean listenDuringPreviousAction = false;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_G_StreamGate;
   }

   public boolean init(Minecraft mc) {
      if (mc != null && mc.player != null && mc.level != null) {
         j.Inner_CachePool entityResolution = n.init_j_CachePool_Str_Minecraft(this.entityId, mc);
         if (!entityResolution.init_Z()) {
            return false;
         }

         String expectedEntity = entityResolution.init_Str();
         boolean var10000;
         switch (this.condition) {
            case init:
               var10000 = this.init(mc.crosshairPickEntity, expectedEntity);
               break;
            case load:
            case write:
               boolean found = false;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity != mc.player && mc.player.distanceTo(entity) <= this.range && this.init(entity, expectedEntity)) {
                     found = true;
                     break;
                  }
               }

               var10000 = found;
               break;
            case read:
               var10000 = this.init(mc.player.getVehicle(), expectedEntity);
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return false;
      }
   }

   private boolean init(Entity entity, String expectedEntity) {
      if (entity == null) {
         return false;
      } else {
         String type = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
         if (this.containerEntitiesOnly && !type.contains("boat") && !type.contains("minecart") && !type.contains("llama") && !type.contains("chest")) {
            return false;
         } else {
            return expectedEntity != null && !expectedEntity.isBlank()
               ? type.equalsIgnoreCase(expectedEntity)
                  || entity.getStringUUID().equalsIgnoreCase(expectedEntity)
                  || entity.getName().getString().equalsIgnoreCase(expectedEntity)
               : true;
         }
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "WAIT_ENTITY_TARGET");
      tag.putString("condition", this.condition.name());
      tag.putString("entityId", this.entityId);
      tag.putDouble("range", this.range);
      tag.putBoolean("containerEntitiesOnly", this.containerEntitiesOnly);
      tag.putInt("timeoutMs", this.timeoutMs);
      p.init(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.condition = i.init(as.Inner_DataUtil.class, tag.getStringOr("condition", "LOOKING_AT"), as.Inner_DataUtil.init);
      this.entityId = tag.getStringOr("entityId", "");
      this.range = tag.getDoubleOr("range", 5.0);
      this.containerEntitiesOnly = tag.getBooleanOr("containerEntitiesOnly", false);
      this.timeoutMs = tag.getIntOr("timeoutMs", 0);
      p.load(tag, this);
   }

   @Override
   public String getDisplayName() {
      return "Wait entity " + this.condition;
   }

   @Override
   public String getIcon() {
      return "E";
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write;
   }
}
