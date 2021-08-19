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

    public String getString()
    {
        return "x: " + x + "y: " + y + "z: " + z;
    }

    @Override
    public int hashCode()
    {
        return x + y + z;
    }

    @Override
    public boolean equals(Object vec)
    {
        if(!(vec instanceof Vec3i)) try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Vec3i vec3i = (Vec3i) vec;
        return this.x == vec3i.x && this.y == vec3i.y && this.z == vec3i.z;
    }
}
