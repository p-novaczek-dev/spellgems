package net.pnovaczek.spellgems.spell.affix;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * Fabric drop capture. A null frame means this death should spawn items normally, so a nested death
 * does not steal the outer capture.
 */
public final class AffixDropCapture {

    private record Frame(@Nullable List<ItemEntity> drops) {
    }

    private static final ThreadLocal<Deque<Frame>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private AffixDropCapture() {
    }

    public static void push(boolean capture) {
        STACK.get().push(new Frame(capture ? new ArrayList<>() : null));
    }

    public static void finish(LivingEntity entity, ServerLevel level) {
        Deque<Frame> frames = STACK.get();
        if (frames.isEmpty()) {
            return;
        }
        List<ItemEntity> captured = frames.pop().drops();
        if (frames.isEmpty()) {
            STACK.remove();
        }
        if (captured != null) {
            for (ItemEntity drop : AffixDrops.rewrite(entity, level, captured)) {
                if (drop != null && !drop.getItem().isEmpty()) {
                    level.addFreshEntity(drop);
                }
            }
        }
        AffixKills.scheduleMagnet(entity);
    }

    /**
     * @return true when this spawn is part of a captured death and must not be added to the level yet
     */
    public static boolean tryCapture(Entity entity, ServerLevel level, ItemStack stack, Vec3 offset, Consumer<ItemEntity> captured) {
        Deque<Frame> frames = STACK.get();
        if (frames.isEmpty()) {
            return false;
        }
        List<ItemEntity> top = frames.peek().drops();
        if (top == null) {
            return false;
        }
        if (stack.isEmpty()) {
            captured.accept(null);
            return true;
        }
        ItemEntity drop = new ItemEntity(
                level,
                entity.getX() + offset.x,
                entity.getY() + offset.y,
                entity.getZ() + offset.z,
                stack
        );
        drop.setDefaultPickUpDelay();
        top.add(drop);
        captured.accept(drop);
        return true;
    }
}
