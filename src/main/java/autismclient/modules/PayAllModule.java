package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.StringListSetting;
import autismclient.api.module.StringSetting;
import autismclient.util.AutismClientMessaging;
import autismclient.util.OnlinePlayerCollector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class PayAllModule extends Module {
   private volatile int generation;

   public PayAllModule() {
      super("pay-all", "PayAll", ae.write, "Pay every listed player.");
      this.add(new StringListSetting("players", "Players", "").playerRankPicker().build());
      this.add(new BoolSetting("probe", "Probe Hidden", true).description("Include hidden players.").build());
      this.add(new StringSetting("amount", "Amount", "1000").description("Supports K, M, B.").build());
      this.add(new BoolSetting("divide", "Split Total", false).description("Split across players.").build());
      this.add(new IntSetting("delay", "Delay (ms)", 1000, 0, 10000, 100).description("Gap between payments.").build());
      this.add(new StringSetting("command", "Command", "/pay <player> <amount>").description("<player> and <amount>.").build());
      this.add(new StringSetting("confirm-macro", "Confirm Macro", "").macroPicker().description("Run after every pay.").build());
   }

   @Override
   public void onEnable() {
      int generation = ++this.generation;
      autismclient.util.BackgroundTaskExecutor.submit("pay-all", () -> this.runPayments(generation));
   }

   @Override
   public void onDisable() {
      this.generation++;
   }

   @Override
   public void onGameLeft() {
      if (this.isEnabled()) {
         this.setEnabled(false);
      }
   }

   private void runPayments(int generation) {
      try {
         List<String> targets = new ArrayList<>();

         for (String player : this.list("players")) {
            if (player != null) {
               String trimmed = player.trim();
               if (!trimmed.isEmpty() && !targets.contains(trimmed)) {
                  targets.add(trimmed);
               }
            }
         }

         if (MC != null && MC.getConnection() != null) {
            boolean everyone = targets.isEmpty();
            if (everyone) {
               if (this.bool("probe")) {
                  this.say("§7Pay All: scanning players...");
               }

               targets.addAll(OnlinePlayerCollector.collect(MC, this.bool("probe"), () -> this.isStale(generation)));
               if (targets.isEmpty()) {
                  this.say("§cPay All: no players listed or visible.");
                  return;
               }
            }

            long amount = Math.max(0L, autismclient.util.macro.PayAction.parseAmount(this.text("amount")));
            long[] divided = this.bool("divide") ? autismclient.util.macro.PayAction.split(amount, targets.size()) : null;
            int sent = 0;

            for (int i = 0; i < targets.size(); i++) {
               if (this.isStale(generation)) {
                  return;
               }

               String player = targets.get(i);
               long perAmount = divided != null ? divided[i] : amount;
               if (perAmount > 0L) {
                  if (sent > 0 && this.integer("delay") > 0) {
                     Thread.sleep(this.integer("delay"));
                  }

                  if (this.isStale(generation)) {
                     return;
                  }

                  String command = this.commandFor(player, perAmount);
                  if (!command.isEmpty()) {
                     int index = ++sent;
                     int total = targets.size();
                     String label = autismclient.util.macro.PayAction.formatAmount(perAmount);
                     String targetName = player;
                     CountDownLatch sendLatch = new CountDownLatch(1);
                     MC.execute(() -> {
                        try {
                           this.sendPayment(targetName, command, label, index, total);
                        } finally {
                           sendLatch.countDown();
                        }
                     });
                     sendLatch.await(2000L, TimeUnit.MILLISECONDS);
                     if (this.isStale(generation)) {
                        return;
                     }

                     String confirm = this.text("confirm-macro");
                     if (confirm != null && !confirm.isBlank()) {
                        autismclient.util.macro.PayAction.init(confirm, () -> this.isStale(generation));
                     }
                  }
               }
            }

            int finalSent = sent;
            MC.execute(() -> this.say("§aPay All done: §f" + finalSent + " payment" + (finalSent == 1 ? "" : "s") + (everyone ? " §7(everyone)" : "") + "."));
         }
      } catch (InterruptedException interrupted) {
         Thread.currentThread().interrupt();
      } finally {
         this.finishRun(generation);
      }
   }

   private void sendPayment(String player, String command, String label, int index, int total) {
      try {
         if (aD.read_Z() || MC == null || MC.getConnection() == null) {
            return;
         }

         if (command.startsWith("/") && command.length() > 1) {
            MC.getConnection().sendCommand(command.substring(1));
         } else {
            MC.getConnection().sendChat(command);
         }

         this.say("§aPaid §f" + player + " §e" + label + " §7(" + index + "/" + total + ")");
      } catch (Exception e) {
         this.say("§cPay failed: " + e.getMessage());
      }
   }

   private String commandFor(String player, long amount) {
      String template = this.text("command");
      if (template == null || template.isBlank()) {
         template = "/pay <player> <amount>";
      }

      String value = String.valueOf(amount);
      return template.replace("<player>", player).replace("{player}", player).replace("<amount>", value).replace("{amount}", value).trim();
   }

   private boolean isStale(int generation) {
      return generation != this.generation || !this.isEnabled();
   }

   private void finishRun(int generation) {
      if (MC == null) {
         if (generation == this.generation) {
            this.setEnabled(false);
         }
      } else {
         MC.execute(() -> {
            if (generation == this.generation && this.isEnabled()) {
               this.setEnabled(false);
            }
         });
      }
   }

   private void say(String message) {
      AutismClientMessaging.sendPrefixed(message);
   }
}
