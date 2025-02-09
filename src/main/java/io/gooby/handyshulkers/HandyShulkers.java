package io.gooby.handyshulkers;

import org.bukkit.Bukkit;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.UUID;

public class HandyShulkers extends JavaPlugin implements Listener {

    private final HashSet<UUID> openShulkers = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin has been disabled.");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType().isAir() || !itemStack.getType().name().endsWith("SHULKER_BOX")) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;

        event.setCancelled(true);

        BlockStateMeta meta = (BlockStateMeta) itemStack.getItemMeta();
        if (meta == null || !(meta.getBlockState() instanceof ShulkerBox shulkerState)) return;

        Inventory inventory = Bukkit.createInventory(null, 27);
        inventory.setContents(shulkerState.getInventory().getContents());

        openShulkers.add(player.getUniqueId());
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!openShulkers.contains(player.getUniqueId())) return;

        Inventory inventory = event.getInventory();
        ItemStack shulker = player.getInventory().getItemInMainHand();
        if (!(shulker.getItemMeta() instanceof BlockStateMeta meta)) return;
        if (!(meta.getBlockState() instanceof ShulkerBox state)) return;

        state.getInventory().setContents(inventory.getContents());
        meta.setBlockState(state);
        shulker.setItemMeta(meta);

        openShulkers.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!openShulkers.contains(player.getUniqueId())) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.getType().name().endsWith("SHULKER_BOX")) return;
        event.setCancelled(true);
    }
}