package space.gorogoro.chunkanalyzer;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

/*
 * ChunkAnalyzer
 * @license    LGPv3
 * @copyright  Copyright gorogoro.space 2021
 * @author     kubotan
 * @see        <a href="https://www.gorogoro.space">gorogoro.space</a>
 */
public class ChunkAnalyzer extends JavaPlugin {

  protected FileConfiguration config;

  /**
   * JavaPlugin method onEnable.
   */
  @Override
  public void onEnable() {
    try {
      getLogger().info("The Plugin Has Been Enabled!");

      // If there is no setting file, it is created
      if(!getDataFolder().exists()){
        getDataFolder().mkdir();
      }

      File configFile = new File(getDataFolder(), "config.yml");
      if(!configFile.exists()){
        saveDefaultConfig();
      }

      config = getConfig();

    } catch (Exception e) {
      logStackTrace(e);
    }
  }

  /**
   * JavaPlugin method onCommand.
   * @return true:Success false:Show the usage set in plugin.yml
   */
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    try {
      if (!command.getName().equals("chunka") && !command.getName().equals("chunkc")) {
        sender.sendMessage("That command is not available.");
        return true;
      }

      if(!(sender instanceof Player)) {
        sender.sendMessage("The command can only be executed by the player.");
        return true;
      }

      if(!sender.isOp()) {
        sender.sendMessage("No operator permissions.");
        return true;
      }

      int limit = config.getInt("limit");
      if(limit < 1 || limit > 50) {
        sender.sendMessage("Please specify the number of lines to be show, between 1 and 50.");
        return true;
      }

      if(command.getName().equals("chunka")) {
        if(args.length == 0){
          showWorldScore(sender, limit);
        } else if(args.length == 1){
          Pattern pattern = Pattern.compile("^[0-9a-z_]+$");
          Matcher matcher = pattern.matcher(args[0]);
          if(!matcher.matches()) {
            sender.sendMessage("The world name should be a character from 0-9a-z_.");
            return true;
          }
          if(getServer().getWorld(args[0]) == null) {
            sender.sendMessage("Can't get the world from the world name.");
            return true;
          }
          showChunkScore(sender, limit, getServer().getWorld(args[0]));
        } else {
          sender.sendMessage("Invalid world name argument.");
        }
      } else if(command.getName().equals("chunkc")) {
        showCurrentChunkScore((Player)sender, limit);
      }
      return true;
    } catch (Exception e) {
      logStackTrace(e);
    }
    return false;
  }

  /**
   * JavaPlugin method onDisable.
   */
  @Override
  public void onDisable() {
    try {
      getLogger().info("The Plugin Has Been Disabled!");
    } catch (Exception e) {
      logStackTrace(e);
    }
  }

  /**
   * Output stack trace to log file.
   * @param Exception Exception
   */
  public void logStackTrace(Exception e){
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      pw.flush();
      getLogger().warning(sw.toString());
  }

  /**
   * Show world counter.
   * @param CommandSender sender
   * @param Integer limit
   */
  public void showWorldScore(CommandSender sender, Integer limit){
    Map<World, Integer> counter = new HashMap<World, Integer>();
    for (World w : getServer().getWorlds()) {
      for (Chunk c : w.getLoadedChunks()) {
        if (!counter.containsKey(w)) {
          counter.put(w, 0);
        }
        counter.put(w, counter.get(w) + c.getEntities().length);
        counter.put(w, counter.get(w) + c.getTileEntities().length);
      }
    }

    Stream<Map.Entry<World, Integer>> sorted = counter.entrySet().stream()
        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

    Map<World, Integer> top = sorted.limit(limit)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    sender.sendMessage("");
    sender.sendMessage(Component.text("■■■■■■ ChunkAnalyzer ■■■■■■", NamedTextColor.GOLD));
    sender.sendMessage("");

    String msg;
    String clickCmd;
    for (World w : top.keySet()) {
      msg = String.format("  ● World: %s Count: %d", w.getName(), counter.get(w));
      clickCmd = "chunka " + w.getName();
      sender.sendMessage(getMsgHoverClickCmd(Component.text(msg), Component.text("Click to view details."), clickCmd));
    }

    sender.sendMessage("");
    sender.sendMessage(Component.text("■■■■■■■■■■■■■■■■■■■■■■■■■■■", NamedTextColor.GOLD));
  }

  /**
   * Show chunk counter.
   * @param CommandSender sender
   * @param Integer limit
   * @param World w
   */
  public void showChunkScore(CommandSender sender, Integer limit, World w){
    Map<Chunk, Integer> counter = new HashMap<Chunk, Integer>();
    for (Chunk c : w.getLoadedChunks()) {
      if (!counter.containsKey(c)) {
        counter.put(c, 0);
      }
      counter.put(c, counter.get(c) + c.getEntities().length);
      counter.put(c, counter.get(c) + c.getTileEntities().length);
    }

    Stream<Map.Entry<Chunk, Integer>> sorted = counter.entrySet().stream()
        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

    Map<Chunk, Integer> top = sorted.limit(limit)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    sender.sendMessage("");
    sender.sendMessage(Component.text("■■■■■■■ ChunkAnalyzer ■■■■■■■", NamedTextColor.GOLD));
    sender.sendMessage("");
    sender.sendMessage(getMsgHoverClickCmd(
      Component.text("  ＞＞ Teleport to \"" + w.getName() + "\" world", NamedTextColor.RED), Component.text("Click to teleport"), "mvtp " + w.getName()
    ));
    sender.sendMessage("");

    String msg;
    Component hoverMsg;
    String clickCmd;
    for (Chunk c : top.keySet()) {
      msg = String.format("  ● Chunk: %d,%d Count: %d", c.getX(), c.getZ(), counter.get(c));
      hoverMsg = getEntityMsg(w, limit, c);
      Location l = getLocation(w, c);
      clickCmd = String.format("tp %.1f %.1f %.1f", l.getX(), l.getY(), l.getZ());
      sender.sendMessage(getMsgHoverClickCmd(Component.text(msg), hoverMsg, clickCmd));
    }

    sender.sendMessage("");
    sender.sendMessage(getMsgHoverClickCmd(Component.text("  ＜＜ Back", NamedTextColor.RED), Component.text("Click to go back"), "chunka"));
    sender.sendMessage("");
    sender.sendMessage(Component.text("■■■■■■■■■■■■■■■■■■■■■■■■■■■", NamedTextColor.GOLD));

  }


  /**
   * Show current chunk counter.
   * @param Player p
   * @param Integer limit
   */
  public void showCurrentChunkScore(Player sender, Integer limit){
    Chunk c = sender.getLocation().getChunk();
    Map<String, Integer> counter = new HashMap<String, Integer>();
    String type;
    for (BlockState b : c.getTileEntities()) {
      type = b.getType().toString();
      if (!counter.containsKey(type)) {
        counter.put(type, 0);
      }
      counter.put(type, counter.get(type) + 1);
    }
    for (Entity e : c.getEntities()) {
      type = e.getType().toString();
      if (!counter.containsKey(type)) {
        counter.put(type, 0);
      }
      counter.put(type, counter.get(type) + 1);
    }

    Stream<Map.Entry<String, Integer>> sorted = counter.entrySet().stream()
        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

    Map<String, Integer> top = sorted.limit(limit)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    sender.sendMessage("");
    sender.sendMessage(Component.text("■■■■■■ ChunkAnalyzer ■■■■■■", NamedTextColor.GOLD));
    sender.sendMessage("");

    Location l = getLocation(sender.getWorld(), c);
    String msg = "\n  File name: " + getRegionFileName(c);
    Integer rownum = 0;
    for (String t : top.keySet()) {
      rownum++;
      msg += String.format("\n  %d. Type: %s Count: %d", rownum, t, counter.get(t));
    }
    sender.sendMessage(Component.text()
      .append(Component.text(String.format("  XYZ: %.1f / %.1f / %.1f", l.getX(), l.getY(), l.getZ()), NamedTextColor.RED))
      .append(Component.text(msg))
      .build());

    sender.sendMessage("");
    sender.sendMessage(Component.text("■■■■■■■■■■■■■■■■■■■■■■■■■■■", NamedTextColor.GOLD));
  }

  /**
   * Get entity message.
   * @param World w
   * @param Integer limit
   * @param Chunk c
   * @return Returns the text of the entity information.
   */
  public Component getEntityMsg(World w, Integer limit, Chunk c) {
    Map<String, Integer> counter = new HashMap<String, Integer>();
    String type;
    for (BlockState b : c.getTileEntities()) {
      type = b.getType().toString();
      if (!counter.containsKey(type)) {
        counter.put(type, 0);
      }
      counter.put(type, counter.get(type) + 1);
    }
    for (Entity e : c.getEntities()) {
      type = e.getType().toString();
      if (!counter.containsKey(type)) {
        counter.put(type, 0);
      }
      counter.put(type, counter.get(type) + 1);
    }

    Stream<Map.Entry<String, Integer>> sorted = counter.entrySet().stream()
        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

    Map<String, Integer> top = sorted.limit(limit)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    Location l = getLocation(w, c);
    String msg = "\n  File name: " + getRegionFileName(c);
    Integer rownum = 0;
    for (String t : top.keySet()) {
      rownum++;
      msg += String.format("\n  %d. Type: %s Count: %d", rownum, t, counter.get(t));
    }
    return Component.text()
      .append(Component.text(String.format("  Click to teleport(XYZ): %.1f / %.1f / %.1f", l.getX(), l.getY(), l.getZ()), NamedTextColor.RED))
      .append(Component.text(msg))
      .build();
  }

  /**
   * Get message with hover and click command.
   * @param Component msg
   * @param Component hoverMsg
   * @param String clickCmd
   * @return Returns the Component corresponding to the argument.
   */
  public static Component getMsgHoverClickCmd(Component msg, Component hoverMsg, String clickCmd) {
    return msg
      .hoverEvent(HoverEvent.showText(hoverMsg))
      .clickEvent(ClickEvent.runCommand("/" + clickCmd));
  }

  /**
   * Get message with click command.
   * @param Component msg
   * @param String clickCmd
   * @return Returns the Component corresponding to the argument.
   */
  public static Component getMsgClickCmd(Component msg, String clickCmd) {
    return msg.clickEvent(ClickEvent.runCommand("/" + clickCmd));
  }

  /**
   * Get region file name.
   * @param Chunk c
   * @return Returns the region file name.
   */
  public static String getRegionFileName(Chunk c) {
    return "r." + String.valueOf(c.getX() >> 5) + "." + String.valueOf(c.getZ() >> 5) + ".mca";
  }

  /**
   * Get location by chunk.
   * @param World w
   * @param Chunk c
   * @return Returns the location.
   */
  public static Location getLocation(World w, Chunk c) {
    return new Location(
      w,
      c.getX() << 4,
      w.getHighestBlockYAt(c.getX(), c.getZ()),
      c.getZ() << 4
    );
  }
}
