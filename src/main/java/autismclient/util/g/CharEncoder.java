package autismclient.util.g;

import autismclient.util.aw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

final class CharEncoder {
   private final Minecraft init_Minecraft;
   private final Player init_Player;
   private final ClassCache init_ClassCache;
   private final CharEncoder.Inner_DataUtil init_CharEncoder_DataUt;

   private CharEncoder(Minecraft minecraft, Player player, ClassCache botSession, CharEncoder.Inner_DataUtil packetDestination) {
      this.init_Minecraft = minecraft;
      this.init_Player = player;
      this.init_ClassCache = botSession;
      this.init_CharEncoder_DataUt = packetDestination;
   }

   static CharEncoder init_CharEncoder_ClassCache_I(ClassCache session, int botEntityId) {
      Minecraft mc = Minecraft.getInstance();
      return session != null && mc.level != null && mc.level.getEntity(botEntityId) instanceof RemotePlayer bot && PoolEntry.load((Entity)bot)
         ? new CharEncoder(mc, bot, session, CharEncoder.Inner_DataUtil.load)
         : new CharEncoder(mc, mc.player, null, CharEncoder.Inner_DataUtil.init);
   }

   boolean init(Entity entity) {
      return this.init_CharEncoder_DataUt == CharEncoder.Inner_DataUtil.load && this.init_Player != null && this.init_Player == entity;
   }

   Player init_Player() {
      return this.init_Player;
   }

   ClassCache init_ClassCache() {
      return this.init_ClassCache;
   }

   CharEncoder.Inner_DataUtil init_CharEncoder_DataUt() {
      return this.init_CharEncoder_DataUt;
   }

   int init_I() {
      if (this.init_ClassCache != null) {
         return this.init_ClassCache.push_I();
      } else {
         return this.init_Minecraft.gameMode == null ? -1 : this.init_Minecraft.gameMode.getPlayerMode().getId();
      }
   }

   Inventory init_Inventory() {
      return this.init_Player == null ? null : this.init_Player.getInventory();
   }

   int load() {
      return this.init_ClassCache == null ? (this.init_Player == null ? -1 : this.init_Player.getInventory().getSelectedSlot()) : this.init_ClassCache.load_I();
   }

   boolean init(Packet<?> packet) {
      if (packet == null) {
         return false;
      }

      if (this.init_ClassCache != null) {
         return this.init_ClassCache.init_Z_Packet(packet);
      }

      if (this.init_Minecraft.getConnection() == null) {
         return false;
      }

      this.init_Minecraft.getConnection().send(packet);
      return true;
   }

   boolean init(int slot) {
      int clamped = Math.max(0, Math.min(8, slot));
      if (this.init_ClassCache != null) {
         return this.init_ClassCache.load_Z_I(clamped);
      } else if (this.init_Player != null && this.init_Minecraft.getConnection() != null) {
         aw.init_V(this.init_Minecraft, clamped);
         return true;
      } else {
         return false;
      }
   }

   boolean init_Z_I_I(int inventorySlot, int hotbarSlot) {
      return this.init_ClassCache != null
         ? this.init_ClassCache.init_Z_I_I(inventorySlot, hotbarSlot)
         : this.init_Player != null && aw.read(this.init_Minecraft, inventorySlot, Math.max(0, Math.min(8, hotbarSlot)));
   }

   enum Inner_DataUtil {
      init,
      load;
   }
}
