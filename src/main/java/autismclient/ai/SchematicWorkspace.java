package autismclient.ai;

import autismclient.compat263.Compat263;
import autismclient.gui.screens.PromptDialogScreen;
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

public final class SchematicWorkspace {
   private static SchematicWorkspace instance;
   private static final LongOpenHashSet loadedChunkKeys = new LongOpenHashSet();
   private static ClientLevel loadedChunkLevel;
   static final int init_I = 256;
   private final Minecraft mc;
   final SchematicLibrary library;
   private final Map<String, SchematicLibrary.Inner_StreamUtil> placementsById = new LinkedHashMap<>();
   private final Map<String, SchematicWorkspace.Inner_StreamUtil> loaded = new LinkedHashMap<>();
   private final Set<String> unavailable = new HashSet<>();
   private ClientLevel trackedLevel;
   private String scope = "";
   private String dimension = "";
   private boolean libraryReady;
   private SchematicLibraryOverlay libraryWindow;
   private Future<?> loadTask;
   private boolean renaming;
   private long loadGeneration;
   private String sync = "";
   private volatile int previewProgress;
   private String flush = "";
   private String close = "";
   private long load_J;
   private boolean write_Z;
   private boolean update_Z;
   private SchematicCapture capture;
   private SchematicRecording activeRecording;
   private SchematicPaste paste;
   private String open;
   private SchematicWorkspace.Inner_DataUtil buildJob;
   private final Object pasteVisualOwner = new Object();
   private final Object recordingVisOwner = new Object();
   SchematicCapture.Inner_DataUtil selectedArea;
   String captureName = "";
   String selectedFileId = "";
   String selectedPlacementId = "";

   public static SchematicWorkspace getInstance() {
      if (instance == null) {
         instance = new SchematicWorkspace(Minecraft.getInstance());
      }

      return instance;
   }

