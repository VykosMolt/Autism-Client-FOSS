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
   private volatile int init;

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
      int generation = ++this.init;
      autismclient.util.BackgroundTaskExecutor.submit("pay-all", () -> this.init_V(generation));
   }

   @Override
   public void onDisable() {
      this.init++;
   }

   @Override
   public void onGameLeft() {
      if (this.isEnabled()) {
         this.setEnabled(false);
      }
   }

   private void init_V(int generation) {
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
                  this.init("§7Pay All: scanning players...");
               }

               targets.addAll(OnlinePlayerCollector.init(MC, this.bool("probe"), () -> this.init_Z(generation)));
               if (targets.isEmpty()) {
                  this.init("§cPay All: no players listed or visible.");
                  return;
               }
            }

            long amount = Math.max(0L, autismclient.util.macro.PayAction.parseAmount(this.text("amount")));
            long[] divided = this.bool("divide") ? autismclient.util.macro.PayAction.init(amount, targets.size()) : null;
            int sent = 0;

            for (int i = 0; i < targets.size(); i++) {
               if (this.init_Z(generation)) {
                  return;
               }

               String player = targets.get(i);
               long perAmount = divided != null ? divided[i] : amount;
               if (perAmount > 0L) {
                  if (sent > 0 && this.integer("delay") > 0) {
                     Thread.sleep(this.integer("delay"));
                  }

                  if (this.init_Z(generation)) {
                     return;
                  }

                  String command = this.init(player, perAmount);
                  if (!command.isEmpty()) {
                     int index = ++sent;
                     int total = targets.size();
                     String label = autismclient.util.macro.PayAction.formatAmount(perAmount);
                     String targetName = player;
                     CountDownLatch sendLatch = new CountDownLatch(1);
                     MC.execute(() -> {
                        try {
                           this.init(targetName, command, label, index, total);
                        } finally {
                           sendLatch.countDown();
                        }
                     });
                     sendLatch.await(2000L, TimeUnit.MILLISECONDS);
                     if (this.init_Z(generation)) {
                        return;
                     }

                     String confirm = this.text("confirm-macro");
                     if (confirm != null && !confirm.isBlank()) {
                        autismclient.util.macro.PayAction.init(confirm, () -> this.init_Z(generation));
                     }
                  }
               }
            }

            int finalSent = sent;
            MC.execute(() -> this.init("§aPay All done: §f" + finalSent + " payment" + (finalSent == 1 ? "" : "s") + (everyone ? " §7(everyone)" : "") + "."));
         }
      } catch (InterruptedException interrupted) {
         Thread.currentThread().interrupt();
      } finally {
         this.load(generation);
      }
   }

   private void init(String player, String command, String label, int index, int total) {
      try {
         if (aD.read_Z() || MC == null || MC.getConnection() == null) {
            return;
         }

         if (command.startsWith("/") && command.length() > 1) {
            MC.getConnection().sendCommand(command.substring(1));
         } else {
            MC.getConnection().sendChat(command);
         }

         this.init("§aPaid §f" + player + " §e" + label + " §7(" + index + "/" + total + ")");
      } catch (Exception e) {
         this.init("§cPay failed: " + e.getMessage());
      }
   }

   private String init(String player, long amount) {
      String template = this.text("command");
      if (template == null || template.isBlank()) {
         template = "/pay <player> <amount>";
      }

      String value = String.valueOf(amount);
      return template.replace("<player>", player).replace("{player}", player).replace("<amount>", value).replace("{amount}", value).trim();
   }

   private boolean init_Z(int generation) {
      return generation != this.init || !this.isEnabled();
   }

   private void load(int generation) {
      if (MC == null) {
         if (generation == this.init) {
            this.setEnabled(false);
         }
      } else {
         MC.execute(() -> {
            if (generation == this.init && this.isEnabled()) {
               this.setEnabled(false);
            }
         });
      }
   }

   private void init(String message) {
      AutismClientMessaging.sendPrefixed(message);
   }
}
