package autismclient.util.macro;

import autismclient.util.bv;
import autismclient.util.bw;
import autismclient.util.ck;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.Packet;

public class DelayPacketsAction implements MacroAction {
   public DelayPacketsAction.Inner_DataUtil mode = DelayPacketsAction.Inner_DataUtil.ENABLE;
   public boolean flushOnDisable = true;
   public List<String> c2sPacketNames = new ArrayList<>();
   public List<String> s2cPacketNames = new ArrayList<>();
   private boolean init = true;

   public void init() {
      this.mode = DelayPacketsAction.Inner_DataUtil.values()[(this.mode.ordinal() + 1) % DelayPacketsAction.Inner_DataUtil.values().length];
   }

   public void load() {
      this.mode = DelayPacketsAction.Inner_DataUtil.values()[(this.mode.ordinal() - 1 + DelayPacketsAction.Inner_DataUtil.values().length)
         % DelayPacketsAction.Inner_DataUtil.values().length];
   }

   public void read() {
      this.c2sPacketNames.clear();
      this.s2cPacketNames.clear();
      String[] defaultNames = new String[]{
         "ServerboundContainerClickPacket",
         "ServerboundContainerButtonClickPacket",
         "ServerboundSetCreativeModeSlotPacket",
         "ServerboundPlayerActionPacket",
         "ServerboundUseItemPacket"
      };

      for (String target : defaultNames) {
         String targetNoSuffix = target.endsWith("Packet") ? target.substring(0, target.length() - 6) : target;

         for (Class<?> clz : bw.load()) {
            String registryName = bw.init_Str_Class((Class<? extends Packet<?>>)clz);
            if (registryName != null && (registryName.equals(target) || registryName.equals(targetNoSuffix))) {
               this.c2sPacketNames.add(bv.init_Str_Class((Class<? extends Packet<?>>)clz));
               break;
            }
         }
      }
   }

   public void write() {
      this.c2sPacketNames.clear();
      this.s2cPacketNames.clear();
      ck shared = ck.init_ck();

      for (Class<?> clz : shared.load_Set()) {
         this.c2sPacketNames.add(bv.init_Str_Class((Class<? extends Packet<?>>)clz));
      }

      for (Class<?> clz : shared.read_Set()) {
         this.s2cPacketNames.add(bv.init_Str_Class((Class<? extends Packet<?>>)clz));
      }
   }

   @Override
   public void execute(Minecraft mc) {
      ck shared = ck.init_ck();
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
      boolean shouldEnable = this.mode == DelayPacketsAction.Inner_DataUtil.ENABLE;
      int flushed = 0;
      if (!shouldEnable && this.flushOnDisable && mc.getConnection() != null) {
         flushed = shared.init_I_ClientPacketListen(mc.getConnection());
      }

      if (this.c2sPacketNames.isEmpty() && this.s2cPacketNames.isEmpty()) {
         shared.init_V_Set(Set.of());
         shared.load_V_Set(Set.of());
         shared.tick(false);
         shared.write(false);
         module.init(false, flushed);
      } else {
         Set<Class<? extends Packet<?>>> resolvedC2S = this.init(this.c2sPacketNames, true);
         Set<Class<? extends Packet<?>>> resolvedS2C = this.init(this.s2cPacketNames, false);
         shared.init_V_Set(resolvedC2S);
         shared.load_V_Set(resolvedS2C);
         shared.tick(true);
         shared.write(shouldEnable);
         module.init(shouldEnable, flushed);
      }
   }

   private Set<Class<? extends Packet<?>>> init(List<String> names, boolean isC2S) {
      Set<Class<? extends Packet<?>>> result = new HashSet<>();
      Set<Class<? extends Packet<?>>> pool = isC2S ? bw.load() : bw.init_Set();

      for (String name : names) {
         String trimmed = name.trim();
         if (!trimmed.isEmpty()) {
            String withSuffix = trimmed.endsWith("Packet") ? trimmed : trimmed + "Packet";

            for (Class<? extends Packet<?>> clazz : pool) {
               String friendlyName = bv.init_Str_Class(clazz);
               if (friendlyName.equalsIgnoreCase(trimmed)
                  || friendlyName.equalsIgnoreCase(withSuffix)
                  || clazz.getSimpleName().equalsIgnoreCase(trimmed)
                  || clazz.getSimpleName().equalsIgnoreCase(withSuffix)) {
                  result.add(clazz);
                  break;
               }
            }
         }
      }

      return result;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.DELAY_PACKETS;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "DELAY_PACKETS");
      tag.putString("mode", this.mode.name());
      tag.putBoolean("flushOnDisable", this.flushOnDisable);
      ListTag c2sList = new ListTag();

      for (String n : this.c2sPacketNames) {
         c2sList.add(StringTag.valueOf(n));
      }

      tag.put("c2sPackets", c2sList);
      ListTag s2cList = new ListTag();

      for (String n : this.s2cPacketNames) {
         s2cList.add(StringTag.valueOf(n));
      }

      tag.put("s2cPackets", s2cList);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("mode")) {
         try {
            this.mode = DelayPacketsAction.Inner_DataUtil.valueOf(tag.getStringOr("mode", "ENABLE"));
         } catch (IllegalArgumentException ignored) {
            this.mode = DelayPacketsAction.Inner_DataUtil.ENABLE;
         }
      }

      this.flushOnDisable = tag.getBooleanOr("flushOnDisable", true);
      this.c2sPacketNames.clear();
      if (tag.contains("c2sPackets")) {
         ListTag c2sList = (ListTag)tag.get("c2sPackets");
         if (c2sList != null) {
            for (int i = 0; i < c2sList.size(); i++) {
               this.c2sPacketNames.add(c2sList.getString(i).orElse(""));
            }
         }
      }

      this.s2cPacketNames.clear();
      if (tag.contains("s2cPackets")) {
         ListTag s2cList = (ListTag)tag.get("s2cPackets");
         if (s2cList != null) {
            for (int i = 0; i < s2cList.size(); i++) {
               this.s2cPacketNames.add(s2cList.getString(i).orElse(""));
            }
         }
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      String base = this.mode == DelayPacketsAction.Inner_DataUtil.ENABLE ? "Delay Pkts: ON" : "Delay Pkts: OFF";
      int total = this.c2sPacketNames.size() + this.s2cPacketNames.size();
      if (this.mode == DelayPacketsAction.Inner_DataUtil.ENABLE && total > 0) {
         base = base + " (" + total + " pkts)";
      }

      return base;
   }

   @Override
   public String getIcon() {
      return "D";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   public enum Inner_DataUtil {
      ENABLE,
      DISABLE;
   }
}
