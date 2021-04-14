package com.marlongrazek.ui;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class UI {

    private final Events events = new Events();
    private static Inventory inv;
    private final HashMap<Integer, Item> items;
    private final Player player;
    private final Plugin plugin;
    private final boolean preventClose;
    private int size;
    private final String title;
    private InventoryType type;

    private void closeInventory() {
        HandlerList.unregisterAll(this.events);
    }

    private void openInventory() {

        if (type != null) inv = Bukkit.createInventory(this.player, this.type, this.title);
        else inv = Bukkit.createInventory(this.player, this.size, this.title);

        for (Item item : this.items.values()) {
            for (Integer slot : items.keySet()) {
                if (items.get(slot) == item) {
                    if (item != null) inv.setItem(slot, item.toItemStack());
                    else inv.setItem(slot, null);
                }
            }
        }
        this.player.openInventory(inv);
        Bukkit.getPluginManager().registerEvents(this.events, this.plugin);
    }

    public UI(Plugin plugin, Player player, String title, int size, HashMap<Integer, Item> items, boolean preventClose) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.size = size;
        this.items = items;
        this.preventClose = preventClose;
        openInventory();
    }

    public UI(Plugin plugin, Player player, String title, InventoryType type, HashMap<Integer, Item> items, boolean preventClose) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.type = type;
        this.items = items;
        this.preventClose = preventClose;
        openInventory();
    }

    public static class Anvil {

        private Item leftItem;
        private Consumer<Player> onClose;
        private BiFunction<Player, String, AnvilGUI.Response> onComplete;
        private Consumer<Player> onLeftItemClick;
        private Consumer<Player> onRightItemClick;
        private Plugin plugin;
        private boolean preventClose;
        private Item rightItem;
        private String text;
        private String title;


        public String getText() {
            return text;
        }

        public String getTitle() {
            return title;
        }

        public Item getLeftItem() {
            return leftItem;
        }

        public Item getRightItem() {
            return rightItem;
        }

        public void onClose(Consumer<Player> onClose) {
            this.onClose = onClose;
        }

        public void onComplete(BiFunction<Player, String, AnvilGUI.Response> onComplete) {
            this.onComplete = onComplete;
        }

        public void onLeftItemClick(Consumer<Player> onLeftItemClick) {
            this.onLeftItemClick = onLeftItemClick;
        }

        public void onRightItemClick(Consumer<Player> onRightItemClick) {
            this.onRightItemClick = onRightItemClick;
        }

        public void open(Player player) {

            AnvilGUI.Builder builder = new AnvilGUI.Builder();

            builder.title(title);
            builder.plugin(plugin);

            builder.text(rightItem.getName());

            builder.itemRight(rightItem.toItemStack());
            builder.itemLeft(leftItem.toItemStack());

            builder.onClose(onClose);
            builder.onComplete(onComplete);
            builder.onLeftInputClick(onLeftItemClick);
            builder.onRightInputClick(onRightItemClick);

            if (preventClose) builder.preventClose();

            builder.open(player);
        }

        public void setLeftItem(Item leftItem) {
            this.leftItem = leftItem;
        }

        public void setRightItem(Item rightItem) {
            this.rightItem = rightItem;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    private class Events implements Listener {

        @EventHandler
        public void onClose(InventoryCloseEvent e) {
            if (e.getInventory() == inv) {
                UI.this.closeInventory();
                if (UI.this.preventClose) Bukkit.getScheduler().runTask(UI.this.plugin, UI.this::openInventory);
            }
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getInventory() == inv)
                if (e.getCurrentItem() != null)
                    for (Item item : items.values()) {
                        if (item != null) {
                            if (e.getCurrentItem().equals(item.toItemStack())) {

                                e.setCancelled(!item.undefinedClicksAllowed());
                                if (item.getClickAction() != null) item.getClickAction().accept(e.getClick());
                            }
                        }
                    }
        }
    }

    public static class Item {

        Integer amount = 1;
        Consumer<ClickType> clickAction;
        HashMap<Enchantment, Integer> enchantments = new HashMap<>();
        ArrayList<ItemFlag> itemFlags = new ArrayList<>();
        ArrayList<String> lore = new ArrayList<>();
        Material material;
        ItemMeta meta;
        String name;
        Boolean undefinedClicks = false;

        public Item() {
        }

        public Item(String name, Material material) {
            this.name = name;
            this.material = material;
        }

        public void addEnchantment(Enchantment enchantment, Integer level) {
            enchantments.put(enchantment, level);
        }

        public void addGlow() {
            addEnchantment(Enchantment.ARROW_DAMAGE, 1);
            addItemFlag(ItemFlag.HIDE_ENCHANTS);
        }

        public void addItemFlag(ItemFlag itemFlag) {
            itemFlags.add(itemFlag);
        }

        public void addLoreLine(String line) {
            lore.add(line);
        }

        public void allowUndefinedClicks() {
            this.undefinedClicks = true;
        }

        public static Item fromItemStack(ItemStack itemStack) {
            ItemMeta itemMeta;
            Material material;
            Item item = new Item();

            if (itemStack.getItemMeta() != null) {
                itemMeta = itemStack.getItemMeta();
                material = itemStack.getType();
                item.setItemMeta(itemMeta);
                item.setMaterial(material);
            }
            return item;
        }

        public Boolean undefinedClicksAllowed() {
            return undefinedClicks;
        }

        public Integer getAmount() {
            return amount;
        }

        public Consumer<ClickType> getClickAction() {
            return this.clickAction;
        }

        public ItemMeta getItemMeta() {
            return this.meta;
        }

        public ArrayList<String> getLore() {
            return this.lore;
        }

        public Material getMaterial() {
            return this.material;
        }

        public String getName() {
            return this.name;
        }

        public void onClick(Consumer<ClickType> clickAction) {
            this.clickAction = clickAction;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public void setEnchantments(HashMap<Enchantment, Integer> enchantments) {
            this.enchantments = enchantments;
        }

        public void setItemMeta(ItemMeta meta) {
            this.meta = meta;
        }

        public void setLore(ArrayList<String> lore) {
            this.lore = lore;
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ItemStack toItemStack() {

            ItemStack item = new ItemStack(this.material);

            if (meta != null) item.setItemMeta(this.meta);
            else {
                String name = this.name;
                ArrayList<String> lore = this.lore;

                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(name);
                meta.setLore(lore);

                for (ItemFlag itemFlag : itemFlags) meta.addItemFlags(itemFlag);
                item.setItemMeta(meta);
                item.addUnsafeEnchantments(enchantments);
                item.setAmount(amount);
            }
            return item;
        }
    }

    public static class Page {

        private Player holder;
        private Boolean isOpen = false;
        private HashMap<Integer, Item> items = new HashMap<>();
        private Plugin plugin;
        private boolean preventClose;
        private int size;
        private String title;
        private InventoryType type;

        public void addItem(Item item) {
            if (!items.isEmpty()) {
                for (int slot = 0; slot < items.size() + 1; slot++) {
                    if (!items.containsKey(slot)) {
                        items.put(slot, item);
                        break;
                    }
                }
            } else items.put(0, item);
            if (isOpen) inv.addItem(item.toItemStack());
        }

        public Player getHolder() {
            return this.holder;
        }

        public Integer getSlot(Item item) {
            for (Integer s : items.keySet()) if (s != null) if (items.get(s).equals(item)) return s;
            return null;
        }

        public Item getItem(Integer slot) {
            return items.get(slot);
        }

        public HashMap<Integer, Item> getItems() {
            return this.items;
        }

        public String getTitle() {
            return this.title;
        }

        public void moveItem(Item item, Integer slot) {
            this.items.remove(slot);
            setItem(item, slot);
        }

        public void open(Player p) {
            isOpen = true;
            if (type != null) new UI(this.plugin, p, this.title, type, this.items, this.preventClose);
            else new UI(this.plugin, p, this.title, this.size, this.items, this.preventClose);
        }

        public void preventClose() {
            this.preventClose = true;
        }

        public void removeItem(Item item) {
            Integer slot = getSlot(item);
            this.items.remove(slot);
            if (isOpen) inv.setItem(slot, null);
        }

        public void removeItem(Integer slot) {
            this.items.remove(slot);
            if (isOpen) inv.setItem(slot, null);
        }

        public void setHolder(Player holder) {
            this.holder = holder;
        }

        public void setItem(Item item, Integer slot) {
            this.items.put(slot, item);
            if (isOpen) inv.setItem(slot, item.toItemStack());
        }

        public void setItems(HashMap<Integer, Item> items) {
            this.items = items;
            if (isOpen) for (Integer slot : items.keySet()) inv.setItem(slot, items.get(slot).toItemStack());
        }

        public void setPlugin(Plugin plugin) {
            this.plugin = plugin;
        }

        public void setSection(Section section, Integer start) {

            for (int i = 0; i < section.getItems().keySet().size(); i++) {
                ArrayList<Integer> slots = new ArrayList<>(section.getItems().keySet());
                int slot = slots.get(i);
                if (section.isReversed()) Collections.reverse(slots);
                setItem(section.getItems().get(slots.get(i)), start + slot);
            }
        }

        public void setSize(int size) {
            this.size = size;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setType(InventoryType type) {
            this.type = type;
        }
    }

    public static class Section {

        Integer width;
        Integer height;
        HashMap<Integer, Item> items = new HashMap<>();
        Boolean reverse = false;

        public Section(Integer width, Integer height) {
            this.width = width;
            this.height = height;
        }

        public void addItem(Item item) {
            if (!items.isEmpty()) {
                outerloop:
                for (int h = 0; h < height * 9; h += 9) {
                    for (int w = 0; w < width; w++) {
                        int slot = w + h;
                        if (!items.containsKey(slot)) {
                            items.put(slot, item);
                            break outerloop;
                        }
                    }
                }
            } else items.put(0, item);
        }

        public void fill(Item item) {
            for (int i = 0; i < width * height; i++) addItem(item);
        }

        public void setItem(Item item, Integer slot) {
            items.put(slot, item);
        }

        public void setItems(HashMap<Integer, Item> items) {
            this.items = items;
        }

        public void reverse() {
            reverse = true;
        }

        public HashMap<Integer, Item> getItems() {
            return items;
        }

        public Integer getWidth() {
            return width;
        }

        public Integer getHeight() {
            return height;
        }

        public Boolean isReversed() {
            return reverse;
        }
    }
}
