package autismclient.util.macro;

import autismclient.util.AutismClientMessaging;
import autismclient.util.ck;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;

public class O implements MacroAction {
   public O.Inner_CachePool startEvent = O.Inner_CachePool.init;
   public O.Inner_StreamUtil endEvent = O.Inner_StreamUtil.init;
   public O.Inner_DataUtil endCondition = O.Inner_DataUtil.init;
   public String reportLabel = "Report";
   public String startConditionType = L.Inner_DataUtil.init.name();
   public String startActionType = StreamGate.reset.name();
   public String endConditionType = L.Inner_DataUtil.load.name();
   public int conditionCount = 0;
   public String commandText = "";
   public String endGuiTitle = "";
   public String endPacketName = "";
   public String endTargetDimension = "";
   public double endDistance = 5.0;
   public boolean endHorizontalOnly = false;
   public int timeoutMs = 10000;
   public boolean stashToSharedState = true;
   public String guiTitleSubstr = "";
   public double positionTolerance = 5.0;

   @Override
   public void execute(Minecraft mc) {
      if (!autismclient.modules.aD.read_Z()) {
         if (mc.getConnection() == null) {
            AutismClientMessaging.sendPrefixed("§cReport: no network connection");
         } else {
            long executeStartNanos = System.nanoTime();
            Long startNanos = this.auditAwaitStartNanos(mc);
            if (startNanos != null) {
               long spentMs = (System.nanoTime() - executeStartNanos) / 1000000L;
               long remainingMs = Math.max(100, this.timeoutMs) - spentMs;
               if (remainingMs <= 0L) {
                  AutismClientMessaging.sendPrefixed("§eReport \"" + this.init_Str_3() + "\": no time left after start event");
               } else {
                  int remainingTimeout = (int)Math.min(Integer.MAX_VALUE, remainingMs);
                  MacroAction condition = this.write_MacroAction();

                  boolean fired;
                  try {
                     fired = MacroExecutor.init_Z_Minecraft_4(mc, condition, remainingTimeout);
                  } catch (Throwable e) {
                     if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        return;
                     }

                     if (e instanceof RuntimeException re) {
                        throw re;
                     }

                     if (e instanceof Error err) {
                        throw err;
                     }

                     throw new RuntimeException(e);
                  }

                  if (!fired) {
                     AutismClientMessaging.sendPrefixed("§eReport \"" + this.init_Str_3() + "\": timeout waiting for " + this.read_Str());
                  } else {
                     long deltaMs = (System.nanoTime() - startNanos) / 1000000L;
                     AutismClientMessaging.sendPrefixed(
                        "§b" + this.init_Str_3() + ": " + deltaMs + " ms (" + this.load_Str_2() + " -> " + this.read_Str() + ")"
                     );
                     if (this.stashToSharedState) {
                        ck.init_ck()
                           .init_V_ck_ConfigStore(
                              new ck.Inner_ConfigStore(this.init_Str_3(), deltaMs, this.load_Str_2(), this.read_Str(), System.currentTimeMillis())
                           );
                     }
                  }
               }
            }
         }
      }
   }

   private Long auditAwaitStartNanos(Minecraft mc) {
      if (this.startEvent != O.Inner_CachePool.init && this.startEvent != O.Inner_CachePool.write) {
         Predicate<Packet<?>> startMatch = this.startEvent == O.Inner_CachePool.load
            ? p -> p instanceof ServerboundChatCommandPacket
               || p instanceof ServerboundChatCommandSignedPacket
               || p instanceof ServerboundChatPacket spc && spc.message() != null && spc.message().startsWith("/")
            : p -> p instanceof ServerboundContainerClickPacket;
         MacroExecutor.Inner_EventBus sendListener = MacroExecutor.load_MacroExecutor_Even_Predicate(startMatch);

         try {
            if (this.startEvent == O.Inner_CachePool.load) {
               String command = init_Str_Str_2(this.commandText);
               if (command.isBlank()) {
                  sendListener.init_V();
                  AutismClientMessaging.sendPrefixed("Report \"" + this.init_Str_3() + "\": set a command for COMMAND_SENT.");
                  return null;
               }

               mc.execute(() -> {
                  if (!autismclient.modules.aD.read_Z() && mc.getConnection() != null) {
                     mc.getConnection().sendCommand(command);
                  }
               });
            }

            sendListener.init_CompletableFuture.get(Math.max(100, this.timeoutMs), TimeUnit.MILLISECONDS);
            return System.nanoTime();
         } catch (Exception e) {
            sendListener.init_V();
            if (e instanceof InterruptedException) {
               Thread.currentThread().interrupt();
               return null;
            }
            AutismClientMessaging.sendPrefixed("§eReport \"" + this.init_Str_3() + "\": timeout waiting for start event " + this.startEvent.name());
            return null;
         }
      } else {
         return System.nanoTime();
      }
   }

   private MacroAction write_MacroAction() {
      MacroAction var10000;
      switch (this.endCondition) {
         case init: {
            ax action = new ax();
            action.waitMode = ax.Inner_DataUtil.init;
            action.guiTitle = this.endGuiTitle == null ? "" : this.endGuiTitle;
            var10000 = action;
            break;
         }
         case load: {
            ax action = new ax();
            action.waitMode = ax.Inner_DataUtil.load;
            action.guiTitle = this.endGuiTitle == null ? "" : this.endGuiTitle;
            var10000 = action;
            break;
         }
         case read: {
            aB action = new aB("S2C:" + ClientboundPlayerPositionPacket.class.getSimpleName());
            var10000 = action;
            break;
         }
         case write: {
            aG action = new aG();
            action.targetDimension = this.endTargetDimension == null ? "" : this.endTargetDimension;
            var10000 = action;
            break;
         }
         case update: {
            aC action = new aC();
            action.distance = this.endDistance;
            action.horizontalOnly = this.endHorizontalOnly;
            var10000 = action;
            break;
         }
         case sync:
            var10000 = new aB(this.endPacketName == null ? "" : this.endPacketName);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("startEvent", this.startEvent.name());
      tag.putString("endEvent", this.init_O_StreamUtil().name());
      tag.putString("endCondition", this.endCondition.name());
      tag.putString("startConditionType", this.startConditionType == null ? L.Inner_DataUtil.init.name() : this.startConditionType);
      tag.putString("startActionType", this.startActionType == null ? StreamGate.reset.name() : this.startActionType);
      tag.putString("endConditionType", this.endConditionType == null ? L.Inner_DataUtil.load.name() : this.endConditionType);
      tag.putInt("conditionCount", Math.max(0, this.conditionCount));
      tag.putString("reportLabel", this.reportLabel == null ? "" : this.reportLabel);
      tag.putString("commandText", this.commandText == null ? "" : this.commandText);
      tag.putString("endGuiTitle", this.endGuiTitle == null ? "" : this.endGuiTitle);
      tag.putString("endPacketName", this.endPacketName == null ? "" : this.endPacketName);
      tag.putString("endTargetDimension", this.endTargetDimension == null ? "" : this.endTargetDimension);
      tag.putDouble("endDistance", this.endDistance);
      tag.putBoolean("endHorizontalOnly", this.endHorizontalOnly);
      tag.putInt("timeoutMs", Math.max(100, this.timeoutMs));
      tag.putBoolean("stashToSharedState", this.stashToSharedState);
      tag.putString("guiTitleSubstr", this.endGuiTitle == null ? "" : this.endGuiTitle);
      tag.putDouble("positionTolerance", this.endDistance);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      try {
         this.startEvent = O.Inner_CachePool.valueOf(tag.getStringOr("startEvent", O.Inner_CachePool.init.name()));
      } catch (IllegalArgumentException e) {
         this.startEvent = O.Inner_CachePool.init;
      }

      this.reportLabel = tag.getStringOr("reportLabel", "Report");
      this.startConditionType = tag.getStringOr("startConditionType", L.Inner_DataUtil.init.name());
      this.startActionType = tag.getStringOr("startActionType", StreamGate.reset.name());
      this.endConditionType = tag.getStringOr("endConditionType", "");
      this.conditionCount = Math.max(0, tag.getIntOr("conditionCount", 0));
      this.commandText = tag.getStringOr("commandText", "");
      this.timeoutMs = Math.max(100, tag.getIntOr("timeoutMs", 10000));
      this.stashToSharedState = tag.getBooleanOr("stashToSharedState", true);
      this.guiTitleSubstr = tag.getStringOr("guiTitleSubstr", "");
      this.positionTolerance = tag.getDoubleOr("positionTolerance", 5.0);
      if (tag.contains("endCondition")) {
         try {
            this.endCondition = O.Inner_DataUtil.valueOf(tag.getStringOr("endCondition", O.Inner_DataUtil.init.name()));
         } catch (IllegalArgumentException e) {
            this.endCondition = O.Inner_DataUtil.init;
         }

         this.endGuiTitle = tag.getStringOr("endGuiTitle", this.guiTitleSubstr);
         this.endPacketName = tag.getStringOr("endPacketName", "");
         this.endTargetDimension = tag.getStringOr("endTargetDimension", "");
         this.endDistance = tag.getDoubleOr("endDistance", this.positionTolerance);
         this.endHorizontalOnly = tag.getBooleanOr("endHorizontalOnly", false);
         if (this.endConditionType == null || this.endConditionType.isBlank()) {
            this.endConditionType = this.write_Str();
         }
      } else {
         try {
            this.endEvent = O.Inner_StreamUtil.valueOf(tag.getStringOr("endEvent", O.Inner_StreamUtil.init.name()));
         } catch (IllegalArgumentException e) {
            this.endEvent = O.Inner_StreamUtil.init;
         }

         this.init_V_6();
      }
   }

   private void init_V_6() {
      switch (this.endEvent) {
         case init:
            this.endCondition = O.Inner_DataUtil.init;
            this.endGuiTitle = this.guiTitleSubstr;
            break;
         case load:
            this.endCondition = O.Inner_DataUtil.load;
            this.endGuiTitle = this.guiTitleSubstr;
            break;
         case read:
            this.endCondition = O.Inner_DataUtil.write;
            this.endTargetDimension = "";
            break;
         case write:
            this.endCondition = O.Inner_DataUtil.read;
            break;
         case update:
            this.endCondition = O.Inner_DataUtil.update;
            this.endDistance = this.positionTolerance;
            this.endHorizontalOnly = false;
      }

      this.endConditionType = this.write_Str();
   }

   private O.Inner_StreamUtil init_O_StreamUtil() {
      O.Inner_StreamUtil var10000;
      switch (this.endCondition) {
         case init:
            var10000 = O.Inner_StreamUtil.init;
            break;
         case load:
            var10000 = O.Inner_StreamUtil.load;
            break;
         case read:
         case sync:
            var10000 = O.Inner_StreamUtil.write;
            break;
         case write:
            var10000 = O.Inner_StreamUtil.read;
            break;
         case update:
            var10000 = O.Inner_StreamUtil.update;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_r_StreamGate;
   }

   @Override
   public String getDisplayName() {
      return "Report \"" + this.init_Str_3() + "\" [" + this.load_Str_2() + " -> " + this.read_Str() + "]";
   }

   @Override
   public String getIcon() {
      return "Rep";
   }

   private String init_Str_3() {
      return this.reportLabel != null && !this.reportLabel.isBlank() ? this.reportLabel.trim() : "Report";
   }

   private String load_Str_2() {
      return this.startActionType != null && !this.startActionType.isBlank()
         ? this.startActionType
         : (
            this.startConditionType != null && !this.startConditionType.isBlank()
               ? this.startConditionType
               : (this.startEvent == null ? O.Inner_CachePool.init.name() : this.startEvent.name())
         );
   }

   private String read_Str() {
      return this.endConditionType != null && !this.endConditionType.isBlank()
         ? this.endConditionType
         : (this.endCondition == null ? O.Inner_DataUtil.init.name() : this.endCondition.name());
   }

   public MacroAction init_MacroAction_2() {
      return L.read_MacroAction(this.startConditionType);
   }

   public MacroAction load_MacroAction_2() {
      return L.load_MacroAction(this.startActionType);
   }

   public MacroAction read_MacroAction() {
      return L.read_MacroAction(this.endConditionType);
   }

   public int init(List<MacroAction> actions, int headerIndex) {
      if (actions != null && headerIndex >= 0 && headerIndex < actions.size()) {
         int max = Math.max(0, Math.min(this.conditionCount, actions.size() - headerIndex - 1));
         int count = 0;

         for (int i = headerIndex + 1; i < actions.size() && count < max; i++) {
            MacroAction action = actions.get(i);
            if (action instanceof L || action instanceof O) {
               break;
            }

            count++;
         }

         return count;
      } else {
         return 0;
      }
   }

   private String write_Str() {
      String var10000;
      switch (this.endCondition) {
         case init:
         case load:
            var10000 = L.Inner_DataUtil.load.name();
            break;
         case read:
            var10000 = L.Inner_DataUtil.prev.name();
            break;
         case write:
            var10000 = L.Inner_DataUtil.stop.name();
            break;
         case update:
            var10000 = L.Inner_DataUtil.next.name();
            break;
         case sync:
            var10000 = L.Inner_DataUtil.write.name();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static String init_Str_Str_2(String command) {
      if (command == null) {
         return "";
      }

      String trimmed = command.trim();
      return trimmed.startsWith("/") ? trimmed.substring(1).trim() : trimmed;
   }

   public enum Inner_CachePool {
      init,
      load,
      read,
      write;
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync;
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update;
   }
}
