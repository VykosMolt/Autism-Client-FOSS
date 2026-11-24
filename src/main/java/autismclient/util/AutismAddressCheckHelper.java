package autismclient.util;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Constructor;
import net.minecraft.client.multiplayer.resolver.AddressCheck;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public final class AutismAddressCheckHelper {
   private static final AddressCheck PERMISSIVE = new AddressCheck() {
      @Override
      public boolean isAllowed(ResolvedServerAddress address) {
         return true;
      }

      @Override
      public boolean isAllowed(ServerAddress address) {
         return true;
      }
   };

   private AutismAddressCheckHelper() {
   }

   public static AddressCheck createPermissive() {
      AddressCheck vanilla = createEmptyVanilla();
      return vanilla != null ? vanilla : PERMISSIVE;
   }

   private static AddressCheck createEmptyVanilla() {
      try {
         for (Class<?> nested : AddressCheck.class.getDeclaredClasses()) {
            if (nested != AddressCheck.class && AddressCheck.class.isAssignableFrom(nested)) {
               Constructor<?> constructor = nested.getDeclaredConstructor(ImmutableList.class);
               constructor.setAccessible(true);
               return (AddressCheck)constructor.newInstance(ImmutableList.of());
            }
         }
      } catch (Throwable ignored) {
      }

      return null;
   }
}
