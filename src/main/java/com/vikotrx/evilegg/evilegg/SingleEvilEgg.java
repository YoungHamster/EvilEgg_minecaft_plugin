package com.vikotrx.evilegg.evilegg;

import javafx.util.Pair;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.jetbrains.annotations.Nullable;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.sqrt;

public class SingleEvilEgg
{
    private final EggBlockStorage eggBlocks = new EggBlockStorage();

    private final EggEnergyStorage eggEnergy;
    // Energy is generated in bursts-like regular bursts of energy from the egg itself
    // or energy from consuming a block-to simplify simulation, only some of blocks with egg's energy
    // (expected to be the blocks that have the most energy) are simulated every tick
    private final List<EnergySignal> energySignals = new ArrayList<>();
    private final double minimalEnergyToStartANewSignal = 3.0d;
    private final double minimalEnergyToCreateExplosion = 9999.0d;//54.0d;

    public int getNumberOfEnergySignals() { return energySignals.size(); }
    public int getNumberOfBlocks() { return eggBlocks.size(); }
    public double getAverageSignalLifeLeftInTicks()
    {
        double total = 0;
        for(EnergySignal es: energySignals)
        {
            total += es.lifespanInTicks - es.ageInTicks;
        }
        return total / (double) energySignals.size();
    }

    private final List<EggAppendageEnding> appendagesEndings = new ArrayList<>();
    private final Vec3i heart;
    private final World world;

    public World getWorld() {
        return world;
    }

    private long tickCounter = 0;

    public boolean pause = false;

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

    public SingleEvilEgg(Vec3i seedPosition, World world, EggEnergyStorage eggEnergy)
    {
        this.eggEnergy = eggEnergy;
        heart = new Vec3i(seedPosition);
        this.world = world;
        Vec3i blockUnderSeed = new Vec3i(seedPosition.x, seedPosition.y - 1, seedPosition.z);
        eggBlocks.put(seedPosition, new EggBlockData(null, blockUnderSeed, null));
        eggBlocks.put(blockUnderSeed, new EggBlockData(seedPosition, null, null));

        appendagesEndings.add(new EggAppendageEnding(blockUnderSeed, null, null));

        eggEnergy.put(heart, 10.0d);
        eggEnergy.put(blockUnderSeed, 1.0d);
    }

    // Returns false if egg was completely destroyed
    public boolean LifeTick()
    {
        if(pause) return true;
        if(eggBlocks.size() == 0) return false;
        EnergySignalsTick();
        StageDependantStuff();
        if(tickCounter % 10 == 0)
            AppendagesEndingsGrowth();

        if(tickCounter % 20 == 0)// every second heart pulses
        {
            energySignals.add(new EnergySignal(heart, eggBlocks.size(), EnergySignal.Directions.Next));
            eggEnergy.put(heart, eggEnergy.get(heart) + 200.0d);
        }

        tickCounter++;

        return true;
    }

    public boolean isEggBlock(Vec3i pos)
    {
        return eggBlocks.containsKey(pos);
    }

