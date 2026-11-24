package autismclient.util;

import autismclient.api.module.Setting;
import autismclient.compat263.Compat263;
import autismclient.modules.Module;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

public final class AuditClientFixes {
   private static volatile Class<?> cachedSodiumManagerClass;
   private static volatile Method cachedMarkGraphDirty;
   private static volatile boolean sodiumSoftRebuildChecked;
   private static volatile boolean sodiumSoftRebuildAvailable;
   private static MethodHandle mhSodiumWorldRenderer;
   private static MethodHandle mhSodiumScheduleChunks;
   private static MethodHandle mhSodiumScheduleTerrainUpdate;
   private static MethodHandle mhChunkTrackerGet;
   private static MethodHandle mhChunkTrackerReady;

   private AuditClientFixes() {
   }

   public static boolean isNoClipActive() {
      return autismclient.modules.av.init_Z_34();
   }

   public static boolean isPhaseSpiderActive() {
      return autismclient.modules.aF.audit$isSpiderActive();
   }

   public static boolean isAutismActive() {
      autismclient.modules.PoolEntry var0 = autismclient.modules.PoolEntry.init_PoolEntry();
      return var0 != null ? var0.init_Z() : false;
   }

   public static boolean isXrayOrFreecamOcclusionBypass() {
      return autismclient.modules.am.update();
   }

   public static boolean isNoRenderInWallOverlay() {
      return autismclient.modules.az.close();
   }

   public static boolean shouldKeepNoPhysics() {
      return isNoClipActive() || isPhaseSpiderActive();
   }

   public static boolean shouldDisableOcclusion() {
      return isXrayOrFreecamOcclusionBypass() || isNoClipActive() || isPhaseSpiderActive();
   }

   public static boolean shouldSuppressInWallOverlay() {
      return isNoRenderInWallOverlay() || shouldKeepNoPhysics();
   }

   public static boolean isBoatFlyEnabled() {
      return autismclient.modules.al.init_Z_Str("boat-fly");
   }

   public static void releaseShiftKey() {
      Minecraft var0 = Minecraft.getInstance();
      if (var0 != null && var0.options != null && var0.options.keyShift != null) {
         var0.options.keyShift.setDown(false);
      }
   }

   public static boolean isLocalPlayerId(int var0) {
      Minecraft var1 = Minecraft.getInstance();
      if (var1 == null) {
         return false;
      }

      LocalPlayer var2 = var1.player;
      return var2 != null && var2.getId() == var0;
   }

   public static void clearNoPhysicsIfUnused() {
      Minecraft var0 = Minecraft.getInstance();
      if (var0 != null) {
         LocalPlayer var1 = var0.player;
         if (var1 != null && !var1.isSpectator() && !shouldKeepNoPhysics()) {
            var1.noPhysics = false;
         }
      }
   }

   public static boolean shouldOverrideNoClipTravel(Player var0) {
      if (var0 != null && !var0.isPassenger() && !var0.isSpectator() && !var0.isDeadOrDying() && !var0.isSleeping()) {
         Minecraft var1 = Minecraft.getInstance();
         return var1 != null && var0 == var1.player && isNoClipActive();
      } else {
         return false;
      }
   }

   public static void markSodiumGraphDirty(Object var0) {
      if (var0 != null) {
         Class var1 = var0.getClass();
         Method var2 = cachedMarkGraphDirty;
         if (cachedSodiumManagerClass != var1) {
            try {
               var2 = var1.getMethod("markGraphDirty");
            } catch (ReflectiveOperationException var5) {
               var2 = null;
            }

            cachedMarkGraphDirty = var2;
            cachedSodiumManagerClass = var1;
         }

         if (var2 != null) {
            try {
               var2.invoke(var0);
            } catch (ReflectiveOperationException var4) {
            }
         }
      }
   }

   public static int autoFishMinFreeSlots(Module var0, int var1) {
      if (var0 != null) {
         for (Setting var3 : var0.settings()) {
            if ("min-free-slots".equals(var3.id())) {
               String var4 = var0.settingValue("min-free-slots");
               if (var4 != null) {
                  try {
                     return Math.max(0, Math.min(36, Integer.parseInt(var4.trim())));
                  } catch (NumberFormatException var6) {
                     break;
                  }
               }
            }
         }
      }

      return Math.max(0, Math.min(36, var1));
   }

