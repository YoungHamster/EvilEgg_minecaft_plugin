package com.vikotrx.evilegg.evilegg;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class EvilEggManager extends BukkitRunnable
{
    private final JavaPlugin plugin;

    private List<SingleEvilEgg> eggs = new ArrayList<SingleEvilEgg>();

    public EvilEggManager(JavaPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        // Evil logic of an evil egg
        for(SingleEvilEgg egg:eggs)
        {
            if(!egg.LifeTick())
            {
                eggs.remove(egg);
            }
        }
    }

    public void addEgg(Vec3i blockPos, World world)
    {
        eggs.add(new SingleEvilEgg(blockPos, world));
    }

    public boolean isEggBlock(Vec3i pos)
    {
        for(SingleEvilEgg egg:eggs)
        {
            if(egg.isEggBlock(pos)) return true;
        }
        return false;
    }
}
