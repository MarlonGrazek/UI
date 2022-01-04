package com.marlongrazek.ui;

import com.marlongrazek.builder.ItemStackBuilder;
import dev.dbassett.skullcreator.SkullCreator;
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

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class UI {

    private final Events events = new Events();
    private Inventory inventory;
    private final HashMap<Integer, Item> items;
    private final Consumer<Player> openAction;
    private final Player player;
    private final Plugin plugin;
    private final Boolean preventClose;
    private int size;
    private final String title;
    private InventoryType type;

    private void closeInventory() {
        HandlerList.unregisterAll(this.events);
    }

    private void openInventory() {

        if (type != null) inventory = Bukkit.createInventory(this.player, this.type, this.title);
        else inventory = Bukkit.createInventory(this.player, this.size, this.title);
        if (openAction != null) openAction.accept(player);
        for (Item item : this.items.values()) {
            for (Integer slot : items.keySet()) {
                if (slot != null) {
                    if (items.get(slot) == item) {
                        if (item != null) {
                            inventory.setItem(slot, item.toItemStack());
                        } else inventory.setItem(slot, null);
                    }
                }
            }
        }
        this.player.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this.events, this.plugin);
    }

    public UI(Plugin plugin, Player player, String title, int size, HashMap<Integer, Item> items, boolean preventClose, Consumer<Player> openAction) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.size = size;
        this.items = items;
        this.preventClose = preventClose;
        this.openAction = openAction;
        openInventory();
    }

    public UI(Plugin plugin, Player player, String title, InventoryType type, HashMap<Integer, Item> items, boolean preventClose, Consumer<Player> openAction) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.type = type;
        this.items = items;
        this.preventClose = preventClose;
        this.openAction = openAction;
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
            if (e.getInventory() == inventory) {
                UI.this.closeInventory();
                if (UI.this.preventClose) Bukkit.getScheduler().runTask(UI.this.plugin, UI.this::openInventory);
            }
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getInventory() == inventory) {
                if (e.getView().getTopInventory() == e.getClickedInventory()) {
                    for (Item item : items.values()) {
                        if (item != null) {
                            if (e.getCurrentItem() != null) {
                                if (e.getCurrentItem().equals(item.toItemStack())) {
                                    e.setCancelled(!item.undefinedClicksAllowed());
                                    if (item.getClickAction() != null) item.getClickAction().accept(e.getClick());
                                    break;
                                }
                            } else {
                                if (player.getItemOnCursor() != null) {
                                    if (player.getItemOnCursor().equals(item.toItemStack())) {
                                        e.setCancelled(!item.undefinedClicksAllowed());
                                        if (item.getClickAction() != null) item.getClickAction().accept(e.getClick());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static class Item {

        Integer amount = 1;
        Consumer<ClickType> clickAction;
        HashMap<Enchantment, Integer> enchantments = new HashMap<>();
        List<ItemFlag> itemFlags = new ArrayList<>();
        List<String> lore = new ArrayList<>();
        Material material;
        ItemMeta meta;
        String name;
        Boolean undefinedClicks = false;

        public Item() {
        }

        public Item(String name) {
            this.name = name;
        }

        public Item(Material material) {
            this.material = material;
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

        public void addItemFlags(List<ItemFlag> itemFlags) {
            for (ItemFlag itemFlag : itemFlags) if (!this.itemFlags.contains(itemFlag)) this.itemFlags.add(itemFlag);
        }

        public void setItemFlags(List<ItemFlag> itemFlags) {
            this.itemFlags = itemFlags;
        }

        public void addLoreLine(String line) {
            lore.add(line);
        }

        public void addLoreLines(String... lines) {
            Collections.addAll(lore, lines);
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

        public void clearLore() {
            this.lore.clear();
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

        public List<String> getLore() {
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

        public void setLore(List<String> lore) {
            this.lore = lore;
        }

        public void setLore(String... lore) {
            this.lore = Arrays.asList(lore);
        }

        public void setLoreLine(String line, int index) {
            this.lore.set(index, line);
        }

        public void setMaterial(Material material) {
            this.material = material;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ItemStack toItemStack() {

            ItemStackBuilder itemStack = new ItemStackBuilder(this.material);

            if (meta != null) itemStack.setItemMeta(this.meta);
            itemStack.setName(this.name);
            itemStack.setLore(new ArrayList<>(this.lore));
            itemStack.setItemFlags(new ArrayList<>(itemFlags));
            itemStack.setEnchantments(enchantments);
            itemStack.setAmount(amount);
            return itemStack.toItemStack();
        }

        public static class Skull extends Item {

            public static UI.Item fromUUID(UUID uuid) {
                return UI.Item.fromItemStack(SkullCreator.itemFromUuid(uuid));
            }

            public static UI.Item fromBase64(String base64) {
                return UI.Item.fromItemStack(SkullCreator.itemFromBase64(base64));
            }

            public static UI.Item fromURL(String url) {
                return UI.Item.fromItemStack(SkullCreator.itemFromUrl(url));
            }
        }
    }

    public static class Page {

        private Player holder;
        private HashMap<Integer, Item> items = new HashMap<>();
        private Consumer<Player> openAction;
        private Plugin plugin;
        private boolean preventClose;
        private int size;
        private String title;
        private InventoryType type;

        public Page() {
        }

        public Page(String title, int size) {
            this.title = title;
            this.size = size;
        }

        public Page(String title, int size, Plugin plugin) {
            this.title = title;
            this.size = size;
            this.plugin = plugin;
        }

        public void addItem(Item item) {
            if (!items.isEmpty()) {
                for (int slot = 0; slot < items.size() + 1; slot++) {
                    if (!items.containsKey(slot)) {
                        items.put(slot, item);
                        break;
                    }
                }
            } else items.put(0, item);
            //if (isOpen) new UI(items);
        }

        public void clear() {
            items.clear();
        }

        public Player getHolder() {
            return this.holder;
        }

        public Integer getSlot(Item item) {
            for (int i = 0; i < size; i++) if (items.get(i) != null) if (items.get(i).equals(item)) return i;
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

        public void onOpen(Consumer<Player> openAction) {
            this.openAction = openAction;
        }

        public void open(Player player) {
            if (type != null)
                new UI(this.plugin, player, this.title, type, this.items, this.preventClose, this.openAction);
            else new UI(this.plugin, player, this.title, this.size, this.items, this.preventClose, this.openAction);
        }

        public void preventClose() {
            this.preventClose = true;
        }

        public void removeItem(Item item) {
            Integer slot = getSlot(item);
            this.items.remove(slot);
            //if (isOpen) new UI(items);
        }

        public void removeItem(Integer slot) {
            this.items.remove(slot);
            //if (isOpen) new UI(items);
        }

        public void setHolder(Player holder) {
            this.holder = holder;
        }

        public void setItem(Item item, Integer slot) {
            this.items.put(slot, item);
            //if (isOpen) new UI(items);
        }

        public void setItems(HashMap<Integer, Item> items) {
            this.items = items;
            //if (isOpen) new UI(items);
        }

        public void setItemOnCursor(Player player, Item item) {
            player.setItemOnCursor(item.toItemStack());
            items.put(null, item);
        }

        public void setPlugin(Plugin plugin) {
            this.plugin = plugin;
        }

        public void setSection(Section section, Integer start) {

            ArrayList<Integer> slots = new ArrayList<>(section.getItems().keySet());
            if (section.isReversed()) Collections.reverse(slots);

            for (int i = 0; i < section.width * section.height; i++) {

                int y = (int) ((float) ((i + 1) / section.getWidth()));
                int x = (i - section.getWidth() * (y - 1));

                for (int slot : slots)
                    if (i == slot) setItem(section.getItems().get(slot), start + x + 9 * (y - 1));
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
                for(int i = 0; i < width * height; i++) {
                    if(!items.containsKey(i)) {
                        items.put(i, item);
                        break;
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

        public Integer getSlot(Item item) {
            for (int i = 0; i < width * height; i++) if (items.get(i) != null) if (items.get(i).equals(item)) return i;
            return null;
        }

        public Boolean isReversed() {
            return reverse;
        }
    }
}

