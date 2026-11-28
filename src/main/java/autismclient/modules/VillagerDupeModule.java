package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.IntSetting;

public final class VillagerDupeModule extends Module {
   private int originalSlot = -1;
   private int dupedCount;
   private autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore status;
   private autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore lastToastStatus;

   public VillagerDupeModule() {
      super("villager-dupe", "VillagerDupe", ae.sync, "Shears trade items off villagers.");
      this.status = autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.WAITING;
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
      this.resetState();
      autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore ready = autismclient.util.villagerdupe.VillagerDupeDriver.getInstance()
         .checkReady(MC, this.driverConfig());
      if (ready != null) {
         this.setStatus(ready);
      }
   }

   @Override
   public void onDisable() {
      autismclient.util.villagerdupe.VillagerDupeDriver.getInstance().restoreSelectedSlot(MC);
      if (this.bool("restore-slot") && this.originalSlot >= 0 && MC.player != null && MC.level != null && !aD.read_Z()) {
         MC.player.getInventory().setSelectedSlot(this.originalSlot);
      }

      this.resetState();
   }

   @Override
   public void onGameLeft() {
      this.resetState();
   }

   @Override
   public void preMovementTick() {
      if (MC.player != null && MC.level != null) {
         if (this.originalSlot < 0) {
            this.originalSlot = MC.player.getInventory().getSelectedSlot();
         }

         autismclient.util.villagerdupe.VillagerDupeDriver.BufferHelper result = autismclient.util.villagerdupe.VillagerDupeDriver.getInstance()
            .step(MC, this.driverConfig());
         this.dupedCount = this.dupedCount + result.count();
         this.setStatus(result.status());
      }
   }

   @Override
   public String info() {
      if (!this.status.init_Z()
         && this.status != autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.SNEAKING
         && this.status != autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.SPECTATOR
         && this.status != autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.OFFHAND_TAKEN) {
         return this.dupedCount > 0 ? Integer.toString(this.dupedCount) : "";
      } else {
         return this.status.label();
      }
   }

   private void setStatus(autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore next) {
      this.status = next;
      if (next.load_Z()) {
         this.lastToastStatus = null;
      } else {
         boolean toast = next.init_Z() || next == autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.SNEAKING;
         if (toast && next != this.lastToastStatus) {
            this.lastToastStatus = next;
            if (next.init_Z()) {
               autismclient.util.bi.write(next.detail());
            } else {
               autismclient.util.bi.read(next.detail());
            }
         }
      }
   }

   private void resetState() {
      this.originalSlot = -1;
      this.dupedCount = 0;
      this.status = autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.WAITING;
      this.lastToastStatus = null;
   }

   private autismclient.util.villagerdupe.VillagerDupeDriver.CachePool driverConfig() {
      return new autismclient.util.villagerdupe.VillagerDupeDriver.CachePool(
         autismclient.util.villagerdupe.VillagerDupeDriver.ItemFilter.parse(this.choice("filter")),
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
