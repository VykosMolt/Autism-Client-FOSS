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
   private static final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private static final long init_J = 50000000L;
   private static final int init_I = -9448565;
   private static final Identifier init_Identifier = init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write);
   private static final Identifier load_Identifier = init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read);
   private static final Identifier read_Identifier = init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.load);
   private static final int load_I = init_CharEncoder.load(autismclient.gui.f.b.l.write);
   private static final int read_I = init_CharEncoder.load(autismclient.gui.f.b.l.read);
   private static String[] init_StrArr = new String[0];
   private static int[] init_IArr = new int[0];
   private static int[] load_IArr = new int[0];
   private static int[] read_IArr = new int[0];
   private static int write = -2147483648;
   private static int update = -2147483648;
   private static int sync = -2147483648;
   private static int flush = -2147483648;
   private static int close = -2147483648;
   private static int open = -2147483648;
   private static int poll = -2147483648;
   private static String init_Str = null;
   private static String load_Str = "";
   private static List<autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil> init_List = List.of();
   private static long load_J;
   private static Font init_Font;
   private static int push = -2147483648;
   private static int pop = -2147483648;
   private static long read_J;
   private static boolean init_Z;
   private static boolean load_Z;

   private MacroStatusHudRenderer() {
   }

   public static void init(GuiGraphicsExtractor context, Font textRenderer, int x, int y, int width, int maxLines) {
      init(context, textRenderer, x - 6, y - 8, width + 12, maxLines, true, true, true);
   }

   public static int init_I(Font textRenderer, int panelWidth, int maxLines) {
      if (textRenderer == null) {
         return 0;
      } else {
         return !init_Z(textRenderer, panelWidth, maxLines) ? 0 : autismclient.gui.ui.layout.InfoCardRenderer.init(init_List.size());
      }
   }

   public static int init(
      GuiGraphicsExtractor context, Font textRenderer, int x, int y, int panelWidth, int maxLines, boolean topBorder, boolean bottomBorder, boolean rightBorder
   ) {
      return textRenderer != null && init_Z(textRenderer, panelWidth, maxLines)
         ? autismclient.gui.ui.layout.InfoCardRenderer.init(
            context, textRenderer, x, y, panelWidth, load_Str, init_List, init_CharEncoder.run(), 0, true, rightBorder, topBorder, bottomBorder
         )
         : 0;
   }

   private static boolean init_Z(Font textRenderer, int panelWidth, int maxLines) {
      if (load_Z && textRenderer == init_Font && panelWidth == push && maxLines == pop && System.nanoTime() - read_J < 8000000L) {
         load_Z = false;
         return init_Z;
      } else {
         boolean result = load(textRenderer, panelWidth, maxLines);
         init_Font = textRenderer;
         push = panelWidth;
         pop = maxLines;
         read_J = System.nanoTime();
         init_Z = result;
         load_Z = true;
         return result;
      }
   }

   private static boolean load(Font textRenderer, int panelWidth, int maxLines) {
      List<MacroExecutor.ActiveRunSnapshot> runs = MacroExecutor.getActiveRunSnapshots();
      if (runs.isEmpty()) {
         init_V();
         return false;
      }

      MacroExecutor.ActiveRunSnapshot primary = runs.get(0);
      AutismMacro macro = primary.macro();
      if (macro != null && macro.actions != null && !macro.actions.isEmpty()) {
         int total = init(primary, macro);
         int current = Math.max(0, primary.currentStep());
         int rowsPerRun = Math.max(2, maxLines / Math.max(1, runs.size()));
         int visibleRows = init(total, rowsPerRun);
         int scroll = init(current, total, rowsPerRun);
         long now = System.nanoTime();
         boolean runsMatch = init(runs, rowsPerRun);
         if (init(runsMatch, current, total, scroll, visibleRows, panelWidth, now)) {
            init(textRenderer, runs, rowsPerRun, panelWidth, now);
         }

         return true;
      } else {
         init_V();
         return false;
      }
   }

   private static int init(MacroExecutor.ActiveRunSnapshot run, AutismMacro macro) {
      int actionCount = macro != null && macro.actions != null ? macro.actions.size() : 0;
      return Math.max(run.totalSteps(), actionCount);
   }

   private static int init(int total, int rowsPerRun) {
      return Math.min(rowsPerRun, total);
   }

   private static int init(int current, int total, int rowsPerRun) {
      return total <= rowsPerRun ? 0 : Math.max(0, Math.min(current - rowsPerRun / 2, total - rowsPerRun));
   }

   private static boolean init(List<MacroExecutor.ActiveRunSnapshot> runs, int rowsPerRun) {
      if (rowsPerRun == write && runs.size() == init_StrArr.length) {
         for (int i = 0; i < runs.size(); i++) {
            MacroExecutor.ActiveRunSnapshot run = runs.get(i);
            if (run.currentStep() != init_IArr[i] || run.totalSteps() != load_IArr[i] || run.lastCompletedStep() != read_IArr[i] || !Objects.equals(run.macroName(), init_StrArr[i])) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean init(boolean runsMatch, int current, int total, int scroll, int visibleRows, int panelWidth, long now) {
      boolean layoutChanged = !runsMatch || sync != total || close != visibleRows || open != panelWidth || poll != ThemePaletteManager.generation();
      if (layoutChanged) {
         return true;
      } else if (init_List.isEmpty()) {
         return true;
      } else {
         return update == current && flush == scroll ? false : now - load_J >= 50000000L;
      }
   }

   private static int init_I() {
      return ThemePaletteManager.recolor(-9448565, ThemePaletteManager.ColorRole.ACCENT);
   }

   private static void init(Font textRenderer, List<MacroExecutor.ActiveRunSnapshot> runs, int rowsPerRun, int panelWidth, long now) {
      int contentWidth = panelWidth - 12;
      int nowColor = init_I();
      ArrayList<autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil> rows = new ArrayList<>();

      for (MacroExecutor.ActiveRunSnapshot run : runs) {
         AutismMacro macro = run.macro();
         if (macro != null && macro.actions != null && !macro.actions.isEmpty()) {
            List<MacroAction> actions = macro.actions;
            int total = init(run, macro);
            int current = Math.max(0, run.currentStep());
            int visibleRows = init(total, rowsPerRun);
            int scroll = init(current, total, rowsPerRun);
            rows.add(
               new autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil(
                  autismclient.gui.f.b.k.init(
                     textRenderer, run.macroName() + "  " + Math.min(current + 1, total) + "/" + total, contentWidth, read_Identifier, read_I
                  ),
                  read_Identifier,
                  read_I
               )
            );

            for (int i = scroll; i < scroll + visibleRows && i < total && i < actions.size(); i++) {
               MacroAction action = actions.get(i);
               int color = MacroStepColorHelper.init(i, current, run.lastCompletedStep(), nowColor);
               rows.add(
                  new autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil(
                     autismclient.gui.f.b.k.init(textRenderer, i + 1 + ". " + action.getDisplayName(), contentWidth, load_Identifier, color),
                     load_Identifier,
                     color
                  )
               );
            }

            if (run.statusText() != null && !run.statusText().isBlank()) {
               rows.add(
                  new autismclient.gui.ui.layout.InfoCardRenderer.Inner_DataUtil(
                     autismclient.gui.f.b.k.init(textRenderer, run.statusText(), contentWidth, init_Identifier, load_I), init_Identifier, load_I
                  )
               );
            }
         }
      }

      MacroExecutor.ActiveRunSnapshot primary = runs.get(0);
      int runCount = runs.size();
      init_StrArr = new String[runCount];
      init_IArr = new int[runCount];
      load_IArr = new int[runCount];
      read_IArr = new int[runCount];

      for (int i = 0; i < runCount; i++) {
         MacroExecutor.ActiveRunSnapshot run = runs.get(i);
         init_StrArr[i] = run.macroName();
         init_IArr[i] = run.currentStep();
         load_IArr[i] = run.totalSteps();
         read_IArr[i] = run.lastCompletedStep();
      }

      int primaryTotal = init(primary, primary.macro());
      int primaryCurrent = Math.max(0, primary.currentStep());
      write = rowsPerRun;
      update = primaryCurrent;
      sync = primaryTotal;
      flush = init(primaryCurrent, primaryTotal, rowsPerRun);
      close = init(primaryTotal, rowsPerRun);
      open = panelWidth;
      poll = ThemePaletteManager.generation();
      init_Str = runs.size() == 1 ? primary.macroName() : runs.size() + " macros";
      load_Str = autismclient.gui.f.b.k.init(textRenderer, runs.size() == 1 ? "MACRO" : "MACROS", contentWidth, read_Identifier, read_I);
      init_List = rows;
      load_J = now;
   }

   private static void init_V() {
      init_StrArr = new String[0];
      init_IArr = new int[0];
      load_IArr = new int[0];
      read_IArr = new int[0];
      write = -2147483648;
      update = -2147483648;
      sync = -2147483648;
      flush = -2147483648;
      close = -2147483648;
      open = -2147483648;
      poll = -2147483648;
      init_Str = null;
      load_Str = "";
      init_List = List.of();
      load_J = 0L;
   }
}
