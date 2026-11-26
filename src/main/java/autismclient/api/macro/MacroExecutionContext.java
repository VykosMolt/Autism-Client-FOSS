package autismclient.api.macro;

import autismclient.util.macro.MacroTemplateResolver;
import autismclient.util.macro.MacroVarValue;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;

public interface MacroExecutionContext {
   Minecraft mc();

   void runOnClientThread(Runnable var1);

   <T> T callOnClientThread(Supplier<T> var1);

   void waitTicks(int var1);

   void awaitCondition(CompletableFuture<Void> var1);

   void awaitCondition(AddonCondition var1);

   boolean isActive();

   void setStatus(String var1);

   void sendPacket(Packet<?> var1);

   Optional<MacroVarValue> variable(String var1);

   void setVariable(String var1, MacroVarValue var2);

   default void setVariable(String name, Object value) {
      this.setVariable(name, MacroVarValue.init_l_Obj(value));
   }

   void removeVariable(String var1);

   Map<String, MacroVarValue> variables();

   MacroTemplateResolver.ResolveResult resolveTemplate(String var1);

   default void captureResult(String name, MacroVarValue value) {
      this.setVariable(name, value);
   }

   default void captureResults(Map<String, MacroVarValue> values) {
      if (values != null) {
         values.forEach(this::setVariable);
      }
   }

   default Optional<Integer> resolveInt(String template) {
      MacroTemplateResolver.ResolveResult resolved = this.resolveTemplate(template);
      if (!resolved.init_Z()) {
         return Optional.empty();
      }

      try {
         return Optional.of(Integer.parseInt(resolved.init_Str().trim()));
      } catch (NumberFormatException ignored) {
         return Optional.empty();
      }
   }

   default Optional<Double> resolveDouble(String template) {
      MacroTemplateResolver.ResolveResult resolved = this.resolveTemplate(template);
      if (!resolved.init_Z()) {
         return Optional.empty();
      }

      try {
         return Optional.of(Double.parseDouble(resolved.init_Str().trim()));
      } catch (NumberFormatException ignored) {
         return Optional.empty();
      }
   }

   default <E extends Enum<E>> Optional<E> resolveEnum(String template, Class<E> type) {
      if (type == null) {
         return Optional.empty();
      }

      MacroTemplateResolver.ResolveResult resolved = this.resolveTemplate(template);
      if (!resolved.init_Z()) {
         return Optional.empty();
      }

      for (E value : type.getEnumConstants()) {
         if (value.name().equalsIgnoreCase(resolved.init_Str().trim())) {
            return Optional.of(value);
         }
      }

      return Optional.empty();
   }
}
