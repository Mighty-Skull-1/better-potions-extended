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
import java.util.Map;
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
        getLogger().info("BetterPotionsExtended v1.0.7 Active!");
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
                sender.sendMessage(ChatColor.RED + "Usage: /bpe give <player> <potion_type> [normal/splash/lingering] [tier: 1/2]");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found online.");
                return true;
            }

            String potionTypeName = args[2].toUpperCase();
            boolean isTier2 = false;
            if (args.length >= 5) {
                isTier2 = args[4].equals("2") || args[4].equalsIgnoreCase("tier_2") || args[4].equalsIgnoreCase("strong");
            }

            PotionType type = getPotionTypeFromConfigKey(potionTypeName, isTier2);
            if (type == PotionType.AWKWARD && !potionTypeName.equalsIgnoreCase("AWKWARD")) {
                sender.sendMessage(ChatColor.RED + "Invalid potion type selection: " + potionTypeName);
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
                target.updateInventory();
                sender.sendMessage(ChatColor.GREEN + "Gave 1x custom " + getDisplayPotionName(potionTypeName) + (isTier2 ? " II" : " I") + " to " + target.getName());
            }
            return true;
        }

        if (!(sender instanceof Player player)) return true;

        if (args.length > 0 && args[0].equalsIgnoreCase("potions")) {
            if (!player.hasPermission("betterpotionsextended.admin")) {
                player.sendMessage(ChatColor.RED + "Access denied.");
                return true;
            }
            openMainMenu(player);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("betterpotionsextended.admin")) {
                player.sendMessage(ChatColor.RED + "Access denied.");
                return true;
            }
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
        if (args.length == 5 && args[0].equalsIgnoreCase("give")) {
            completions.add("1");
            completions.add("2");
            return completions.stream().filter(s -> s.startsWith(args[4].toLowerCase())).collect(Collectors.toList());
        }
        return completions;
    }

    private void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(new MainMenuHolder(), 54, ChatColor.DARK_GRAY + "BPE: Potion Configurations");
        ConfigurationSection section = getConfig().getConfigurationSection("potions");

        if (section != null) {
            int slot = 0;
            for (String key : section.getKeys(false)) {
                if (slot >= 45) break;

                ItemStack item = new ItemStack(Material.POTION);
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                if (meta == null) continue;

                PotionType baseType = getPotionTypeFromConfigKey(key, false);
                meta.setBasePotionType(baseType);
                org.bukkit.Color customCol = getCustomPotionColor(key);
                if (customCol != null) meta.setColor(customCol);

                meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.setDisplayName(ChatColor.YELLOW + key.replace("_", " "));

                List<String> lore = new ArrayList<>();
                int normalSec = section.getInt(key + ".tier_1.normal_duration", 0);
                String cleanName = getDisplayPotionName(key);

                lore.add(ChatColor.BLUE + cleanName + " (" + String.format("%02d:%02d", normalSec / 60, normalSec % 60) + ")");
                lore.add(ChatColor.DARK_GRAY + "----------------------");
                lore.add(ChatColor.GOLD + "Tier I Durations:");
                lore.add(ChatColor.GRAY + "  Normal:    " + ChatColor.WHITE + normalSec + "s");
                lore.add(ChatColor.GRAY + "  Extended:  " + ChatColor.WHITE + section.getInt(key + ".tier_1.extended_duration", 0) + "s");
                lore.add(ChatColor.GRAY + "  Splash:    " + ChatColor.WHITE + section.getInt(key + ".tier_1.splash_duration", 0) + "s");
                lore.add(ChatColor.GRAY + "  Lingering: " + ChatColor.WHITE + section.getInt(key + ".tier_1.lingering_duration", 0) + "s");

                if (section.contains(key + ".tier_2")) {
                    lore.add(ChatColor.DARK_GRAY + "----------------------");
                    lore.add(ChatColor.AQUA + "Tier II Durations:");
                    lore.add(ChatColor.GRAY + "  Normal:    " + ChatColor.WHITE + section.getInt(key + ".tier_2.normal_duration", 0) + "s");
                    lore.add(ChatColor.GRAY + "  Splash:    " + ChatColor.WHITE + section.getInt(key + ".tier_2.splash_duration", 0) + "s");
                    lore.add(ChatColor.GRAY + "  Lingering: " + ChatColor.WHITE + section.getInt(key + ".tier_2.lingering_duration", 0) + "s");
                }

                lore.add(ChatColor.DARK_GRAY + "----------------------");
                lore.add(ChatColor.GREEN + "➔ Click to configure durations");

                meta.setLore(lore);
                meta.getPersistentDataContainer().set(potionKey, PersistentDataType.STRING, key);
                item.setItemMeta(meta);
                gui.setItem(slot++, item);
            }
        }

        // Bottom control row
        gui.setItem(48, createGuiItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Reload Config", "none", "cmd_reload"));
        gui.setItem(50, createGuiItem(Material.BARRIER, ChatColor.DARK_RED + "Close Dashboard", "none", "cmd_close"));

        player.openInventory(gui);
    }

    private void openEditMenu(Player player, String potionName, int tier) {
        Inventory gui = Bukkit.createInventory(new EditMenuHolder(), 27, ChatColor.DARK_GRAY + "BPE: " + potionName + " (Tier " + (tier == 1 ? "I" : "II") + ")");

        ConfigurationSection potionSec = getConfig().getConfigurationSection("potions." + potionName);
        boolean hasTier2 = potionSec != null && potionSec.contains("tier_2");
        String tierPath = "tier_" + tier;

        int normal = potionSec != null ? potionSec.getInt(tierPath + ".normal_duration", 0) : 0;
        int extended = potionSec != null ? potionSec.getInt(tierPath + ".extended_duration", 0) : 0;
        int splash = potionSec != null ? potionSec.getInt(tierPath + ".splash_duration", 0) : 0;
        int lingering = potionSec != null ? potionSec.getInt(tierPath + ".lingering_duration", 0) : 0;

        // Row 1: +30s buttons
        gui.setItem(1, createGuiItem(Material.SUGAR, ChatColor.GREEN + "+30s Normal (" + normal + "s)", potionName, "add_normal_" + tier));
        if (tier == 1) {
            gui.setItem(2, createGuiItem(Material.REDSTONE, ChatColor.GREEN + "+30s Extended (" + extended + "s)", potionName, "add_extended_" + tier));
        } else {
            gui.setItem(2, createGuiItem(Material.GRAY_DYE, ChatColor.DARK_GRAY + "Extended N/A in Tier II", potionName, "noop"));
        }

        // Center Preview Potion Bottle
        ItemStack preview = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) preview.getItemMeta();
        if (meta != null) {
            PotionType pType = getPotionTypeFromConfigKey(potionName, tier == 2);
            meta.setBasePotionType(pType);
            org.bukkit.Color customCol = getCustomPotionColor(potionName);
            if (customCol != null) meta.setColor(customCol);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            meta.setDisplayName(ChatColor.GOLD + getDisplayPotionName(potionName) + (tier == 2 ? " II" : " I"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Current Durations (" + (tier == 1 ? "Tier I" : "Tier II") + "):");
            lore.add(ChatColor.GRAY + "Normal:    " + ChatColor.WHITE + normal + "s (" + String.format("%02d:%02d", normal / 60, normal % 60) + ")");
            if (tier == 1) {
                lore.add(ChatColor.GRAY + "Extended:  " + ChatColor.WHITE + extended + "s (" + String.format("%02d:%02d", extended / 60, extended % 60) + ")");
            }
            lore.add(ChatColor.GRAY + "Splash:    " + ChatColor.WHITE + splash + "s (" + String.format("%02d:%02d", splash / 60, splash % 60) + ")");
            lore.add(ChatColor.GRAY + "Lingering: " + ChatColor.WHITE + lingering + "s (" + String.format("%02d:%02d", lingering / 60, lingering % 60) + ")");
            meta.setLore(lore);
            preview.setItemMeta(meta);
        }
        gui.setItem(4, preview);

        gui.setItem(6, createGuiItem(Material.GUNPOWDER, ChatColor.GREEN + "+30s Splash (" + splash + "s)", potionName, "add_splash_" + tier));
        gui.setItem(7, createGuiItem(Material.DRAGON_BREATH, ChatColor.GREEN + "+30s Lingering (" + lingering + "s)", potionName, "add_lingering_" + tier));

        // Row 2: -30s buttons
        gui.setItem(10, createGuiItem(Material.FERMENTED_SPIDER_EYE, ChatColor.RED + "-30s Normal", potionName, "sub_normal_" + tier));
        if (tier == 1) {
            gui.setItem(11, createGuiItem(Material.REPEATER, ChatColor.RED + "-30s Extended", potionName, "sub_extended_" + tier));
        } else {
            gui.setItem(11, createGuiItem(Material.GRAY_DYE, ChatColor.DARK_GRAY + "Extended N/A in Tier II", potionName, "noop"));
        }
        gui.setItem(15, createGuiItem(Material.FLINT, ChatColor.RED + "-30s Splash", potionName, "sub_splash_" + tier));
        gui.setItem(16, createGuiItem(Material.GLASS_BOTTLE, ChatColor.RED + "-30s Lingering", potionName, "sub_lingering_" + tier));

        // Row 3: Navigation & Tier Switcher
        gui.setItem(18, createGuiItem(Material.BARRIER, ChatColor.DARK_RED + "Return to Dashboard", potionName, "back"));

        if (hasTier2) {
            int targetTier = (tier == 1) ? 2 : 1;
            gui.setItem(22, createGuiItem(Material.GLOWSTONE_DUST,
                    ChatColor.GOLD + "Switch to " + (targetTier == 2 ? "Tier II" : "Tier I"),
                    potionName,
                    "switch_tier_" + targetTier));
        } else {
            gui.setItem(22, createGuiItem(Material.GRAY_DYE, ChatColor.DARK_GRAY + "No Tier II for this potion", potionName, "noop"));
        }

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
        ItemStack[] contents = player.getInventory().getContents();
        boolean changed = false;
        for (ItemStack item : contents) {
            if (item != null && item.getItemMeta() instanceof PotionMeta) {
                updatePotionLore(item);
                changed = true;
            }
        }
        if (changed) {
            player.updateInventory();
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
            String displayName = getDisplayPotionName(configKey);
            String tierSuffix = isTier2 ? " II" : "";
            String time = String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60);
            lore.add(ChatColor.BLUE + displayName + tierSuffix + " (" + time + ")");
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
            String action = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
            if ("cmd_reload".equals(action)) {
                reloadConfig();
                player.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
                for (Player online : getServer().getOnlinePlayers()) {
                    updateInventoryPotions(online);
                }
                openMainMenu(player);
                return;
            }
            if ("cmd_close".equals(action)) {
                player.closeInventory();
                return;
            }
            openEditMenu(player, potion, 1);
        } else if (holder instanceof EditMenuHolder) {
            String action = meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
            if (action == null || action.equals("noop")) return;

            if (action.equals("back")) {
                openMainMenu(player);
                return;
            }

            if (action.startsWith("switch_tier_")) {
                int targetTier = Integer.parseInt(action.substring("switch_tier_".length()));
                openEditMenu(player, potion, targetTier);
                return;
            }

            String[] parts = action.split("_");
            if (parts.length >= 3) {
                String op = parts[0];
                String track = parts[1];
                int tier = Integer.parseInt(parts[2]);

                String path = "potions." + potion + ".tier_" + tier + "." + track + "_duration";
                int modifier = op.equals("add") ? 30 : -30;
                int currentVal = getConfig().getInt(path, 0);
                int newVal = Math.max(0, currentVal + modifier);
                getConfig().set(path, newVal);
                saveConfig();

                for (Player online : getServer().getOnlinePlayers()) {
                    updateInventoryPotions(online);
                }

                player.sendMessage(ChatColor.GREEN + "Adjusted " + getDisplayPotionName(potion) + " (Tier " + (tier == 1 ? "I" : "II") + ") " + track + " duration to " + newVal + "s!");
                openEditMenu(player, potion, tier);
            }
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
        PotionMeta meta = thrownPotion.getPotionMeta();
        PotionType potionType = meta.getBasePotionType();
        if (potionType == null) return;

        Collection<PotionEffect> modifiedEffects = getModifiedEffects(potionType, "splash_duration", "splash_duration");
        if (modifiedEffects.isEmpty()) return;

        List<Map.Entry<LivingEntity, Double>> affected = new ArrayList<>();
        for (LivingEntity entity : event.getAffectedEntities()) {
            affected.add(Map.entry(entity, event.getIntensity(entity)));
        }

        getServer().getScheduler().runTaskLater(this, () -> {
            for (Map.Entry<LivingEntity, Double> entry : affected) {
                LivingEntity entity = entry.getKey();
                if (!entity.isValid()) continue;
                double intensity = entry.getValue();
                for (PotionEffect effect : modifiedEffects) {
                    int adjustedDuration = (int) (effect.getDuration() * intensity);
                    if (adjustedDuration > 0) {
                        entity.removePotionEffect(effect.getType());
                        entity.addPotionEffect(new PotionEffect(effect.getType(), adjustedDuration, effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon()));
                    }
                }
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCloudApply(AreaEffectCloudApplyEvent event) {
        AreaEffectCloud cloud = event.getEntity();
        PotionType potionType = cloud.getBasePotionType();
        if (potionType == null) return;

        Collection<PotionEffect> modifiedEffects = getModifiedEffects(potionType, "lingering_duration", "lingering_duration");
        if (modifiedEffects.isEmpty()) return;

        List<LivingEntity> affected = new ArrayList<>(event.getAffectedEntities());
        getServer().getScheduler().runTaskLater(this, () -> {
            for (LivingEntity entity : affected) {
                if (!entity.isValid()) continue;
                for (PotionEffect effect : modifiedEffects) {
                    entity.removePotionEffect(effect.getType());
                    entity.addPotionEffect(effect);
                }
            }
        }, 1L);
    }

    static String getDisplayPotionName(String key) {
        if (key.equalsIgnoreCase("SPEED") || key.equalsIgnoreCase("SWIFTNESS")) return "Speed";
        if (key.equalsIgnoreCase("JUMP_BOOST") || key.equalsIgnoreCase("LEAPING")) return "Jump Boost";
        return key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase().replace("_", " ");
    }

    static String getCleanConfigKey(PotionType type) {
        String name = type.name();
        if (name.startsWith("LONG_")) name = name.substring(5);
        if (name.startsWith("STRONG_")) name = name.substring(7);

        // Remap to match config.yml keys
        if (name.equalsIgnoreCase("SWIFTNESS")) return "SPEED";
        if (name.equalsIgnoreCase("LEAPING")) return "JUMP_BOOST";

        return name;
    }

    static PotionType getPotionTypeFromConfigKey(String key, boolean tier2) {
        String searchKey = key.toUpperCase();
        if (searchKey.equals("SPEED")) searchKey = "SWIFTNESS";
        if (searchKey.equals("JUMP_BOOST") || searchKey.equals("JUMP")) searchKey = "LEAPING";

        if (tier2) {
            try {
                return PotionType.valueOf("STRONG_" + searchKey);
            } catch (IllegalArgumentException ignored) {}
        }

        try {
            return PotionType.valueOf(searchKey);
        } catch (IllegalArgumentException e) {
            return PotionType.AWKWARD;
        }
    }

    static org.bukkit.Color getCustomPotionColor(String key) {
        String k = key.toUpperCase();
        if (k.equals("HASTE")) return org.bukkit.Color.fromRGB(255, 192, 33);
        return null;
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
                effects.add(new PotionEffect(currentEffect.getType(), durationSeconds * 20, currentEffect.getAmplifier(), true, true, true));
            }
        }
        return effects;
    }
}
