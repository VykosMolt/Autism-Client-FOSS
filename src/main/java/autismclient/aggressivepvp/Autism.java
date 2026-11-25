package autismclient.aggressivepvp;

import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.modules.ByteReader;
import autismclient.modules.FieldCache;
import autismclient.modules.Module;
import autismclient.modules.aL;
import autismclient.modules.al;
import autismclient.util.AutismClientMessaging;
import autismclient.util.aD;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Direct typed bridge into AUTISM 5.3-26.3 internals (replaces the addon's reflective MethodHandle lookups).
 */
public final class Autism {
    public static final Logger LOG = LoggerFactory.getLogger("aggressive-pvp");

    private Autism() {}

    public static boolean verified() {
        return true;
    }

    /** The AUTISM module that holds the look this tick, or null. */
    public static String aimOwner() {
        return aD.init_Str();
    }

    /** One of AUTISM's extra-account connections, not the player's own. */
    public static boolean extraConnection(Connection connection) {
        return false;
    }

    /** Left alone by AUTISM's own Crystal Aura: a bot (AntiBot) or a friend Teams protects from attacks. */
    public static boolean excluded(Entity entity) {
        return ByteReader.load_Z(entity) || aL.init(entity, "killaura");
    }

    public static boolean friend(Entity entity) {
        return aL.init(entity, "killaura");
    }

    /** AUTISM's own Auto Totem is in the middle of a swap or click sequence. */
    public static boolean defaultTotemBusy() {
        return FieldCache.init_Z_6();
    }

    public static boolean enabled(String moduleId) {
        Module module = al.init_Module_Str(moduleId);
        return module != null && module.isEnabled();
    }

    /** AUTISM's Panic Mode ("hide" module) is active or mid-transition. */
    public static boolean panicMode() {
        return autismclient.modules.aD.write_Z();
    }

    public static void chat(String message) {
        try {
            AutismClientMessaging.sendPrefixed(message);
        } catch (Throwable t) {
            LOG.info("[chat] {}", message);
        }
    }

    /** The game's own slot sync before a use or attack (MultiPlayerGameMode.ensureHasSentCarriedItem). */
    public static void syncSelected() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor) accessor.autism$ensureHasSentCarriedItem();
    }
}
