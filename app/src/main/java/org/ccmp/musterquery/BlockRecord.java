package org.ccmp.musterquery;

public class BlockRecord {
    private int id;
    private String name;
    private String blockCode;

    public BlockRecord(int id, String name, String blockCode) {
        this.id = id;
        this.name = name;
        this.blockCode = blockCode;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBlockCode() { return blockCode; }

    @Override
    public String toString() {
        return name;
    }
}
