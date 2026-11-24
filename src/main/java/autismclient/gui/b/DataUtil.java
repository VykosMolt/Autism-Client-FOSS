package autismclient.gui.b;

import autismclient.commands.AutismCommandSource;
import autismclient.modules.al;
import autismclient.util.aN;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;

public final class DataUtil {
   private static DataUtil.Inner_DataUtil init;

   private DataUtil() {
   }

   public static boolean init_Z(String value) {
      return autismclient.commands.DataUtil.init_Z(value);
   }

   public static DataUtil.Inner_StreamUtil init_DataUtil_StreamUti(String value) {
      return init(value, value == null ? 0 : value.length());
   }

   public static DataUtil.Inner_StreamUtil init(String value, int requestedCursor) {
      try {
         int prefixLen = autismclient.commands.DataUtil.init_Str().length();
         if (value != null && value.length() >= prefixLen) {
            int cursor = Math.max(prefixLen, Math.min(requestedCursor, value.length()));
            int commandRevision = autismclient.commands.DataUtil.init_I();
            int moduleRevision = al.init_I();
            long macroRevision = aN.init_aN().init_J();
            Minecraft minecraft = Minecraft.getInstance();
            AbstractContainerMenu menu = minecraft.player == null ? null : minecraft.player.containerMenu;
            int menuRevision = menu == null ? -1 : menu.getStateId();
            DataUtil.Inner_DataUtil hit = init;
            if (hit == null || !hit.init(value, cursor, commandRevision, moduleRevision, macroRevision, menu, menuRevision)) {
               StringReader reader = new StringReader(value);
               reader.setCursor(prefixLen);
               ParseResults<AutismCommandSource> parse = autismclient.commands.DataUtil.init_CommandDispatcher().parse(reader, AutismCommandSource.INSTANCE);
               CompletableFuture<Suggestions> future = autismclient.commands.DataUtil.init_CommandDispatcher().getCompletionSuggestions(parse, cursor);
               Suggestions built = future.getNow(null);
               DataUtil.Inner_StreamUtil result = built == null ? null : init(built);
               init = new DataUtil.Inner_DataUtil(value, cursor, commandRevision, moduleRevision, macroRevision, menu, menuRevision, future, result);
               return result;
            }

            if (hit.init_DataUtil_StreamUti() != null) {
               return hit.init_DataUtil_StreamUti();
            }

            Suggestions completed = hit.init_CompletableFuture().getNow(null);
            if (completed == null) {
               return null;
            }

            DataUtil.Inner_StreamUtil result = init(completed);
            init = new DataUtil.Inner_DataUtil(
               value, cursor, commandRevision, moduleRevision, macroRevision, menu, menuRevision, hit.init_CompletableFuture(), result
            );
            return result;
         } else {
            return new DataUtil.Inner_StreamUtil(0, 0, List.of());
         }
      } catch (RuntimeException error) {
         return new DataUtil.Inner_StreamUtil(0, 0, List.of());
      }
   }

   private static DataUtil.Inner_StreamUtil init(Suggestions built) {
      List<String> entries = new ArrayList<>(built.getList().size());

      for (Suggestion suggestion : built.getList()) {
         entries.add(suggestion.getText());
      }

      return new DataUtil.Inner_StreamUtil(built.getRange().getStart(), built.getRange().getLength(), List.copyOf(entries));
   }

   private record Inner_DataUtil(
      String init_Str,
      int init_I,
      int load,
      int read,
      long init_J,
      AbstractContainerMenu init_AbstractContainerM,
      int write,
      CompletableFuture<Suggestions> init_CompletableFuture,
      DataUtil.Inner_StreamUtil init_DataUtil_StreamUti
   ) {
      private boolean init(
         String nextValue,
         int nextCursor,
         int nextCommandRevision,
         int nextModuleRevision,
         long nextMacroRevision,
         AbstractContainerMenu nextMenu,
         int nextMenuRevision
      ) {
         return this.init_Str.equals(nextValue)
            && this.init_I == nextCursor
            && this.load == nextCommandRevision
            && this.read == nextModuleRevision
            && this.init_J == nextMacroRevision
            && this.init_AbstractContainerM == nextMenu
            && this.write == nextMenuRevision;
      }
   }

   public record Inner_StreamUtil(int init_I, int load, List<String> init_List) {
   }
}
