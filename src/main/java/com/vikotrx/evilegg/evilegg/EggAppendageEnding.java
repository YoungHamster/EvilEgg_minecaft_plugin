package com.vikotrx.evilegg.evilegg;

import org.jetbrains.annotations.Nullable;

public class EggAppendageEnding {
    private enum AppendageTypes
    {
        Default,
        Tentacle,
        Liana,
        RedLawn
    }

    public AppendageTypes type = AppendageTypes.Default;
    public Vec3i position;
    public Vec3i prevPos;
    public int growthTicksSinceLastDivision = 0;

    public EggAppendageEnding(Vec3i position, @Nullable Vec3i prevPos, @Nullable AppendageTypes type)
    {
        if(type != null)
            this.type = type;
        if(prevPos != null)
            this.prevPos = new Vec3i(prevPos);
        this.position = new Vec3i(position);
    }
}
