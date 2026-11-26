package autismclient.mixin;

import autismclient.AutismClientAddon;
import autismclient.commands.AutismCommandSource;
import autismclient.commands.CommandManager;
import autismclient.mixin.accessor.AutismCommandSuggestionsListAccessor;
import autismclient.modules.al;
import autismclient.util.MacroLibraryManager;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.CommandSuggestions.SuggestionsList;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
public abstract class AutismChatSuggestMixin {
   @Shadow
   @Final
   private EditBox input;
   @Shadow
   @Nullable
   private ParseResults<ClientSuggestionProvider> currentParse;
   @Shadow
   @Nullable
   private CompletableFuture<Suggestions> pendingSuggestions;
   @Shadow
   @Nullable
   private SuggestionsList/* @Nullable */ suggestions;
   @Shadow
   @Final
   private List<FormattedCharSequence> commandUsage;
   @Shadow
   private boolean currentParseIsCommand;
   @Shadow
   private boolean currentParseIsMessage;
   @Shadow
   private boolean keepSuggestions;
   @Unique
   private boolean autism$owningSuggestions;
   @Unique
   private String autism$cachedInput;
   @Unique
   private int autism$cachedCursor = -1;
   @Unique
   private int autism$cachedCommandRevision = -2147483648;
   @Unique
   private int autism$cachedModuleRevision = -2147483648;
   @Unique
   private long autism$cachedMacroRevision = -9223372036854775808L;
   @Unique
   private AbstractContainerMenu autism$cachedMenu;
   @Unique
   private int autism$cachedMenuRevision = -2147483648;
   @Unique
   private static final int AUTISM_MAX_SUGGESTION_INPUT = 4096;

   @Shadow
   public abstract void showSuggestions(boolean var1);

   @Shadow
   private void updateUsageInfo(ParseResults<ClientSuggestionProvider> parseResults, Suggestions suggestions) {
   }

