package com.vikotrx.evilegg.evilegg;

import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.Vec3d;
import javafx.util.Pair;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.abs;

public class SingleEvilEgg
{
    private HashMap<Vec3i, EggBlockData> eggBlocks = new HashMap<Vec3i, EggBlockData>();
    private List<Vec3iVec2d> appendagesEndings = new ArrayList<Vec3iVec2d>();
    private Vec3i heart;
    private World world;

    private enum Stages
    { // in the correct order
        Sprout,
        Egg,
        Mycelium,
        FlowerGrowth,
        Polination, // obviously self polination
        SeedFormation,
        AllEggBlocksStartTurningIntoLava // because at the moment idk what's really going to happen on the smp
    }
    Stages stage = Stages.Sprout;
    public SingleEvilEgg(Vec3i seedPosition, World world)
    {
        heart = seedPosition;
        eggBlocks.put(seedPosition, new EggBlockData(100.0d));
        this.world = world;
        Vec3i blockUnderSeed = new Vec3i(seedPosition.x, seedPosition.y - 1, seedPosition.z);
        eggBlocks.put(blockUnderSeed, new EggBlockData());

        appendagesEndings.add(new Vec3iVec2d(seedPosition,
                new Vec2d(EvilEgg.rng.nextDouble() * 180, EvilEgg.rng.nextDouble() * 180)));
    }

    // Returns false if egg was completely destroyed
    public boolean LifeTick()
    {
        if(eggBlocks.size() == 0) return false;
        StageDependantStuff();
        AppendagesEndingsGrowth();

        // Sometimes appendage splits into two

        return true;
    }

    public boolean isEggBlock(Vec3i pos)
    {
        return eggBlocks.containsKey(pos);
    }

    private void StageDependantStuff()
    {
        switch (stage)
        {
            case Sprout: if(eggBlocks.size() > 50)
            {
                world.getPlayers().get(0).sendMessage("Egg reached \"Egg\" stage");
                stage = Stages.Egg;
            }
                break;
            case Egg: if(eggBlocks.size() > 150)
            {
                world.getPlayers().get(0).sendMessage("Egg reached \"Mycelium\" stage");
                stage = Stages.Mycelium;
            }
                break;
            case Mycelium: if(eggBlocks.size() > 350)
            {
                world.getPlayers().get(0).sendMessage("Egg reached \"FlowerGrowth\" stage");
                stage = Stages.FlowerGrowth;
            }
                break;
            case FlowerGrowth: if(eggBlocks.size() > 700)
            {
                world.getPlayers().get(0).sendMessage("Egg reached \"Polination\" stage");
                stage = Stages.Polination;
            }
                break;
            case Polination: if(eggBlocks.size() > 1200)
            {
                world.getPlayers().get(0).sendMessage("Egg reached \"SeedFormation\" stage");
                stage = Stages.SeedFormation;
            }
                break;
            case SeedFormation: if(eggBlocks.size() > 3000)
            {
                world.getPlayers().get(0).sendMessage("Egg reached \"AllEggBlocksStartTurningIntoLava\" stage");
                stage = Stages.AllEggBlocksStartTurningIntoLava;
            }
                break;
        }
    }

    private void AppendagesEndingsGrowth()
    {
        for(Vec3iVec2d appEnd:appendagesEndings)
        {
            if(EvilEgg.rng.nextDouble() > 0.5d)
            {
                appEnd.vec2d.x += EvilEgg.rng.nextDouble() * 30; // max difference 30 degrees of yaw and pitch
                appEnd.vec2d.y += EvilEgg.rng.nextDouble() * 30;
            }
            Vec3d directionVec = Helper.YawPitchToVec3d(appEnd.vec2d);

            Vec3i newAppendageEndingPos = appEnd.vec3i;
            if(abs(directionVec.x) > abs(directionVec.y))
            {
                if(abs(directionVec.x) > abs(directionVec.z))
                {
                    if(directionVec.x > 0) newAppendageEndingPos.x += 1;
                    else newAppendageEndingPos.x -= 1;
                }
                else
                {
                    if(directionVec.z > 0) newAppendageEndingPos.z += 1;
                    else newAppendageEndingPos.z -= 1;
                }
            }
            else
            {
                if(abs(directionVec.y) > abs(directionVec.z))
                {
                    if(directionVec.y > 0) newAppendageEndingPos.y += 1;
                    else newAppendageEndingPos.y-= 1;
                }
                else
                {
                    if(directionVec.z > 0) newAppendageEndingPos.z += 1;
                    else newAppendageEndingPos.z -= 1;
                }
            }
            PutRandomEggBlock(newAppendageEndingPos, new EggBlockData());
            appEnd.vec3i = newAppendageEndingPos;
        }
    }

    // Calculates how suitable block(block that isn't part of an egg) is to place there egg block
    // appendage should be the Vec3i of the appendageEnding that calls this method
    private double CalcBlockSuitabilityToGrow(Vec3i block, Vec3i appendage)
    {
        Material blockType = world.getBlockAt(block.x, block.y, block.z).getType();
        if(blockType == Material.VOID_AIR || blockType == Material.OBSIDIAN || blockType == Material.CRYING_OBSIDIAN)
            return 0;

        // Step 1
        double suitability = block.DistanceToVec3i(heart);

        // Step 2
        Pair<Vec3i, Double> closestAppEnding = new Pair<Vec3i, Double>(new Vec3i(), new Double(0));
        for(Vec3iVec2d appEnd:appendagesEndings)
        {
            if(appendage == appEnd.vec3i) continue; // avoid calculating distance to the appendage that called this method
            double endingDist = block.DistanceToVec3i(appEnd.vec3i);
            if(closestAppEnding.getValue() > endingDist)
            {
                closestAppEnding = new Pair<Vec3i, Double>(new Vec3i(appEnd.vec3i), new Double(endingDist));
            }
        }
        suitability += closestAppEnding.getValue();

        // Step 3
        Material oneBlockUnderType = world.getBlockAt(block.x, block.y - 1, block.z).getType();
        if(oneBlockUnderType.isAir())
            suitability *= 0.9;

        // Step 4
        if(!blockType.isAir())
            suitability *= 0.2;
        return suitability;
    }

    private void PutRandomEggBlock(Vec3i where, EggBlockData eggBlockData)
    {
        world.getBlockAt(where.x, where.y, where.z).setType(EvilEgg.getRandomEggBlock());
        eggBlocks.put(where, eggBlockData);
    }
}
