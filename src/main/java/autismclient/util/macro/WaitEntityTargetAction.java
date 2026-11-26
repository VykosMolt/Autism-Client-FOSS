package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class WaitEntityTargetAction implements MacroAction {
   public WaitEntityTargetAction.Inner_DataUtil condition = WaitEntityTargetAction.Inner_DataUtil.LOOKING_AT;
   public String entityId = "";
   public double range = 5.0;
   public boolean containerEntitiesOnly = false;
   public int timeoutMs = 0;
   public boolean listenDuringPreviousAction = false;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_ENTITY_TARGET;
   }

   public boolean init(Minecraft mc) {
      if (mc != null && mc.player != null && mc.level != null) {
         MacroTemplateResolver.ResolveResult entityResolution = MacroVariableContext.init_j_CachePool_Str_Minecraft(this.entityId, mc);
         if (!entityResolution.init_Z()) {
            return false;
         }

         String expectedEntity = entityResolution.init_Str();
         boolean var10000;
         switch (this.condition) {
            case LOOKING_AT:
               var10000 = this.init(mc.crosshairPickEntity, expectedEntity);
               break;
            case WITHIN_REACH:
            case NEARBY:
               boolean found = false;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity != mc.player && mc.player.distanceTo(entity) <= this.range && this.init(entity, expectedEntity)) {
                     found = true;
                     break;
                  }
               }

               var10000 = found;
               break;
            case MOUNTED_IN:
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
      ListenDuringPreviousSerializer.init(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.condition = MacroNbtCodecHelper.parseEnum(WaitEntityTargetAction.Inner_DataUtil.class, tag.getStringOr("condition", "LOOKING_AT"), WaitEntityTargetAction.Inner_DataUtil.LOOKING_AT);
      this.entityId = tag.getStringOr("entityId", "");
      this.range = tag.getDoubleOr("range", 5.0);
      this.containerEntitiesOnly = tag.getBooleanOr("containerEntitiesOnly", false);
      this.timeoutMs = tag.getIntOr("timeoutMs", 0);
      ListenDuringPreviousSerializer.load(tag, this);
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
      LOOKING_AT,
      WITHIN_REACH,
      MOUNTED_IN,
      NEARBY;
   }
}
