package autismclient.util.macro;

import autismclient.api.macro.ContextMacroAction;
import autismclient.api.macro.MacroExecutionContext;
import autismclient.util.AutismClientMessaging;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MerchantMenu;

public final class am implements ContextMacroAction {
   public am.Inner_StreamUtil target = am.Inner_StreamUtil.OPEN_SCREEN;
   public double range = 4.0;
   public am.Inner_DataUtil selection = am.Inner_DataUtil.OFFER;
   public int offerNumber = 1;
   public String resultItem = "minecraft:emerald";
   public String enchantment = "minecraft:mending";
   public int minLevel = 1;
   public boolean exactLevel;
   public int maxEmeralds = 64;
   public int amount = 1;
   public boolean untilUnavailable;
   public int delayTicks = 4;
   public int timeoutTicks = 100;
   public int emeraldBudget;
   public int keepEmeralds;
   public boolean closeWhenDone;
   private boolean init = true;

   public autismclient.util.h.StreamUtil init() {
      return new autismclient.util.h.StreamUtil(
         this.selection == am.Inner_DataUtil.OFFER ? init(this.offerNumber, 1, 512) : 0,
         this.selection == am.Inner_DataUtil.ITEM ? List.of(autismclient.util.h.StreamUtil.init(this.resultItem)) : List.of(),
         List.of(),
         this.selection == am.Inner_DataUtil.BOOK
            ? List.of(new autismclient.util.h.StreamUtil.Inner_DataUtil(this.enchantment, init(this.minLevel, 1, 255), 0))
            : List.of(),
         this.exactLevel,
         init(this.maxEmeralds, 0, 128)
      );
   }

   @Override
   public void run(MacroExecutionContext ctx) {
      if (this.selection != am.Inner_DataUtil.ITEM || this.resultItem != null && !this.resultItem.isBlank()) {
         autismclient.util.h.CachePool session = new autismclient.util.h.CachePool(
            this.init(), this.untilUnavailable ? 10000 : init(this.amount, 1, 10000), this.delayTicks, this.timeoutTicks, this.emeraldBudget, this.keepEmeralds
         );

         try {
            Boolean reserved = ctx.callOnClientThread(session::read_Z);
            if (!Boolean.TRUE.equals(reserved)) {
               ctx.setVariable("trade_count", 0);
               ctx.setVariable("trade_spent", 0);
               ctx.setVariable("trade_status", "BUSY");
               ctx.setStatus("Trade: another trade is running");
               return;
            }

            if (this.target == am.Inner_StreamUtil.OPEN_SCREEN || this.init(ctx)) {
               while (ctx.isActive()) {
                  autismclient.util.h.CachePool.Inner_CachePool state = ctx.callOnClientThread(
                     () -> ctx.isActive() ? session.init_CachePool_CachePoo_Minecraft(ctx.mc()) : autismclient.util.h.CachePool.Inner_CachePool.CANCELLED
                  );
                  if (state == null) {
                     return;
                  }

                  ctx.setStatus("Trade: " + session.init_I() + " / " + session.init_Str());
                  if (session.load_Z()) {
                     ctx.setVariable("trade_count", session.init_I());
                     ctx.setVariable("trade_spent", session.load_I());
                     ctx.setVariable("trade_status", session.init_CachePool_CachePoo().name());
                     if (session.init_CachePool_CachePoo() != autismclient.util.h.CachePool.Inner_CachePool.COMPLETE) {
                        ctx.runOnClientThread(
                           () -> AutismClientMessaging.sendPrefixed("Trade: " + session.init_Str() + " (" + session.init_I() + " completed)")
                        );
                     }

                     if (this.closeWhenDone) {
                        ctx.runOnClientThread(session::init_V);
                     }

                     return;
                  }

                  ctx.waitTicks(1);
               }

               return;
            }

            ctx.setVariable("trade_count", 0);
            ctx.setVariable("trade_spent", 0);
            ctx.setVariable("trade_status", "NO_MENU");
            ctx.runOnClientThread(() -> AutismClientMessaging.sendPrefixed("Trade: no reachable merchant opened"));
         } finally {
            ctx.runOnClientThread(() -> session.init_V_Minecraft(ctx.mc()));
         }
      } else {
         ctx.setVariable("trade_count", 0);
         ctx.setVariable("trade_spent", 0);
         ctx.setVariable("trade_status", "NO_MATCH");
         ctx.setStatus("Trade: set a result item");
      }
   }

