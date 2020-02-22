package tyze.bukkit.bartender;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class App extends JavaPlugin implements Listener {
    private static ArrayList<Integer> expTotalTable = new ArrayList<Integer>();
    private static ArrayList<Integer> expRequireTable = new ArrayList<Integer>();
    private static int cost = 15;

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        // prevent event triggered twice.
        if (event.getHand() == null || event.getHand().equals(EquipmentSlot.HAND) == false)
            return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType().equals(Material.DIAMOND_BLOCK) == false)
            return;

        Player p = event.getPlayer();
        if (p == null)
            return;

        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.isSimilar(new ItemStack(Material.GLASS_BOTTLE)) == false)
            return;

        int level = p.getLevel();
        float exp = p.getExp();

        int total = getTotalExp(level, exp);

        if (level <= 1 || total < cost) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§c§l經驗值不夠哦！快去打怪升等吧"));
            event.setCancelled(true);
            return;
        }

        int now = (int) (expRequireTable.get(level) * exp);

        // lv3 -> lv4 needs 13 exp, there is not enough. here should be while-loop
        while (now < cost) {
            level -= 1;
            now += expRequireTable.get(level);
        }
        now -= cost;
        exp = now * 1f / expRequireTable.get(level);

        if (p.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE)).isEmpty()) {
            p.getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE, 1));
            p.setExp(exp * 1f);
            p.setLevel(level);
        } else {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§c§l背包空間不足"));
        }
    }

    private int getTotalExp(Integer level, float exp) {
        return expTotalTable.get(level) + (int) (expRequireTable.get(level) * exp);
    }

    @Override
    public void onEnable() {
        getLogger().info("Bartender enable.");
        int sum = 0;
        int req = 0;
        for (int level = 0; level <= 25000; level++) {
            expTotalTable.add(sum);
            if (level <= 15) {
                req = level * 2 + 7;
            } else if (level <= 30) {
                req = level * 5 - 38;
            } else {
                req = 9 * level - 158;
            }
            sum += req;
            expRequireTable.add(req);
        }
        getLogger().info("Bartender built exp table.");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Bartender disable.");
    }
}