    private void EnergySignalsTick()
    {
        List<EnergySignal> newEnergySignals = new ArrayList<>();
        List<EnergySignal> removedEnergySignals = new ArrayList<>();
        for (EnergySignal eSignal:energySignals)
        {
            if(eSignal.ageInTicks % (10 + (int) EvilEgg.rng.nextDouble() * 10) == 0)
                world.playSound(new Location(world, eSignal.pos.x, eSignal.pos.y, eSignal.pos.z),
                        Sound.BLOCK_NOTE_BLOCK_BIT, (float) sqrt(sqrt(eggEnergy.get(eSignal.pos)))*0.01f, 1.0f);
            if(eSignal.ageInTicks >= eSignal.lifespanInTicks || eggEnergy.get(eSignal.pos) <= 1.0d)
            {
                removedEnergySignals.add(eSignal);
                continue;
            }
            if (eggEnergy.get(eSignal.pos) >= minimalEnergyToCreateExplosion)
            {
                newEnergySignals.addAll(EnergyExplosion(eSignal.pos));
                if(eggEnergy.get(eSignal.pos) <= 1)
                {
                    removedEnergySignals.add(eSignal);
                    continue;
                }
            }
            if(eggBlocks.containsKey(eSignal.pos))
            {
                if(eggEnergy.get(eSignal.pos) >= minimalEnergyToStartANewSignal * 10)
                {
                    newEnergySignals.add(new EnergySignal(eSignal.pos,
                            eSignal.lifespanInTicks - eSignal.ageInTicks, eSignal.direction));
                }
                switch (eSignal.direction)
                {
                    case Next:
                        Vec3i nextPos1 = eggBlocks.get(eSignal.pos).getNextBlock1();
                        Vec3i nextPos2 = eggBlocks.get(eSignal.pos).getNextBlock2();
                        if(nextPos1 == null && nextPos2 == null)
                            break;
                        if(nextPos2 == null)
                            MoveEnergyInsideEgg(eSignal.pos, nextPos1);
                        else
                        {
                            eggEnergy.IncreaseEnergy(nextPos1,
                                 eggEnergy.get(eSignal.pos) * 0.45d);
                            eggEnergy.IncreaseEnergy(nextPos2,
                                 eggEnergy.get(eSignal.pos) * 0.45d);
                            eggEnergy.put(eSignal.pos, eggEnergy.get(eSignal.pos) * 0.1d);
                            if(eggEnergy.get(nextPos2) >= minimalEnergyToStartANewSignal)
                                newEnergySignals.add(new EnergySignal(nextPos2,
                                            eSignal.lifespanInTicks - eSignal.ageInTicks, EnergySignal.Directions.Next));
                        }
                        eSignal.pos = nextPos1;
                        break;
                    case Prev:
                        Vec3i newPosPrev = eggBlocks.get(eSignal.pos).getPrevBlock();
                        MoveEnergyInsideEgg(eSignal.pos, newPosPrev);
                        eSignal.pos = newPosPrev;
                        break;
                    default: removedEnergySignals.add(eSignal); continue;
                }
            }
            else
            {
                List<Vec3i> blocksToMove = CalcBestBlocksForFreeEnergyToMove(eSignal.pos);
                newEnergySignals.addAll(MoveEnergyToMultipleBlocks(eSignal.pos, 0.9d, blocksToMove));
            }
            eSignal.ageInTicks++;
        }
        energySignals.removeAll(removedEnergySignals);
        energySignals.addAll(newEnergySignals);
    }

    private List<Vec3i> CalcBestBlocksForFreeEnergyToMove(Vec3i energyPos)
    {
        List<Vec3i> bestBlocks = new ArrayList<>();
        if(!world.getBlockAt(energyPos.x, energyPos.y - 1, energyPos.z).getType().isSolid())
            bestBlocks.add(new Vec3i(energyPos.x, energyPos.y - 1, energyPos.z));
        else {
            List<Vec3i> notEggBlocks = new ArrayList<>();
            Vec3i tempPos = new Vec3i(energyPos);
            for(int x = 0 ; x < 3; x++)
            {
                for(int y = 0 ; y < 3; y++)
                {
                    for(int z = 0 ; z < 3; z++)
                    {
                        if(tempPos != energyPos)
                            if(!eggBlocks.containsKey(tempPos))
                                notEggBlocks.add(tempPos);
                        tempPos.z += 1;
                    }
                    tempPos.y += 1;
                }
                tempPos.x += 1;
            }
            for(Vec3i pos:notEggBlocks)
            {
                if(world.getBlockAt(pos.x, pos.y, pos.z).getType().isAir() &&
                        world.getBlockAt(pos.x, pos.y, pos.z).getType().isBlock())
                    bestBlocks.add(pos);
            }
            if(bestBlocks.size() > 0)
                return bestBlocks;
            else
                return notEggBlocks;
        }

        return bestBlocks;
    }

