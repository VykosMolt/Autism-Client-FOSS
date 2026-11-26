package autismclient.util.macro;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class PayAction implements MacroAction {
   public String commandTemplate = "/pay <player> <amount>";
   public String amountInput = "1";
   public int delayMs = 1000;
   public boolean delayEnabled = true;
   public boolean divideEnabled = false;
   public boolean probeHidden = true;
   public String confirmMacro = "";
   public List<String> players = new ArrayList<>();
   private boolean init = true;
   public static final int CONFIRM_TIMEOUT_MS = 15000;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("commandTemplate", this.commandTemplate);
      tag.putString("amountInput", this.amountInput);
      tag.putInt("delayMs", init_I_I(this.delayMs));
      tag.putBoolean("delayEnabled", this.delayEnabled);
      tag.putBoolean("divideEnabled", this.divideEnabled);
      tag.putBoolean("probeHidden", this.probeHidden);
      tag.putString("confirmMacro", this.confirmMacro == null ? "" : this.confirmMacro);
      ListTag list = new ListTag();

      for (String player : this.players) {
         if (player != null && !player.isBlank()) {
            list.add(StringTag.valueOf(player));
         }
      }

      tag.put("players", list);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("commandTemplate")) {
         this.commandTemplate = tag.getStringOr("commandTemplate", this.commandTemplate);
      }

      if (tag.contains("amountInput")) {
         this.amountInput = tag.getStringOr("amountInput", this.amountInput);
      }

      if (tag.contains("delayMs")) {
         this.delayMs = init_I_I(tag.getIntOr("delayMs", 1000));
      }

      if (tag.contains("delayEnabled")) {
         this.delayEnabled = tag.getBooleanOr("delayEnabled", true);
      }

      if (tag.contains("divideEnabled")) {
         this.divideEnabled = tag.getBooleanOr("divideEnabled", false);
      }

      if (tag.contains("probeHidden")) {
         this.probeHidden = tag.getBooleanOr("probeHidden", true);
      }

      this.confirmMacro = tag.getStringOr("confirmMacro", "");
      this.players.clear();
      if (tag.contains("players")) {
         for (Tag element : tag.getList("players").orElse(new ListTag())) {
            String value = element.asString().orElse("").trim();
            if (!value.isEmpty() && !this.players.contains(value)) {
               this.players.add(value);
            }
         }
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.PAY;
   }

   @Override
   public String getDisplayName() {
      String target = this.players.isEmpty() ? "no players" : (this.players.size() == 1 ? this.players.get(0) : this.players.size() + " players");
      String amount = this.amountInput != null && !this.amountInput.isBlank() ? this.amountInput.trim() : "?";
      return "Pay " + target + (this.divideEnabled ? " split " : " ") + amount;
   }

   @Override
   public String getIcon() {
      return "$";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.init = enabled;
   }

   public long init() {
      return init_J_Str(this.amountInput);
   }

   public long load() {
      return this.init() * Math.max(0, this.players.size());
   }

   public static long[] init(long total, int count) {
      if (count <= 0) {
         return new long[0];
      }

      long safeTotal = Math.max(0L, total);
      long base = safeTotal / count;
      long remainder = safeTotal - base * count;
      long[] amounts = new long[count];

      for (int i = 0; i < count; i++) {
         amounts[i] = base + (i < remainder ? 1L : 0L);
      }

      return amounts;
   }

   public static int init_I_I(int delayMs) {
      return Math.max(0, delayMs);
   }

   public static long init_J_Str(String input) {
      if (input == null) {
         return 0L;
      }

      String normalized = input.trim().replace(" ", "").replace("_", "");
      if (normalized.isEmpty()) {
         return 0L;
      }

      char suffix = Character.toUpperCase(normalized.charAt(normalized.length() - 1));
      BigDecimal multiplier = BigDecimal.ONE;
      if (suffix == 'K' || suffix == 'M' || suffix == 'B') {
         normalized = normalized.substring(0, normalized.length() - 1);
         BigDecimal var10000;
         switch (suffix) {
            case 'B':
               var10000 = new BigDecimal("1000000000");
               break;
            case 'K':
               var10000 = new BigDecimal("1000");
               break;
            case 'M':
               var10000 = new BigDecimal("1000000");
               break;
            default:
               var10000 = BigDecimal.ONE;
         }

         multiplier = var10000;
      }

      if (normalized.contains(",") && normalized.contains(".")) {
         normalized = normalized.replace(",", "");
      } else if (normalized.contains(",")) {
         normalized = normalized.replace(',', '.');
      }

      if (!normalized.isEmpty() && !normalized.equals(".")) {
         try {
            BigDecimal value = new BigDecimal(normalized);
            return value.multiply(multiplier).setScale(0, RoundingMode.HALF_UP).longValue();
         } catch (NumberFormatException e) {
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   public static boolean init(String macroName, BooleanSupplier cancelled) {
      if (macroName == null || macroName.isBlank()) {
         return true;
      }

      if (MacroExecutor.isMacroNameRunning(macroName)) {
         return true;
      }

      autismclient.util.AutismMacro macro = autismclient.util.MacroLibraryManager.getInstance().findByName(macroName);
      if (macro == null) {
         return true;
      }

      long runId = macro.executeWithRunId();
      if (runId < 0L) {
         return true;
      }

      long deadline = System.nanoTime() + 15000000000L;

      while (MacroExecutor.isRunActive(runId)) {
         if (cancelled != null && cancelled.getAsBoolean()) {
            return false;
         }

         if (System.nanoTime() >= deadline) {
            break;
         }

         try {
            Thread.sleep(20L);
         } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return false;
         }
      }

      return true;
   }

   public static String init_Str_J(long amount) {
      DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
      DecimalFormat format = new DecimalFormat("#,##0", symbols);
      return format.format(amount);
   }
}
