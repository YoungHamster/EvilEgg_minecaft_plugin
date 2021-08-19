package com.vikotrx.evilegg.evilegg;

import java.util.HashMap;

public class EggBlockStorage {
    private final HashMap<Vec3i, EggBlockData> blocks = new HashMap<>();

    public void put(Vec3i pos, EggBlockData blockData)
    {
        blocks.put(new Vec3i(pos), new EggBlockData(blockData));
    }

    public EggBlockData get(Vec3i pos)
    {
        return blocks.get(pos);
    }

    public int size()
    {
        return blocks.size();
    }

    public boolean containsKey(Vec3i key)
    {
        return blocks.containsKey(key);
    }
}