   @Inject(method = "updateCommandInfo", at = @At("HEAD"), cancellable = true)
   private void autism$updateAutismCommandInfo(CallbackInfo ci) {
      try {
         this.autism$updateAutismCommandInfoSafe(ci);
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Commands] suggestion update failed", t);

         try {
            this.autism$clearActiveSuggestions();
         } catch (Throwable var4) {
         }

         ci.cancel();
      }
   }

   @Unique
   private void autism$updateAutismCommandInfoSafe(CallbackInfo ci) {
      String value = this.input.getValue();
      if (!autism$isAutismCommandInput(value)) {
         if (this.autism$owningSuggestions) {
            this.autism$clearActiveSuggestions();
         }
      } else if (value.length() > 4096) {
         if (this.autism$owningSuggestions) {
            this.autism$clearActiveSuggestions();
         }

         ci.cancel();
      } else {
         int prefixLen = CommandManager.init_Str().length();
         StringReader reader = new StringReader(value);
         reader.setCursor(prefixLen);
         int cursor = Math.max(prefixLen, Math.min(this.input.getCursorPosition(), value.length()));
         int commandRevision = CommandManager.init_I();
         int moduleRevision = al.init_I();
         long macroRevision = MacroLibraryManager.init_aN().init_J();
         Minecraft minecraft = Minecraft.getInstance();
         AbstractContainerMenu menu = minecraft.player == null ? null : minecraft.player.containerMenu;
         int menuRevision = menu == null ? -1 : menu.getStateId();
         if (!value.equals(this.autism$cachedInput)
            || cursor != this.autism$cachedCursor
            || commandRevision != this.autism$cachedCommandRevision
            || moduleRevision != this.autism$cachedModuleRevision
            || macroRevision != this.autism$cachedMacroRevision
            || menu != this.autism$cachedMenu
            || menuRevision != this.autism$cachedMenuRevision
            || !this.autism$owningSuggestions && this.pendingSuggestions == null) {
            ParseResults<AutismCommandSource> parse = CommandManager.init_CommandDispatcher().parse(reader, AutismCommandSource.INSTANCE);
            @SuppressWarnings("unchecked")
            ParseResults<ClientSuggestionProvider> widgetParse = (ParseResults)parse;
            this.currentParse = widgetParse;
            this.currentParseIsCommand = true;
            this.currentParseIsMessage = false;
            this.commandUsage.clear();
            if (this.suggestions != null && this.keepSuggestions && this.autism$canKeepSuggestionList(value)) {
               this.autism$rememberSuggestionKey(value, cursor, commandRevision, moduleRevision, macroRevision, menu, menuRevision);
               ci.cancel();
            } else {
               this.autism$clearActiveSuggestions();
               this.autism$rememberSuggestionKey(value, cursor, commandRevision, moduleRevision, macroRevision, menu, menuRevision);
               String inputSnapshot = value;
               CompletableFuture<Suggestions> future = CommandManager.init_CommandDispatcher().getCompletionSuggestions(parse, cursor);
               this.pendingSuggestions = future;
               future.thenAccept(result -> {
                  Runnable apply = () -> {
                     try {
                        if (this.pendingSuggestions != future) {
                           return;
                        }

                        if (!inputSnapshot.equals(this.input.getValue())) {
                           return;
                        }

                        if (!autism$isAutismCommandInput(inputSnapshot)) {
                           return;
                        }

                        this.autism$owningSuggestions = true;
                        this.updateUsageInfo(widgetParse, result);
                        if (this.pendingSuggestions == future && inputSnapshot.equals(this.input.getValue())) {
                           this.showSuggestions(false);
                        }
                     } catch (Throwable t) {
                        AutismClientAddon.LOG.warn("[Commands] suggestion apply failed", t);

                        try {
                           this.autism$clearActiveSuggestions();
                        } catch (Throwable var7x) {
                        }
                     }
                  };
                  if (minecraft.isSameThread()) {
                     apply.run();
                  } else {
                     minecraft.execute(apply);
                  }
               });
               ci.cancel();
            }
         } else {
            ci.cancel();
         }
      }
   }

   @Unique
   private static boolean autism$isAutismCommandInput(String value) {
      return !CommandManager.init_Z() && CommandManager.init_Z(value);
   }

   @Unique
   private boolean autism$canKeepSuggestionList(String currentInput) {
      if (this.suggestions != null && currentInput != null) {
         try {
            AutismCommandSuggestionsListAccessor accessor = (AutismCommandSuggestionsListAccessor)(Object)this.suggestions;
            String original = accessor.autism$getOriginalContents();
            List<Suggestion> list = accessor.autism$getSuggestionList();
            if (original != null && list != null && !list.isEmpty()) {
               for (Suggestion suggestion : list) {
                  if (suggestion != null) {
                     try {
                        if (currentInput.equals(suggestion.apply(original))) {
                           return true;
                        }
                     } catch (Throwable ignored) {
                        return false;
                     }
                  }
               }

               return false;
            } else {
               return false;
            }
         } catch (Throwable ignored) {
            return false;
         }
      } else {
         return false;
      }
   }

   @Unique
   private void autism$rememberSuggestionKey(
      String value, int cursor, int commandRevision, int moduleRevision, long macroRevision, AbstractContainerMenu menu, int menuRevision
   ) {
      this.autism$cachedInput = value;
      this.autism$cachedCursor = cursor;
      this.autism$cachedCommandRevision = commandRevision;
      this.autism$cachedModuleRevision = moduleRevision;
      this.autism$cachedMacroRevision = macroRevision;
      this.autism$cachedMenu = menu;
      this.autism$cachedMenuRevision = menuRevision;
   }

   @Unique
   private void autism$clearActiveSuggestions() {
      this.input.setSuggestion(null);
      this.suggestions = null;
      this.pendingSuggestions = null;
      this.currentParse = null;
      this.currentParseIsCommand = false;
      this.commandUsage.clear();
      this.keepSuggestions = false;
      this.autism$owningSuggestions = false;
   }
}
