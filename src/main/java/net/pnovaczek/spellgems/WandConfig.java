package net.pnovaczek.spellgems;

public class WandConfig {

    public float spellEnchantmentDurabilityCostMultiplier = 6.0F;
    public WandSpellDurabilityCosts spells = new WandSpellDurabilityCosts();

    public static class WandSpellDurabilityCosts {
        public int projectile = 4;
        public int nova = 4;
        public int vortex = 4;
        public int blink = 32;
        public int windCharge = 2;
        public int magnet = 1;
        public int placeBlock = 1;
        public int breakBlock = 1;
        public int plant = 1;
        public int harvest = 1;
        public int feed = 1;
        public int grow = 1;
        public int potion = 1;
    }
}