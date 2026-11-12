package dev.paradoxutilities.tardis;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

/** Runs against Fabric's real transaction implementation, without a Minecraft server. */
public final class TardisFuelStorageChecks {
    public static void main(String[] args) {
        FakeTardis tardis = new FakeTardis();
        TardisFuelStorage storage = new TardisFuelStorage(() -> tardis);
        try (Transaction tx = Transaction.openOuter()) {
            equal(100, storage.extract(100, tx), "simulated emptying");
            equal(0, storage.getAmount(), "pending fuel visible to another core");
            equal(0, tardis.writes, "simulation must not call fuel setter");
        }
        equal(100, storage.getAmount(), "abort restores fuel");
        equal(0, tardis.writes, "abort must not call setter");
        try (Transaction outer = Transaction.openOuter()) {
            equal(30, storage.extract(30, outer), "outer extraction");
            try (Transaction nested = outer.openNested()) {
                equal(70, storage.extract(100, nested), "nested capacity limit");
            }
            equal(70, storage.getAmount(), "nested abort restores outer state");
            try (Transaction nested = outer.openNested()) {
                equal(20, storage.insert(20, nested), "nested insert");
                nested.commit();
            }
            equal(0, tardis.writes, "nested commit does not publish fuel");
            outer.commit();
        }
        equal(90, storage.getAmount(), "outer commit publishes net transfer");
        equal(1, tardis.writes, "one setter call per outer commit");
        try (Transaction tx = Transaction.openOuter()) {
            equal(10, storage.insert(Long.MAX_VALUE, tx), "capacity limit");
            tx.commit();
        }
        equal(100, storage.getAmount(), "full reservoir");
        try (Transaction tx = Transaction.openOuter()) {
            equal(0, storage.insert(1, tx), "no overfill");
            equal(0, storage.extract(-1, tx), "negative request");
        }
        int writes = tardis.writes;
        try (Transaction tx = Transaction.openOuter()) {
            storage.extract(100, tx);
            storage.insert(100, tx);
            tx.commit();
        }
        equal(writes, tardis.writes, "net-zero transaction has no fuel side effects");
        try (Transaction outer = Transaction.openOuter()) {
            try (Transaction nested = outer.openNested()) {
                storage.extract(100, nested);
                nested.commit();
            }
        }
        equal(100, storage.getAmount(), "outer abort undoes nested commit");
        equal(writes, tardis.writes, "outer abort has no side effects");
        tardis.fuel = 0.29;
        equal(29, storage.getAmount(), "fractional AU rounding");
        try (Transaction tx = Transaction.openOuter()) {
            equal(29, storage.extract(29, tx), "fractional AU extraction");
            tx.commit();
        }
        equal(0, storage.getAmount(), "exact depletion");
        tardis.fuel = 0.005;
        try (Transaction tx = Transaction.openOuter()) {
            storage.insert(1, tx);
            tx.commit();
        }
        if (Math.abs(tardis.fuel - 0.015) > 1e-12) throw new AssertionError("fractional fuel lost");
        tardis.capacity = 50000;
        tardis.fuel = 49999.99;
        try (Transaction tx = Transaction.openOuter()) {
            equal(1, storage.insert(1, tx), "one FE below large capacity");
            tx.commit();
        }
        equal(5000000, storage.getAmount(), "large reservoir filled exactly");
        for (int i = 0; i < 100; i++) {
            try (Transaction tx = Transaction.openOuter()) {
                equal(1, storage.extract(1, tx), "single FE extraction");
                tx.commit();
            }
        }
        equal(4999900, storage.getAmount(), "repeated transfers conserve energy");
        TardisFuelStorage disconnected = new TardisFuelStorage(() -> null);
        try (Transaction tx = Transaction.openOuter()) {
            equal(0, disconnected.insert(100, tx), "disconnected insert");
            equal(0, disconnected.extract(100, tx), "disconnected extract");
        }
        equal(0, disconnected.getCapacity(), "disconnected capacity");
        if (disconnected.supportsInsertion()) throw new AssertionError("disconnected insertion supported");
        System.out.println("Fuel transaction regression checks passed");
    }

    private static void equal(long expected, long actual, String message) {
        if (expected != actual) throw new AssertionError(message + ": expected " + expected + ", got " + actual);
    }

    private static final class FakeTardis implements TardisAccess {
        double fuel = 1;
        double capacity = 1;
        int writes;
        @Override public String provider() { return "test"; }
        @Override public String name() { return "test"; }
        @Override public double fuel() { return fuel; }
        @Override public double maximumFuel() { return capacity; }
        @Override public void setFuel(double value) { writes++; fuel = value; }
        @Override public String overallStatus() { return "Idle"; }
        @Override public String flightStatus() { return "Landed"; }
        @Override public boolean refueling() { return false; }
        @Override public boolean crashing() { return false; }
        @Override public int flightProgress() { return 0; }
    }
}
