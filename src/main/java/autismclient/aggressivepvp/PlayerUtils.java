package autismclient.aggressivepvp;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

/** The parts of Meteor's PlayerUtils and Utils that Crystal Aura and Auto Totem use. */
public final class PlayerUtils {
    public static final Minecraft mc = Minecraft.getInstance();

    private PlayerUtils() {}

    public static float possibleHealthReductions(boolean entities, boolean fall) {
        float damageTaken = 0;

        if (entities) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof EndCrystal) {
                    float crystalDamage = DamageUtils.crystalDamage(mc.player, entity.position());
                    if (crystalDamage > damageTaken) damageTaken = crystalDamage;
                } else if (entity instanceof Player player && player != mc.player && !Autism.friend(player) && isWithin(entity, 5)) {
                    float attackDamage = DamageUtils.getAttackDamage(player, mc.player);
                    if (attackDamage > damageTaken) damageTaken = attackDamage;
                }
            }

            if (mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.BED_RULE).destroyOnUse()) {
                BlockPos playerPos = mc.player.blockPosition();
                int range = 6;
                for (BlockPos bp : BlockPos.betweenClosed(playerPos.offset(-range, -range, -range), playerPos.offset(range, range, range))) {
                    if (mc.level.getBlockState(bp).getBlock() instanceof BedBlock) {
                        float explosionDamage = DamageUtils.bedDamage(mc.player, new Vec3(bp.getX(), bp.getY(), bp.getZ()));
                        if (explosionDamage > damageTaken) damageTaken = explosionDamage;
                    }
                }
            }
        }

        if (fall && !Autism.enabled("no-fall") && mc.player.fallDistance > 3) {
            float damage = DamageUtils.fallDamage(mc.player);
            if (damage > damageTaken && !EntityUtils.isAboveWater(mc.player)) damageTaken = damage;
        }

        return damageTaken;
    }

    public static double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(squaredDistance(x1, y1, z1, x2, y2, z2));
    }

    public static double squaredDistanceTo(Entity entity) {
        return squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ());
    }

    public static double squaredDistanceTo(double x, double y, double z) {
        return squaredDistance(mc.player.getX(), mc.player.getY(), mc.player.getZ(), x, y, z);
    }

    public static double squaredDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double f = x1 - x2, g = y1 - y2, h = z1 - z2;
        return f * f + g * g + h * h;
    }

    public static boolean isWithin(Entity entity, double r) {
        return squaredDistanceTo(entity.getX(), entity.getY(), entity.getZ()) <= r * r;
    }

    public static boolean isWithin(Vec3 vec3d, double r) {
        return squaredDistanceTo(vec3d.x(), vec3d.y(), vec3d.z()) <= r * r;
    }

    public static boolean isWithin(BlockPos blockPos, double r) {
        return squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ()) <= r * r;
    }

    public static int getPing() {
        if (mc.getConnection() == null || mc.player == null) return 0;
        PlayerInfo entry = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return entry == null ? 0 : entry.getLatency();
    }

    /** DonutShards.onServer from the Meteor fork: the server address is donutsmp.net or one of its subdomains. */
    public static boolean onDonut() {
        ServerData server = mc.getCurrentServer();
        if (server == null || server.ip == null || mc.level == null) return false;
        String host = server.ip.trim().toLowerCase(Locale.ROOT);
        int colon = host.lastIndexOf(':');
        if (colon > 0 && host.indexOf(':') == colon) host = host.substring(0, colon);
        while (host.endsWith(".")) host = host.substring(0, host.length() - 1);
        return host.equals("donutsmp.net") || host.endsWith(".donutsmp.net");
    }

    public static void getEnchantments(ItemStack itemStack, Object2IntMap<Holder<Enchantment>> enchantments) {
        enchantments.clear();
        if (itemStack.isEmpty()) return;
        var entries = itemStack.getItem() == Items.ENCHANTED_BOOK
            ? itemStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet()
            : itemStack.getEnchantments().entrySet();
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : entries) enchantments.put(entry.getKey(), entry.getIntValue());
    }

    public static int getEnchantmentLevel(ItemStack itemStack, ResourceKey<Enchantment> enchantment) {
        if (itemStack.isEmpty()) return 0;
        Object2IntMap<Holder<Enchantment>> map = new Object2IntArrayMap<>();
        getEnchantments(itemStack, map);
        return getEnchantmentLevel(map, enchantment);
    }

    public static int getEnchantmentLevel(Object2IntMap<Holder<Enchantment>> enchantments, ResourceKey<Enchantment> enchantment) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : Object2IntMaps.fastIterable(enchantments)) {
            if (entry.getKey().is(enchantment)) return entry.getIntValue();
        }
        return 0;
    }
}
