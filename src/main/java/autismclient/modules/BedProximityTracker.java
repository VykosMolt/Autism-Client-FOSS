package autismclient.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;

final class BedProximityTracker {
   private Object init_Obj;
   private DyeColor init_DyeColor;
   private String init_Str;
   private BlockPos init_BlockPos;
   private int init_I;
   private final BedProximityTracker.Inner_CachePool init_v_CachePool = new BedProximityTracker.Inner_CachePool();
   private int load = -60;
   private List<BedProximityTracker.Inner_DataUtil> init_List = List.of();

   List<BedProximityTracker.Inner_DataUtil> init_List() {
      return this.init_List;
   }

   void init_V() {
      this.init_Obj = null;
      this.init_DyeColor = null;
      this.init_Str = null;
      this.init_BlockPos = null;
      this.init_I = 0;
      this.init_v_CachePool.init();
      this.load = -60;
      this.init_List = List.of();
   }

   static List<BedProximityTracker.Inner_DataUtil> init_List_List_D(List<BedProximityTracker.Inner_StreamUtil> people, double radius) {
      Map<String, List<BedProximityTracker.Inner_StreamUtil>> teams = new TreeMap<>();
      Set<UUID> seen = new HashSet<>();

      for (BedProximityTracker.Inner_StreamUtil person : people) {
         if (Double.isFinite(person.init_D) && person.init_D >= 0.0 && person.init_D <= radius && seen.add(person.init_UUID)) {
            teams.computeIfAbsent(person.init_Str, key -> new ArrayList<>()).add(person);
         }
      }

      List<BedProximityTracker.Inner_DataUtil> result = new ArrayList<>();

      for (Entry<String, List<BedProximityTracker.Inner_StreamUtil>> entry : teams.entrySet()) {
         BedProximityTracker.Inner_StreamUtil closest = entry.getValue()
            .stream()
            .min(Comparator.comparingDouble(BedProximityTracker.Inner_StreamUtil::init_D).thenComparing(BedProximityTracker.Inner_StreamUtil::init_UUID))
            .orElseThrow();
         result.add(new BedProximityTracker.Inner_DataUtil(entry.getKey(), closest.init_Component, closest.init_I, entry.getValue().size(), closest.init_D));
      }

      result.sort(Comparator.comparingDouble(BedProximityTracker.Inner_DataUtil::init_D).thenComparing(BedProximityTracker.Inner_DataUtil::init_Str));
      return List.copyOf(result);
   }

   void init_V_Minecraft_z(Minecraft mc, DangerModule module) {
      if (module.bool("bed-warning") && mc.level != null && mc.player != null && mc.player.isAlive() && !mc.player.isSpectator()) {
         PlayerTeam ownTeam = mc.player.getTeam();
         TextColor ownRgb = ownTeam == null ? null : ownTeam.getColor().map(TeamColor::textColor).orElse(null);
         PlayerInfo ownInfo = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(mc.player.getUUID());
         DyeColor own = TeamDyeColorResolver.load(
            mc.player.getGameProfile().name(),
            ownInfo == null ? null : ownInfo.getTabListDisplayName(),
            mc.player.getDisplayName(),
            ownRgb == null ? null : ownRgb.getValue()
         );
         String teamName = ownTeam == null ? null : ownTeam.getName();
         if (this.init_Obj != mc.level || this.init_DyeColor != own || !Objects.equals(this.init_Str, teamName)) {
            this.init_V();
            this.init_Obj = mc.level;
            this.init_DyeColor = own;
            this.init_Str = teamName;
         }

         this.init_I++;
         if (!this.init_v_CachePool.init(mc.player.getUUID(), teamName, own, this.init_I, 40)) {
            this.init_List = List.of();
         } else {
            if (this.init_BlockPos != null
               && mc.level.isLoaded(this.init_BlockPos)
               && !(mc.level.getBlockState(this.init_BlockPos).getBlock() instanceof BedBlock block && block.getColor() == own)) {
               this.init_BlockPos = null;
            }

            if (this.init_I % 20 == 1 || this.init_BlockPos == null && this.init_I % 5 == 1) {
               this.init_V_Minecraft_I(mc, module.integer("bed-scan-range"));
            }

            if (this.init_BlockPos == null) {
               this.init_List = List.of();
            } else {
               Vec3 center = Vec3.atCenterOf(this.init_BlockPos);
               if (mc.level.isLoaded(this.init_BlockPos)) {
                  BlockState state = mc.level.getBlockState(this.init_BlockPos);
                  if (state.getBlock() instanceof BedBlock) {
                     center = center.add(Vec3.atLowerCornerOf(BedBlock.getConnectedDirection(state).getUnitVec3i()).scale(0.5));
                  }
               }

               ArrayList<BedProximityTracker.Inner_StreamUtil> people = new ArrayList<>();

               for (AbstractClientPlayer player : mc.level.players()) {
                  if (player != mc.player && player.isAlive() && !player.isSpectator() && !AntiBotTracker.init_Z(player)) {
                     PlayerTeam team = player.getTeam();
                     TextColor fallback = team == null ? null : team.getColor().map(TeamColor::textColor).orElse(null);
                     PlayerInfo info = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(player.getUUID());
                     Component display = info == null ? null : info.getTabListDisplayName();
                     String account = player.getGameProfile().name();
                     DyeColor dye = TeamDyeColorResolver.load(account, display, player.getDisplayName(), fallback == null ? null : fallback.getValue());
                     if (this.init_v_CachePool.init(player.getUUID(), team == null ? null : team.getName(), dye, this.init_I, 20)
                        && (module.bool("bed-include-team") || dye != own && (team == null || team != mc.player.getTeam()))
                        && (module.bool("bed-include-friends") || !TeamsModule.init_Z_Entity_5(player) || dye == own)) {
                        int tint = PlayerDisplayNameParser.init_I(account, display, fallback);
                        String key = dye != null ? "color:" + dye.getName() : (team != null ? "team:" + team.getName() : "player:" + player.getUUID());
                        people.add(
                           new BedProximityTracker.Inner_StreamUtil(
                              player.getUUID(), key, PlayerDisplayNameParser.init_MutableComponent(account, display, fallback), tint, player.position().distanceTo(center)
                           )
                        );
                     }
                  }
               }

               List<BedProximityTracker.Inner_DataUtil> next = init_List_List_D(people, module.decimal("bed-warning-radius"));
               if (module.bool("bed-warning-sound") && this.init_I - this.load >= 60 && init_Z_List_List(this.init_List, next)) {
                  module.init_V_52();
                  this.load = this.init_I;
               }

               this.init_List = next;
            }
         }
      } else {
         this.init_V();
      }
   }

