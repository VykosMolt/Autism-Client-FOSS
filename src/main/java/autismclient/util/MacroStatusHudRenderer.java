package autismclient.util;

import autismclient.util.macro.MacroAction;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.MacroStepColorHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class MacroStatusHudRenderer {
   private static final autismclient.gui.f.b.CharEncoder THEME = new autismclient.gui.f.b.CharEncoder();
   private static final long REFRESH_INTERVAL_NANOS = 50000000L;
   private static final int ACCENT_COLOR = -9448565;
   private static final Identifier STATUS_FONT = THEME.init_Identifier_l(autismclient.gui.f.b.l.write);
   private static final Identifier ACTION_FONT = THEME.init_Identifier_l(autismclient.gui.f.b.l.read);
   private static final Identifier TITLE_FONT = THEME.init_Identifier_l(autismclient.gui.f.b.l.load);
   private static final int STATUS_COLOR = THEME.load(autismclient.gui.f.b.l.write);
   private static final int TITLE_COLOR = THEME.load(autismclient.gui.f.b.l.read);
   private static String[] cachedNames = new String[0];
   private static int[] cachedSteps = new int[0];
   private static int[] cachedTotals = new int[0];
   private static int[] cachedCompletedSteps = new int[0];
   private static int cachedRowsPerRun = -2147483648;
   private static int cachedCurrent = -2147483648;
   private static int cachedTotal = -2147483648;
   private static int cachedScroll = -2147483648;
   private static int cachedVisibleRows = -2147483648;
   private static int cachedPanelWidth = -2147483648;
   private static int cachedThemeGeneration = -2147483648;
   private static String title = null;
   private static String cachedTitle = "";
   private static List<autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil> cachedLines = List.of();
   private static long lastRefreshNanos;
   private static Font cachedFont;
   private static int visibilityPanelWidth = -2147483648;
   private static int visibilityMaxLines = -2147483648;
   private static long visibilityCheckedAt;
   private static boolean cachedVisible;
   private static boolean visibilityCached;

   private MacroStatusHudRenderer() {
   }

   public static void renderAt(GuiGraphicsExtractor context, Font textRenderer, int x, int y, int width, int maxLines) {
      render(context, textRenderer, x - 6, y - 8, width + 12, maxLines, true, true, true);
   }

   public static int layoutHeight(Font textRenderer, int panelWidth, int maxLines) {
      if (textRenderer == null) {
         return 0;
      } else {
         return !isVisibleCached(textRenderer, panelWidth, maxLines) ? 0 : autismclient.gui.ui.layout.InfoCardRenderer.height(cachedLines.size());
      }
   }

   public static int render(
      GuiGraphicsExtractor context, Font textRenderer, int x, int y, int panelWidth, int maxLines, boolean topBorder, boolean bottomBorder, boolean rightBorder
   ) {
      return textRenderer != null && isVisibleCached(textRenderer, panelWidth, maxLines)
         ? autismclient.gui.ui.layout.InfoCardRenderer.render(
            context, textRenderer, x, y, panelWidth, cachedTitle, cachedLines, THEME.run(), 0, true, rightBorder, topBorder, bottomBorder
         )
         : 0;
   }

   private static boolean isVisibleCached(Font textRenderer, int panelWidth, int maxLines) {
      if (visibilityCached && textRenderer == cachedFont && panelWidth == visibilityPanelWidth && maxLines == visibilityMaxLines && System.nanoTime() - visibilityCheckedAt < 8000000L) {
         visibilityCached = false;
         return cachedVisible;
      } else {
         boolean result = computeVisible(textRenderer, panelWidth, maxLines);
         cachedFont = textRenderer;
         visibilityPanelWidth = panelWidth;
         visibilityMaxLines = maxLines;
         visibilityCheckedAt = System.nanoTime();
         cachedVisible = result;
         visibilityCached = true;
         return result;
      }
   }

   private static boolean computeVisible(Font textRenderer, int panelWidth, int maxLines) {
      List<MacroExecutor.ActiveRunSnapshot> runs = MacroExecutor.getActiveRunSnapshots();
      if (runs.isEmpty()) {
         clearCache();
         return false;
      }

      MacroExecutor.ActiveRunSnapshot primary = runs.get(0);
      AutismMacro macro = primary.macro();
      if (macro != null && macro.actions != null && !macro.actions.isEmpty()) {
         int total = actionTotal(primary, macro);
         int current = Math.max(0, primary.currentStep());
         int rowsPerRun = Math.max(2, maxLines / Math.max(1, runs.size()));
         int visibleRows = visibleRowCount(total, rowsPerRun);
         int scroll = scrollStart(current, total, rowsPerRun);
         long now = System.nanoTime();
         boolean runsMatch = runsMatchCache(runs, rowsPerRun);
         if (needsRefresh(runsMatch, current, total, scroll, visibleRows, panelWidth, now)) {
            rebuildCache(textRenderer, runs, rowsPerRun, panelWidth, now);
         }

         return true;
      } else {
         clearCache();
         return false;
      }
   }

   private static int actionTotal(MacroExecutor.ActiveRunSnapshot run, AutismMacro macro) {
      int actionCount = macro != null && macro.actions != null ? macro.actions.size() : 0;
      return Math.max(run.totalSteps(), actionCount);
   }

   private static int visibleRowCount(int total, int rowsPerRun) {
      return Math.min(rowsPerRun, total);
   }

   private static int scrollStart(int current, int total, int rowsPerRun) {
      return total <= rowsPerRun ? 0 : Math.max(0, Math.min(current - rowsPerRun / 2, total - rowsPerRun));
   }

   private static boolean runsMatchCache(List<MacroExecutor.ActiveRunSnapshot> runs, int rowsPerRun) {
      if (rowsPerRun == cachedRowsPerRun && runs.size() == cachedNames.length) {
         for (int i = 0; i < runs.size(); i++) {
            MacroExecutor.ActiveRunSnapshot run = runs.get(i);
            if (run.currentStep() != cachedSteps[i] || run.totalSteps() != cachedTotals[i] || run.lastCompletedStep() != cachedCompletedSteps[i] || !Objects.equals(run.macroName(), cachedNames[i])) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean needsRefresh(boolean runsMatch, int current, int total, int scroll, int visibleRows, int panelWidth, long now) {
      boolean layoutChanged = !runsMatch || cachedTotal != total || cachedVisibleRows != visibleRows || cachedPanelWidth != panelWidth || cachedThemeGeneration != ThemePaletteManager.generation();
      if (layoutChanged) {
         return true;
      } else if (cachedLines.isEmpty()) {
         return true;
      } else {
         return cachedCurrent == current && cachedScroll == scroll ? false : now - lastRefreshNanos >= 50000000L;
      }
   }

   private static int accentColor() {
      return ThemePaletteManager.recolor(-9448565, ThemePaletteManager.ColorRole.ACCENT);
   }

   private static void rebuildCache(Font textRenderer, List<MacroExecutor.ActiveRunSnapshot> runs, int rowsPerRun, int panelWidth, long now) {
      int contentWidth = panelWidth - 12;
      int nowColor = accentColor();
      ArrayList<autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil> rows = new ArrayList<>();

      for (MacroExecutor.ActiveRunSnapshot run : runs) {
         AutismMacro macro = run.macro();
         if (macro != null && macro.actions != null && !macro.actions.isEmpty()) {
            List<MacroAction> actions = macro.actions;
            int total = actionTotal(run, macro);
            int current = Math.max(0, run.currentStep());
            int visibleRows = visibleRowCount(total, rowsPerRun);
            int scroll = scrollStart(current, total, rowsPerRun);
            rows.add(
               new autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil(
                  autismclient.gui.f.b.k.init(
                     textRenderer, run.macroName() + "  " + Math.min(current + 1, total) + "/" + total, contentWidth, TITLE_FONT, TITLE_COLOR
                  ),
                  TITLE_FONT,
                  TITLE_COLOR
               )
            );

            for (int i = scroll; i < scroll + visibleRows && i < total && i < actions.size(); i++) {
               MacroAction action = actions.get(i);
               int color = MacroStepColorHelper.stepColor(i, current, run.lastCompletedStep(), nowColor);
               rows.add(
                  new autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil(
                     autismclient.gui.f.b.k.init(textRenderer, i + 1 + ". " + action.getDisplayName(), contentWidth, ACTION_FONT, color),
                     ACTION_FONT,
                     color
                  )
               );
            }

            if (run.statusText() != null && !run.statusText().isBlank()) {
               rows.add(
                  new autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil(
                     autismclient.gui.f.b.k.init(textRenderer, run.statusText(), contentWidth, STATUS_FONT, STATUS_COLOR), STATUS_FONT, STATUS_COLOR
                  )
               );
            }
         }
      }

      MacroExecutor.ActiveRunSnapshot primary = runs.get(0);
      int runCount = runs.size();
      cachedNames = new String[runCount];
      cachedSteps = new int[runCount];
      cachedTotals = new int[runCount];
      cachedCompletedSteps = new int[runCount];

      for (int i = 0; i < runCount; i++) {
         MacroExecutor.ActiveRunSnapshot run = runs.get(i);
         cachedNames[i] = run.macroName();
         cachedSteps[i] = run.currentStep();
         cachedTotals[i] = run.totalSteps();
         cachedCompletedSteps[i] = run.lastCompletedStep();
      }

      int primaryTotal = actionTotal(primary, primary.macro());
      int primaryCurrent = Math.max(0, primary.currentStep());
      cachedRowsPerRun = rowsPerRun;
      cachedCurrent = primaryCurrent;
      cachedTotal = primaryTotal;
      cachedScroll = scrollStart(primaryCurrent, primaryTotal, rowsPerRun);
      cachedVisibleRows = visibleRowCount(primaryTotal, rowsPerRun);
      cachedPanelWidth = panelWidth;
      cachedThemeGeneration = ThemePaletteManager.generation();
      title = runs.size() == 1 ? primary.macroName() : runs.size() + " macros";
      cachedTitle = autismclient.gui.f.b.k.init(textRenderer, runs.size() == 1 ? "MACRO" : "MACROS", contentWidth, TITLE_FONT, TITLE_COLOR);
      cachedLines = rows;
      lastRefreshNanos = now;
   }

   private static void clearCache() {
      cachedNames = new String[0];
      cachedSteps = new int[0];
      cachedTotals = new int[0];
      cachedCompletedSteps = new int[0];
      cachedRowsPerRun = -2147483648;
      cachedCurrent = -2147483648;
      cachedTotal = -2147483648;
      cachedScroll = -2147483648;
      cachedVisibleRows = -2147483648;
      cachedPanelWidth = -2147483648;
      cachedThemeGeneration = -2147483648;
      title = null;
      cachedTitle = "";
      cachedLines = List.of();
      lastRefreshNanos = 0L;
   }
}
