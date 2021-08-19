package com.vikotrx.evilegg.evilegg;

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

/* After world loads evil egg schedules async task that checks if all of its blocks are in place, then this task
* schedules sync task that runs every tick with life ticks of the egg */

public final class EvilEgg extends JavaPlugin implements Listener
{

    public static Random rng = new Random();
    private final EvilEggManager theEgg = new EvilEggManager(this);
    BukkitTask theEggTask = null;

    public static final List<Material> EvilEggBlocks = ImmutableList.of(
            Material.CRIMSON_ROOTS,
            Material.MAGMA_BLOCK,
            Material.REDSTONE_BLOCK,
            Material.NETHER_WART_BLOCK,
            Material.CRIMSON_NYLIUM,
            Material.RED_NETHER_BRICKS,
            Material.GLOWSTONE,
            Material.LAVA,
            Material.WEEPING_VINES);

    @Override
    public void onEnable()
    {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(event.getBlock().getType() == Material.CRIMSON_ROOTS &&
                isEggTypeBlock(
                        event.getBlock().getWorld().getBlockAt(
                                event.getBlock().getX(),
                                event.getBlock().getY() - 1,
                                event.getBlock().getZ()).getType()))
        {
            theEgg.AddEgg(new Vec3i(event.getBlock().getX(),
                    event.getBlock().getY(),
                    event.getBlock().getZ()),
                    event.getBlock().getWorld());
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "YOU UNLEASHED THE EVIL!");
            if(theEggTask == null)
                theEggTask = theEgg.runTaskTimer(this, 1, 1);
        }
        else
        {
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "Nothing bad happened");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if(!isEggTypeBlock(event.getBlock().getType())) return;
        if(!theEgg.isEggBlock(new Vec3i(event.getBlock().getX(),
                event.getBlock().getY(),
                event.getBlock().getZ()))){
            return;
        }
        // TODO BlockBreakLogic
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, @NotNull String[] args)
    {
        if(command.getName().equals("ke"))
        {
            theEgg.KillEggs();
            sender.sendMessage(ChatColor.DARK_RED + "Killed eggs");
        }
        if(command.getName().equals("pe"))
        {
            theEgg.PauseEggs();
        }
        if(command.getName().equals("ue"))
        {
            theEgg.UnpauseEggs();
        }
        if(command.getName().equals("ges"))
        {
            sender.sendMessage(ChatColor.BOLD + "Total energy: " + theEgg.getEnergyCount() +
                    ", number of energy blocks: " + theEgg.getEnergyBlocksCount());
        }
        if(command.getName().equals("gaes"))
        {
            sender.sendMessage(ChatColor.BOLD + theEgg.getAllEggStats());
        }
        if(command.getName().equals("showebs"))
        {
            theEgg.startShowingEnergyBlocks();
        }
        if(command.getName().equals("hideebs"))
        {
            theEgg.stopShowingEnergyBlocks();
        }
        return false;
    }

    public static boolean isEggTypeBlock(Material blockType)
    {
        for(Material type:EvilEggBlocks)
        {
            if(type == blockType) return true;
        }
        return false;
    }

    // Doesn't return lava, because it's supposed to replace blocks where evilEnergyLevel is too high
    public static Material getRandomEggBlock()
    {
        double number = rng.nextDouble();
        if(number < 0.166d) return Material.MAGMA_BLOCK;
        if(number < 0.332d) return Material.REDSTONE_BLOCK;
        if(number < 0.498d) return Material.NETHER_WART_BLOCK;
        if(number < 0.664d) return Material.CRIMSON_NYLIUM;
        if(number < 0.83d) return Material.RED_NETHER_BRICKS;
        return Material.GLOWSTONE;
    }
}
