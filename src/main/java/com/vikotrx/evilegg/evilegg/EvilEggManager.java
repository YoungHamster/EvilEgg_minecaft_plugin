package com.vikotrx.evilegg.evilegg;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Vector;

public class EvilEggManager extends BukkitRunnable
{
    private final JavaPlugin plugin;

    private final Vector<SingleEvilEgg> singleEvilEggs = new Vector<>();
    private final EggEnergyStorage eggEnergyData = new EggEnergyStorage();
    private final HashMap<Vec3i, Material> energyBlocksReplace = new HashMap<>();
    private boolean showEnergyBlocks = true;
    //private List<SingleEvilEgg> eggs = new ArrayList<SingleEvilEgg>();

    public EvilEggManager(JavaPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        // Evil logic of an evil egg
        for(SingleEvilEgg egg: singleEvilEggs)
        {
            if(!egg.LifeTick())
            {
                singleEvilEggs.remove(egg);
            }
            showAllEnergyBlocks();
        }
    }

    public void AddEgg(Vec3i blockPos, World world)
    {
        singleEvilEggs.add(new SingleEvilEgg(blockPos, world, eggEnergyData));
    }

    public synchronized void KillEggs()
    {
        singleEvilEggs.clear();
    }

    public synchronized void PauseEggs()
    {
        for(SingleEvilEgg egg:singleEvilEggs)
            egg.pause = true;
    }

    public synchronized void UnpauseEggs()
    {
        for(SingleEvilEgg egg:singleEvilEggs)
            egg.pause = false;
    }

    public boolean isEggBlock(Vec3i pos)
    {
        for(SingleEvilEgg egg: singleEvilEggs)
        {
            if(egg.isEggBlock(pos)) return true;
        }
        return false;
    }

    public synchronized double getEnergyCount()
    {
        return eggEnergyData.GetTotalEnergy();
    }

    public synchronized int getEnergyBlocksCount()
    {
        return eggEnergyData.GetNumberOfEnergyBlocks();
    }

    public synchronized String getAllEggStats()
    {
        return "Number of energy signals: " + singleEvilEggs.get(0).getNumberOfEnergySignals() +
                ", number of blocks: " + singleEvilEggs.get(0).getNumberOfBlocks() +
                ", average energy signal life left: "+ singleEvilEggs.get(0).getAverageSignalLifeLeftInTicks() + "ticks";
    }

    public synchronized void startShowingEnergyBlocks()
    {
        showEnergyBlocks = true;
    }

    public synchronized void stopShowingEnergyBlocks()
    {
        showEnergyBlocks = false;
        for(Vec3i pos:energyBlocksReplace.keySet())
        {
            singleEvilEggs.get(0).getWorld().getBlockAt(pos.x, pos.y, pos.z).setType(energyBlocksReplace.get(pos));
        }
        energyBlocksReplace.clear();
    }

    private void showAllEnergyBlocks()
    {
        for(Vec3i key:eggEnergyData.keySet())
        {
            if(!energyBlocksReplace.containsKey(key))
                energyBlocksReplace.put(key, singleEvilEggs.get(0).getWorld().getBlockAt(key.x, key.y, key.z).getType());
            if(eggEnergyData.get(key) < 0.5d)
            {
                singleEvilEggs.get(0).getWorld().getBlockAt(key.x, key.y, key.z).setType(Material.BLUE_STAINED_GLASS);
                continue;
            }
            if(eggEnergyData.get(key) < 2.0d)
            {
                singleEvilEggs.get(0).getWorld().getBlockAt(key.x, key.y, key.z).setType(Material.GREEN_STAINED_GLASS);
                continue;
            }
            if(eggEnergyData.get(key) < 10.0d)
            {
                singleEvilEggs.get(0).getWorld().getBlockAt(key.x, key.y, key.z).setType(Material.YELLOW_STAINED_GLASS);
                continue;
            }
            singleEvilEggs.get(0).getWorld().getBlockAt(key.x, key.y, key.z).setType(Material.RED_STAINED_GLASS);
        }
    }
}
