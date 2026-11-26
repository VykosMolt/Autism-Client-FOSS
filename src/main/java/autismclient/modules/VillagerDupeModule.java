package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.IntSetting;

public final class VillagerDupeModule extends Module {
   private int init_I = -1;
   private int load_I;
   private autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore init_DataUtil_ConfigSto;
   private autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore load_DataUtil_ConfigSto;

   public VillagerDupeModule() {
      super("villager-dupe", "VillagerDupe", ae.sync, "Shears trade items off villagers.");
      this.init_DataUtil_ConfigSto = autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.WAITING;
      this.add(new ChoiceSetting("filter", "Items", "All", "All", "Saddles", "Horse Armor", "Carpets").description("Items to dupe").build());
      this.add(new BoolSetting("refresh", "Refresh", true).description("Re-raise items faster").build());
      this.add(new IntSetting("interval", "Interval", 1, 1, 20, 1).description("Ticks between bursts").build());
      this.add(new BoolSetting("traders", "Traders", true).description("Include wandering traders").build());
      this.add(new BoolSetting("auto-emerald", "Auto Emerald", true).description("Fetch emerald from inventory").build());
      this.add(new BoolSetting("auto-shears", "Auto Shears", true).description("Swap shears to offhand").build());
      this.add(new BoolSetting("keep-totem", "Keep Totem", true).description("Never replace offhand totem").visibleWhen(() -> this.bool("auto-shears")).build());
      this.add(new BoolSetting("restore-slot", "Restore Slot", true).description("Reselect old slot on disable").build());
   }

   @Override
   public void onEnable() {
      this.init_V_32();
      autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore ready = autismclient.util.villagerdupe.VillagerDupeDriver.init_DataUtil()
         .init_DataUtil_ConfigSto_Minecraft_DataUtil_CachePool(MC, this.init_DataUtil_CachePool());
      if (ready != null) {
         this.init(ready);
      }
   }

   @Override
   public void onDisable() {
      autismclient.util.villagerdupe.VillagerDupeDriver.init_DataUtil().read_V_Minecraft(MC);
      if (this.bool("restore-slot") && this.init_I >= 0 && MC.player != null && MC.level != null && !aD.read_Z()) {
         MC.player.getInventory().setSelectedSlot(this.init_I);
      }

      this.init_V_32();
   }

   @Override
   public void onGameLeft() {
      this.init_V_32();
   }

   @Override
   public void preMovementTick() {
      if (MC.player != null && MC.level != null) {
         if (this.init_I < 0) {
            this.init_I = MC.player.getInventory().getSelectedSlot();
         }

         autismclient.util.villagerdupe.VillagerDupeDriver.BufferHelper result = autismclient.util.villagerdupe.VillagerDupeDriver.init_DataUtil()
            .init_DataUtil_BufferHel_Minecraft_DataUtil_CachePool(MC, this.init_DataUtil_CachePool());
         this.load_I = this.load_I + result.init_I();
         this.init(result.init_DataUtil_ConfigSto());
      }
   }

   @Override
   public String info() {
      if (!this.init_DataUtil_ConfigSto.init_Z()
         && this.init_DataUtil_ConfigSto != autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.SNEAKING
         && this.init_DataUtil_ConfigSto != autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.SPECTATOR
         && this.init_DataUtil_ConfigSto != autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.OFFHAND_TAKEN) {
         return this.load_I > 0 ? Integer.toString(this.load_I) : "";
      } else {
         return this.init_DataUtil_ConfigSto.init_Str();
      }
   }

   private void init(autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore next) {
      this.init_DataUtil_ConfigSto = next;
      if (next.load_Z()) {
         this.load_DataUtil_ConfigSto = null;
      } else {
         boolean toast = next.init_Z() || next == autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.SNEAKING;
         if (toast && next != this.load_DataUtil_ConfigSto) {
            this.load_DataUtil_ConfigSto = next;
            if (next.init_Z()) {
               autismclient.util.bi.write(next.load_Str());
            } else {
               autismclient.util.bi.read(next.load_Str());
            }
         }
      }
   }

   private void init_V_32() {
      this.init_I = -1;
      this.load_I = 0;
      this.init_DataUtil_ConfigSto = autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.WAITING;
      this.load_DataUtil_ConfigSto = null;
   }

   private autismclient.util.villagerdupe.VillagerDupeDriver.CachePool init_DataUtil_CachePool() {
      return new autismclient.util.villagerdupe.VillagerDupeDriver.CachePool(
         autismclient.util.villagerdupe.VillagerDupeDriver.Inner_DataUtil.init_DataUtil_DataUtil_Str(this.choice("filter")),
         this.bool("traders"),
         this.bool("auto-emerald"),
         this.bool("auto-shears"),
         this.bool("keep-totem"),
         false,
         this.bool("refresh"),
         this.integer("interval")
      );
   }
}
