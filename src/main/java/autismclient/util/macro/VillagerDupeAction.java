package autismclient.util.macro;

import autismclient.api.macro.ContextMacroAction;
import autismclient.api.macro.MacroExecutionContext;
import autismclient.util.AutismClientMessaging;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public final class VillagerDupeAction implements ContextMacroAction {
   private static final int MAX_COUNT = 64;
   private static final int MAX_WATCH_TICKS = 10;
   public autismclient.util.villagerdupe.VillagerDupeDriver.Inner_DataUtil filter = autismclient.util.villagerdupe.VillagerDupeDriver.Inner_DataUtil.ALL;
   public VillagerDupeAction.TargetSelection targets = VillagerDupeAction.TargetSelection.ALL_IN_RANGE;
   public int count = 1;
   public int intervalTicks = 1;
   public int stopAfter = 0;
   public boolean refresh = true;
   public boolean traders = true;
   public boolean autoEmerald = true;
   public boolean autoShears = true;
   public boolean keepTotem = true;
   public boolean stopWhenMissing = true;
   private boolean enabled = true;

   @Override
   public void run(MacroExecutionContext ctx) {
      VillagerDupeAction.DupeRun driver = new VillagerDupeAction.DupeRun(
         ctx,
         new autismclient.util.villagerdupe.VillagerDupeDriver.CachePool(
            this.filter,
            this.traders,
            this.autoEmerald,
            this.autoShears,
            this.keepTotem,
            this.targets == VillagerDupeAction.TargetSelection.NEAREST,
            this.refresh,
            clamp(this.intervalTicks, 1, 20)
         ),
         clamp(this.count, 0, 64),
         Math.max(0, this.stopAfter),
         this.stopWhenMissing
      );
      autismclient.util.villagerdupe.VillagerDupeDriver.addWatcher(driver::tick);

      while (!driver.finished && ctx.isActive()) {
         CompletableFuture<Void> next = driver.init_CompletableFuture;
         ctx.setStatus(driver.statusText());
         ctx.awaitCondition(next);
      }

      autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore last = driver.lastStatus;
      if (driver.init_Z && ctx.isActive()) {
         String why = last.detail();
         ctx.runOnClientThread(() -> AutismClientMessaging.sendPrefixed("§cVillager Dupe: " + why));
         MacroExecutor.stopCurrentRun();
      }

      int total = driver.total;
      ctx.setVariable("villager_dupe_count", total);
      ctx.setVariable("villager_dupe_status", total > 0 ? "SENT" : (last.init_Z() ? "MISSING_ITEMS" : (last.load_Z() ? "NO_TARGET" : "BLOCKED")));
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("filter", this.filter.name());
      tag.putString("targets", this.targets.name());
      tag.putInt("count", clamp(this.count, 0, 64));
      tag.putInt("intervalTicks", clamp(this.intervalTicks, 1, 20));
      tag.putInt("stopAfter", Math.max(0, this.stopAfter));
      tag.putBoolean("refresh", this.refresh);
      tag.putBoolean("traders", this.traders);
      tag.putBoolean("autoEmerald", this.autoEmerald);
      tag.putBoolean("autoShears", this.autoShears);
      tag.putBoolean("keepTotem", this.keepTotem);
      tag.putBoolean("stopWhenMissing", this.stopWhenMissing);
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.filter = autismclient.util.villagerdupe.VillagerDupeDriver.Inner_DataUtil.parse(tag.getStringOr("filter", "ALL"));

      try {
         this.targets = VillagerDupeAction.TargetSelection.valueOf(tag.getStringOr("targets", "ALL_IN_RANGE"));
      } catch (IllegalArgumentException e) {
         this.targets = VillagerDupeAction.TargetSelection.ALL_IN_RANGE;
      }

      this.count = clamp(tag.getIntOr("count", 1), 0, 64);
      this.intervalTicks = clamp(tag.getIntOr("intervalTicks", 1), 1, 20);
      this.stopAfter = Math.max(0, tag.getIntOr("stopAfter", 0));
      this.refresh = tag.getBooleanOr("refresh", true);
      this.traders = tag.getBooleanOr("traders", true);
      this.autoEmerald = tag.getBooleanOr("autoEmerald", true);
      this.autoShears = tag.getBooleanOr("autoShears", true);
      this.keepTotem = tag.getBooleanOr("keepTotem", true);
      this.stopWhenMissing = tag.getBooleanOr("stopWhenMissing", true);
      this.enabled = tag.getBooleanOr("enabled", true);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.VILLAGER_DUPE;
   }

   @Override
   public String getDisplayName() {
      String passes = this.count <= 0 ? (this.stopAfter > 0 ? " / " + this.stopAfter + " items" : " / until stopped") : " x" + this.count;
      return "Villager Dupe / " + this.filter.label() + (this.targets == VillagerDupeAction.TargetSelection.NEAREST ? " / Nearest" : "") + passes;
   }

   @Override
   public String getIcon() {
      return "VD";
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

   private static final class DupeRun {
      private final MacroExecutionContext ctx;
      private final autismclient.util.villagerdupe.VillagerDupeDriver driver = autismclient.util.villagerdupe.VillagerDupeDriver.getInstance();
      private final autismclient.util.villagerdupe.VillagerDupeDriver.CachePool options;
      private final int passes;
      private final int limit;
      private final boolean stopWhenMissing;
      private int write_I;
      private int activeTicks;
      private boolean write_Z;
      volatile int total;
      volatile autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore lastStatus = autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore.WAITING;
      volatile boolean init_Z;
      volatile boolean finished;
      volatile CompletableFuture<Void> init_CompletableFuture = new CompletableFuture<>();

      DupeRun(MacroExecutionContext ctx, autismclient.util.villagerdupe.VillagerDupeDriver.CachePool opts, int passes, int limit, boolean stopWhenMissing) {
         this.ctx = ctx;
         this.options = opts;
         this.passes = passes;
         this.limit = limit;
         this.stopWhenMissing = stopWhenMissing;
      }

      boolean tick(Minecraft mc) {
         if (!this.ctx.isActive()) {
            if (this.driver.isActive()) {
               this.driver.restoreSelectedSlot(mc);
            }

            return this.finish();
         } else if (this.write_Z) {
            if (this.driver.isActive() && this.activeTicks++ < 10) {
               this.driver.start(mc, this.options);
            }

            if (!this.driver.isActive()) {
               return this.finish();
            }

            if (this.activeTicks < 10) {
               return true;
            }

            this.driver.restoreSelectedSlot(mc);
            return this.finish();
         } else {
            autismclient.util.villagerdupe.VillagerDupeDriver.BufferHelper result = this.driver
               .step(mc, this.options);
            this.write_I++;
            autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore status = result.status();
            boolean change = result.count() > 0 || status != this.lastStatus;
            this.total = this.total + result.count();
            this.lastStatus = status;
            if (this.stopWhenMissing && status.init_Z()) {
               this.init_Z = true;
               this.write_Z = true;
            } else if (this.limit > 0 && this.total >= this.limit || this.passes > 0 && this.write_I >= this.passes) {
               this.write_Z = true;
            }

            if (change) {
               this.init_V();
            }

            return true;
         }
      }

      String statusText() {
         autismclient.util.villagerdupe.VillagerDupeDriver.ConfigStore status = this.lastStatus;
         return "Villager Dupe: " + this.total + (status.load_Z() ? "" : " (" + status.label() + ")");
      }

      private boolean finish() {
         this.finished = true;
         this.init_V();
         return false;
      }

      private void init_V() {
         CompletableFuture<Void> was = this.init_CompletableFuture;
         this.init_CompletableFuture = new CompletableFuture<>();
         was.complete(null);
      }
   }

   public enum TargetSelection {
      ALL_IN_RANGE,
      NEAREST;
   }
}
