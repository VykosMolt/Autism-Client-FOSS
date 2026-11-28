package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiEntityQuery {
   static final Set<String> init_Set = Set.of("minecraft:ender_dragon", "minecraft:wither", "minecraft:elder_guardian");
   static final Set<String> load = Set.of("hostile", "passive", "villager", "player", "item", "projectile", "boss", "vehicle", "all");
   private static final EquipmentSlot[] init_EquipmentSlotArr = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private static final String[] init_StrArr = new String[]{"head", "chest", "legs", "feet"};

   private AiEntityQuery() {
   }

   static String init(AiEntityQuery.EntityFacts facts) {
      if (init_Set.contains(facts.typeId())) {
         return "boss";
      } else if (facts.enemy()) {
         return "hostile";
      } else if (facts.villager()) {
         return "villager";
      } else if (facts.player()) {
         return "player";
      } else if (facts.item()) {
         return "item";
      } else if (facts.projectile()) {
         return "projectile";
      } else if (facts.vehicle()) {
         return "vehicle";
      } else {
         return facts.living() ? "passive" : "other";
      }
   }

   static boolean init(String filter, String category) {
      return filter.equals("all") || filter.equals(category);
   }

   static boolean load(String typeId, String spec) {
      String clean = spec.trim().toLowerCase(Locale.ROOT);
      return !clean.isEmpty() && typeId.toLowerCase(Locale.ROOT).contains(clean);
   }

   static JsonObject init(
      int id,
      String typeId,
      double x,
      double y,
      double z,
      double distance,
      Float health,
      boolean hostile,
      String category,
      List<String> equipment,
      boolean lineOfSight,
      String itemId,
      Integer itemCount
   ) {
      JsonObject out = new JsonObject();
      out.addProperty("id", id);
      out.addProperty("type", typeId);
      out.add("pos", AiJson.objectOf("x", init(x), "y", init(y), "z", init(z)));
      out.addProperty("distance", init(distance));
      out.add("health", health == null ? JsonNull.INSTANCE : AiJson.GSON.toJsonTree(init(health.floatValue())));
      out.addProperty("hostile", hostile);
      out.addProperty("category", category);
      if (!equipment.isEmpty()) {
         out.add("equipment", AiJson.GSON.toJsonTree(equipment));
      }

      out.addProperty("lineOfSight", lineOfSight);
      if (itemId != null) {
         out.add("item", AiJson.objectOf("id", itemId, "count", itemCount));
      }

      return out;
   }

   static JsonObject query(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         double radius = Math.max(1.0, Math.min(128.0, AiJson.getDouble(args, "radius", 32.0)));
         boolean boxMode = args.has("dx") || args.has("dy") || args.has("dz");
         double dx = Math.max(1.0, Math.min(128.0, AiJson.getDouble(args, "dx", 32.0)));
         double dy = Math.max(1.0, Math.min(128.0, AiJson.getDouble(args, "dy", 32.0)));
         double dz = Math.max(1.0, Math.min(128.0, AiJson.getDouble(args, "dz", 32.0)));
         String spec = AiJson.getString(args, "type", "");
         String category = AiJson.getString(args, "category", "all").trim().toLowerCase(Locale.ROOT);
         if (!load.contains(category)) {
            throw new IllegalArgumentException("category must be one of hostile, passive, villager, player, item, projectile, boss, vehicle, all");
         }

         int limit = Math.max(1, Math.min(128, AiJson.getInt(args, "limit", 32)));

         record Inner_DataUtil(JsonObject entry, double distance, int id) {
         }

         List<Inner_DataUtil> rows = new ArrayList<>();
         Vec3 origin = mc.player.position();
         int matched = 0;

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (!entity.isRemoved()) {
               Vec3 pos = entity.position();
               double distanceSqr = mc.player.distanceToSqr(pos);
               if (boxMode
                  ? !(Math.abs(pos.x - origin.x) > dx) && !(Math.abs(pos.y - origin.y) > dy) && !(Math.abs(pos.z - origin.z) > dz)
                  : !(distanceSqr > radius * radius)) {
                  String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                  if (spec.isBlank() || load(typeId, spec)) {
                     AiEntityQuery.EntityFacts facts = init(entity, typeId);
                     String assigned = init(facts);
                     if (init(category, assigned)) {
                        matched++;
                        double distance = Math.sqrt(distanceSqr);
                        Float health = entity instanceof LivingEntity living ? living.getHealth() : null;
                        String itemId = null;
                        Integer itemCount = null;
                        if (entity instanceof ItemEntity item) {
                           itemId = BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString();
                           itemCount = item.getItem().getCount();
                        }

                        rows.add(
                           new Inner_DataUtil(
                              init(
                                 entity.getId(),
                                 typeId,
                                 pos.x,
                                 pos.y,
                                 pos.z,
                                 distance,
                                 health,
                                 facts.enemy(),
                                 assigned,
                                 init(entity),
                                 init(mc, entity),
                                 itemId,
                                 itemCount
                              ),
                              distance,
                              entity.getId()
                           )
                        );
                     }
                  }
               }
            }
         }

         rows.sort(Comparator.comparingDouble(Inner_DataUtil::distance).thenComparingInt(Inner_DataUtil::id));
         JsonArray entities = new JsonArray();

         for (Inner_DataUtil row : rows) {
            if (entities.size() >= limit) {
               break;
            }

            entities.add(row.entry());
         }

         return AiJson.objectOf("entities", entities, "count", entities.size(), "matched", matched, "mode", boxMode ? "box" : "radius");
      } else {
         throw new IllegalArgumentException("Entity query requires a connected living player");
      }
   }

   static AiEntityQuery.EntityFacts init(Entity entity, String typeId) {
      return new AiEntityQuery.EntityFacts(
         typeId,
         entity instanceof Enemy,
         entity instanceof LivingEntity,
         entity instanceof Player,
         entity instanceof AbstractVillager,
         entity instanceof ItemEntity,
         entity instanceof Projectile,
         entity instanceof VehicleEntity || entity instanceof AbstractMinecart || entity instanceof PlayerRideable
      );
   }

   private static List<String> init(Entity entity) {
      if (!(entity instanceof LivingEntity living)) {
         return List.of();
      } else {
         List<String> out = new ArrayList<>(6);
         init(out, "mainhand", living.getMainHandItem());
         init(out, "offhand", living.getOffhandItem());

         for (int i = 0; i < init_EquipmentSlotArr.length; i++) {
            init(out, init_StrArr[i], living.getItemBySlot(init_EquipmentSlotArr[i]));
         }

         return out;
      }
   }

   private static void init(List<String> out, String name, ItemStack stack) {
      if (!stack.isEmpty()) {
         out.add(name + "=" + BuiltInRegistries.ITEM.getKey(stack.getItem()));
      }
   }

   private static boolean init(Minecraft mc, Entity entity) {
      Vec3 eye = mc.player.getEyePosition();
      Vec3 target = entity instanceof LivingEntity living ? living.getEyePosition() : entity.getBoundingBox().getCenter();
      return mc.level.clip(new ClipContext(eye, target, Block.OUTLINE, Fluid.NONE, mc.player)).getType() == Type.MISS;
   }

   private static double init(double value) {
      return Math.round(value * 100.0) / 100.0;
   }

   record EntityFacts(String typeId, boolean enemy, boolean living, boolean player, boolean villager, boolean item, boolean projectile, boolean vehicle) {
   }
}
