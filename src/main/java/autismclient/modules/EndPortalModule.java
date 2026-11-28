package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.EnumSetting;
import autismclient.util.EndPortalMemoryCache;
import autismclient.util.StrongholdTriangulator;
import autismclient.util.StrongholdRingEstimator;
import autismclient.util.EndPortalSessionTracker;
import autismclient.util.EndPortalDisplayHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class EndPortalModule extends Module {
   static final int init_I = -11279164;
   private final ColorSetting color;
   private final DoubleSetting scale;
   private final EnumSetting<EndPortalModule.DisplayMode> displayMode;
   private final EndPortalMemoryCache memoryCache = new EndPortalMemoryCache();
   private final EndPortalSessionTracker sessionTracker = new EndPortalSessionTracker(this.memoryCache);
   private final Map<Integer, EndPortalModule.TrackedEye> trackedEyes = new LinkedHashMap<>();
   private EndPortalMemoryCache.DimensionMemory memory;
   private List<int[]> offsets = List.of();
   private int scannedRadius = -1;
   private int cx;
   private int lastChunkZ;
   private int scanIndex;
   private int tickCounter;
   private long trackingStartAt = -1L;
   private Vec3 nearestPortal;
   private Vec3 navTarget;
   private Map<Long, StrongholdTriangulator.PortalFrame> knownFrames = Map.of();
   private List<Vec3> portalCenters = List.of();
   private volatile List<BlockPos> shownFrames = List.of();

   private static long nowMillis() {
      return System.nanoTime() / 1000000L;
   }

   public EndPortalModule() {
      super("end-portal", "EndPortal", ae.update, "Portal frames and Eye of Ender navigation.");
      this.color = this.add(new ColorSetting("color", "Color", -11279164).description("Color of the frame ESP, arrow, tracer and light column."));
      this.scale = this.add(new DoubleSetting("scale", "Scale", 1.0, 0.5, 2.0, 0.05).description("Size of the direction icon, prompt and label."));
      this.displayMode = this.add(
         new EnumSetting<>("display", "Display", EndPortalModule.DisplayMode.DISTANCE, EndPortalModule.DisplayMode.values()).description("Show distance or target coordinates.")
      );
      this.add(new BoolSetting("frames", "Frame ESP", true).description("Highlight portal frames."));
      this.add(new BoolSetting("tracer", "Tracers", true).description("Show one tracer to the destination."));
      this.add(new BoolSetting("beacon", "Light Column", true).description("Show the static column in loaded chunks within render distance."));
      this.add(new BoolSetting("guide", "Guide", true).description("Show the direction icon and label."));
   }

   public static EndPortalModule active() {
      Module m = al.init_Module_Str("end-portal");
      return m instanceof EndPortalModule p && m.isEnabled() ? p : null;
   }

   static boolean showsFrames() {
      EndPortalModule p = active();
      return p != null && p.bool("frames");
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.memoryCache.hasMemory();
   }

   @Override
   public void onEnable() {
      this.memoryCache.stop(nowMillis());
      this.resetSession();
      this.trackingStartAt = -1L;
   }

   @Override
   public void onDisable() {
      this.memoryCache.start(nowMillis());
      this.resetSession();
      EspChunkWorker.reset();
   }

   @Override
   public void onGameLeft() {
      this.resetSession();
   }

   @Override
   public void onGameJoin() {
      this.resetSession();
   }

   private void resetSession() {
      this.sessionTracker.reset();
      this.clearState();
   }

   private void clearState() {
      this.memory = null;
      this.trackedEyes.clear();
      this.shownFrames = List.of();
      this.nearestPortal = null;
      this.navTarget = null;
      this.portalCenters = List.of();
      this.knownFrames = Map.of();
      this.offsets = List.of();
      this.scannedRadius = -1;
      this.scanIndex = this.tickCounter = 0;
   }

   private boolean syncSession() {
      if (MC.level != null && MC.player != null && MC.getConnection() != null) {
         String server;
         if (MC.getCurrentServer() != null) {
            server = "server:" + MC.getCurrentServer().ip.toLowerCase(Locale.ROOT);
         } else if (MC.getSingleplayerServer() != null) {
            server = "local:" + MC.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
         } else {
            server = "connection:" + System.identityHashCode(MC.getConnection());
         }

         if (this.sessionTracker.init(MC.level, MC.getConnection(), server, MC.level.dimension().identifier().toString())) {
            this.clearState();
            this.memory = this.sessionTracker.getMemory();
            this.memory.triangulator.configure(true, false);
            this.trackingStartAt = nowMillis() + 2000L;
         }

         return true;
      } else {
         return false;
      }
   }

   static boolean load_Z() {
      return MC.level != null && EndPortalSessionTracker.isStrongholdDimension(MC.level.dimensionType().hasSkyLight(), MC.level.dimensionType().hasEnderDragonFight());
   }

   private boolean isSameSession() {
      return this.sessionTracker.isSameSession(MC.level, MC.getConnection());
   }

   @Override
   public void tick() {
      if (!this.isEnabled()) {
         this.memoryCache.expire(nowMillis());
      } else if (!this.syncSession()) {
         this.resetSession();
      } else {
         this.tickCounter++;
         EspChunkWorker.setLevel(MC.level);
         this.updateEyes();
         this.scanChunks();
         if (this.tickCounter % 5 == 0) {
            this.refreshFrames();
         }

         if (this.memory.triangulator.lastThrow() == null
            && this.memory.frames.isEmpty()
            && this.trackedEyes.isEmpty()
            && load_Z()
            && MC.gui.screen() == null
            && (this.trackingStartAt < 0L || nowMillis() >= this.trackingStartAt)) {
            this.trackingStartAt = nowMillis() + 60000L;
            autismclient.util.bi.read("EndPortal: throw an Eye of Ender to record a direction.");
         }
      }
   }

   public static void onAddEntity(ClientPacketListener listener, ClientboundAddEntityPacket packet) {
      if (packet.getType() == EntityTypes.EYE_OF_ENDER) {
         EndPortalModule module = active();
         if (module != null
            && MC.isSameThread()
            && listener == MC.getConnection()
            && module.syncSession()
            && load_Z()
            && MC.level.getEntity(packet.getId()) instanceof EyeOfEnder eye
            && eye.getUUID().equals(packet.getUUID())
            && !(eye.position().distanceToSqr(MC.player.position()) > 4096.0)
            && !(eye.getDeltaMovement().horizontalDistance() > 0.09)
            && module.trackedEyes.size() < 4) {
            module.trackedEyes.put(eye.getId(), new EndPortalModule.TrackedEye(eye.getUUID(), MC.level.getGameTime(), new StrongholdTriangulator.ThrowTracker(eye.position())));
         }
      }
   }

   private void updateEyes() {
      Iterator<Entry<Integer, EndPortalModule.TrackedEye>> iterator = this.trackedEyes.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<Integer, EndPortalModule.TrackedEye> entry = iterator.next();
         EndPortalModule.TrackedEye tracked = entry.getValue();
         Entity eye = MC.level.getEntity(entry.getKey());
         int age = (int)(MC.level.getGameTime() - tracked.thrownAtTick);
         if (eye instanceof EyeOfEnder && eye.isAlive() && eye.getUUID().equals(tracked.uuid) && age >= 0 && age <= 60) {
            StrongholdTriangulator.ThrowSample ray = tracked.tracker.sample(eye.position(), age);
            if (ray != null) {
               boolean added = this.memory.triangulator.addThrow(ray.origin(), ray.direction().scale(8.0));
               if (added) {
                  StrongholdRingEstimator.RingEstimate prediction = this.memory.triangulator.ringEstimate();
                  autismclient.util.bi.read(
                     prediction != null
                        ? "EndPortal: stronghold location estimated."
                        : (
                           this.memory.triangulator.estimate() == null
                              ? "EndPortal: direction recorded."
                              : "EndPortal: stronghold position estimated; find the portal room nearby."
                        )
                  );
               }
            }

            if (tracked.tracker.isDone()) {
               iterator.remove();
            }
         } else {
            iterator.remove();
         }
      }
   }

   private void scanChunks() {
      int r = Math.min(am.init_I(), 16);
      int cx = MC.player.chunkPosition().x();
      int cz = MC.player.chunkPosition().z();
      if (r != this.scannedRadius || cx != this.cx || cz != this.lastChunkZ) {
         this.scannedRadius = r;
         this.cx = cx;
         this.lastChunkZ = cz;
         this.scanIndex = 0;
         List<int[]> offsets = new ArrayList<>();

         for (int n = 0; n <= r; n++) {
            for (int x = -n; x <= n; x++) {
               for (int z = -n; z <= n; z++) {
                  if (Math.max(Math.abs(x), Math.abs(z)) == n) {
                     offsets.add(new int[]{x, z});
                  }
               }
            }
         }

         this.offsets = offsets;
      }

      int checked = 0;
      int scanned = 0;

      while (!this.offsets.isEmpty() && checked++ < Math.min(this.offsets.size(), 32) && scanned < 2) {
         int[] offset = this.offsets.get(Math.floorMod(this.scanIndex++, this.offsets.size()));
         LevelChunk chunk = MC.level.getChunkSource().getChunk(cx + offset[0], cz + offset[1], ChunkStatus.FULL, false);
         if (chunk != null) {
            scanned++;
            List<EspChunkWorker.EspBox> entries = EspChunkWorker.endPortals.scan(chunk, MC.level.getGameTime(), "end-portal", EndPortalModule::collectFrames);
            int x = chunk.getPos().x();
            int z = chunk.getPos().z();
            Set<Long> current = new HashSet<>();

            for (EspChunkWorker.EspBox hit : entries) {
               BlockPos pos = BlockPos.containing(hit.tracer());
               current.add(pos.asLong());
               BlockState state = chunk.getBlockState(pos);
               if (state.is(Blocks.END_PORTAL_FRAME)) {
                  this.memory.init(new StrongholdTriangulator.PortalFrame(pos, state.getValue(EndPortalFrameBlock.FACING)));
               }
            }

            this.memory
               .frames
               .values()
               .removeIf(f -> f.pos().getX() >> 4 == x && f.pos().getZ() >> 4 == z && !current.contains(f.pos().asLong()));
         }
      }
   }

   private static void collectFrames(LevelChunk chunk, List<EspChunkWorker.EspBox> out) {
      LevelChunkSection[] sections = chunk.getSections();

      for (int section = 0; section < sections.length; section++) {
         LevelChunkSection cells = sections[section];
         if (cells != null && !cells.hasOnlyAir() && cells.maybeHas(s -> s.is(Blocks.END_PORTAL_FRAME))) {
            for (int y = 0; y < 16; y++) {
               for (int x = 0; x < 16; x++) {
                  for (int z = 0; z < 16; z++) {
                     if (cells.getBlockState(x, y, z).is(Blocks.END_PORTAL_FRAME)) {
                        BlockPos p = new BlockPos(
                           chunk.getPos().getMinBlockX() + x, (chunk.getSectionYFromSectionIndex(section) << 4) + y, chunk.getPos().getMinBlockZ() + z
                        );
                        out.add(new EspChunkWorker.EspBox(new AABB(p), Vec3.atCenterOf(p), 0));
                        if (out.size() >= 256) {
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void refreshFrames() {
      List<BlockPos> shown = new ArrayList<>();
      Iterator<Entry<Long, StrongholdTriangulator.PortalFrame>> iterator = this.memory.frames.entrySet().iterator();
      int view = am.init_I();

      while (iterator.hasNext()) {
         StrongholdTriangulator.PortalFrame f = iterator.next().getValue();
         BlockPos pos = f.pos();
         if (MC.level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false) != null) {
            BlockState state = MC.level.getBlockState(pos);
            if (!state.is(Blocks.END_PORTAL_FRAME)) {
               iterator.remove();
            } else {
               if (state.getValue(EndPortalFrameBlock.FACING) != f.facing()) {
                  this.memory.init(new StrongholdTriangulator.PortalFrame(pos, state.getValue(EndPortalFrameBlock.FACING)));
               }

               if (EndPortalDisplayHelper.init(MC.player.position(), Vec3.atCenterOf(pos), view)) {
                  shown.add(pos);
               }
            }
         }
      }

      shown.sort(Comparator.comparingDouble(p -> p.distToCenterSqr(MC.player.position())));
      this.shownFrames = List.copyOf(shown);
      if (!this.knownFrames.equals(this.memory.frames)) {
         this.portalCenters = StrongholdTriangulator.portalCentersFromFrames(this.memory.frames.values());
         this.knownFrames = Map.copyOf(this.memory.frames);
      }

      this.nearestPortal = this.portalCenters.stream().min(Comparator.comparingDouble(p -> p.distanceToSqr(MC.player.position()))).orElse(null);
      this.navTarget = this.nearestPortal != null
         ? this.nearestPortal
         : this.memory
            .frames
            .values()
            .stream()
            .min(Comparator.comparingDouble(fx -> fx.pos().distToCenterSqr(MC.player.position())))
            .map(fx -> Vec3.atCenterOf(fx.pos()))
            .orElse(null);
   }

   List<BlockPos> frames() {
      return this.isSameSession() && this.bool("frames") ? this.shownFrames : List.of();
   }

   int color() {
      return this.color.get();
   }

   float init_F() {
      return this.scale.get().floatValue();
   }

   boolean showsCoordinates() {
      return this.displayMode.get() == EndPortalModule.DisplayMode.COORDINATES;
   }

   boolean showsTracer() {
      return this.bool("tracer");
   }

   boolean showsGuide() {
      return this.bool("guide");
   }

   boolean sync() {
      return this.bool("beacon") && (this.close() || this.open());
   }

   boolean flush() {
      return this.isSameSession() && !this.trackedEyes.isEmpty();
   }

   Vec3 target() {
      if (this.memory == null || MC.player == null || MC.level == null || !this.isSameSession()) {
         return null;
      } else if (this.navTarget != null) {
         return this.navTarget;
      } else {
         return load_Z() ? this.memory.triangulator.destinationFor(MC.player.position()) : null;
      }
   }

   boolean close() {
      return this.isSameSession() && this.memory != null && !this.memory.frames.isEmpty();
   }

   boolean open() {
      return this.isSameSession() && !this.close() && this.memory != null && this.memory.triangulator.estimate() != null;
   }

   boolean hasRingEstimate() {
      return this.open() && this.memory.triangulator.ringEstimate() != null;
   }

   double init_D_2() {
      return this.open() ? this.memory.triangulator.estimate().uncertainty() : 0.0;
   }

   String statusText() {
      return this.close()
         ? (this.nearestPortal != null ? "Portal ring observed" : "Portal frame observed")
         : (
            this.open()
               ? (this.hasRingEstimate() ? (this.memory.triangulator.throwCount() == 1 ? "One-Eye estimate" : "Generation estimate") : "Stronghold estimate")
               : (
                  this.memory != null && this.memory.triangulator.lastThrow() != null
                     ? "Eye bearing / distance unknown"
                     : "No Eye data"
               )
         );
   }

   @Override
   public String info() {
      return this.statusText();
   }

   private enum DisplayMode {
      DISTANCE,
      COORDINATES;
   }

   private record TrackedEye(UUID uuid, long thrownAtTick, StrongholdTriangulator.ThrowTracker tracker) {
   }
}
