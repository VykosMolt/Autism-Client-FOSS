package autismclient.ai;

import java.util.Collection;
import java.util.Locale;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AiTaskStatus {
   private static final Pattern init_Pattern = Pattern.compile("\\bfurnace\\s+(\\d+)\\s*/\\s*(\\d+)", 2);
   static final int init_I = 200;
   static final long init_J = 1500000000L;
   private static final AiGoal.StableLabel init_W_StreamUtil = new AiGoal.StableLabel();
   private static final AiGoal.StableLabel load = new AiGoal.StableLabel();

   private AiTaskStatus() {
   }

   static AiTaskStatus.Inner_StreamUtil init(String title, String activity, int completed, int target) {
      String heading = init(title);
      if (heading.isEmpty()) {
         return AiTaskStatus.Inner_StreamUtil.EMPTY;
      }

      String action = init(activity);
      int percent = target > 0 ? (int)(100L * Math.clamp(completed, 0, target) / target) : -1;
      return new AiTaskStatus.Inner_StreamUtil(heading, action.isEmpty() ? "In progress" : action, "", percent);
   }

   public static String init(String diagnostic, String fallback) {
      String phase = load(diagnostic, fallback);
      Matcher furnace = init_Pattern.matcher(init(diagnostic));
      return furnace.find()
            && (
               phase.startsWith("Waiting")
                  || phase.startsWith("Loading")
                  || phase.startsWith("Adding")
                  || phase.startsWith("Opening")
                  || phase.startsWith("Moving")
                  || phase.startsWith("Collecting")
            )
         ? phase + " / furnace " + furnace.group(1) + " of " + furnace.group(2)
         : phase;
   }

   private static String load(String diagnostic, String fallback) {
      String message = init(diagnostic).toLowerCase(Locale.ROOT);
      if (message.isEmpty()) {
         return init(fallback);
      } else if (message.contains("paused")) {
         return "Paused";
      } else if (message.contains("eating") || message.contains("recovering health") || message.contains("low health")) {
         return "Recovering health";
      } else if (message.contains("survival interruption") || message.contains("guard") || message.contains("avoiding danger")) {
         return "Responding to danger";
      } else if (message.contains("waiting for furnace output") || message.contains("batch is cooking")) {
         return "Waiting for smelting";
      } else if (message.contains("clearing obstruction") || message.contains("breaking the way") || message.contains("finishing the route block")) {
         return "Clearing obstruction";
      } else if (message.contains("breaking")) {
         return "Breaking blocks";
      } else if (message.contains("collecting smelted output") || message.contains("collecting existing furnace output")) {
         return "Collecting smelted output";
      } else if (message.contains("ejecting foreign furnace")) {
         return "Clearing the furnace slots";
      } else if (message.contains("collecting") || message.contains("picking up") || message.contains("stepping onto") || message.contains("drop to land")) {
         return "Collecting drops";
      } else if (message.contains("loading furnace input")) {
         return "Loading furnace input";
      } else if (message.contains("loading furnace fuel")) {
         return "Adding furnace fuel";
      } else if (message.contains("loading")) {
         return "Loading ingredients";
      } else if (message.contains("opening")) {
         return "Opening the workstation";
      } else if (message.contains("turning") || message.contains("aiming")) {
         return "Aiming at the next action";
      } else if (message.contains("verifying")
         || message.contains("waiting for the cut")
         || message.contains("waiting for the result")
         || message.contains("confirm")) {
         return "Verifying the result";
      } else if (message.contains("crafting") || message.contains("stonecutting") || message.startsWith("cutting")) {
         return "Crafting ingredients";
      } else if (message.contains("placing")) {
         return "Placing blocks";
      } else if (message.contains("scanning") || message.contains("looking for") || message.contains("looking further") || message.contains("exploring")) {
         return "Finding nearby materials";
      } else if (message.contains("descending") || message.contains("digging down")) {
         return "Reaching the mining level";
      } else if (message.contains("travelling")
         || message.contains("walking")
         || message.contains("moving")
         || message.contains("approaching")
         || message.contains("planning")
         || message.contains("route")) {
         return "Moving to the next action";
      } else if (message.contains("preparing")) {
         return "Preparing materials";
      } else {
         return !message.startsWith("mine ") && !message.startsWith("mining ") ? init(fallback) : "Mining nearby blocks";
      }
   }

   static AiTaskStatus.Inner_StreamUtil init(Collection<AiTaskStatus.Inner_DataUtil> candidates, long now) {
      AiTaskStatus.Inner_DataUtil chosen = init_cs_DataUtil(candidates);
      if (chosen == null) {
         init_W_StreamUtil.init();
         load.init();
         return AiTaskStatus.Inner_StreamUtil.EMPTY;
      } else if (!chosen.immediate && chosen.priority < 200) {
         String title = init_W_StreamUtil.init(chosen.content.title, now, 1500000000L);
         String activity = load.init(chosen.content.activity, now, 1500000000L);
         return new AiTaskStatus.Inner_StreamUtil(title, activity, chosen.content.detail, chosen.content.percent);
      } else {
         init_W_StreamUtil.init();
         load.init();
         return chosen.content;
      }
   }

   static AiTaskStatus.Inner_StreamUtil init_cs_StreamUtil(Collection<AiTaskStatus.Inner_DataUtil> candidates) {
      AiTaskStatus.Inner_DataUtil chosen = init_cs_DataUtil(candidates);
      return chosen == null ? AiTaskStatus.Inner_StreamUtil.EMPTY : chosen.content;
   }

   private static AiTaskStatus.Inner_DataUtil init_cs_DataUtil(Collection<AiTaskStatus.Inner_DataUtil> candidates) {
      AiTaskStatus.Inner_DataUtil chosen = null;

      for (AiTaskStatus.Inner_DataUtil candidate : candidates) {
         if (candidate.content.visible()
            && (chosen == null || candidate.priority > chosen.priority || candidate.priority == chosen.priority && candidate.publication > chosen.publication)) {
            chosen = candidate;
         }
      }

      return chosen;
   }

   static int init(AiTaskStatus.Inner_StreamUtil content, ToIntFunction<String> measure, int available) {
      int titleW = measure.applyAsInt(content.title);
      if (content.percent >= 0) {
         titleW += measure.applyAsInt("100%") + 12;
      }

      int widest = Math.max(titleW, Math.max(measure.applyAsInt(content.bannerActivity()), measure.applyAsInt(content.bannerDetail())));
      return (int)Math.max(1L, Math.min(Math.max(1, available), Math.max(0, widest) + 10L));
   }

   private static String init(String value) {
      return value == null ? "" : value.strip().replaceAll("[\\r\\n\\t]+", " ");
   }

   record Inner_DataUtil(AiTaskStatus.Inner_StreamUtil content, int priority, long publication, boolean immediate) {
      Inner_DataUtil(AiTaskStatus.Inner_StreamUtil content, int priority, long publication) {
         this(content, priority, publication, false);
      }
   }

   record Inner_StreamUtil(String title, String activity, String detail, int percent) {
      static final AiTaskStatus.Inner_StreamUtil EMPTY = new AiTaskStatus.Inner_StreamUtil("", "", "");

      Inner_StreamUtil(String title, String activity, String detail) {
         this(title, activity, detail, -1);
      }

      boolean visible() {
         return !this.title.isEmpty();
      }

      String bannerActivity() {
         if (this.percent >= 0 && !this.detail.isEmpty()) {
            return this.activity.isEmpty() ? this.detail : this.activity + "  ·  " + this.detail;
         } else {
            return this.activity;
         }
      }

      String bannerDetail() {
         return this.percent >= 0 ? "" : this.detail;
      }
   }
}
