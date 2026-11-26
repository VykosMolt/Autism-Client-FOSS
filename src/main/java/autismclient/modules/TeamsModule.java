package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.KeybindSetting;
import autismclient.api.module.StringListSetting;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismLiteVariant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.phys.EntityHitResult;

public final class TeamsModule extends Module {
   public static final int init_I = -866779137;
   static final int load_I = 100;
   private static TeamsModule init_aL;
   private String init_Str;
   private Set<String> init_Set = Set.of();
   private boolean init_Z;
   private final Set<UUID> read = new HashSet<>();
   private Object init_Obj;

   public TeamsModule() {
      super("teams", "Teams", ae.write, "Friends and team detection.");
      init_aL = this;
      this.add(new StringListSetting("friends", "Friends", "").playerNameList().build());
      this.add(new BoolSetting("auto-teams", "Auto Teams", false).description("Detect teammates automatically.").build());
      this.add(new BoolSetting("quick-add", "Quick Add", true).description("Add/remove with key.").build());
      this.add(new KeybindSetting("quick-add-bind", "Quick Add Key", autismclient.util.ValueSink.init_I(2)).visibleWhen(() -> this.bool("quick-add")).build());
      this.add(new BoolSetting("esp", "ESP", true).build());
      this.add(new BoolSetting("tracers", "Tracers", true).build());
      this.add(new BoolSetting("nametags", "Nametags", true).build());
      this.add(
         new ColorSetting("friends-color", "Friends Color", -866779137)
            .visibleWhen(() -> this.bool("esp") || this.bool("tracers") || this.bool("nametags"))
            .build()
      );
      this.add(new BoolSetting("aimassist", "AimAssist", false).build());
      this.add(new BoolSetting("killaura", "KillAura", false).build());
      this.add(new BoolSetting("triggerbot", "TriggerBot", false).build());
   }

   @Override
   public void onOptionValueChanged(String settingId) {
      if ("friends".equals(settingId)) {
         this.init_Str = null;
      }
   }

   @Override
   public void tick() {
      if (!AutismLiteVariant.enabled() && MC != null && MC.player != null && MC.level != null) {
         if (this.bool("auto-teams")) {
            for (Player player : MC.level.players()) {
               if (player != MC.player) {
                  this.read_Z_Player(player);
               }
            }
         }

         int bind = this.integer("quick-add-bind");
         boolean down = this.bool("quick-add") && bind != -1 && MC.gui.screen() == null && autismclient.util.ValueSink.init(MC, bind);
         if (down && !this.init_Z && MC.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Player player && player != MC.player) {
            this.init_V_Str_4(player.getScoreboardName());
         }

         this.init_Z = down;
      }
   }

   private void init_V_Str_4(String name) {
      if (name != null && !name.isBlank()) {
         List<String> friends = new ArrayList<>(this.list("friends"));
         String existing = null;

         for (String friend : friends) {
            if (friend.equalsIgnoreCase(name)) {
               existing = friend;
               break;
            }
         }

         if (existing != null) {
            friends.remove(existing);
            AutismClientMessaging.sendPrefixed("Removed friend " + existing + ".");
         } else {
            friends.add(name);
            AutismClientMessaging.sendPrefixed("Added friend " + name + ".");
         }

         this.setValue("friends", String.join("|", friends));
      }
   }

   public static boolean init_Z_Str_2(String name) {
      TeamsModule module = init_aL;
      if (module != null && name != null && !name.isBlank()) {
         List<String> friends = new ArrayList<>(module.list("friends"));

         for (String friend : friends) {
            if (friend.equalsIgnoreCase(name)) {
               AutismClientMessaging.sendPrefixed("§e" + friend + " §7is already a friend.");
               return false;
            }
         }

         friends.add(name);
         module.setValue("friends", String.join("|", friends));
         AutismClientMessaging.sendPrefixed("§aAdded friend §f" + name + "§a.");
         return true;
      } else {
         AutismClientMessaging.sendPrefixed("§cTeams module unavailable.");
         return false;
      }
   }

   public static boolean load_Z_Str(String name) {
      TeamsModule module = init_aL;
      if (module != null && name != null && !name.isBlank()) {
         List<String> friends = new ArrayList<>(module.list("friends"));

         for (String friend : friends) {
            if (friend.equalsIgnoreCase(name)) {
               friends.remove(friend);
               module.setValue("friends", String.join("|", friends));
               AutismClientMessaging.sendPrefixed("§aRemoved friend §f" + friend + "§a.");
               return true;
            }
         }

         AutismClientMessaging.sendPrefixed("§cNo friend named §f" + name + "§c.");
         return false;
      } else {
         AutismClientMessaging.sendPrefixed("§cTeams module unavailable.");
         return false;
      }
   }

   public static boolean init_Z_28() {
      TeamsModule module = init_aL;
      if (module == null) {
         AutismClientMessaging.sendPrefixed("§cTeams module unavailable.");
         return false;
      } else {
         int count = module.list("friends").size();
         if (count == 0) {
            AutismClientMessaging.sendPrefixed("§7Friend list is already empty.");
            return false;
         } else {
            module.setValue("friends", "");
            AutismClientMessaging.sendPrefixed("§aCleared §f" + count + " §afriend" + (count == 1 ? "" : "s") + ".");
            return true;
         }
      }
   }

   public static List<String> init_List_11() {
      TeamsModule module = init_aL;
      return module == null ? List.of() : module.list("friends");
   }

