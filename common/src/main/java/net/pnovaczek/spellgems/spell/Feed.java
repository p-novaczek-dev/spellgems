package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.pnovaczek.spellgems.Spellgems;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class Feed extends AbstractSpell {

    private static final int AREA_RADIUS = 8;

    @Override
    public Identifier id() {
        return SpellIds.FEED;
    }

    @Override
    public boolean canCast(SpellContext context) {
        if (context.resolveItemSource() == null && Spellgems.CONFIG.spells.feed.requireFeedItems) {
            return false;
        }

        if (!Spellgems.CONFIG.spells.feed.requireFeedItems) {
            return !findFeedableAnimals(context).isEmpty();
        }

        return hasFeedableAnimalWithFood(context);
    }

    private static boolean hasFeedableAnimalWithFood(SpellContext context) {
        var level = context.level();
        for (Animal animal : findFeedableAnimals(context)) {
            if (HotbarUtils.pickWeighted(context, level.getRandom(), stack -> animal.isFood(stack)) != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean isReadyToLove(Animal animal) {
        if (animal.level().isClientSide()) {
            return !animal.isBaby();
        }
        return animal.getAge() == 0;
    }

    private static List<Animal> findFeedableAnimals(SpellContext context) {
        Vec3 origin = context.origin();
        double px = origin.x;
        double pz = origin.z;
        AABB searchBox = new AABB(origin, origin).inflate(AREA_RADIUS, 4.0, AREA_RADIUS);
        return context.level().getEntitiesOfClass(Animal.class, searchBox, animal ->
                animal.isAlive()
                        && Math.abs(animal.getX() - px) <= AREA_RADIUS
                        && Math.abs(animal.getZ() - pz) <= AREA_RADIUS
                        && ((isReadyToLove(animal) && animal.canFallInLove()) || animal.canAgeUp())
        );
    }

    @Override
    protected boolean performCast(SpellContext context) {
        if (context.level().isClientSide()) {
            return false;
        }

        var level = context.level();
        boolean requireFeedItems = Spellgems.CONFIG.spells.feed.requireFeedItems;

        List<Animal> animals = findFeedableAnimals(context);
        boolean fedAny = false;

        for (Animal animal : animals) {
            ItemStack food = requireFeedItems
                    ? HotbarUtils.pickWeighted(context, level.getRandom(), stack -> animal.isFood(stack))
                    : null;

            if (tryFeed(animal, context.caster() instanceof Player player ? player : null, food, requireFeedItems)) {
                fedAny = true;
            }
        }

        if (!fedAny) {
            return false;
        }

        if (requireFeedItems && context.caster() instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }

        return true;
    }

    private static boolean tryFeed(Animal animal, @Nullable Player player, @Nullable ItemStack food, boolean consumeItem) {
        int age = animal.getAge();

        if (age == 0 && animal.canFallInLove()) {
            if (consumeItem) {
                if (food == null || food.isEmpty() || !animal.isFood(food)) {
                    return false;
                }
                food.shrink(1);
            }

            if (player instanceof ServerPlayer serverPlayer) {
                animal.setInLove(serverPlayer);
            } else {
                animal.setInLove(player);
            }
            playEatingSound(animal);
            return true;
        }

        if (animal.canAgeUp()) {
            if (consumeItem) {
                if (food == null || food.isEmpty() || !animal.isFood(food)) {
                    return false;
                }
                food.shrink(1);
            }

            animal.ageUp(AgeableMob.getSpeedUpSecondsWhenFeeding(-age), true);
            playEatingSound(animal);
            return true;
        }

        return false;
    }

    private static void playEatingSound(Animal animal) {
        animal.level().playSound(
                null,
                animal.getX(),
                animal.getY(),
                animal.getZ(),
                SoundEvents.GENERIC_EAT,
                SoundSource.NEUTRAL,
                1.0F,
                1.0F
        );
    }
}
