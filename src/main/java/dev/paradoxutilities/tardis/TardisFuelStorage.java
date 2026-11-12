package dev.paradoxutilities.tardis;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

/** Shared by all cores in an interior; fuel side effects occur only on final commit. */
public final class TardisFuelStorage extends SnapshotParticipant<Long> implements EnergyStorage {
    public static final long ENERGY_PER_ARTRON = 100L;
    private final Supplier<TardisAccess> provider;
    private long pendingEnergy;

    public TardisFuelStorage(Supplier<TardisAccess> provider) {
        this.provider = provider;
    }

    public static long toEnergy(double fuel) {
        // Correct the rounding of values such as 0.29 AU without rounding up fractional FE.
        return Math.max(0L, (long) Math.floor(Math.nextUp(fuel * ENERGY_PER_ARTRON)));
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        TardisAccess tardis = provider.get();
        if (tardis == null || maxAmount <= 0) return 0;
        double available = tardis.maximumFuel() * ENERGY_PER_ARTRON
                - (tardis.fuel() * ENERGY_PER_ARTRON + pendingEnergy);
        long amount = Math.min(maxAmount, Math.max(0L, (long) Math.floor(Math.nextUp(available))));
        if (amount <= 0) return 0;
        updateSnapshots(transaction);
        pendingEnergy += amount;
        return amount;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        TardisAccess tardis = provider.get();
        if (tardis == null || maxAmount <= 0) return 0;
        long amount = Math.min(maxAmount, Math.max(0L, toEnergy(tardis.fuel()) + pendingEnergy));
        if (amount <= 0) return 0;
        updateSnapshots(transaction);
        pendingEnergy -= amount;
        return amount;
    }

    @Override public long getAmount() {
        TardisAccess tardis = provider.get();
        return tardis == null ? 0 : Math.max(0, toEnergy(tardis.fuel()) + pendingEnergy);
    }
    @Override public long getCapacity() {
        TardisAccess tardis = provider.get();
        return tardis == null ? 0 : toEnergy(tardis.maximumFuel());
    }
    @Override public boolean supportsInsertion() { return provider.get() != null; }
    @Override public boolean supportsExtraction() { return provider.get() != null; }
    @Override protected Long createSnapshot() { return pendingEnergy; }
    @Override protected void readSnapshot(Long snapshot) { pendingEnergy = snapshot; }

    @Override
    protected void onFinalCommit() {
        long delta = pendingEnergy;
        pendingEnergy = 0;
        TardisAccess tardis = provider.get();
        if (tardis != null && delta != 0) {
            tardis.setFuel((tardis.fuel() * ENERGY_PER_ARTRON + delta) / ENERGY_PER_ARTRON);
        }
    }
}