    private List<EnergySignal> MoveEnergyToMultipleBlocks(Vec3i energySrc, double percentageToMove, List<Vec3i> where)
    {
        List<EnergySignal> newEnergySignals = new ArrayList<>();
        for(Vec3i pos:where)
        {
            eggEnergy.IncreaseEnergy(pos, eggEnergy.get(energySrc) * percentageToMove / where.size());
            if(eggEnergy.get(pos) >= minimalEnergyToStartANewSignal)
                newEnergySignals.add(new EnergySignal(pos, null, EnergySignal.Directions.Default));
        }
        if(where.size() > 0)
            eggEnergy.put(energySrc, eggEnergy.get(energySrc) * (1 - percentageToMove));
        return newEnergySignals;
    }

    private void MoveEnergyInsideEgg(Vec3i oldPos, Vec3i newPos)
    {
        if (eggEnergy.get(oldPos) >= 10.0d)
        {
            eggEnergy.put(newPos, eggEnergy.get(oldPos) * 0.9d + eggEnergy.get(newPos));
            eggEnergy.put(oldPos, eggEnergy.get(oldPos) * 0.1d);
        } else {
            eggEnergy.put(newPos, eggEnergy.get(oldPos) - 1.0d + eggEnergy.get(newPos));
            eggEnergy.put(oldPos, 1.0d);
        }
    }

    private List<EnergySignal> EnergyExplosion(Vec3i pos)
    {
        eggEnergy.put(pos, eggEnergy.get(pos) - 26.0d);
        List<EnergySignal> newEnergySignals = new ArrayList<>();
        Vec3i tempPos = new Vec3i(pos.x - 1, pos.y - 1, pos.z - 1);
        for(int x = 0 ; x < 3; x++)
        {
            for(int y = 0 ; y < 3; y++)
            {
                for(int z = 0 ; z < 3; z++)
                {
                    if(tempPos.equals(pos))
                    {
                        tempPos.z += 1;
                        continue;
                    }
                    if(!eggEnergy.containsKey(tempPos))
                        eggEnergy.put(tempPos, 1.0d);
                    else
                    {
                        eggEnergy.put(tempPos, eggEnergy.get(tempPos) + 1.0d);
                        if(eggEnergy.get(tempPos) >= minimalEnergyToStartANewSignal)
                            newEnergySignals.add(new EnergySignal(tempPos, null, EnergySignal.Directions.Default));
                    }
                    tempPos.z += 1;
                }
                tempPos.y += 1;
            }
            tempPos.x += 1;
        }
        return newEnergySignals;
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
        List<EggAppendageEnding> newAppendages = new ArrayList<>();
        for(EggAppendageEnding appEnd:appendagesEndings)
        {
            EggAppendageEnding newAppEnd = AppendageEndingGrowth(appEnd);
            if(newAppEnd != null) newAppendages.add(newAppEnd);
        }
        appendagesEndings.addAll(newAppendages);
    }

    private @Nullable EggAppendageEnding AppendageEndingGrowth(EggAppendageEnding appEnd)
    {
        Vec3i bestBlockToGrow = CalcBestBlockToGrow(appEnd.position, appEnd.position);
        if(bestBlockToGrow == null) return null;
        appEnd.prevPos = appEnd.position;
        appEnd.position = bestBlockToGrow;
        appEnd.growthTicksSinceLastDivision += 1;

        PutRandomEggBlock(bestBlockToGrow, new EggBlockData(appEnd.prevPos, null, null));

        eggBlocks.get(appEnd.prevPos).SetNextBlock1(bestBlockToGrow);
        // Sometimes appendage splits into two
        if(appEnd.growthTicksSinceLastDivision > 10)
        {
            bestBlockToGrow = CalcBestBlockToGrow(appEnd.prevPos, appEnd.position);
            if(bestBlockToGrow == null) return null;
            PutRandomEggBlock(bestBlockToGrow, new EggBlockData(appEnd.prevPos, null, null));
            eggBlocks.get(appEnd.prevPos).SetNextBlock2(bestBlockToGrow);

            appEnd.growthTicksSinceLastDivision = 0;
            return new EggAppendageEnding(bestBlockToGrow, appEnd.prevPos, null);
        }
        return null;
    }

