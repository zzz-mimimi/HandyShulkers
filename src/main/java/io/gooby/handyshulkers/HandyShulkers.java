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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.HashMap;

public class HandyShulkers extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin has been disabled.");
    }

    private final Map<Player, Inventory> playerViewingShulker = new HashMap<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = getItemInActiveHand(player);

        if (itemStack == null || !itemStack.getType().name().endsWith("SHULKER_BOX")) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR) return;

        event.setCancelled(true);

        BlockStateMeta meta = (BlockStateMeta) itemStack.getItemMeta();
        if (meta == null || !(meta.getBlockState() instanceof ShulkerBox shulkerState)) return;

        Inventory inventory = Bukkit.createInventory((InventoryHolder) null, 27);
        inventory.setContents(shulkerState.getInventory().getContents());

        playerViewingShulker.put(player, inventory);
        player.openInventory(inventory);
    }

    private ItemStack getItemInActiveHand(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack mainhand = inventory.getItemInMainHand();
        ItemStack offhand = inventory.getItemInOffHand();
        return mainhand.getType().isAir() ? offhand : mainhand;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory inventory = event.getInventory();
        if (inventory.getSize() != 27 || !playerViewingShulker.containsKey(player)) return;

        if (playerViewingShulker.get(player) != inventory) return;

        ItemStack shulker = getItemInActiveHand(player);
        BlockStateMeta meta = (BlockStateMeta) shulker.getItemMeta();
        if (meta == null || !(meta.getBlockState() instanceof ShulkerBox state)) return;

        state.getInventory().setContents(inventory.getContents());
        meta.setBlockState(state);
        shulker.setItemMeta(meta);

        playerViewingShulker.remove(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getSize() != 27 || event.getCurrentItem() == null ||
            !event.getCurrentItem().getType().name().endsWith("SHULKER_BOX")) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemMove(InventoryClickEvent event) {
        if (event.getInventory().getSize() != 27 || event.getCurrentItem() == null ||
            !event.getCurrentItem().getType().name().endsWith("SHULKER_BOX")) return;
        event.setCancelled(true);
    }
}