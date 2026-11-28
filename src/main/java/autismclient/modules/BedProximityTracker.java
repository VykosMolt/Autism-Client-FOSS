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
   private Object trackedLevel;
   private DyeColor own;
   private String teamName;
   private BlockPos ownBed;
   private int tickCounter;
   private final BedProximityTracker.SettleTracker signalTracker = new BedProximityTracker.SettleTracker();
   private int lastSoundTick = -60;
   private List<BedProximityTracker.PlayerGroup> currentGroups = List.of();

   List<BedProximityTracker.PlayerGroup> entries() {
      return this.currentGroups;
   }

   void reset() {
      this.trackedLevel = null;
      this.own = null;
      this.teamName = null;
      this.ownBed = null;
      this.tickCounter = 0;
      this.signalTracker.clearSamples();
      this.lastSoundTick = -60;
      this.currentGroups = List.of();
   }

   static List<BedProximityTracker.PlayerGroup> groupNearby(List<BedProximityTracker.NearbyPlayer> people, double radius) {
      Map<String, List<BedProximityTracker.NearbyPlayer>> teams = new TreeMap<>();
      Set<UUID> seen = new HashSet<>();

      for (BedProximityTracker.NearbyPlayer person : people) {
         if (Double.isFinite(person.distance) && person.distance >= 0.0 && person.distance <= radius && seen.add(person.uuid)) {
            teams.computeIfAbsent(person.groupKey, key -> new ArrayList<>()).add(person);
         }
      }

      List<BedProximityTracker.PlayerGroup> result = new ArrayList<>();

      for (Entry<String, List<BedProximityTracker.NearbyPlayer>> entry : teams.entrySet()) {
         BedProximityTracker.NearbyPlayer closest = entry.getValue()
            .stream()
            .min(Comparator.comparingDouble(BedProximityTracker.NearbyPlayer::distance).thenComparing(BedProximityTracker.NearbyPlayer::uuid))
            .orElseThrow();
         result.add(new BedProximityTracker.PlayerGroup(entry.getKey(), closest.displayName, closest.tint, entry.getValue().size(), closest.distance));
      }

      result.sort(Comparator.comparingDouble(BedProximityTracker.PlayerGroup::distance).thenComparing(BedProximityTracker.PlayerGroup::groupKey));
      return List.copyOf(result);
   }

   void tick(Minecraft mc, DangerModule module) {
      if (module.bool("bed-warning") && mc.level != null && mc.player != null && mc.player.isAlive() && !mc.player.isSpectator()) {
         PlayerTeam ownTeam = mc.player.getTeam();
         TextColor ownRgb = ownTeam == null ? null : ownTeam.getColor().map(TeamColor::textColor).orElse(null);
         PlayerInfo ownInfo = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(mc.player.getUUID());
         DyeColor own = TeamDyeColorResolver.resolve(
            mc.player.getGameProfile().name(),
            ownInfo == null ? null : ownInfo.getTabListDisplayName(),
            mc.player.getDisplayName(),
            ownRgb == null ? null : ownRgb.getValue()
         );
         String teamName = ownTeam == null ? null : ownTeam.getName();
         if (this.trackedLevel != mc.level || this.own != own || !Objects.equals(this.teamName, teamName)) {
            this.reset();
            this.trackedLevel = mc.level;
            this.own = own;
            this.teamName = teamName;
         }

         this.tickCounter++;
         if (!this.signalTracker.isSettled(mc.player.getUUID(), teamName, own, this.tickCounter, 40)) {
            this.currentGroups = List.of();
         } else {
            if (this.ownBed != null
               && mc.level.isLoaded(this.ownBed)
               && !(mc.level.getBlockState(this.ownBed).getBlock() instanceof BedBlock block && block.getColor() == own)) {
               this.ownBed = null;
            }

            if (this.tickCounter % 20 == 1 || this.ownBed == null && this.tickCounter % 5 == 1) {
               this.findOwnBed(mc, module.integer("bed-scan-range"));
            }

            if (this.ownBed == null) {
               this.currentGroups = List.of();
            } else {
               Vec3 center = Vec3.atCenterOf(this.ownBed);
               if (mc.level.isLoaded(this.ownBed)) {
                  BlockState state = mc.level.getBlockState(this.ownBed);
                  if (state.getBlock() instanceof BedBlock) {
                     center = center.add(Vec3.atLowerCornerOf(BedBlock.getConnectedDirection(state).getUnitVec3i()).scale(0.5));
                  }
               }

               ArrayList<BedProximityTracker.NearbyPlayer> people = new ArrayList<>();

               for (AbstractClientPlayer player : mc.level.players()) {
                  if (player != mc.player && player.isAlive() && !player.isSpectator() && !AntiBotTracker.isBot(player)) {
                     PlayerTeam team = player.getTeam();
                     TextColor fallback = team == null ? null : team.getColor().map(TeamColor::textColor).orElse(null);
                     PlayerInfo info = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(player.getUUID());
                     Component display = info == null ? null : info.getTabListDisplayName();
                     String account = player.getGameProfile().name();
                     DyeColor dye = TeamDyeColorResolver.resolve(account, display, player.getDisplayName(), fallback == null ? null : fallback.getValue());
                     if (this.signalTracker.isSettled(player.getUUID(), team == null ? null : team.getName(), dye, this.tickCounter, 20)
                        && (module.bool("bed-include-team") || dye != own && (team == null || team != mc.player.getTeam()))
                        && (module.bool("bed-include-friends") || !TeamsModule.isFriend(player) || dye == own)) {
                        int tint = PlayerDisplayNameParser.displayColor(account, display, fallback);
                        String key = dye != null ? "color:" + dye.getName() : (team != null ? "team:" + team.getName() : "player:" + player.getUUID());
                        people.add(
                           new BedProximityTracker.NearbyPlayer(
                              player.getUUID(), key, PlayerDisplayNameParser.displayName(account, display, fallback), tint, player.position().distanceTo(center)
                           )
                        );
                     }
                  }
               }

               List<BedProximityTracker.PlayerGroup> next = groupNearby(people, module.decimal("bed-warning-radius"));
               if (module.bool("bed-warning-sound") && this.tickCounter - this.lastSoundTick >= 60 && hasNewGroup(this.currentGroups, next)) {
                  module.playBedSound();
                  this.lastSoundTick = this.tickCounter;
               }

               this.currentGroups = next;
            }
         }
      } else {
         this.reset();
      }
   }

   static boolean hasNewGroup(List<BedProximityTracker.PlayerGroup> previous, List<BedProximityTracker.PlayerGroup> next) {
      return next.stream().anyMatch(group -> previous.stream().noneMatch(old -> old.groupKey().equals(group.groupKey())));
   }

   private void findOwnBed(Minecraft mc, int range) {
      EspChunkWorker.setLevel(mc.level);
      if (this.ownBed == null) {
         int radius = Math.min(am.init_I(), (range + 15) / 16);
         int cx = mc.player.chunkPosition().x();
         int cz = mc.player.chunkPosition().z();
         double closest = (double)range * range;

         for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
               LevelChunk chunk = mc.level.getChunkSource().getChunk(cx + dx, cz + dz, ChunkStatus.FULL, false);
               if (chunk != null) {
                  for (EspChunkWorker.EspBox entry : scanBeds(chunk, mc.level.getGameTime())) {
                     BlockPos at = BlockPos.containing(entry.tracer());
                     BlockState state = mc.level.getBlockState(at);
                     if (state.getBlock() instanceof BedBlock block && block.getColor() == this.own) {
                        double distance = at.distToCenterSqr(mc.player.position());
                        if (!(distance >= closest)) {
                           closest = distance;
                           this.ownBed = (state.getValue(BedBlock.PART) == BedPart.HEAD ? at.relative(BedBlock.getConnectedDirection(state)) : at)
                              .immutable();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   static List<EspChunkWorker.EspBox> scanBeds(LevelChunk chunk, long time) {
      return EspChunkWorker.beds
         .scan(
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

   static final class SettleTracker {
      private final Map<UUID, BedProximityTracker.SettleTracker.Inner_DataUtil> samples = new HashMap<>();
      private int lastPruneTick = -2147483648;

      boolean isSettled(UUID id, String team, DyeColor color, int tick, int settleTicks) {
         if (this.lastPruneTick != tick) {
            this.samples.values().removeIf(sample -> tick - sample.load() > 1);
            this.lastPruneTick = tick;
         }

         if (color == null) {
            this.samples.remove(id);
            return false;
         } else {
            BedProximityTracker.SettleTracker.Inner_StreamUtil signal = new BedProximityTracker.SettleTracker.Inner_StreamUtil(team, color);
            BedProximityTracker.SettleTracker.Inner_DataUtil old = this.samples.get(id);
            int since = old != null && old.signal().equals(signal) ? old.since() : tick;
            this.samples.put(id, new BedProximityTracker.SettleTracker.Inner_DataUtil(signal, since, tick));
            return tick - since >= settleTicks;
         }
      }

      void clearSamples() {
         this.samples.clear();
         this.lastPruneTick = -2147483648;
      }

      private record Inner_DataUtil(BedProximityTracker.SettleTracker.Inner_StreamUtil signal, int since, int load) {
      }

      private record Inner_StreamUtil(String team, DyeColor color) {
      }
   }

   record PlayerGroup(String groupKey, Component displayName, int tint, int load, double distance) {
      Component label(boolean distances) {
         MutableComponent label = this.load == 1
            ? (as.init_Z_33() ? as.load(this.displayName).copy() : this.displayName.copy())
            : Component.literal(this.load + " players").withColor(this.tint & 16777215);
         if (distances) {
            label.append(Component.literal("  " + (int)Math.ceil(this.distance) + " m").withColor(14540253));
         }

         return label;
      }
   }

   record NearbyPlayer(UUID uuid, String groupKey, Component displayName, int tint, double distance) {
   }
}
