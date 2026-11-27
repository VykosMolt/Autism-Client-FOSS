package autismclient.util.macro;

import autismclient.api.macro.ContextMacroAction;
import autismclient.api.macro.MacroExecutionContext;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public final class WaitVillagerItemAction implements ContextMacroAction {
   private static final int init_I = 1200;
   public autismclient.util.villagerdupe.VillagerDupeDriver.Inner_DataUtil filter = autismclient.util.villagerdupe.VillagerDupeDriver.Inner_DataUtil.ALL;
   public boolean traders = true;
   public boolean holdEmerald = true;
   public int timeoutTicks = 0;
   private boolean init_Z = true;

   @Override
   public void run(MacroExecutionContext ctx) {
      WaitVillagerItemAction.Inner_DataUtil watcher = new WaitVillagerItemAction.Inner_DataUtil(ctx, init(this.timeoutTicks, 0, 1200));
      autismclient.util.villagerdupe.VillagerDupeDriver.addWatcher(watcher::init);

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
      tag.putInt("timeoutTicks", init(this.timeoutTicks, 0, 1200));
      tag.putBoolean("enabled", this.init_Z);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.filter = autismclient.util.villagerdupe.VillagerDupeDriver.Inner_DataUtil.parse(tag.getStringOr("filter", "ALL"));
      this.traders = tag.getBooleanOr("traders", true);
      this.holdEmerald = tag.getBooleanOr("holdEmerald", true);
      this.timeoutTicks = init(tag.getIntOr("timeoutTicks", 0), 0, 1200);
      this.init_Z = tag.getBooleanOr("enabled", true);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_VILLAGER_ITEM;
   }

   @Override
   public String getDisplayName() {
      return "Wait Villager Item / " + this.filter.init() + (this.timeoutTicks > 0 ? " / " + this.timeoutTicks + "t" : "");
   }

   @Override
   public String getIcon() {
      return "W";
   }

   @Override
   public boolean isEnabled() {
      return this.init_Z;
   }

   @Override
   public void setEnabled(boolean value) {
      this.init_Z = value;
   }

   private static int init(int n, int min, int max) {
      return Math.max(min, Math.min(max, n));
   }

   private final class Inner_DataUtil {
      private final MacroExecutionContext init_MacroExecutionCont;
      private final autismclient.util.villagerdupe.VillagerDupeDriver init_DataUtil = autismclient.util.villagerdupe.VillagerDupeDriver.getInstance();
      private final autismclient.util.villagerdupe.VillagerDupeDriver.CachePool init_DataUtil_CachePool = new autismclient.util.villagerdupe.VillagerDupeDriver.CachePool(
         WaitVillagerItemAction.this.filter, WaitVillagerItemAction.this.traders, true, false, true, false, false, 1
      );
      private final autismclient.util.villagerdupe.VillagerDupeDriver.Inner_DataUtil init_DataUtil_DataUtil = WaitVillagerItemAction.this.filter;
      private final boolean read = WaitVillagerItemAction.this.traders;
      private final boolean write = WaitVillagerItemAction.this.holdEmerald;
      final int init_I;
      private int load_I;
      volatile autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore init_DataUtil_ConfigSto = autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.WAITING;
      volatile boolean init_Z;
      volatile boolean load_Z;
      volatile CompletableFuture<Void> init_CompletableFuture = new CompletableFuture<>();

      Inner_DataUtil(MacroExecutionContext ctx, int timeout) {
         this.init_MacroExecutionCont = ctx;
         this.init_I = timeout;
      }

      boolean init(Minecraft mc) {
         if (!this.init_MacroExecutionCont.isActive()) {
            return this.init_Z();
         }

         autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore next = (this.write
               ? this.init_DataUtil.load(mc, this.init_DataUtil_CachePool)
               : this.init_DataUtil.read(mc, this.init_DataUtil_CachePool))
            .status();
         this.init_Z = autismclient.util.villagerdupe.VillagerDupeDriver.init_Z_Minecraft(mc, this.init_DataUtil_DataUtil, this.read);
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
