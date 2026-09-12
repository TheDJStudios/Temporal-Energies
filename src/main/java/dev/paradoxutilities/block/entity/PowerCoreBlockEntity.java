package dev.paradoxutilities.block.entity;

import dev.amble.ait.core.tardis.ServerTardis;
import dev.amble.ait.core.tardis.handler.FuelHandler;
import dev.amble.ait.core.world.TardisServerWorld;
import dev.paradoxutilities.ParadoxUtilities;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

/** A transactional, zero-buffer bridge between Fabric Energy and TARDIS AU. */
public final class PowerCoreBlockEntity extends BlockEntity {
    public static final long ENERGY_PER_ARTRON = 100L;

    private final TardisEnergyStorage energyStorage = new TardisEnergyStorage();
    private long inputThisTick;
    private long outputThisTick;
    private long inputLastTick;
    private long outputLastTick;
    private long rateTick = Long.MIN_VALUE;

    public PowerCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ParadoxUtilities.POWER_CORE_BLOCK_ENTITY, pos, state);
    }

    public EnergyStorage energyStorage() {
        return energyStorage;
    }

    public long getInputRate() {
        refreshRates();
        return inputThisTick > 0 ? inputThisTick : inputLastTick;
    }

    public long getOutputRate() {
        refreshRates();
        return outputThisTick > 0 ? outputThisTick : outputLastTick;
    }

    private void refreshRates() {
        if (world == null) return;
        long now = world.getTime();
        if (rateTick == Long.MIN_VALUE) {
            rateTick = now;
        } else if (rateTick != now) {
            inputLastTick = now == rateTick + 1 ? inputThisTick : 0;
            outputLastTick = now == rateTick + 1 ? outputThisTick : 0;
            inputThisTick = 0;
            outputThisTick = 0;
            rateTick = now;
        }
    }

    @Nullable
    public ServerTardis getTardis() {
        if (world instanceof TardisServerWorld tardisWorld) return tardisWorld.getTardis();
        return null;
    }

    private final class TardisEnergyStorage extends SnapshotParticipant<TransferSnapshot> implements EnergyStorage {
        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            FuelHandler fuel = getFuelHandler();
            if (fuel == null || maxAmount <= 0) return 0;

            long accepted = Math.min(maxAmount, toEnergy(Math.max(0.0D,
                    fuel.getMaxFuel() - fuel.getCurrentFuel())));
            if (accepted <= 0) return 0;

            updateSnapshots(transaction);
            fuel.setCurrentFuel(fuel.getCurrentFuel() + toArtron(accepted));
            refreshRates();
            inputThisTick += accepted;
            return accepted;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            FuelHandler fuel = getFuelHandler();
            if (fuel == null || maxAmount <= 0) return 0;

            long extracted = Math.min(maxAmount, toEnergy(fuel.getCurrentFuel()));
            if (extracted <= 0) return 0;

            updateSnapshots(transaction);
            fuel.setCurrentFuel(fuel.getCurrentFuel() - toArtron(extracted));
            refreshRates();
            outputThisTick += extracted;
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
        protected TransferSnapshot createSnapshot() {
            FuelHandler fuel = getFuelHandler();
            return new TransferSnapshot(fuel == null ? 0.0D : fuel.getCurrentFuel(), inputThisTick,
                    outputThisTick, inputLastTick, outputLastTick, rateTick);
        }

        @Override
        protected void readSnapshot(TransferSnapshot snapshot) {
            FuelHandler fuel = getFuelHandler();
            if (fuel != null) fuel.setCurrentFuel(snapshot.fuel());
            inputThisTick = snapshot.inputThisTick();
            outputThisTick = snapshot.outputThisTick();
            inputLastTick = snapshot.inputLastTick();
            outputLastTick = snapshot.outputLastTick();
            rateTick = snapshot.rateTick();
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

    private record TransferSnapshot(double fuel, long inputThisTick, long outputThisTick,
                                    long inputLastTick, long outputLastTick, long rateTick) {}
}