   private boolean init(MacroExecutionContext ctx) {
      if (!Boolean.TRUE
         .equals(
            ctx.callOnClientThread(
               () -> ctx.mc().player != null && ctx.mc().gui.screen() == null && ctx.mc().player.containerMenu == ctx.mc().player.inventoryMenu
            )
         )) {
         return false;
      }

      Entity entity = ctx.callOnClientThread(() -> autismclient.util.h.BufferHelper.init(ctx.mc(), this.target == am.Inner_StreamUtil.AIMED, this.range));
      if (entity == null) {
         return false;
      }

      boolean sent = false;

      for (int tick = 0; tick < this.timeoutTicks && ctx.isActive(); tick++) {
         if (Boolean.TRUE.equals(ctx.callOnClientThread(() -> ctx.mc().player != null && ctx.mc().player.containerMenu instanceof MerchantMenu))) {
            return true;
         }

         if (!sent) {
            sent = Boolean.TRUE.equals(ctx.callOnClientThread(() -> ctx.isActive() && autismclient.util.h.BufferHelper.load(ctx.mc(), entity, this.range)));
         }

         ctx.setStatus("Trade: opening merchant");
         ctx.waitTicks(1);
      }

      return false;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("target", this.target.name());
      tag.putDouble("range", Double.isFinite(this.range) ? Math.clamp(this.range, 1.0, 6.0) : 4.0);
      tag.putString("selection", this.selection.name());
      tag.putInt("offerNumber", init(this.offerNumber, 1, 512));
      tag.putString("resultItem", this.resultItem == null ? "" : this.resultItem.strip());
      tag.putString("enchantment", this.enchantment == null ? "" : this.enchantment.strip());
      tag.putInt("minLevel", init(this.minLevel, 1, 255));
      tag.putBoolean("exactLevel", this.exactLevel);
      tag.putInt("maxEmeralds", init(this.maxEmeralds, 0, 128));
      tag.putInt("amount", init(this.amount, 1, 10000));
      tag.putBoolean("untilUnavailable", this.untilUnavailable);
      tag.putInt("delayTicks", init(this.delayTicks, 1, 200));
      tag.putInt("timeoutTicks", init(this.timeoutTicks, 20, 1200));
      tag.putInt("emeraldBudget", init(this.emeraldBudget, 0, 1000000));
      tag.putInt("keepEmeralds", init(this.keepEmeralds, 0, 2304));
      tag.putBoolean("closeWhenDone", this.closeWhenDone);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      try {
         this.target = am.Inner_StreamUtil.valueOf(tag.getStringOr("target", "OPEN_SCREEN"));
      } catch (IllegalArgumentException e) {
         this.target = am.Inner_StreamUtil.OPEN_SCREEN;
      }

      this.range = tag.getDoubleOr("range", 4.0);
      this.range = Double.isFinite(this.range) ? Math.clamp(this.range, 1.0, 6.0) : 4.0;

      try {
         this.selection = am.Inner_DataUtil.valueOf(tag.getStringOr("selection", "OFFER"));
      } catch (IllegalArgumentException e) {
         this.selection = am.Inner_DataUtil.OFFER;
      }

      this.offerNumber = init(tag.getIntOr("offerNumber", 1), 1, 512);
      this.resultItem = tag.getStringOr("resultItem", "minecraft:emerald").strip();
      this.enchantment = tag.getStringOr("enchantment", "minecraft:mending").strip();
      this.minLevel = init(tag.getIntOr("minLevel", 1), 1, 255);
      this.exactLevel = tag.getBooleanOr("exactLevel", false);
      this.maxEmeralds = init(tag.getIntOr("maxEmeralds", 64), 0, 128);
      this.amount = init(tag.getIntOr("amount", 1), 1, 10000);
      this.untilUnavailable = tag.getBooleanOr("untilUnavailable", false);
      this.delayTicks = init(tag.getIntOr("delayTicks", 4), 1, 200);
      this.timeoutTicks = init(tag.getIntOr("timeoutTicks", 100), 20, 1200);
      this.emeraldBudget = init(tag.getIntOr("emeraldBudget", 0), 0, 1000000);
      this.keepEmeralds = init(tag.getIntOr("keepEmeralds", 0), 0, 2304);
      this.closeWhenDone = tag.getBooleanOr("closeWhenDone", false);
      this.init = tag.getBooleanOr("enabled", true);
   }

   @Override
   public StreamGate getType() {
      return StreamGate.TRADE;
   }

   @Override
   public String getDisplayName() {
      String var10000;
      switch (this.selection) {
         case OFFER:
            var10000 = "Offer " + this.offerNumber;
            break;
         case ITEM:
            var10000 = this.resultItem;
            break;
         case BOOK:
            var10000 = this.enchantment + " " + this.minLevel;
            break;
         default:
            throw new MatchException(null, null);
      }

      return "Trade / " + var10000 + (this.untilUnavailable ? " / All" : " / " + this.amount);
   }

   @Override
   public String getIcon() {
      return "T";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean value) {
      this.init = value;
   }

   private static int init(int n, int min, int max) {
      return Math.max(min, Math.min(max, n));
   }

   public enum Inner_DataUtil {
      OFFER,
      ITEM,
      BOOK;
   }

   public enum Inner_StreamUtil {
      OPEN_SCREEN,
      AIMED,
      NEAREST;
   }
}
