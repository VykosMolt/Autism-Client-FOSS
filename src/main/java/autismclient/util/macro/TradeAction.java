package autismclient.util.macro;

import autismclient.api.macro.ContextMacroAction;
import autismclient.api.macro.MacroExecutionContext;
import autismclient.util.AutismClientMessaging;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MerchantMenu;

public final class TradeAction implements ContextMacroAction {
   public TradeAction.MerchantTarget target = TradeAction.MerchantTarget.OPEN_SCREEN;
   public double range = 4.0;
   public TradeAction.TradeSelection selection = TradeAction.TradeSelection.OFFER;
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
   private boolean enabled = true;

   public autismclient.util.villager.TradeOfferFilter init() {
      return new autismclient.util.villager.TradeOfferFilter(
         this.selection == TradeAction.TradeSelection.OFFER ? clamp(this.offerNumber, 1, 512) : 0,
         this.selection == TradeAction.TradeSelection.ITEM ? List.of(autismclient.util.villager.TradeOfferFilter.normalizeItem(this.resultItem)) : List.of(),
         List.of(),
         this.selection == TradeAction.TradeSelection.BOOK
            ? List.of(new autismclient.util.villager.TradeOfferFilter.Inner_DataUtil(this.enchantment, clamp(this.minLevel, 1, 255), 0))
            : List.of(),
         this.exactLevel,
         clamp(this.maxEmeralds, 0, 128)
      );
   }

   @Override
   public void run(MacroExecutionContext ctx) {
      if (this.selection != TradeAction.TradeSelection.ITEM || this.resultItem != null && !this.resultItem.isBlank()) {
         autismclient.util.villager.VillagerTradeSession session = new autismclient.util.villager.VillagerTradeSession(
            this.init(), this.untilUnavailable ? 10000 : clamp(this.amount, 1, 10000), this.delayTicks, this.timeoutTicks, this.emeraldBudget, this.keepEmeralds
         );

         try {
            Boolean reserved = ctx.callOnClientThread(session::reserve);
            if (!Boolean.TRUE.equals(reserved)) {
               ctx.setVariable("trade_count", 0);
               ctx.setVariable("trade_spent", 0);
               ctx.setVariable("trade_status", "BUSY");
               ctx.setStatus("Trade: another trade is running");
               return;
            }

            if (this.target == TradeAction.MerchantTarget.OPEN_SCREEN || this.init(ctx)) {
               while (ctx.isActive()) {
                  autismclient.util.villager.VillagerTradeSession.Inner_CachePool state = ctx.callOnClientThread(
                     () -> ctx.isActive() ? session.tick(ctx.mc()) : autismclient.util.villager.VillagerTradeSession.Inner_CachePool.CANCELLED
                  );
                  if (state == null) {
                     return;
                  }

                  ctx.setStatus("Trade: " + session.traded() + " / " + session.message());
                  if (session.isFinished()) {
                     ctx.setVariable("trade_count", session.traded());
                     ctx.setVariable("trade_spent", session.spent());
                     ctx.setVariable("trade_status", session.status().name());
                     if (session.status() != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.COMPLETE) {
                        ctx.runOnClientThread(
                           () -> AutismClientMessaging.sendPrefixed("Trade: " + session.message() + " (" + session.traded() + " completed)")
                        );
                     }

                     if (this.closeWhenDone) {
                        ctx.runOnClientThread(session::cancel);
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

      Entity entity = ctx.callOnClientThread(() -> autismclient.util.villager.MerchantInteractHelper.findMerchant(ctx.mc(), this.target == TradeAction.MerchantTarget.AIMED, this.range));
      if (entity == null) {
         return false;
      }

      boolean sent = false;

      for (int tick = 0; tick < this.timeoutTicks && ctx.isActive(); tick++) {
         if (Boolean.TRUE.equals(ctx.callOnClientThread(() -> ctx.mc().player != null && ctx.mc().player.containerMenu instanceof MerchantMenu))) {
            return true;
         }

         if (!sent) {
            sent = Boolean.TRUE.equals(ctx.callOnClientThread(() -> ctx.isActive() && autismclient.util.villager.MerchantInteractHelper.interact(ctx.mc(), entity, this.range)));
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
      tag.putInt("offerNumber", clamp(this.offerNumber, 1, 512));
      tag.putString("resultItem", this.resultItem == null ? "" : this.resultItem.strip());
      tag.putString("enchantment", this.enchantment == null ? "" : this.enchantment.strip());
      tag.putInt("minLevel", clamp(this.minLevel, 1, 255));
      tag.putBoolean("exactLevel", this.exactLevel);
      tag.putInt("maxEmeralds", clamp(this.maxEmeralds, 0, 128));
      tag.putInt("amount", clamp(this.amount, 1, 10000));
      tag.putBoolean("untilUnavailable", this.untilUnavailable);
      tag.putInt("delayTicks", clamp(this.delayTicks, 1, 200));
      tag.putInt("timeoutTicks", clamp(this.timeoutTicks, 20, 1200));
      tag.putInt("emeraldBudget", clamp(this.emeraldBudget, 0, 1000000));
      tag.putInt("keepEmeralds", clamp(this.keepEmeralds, 0, 2304));
      tag.putBoolean("closeWhenDone", this.closeWhenDone);
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      try {
         this.target = TradeAction.MerchantTarget.valueOf(tag.getStringOr("target", "OPEN_SCREEN"));
      } catch (IllegalArgumentException e) {
         this.target = TradeAction.MerchantTarget.OPEN_SCREEN;
      }

      this.range = tag.getDoubleOr("range", 4.0);
      this.range = Double.isFinite(this.range) ? Math.clamp(this.range, 1.0, 6.0) : 4.0;

      try {
         this.selection = TradeAction.TradeSelection.valueOf(tag.getStringOr("selection", "OFFER"));
      } catch (IllegalArgumentException e) {
         this.selection = TradeAction.TradeSelection.OFFER;
      }

      this.offerNumber = clamp(tag.getIntOr("offerNumber", 1), 1, 512);
      this.resultItem = tag.getStringOr("resultItem", "minecraft:emerald").strip();
      this.enchantment = tag.getStringOr("enchantment", "minecraft:mending").strip();
      this.minLevel = clamp(tag.getIntOr("minLevel", 1), 1, 255);
      this.exactLevel = tag.getBooleanOr("exactLevel", false);
      this.maxEmeralds = clamp(tag.getIntOr("maxEmeralds", 64), 0, 128);
      this.amount = clamp(tag.getIntOr("amount", 1), 1, 10000);
      this.untilUnavailable = tag.getBooleanOr("untilUnavailable", false);
      this.delayTicks = clamp(tag.getIntOr("delayTicks", 4), 1, 200);
      this.timeoutTicks = clamp(tag.getIntOr("timeoutTicks", 100), 20, 1200);
      this.emeraldBudget = clamp(tag.getIntOr("emeraldBudget", 0), 0, 1000000);
      this.keepEmeralds = clamp(tag.getIntOr("keepEmeralds", 0), 0, 2304);
      this.closeWhenDone = tag.getBooleanOr("closeWhenDone", false);
      this.enabled = tag.getBooleanOr("enabled", true);
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.TRADE;
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
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean value) {
      this.enabled = value;
   }

   private static int clamp(int n, int min, int max) {
      return Math.max(min, Math.min(max, n));
   }

   public enum TradeSelection {
      OFFER,
      ITEM,
      BOOK;
   }

   public enum MerchantTarget {
      OPEN_SCREEN,
      AIMED,
      NEAREST;
   }
}
