package autismclient.modules;

import autismclient.util.AutismConfig;
import autismclient.util.cA;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.TeamColor;

public final class w {
   private final List<RemotePlayer> init_List = new ArrayList<>();
   private final List<PlayerTeam> load_List = new ArrayList<>();
   private final Map<BlockPos, BlockState> init_Map = new HashMap<>();
   private final Map<String, String> load_Map = new HashMap<>();
   private PlayerTeam init_PlayerTeam;
   private boolean init_Z;
   private BlockPos init_BlockPos;
   private int init_I;
   private int load_I;
   private String init_Str;
   private boolean load_Z;
   private boolean read_Z;
   private final Map<String, String> read_Map = new HashMap<>();

   private w() {
   }

   public static w init_w_Minecraft_Z(Minecraft mc, boolean multiple) {
      if (mc.level != null && mc.player != null && mc.gameDirectory.toString().contains("autism-ai-qualification-")) {
         w check = new w();
         z danger = z.init_z();
         check.init_Z = danger.isEnabled();

         for (String id : new String[]{
            "bed-warning", "bed-warning-radius", "bed-scan-range", "bed-include-team", "bed-include-friends", "bed-distances", "bed-warning-scale"
         }) {
            check.load_Map.put(id, danger.value(id));
         }

         AutismConfig.HudElementState hud = autismclient.util.al.init_AutismConfig_HudEl_Str("bed_danger");
         check.init_I = hud.f_x_I;
         check.load_I = hud.f_y_I;
         check.init_Str = hud.anchor;
         check.load_Z = hud.enabled;
         hud.f_x_I = 0;
         hud.f_y_I = 52;
         hud.anchor = "TOP_CENTER";
         hud.enabled = true;
         Module censor = al.init_Module_Str("name-censor");
         check.read_Z = censor.isEnabled();

         for (String key : new String[]{"names", "custom-aliases", "censor-self", "censor-everyone", "censor-everything"}) {
            check.read_Map.put(key, censor.value(key));
         }

         censor.setEnabled(false);
         danger.setEnabled(false);
         danger.setValueTransient("bed-warning", "true");
         danger.setValueTransient("bed-warning-radius", "20");
         danger.setValueTransient("bed-include-team", "false");
         danger.setValueTransient("bed-include-friends", "true");
         danger.setValueTransient("bed-distances", "true");
         danger.setValueTransient("bed-warning-scale", "1");
         Scoreboard board = mc.level.getScoreboard();
         check.init_PlayerTeam = mc.player.getTeam();
         PlayerTeam home = board.addPlayerTeam("qa_bed_home");
         home.setColor(Optional.of(TeamColor.RED));
         check.load_List.add(home);
         board.addPlayerToTeam(mc.player.getGameProfile().name(), home);
         check.init_BlockPos = mc.player.blockPosition().offset(4, 0, 4);

         for (int half = 0; half < 2; half++) {
            BlockPos at = check.init_BlockPos.relative(Direction.SOUTH, half);
            check.init_Map.put(at, mc.level.getBlockState(at));
            Block redBed = BuiltInRegistries.BLOCK.getOptional(Identifier.withDefaultNamespace("red_bed")).orElseThrow();
            mc.level
               .setBlock(
                  at, redBed.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH).setValue(BedBlock.PART, half == 0 ? BedPart.FOOT : BedPart.HEAD), 2
               );
         }

         PlayerTeam blue = board.addPlayerTeam("qa_bed_blue");
         blue.setColor(Optional.of(TeamColor.BLUE));
         check.load_List.add(blue);
         check.init(mc, "BlueRusher", blue, 5);
         check.init(mc, "HomeGuard", home, 2);
         if (multiple) {
            check.init(mc, "BlueSecond", blue, 12);
            PlayerTeam green = board.addPlayerTeam("qa_bed_green");
            green.setColor(Optional.of(TeamColor.GREEN));
            check.load_List.add(green);
            check.init(mc, "GreenRusher", green, 8);
            check.init(mc, "GreenSecond", green, 15);
            check.init(mc, "TooFar", blue, 30);
         }

         af.load();
         danger.setEnabled(true);
         danger.tick();
         return check;
      } else {
         return null;
      }
   }

   public void init_V_Minecraft_Str(Minecraft mc, String variant) {
      if (variant.equals("censored")) {
         Module censor = al.init_Module_Str("name-censor");
         censor.setValueTransient("names", "BlueRusher");
         censor.setValueTransient("custom-aliases", "BlueRusher=Visitor");
         censor.setValueTransient("censor-self", "false");
         censor.setValueTransient("censor-everyone", "false");
         censor.setValueTransient("censor-everything", "false");
         censor.setEnabled(true);
      } else if (variant.equals("moved")) {
         autismclient.util.al.init_V_Str("bed_danger", 30, 120, cA.init_I(), cA.load_I());
      } else if (variant.equals("distance")) {
         z.init_z().setValueTransient("bed-warning-radius", "4");
      } else if (variant.equals("editor")) {
         z.init_z().setEnabled(false);
         mc.gui.setScreen(new autismclient.gui.d.RenderState(null));
      }

      z.init_z().tick();
   }

   private void init(Minecraft mc, String name, PlayerTeam team, int distance) {
      RemotePlayer player = new RemotePlayer(mc.level, new GameProfile(UUID.randomUUID(), name));
      player.setId(-17400 - this.init_List.size());
      player.setPos(this.init_BlockPos.getX() + distance + 0.5, this.init_BlockPos.getY(), this.init_BlockPos.getZ() + 1);
      player.xOld = player.getX();
      player.yOld = player.getY();
      player.zOld = player.getZ();
      mc.level.getScoreboard().addPlayerToTeam(name, team);
      mc.level.addEntity(player);
      this.init_List.add(player);
   }

   public JsonObject init() {
      JsonObject result = new JsonObject();
      List<v.Inner_DataUtil> groups = z.init_z().init_List_19();
      JsonArray lines = new JsonArray();

      for (v.Inner_DataUtil group : groups) {
         JsonObject row = new JsonObject();
         row.addProperty("text", group.init(true).getString());
         row.addProperty("color", Integer.toHexString(group.init_I()));
         row.addProperty("distance", group.init_D());
         row.addProperty("count", group.load());
         lines.add(row);
      }

      result.add("teams", lines);
      result.addProperty("groups", groups.size());
      result.addProperty("censored", as.init_Z_33());
      autismclient.util.al.Inner_BufferHelper bounds = autismclient.util.al.init_al_BufferHelper_Str_Font("bed_danger", Minecraft.getInstance().font);
      result.addProperty("hudX", bounds.read());
      result.addProperty("hudY", bounds.write());
      result.addProperty("warningDistance", z.init_z().decimal("bed-warning-radius"));
      return result;
   }

   public void init(Minecraft mc) {
      AutismConfig.HudElementState hud = autismclient.util.al.init_AutismConfig_HudEl_Str("bed_danger");
      hud.f_x_I = this.init_I;
      hud.f_y_I = this.load_I;
      hud.anchor = this.init_Str;
      hud.enabled = this.load_Z;
      Module censor = al.init_Module_Str("name-censor");
      censor.setEnabled(false);
      this.read_Map.forEach(censor::setValueTransient);
      censor.setEnabled(this.read_Z);
      z.init_z().setEnabled(false);

      for (RemotePlayer player : this.init_List) {
         mc.level.getScoreboard().removePlayerFromTeam(player.getGameProfile().name(), player.getTeam());
         player.discard();
      }

      mc.level.getScoreboard().removePlayerFromTeam(mc.player.getGameProfile().name(), mc.player.getTeam());
      if (this.init_PlayerTeam != null) {
         mc.level.getScoreboard().addPlayerToTeam(mc.player.getGameProfile().name(), this.init_PlayerTeam);
      }

      for (PlayerTeam team : this.load_List) {
         mc.level.getScoreboard().removePlayerTeam(team);
      }

      for (Entry<BlockPos, BlockState> entry : this.init_Map.entrySet()) {
         mc.level.setBlock(entry.getKey(), entry.getValue(), 2);
      }

      this.load_Map.forEach(z.init_z()::setValueTransient);
      z.init_z().setEnabled(this.init_Z);
      af.load();
   }
}