   public static void applyFlightAbilities(Module var0, ClientboundPlayerAbilitiesPacket var1) {
      if (var0 != null && var1 != null) {
         Minecraft var2 = Minecraft.getInstance();
         if (var2 != null) {
            ClientPacketListener var3 = var2.getConnection();
            if (var3 != null) {
               boolean var4 = var1.isInvulnerable();
               boolean var5 = var1.canInstabuild();
               float var6 = var1.getWalkingSpeed();
               var2.execute(() -> {
                  if (var2.getConnection() == var3) {
                     LocalPlayer var5x = var2.player;
                     if (var5x != null) {
                        Abilities var6x = var5x.getAbilities();
                        var6x.invulnerable = var4;
                        var6x.instabuild = var5;
                        var6x.setWalkingSpeed(var6);
                     }
                  }
               });
            }
         }
      }
   }

   public static void refreshChunksSoft(Minecraft var0) {
      if (var0 != null && var0.level != null) {
         if (!trySodiumSoftRebuild(var0.level)) {
            if (var0.levelExtractor != null) {
               Compat263.softReloadChunks(var0.levelExtractor);
            }
         }
      }
   }

   private static boolean trySodiumSoftRebuild(ClientLevel var0) {
      if (!ensureSodiumSoftRebuildHandles()) {
         return false;
      }

      try {
         Object var1 = (Object)mhSodiumWorldRenderer.invokeExact();
         if (var1 == null) {
            return false;
         }

         Object var2 = (Object)mhChunkTrackerGet.invokeExact((ClientLevel)var0);
         if (var2 == null) {
            return false;
         }

         LongCollection var3 = (LongCollection)mhChunkTrackerReady.invokeExact((Object)var2);
         if (var3 != null && !var3.isEmpty()) {
            int var4 = var0.getMinSectionY();
            int var5 = var0.getMaxSectionY();
            LongIterator var6 = var3.iterator();

            while (var6.hasNext()) {
               long var7 = var6.nextLong();
               int var9 = ChunkPos.getX(var7);
               int var10 = ChunkPos.getZ(var7);
               mhSodiumScheduleChunks.invokeExact((Object)var1, (int)var9, (int)var4, (int)var10, (int)var9, (int)var5, (int)var10, (boolean)false);
            }
         }

         mhSodiumScheduleTerrainUpdate.invokeExact((Object)var1);
         return true;
      } catch (Throwable var11) {
         return false;
      }
   }

   private static boolean ensureSodiumSoftRebuildHandles() {
      if (sodiumSoftRebuildChecked) {
         return sodiumSoftRebuildAvailable;
      }

      synchronized (AuditClientFixes.class) {
         if (sodiumSoftRebuildChecked) {
            return sodiumSoftRebuildAvailable;
         }

         try {
            Lookup var1 = MethodHandles.publicLookup();
            Class var2 = Class.forName("net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer");
            Class var3 = Class.forName("net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTrackerHolder");
            Class var4 = Class.forName("net.caffeinemc.mods.sodium.client.render.chunk.map.ChunkTracker");
            Method var5 = var2.getMethod("instanceNullable");
            Method var6 = var2.getMethod("scheduleRebuildForChunks", int.class, int.class, int.class, int.class, int.class, int.class, boolean.class);
            Method var7 = var2.getMethod("scheduleTerrainUpdate");
            Method var8 = var3.getMethod("get", ClientLevel.class);
            Method var9 = var4.getMethod("getReadyChunks");
            mhSodiumWorldRenderer = var1.unreflect(var5).asType(MethodType.methodType(Object.class));
            mhSodiumScheduleChunks = var1.unreflect(var6)
               .asType(MethodType.methodType(void.class, Object.class, int.class, int.class, int.class, int.class, int.class, int.class, boolean.class));
            mhSodiumScheduleTerrainUpdate = var1.unreflect(var7).asType(MethodType.methodType(void.class, Object.class));
            mhChunkTrackerGet = var1.unreflect(var8).asType(MethodType.methodType(Object.class, ClientLevel.class));
            mhChunkTrackerReady = var1.unreflect(var9).asType(MethodType.methodType(LongCollection.class, Object.class));
            sodiumSoftRebuildAvailable = true;
         } catch (Throwable var11) {
            sodiumSoftRebuildAvailable = false;
         }

         sodiumSoftRebuildChecked = true;
         return sodiumSoftRebuildAvailable;
      }
   }
}
