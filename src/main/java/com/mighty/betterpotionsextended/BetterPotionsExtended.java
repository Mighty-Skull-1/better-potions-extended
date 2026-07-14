package com.mighty.betterpotionsextended;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class BetterPotionsExtended extends JavaPlugin implements CommandExecutor, TabCompleter, Listener {

    private NamespacedKey potionKey;
    private NamespacedKey actionKey;

    private static class MainMenuHolder implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }
    
    private static class EditMenuHolder implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        potionKey = new NamespacedKey(this, "bpe_potion");
        actionKey = new NamespacedKey(this, "bpe_action");
        
        this.getCommand("bpe").setExecutor(this);
        this.getCommand("bpe").setTabCompleter(this);
        
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("BetterPotionsExtended Fixed Live Core Active!");
    }

    @Override
    public void onDisable() {}

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("betterpotionsextended.admin")) {
                sender.sendMessage(ChatColor.RED + "Access denied.");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /bpe give <player> <potion_type> [normal/splash/lingering]");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found online.");
                return true;
            }
            String potionTypeName = args[2].toUpperCase();
            PotionType type;
            try {
                type = PotionType.valueOf(potionTypeName);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Invalid potion type selection.");
                return true;
            }
            Material material = Material.POTION;
            if (args.length >= 4) {
                String typeForm = args[3].toLowerCase();
                if (typeForm.equals("splash")) material = Material.SPLASH_POTION;
                else if (typeForm.equals("lingering")) material = Material.LINGERING_POTION;
            }
            ItemStack potionItem = new ItemStack(material);
            PotionMeta meta = (PotionMeta) potionItem.getItemMeta();
            if (meta != null) {
                meta.setBasePotionType(type);
                potionItem.setItemMeta(meta);
                updatePotionLore(potionItem);
                target.getInventory().addItem(potionItem);
                sender.sendMessage(ChatColor.GREEN + "Gave 1x custom " + potionTypeName + " to " + target.getName());
            }
            return true;
        }

        if (!(sender instanceof Player player)) return true;

        if (args.length > 0 && args[0].equalsIgnoreCase("potions")) {
            if (!player.hasPermission("betterpotionsextended.admin")) return true;
            openMainMenu(player);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("betterpotionsextended.admin")) return true;
            reloadConfig();
            player.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
            for (Player online : getServer().getOnlinePlayers()) {
                updateInventoryPotions(online);
            }
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("betterpotionsextended.admin")) return completions;

        if (args.length == 1) {
            completions.add("potions");
            completions.add("reload");
            completions.add("give");
            return completions.stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            ConfigurationSection section = getConfig().getConfigurationSection("potions");
            if (section != null) {
                return section.getKeys(false).stream().filter(key -> key.toLowerCase().startsWith(args[2].toLowerCase())).collect(Collectors.toList());
            }
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            completions.add("normal");
            completions.add("splash");
            completions.add("lingering");
            return completions.stream().filter(s -> s.startsWith(args[3].toLowerCase())).collect(Collectors.toList());
        }
        return completions;
    }

    private void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(new MainMenuHolder(), 27, ChatColor.DARK_GRAY + "BPE: Potion Configurations");
        ConfigurationSection section = getConfig().getConfigurationSection("potions");

        if (section != null) {
            int slot = 0;
            for (String key : section.getKeys(false)) {
                if (slot >= 27) break;
                
                ItemStack item = new ItemStack(Material.POTION);
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                if (meta == null) continue;

                try {
                    meta.setBasePotionType(PotionType.valueOf(key));
                } catch (Exception e) {
                    try { meta.setBasePotionType(PotionType.valueOf("AWKWARD")); } catch (Exception ignored) {}
                }

                meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.setDisplayName(ChatColor.YELLOW + key.replace("_", " "));
                List<String> lore = new ArrayList<>();
                
                int normalSec = section.getInt(key + ".tier_1.normal_duration", 0);
                String cleanName = key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase().replace("_", " ");
                if (cleanName.equalsIgnoreCase("Swiftness")) cleanName = "Speed";
                
                lore.add(ChatColor.BLUE + cleanName + " (" + String.format("%02d:%02d", normalSec / 60, normalSec % 60) + ")");
                
                lore.add(ChatColor.DARK_GRAY + "----------------------");
                lore.add(ChatColor.GRAY + "Normal:    " + ChatColor.WHITE + normalSec + "s");
                lore.add(ChatColor.GRAY + "Extended:  " + ChatColor.WHITE + section.getInt(key + ".tier_1.extended_duration") + "s");
                lore.add(ChatColor.GRAY + "Splash:    " + ChatColor.WHITE + section.getInt(key + ".tier_1.splash_duration") + "s");
                lore.add(ChatColor.GRAY + "Lingering: " + ChatColor.WHITE + section.getInt(key + ".tier_1.lingering_duration") + "s");
                lore.add(ChatColor.DARK_GRAY + "----------------------");
                lore.add(ChatColor.AQUA + "➔ Click to modify options");

                meta.setLore(lore);
                meta.getPersistentDataContainer().set(potionKey, PersistentDataType.STRING, key);
                item.setItemMeta(meta);
                gui.setItem(slot++, item);
            }
        }
        player.openInventory(gui);
    }

    private void openEditMenu(Player player, String potionName) {
        Inventory gui = Bukkit.createInventory(new EditMenuHolder(), 18, ChatColor.BLUE + "Modify " + potionName);

        gui.setItem(0, createGuiItem(Material.SUGAR, ChatColor.GREEN + "+30s Normal", potionName, "add_normal"));
        gui.setItem(2, createGuiItem(Material.REDSTONE, ChatColor.GREEN + "+30s Extended", potionName, "add_extended"));
        gui.setItem(4, createGuiItem(Material.GUNPOWDER, ChatColor.GREEN + "+30s Splash", potionName, "add_splash"));
        gui.setItem(6, createGuiItem(Material.DRAGON_BREATH, ChatColor.GREEN + "+30s Lingering", potionName, "add_lingering"));

        gui.setItem(9, createGuiItem(Material.FERMENTED_SPIDER_EYE, ChatColor.RED + "-30s Normal", potionName, "sub_normal"));
        gui.setItem(11, createGuiItem(Material.REPEATER, ChatColor.RED + "-30s Extended", potionName, "sub_extended"));
        gui.setItem(13, createGuiItem(Material.FLINT, ChatColor.RED + "-30s Splash", potionName, "sub_splash"));
        gui.setItem(15, createGuiItem(Material.GLASS_BOTTLE, ChatColor.RED + "-30s Lingering", potionName, "sub_lingering"));

        gui.setItem(17, createGuiItem(Material.BARRIER, ChatColor.DARK_RED + "Return to Dashboard", potionName, "back"));

        player.openInventory(gui);
    }

    private ItemStack createGuiItem(Material material, String name, String potion, String action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.getPersistentDataContainer().set(potionKey, PersistentDataType.STRING, potion);
            meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler public void onInventoryOpen(InventoryOpenEvent e) { if (e.getPlayer() instanceof Player p) updateInventoryPotions(p); }
    @EventHandler public void onJoin(PlayerJoinEvent e) { updateInventoryPotions(e.getPlayer()); }

    private void updateInventoryPotions(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getItemMeta() instanceof PotionMeta) {
                updatePotionLore(item);
            }
        }
    }

    private void updatePotionLore(ItemStack item) {
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        if (meta == null) return;

        PotionType potionType = meta.getBasePotionType();
        if (potionType == null) return;

        String configKey = getCleanConfigKey(potionType);
        ConfigurationSection section = getConfig().getConfigurationSection("potions." + configKey);
        if (section == null) return;

        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        List<String> lore = new ArrayList<>();
        
        String typeName = potionType.name();
        boolean isExtended = typeName.startsWith("LONG_");
        boolean isTier2 = typeName.startsWith("STRONG_");
        
        String keyPath = isExtended ? "extended_duration" : "normal_duration";
        if (item.getType().name().contains("SPLASH")) keyPath = "splash_duration";
        if (item.getType().name().contains("LINGERING")) keyPath = "lingering_duration";
        String tierPath = isTier2 ? "tier_2" : "tier_1";

        int durationSeconds = section.getInt(tierPath + "." + keyPath, -1);
        if (durationSeconds != -1) {
            String displayName = configKey.substring(0,1).toUpperCase() + configKey.substring(1).toLowerCase().replace("_", " ");
            if (displayName.equalsIgnoreCase("Swiftness")) displayName = "Speed";
            
            String time = String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60);
            lore.add(ChatColor.BLUE + displayName + " (" + time + ")");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;
        
        InventoryHolder holder = clickedInventory.getHolder();
        if (!(holder instanceof MainMenuHolder) && !(holder instanceof EditMenuHolder)) return;
        
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getItemMeta() == null) return;

        ItemMeta meta = clicked.getItemMeta();
        String potion = meta.getPersistentDataContainer().get(potionKey, PersistentDataType.STRING);
        if (potion == null) return;

        if (holder instanceof MainMenuHolder) {
            openEditMenu(player, potion);
        } else if (holder instanceof EditMenuHolder) {
            String action = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
            if (action == null) return;

            if (action.equals("back")) {
                openMainMenu(player);
                return;
            }

            String path = "potions." + potion + ".tier_1.";
            int modifier = action.startsWith("add_") ? 30 : -30;

            if (action.endsWith("normal")) path += "normal_duration";
            if (action.endsWith("extended")) path += "extended_duration";
            if (action.endsWith("splash")) path += "splash_duration";
            if (action.endsWith("lingering")) path += "lingering_duration";

            int currentVal = getConfig().getInt(path, 0);
            int newVal = Math.max(0, currentVal + modifier);
            getConfig().set(path, newVal);
            saveConfig();

            player.sendMessage(ChatColor.GREEN + "Adjusted custom layout timer!");
            openEditMenu(player, potion); 
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!(item.getItemMeta() instanceof PotionMeta meta)) return;
        PotionType potionType = meta.getBasePotionType();
        if (potionType == null) return;

        Player player = event.getPlayer();
        getServer().getScheduler().runTaskLater(this, () -> {
            Collection<PotionEffect> modifiedEffects = getModifiedEffects(potionType, "normal_duration", "extended_duration");
            for (PotionEffect effect : modifiedEffects) {
                player.removePotionEffect(effect.getType());
                player.addPotionEffect(effect);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion thrownPotion = event.getPotion();
        PotionType potionType = thrownPotion.getPotionMeta().getBasePotionType();
        if (potionType == null) return;

        Collection<PotionEffect> modifiedEffects = getModifiedEffects(potionType, "splash_duration", "splash_duration");
        if (modifiedEffects.isEmpty()) return;

        for (LivingEntity entity : event.getAffectedEntities()) {
            double intensity = event.getIntensity(entity);
            for (PotionEffect effect : modifiedEffects) {
                int adjustedDuration = (int) (effect.getDuration() * intensity);
                if (adjustedDuration > 0) {
                    entity.removePotionEffect(effect.getType());
                    entity.addPotionEffect(new PotionEffect(effect.getType(), adjustedDuration, effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon()));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCloudApply(AreaEffectCloudApplyEvent event) {
        AreaEffectCloud cloud = event.getEntity();
        PotionType potionType = cloud.getBasePotionType();
        if (potionType == null) return;

        Collection<PotionEffect> modifiedEffects = getModifiedEffects(potionType, "lingering_duration", "lingering_duration");
        for (LivingEntity entity : event.getAffectedEntities()) {
            for (PotionEffect effect : modifiedEffects) {
                entity.removePotionEffect(effect.getType());
                entity.addPotionEffect(effect);
            }
        }
    }

    private String getCleanConfigKey(PotionType type) {
        String name = type.name();
        if (name.startsWith("LONG_")) name = name.substring(5);
        if (name.startsWith("STRONG_")) name = name.substring(7);
        
        // Dynamic name remapping for internal engine inconsistencies
        if (name.equalsIgnoreCase("SWIFTNESS")) return "SPEED";
        
        return name;
    }

    private Collection<PotionEffect> getModifiedEffects(PotionType type, String normalKey, String extendedKey) {
        Collection<PotionEffect> effects = new ArrayList<>();
        String configKey = getCleanConfigKey(type);
        
        ConfigurationSection section = getConfig().getConfigurationSection("potions." + configKey);
        if (section == null) return effects;

        String typeName = type.name();
        boolean isExtended = typeName.startsWith("LONG_");
        boolean isTier2 = typeName.startsWith("STRONG_");

        String tierPath = isTier2 ? "tier_2" : "tier_1";
        String keyPath = isExtended ? extendedKey : normalKey;

        int durationSeconds = section.getInt(tierPath + "." + keyPath, -1);
        if (durationSeconds != -1) {
            for (PotionEffect currentEffect : type.getPotionEffects()) {
                effects.add(new PotionEffect(currentEffect.getType(), durationSeconds * 20, isTier2 ? 1 : 0, true, true, true));
            }
        }
        return effects;
    }
}