   static boolean init_Z_List_List(List<BedProximityTracker.Inner_DataUtil> previous, List<BedProximityTracker.Inner_DataUtil> next) {
      return next.stream().anyMatch(group -> previous.stream().noneMatch(old -> old.init_Str().equals(group.init_Str())));
   }

   private void init_V_Minecraft_I(Minecraft mc, int range) {
      EspChunkWorker.init_V_Obj(mc.level);
      if (this.init_BlockPos == null) {
         int radius = Math.min(am.init_I(), (range + 15) / 16);
         int cx = mc.player.chunkPosition().x();
         int cz = mc.player.chunkPosition().z();
         double closest = (double)range * range;

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               LevelChunk chunk = mc.level.getChunkSource().getChunk(cx + dx, cz + dz, ChunkStatus.FULL, false);
               if (chunk != null) {
                  for (EspChunkWorker.EspBox entry : init_List_LevelChunk_J(chunk, mc.level.getGameTime())) {
                     BlockPos at = BlockPos.containing(entry.init_Vec3());
                     BlockState state = mc.level.getBlockState(at);
                     if (state.getBlock() instanceof BedBlock block && block.getColor() == this.init_DyeColor) {
                        double distance = at.distToCenterSqr(mc.player.position());
                        if (!(distance >= closest)) {
                           closest = distance;
                           this.init_BlockPos = (state.getValue(BedBlock.PART) == BedPart.HEAD ? at.relative(BedBlock.getConnectedDirection(state)) : at)
                              .immutable();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   static List<EspChunkWorker.EspBox> init_List_LevelChunk_J(LevelChunk chunk, long time) {
      return EspChunkWorker.load
         .init(
            chunk,
            time,
            "beds",
            (c, out) -> {
               LevelChunkSection[] sections = c.getSections();

               for (int section = 0; section < sections.length; section++) {
                  LevelChunkSection blocks = sections[section];
                  if (blocks != null && !blocks.hasOnlyAir() && blocks.maybeHas(s -> s.getBlock() instanceof BedBlock)) {
                     for (int y = 0; y < 16; y++) {
                        for (int x = 0; x < 16; x++) {
                           for (int z = 0; z < 16; z++) {
                              if (blocks.getBlockState(x, y, z).getBlock() instanceof BedBlock) {
                                 BlockPos at = new BlockPos(
                                    c.getPos().getMinBlockX() + x, (c.getSectionYFromSectionIndex(section) << 4) + y, c.getPos().getMinBlockZ() + z
                                 );
                                 out.add(new EspChunkWorker.EspBox(new AABB(at), Vec3.atCenterOf(at), 0));
                                 if (out.size() >= 128) {
                                    return;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         );
   }

   static final class Inner_CachePool {
      private final Map<UUID, BedProximityTracker.Inner_CachePool.Inner_DataUtil> init_Map = new HashMap<>();
      private int init_I = -2147483648;

      boolean init(UUID id, String team, DyeColor color, int tick, int settleTicks) {
         if (this.init_I != tick) {
            this.init_Map.values().removeIf(sample -> tick - sample.load() > 1);
            this.init_I = tick;
         }

         if (color == null) {
            this.init_Map.remove(id);
            return false;
         } else {
            BedProximityTracker.Inner_CachePool.Inner_StreamUtil signal = new BedProximityTracker.Inner_CachePool.Inner_StreamUtil(team, color);
            BedProximityTracker.Inner_CachePool.Inner_DataUtil old = this.init_Map.get(id);
            int since = old != null && old.init_v_CachePool_Stream().equals(signal) ? old.init_I() : tick;
            this.init_Map.put(id, new BedProximityTracker.Inner_CachePool.Inner_DataUtil(signal, since, tick));
            return tick - since >= settleTicks;
         }
      }

      void init() {
         this.init_Map.clear();
         this.init_I = -2147483648;
      }

      private record Inner_DataUtil(BedProximityTracker.Inner_CachePool.Inner_StreamUtil init_v_CachePool_Stream, int init_I, int load) {
      }

      private record Inner_StreamUtil(String init_Str, DyeColor init_DyeColor) {
      }
   }

   record Inner_DataUtil(String init_Str, Component init_Component, int init_I, int load, double init_D) {
      Component init(boolean distances) {
         MutableComponent label = this.load == 1
            ? (as.init_Z_33() ? as.load(this.init_Component).copy() : this.init_Component.copy())
            : Component.literal(this.load + " players").withColor(this.init_I & 16777215);
         if (distances) {
            label.append(Component.literal("  " + (int)Math.ceil(this.init_D) + " m").withColor(14540253));
         }

         return label;
      }
   }

   record Inner_StreamUtil(UUID init_UUID, String init_Str, Component init_Component, int init_I, double init_D) {
   }
}
