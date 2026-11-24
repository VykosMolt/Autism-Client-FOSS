package autismclient.ai;

import autismclient.api.ai.AiObservation;
import autismclient.util.AuditWaypointScope;
import autismclient.util.AutismWaypoints;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

final class S {
   private static Map<String, Integer> init_Map = Map.of();
   private static long init_J = -1L;
   private static LocalPlayer init_LocalPlayer;

   private S() {
   }

   static AiObservation init(String session, String scope, String source) {
      return init(session, scope, source, -1L, 0);
   }

   static AiObservation init(String session, String scope, String source, long taskElapsedSeconds, int taskMinutes) {
      Minecraft mc = Minecraft.getInstance();
      JsonObject state = ag.init_JsonObject_ObjArr("connected", mc.player != null && mc.level != null);
      if (taskElapsedSeconds >= 0L) {
         state.addProperty("taskElapsedSeconds", taskElapsedSeconds);
         state.addProperty("taskRemainingSeconds", Math.max(0L, taskMinutes * 60L - taskElapsedSeconds));
      }

      FabricLoader loader = FabricLoader.getInstance();
      state.addProperty("clientVersion", loader.getModContainer("autism").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown"));
      state.addProperty("gameVersion", loader.getModContainer("minecraft").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown"));
      if (mc.player != null && mc.level != null) {
         state.add(
            "position",
            ag.init_JsonObject_ObjArr(
               "x",
               init(mc.player.getX(), 1000.0),
               "y",
               init(mc.player.getY(), 1000.0),
               "z",
               init(mc.player.getZ(), 1000.0),
               "yaw",
               init(mc.player.getYRot(), 10.0),
               "pitch",
               init(mc.player.getXRot(), 10.0)
            )
         );
         state.addProperty("health", mc.player.getHealth());
         state.addProperty("food", mc.player.getFoodData().getFoodLevel());
         state.addProperty("dimension", mc.level.dimension().identifier().toString());
         state.addProperty("gameTick", mc.level.getGameTime());
         state.addProperty("eventsPending", x.init_x().init_I());
         state.addProperty("biome", mc.level.getBiome(mc.player.blockPosition()).unwrapKey().map(key -> key.identifier().toString()).orElse("unknown"));
         state.addProperty("weather", mc.level.isThundering() ? "thunder" : (mc.level.isRaining() ? "rain" : "clear"));
         long dayTime = mc.level.getOverworldClockTime();
         state.addProperty("dayTime", dayTime);
         state.addProperty("timeOfDay", init_Str_J(dayTime));
         state.addProperty("difficulty", mc.level.getDifficulty().getSerializedName());
         state.addProperty("gameMode", mc.gameMode.getPlayerMode().getName());
         JsonArray effects = new JsonArray();

         for (MobEffectInstance effect : mc.player.getActiveEffects()) {
            if (effects.size() >= 12) {
               break;
            }

            effects.add(init(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString(), effect.getAmplifier(), effect.getDuration()));
         }

         state.add("effects", effects);
         JsonArray inventory = new JsonArray();

         for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
               inventory.add(
                  ag.init_JsonObject_ObjArr(
                     "slot",
                     i,
                     "item",
                     BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
                     "count",
                     stack.getCount(),
                     "name",
                     stack.getHoverName().getString(),
                     "damage",
                     stack.getDamageValue(),
                     "maxDamage",
                     stack.getMaxDamage(),
                     "components",
                     cN.init(stack)
                  )
               );
            }
         }

         state.add("inventory", inventory);
         state.add("scoreboard", cN.init_JsonArray(mc));
         state.add("tabList", cN.load_JsonArray(mc));
         state.add("tabDecoration", cN.init_JsonObject(mc));
         state.add("bossBars", cN.read(mc));
         state.add("vitals", cN.load_JsonObject(mc));
         state.add("customMenu", T.init());
         state.add("recentServerMessages", ag.init.toJsonTree(ca.init_List_I(6)));
         JsonArray chatLines = new JsonArray();

         for (TokenStore.Inner_StreamUtil entry : TokenStore.init_List(8)) {
            JsonArray actions = new JsonArray();

            for (TokenStore.Inner_DataUtil click : entry.clicks()) {
               actions.add(ag.init_JsonObject_ObjArr("action", click.action(), "value", click.value()));
            }

            chatLines.add(
               ag.init_JsonObject_ObjArr(
                  "source", entry.source(), "sender", entry.sender(), "text", entry.text(), "hoverText", entry.hoverText(), "clicks", actions
               )
            );
         }

         state.add("recentChat", chatLines);
         JsonArray slots = new JsonArray();

         for (Slot slot : mc.player.containerMenu.slots) {
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
               slots.add(
                  ag.init_JsonObject_ObjArr(
                     "slot",
                     slot.index,
                     "item",
                     BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
                     "count",
                     stack.getCount(),
                     "name",
                     stack.getHoverName().getString(),
                     "components",
                     cN.init(stack)
                  )
               );
            }
         }

         state.add(
            "container",
            ag.init_JsonObject_ObjArr(
               "id",
               mc.player.containerMenu.containerId,
               "type",
               mc.player.containerMenu.getClass().getSimpleName(),
               "screen",
               mc.gui.screen() == null ? "none" : mc.gui.screen().getTitle().getString(),
               "slots",
               slots
            )
         );
         JsonArray entities = new JsonArray();

         for (Entity e : mc.level.entitiesForRendering()) {
            if (e != mc.player && e.distanceToSqr(mc.player) < 2304.0 && entities.size() < 20) {
               String entityType = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();
               JsonObject entry = ag.init_JsonObject_ObjArr(
                  "id",
                  e.getId(),
                  "uuid",
                  e.getUUID().toString(),
                  "type",
                  entityType,
                  "x",
                  init(e.getX(), 100.0),
                  "y",
                  init(e.getY(), 100.0),
                  "z",
                  init(e.getZ(), 100.0)
               );
               if (e instanceof LivingEntity living) {
                  entry.addProperty("health", Math.round(living.getHealth() * 10.0F) / 10.0);
               }

               String category = v.init(v.init(e, entityType));
               entry.addProperty("hostile", category.equals("hostile") || category.equals("boss"));
               entities.add(entry);
            }
         }

         state.add("entities", entities);
         JsonArray projectiles = new JsonArray();

         for (by.Inner_StreamUtil tracked : by.init(mc)) {
            if (projectiles.size() < 16) {
               projectiles.add(
                  ag.init_JsonObject_ObjArr(
                     "id",
                     tracked.entityId(),
                     "type",
                     tracked.type(),
                     "x",
                     tracked.x(),
                     "y",
                     tracked.y(),
                     "z",
                     tracked.z(),
                     "vx",
                     tracked.vx(),
                     "vy",
                     tracked.vy(),
                     "vz",
                     tracked.vz(),
                     "gravity",
                     tracked.gravity(),
                     "closestApproach",
                     tracked.closestApproach(),
                     "secondsToClosest",
                     tracked.secondsToClosest()
                  )
               );
            }
         }

         state.add("projectiles", projectiles);
         state.add("danger", i.init_JsonObject_Minecraft_I(mc, 8));
         JsonArray blocks = new JsonArray();
         BlockPos origin = mc.player.blockPosition();
         List<BlockPos> solid = new ArrayList<>();

         for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
               for (int y = -2; y <= 3; y++) {
                  BlockPos p = origin.offset(x, y, z);
                  if (cu.init(mc.level, p) && !mc.level.getBlockState(p).isAir()) {
                     solid.add(p);
                  }
               }
            }
         }

         solid.sort(Comparator.comparingInt(px -> px.distManhattan(origin)));

         for (BlockPos p : solid) {
            if (blocks.size() >= 48) {
               break;
            }

            BlockState b = mc.level.getBlockState(p);
            JsonObject entry = ag.init_JsonObject_ObjArr(
               "x", p.getX(), "y", p.getY(), "z", p.getZ(), "block", BuiltInRegistries.BLOCK.getKey(b.getBlock()).toString()
            );
            if (!b.getProperties().isEmpty()) {
               entry.addProperty("state", b.toString());
            }

            blocks.add(entry);
         }

         state.add("nearbyBlocks", blocks);
         state.add("activeModules", ag.init.toJsonTree(autismclient.modules.al.load_List().stream().map(m -> m.id()).toList()));
         state.add("waypoints", ag.init.toJsonTree(AutismWaypoints.init_AutismWaypoints().init_List_Str(AuditWaypointScope.current(mc))));
         state.addProperty(
            "observationLimits",
            "Nearby blocks are capped at 48 within 5 blocks horizontally (use game:survey for wider/detail). Entities capped at 20 within 48 blocks; health is living-only, hostile follows game:entities classification (bosses included). Effects capped at 12. Projectiles (capped at 16) are ballistic predictions, not guarantees. Danger is a client-side damage prediction, not a guarantee. Unloaded terrain and remote inventories are unknown."
         );
      }

      return new AiObservation(UUID.randomUUID().toString(), session, scope, System.currentTimeMillis(), source, state.toString());
   }

   static double init(double value, double scale) {
      return Math.round(value * scale) / scale;
   }

   static int init_I_Str(String item) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return 0;
      }

      long revision = ca.init_J();
      if (init_LocalPlayer != mc.player || init_J != revision) {
         Map<String, Integer> rebuilt = new HashMap<>();

         for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
               rebuilt.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
            }
         }

         init_Map = rebuilt;
         init_J = revision;
         init_LocalPlayer = mc.player;
      }

      return init_Map.getOrDefault(item, 0);
   }

   static int load(String item) {
      Minecraft mc = Minecraft.getInstance();
      int count = 0;
      if (mc.player != null) {
         for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(item)) {
               count += stack.getCount();
            }
         }
      }

      return count;
   }

   static String init_Str_J(long dayTime) {
      long t = Math.floorMod(dayTime, 24000L);
      if (t > 23460L) {
         return "sunrise";
      } else if (t >= 12542L) {
         return "night";
      } else if (t >= 11800L) {
         return "sunset";
      } else {
         return t < 1000L ? "sunrise" : "day";
      }
   }

   static String init(String id, int amplifier, int durationTicks) {
      return id + " " + load(amplifier + 1) + " (" + init_Str_I(durationTicks) + ")";
   }

   static String init_Str_I(int durationTicks) {
      if (durationTicks < 0) {
         return "inf";
      }

      int seconds = durationTicks / 20;
      return seconds / 60 + ":" + String.format(Locale.ROOT, "%02d", seconds % 60);
   }

   static String load(int value) {
      String var10000;
      switch (value) {
         case 1:
            var10000 = "I";
            break;
         case 2:
            var10000 = "II";
            break;
         case 3:
            var10000 = "III";
            break;
         case 4:
            var10000 = "IV";
            break;
         case 5:
            var10000 = "V";
            break;
         case 6:
            var10000 = "VI";
            break;
         case 7:
            var10000 = "VII";
            break;
         case 8:
            var10000 = "VIII";
            break;
         case 9:
            var10000 = "IX";
            break;
         case 10:
            var10000 = "X";
            break;
         default:
            var10000 = Integer.toString(value);
      }

      return var10000;
   }

   static JsonObject init_JsonObject_JsonObject(JsonObject state) {
      JsonObject out = state.deepCopy();

      for (String key : List.of("nearbyBlocks", "entities", "scoreboard", "tabList", "bossBars")) {
         JsonElement value = out.remove(key);
         if (value != null && value.isJsonArray()) {
            out.addProperty(key + "Count", value.getAsJsonArray().size());
         }
      }

      out.remove("tabDecoration");
      JsonElement effects = out.get("effects");
      if (effects != null && effects.isJsonArray() && effects.getAsJsonArray().size() > 6) {
         JsonArray first = new JsonArray();

         for (int i = 0; i < 6; i++) {
            first.add(effects.getAsJsonArray().get(i));
         }

         out.add("effects", first);
         out.addProperty("effectsTruncated", effects.getAsJsonArray().size() - 6);
      }

      JsonElement chat = out.get("recentChat");
      if (chat != null && chat.isJsonArray() && chat.getAsJsonArray().size() > 3) {
         JsonArray last = new JsonArray();

         for (int i = chat.getAsJsonArray().size() - 3; i < chat.getAsJsonArray().size(); i++) {
            last.add(chat.getAsJsonArray().get(i));
         }

         out.add("recentChat", last);
      }

      JsonElement container = out.get("container");
      if (container != null && container.isJsonObject()) {
         JsonObject c = container.getAsJsonObject();
         JsonElement slots = c.get("slots");
         if (slots != null && slots.isJsonArray() && slots.getAsJsonArray().size() > 20) {
            JsonArray first = new JsonArray();

            for (int i = 0; i < 20; i++) {
               first.add(slots.getAsJsonArray().get(i));
            }

            c.add("slots", first);
            c.addProperty("slotsTruncated", slots.getAsJsonArray().size() - 20);
         }
      }

      return out;
   }
}
