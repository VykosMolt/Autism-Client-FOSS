package autismclient.modules;

import autismclient.util.AutismLiteVariant;
import autismclient.util.bK;
import autismclient.util.cA;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class NameTagsRenderer {
   private static final Minecraft mc = Minecraft.getInstance();
   private static final int init_I = 10;
   private static final Matrix4f SCRATCH_MATRIX = new Matrix4f();
   private static final Vector4f SCRATCH_VECTOR = new Vector4f();
   private static final Int2ObjectOpenHashMap<NameTagsRenderer.Inner_StreamUtil> tagCache = new Int2ObjectOpenHashMap<>();
   private static final List<ItemEntity> itemEntityBuffer = new ArrayList<>();
   private static final List<NameTagsRenderer.Inner_BufferHelper> load_List = new ArrayList<>();
   private static List<ItemEntity> read_List = List.of();
   private static Object lastLevel;
   private static long frameCounter;
   private static long lastTick = -9223372036854775808L;
   private static int lastRevision = -1;
   private static int lastFontEpoch = -1;
   private static int lastAccent;
   private static boolean vanillaFont;
   private static boolean lastVanillaFont;
   private static boolean read_Z = true;
   private static int update_I = -1;
   private static int sync_I = -1;
   private static int flush_I = -5592406;
   private static int close_I = -1879048192;
   private static int open = -1;
   private static boolean enabled;
   private static boolean showPlayers;
   private static boolean showMobs;
   private static boolean showItems;
   private static boolean hideVanilla;
   private static double maxDistanceSq;
   private static double itemsMaxDistSq;
   private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private static final int poll = 12;
   private static final float init_F = 0.75F;
   private static final int push = 1;
   private static final int pop = 14;
   private static final int tick = 2;
   private static final Map<String, String> ENCHANT_ABBREVIATIONS = Map.ofEntries(
      Map.entry("protection", "Prot"),
      Map.entry("fire_protection", "Fire"),
      Map.entry("blast_protection", "Blast"),
      Map.entry("projectile_protection", "Proj"),
      Map.entry("feather_falling", "FF"),
      Map.entry("thorns", "Thorn"),
      Map.entry("respiration", "Resp"),
      Map.entry("aqua_affinity", "Aqua"),
      Map.entry("depth_strider", "DS"),
      Map.entry("frost_walker", "FW"),
      Map.entry("soul_speed", "Soul"),
      Map.entry("swift_sneak", "Sneak"),
      Map.entry("unbreaking", "Unb"),
      Map.entry("mending", "Mend")
   );

   private NameTagsRenderer() {
   }

   public static void init(GuiGraphicsExtractor context) {
      if (!aD.init_Z() && (AutismLiteVariant.enabled() || autismclient.e.PathHelper.init())) {
         if (mc == null || mc.level == null || mc.player == null) {
            reset();
         } else if (!mc.gui.hud.isHidden()) {
            Module module = al.init_Module_Str("nametags");
            if (module != null && module.isEnabled()) {
               long perf = bK.load_J();

               try {
                  init(context, module);
               } finally {
                  bK.init("modules.nametags", perf);
               }
            } else {
               reset();
            }
         }
      }
   }

   static void reset() {
      tagCache.clear();
      itemEntityBuffer.clear();
      load_List.clear();
      read_List = List.of();
      lastTick = -9223372036854775808L;
      update_I = -1;
      lastLevel = null;
   }

   private static void init(GuiGraphicsExtractor context, Module module) {
      if (WorldProjectionHelper.init(SCRATCH_MATRIX)) {
         if (lastLevel != mc.level) {
            reset();
            lastLevel = mc.level;
         }

         long frame = ++frameCounter;
         long tick = mc.level.getGameTime();
         int revision = al.init_I();
         int fontEpoch = autismclient.gui.sdf.MsdfTextRenderer.cacheGeneration();
         boolean players = module.bool("players");
         boolean mobs = module.bool("mobs");
         boolean items = module.bool("items");
         if (players || mobs || items) {
            boolean showHealth = module.bool("show-health");
            boolean showDistance = module.bool("show-distance");
            boolean showArmor = module.bool("show-armor");
            boolean showHeldItem = module.bool("show-held-item");
            boolean showEnchantments = module.bool("show-enchantments");
            if (update_I != revision) {
               vanillaFont = "Vanilla".equalsIgnoreCase(module.value("font"));
               read_Z = module.bool("pill-background");
               sync_I = am.init_I_Module(module, "distance-color", -1);
               flush_I = am.init_I_Module(module, "enchantment-color", -5592406);
               close_I = am.init_I_Module(module, "background-color", -1879048192);
               update_I = revision;
            }

            boolean distanceScale = module.bool("distance-scale");
            boolean groupItems = module.bool("group-items");
            double baseScale = module.decimal("scale");
            double maxDist = module.decimal("max-distance");
            double itemsMaxDist = module.decimal("items-max-distance");
            double groupRadius = module.decimal("group-radius");
            Vec3 camPos = WorldProjectionHelper.init();
            NameTagsRenderer.Inner_TaskQueue projection = new NameTagsRenderer.Inner_TaskQueue(camPos, SCRATCH_MATRIX, cA.init_I(), cA.load_I());
            float tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            Entity self = mc.player;
            itemEntityBuffer.clear();

            for (Entity entity : mc.level.entitiesForRendering()) {
               try {
                  if (entity != self && !AntiBotTracker.shouldIgnoreEntity(entity)) {
                     boolean friend = TeamsModule.isFriend(entity);
                     if (!friend || TeamsModule.option("nametags")) {
                        double x = Mth.lerp(tickDelta, entity.xOld, entity.getX());
                        double y = Mth.lerp(tickDelta, entity.yOld, entity.getY());
                        double z = Mth.lerp(tickDelta, entity.zOld, entity.getZ());
                        double dist = Math.sqrt(distanceSq(camPos, x, y, z));
                        if (entity instanceof ItemEntity item) {
                           if (items && (itemsMaxDist <= 0.0 || dist <= itemsMaxDist)) {
                              itemEntityBuffer.add(item);
                           }
                        } else if (!(maxDist > 0.0) || !(dist > maxDist)) {
                           boolean isPlayer = entity instanceof Player;
                           if (isPlayer ? players : mobs && entity instanceof LivingEntity) {
                              float[] screen = project(x, y + entity.getDimensions(entity.getPose()).height() + 0.45, z, projection);
                              if (screen != null) {
                                 NameTagsRenderer.Inner_StreamUtil tag = tagCache.get(entity.getId());
                                 if (tag == null || tag.entity != entity) {
                                    tag = new NameTagsRenderer.Inner_StreamUtil(entity);
                                    tagCache.put(entity.getId(), tag);
                                 }

                                 tag.frame = frame;
                                 tag.init(tick, revision, fontEpoch, showHealth, showDistance, dist, friend, showArmor, showHeldItem, showEnchantments);
                                 if (!(tag.init_aj_ConfigStore.width <= 0.0F)) {
                                    float tagScale = init(baseScale, distanceScale, dist);
                                    drawTag(context, screen[0], screen[1], tag, tagScale);
                                 }
                              }
                           }
                        }
                     }
                  }
               } catch (Throwable var46) {
               }
            }

            if (items && !itemEntityBuffer.isEmpty()) {
               refreshCache(tick, revision, fontEpoch, groupItems, groupRadius);
               init_V_GuiGraphicsExtract(context, baseScale, distanceScale, camPos, tickDelta, projection);
            } else {
               load_List.clear();
               read_List = List.of();
            }

            itemEntityBuffer.clear();
            if ((frame & 63L) == 0L) {
               tagCache.values().removeIf(tagx -> frame - tagx.frame > 20L);
            }
         }
      }
   }

   private static void load() {
      int revision = al.init_I();
      if (revision != open) {
         open = revision;
         Module module = al.init_Module_Str("nametags");
         enabled = module != null && module.isEnabled();
         showPlayers = enabled && module.bool("players");
         showMobs = enabled && module.bool("mobs");
         showItems = enabled && module.bool("items");
         hideVanilla = enabled && module.bool("hide-vanilla");
         double maxDist = enabled ? module.decimal("max-distance") : 0.0;
         maxDistanceSq = maxDist > 0.0 ? maxDist * maxDist : 0.0;
         double itemsMaxDist = enabled ? module.decimal("items-max-distance") : 0.0;
         itemsMaxDistSq = itemsMaxDist > 0.0 ? itemsMaxDist * itemsMaxDist : 0.0;
      }
   }

   public static boolean overridesNameTag(Entity entity) {
      load();
      if (!enabled) {
         return false;
      }

      if (aD.init_Z()) {
         return false;
      }

      if (!AutismLiteVariant.enabled() && !autismclient.e.TextFormat.init()) {
         return false;
      }

      if (mc == null || mc.player == null || entity == null || entity == mc.player) {
         return false;
      }

      if (AntiBotTracker.isBot(entity)) {
         return false;
      }

      if (TeamsModule.isFriend(entity) && !TeamsModule.option("nametags")) {
         return false;
      }

      if (entity instanceof ItemEntity) {
         if (!showItems) {
            return false;
         }
      } else if (entity instanceof Player) {
         if (!showPlayers) {
            return false;
         }
      } else if (!(entity instanceof LivingEntity) || !showMobs) {
         return false;
      }

      if (hideVanilla) {
         return true;
      }

      double maxDistSq = entity instanceof ItemEntity ? itemsMaxDistSq : maxDistanceSq;
      if (maxDistSq > 0.0) {
         Camera cam = mc.gameRenderer.mainCamera();
         if (cam != null && entity.distanceToSqr(cam.position()) > maxDistSq) {
            return false;
         }
      }

      return true;
   }

   private static Component labelFor(Entity entity) {
      Component label;
      if (entity instanceof Player player) {
         String accountName = player.getGameProfile().name();
         if (!PlayerDisplayNameParser.init(accountName)) {
            return Component.empty();
         }

         PlayerInfo info = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(player.getUUID());
         PlayerTeam team = player.getTeam();
         TextColor teamColor = team == null ? null : team.getColor().map(TeamColor::textColor).orElse(null);
         label = PlayerDisplayNameParser.displayName(accountName, info == null ? null : info.getTabListDisplayName(), teamColor);
      } else {
         label = entity.getDisplayName().copy();
      }

      return as.init_Z_33() ? as.load(label) : label;
   }

   private static void refreshCache(long tick, int revision, int fontEpoch, boolean group, double groupRadius) {
      int accent = autismclient.util.UiColorPalette.accent();
      if (tick != lastTick || revision != lastRevision || fontEpoch != lastFontEpoch || lastVanillaFont != vanillaFont || accent != lastAccent || !read_List.equals(itemEntityBuffer)) {
         Map<List<Object>, List<NameTagsRenderer.Inner_ConfigStore>> previous = new HashMap<>();
         if (revision == lastRevision && fontEpoch == lastFontEpoch && lastVanillaFont == vanillaFont && accent == lastAccent) {
            for (NameTagsRenderer.Inner_BufferHelper old : load_List) {
               previous.put(old.read, old.load);
            }
         }

         lastTick = tick;
         lastRevision = revision;
         lastFontEpoch = fontEpoch;
         lastVanillaFont = vanillaFont;
         lastAccent = accent;
         read_List = List.copyOf(itemEntityBuffer);
         load_List.clear();
         List<ItemEntity> itemEntities = itemEntityBuffer;
         boolean[] visited = new boolean[itemEntities.size()];
         double r2 = groupRadius * groupRadius;
         Map<NameTagsRenderer.Inner_CachePool, List<Integer>> cells = new HashMap<>();
         if (group) {
            for (int i = 0; i < itemEntities.size(); i++) {
               cells.computeIfAbsent(NameTagsRenderer.Inner_CachePool.init(itemEntities.get(i), groupRadius), key -> new ArrayList<>()).add(i);
            }
         }

         Style countStyle = Style.EMPTY.withColor(TextColor.fromRgb(autismclient.util.UiColorPalette.accent() & 16777215));

         for (int i = 0; i < itemEntities.size(); i++) {
            try {
               if (!visited[i]) {
                  ItemEntity base = itemEntities.get(i);
                  visited[i] = true;
                  List<ItemEntity> cluster = new ArrayList<>();
                  cluster.add(base);
                  if (group) {
                     NameTagsRenderer.Inner_CachePool center = NameTagsRenderer.Inner_CachePool.init(base, groupRadius);
                     List<Integer> nearby = new ArrayList<>();

                     for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                           for (int dz = -1; dz <= 1; dz++) {
                              List<Integer> candidates = cells.get(new NameTagsRenderer.Inner_CachePool(center.init + dx, center.load + dy, center.read + dz));
                              if (candidates != null) {
                                 for (int j : candidates) {
                                    if (j > i && !visited[j] && base.distanceToSqr(itemEntities.get(j)) <= r2) {
                                       nearby.add(j);
                                    }
                                 }
                              }
                           }
                        }
                     }

                     nearby.sort(Integer::compare);

                     for (int j : nearby) {
                        visited[j] = true;
                        cluster.add(itemEntities.get(j));
                     }
                  }

                  Map<String, NameTagsRenderer.Inner_DataUtil> agg = new LinkedHashMap<>();

                  for (ItemEntity ie : cluster) {
                     ItemStack st = ie.getItem();
                     Component name = st.getHoverName();
                     NameTagsRenderer.Inner_DataUtil var10000 = agg.computeIfAbsent(name.getString(), kx -> new NameTagsRenderer.Inner_DataUtil(name));
                     var10000.count = var10000.count + st.getCount();
                  }

                  List<NameTagsRenderer.Inner_DataUtil> aggs = new ArrayList<>(agg.values());
                  aggs.sort((ax, b) -> Integer.compare(b.count, ax.count));
                  ArrayList<Object> parts = new ArrayList<>(aggs.size() * 2);

                  for (NameTagsRenderer.Inner_DataUtil a : aggs) {
                     parts.add(a.name);
                     parts.add(a.count);
                  }

                  List<Object> content = List.copyOf(parts);
                  List<NameTagsRenderer.Inner_ConfigStore> cached = previous.get(content);
                  if (cached != null) {
                     load_List.add(new NameTagsRenderer.Inner_BufferHelper(List.copyOf(cluster), cached, content));
                  } else {
                     List<NameTagsRenderer.Inner_ConfigStore> lines = new ArrayList<>();
                     int max = Math.min(aggs.size(), 6);

                     for (int k = 0; k < max; k++) {
                        NameTagsRenderer.Inner_DataUtil a = aggs.get(k);
                        lines.add(NameTagsRenderer.Inner_ConfigStore.init(a.name.copy().append(Component.literal(" ×" + a.count).withStyle(countStyle))));
                     }

                     if (aggs.size() > max) {
                        lines.add(NameTagsRenderer.Inner_ConfigStore.init(Component.literal("+" + (aggs.size() - max) + " more")));
                     }

                     load_List.add(new NameTagsRenderer.Inner_BufferHelper(List.copyOf(cluster), List.copyOf(lines), content));
                  }
               }
            } catch (Throwable var27) {
            }
         }
      }
   }

   private static void init_V_GuiGraphicsExtract(
      GuiGraphicsExtractor context, double baseScale, boolean distanceScale, Vec3 camPos, float tickDelta, NameTagsRenderer.Inner_TaskQueue projection
   ) {
      for (NameTagsRenderer.Inner_BufferHelper group : load_List) {
         double x = 0.0;
         double y = 0.0;
         double z = 0.0;
         int count = 0;

         for (ItemEntity item : group.init) {
            if (!item.isRemoved()) {
               x += Mth.lerp(tickDelta, item.xOld, item.getX());
               y += Mth.lerp(tickDelta, item.yOld, item.getY());
               z += Mth.lerp(tickDelta, item.zOld, item.getZ());
               count++;
            }
         }

         if (count != 0) {
            x /= count;
            y = y / count + 0.5;
            z /= count;
            float[] screen = project(x, y, z, projection);
            if (screen != null) {
               double dist = Math.sqrt(distanceSq(camPos, x, y, z));
               drawLines(context, screen[0], screen[1], group.load, -1, init(baseScale, distanceScale, dist));
            }
         }
      }
   }

   static float init(double baseScale, boolean distanceScale, double dist) {
      if (!distanceScale) {
         return Math.max(0.05F, (float)baseScale);
      }

      double t = Mth.clamp((dist - 12.0) / 36.0, 0.0, 1.0);
      double eased = t * t * (3.0 - 2.0 * t);
      return Math.max(0.05F, (float)(baseScale * (1.0 - 0.2 * eased)));
   }

   private static int init(LivingEntity living) {
      float fraction = Mth.clamp(living.getHealth() / Math.max(1.0F, living.getMaxHealth()), 0.0F, 1.0F);
      return 0xFF000000 | Color.HSBtoRGB(fraction / 3.0F, 0.9F, 1.0F) & 16777215;
   }

   private static int lineHeight() {
      return vanillaFont
         ? Math.max(10, 11)
         : Math.max(
            10,
            (int)Math.ceil(
                  autismclient.gui.sdf.MsdfTextRenderer.glyphBoxHeight(
                     autismclient.gui.sdf.MsdfFontFace.BODY,
                     autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(autismclient.gui.sdf.MsdfFontFace.BODY)
                  )
               )
               + 2
         );
   }

   private static void drawLines(
      GuiGraphicsExtractor context, float screenX, float screenY, List<NameTagsRenderer.Inner_ConfigStore> lines, int color, float scale
   ) {
      if (!lines.isEmpty()) {
         context.pose().pushMatrix();

         try {
            context.pose().translate(screenX, screenY);
            context.pose().scale(scale, scale);
            int lineH = lineHeight();
            int ty = -lines.size() * lineH - 2;
            float[] widths = new float[lines.size()];

            for (int i = 0; i < widths.length; i++) {
               widths[i] = lines.get(i).width + 4.0F;
            }

            autismclient.gui.f.PacketUtil.init(context, 0.0F, ty, widths, lineH, read_Z ? lineH * 0.5F : 0.0F, close_I);

            for (NameTagsRenderer.Inner_ConfigStore line : lines) {
               line.init(context, -line.width * 0.5F, ty, lineH, color);
               ty += lineH;
            }
         } finally {
            context.pose().popMatrix();
         }
      }
   }

   private static void drawStack(GuiGraphicsExtractor context, ItemStack stack, int x, int y) {
      context.pose().pushMatrix();

      try {
         context.pose().translate(x, y);
         context.pose().scale(0.75F, 0.75F);
         context.item(stack, 0, 0);
         context.itemDecorations(mc.font, stack, 0, 0);
      } finally {
         context.pose().popMatrix();
      }
   }

   private static void drawTag(GuiGraphicsExtractor context, float screenX, float screenY, NameTagsRenderer.Inner_StreamUtil tag, float scale) {
      List<ItemStack> gear = tag.gear;
      int lineH = lineHeight();
      int gearRowW = gear.isEmpty() ? 0 : gear.size() * 12 + (gear.size() - 1) * 1;
      int totalH = lineH + (gear.isEmpty() ? 0 : 14) + (tag.write_aj_ConfigStore == null ? 0 : lineH);
      context.pose().pushMatrix();

      try {
         context.pose().translate(screenX, screenY);
         context.pose().scale(scale, scale);
         int ty = -totalH - 2;
         int rows = 1 + (gear.isEmpty() ? 0 : 1) + (tag.write_aj_ConfigStore == null ? 0 : 1);
         int row = 0;
         float[] widths = new float[rows];
         float[] heights = new float[rows];
         widths[row] = tag.init() + 4.0F;
         heights[row++] = lineH;
         if (!gear.isEmpty()) {
            widths[row] = gearRowW + 4;
            heights[row++] = 14.0F;
         }

         if (tag.write_aj_ConfigStore != null) {
            widths[row] = tag.write_aj_ConfigStore.width + 4.0F;
            heights[row] = lineH;
         }

         autismclient.gui.f.PacketUtil.init(context, 0.0F, ty, widths, heights, read_Z ? lineH * 0.5F : 0.0F, close_I);
         float textX = -tag.init() * 0.5F;
         tag.init_aj_ConfigStore.init(context, textX, ty, lineH, -1);
         textX += tag.init_aj_ConfigStore.width;
         if (tag.hpSuffix != null) {
            tag.hpSuffix.init(context, textX, ty, lineH, tag.sync);
            textX += tag.hpSuffix.width;
         }

         if (tag.distanceSuffix != null) {
            tag.distanceSuffix.init(context, textX, ty, lineH, sync_I);
         }

         ty += lineH;
         if (!gear.isEmpty()) {
            context.pose().pushMatrix();

            try {
               context.pose().translate(-gearRowW * 0.5F, 0.0F);
               int ix = 0;

               for (ItemStack stack : gear) {
                  drawStack(context, stack, ix, ty + 1);
                  ix += 13;
               }
            } finally {
               context.pose().popMatrix();
            }

            ty += 14;
         }

         if (tag.write_aj_ConfigStore != null) {
            tag.write_aj_ConfigStore.init(context, -tag.write_aj_ConfigStore.width * 0.5F, ty, lineH, flush_I);
         }
      } finally {
         context.pose().popMatrix();
      }
   }

   private static String init(List<ItemStack> armor, int maxWidth) {
      Map<String, Integer> maxLevelByPath = new LinkedHashMap<>();

      for (ItemStack stack : armor) {
         for (Entry<Holder<Enchantment>> entry : stack.getEnchantments().entrySet()) {
            String path = entry.getKey().unwrapKey().map(key -> key.identifier().getPath()).orElseGet(() -> {
               String name = entry.getKey().getRegisteredName();
               return name.substring(name.indexOf(58) + 1);
            });
            maxLevelByPath.merge(path, entry.getValue(), Math::max);
         }
      }

      if (maxLevelByPath.isEmpty()) {
         return "";
      }

      List<java.util.Map.Entry<String, Integer>> entries = new ArrayList<>(maxLevelByPath.entrySet());
      entries.sort((a, b) -> {
         int byLevel = Integer.compare(b.getValue(), a.getValue());
         return byLevel != 0 ? byLevel : a.getKey().compareTo(b.getKey());
      });
      StringBuilder line = new StringBuilder();
      int shown = 0;

      for (java.util.Map.Entry<String, Integer> entry : entries) {
         String part = abbreviate(entry.getKey()) + " " + init(entry.getValue());
         String candidate = line.length() == 0 ? part : line + "  " + part;
         if (shown > 0 && textWidth(candidate) > maxWidth) {
            break;
         }

         line = new StringBuilder(candidate);
         shown++;
      }

      int remaining = entries.size() - shown;
      if (remaining > 0) {
         line.append(" +").append(remaining);
      }

      return line.toString();
   }

   static String abbreviate(String path) {
      String mapped = ENCHANT_ABBREVIATIONS.get(path);
      if (mapped != null) {
         return mapped;
      }

      String[] words = path.split("_");
      StringBuilder out = new StringBuilder();
      if (words.length == 1) {
         out.append(Character.toUpperCase(path.charAt(0))).append(path, 1, Math.min(4, path.length()));
      } else {
         for (String word : words) {
            if (!word.isEmpty()) {
               out.append(Character.toUpperCase(word.charAt(0)));
            }
         }
      }

      return out.length() <= 5 ? out.toString() : out.substring(0, 5);
   }

   static String init(int level) {
      String var10000;
      switch (level) {
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
         default:
            var10000 = Integer.toString(level);
      }

      return var10000;
   }

   private static float textWidth(String text) {
      return !vanillaFont && autismclient.gui.sdf.MsdfTextRenderer.canRender(autismclient.gui.sdf.MsdfFontFace.BODY, text)
         ? autismclient.gui.sdf.MsdfTextRenderer.measureWidthFloat(
            autismclient.gui.sdf.MsdfFontFace.BODY,
            text,
            autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(autismclient.gui.sdf.MsdfFontFace.BODY)
         )
         : mc.font.width(text);
   }

   private static float[] project(double worldX, double worldY, double worldZ, NameTagsRenderer.Inner_TaskQueue p) {
      Vec3 cam = p.camPos();
      Vector4f v = SCRATCH_VECTOR.set((float)(worldX - cam.x), (float)(worldY - cam.y), (float)(worldZ - cam.z), 1.0F);
      p.projection().transform(v);
      if (v.w <= 0.05F) {
         return null;
      } else {
         float ndcX = v.x / v.w;
         float ndcY = v.y / v.w;
         if (!Float.isNaN(ndcX) && !Float.isNaN(ndcY) && !Float.isInfinite(ndcX) && !Float.isInfinite(ndcY)) {
            float sx = (ndcX * 0.5F + 0.5F) * p.init_I();
            float sy = (0.5F - ndcY * 0.5F) * p.load();
            return new float[]{sx, sy};
         } else {
            return null;
         }
      }
   }

   private static double distanceSq(Vec3 cam, double x, double y, double z) {
      double dx = x - cam.x;
      double dy = y - cam.y;
      double dz = z - cam.z;
      return dx * dx + dy * dy + dz * dz;
   }

   private record Inner_BufferHelper(List<ItemEntity> init, List<NameTagsRenderer.Inner_ConfigStore> load, List<Object> read) {
   }

   private record Inner_CachePool(int init, int load, int read) {
      static NameTagsRenderer.Inner_CachePool init(ItemEntity item, double radius) {
         return new NameTagsRenderer.Inner_CachePool(Mth.floor(item.getX() / radius), Mth.floor(item.getY() / radius), Mth.floor(item.getZ() / radius));
      }
   }

   private static final class Inner_ConfigStore {
      final FormattedCharSequence vanilla;
      final List<NameTagsRenderer.Inner_NetHelper> runs;
      final float width;

      private Inner_ConfigStore(FormattedCharSequence vanilla, List<NameTagsRenderer.Inner_NetHelper> runs, float width) {
         this.vanilla = vanilla;
         this.runs = runs;
         this.width = width;
      }

      static NameTagsRenderer.Inner_ConfigStore init(Component component) {
         FormattedCharSequence visual = component.getVisualOrderText();
         if (NameTagsRenderer.vanillaFont) {
            return new NameTagsRenderer.Inner_ConfigStore(visual, List.of(), NameTagsRenderer.mc.font.width(visual));
         }

         List<NameTagsRenderer.Inner_NetHelper> runs = new ArrayList<>();
         StringBuilder text = new StringBuilder();
         Style[] current = new Style[]{null};
         boolean supported = visual.accept((index, style, codepoint) -> {
            if (!style.isObfuscated() && !style.isUnderlined() && !style.isStrikethrough() && style.getFont().equals(Style.EMPTY.getFont())) {
               if (!style.equals(current[0])) {
                  if (!text.isEmpty()) {
                     runs.add(NameTagsRenderer.Inner_NetHelper.init(text.toString(), current[0]));
                  }

                  text.setLength(0);
                  current[0] = style;
               }

               text.appendCodePoint(codepoint);
               return true;
            } else {
               return false;
            }
         });
         if (!text.isEmpty()) {
            runs.add(NameTagsRenderer.Inner_NetHelper.init(text.toString(), current[0]));
         }

         if (supported && !runs.stream().anyMatch(runx -> !autismclient.gui.sdf.MsdfTextRenderer.canRender(runx.face, runx.text))) {
            float width = 0.0F;

            for (NameTagsRenderer.Inner_NetHelper run : runs) {
               width += run.width;
            }

            return new NameTagsRenderer.Inner_ConfigStore(visual, List.copyOf(runs), width);
         } else {
            return new NameTagsRenderer.Inner_ConfigStore(visual, List.of(), NameTagsRenderer.mc.font.width(visual));
         }
      }

      void init(GuiGraphicsExtractor context, float x, float rowTop, int rowHeight, int color) {
         if ((color & 0xFF000000) != 0) {
            if (this.runs.isEmpty()) {
               context.pose().pushMatrix();

               try {
                  context.pose().translate(x, rowTop + (rowHeight - 9) * 0.5F);
                  context.text(NameTagsRenderer.mc.font, this.vanilla, 0, 0, color, true);
               } finally {
                  context.pose().popMatrix();
               }
            } else {
               float size = autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(autismclient.gui.sdf.MsdfFontFace.BODY);

               for (NameTagsRenderer.Inner_NetHelper run : this.runs) {
                  int tint = run.color == null ? color : color & 0xFF000000 | run.color;
                  context.pose().pushMatrix();

                  try {
                     context.pose().translate(x, rowTop);
                     if (run.italic) {
                        context.pose().shearX(-0.2F);
                     }

                     autismclient.gui.sdf.MsdfTextRenderer.draw(
                        context,
                        run.face,
                        run.text,
                        0.0F,
                        autismclient.gui.sdf.MsdfTextRenderer.alignToVanillaTop(
                           run.face, autismclient.gui.sdf.MsdfTextRenderer.centerVertically(run.face, 0.0F, rowHeight, size), size
                        ),
                        size,
                        tint,
                        true
                     );
                  } finally {
                     context.pose().popMatrix();
                  }

                  x += run.width;
               }
            }
         }
      }
   }

   private static final class Inner_DataUtil {
      final Component name;
      int count;

      Inner_DataUtil(Component name) {
         this.name = name;
      }
   }

   private record Inner_NetHelper(String text, autismclient.gui.sdf.MsdfFontFace face, Integer color, float width, boolean italic) {
      static NameTagsRenderer.Inner_NetHelper init(String text, Style style) {
         autismclient.gui.sdf.MsdfFontFace face = style.isBold() ? autismclient.gui.sdf.MsdfFontFace.TITLE : autismclient.gui.sdf.MsdfFontFace.BODY;
         TextColor color = style.getColor();
         return new NameTagsRenderer.Inner_NetHelper(
            text,
            face,
            color == null ? null : color.getValue(),
            autismclient.gui.sdf.MsdfTextRenderer.measureWidthFloat(
               face, text, autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(autismclient.gui.sdf.MsdfFontFace.BODY)
            ),
            style.isItalic()
         );
      }
   }

   private static final class Inner_StreamUtil {
      final Entity entity;
      long init_J = -9223372036854775808L;
      long frame;
      int settings = -1;
      int epoch = -1;
      int meters = -1;
      int hp = -1;
      int update;
      int sync;
      boolean init_Z;
      Component label;
      NameTagsRenderer.Inner_ConfigStore init_aj_ConfigStore;
      NameTagsRenderer.Inner_ConfigStore hpSuffix;
      NameTagsRenderer.Inner_ConfigStore distanceSuffix;
      NameTagsRenderer.Inner_ConfigStore write_aj_ConfigStore;
      List<ItemStack> gear = List.of();
      float init_F = -1.0F;

      Inner_StreamUtil(Entity entity) {
         this.entity = entity;
      }

      void init(
         long now,
         int settings,
         int epoch,
         boolean showHealth,
         boolean showDistance,
         double dist,
         boolean friend,
         boolean showArmor,
         boolean showHeldItem,
         boolean showEnchantments
      ) {
         int meters = showDistance ? (int)dist : -1;
         int tint = friend ? TeamsModule.friendColor() : 0;
         boolean fontChanged = this.epoch != epoch || this.init_Z != NameTagsRenderer.vanillaFont;
         boolean refreshGear = this.init_J != now || this.settings != settings || fontChanged;
         if (refreshGear || meters != this.meters || tint != this.update) {
            Component label = NameTagsRenderer.labelFor(this.entity);
            if (friend) {
               label = label.copy().withStyle(style -> style.withColor(TextColor.fromRgb(tint & 16777215)));
            }

            if (fontChanged || !label.equals(this.label)) {
               this.label = label;
               this.init_aj_ConfigStore = NameTagsRenderer.Inner_ConfigStore.init(label);
            }

            int hp = showHealth && this.entity instanceof LivingEntity living ? (int)Math.ceil(living.getHealth()) : -1;
            if (fontChanged || hp != this.hp) {
               this.hpSuffix = hp < 0 ? null : NameTagsRenderer.Inner_ConfigStore.init(Component.literal("  " + hp + "HP"));
            }

            this.sync = hp < 0 ? 0 : NameTagsRenderer.init((LivingEntity)this.entity);
            if (fontChanged || meters != this.meters) {
               this.distanceSuffix = meters < 0 ? null : NameTagsRenderer.Inner_ConfigStore.init(Component.literal("  " + meters + "m"));
            }

            if (this.entity instanceof Player player && refreshGear) {
               List<ItemStack> armor = new ArrayList<>(4);
               List<ItemStack> equipment = new ArrayList<>(5);
               if (showHeldItem && !player.getMainHandItem().isEmpty()) {
                  equipment.add(player.getMainHandItem());
               }

               if (showArmor) {
                  for (EquipmentSlot slot : NameTagsRenderer.ARMOR_SLOTS) {
                     ItemStack stack = player.getItemBySlot(slot);
                     if (!stack.isEmpty()) {
                        armor.add(stack);
                     }
                  }
               }

               equipment.addAll(armor);
               boolean changed = equipment.size() != this.gear.size();
               if (!changed) {
                  for (int i = 0; i < this.gear.size(); i++) {
                     if (!ItemStack.matches(equipment.get(i), this.gear.get(i))) {
                        changed = true;
                        break;
                     }
                  }
               }

               if (changed) {
                  this.gear = equipment.stream().map(ItemStack::copy).toList();
               }

               if (changed || fontChanged || this.settings != settings || this.init_F != this.init()) {
                  String line = showEnchantments ? NameTagsRenderer.init(armor, Math.max(48, (int)Math.ceil(this.init()))) : "";
                  this.write_aj_ConfigStore = line.isEmpty() ? null : NameTagsRenderer.Inner_ConfigStore.init(Component.literal(line));
                  this.init_F = this.init();
               }
            }

            this.init_J = now;
            this.settings = settings;
            this.epoch = epoch;
            this.init_Z = NameTagsRenderer.vanillaFont;
            this.meters = meters;
            this.hp = hp;
            this.update = tint;
         }
      }

      float init() {
         return this.init_aj_ConfigStore.width
            + (this.hpSuffix == null ? 0.0F : this.hpSuffix.width)
            + (this.distanceSuffix == null ? 0.0F : this.distanceSuffix.width);
      }
   }

   private record Inner_TaskQueue(Vec3 camPos, Matrix4f projection, int init_I, int load) {
   }
}
