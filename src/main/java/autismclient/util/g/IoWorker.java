package autismclient.util.g;

import autismclient.modules.Module;
import autismclient.modules.al;
import autismclient.util.bw;
import autismclient.util.dd;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;

public final class IoWorker {
   private boolean init_Z = true;
   private boolean load_Z = true;
   private boolean read = false;
   private boolean write = false;
   private final List<IoWorker.Inner_BufferHelper> init_List = new ArrayList<>(
      List.of(
         new IoWorker.Inner_BufferHelper("", true),
         new IoWorker.Inner_BufferHelper("", true),
         new IoWorker.Inner_BufferHelper("", true),
         new IoWorker.Inner_BufferHelper("", true)
      )
   );
   private final List<IoWorker.Inner_CachePool> load_List = new ArrayList<>();
   private boolean update = true;

   public IoWorker() {
   }

   public IoWorker(IoWorker source) {
      if (source != null) {
         this.init_Z = source.init_Z;
         this.load_Z = source.load_Z;
         this.read = source.read;
         this.write = source.write;
         this.init_List.clear();
         this.init_List.addAll(source.init_List);
         this.load_List.addAll(source.load_List);
         this.update = source.update;
         this.init_V();
      }
   }

   public boolean init_Z() {
      return this.init_Z;
   }

   public void init_V_Z(boolean gravity) {
      this.init_Z = gravity;
   }

   public boolean load_Z() {
      return this.load_Z;
   }

   public void load(boolean autoPosition) {
      this.load_Z = autoPosition;
   }

   public boolean read() {
      return this.read;
   }

   public void read(boolean autoLook) {
      this.read = autoLook;
   }

   public boolean write() {
      return this.write;
   }

   public void write(boolean autoSwing) {
      this.write = autoSwing;
   }

   public List<IoWorker.Inner_BufferHelper> init_List() {
      return List.copyOf(this.init_List);
   }

   public void init_V_I_IoWorker_BufferHel(int index, IoWorker.Inner_BufferHelper slot) {
      if (index >= 0 && index < 4) {
         this.init_List.set(index, slot == null ? new IoWorker.Inner_BufferHelper("", true) : slot);
      } else {
         throw new IndexOutOfBoundsException(index);
      }
   }

   public List<IoWorker.Inner_CachePool> load_List() {
      return List.copyOf(this.load_List);
   }

   public boolean update() {
      return this.update;
   }

   public void update(boolean enabled) {
      this.update = enabled;
   }

   public int init_I_IoWorker_DataUtil(IoWorker.Inner_DataUtil direction) {
      int count = 0;

      for (IoWorker.Inner_CachePool rule : this.load_List) {
         if (rule.init_IoWorker_DataUtil() == direction) {
            count++;
         }
      }

      return count;
   }

   public IoWorker.Inner_StreamUtil init_IoWorker_StreamUti() {
      Module module = al.init_Module_Str("packet-canceller");
      if (module == null) {
         return new IoWorker.Inner_StreamUtil(0, 0, 0);
      }

      List<IoWorker.Inner_CachePool> rules = new ArrayList<>();
      int c2s = 0;
      int s2c = 0;
      int skipped = 0;

      for (IoWorker.Inner_DataUtil direction : IoWorker.Inner_DataUtil.values()) {
         boolean outgoing = direction == IoWorker.Inner_DataUtil.init;
         String value = module.value(outgoing ? "C2S-packets" : "S2C-packets");
         skipped += dd.init_List(value, outgoing).size();

         for (Class<? extends Packet<?>> packet : dd.init_Set(value, outgoing)) {
            if (init_Z_IoWorker_DataUtil_Str(direction, packet.getName())) {
               skipped++;
            } else {
               rules.add(new IoWorker.Inner_CachePool(direction, packet.getName()));
               if (outgoing) {
                  c2s++;
               } else {
                  s2c++;
               }
            }
         }
      }

      this.init_V_List(rules);
      return new IoWorker.Inner_StreamUtil(c2s, s2c, skipped);
   }

   public void init_V_List(List<IoWorker.Inner_CachePool> rules) {
      this.load_List.clear();
      if (rules != null) {
         this.load_List.addAll(rules);
      }

      this.init_V();
   }

