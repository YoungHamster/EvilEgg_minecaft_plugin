package com.vikotrx.evilegg.evilegg;

import java.util.HashMap;
import java.util.Set;

public class EggEnergyStorage {
    private final HashMap<Vec3i, Double> energyData = new HashMap<>();

    public void put(Vec3i key, Double value)
    {
        energyData.put(new Vec3i(key), value);
    }

    public Double get(Vec3i key)
    {
        return energyData.get(key);
    }

    public boolean containsKey(Vec3i key)
    {
        return energyData.containsKey(key);
    }

    public Set<Vec3i> keySet()
    {
        return energyData.keySet();
    }

    public double GetTotalEnergy()
    {
        double total = 0;
        for(Double d:energyData.values())
            total += d;
        return total;
    }

    public int GetNumberOfEnergyBlocks()
    {
        return energyData.size();
    }

    public void IncreaseEnergy(Vec3i where, double energy)
    {
        if(energyData.containsKey(where))
            put(where, get(where) + energy);
        else
            put(where, energy);
    }
}
