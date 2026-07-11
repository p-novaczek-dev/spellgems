package net.pnovaczek.spellgems.spell;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.pnovaczek.spellgems.Spellgems;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class Feed extends AbstractSpell {

    private static final int COOLDOWN_TICKS = 10;
    private static final int AREA_RADIUS = 8;

    @Override
    public Identifier id() {
        return Spells.FEED;
    }

    @Override
    public boolean canCast(SpellContext context) {
        if (!(context.caster() instanceof Player player)) {
            return false;
        }

        if (!Spellgems.CONFIG.spells.feed.requireFeedItems) {
            return hasFeedableAnimal(context, player);
        }

        return hasFeedableAnimalWithFood(context, player);
    }

    private static boolean hasFeedableAnimal(SpellContext context, Player player) {
        return !findFeedableAnimals(context, player).isEmpty();
    }

    private static boolean hasFeedableAnimalWithFood(SpellContext context, Player player) {
        var level = context.level();
        for (Animal animal : findFeedableAnimals(context, player)) {
            if (HotbarFeeds.pickWeightedFoodFor(player, animal, level.getRandom()) != null) {
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

    private static List<Animal> findFeedableAnimals(SpellContext context, Player player) {
        double px = player.getX();
        double pz = player.getZ();
        var searchBox = player.getBoundingBox().inflate(AREA_RADIUS, 4.0, AREA_RADIUS);
        return context.level().getEntitiesOfClass(Animal.class, searchBox, animal ->
                animal.isAlive()
                        && Math.abs(animal.getX() - px) <= AREA_RADIUS
                        && Math.abs(animal.getZ() - pz) <= AREA_RADIUS
                        && ((isReadyToLove(animal) && animal.canFallInLove()) || animal.canAgeUp())
        );
    }

    @Override
    public void cast(SpellContext context) {
        if (!(context.caster() instanceof Player player) || context.level().isClientSide()) {
            return;
        }

        var level = context.level();
        boolean requireFeedItems = Spellgems.CONFIG.spells.feed.requireFeedItems;

        List<Animal> animals = findFeedableAnimals(context, player);
        boolean fedAny = false;

        for (Animal animal : animals) {
            ItemStack food = requireFeedItems
                    ? HotbarFeeds.pickWeightedFoodFor(player, animal, level.getRandom())
                    : null;

            if (tryFeed(animal, player, food, requireFeedItems)) {
                fedAny = true;
            }
        }

        if (!fedAny) {
            return;
        }

        if (requireFeedItems && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }

        applyCastCooldown(context, COOLDOWN_TICKS);
    }

    private static boolean tryFeed(Animal animal, Player player, @Nullable ItemStack food, boolean consumeItem) {
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