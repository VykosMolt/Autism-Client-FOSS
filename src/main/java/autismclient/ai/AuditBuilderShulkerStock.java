package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assimilated from builder-fixes (minus features already native in AUTISM obf(2)):
 * <ul>
 *   <li>Any-height external shulker stock discovery at build start ({@link #findExternalStock}).</li>
 *   <li>Shulker-first material withdrawal before {@code game:craft} / {@code game:obtain}, plus
 *       refusing {@code game:obtain} gathering when standing over water ({@link #before}).</li>
 * </ul>
 */
final class AuditBuilderShulkerStock {
   private static final Logger LOG = LoggerFactory.getLogger("autism-builder");
   static final int REACH = 24;

   private AuditBuilderShulkerStock() {
   }

   static List<BlockPos> boxesInRange(int minX, int maxX, int minZ, int maxZ, Set<Long> schematicCells) {
      List<BlockPos> found = new ArrayList<>();
      ClientLevel level = Minecraft.getInstance().level;
      if (level == null) {
         return found;
      }

      for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
         for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
            if (!level.hasChunk(cx, cz)) {
               continue;
            }

            for (BlockPos p : level.getChunk(cx, cz).getBlockEntities().keySet()) {
               if (p.getX() >= minX
                  && p.getX() <= maxX
                  && p.getZ() >= minZ
                  && p.getZ() <= maxZ
                  && !schematicCells.contains(p.asLong())
                  && level.getBlockState(p).getBlock() instanceof ShulkerBoxBlock) {
                  found.add(p.immutable());
               }
            }
         }
      }

      return found;
   }

   static BlockPos findExternalStock(BlockPos player, int minX, int maxX, int minZ, int maxZ, Set<Long> schematicCells) {
      List<BlockPos> found = boxesInRange(player.getX() - REACH, player.getX() + REACH, player.getZ() - REACH, player.getZ() + REACH, schematicCells);
      if (found.isEmpty() && minX <= maxX) {
         found = boxesInRange(minX - REACH, maxX + REACH, minZ - REACH, maxZ + REACH, schematicCells);
      }

      if (!found.isEmpty()) {
         BlockPos first = found.get(0);
         LOG.info("external shulker stock found at {} (any height)", first.toShortString());
         return first;
      }

      return null;
   }

   static JsonObject before(AiService assistant, long generation, AiService.SessionState session, AiIntents.IntentStep step, String label)
      throws InterruptedException {
      try {
         String capability = step.capability();
         if ((label != null && label.startsWith("stock/")) || (!"game:craft".equals(capability) && !"game:obtain".equals(capability))) {
            return null;
         }

         JsonObject args = step.arguments();
         String id = args.has("item") ? args.get("item").getAsString().trim() : "";
         String item = id.contains(":") ? id : "minecraft:" + id;
         Identifier key = Identifier.tryParse(item);
         if (id.isEmpty() || key == null || BuiltInRegistries.ITEM.getOptional(key).isEmpty()) {
            return null;
         }

         int count = args.has("count") ? Math.max(1, args.get("count").getAsInt()) : 1;
         boolean additional = args.has("additional") && args.get("additional").getAsBoolean();
         int target = additional ? held(item) + count : count;
         AiCraftPlanner.CraftPlan plan = plan(item, target);
         Map<String, Integer> missing = new LinkedHashMap<>(plan.missing());
         if (plan.requiresTable() && held("minecraft:crafting_table") == 0) {
            missing.putIfAbsent("minecraft:crafting_table", 1);
         }

         if (!missing.isEmpty()) {
            for (BlockPos box : dryBoxes()) {
               LOG.info("shulker stock: {} x{} lacks {}; taking it from the box at {}", item, target, missing, box.toShortString());
               JsonObject take = new JsonObject();
               take.addProperty("action", "withdraw");
               take.addProperty("x", box.getX());
               take.addProperty("y", box.getY());
               take.addProperty("z", box.getZ());
               JsonArray items = new JsonArray();
               missing.keySet().forEach(items::add);
               take.add("items", items);
               take.addProperty("navigate", true);
               take.addProperty("timeoutSeconds", 60);
               assistant.auditRunStep(
                  generation,
                  session,
                  new AiIntents.IntentStep("take materials from a shulker box", "game:container", take, "take " + missing.keySet() + " from the shulker box at " + box.toShortString()),
                  "stock/withdraw"
               );
               plan = plan(item, target);
               missing = new LinkedHashMap<>(plan.missing());
               boolean table = plan.requiresTable() && held("minecraft:crafting_table") == 0;
               if (missing.isEmpty() && !table) {
                  break;
               }

               if (table) {
                  missing.putIfAbsent("minecraft:crafting_table", 1);
               }
            }
         }

         missing.remove("minecraft:crafting_table");
         if (missing.isEmpty()) {
            if ("game:obtain".equals(capability)) {
               JsonObject craft = new JsonObject();
               craft.addProperty("item", item);
               craft.addProperty("count", target);
               LOG.info("shulker stock: everything for {} x{} is at hand; crafting instead of gathering", item, target);
               return assistant.auditRunStep(
                  generation,
                  session,
                  new AiIntents.IntentStep("craft " + item, "game:craft", craft, "craft " + target + " " + item),
                  "stock/craft"
               );
            }

            return null;
         }

         if ("game:obtain".equals(capability) && overWater()) {
            JsonObject refused = new JsonObject();
            refused.addProperty("status", "FAILED");
            refused.addProperty(
               "message",
               "Not gathering here: this base stands over water. Missing for " + item + ": " + missing
                  + " (none of it in the shulker boxes nearby). Bring it, then craft again."
            );
            LOG.info("shulker stock: refused to gather {} over water, missing {}", item, missing);
            return refused;
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw e;
      } catch (CancellationException e) {
         throw e;
      } catch (Throwable e) {
         LOG.warn("shulker stock: left the step as it was: {}", e.toString());
      }

      return null;
   }

   private static AiCraftPlanner.CraftPlan plan(String item, int target) throws Exception {
      return onClient(() -> AiCraftPlanner.plan(AiCraft.buildRecipeGraph(), item, target));
   }

   private static int held(String item) throws Exception {
      Item it = BuiltInRegistries.ITEM.getValue(Identifier.parse(item));
      return onClient(() -> {
         LocalPlayer p = Minecraft.getInstance().player;
         int n = 0;
         if (p != null) {
            for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
               if (p.getInventory().getItem(i).is(it)) {
                  n += p.getInventory().getItem(i).getCount();
               }
            }
         }

         return n;
      });
   }

   private static List<BlockPos> dryBoxes() throws Exception {
      return onClient(() -> {
         Minecraft mc = Minecraft.getInstance();
         List<BlockPos> out = new ArrayList<>();
         if (mc.player == null || mc.level == null) {
            return out;
         }

         BlockPos at = mc.player.blockPosition();
         for (int cx = at.getX() - REACH >> 4; cx <= at.getX() + REACH >> 4; cx++) {
            for (int cz = at.getZ() - REACH >> 4; cz <= at.getZ() + REACH >> 4; cz++) {
               if (!mc.level.hasChunk(cx, cz)) {
                  continue;
               }

               for (Map.Entry<BlockPos, BlockEntity> entry : mc.level.getChunk(cx, cz).getBlockEntities().entrySet()) {
                  BlockPos p = entry.getKey();
                  if (Math.abs(p.getX() - at.getX()) <= REACH
                     && Math.abs(p.getZ() - at.getZ()) <= REACH
                     && mc.level.getBlockState(p).getBlock() instanceof ShulkerBoxBlock
                     && drySide(mc, p)) {
                     out.add(p.immutable());
                  }
               }
            }
         }

         out.sort(Comparator.comparingDouble(p -> p.distSqr(at)));
         return out;
      });
   }

   private static boolean drySide(Minecraft mc, BlockPos box) {
      for (Direction d : Direction.Plane.HORIZONTAL) {
         BlockPos c = box.relative(d);
         BlockPos floor = c.below();
         if (mc.level.getFluidState(floor).isEmpty()
            && mc.level.getBlockState(floor).isFaceSturdy(mc.level, floor, Direction.UP)
            && mc.level.getFluidState(c).isEmpty()
            && mc.level.getFluidState(c.above()).isEmpty()
            && mc.level.getBlockState(c).getCollisionShape(mc.level, c).isEmpty()
            && mc.level.getBlockState(c.above()).getCollisionShape(mc.level, c.above()).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   private static boolean overWater() throws Exception {
      return onClient(() -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player == null || mc.level == null) {
            return false;
         }

         BlockPos at = mc.player.blockPosition();
         int water = 0;
         int all = 0;

         for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
               for (int dy = 1; dy <= 4; dy++) {
                  all++;
                  if (!mc.level.getFluidState(at.offset(dx, -dy, dz)).isEmpty()) {
                     water++;
                  }
               }
            }
         }

         return water * 4 >= all;
      });
   }

   private static <T> T onClient(Supplier<T> work) throws Exception {
      Minecraft mc = Minecraft.getInstance();
      return mc.isSameThread() ? work.get() : mc.submit(work).get();
   }
}
