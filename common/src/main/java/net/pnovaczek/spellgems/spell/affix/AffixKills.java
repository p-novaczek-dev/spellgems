package net.pnovaczek.spellgems.spell.affix;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.ModComponents;
import net.pnovaczek.spellgems.item.data.SpellGemData;
import net.pnovaczek.spellgems.spell.MagnetPull;
import net.pnovaczek.spellgems.spell.SpellContext;
import net.pnovaczek.spellgems.spell.enchantment.SpellAffixes;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Remembers which affixes belong to a mob until it dies or something other than the spell hurts it.
 * Spell damage, explosions, lightning, and burning keep the credit. A later sword or projectile hit clears it
 * before that hit can drop loot.
 */
public final class AffixKills {

    private static final ThreadLocal<SpellKillCredit> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<Integer> SPELL_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final Map<UUID, Stored> CREDITS = new HashMap<>();
    private static final Map<UUID, Pull> PULLS = new HashMap<>();
    private static final double STAMP_RADIUS = 8.0;

    private AffixKills() {
    }

    public static void onServerTickEnd(MinecraftServer server) {
        pullPending(server);
        sweep(server);
    }

    public static void beforeDamage(LivingEntity entity, DamageSource source) {
        if (inSpellDamage() || keepsCredit(source)) {
            return;
        }
        CREDITS.remove(entity.getUUID());
    }

    public static SpellKillCredit creditFor(SpellContext context) {
        List<net.pnovaczek.spellgems.spell.enchantment.SpellAffix> affixes = readAffixes(context);
        // Dispenser kills stay uncredited, matching vanilla mobs not hurt by a player.
        Player player = context.caster() instanceof Player caster && !context.isDispenserCast() ? caster : null;
        if (affixes.isEmpty()) {
            if (player == null) {
                return SpellKillCredit.NONE;
            }
            // Still name the player so a gem with no affixes drops vanilla experience.
            return new SpellKillCredit(List.of(), player, Vec3.ZERO, false);
        }
        return new SpellKillCredit(affixes, player, MagnetPull.target(context), context.isDispenserCast());
    }

    public static @Nullable SpellKillCredit current() {
        return CURRENT.get();
    }

    public static void runWith(@Nullable SpellKillCredit credit, Runnable action) {
        SpellKillCredit previous = CURRENT.get();
        CURRENT.set(credit);
        try {
            action.run();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }

    public static void pushSpellDamage() {
        SPELL_DEPTH.set(SPELL_DEPTH.get() + 1);
    }

    public static void popSpellDamage() {
        int depth = SPELL_DEPTH.get() - 1;
        if (depth <= 0) {
            SPELL_DEPTH.remove();
        } else {
            SPELL_DEPTH.set(depth);
        }
    }

    public static boolean inSpellDamage() {
        return SPELL_DEPTH.get() > 0;
    }

    public static void record(LivingEntity entity, @Nullable SpellKillCredit credit) {
        if (entity.level().isClientSide() || entity instanceof Player || credit == null || !credit.active()) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        Stored existing = CREDITS.get(entity.getUUID());
        List<AffixEquipment.Piece> equipment = existing == null ? List.of() : existing.equipment;
        CREDITS.put(entity.getUUID(), new Stored(level.dimension(), credit, equipment));
    }

    public static @Nullable SpellKillCredit creditOf(LivingEntity entity) {
        Stored stored = CREDITS.get(entity.getUUID());
        return stored == null ? null : stored.credit;
    }

    public static void storeEquipment(LivingEntity entity) {
        Stored stored = CREDITS.get(entity.getUUID());
        if (stored == null || entity instanceof Player) {
            return;
        }
        stored.equipment = AffixEquipment.snapshot(entity);
    }

    public static List<AffixEquipment.Piece> equipmentOf(LivingEntity entity) {
        Stored stored = CREDITS.get(entity.getUUID());
        return stored == null ? List.of() : stored.equipment;
    }

    public static void stampNearby(Level level, Vec3 pos, @Nullable LivingEntity exclude) {
        SpellKillCredit credit = current();
        if (credit == null || !credit.active() || !(level instanceof ServerLevel serverLevel) || pos == null) {
            return;
        }
        AABB box = new AABB(pos, pos).inflate(STAMP_RADIUS);
        double radiusSqr = STAMP_RADIUS * STAMP_RADIUS;
        for (LivingEntity nearby : serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                box,
                entity -> entity != exclude && !(entity instanceof Player) && entity.isAlive()
        )) {
            if (pos.distanceToSqr(nearby.position()) <= radiusSqr) {
                record(nearby, credit);
            }
        }
    }

