package autismclient.util.macro;

import autismclient.api.macro.ContextMacroAction;
import autismclient.api.macro.MacroExecutionContext;
import autismclient.util.AutismClientMessaging;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public final class aq implements ContextMacroAction {
   private static final int init_I = 64;
   private static final int load = 10;
   public autismclient.util.i.DataUtil.Inner_DataUtil filter = autismclient.util.i.DataUtil.Inner_DataUtil.init_DataUtil_DataUtil;
   public aq.Inner_StreamUtil targets = aq.Inner_StreamUtil.init;
   public int count = 1;
   public int intervalTicks = 1;
   public int stopAfter = 0;
   public boolean refresh = true;
   public boolean traders = true;
   public boolean autoEmerald = true;
   public boolean autoShears = true;
   public boolean keepTotem = true;
   public boolean stopWhenMissing = true;
   private boolean init_Z = true;

   @Override
   public void run(MacroExecutionContext ctx) {
      aq.Inner_DataUtil driver = new aq.Inner_DataUtil(
         ctx,
         new autismclient.util.i.DataUtil.CachePool(
            this.filter,
            this.traders,
            this.autoEmerald,
            this.autoShears,
            this.keepTotem,
            this.targets == aq.Inner_StreamUtil.load,
            this.refresh,
            init(this.intervalTicks, 1, 20)
         ),
         init(this.count, 0, 64),
         Math.max(0, this.stopAfter),
         this.stopWhenMissing
      );
      autismclient.util.i.DataUtil.init_V_Predicate(driver::init);

      while (!driver.load_Z && ctx.isActive()) {
         CompletableFuture<Void> next = driver.init_CompletableFuture;
         ctx.setStatus(driver.init_Str());
         ctx.awaitCondition(next);
      }

      autismclient.util.i.DataUtil.ConfigStore last = driver.init_DataUtil_ConfigSto;
      if (driver.init_Z && ctx.isActive()) {
         String why = last.load_Str();
         ctx.runOnClientThread(() -> AutismClientMessaging.sendPrefixed("§cVillager Dupe: " + why));
         MacroExecutor.init_V();
      }

      int total = driver.init_I;
      ctx.setVariable("villager_dupe_count", total);
      ctx.setVariable("villager_dupe_status", total > 0 ? "SENT" : (last.init_Z() ? "MISSING_ITEMS" : (last.load_Z() ? "NO_TARGET" : "BLOCKED")));
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("filter", this.filter.name());
      tag.putString("targets", this.targets.name());
      tag.putInt("count", init(this.count, 0, 64));
      tag.putInt("intervalTicks", init(this.intervalTicks, 1, 20));
      tag.putInt("stopAfter", Math.max(0, this.stopAfter));
      tag.putBoolean("refresh", this.refresh);
      tag.putBoolean("traders", this.traders);
      tag.putBoolean("autoEmerald", this.autoEmerald);
      tag.putBoolean("autoShears", this.autoShears);
      tag.putBoolean("keepTotem", this.keepTotem);
      tag.putBoolean("stopWhenMissing", this.stopWhenMissing);
      tag.putBoolean("enabled", this.init_Z);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.filter = autismclient.util.i.DataUtil.Inner_DataUtil.init_DataUtil_DataUtil_Str(tag.getStringOr("filter", "ALL"));

      try {
         this.targets = aq.Inner_StreamUtil.valueOf(tag.getStringOr("targets", "ALL_IN_RANGE"));
      } catch (IllegalArgumentException e) {
         this.targets = aq.Inner_StreamUtil.init;
      }

      this.count = init(tag.getIntOr("count", 1), 0, 64);
      this.intervalTicks = init(tag.getIntOr("intervalTicks", 1), 1, 20);
      this.stopAfter = Math.max(0, tag.getIntOr("stopAfter", 0));
      this.refresh = tag.getBooleanOr("refresh", true);
      this.traders = tag.getBooleanOr("traders", true);
      this.autoEmerald = tag.getBooleanOr("autoEmerald", true);
      this.autoShears = tag.getBooleanOr("autoShears", true);
      this.keepTotem = tag.getBooleanOr("keepTotem", true);
      this.stopWhenMissing = tag.getBooleanOr("stopWhenMissing", true);
      this.init_Z = tag.getBooleanOr("enabled", true);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_Y_StreamGate;
   }

   @Override
   public String getDisplayName() {
      String passes = this.count <= 0 ? (this.stopAfter > 0 ? " / " + this.stopAfter + " items" : " / until stopped") : " x" + this.count;
      return "Villager Dupe / " + this.filter.init() + (this.targets == aq.Inner_StreamUtil.load ? " / Nearest" : "") + passes;
   }

   @Override
   public String getIcon() {
      return "VD";
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

   private static final class Inner_DataUtil {
      private final MacroExecutionContext init_MacroExecutionCont;
      private final autismclient.util.i.DataUtil init_DataUtil = autismclient.util.i.DataUtil.init_DataUtil();
      private final autismclient.util.i.DataUtil.CachePool init_DataUtil_CachePool;
      private final int load_I;
      private final int read_I;
      private final boolean read_Z;
      private int write_I;
      private int update;
      private boolean write_Z;
      volatile int init_I;
      volatile autismclient.util.i.DataUtil.ConfigStore init_DataUtil_ConfigSto = autismclient.util.i.DataUtil.ConfigStore.load_DataUtil_ConfigSto;
      volatile boolean init_Z;
      volatile boolean load_Z;
      volatile CompletableFuture<Void> init_CompletableFuture = new CompletableFuture<>();

      Inner_DataUtil(MacroExecutionContext ctx, autismclient.util.i.DataUtil.CachePool opts, int passes, int limit, boolean stopWhenMissing) {
         this.init_MacroExecutionCont = ctx;
         this.init_DataUtil_CachePool = opts;
         this.load_I = passes;
         this.read_I = limit;
         this.read_Z = stopWhenMissing;
      }

      boolean init(Minecraft mc) {
         if (!this.init_MacroExecutionCont.isActive()) {
            if (this.init_DataUtil.init_Z()) {
               this.init_DataUtil.read_V_Minecraft(mc);
            }

            return this.init_Z();
         } else if (this.write_Z) {
            if (this.init_DataUtil.init_Z() && this.update++ < 10) {
               this.init_DataUtil.load(mc, this.init_DataUtil_CachePool);
            }

            if (!this.init_DataUtil.init_Z()) {
               return this.init_Z();
            }

            if (this.update < 10) {
               return true;
            }

            this.init_DataUtil.read_V_Minecraft(mc);
            return this.init_Z();
         } else {
            autismclient.util.i.DataUtil.BufferHelper result = this.init_DataUtil
               .init_DataUtil_BufferHel_Minecraft_DataUtil_CachePool(mc, this.init_DataUtil_CachePool);
            this.write_I++;
            autismclient.util.i.DataUtil.ConfigStore status = result.init_DataUtil_ConfigSto();
            boolean change = result.init_I() > 0 || status != this.init_DataUtil_ConfigSto;
            this.init_I = this.init_I + result.init_I();
            this.init_DataUtil_ConfigSto = status;
            if (this.read_Z && status.init_Z()) {
               this.init_Z = true;
               this.write_Z = true;
            } else if (this.read_I > 0 && this.init_I >= this.read_I || this.load_I > 0 && this.write_I >= this.load_I) {
               this.write_Z = true;
            }

            if (change) {
               this.init_V();
            }

            return true;
         }
      }

      String init_Str() {
         autismclient.util.i.DataUtil.ConfigStore status = this.init_DataUtil_ConfigSto;
         return "Villager Dupe: " + this.init_I + (status.load_Z() ? "" : " (" + status.init_Str() + ")");
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

   public enum Inner_StreamUtil {
      init,
      load;
   }
}
