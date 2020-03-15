package fr.younishd.sleepctl;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class SleepCtlPlugin extends JavaPlugin implements Listener {

    private Set<Player> sleepingPlayers;
    private int percentage;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();

        this.sleepingPlayers = new HashSet<>();
        this.percentage = this.getConfig().getInt("percentage");
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent e) {
        if (e.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }

        if (e.getPlayer().getWorld().getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        this.sleepingPlayers.add(e.getPlayer());

        if (!this.enoughSleepingPlayers()) {
            return;
        }

        this.getServer().getScheduler().cancelTasks(this);
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if (!this.enoughSleepingPlayers()) {
                return;
            }

            this.getServer().getWorlds().get(0).setTime(0);
            this.getServer().getWorlds().get(0).setStorm(false);

            for (Player p : this.getServer().getWorlds().get(0).getPlayers()) {
                if (p.isSleeping()) {
                    p.wakeup(false);
                }
            }
        }, 101);
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent e) {
        this.sleepingPlayers.remove(e.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("sleepctl")) {
            if (args.length > 1) {
                sender.sendMessage("Wrong number of arguments.");
                return false;
            }

            if (args.length == 1) {
                int percentage;
                try {
                    percentage = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid number format.");
                    return false;
                }

                if (percentage > 100 || percentage < 0) {
                    sender.sendMessage("Percentage needs to be between 0 and 100.");
                    return false;
                }

                this.percentage = percentage;
                this.getConfig().set("percentage", percentage);
                this.saveConfig();
            }

            sender.sendMessage("Sleeping percentage: " + this.percentage + "%");
            return true;
        }

        return false;
    }

    private boolean enoughSleepingPlayers() {
        return (double)this.sleepingPlayers.size() / this.getServer().getWorlds().get(0).getPlayers().size() + 1e-10
                > (double)this.percentage / 100;
    }

}
