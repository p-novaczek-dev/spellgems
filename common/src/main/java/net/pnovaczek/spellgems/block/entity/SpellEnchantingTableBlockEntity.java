package net.pnovaczek.spellgems.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.pnovaczek.spellgems.ModBlockEntities;
import org.jspecify.annotations.Nullable;

public class SpellEnchantingTableBlockEntity extends BlockEntity implements Nameable {

    private static final Component DEFAULT_NAME = Component.translatable("container.spellgems.spell_enchanting_table");
    /** Compensates for ENCHANT particles ending 1.2 below their target. */
    private static final double GLYPH_TARGET_Y_OFFSET = 2.2;
    private static final double GLYPH_START_Y_DELTA = -1.2;
    private static final double GLYPH_RADIUS = 0.48;

    private @Nullable Component name;

    public SpellEnchantingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPELL_ENCHANTING_TABLE, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SpellEnchantingTableBlockEntity blockEntity) {
        RandomSource random = level.getRandom();
        if (random.nextInt(32) != 0) {
            return;
        }

        double cx = pos.getX() + 0.5;
        double cz = pos.getZ() + 0.5;
        double targetY = pos.getY() + GLYPH_TARGET_Y_OFFSET;
        double angle = random.nextDouble() * Math.PI * 2.0;
        double sweep = (random.nextBoolean() ? 1.0 : -1.0) * (0.6 + random.nextDouble() * 0.8);
        double r1 = GLYPH_RADIUS * (0.8 + random.nextDouble() * 0.4);
        double r2 = GLYPH_RADIUS * (0.8 + random.nextDouble() * 0.4);
        double sx = cx + Math.cos(angle) * r1;
        double sz = cz + Math.sin(angle) * r1;
        double tx = cx + Math.cos(angle + sweep) * r2;
        double tz = cz + Math.sin(angle + sweep) * r2;
        level.addParticle(ParticleTypes.ENCHANT, tx, targetY, tz, sx - tx, GLYPH_START_Y_DELTA, sz - tz);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("CustomName", ComponentSerialization.CODEC, this.name);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.name = parseCustomNameSafe(input, "CustomName");
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : DEFAULT_NAME;
    }

    @Override
    public @Nullable Component getCustomName() {
        return this.name;
    }

    public void setCustomName(@Nullable Component name) {
        this.name = name;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.name = components.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CUSTOM_NAME, this.name);
    }
}
