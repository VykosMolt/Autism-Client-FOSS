package autismclient.modules;

import autismclient.api.module.ActionSetting;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.api.module.StringListSetting;
import autismclient.api.module.StringSetting;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismClientLevelPredictionAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class AutoTradeModule extends Module {
   private AutoTradeModule.Inner_DataUtil phase = AutoTradeModule.Inner_DataUtil.SEARCH;
   private Entity villager;
   private MerchantMenu menu;
   private Object level;
   private autismclient.util.villager.VillagerTradeSession session;
   private autismclient.util.villager.TradeOfferFilter offerFilter;
   private AutoTradeModule.Inner_StreamUtil profession;
   private BlockPos workstationPos;
   private Block workstation;
   private boolean rerollMode;
   private boolean matchProfession;
   private boolean lockTrades;
   private boolean write_Z;
   private boolean update_Z;
   private boolean sync_Z;
   private boolean flush_Z;
   private int ticks;
   private int offerWaitTicks;
   private int lastJobTick;
   private int write_I;
   private int nextActionTick;
   private int tradesDone;
   private int emeraldsSpent;
   private int close;
   private int open = -1;
   private int poll = -1;
   private UUID villagerUuid;
   private int push = -1;
   private int pop = -1;
   private String status = "Idle";
   private final Map<UUID, Integer> revisitAt = new HashMap<>();

   public AutoTradeModule() {
      super("auto-trade", "AutoTrade", ae.read, "Trade with villagers or reroll their profession and offers.");
      this.add(new ChoiceSetting("mode", "Mode", "Trade", "Trade", "Reroll"));
      this.add(new ChoiceSetting("target", "Villagers", "Nearby", "Nearby", "Aimed", "Open screen").visibleWhen(() -> !this.isRerollMode()));
      this.add(new DoubleSetting("range", "Reach", 4.0, 1.0, 6.0, 0.25).group("Target"));
      this.add(new BoolSetting("wandering", "Wandering traders", false).group("Target").visibleWhen(() -> !this.isRerollMode()));
      this.add(new StringSetting("villager", "Villager UUID", "").group("Target"));
      this.add(new ActionSetting("capture-villager", "Villager", this::pickLookedVillager).buttonLabel("Use aimed villager").group("Target"));
      this.add(new ChoiceSetting("match", "Match", "Items", "Items", "Books", "Offer", "Profession").group("Offers"));
      this.add(RegistryListSetting.items("results", "Buy items", "minecraft:emerald").group("Offers").visibleWhen(() -> this.choice("match").equals("Items")));
      this.add(
         RegistryListSetting.items("payments", "Sell items")
            .group("Offers")
            .description("Empty allows any payment.")
            .visibleWhen(() -> !this.choice("match").equals("Profession"))
      );
      this.add(new IntSetting("offer", "Offer number", 1, 1, 512, 1).group("Offers").visibleWhen(() -> this.choice("match").equals("Offer")));
      this.add(
         new ChoiceSetting(
               "enchantment",
               "Enchantment",
               "mending",
               "mending",
               "efficiency",
               "unbreaking",
               "fortune",
               "silk_touch",
               "protection",
               "feather_falling",
               "sharpness",
               "looting",
               "power",
               "infinity",
               "aqua_affinity",
               "respiration",
               "depth_strider",
               "frost_walker",
               "thorns",
               "fire_protection",
               "blast_protection",
               "projectile_protection",
               "smite",
               "bane_of_arthropods",
               "knockback",
               "fire_aspect",
               "sweeping_edge",
               "punch",
               "flame",
               "luck_of_the_sea",
               "lure",
               "loyalty",
               "impaling",
               "riptide",
               "channeling",
               "multishot",
               "piercing",
               "quick_charge",
               "density",
               "breach",
               "lunge",
               "binding_curse",
               "vanishing_curse",
               "Custom"
            )
            .group("Books")
            .visibleWhen(this::matchesBooks)
      );
      this.add(
         new StringSetting("custom-enchantment", "Custom enchantment", "minecraft:mending")
            .group("Books")
            .visibleWhen(() -> this.matchesBooks() && this.choice("enchantment").equals("Custom"))
      );
      this.add(new IntSetting("level", "Minimum level", 1, 1, 255, 1).group("Books").visibleWhen(this::matchesBooks));
      this.add(new BoolSetting("exact-level", "Exact level", false).group("Books").visibleWhen(this::matchesBooks));
      this.add(
         new StringListSetting("other-books", "Also accept books").group("Books").visibleWhen(this::matchesBooks).description("enchantment level [max price]")
      );
      this.add(new IntSetting("max-price", "Max emerald price", 64, 0, 128, 1).group("Limits").description("0 allows any price."));
      this.add(new IntSetting("budget", "Emerald budget", 0, 0, 1000000, 1).group("Limits").description("0 is unlimited."));
      this.add(new IntSetting("reserve", "Keep emeralds", 0, 0, 2304, 1).group("Limits"));
      this.add(new IntSetting("trade-limit", "Total trades", 0, 0, 10000, 1).group("Limits").visibleWhen(() -> !this.isRerollMode()));
      this.add(new IntSetting("visit-limit", "Trades per visit", 64, 1, 512, 1).group("Trading").visibleWhen(() -> !this.isRerollMode()));
      this.add(
         new BoolSetting("restock", "Wait for restock", true)
            .group("Trading")
            .visibleWhen(() -> !this.isRerollMode() && !this.choice("target").equals("Open screen"))
      );
      this.add(
         new IntSetting("revisit", "Revisit (seconds)", 15, 2, 600, 1)
            .group("Trading")
            .visibleWhen(() -> !this.isRerollMode() && !this.choice("target").equals("Open screen"))
      );
      this.add(
         new ChoiceSetting("profession", "Profession", "LIBRARIAN", Arrays.stream(AutoTradeModule.Inner_StreamUtil.values()).map(Enum::name).toList())
            .group("Reroll")
            .visibleWhen(this::isRerollMode)
      );
      this.add(new StringSetting("station", "Workstation x y z", "").group("Reroll").visibleWhen(this::isRerollMode));
      this.add(new StringSetting("dimension", "Workstation dimension", "").group("Reroll").visibleWhen(this::isRerollMode));
      this.add(new ActionSetting("capture-station", "Workstation", this::pickLookedWorkstation).buttonLabel("Use aimed block").group("Reroll").visibleWhen(this::isRerollMode));
      this.add(new BoolSetting("lock", "Buy match to lock", false).group("Reroll").visibleWhen(this::isRerollMode));
      this.add(new BoolSetting("refill-station", "Workstations from inventory", true).group("Reroll").visibleWhen(this::isRerollMode));
      this.add(new IntSetting("max-rolls", "Max rerolls", 1000, 1, 100000, 1).group("Reroll").visibleWhen(this::isRerollMode));
      this.add(new BoolSetting("work-hours", "Wait for work hours", true).group("Reroll").visibleWhen(this::isRerollMode));
      this.add(new IntSetting("job-timeout", "Job change timeout (seconds)", 90, 10, 600, 1).group("Reroll").visibleWhen(this::isRerollMode));
      this.add(new IntSetting("delay", "Trade delay (ticks)", 4, 1, 200, 1).group("Timing"));
      this.add(new IntSetting("timeout", "Server timeout (ticks)", 100, 20, 1200, 1).group("Timing"));
      this.add(new IntSetting("run-time", "Run limit (minutes)", 60, 0, 1440, 1).group("Timing"));
   }

   private boolean isRerollMode() {
      return this.choice("mode").equals("Reroll");
   }

   private boolean matchesBooks() {
      return this.choice("match").equals("Books");
   }

   @Override
   public String info() {
      return this.status + " / " + (this.rerollMode ? this.close + " rolls" : this.tradesDone + " trades");
   }

   private void pickLookedVillager() {
      if (MC.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Merchant) {
         this.setValue("villager", hit.getEntity().getUUID().toString());
         autismclient.util.bi.load("Villager selected");
      } else {
         autismclient.util.bi.read("Aim at a villager first");
      }
   }

   private void pickLookedWorkstation() {
      if (MC.level != null
         && MC.hitResult instanceof BlockHitResult hit
         && hit.getType() == Type.BLOCK
         && isWorkstation(MC.level.getBlockState(hit.getBlockPos()).getBlock())) {
         BlockPos pos = hit.getBlockPos();
         this.setValue("station", pos.getX() + " " + pos.getY() + " " + pos.getZ());
         this.setValue("dimension", MC.level.dimension().identifier().toString());
         autismclient.util.bi.load("Workstation selected");
      } else {
         autismclient.util.bi.read("Aim at the workstation first");
      }
   }

   public static boolean isWorkstation(Block block) {
      return Arrays.stream(AutoTradeModule.Inner_StreamUtil.values()).anyMatch(p -> p.init_Block == block);
   }

   public static boolean init(boolean baby, boolean nitwit, int level, int xp, boolean usedOffer) {
      return !baby && !nitwit && level <= 1 && xp == 0 && !usedOffer;
   }

   @Override
   public void onEnable() {
      this.ticks = this.offerWaitTicks = this.lastJobTick = this.nextActionTick = this.tradesDone = this.emeraldsSpent = this.close = 0;
      this.flush_Z = false;
      this.villagerUuid = null;
      this.push = this.pop = -1;
      this.villager = null;
      this.menu = null;
      this.session = null;
      this.workstationPos = null;
      this.write_Z = this.update_Z = false;
      this.open = this.poll = -1;
      this.revisitAt.clear();
      this.rerollMode = this.isRerollMode();
      this.matchProfession = this.choice("match").equals("Profession");
      this.lockTrades = this.bool("lock");
      this.phase = AutoTradeModule.Inner_DataUtil.SEARCH;
      this.status = "Searching";
      this.level = MC.level;
      if (MC.player != null && MC.level != null) {
         try {
            if (!this.rerollMode && this.matchProfession) {
               throw new IllegalArgumentException("Choose Items, Books or Offer for trading");
            }

            List<autismclient.util.villager.TradeOfferFilter.Inner_DataUtil> desired = new ArrayList<>();
            if (this.matchesBooks()) {
               String book = this.choice("enchantment").equals("Custom") ? this.text("custom-enchantment") : this.choice("enchantment");
               if (book.isBlank()) {
                  throw new IllegalArgumentException("Enter a custom enchantment");
               }

               desired.add(new autismclient.util.villager.TradeOfferFilter.Inner_DataUtil(book, this.integer("level"), 0));

               for (String rule : this.list("other-books")) {
                  try {
                     desired.add(autismclient.util.villager.TradeOfferFilter.Inner_DataUtil.init(rule));
                  } catch (IllegalArgumentException e) {
                     throw new IllegalArgumentException("Invalid book rule: " + rule.strip());
                  }
               }
            }

            if (this.choice("match").equals("Items") && this.list("results").isEmpty()) {
               throw new IllegalArgumentException("Choose at least one buy item");
            }

            this.offerFilter = new autismclient.util.villager.TradeOfferFilter(
               this.choice("match").equals("Offer") ? this.integer("offer") : 0,
               this.choice("match").equals("Items") ? this.list("results") : List.of(),
               this.list("payments"),
               desired,
               this.bool("exact-level"),
               this.integer("max-price")
            );
            this.profession = AutoTradeModule.Inner_StreamUtil.valueOf(this.choice("profession"));
            if (this.rerollMode) {
               if (this.matchesBooks() && this.profession != AutoTradeModule.Inner_StreamUtil.LIBRARIAN) {
                  throw new IllegalArgumentException("Choose Librarian for enchanted books");
               }

               String[] xyz = this.text("station").strip().split("\\s+");
               if (xyz.length != 3) {
                  throw new IllegalArgumentException("Select a workstation first");
               }

               try {
                  this.workstationPos = new BlockPos(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
               } catch (NumberFormatException e) {
                  throw new IllegalArgumentException("Workstation must be: x y z");
               }

               if (!this.text("dimension").equals(MC.level.dimension().identifier().toString())) {
                  throw new IllegalArgumentException("Workstation is in another dimension");
               }

               this.workstation = MC.level.getBlockState(this.workstationPos).getBlock();
               if (!isWorkstation(this.workstation) && !MC.level.getBlockState(this.workstationPos).isAir()) {
                  throw new IllegalArgumentException("Selected block is not a workstation");
               }
            }

            if (this.text("villager").isBlank()) {
               if (MC.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Merchant) {
                  this.villager = hit.getEntity();
                  if (!this.rerollMode && this.choice("target").equals("Aimed")) {
                     this.villagerUuid = this.villager.getUUID();
                  }
               } else if (!this.rerollMode && this.choice("target").equals("Aimed")) {
                  throw new IllegalArgumentException("Aim at a villager first");
               }
            } else {
               UUID uuid;
               try {
                  uuid = UUID.fromString(this.text("villager").strip());
               } catch (IllegalArgumentException e) {
                  throw new IllegalArgumentException("Invalid villager UUID");
               }

               for (Entity entity : MC.level.entitiesForRendering()) {
                  if (uuid.equals(entity.getUUID())) {
                     this.villager = entity;
                  }
               }

               if (!(this.villager instanceof Merchant)) {
                  throw new IllegalArgumentException("Selected villager is not loaded");
               }
            }

            if (this.rerollMode && this.villager == null) {
               this.villager = this.nearestVillager();
            }

            if (this.rerollMode && !(this.villager instanceof Villager)) {
               throw new IllegalArgumentException("No adult villager in reach of the workstation");
            }

            if (this.rerollMode && this.villager.position().distanceToSqr(Vec3.atCenterOf(this.workstationPos)) > 16.0) {
               throw new IllegalArgumentException("Villager is too far from the workstation");
            }
         } catch (IllegalArgumentException e) {
            this.init(e.getMessage(), false);
         }
      } else {
         this.init("Join a world first", false);
      }
   }

   @Override
   public void tick() {
      if (this.flush_Z && !this.isEnabled()) {
         this.push();
      } else if (this.isEnabled()) {
         if (MC.player == null || MC.level == null || MC.gameMode == null || MC.getConnection() == null || MC.level != this.level || !MC.player.isAlive()) {
            this.init("World changed", false);
         } else if (!MC.isPaused()) {
            if (++this.ticks > this.integer("run-time") * 1200 && this.integer("run-time") > 0) {
               this.init("Run limit reached", false);
            } else if (this.session != null) {
               this.write();
            } else if (autismclient.util.villager.VillagerTradeSession.isRunning()) {
               this.status = "Waiting for macro trade";
            } else if (this.menu != null && MC.player.containerMenu != this.menu) {
               this.init("Trade screen closed", false);
            } else if (MC.gui.screen() != null && !(MC.player.containerMenu instanceof MerchantMenu)) {
               this.status = "Paused in menu";
            } else if (this.rerollMode
               && this.menu == null
               && MC.player.containerMenu instanceof MerchantMenu
               && this.phase != AutoTradeModule.Inner_DataUtil.OPEN) {
               this.status = "Close the current trade screen";
            } else if (this.rerollMode) {
               this.sync();
            } else {
               this.read();
            }
         }
      }
   }

   private void read() {
      if (MC.player.containerMenu instanceof MerchantMenu menu) {
         if (this.menu == null && !this.choice("target").equals("Open screen") && this.phase != AutoTradeModule.Inner_DataUtil.OPEN) {
            this.status = "Close the current trade screen";
         } else if (menu.getOffers().isEmpty() && ++this.offerWaitTicks < this.integer("timeout")) {
            this.status = "Loading offers";
         } else {
            this.menu = menu;
            this.init_V_I_4(Math.min(this.integer("visit-limit"), this.integer("trade-limit") == 0 ? 10000 : this.integer("trade-limit") - this.tradesDone));
         }
      } else if (this.choice("target").equals("Open screen")) {
         this.status = "Open a trade screen";
      } else if (this.phase == AutoTradeModule.Inner_DataUtil.OPEN) {
         if (++this.offerWaitTicks >= this.integer("timeout")) {
            this.update();
         } else {
            this.status = "Opening villager";
         }
      } else {
         if (this.villager == null || !this.inRange(this.villager) || this.revisitAt.getOrDefault(this.villager.getUUID(), 0) > this.ticks) {
            this.villager = this.nearestVillager();
         }

         if (this.villager == null) {
            this.status = "Waiting for reachable villagers";
         } else if (this.ticks >= this.nextActionTick) {
            this.flush();
         }
      }
   }

   private void init_V_I_4(int count) {
      if (this.integer("budget") > 0 && this.emeraldsSpent >= this.integer("budget")) {
         this.init("Emerald budget reached", true);
      } else {
         this.session = new autismclient.util.villager.VillagerTradeSession(
            this.offerFilter,
            Math.max(1, count),
            this.integer("delay"),
            this.integer("timeout"),
            this.integer("budget") == 0 ? 0 : this.integer("budget") - this.emeraldsSpent,
            this.integer("reserve")
         );
         if (!this.session.reserve()) {
            this.session = null;
            this.status = "Waiting for macro trade";
         }
      }
   }

   private void write() {
      this.session.tick(MC);
      this.status = this.session.message();
      if (this.session.isFinished()) {
         autismclient.util.villager.VillagerTradeSession.Inner_CachePool result = this.session.status();
         this.tradesDone = this.tradesDone + this.session.traded();
         this.emeraldsSpent = this.emeraldsSpent + this.session.spent();
         this.session.init_V_Minecraft(MC);
         this.session = null;
         if (this.rerollMode) {
            this.init(
               result == autismclient.util.villager.VillagerTradeSession.Inner_CachePool.COMPLETE ? "Match purchased and locked" : "Match found; " + this.status,
               result == autismclient.util.villager.VillagerTradeSession.Inner_CachePool.COMPLETE
            );
         } else if (result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.UNCERTAIN
            && result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.REJECTED
            && result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.CURSOR_BUSY
            && result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.INVENTORY_FULL
            && result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.BUDGET
            && result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.CANCELLED
            && result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.NO_MENU) {
            if (this.integer("trade-limit") > 0 && this.tradesDone >= this.integer("trade-limit")) {
               this.init("Trade limit reached", true);
            } else if (this.choice("target").equals("Open screen")) {
               this.init(this.status, this.tradesDone > 0);
            } else {
               this.update();
            }
         } else {
            this.init(this.status, false);
         }
      }
   }

   private void update() {
      if (this.villager != null) {
         this.revisitAt.put(this.villager.getUUID(), this.bool("restock") ? this.ticks + this.integer("revisit") * 20 : 2147483647);
      }

      this.poll();
      this.villager = null;
      this.phase = AutoTradeModule.Inner_DataUtil.SEARCH;
      this.offerWaitTicks = 0;
      this.nextActionTick = this.ticks + this.integer("delay");
   }

   private void sync() {
      if (!(this.villager instanceof Villager villager && this.inRange(villager))) {
         this.init("Selected villager is out of reach", false);
      } else if (!villager.isBaby() && !villager.getVillagerData().profession().is(VillagerProfession.NITWIT)) {
         this.offerWaitTicks++;
         if (this.ticks - this.lastJobTick > this.integer("job-timeout") * 20) {
            this.init("Job change timed out; check workstation access", false);
         } else {
            switch (this.phase) {
               case SEARCH:
                  if (villager.getVillagerData().profession().is(VillagerProfession.NONE)) {
                     if (!init(false, false, villager.getVillagerData().level(), villager.getVillagerXp(), false)) {
                        this.init("Villager profession is locked", false);
                        return;
                     }

                     if (MC.level.getBlockState(this.workstationPos).isAir()) {
                        this.write_Z = true;
                        this.init(AutoTradeModule.Inner_DataUtil.PLACE, "Placing workstation");
                     } else {
                        this.init(AutoTradeModule.Inner_DataUtil.BREAK, "Removing selected workstation");
                     }
                  } else {
                     this.flush();
                  }
                  break;
               case OPEN:
                  if (MC.player.containerMenu instanceof MerchantMenu menu) {
                     this.menu = menu;
                     if (!menu.getOffers().isEmpty()) {
                        this.init(AutoTradeModule.Inner_DataUtil.INSPECT, "Inspecting offers");
                     }
                  } else if (this.offerWaitTicks >= this.integer("timeout")) {
                     this.init("Villager did not open", false);
                  }
                  break;
               case INSPECT:
                  MerchantMenu menu = this.menu;
                  boolean match = this.hasProfession(villager) && (this.matchProfession || this.matchingOfferIndex(menu) >= 0);
                  if (match) {
                     if (this.lockTrades) {
                        this.init(AutoTradeModule.Inner_DataUtil.LOCK, "Locking match");
                        this.init_V_I_4(1);
                     } else {
                        this.init("Match found; trade once to lock it", true);
                     }

                     return;
                  }

                  if (!init(
                     villager.isBaby(), false, menu.getTraderLevel(), menu.getTraderXp(), menu.getOffers().stream().anyMatch(offer -> offer.getUses() > 0)
                  )) {
                     this.init("Villager already traded; profession is locked", false);
                     return;
                  }

                  if (this.close >= this.integer("max-rolls")) {
                     this.init("Reroll limit reached", false);
                     return;
                  }

                  this.poll();
                  this.lastJobTick = this.ticks;
                  this.init(AutoTradeModule.Inner_DataUtil.BREAK, "Removing selected workstation");
                  break;
               case BREAK:
                  if (this.bool("work-hours") && !isWorkTime(MC.level.getOverworldClockTime())) {
                     this.lastJobTick = this.ticks;
                     this.offerWaitTicks = 0;
                     this.status = "Waiting for work hours";
                     return;
                  }

                  if (this.load_I(this.profession.init_Block) < 0) {
                     this.status = "Need spare " + this.profession.init_Block.getName().getString();
                     return;
                  }

                  Block current = MC.level.getBlockState(this.workstationPos).getBlock();
                  if (MC.level.getBlockState(this.workstationPos).isAir()) {
                     if (this.push >= 0 && !autismclient.util.villager.BlockChangeAckTracker.isAcknowledged(MC.level, this.push)) {
                        return;
                     }

                     MC.gameMode.stopDestroyBlock();
                     this.update_Z = false;
                     this.write_Z = true;
                     this.init(AutoTradeModule.Inner_DataUtil.LOSE_JOB, "Waiting for unemployment");
                     return;
                  }

                  if (current != this.workstation && current != this.profession.init_Block) {
                     this.init("Workstation block changed", false);
                     return;
                  }

                  BlockHitResult hit = this.hitFor(this.workstationPos);
                  if (hit == null) {
                     this.status = "Move within sight of workstation";
                     return;
                  }

                  if (!this.canReach(hit.getLocation())) {
                     return;
                  }

                  this.close();
                  if (!this.update_Z) {
                     MC.gameMode.startDestroyBlock(this.workstationPos, hit.getDirection());
                     this.update_Z = true;
                  } else {
                     MC.gameMode.continueDestroyBlock(this.workstationPos, hit.getDirection());
                  }

                  this.push = this.predictionSequence();
                  if (MC.level.getBlockState(this.workstationPos).isAir()) {
                     this.write_Z = true;
                  }

                  Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
                  break;
               case LOSE_JOB:
                  if (villager.getVillagerData().profession().is(VillagerProfession.NONE)) {
                     this.init(AutoTradeModule.Inner_DataUtil.PLACE, "Placing workstation");
                  } else if (!MC.level.getBlockState(this.workstationPos).isAir()) {
                     this.write_Z = false;
                     this.init(AutoTradeModule.Inner_DataUtil.BREAK, "Workstation break rejected");
                  }
                  break;
               case PLACE:
                  if (MC.level.getBlockState(this.workstationPos).is(this.profession.init_Block)) {
                     this.init(AutoTradeModule.Inner_DataUtil.ACQUIRE_JOB, "Waiting for profession");
                     return;
                  }

                  if (!MC.level.getBlockState(this.workstationPos).isAir()) {
                     this.init("Workstation space is occupied", false);
                     return;
                  }

                  if (this.load_Z(this.profession.init_Block)) {
                     this.init(AutoTradeModule.Inner_DataUtil.ACQUIRE_JOB, "Waiting for profession");
                  }
                  break;
               case ACQUIRE_JOB:
                  if (!this.hasProfession(villager)
                     || !MC.level.getBlockState(this.workstationPos).is(this.profession.init_Block)
                     || this.pop >= 0 && !autismclient.util.villager.BlockChangeAckTracker.isAcknowledged(MC.level, this.pop)) {
                     if (this.pop >= 0 && autismclient.util.villager.BlockChangeAckTracker.isAcknowledged(MC.level, this.pop) && MC.level.getBlockState(this.workstationPos).isAir()) {
                        this.init(AutoTradeModule.Inner_DataUtil.PLACE, "Retrying workstation placement");
                     } else if (!villager.getVillagerData().profession().is(VillagerProfession.NONE) && !this.hasProfession(villager)) {
                        this.init("Villager claimed another workstation", false);
                     }
                  } else {
                     this.write_Z = false;
                     this.close++;
                     this.lastJobTick = this.ticks;
                     this.open();
                     this.init(AutoTradeModule.Inner_DataUtil.SEARCH, "Inspecting new profession");
                     this.flush();
                  }
               case LOCK:
            }
         }
      } else {
         this.init("This villager cannot take a profession", false);
      }
   }

   public static boolean isWorkTime(long dayTime) {
      long time = Math.floorMod(dayTime, 24000L);
      return time >= 2000L && time < 9000L;
   }

   private boolean hasProfession(Villager villager) {
      return villager.getVillagerData()
         .profession()
         .unwrapKey()
         .map(k -> k.identifier().getPath().equals(this.profession.name().toLowerCase(Locale.ROOT)))
         .orElse(false);
   }

   private int matchingOfferIndex(MerchantMenu menu) {
      for (int i = 0; i < menu.getOffers().size(); i++) {
         if (this.offerFilter.matches(menu.getOffers().get(i), i)) {
            return i;
         }
      }

      return -1;
   }

   private void init(AutoTradeModule.Inner_DataUtil value, String message) {
      this.phase = value;
      this.offerWaitTicks = 0;
      this.status = message;
   }

   private Entity nearestVillager() {
      Entity best = null;
      double distance = 1.7976931348623157E308;

      for (Entity entity : MC.level.entitiesForRendering()) {
         if (entity instanceof Merchant
            && (this.bool("wandering") || entity instanceof Villager)
            && this.inRange(entity)
            && !(entity instanceof Villager v && (v.isBaby() || v.getVillagerData().profession().is(VillagerProfession.NITWIT)))
            && (this.text("villager").isBlank() || entity.getUUID().toString().equals(this.text("villager").strip()))
            && (this.villagerUuid == null || entity.getUUID().equals(this.villagerUuid))
            && (!this.rerollMode || this.workstationPos != null && !(entity.position().distanceToSqr(Vec3.atCenterOf(this.workstationPos)) > 16.0))
            && this.revisitAt.getOrDefault(entity.getUUID(), 0) <= this.ticks) {
            double d = MC.player.distanceToSqr(entity);
            if (d < distance) {
               distance = d;
               best = entity;
            }
         }
      }

      return best;
   }

   private boolean inRange(Entity entity) {
      return autismclient.util.villager.MerchantInteractHelper.inRange(MC, entity, this.decimal("range"));
   }

   private void flush() {
      if (this.inRange(this.villager)) {
         if (MC.player.containerMenu != MC.player.inventoryMenu || MC.gui.screen() != null) {
            this.status = "Close the current screen";
         } else if (this.canReach(this.villager.getBoundingBox().getCenter())) {
            MC.gameMode
               .interact(
                  MC.player, this.villager, new EntityHitResult(this.villager, this.villager.getBoundingBox().getCenter()), InteractionHand.MAIN_HAND
               );
            this.init(AutoTradeModule.Inner_DataUtil.OPEN, "Opening villager");
         }
      }
   }

   private boolean canReach(Vec3 point) {
      return autismclient.util.villager.MerchantInteractHelper.lookAt(MC, point);
   }

   private BlockHitResult hitFor(BlockPos pos) {
      Vec3 eye = MC.player.getEyePosition();

      for (Direction face : Direction.values()) {
         Vec3 point = Vec3.atCenterOf(pos).add(face.getStepX() * 0.499, face.getStepY() * 0.499, face.getStepZ() * 0.499);
         if (!(eye.distanceToSqr(point) > MC.player.blockInteractionRange() * MC.player.blockInteractionRange())) {
            BlockHitResult hit = MC.level.clip(new ClipContext(eye, point, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, MC.player));
            if (hit.getType() == Type.BLOCK && hit.getBlockPos().equals(pos)) {
               return hit;
            }
         }
      }

      return null;
   }

   private boolean load_Z(Block block) {
      int slot = this.load_I(block);
      if (slot < 0) {
         this.status = "Waiting for workstation in hotbar";
         return false;
      }

      if (this.ticks % 5 != 0) {
         return false;
      }

      for (Direction direction : Direction.values()) {
         BlockPos support = this.workstationPos.relative(direction);
         if (!MC.level.getBlockState(support).isAir()) {
            Direction face = direction.getOpposite();
            Vec3 point = Vec3.atCenterOf(support).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
            Vec3 rayEnd = point.add(direction.getStepX() * 0.001, direction.getStepY() * 0.001, direction.getStepZ() * 0.001);
            if (!(MC.player.getEyePosition().distanceToSqr(point) > MC.player.blockInteractionRange() * MC.player.blockInteractionRange())) {
               BlockHitResult ray = MC.level
                  .clip(new ClipContext(MC.player.getEyePosition(), rayEnd, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, MC.player));
               if (ray.getType() == Type.BLOCK && ray.getBlockPos().equals(support) && ray.getDirection() == face) {
                  if (!this.canReach(point)) {
                     return false;
                  }

                  this.load(slot);
                  MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, new BlockHitResult(point, face, support, false));
                  this.pop = this.predictionSequence();
                  Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
                  return true;
               }
            }
         }
      }

      return false;
   }

   private int hotbarSlotOf(Block block) {
      for (int i = 0; i < 9; i++) {
         if (MC.player.getInventory().getItem(i).is(block.asItem())) {
            return i;
         }
      }

      return -1;
   }

   private int predictionSequence() {
      return ((AutismClientLevelPredictionAccessor)MC.level).autism$getBlockPredictionHandler().currentSequence();
   }

   private int load_I(Block block) {
      int slot = this.hotbarSlotOf(block);
      if (slot < 0 && this.bool("refill-station") && MC.player.containerMenu == MC.player.inventoryMenu) {
         for (int i = 9; i < 36; i++) {
            if (MC.player.getInventory().getItem(i).is(block.asItem())) {
               if (this.open < 0) {
                  this.open = MC.player.getInventory().getSelectedSlot();
               }

               this.poll = autismclient.util.InventorySlotHelper.ensureItemInHotbar(MC, BuiltInRegistries.ITEM.getKey(block.asItem()).toString(), this.open);
               return this.poll;
            }
         }

         return -1;
      } else {
         return slot;
      }
   }

   private void close() {
      int best = MC.player.getInventory().getSelectedSlot();
      float speed = 1.0F;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = MC.player.getInventory().getItem(i);
         if (!stack.isDamageableItem() || stack.getMaxDamage() - stack.getDamageValue() > 1) {
            float candidate = stack.getDestroySpeed(MC.level.getBlockState(this.workstationPos));
            if (candidate > speed) {
               speed = candidate;
               best = i;
            }
         }
      }

      this.load(best);
   }

   private void load(int slot) {
      if (this.open < 0) {
         this.open = MC.player.getInventory().getSelectedSlot();
      }

      autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
      this.poll = slot;
   }

   private void open() {
      if (MC.player != null && this.open >= 0 && MC.player.getInventory().getSelectedSlot() == this.poll) {
         autismclient.util.InventorySlotHelper.selectHotbarSlotForce(MC, this.open);
      }

      this.open = this.poll = -1;
   }

   private void poll() {
      if (MC.player != null && this.menu != null && MC.player.containerMenu == this.menu) {
         MC.player.closeContainer();
      }

      this.menu = null;
   }

   private void init(String message, boolean success) {
      this.status = message;
      if (success) {
         autismclient.util.bi.load(message);
      } else {
         autismclient.util.bi.read(message);
      }

      this.sync_Z = true;
      this.setEnabled(false);
      this.sync_Z = false;
   }

   @Override
   public void onDisable() {
      if (this.session != null) {
         this.session.init_V_Minecraft(MC);
         this.session = null;
      }

      if (MC.gameMode != null && this.update_Z) {
         MC.gameMode.stopDestroyBlock();
      }

      this.update_Z = false;
      if (this.write_Z && MC.level == this.level && MC.player != null && this.workstationPos != null && !aD.read_Z()) {
         this.poll();
         this.flush_Z = true;
         this.write_I = 0;
         this.status = "Restoring workstation";
      }

      if (!this.flush_Z) {
         this.open();
      }

      if (!this.sync_Z) {
         this.poll();
         if (!this.flush_Z) {
            this.status = "Stopped";
         }
      }
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.flush_Z;
   }

   private void push() {
      this.ticks++;
      if (MC.player == null || MC.level != this.level || MC.getConnection() == null || aD.read_Z() || ++this.write_I > this.integer("timeout")) {
         this.flush_Z = false;
         this.open();
         autismclient.util.bi.read("Replace workstation at " + this.workstationPos.toShortString());
      } else if (this.push < 0 || autismclient.util.villager.BlockChangeAckTracker.isAcknowledged(MC.level, this.push)) {
         if (!MC.level.getBlockState(this.workstationPos).isAir()) {
            if (this.pop < 0 || autismclient.util.villager.BlockChangeAckTracker.isAcknowledged(MC.level, this.pop)) {
               this.flush_Z = false;
               this.write_Z = false;
               this.open();
               this.status = "Stopped";
            }
         } else if (MC.player.containerMenu == MC.player.inventoryMenu && MC.gui.screen() == null) {
            this.load_Z(isWorkstation(this.workstation) && this.load_I(this.workstation) >= 0 ? this.workstation : this.profession.init_Block);
         }
      }
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      if (this.isEnabled()) {
         this.init("Settings changed; enable to restart", false);
      }
   }

   @Override
   public void onGameLeft() {
      autismclient.util.villager.VillagerTradeSession var1 = this.session;
      if (var1 != null) {
         var1.init_V_Minecraft(Module.MC);
      }

      this.session = null;
      this.offerFilter = null;
      this.profession = null;
      this.villager = null;
      this.menu = null;
      this.level = null;
      this.workstationPos = null;
      this.workstation = null;
      this.villagerUuid = null;
      this.update_Z = false;
      this.write_Z = false;
      this.flush_Z = false;
      this.sync_Z = false;
      this.ticks = 0;
      this.offerWaitTicks = 0;
      this.lastJobTick = 0;
      this.write_I = 0;
      this.nextActionTick = 0;
      this.tradesDone = 0;
      this.emeraldsSpent = 0;
      this.close = 0;
      this.open = -1;
      this.poll = -1;
      this.push = -1;
      this.pop = -1;
      this.phase = AutoTradeModule.Inner_DataUtil.SEARCH;
      this.status = "Stopped";
      Map var2 = this.revisitAt;
      if (var2 != null) {
         var2.clear();
      }
   }

   private enum Inner_DataUtil {
      SEARCH,
      OPEN,
      INSPECT,
      BREAK,
      LOSE_JOB,
      PLACE,
      ACQUIRE_JOB,
      LOCK;
   }

   public enum Inner_StreamUtil {
      ARMORER(Blocks.BLAST_FURNACE),
      BUTCHER(Blocks.SMOKER),
      CARTOGRAPHER(Blocks.CARTOGRAPHY_TABLE),
      CLERIC(Blocks.BREWING_STAND),
      FARMER(Blocks.COMPOSTER),
      FISHERMAN(Blocks.BARREL),
      FLETCHER(Blocks.FLETCHING_TABLE),
      LEATHERWORKER(Blocks.CAULDRON),
      LIBRARIAN(Blocks.LECTERN),
      MASON(Blocks.STONECUTTER),
      SHEPHERD(Blocks.LOOM),
      TOOLSMITH(Blocks.SMITHING_TABLE),
      WEAPONSMITH(Blocks.GRINDSTONE);

      final Block init_Block;

      Inner_StreamUtil(Block block) {
         this.init_Block = block;
      }
   }
}
