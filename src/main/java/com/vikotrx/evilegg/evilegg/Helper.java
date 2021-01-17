package com.vikotrx.evilegg.evilegg;

import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.Vec3d;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Helper {
    public static Vec3d YawPitchToVec3d(Vec2d yawPitch){
        double xzLen = cos(yawPitch.y);
        return new Vec3d(xzLen * cos(yawPitch.x), sin(yawPitch.y), xzLen * sin(-yawPitch.x));
    }
}
