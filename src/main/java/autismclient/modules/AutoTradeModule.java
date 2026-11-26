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
   private AutoTradeModule.Inner_DataUtil init_MethodUtil_DataUti = AutoTradeModule.Inner_DataUtil.SEARCH;
   private Entity init_Entity;
   private MerchantMenu init_MerchantMenu;
   private Object init_Obj;
   private autismclient.util.villager.VillagerTradeSession init_CachePool;
   private autismclient.util.villager.TradeOfferFilter init_StreamUtil;
   private AutoTradeModule.Inner_StreamUtil init_MethodUtil_StreamU;
   private BlockPos init_BlockPos;
   private Block init_Block;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private boolean write_Z;
   private boolean update_Z;
   private boolean sync_Z;
   private boolean flush_Z;
   private int init_I;
   private int load_I;
   private int read_I;
   private int write_I;
   private int update_I;
   private int sync_I;
   private int flush_I;
   private int close;
   private int open = -1;
   private int poll = -1;
   private UUID init_UUID;
   private int push = -1;
   private int pop = -1;
   private String init_Str = "Idle";
   private final Map<UUID, Integer> init_Map = new HashMap<>();

   public AutoTradeModule() {
      super("auto-trade", "AutoTrade", ae.read, "Trade with villagers or reroll their profession and offers.");
      this.add(new ChoiceSetting("mode", "Mode", "Trade", "Trade", "Reroll"));
      this.add(new ChoiceSetting("target", "Villagers", "Nearby", "Nearby", "Aimed", "Open screen").visibleWhen(() -> !this.init_Z_12()));
      this.add(new DoubleSetting("range", "Reach", 4.0, 1.0, 6.0, 0.25).group("Target"));
      this.add(new BoolSetting("wandering", "Wandering traders", false).group("Target").visibleWhen(() -> !this.init_Z_12()));
      this.add(new StringSetting("villager", "Villager UUID", "").group("Target"));
      this.add(new ActionSetting("capture-villager", "Villager", this::init_V_11).buttonLabel("Use aimed villager").group("Target"));
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
            .visibleWhen(this::load_Z_6)
      );
      this.add(
         new StringSetting("custom-enchantment", "Custom enchantment", "minecraft:mending")
            .group("Books")
            .visibleWhen(() -> this.load_Z_6() && this.choice("enchantment").equals("Custom"))
      );
      this.add(new IntSetting("level", "Minimum level", 1, 1, 255, 1).group("Books").visibleWhen(this::load_Z_6));
      this.add(new BoolSetting("exact-level", "Exact level", false).group("Books").visibleWhen(this::load_Z_6));
      this.add(
         new StringListSetting("other-books", "Also accept books").group("Books").visibleWhen(this::load_Z_6).description("enchantment level [max price]")
      );
      this.add(new IntSetting("max-price", "Max emerald price", 64, 0, 128, 1).group("Limits").description("0 allows any price."));
      this.add(new IntSetting("budget", "Emerald budget", 0, 0, 1000000, 1).group("Limits").description("0 is unlimited."));
      this.add(new IntSetting("reserve", "Keep emeralds", 0, 0, 2304, 1).group("Limits"));
      this.add(new IntSetting("trade-limit", "Total trades", 0, 0, 10000, 1).group("Limits").visibleWhen(() -> !this.init_Z_12()));
      this.add(new IntSetting("visit-limit", "Trades per visit", 64, 1, 512, 1).group("Trading").visibleWhen(() -> !this.init_Z_12()));
      this.add(
         new BoolSetting("restock", "Wait for restock", true)
            .group("Trading")
            .visibleWhen(() -> !this.init_Z_12() && !this.choice("target").equals("Open screen"))
      );
      this.add(
         new IntSetting("revisit", "Revisit (seconds)", 15, 2, 600, 1)
            .group("Trading")
            .visibleWhen(() -> !this.init_Z_12() && !this.choice("target").equals("Open screen"))
      );
      this.add(
         new ChoiceSetting("profession", "Profession", "LIBRARIAN", Arrays.stream(AutoTradeModule.Inner_StreamUtil.values()).map(Enum::name).toList())
            .group("Reroll")
            .visibleWhen(this::init_Z_12)
      );
      this.add(new StringSetting("station", "Workstation x y z", "").group("Reroll").visibleWhen(this::init_Z_12));
      this.add(new StringSetting("dimension", "Workstation dimension", "").group("Reroll").visibleWhen(this::init_Z_12));
      this.add(new ActionSetting("capture-station", "Workstation", this::load_V_8).buttonLabel("Use aimed block").group("Reroll").visibleWhen(this::init_Z_12));
      this.add(new BoolSetting("lock", "Buy match to lock", false).group("Reroll").visibleWhen(this::init_Z_12));
      this.add(new BoolSetting("refill-station", "Workstations from inventory", true).group("Reroll").visibleWhen(this::init_Z_12));
      this.add(new IntSetting("max-rolls", "Max rerolls", 1000, 1, 100000, 1).group("Reroll").visibleWhen(this::init_Z_12));
      this.add(new BoolSetting("work-hours", "Wait for work hours", true).group("Reroll").visibleWhen(this::init_Z_12));
      this.add(new IntSetting("job-timeout", "Job change timeout (seconds)", 90, 10, 600, 1).group("Reroll").visibleWhen(this::init_Z_12));
      this.add(new IntSetting("delay", "Trade delay (ticks)", 4, 1, 200, 1).group("Timing"));
      this.add(new IntSetting("timeout", "Server timeout (ticks)", 100, 20, 1200, 1).group("Timing"));
      this.add(new IntSetting("run-time", "Run limit (minutes)", 60, 0, 1440, 1).group("Timing"));
   }

   private boolean init_Z_12() {
      return this.choice("mode").equals("Reroll");
   }

   private boolean load_Z_6() {
      return this.choice("match").equals("Books");
   }

   @Override
   public String info() {
      return this.init_Str + " / " + (this.init_Z ? this.close + " rolls" : this.sync_I + " trades");
   }

   private void init_V_11() {
      if (MC.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Merchant) {
         this.setValue("villager", hit.getEntity().getUUID().toString());
         autismclient.util.bi.load("Villager selected");
      } else {
         autismclient.util.bi.read("Aim at a villager first");
      }
   }

   private void load_V_8() {
      if (MC.level != null
         && MC.hitResult instanceof BlockHitResult hit
         && hit.getType() == Type.BLOCK
         && init_Z_Block_2(MC.level.getBlockState(hit.getBlockPos()).getBlock())) {
         BlockPos pos = hit.getBlockPos();
         this.setValue("station", pos.getX() + " " + pos.getY() + " " + pos.getZ());
         this.setValue("dimension", MC.level.dimension().identifier().toString());
         autismclient.util.bi.load("Workstation selected");
      } else {
         autismclient.util.bi.read("Aim at the workstation first");
      }
   }

   public static boolean init_Z_Block_2(Block block) {
      return Arrays.stream(AutoTradeModule.Inner_StreamUtil.values()).anyMatch(p -> p.init_Block == block);
   }

   public static boolean init(boolean baby, boolean nitwit, int level, int xp, boolean usedOffer) {
      return !baby && !nitwit && level <= 1 && xp == 0 && !usedOffer;
   }

   @Override
   public void onEnable() {
      this.init_I = this.load_I = this.read_I = this.update_I = this.sync_I = this.flush_I = this.close = 0;
      this.flush_Z = false;
      this.init_UUID = null;
      this.push = this.pop = -1;
      this.init_Entity = null;
      this.init_MerchantMenu = null;
      this.init_CachePool = null;
      this.init_BlockPos = null;
      this.write_Z = this.update_Z = false;
      this.open = this.poll = -1;
      this.init_Map.clear();
      this.init_Z = this.init_Z_12();
      this.load_Z = this.choice("match").equals("Profession");
      this.read_Z = this.bool("lock");
      this.init_MethodUtil_DataUti = AutoTradeModule.Inner_DataUtil.SEARCH;
      this.init_Str = "Searching";
      this.init_Obj = MC.level;
      if (MC.player != null && MC.level != null) {
         try {
            if (!this.init_Z && this.load_Z) {
               throw new IllegalArgumentException("Choose Items, Books or Offer for trading");
            }

            List<autismclient.util.villager.TradeOfferFilter.Inner_DataUtil> desired = new ArrayList<>();
            if (this.load_Z_6()) {
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

            this.init_StreamUtil = new autismclient.util.villager.TradeOfferFilter(
               this.choice("match").equals("Offer") ? this.integer("offer") : 0,
               this.choice("match").equals("Items") ? this.list("results") : List.of(),
               this.list("payments"),
               desired,
               this.bool("exact-level"),
               this.integer("max-price")
            );
            this.init_MethodUtil_StreamU = AutoTradeModule.Inner_StreamUtil.valueOf(this.choice("profession"));
            if (this.init_Z) {
               if (this.load_Z_6() && this.init_MethodUtil_StreamU != AutoTradeModule.Inner_StreamUtil.LIBRARIAN) {
                  throw new IllegalArgumentException("Choose Librarian for enchanted books");
               }

               String[] xyz = this.text("station").strip().split("\\s+");
               if (xyz.length != 3) {
                  throw new IllegalArgumentException("Select a workstation first");
               }

               try {
                  this.init_BlockPos = new BlockPos(Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
               } catch (NumberFormatException e) {
                  throw new IllegalArgumentException("Workstation must be: x y z");
               }

               if (!this.text("dimension").equals(MC.level.dimension().identifier().toString())) {
                  throw new IllegalArgumentException("Workstation is in another dimension");
               }

               this.init_Block = MC.level.getBlockState(this.init_BlockPos).getBlock();
               if (!init_Z_Block_2(this.init_Block) && !MC.level.getBlockState(this.init_BlockPos).isAir()) {
                  throw new IllegalArgumentException("Selected block is not a workstation");
               }
            }

            if (this.text("villager").isBlank()) {
               if (MC.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Merchant) {
                  this.init_Entity = hit.getEntity();
                  if (!this.init_Z && this.choice("target").equals("Aimed")) {
                     this.init_UUID = this.init_Entity.getUUID();
                  }
               } else if (!this.init_Z && this.choice("target").equals("Aimed")) {
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
                     this.init_Entity = entity;
                  }
               }

               if (!(this.init_Entity instanceof Merchant)) {
                  throw new IllegalArgumentException("Selected villager is not loaded");
               }
            }

            if (this.init_Z && this.init_Entity == null) {
               this.init_Entity = this.init_Entity();
            }

            if (this.init_Z && !(this.init_Entity instanceof Villager)) {
               throw new IllegalArgumentException("No adult villager in reach of the workstation");
            }

            if (this.init_Z && this.init_Entity.position().distanceToSqr(Vec3.atCenterOf(this.init_BlockPos)) > 16.0) {
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
         if (MC.player == null || MC.level == null || MC.gameMode == null || MC.getConnection() == null || MC.level != this.init_Obj || !MC.player.isAlive()) {
            this.init("World changed", false);
         } else if (!MC.isPaused()) {
            if (++this.init_I > this.integer("run-time") * 1200 && this.integer("run-time") > 0) {
               this.init("Run limit reached", false);
            } else if (this.init_CachePool != null) {
               this.write();
            } else if (autismclient.util.villager.VillagerTradeSession.init_Z()) {
               this.init_Str = "Waiting for macro trade";
            } else if (this.init_MerchantMenu != null && MC.player.containerMenu != this.init_MerchantMenu) {
               this.init("Trade screen closed", false);
            } else if (MC.gui.screen() != null && !(MC.player.containerMenu instanceof MerchantMenu)) {
               this.init_Str = "Paused in menu";
            } else if (this.init_Z
               && this.init_MerchantMenu == null
               && MC.player.containerMenu instanceof MerchantMenu
               && this.init_MethodUtil_DataUti != AutoTradeModule.Inner_DataUtil.OPEN) {
               this.init_Str = "Close the current trade screen";
            } else if (this.init_Z) {
               this.sync();
            } else {
               this.read();
            }
         }
      }
   }

   private void read() {
      if (MC.player.containerMenu instanceof MerchantMenu menu) {
         if (this.init_MerchantMenu == null && !this.choice("target").equals("Open screen") && this.init_MethodUtil_DataUti != AutoTradeModule.Inner_DataUtil.OPEN) {
            this.init_Str = "Close the current trade screen";
         } else if (menu.getOffers().isEmpty() && ++this.load_I < this.integer("timeout")) {
            this.init_Str = "Loading offers";
         } else {
            this.init_MerchantMenu = menu;
            this.init_V_I_4(Math.min(this.integer("visit-limit"), this.integer("trade-limit") == 0 ? 10000 : this.integer("trade-limit") - this.sync_I));
         }
      } else if (this.choice("target").equals("Open screen")) {
         this.init_Str = "Open a trade screen";
      } else if (this.init_MethodUtil_DataUti == AutoTradeModule.Inner_DataUtil.OPEN) {
         if (++this.load_I >= this.integer("timeout")) {
            this.update();
         } else {
            this.init_Str = "Opening villager";
         }
      } else {
         if (this.init_Entity == null || !this.init_Z_Entity_3(this.init_Entity) || this.init_Map.getOrDefault(this.init_Entity.getUUID(), 0) > this.init_I) {
            this.init_Entity = this.init_Entity();
         }

         if (this.init_Entity == null) {
            this.init_Str = "Waiting for reachable villagers";
         } else if (this.init_I >= this.update_I) {
            this.flush();
         }
      }
   }

   private void init_V_I_4(int count) {
      if (this.integer("budget") > 0 && this.flush_I >= this.integer("budget")) {
         this.init("Emerald budget reached", true);
      } else {
         this.init_CachePool = new autismclient.util.villager.VillagerTradeSession(
            this.init_StreamUtil,
            Math.max(1, count),
            this.integer("delay"),
            this.integer("timeout"),
            this.integer("budget") == 0 ? 0 : this.integer("budget") - this.flush_I,
            this.integer("reserve")
         );
         if (!this.init_CachePool.read_Z()) {
            this.init_CachePool = null;
            this.init_Str = "Waiting for macro trade";
         }
      }
   }

   private void write() {
      this.init_CachePool.init_CachePool_CachePoo_Minecraft(MC);
      this.init_Str = this.init_CachePool.init_Str();
      if (this.init_CachePool.load_Z()) {
         autismclient.util.villager.VillagerTradeSession.Inner_CachePool result = this.init_CachePool.init_CachePool_CachePoo();
         this.sync_I = this.sync_I + this.init_CachePool.init_I();
         this.flush_I = this.flush_I + this.init_CachePool.load_I();
         this.init_CachePool.init_V_Minecraft(MC);
         this.init_CachePool = null;
         if (this.init_Z) {
            this.init(
               result == autismclient.util.villager.VillagerTradeSession.Inner_CachePool.COMPLETE ? "Match purchased and locked" : "Match found; " + this.init_Str,
               result == autismclient.util.villager.VillagerTradeSession.Inner_CachePool.COMPLETE
            );
         } else if (result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.UNCERTAIN
            && result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.REJECTED
            && result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.CURSOR_BUSY
            && result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.INVENTORY_FULL
            && result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.BUDGET
            && result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.CANCELLED
            && result != autismclient.util.villager.VillagerTradeSession.Inner_CachePool.NO_MENU) {
            if (this.integer("trade-limit") > 0 && this.sync_I >= this.integer("trade-limit")) {
               this.init("Trade limit reached", true);
            } else if (this.choice("target").equals("Open screen")) {
               this.init(this.init_Str, this.sync_I > 0);
            } else {
               this.update();
            }
         } else {
            this.init(this.init_Str, false);
         }
      }
   }

   private void update() {
      if (this.init_Entity != null) {
         this.init_Map.put(this.init_Entity.getUUID(), this.bool("restock") ? this.init_I + this.integer("revisit") * 20 : 2147483647);
      }

      this.poll();
      this.init_Entity = null;
      this.init_MethodUtil_DataUti = AutoTradeModule.Inner_DataUtil.SEARCH;
      this.load_I = 0;
      this.update_I = this.init_I + this.integer("delay");
   }

   private void sync() {
      if (!(this.init_Entity instanceof Villager villager && this.init_Z_Entity_3(villager))) {
         this.init("Selected villager is out of reach", false);
      } else if (!villager.isBaby() && !villager.getVillagerData().profession().is(VillagerProfession.NITWIT)) {
         this.load_I++;
         if (this.init_I - this.read_I > this.integer("job-timeout") * 20) {
            this.init("Job change timed out; check workstation access", false);
         } else {
            switch (this.init_MethodUtil_DataUti) {
               case SEARCH:
                  if (villager.getVillagerData().profession().is(VillagerProfession.NONE)) {
                     if (!init(false, false, villager.getVillagerData().level(), villager.getVillagerXp(), false)) {
                        this.init("Villager profession is locked", false);
                        return;
                     }

                     if (MC.level.getBlockState(this.init_BlockPos).isAir()) {
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
                     this.init_MerchantMenu = menu;
                     if (!menu.getOffers().isEmpty()) {
                        this.init(AutoTradeModule.Inner_DataUtil.INSPECT, "Inspecting offers");
                     }
                  } else if (this.load_I >= this.integer("timeout")) {
                     this.init("Villager did not open", false);
                  }
                  break;
               case INSPECT:
                  MerchantMenu menu = this.init_MerchantMenu;
                  boolean match = this.init_Z_Villager(villager) && (this.load_Z || this.init_I_MerchantMenu(menu) >= 0);
                  if (match) {
                     if (this.read_Z) {
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
                  this.read_I = this.init_I;
                  this.init(AutoTradeModule.Inner_DataUtil.BREAK, "Removing selected workstation");
                  break;
               case BREAK:
                  if (this.bool("work-hours") && !init_Z_J(MC.level.getOverworldClockTime())) {
                     this.read_I = this.init_I;
                     this.load_I = 0;
                     this.init_Str = "Waiting for work hours";
                     return;
                  }

                  if (this.load_I(this.init_MethodUtil_StreamU.init_Block) < 0) {
                     this.init_Str = "Need spare " + this.init_MethodUtil_StreamU.init_Block.getName().getString();
                     return;
                  }

                  Block current = MC.level.getBlockState(this.init_BlockPos).getBlock();
                  if (MC.level.getBlockState(this.init_BlockPos).isAir()) {
                     if (this.push >= 0 && !autismclient.util.villager.BlockChangeAckTracker.init_Z(MC.level, this.push)) {
                        return;
                     }

                     MC.gameMode.stopDestroyBlock();
                     this.update_Z = false;
                     this.write_Z = true;
                     this.init(AutoTradeModule.Inner_DataUtil.LOSE_JOB, "Waiting for unemployment");
                     return;
                  }

                  if (current != this.init_Block && current != this.init_MethodUtil_StreamU.init_Block) {
                     this.init("Workstation block changed", false);
                     return;
                  }

                  BlockHitResult hit = this.init_BlockHitResult_BlockPos(this.init_BlockPos);
                  if (hit == null) {
                     this.init_Str = "Move within sight of workstation";
                     return;
                  }

                  if (!this.init_Z_Vec3(hit.getLocation())) {
                     return;
                  }

                  this.close();
                  if (!this.update_Z) {
                     MC.gameMode.startDestroyBlock(this.init_BlockPos, hit.getDirection());
                     this.update_Z = true;
                  } else {
                     MC.gameMode.continueDestroyBlock(this.init_BlockPos, hit.getDirection());
                  }

                  this.push = this.init_I_7();
                  if (MC.level.getBlockState(this.init_BlockPos).isAir()) {
                     this.write_Z = true;
                  }

                  Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
                  break;
               case LOSE_JOB:
                  if (villager.getVillagerData().profession().is(VillagerProfession.NONE)) {
                     this.init(AutoTradeModule.Inner_DataUtil.PLACE, "Placing workstation");
                  } else if (!MC.level.getBlockState(this.init_BlockPos).isAir()) {
                     this.write_Z = false;
                     this.init(AutoTradeModule.Inner_DataUtil.BREAK, "Workstation break rejected");
                  }
                  break;
               case PLACE:
                  if (MC.level.getBlockState(this.init_BlockPos).is(this.init_MethodUtil_StreamU.init_Block)) {
                     this.init(AutoTradeModule.Inner_DataUtil.ACQUIRE_JOB, "Waiting for profession");
                     return;
                  }

                  if (!MC.level.getBlockState(this.init_BlockPos).isAir()) {
                     this.init("Workstation space is occupied", false);
                     return;
                  }

                  if (this.load_Z(this.init_MethodUtil_StreamU.init_Block)) {
                     this.init(AutoTradeModule.Inner_DataUtil.ACQUIRE_JOB, "Waiting for profession");
                  }
                  break;
               case ACQUIRE_JOB:
                  if (!this.init_Z_Villager(villager)
                     || !MC.level.getBlockState(this.init_BlockPos).is(this.init_MethodUtil_StreamU.init_Block)
                     || this.pop >= 0 && !autismclient.util.villager.BlockChangeAckTracker.init_Z(MC.level, this.pop)) {
                     if (this.pop >= 0 && autismclient.util.villager.BlockChangeAckTracker.init_Z(MC.level, this.pop) && MC.level.getBlockState(this.init_BlockPos).isAir()) {
                        this.init(AutoTradeModule.Inner_DataUtil.PLACE, "Retrying workstation placement");
                     } else if (!villager.getVillagerData().profession().is(VillagerProfession.NONE) && !this.init_Z_Villager(villager)) {
                        this.init("Villager claimed another workstation", false);
                     }
                  } else {
                     this.write_Z = false;
                     this.close++;
                     this.read_I = this.init_I;
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

   public static boolean init_Z_J(long dayTime) {
      long time = Math.floorMod(dayTime, 24000L);
      return time >= 2000L && time < 9000L;
   }

   private boolean init_Z_Villager(Villager villager) {
      return villager.getVillagerData()
         .profession()
         .unwrapKey()
         .map(k -> k.identifier().getPath().equals(this.init_MethodUtil_StreamU.name().toLowerCase(Locale.ROOT)))
         .orElse(false);
   }

   private int init_I_MerchantMenu(MerchantMenu menu) {
      for (int i = 0; i < menu.getOffers().size(); i++) {
         if (this.init_StreamUtil.init_Z_MerchantOffer_I(menu.getOffers().get(i), i)) {
            return i;
         }
      }

      return -1;
   }

   private void init(AutoTradeModule.Inner_DataUtil value, String message) {
      this.init_MethodUtil_DataUti = value;
      this.load_I = 0;
      this.init_Str = message;
   }

   private Entity init_Entity() {
      Entity best = null;
      double distance = 1.7976931348623157E308;

      for (Entity entity : MC.level.entitiesForRendering()) {
         if (entity instanceof Merchant
            && (this.bool("wandering") || entity instanceof Villager)
            && this.init_Z_Entity_3(entity)
            && !(entity instanceof Villager v && (v.isBaby() || v.getVillagerData().profession().is(VillagerProfession.NITWIT)))
            && (this.text("villager").isBlank() || entity.getUUID().toString().equals(this.text("villager").strip()))
            && (this.init_UUID == null || entity.getUUID().equals(this.init_UUID))
            && (!this.init_Z || this.init_BlockPos != null && !(entity.position().distanceToSqr(Vec3.atCenterOf(this.init_BlockPos)) > 16.0))
            && this.init_Map.getOrDefault(entity.getUUID(), 0) <= this.init_I) {
            double d = MC.player.distanceToSqr(entity);
            if (d < distance) {
               distance = d;
               best = entity;
            }
         }
      }

      return best;
   }

   private boolean init_Z_Entity_3(Entity entity) {
      return autismclient.util.villager.MerchantInteractHelper.init(MC, entity, this.decimal("range"));
   }

   private void flush() {
      if (this.init_Z_Entity_3(this.init_Entity)) {
         if (MC.player.containerMenu != MC.player.inventoryMenu || MC.gui.screen() != null) {
            this.init_Str = "Close the current screen";
         } else if (this.init_Z_Vec3(this.init_Entity.getBoundingBox().getCenter())) {
            MC.gameMode
               .interact(
                  MC.player, this.init_Entity, new EntityHitResult(this.init_Entity, this.init_Entity.getBoundingBox().getCenter()), InteractionHand.MAIN_HAND
               );
            this.init(AutoTradeModule.Inner_DataUtil.OPEN, "Opening villager");
         }
      }
   }

   private boolean init_Z_Vec3(Vec3 point) {
      return autismclient.util.villager.MerchantInteractHelper.init(MC, point);
   }

   private BlockHitResult init_BlockHitResult_BlockPos(BlockPos pos) {
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
         this.init_Str = "Waiting for workstation in hotbar";
         return false;
      }

      if (this.init_I % 5 != 0) {
         return false;
      }

      for (Direction direction : Direction.values()) {
         BlockPos support = this.init_BlockPos.relative(direction);
         if (!MC.level.getBlockState(support).isAir()) {
            Direction face = direction.getOpposite();
            Vec3 point = Vec3.atCenterOf(support).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5);
            Vec3 rayEnd = point.add(direction.getStepX() * 0.001, direction.getStepY() * 0.001, direction.getStepZ() * 0.001);
            if (!(MC.player.getEyePosition().distanceToSqr(point) > MC.player.blockInteractionRange() * MC.player.blockInteractionRange())) {
               BlockHitResult ray = MC.level
                  .clip(new ClipContext(MC.player.getEyePosition(), rayEnd, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, MC.player));
               if (ray.getType() == Type.BLOCK && ray.getBlockPos().equals(support) && ray.getDirection() == face) {
                  if (!this.init_Z_Vec3(point)) {
                     return false;
                  }

                  this.load(slot);
                  MC.gameMode.useItemOn(MC.player, InteractionHand.MAIN_HAND, new BlockHitResult(point, face, support, false));
                  this.pop = this.init_I_7();
                  Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
                  return true;
               }
            }
         }
      }

      return false;
   }

   private int init_I_Block(Block block) {
      for (int i = 0; i < 9; i++) {
         if (MC.player.getInventory().getItem(i).is(block.asItem())) {
            return i;
         }
      }

      return -1;
   }

   private int init_I_7() {
      return ((AutismClientLevelPredictionAccessor)MC.level).autism$getBlockPredictionHandler().currentSequence();
   }

   private int load_I(Block block) {
      int slot = this.init_I_Block(block);
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
            float candidate = stack.getDestroySpeed(MC.level.getBlockState(this.init_BlockPos));
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
      if (MC.player != null && this.init_MerchantMenu != null && MC.player.containerMenu == this.init_MerchantMenu) {
         MC.player.closeContainer();
      }

      this.init_MerchantMenu = null;
   }

   private void init(String message, boolean success) {
      this.init_Str = message;
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
      if (this.init_CachePool != null) {
         this.init_CachePool.init_V_Minecraft(MC);
         this.init_CachePool = null;
      }

      if (MC.gameMode != null && this.update_Z) {
         MC.gameMode.stopDestroyBlock();
      }

      this.update_Z = false;
      if (this.write_Z && MC.level == this.init_Obj && MC.player != null && this.init_BlockPos != null && !aD.read_Z()) {
         this.poll();
         this.flush_Z = true;
         this.write_I = 0;
         this.init_Str = "Restoring workstation";
      }

      if (!this.flush_Z) {
         this.open();
      }

      if (!this.sync_Z) {
         this.poll();
         if (!this.flush_Z) {
            this.init_Str = "Stopped";
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
      this.init_I++;
      if (MC.player == null || MC.level != this.init_Obj || MC.getConnection() == null || aD.read_Z() || ++this.write_I > this.integer("timeout")) {
         this.flush_Z = false;
         this.open();
         autismclient.util.bi.read("Replace workstation at " + this.init_BlockPos.toShortString());
      } else if (this.push < 0 || autismclient.util.villager.BlockChangeAckTracker.init_Z(MC.level, this.push)) {
         if (!MC.level.getBlockState(this.init_BlockPos).isAir()) {
            if (this.pop < 0 || autismclient.util.villager.BlockChangeAckTracker.init_Z(MC.level, this.pop)) {
               this.flush_Z = false;
               this.write_Z = false;
               this.open();
               this.init_Str = "Stopped";
            }
         } else if (MC.player.containerMenu == MC.player.inventoryMenu && MC.gui.screen() == null) {
            this.load_Z(init_Z_Block_2(this.init_Block) && this.load_I(this.init_Block) >= 0 ? this.init_Block : this.init_MethodUtil_StreamU.init_Block);
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
      autismclient.util.villager.VillagerTradeSession var1 = this.init_CachePool;
      if (var1 != null) {
         var1.init_V_Minecraft(Module.MC);
      }

      this.init_CachePool = null;
      this.init_StreamUtil = null;
      this.init_MethodUtil_StreamU = null;
      this.init_Entity = null;
      this.init_MerchantMenu = null;
      this.init_Obj = null;
      this.init_BlockPos = null;
      this.init_Block = null;
      this.init_UUID = null;
      this.update_Z = false;
      this.write_Z = false;
      this.flush_Z = false;
      this.sync_Z = false;
      this.init_I = 0;
      this.load_I = 0;
      this.read_I = 0;
      this.write_I = 0;
      this.update_I = 0;
      this.sync_I = 0;
      this.flush_I = 0;
      this.close = 0;
      this.open = -1;
      this.poll = -1;
      this.push = -1;
      this.pop = -1;
      this.init_MethodUtil_DataUti = AutoTradeModule.Inner_DataUtil.SEARCH;
      this.init_Str = "Stopped";
      Map var2 = this.init_Map;
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
