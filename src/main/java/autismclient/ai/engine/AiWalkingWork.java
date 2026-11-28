package autismclient.ai.engine;

import autismclient.api.ai.AiOperation;
import autismclient.util.AutismLiteVariant;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class AiWalkingWork {
   private static final Map<AiOperation, WeakReference<AiWrExecutor>> executors = Collections.synchronizedMap(new WeakHashMap<>());

   private AiWalkingWork() {
   }

   static void register(AiOperation var0, AiWrExecutor var1) {
      executors.put(var0, new WeakReference<>(var1));
   }

   static void forwardLookAndClick(AiOperation var0, Function<Predicate<AABB>, Vec3> var1, Runnable var2) {
      if (!AutismLiteVariant.enabled()) {
         var var3 = executors.get(var0);
         AiWrExecutor var4 = var3 == null ? null : (AiWrExecutor)var3.get();
         if (var4 != null) {
            var4.putAt_V_Function_Runnable(var1, var2);
         }
      }
   }

   // 5.3 external ABI bridges
   static void bindExecutor(AiOperation op, AiWrExecutor exec) { register(op, exec); }
   static void setLookAndClick(AiOperation op, Function<Predicate<AABB>, Vec3> look, Runnable click) { forwardLookAndClick(op, look, click); }
}
