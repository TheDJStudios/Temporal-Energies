package dev.paradoxutilities.compat.ait;

import dev.amble.ait.core.AITItems;
import dev.paradoxutilities.tardis.TardisFuelStorage;
import dev.amble.ait.core.item.SonicItem;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;

final class SonicEnergyCompat {
    private static final long ENERGY_PER_ARTRON = 100L;

    private SonicEnergyCompat() {}

    static void initialize() {
        EnergyStorage.ITEM.registerForItems((stack, context) ->
                        stack.getItem() instanceof SonicItem sonic ? new SonicStorage(context, sonic) : null,
                AITItems.SONIC_SCREWDRIVER);
    }

    private static final class SonicStorage implements EnergyStorage {
        private final ContainerItemContext context;
        private final SonicItem sonic;

        private SonicStorage(ContainerItemContext context, SonicItem sonic) {
            this.context = context;
            this.sonic = sonic;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            long itemCount = context.getAmount();
            if (!isValid() || maxAmount <= 0 || itemCount <= 0) return 0;

            long current = energyPerItem();
            long capacity = capacityPerItem();
            long insertedPerItem = Math.min(maxAmount / itemCount, capacity - current);
            if (insertedPerItem <= 0 || !tryChangeEnergy(insertedPerItem, itemCount, transaction)) return 0;
            return insertedPerItem * itemCount;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            long itemCount = context.getAmount();
            if (!isValid() || maxAmount <= 0 || itemCount <= 0) return 0;

            long current = energyPerItem();
            long extractedPerItem = Math.min(maxAmount / itemCount, current);
            if (extractedPerItem <= 0 || !tryChangeEnergy(-extractedPerItem, itemCount, transaction)) return 0;
            return extractedPerItem * itemCount;
        }

        @Override
        public long getAmount() {
            return isValid() ? energyPerItem() * context.getAmount() : 0L;
        }

        @Override
        public long getCapacity() {
            return isValid() ? capacityPerItem() * context.getAmount() : 0L;
        }

        private boolean isValid() {
            return context.getAmount() > 0 && context.getItemVariant().isOf(AITItems.SONIC_SCREWDRIVER);
        }

        private long energyPerItem() {
            return toEnergy(sonic.getCurrentFuel(context.getItemVariant().toStack()));
        }

        private long capacityPerItem() {
            return toEnergy(sonic.getMaxFuel(context.getItemVariant().toStack()));
        }

        private boolean tryChangeEnergy(long delta, long itemCount, TransactionContext transaction) {
            ItemVariant oldVariant = context.getItemVariant();
            ItemStack updatedStack = oldVariant.toStack();
            sonic.setCurrentFuel((sonic.getCurrentFuel(updatedStack) * ENERGY_PER_ARTRON + delta) / ENERGY_PER_ARTRON, updatedStack);
            ItemVariant updatedVariant = ItemVariant.of(updatedStack);

            try (Transaction nested = transaction.openNested()) {
                if (context.extract(oldVariant, itemCount, nested) != itemCount
                        || context.insert(updatedVariant, itemCount, nested) != itemCount) {
                    return false;
                }
                nested.commit();
                return true;
            }
        }

        private static long toEnergy(double artron) {
            return TardisFuelStorage.toEnergy(artron);
        }
    }
}
