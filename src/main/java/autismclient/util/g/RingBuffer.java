package autismclient.util.g;

import autismclient.AutismClientAddon;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ChatComponent.State;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;

public final class RingBuffer {
   private static final int init_I = 100;
   private static volatile String init_Str;
   private static volatile long init_J;
   private static ChatComponent init_ChatComponent;
   private static State init_ChatComponent_Stat;
   private static final ThreadLocal<Boolean> init_ThreadLocal = ThreadLocal.withInitial(() -> false);
   private static final ThreadLocal<Boolean> load = ThreadLocal.withInitial(() -> false);

   private RingBuffer() {
   }

   static void init(String accountId) {
      Minecraft mc = Minecraft.getInstance();
      if (accountId != null && mc != null && mc.gui != null && mc.gui.hud != null) {
         ChatComponent chat = mc.gui.hud.getChat();
         if (chat != null) {
            init();
            init_ChatComponent = chat;
            init_ChatComponent_Stat = chat.storeState();
            init_Str = accountId;
            init_J++;
            PacketUtil manager = PacketUtil.load_PacketUtil();
            List<RingBuffer.Inner_DataUtil> history = manager == null ? List.of() : manager.load_List_Str(accountId);
            chat.restoreState(init(mc, history));
            chat.resetChatScroll();
         }
      }
   }

   static void init() {
      ChatComponent chat = init_ChatComponent;
      State restore = init_ChatComponent_Stat;
      init_Str = null;
      init_J++;
      init_ChatComponent = null;
      init_ChatComponent_Stat = null;
      if (chat != null && restore != null) {
         try {
            chat.restoreState(restore);
            chat.resetChatScroll();
         } catch (RuntimeException | Error error) {
            AutismClientAddon.LOG.warn("Could not restore rendered-client chat after POV", error);
         }
      }
   }

   static void init(ClassCache session, Component message) {
      String accountId = init_Str;
      if (session != null && message != null && accountId != null && accountId.equals(session.open_Str())) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            long expectedEpoch = init_J;
            mc.execute(() -> {
               if (expectedEpoch == init_J && session.open_Str().equals(init_Str)) {
                  ChatComponent chat = init_ChatComponent;
                  if (chat != null && mc.gui != null && mc.gui.hud != null && mc.gui.hud.getChat() == chat) {
                     init_ThreadLocal.set(true);

                     try {
                        chat.addServerSystemMessage(message);
                     } finally {
                        init_ThreadLocal.remove();
                     }
                  }
               }
            });
         }
      }
   }

   public static State init(ChatComponent chat) {
      if (chat != null && chat == init_ChatComponent && init_Str != null && !init_ThreadLocal.get() && !load.get()) {
         State botState = chat.storeState();
         State mainState = init_ChatComponent_Stat;
         if (mainState == null) {
            return null;
         }

         load.set(true);

         try {
            chat.restoreState(mainState);
            return botState;
         } catch (Throwable error) {
            load.remove();
            throw error;
         }
      } else {
         return null;
      }
   }

   public static void init(ChatComponent chat, State botState) {
      if (chat != null && botState != null) {
         try {
            init_ChatComponent_Stat = chat.storeState();
            chat.restoreState(botState);
         } finally {
            load.remove();
         }
      }
   }

   private static State init(Minecraft mc, List<RingBuffer.Inner_DataUtil> history) {
      int size = Math.min(100, history == null ? 0 : history.size());
      List<GuiMessage> messages = new ArrayList<>(size);
      int currentTick = mc.gui.hud.getGuiTicks();
      long now = System.currentTimeMillis();
      int first = history == null ? 0 : history.size() - size;

      for (int i = first; history != null && i < history.size(); i++) {
         RingBuffer.Inner_DataUtil line = history.get(i);
         long ageTicks = Math.max(0L, (now - line.init_J()) / 50L);
         GuiMessage message = new GuiMessage(
            (int)Math.max(0L, currentTick - Math.min(2147483647L, ageTicks)),
            line.init_Component(),
            null,
            GuiMessageSource.SYSTEM_SERVER,
            GuiMessageTag.systemSinglePlayer()
         );
         messages.add(0, message);
      }

      return new State(messages, List.of(), List.of());
   }

   record Inner_DataUtil(long init_J, Component init_Component) {
   }
}