   public static void registerEvents() {
      ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> {
         if (loadedChunkLevel != level) {
            loadedChunkKeys.clear();
            loadedChunkLevel = level;
         }

         loadedChunkKeys.add(chunk.getPos().pack());
         if (instance != null && instance.activeRecording != null) {
            instance.activeRecording.putAt_V_ClientLevel_LevelChunk(level, chunk);
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

         if (instance == null && client.level != null && autismclient.e.FileCache.init()) {
            getInstance();
         }

         if (instance != null) {
            instance.open_V();
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

   public static void toggleWindow() {
      if (close_Z()) {
         SchematicWorkspace workspace = getInstance();
         OverlayManager manager = OverlayManager.getInstance();
         SchematicLibraryOverlay window = workspace.libraryWindow;
         if (window != null && window.isVisible() && manager.getOverlays().contains(window) && !manager.isHidden(window)) {
            window.setVisible(false);
         } else {
            workspace.showOverlay();
         }
      }
   }

   public static void restoreWindow() {
      if (close_Z()) {
         if (instance == null || instance.libraryWindow == null) {
            OverlayBounds saved = autismclient.util.AutismSessionState.getInstance().getOverlayBounds("schematic-library");
            if (saved == null || !saved.visible) {
               return;
            }

            SchematicWorkspace workspace = getInstance();
            workspace.libraryWindow = new SchematicLibraryOverlay(workspace);
         }

         if (instance.libraryWindow.isVisible()) {
            OverlayManager manager = OverlayManager.getInstance();
            manager.register(instance.libraryWindow);
            manager.setHidden(instance.libraryWindow, false);
         }
      }
   }

   private static boolean close_Z() {
      return !autismclient.modules.aD.init_Z() && autismclient.e.CharEncoder.init();
   }

   private SchematicWorkspace(Minecraft mc) {
      this.mc = mc;
      this.library = new SchematicLibrary(mc.gameDirectory.toPath());
      SchematicLibrary.EXECUTOR.execute(() -> {
         String problem = "";

         SchematicLibrary.Inner_CachePool state;
         try {
            state = this.library.init();
         } catch (Exception e) {
            state = new SchematicLibrary.Inner_CachePool(List.of(this.library.defaultRoot.toString()), List.of());
            problem = SchematicLibrary.describeError(e);
         }

         SchematicLibrary.Inner_CachePool result = state;
         String failure = problem;
         mc.execute(() -> {
            this.library.applyState(result);
            result.placements().forEach(p -> this.placementsById.put(p.id(), p));
            this.libraryReady = true;
            if (!failure.isEmpty()) {
               this.close = "Could not read saved placements: " + failure;
            }

            this.refresh();
         });
      });
   }

   SchematicLibraryOverlay showOverlay() {
      if (this.libraryWindow == null) {
         this.libraryWindow = new SchematicLibraryOverlay(this);
      }

      OverlayManager manager = OverlayManager.getInstance();
      if (this.mc.gui.screen() == null) {
         autismclient.util.OverlayScreenController.setScreenExplicit(this.mc, new autismclient.gui.screens.AutismOverlaysScreen(this.libraryWindow, null, false, false));
      }

      manager.register(this.libraryWindow);
      manager.setHidden(this.libraryWindow, false);
      this.libraryWindow.setVisible(true);
      this.libraryWindow.setCollapsed(false);
      manager.bringToFront(this.libraryWindow);
      return this.libraryWindow;
   }

   boolean isPasting() {
      return this.paste != null;
   }

   private SchematicWorkspace.Inner_DataUtil activeBuild() {
      return this.buildJob != null && !this.buildJob.job().load().isDone() ? this.buildJob : null;
   }

   boolean hasActiveBuild() {
      return this.activeBuild() != null;
   }

   private boolean isBuilding(String placementId) {
      SchematicWorkspace.Inner_DataUtil active = this.activeBuild();
      return active != null && active.placementId().equals(placementId);
   }

   String activeBuildLabel() {
      SchematicWorkspace.Inner_DataUtil active = this.activeBuild();
      SchematicLibrary.Inner_StreamUtil placement = active == null ? null : this.placementById(active.placementId());
      return placement == null ? "" : displayName(placement);
   }

   SchematicLibrary.Inner_StreamUtil placementForFile(SchematicLibrary.Inner_DataUtil file) {
      if (file == null) {
         return null;
      }

      String path = file.file().toString();
      SchematicWorkspace.Inner_DataUtil active = this.activeBuild();
      SchematicLibrary.Inner_StreamUtil building = active == null ? null : this.placementById(active.placementId());
      if (building != null && building.visible() && building.file().equals(path)) {
         return building;
      }

      for (SchematicLibrary.Inner_StreamUtil placement : this.placementsById.values()) {
         if (placement.scope().equals(this.scope) && placement.dimension().equals(this.dimension) && placement.visible() && placement.file().equals(path)) {
            return placement;
         }
      }

      return null;
   }

   boolean isLibraryReady() {
      return this.libraryReady;
   }

   boolean isInWorld() {
      return this.trackedLevel != null && this.mc.player != null && !this.scope.isEmpty();
   }

   boolean isLocalWorld() {
      IntegratedServer server = this.mc.getSingleplayerServer();
      return this.isInWorld() && server != null && !server.isPublished();
   }

   boolean sync_Z() {
      return this.renaming;
   }

   boolean isBusy() {
      return this.renaming || this.loadTask != null || this.capture != null || this.activeRecording != null || this.paste != null;
   }

   boolean isLoaded(SchematicLibrary.Inner_StreamUtil p) {
      return p != null && this.loaded.containsKey(p.id());
   }

   boolean canMove(SchematicLibrary.Inner_StreamUtil p) {
      return this.isLoaded(p) && !this.isBuilding(p.id()) && p.paste().isEmpty() && this.loaded.get(p.id()).preview != null;
   }

   boolean canModify(SchematicLibrary.Inner_StreamUtil p) {
      return p != null && (!this.isBusy() || this.isBuilding(p.id()));
   }

   String getStatus() {
      if (this.capture != null) {
         return this.capture.stage;
      } else if (this.activeRecording != null) {
         return this.activeRecording.putAt_Str;
      } else if (this.paste != null) {
         return this.paste.progress.phase();
      } else if (this.renaming || this.loadTask != null) {
         return this.flush;
      } else if (!this.close.isEmpty()) {
         return this.close;
      } else {
         return !this.flush.isEmpty() ? this.flush : this.library.status;
      }
   }

   int getProgress() {
      if (this.capture != null) {
         return this.capture.progress;
      } else if (this.activeRecording != null) {
         return this.activeRecording.flowTemp();
      } else if (this.paste != null) {
         return this.paste.progress.percent();
      } else {
         return this.loadTask != null ? this.previewProgress : -1;
      }
   }

   List<SchematicLibrary.Inner_StreamUtil> placements() {
      return this.placementsById.values().stream().filter(p -> p.scope().equals(this.scope) && p.dimension().equals(this.dimension)).toList();
   }

   SchematicLibrary.Inner_StreamUtil placementById(String id) {
      SchematicLibrary.Inner_StreamUtil p = this.placementsById.get(id);
      return p != null && p.scope().equals(this.scope) && p.dimension().equals(this.dimension) ? p : null;
   }

   SchematicLibrary.Inner_DataUtil libraryEntry(String id) {
      for (SchematicLibrary.Inner_DataUtil e : this.library.entries) {
         if (e.id().equals(id)) {
            return e;
         }
      }

      return null;
   }

   String placementStatus(SchematicLibrary.Inner_StreamUtil p) {
      if (this.isBuilding(p.id())) {
         return "Building";
      } else if (!p.paste().isEmpty()) {
         return "Paste paused";
      } else if (p.id().equals(this.sync)) {
         return "Loading";
      } else {
         SchematicWorkspace.Inner_StreamUtil data = this.loaded.get(p.id());
         if (data != null && data.previewError != null) {
            return "Preview unavailable";
         } else if (p.visible() && data == null) {
            return this.unavailable.contains(p.id()) ? "File unavailable" : "Waiting to load";
         } else {
            return p.visible() ? "Visible" : "Unloaded";
         }
      }
   }

   void refresh() {
      if (this.libraryReady) {
         this.close = "";
         this.flush = "";
         this.unavailable.clear();
         this.library.rescan(this.mc::execute);
      }
   }

   void addFolder() {
      if (this.libraryReady) {
         String selected = TinyFileDialogs.tinyfd_selectFolderDialog("Add schematic folder", this.library.defaultRoot.toString());
         if (selected != null && !selected.isBlank()) {
            this.addFolderAsync(selected);
         }
      }
   }

   void addFolderAsync(String path) {
      if (this.libraryReady) {
         SchematicLibrary.EXECUTOR.execute(() -> {
            try {
               Path folder = Path.of(path).toRealPath();
               if (!Files.isDirectory(folder)) {
                  throw new IllegalArgumentException("Choose an existing folder");
               }

               this.mc.execute(() -> {
                  LinkedHashSet<Path> roots = new LinkedHashSet<>(this.library.roots);
                  if (roots.size() >= 32 && !roots.contains(folder)) {
                     this.read("Up to 32 folders can be indexed");
                  } else if (roots.add(folder)) {
                     this.library.roots = List.copyOf(roots);
                     this.push();
                     this.refresh();
                  }
               });
            } catch (Exception e) {
               this.mc.execute(() -> this.read(SchematicLibrary.describeError(e)));
            }
         });
      }
   }

   void removeFolder(Path root) {
      if (this.libraryReady && !root.equals(this.library.defaultRoot)) {
         this.library.roots = this.library.roots.stream().filter(p -> !p.equals(root)).toList();
         this.push();
         this.refresh();
      }
   }

   void openPath(Path path) {
      Compat263.openPath(Util.getPlatform(), path);
   }

   void selectArea() {
      if (this.isInWorld() && !this.isBusy()) {
         SchematicSelection selector = new SchematicSelection(this.mc);
         if (!aP.init_Z_aR_Runnable(selector, () -> this.selectedArea = selector.getArea())) {
            this.read("Stop the current AI task before selecting an area");
         }
      }
   }

   void startCapture(boolean allLoaded, boolean entities) {
      if (this.isLibraryReady() && this.isInWorld() && !this.isBusy()) {
         if (!allLoaded && this.selectedArea == null) {
            this.read("Select two corners first");
         } else {
            try {
               this.capture = new SchematicCapture(this.mc, this.library.defaultRoot, this.captureName, allLoaded ? null : this.selectedArea, entities);
               this.close = "";
               this.flush = "";
            } catch (Exception e) {
               this.read(SchematicLibrary.describeError(e));
            }
         }
      }
   }

   SchematicRecording activeRecording() {
      return this.activeRecording;
   }

   void startRecording(boolean entities) {
      if (this.isLibraryReady() && this.isInWorld() && !this.isBusy()) {
         try {
            this.activeRecording = new SchematicRecording(this.mc, this.library.defaultRoot, this.captureName, entities);
            this.close = "";
            this.flush = "";
         } catch (Exception e) {
            this.read(SchematicLibrary.describeError(e));
         }
      }
   }

   void stopAndSaveRecording() {
      if (this.activeRecording != null) {
         try {
            this.activeRecording.putAt_V_Str(this.captureName);
         } catch (IllegalArgumentException e) {
            this.read(SchematicLibrary.describeError(e));
         }
      }
   }

   void renameFile(SchematicLibrary.Inner_DataUtil file) {
      if (this.isLibraryReady() && !this.isBusy() && file != null) {
         PromptDialogScreen screen = new PromptDialogScreen("Rename file", file.name(), name -> this.renameFile(file, name));
         autismclient.util.OverlayScreenController.openModalScreen(this.mc, screen);
      }
   }

   private void renameFile(SchematicLibrary.Inner_DataUtil file, String name) {
      if (this.isLibraryReady() && !this.isBusy() && this.libraryEntry(file.id()) != null) {
         this.renaming = true;
         this.flush = "Renaming file";
         this.close = "";

         try {
            SchematicLibrary.EXECUTOR.execute(() -> {
               try {
                  SchematicLibrary.Inner_DataUtil updated = this.library.rename(file, name);
                  this.mc.execute(() -> {
                     this.renaming = false;
                     this.placementsById.replaceAll((id, p) -> p.file().equals(file.file().toString()) ? p.movedTo(updated) : p);
                     this.selectedFileId = updated.id();
                     this.unavailable.clear();
                     this.library.replaceEntry(file, updated, this.mc::execute);
                     this.push();
                     this.flush = "";
                     autismclient.util.bi.load("Renamed to " + updated.name());
                  });
               } catch (Exception e) {
                  this.mc.execute(() -> {
                     this.renaming = false;
                     this.read(SchematicLibrary.describeError(e));
                  });
               }
            });
         } catch (RejectedExecutionException busy) {
            this.renaming = false;
            this.read("Schematic files are busy. Try again");
         }
      }
   }

   void renamePlacement(SchematicLibrary.Inner_StreamUtil p) {
      if (!this.isBusy() && p != null) {
         PromptDialogScreen screen = new PromptDialogScreen("Rename schematic", displayName(p), name -> {
            SchematicLibrary.Inner_StreamUtil current = this.placementById(p.id());
            if (current != null && !this.isBusy()) {
               this.placementsById.put(current.id(), current.named(name));
               this.push();
            }
         });
         autismclient.util.OverlayScreenController.openModalScreen(this.mc, screen);
      }
   }

   void placeFile(SchematicLibrary.Inner_DataUtil file) {
      if (this.isLibraryReady() && this.isInWorld() && !this.isBusy() && file != null) {
         if (this.placementsById.size() >= 256) {
            this.read("Remove an existing placement first");
         } else {
            BlockPos at = this.aimTarget(true, 4.0);
            SchematicLibrary.Inner_StreamUtil row = new SchematicLibrary.Inner_StreamUtil(
               UUID.randomUUID().toString(),
               this.scope,
               this.dimension,
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
            this.loadPlacement(row, true);
         }
      }
   }

   void loadPlacement(SchematicLibrary.Inner_StreamUtil p, boolean add) {
      if (p != null && this.loadTask == null && this.isInWorld()) {
         Path file = Path.of(p.file());
         if (this.library.isInRoots(file)) {
            long generation = ++this.loadGeneration;
            ClientLevel capturedWorld = this.trackedLevel;
            this.sync = p.id();
            this.previewProgress = 0;
            this.flush = "Reading schematic";
            this.close = "";
            this.loadTask = SchematicLibrary.EXECUTOR.submit(() -> {
               try {
                  init_V_cR_StreamUtil_Path(p, file);
                  SchematicDocument document = SchematicDocument.loadFile(file);
                  init_V_cR_StreamUtil_Path(p, file);
                  SchematicDocument.Inner_StreamUtil preview = null;
                  String previewError = null;

                  try {
                     preview = document.buildPreview(p.rotation(), p.mirror(), value -> this.previewProgress = 15 + value * 70 / 100);
                  } catch (CancellationException e) {
                     throw e;
                  } catch (IllegalArgumentException e) {
                     previewError = SchematicLibrary.describeError(e);
                  }

                  SchematicWorkspace.Inner_StreamUtil data = new SchematicWorkspace.Inner_StreamUtil(document, preview, previewError);
                  this.mc.execute(() -> {
                     if (generation == this.loadGeneration && capturedWorld == this.trackedLevel) {
                        this.loadTask = null;
                        this.sync = "";
                        if (add) {
                           this.placementsById.put(p.id(), p);
                           this.selectedPlacementId = p.id();
                           this.push();
                        }

                        this.loaded.put(p.id(), data);
                        this.flush = data.previewError != null ? "Loaded · " + data.previewError : "Loaded " + displayName(p);
                     }
                  });
               } catch (CancellationException var13) {
               } catch (Exception e) {
                  this.mc.execute(() -> {
                     if (this.loadGeneration == generation) {
                        this.loadTask = null;
                        this.sync = "";
                        this.unavailable.add(p.id());
                        this.read(SchematicLibrary.describeError(e));
                     }
                  });
               }
            });
         } else {
            this.unavailable.add(p.id());
            if (add || p.id().equals(this.selectedPlacementId)) {
               this.read("Add this schematic's folder to the library");
            }
         }
      }
   }

   void toggleShown(SchematicLibrary.Inner_StreamUtil p) {
      p = p == null ? null : this.placementById(p.id());
      if (this.canModify(p)) {
         if (p.visible()) {
            if (this.isBuilding(p.id())) {
               this.stopBuild();
            }

            this.placementsById.put(p.id(), p.shown(false));
            this.unloadPlacement(p.id());
         } else {
            this.placementsById.put(p.id(), p.shown(true));
            this.unavailable.remove(p.id());
         }

         this.push();
      }
   }

   void removePlacement(SchematicLibrary.Inner_StreamUtil p) {
      p = p == null ? null : this.placementById(p.id());
      if (this.canModify(p)) {
         if (this.isBuilding(p.id())) {
            this.stopBuild();
         }

         this.placementsById.remove(p.id());
         this.unloadPlacement(p.id());
         this.push();
      }
   }

   void write(SchematicLibrary.Inner_StreamUtil p) {
      if (this.canMove(p) && !this.isBusy()) {
         SchematicWorkspace.Inner_StreamUtil data = this.loaded.get(p.id());
         if (data.mover == null) {
            data.mover = new SchematicWorkspace.Inner_CachePool(this, p.id());
         }

         if (!aP.init_Z_aR_Runnable(data.mover, this::push)) {
            this.read("Stop the current AI task before moving a hologram");
         }
      }
   }

   void movePlacement(String id, BlockPos at) {
      SchematicLibrary.Inner_StreamUtil placement = this.placementsById.get(id);
      if (placement != null && !origin(placement).equals(at)) {
         this.placementsById.put(id, placement.at(at.getX(), at.getY(), at.getZ(), placement.rotation(), placement.mirror()));
      }
   }

   void init(SchematicLibrary.Inner_StreamUtil p, boolean includeAir, boolean includeEntities) {
      if (p != null && this.isLocalWorld() && this.isLoaded(p) && !this.isBusy() && !this.hasActiveBuild()) {
         JsonObject checkpoint = p.paste();

         try {
            if (!checkpoint.isEmpty()) {
               includeAir = checkpoint.get("includeAir").getAsBoolean();
               includeEntities = checkpoint.get("includeEntities").getAsBoolean();
            }

            this.paste = SchematicPaste.init(
               this.mc,
               this.loaded.get(p.id()).document,
               origin(p),
               p.rotation(),
               p.mirror(),
               includeAir,
               includeEntities,
               checkpoint
            );
            this.open = p.id();
            SchematicWorkspace.Inner_StreamUtil preview = this.loaded.get(p.id());
            AiVisualization.clearOwner(preview.mover == null ? preview : preview.mover);
            this.placementsById.put(p.id(), p.checkpoint(this.paste.progress.checkpoint()));
            this.push();
            this.close = "";
            this.flush = "";
            if (this.libraryWindow != null) {
               this.libraryWindow.setVisible(false);
            }

            autismclient.util.OverlayScreenController.setScreenExplicit(this.mc, null);
         } catch (Exception e) {
            this.read(SchematicLibrary.describeError(e));
         }
      }
   }

   void update(SchematicLibrary.Inner_StreamUtil p) {
      p = p == null ? null : this.placementById(p.id());
      if (p != null && p.visible() && p.paste().isEmpty() && this.isInWorld() && this.isLoaded(p) && !this.isBusy() && !this.hasActiveBuild()) {
         SchematicWorkspace.Inner_StreamUtil data = this.loaded.get(p.id());
         if (data.preview == null) {
            this.read("This schematic exceeds the AI builder's preview limit");
         } else if (data.preview.blocks() > 2097152L) {
            this.read("This schematic exceeds the AI builder's block limit");
         } else {
            SchematicLibrary.Inner_DataUtil entry = this.libraryEntry(SchematicLibrary.idOf(Path.of(p.file())));
            if (entry != null && entry.bytes() == p.bytes() && entry.modified() == p.modified()) {
               BlockPos at = origin(p).offset(data.preview.nonAirMinimum());

               try {
                  if (data.preview.blocks() == 0L) {
                     throw new IllegalArgumentException("This schematic contains only air");
                  }

                  JsonObject args = AiJson.objectOf(
                     "schematic",
                     "sc_" + entry.id(),
                     "origin",
                     AiJson.objectOf("x", at.getX(), "y", at.getY(), "z", at.getZ()),
                     "rotation",
                     p.rotation(),
                     "mirror",
                     p.mirror()
                  );
                  this.buildJob = new SchematicWorkspace.Inner_DataUtil(p.id(), AiDirectOps.start("game:build", args));
                  autismclient.util.OverlayScreenController.setScreenExplicit(this.mc, null);
               } catch (Exception e) {
                  this.read(SchematicLibrary.describeError(e));
               }
            } else {
               this.read("Refresh the file list and load the current file");
            }
         }
      }
   }

   void stopBuild() {
      if (this.cancelBuild()) {
         autismclient.util.bi.read("Build stopped");
      }
   }

   private boolean cancelBuild() {
      SchematicWorkspace.Inner_DataUtil active = this.activeBuild();
      this.buildJob = null;
      if (active == null) {
         return false;
      }

      AiDirectOps.init(active.job());
      return true;
   }

   void cancelOperation() {
      this.close_V(false);
   }

   private void close_V(boolean keepRecording) {
      if (this.capture != null) {
         this.capture.cancel();
         this.capture = null;
         this.flush = "Save cancelled";
      }

      if (this.activeRecording != null && !keepRecording) {
         this.activeRecording.fill_V();
         this.activeRecording = null;
         AiVisualization.clearOwner(this.recordingVisOwner);
         this.flush = "Recording cancelled";
      }

      if (this.paste != null) {
         this.paste.init();
         this.flush = "Pausing paste";
      }

      if (this.loadTask != null) {
         this.loadGeneration++;
         this.loadTask.cancel(true);
         this.loadTask = null;
         this.sync = "";
         this.flush = "Loading cancelled";
      }
   }

   public void open_V() {
      if (this.trackedLevel != this.mc.level) {
         this.cancelBuild();
         if (this.activeRecording != null && this.activeRecording.putAt_Z()) {
            this.activeRecording.putAt_V();
         }

         this.close_V(true);
         this.poll();
         this.selectedArea = null;
         this.trackedLevel = this.mc.level;
         this.scope = this.trackedLevel == null ? "" : worldScope(this.mc);
         this.dimension = this.trackedLevel == null ? "" : AiWorldIdentity.currentDimensionId(this.mc);
         this.unavailable.clear();
         this.close = "";
         this.flush = "";
      }

      if (this.buildJob != null && this.buildJob.job().load().isDone()) {
         this.buildJob = null;
      }

      if (this.libraryReady) {
         if (this.capture != null) {
            this.capture.tick(this.mc);
            if (this.capture.finished || this.capture.cancelled) {
               SchematicCapture finished = this.capture;
               this.capture = null;
               if (finished.savedPath != null) {
                  this.refresh();
                  this.flush = "Saved " + finished.savedPath.getFileName();
                  autismclient.util.bi.load(this.flush);
               } else if (!finished.error.isEmpty()) {
                  this.read(finished.error);
               }
            }
         }

         if (this.activeRecording != null) {
            this.activeRecording.putAt_V_Minecraft(this.mc);
            if (!this.activeRecording.readEx_Z && !this.activeRecording.fill_Z) {
               AiVisualization.putAt(
                  this.recordingVisOwner,
                  "Recording chunks",
                  this.activeRecording.putAt_Z ? this.activeRecording.putAt_Str : "",
                  this.activeRecording.putAt_Str(),
                  this.activeRecording.flowTemp()
               );
            } else {
               SchematicRecording finished = this.activeRecording;
               this.activeRecording = null;
               AiVisualization.clearOwner(this.recordingVisOwner);
               if (finished.putAt_Path != null) {
                  this.refresh();
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

         if (this.paste != null) {
            boolean terminal = this.paste.finished;
            SchematicPaste.Inner_StreamUtil progress = this.paste.progress;
            AiVisualization.init(this.pasteVisualOwner, "Schematic paste", progress.phase(), progress.percent(), 100);
            if (terminal || System.nanoTime() - this.load_J > 2000000000L) {
               SchematicLibrary.Inner_StreamUtil p = this.placementsById.get(this.open);
               if (p != null) {
                  this.placementsById
                     .put(p.id(), p.checkpoint(progress.complete() ? new JsonObject() : progress.checkpoint()).shown(!progress.complete() && p.visible()));
                  this.push();
               }

               this.load_J = System.nanoTime();
            }

            if (terminal) {
               if (progress.complete()) {
                  this.unloadPlacement(this.open);
               }

               this.paste = null;
               this.open = null;
               AiVisualization.clearOwner(this.pasteVisualOwner);
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

         if (this.isInWorld()) {
            if (!this.isBusy()) {
               for (SchematicLibrary.Inner_StreamUtil p : this.placements()) {
                  if ((p.visible() || !p.paste().isEmpty()) && !this.loaded.containsKey(p.id()) && !this.unavailable.contains(p.id())) {
                     this.loadPlacement(p, false);
                     break;
                  }
               }
            }

            long now = System.nanoTime();
            SchematicWorkspace.Inner_StreamUtil oldest = null;
            SchematicLibrary.Inner_StreamUtil row = null;

            for (Entry<String, SchematicWorkspace.Inner_StreamUtil> entry : this.loaded.entrySet()) {
               SchematicLibrary.Inner_StreamUtil p = this.placementsById.get(entry.getKey());
               SchematicWorkspace.Inner_StreamUtil data = entry.getValue();
               if (p != null && p.visible() && !p.id().equals(this.open) && data.preview != null && !data.preview.cells().isEmpty()) {
                  AiVisualization.init(data.mover == null ? data : data.mover, now);
                  if (oldest == null || data.lastUsedNanos < oldest.lastUsedNanos) {
                     oldest = data;
                     row = p;
                  }
               }
            }

            if (oldest != null) {
               oldest.lastUsedNanos = now;
               SchematicLibrary.Inner_StreamUtil p = row;
               SchematicWorkspace.Inner_StreamUtil data = oldest;
               Object owner = data.mover == null ? data : data.mover;
               AiVisualization.showCells(
                  owner, origin(p), data.preview.cells(), at -> data.document.isSolidAt(at, p.rotation(), p.mirror())
               );
            }
         }
      }
   }

   private void unloadPlacement(String id) {
      SchematicWorkspace.Inner_StreamUtil data = this.loaded.remove(id);
      if (data != null) {
         AiVisualization.clearOwner(data);
         if (data.mover != null) {
            AiVisualization.clearOwner(data.mover);
         }
      }
   }

   private void poll() {
      for (String id : List.copyOf(this.loaded.keySet())) {
         this.unloadPlacement(id);
      }
   }

   private void read(String message) {
      this.close = message;
      this.flush = "";
      autismclient.util.bi.write(message);
   }

   private void push() {
      if (this.libraryReady) {
         if (this.write_Z) {
            this.update_Z = true;
         } else {
            this.write_Z = true;
            this.update_Z = false;
            SchematicLibrary.Inner_CachePool snapshot = new SchematicLibrary.Inner_CachePool(
               this.library.roots.stream().map(Path::toString).toList(), List.copyOf(this.placementsById.values())
            );
            SchematicLibrary.EXECUTOR.execute(() -> {
               String problem = "";

               try {
                  this.library.saveState(snapshot);
               } catch (Exception e) {
                  problem = SchematicLibrary.describeError(e);
               }

               String failure = problem;
               this.mc.execute(() -> {
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

   private static void init_V_cR_StreamUtil_Path(SchematicLibrary.Inner_StreamUtil p, Path file) throws IOException {
      BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
      if (!attributes.isRegularFile() || attributes.size() != p.bytes() || attributes.lastModifiedTime().toMillis() != p.modified()) {
         throw new IllegalArgumentException("Schematic changed. Refresh and load it again");
      }
   }

   static BlockPos origin(SchematicLibrary.Inner_StreamUtil p) {
      return new BlockPos(p.x(), p.y(), p.z());
   }

   static String displayName(SchematicLibrary.Inner_StreamUtil p) {
      return p.name().isBlank() ? Path.of(p.file()).getFileName().toString() : p.name();
   }

   private static String worldScope(Minecraft mc) {
      if (mc.hasSingleplayerServer()) {
         return AiWorldIdentity.currentWorldKey(mc);
      }

      ServerData server = mc.getCurrentServer();
      return server != null && !server.ip.isBlank() ? "server/" + AiJson.sha256Hex(server.ip.trim().toLowerCase(Locale.ROOT)) : AiWorldIdentity.currentWorldKey(mc);
   }

   private BlockPos aimTarget(boolean surface, double depth) {
      Vec3 eye = SchematicSelection.eyePosition(this.mc);
      Vec3 direction = SchematicSelection.lookDirection(this.mc);
      Vec3 end = eye.add(direction.scale(depth));
      if (surface) {
         BlockHitResult hit = this.mc
            .level
            .clip(new ClipContext(eye, eye.add(direction.scale(96.0)), Block.OUTLINE, Fluid.NONE, this.mc.player));
         if (hit.getType() == Type.BLOCK) {
            return hit.getBlockPos().relative(hit.getDirection());
         }
      }

      return BlockPos.containing(end);
   }

   static final class Inner_CachePool extends AiPlacementSession {
      private final SchematicWorkspace workspace;
      private final String placementId;
      private double aimDepth = 4.0;

      Inner_CachePool(SchematicWorkspace workspace, String id) {
         this.workspace = workspace;
         this.placementId = id;
         SchematicWorkspace.Inner_StreamUtil data = workspace.loaded.get(id);
         AiVisualization.clearOwner(data);
      }

      private SchematicLibrary.Inner_StreamUtil placement() {
         return this.workspace.placementsById.get(this.placementId);
      }

      @Override
      boolean isReadyToConfirm() {
         return this.isActive() && this.workspace.loadTask == null;
      }

      @Override
      boolean isActive() {
         return this.workspace.isInWorld() && this.workspace.placementById(this.placementId) != null && this.workspace.loaded.containsKey(this.placementId);
      }

      @Override
      BlockPos getOrigin() {
         return SchematicWorkspace.origin(this.placement());
      }

      @Override
      int getRotation() {
         return this.placement().rotation();
      }

      @Override
      String getMirror() {
         return this.placement().mirror();
      }

      @Override
      void moveToAim(boolean surface) {
         this.init(this.workspace.aimTarget(surface, this.aimDepth));
      }

      @Override
      void init(double scroll, double x, double y, double w, double h) {
         this.aimDepth = Math.max(1.0, Math.min(96.0, this.aimDepth + scroll));
         this.moveToAim(false);
      }

      @Override
      void snapToGround() {
         BlockPos p = this.getOrigin();
         ClientLevel level = this.workspace.trackedLevel;
         if (level.hasChunkAt(p)) {
            int y = level.getHeight(Types.MOTION_BLOCKING, p.getX(), p.getZ());
            this.init(new BlockPos(p.getX(), y, p.getZ()));
         }
      }

      @Override
      void init(int x, int y, int z) {
         this.init(this.getOrigin().offset(x, y, z));
      }

      private void init(BlockPos at) {
         this.workspace.movePlacement(this.placementId, at);
      }

      @Override
      void rotate(int steps) {
         this.init(Math.floorMod(this.getRotation() + steps * 90, 360), this.getMirror());
      }

      @Override
      void cycleMirror() {
         int var10001 = this.getRotation();
         String var10002;
         switch (this.getMirror()) {
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
         if (this.getRotation() != r || !this.getMirror().equals(m)) {
            this.init(r, m);
         }
      }

      @Override
      void cancel() {
      }

      private void init(int r, String m) {
         SchematicLibrary.Inner_StreamUtil p = this.placement();
         SchematicWorkspace.Inner_StreamUtil data = this.workspace.loaded.get(this.placementId);
         if (r == p.rotation() && m.equals(p.mirror())) {
            if (this.workspace.sync.equals(this.placementId)) {
               this.workspace.cancelOperation();
            }
         } else {
            if (this.workspace.loadTask != null) {
               this.workspace.cancelOperation();
            }

            long token = ++this.workspace.loadGeneration;
            this.workspace.sync = this.placementId;
            this.workspace.flush = "Rotating hologram";
            this.workspace.previewProgress = 0;
            this.workspace.loadTask = SchematicLibrary.EXECUTOR.submit(() -> {
               try {
                  SchematicDocument.Inner_StreamUtil result = data.document.buildPreview(r, m, value -> this.workspace.previewProgress = value);
                  this.workspace.mc.execute(() -> {
                     if (this.workspace.loadGeneration == token && this.isActive()) {
                        SchematicLibrary.Inner_StreamUtil latest = this.placement();
                        this.workspace.placementsById.put(this.placementId, latest.at(latest.x(), latest.y(), latest.z(), r, m));
                        data.preview = result;
                        this.workspace.loadTask = null;
                        this.workspace.sync = "";
                        this.workspace.flush = "";
                     }
                  });
               } catch (CancellationException var7) {
               } catch (Exception e) {
                  this.workspace.mc.execute(() -> {
                     if (this.workspace.loadGeneration == token) {
                        this.workspace.loadTask = null;
                        this.workspace.sync = "";
                        this.workspace.read(SchematicLibrary.describeError(e));
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
      final SchematicDocument document;
      SchematicDocument.Inner_StreamUtil preview;
      String previewError;
      SchematicWorkspace.Inner_CachePool mover;
      long lastUsedNanos;

      Inner_StreamUtil(SchematicDocument document, SchematicDocument.Inner_StreamUtil preview, String previewError) {
         this.document = document;
         this.preview = preview;
         this.previewError = previewError;
      }
   }
}