    private Vec3i CalcBestBlockToGrow(Vec3i blockToGrowFrom, @Nullable Vec3i appendage)
    {
        Pair<Vec3i, Double> bestBlockToGrow = new Pair<>(blockToGrowFrom, 0.0d);
        for(int i = 0; i < 6; i++)
        {
            Vec3i probeBlock = new Vec3i(blockToGrowFrom);
            switch (i)
            {
                case 0: probeBlock.x += 1; break;
                case 1: probeBlock.x -= 1; break;
                case 2: probeBlock.y += 1; break;
                case 3: probeBlock.y -= 1; break;
                case 4: probeBlock.z += 1; break;
                case 5: probeBlock.z -= 1; break;
            }
            double suitability = CalcBlockSuitabilityToGrow(probeBlock, appendage);
            if(suitability > bestBlockToGrow.getValue())
                bestBlockToGrow = new Pair<>(new Vec3i(probeBlock), suitability);
        }
        if(bestBlockToGrow.getValue() == 0.0d) return null;
        return bestBlockToGrow.getKey();
    }

    // Calculates how suitable block(block that isn't part of an egg) is to place there egg block
    // appendage should be the Vec3i of the appendageEnding that calls this method
    private double CalcBlockSuitabilityToGrow(Vec3i block, @Nullable Vec3i appendage)
    {
        Material blockType = world.getBlockAt(block.x, block.y, block.z).getType();
        if(blockType == Material.VOID_AIR || blockType == Material.OBSIDIAN || blockType == Material.CRYING_OBSIDIAN
        || blockType == Material.BEDROCK || eggBlocks.containsKey(block))
            return 0;
        // Step 1
        double suitability = block.DistanceToVec3i(heart);

        // Step 2
        Pair<Vec3i, Double> closestAppEnding = new Pair<>(new Vec3i(), 0.0d);
        for(EggAppendageEnding appEnd:appendagesEndings)
        {
            if(appendage != null)
                if(appendage == appEnd.position) continue; // avoid calculating distance to the appendage that called this method
            double endingDist = block.DistanceToVec3i(appEnd.position);
            if(closestAppEnding.getValue() > endingDist)
            {
                closestAppEnding = new Pair<>(new Vec3i(appEnd.position), endingDist);
            }
        }
        suitability += closestAppEnding.getValue() * suitability * 2;

        // Step 3
        // Making appendages attracted to ground
        Material oneBlockUnderType = world.getBlockAt(block.x, block.y - 1, block.z).getType();
        if(oneBlockUnderType.isAir())
            suitability *= 0.2;
        if(eggBlocks.containsKey(new Vec3i(block.x, block.y - 1, block.z)))
            suitability *= 0.1;
        suitability += (1.0d / (block.y * 50.0d)) * suitability;

        // Step 4
        if(!blockType.isAir())
            suitability *= 0.2;

        // Randomizing
        suitability += EvilEgg.rng.nextDouble() * suitability * 3;
        return suitability;
    }

    private void PutRandomEggBlock(Vec3i where, EggBlockData eggBlockData)
    {
        world.getBlockAt(where.x, where.y, where.z).setType(EvilEgg.getRandomEggBlock());
        world.playSound(new Location(world, where.x, where.y, where.z),
                Sound.BLOCK_HONEY_BLOCK_BREAK,
                0.5f, 1.0f);
        eggBlocks.put(where, eggBlockData);
        eggEnergy.put(where, 1.0d);
    }
}
