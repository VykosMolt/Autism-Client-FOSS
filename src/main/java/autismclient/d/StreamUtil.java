package autismclient.d;

import java.util.List;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface StreamUtil {
   void init(List<Component> var1, @Nullable Tag var2, boolean var3);
}
