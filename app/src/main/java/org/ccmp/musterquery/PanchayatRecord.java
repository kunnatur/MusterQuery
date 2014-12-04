package org.ccmp.musterquery;

public class PanchayatRecord {
    private int id;
    private String name;
    private String panchayatCode;
    private String blockCode;

    public PanchayatRecord(int id, String name, String panchayatCode, String blockCode) {
        this.id = id;
        this.name = name;
        this.panchayatCode = panchayatCode;
        this.blockCode = blockCode;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPanchayatCode() { return panchayatCode; }

    public String getBlockCode() { return blockCode; }

    @Override
    public String toString() {
        return name;
    }
}
