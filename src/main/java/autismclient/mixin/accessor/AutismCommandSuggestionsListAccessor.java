package autismclient.mixin.accessor;

import com.mojang.brigadier.suggestion.Suggestion;
import java.util.List;
import net.minecraft.client.gui.components.CommandSuggestions.SuggestionsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SuggestionsList.class)
public interface AutismCommandSuggestionsListAccessor {
   @Accessor("originalContents")
   String autism$getOriginalContents();

   @Accessor("suggestionList")
   List<Suggestion> autism$getSuggestionList();
}
