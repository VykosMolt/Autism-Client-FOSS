package autismclient.util.macro;

import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.util.TabPlayerListParser;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.TeamColor;

public class CaptureValueAction implements CaptureOutputAction, MacroAction {
   private static final Pattern NUMBER_PATTERN = Pattern.compile(
      "(?i)(?<![a-z0-9_])[-+]?\\d[\\d,._]*(?:[.,]\\d+)?\\s*(?:k|m|b|t|q|thousand|million|billion|trillion)?(?![a-z0-9_])"
   );
   private static final BigDecimal HUNDRED = new BigDecimal("100");
   private static final Comparator<PlayerScoreEntry> SCORE_ORDER = Comparator.comparing(PlayerScoreEntry::value)
      .reversed()
      .thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);
   public CaptureValueAction.Inner_StateCache source = CaptureValueAction.Inner_StateCache.GUI_TITLE;
   public String saveAs = "value";
   public PatternCaptureMatcher.MatchMode matchMode = PatternCaptureMatcher.MatchMode.MATCH;
   public String pattern = "";
   public String exampleText = "";
   public String selectedText = "";
   public String itemFilter = "";
   public String scoreboardRow = "";
   public int scoreboardRowIndex = -1;
   public String scoreboardObjective = "";
   public int slot = -1;
   public CaptureValueAction.Inner_CachePool itemText = CaptureValueAction.Inner_CachePool.NAME;
   public CaptureValueAction.Inner_BufferHelper numberMode = CaptureValueAction.Inner_BufferHelper.OFF;
   public CaptureValueAction.Inner_TaskQueue numberModifier = CaptureValueAction.Inner_TaskQueue.NONE;
   public double numberModifierAmount = 1.0;
   public boolean waitForTrigger = isRecentChatSource(CaptureValueAction.Inner_StateCache.GUI_TITLE);
   public String autofillCommand = "";
   public int autofillTimeoutMs = 5000;
   public boolean autofillCacheList = true;
   public CandidatePoolSelector.Inner_CachePool listSelection = CandidatePoolSelector.Inner_CachePool.RANDOM;
   public CandidatePoolSelector.Inner_DataUtil listFilter = CandidatePoolSelector.Inner_DataUtil.NONE;
   public String listFilterText = "";
   public String listExcludeText = "";
   public int listPickPosition = 1;
   public boolean listStripPrefix;
   public boolean excludeSelf = true;
   private boolean enabled = true;

   @Override
   public void execute(Minecraft mc) {
      this.capture(mc);
   }

   public CaptureValueAction.Inner_NetHelper capture(Minecraft mc) {
      if (mc == null) {
         return CaptureValueAction.Inner_NetHelper.failure("Game unavailable");
      }

      this.clearCaptureOutputs();
      CaptureValueAction.Inner_NetHelper preview = this.preview(mc);
      if (preview.success()) {
         MacroVariableContext.setAll(preview.variables());
      }

      return preview;
   }

   public CaptureValueAction.Inner_NetHelper preview(Minecraft mc) {
      if (mc == null) {
         return CaptureValueAction.Inner_NetHelper.failure("Game unavailable");
      }

      if (this.source == CaptureValueAction.Inner_StateCache.RECENT_CHAT) {
         List<MacroExecutor.LoggedChatMessage> messages = MacroExecutor.getRecentChatMessages();
         if (messages.isEmpty()) {
            return CaptureValueAction.Inner_NetHelper.failure("No recent chat");
         }

         for (MacroExecutor.LoggedChatMessage chat : messages) {
            CaptureValueAction.Inner_NetHelper candidate = this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(this.captureFromChat(chat));
            if (candidate.success()) {
               return candidate;
            }
         }

         return CaptureValueAction.Inner_NetHelper.failure("No recent message matches");
      } else if (this.source == CaptureValueAction.Inner_StateCache.GUI_TITLE) {
         CaptureValueAction.Inner_NetHelper current = this.previewCurrent(mc);
         if (current.success()) {
            return current;
         }

         List<MacroExecutor.ChatHistoryEntry> titles = MacroExecutor.getRecentGuiTitles();

         for (MacroExecutor.ChatHistoryEntry title : titles) {
            CaptureValueAction.Inner_NetHelper candidate = this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(this.captureFromGuiTitle(title));
            if (candidate.success()) {
               return candidate;
            }
         }

         return titles.isEmpty() ? current : CaptureValueAction.Inner_NetHelper.failure("No recent GUI matches");
      } else {
         return this.previewCurrent(mc);
      }
   }

   public CaptureValueAction.Inner_NetHelper previewCurrent(Minecraft mc) {
      if (mc == null) {
         return CaptureValueAction.Inner_NetHelper.failure("Game unavailable");
      }

      if (this.init_Z_6()) {
         String filterError = CandidatePoolSelector.init(this.listFilter, this.listFilterText);
         if (!filterError.isBlank()) {
            return CaptureValueAction.Inner_NetHelper.failure(filterError);
         }

         if (this.source == CaptureValueAction.Inner_StateCache.COMMAND_AUTOFILL || this.source == CaptureValueAction.Inner_StateCache.NAMESCRAPE) {
            return CaptureValueAction.Inner_NetHelper.failure("Fetched when macro runs");
         }
      }

      CaptureValueAction.Inner_DataUtil captured = this.captureFromSource(mc);
      return this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(captured);
   }

   public CaptureValueAction.Inner_NetHelper previewSuggestions(List<String> suggestions, String query) {
      CaptureValueAction.Inner_DataUtil captured = this.pickCandidate(suggestions, Map.of());
      if (captured != null && query != null && !query.isBlank()) {
         Map<String, MacroVarValue> props = new LinkedHashMap<>(captured.value().getProperties());
         props.put("query", MacroVarValue.ofText(query));
         captured = new CaptureValueAction.Inner_DataUtil(captured.text(), MacroVarValue.of(MacroVarValue.ValueType.TEXT, captured.value().asString(), props));
      }

      return this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(captured);
   }

   private boolean init_Z_6() {
      return this.source == CaptureValueAction.Inner_StateCache.COMMAND_AUTOFILL
         || this.source == CaptureValueAction.Inner_StateCache.TABLIST
         || this.source == CaptureValueAction.Inner_StateCache.NAMESCRAPE;
   }

   public CaptureValueAction.Inner_NetHelper previewChat(MacroExecutor.LoggedChatMessage chat) {
      return this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(this.captureFromChat(chat));
   }

   public CaptureValueAction.Inner_NetHelper previewSidebarLine(CaptureValueAction.Inner_JsonUtil line) {
      return this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(this.captureFromScoreboardLine(line));
   }

   public CaptureValueAction.Inner_JsonUtil selectedScoreboardLine(Minecraft mc) {
      return this.init(mc, this.scoreboardRow, this.scoreboardRowIndex, this.scoreboardObjective);
   }

   public CaptureValueAction.Inner_JsonUtil init(Minecraft mc, String preferredKey, int preferredRow, String preferredObjective) {
      List<CaptureValueAction.Inner_JsonUtil> lines = sidebarLines(mc);
      if (lines.isEmpty()) {
         return null;
      }

      String key = preferredKey == null ? "" : preferredKey;

      for (CaptureValueAction.Inner_JsonUtil line : lines) {
         if (!key.isBlank() && key.equals(line.key())) {
            return line;
         }
      }

      CaptureValueAction.Inner_JsonUtil rowCandidate = null;

      for (CaptureValueAction.Inner_JsonUtil line : lines) {
         if (lineMatchesObjective(line, preferredObjective) && line.row() == preferredRow) {
            rowCandidate = line;
            break;
         }
      }

      if (rowCandidate != null && this.previewSidebarLine(rowCandidate).success()) {
         return rowCandidate;
      }

      CaptureValueAction.Inner_JsonUtil best = this.init(lines, preferredRow, preferredObjective, true);
      return best != null ? best : this.init(lines, preferredRow, preferredObjective, false);
   }

   private CaptureValueAction.Inner_JsonUtil init(List<CaptureValueAction.Inner_JsonUtil> lines, int preferredRow, String preferredObjective, boolean requireObjective) {
      CaptureValueAction.Inner_JsonUtil best = null;
      int bestDistance = 2147483647;

      for (CaptureValueAction.Inner_JsonUtil line : lines) {
         if ((!requireObjective || lineMatchesObjective(line, preferredObjective)) && this.previewSidebarLine(line).success()) {
            int distance = preferredRow < 0 ? line.row() : Math.abs(line.row() - preferredRow);
            if (best == null || distance < bestDistance) {
               best = line;
               bestDistance = distance;
            }
         }
      }

      return best;
   }

   private static boolean lineMatchesObjective(CaptureValueAction.Inner_JsonUtil line, String objective) {
      return line != null && (objective == null || objective.isBlank() || objective.equals(line.load()));
   }

   public static List<CaptureValueAction.Inner_JsonUtil> sidebarLines(Minecraft mc) {
      if (mc != null && mc.level != null && mc.player != null) {
         Scoreboard scoreboard = mc.level.getScoreboard();
         Objective objective = sidebarObjective(scoreboard, mc);
         if (objective == null) {
            return List.of();
         }

         NumberFormat scoreFormat = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);
         List<PlayerScoreEntry> entries = scoreboard.listPlayerScores(objective)
            .stream()
            .filter(entryx -> !entryx.isHidden())
            .sorted(SCORE_ORDER)
            .limit(15L)
            .toList();
         List<CaptureValueAction.Inner_JsonUtil> lines = new ArrayList<>(entries.size());
         String objectiveTitle = objective.getDisplayName() == null ? "" : objective.getDisplayName().getString();

         for (int i = 0; i < entries.size(); i++) {
            PlayerScoreEntry entry = entries.get(i);
            PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());
            Component nameComponent = PlayerTeam.formatNameForTeam(team, entry.ownerName());
            Component scoreComponent = entry.formatValue(scoreFormat);
            String name = nameComponent == null ? "" : nameComponent.getString();
            String score = scoreComponent == null ? "" : scoreComponent.getString();
            String text = score.isEmpty() ? name : name + ": " + score;
            String key = objective.getName() + "\u001f" + entry.owner();
            lines.add(new CaptureValueAction.Inner_JsonUtil(key, i, objective.getName(), objectiveTitle, entry.owner(), name, score, text));
         }

         return List.copyOf(lines);
      } else {
         return List.of();
      }
   }

   private static Objective sidebarObjective(Scoreboard scoreboard, Minecraft mc) {
      if (scoreboard != null && mc != null && mc.player != null) {
         Objective teamObjective = null;
         PlayerTeam team = scoreboard.getPlayersTeam(mc.player.getScoreboardName());
         if (team != null) {
            Optional<TeamColor> color = team.getColor();
            if (color.isPresent()) {
               teamObjective = scoreboard.getDisplayObjective(color.get().displaySlot());
            }
         }

         return teamObjective != null ? teamObjective : scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
      } else {
         return null;
      }
   }

   public CaptureValueAction.Inner_NetHelper previewExample() {
      return this.exampleText != null && !this.exampleText.isBlank()
         ? this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(new CaptureValueAction.Inner_DataUtil(this.exampleText, MacroVarValue.ofText(this.exampleText)))
         : CaptureValueAction.Inner_NetHelper.failure("Enter an example message");
   }

   private CaptureValueAction.Inner_NetHelper init_TaskQueue_NetHelpe_TaskQueue_DataUtil(CaptureValueAction.Inner_DataUtil captured) {
      if (captured != null && captured.value != null) {
         Map<String, MacroVarValue> additions = new LinkedHashMap<>();
         if (this.init_Z_6()) {
            additions = new LinkedHashMap<>(init(additions, this.saveAs, captured.value));
            return additions.isEmpty()
               ? CaptureValueAction.Inner_NetHelper.failure("Enter a variable name")
               : new CaptureValueAction.Inner_NetHelper(true, captured.text, additions, "Ready");
         }

         if (this.matchMode != PatternCaptureMatcher.MatchMode.MATCH && this.pattern != null && !this.pattern.isBlank()) {
            Optional<PatternCaptureMatcher.CaptureResult> result = PatternCaptureMatcher.init(this.matchMode, this.pattern, captured.text);
            if (result.isEmpty()) {
               return CaptureValueAction.Inner_NetHelper.failure("Pattern does not match");
            }

            additions.putAll(result.get().variables());
         } else if (this.pattern != null
            && !this.pattern.isBlank()
            && !captured.text.toLowerCase(Locale.ROOT).contains(this.pattern.toLowerCase(Locale.ROOT))) {
            return CaptureValueAction.Inner_NetHelper.failure("Text does not match");
         }

         additions = new LinkedHashMap<>(init(additions, this.saveAs, captured.value));
         if (this.effectiveNumberMode() != CaptureValueAction.Inner_BufferHelper.OFF) {
            additions = new LinkedHashMap<>(init(additions, this.saveAs, this.effectiveNumberMode()));
         }

         CaptureValueAction.Inner_ConfigStore modification = this.init_TaskQueue_ConfigSt_Map(additions);
         if (!modification.init_Z()) {
            return CaptureValueAction.Inner_NetHelper.failure(modification.init_Str());
         }

         additions = new LinkedHashMap<>(modification.init_Map());
         return additions.isEmpty()
            ? CaptureValueAction.Inner_NetHelper.failure("Enter a variable name")
            : new CaptureValueAction.Inner_NetHelper(true, captured.text, additions, "Ready");
      } else {
         return CaptureValueAction.Inner_NetHelper.failure("Nothing available now");
      }
   }

   public static boolean isRecentChatSource(CaptureValueAction.Inner_StateCache source) {
      return source == CaptureValueAction.Inner_StateCache.RECENT_CHAT;
   }

   public String numberModifierError() {
      if (this.init_Z_6()) {
         return "";
      } else {
         CaptureValueAction.Inner_TaskQueue modifier = this.numberModifier == null ? CaptureValueAction.Inner_TaskQueue.NONE : this.numberModifier;
         if (modifier == CaptureValueAction.Inner_TaskQueue.NONE) {
            return "";
         } else if (!Double.isFinite(this.numberModifierAmount)) {
            return "Enter a valid modifier";
         } else {
            return modifier == CaptureValueAction.Inner_TaskQueue.DIVIDE && this.numberModifierAmount == 0.0 ? "Cannot divide by zero" : "";
         }
      }
   }

   private CaptureValueAction.Inner_BufferHelper effectiveNumberMode() {
      return this.numberMode == null ? CaptureValueAction.Inner_BufferHelper.OFF : this.numberMode;
   }

   private MacroTemplateResolver.ResolveMode numberResolveMode() {
      return this.effectiveNumberMode() == CaptureValueAction.Inner_BufferHelper.DROP_CENTS ? MacroTemplateResolver.ResolveMode.WHOLE : MacroTemplateResolver.ResolveMode.AUTO;
   }

   private CaptureValueAction.Inner_ConfigStore init_TaskQueue_ConfigSt_Map(Map<String, MacroVarValue> values) {
      String error = this.numberModifierError();
      if (!error.isBlank()) {
         return CaptureValueAction.Inner_ConfigStore.init(error);
      }

      CaptureValueAction.Inner_TaskQueue modifier = this.numberModifier == null ? CaptureValueAction.Inner_TaskQueue.NONE : this.numberModifier;
      if (modifier != CaptureValueAction.Inner_TaskQueue.NONE && values != null && !values.isEmpty()) {
         BigDecimal operand = BigDecimal.valueOf(this.numberModifierAmount);
         String outputName = MacroVariableStore.load(this.saveAs);
         boolean targetTracked = !outputName.isBlank() && values.containsKey(outputName);
         Map<String, MacroVarValue> modified = new LinkedHashMap<>();
         boolean ok = false;

         for (Entry<String, MacroVarValue> entry : values.entrySet()) {
            boolean target = targetTracked && entry.getKey().equals(outputName);
            MacroVarValue original = entry.getValue();
            MacroVarValue result = init(original, modifier, operand, target, this.numberResolveMode());
            modified.put(entry.getKey(), result);
            if (result != original && (target || !targetTracked)) {
               ok = true;
            }
         }

         return ok ? CaptureValueAction.Inner_ConfigStore.init(modified) : CaptureValueAction.Inner_ConfigStore.init("Not a number");
      } else {
         return CaptureValueAction.Inner_ConfigStore.init(values == null ? Map.of() : values);
      }
   }

   private static MacroVarValue init(MacroVarValue value, CaptureValueAction.Inner_TaskQueue modifier, BigDecimal operand, boolean embedded, MacroTemplateResolver.ResolveMode style) {
      BigDecimal number = init_BigDecimal(value, embedded, style);
      if (number == null) {
         return value;
      }

      try {
         BigDecimal var10000;
         switch (modifier) {
            case NONE:
               var10000 = number;
               break;
            case PLUS:
               var10000 = number.add(operand);
               break;
            case MINUS:
               var10000 = number.subtract(operand);
               break;
            case MULTIPLY:
               var10000 = number.multiply(operand);
               break;
            case DIVIDE:
               var10000 = init_BigDecimal_BigDecimal_BigDecimal(number, operand);
               break;
            case PLUS_PERCENT:
               var10000 = load(number, BigDecimal.ONE.add(percentToFraction(operand)));
               break;
            case MINUS_PERCENT:
               var10000 = load(number, BigDecimal.ONE.subtract(percentToFraction(operand)));
               break;
            default:
               throw new MatchException(null, null);
         }

         BigDecimal result = var10000;
         return withNumber(value, result);
      } catch (ArithmeticException ignored) {
         return value;
      }
   }

   private static BigDecimal init_BigDecimal_BigDecimal_BigDecimal(BigDecimal number, BigDecimal operand) {
      BigDecimal exact = number.divide(operand, new MathContext(Math.max(16, number.precision() + 8), RoundingMode.HALF_UP));
      return read(exact, number);
   }

   public static BigDecimal percentToFraction(BigDecimal percent) {
      return percent.divide(HUNDRED, new MathContext(34, RoundingMode.HALF_UP));
   }

   private static BigDecimal load(BigDecimal number, BigDecimal factor) {
      return read(number.multiply(factor), number);
   }

   private static BigDecimal read(BigDecimal result, BigDecimal source) {
      int scale = Math.max(2, source.scale() + 2);
      int adjustedExponent = result.precision() - result.scale() - 1;
      if (result.signum() != 0 && adjustedExponent < 0) {
         scale = Math.max(scale, 1 - adjustedExponent);
      }

      return result.setScale(scale, RoundingMode.HALF_UP);
   }

   private static BigDecimal init_BigDecimal(MacroVarValue value, boolean embedded, MacroTemplateResolver.ResolveMode style) {
      if (value != null && value.getType() != MacroVarValue.ValueType.SLOT && value.getType() != MacroVarValue.ValueType.IDENTIFIER) {
         try {
            return new BigDecimal(embedded ? MacroTemplateResolver.extractNormalizedNumber(value.asString(), style) : MacroTemplateResolver.parseNormalizedNumber(value.asString(), style));
         } catch (IllegalArgumentException ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   static Map<String, MacroVarValue> init(Map<String, MacroVarValue> patternValues, String saveAs, MacroVarValue sourceValue) {
      Map<String, MacroVarValue> outputs = new LinkedHashMap<>();
      if (patternValues != null) {
         outputs.putAll(patternValues);
      }

      String outputName = MacroVariableStore.load(saveAs);
      if (!outputName.isBlank() && sourceValue != null) {
         outputs.putIfAbsent(outputName, sourceValue);
      }

      return outputs;
   }

   static Map<String, MacroVarValue> init_Map_Map(Map<String, MacroVarValue> values) {
      return init(values, "", CaptureValueAction.Inner_BufferHelper.SUFFIX_KMB);
   }

   static Map<String, MacroVarValue> init_Map_Map_Str(Map<String, MacroVarValue> values, String saveAs) {
      return init(values, saveAs, CaptureValueAction.Inner_BufferHelper.SUFFIX_KMB);
   }

   static Map<String, MacroVarValue> init(Map<String, MacroVarValue> values, String saveAs, CaptureValueAction.Inner_BufferHelper mode) {
      if (values != null && !values.isEmpty()) {
         MacroTemplateResolver.ResolveMode style = mode == CaptureValueAction.Inner_BufferHelper.DROP_CENTS ? MacroTemplateResolver.ResolveMode.WHOLE : MacroTemplateResolver.ResolveMode.AUTO;
         String outputName = MacroVariableStore.load(saveAs);
         Map<String, MacroVarValue> normalized = new LinkedHashMap<>();
         values.forEach((name, value) -> normalized.put(name, init_l(value, !outputName.isBlank() && outputName.equals(name), style)));
         return normalized;
      } else {
         return Map.of();
      }
   }

   private static MacroVarValue init_l(MacroVarValue value, boolean embedded, MacroTemplateResolver.ResolveMode style) {
      BigDecimal number = init_BigDecimal(value, embedded, style);
      return number == null ? value : withNumber(value, number);
   }

   private static MacroVarValue withNumber(MacroVarValue source, BigDecimal number) {
      return source.getProperties().isEmpty() ? MacroVarValue.ofNumber(number) : MacroVarValue.of(source.getType(), MacroTemplateResolver.formatBigDecimal(number), source.getProperties());
   }

   private CaptureValueAction.Inner_DataUtil captureFromSource(Minecraft mc) {
      CaptureValueAction.Inner_DataUtil var10000;
      switch (this.source == null ? CaptureValueAction.Inner_StateCache.GUI_TITLE : this.source) {
         case GUI_TITLE:
            Screen screen = mc.gui.screen();
            if (screen != null && !GuiScreenMatcher.isClientScreen(screen)) {
               String title = screen.getTitle() == null ? "" : screen.getTitle().getString();
               var10000 = new CaptureValueAction.Inner_DataUtil(
                  title, MacroVarValue.of(MacroVarValue.ValueType.GUI, title, Map.of("title", MacroVarValue.ofText(title), "type", MacroVarValue.ofText(GuiScreenMatcher.screenTypeName(screen))))
               );
            } else {
               CustomMenuSnapshot menu = autismclient.util.custommenu.CustomMenuTracker.snapshot();
               if (menu == null) {
                  var10000 = null;
               } else {
                  String title = menu.title();
                  var10000 = new CaptureValueAction.Inner_DataUtil(
                     title, MacroVarValue.of(MacroVarValue.ValueType.GUI, title, Map.of("title", MacroVarValue.ofText(title), "type", MacroVarValue.ofText("CUSTOM_MENU")))
                  );
               }
            }
            break;
         case RECENT_CHAT:
            List<MacroExecutor.LoggedChatMessage> messages = MacroExecutor.getRecentChatMessages();
            var10000 = messages.isEmpty() ? null : this.captureFromChat(messages.get(0));
            break;
         case SCOREBOARD:
            var10000 = this.captureFromScoreboardLine(this.selectedScoreboardLine(mc));
            break;
         case GUI_ITEM:
            var10000 = this.captureFromContainer(mc, false);
            break;
         case PLAYER_ITEM:
            var10000 = this.read_TaskQueue_DataUtil(mc);
            break;
         case CURSOR_ITEM:
            var10000 = mc.player != null && mc.player.containerMenu != null
               ? this.captureFromStack(mc.player.containerMenu.getCarried(), -1)
               : null;
            break;
         case HELD_ITEM:
            var10000 = mc.player == null
               ? null
               : this.captureFromStack(mc.player.getMainHandItem(), mc.player.getInventory().getSelectedSlot());
            break;
         case COMMAND_AUTOFILL:
         case NAMESCRAPE:
            var10000 = null;
            break;
         case TABLIST:
            var10000 = this.captureFromTabList(mc);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private CaptureValueAction.Inner_DataUtil captureFromTabList(Minecraft mc) {
      List<TabPlayerListParser.ParsedPlayer> players = TabPlayerListParser.init(mc);
      List<String> names = new ArrayList<>(players.size() + 1);
      Map<String, String> prefixes = new HashMap<>();

      for (TabPlayerListParser.ParsedPlayer player : players) {
         names.add(player.name());
         if (player.hasPrefix()) {
            prefixes.put(player.name(), player.load());
         }
      }

      if (!this.excludeSelf && mc.player != null && mc.player.getGameProfile() != null) {
         names.add(mc.player.getGameProfile().name());
         names.sort(String.CASE_INSENSITIVE_ORDER);
      }

      return this.pickCandidate(names, prefixes);
   }

   public List<String> filterCandidates(List<String> candidates) {
      if (candidates == null) {
         return List.of();
      }

      List<String> pool = CandidatePoolSelector.init(candidates, this.listFilter, this.listFilterText);
      return CandidatePoolSelector.init(pool, this.listExcludeText);
   }

   private CaptureValueAction.Inner_DataUtil pickCandidate(List<String> candidates, Map<String, String> prefixByValue) {
      if (candidates == null) {
         return null;
      }

      List<String> pool = this.filterCandidates(candidates);
      CandidatePoolSelector.Inner_BufferHelper state = MacroExecutor.getCandidatePoolState(this);
      Optional<CandidatePoolSelector.Inner_StreamUtil> pick = CandidatePoolSelector.init(pool, this.listSelection, this.listPickPosition, state);
      if (pick.isEmpty()) {
         return null;
      }

      CandidatePoolSelector.Inner_StreamUtil picked = pick.get();
      String value = this.listStripPrefix ? CandidatePoolSelector.init(picked.value(), this.listFilter, this.listFilterText) : picked.value();
      Map<String, MacroVarValue> props = new LinkedHashMap<>();
      props.put("index", MacroVarValue.ofNumber(picked.index() + 1));
      props.put("count", MacroVarValue.ofNumber(pool.size()));
      props.put("total", MacroVarValue.ofNumber(candidates.size()));
      props.put("list", MacroVarValue.ofText(String.join(", ", pool)));
      String prefix = prefixByValue == null ? null : prefixByValue.get(picked.value());
      if (prefix != null && !prefix.isBlank()) {
         props.put("prefix", MacroVarValue.ofText(prefix));
      }

      return new CaptureValueAction.Inner_DataUtil(value, MacroVarValue.of(MacroVarValue.ValueType.TEXT, value, props));
   }

   private CaptureValueAction.Inner_DataUtil captureFromChat(MacroExecutor.LoggedChatMessage chat) {
      if (chat == null) {
         return null;
      }

      String display = chat.displayText() == null ? "" : chat.displayText();
      Map<String, MacroVarValue> props = new LinkedHashMap<>();
      props.put("sender", MacroVarValue.ofText(chat.sender()));
      props.put("message", MacroVarValue.ofText(chat.message()));
      props.put("display", MacroVarValue.ofText(display));
      props.put("source", MacroVarValue.ofText(chat.source() == null ? "" : chat.source().name()));
      return new CaptureValueAction.Inner_DataUtil(display, MacroVarValue.of(MacroVarValue.ValueType.TEXT, display, props));
   }

   private CaptureValueAction.Inner_DataUtil captureFromGuiTitle(MacroExecutor.ChatHistoryEntry gui) {
      if (gui == null) {
         return null;
      }

      String title = gui.title() == null ? "" : gui.title();
      return new CaptureValueAction.Inner_DataUtil(
         title, MacroVarValue.of(MacroVarValue.ValueType.GUI, title, Map.of("title", MacroVarValue.ofText(title), "type", MacroVarValue.ofText(gui.screenType() == null ? "" : gui.screenType())))
      );
   }

   private CaptureValueAction.Inner_DataUtil captureFromScoreboardLine(CaptureValueAction.Inner_JsonUtil line) {
      if (line == null) {
         return null;
      }

      Map<String, MacroVarValue> props = new LinkedHashMap<>();
      props.put("line", MacroVarValue.ofText(line.flush()));
      props.put("name", MacroVarValue.ofText(line.update()));
      props.put("score", MacroVarValue.ofText(line.sync()));
      props.put("row", MacroVarValue.ofNumber(BigDecimal.valueOf(line.row() + 1L)));
      props.put("title", MacroVarValue.ofText(line.read()));
      props.put("objective", MacroVarValue.ofText(line.load()));
      return new CaptureValueAction.Inner_DataUtil(line.flush(), MacroVarValue.of(MacroVarValue.ValueType.TEXT, line.flush(), props));
   }

   private CaptureValueAction.Inner_DataUtil captureFromContainer(Minecraft mc, boolean playerOnly) {
      if (mc.player != null && mc.player.containerMenu != null) {
         CaptureValueAction.Inner_StreamUtil filterResolution = this.resolveItemFilter(mc);
         if (!filterResolution.init_Z()) {
            return null;
         }

         MacroItemTarget filter = filterResolution.filter();

         for (Slot candidate : mc.player.containerMenu.slots) {
            if (candidate != null && !candidate.getItem().isEmpty()) {
               boolean playerSlot = autismclient.util.InventorySlotHelper.isPlayerInventorySlot(mc, candidate);
               if (playerOnly == playerSlot) {
                  int visible = autismclient.util.InventorySlotHelper.toVisibleSlot(mc, candidate.index);
                  if ((this.slot < 0 || this.slot == visible || this.slot == candidate.index)
                     && (filter == null || !filter.hasItemFilter() || filter.matchScore(candidate.getItem(), visible) >= 0)) {
                     return this.captureFromStack(candidate.getItem(), visible);
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private CaptureValueAction.Inner_DataUtil read_TaskQueue_DataUtil(Minecraft mc) {
      CaptureValueAction.Inner_DataUtil menu = this.captureFromContainer(mc, true);
      if (menu == null && mc.player != null) {
         CaptureValueAction.Inner_StreamUtil filterResolution = this.resolveItemFilter(mc);
         if (!filterResolution.init_Z()) {
            return null;
         }

         MacroItemTarget filter = filterResolution.filter();

         for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && (this.slot < 0 || this.slot == i) && (filter == null || !filter.hasItemFilter() || filter.matchScore(stack, i) >= 0)) {
               return this.captureFromStack(stack, i);
            }
         }

         return null;
      } else {
         return menu;
      }
   }

   private CaptureValueAction.Inner_StreamUtil resolveItemFilter(Minecraft mc) {
      if (this.itemFilter != null && !this.itemFilter.isBlank()) {
         MacroTemplateResolver.ResolveResult resolution = MacroVariableContext.resolveTemplate(this.itemFilter, mc);
         return resolution.success()
            ? new CaptureValueAction.Inner_StreamUtil(true, MacroItemTarget.parse(resolution.value()))
            : new CaptureValueAction.Inner_StreamUtil(false, null);
      } else {
         return new CaptureValueAction.Inner_StreamUtil(true, null);
      }
   }

   private CaptureValueAction.Inner_DataUtil captureFromStack(ItemStack stack, int visibleSlot) {
      if (stack != null && !stack.isEmpty()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         String itemId = id == null ? "" : id.toString();
         String name = stack.getHoverName().getString();
         ItemLore lore = stack.get(DataComponents.LORE);
         String loreText = lore == null ? "" : lore.lines().stream().map(line -> line.getString()).reduce((a, b) -> a + "\n" + b).orElse("");
         Map<String, MacroVarValue> props = new LinkedHashMap<>();
         props.put("name", MacroVarValue.ofText(name));
         props.put("id", MacroVarValue.ofIdentifier(itemId));
         props.put("count", MacroVarValue.ofNumber(stack.getCount()));
         props.put("slot", MacroVarValue.ofSlot(visibleSlot));
         props.put("lore", MacroVarValue.ofText(loreText));
         if (lore != null) {
            for (int i = 0; i < lore.lines().size(); i++) {
               props.put("lore" + (i + 1), MacroVarValue.ofText(lore.lines().get(i).getString()));
            }
         }

         String var10000;
         switch (this.itemText == null ? CaptureValueAction.Inner_CachePool.NAME : this.itemText) {
            case NAME:
               var10000 = name;
               break;
            case ID:
               var10000 = itemId;
               break;
            case LORE:
               var10000 = loreText;
               break;
            default:
               throw new MatchException(null, null);
         }

         String text = var10000;
         return new CaptureValueAction.Inner_DataUtil(text, MacroVarValue.of(MacroVarValue.ValueType.ITEM, text, props));
      } else {
         return null;
      }
   }

   private void clearCaptureOutputs() {
      if (this.saveAs != null && !this.saveAs.isBlank()) {
         MacroVariableContext.remove(this.saveAs);
      }

      for (String name : PatternCaptureMatcher.init(this.matchMode, this.pattern)) {
         MacroVariableContext.remove(name);
      }
   }

   public static String extractNumber(String example) {
      if (example != null && !example.isBlank()) {
         Matcher matcher = NUMBER_PATTERN.matcher(example);
         return matcher.find() ? matcher.group().trim() : "";
      } else {
         return "";
      }
   }

   public static String init(String example, String selected, String variableName) {
      if (example != null && selected != null && !selected.isBlank()) {
         String root = MacroVariableStore.load(variableName);
         if (root.isBlank()) {
            if (variableName != null && !variableName.isBlank()) {
               return "";
            }

            root = "value";
         }

         int index = example.indexOf(selected);
         if (index < 0) {
            index = example.toLowerCase(Locale.ROOT).indexOf(selected.toLowerCase(Locale.ROOT));
         }

         if (index < 0) {
            return "";
         }

         String before = escapeBraces(example.substring(0, index));
         String after = escapeBraces(example.substring(index + selected.length()));
         return before + "{" + root + "}" + after;
      } else {
         return "";
      }
   }

   private static String escapeBraces(String value) {
      return value == null ? "" : value.replace("{", "{{").replace("}", "}}");
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.CAPTURE_VALUE;
   }

   @Override
   public String getDisplayName() {
      return "Capture " + (this.saveAs != null && !this.saveAs.isBlank() ? this.saveAs : "Value");
   }

   @Override
   public String getIcon() {
      return "{}";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Override
   public String load() {
      return this.saveAs;
   }

   @Override
   public void a_(String name) {
      this.saveAs = name == null ? "" : name;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("source", (this.source == null ? CaptureValueAction.Inner_StateCache.GUI_TITLE : this.source).name());
      tag.putString("saveAs", this.saveAs == null ? "" : this.saveAs);
      tag.putString("matchMode", (this.matchMode == null ? PatternCaptureMatcher.MatchMode.MATCH : this.matchMode).name());
      tag.putString("pattern", this.pattern == null ? "" : this.pattern);
      tag.putString("exampleText", this.exampleText == null ? "" : this.exampleText);
      tag.putString("selectedText", this.selectedText == null ? "" : this.selectedText);
      tag.putString("itemFilter", this.itemFilter == null ? "" : this.itemFilter);
      tag.putString("scoreboardRow", this.scoreboardRow == null ? "" : this.scoreboardRow);
      tag.putInt("scoreboardRowIndex", this.scoreboardRowIndex);
      tag.putString("scoreboardObjective", this.scoreboardObjective == null ? "" : this.scoreboardObjective);
      tag.putInt("slot", this.slot);
      tag.putString("itemText", (this.itemText == null ? CaptureValueAction.Inner_CachePool.NAME : this.itemText).name());
      tag.putString("numberMode", this.effectiveNumberMode().name());
      tag.putString("numberModifier", (this.numberModifier == null ? CaptureValueAction.Inner_TaskQueue.NONE : this.numberModifier).name());
      tag.putDouble("numberModifierAmount", this.numberModifierAmount);
      tag.putBoolean("waitForTrigger", this.waitForTrigger);
      tag.putString("autofillCommand", this.autofillCommand == null ? "" : this.autofillCommand);
      tag.putInt("autofillTimeoutMs", this.autofillTimeoutMs);
      tag.putBoolean("autofillCacheList", this.autofillCacheList);
      tag.putString("listSelection", (this.listSelection == null ? CandidatePoolSelector.Inner_CachePool.RANDOM : this.listSelection).name());
      tag.putString("listFilter", (this.listFilter == null ? CandidatePoolSelector.Inner_DataUtil.NONE : this.listFilter).name());
      tag.putString("listFilterText", this.listFilterText == null ? "" : this.listFilterText);
      tag.putString("listExcludeText", this.listExcludeText == null ? "" : this.listExcludeText);
      tag.putInt("listPickPosition", this.listPickPosition);
      tag.putBoolean("listStripPrefix", this.listStripPrefix);
      tag.putBoolean("excludeSelf", this.excludeSelf);
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.source = MacroNbtCodecHelper.parseEnum(CaptureValueAction.Inner_StateCache.class, tag.getStringOr("source", "GUI_TITLE"), CaptureValueAction.Inner_StateCache.GUI_TITLE);
      this.saveAs = tag.getStringOr("saveAs", "value");
      this.matchMode = MacroNbtCodecHelper.parseEnum(PatternCaptureMatcher.MatchMode.class, tag.getStringOr("matchMode", "MATCH"), PatternCaptureMatcher.MatchMode.MATCH);
      this.pattern = tag.getStringOr("pattern", "");
      this.exampleText = tag.getStringOr("exampleText", "");
      this.selectedText = tag.getStringOr("selectedText", "");
      this.itemFilter = tag.getStringOr("itemFilter", "");
      this.scoreboardRow = tag.getStringOr("scoreboardRow", "");
      this.scoreboardRowIndex = tag.getIntOr("scoreboardRowIndex", -1);
      this.scoreboardObjective = tag.getStringOr("scoreboardObjective", "");
      this.slot = tag.getIntOr("slot", -1);
      this.itemText = MacroNbtCodecHelper.parseEnum(CaptureValueAction.Inner_CachePool.class, tag.getStringOr("itemText", "NAME"), CaptureValueAction.Inner_CachePool.NAME);
      this.numberMode = tag.contains("numberMode")
         ? MacroNbtCodecHelper.parseEnum(CaptureValueAction.Inner_BufferHelper.class, tag.getStringOr("numberMode", "OFF"), CaptureValueAction.Inner_BufferHelper.OFF)
         : (tag.getBooleanOr("normalizeNumbers", false) ? CaptureValueAction.Inner_BufferHelper.SUFFIX_KMB : CaptureValueAction.Inner_BufferHelper.OFF);
      this.numberModifier = MacroNbtCodecHelper.parseEnum(CaptureValueAction.Inner_TaskQueue.class, tag.getStringOr("numberModifier", "NONE"), CaptureValueAction.Inner_TaskQueue.NONE);
      this.numberModifierAmount = tag.getDoubleOr("numberModifierAmount", 1.0);
      this.waitForTrigger = tag.getBooleanOr("waitForTrigger", isRecentChatSource(this.source));
      this.autofillCommand = tag.getStringOr("autofillCommand", "");
      this.autofillTimeoutMs = tag.getIntOr("autofillTimeoutMs", 5000);
      this.autofillCacheList = tag.getBooleanOr("autofillCacheList", true);
      this.listSelection = MacroNbtCodecHelper.parseEnum(CandidatePoolSelector.Inner_CachePool.class, tag.getStringOr("listSelection", "RANDOM"), CandidatePoolSelector.Inner_CachePool.RANDOM);
      this.listFilter = MacroNbtCodecHelper.parseEnum(CandidatePoolSelector.Inner_DataUtil.class, tag.getStringOr("listFilter", "NONE"), CandidatePoolSelector.Inner_DataUtil.NONE);
      this.listFilterText = tag.getStringOr("listFilterText", "");
      this.listExcludeText = tag.getStringOr("listExcludeText", "");
      this.listPickPosition = tag.getIntOr("listPickPosition", 1);
      this.listStripPrefix = tag.getBooleanOr("listStripPrefix", false);
      this.excludeSelf = tag.getBooleanOr("excludeSelf", true);
      this.enabled = tag.getBooleanOr("enabled", true);
   }

   public enum Inner_BufferHelper {
      OFF,
      SUFFIX_KMB,
      DROP_CENTS;
   }

   public enum Inner_CachePool {
      NAME,
      ID,
      LORE;
   }

   private record Inner_ConfigStore(boolean init_Z, Map<String, MacroVarValue> init_Map, String init_Str) {
      private static CaptureValueAction.Inner_ConfigStore init(Map<String, MacroVarValue> values) {
         return new CaptureValueAction.Inner_ConfigStore(true, values == null ? Map.of() : values, "");
      }

      private static CaptureValueAction.Inner_ConfigStore init(String message) {
         return new CaptureValueAction.Inner_ConfigStore(false, Map.of(), message == null ? "Invalid number modifier" : message);
      }
   }

   private record Inner_DataUtil(String text, MacroVarValue value) {
   }

   public record Inner_JsonUtil(String key, int row, String load, String read, String write, String update, String sync, String flush) {
   }

   public record Inner_NetHelper(boolean success, String text, Map<String, MacroVarValue> variables, String load) {
      public Inner_NetHelper {
         text = text == null ? "" : text;
         variables = variables == null ? Map.of() : Map.copyOf(variables);
         load = load == null ? "" : load;
      }

      public static CaptureValueAction.Inner_NetHelper failure(String message) {
         return new CaptureValueAction.Inner_NetHelper(false, "", Map.of(), message);
      }

      public String textOf(String variableName) {
         String clean = MacroVariableStore.load(variableName);
         MacroVarValue value = clean.isBlank() ? null : this.variables.get(clean);
         return value == null ? "" : value.asString();
      }
   }

   public enum Inner_StateCache {
      GUI_TITLE,
      RECENT_CHAT,
      SCOREBOARD,
      GUI_ITEM,
      PLAYER_ITEM,
      CURSOR_ITEM,
      HELD_ITEM,
      COMMAND_AUTOFILL,
      TABLIST,
      NAMESCRAPE;
   }

   private record Inner_StreamUtil(boolean init_Z, MacroItemTarget filter) {
   }

   public enum Inner_TaskQueue {
      NONE,
      PLUS,
      MINUS,
      MULTIPLY,
      DIVIDE,
      PLUS_PERCENT,
      MINUS_PERCENT;
   }
}