   public boolean init_Z_IoWorker_DataUtil(IoWorker.Inner_DataUtil direction, String packetClass, boolean protocolCritical, boolean movementPacket) {
      if (!protocolCritical && !init_Z_IoWorker_DataUtil_Str(direction, packetClass)) {
         String packet = packetClass == null ? "" : packetClass;
         if (this.load(direction, packet)) {
            return false;
         }

         if (direction == IoWorker.Inner_DataUtil.init) {
            for (IoWorker.Inner_BufferHelper slot : this.init_List) {
               if (!slot.init_Str().isBlank() && slot.init_Str().equals(packet)) {
                  return slot.init_Z();
               }
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public static Set<Class<? extends Packet<?>>> init_Set_IoWorker_DataUtil(IoWorker.Inner_DataUtil direction) {
      Set<Class<? extends Packet<?>>> out = new LinkedHashSet<>();

      for (Class<? extends Packet<?>> packet : direction == IoWorker.Inner_DataUtil.init ? bw.load() : bw.init_Set()) {
         if (init_Z_IoWorker_DataUtil_Str(direction, packet.getName())) {
            out.add(packet);
         }
      }

      return out;
   }

   public static boolean init_Z_IoWorker_DataUtil_Str(IoWorker.Inner_DataUtil direction, String packetClass) {
      String name = packetClass == null ? "" : packetClass;
      return direction == IoWorker.Inner_DataUtil.init
         ? init_Z_Str_StrArr(
            name,
            "ServerboundKeepAlivePacket",
            "ServerboundPongPacket",
            "ServerboundAcceptTeleportationPacket",
            "ServerboundCookieResponsePacket",
            "ServerboundResourcePackPacket",
            "ServerboundLoginAcknowledgedPacket",
            "ServerboundFinishConfigurationPacket",
            "ServerboundSelectKnownPacks",
            "ServerboundAcceptCodeOfConductPacket",
            "ServerboundCustomQueryAnswerPacket",
            "ServerboundConfigurationAcknowledgedPacket",
            "ServerboundChatAckPacket",
            "ServerboundChatSessionUpdatePacket",
            "ServerboundPlayerLoadedPacket",
            "ServerboundChunkBatchReceivedPacket",
            "ServerboundClientTickEndPacket",
            "ServerboundClientInformationPacket",
            "ServerboundCustomPayloadPacket",
            "ServerboundCustomClickActionPacket"
         )
         : init_Z_Str_StrArr(
            name,
            "ClientboundKeepAlivePacket",
            "ClientboundPingPacket",
            "ClientboundDisconnectPacket",
            "ClientboundLoginDisconnectPacket",
            "ClientboundHelloPacket",
            "ClientboundLoginFinishedPacket",
            "ClientboundLoginCompressionPacket",
            "ClientboundCustomQueryPacket",
            "ClientboundFinishConfigurationPacket",
            "ClientboundRegistryDataPacket",
            "ClientboundUpdateEnabledFeaturesPacket",
            "ClientboundSelectKnownPacks",
            "ClientboundResetChatPacket",
            "ClientboundCodeOfConductPacket",
            "ClientboundUpdateTagsPacket",
            "ClientboundCookieRequestPacket",
            "ClientboundStoreCookiePacket",
            "ClientboundResourcePackPushPacket",
            "ClientboundResourcePackPopPacket",
            "ClientboundTransferPacket",
            "ClientboundLoginPacket",
            "ClientboundRespawnPacket",
            "ClientboundGameEventPacket",
            "ClientboundChunkBatchStartPacket",
            "ClientboundChunkBatchFinishedPacket",
            "ClientboundStartConfigurationPacket",
            "ClientboundPlayerPositionPacket",
            "ClientboundPlayerRotationPacket",
            "ClientboundAddEntityPacket",
            "ClientboundMoveEntityPacket$Pos",
            "ClientboundMoveEntityPacket$PosRot",
            "ClientboundMoveEntityPacket$Rot",
            "ClientboundEntityPositionSyncPacket",
            "ClientboundTeleportEntityPacket",
            "ClientboundRotateHeadPacket",
            "ClientboundSetEntityDataPacket",
            "ClientboundSetEntityMotionPacket",
            "ClientboundRemoveEntitiesPacket",
            "ClientboundSetPassengersPacket",
            "ClientboundMoveMinecartPacket",
            "ClientboundShowDialogPacket",
            "ClientboundClearDialogPacket"
         );
   }

   private static boolean init_Z_Str_StrArr(String value, String... names) {
      for (String name : names) {
         if (value.endsWith(name)) {
            return true;
         }
      }

      return false;
   }

   private boolean load(IoWorker.Inner_DataUtil direction, String packetClass) {
      if (!this.update) {
         return false;
      }

      for (IoWorker.Inner_CachePool rule : this.load_List) {
         if (rule.init_IoWorker_DataUtil() == direction && rule.init_Str().equals(packetClass)) {
            return true;
         }
      }

      return false;
   }

   public CompoundTag init_CompoundTag() {
      this.init_V();
      CompoundTag tag = new CompoundTag();
      tag.putBoolean("gravity", this.init_Z);
      tag.putBoolean("autoPosition", this.load_Z);
      tag.putBoolean("autoLook", this.read);
      tag.putBoolean("autoSwing", this.write);
      ListTag slotTags = new ListTag();

      for (IoWorker.Inner_BufferHelper slot : this.init_List) {
         slotTags.add(slot.init_CompoundTag());
      }

      tag.put("slots", slotTags);
      ListTag blockTags = new ListTag();

      for (IoWorker.Inner_CachePool rule : this.load_List) {
         blockTags.add(rule.init_CompoundTag());
      }

      tag.put("blocklist", blockTags);
      tag.putBoolean("cancellerEnabled", this.update);
      return tag;
   }

   public static IoWorker init_IoWorker_CompoundTag(CompoundTag tag) {
      IoWorker policy = new IoWorker();
      policy.init_Z = tag.getBooleanOr("gravity", true);
      policy.update = tag.getBooleanOr("cancellerEnabled", true);
      policy.load_Z = tag.getBooleanOr("autoPosition", true);
      policy.read = tag.getBooleanOr("autoLook", false);
      policy.write = tag.getBooleanOr("autoSwing", false);
      policy.init_List.clear();

      for (Tag value : tag.getListOrEmpty("slots")) {
         if (value instanceof CompoundTag compound) {
            policy.init_List.add(IoWorker.Inner_BufferHelper.init(compound));
         }
      }

      for (Tag value : tag.getListOrEmpty("blocklist")) {
         if (value instanceof CompoundTag compound) {
            policy.load_List.add(IoWorker.Inner_CachePool.init(compound));
         }
      }

      policy.init_V();
      return policy;
   }

   private void init_V() {
      while (this.init_List.size() < 4) {
         this.init_List.add(new IoWorker.Inner_BufferHelper("", true));
      }

      while (this.init_List.size() > 4) {
         this.init_List.remove(this.init_List.size() - 1);
      }

      Set<String> seen = new HashSet<>();
      this.load_List
         .removeIf(
            rule -> rule == null
               || rule.init_Str().isBlank()
               || init_Z_IoWorker_DataUtil_Str(rule.init_IoWorker_DataUtil(), rule.init_Str())
               || !seen.add(rule.init_IoWorker_DataUtil().name() + "\u0000" + rule.init_Str())
         );
   }

   public record Inner_BufferHelper(String init_Str, boolean init_Z) {
      public Inner_BufferHelper {
         init_Str = init_Str == null ? "" : init_Str.trim();
      }

      CompoundTag init_CompoundTag() {
         CompoundTag tag = new CompoundTag();
         tag.putString("packet", this.init_Str);
         tag.putBoolean("enabled", this.init_Z);
         return tag;
      }

      static IoWorker.Inner_BufferHelper init(CompoundTag tag) {
         return new IoWorker.Inner_BufferHelper(tag.getStringOr("packet", ""), tag.getBooleanOr("enabled", true));
      }
   }

   public record Inner_CachePool(IoWorker.Inner_DataUtil init_IoWorker_DataUtil, String init_Str) {
      public Inner_CachePool {
         init_IoWorker_DataUtil = init_IoWorker_DataUtil == null ? IoWorker.Inner_DataUtil.init : init_IoWorker_DataUtil;
         init_Str = init_Str == null ? "" : init_Str.trim();
      }

      CompoundTag init_CompoundTag() {
         CompoundTag tag = new CompoundTag();
         tag.putString("direction", this.init_IoWorker_DataUtil.name());
         tag.putString("packet", this.init_Str);
         return tag;
      }

      static IoWorker.Inner_CachePool init(CompoundTag tag) {
         IoWorker.Inner_DataUtil direction;
         try {
            direction = IoWorker.Inner_DataUtil.valueOf(tag.getStringOr("direction", IoWorker.Inner_DataUtil.init.name()).toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException ignored) {
            direction = IoWorker.Inner_DataUtil.init;
         }

         return new IoWorker.Inner_CachePool(direction, tag.getStringOr("packet", ""));
      }
   }

   public enum Inner_DataUtil {
      init,
      load;
   }

   public record Inner_StreamUtil(int init, int load, int read) {
      public String init_Str() {
         String text = "Imported " + this.init + " C2S and " + this.load + " S2C from PacketCanceller";
         return this.read > 0 ? text + " (" + this.read + " skipped: bots need them or unknown)" : text;
      }

      public int init_I() {
         return this.init;
      }
   }
}
