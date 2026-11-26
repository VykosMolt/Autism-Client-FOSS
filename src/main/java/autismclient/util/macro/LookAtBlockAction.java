package autismclient.util.macro;

import autismclient.util.cc;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class LookAtBlockAction implements MacroAction {
   public int blockX = 0;
   public int blockY = 0;
   public int blockZ = 0;
   public LookAtBlockAction.Inner_StreamUtil targetMode = LookAtBlockAction.Inner_StreamUtil.SPECIFIC;
   public List<String> blockIds = new ArrayList<>();
   public List<String> entityIds = new ArrayList<>();
   public double searchRadius = 16.0;
   public boolean smooth;
   public int smoothness = 6;
   public boolean waitForCompletion = true;
   private boolean init = true;

   public LookAtBlockAction() {
   }

   public LookAtBlockAction(int x, int y, int z) {
      this.blockX = x;
      this.blockY = y;
      this.blockZ = z;
      this.targetMode = LookAtBlockAction.Inner_StreamUtil.SPECIFIC;
   }

   public static String init_Str_Entity(Entity entity) {
      if (entity == null) {
         return "";
      }

      String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
      String displayName = entity.getDisplayName().getString().replaceAll("§.", "").trim();
      return "~" + entity.getStringUUID() + "~" + typeId + "~" + displayName;
   }

   public double init_D_2() {
      return RotateAction.init_D(this.smoothness);
   }

   public LookAtBlockAction.Inner_DataUtil init_ValueSink_DataUtil_Minecraft(Minecraft mc) {
      if (mc != null && mc.player != null && mc.level != null) {
         LookAtBlockAction.Inner_DataUtil var10000;
         switch (this.targetMode) {
            case SPECIFIC:
               var10000 = init(mc.player.getEyePosition(), new Vec3(this.blockX + 0.5, this.blockY + 0.5, this.blockZ + 0.5));
               break;
            case BLOCK:
               var10000 = this.load(mc);
               break;
            case ENTITY:
               var10000 = this.read(mc);
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return null;
      }
   }

   @Override
   public void execute(Minecraft mc) {
      if (mc != null && mc.player != null) {
         LookAtBlockAction.Inner_DataUtil target = this.init_ValueSink_DataUtil_Minecraft(mc);
         if (target != null && !this.smooth) {
            mc.player.setYRot(target.init());
            mc.player.setXRot(target.load());
         }
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putInt("blockX", this.blockX);
      tag.putInt("blockY", this.blockY);
      tag.putInt("blockZ", this.blockZ);
      tag.putString("targetMode", this.targetMode.name());
      ListTag blocks = new ListTag();

      for (String id : this.blockIds) {
         blocks.add(StringTag.valueOf(id));
      }

      tag.put("blockIds", blocks);
      ListTag entities = new ListTag();

      for (String id : this.entityIds) {
         entities.add(StringTag.valueOf(id));
      }

      tag.put("entityIds", entities);
      tag.putDouble("searchRadius", this.searchRadius);
      tag.putBoolean("smooth", this.smooth);
      tag.putInt("smoothness", RotateAction.init_I(this.smoothness));
      tag.putBoolean("waitForCompletion", this.waitForCompletion);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("blockX")) {
         this.blockX = tag.getIntOr("blockX", 0);
      }

      if (tag.contains("blockY")) {
         this.blockY = tag.getIntOr("blockY", 0);
      }

      if (tag.contains("blockZ")) {
         this.blockZ = tag.getIntOr("blockZ", 0);
      }

      if (tag.contains("targetMode")) {
         try {
            this.targetMode = LookAtBlockAction.Inner_StreamUtil.valueOf(tag.getStringOr("targetMode", LookAtBlockAction.Inner_StreamUtil.SPECIFIC.name()));
         } catch (Exception ignored) {
            this.targetMode = LookAtBlockAction.Inner_StreamUtil.SPECIFIC;
         }
      } else {
         this.targetMode = LookAtBlockAction.Inner_StreamUtil.SPECIFIC;
      }

      this.blockIds.clear();
      if (tag.contains("blockIds")) {
         for (Tag el : tag.getList("blockIds").orElse(new ListTag())) {
            String value = el.asString().orElse("");
            if (!value.isEmpty()) {
               this.blockIds.add(value);
            }
         }
      }

      this.entityIds.clear();
      if (tag.contains("entityIds")) {
         for (Tag el : tag.getList("entityIds").orElse(new ListTag())) {
            String value = el.asString().orElse("");
            if (!value.isEmpty()) {
               this.entityIds.add(value);
            }
         }
      }

      this.searchRadius = Math.max(1.0, tag.getDoubleOr("searchRadius", 16.0));
      this.smooth = tag.getBooleanOr("smooth", false);
      this.smoothness = RotateAction.init_I(tag.getIntOr("smoothness", 6));
      this.waitForCompletion = tag.getBooleanOr("waitForCompletion", true);
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.LOOK_AT_BLOCK;
   }

   @Override
   public String getDisplayName() {
      String var10000;
      switch (this.targetMode) {
         case SPECIFIC:
            var10000 = "(" + this.blockX + ", " + this.blockY + ", " + this.blockZ + ")";
            break;
         case BLOCK:
            var10000 = this.init_Str_5();
            break;
         case ENTITY:
            var10000 = this.load();
            break;
         default:
            throw new MatchException(null, null);
      }

      String targetLabel = var10000;
      String suffix = this.smooth ? " (Smooth)" : "";
      if (!this.waitForCompletion) {
         suffix = suffix + " [NoWait]";
      }

      return "Look At " + targetLabel + suffix;
   }

   @Override
   public String getIcon() {
      return "LAB";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   private LookAtBlockAction.Inner_DataUtil load(Minecraft mc) {
      if (mc.player != null && mc.level != null && !this.blockIds.isEmpty()) {
         Vec3 eyePos = mc.player.getEyePosition();
         BlockPos center = mc.player.blockPosition();
         int radius = Math.max(1, Mth.ceil(this.searchRadius));
         double bestSq = this.searchRadius * this.searchRadius;
         BlockPos bestPos = null;
         Set<String> wanted = new HashSet<>(this.blockIds);
         MutableBlockPos mutable = new MutableBlockPos();

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  mutable.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                  Vec3 targetPos = Vec3.atCenterOf(mutable);
                  double distSq = eyePos.distanceToSqr(targetPos);
                  if (!(distSq > bestSq)) {
                     Block block = mc.level.getBlockState(mutable).getBlock();
                     String id = BuiltInRegistries.BLOCK.getKey(block).toString();
                     if (wanted.contains(id)) {
                        bestSq = distSq;
                        bestPos = mutable.immutable();
                     }
                  }
               }
            }
         }

         return bestPos == null ? null : init(eyePos, Vec3.atCenterOf(bestPos));
      } else {
         return null;
      }
   }

   private LookAtBlockAction.Inner_DataUtil read(Minecraft mc) {
      if (mc.player != null && mc.level != null && !this.entityIds.isEmpty()) {
         Set<String> typeIds = new HashSet<>();
         Set<String> specificUuids = new HashSet<>();

         for (String entry : this.entityIds) {
            if (entry != null && !entry.isBlank()) {
               if (entry.startsWith("~")) {
                  String[] parts = entry.split("~", 4);
                  if (parts.length >= 2 && !parts[1].isBlank()) {
                     specificUuids.add(parts[1]);
                  }
               } else {
                  typeIds.add(entry);
               }
            }
         }

         double maxSq = this.searchRadius * this.searchRadius;
         double bestSq = maxSq;
         Entity bestEntity = null;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity != null && entity != mc.player && entity.isAlive()) {
               double distSq = mc.player.distanceToSqr(entity);
               if (!(distSq > bestSq) && this.init(entity, typeIds, specificUuids)) {
                  bestSq = distSq;
                  bestEntity = entity;
               }
            }
         }

         if (bestEntity == null) {
            return null;
         }

         Vec3 targetPos = bestEntity.getBoundingBox().getCenter();
         return init(mc.player.getEyePosition(), targetPos);
      } else {
         return null;
      }
   }

   private boolean init(Entity entity, Set<String> typeIds, Set<String> specificUuids) {
      if (specificUuids.contains(entity.getStringUUID())) {
         return true;
      }

      String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
      return typeIds.contains(typeId);
   }

   private String init_Str_5() {
      if (this.blockIds.isEmpty()) {
         return "Block (none)";
      }

      String first = cc.load(this.blockIds.get(0));
      return this.blockIds.size() == 1 ? "Nearest " + first : "Nearest " + first + " (+" + (this.blockIds.size() - 1) + ")";
   }

   private String load() {
      if (this.entityIds.isEmpty()) {
         return "Entity (none)";
      }

      String firstEntry = this.entityIds.get(0);
      String first;
      if (firstEntry.startsWith("~")) {
         String[] parts = firstEntry.split("~", 4);
         String rawName = parts.length >= 4 ? parts[3] : "";
         String type = parts.length >= 3 ? cc.read(parts[2]) : "?";
         first = rawName != null && !rawName.isBlank() ? rawName + " (" + type + ")" : type;
      } else {
         first = cc.read(firstEntry);
      }

      return this.entityIds.size() == 1 ? "Nearest " + first : "Nearest " + first + " (+" + (this.entityIds.size() - 1) + ")";
   }

   private static LookAtBlockAction.Inner_DataUtil init(Vec3 from, Vec3 to) {
      Vec3 diff = to.subtract(from);
      double dist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
      float yaw = (float)Math.toDegrees(Math.atan2(-diff.x, diff.z));
      float pitch = (float)(-Math.toDegrees(Math.atan2(diff.y, dist)));
      return new LookAtBlockAction.Inner_DataUtil(yaw, pitch);
   }

   public record Inner_DataUtil(float init, float load) {
   }

   public enum Inner_StreamUtil {
      SPECIFIC,
      BLOCK,
      ENTITY;
   }
}
