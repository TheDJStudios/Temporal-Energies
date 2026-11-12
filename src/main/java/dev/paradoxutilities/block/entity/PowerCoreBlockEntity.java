package dev.paradoxutilities.block.entity;

import dev.paradoxutilities.ParadoxUtilities;
import dev.paradoxutilities.tardis.TardisAccess;
import dev.paradoxutilities.tardis.TardisFuelStorage;
import dev.paradoxutilities.tardis.TardisAccessManager;
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
    public TardisAccess getTardis() {
        return world == null ? null : TardisAccessManager.find(world);
    }

    private final class TardisEnergyStorage extends SnapshotParticipant<TransferSnapshot> implements EnergyStorage {
        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            if (world == null) return 0;
            long accepted = TardisAccessManager.energyStorage(world).insert(maxAmount, transaction);
            if (accepted <= 0) return 0;
            updateSnapshots(transaction);
            refreshRates();
            inputThisTick += accepted;
            return accepted;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            if (world == null) return 0;
            long extracted = TardisAccessManager.energyStorage(world).extract(maxAmount, transaction);
            if (extracted <= 0) return 0;
            updateSnapshots(transaction);
            refreshRates();
            outputThisTick += extracted;
            return extracted;
        }

        @Override
        public long getAmount() {
            return world == null ? 0 : TardisAccessManager.energyStorage(world).getAmount();
        }

        @Override
        public long getCapacity() {
            TardisAccess tardis = getTardis();
            return tardis == null ? 0 : TardisFuelStorage.toEnergy(tardis.maximumFuel());
        }

        @Override
        public boolean supportsInsertion() {
            return getTardis() != null;
        }

        @Override
        public boolean supportsExtraction() {
            return getTardis() != null;
        }

        @Override
        protected TransferSnapshot createSnapshot() {
            return new TransferSnapshot(inputThisTick,
                    outputThisTick, inputLastTick, outputLastTick, rateTick);
        }

        @Override
        protected void readSnapshot(TransferSnapshot snapshot) {
            inputThisTick = snapshot.inputThisTick();
            outputThisTick = snapshot.outputThisTick();
            inputLastTick = snapshot.inputLastTick();
            outputLastTick = snapshot.outputLastTick();
            rateTick = snapshot.rateTick();
        }

    }

    private record TransferSnapshot(long inputThisTick, long outputThisTick,
                                    long inputLastTick, long outputLastTick, long rateTick) {}
}
