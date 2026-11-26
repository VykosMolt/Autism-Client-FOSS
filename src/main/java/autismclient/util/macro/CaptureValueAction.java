package autismclient.util.macro;

import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.util.bO;
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

public class CaptureValueAction implements CacheEntry, MacroAction {
   private static final Pattern init_Pattern = Pattern.compile(
      "(?i)(?<![a-z0-9_])[-+]?\\d[\\d,._]*(?:[.,]\\d+)?\\s*(?:k|m|b|t|q|thousand|million|billion|trillion)?(?![a-z0-9_])"
   );
   private static final BigDecimal init_BigDecimal = new BigDecimal("100");
   private static final Comparator<PlayerScoreEntry> init_Comparator = Comparator.comparing(PlayerScoreEntry::value)
      .reversed()
      .thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);
   public CaptureValueAction.Inner_StateCache source = CaptureValueAction.Inner_StateCache.GUI_TITLE;
   public String saveAs = "value";
   public UtilCore.Inner_DataUtil matchMode = UtilCore.Inner_DataUtil.MATCH;
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
   public boolean waitForTrigger = init_Z_TaskQueue_StateCac(CaptureValueAction.Inner_StateCache.GUI_TITLE);
   public String autofillCommand = "";
   public int autofillTimeoutMs = 5000;
   public boolean autofillCacheList = true;
   public ConfigStore.Inner_CachePool listSelection = ConfigStore.Inner_CachePool.RANDOM;
   public ConfigStore.Inner_DataUtil listFilter = ConfigStore.Inner_DataUtil.NONE;
   public String listFilterText = "";
   public String listExcludeText = "";
   public int listPickPosition = 1;
   public boolean listStripPrefix;
   public boolean excludeSelf = true;
   private boolean init_Z = true;

   @Override
   public void execute(Minecraft mc) {
      this.init_TaskQueue_NetHelpe_Minecraft(mc);
   }

   public CaptureValueAction.Inner_NetHelper init_TaskQueue_NetHelpe_Minecraft(Minecraft mc) {
      if (mc == null) {
         return CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("Game unavailable");
      }

      this.init_V_9();
      CaptureValueAction.Inner_NetHelper preview = this.load_TaskQueue_NetHelpe_Minecraft(mc);
      if (preview.init_Z()) {
         n.init_V_Map(preview.init_Map());
      }

      return preview;
   }

   public CaptureValueAction.Inner_NetHelper load_TaskQueue_NetHelpe_Minecraft(Minecraft mc) {
      if (mc == null) {
         return CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("Game unavailable");
      }

      if (this.source == CaptureValueAction.Inner_StateCache.RECENT_CHAT) {
         List<MacroExecutor.Inner_PacketUtil> messages = MacroExecutor.load_List();
         if (messages.isEmpty()) {
            return CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("No recent chat");
         }

         for (MacroExecutor.Inner_PacketUtil chat : messages) {
            CaptureValueAction.Inner_NetHelper candidate = this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(this.init_TaskQueue_DataUtil_MacroExecutor_Pack(chat));
            if (candidate.init_Z()) {
               return candidate;
            }
         }

         return CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("No recent message matches");
      } else if (this.source == CaptureValueAction.Inner_StateCache.GUI_TITLE) {
         CaptureValueAction.Inner_NetHelper current = this.read_TaskQueue_NetHelpe(mc);
         if (current.init_Z()) {
            return current;
         }

         List<MacroExecutor.Inner_HashUtil> titles = MacroExecutor.read_List();

         for (MacroExecutor.Inner_HashUtil title : titles) {
            CaptureValueAction.Inner_NetHelper candidate = this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(this.init_TaskQueue_DataUtil_MacroExecutor_Hash(title));
            if (candidate.init_Z()) {
               return candidate;
            }
         }

         return titles.isEmpty() ? current : CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("No recent GUI matches");
      } else {
         return this.read_TaskQueue_NetHelpe(mc);
      }
   }

   public CaptureValueAction.Inner_NetHelper read_TaskQueue_NetHelpe(Minecraft mc) {
      if (mc == null) {
         return CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("Game unavailable");
      }

      if (this.init_Z_6()) {
         String filterError = ConfigStore.init(this.listFilter, this.listFilterText);
         if (!filterError.isBlank()) {
            return CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str(filterError);
         }

         if (this.source == CaptureValueAction.Inner_StateCache.COMMAND_AUTOFILL || this.source == CaptureValueAction.Inner_StateCache.NAMESCRAPE) {
            return CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("Fetched when macro runs");
         }
      }

      CaptureValueAction.Inner_DataUtil captured = this.init_TaskQueue_DataUtil_Minecraft(mc);
      return this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(captured);
   }

   public CaptureValueAction.Inner_NetHelper init_TaskQueue_NetHelpe_List_Str(List<String> suggestions, String query) {
      CaptureValueAction.Inner_DataUtil captured = this.init_TaskQueue_DataUtil_List_Map(suggestions, Map.of());
      if (captured != null && query != null && !query.isBlank()) {
         Map<String, l> props = new LinkedHashMap<>(captured.init_l().init_Map());
         props.put("query", l.init_l_Obj(query));
         captured = new CaptureValueAction.Inner_DataUtil(captured.init_Str(), l.init(l.Inner_DataUtil.TEXT, captured.init_l().init_Str(), props));
      }

      return this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(captured);
   }

   private boolean init_Z_6() {
      return this.source == CaptureValueAction.Inner_StateCache.COMMAND_AUTOFILL
         || this.source == CaptureValueAction.Inner_StateCache.TABLIST
         || this.source == CaptureValueAction.Inner_StateCache.NAMESCRAPE;
   }

   public CaptureValueAction.Inner_NetHelper init_TaskQueue_NetHelpe_MacroExecutor_Pack(MacroExecutor.Inner_PacketUtil chat) {
      return this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(this.init_TaskQueue_DataUtil_MacroExecutor_Pack(chat));
   }

   public CaptureValueAction.Inner_NetHelper init_TaskQueue_NetHelpe_TaskQueue_JsonUtil(CaptureValueAction.Inner_JsonUtil line) {
      return this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(this.init_TaskQueue_DataUtil_TaskQueue_JsonUtil(line));
   }

   public CaptureValueAction.Inner_JsonUtil init_TaskQueue_JsonUtil_Minecraft(Minecraft mc) {
      return this.init(mc, this.scoreboardRow, this.scoreboardRowIndex, this.scoreboardObjective);
   }

   public CaptureValueAction.Inner_JsonUtil init(Minecraft mc, String preferredKey, int preferredRow, String preferredObjective) {
      List<CaptureValueAction.Inner_JsonUtil> lines = init_List_Minecraft_2(mc);
      if (lines.isEmpty()) {
         return null;
      }

      String key = preferredKey == null ? "" : preferredKey;

      for (CaptureValueAction.Inner_JsonUtil line : lines) {
         if (!key.isBlank() && key.equals(line.init_Str())) {
            return line;
         }
      }

      CaptureValueAction.Inner_JsonUtil rowCandidate = null;

      for (CaptureValueAction.Inner_JsonUtil line : lines) {
         if (init_Z_TaskQueue_JsonUtil_Str(line, preferredObjective) && line.init_I() == preferredRow) {
            rowCandidate = line;
            break;
         }
      }

      if (rowCandidate != null && this.init_TaskQueue_NetHelpe_TaskQueue_JsonUtil(rowCandidate).init_Z()) {
         return rowCandidate;
      }

      CaptureValueAction.Inner_JsonUtil best = this.init(lines, preferredRow, preferredObjective, true);
      return best != null ? best : this.init(lines, preferredRow, preferredObjective, false);
   }

   private CaptureValueAction.Inner_JsonUtil init(List<CaptureValueAction.Inner_JsonUtil> lines, int preferredRow, String preferredObjective, boolean requireObjective) {
      CaptureValueAction.Inner_JsonUtil best = null;
      int bestDistance = 2147483647;

      for (CaptureValueAction.Inner_JsonUtil line : lines) {
         if ((!requireObjective || init_Z_TaskQueue_JsonUtil_Str(line, preferredObjective)) && this.init_TaskQueue_NetHelpe_TaskQueue_JsonUtil(line).init_Z()) {
            int distance = preferredRow < 0 ? line.init_I() : Math.abs(line.init_I() - preferredRow);
            if (best == null || distance < bestDistance) {
               best = line;
               bestDistance = distance;
            }
         }
      }

      return best;
   }

   private static boolean init_Z_TaskQueue_JsonUtil_Str(CaptureValueAction.Inner_JsonUtil line, String objective) {
      return line != null && (objective == null || objective.isBlank() || objective.equals(line.load()));
   }

   public static List<CaptureValueAction.Inner_JsonUtil> init_List_Minecraft_2(Minecraft mc) {
      if (mc != null && mc.level != null && mc.player != null) {
         Scoreboard scoreboard = mc.level.getScoreboard();
         Objective objective = init_Objective_Scoreboard_Minecraft(scoreboard, mc);
         if (objective == null) {
            return List.of();
         }

         NumberFormat scoreFormat = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);
         List<PlayerScoreEntry> entries = scoreboard.listPlayerScores(objective)
            .stream()
            .filter(entryx -> !entryx.isHidden())
            .sorted(init_Comparator)
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

   private static Objective init_Objective_Scoreboard_Minecraft(Scoreboard scoreboard, Minecraft mc) {
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

   public CaptureValueAction.Inner_NetHelper init_TaskQueue_NetHelpe() {
      return this.exampleText != null && !this.exampleText.isBlank()
         ? this.init_TaskQueue_NetHelpe_TaskQueue_DataUtil(new CaptureValueAction.Inner_DataUtil(this.exampleText, l.init_l_Obj(this.exampleText)))
         : CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("Enter an example message");
   }

   private CaptureValueAction.Inner_NetHelper init_TaskQueue_NetHelpe_TaskQueue_DataUtil(CaptureValueAction.Inner_DataUtil captured) {
      if (captured != null && captured.init_l != null) {
         Map<String, l> additions = new LinkedHashMap<>();
         if (this.init_Z_6()) {
            additions = new LinkedHashMap<>(init(additions, this.saveAs, captured.init_l));
            return additions.isEmpty()
               ? CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("Enter a variable name")
               : new CaptureValueAction.Inner_NetHelper(true, captured.init_Str, additions, "Ready");
         }

         if (this.matchMode != UtilCore.Inner_DataUtil.MATCH && this.pattern != null && !this.pattern.isBlank()) {
            Optional<UtilCore.Inner_StreamUtil> result = UtilCore.init(this.matchMode, this.pattern, captured.init_Str);
            if (result.isEmpty()) {
               return CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("Pattern does not match");
            }

            additions.putAll(result.get().init_Map());
         } else if (this.pattern != null
            && !this.pattern.isBlank()
            && !captured.init_Str.toLowerCase(Locale.ROOT).contains(this.pattern.toLowerCase(Locale.ROOT))) {
            return CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("Text does not match");
         }

         additions = new LinkedHashMap<>(init(additions, this.saveAs, captured.init_l));
         if (this.init_TaskQueue_BufferHe() != CaptureValueAction.Inner_BufferHelper.OFF) {
            additions = new LinkedHashMap<>(init(additions, this.saveAs, this.init_TaskQueue_BufferHe()));
         }

         CaptureValueAction.Inner_ConfigStore modification = this.init_TaskQueue_ConfigSt_Map(additions);
         if (!modification.init_Z()) {
            return CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str(modification.init_Str());
         }

         additions = new LinkedHashMap<>(modification.init_Map());
         return additions.isEmpty()
            ? CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("Enter a variable name")
            : new CaptureValueAction.Inner_NetHelper(true, captured.init_Str, additions, "Ready");
      } else {
         return CaptureValueAction.Inner_NetHelper.init_TaskQueue_NetHelpe_Str("Nothing available now");
      }
   }

   public static boolean init_Z_TaskQueue_StateCac(CaptureValueAction.Inner_StateCache source) {
      return source == CaptureValueAction.Inner_StateCache.RECENT_CHAT;
   }

   public String init_Str_4() {
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

   private CaptureValueAction.Inner_BufferHelper init_TaskQueue_BufferHe() {
      return this.numberMode == null ? CaptureValueAction.Inner_BufferHelper.OFF : this.numberMode;
   }

   private j.Inner_DataUtil init_j_DataUtil() {
      return this.init_TaskQueue_BufferHe() == CaptureValueAction.Inner_BufferHelper.DROP_CENTS ? j.Inner_DataUtil.WHOLE : j.Inner_DataUtil.AUTO;
   }

   private CaptureValueAction.Inner_ConfigStore init_TaskQueue_ConfigSt_Map(Map<String, l> values) {
      String error = this.init_Str_4();
      if (!error.isBlank()) {
         return CaptureValueAction.Inner_ConfigStore.init(error);
      }

      CaptureValueAction.Inner_TaskQueue modifier = this.numberModifier == null ? CaptureValueAction.Inner_TaskQueue.NONE : this.numberModifier;
      if (modifier != CaptureValueAction.Inner_TaskQueue.NONE && values != null && !values.isEmpty()) {
         BigDecimal operand = BigDecimal.valueOf(this.numberModifierAmount);
         String outputName = m.load(this.saveAs);
         boolean targetTracked = !outputName.isBlank() && values.containsKey(outputName);
         Map<String, l> modified = new LinkedHashMap<>();
         boolean ok = false;

         for (Entry<String, l> entry : values.entrySet()) {
            boolean target = targetTracked && entry.getKey().equals(outputName);
            l original = entry.getValue();
            l result = init(original, modifier, operand, target, this.init_j_DataUtil());
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

   private static l init(l value, CaptureValueAction.Inner_TaskQueue modifier, BigDecimal operand, boolean embedded, j.Inner_DataUtil style) {
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
               var10000 = load(number, BigDecimal.ONE.add(init_BigDecimal_BigDecimal(operand)));
               break;
            case MINUS_PERCENT:
               var10000 = load(number, BigDecimal.ONE.subtract(init_BigDecimal_BigDecimal(operand)));
               break;
            default:
               throw new MatchException(null, null);
         }

         BigDecimal result = var10000;
         return init_l_l_BigDecimal(value, result);
      } catch (ArithmeticException ignored) {
         return value;
      }
   }

   private static BigDecimal init_BigDecimal_BigDecimal_BigDecimal(BigDecimal number, BigDecimal operand) {
      BigDecimal exact = number.divide(operand, new MathContext(Math.max(16, number.precision() + 8), RoundingMode.HALF_UP));
      return read(exact, number);
   }

   public static BigDecimal init_BigDecimal_BigDecimal(BigDecimal percent) {
      return percent.divide(init_BigDecimal, new MathContext(34, RoundingMode.HALF_UP));
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

   private static BigDecimal init_BigDecimal(l value, boolean embedded, j.Inner_DataUtil style) {
      if (value != null && value.init_l_DataUtil() != l.Inner_DataUtil.SLOT && value.init_l_DataUtil() != l.Inner_DataUtil.IDENTIFIER) {
         try {
            return new BigDecimal(embedded ? j.init(value.init_Str(), style) : j.load(value.init_Str(), style));
         } catch (IllegalArgumentException ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   static Map<String, l> init(Map<String, l> patternValues, String saveAs, l sourceValue) {
      Map<String, l> outputs = new LinkedHashMap<>();
      if (patternValues != null) {
         outputs.putAll(patternValues);
      }

      String outputName = m.load(saveAs);
      if (!outputName.isBlank() && sourceValue != null) {
         outputs.putIfAbsent(outputName, sourceValue);
      }

      return outputs;
   }

   static Map<String, l> init_Map_Map(Map<String, l> values) {
      return init(values, "", CaptureValueAction.Inner_BufferHelper.SUFFIX_KMB);
   }

   static Map<String, l> init_Map_Map_Str(Map<String, l> values, String saveAs) {
      return init(values, saveAs, CaptureValueAction.Inner_BufferHelper.SUFFIX_KMB);
   }

   static Map<String, l> init(Map<String, l> values, String saveAs, CaptureValueAction.Inner_BufferHelper mode) {
      if (values != null && !values.isEmpty()) {
         j.Inner_DataUtil style = mode == CaptureValueAction.Inner_BufferHelper.DROP_CENTS ? j.Inner_DataUtil.WHOLE : j.Inner_DataUtil.AUTO;
         String outputName = m.load(saveAs);
         Map<String, l> normalized = new LinkedHashMap<>();
         values.forEach((name, value) -> normalized.put(name, init_l(value, !outputName.isBlank() && outputName.equals(name), style)));
         return normalized;
      } else {
         return Map.of();
      }
   }

   private static l init_l(l value, boolean embedded, j.Inner_DataUtil style) {
      BigDecimal number = init_BigDecimal(value, embedded, style);
      return number == null ? value : init_l_l_BigDecimal(value, number);
   }

   private static l init_l_l_BigDecimal(l source, BigDecimal number) {
      return source.init_Map().isEmpty() ? l.init_l_Number(number) : l.init(source.init_l_DataUtil(), j.init(number), source.init_Map());
   }

   private CaptureValueAction.Inner_DataUtil init_TaskQueue_DataUtil_Minecraft(Minecraft mc) {
      CaptureValueAction.Inner_DataUtil var10000;
      switch (this.source == null ? CaptureValueAction.Inner_StateCache.GUI_TITLE : this.source) {
         case GUI_TITLE:
            Screen screen = mc.gui.screen();
            if (screen != null && !f.init_Z(screen)) {
               String title = screen.getTitle() == null ? "" : screen.getTitle().getString();
               var10000 = new CaptureValueAction.Inner_DataUtil(
                  title, l.init(l.Inner_DataUtil.GUI, title, Map.of("title", l.init_l_Obj(title), "type", l.init_l_Obj(f.init_Str(screen))))
               );
            } else {
               CustomMenuSnapshot menu = autismclient.util.b.CustomMenuTracker.init_CustomMenuSnapshot();
               if (menu == null) {
                  var10000 = null;
               } else {
                  String title = menu.title();
                  var10000 = new CaptureValueAction.Inner_DataUtil(
                     title, l.init(l.Inner_DataUtil.GUI, title, Map.of("title", l.init_l_Obj(title), "type", l.init_l_Obj("CUSTOM_MENU")))
                  );
               }
            }
            break;
         case RECENT_CHAT:
            List<MacroExecutor.Inner_PacketUtil> messages = MacroExecutor.load_List();
            var10000 = messages.isEmpty() ? null : this.init_TaskQueue_DataUtil_MacroExecutor_Pack(messages.get(0));
            break;
         case SCOREBOARD:
            var10000 = this.init_TaskQueue_DataUtil_TaskQueue_JsonUtil(this.init_TaskQueue_JsonUtil_Minecraft(mc));
            break;
         case GUI_ITEM:
            var10000 = this.init_TaskQueue_DataUtil_Minecraft_Z(mc, false);
            break;
         case PLAYER_ITEM:
            var10000 = this.read_TaskQueue_DataUtil(mc);
            break;
         case CURSOR_ITEM:
            var10000 = mc.player != null && mc.player.containerMenu != null
               ? this.init_TaskQueue_DataUtil_ItemStack_I(mc.player.containerMenu.getCarried(), -1)
               : null;
            break;
         case HELD_ITEM:
            var10000 = mc.player == null
               ? null
               : this.init_TaskQueue_DataUtil_ItemStack_I(mc.player.getMainHandItem(), mc.player.getInventory().getSelectedSlot());
            break;
         case COMMAND_AUTOFILL:
         case NAMESCRAPE:
            var10000 = null;
            break;
         case TABLIST:
            var10000 = this.load_TaskQueue_DataUtil_Minecraft(mc);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private CaptureValueAction.Inner_DataUtil load_TaskQueue_DataUtil_Minecraft(Minecraft mc) {
      List<bO.Inner_StreamUtil> players = bO.init(mc);
      List<String> names = new ArrayList<>(players.size() + 1);
      Map<String, String> prefixes = new HashMap<>();

      for (bO.Inner_StreamUtil player : players) {
         names.add(player.init_Str());
         if (player.init_Z()) {
            prefixes.put(player.init_Str(), player.load());
         }
      }

      if (!this.excludeSelf && mc.player != null && mc.player.getGameProfile() != null) {
         names.add(mc.player.getGameProfile().name());
         names.sort(String.CASE_INSENSITIVE_ORDER);
      }

      return this.init_TaskQueue_DataUtil_List_Map(names, prefixes);
   }

   public List<String> init_List_List(List<String> candidates) {
      if (candidates == null) {
         return List.of();
      }

      List<String> pool = ConfigStore.init(candidates, this.listFilter, this.listFilterText);
      return ConfigStore.init(pool, this.listExcludeText);
   }

   private CaptureValueAction.Inner_DataUtil init_TaskQueue_DataUtil_List_Map(List<String> candidates, Map<String, String> prefixByValue) {
      if (candidates == null) {
         return null;
      }

      List<String> pool = this.init_List_List(candidates);
      ConfigStore.Inner_BufferHelper state = MacroExecutor.init_ConfigStore_Buffer_MacroAction(this);
      Optional<ConfigStore.Inner_StreamUtil> pick = ConfigStore.init(pool, this.listSelection, this.listPickPosition, state);
      if (pick.isEmpty()) {
         return null;
      }

      ConfigStore.Inner_StreamUtil picked = pick.get();
      String value = this.listStripPrefix ? ConfigStore.init(picked.init_Str(), this.listFilter, this.listFilterText) : picked.init_Str();
      Map<String, l> props = new LinkedHashMap<>();
      props.put("index", l.init_l_Number(picked.init_I() + 1));
      props.put("count", l.init_l_Number(pool.size()));
      props.put("total", l.init_l_Number(candidates.size()));
      props.put("list", l.init_l_Obj(String.join(", ", pool)));
      String prefix = prefixByValue == null ? null : prefixByValue.get(picked.init_Str());
      if (prefix != null && !prefix.isBlank()) {
         props.put("prefix", l.init_l_Obj(prefix));
      }

      return new CaptureValueAction.Inner_DataUtil(value, l.init(l.Inner_DataUtil.TEXT, value, props));
   }

   private CaptureValueAction.Inner_DataUtil init_TaskQueue_DataUtil_MacroExecutor_Pack(MacroExecutor.Inner_PacketUtil chat) {
      if (chat == null) {
         return null;
      }

      String display = chat.read() == null ? "" : chat.read();
      Map<String, l> props = new LinkedHashMap<>();
      props.put("sender", l.init_l_Obj(chat.init_Str()));
      props.put("message", l.init_l_Obj(chat.load()));
      props.put("display", l.init_l_Obj(display));
      props.put("source", l.init_l_Obj(chat.init_MacroExecutor_Task() == null ? "" : chat.init_MacroExecutor_Task().name()));
      return new CaptureValueAction.Inner_DataUtil(display, l.init(l.Inner_DataUtil.TEXT, display, props));
   }

   private CaptureValueAction.Inner_DataUtil init_TaskQueue_DataUtil_MacroExecutor_Hash(MacroExecutor.Inner_HashUtil gui) {
      if (gui == null) {
         return null;
      }

      String title = gui.init_Str() == null ? "" : gui.init_Str();
      return new CaptureValueAction.Inner_DataUtil(
         title, l.init(l.Inner_DataUtil.GUI, title, Map.of("title", l.init_l_Obj(title), "type", l.init_l_Obj(gui.load() == null ? "" : gui.load())))
      );
   }

   private CaptureValueAction.Inner_DataUtil init_TaskQueue_DataUtil_TaskQueue_JsonUtil(CaptureValueAction.Inner_JsonUtil line) {
      if (line == null) {
         return null;
      }

      Map<String, l> props = new LinkedHashMap<>();
      props.put("line", l.init_l_Obj(line.flush()));
      props.put("name", l.init_l_Obj(line.update()));
      props.put("score", l.init_l_Obj(line.sync()));
      props.put("row", l.init_l_Number(BigDecimal.valueOf(line.init_I() + 1L)));
      props.put("title", l.init_l_Obj(line.read()));
      props.put("objective", l.init_l_Obj(line.load()));
      return new CaptureValueAction.Inner_DataUtil(line.flush(), l.init(l.Inner_DataUtil.TEXT, line.flush(), props));
   }

   private CaptureValueAction.Inner_DataUtil init_TaskQueue_DataUtil_Minecraft_Z(Minecraft mc, boolean playerOnly) {
      if (mc.player != null && mc.player.containerMenu != null) {
         CaptureValueAction.Inner_StreamUtil filterResolution = this.init_TaskQueue_StreamUt_Minecraft(mc);
         if (!filterResolution.init_Z()) {
            return null;
         }

         ArrayUtil filter = filterResolution.init_ArrayUtil();

         for (Slot candidate : mc.player.containerMenu.slots) {
            if (candidate != null && !candidate.getItem().isEmpty()) {
               boolean playerSlot = autismclient.util.aw.init_Z(mc, candidate);
               if (playerOnly == playerSlot) {
                  int visible = autismclient.util.aw.init_I(mc, candidate.index);
                  if ((this.slot < 0 || this.slot == visible || this.slot == candidate.index)
                     && (filter == null || !filter.update() || filter.init_I_ItemStack_I(candidate.getItem(), visible) >= 0)) {
                     return this.init_TaskQueue_DataUtil_ItemStack_I(candidate.getItem(), visible);
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
      CaptureValueAction.Inner_DataUtil menu = this.init_TaskQueue_DataUtil_Minecraft_Z(mc, true);
      if (menu == null && mc.player != null) {
         CaptureValueAction.Inner_StreamUtil filterResolution = this.init_TaskQueue_StreamUt_Minecraft(mc);
         if (!filterResolution.init_Z()) {
            return null;
         }

         ArrayUtil filter = filterResolution.init_ArrayUtil();

         for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && (this.slot < 0 || this.slot == i) && (filter == null || !filter.update() || filter.init_I_ItemStack_I(stack, i) >= 0)) {
               return this.init_TaskQueue_DataUtil_ItemStack_I(stack, i);
            }
         }

         return null;
      } else {
         return menu;
      }
   }

   private CaptureValueAction.Inner_StreamUtil init_TaskQueue_StreamUt_Minecraft(Minecraft mc) {
      if (this.itemFilter != null && !this.itemFilter.isBlank()) {
         j.Inner_CachePool resolution = n.init_j_CachePool_Str_Minecraft(this.itemFilter, mc);
         return resolution.init_Z()
            ? new CaptureValueAction.Inner_StreamUtil(true, ArrayUtil.update(resolution.init_Str()))
            : new CaptureValueAction.Inner_StreamUtil(false, null);
      } else {
         return new CaptureValueAction.Inner_StreamUtil(true, null);
      }
   }

   private CaptureValueAction.Inner_DataUtil init_TaskQueue_DataUtil_ItemStack_I(ItemStack stack, int visibleSlot) {
      if (stack != null && !stack.isEmpty()) {
         Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
         String itemId = id == null ? "" : id.toString();
         String name = stack.getHoverName().getString();
         ItemLore lore = stack.get(DataComponents.LORE);
         String loreText = lore == null ? "" : lore.lines().stream().map(line -> line.getString()).reduce((a, b) -> a + "\n" + b).orElse("");
         Map<String, l> props = new LinkedHashMap<>();
         props.put("name", l.init_l_Obj(name));
         props.put("id", l.init_l_Str(itemId));
         props.put("count", l.init_l_Number(stack.getCount()));
         props.put("slot", l.init_l_I(visibleSlot));
         props.put("lore", l.init_l_Obj(loreText));
         if (lore != null) {
            for (int i = 0; i < lore.lines().size(); i++) {
               props.put("lore" + (i + 1), l.init_l_Obj(lore.lines().get(i).getString()));
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
         return new CaptureValueAction.Inner_DataUtil(text, l.init(l.Inner_DataUtil.ITEM, text, props));
      } else {
         return null;
      }
   }

   private void init_V_9() {
      if (this.saveAs != null && !this.saveAs.isBlank()) {
         n.init_V_Str(this.saveAs);
      }

      for (String name : UtilCore.init(this.matchMode, this.pattern)) {
         n.init_V_Str(name);
      }
   }

   public static String init_Str_Str_5(String example) {
      if (example != null && !example.isBlank()) {
         Matcher matcher = init_Pattern.matcher(example);
         return matcher.find() ? matcher.group().trim() : "";
      } else {
         return "";
      }
   }

   public static String init(String example, String selected, String variableName) {
      if (example != null && selected != null && !selected.isBlank()) {
         String root = m.load(variableName);
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

         String before = load_Str_Str(example.substring(0, index));
         String after = load_Str_Str(example.substring(index + selected.length()));
         return before + "{" + root + "}" + after;
      } else {
         return "";
      }
   }

   private static String load_Str_Str(String value) {
      return value == null ? "" : value.replace("{", "{{").replace("}", "}}");
   }

   @Override
   public StreamGate getType() {
      return StreamGate.CAPTURE_VALUE;
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
      return this.init_Z;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.init_Z = enabled;
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
      tag.putString("matchMode", (this.matchMode == null ? UtilCore.Inner_DataUtil.MATCH : this.matchMode).name());
      tag.putString("pattern", this.pattern == null ? "" : this.pattern);
      tag.putString("exampleText", this.exampleText == null ? "" : this.exampleText);
      tag.putString("selectedText", this.selectedText == null ? "" : this.selectedText);
      tag.putString("itemFilter", this.itemFilter == null ? "" : this.itemFilter);
      tag.putString("scoreboardRow", this.scoreboardRow == null ? "" : this.scoreboardRow);
      tag.putInt("scoreboardRowIndex", this.scoreboardRowIndex);
      tag.putString("scoreboardObjective", this.scoreboardObjective == null ? "" : this.scoreboardObjective);
      tag.putInt("slot", this.slot);
      tag.putString("itemText", (this.itemText == null ? CaptureValueAction.Inner_CachePool.NAME : this.itemText).name());
      tag.putString("numberMode", this.init_TaskQueue_BufferHe().name());
      tag.putString("numberModifier", (this.numberModifier == null ? CaptureValueAction.Inner_TaskQueue.NONE : this.numberModifier).name());
      tag.putDouble("numberModifierAmount", this.numberModifierAmount);
      tag.putBoolean("waitForTrigger", this.waitForTrigger);
      tag.putString("autofillCommand", this.autofillCommand == null ? "" : this.autofillCommand);
      tag.putInt("autofillTimeoutMs", this.autofillTimeoutMs);
      tag.putBoolean("autofillCacheList", this.autofillCacheList);
      tag.putString("listSelection", (this.listSelection == null ? ConfigStore.Inner_CachePool.RANDOM : this.listSelection).name());
      tag.putString("listFilter", (this.listFilter == null ? ConfigStore.Inner_DataUtil.NONE : this.listFilter).name());
      tag.putString("listFilterText", this.listFilterText == null ? "" : this.listFilterText);
      tag.putString("listExcludeText", this.listExcludeText == null ? "" : this.listExcludeText);
      tag.putInt("listPickPosition", this.listPickPosition);
      tag.putBoolean("listStripPrefix", this.listStripPrefix);
      tag.putBoolean("excludeSelf", this.excludeSelf);
      tag.putBoolean("enabled", this.init_Z);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.source = i.init(CaptureValueAction.Inner_StateCache.class, tag.getStringOr("source", "GUI_TITLE"), CaptureValueAction.Inner_StateCache.GUI_TITLE);
      this.saveAs = tag.getStringOr("saveAs", "value");
      this.matchMode = i.init(UtilCore.Inner_DataUtil.class, tag.getStringOr("matchMode", "MATCH"), UtilCore.Inner_DataUtil.MATCH);
      this.pattern = tag.getStringOr("pattern", "");
      this.exampleText = tag.getStringOr("exampleText", "");
      this.selectedText = tag.getStringOr("selectedText", "");
      this.itemFilter = tag.getStringOr("itemFilter", "");
      this.scoreboardRow = tag.getStringOr("scoreboardRow", "");
      this.scoreboardRowIndex = tag.getIntOr("scoreboardRowIndex", -1);
      this.scoreboardObjective = tag.getStringOr("scoreboardObjective", "");
      this.slot = tag.getIntOr("slot", -1);
      this.itemText = i.init(CaptureValueAction.Inner_CachePool.class, tag.getStringOr("itemText", "NAME"), CaptureValueAction.Inner_CachePool.NAME);
      this.numberMode = tag.contains("numberMode")
         ? i.init(CaptureValueAction.Inner_BufferHelper.class, tag.getStringOr("numberMode", "OFF"), CaptureValueAction.Inner_BufferHelper.OFF)
         : (tag.getBooleanOr("normalizeNumbers", false) ? CaptureValueAction.Inner_BufferHelper.SUFFIX_KMB : CaptureValueAction.Inner_BufferHelper.OFF);
      this.numberModifier = i.init(CaptureValueAction.Inner_TaskQueue.class, tag.getStringOr("numberModifier", "NONE"), CaptureValueAction.Inner_TaskQueue.NONE);
      this.numberModifierAmount = tag.getDoubleOr("numberModifierAmount", 1.0);
      this.waitForTrigger = tag.getBooleanOr("waitForTrigger", init_Z_TaskQueue_StateCac(this.source));
      this.autofillCommand = tag.getStringOr("autofillCommand", "");
      this.autofillTimeoutMs = tag.getIntOr("autofillTimeoutMs", 5000);
      this.autofillCacheList = tag.getBooleanOr("autofillCacheList", true);
      this.listSelection = i.init(ConfigStore.Inner_CachePool.class, tag.getStringOr("listSelection", "RANDOM"), ConfigStore.Inner_CachePool.RANDOM);
      this.listFilter = i.init(ConfigStore.Inner_DataUtil.class, tag.getStringOr("listFilter", "NONE"), ConfigStore.Inner_DataUtil.NONE);
      this.listFilterText = tag.getStringOr("listFilterText", "");
      this.listExcludeText = tag.getStringOr("listExcludeText", "");
      this.listPickPosition = tag.getIntOr("listPickPosition", 1);
      this.listStripPrefix = tag.getBooleanOr("listStripPrefix", false);
      this.excludeSelf = tag.getBooleanOr("excludeSelf", true);
      this.init_Z = tag.getBooleanOr("enabled", true);
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

   private record Inner_ConfigStore(boolean init_Z, Map<String, l> init_Map, String init_Str) {
      private static CaptureValueAction.Inner_ConfigStore init(Map<String, l> values) {
         return new CaptureValueAction.Inner_ConfigStore(true, values == null ? Map.of() : values, "");
      }

      private static CaptureValueAction.Inner_ConfigStore init(String message) {
         return new CaptureValueAction.Inner_ConfigStore(false, Map.of(), message == null ? "Invalid number modifier" : message);
      }
   }

   private record Inner_DataUtil(String init_Str, l init_l) {
   }

   public record Inner_JsonUtil(String init_Str, int init_I, String load, String read, String write, String update, String sync, String flush) {
   }

   public record Inner_NetHelper(boolean init_Z, String init_Str, Map<String, l> init_Map, String load) {
      public Inner_NetHelper {
         init_Str = init_Str == null ? "" : init_Str;
         init_Map = init_Map == null ? Map.of() : Map.copyOf(init_Map);
         load = load == null ? "" : load;
      }

      public static CaptureValueAction.Inner_NetHelper init_TaskQueue_NetHelpe_Str(String message) {
         return new CaptureValueAction.Inner_NetHelper(false, "", Map.of(), message);
      }

      public String init_Str_Str(String variableName) {
         String clean = m.load(variableName);
         l value = clean.isBlank() ? null : this.init_Map.get(clean);
         return value == null ? "" : value.init_Str();
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

   private record Inner_StreamUtil(boolean init_Z, ArrayUtil init_ArrayUtil) {
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
