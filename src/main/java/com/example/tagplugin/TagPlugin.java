package com.example.tagplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TagPlugin extends JavaPlugin implements Listener {
    private TagLeaderboard leaderboard;
    private List<Player> players;
    private Player itPlayer;
    private FileConfiguration config;
    private long tagCooldownDuration;
    private Map<Player, Long> tagCooldowns; // Declare the tagCooldowns variable here


    @Override
    public void onEnable() {
        players = new ArrayList<>();
        itPlayer = null;
        saveDefaultConfig(); // Save the default config if it doesn't exist
        config = getConfig(); // Load the config
        tagCooldowns = new HashMap<>(); // Initialize the tagCooldowns map
        getServer().getPluginManager().registerEvents(this, this);

        leaderboard = new TagLeaderboard();
    }

    @Override
    public void onDisable() {
        players.clear();
        leaderboard.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("starttag")) {
            if (itPlayer != null) {
                sender.sendMessage(ChatColor.RED + "The tag game is already running!");
                return true;
            }

            if (Bukkit.getOnlinePlayers().size() < 2) {
                sender.sendMessage(ChatColor.RED + "There must be at least 2 players online to start the tag game.");
                return true;
            }

            startGame();
            sender.sendMessage(ChatColor.GREEN + "Starting the game...");
            return true;
        } else if (command.getName().equalsIgnoreCase("stoptag")) {
            if (itPlayer == null) {
                sender.sendMessage(ChatColor.RED + "The tag game is not currently running.");
                return true;
            }

            stopGame();
            sender.sendMessage(ChatColor.GREEN + "The tag game has been stopped.");
            return true;
        } else if (command.getName().equalsIgnoreCase("tagleaderboard")) {
            if (itPlayer == null) {
                sender.sendMessage(ChatColor.RED + "There is no game currently running.");
                return true;
            }

            leaderboard.sendLeaderboard(sender);
            return true;
        } else if (command.getName().equalsIgnoreCase("leaderboard")) {
            if (itPlayer == null) {
                sender.sendMessage(ChatColor.RED + "There is no game currently running.");
                return true;
            }

            leaderboard.sendTopTaggedPlayers(sender, 5);
            return true;
        }

        return false;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (itPlayer != null) {
            players.add(player);
            player.setPlayerListName(player.getName());

            String gameStartMessage = config.getString("game_start_message");
            if (gameStartMessage != null && !gameStartMessage.isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', gameStartMessage));
            } else {
                player.sendMessage(ChatColor.GREEN + "The tag game has started!");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        players.remove(player);

        if (player == itPlayer) {
            itPlayer = null;
            if (players.size() > 0) {
                selectNewItPlayer();
            }
        }
    }






    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player tagged = (Player) event.getEntity();
            Player tagger = (Player) event.getDamager();

            if (itPlayer == tagger) {
                if (tagCooldowns.containsKey(tagger)) {
                    long lastTagTime = tagCooldowns.get(tagger);
                    long currentTime = System.currentTimeMillis();
                    config = getConfig(); // Load the config
                    tagCooldownDuration = config.getLong("tag_cooldown", 30) * 1000; // Read the tag cooldown duration from the config (default: 30 seconds)

                    long cooldownTime = tagCooldownDuration; // Use the configurable cooldown duration

                    if (currentTime - lastTagTime < cooldownTime) {
                        tagger.sendMessage(ChatColor.RED + "You must wait before tagging another player.");
                        return;
                    }
                }

                if (tagCooldowns.containsKey(tagged)) {
                    long lastTagTime = tagCooldowns.get(tagged);
                    long currentTime = System.currentTimeMillis();
                    config = getConfig(); // Load the config
                    tagCooldownDuration = config.getLong("tag_cooldown", 30) * 1000; // Read the tag cooldown duration from the config (default: 30 seconds)

                    long cooldownTime = tagCooldownDuration; // Use the configurable cooldown duration

                    if (currentTime - lastTagTime < cooldownTime) {
                        tagger.sendMessage(ChatColor.RED + "This player cannot be tagged yet. Please wait.");
                        return;
                    }
                }

                tagPlayer(tagged);
                tagCooldowns.put(tagger, System.currentTimeMillis()); // Update the tagger's cooldown time
                tagCooldowns.put(tagged, System.currentTimeMillis()); // Update the tagged player's cooldown time

                String tagMessage = config.getString("tag_message");
                if (tagMessage != null && !tagMessage.isEmpty()) {
                    tagged.sendMessage(ChatColor.translateAlternateColorCodes('&', tagMessage.replace("{tagged}", tagged.getName())));
                } else {
                    tagged.sendMessage(ChatColor.RED + "You got tagged!");
                }

                String taggerMessage = config.getString("tagger_message");
                if (taggerMessage != null && !taggerMessage.isEmpty()) {
                    tagger.sendMessage(ChatColor.translateAlternateColorCodes('&', taggerMessage.replace("{tagged}", tagged.getName())));
                } else {
                    tagger.sendMessage(ChatColor.GREEN + "You tagged " + tagged.getName() + "!");
                }

                String consoleMessage = config.getString("console_message");
                if (consoleMessage != null && !consoleMessage.isEmpty()) {
                    consoleMessage = consoleMessage.replace("{tagger}", tagger.getName()).replace("{tagged}", tagged.getName());
                    getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', consoleMessage));
                }
                String globalMessage = config.getString("global_message");
                if (globalMessage != null && !globalMessage.isEmpty()) {
                    globalMessage = globalMessage.replace("{tagger}", tagger.getName()).replace("{tagged}", tagged.getName());
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player != tagger && player != tagged) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', globalMessage));
                        }
                    }
                }

                // Update tag counts
                if (tagger != tagged) {
                    leaderboard.incrementTagCount(tagger.getName());
                }

                // Send updated leaderboard to all players
                leaderboard.sendLeaderboard(Bukkit.getServer().getConsoleSender());
            }
        }
    }



    private void startGame() {
        players.clear();
        players.addAll(Bukkit.getOnlinePlayers());

        for (Player player : players) {
            player.setPlayerListName(player.getName());

            String gameStartMessage = config.getString("game_start_message");
            if (gameStartMessage != null && !gameStartMessage.isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', gameStartMessage));
            } else {
                player.sendMessage(ChatColor.GREEN + "The tag game has started!");
            }
        }

        selectNewItPlayer();
    }
    private void stopGame() {
        for (Player player : players) {
            player.setPlayerListName(player.getName());
        }

        players.clear();
        itPlayer = null;
        leaderboard.clear();
    }

    private void selectNewItPlayer() {
        Random random = new Random();
        int index = random.nextInt(players.size());

        itPlayer = players.get(index);

        for (Player player : players) {
            if (player == itPlayer) {
                player.setPlayerListName(ChatColor.RED + player.getName());

                String tagMessage = config.getString("tag_message");
                if (tagMessage != null && !tagMessage.isEmpty()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', tagMessage.replace("{tagged}", player.getName())));
                } else {
                    player.sendMessage(ChatColor.RED + "You got tagged!");
                }
            } else {
                player.setPlayerListName(player.getName());
            }
        }
    }

    private void tagPlayer(Player tagger) {
        if (tagger == itPlayer) {
            return;
        }

        itPlayer.setPlayerListName(itPlayer.getName());
        tagger.setPlayerListName(ChatColor.RED + tagger.getName());

        itPlayer = tagger;
    }
}