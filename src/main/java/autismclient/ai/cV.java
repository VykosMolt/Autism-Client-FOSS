package autismclient.ai;

import autismclient.compat263.Compat263;
import autismclient.gui.d.PromptDialogScreen;
import autismclient.util.OverlayManager;
import autismclient.util.OverlayBounds;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class cV {
   private static cV init_cV;
   private static final LongOpenHashSet loadedChunkKeys = new LongOpenHashSet();
   private static ClientLevel loadedChunkLevel;
   static final int init_I = 256;
   private final Minecraft init_Minecraft;
   final cR init_cR;
   private final Map<String, cR.Inner_StreamUtil> init_Map = new LinkedHashMap<>();
   private final Map<String, cV.Inner_StreamUtil> load_Map = new LinkedHashMap<>();
   private final Set<String> init_Set = new HashSet<>();
   private ClientLevel init_ClientLevel;
   private String write_Str = "";
   private String update_Str = "";
   private boolean load_Z;
   private SchematicLibraryOverlay init_SchematicLibraryOv;
   private Future<?> init_Future;
   private boolean read_Z;
   private long init_J;
   private String sync = "";
   private volatile int load_I;
   private String flush = "";
   private String close = "";
   private long load_J;
   private boolean write_Z;
   private boolean update_Z;
   private cP init_cP;
   private SchematicRecording activeRecording;
   private cT init_cT;
   private String open;
   private cV.Inner_DataUtil init_cV_DataUtil;
   private final Object init_Obj = new Object();
   private final Object recordingVisOwner = new Object();
   cP.Inner_DataUtil init_cP_DataUtil;
   String init_Str = "";
   String load_Str = "";
   String read_Str = "";

   public static cV init_cV() {
      if (init_cV == null) {
         init_cV = new cV(Minecraft.getInstance());
      }

      return init_cV;
   }

   public static void init_V() {
      ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
         if (loadedChunkLevel != level) {
            loadedChunkKeys.clear();
            loadedChunkLevel = level;
         }

         loadedChunkKeys.add(chunk.getPos().pack());
         if (init_cV != null && init_cV.activeRecording != null) {
            init_cV.activeRecording.putAt_V_ClientLevel_LevelChunk(level, chunk);
         }
      });
      ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
         if (loadedChunkLevel == level) {
            loadedChunkKeys.remove(chunk.getPos().pack());
         }
      });
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         if (client.level == null && loadedChunkLevel != null) {
            loadedChunkKeys.clear();
            loadedChunkLevel = null;
         }

         if (init_cV == null && client.level != null && autismclient.e.FileCache.init()) {
            init_cV();
         }

         if (init_cV != null) {
            init_cV.open_V();
         }
      });
   }

   static List<LevelChunk> loadedChunks(ClientLevel level) {
      if (level != loadedChunkLevel) {
         return List.of();
      }

      ArrayList<LevelChunk> chunks = new ArrayList<>(loadedChunkKeys.size());
      ClientChunkCache source = level.getChunkSource();
      LongIterator iterator = loadedChunkKeys.iterator();

      while (iterator.hasNext()) {
         long key = iterator.nextLong();
         LevelChunk chunk = source.getChunk(ChunkPos.getX(key), ChunkPos.getZ(key), ChunkStatus.FULL, false);
         if (chunk != null) {
            chunks.add(chunk);
         }
      }

      return chunks;
   }

   public static void load_V() {
      if (close_Z()) {
         cV workspace = init_cV();
         OverlayManager manager = OverlayManager.init_bk();
         SchematicLibraryOverlay window = workspace.init_SchematicLibraryOv;
         if (window != null && window.sync_Z() && manager.init_List().contains(window) && !manager.load_Z(window)) {
            window.init_V_Z(false);
         } else {
            workspace.init_SchematicLibraryOv();
         }
      }
   }

   public static void read_V() {
      if (close_Z()) {
         if (init_cV == null || init_cV.init_SchematicLibraryOv == null) {
            OverlayBounds saved = autismclient.util.AutismSessionState.init_ck().init_cH_Str("schematic-library");
            if (saved == null || !saved.init_Z) {
               return;
            }

            cV workspace = init_cV();
            workspace.init_SchematicLibraryOv = new SchematicLibraryOverlay(workspace);
         }

         if (init_cV.init_SchematicLibraryOv.sync_Z()) {
            OverlayManager manager = OverlayManager.init_bk();
            manager.init_V(init_cV.init_SchematicLibraryOv);
            manager.init_V_cX_Z(init_cV.init_SchematicLibraryOv, false);
         }
      }
   }

   private static boolean close_Z() {
      return !autismclient.modules.aD.init_Z() && autismclient.e.CharEncoder.init();
   }

   private cV(Minecraft mc) {
      this.init_Minecraft = mc;
      this.init_cR = new cR(mc.gameDirectory.toPath());
      cR.init_ExecutorService.execute(() -> {
         String problem = "";

         cR.Inner_CachePool state;
         try {
            state = this.init_cR.init();
         } catch (Exception e) {
            state = new cR.Inner_CachePool(List.of(this.init_cR.load_Path.toString()), List.of());
            problem = cR.init_Str_Throwable(e);
         }

         cR.Inner_CachePool result = state;
         String failure = problem;
         mc.execute(() -> {
            this.init_cR.init_V_cR_CachePool(result);
            result.placements().forEach(p -> this.init_Map.put(p.id(), p));
            this.load_Z = true;
            if (!failure.isEmpty()) {
               this.close = "Could not read saved placements: " + failure;
            }

            this.write_V();
         });
      });
   }

   SchematicLibraryOverlay init_SchematicLibraryOv() {
      if (this.init_SchematicLibraryOv == null) {
         this.init_SchematicLibraryOv = new SchematicLibraryOverlay(this);
      }

      OverlayManager manager = OverlayManager.init_bk();
      if (this.init_Minecraft.gui.screen() == null) {
         autismclient.util.OverlayScreenController.init_V(this.init_Minecraft, new autismclient.gui.d.AutismOverlaysScreen(this.init_SchematicLibraryOv, null, false, false));
      }

      manager.init_V(this.init_SchematicLibraryOv);
      manager.init_V_cX_Z(this.init_SchematicLibraryOv, false);
      this.init_SchematicLibraryOv.init_V_Z(true);
      this.init_SchematicLibraryOv.write_V_Z(false);
      manager.update_V(this.init_SchematicLibraryOv);
      return this.init_SchematicLibraryOv;
   }

   boolean init_Z() {
      return this.init_cT != null;
   }

   private cV.Inner_DataUtil init_cV_DataUtil() {
      return this.init_cV_DataUtil != null && !this.init_cV_DataUtil.job().load().isDone() ? this.init_cV_DataUtil : null;
   }

   boolean load_Z() {
      return this.init_cV_DataUtil() != null;
   }

   private boolean init_Z_Str(String placementId) {
      cV.Inner_DataUtil active = this.init_cV_DataUtil();
      return active != null && active.placementId().equals(placementId);
   }

   String init_Str() {
      cV.Inner_DataUtil active = this.init_cV_DataUtil();
      cR.Inner_StreamUtil placement = active == null ? null : this.init_cR_StreamUtil_Str(active.placementId());
      return placement == null ? "" : load_Str_cR_StreamUtil(placement);
   }

   cR.Inner_StreamUtil init_cR_StreamUtil_cR_DataUtil(cR.Inner_DataUtil file) {
      if (file == null) {
         return null;
      }

      String path = file.file().toString();
      cV.Inner_DataUtil active = this.init_cV_DataUtil();
      cR.Inner_StreamUtil building = active == null ? null : this.init_cR_StreamUtil_Str(active.placementId());
      if (building != null && building.visible() && building.file().equals(path)) {
         return building;
      }

      for (cR.Inner_StreamUtil placement : this.init_Map.values()) {
         if (placement.scope().equals(this.write_Str) && placement.dimension().equals(this.update_Str) && placement.visible() && placement.file().equals(path)) {
            return placement;
         }
      }

      return null;
   }

   boolean read_Z() {
      return this.load_Z;
   }

   boolean write_Z() {
      return this.init_ClientLevel != null && this.init_Minecraft.player != null && !this.write_Str.isEmpty();
   }

   boolean update_Z() {
      IntegratedServer server = this.init_Minecraft.getSingleplayerServer();
      return this.write_Z() && server != null && !server.isPublished();
   }

   boolean sync_Z() {
      return this.read_Z;
   }

   boolean flush_Z() {
      return this.read_Z || this.init_Future != null || this.init_cP != null || this.activeRecording != null || this.init_cT != null;
   }

   boolean init_Z_cR_StreamUtil(cR.Inner_StreamUtil p) {
      return p != null && this.load_Map.containsKey(p.id());
   }

   boolean load_Z_cR_StreamUtil(cR.Inner_StreamUtil p) {
      return this.init_Z_cR_StreamUtil(p) && !this.init_Z_Str(p.id()) && p.paste().isEmpty() && this.load_Map.get(p.id()).init_cQ_StreamUtil != null;
   }

   boolean read_Z(cR.Inner_StreamUtil p) {
      return p != null && (!this.flush_Z() || this.init_Z_Str(p.id()));
   }

   String load_Str() {
      if (this.init_cP != null) {
         return this.init_cP.init_Str;
      } else if (this.activeRecording != null) {
         return this.activeRecording.putAt_Str;
      } else if (this.init_cT != null) {
         return this.init_cT.init_cT_StreamUtil.phase();
      } else if (this.read_Z || this.init_Future != null) {
         return this.flush;
      } else if (!this.close.isEmpty()) {
         return this.close;
      } else {
         return !this.flush.isEmpty() ? this.flush : this.init_cR.init_Str;
      }
   }

   int init_I() {
      if (this.init_cP != null) {
         return this.init_cP.init_I;
      } else if (this.activeRecording != null) {
         return this.activeRecording.flowTemp();
      } else if (this.init_cT != null) {
         return this.init_cT.init_cT_StreamUtil.percent();
      } else {
         return this.init_Future != null ? this.load_I : -1;
      }
   }

   List<cR.Inner_StreamUtil> init_List() {
      return this.init_Map.values().stream().filter(p -> p.scope().equals(this.write_Str) && p.dimension().equals(this.update_Str)).toList();
   }

   cR.Inner_StreamUtil init_cR_StreamUtil_Str(String id) {
      cR.Inner_StreamUtil p = this.init_Map.get(id);
      return p != null && p.scope().equals(this.write_Str) && p.dimension().equals(this.update_Str) ? p : null;
   }

   cR.Inner_DataUtil init_cR_DataUtil_Str(String id) {
      for (cR.Inner_DataUtil e : this.init_cR.load_List) {
         if (e.id().equals(id)) {
            return e;
         }
      }

      return null;
   }

   String init_Str_cR_StreamUtil(cR.Inner_StreamUtil p) {
      if (this.init_Z_Str(p.id())) {
         return "Building";
      } else if (!p.paste().isEmpty()) {
         return "Paste paused";
      } else if (p.id().equals(this.sync)) {
         return "Loading";
      } else {
         cV.Inner_StreamUtil data = this.load_Map.get(p.id());
         if (data != null && data.init_Str != null) {
            return "Preview unavailable";
         } else if (p.visible() && data == null) {
            return this.init_Set.contains(p.id()) ? "File unavailable" : "Waiting to load";
         } else {
            return p.visible() ? "Visible" : "Unloaded";
         }
      }
   }

   void write_V() {
      if (this.load_Z) {
         this.close = "";
         this.flush = "";
         this.init_Set.clear();
         this.init_cR.init_V_Consumer(this.init_Minecraft::execute);
      }
   }

   void update_V() {
      if (this.load_Z) {
         String selected = TinyFileDialogs.tinyfd_selectFolderDialog("Add schematic folder", this.init_cR.load_Path.toString());
         if (selected != null && !selected.isBlank()) {
            this.init_V_Str(selected);
         }
      }
   }

   void init_V_Str(String path) {
      if (this.load_Z) {
         cR.init_ExecutorService.execute(() -> {
            try {
               Path folder = Path.of(path).toRealPath();
               if (!Files.isDirectory(folder)) {
                  throw new IllegalArgumentException("Choose an existing folder");
               }

               this.init_Minecraft.execute(() -> {
                  LinkedHashSet<Path> roots = new LinkedHashSet<>(this.init_cR.init_List);
                  if (roots.size() >= 32 && !roots.contains(folder)) {
                     this.read("Up to 32 folders can be indexed");
                  } else if (roots.add(folder)) {
                     this.init_cR.init_List = List.copyOf(roots);
                     this.push();
                     this.write_V();
                  }
               });
            } catch (Exception e) {
               this.init_Minecraft.execute(() -> this.read(cR.init_Str_Throwable(e)));
            }
         });
      }
   }

   void init_V_Path(Path root) {
      if (this.load_Z && !root.equals(this.init_cR.load_Path)) {
         this.init_cR.init_List = this.init_cR.init_List.stream().filter(p -> !p.equals(root)).toList();
         this.push();
         this.write_V();
      }
   }

   void load_V_Path(Path path) {
      Compat263.openPath(Util.getPlatform(), path);
   }

   void sync_V() {
      if (this.write_Z() && !this.flush_Z()) {
         cU selector = new cU(this.init_Minecraft);
         if (!aP.init_Z_aR_Runnable(selector, () -> this.init_cP_DataUtil = selector.init_cP_DataUtil())) {
            this.read("Stop the current AI task before selecting an area");
         }
      }
   }

   void init_V_Z_Z(boolean allLoaded, boolean entities) {
      if (this.read_Z() && this.write_Z() && !this.flush_Z()) {
         if (!allLoaded && this.init_cP_DataUtil == null) {
            this.read("Select two corners first");
         } else {
            try {
               this.init_cP = new cP(this.init_Minecraft, this.init_cR.load_Path, this.init_Str, allLoaded ? null : this.init_cP_DataUtil, entities);
               this.close = "";
               this.flush = "";
            } catch (Exception e) {
               this.read(cR.init_Str_Throwable(e));
            }
         }
      }
   }

   SchematicRecording activeRecording() {
      return this.activeRecording;
   }

   void startRecording(boolean entities) {
      if (this.read_Z() && this.write_Z() && !this.flush_Z()) {
         try {
            this.activeRecording = new SchematicRecording(this.init_Minecraft, this.init_cR.load_Path, this.init_Str, entities);
            this.close = "";
            this.flush = "";
         } catch (Exception e) {
            this.read(cR.init_Str_Throwable(e));
         }
      }
   }

   void stopAndSaveRecording() {
      if (this.activeRecording != null) {
         try {
            this.activeRecording.putAt_V_Str(this.init_Str);
         } catch (IllegalArgumentException e) {
            this.read(cR.init_Str_Throwable(e));
         }
      }
   }

   void init_V_cR_DataUtil(cR.Inner_DataUtil file) {
      if (this.read_Z() && !this.flush_Z() && file != null) {
         PromptDialogScreen screen = new PromptDialogScreen("Rename file", file.name(), name -> this.init_V_cR_DataUtil_Str(file, name));
         autismclient.util.OverlayScreenController.init_Z(this.init_Minecraft, screen);
      }
   }

   private void init_V_cR_DataUtil_Str(cR.Inner_DataUtil file, String name) {
      if (this.read_Z() && !this.flush_Z() && this.init_cR_DataUtil_Str(file.id()) != null) {
         this.read_Z = true;
         this.flush = "Renaming file";
         this.close = "";

         try {
            cR.init_ExecutorService.execute(() -> {
               try {
                  cR.Inner_DataUtil updated = this.init_cR.init_cR_DataUtil_cR_DataUtil_Str(file, name);
                  this.init_Minecraft.execute(() -> {
                     this.read_Z = false;
                     this.init_Map.replaceAll((id, p) -> p.file().equals(file.file().toString()) ? p.movedTo(updated) : p);
                     this.load_Str = updated.id();
                     this.init_Set.clear();
                     this.init_cR.init_V_cR_DataUtil(file, updated, this.init_Minecraft::execute);
                     this.push();
                     this.flush = "";
                     autismclient.util.bi.load("Renamed to " + updated.name());
                  });
               } catch (Exception e) {
                  this.init_Minecraft.execute(() -> {
                     this.read_Z = false;
                     this.read(cR.init_Str_Throwable(e));
                  });
               }
            });
         } catch (RejectedExecutionException busy) {
            this.read_Z = false;
            this.read("Schematic files are busy. Try again");
         }
      }
   }

   void init_V_cR_StreamUtil(cR.Inner_StreamUtil p) {
      if (!this.flush_Z() && p != null) {
         PromptDialogScreen screen = new PromptDialogScreen("Rename schematic", load_Str_cR_StreamUtil(p), name -> {
            cR.Inner_StreamUtil current = this.init_cR_StreamUtil_Str(p.id());
            if (current != null && !this.flush_Z()) {
               this.init_Map.put(current.id(), current.named(name));
               this.push();
            }
         });
         autismclient.util.OverlayScreenController.init_Z(this.init_Minecraft, screen);
      }
   }

   void load_V_cR_DataUtil(cR.Inner_DataUtil file) {
      if (this.read_Z() && this.write_Z() && !this.flush_Z() && file != null) {
         if (this.init_Map.size() >= 256) {
            this.read("Remove an existing placement first");
         } else {
            BlockPos at = this.init_BlockPos_Z_D(true, 4.0);
            cR.Inner_StreamUtil row = new cR.Inner_StreamUtil(
               UUID.randomUUID().toString(),
               this.write_Str,
               this.update_Str,
               file.file().toString(),
               file.bytes(),
               file.modified(),
               at.getX(),
               at.getY(),
               at.getZ(),
               0,
               "none",
               true,
               "",
               new JsonObject()
            );
            this.init_V_cR_StreamUtil_Z(row, true);
         }
      }
   }

   void init_V_cR_StreamUtil_Z(cR.Inner_StreamUtil p, boolean add) {
      if (p != null && this.init_Future == null && this.write_Z()) {
         Path file = Path.of(p.file());
         if (this.init_cR.init_Z_Path(file)) {
            long generation = ++this.init_J;
            ClientLevel capturedWorld = this.init_ClientLevel;
            this.sync = p.id();
            this.load_I = 0;
            this.flush = "Reading schematic";
            this.close = "";
            this.init_Future = cR.init_ExecutorService.submit(() -> {
               try {
                  init_V_cR_StreamUtil_Path(p, file);
                  cQ document = cQ.init_cQ_Path(file);
                  init_V_cR_StreamUtil_Path(p, file);
                  cQ.Inner_StreamUtil preview = null;
                  String previewError = null;

                  try {
                     preview = document.init_cQ_StreamUtil_I(p.rotation(), p.mirror(), value -> this.load_I = 15 + value * 70 / 100);
                  } catch (CancellationException e) {
                     throw e;
                  } catch (IllegalArgumentException e) {
                     previewError = cR.init_Str_Throwable(e);
                  }

                  cV.Inner_StreamUtil data = new cV.Inner_StreamUtil(document, preview, previewError);
                  this.init_Minecraft.execute(() -> {
                     if (generation == this.init_J && capturedWorld == this.init_ClientLevel) {
                        this.init_Future = null;
                        this.sync = "";
                        if (add) {
                           this.init_Map.put(p.id(), p);
                           this.read_Str = p.id();
                           this.push();
                        }

                        this.load_Map.put(p.id(), data);
                        this.flush = data.init_Str != null ? "Loaded · " + data.init_Str : "Loaded " + load_Str_cR_StreamUtil(p);
                     }
                  });
               } catch (CancellationException var13) {
               } catch (Exception e) {
                  this.init_Minecraft.execute(() -> {
                     if (this.init_J == generation) {
                        this.init_Future = null;
                        this.sync = "";
                        this.init_Set.add(p.id());
                        this.read(cR.init_Str_Throwable(e));
                     }
                  });
               }
            });
         } else {
            this.init_Set.add(p.id());
            if (add || p.id().equals(this.read_Str)) {
               this.read("Add this schematic's folder to the library");
            }
         }
      }
   }

   void load_V_cR_StreamUtil(cR.Inner_StreamUtil p) {
      p = p == null ? null : this.init_cR_StreamUtil_Str(p.id());
      if (this.read_Z(p)) {
         if (p.visible()) {
            if (this.init_Z_Str(p.id())) {
               this.flush_V();
            }

            this.init_Map.put(p.id(), p.shown(false));
            this.load_V_Str(p.id());
         } else {
            this.init_Map.put(p.id(), p.shown(true));
            this.init_Set.remove(p.id());
         }

         this.push();
      }
   }

   void read_V(cR.Inner_StreamUtil p) {
      p = p == null ? null : this.init_cR_StreamUtil_Str(p.id());
      if (this.read_Z(p)) {
         if (this.init_Z_Str(p.id())) {
            this.flush_V();
         }

         this.init_Map.remove(p.id());
         this.load_V_Str(p.id());
         this.push();
      }
   }

   void write(cR.Inner_StreamUtil p) {
      if (this.load_Z_cR_StreamUtil(p) && !this.flush_Z()) {
         cV.Inner_StreamUtil data = this.load_Map.get(p.id());
         if (data.init_cV_CachePool == null) {
            data.init_cV_CachePool = new cV.Inner_CachePool(this, p.id());
         }

         if (!aP.init_Z_aR_Runnable(data.init_cV_CachePool, this::push)) {
            this.read("Stop the current AI task before moving a hologram");
         }
      }
   }

   void init_V_Str_BlockPos(String id, BlockPos at) {
      cR.Inner_StreamUtil placement = this.init_Map.get(id);
      if (placement != null && !init_BlockPos_cR_StreamUtil(placement).equals(at)) {
         this.init_Map.put(id, placement.at(at.getX(), at.getY(), at.getZ(), placement.rotation(), placement.mirror()));
      }
   }

   void init(cR.Inner_StreamUtil p, boolean includeAir, boolean includeEntities) {
      if (p != null && this.update_Z() && this.init_Z_cR_StreamUtil(p) && !this.flush_Z() && !this.load_Z()) {
         JsonObject checkpoint = p.paste();

         try {
            if (!checkpoint.isEmpty()) {
               includeAir = checkpoint.get("includeAir").getAsBoolean();
               includeEntities = checkpoint.get("includeEntities").getAsBoolean();
            }

            this.init_cT = cT.init(
               this.init_Minecraft,
               this.load_Map.get(p.id()).init_cQ,
               init_BlockPos_cR_StreamUtil(p),
               p.rotation(),
               p.mirror(),
               includeAir,
               includeEntities,
               checkpoint
            );
            this.open = p.id();
            cV.Inner_StreamUtil preview = this.load_Map.get(p.id());
            cG.init_V(preview.init_cV_CachePool == null ? preview : preview.init_cV_CachePool);
            this.init_Map.put(p.id(), p.checkpoint(this.init_cT.init_cT_StreamUtil.checkpoint()));
            this.push();
            this.close = "";
            this.flush = "";
            if (this.init_SchematicLibraryOv != null) {
               this.init_SchematicLibraryOv.init_V_Z(false);
            }

            autismclient.util.OverlayScreenController.init_V(this.init_Minecraft, null);
         } catch (Exception e) {
            this.read(cR.init_Str_Throwable(e));
         }
      }
   }

   void update(cR.Inner_StreamUtil p) {
      p = p == null ? null : this.init_cR_StreamUtil_Str(p.id());
      if (p != null && p.visible() && p.paste().isEmpty() && this.write_Z() && this.init_Z_cR_StreamUtil(p) && !this.flush_Z() && !this.load_Z()) {
         cV.Inner_StreamUtil data = this.load_Map.get(p.id());
         if (data.init_cQ_StreamUtil == null) {
            this.read("This schematic exceeds the AI builder's preview limit");
         } else if (data.init_cQ_StreamUtil.blocks() > 2097152L) {
            this.read("This schematic exceeds the AI builder's block limit");
         } else {
            cR.Inner_DataUtil entry = this.init_cR_DataUtil_Str(cR.init_Str_Path(Path.of(p.file())));
            if (entry != null && entry.bytes() == p.bytes() && entry.modified() == p.modified()) {
               BlockPos at = init_BlockPos_cR_StreamUtil(p).offset(data.init_cQ_StreamUtil.nonAirMinimum());

               try {
                  if (data.init_cQ_StreamUtil.blocks() == 0L) {
                     throw new IllegalArgumentException("This schematic contains only air");
                  }

                  JsonObject args = ag.init_JsonObject_ObjArr(
                     "schematic",
                     "sc_" + entry.id(),
                     "origin",
                     ag.init_JsonObject_ObjArr("x", at.getX(), "y", at.getY(), "z", at.getZ()),
                     "rotation",
                     p.rotation(),
                     "mirror",
                     p.mirror()
                  );
                  this.init_cV_DataUtil = new cV.Inner_DataUtil(p.id(), o.init_k_StreamUtil("game:build", args));
                  autismclient.util.OverlayScreenController.init_V(this.init_Minecraft, null);
               } catch (Exception e) {
                  this.read(cR.init_Str_Throwable(e));
               }
            } else {
               this.read("Refresh the file list and load the current file");
            }
         }
      }
   }

   void flush_V() {
      if (this.open_Z()) {
         autismclient.util.bi.read("Build stopped");
      }
   }

   private boolean open_Z() {
      cV.Inner_DataUtil active = this.init_cV_DataUtil();
      this.init_cV_DataUtil = null;
      if (active == null) {
         return false;
      }

      o.init(active.job());
      return true;
   }

   void close_V() {
      this.close_V(false);
   }

   private void close_V(boolean keepRecording) {
      if (this.init_cP != null) {
         this.init_cP.init_V();
         this.init_cP = null;
         this.flush = "Save cancelled";
      }

      if (this.activeRecording != null && !keepRecording) {
         this.activeRecording.fill_V();
         this.activeRecording = null;
         cG.init_V(this.recordingVisOwner);
         this.flush = "Recording cancelled";
      }

      if (this.init_cT != null) {
         this.init_cT.init();
         this.flush = "Pausing paste";
      }

      if (this.init_Future != null) {
         this.init_J++;
         this.init_Future.cancel(true);
         this.init_Future = null;
         this.sync = "";
         this.flush = "Loading cancelled";
      }
   }

   public void open_V() {
      if (this.init_ClientLevel != this.init_Minecraft.level) {
         this.open_Z();
         if (this.activeRecording != null && this.activeRecording.putAt_Z()) {
            this.activeRecording.putAt_V();
         }

         this.close_V(true);
         this.poll();
         this.init_cP_DataUtil = null;
         this.init_ClientLevel = this.init_Minecraft.level;
         this.write_Str = this.init_ClientLevel == null ? "" : init_Str_Minecraft(this.init_Minecraft);
         this.update_Str = this.init_ClientLevel == null ? "" : cL.load(this.init_Minecraft);
         this.init_Set.clear();
         this.close = "";
         this.flush = "";
      }

      if (this.init_cV_DataUtil != null && this.init_cV_DataUtil.job().load().isDone()) {
         this.init_cV_DataUtil = null;
      }

      if (this.load_Z) {
         if (this.init_cP != null) {
            this.init_cP.init_V_Minecraft(this.init_Minecraft);
            if (this.init_cP.load_Z || this.init_cP.init_Z) {
               cP finished = this.init_cP;
               this.init_cP = null;
               if (finished.init_Path != null) {
                  this.write_V();
                  this.flush = "Saved " + finished.init_Path.getFileName();
                  autismclient.util.bi.load(this.flush);
               } else if (!finished.load_Str.isEmpty()) {
                  this.read(finished.load_Str);
               }
            }
         }

         if (this.activeRecording != null) {
            this.activeRecording.putAt_V_Minecraft(this.init_Minecraft);
            if (!this.activeRecording.readEx_Z && !this.activeRecording.fill_Z) {
               cG.putAt(
                  this.recordingVisOwner,
                  "Recording chunks",
                  this.activeRecording.putAt_Z ? this.activeRecording.putAt_Str : "",
                  this.activeRecording.putAt_Str(),
                  this.activeRecording.flowTemp()
               );
            } else {
               SchematicRecording finished = this.activeRecording;
               this.activeRecording = null;
               cG.init_V(this.recordingVisOwner);
               if (finished.putAt_Path != null) {
                  this.write_V();
                  if (finished.fill_Z()) {
                     this.flush = "Recording limit reached · saved " + finished.putAt_Path.getFileName();
                     autismclient.util.bi.read(this.flush);
                  } else {
                     this.flush = "Saved " + finished.putAt_Path.getFileName();
                     autismclient.util.bi.load(this.flush);
                  }
               } else if (!finished.fill_Str.isEmpty()) {
                  this.read(finished.fill_Str);
               } else {
                  this.flush = "Recording cancelled";
               }
            }
         }

         if (this.init_cT != null) {
            boolean terminal = this.init_cT.load_Z;
            cT.Inner_StreamUtil progress = this.init_cT.init_cT_StreamUtil;
            cG.init(this.init_Obj, "Schematic paste", progress.phase(), progress.percent(), 100);
            if (terminal || System.nanoTime() - this.load_J > 2000000000L) {
               cR.Inner_StreamUtil p = this.init_Map.get(this.open);
               if (p != null) {
                  this.init_Map
                     .put(p.id(), p.checkpoint(progress.complete() ? new JsonObject() : progress.checkpoint()).shown(!progress.complete() && p.visible()));
                  this.push();
               }

               this.load_J = System.nanoTime();
            }

            if (terminal) {
               if (progress.complete()) {
                  this.load_V_Str(this.open);
               }

               this.init_cT = null;
               this.open = null;
               cG.init_V(this.init_Obj);
               if (!progress.error().isEmpty()) {
                  this.read(progress.error());
               } else {
                  this.flush = progress.complete() ? "Paste complete" : "Paste paused";
                  if (progress.complete()) {
                     autismclient.util.bi.load(this.flush);
                  }
               }
            }
         }

         if (this.write_Z()) {
            if (!this.flush_Z()) {
               for (cR.Inner_StreamUtil p : this.init_List()) {
                  if ((p.visible() || !p.paste().isEmpty()) && !this.load_Map.containsKey(p.id()) && !this.init_Set.contains(p.id())) {
                     this.init_V_cR_StreamUtil_Z(p, false);
                     break;
                  }
               }
            }

            long now = System.nanoTime();
            cV.Inner_StreamUtil oldest = null;
            cR.Inner_StreamUtil row = null;

            for (Entry<String, cV.Inner_StreamUtil> entry : this.load_Map.entrySet()) {
               cR.Inner_StreamUtil p = this.init_Map.get(entry.getKey());
               cV.Inner_StreamUtil data = entry.getValue();
               if (p != null && p.visible() && !p.id().equals(this.open) && data.init_cQ_StreamUtil != null && !data.init_cQ_StreamUtil.cells().isEmpty()) {
                  cG.init(data.init_cV_CachePool == null ? data : data.init_cV_CachePool, now);
                  if (oldest == null || data.init_J < oldest.init_J) {
                     oldest = data;
                     row = p;
                  }
               }
            }

            if (oldest != null) {
               oldest.init_J = now;
               cR.Inner_StreamUtil p = row;
               cV.Inner_StreamUtil data = oldest;
               Object owner = data.init_cV_CachePool == null ? data : data.init_cV_CachePool;
               cG.init_V_Obj_2(
                  owner, init_BlockPos_cR_StreamUtil(p), data.init_cQ_StreamUtil.cells(), at -> data.init_cQ.init_Z_BlockPos(at, p.rotation(), p.mirror())
               );
            }
         }
      }
   }

   private void load_V_Str(String id) {
      cV.Inner_StreamUtil data = this.load_Map.remove(id);
      if (data != null) {
         cG.init_V(data);
         if (data.init_cV_CachePool != null) {
            cG.init_V(data.init_cV_CachePool);
         }
      }
   }

   private void poll() {
      for (String id : List.copyOf(this.load_Map.keySet())) {
         this.load_V_Str(id);
      }
   }

   private void read(String message) {
      this.close = message;
      this.flush = "";
      autismclient.util.bi.write(message);
   }

   private void push() {
      if (this.load_Z) {
         if (this.write_Z) {
            this.update_Z = true;
         } else {
            this.write_Z = true;
            this.update_Z = false;
            cR.Inner_CachePool snapshot = new cR.Inner_CachePool(
               this.init_cR.init_List.stream().map(Path::toString).toList(), List.copyOf(this.init_Map.values())
            );
            cR.init_ExecutorService.execute(() -> {
               String problem = "";

               try {
                  this.init_cR.load_V_cR_CachePool(snapshot);
               } catch (Exception e) {
                  problem = cR.init_Str_Throwable(e);
               }

               String failure = problem;
               this.init_Minecraft.execute(() -> {
                  this.write_Z = false;
                  if (!failure.isEmpty()) {
                     this.read("Could not save schematic settings: " + failure);
                  }

                  if (this.update_Z) {
                     this.push();
                  }
               });
            });
         }
      }
   }

   private static void init_V_cR_StreamUtil_Path(cR.Inner_StreamUtil p, Path file) throws IOException {
      BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
      if (!attributes.isRegularFile() || attributes.size() != p.bytes() || attributes.lastModifiedTime().toMillis() != p.modified()) {
         throw new IllegalArgumentException("Schematic changed. Refresh and load it again");
      }
   }

   static BlockPos init_BlockPos_cR_StreamUtil(cR.Inner_StreamUtil p) {
      return new BlockPos(p.x(), p.y(), p.z());
   }

   static String load_Str_cR_StreamUtil(cR.Inner_StreamUtil p) {
      return p.name().isBlank() ? Path.of(p.file()).getFileName().toString() : p.name();
   }

   private static String init_Str_Minecraft(Minecraft mc) {
      if (mc.hasSingleplayerServer()) {
         return cL.init(mc);
      }

      ServerData server = mc.getCurrentServer();
      return server != null && !server.ip.isBlank() ? "server/" + ag.init_Str_Str(server.ip.trim().toLowerCase(Locale.ROOT)) : cL.init(mc);
   }

   private BlockPos init_BlockPos_Z_D(boolean surface, double depth) {
      Vec3 eye = cU.init_Vec3_Minecraft(this.init_Minecraft);
      Vec3 direction = cU.load_Vec3_Minecraft(this.init_Minecraft);
      Vec3 end = eye.add(direction.scale(depth));
      if (surface) {
         BlockHitResult hit = this.init_Minecraft
            .level
            .clip(new ClipContext(eye, eye.add(direction.scale(96.0)), Block.OUTLINE, Fluid.NONE, this.init_Minecraft.player));
         if (hit.getType() == Type.BLOCK) {
            return hit.getBlockPos().relative(hit.getDirection());
         }
      }

      return BlockPos.containing(end);
   }

   static final class Inner_CachePool extends aR {
      private final cV init_cV;
      private final String init_Str;
      private double init_D = 4.0;

      Inner_CachePool(cV workspace, String id) {
         this.init_cV = workspace;
         this.init_Str = id;
         cV.Inner_StreamUtil data = workspace.load_Map.get(id);
         cG.init_V(data);
      }

      private cR.Inner_StreamUtil init_cR_StreamUtil() {
         return this.init_cV.init_Map.get(this.init_Str);
      }

      @Override
      boolean load_Z() {
         return this.read_Z() && this.init_cV.init_Future == null;
      }

      @Override
      boolean read_Z() {
         return this.init_cV.write_Z() && this.init_cV.init_cR_StreamUtil_Str(this.init_Str) != null && this.init_cV.load_Map.containsKey(this.init_Str);
      }

      @Override
      BlockPos init_BlockPos() {
         return cV.init_BlockPos_cR_StreamUtil(this.init_cR_StreamUtil());
      }

      @Override
      int init_I() {
         return this.init_cR_StreamUtil().rotation();
      }

      @Override
      String read_Str() {
         return this.init_cR_StreamUtil().mirror();
      }

      @Override
      void init_V_Z(boolean surface) {
         this.init(this.init_cV.init_BlockPos_Z_D(surface, this.init_D));
      }

      @Override
      void init(double scroll, double x, double y, double w, double h) {
         this.init_D = Math.max(1.0, Math.min(96.0, this.init_D + scroll));
         this.init_V_Z(false);
      }

      @Override
      void load_V() {
         BlockPos p = this.init_BlockPos();
         ClientLevel level = this.init_cV.init_ClientLevel;
         if (level.hasChunkAt(p)) {
            int y = level.getHeight(Types.MOTION_BLOCKING, p.getX(), p.getZ());
            this.init(new BlockPos(p.getX(), y, p.getZ()));
         }
      }

      @Override
      void init(int x, int y, int z) {
         this.init(this.init_BlockPos().offset(x, y, z));
      }

      private void init(BlockPos at) {
         this.init_cV.init_V_Str_BlockPos(this.init_Str, at);
      }

      @Override
      void init_V_I(int steps) {
         this.init(Math.floorMod(this.init_I() + steps * 90, 360), this.read_Str());
      }

      @Override
      void init_V() {
         int var10001 = this.init_I();
         String var10002;
         switch (this.read_Str()) {
            case "none":
               var10002 = "x";
               break;
            case "x":
               var10002 = "z";
               break;
            default:
               var10002 = "none";
         }

         this.init(var10001, var10002);
      }

      @Override
      void init(BlockPos at, int r, String m) {
         this.init(at);
         if (this.init_I() != r || !this.read_Str().equals(m)) {
            this.init(r, m);
         }
      }

      @Override
      void read_V() {
      }

      private void init(int r, String m) {
         cR.Inner_StreamUtil p = this.init_cR_StreamUtil();
         cV.Inner_StreamUtil data = this.init_cV.load_Map.get(this.init_Str);
         if (r == p.rotation() && m.equals(p.mirror())) {
            if (this.init_cV.sync.equals(this.init_Str)) {
               this.init_cV.close_V();
            }
         } else {
            if (this.init_cV.init_Future != null) {
               this.init_cV.close_V();
            }

            long token = ++this.init_cV.init_J;
            this.init_cV.sync = this.init_Str;
            this.init_cV.flush = "Rotating hologram";
            this.init_cV.load_I = 0;
            this.init_cV.init_Future = cR.init_ExecutorService.submit(() -> {
               try {
                  cQ.Inner_StreamUtil result = data.init_cQ.init_cQ_StreamUtil_I(r, m, value -> this.init_cV.load_I = value);
                  this.init_cV.init_Minecraft.execute(() -> {
                     if (this.init_cV.init_J == token && this.read_Z()) {
                        cR.Inner_StreamUtil latest = this.init_cR_StreamUtil();
                        this.init_cV.init_Map.put(this.init_Str, latest.at(latest.x(), latest.y(), latest.z(), r, m));
                        data.init_cQ_StreamUtil = result;
                        this.init_cV.init_Future = null;
                        this.init_cV.sync = "";
                        this.init_cV.flush = "";
                     }
                  });
               } catch (CancellationException var7) {
               } catch (Exception e) {
                  this.init_cV.init_Minecraft.execute(() -> {
                     if (this.init_cV.init_J == token) {
                        this.init_cV.init_Future = null;
                        this.init_cV.sync = "";
                        this.init_cV.read(cR.init_Str_Throwable(e));
                     }
                  });
               }
            });
         }
      }
   }

   private record Inner_DataUtil(String placementId, autismclient.util.macro.MacroToolBridge.Inner_StreamUtil job) {
   }

   private static final class Inner_StreamUtil {
      final cQ init_cQ;
      cQ.Inner_StreamUtil init_cQ_StreamUtil;
      String init_Str;
      cV.Inner_CachePool init_cV_CachePool;
      long init_J;

      Inner_StreamUtil(cQ document, cQ.Inner_StreamUtil preview, String previewError) {
         this.init_cQ = document;
         this.init_cQ_StreamUtil = preview;
         this.init_Str = previewError;
      }
   }
}
