package dev.temporalenergies.block.entity;

import dev.amble.ait.core.tardis.ServerTardis;
import dev.amble.ait.core.tardis.handler.FuelHandler;
import dev.amble.ait.core.world.TardisServerWorld;
import dev.temporalenergies.TemporalEnergies;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/**
 * A zero-buffer bridge between a Fabric energy network and AiT's main Artron
 * reservoir. Changes participate in Fabric Transfer API transactions, so a
 * simulated or aborted transfer restores the previous fuel level.
 */
public final class PowerTranslatorBlockEntity extends BlockEntity {
    public static final long ENERGY_PER_ARTRON = 100L;

    private final TardisEnergyStorage energyStorage = new TardisEnergyStorage();

    public PowerTranslatorBlockEntity(BlockPos pos, BlockState state) {
        super(TemporalEnergies.POWER_TRANSLATOR_BLOCK_ENTITY, pos, state);
    }

    public EnergyStorage energyStorage() {
        return energyStorage;
    }

    @Nullable
    public ServerTardis getTardis() {
        if (!(world instanceof ServerWorld serverWorld)) {
            return null;
        }

        if (serverWorld instanceof TardisServerWorld tardisWorld) {
            return tardisWorld.getTardis();
        }

        return null;
    }

    private final class TardisEnergyStorage extends SnapshotParticipant<Double> implements EnergyStorage {
        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            if (maxAmount <= 0) {
                return 0;
            }

            FuelHandler fuel = getFuelHandler();
            if (fuel == null) {
                return 0;
            }

            double availableAu = Math.max(0.0D, fuel.getMaxFuel() - fuel.getCurrentFuel());
            long accepted = Math.min(maxAmount, toEnergy(availableAu));
            if (accepted <= 0) {
                return 0;
            }

            updateSnapshots(transaction);
            fuel.setCurrentFuel(fuel.getCurrentFuel() + toArtron(accepted));
            return accepted;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            if (maxAmount <= 0) {
                return 0;
            }

            FuelHandler fuel = getFuelHandler();
            if (fuel == null) {
                return 0;
            }

            long extracted = Math.min(maxAmount, toEnergy(fuel.getCurrentFuel()));
            if (extracted <= 0) {
                return 0;
            }

            updateSnapshots(transaction);
            fuel.setCurrentFuel(fuel.getCurrentFuel() - toArtron(extracted));
            return extracted;
        }

        @Override
        public long getAmount() {
            FuelHandler fuel = getFuelHandler();
            return fuel == null ? 0 : toEnergy(fuel.getCurrentFuel());
        }

        @Override
        public long getCapacity() {
            FuelHandler fuel = getFuelHandler();
            return fuel == null ? 0 : toEnergy(fuel.getMaxFuel());
        }

        @Override
        public boolean supportsInsertion() {
            return getFuelHandler() != null;
        }

        @Override
        public boolean supportsExtraction() {
            return getFuelHandler() != null;
        }

        @Override
        protected Double createSnapshot() {
            FuelHandler fuel = getFuelHandler();
            return fuel == null ? 0.0D : fuel.getCurrentFuel();
        }

        @Override
        protected void readSnapshot(Double snapshot) {
            FuelHandler fuel = getFuelHandler();
            if (fuel != null) {
                fuel.setCurrentFuel(snapshot);
            }
        }

        @Nullable
        private FuelHandler getFuelHandler() {
            ServerTardis tardis = getTardis();
            return tardis == null ? null : tardis.fuel();
        }

        private long toEnergy(double artron) {
            return Math.max(0L, (long) Math.floor(artron * ENERGY_PER_ARTRON));
        }

        private double toArtron(long energy) {
            return energy / (double) ENERGY_PER_ARTRON;
        }
    }
}
