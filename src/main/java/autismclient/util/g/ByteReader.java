package autismclient.util.g;

import autismclient.util.aM;
import autismclient.util.macro.MacroAction;
import autismclient.util.macro.a;
import autismclient.util.macro.ap;
import autismclient.util.macro.as;
import autismclient.util.macro.at;
import autismclient.util.macro.aw;
import autismclient.util.macro.u;
import autismclient.util.macro.v;
import autismclient.util.macro.y;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ByteReader {
   private ByteReader() {
   }

   static ByteReader.Inner_StreamUtil init(autismclient.util.macro.StreamGate t) {
      ByteReader.Inner_StreamUtil var10000;
      switch (t) {
         case init:
         case f_h_StreamGate:
         case f_A_StreamGate:
         case put:
         case pop:
         case valid:
         case tick:
         case clear:
         case read:
         case reset:
         case buffer:
         case node:
         case task:
         case f_T_StreamGate:
         case f_U_StreamGate:
         case f_V_StreamGate:
         case f_I_StreamGate:
         case frame:
         case f_g_StreamGate:
         case f_y_StreamGate:
         case f_Q_StreamGate:
         case f_J_StreamGate:
         case f_q_StreamGate:
         case f_K_StreamGate:
         case add:
         case scan:
         case fill:
         case mark:
         case open:
         case f_X_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.init, "");
            break;
         case poll:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.read, "uses the no-crosshair behavior: left swings and right uses the held item"
            );
            break;
         case f_z_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.read,
               "supports use/start/release/swing, repeat and hold timing; item auto-select, packet gates and slot choreography are not reproduced"
            );
            break;
         case stop:
         case prev:
         case next:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.init, "");
            break;
         case f_b_StreamGate:
         case size:
         case start:
         case f_x_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.init, "");
            break;
         case close:
         case f_H_StreamGate:
         case update:
         case f_F_StreamGate:
         case f_D_StreamGate:
         case f_E_StreamGate:
         case state:
         case sync:
         case table:
         case f_m_StreamGate:
         case f_n_StreamGate:
         case f_l_StreamGate:
         case f_p_StreamGate:
         case block:
         case f_o_StreamGate:
         case write:
         case f_c_StreamGate:
         case f_C_StreamGate:
         case data:
         case f_k_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.init, "");
            break;
         case f_L_StreamGate:
         case f_w_StreamGate:
         case f_i_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.init, "");
            break;
         case push:
         case pool:
         case count:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.init, "");
            break;
         case flags:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.read, "walks in a straight line via position packets (no collision or pathfinding)"
            );
            break;
         case index:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.read, "jumps via a streamed position arc");
            break;
         case f_s_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.read, "manual relative vertical packet clips are supported; Top/Bottom world-scan modes are not"
            );
            break;
         case f_t_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.read, "manual relative horizontal packet clips are supported; Forward/Back route-scan modes are not"
            );
            break;
         case f_u_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.read, "ground-first HClip teleport is supported with correction rebasing");
            break;
         case f_f_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.read, "digs a block by coordinates (instant/creative only - no survival dig timing)"
            );
            break;
         case f_P_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.read, "places on a block by coordinates (uses the held item; doesn't auto-select)"
            );
            break;
         case f_e_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.read, "breaks the block at its configured coordinates (instant); no block-hardness timing"
            );
            break;
         case store:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.read, "opens a block container by coordinates (Last-target mode needs a crosshair no bot has)"
            );
            break;
         case level:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.read, "aims at specific coordinates ('find nearest block of type' needs world data no bot has)"
            );
            break;
         case drain:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.load, "interacts with the nearest tracked entity of the given type (targets by type, not crosshair)"
            );
            break;
         case chunk:
         case f_G_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.load,
               "waits for a tracked entity of the given type within range (radius/reach modes; crosshair/mount modes can't be checked headlessly)"
            );
            break;
         case f_j_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "LAN step coordination is not available per bot");
            break;
         case flush:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.read,
               "AT_POSITION works via tracked block-change packets (a bot loads no chunks, so it only sees blocks it was told changed); IN_REACH / LOOKING_AT need world/crosshair"
            );
            break;
         case f_d_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.write, "scans the world for blocks of a type to break -no chunk data headless (use INSTA_BREAK/BREAK by coordinates)"
            );
            break;
         case value:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "native pathfinding requires a real client world");
            break;
         case cache:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "Tools controls the main client; run this step outside Multi");
            break;
         case set:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "requires the local merchant screen");
            break;
         case get:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.write, "needs the client recipe book to resolve + auto-fill recipes (no recipe manager on a headless bot)"
            );
            break;
         case f_a_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.write, "container dupe: needs client GUI-timing / world-raycast reopen a headless bot can't reproduce"
            );
            break;
         case queue:
            var10000 = new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.read, "sends raw-byte custom payloads; the structured / JSON / Java-script payload builders aren't run headlessly"
            );
            break;
         case load:
         case run:
         case bind:
         case f_v_StreamGate:
         case f_B_StreamGate:
         case parse:
         case f_O_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "raw/queued packet tooling -not wired into the bot executor");
            break;
         case entry:
         case f_M_StreamGate:
         case f_R_StreamGate:
         case f_S_StreamGate:
         case f_N_StreamGate:
         case f_r_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "client-only / advanced feature not available to a headless bot");
            break;
         case f_W_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "orchestrates other bots from the main client");
            break;
         case f_Y_StreamGate:
         case f_Z_StreamGate:
            var10000 = new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "needs the local client world and player hands");
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static ByteReader.Inner_StreamUtil init(MacroAction action, ByteReader.Inner_StreamUtil base) {
      if (action instanceof aw w && (w.checkMode == aw.Inner_DataUtil.load || w.checkMode == aw.Inner_DataUtil.write)) {
         return new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "waits on the crosshair/mount state, which no bot can observe");
      } else if (action instanceof as w && (w.condition == as.Inner_DataUtil.init || w.condition == as.Inner_DataUtil.read)) {
         return new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "waits on the crosshair/mount state, which no bot can observe");
      } else if (action instanceof at w && w.checkMode != at.Inner_DataUtil.init) {
         return new ByteReader.Inner_StreamUtil(
            ByteReader.Inner_DataUtil.write, "IN_REACH / LOOKING_AT block waits need a world/crosshair no bot has; only AT_POSITION works"
         );
      } else if (action instanceof ap v && v.mode != ap.Inner_StreamUtil.init) {
         return new ByteReader.Inner_StreamUtil(
            ByteReader.Inner_DataUtil.write, "Top/Bottom clip modes scan client world collision; only Manual works on Multi bots"
         );
      } else if (action instanceof autismclient.util.macro.TokenStore h && h.mode != autismclient.util.macro.TokenStore.Inner_CachePool.init) {
         return new ByteReader.Inner_StreamUtil(
            ByteReader.Inner_DataUtil.write, "Forward/Back clip modes scan and route through client world collision; only Manual works on Multi bots"
         );
      } else if (action instanceof autismclient.util.macro.ValueSink look) {
         if (look.targetMode == autismclient.util.macro.ValueSink.Inner_StreamUtil.load) {
            return new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "nearest-block lookup needs chunk data; use Specific coordinates");
         } else {
            return look.targetMode == autismclient.util.macro.ValueSink.Inner_StreamUtil.read
               ? new ByteReader.Inner_StreamUtil(
                  ByteReader.Inner_DataUtil.load, "aims at the nearest tracked entity of the given type; Specific coordinates also work"
               )
               : new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.init, "");
         }
      } else if (action instanceof autismclient.util.macro.PathHelper interact && interact.targetMode == autismclient.util.macro.PathHelper.Inner_DataUtil.load
         )
       {
         return new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "the rendered client's last crosshair target is not shared with Multi bots");
      } else if (!(action instanceof v open)) {
         if (action instanceof y burst) {
            if (burst.mode == y.Inner_DataUtil.read) {
               return new ByteReader.Inner_StreamUtil(
                  ByteReader.Inner_DataUtil.write, "CLIENT_COMMAND needs a real client-player entity; the other burst modes work per bot"
               );
            }

            if (burst.mode == y.Inner_DataUtil.load && burst.entityId < 0) {
               return new ByteReader.Inner_StreamUtil(
                  ByteReader.Inner_DataUtil.write, "ENTITY_INTERACT needs an explicit entity id because a bot has no crosshair target"
               );
            }

            if (burst.flushBefore || burst.count > 64) {
               return new ByteReader.Inner_StreamUtil(
                  ByteReader.Inner_DataUtil.read, "runs per bot with a 64-packet safety cap; flush-before has no per-bot delayed queue to flush"
               );
            }
         }

         if (action instanceof u book) {
            if ("File".equalsIgnoreCase(book.dataSource)) {
               return new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.write, "File-source books remain client-only; Random and Pasted sources work");
            }

            if (book.dropInventoryBefore || book.bookCount > 9 || "Pasted".equalsIgnoreCase(book.dataSource) && book.wordWrap) {
               return new ByteReader.Inner_StreamUtil(
                  ByteReader.Inner_DataUtil.read,
                  "supports Random/Pasted books (up to 9 per action); pixel-font wrapping and drop-inventory-before aren't reproduced"
               );
            }
         }

         if (action instanceof autismclient.util.macro.TaskQueue capture) {
            if (capture.source == autismclient.util.macro.TaskQueue.Inner_StateCache.close) {
               return capture.autofillCacheList
                  ? new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.init, "")
                  : new ByteReader.Inner_StreamUtil(
                     ByteReader.Inner_DataUtil.read, "uses the bot's live server suggestion reply; enable Cache List to sweep past the server's suggestion cap"
                  );
            }

            if (capture.source == autismclient.util.macro.TaskQueue.Inner_StateCache.open) {
               return new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.read, "captures live player names; team-prefix metadata isn't tracked per bot");
            }
         }

         return base;
      } else if (open.targetMode == v.Inner_DataUtil.read) {
         return new ByteReader.Inner_StreamUtil(
            ByteReader.Inner_DataUtil.write, "the rendered client's last container target is not shared with Multi bots; use Block coordinates"
         );
      } else {
         return open.targetMode == v.Inner_DataUtil.load
            ? new ByteReader.Inner_StreamUtil(
               ByteReader.Inner_DataUtil.load, "opens the nearest tracked entity's container of the given type; Block coordinates also work"
            )
            : new ByteReader.Inner_StreamUtil(ByteReader.Inner_DataUtil.init, "");
      }
   }

   private static String init(a.Inner_CachePool kind) {
      String var10000;
      switch (kind) {
         case reset:
            var10000 = "no crosshair/world to raycast a block";
            break;
         case add:
            var10000 = "mount state isn't tracked headlessly";
            break;
         case valid:
            var10000 = "bundle-v2 readiness needs the client inventory GUI";
            break;
         case stop:
            var10000 = "villager held items need the local client world";
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   static List<String> init(aM macro) {
      Map<String, String> warnings = new LinkedHashMap<>();

      for (MacroAction action : macro.init_List) {
         if (action != null && action.isEnabled()) {
            autismclient.util.macro.StreamGate type = action.getType();
            if (type != null) {
               ByteReader.Inner_StreamUtil note = init(action, init(type));
               String label = type.name();
               switch (note.init_ByteReader_DataUti()) {
                  case init:
                  case load:
                  default:
                     break;
                  case read:
                     warnings.put("A:" + label, "- " + label + ": " + note.init_Str() + ".");
                     break;
                  case write:
                     warnings.put("A:" + label, "- " + label + ": " + note.init_Str() + "; SKIPPED.");
               }
            }

            a condition = init(action);
            if (condition != null) {
               init(condition, warnings);
            }
         }
      }

      return new ArrayList<>(warnings.values());
   }

   private static a init(MacroAction action) {
      if (action instanceof autismclient.util.macro.FieldCache a) {
         return a.condition;
      } else if (action instanceof autismclient.util.macro.TableIndex a) {
         return a.conditional ? a.condition : null;
      } else {
         return null;
      }
   }

   private static void init(a node, Map<String, String> out) {
      if (node != null) {
         if (node.init_a_BufferHelper == a.Inner_BufferHelper.load) {
            String reason = init(node.init_a_CachePool);
            if (reason != null) {
               out.put("C:" + node.init_a_CachePool.name(), "- condition " + node.init_a_CachePool.name() + ": " + reason + "; treated as false.");
            }
         } else {
            for (a child : node.init_List) {
               init(child, out);
            }
         }
      }
   }

   enum Inner_DataUtil {
      init,
      load,
      read,
      write;
   }

   record Inner_StreamUtil(ByteReader.Inner_DataUtil init_ByteReader_DataUti, String init_Str) {
   }
}
