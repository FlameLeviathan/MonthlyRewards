package me.FlameKnight15.MonthlyRewards;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Main extends JavaPlugin {

    //Variable Declaration
    //File cFile = new File(getDataFolder(), "config.yml");
    //public FileConfiguration config = YamlConfiguration.loadConfiguration(cFile);
    String prefix;
    static ArrayList<String> usedCommands = new ArrayList<String>();

    @Override
    public void onEnable(){
        //Fired when the server enables the plugin
        getLogger().info("onEnable has been invoked!");
        saveDefaultConfig();
        //config.options().copyDefaults(true);
        //config = YamlConfiguration.loadConfiguration(cFile);
        //registerEvents(this, this);
        prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("chat-prefix"));

        try {
            usedCommands = (ArrayList<String>) SLAPI.load("plugins/MonthlyRewards/usedCommands.bin");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        checkDate();


    }

    @Override
    public void onDisable(){
        //Fired when the server stops and disables all plugins
        //config = YamlConfiguration.loadConfiguration(cFile);
        //config.options().copyDefaults(true);
        saveConfig();
        getLogger().info("onDisable has been invoked!");
        try {
            SLAPI.save(usedCommands, "plugins/MonthlyRewards/usedCommands.bin");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Registers all listeners that portain to the plugin
     * @param plugin plugin to load from
     * @param listeners to be loaded with the plugin
     */
    public static void registerEvents(org.bukkit.plugin.Plugin plugin, Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    /**
     * Handles the use of commands
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("mr") || cmd.getName().equalsIgnoreCase("monthlyrewards")){
            if(args.length == 1){
                if (args[0].equalsIgnoreCase("reload")) {
                    if(sender.hasPermission("monthlyrewards.reload")) {
                        if (getConfig() != null) {
                            reloadConfig();
                            //config = YamlConfiguration.loadConfiguration(cFile);
                        } else {
                            saveDefaultConfig();
                            //config.options().copyDefaults(true);
                        }


                        sender.sendMessage(ChatColor.GREEN + " Monthly Rewards has been reloaded!");
                        return true;
                    } else{
                        sender.sendMessage(prefix + ChatColor.RED + " You do not have permission to run that command!");
                    }
                }
                else if(args[0].equalsIgnoreCase("purge")){
                    if(sender.hasPermission("monthlyrewards.purge")) {
                        sender.sendMessage(prefix + ChatColor.GREEN + " You have purged the system! Players may use their rewards again!");
                        usedCommands = new ArrayList<String>();
                        return true;
                    } else{
                        sender.sendMessage(prefix + ChatColor.RED + " You do not have permission to run that command!");
                    }
                } else

                if(runCommands(args[0], (Player) sender) == false){
                    sender.sendMessage(prefix + ChatColor.RED + " That command was not recognized or does not exist!");
                    return true;
                }
            }
            if(args.length == 0) {
                if (sender.hasPermission("monthlyrewards.reload")) {
                    sender.sendMessage(ChatColor.GREEN + "/mr reload -" + ChatColor.GOLD + " Reloads the plugin");
                }
                if (sender.hasPermission("monthlyrewards.purge")) {
                    sender.sendMessage(ChatColor.GREEN + "/mr purge -" + ChatColor.GOLD + " Clears the file and allows players to claim rewards again!");
                } else
                    sender.sendMessage(listCommands((Player) sender));
            }

        }
        return true;

    }

    public void checkDate(){
        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (day == 1){
            if(getConfig().getBoolean("dontTouchThis")){
                return;
            } else{
                usedCommands = new ArrayList<String>();
                getConfig().set("dontTouchThis", true);
                return;
            }
        } else{
            getConfig().set("dontTouchThis", false);
        }
    }


    public boolean runCommands(String cmd, Player p){

        checkDate();

        for(String s : getConfig().getConfigurationSection("Commands").getKeys(false)){
            if(cmd.equalsIgnoreCase(s)){
                if(p.hasPermission("monthlyrewards."+s)){
                    if(!getValue(p, s)){
                        for(String command : getConfig().getStringList("Commands." + s + ".cmds")) {
                            if(command.contains("{player}")) {
                                command = command.replace("{player}", p.getName() + "");
                            }
                            /*if(command.contains("{PLAYER}"))
                                command.replace("{PLAYER}", p.getName());
                            if(command.contains("{POINTS}"))
                                command.replace("{POINTS}", u.getPoints() + "");
                            if(command.contains("{RANK}"))
                                command.replace("{RANK}", u.getRank() + "");
                            command = c.prefix + command;*/
                            
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                            usedCommands.add(p.getDisplayName() +":" + s);
                            p.sendMessage(prefix + ChatColor.GRAY + " You have claimed the " + ChatColor.translateAlternateColorCodes('&', getConfig().getString("Commands." + s + ".name")) + ChatColor.GRAY + " monthly award!  You can claim it again on the first of the month.");
                            return true;
                        }
                        return true;
                    }
                    else {
                        p.sendMessage(prefix + ChatColor.GRAY + " You've already claimed your monthly reward! You can claim it again on the first of the month!");
                        return true;
                    }
                }
                else {
                    p.sendMessage(prefix + ChatColor.GRAY + " Sorry,  you don't have access to this donation reward. Gain access with /buy!");
                    return true;
                }
            }
        }
        return false;
    }

    public String listCommands(Player p){
        String msg = prefix + ChatColor.GRAY + "There are no commands available for you!";
        ArrayList<String> cmds = new ArrayList<String>();
        for(String s : getConfig().getConfigurationSection("Commands").getKeys(false)) {
            if (p.hasPermission("monthlyrewards." + s)) {
                msg = prefix;
                cmds.add(s);
            }
        }

        for (String s : cmds){
            msg = msg + " " + s +",";
        }
        return msg;
    }

    public boolean getValue(Player p, String cmd){
        if (!(usedCommands.isEmpty()) || (usedCommands != null)){
            for (String s : usedCommands) {
                if (s.contains(p.getDisplayName())) {
                    if (s.contains(cmd)) {
                        return true;
                    }
                }
            }
        } else{
            return true;
        }
        return false;
    }
}
