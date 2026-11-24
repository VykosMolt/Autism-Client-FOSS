package autismclient.ai.a;

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

final class o {
   private static final Map<AiOperation, WeakReference<RefQueue>> init = Collections.synchronizedMap(new WeakHashMap<>());

   private o() {
   }

   static void init(AiOperation owner, RefQueue executor) {
      init.put(owner, new WeakReference<>(executor));
   }

   static void init(AiOperation owner, Function<Predicate<AABB>, Vec3> look, Runnable click) {
      if (!AutismLiteVariant.enabled()) {
         WeakReference<RefQueue> reference = init.get(owner);
         RefQueue executor = reference == null ? null : reference.get();
         if (executor != null) {
            executor.init_V_Function_Runnable(look, click);
         }
      }
   }
}