    public static void scheduleMagnet(LivingEntity entity) {
        if (entity.level().isClientSide() || entity instanceof Player) {
            return;
        }
        SpellKillCredit credit = creditOf(entity);
        if (credit == null || !SpellAffixes.has(credit.affixes(), SpellAffixes.MAGNET)) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        UUID playerId = credit.player() == null ? null : credit.player().getUUID();
        PULLS.put(entity.getUUID(), new Pull(
                level.dimension(),
                entity.position(),
                credit.pullPosition(),
                playerId,
                playerId != null && !credit.dispenser()
        ));
    }

    private static void pullPending(MinecraftServer server) {
        if (PULLS.isEmpty()) {
            return;
        }
        List<Pull> pulls = new ArrayList<>(PULLS.values());
        PULLS.clear();
        for (Pull pull : pulls) {
            ServerLevel level = server.getLevel(pull.dimension);
            if (level == null) {
                continue;
            }
            Player player = null;
            if (pull.playerId != null) {
                ServerPlayer serverPlayer = server.getPlayerList().getPlayer(pull.playerId);
                if (serverPlayer != null) {
                    player = serverPlayer;
                }
            }
            MagnetPull.pullFresh(level, pull.corpse, pull.target, player, pull.setPlayerTarget);
        }
    }

    private static void sweep(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Stored>> iterator = CREDITS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Stored> entry = iterator.next();
            ServerLevel level = server.getLevel(entry.getValue().dimension);
            if (level == null) {
                iterator.remove();
                continue;
            }
            Entity entity = level.getEntity(entry.getKey());
            if (entity == null || (entity instanceof LivingEntity living && living.isDeadOrDying())) {
                iterator.remove();
            }
        }
    }

    private static boolean keepsCredit(DamageSource source) {
        return source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(DamageTypes.EXPLOSION)
                || source.is(DamageTypes.PLAYER_EXPLOSION)
                || source.is(DamageTypes.LIGHTNING_BOLT)
                || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.ON_FIRE)
                || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.LAVA);
    }

    private static List<net.pnovaczek.spellgems.spell.enchantment.SpellAffix> readAffixes(SpellContext context) {
        ItemStack stack = context.castingItem();
        if (stack != null && !stack.isEmpty()) {
            SpellGemData data = stack.get(ModComponents.SPELL_GEM_DATA);
            if (data != null && !data.affixes().isEmpty()) {
                return data.affixes();
            }
        }
        if (context.data() != null && !context.data().affixes().isEmpty()) {
            return context.data().affixes();
        }
        return List.of();
    }

    private static final class Stored {
        private final ResourceKey<Level> dimension;
        private final SpellKillCredit credit;
        private List<AffixEquipment.Piece> equipment;

        private Stored(ResourceKey<Level> dimension, SpellKillCredit credit, List<AffixEquipment.Piece> equipment) {
            this.dimension = dimension;
            this.credit = credit;
            this.equipment = equipment;
        }
    }

    private record Pull(
            ResourceKey<Level> dimension,
            Vec3 corpse,
            Vec3 target,
            @Nullable UUID playerId,
            boolean setPlayerTarget
    ) {
    }
}
