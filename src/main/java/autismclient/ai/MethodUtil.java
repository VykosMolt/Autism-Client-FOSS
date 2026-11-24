package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class MethodUtil {
   private static final long init = 5000000000L;

   private MethodUtil() {
   }

   public static AiOperation init(JsonObject args) {
      int index = ag.init_I_JsonObject(args, "index", 0);
      int actionIndex = ag.init_I_JsonObject(args, "actionIndex", 0);
      int size = TokenStore.init_I();
      if (index >= 0 && index < size) {
         TokenStore.Inner_StreamUtil entry = TokenStore.init_TokenStore_StreamU(index);
         if (actionIndex >= 0 && actionIndex < entry.clicks().size()) {
            TokenStore.Inner_DataUtil action = entry.clicks().get(actionIndex);
            AiOperation var10000;
            switch (action.action()) {
               case "RUN_COMMAND":
                  var10000 = init(action.value());
                  break;
               case "SUGGEST_COMMAND":
                  var10000 = R.init_AiOperation(
                     AiOperation.Status.SUCCEEDED,
                     "Returned the suggested command text WITHOUT sending it: suggest means the server offers it for user editing. Edit it if needed, then send it yourself via game:command/chat.",
                     ag.init_JsonObject_ObjArr("suggested", action.value())
                  );
                  break;
               case "OPEN_URL":
                  var10000 = R.init_AiOperation(
                     AiOperation.Status.SUCCEEDED,
                     "Returned the URL WITHOUT opening a browser: autonomous link opening is disabled by design. If you trust this untrusted chat content, fetch it with web_fetch instead.",
                     ag.init_JsonObject_ObjArr("url", action.value())
                  );
                  break;
               case "COPY_TO_CLIPBOARD":
                  var10000 = load(action.value());
                  break;
               default:
                  throw new IllegalArgumentException("Unsupported chat click action '" + action.action() + "'");
            }

            return var10000;
         } else {
            throw new IllegalArgumentException(
               "No click action "
                  + actionIndex
                  + " on chat entry "
                  + index
                  + "; that entry has "
                  + entry.clicks().size()
                  + " click actions (ledger holds "
                  + size
                  + " entries)"
            );
         }
      } else {
         throw new IllegalArgumentException("No chat entry at index " + index + "; the chat ledger holds " + size + " entries (0 = newest)");
      }
   }

   private static AiOperation init(String raw) {
      final String command = raw.startsWith("/") ? raw.substring(1) : raw;
      if (command.isBlank()) {
         throw new IllegalArgumentException("Chat click action carries an empty command");
      } else if (Minecraft.getInstance().getConnection() == null) {
         throw new IllegalArgumentException("Chat click requires a connected server session");
      } else {
         return new AiOperation() {
            final Minecraft init_Minecraft = Minecraft.getInstance();
            final long init_J = TokenStore.init_J();
            final long load_J = System.nanoTime() + 5000000000L;
            final int init_I = this.init_Minecraft.player != null ? this.init_Minecraft.player.containerMenu.containerId : -1;
            final Screen init_Screen = this.init_Minecraft.gui.screen();
            boolean init_Z;
            boolean load_Z;

            @Override
            public AiOperation.Result tick() {
               if (this.load_Z || this.init_Minecraft.getConnection() == null) {
                  return R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "Chat click cancelled", new JsonObject());
               } else if (!this.init_Z) {
                  this.init_Minecraft.getConnection().sendCommand(command);
                  this.init_Z = true;
                  return R.init_AiOperation_Result(
                     AiOperation.Status.RUNNING, "Chat button command sent; observing for a response", ag.init_JsonObject_ObjArr("command", command)
                  );
               } else {
                  boolean menuChanged = this.init_Minecraft.player != null && this.init_Minecraft.player.containerMenu.containerId != this.init_I;
                  boolean screenChanged = this.init_Minecraft.gui.screen() != this.init_Screen;
                  if (TokenStore.init_J() <= this.init_J && !menuChanged && !screenChanged) {
                     return System.nanoTime() > this.load_J
                        ? R.init_AiOperation_Result(
                           AiOperation.Status.SUCCEEDED,
                           "Chat button command sent; no response observed",
                           ag.init_JsonObject_ObjArr("command", command, "observation", "no response observed within 5s")
                        )
                        : R.init_AiOperation_Result(
                           AiOperation.Status.RUNNING, "Waiting for a chat response to the clicked command", ag.init_JsonObject_ObjArr("command", command)
                        );
                  } else {
                     return R.init_AiOperation_Result(
                        AiOperation.Status.SUCCEEDED,
                        "Chat button command observed a response",
                        ag.init_JsonObject_ObjArr(
                           "command", command, "responses", MethodUtil.init(this.init_J), "menuChanged", menuChanged, "screenChanged", screenChanged
                        )
                     );
                  }
               }
            }

            @Override
            public void cancel() {
               this.load_Z = true;
            }
         };
      }
   }

   private static AiOperation load(String value) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.keyboardHandler == null) {
         return R.init_AiOperation(AiOperation.Status.FAILED, "Client clipboard unavailable", new JsonObject());
      }

      mc.keyboardHandler.setClipboard(value);
      return R.init_AiOperation(AiOperation.Status.SUCCEEDED, "Copied chat click text to the client clipboard", ag.init_JsonObject_ObjArr("copied", value));
   }

   private static JsonArray init(long baseline) {
      JsonArray out = new JsonArray();

      for (TokenStore.Inner_StreamUtil entry : TokenStore.init_List(4)) {
         if (entry.revision() > baseline) {
            out.add(ag.init_JsonObject_ObjArr("source", entry.source(), "sender", entry.sender(), "text", entry.text()));
         }
      }

      return out;
   }
}
