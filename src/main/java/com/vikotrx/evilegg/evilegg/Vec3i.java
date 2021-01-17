package com.vikotrx.evilegg.evilegg;

import static java.lang.Math.sqrt;

public class Vec3i {
    public int x;
    public int y;
    public int z;

    public Vec3i(){}

    public Vec3i(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3i(Vec3i vec)
    {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public double DistanceToVec3i(Vec3i vec)
    {
        return sqrt(
                (x - vec.x)*(x - vec.x) +
                (y - vec.y)*(y - vec.y) +
                (z - vec.z)*(z - vec.z));
    }
}
