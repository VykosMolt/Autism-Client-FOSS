package autismclient.util.macro;

import autismclient.api.macro.ContextMacroAction;
import autismclient.api.macro.MacroExecutionContext;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public final class WaitVillagerItemAction implements ContextMacroAction {
   private static final int init_I = 1200;
   public autismclient.util.villagerdupe.VillagerDupeDriver.ItemFilter filter = autismclient.util.villagerdupe.VillagerDupeDriver.ItemFilter.ALL;
   public boolean traders = true;
   public boolean holdEmerald = true;
   public int timeoutTicks = 0;
   private boolean enabled = true;

   @Override
   public void run(MacroExecutionContext ctx) {
      WaitVillagerItemAction.Watcher watcher = new WaitVillagerItemAction.Watcher(ctx, clamp(this.timeoutTicks, 0, 1200));
      autismclient.util.villagerdupe.VillagerDupeDriver.addWatcher(watcher::poll);

      while (!watcher.load_Z && ctx.isActive()) {
         CompletableFuture<Void> next = watcher.init_CompletableFuture;
         autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore status = watcher.init_DataUtil_ConfigSto;
         ctx.setStatus(
            !status.load_Z() && status != autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.SCREEN
               ? "Waiting for a villager item (" + status.label() + ")"
               : "Waiting for a villager item"
         );
         ctx.awaitCondition(next);
      }

      ctx.setVariable("villager_item", watcher.init_Z);
      if (!watcher.init_Z && ctx.isActive() && watcher.init_I > 0) {
         ctx.setStatus("No item in " + watcher.init_I + " ticks");
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("filter", this.filter.name());
      tag.putBoolean("traders", this.traders);
      tag.putBoolean("holdEmerald", this.holdEmerald);
      tag.putInt("timeoutTicks", clamp(this.timeoutTicks, 0, 1200));
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.filter = autismclient.util.villagerdupe.VillagerDupeDriver.ItemFilter.parse(tag.getStringOr("filter", "ALL"));
      this.traders = tag.getBooleanOr("traders", true);
      this.holdEmerald = tag.getBooleanOr("holdEmerald", true);
      this.timeoutTicks = clamp(tag.getIntOr("timeoutTicks", 0), 0, 1200);
      this.enabled = tag.getBooleanOr("enabled", true);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_VILLAGER_ITEM;
   }

   @Override
   public String getDisplayName() {
      return "Wait Villager Item / " + this.filter.label() + (this.timeoutTicks > 0 ? " / " + this.timeoutTicks + "t" : "");
   }

   @Override
   public String getIcon() {
      return "W";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean value) {
      this.enabled = value;
   }

   private static int clamp(int n, int min, int max) {
      return Math.max(min, Math.min(max, n));
   }

   private final class Watcher {
      private final MacroExecutionContext context;
      private final autismclient.util.villagerdupe.VillagerDupeDriver driver = autismclient.util.villagerdupe.VillagerDupeDriver.getInstance();
      private final autismclient.util.villagerdupe.VillagerDupeDriver.CachePool driverOptions = new autismclient.util.villagerdupe.VillagerDupeDriver.CachePool(
         WaitVillagerItemAction.this.filter, WaitVillagerItemAction.this.traders, true, false, true, false, false, 1
      );
      private final autismclient.util.villagerdupe.VillagerDupeDriver.ItemFilter itemFilter = WaitVillagerItemAction.this.filter;
      private final boolean useTraders = WaitVillagerItemAction.this.traders;
      private final boolean holdingEmerald = WaitVillagerItemAction.this.holdEmerald;
      final int init_I;
      private int load_I;
      volatile autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore init_DataUtil_ConfigSto = autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.WAITING;
      volatile boolean init_Z;
      volatile boolean load_Z;
      volatile CompletableFuture<Void> init_CompletableFuture = new CompletableFuture<>();

      Watcher(MacroExecutionContext ctx, int timeout) {
         this.context = ctx;
         this.init_I = timeout;
      }

      boolean poll(Minecraft mc) {
         if (!this.context.isActive()) {
            return this.init_Z();
         }

         autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore next = (this.holdingEmerald
               ? this.driver.start(mc, this.driverOptions)
               : this.driver.resume(mc, this.driverOptions))
            .status();
         this.init_Z = autismclient.util.villagerdupe.VillagerDupeDriver.hasTrader(mc, this.itemFilter, this.useTraders);
         this.load_I++;
         if (!this.init_Z && (this.init_I <= 0 || this.load_I < this.init_I)) {
            if (next != this.init_DataUtil_ConfigSto) {
               this.init_DataUtil_ConfigSto = next;
               this.init_V();
            }

            return true;
         } else {
            return this.init_Z();
         }
      }

      private boolean init_Z() {
         this.load_Z = true;
         this.init_V();
         return false;
      }

      private void init_V() {
         CompletableFuture<Void> was = this.init_CompletableFuture;
         this.init_CompletableFuture = new CompletableFuture<>();
         was.complete(null);
      }
   }
}
