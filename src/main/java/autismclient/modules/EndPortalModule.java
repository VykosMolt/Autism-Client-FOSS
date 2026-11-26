package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.EnumSetting;
import autismclient.util.cR;
import autismclient.util.cS;
import autismclient.util.cT;
import autismclient.util.cU;
import autismclient.util.cV;
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
   private final ColorSetting init_ColorSetting;
   private final DoubleSetting init_DoubleSetting;
   private final EnumSetting<EndPortalModule.Inner_DataUtil> init_EnumSetting;
   private final cR init_cR = new cR();
   private final cU init_cU = new cU(this.init_cR);
   private final Map<Integer, EndPortalModule.Inner_StreamUtil> init_Map = new LinkedHashMap<>();
   private cR.Inner_DataUtil init_cR_DataUtil;
   private List<int[]> init_List = List.of();
   private int load_I = -1;
   private int read_I;
   private int write;
   private int update;
   private int sync;
   private long init_J = -1L;
   private Vec3 init_Vec3;
   private Vec3 load_Vec3;
   private Map<Long, cS.Inner_CachePool> load_Map = Map.of();
   private List<Vec3> load_List = List.of();
   private volatile List<BlockPos> read_List = List.of();

   private static long init_J_2() {
      return System.nanoTime() / 1000000L;
   }

   public EndPortalModule() {
      super("end-portal", "EndPortal", ae.update, "Portal frames and Eye of Ender navigation.");
      this.init_ColorSetting = this.add(new ColorSetting("color", "Color", -11279164).description("Color of the frame ESP, arrow, tracer and light column."));
      this.init_DoubleSetting = this.add(new DoubleSetting("scale", "Scale", 1.0, 0.5, 2.0, 0.05).description("Size of the direction icon, prompt and label."));
      this.init_EnumSetting = this.add(
         new EnumSetting<>("display", "Display", EndPortalModule.Inner_DataUtil.DISTANCE, EndPortalModule.Inner_DataUtil.values()).description("Show distance or target coordinates.")
      );
      this.add(new BoolSetting("frames", "Frame ESP", true).description("Highlight portal frames."));
      this.add(new BoolSetting("tracer", "Tracers", true).description("Show one tracer to the destination."));
      this.add(new BoolSetting("beacon", "Light Column", true).description("Show the static column in loaded chunks within render distance."));
      this.add(new BoolSetting("guide", "Guide", true).description("Show the direction icon and label."));
   }

   public static EndPortalModule init_E() {
      Module m = al.init_Module_Str("end-portal");
      return m instanceof EndPortalModule p && m.isEnabled() ? p : null;
   }

   static boolean init_Z_5() {
      EndPortalModule p = init_E();
      return p != null && p.bool("frames");
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.init_cR.init_Z();
   }

   @Override
   public void onEnable() {
      this.init_cR.read(init_J_2());
      this.init_V_4();
      this.init_J = -1L;
   }

   @Override
   public void onDisable() {
      this.init_cR.init(init_J_2());
      this.init_V_4();
      EspChunkWorker.init_V();
   }

   @Override
   public void onGameLeft() {
      this.init_V_4();
   }

   @Override
   public void onGameJoin() {
      this.init_V_4();
   }

   private void init_V_4() {
      this.init_cU.init_V();
      this.load_V_3();
   }

   private void load_V_3() {
      this.init_cR_DataUtil = null;
      this.init_Map.clear();
      this.read_List = List.of();
      this.init_Vec3 = null;
      this.load_Vec3 = null;
      this.load_List = List.of();
      this.load_Map = Map.of();
      this.init_List = List.of();
      this.load_I = -1;
      this.update = this.sync = 0;
   }

   private boolean push() {
      if (MC.level != null && MC.player != null && MC.getConnection() != null) {
         String server;
         if (MC.getCurrentServer() != null) {
            server = "server:" + MC.getCurrentServer().ip.toLowerCase(Locale.ROOT);
         } else if (MC.getSingleplayerServer() != null) {
            server = "local:" + MC.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
         } else {
            server = "connection:" + System.identityHashCode(MC.getConnection());
         }

         if (this.init_cU.init(MC.level, MC.getConnection(), server, MC.level.dimension().identifier().toString())) {
            this.load_V_3();
            this.init_cR_DataUtil = this.init_cU.init_cR_DataUtil();
            this.init_cR_DataUtil.init_cS.init_V_Z_Z(true, false);
            this.init_J = init_J_2() + 2000L;
         }

         return true;
      } else {
         return false;
      }
   }

   static boolean load_Z() {
      return MC.level != null && cU.init_Z_Z_Z(MC.level.dimensionType().hasSkyLight(), MC.level.dimensionType().hasEnderDragonFight());
   }

   private boolean pop() {
      return this.init_cU.init_Z_Obj_Obj(MC.level, MC.getConnection());
   }

   @Override
   public void tick() {
      if (!this.isEnabled()) {
         this.init_cR.load(init_J_2());
      } else if (!this.push()) {
         this.init_V_4();
      } else {
         this.sync++;
         EspChunkWorker.init_V_Obj(MC.level);
         this.read_V_2();
         this.write_V();
         if (this.sync % 5 == 0) {
            this.update_V();
         }

         if (this.init_cR_DataUtil.init_cS.init_cS_BufferHelper() == null
            && this.init_cR_DataUtil.init_Map.isEmpty()
            && this.init_Map.isEmpty()
            && load_Z()
            && MC.gui.screen() == null
            && (this.init_J < 0L || init_J_2() >= this.init_J)) {
            this.init_J = init_J_2() + 60000L;
            autismclient.util.bi.read("EndPortal: throw an Eye of Ender to record a direction.");
         }
      }
   }

   public static void init(ClientPacketListener listener, ClientboundAddEntityPacket packet) {
      if (packet.getType() == EntityTypes.EYE_OF_ENDER) {
         EndPortalModule module = init_E();
         if (module != null
            && MC.isSameThread()
            && listener == MC.getConnection()
            && module.push()
            && load_Z()
            && MC.level.getEntity(packet.getId()) instanceof EyeOfEnder eye
            && eye.getUUID().equals(packet.getUUID())
            && !(eye.position().distanceToSqr(MC.player.position()) > 4096.0)
            && !(eye.getDeltaMovement().horizontalDistance() > 0.09)
            && module.init_Map.size() < 4) {
            module.init_Map.put(eye.getId(), new EndPortalModule.Inner_StreamUtil(eye.getUUID(), MC.level.getGameTime(), new cS.Inner_StreamUtil(eye.position())));
         }
      }
   }

   private void read_V_2() {
      Iterator<Entry<Integer, EndPortalModule.Inner_StreamUtil>> iterator = this.init_Map.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<Integer, EndPortalModule.Inner_StreamUtil> entry = iterator.next();
         EndPortalModule.Inner_StreamUtil tracked = entry.getValue();
         Entity eye = MC.level.getEntity(entry.getKey());
         int age = (int)(MC.level.getGameTime() - tracked.init_J);
         if (eye instanceof EyeOfEnder && eye.isAlive() && eye.getUUID().equals(tracked.init_UUID) && age >= 0 && age <= 60) {
            cS.Inner_BufferHelper ray = tracked.init_cS_StreamUtil.init(eye.position(), age);
            if (ray != null) {
               boolean added = this.init_cR_DataUtil.init_cS.init_Z_Vec3_Vec3(ray.init(), ray.load().scale(8.0));
               if (added) {
                  cT.Inner_StreamUtil prediction = this.init_cR_DataUtil.init_cS.init_cT_StreamUtil();
                  autismclient.util.bi.read(
                     prediction != null
                        ? "EndPortal: stronghold location estimated."
                        : (
                           this.init_cR_DataUtil.init_cS.init_cS_DataUtil() == null
                              ? "EndPortal: direction recorded."
                              : "EndPortal: stronghold position estimated; find the portal room nearby."
                        )
                  );
               }
            }

            if (tracked.init_cS_StreamUtil.init()) {
               iterator.remove();
            }
         } else {
            iterator.remove();
         }
      }
   }

   private void write_V() {
      int r = Math.min(am.init_I(), 16);
      int cx = MC.player.chunkPosition().x();
      int cz = MC.player.chunkPosition().z();
      if (r != this.load_I || cx != this.read_I || cz != this.write) {
         this.load_I = r;
         this.read_I = cx;
         this.write = cz;
         this.update = 0;
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

         this.init_List = offsets;
      }

      int checked = 0;
      int scanned = 0;

      while (!this.init_List.isEmpty() && checked++ < Math.min(this.init_List.size(), 32) && scanned < 2) {
         int[] offset = this.init_List.get(Math.floorMod(this.update++, this.init_List.size()));
         LevelChunk chunk = MC.level.getChunkSource().getChunk(cx + offset[0], cz + offset[1], ChunkStatus.FULL, false);
         if (chunk != null) {
            scanned++;
            List<EspChunkWorker.Inner_CachePool> entries = EspChunkWorker.read.init(chunk, MC.level.getGameTime(), "end-portal", EndPortalModule::init);
            int x = chunk.getPos().x();
            int z = chunk.getPos().z();
            Set<Long> current = new HashSet<>();

            for (EspChunkWorker.Inner_CachePool hit : entries) {
               BlockPos pos = BlockPos.containing(hit.init_Vec3());
               current.add(pos.asLong());
               BlockState state = chunk.getBlockState(pos);
               if (state.is(Blocks.END_PORTAL_FRAME)) {
                  this.init_cR_DataUtil.init(new cS.Inner_CachePool(pos, state.getValue(EndPortalFrameBlock.FACING)));
               }
            }

            this.init_cR_DataUtil
               .init_Map
               .values()
               .removeIf(f -> f.init_BlockPos().getX() >> 4 == x && f.init_BlockPos().getZ() >> 4 == z && !current.contains(f.init_BlockPos().asLong()));
         }
      }
   }

   private static void init(LevelChunk chunk, List<EspChunkWorker.Inner_CachePool> out) {
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
                        out.add(new EspChunkWorker.Inner_CachePool(new AABB(p), Vec3.atCenterOf(p), 0));
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

   private void update_V() {
      List<BlockPos> shown = new ArrayList<>();
      Iterator<Entry<Long, cS.Inner_CachePool>> iterator = this.init_cR_DataUtil.init_Map.entrySet().iterator();
      int view = am.init_I();

      while (iterator.hasNext()) {
         cS.Inner_CachePool f = iterator.next().getValue();
         BlockPos pos = f.init_BlockPos();
         if (MC.level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false) != null) {
            BlockState state = MC.level.getBlockState(pos);
            if (!state.is(Blocks.END_PORTAL_FRAME)) {
               iterator.remove();
            } else {
               if (state.getValue(EndPortalFrameBlock.FACING) != f.init_Direction()) {
                  this.init_cR_DataUtil.init(new cS.Inner_CachePool(pos, state.getValue(EndPortalFrameBlock.FACING)));
               }

               if (cV.init(MC.player.position(), Vec3.atCenterOf(pos), view)) {
                  shown.add(pos);
               }
            }
         }
      }

      shown.sort(Comparator.comparingDouble(p -> p.distToCenterSqr(MC.player.position())));
      this.read_List = List.copyOf(shown);
      if (!this.load_Map.equals(this.init_cR_DataUtil.init_Map)) {
         this.load_List = cS.init_List_Collection(this.init_cR_DataUtil.init_Map.values());
         this.load_Map = Map.copyOf(this.init_cR_DataUtil.init_Map);
      }

      this.init_Vec3 = this.load_List.stream().min(Comparator.comparingDouble(p -> p.distanceToSqr(MC.player.position()))).orElse(null);
      this.load_Vec3 = this.init_Vec3 != null
         ? this.init_Vec3
         : this.init_cR_DataUtil
            .init_Map
            .values()
            .stream()
            .min(Comparator.comparingDouble(fx -> fx.init_BlockPos().distToCenterSqr(MC.player.position())))
            .map(fx -> Vec3.atCenterOf(fx.init_BlockPos()))
            .orElse(null);
   }

   List<BlockPos> init_List_3() {
      return this.pop() && this.bool("frames") ? this.read_List : List.of();
   }

   int init_I_2() {
      return this.init_ColorSetting.get();
   }

   float init_F() {
      return this.init_DoubleSetting.get().floatValue();
   }

   boolean read_Z_2() {
      return this.init_EnumSetting.get() == EndPortalModule.Inner_DataUtil.COORDINATES;
   }

   boolean write_Z() {
      return this.bool("tracer");
   }

   boolean update_Z() {
      return this.bool("guide");
   }

   boolean sync() {
      return this.bool("beacon") && (this.close() || this.open());
   }

   boolean flush() {
      return this.pop() && !this.init_Map.isEmpty();
   }

   Vec3 init_Vec3() {
      if (this.init_cR_DataUtil == null || MC.player == null || MC.level == null || !this.pop()) {
         return null;
      } else if (this.load_Vec3 != null) {
         return this.load_Vec3;
      } else {
         return load_Z() ? this.init_cR_DataUtil.init_cS.init_Vec3_Vec3(MC.player.position()) : null;
      }
   }

   boolean close() {
      return this.pop() && this.init_cR_DataUtil != null && !this.init_cR_DataUtil.init_Map.isEmpty();
   }

   boolean open() {
      return this.pop() && !this.close() && this.init_cR_DataUtil != null && this.init_cR_DataUtil.init_cS.init_cS_DataUtil() != null;
   }

   boolean poll() {
      return this.open() && this.init_cR_DataUtil.init_cS.init_cT_StreamUtil() != null;
   }

   double init_D_2() {
      return this.open() ? this.init_cR_DataUtil.init_cS.init_cS_DataUtil().init_D() : 0.0;
   }

   String init_Str() {
      return this.close()
         ? (this.init_Vec3 != null ? "Portal ring observed" : "Portal frame observed")
         : (
            this.open()
               ? (this.poll() ? (this.init_cR_DataUtil.init_cS.init_I() == 1 ? "One-Eye estimate" : "Generation estimate") : "Stronghold estimate")
               : (
                  this.init_cR_DataUtil != null && this.init_cR_DataUtil.init_cS.init_cS_BufferHelper() != null
                     ? "Eye bearing / distance unknown"
                     : "No Eye data"
               )
         );
   }

   @Override
   public String info() {
      return this.init_Str();
   }

   private enum Inner_DataUtil {
      DISTANCE,
      COORDINATES;
   }

   private record Inner_StreamUtil(UUID init_UUID, long init_J, cS.Inner_StreamUtil init_cS_StreamUtil) {
   }
}
