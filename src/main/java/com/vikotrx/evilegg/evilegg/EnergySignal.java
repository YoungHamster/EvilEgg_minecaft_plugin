package com.vikotrx.evilegg.evilegg;

import org.jetbrains.annotations.Nullable;

public class EnergySignal
{
    public Vec3i pos;
    public int ageInTicks = 0; // expected to kill energy signal after a few ticks
    public final int lifespanInTicks;

    public enum Directions
    {
        Next,
        Prev,
        Default
    }
    public final Directions direction;
    public EnergySignal(Vec3i pos, @Nullable Integer lifespanInTicks, Directions direction)
    {
        this.pos = new Vec3i(pos);
        if(lifespanInTicks != null)
            this.lifespanInTicks = lifespanInTicks;
        else
            this.lifespanInTicks = 3; // default lifespan
        this.direction = direction;
    }
}