   private static TeamsModule init_aL() {
      TeamsModule module = init_aL;
      return module != null && module.isEnabled() ? module : null;
   }

   public static boolean init_Z_Entity_5(Entity entity) {
      TeamsModule module = init_aL();
      return module != null && entity instanceof Player player && entity != MC.player
         ? module.init_Z_Player_2(player)
            || module.bool("auto-teams") && module.read_Z_Player(player)
         : false;
   }

   public static boolean init(Entity entity, String option) {
      TeamsModule module = init_aL();
      if (module == null) {
         return false;
      } else {
         return module.bool(option) ? false : init_Z_Entity_5(entity);
      }
   }

   public static boolean read_Z_Str(String option) {
      TeamsModule module = init_aL();
      return module != null && module.bool(option);
   }

   public static int init_I_18() {
      TeamsModule module = init_aL();
      return module == null ? -866779137 : am.init_I_Module(module, "friends-color", -866779137);
   }

   private boolean init_Z_Player_2(Player player) {
      String name = player.getScoreboardName();
      return name != null && !name.isBlank() ? this.init_Set_8().contains(name.toLowerCase(Locale.ROOT)) : false;
   }

   private Set<String> init_Set_8() {
      List<String> entries = this.list("friends");
      String source = String.join("|", entries);
      if (source.equals(this.init_Str)) {
         return this.init_Set;
      }

      Set<String> names = new LinkedHashSet<>();

      for (String entry : entries) {
         if (entry != null) {
            String name = entry.trim().toLowerCase(Locale.ROOT);
            if (!name.isEmpty()) {
               names.add(name);
            }
         }
      }

      this.init_Str = source;
      this.init_Set = Set.copyOf(names);
      return this.init_Set;
   }

   private boolean read_Z_Player(Player suspected) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      LocalPlayer self = MC.player;
      if (self == null) {
         return false;
      }

      if (this.init_Obj != MC.level) {
         this.read.clear();
         this.init_Obj = MC.level;
      }

      UUID id = suspected.getUUID();
      TeamsModule.Inner_DataUtil verdict = init(self, suspected);
      if (verdict == TeamsModule.Inner_DataUtil.TEAMMATE) {
         this.read.add(id);
         return true;
      } else if (!this.read.contains(id)) {
         return false;
      } else if (verdict == TeamsModule.Inner_DataUtil.OPPONENT && !write(self) && !write(suspected)) {
         this.read.remove(id);
         return false;
      } else {
         return true;
      }
   }

   static TeamsModule.Inner_DataUtil init(boolean allied, Object ownColor, Object theirColor, Integer ownHelmet, Integer theirHelmet) {
      if (allied) {
         return TeamsModule.Inner_DataUtil.TEAMMATE;
      } else if (ownColor != null && ownColor.equals(theirColor)) {
         return TeamsModule.Inner_DataUtil.TEAMMATE;
      } else if (ownHelmet != null && ownHelmet.equals(theirHelmet)) {
         return TeamsModule.Inner_DataUtil.TEAMMATE;
      } else if (ownColor != null && theirColor != null) {
         return TeamsModule.Inner_DataUtil.OPPONENT;
      } else {
         return ownHelmet != null && theirHelmet != null ? TeamsModule.Inner_DataUtil.OPPONENT : TeamsModule.Inner_DataUtil.UNKNOWN;
      }
   }

   private static TeamsModule.Inner_DataUtil init(LocalPlayer self, Player suspected) {
      Component ownName = self.getDisplayName();
      Component theirName = suspected.getDisplayName();
      Integer ownColor = init(self, ownName);
      Integer theirColor = init(suspected, theirName);
      return init(self.isAlliedTo(suspected), ownColor, theirColor, init_Integer_Player(self), init_Integer_Player(suspected));
   }

   private static Integer init(Player player, Component display) {
      String name = player.getGameProfile().name();
      PlayerInfo info = MC.getConnection() == null ? null : MC.getConnection().getPlayerInfo(player.getUUID());
      Integer color = init(name, info == null ? null : info.getTabListDisplayName());
      return color != null ? color : init(name, display);
   }

   static Integer init(String name, Component display) {
      if (AutismLiteVariant.enabled()) {
         return null;
      }

      Integer[] rgb = new Integer[]{null};
      boolean[] plain = new boolean[]{false};
      boolean[] conflict = new boolean[]{false};
      PlayerDisplayNameParser.init_MutableComponent(name, display, null).visit((style, text) -> {
         if (!text.isBlank()) {
            TextColor color = style.getColor();
            if (color == null) {
               plain[0] = true;
            } else if (rgb[0] != null && rgb[0] != color.getValue()) {
               conflict[0] = true;
            } else {
               rgb[0] = color.getValue();
            }
         }

         return Optional.empty();
      }, Style.EMPTY);
      return !plain[0] && !conflict[0] ? rgb[0] : null;
   }

   private static boolean write(Player player) {
      return player.tickCount < 100 || player.isSpectator();
   }

   private static Integer init_Integer_Player(Player player) {
      ItemStack stack = player.getItemBySlot(EquipmentSlot.HEAD);
      if (stack != null && !stack.isEmpty()) {
         DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
         return dyed == null ? null : dyed.rgb() & 16777215;
      } else {
         return null;
      }
   }

   enum Inner_DataUtil {
      TEAMMATE,
      OPPONENT,
      UNKNOWN;
   }
}
