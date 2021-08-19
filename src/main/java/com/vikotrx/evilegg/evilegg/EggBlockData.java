package com.vikotrx.evilegg.evilegg;

import org.jetbrains.annotations.Nullable;

public class EggBlockData
{
    private Vec3i prevBlock = null;
    private Vec3i nextBlock1 = null; // 2 possible next blocks cuz appendages can split into 2
    private Vec3i nextBlock2 = null;

    public EggBlockData(EggBlockData data)
    {
        if (data.prevBlock != null)
            this.prevBlock = new Vec3i(data.prevBlock);
        if (data.nextBlock1 != null)
            this.nextBlock1 = new Vec3i(data.nextBlock1);
        if (data.nextBlock2 != null)
            this.nextBlock2 = new Vec3i(data.nextBlock2);
    }

    public EggBlockData(@Nullable Vec3i prevBlock, @Nullable Vec3i nextBlock1, @Nullable Vec3i nextBlock2)
    {
        if (prevBlock != null)
            this.prevBlock = new Vec3i(prevBlock);
        if (nextBlock1 != null)
            this.nextBlock1 = new Vec3i(nextBlock1);
        if (nextBlock2 != null)
            this.nextBlock2 = new Vec3i(nextBlock2);
    }

    public void SetNextBlock1(Vec3i nextBlock1)
    {
        this.nextBlock1 = new Vec3i(nextBlock1);
    }

    public void SetNextBlock2(Vec3i nextBlock2)
    {
        this.nextBlock2 = new Vec3i(nextBlock2);
    }

    Vec3i getPrevBlock()
    {
        return prevBlock;
    }

    Vec3i getNextBlock1()
    {
        return nextBlock1;
    }

    Vec3i getNextBlock2()
    {
        return nextBlock2;
    }
}
