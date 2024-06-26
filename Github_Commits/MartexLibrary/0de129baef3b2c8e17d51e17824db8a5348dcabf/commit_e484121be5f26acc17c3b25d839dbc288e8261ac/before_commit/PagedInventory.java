package com.georgev22.library.minecraft.inventory;

import com.georgev22.library.maps.ConcurrentObjectMap;
import com.georgev22.library.maps.ObjectMap;
import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import com.georgev22.library.minecraft.colors.Animation;
import com.georgev22.library.minecraft.inventory.handlers.PagedInventoryClickHandler;
import com.georgev22.library.minecraft.inventory.handlers.PagedInventoryCloseHandler;
import com.georgev22.library.minecraft.inventory.handlers.PagedInventorySwitchPageHandler;
import com.georgev22.library.minecraft.inventory.navigationitems.CloseNavigationItem;
import com.georgev22.library.minecraft.inventory.navigationitems.NavigationItem;
import com.georgev22.library.minecraft.inventory.navigationitems.NextNavigationItem;
import com.georgev22.library.minecraft.inventory.navigationitems.PreviousNavigationItem;
import com.georgev22.library.minecraft.inventory.utils.InventoryUtil;
import com.georgev22.library.minecraft.scheduler.MinecraftBukkitScheduler;
import com.georgev22.library.minecraft.scheduler.MinecraftFoliaScheduler;
import com.georgev22.library.minecraft.scheduler.MinecraftScheduler;
import com.georgev22.library.minecraft.scheduler.SchedulerTask;
import com.georgev22.library.utilities.Color;
import com.georgev22.library.utilities.KryoUtils;
import com.georgev22.library.utilities.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.*;

public class PagedInventory implements IPagedInventory {

    private final InventoryRegistrar registrar;
    private final List<Inventory> pages;
    private final NavigationRow navigationRow;

    private final List<PagedInventoryClickHandler> clickHandlers;
    private final List<PagedInventoryCloseHandler> closeHandlers;
    private final List<PagedInventorySwitchPageHandler> switchHandlers;

    private final MinecraftScheduler minecraftScheduler;

    private final ObjectMap<Player, SchedulerTask> playerSchedulerFramesMap;
    private final ObjectMap<Player, SchedulerTask> playerSchedulerAnimatedMap;

    private boolean kryo = false;

    protected PagedInventory(InventoryRegistrar registrar, NavigationRow navigationRow) {
        this.registrar = registrar;
        this.pages = new ArrayList<>();
        this.clickHandlers = new ArrayList<>(3);
        this.closeHandlers = new ArrayList<>(3);
        this.switchHandlers = new ArrayList<>(3);
        this.navigationRow = navigationRow;
        this.minecraftScheduler = BukkitMinecraftUtils.isFolia() ? new MinecraftFoliaScheduler() : new MinecraftBukkitScheduler();
        this.playerSchedulerFramesMap = new ConcurrentObjectMap<>();
        this.playerSchedulerAnimatedMap = new ConcurrentObjectMap<>();
    }

    @Deprecated
    protected PagedInventory(InventoryRegistrar registrar, Map<Integer, NavigationItem> navigation) {
        this(registrar, getFromMap(navigation));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHandler(PagedInventoryClickHandler handler) {
        clickHandlers.add(handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void callClickHandlers(PagedInventoryClickHandler.ClickHandler clickHandler) {
        clickHandlers.forEach(pagedInventoryClickHandler -> pagedInventoryClickHandler.handle(clickHandler));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearClickHandlers() {
        clickHandlers.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHandler(PagedInventoryCloseHandler handler) {
        closeHandlers.add(handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void callCloseHandlers(PagedInventoryCloseHandler.CloseHandler closeHandler) {
        closeHandlers.forEach(pagedInventoryCloseHandler -> pagedInventoryCloseHandler.handle(closeHandler));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearCloseHandlers() {
        closeHandlers.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addHandler(PagedInventorySwitchPageHandler handler) {
        switchHandlers.add(handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void callSwitchHandlers(PagedInventorySwitchPageHandler.SwitchHandler switchHandler) {
        switchHandlers.forEach(pagedInventorySwitchPageHandler -> pagedInventorySwitchPageHandler.handle(switchHandler));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearSwitchHandlers() {
        switchHandlers.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearAllHandlers() {
        clearClickHandlers();
        clearCloseHandlers();
        clearSwitchHandlers();
    }

    @Override
    public PageModifier getPageModifier(int index) {
        return new PageModifier(pages.get(index));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean openNext(Player player, Inventory currentlyOpen) {
        int index = pages.indexOf(currentlyOpen);
        if (index == -1 || pages.size() - 1 == index)
            return false;

        registrar.registerSwitch(player);
        boolean success = open(player, index + 1, true);

        if (success) {
            PagedInventorySwitchPageHandler.SwitchHandler switchHandler = new PagedInventorySwitchPageHandler.SwitchHandler(
                    this, player.getOpenInventory(), player, PagedInventorySwitchPageHandler.PageAction.NEXT, index);
            registrar.callGlobalSwitchHandlers(switchHandler);
            callSwitchHandlers(switchHandler);
        }

        return success;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean openPrevious(Player player, Inventory currentlyOpen) {
        int index = pages.indexOf(currentlyOpen);
        if (index <= 0)
            return false;

        registrar.registerSwitch(player);
        boolean success = open(player, index - 1, true);

        if (success) {
            PagedInventorySwitchPageHandler.SwitchHandler switchHandler = new PagedInventorySwitchPageHandler.SwitchHandler(
                    this, player.getOpenInventory(), player, PagedInventorySwitchPageHandler.PageAction.PREVIOUS, index);
            registrar.callGlobalSwitchHandlers(switchHandler);
            callSwitchHandlers(switchHandler);
        }

        return success;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean open(Player player) {
        if (pages.isEmpty())
            return false;
        return open(player, 0, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean open(Player player, int index) {
        if (pages.isEmpty())
            return false;
        return open(player, index, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean open(Player player, int index, boolean animated) {
        if (pages.size() - 1 < index || index < 0)
            return false;
        Inventory openInventory = pages.get(index);

        player.openInventory(openInventory);
        registrar.register(player, this, openInventory);

        ObjectMap<Integer, Integer> slotFrame = ObjectMap.newConcurrentObjectMap();

        if (player.getOpenInventory().getTopInventory() != null && player.getOpenInventory().getTopInventory().equals(openInventory)) {
            for (int slot = 0; slot < openInventory.getSize(); ++slot) {
                ItemStack itemStack = openInventory.getItem(slot);

                if (itemStack == null) continue;

                NBTItem nbtItem = new NBTItem(itemStack);

                if (!nbtItem.hasKey("frames")) continue;

                slotFrame.append(slot, 0);
            }
        }
        playerSchedulerFramesMap.append(player, minecraftScheduler.createRepeatingTask(registrar.getPlugin(), () -> {
            if (player.getOpenInventory().getTopInventory() != null && player.getOpenInventory().getTopInventory().equals(openInventory)) {
                for (int i = 0; i < openInventory.getSize(); ++i) {

                    ItemStack itemStack = openInventory.getItem(i);

                    if (itemStack == null) continue;

                    NBTItem nbtItem = new NBTItem(itemStack);

                    if (!nbtItem.hasKey("frames")) continue;

                    List<ItemStack> itemStacks = BukkitMinecraftUtils.itemStackListFromBase64(nbtItem.getString("frames"));

                    int size = itemStacks.size() - 1;
                    if (size > 0 & slotFrame.get(i) <= size) {
                        ItemStack item = itemStacks.get(slotFrame.get(i));
                        item.setItemMeta(itemStack.getItemMeta());
                        NBTItem newNBTItem = new NBTItem(item, true);
                        newNBTItem.setString("frames", nbtItem.getString("frames"));
                        openInventory.setItem(i, item);
                        if (slotFrame.get(i) < size) {
                            slotFrame.append(i, slotFrame.get(i) + 1);
                        } else if (slotFrame.get(i) >= size) {
                            slotFrame.append(i, 0);
                        }
                    }
                }
            }
        }, 1L, 20L));
        if (animated) {
            playerSchedulerAnimatedMap.append(player, minecraftScheduler.createRepeatingTask(registrar.getPlugin(), () -> {
                if (player.getOpenInventory().getTopInventory() != null && player.getOpenInventory().getTopInventory().equals(openInventory)) {
                    for (int i = 0; i < openInventory.getSize(); i++) {
                        ItemStack itemStack = openInventory.getItem(i);

                        if (itemStack == null || !itemStack.hasItemMeta()) {
                            continue;
                        }

                        ItemMeta itemMeta = itemStack.getItemMeta();

                        NBTItem nbtItem = new NBTItem(itemStack);

                        if (!nbtItem.hasKey("colors") & !nbtItem.hasKey("animation")) continue;

                        List<String> color;
                        if (registrar.kryo()) {
                            color = KryoUtils.deserialize(nbtItem.getByteArray("colors"));
                        } else {
                            try {
                                color = (List<String>) Utils.deserializeObjectFromString(nbtItem.getString("colors"));
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                                color = Lists.newArrayList();
                            }
                        }

                        if (color.isEmpty()) continue;
                        List<Color> colorsList = Lists.newArrayList();
                        for (String s : color) {
                            colorsList.add(Color.from(s));
                        }

                        itemMeta.setDisplayName(nbtItem.getString("animation").equalsIgnoreCase("wave") ? Animation.wave(BukkitMinecraftUtils.stripColor(itemMeta.getDisplayName()), colorsList) : Animation.fading(BukkitMinecraftUtils.stripColor(itemMeta.getDisplayName()), colorsList));
                        itemStack.setItemMeta(itemMeta);
                        openInventory.setItem(i, itemStack);

                    }

                    player.updateInventory();
                }
            }, 1L, 1L));
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(Inventory inventory) {
        return pages.indexOf(inventory);
    }

    @Override
    public void setPage(Map<Integer, ItemStack> contents, String title, int size, int index) {
        Preconditions.checkArgument(size >= MIN_INV_SIZE, "Inventory size must be >= " + MIN_INV_SIZE);
        Inventory inventory = Bukkit.createInventory(null, size, title);
        Preconditions.checkState(!pages.contains(inventory), "Cannot add duplicate inventory");

        if (inventory.getContents().length != 0) {
            for (int i = inventory.getSize() - 9; i < inventory.getSize(); i++) {
                ItemStack itemStack = inventory.getItem(i);
                if (itemStack != null)
                    itemStack.setAmount(0);
            }
        }

        if (!pages.isEmpty()) {
            NavigationItem previousItem = navigationRow.getNavigationItem(NavigationType.PREVIOUS);
            NavigationItem nextItem = navigationRow.getNavigationItem(NavigationType.NEXT);

            inventory.setItem(previousItem.getSlot() + inventory.getSize() - 9, previousItem.getItemStack());
            Inventory currentLast = pages.get(pages.size() - 1);
            currentLast.setItem(nextItem.getSlot() + inventory.getSize() - 9, nextItem.getItemStack());
        }

        NavigationItem closeItem = navigationRow.getNavigationItem(NavigationType.CLOSE);
        inventory.setItem(closeItem.getSlot() + inventory.getSize() - 9, closeItem.getItemStack());

        for (int i = 0; i < 9; i++) {
            NavigationItem item = navigationRow.get(i);

            if (item != null && item.getNavigationType() == NavigationType.CUSTOM)
                inventory.setItem(inventory.getSize() - 9 + i, item.getItemStack());
        }

        pages.set(index, inventory);
    }

    @Override
    public void setPage(Inventory inventory, int index) {
        Preconditions.checkArgument(inventory.getSize() >= MIN_INV_SIZE, "Inventory size must be >= " + MIN_INV_SIZE);
        Preconditions.checkState(!pages.contains(inventory), "Cannot add duplicate inventory");

        if (inventory.getContents().length != 0) {
            for (int i = inventory.getSize() - 9; i < inventory.getSize(); i++) {
                ItemStack itemStack = inventory.getItem(i);
                if (itemStack != null)
                    itemStack.setAmount(0);
            }
        }

        if (!pages.isEmpty()) {
            NavigationItem previousItem = navigationRow.getNavigationItem(NavigationType.PREVIOUS);
            NavigationItem nextItem = navigationRow.getNavigationItem(NavigationType.NEXT);

            inventory.setItem(previousItem.getSlot() + inventory.getSize() - 9, previousItem.getItemStack());
            Inventory currentLast = pages.get(pages.size() - 1);
            currentLast.setItem(nextItem.getSlot() + inventory.getSize() - 9, nextItem.getItemStack());
        }

        NavigationItem closeItem = navigationRow.getNavigationItem(NavigationType.CLOSE);
        inventory.setItem(closeItem.getSlot() + inventory.getSize() - 9, closeItem.getItemStack());

        for (int i = 0; i < 9; i++) {
            NavigationItem item = navigationRow.get(i);

            if (item != null && item.getNavigationType() == NavigationType.CUSTOM)
                inventory.setItem(inventory.getSize() - 9 + i, item.getItemStack());
        }

        pages.set(index, inventory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPage(Map<Integer, ItemStack> contents, String title, final int size) {
        Preconditions.checkArgument(size >= MIN_INV_SIZE, "Inventory size must be >= " + MIN_INV_SIZE);
        Inventory inventory = Bukkit.createInventory(null, size, title);

        //Adding items to inventory
        contents.forEach((slot, itemStack) -> {
            if (InventoryUtil.isValidSlot(slot, size))
                inventory.setItem(slot, itemStack);
        });

        addPage(inventory, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPage(Inventory inventory) {
        addPage(inventory, false);
    }

    private void addPage(Inventory inventory, boolean skipContentCheck) {
        Preconditions.checkArgument(inventory.getSize() >= MIN_INV_SIZE, "Inventory size must be >= " + MIN_INV_SIZE);
        Preconditions.checkState(!pages.contains(inventory), "Cannot add duplicate inventory");

        if (!skipContentCheck && inventory.getContents().length != 0) {
            for (int i = inventory.getSize() - 9; i < inventory.getSize(); i++) {
                ItemStack itemStack = inventory.getItem(i);
                if (itemStack != null)
                    itemStack.setAmount(0);
            }
        }

        if (!pages.isEmpty()) {
            NavigationItem previousItem = navigationRow.getNavigationItem(NavigationType.PREVIOUS);
            NavigationItem nextItem = navigationRow.getNavigationItem(NavigationType.NEXT);

            inventory.setItem(previousItem.getSlot() + inventory.getSize() - 9, previousItem.getItemStack());
            Inventory currentLast = pages.get(pages.size() - 1);
            currentLast.setItem(nextItem.getSlot() + inventory.getSize() - 9, nextItem.getItemStack());
        }

        NavigationItem closeItem = navigationRow.getNavigationItem(NavigationType.CLOSE);
        inventory.setItem(closeItem.getSlot() + inventory.getSize() - 9, closeItem.getItemStack());

        for (int i = 0; i < 9; i++) {
            NavigationItem item = navigationRow.get(i);

            if (item != null && item.getNavigationType() == NavigationType.CUSTOM)
                inventory.setItem(inventory.getSize() - 9 + i, item.getItemStack());
        }

        pages.add(inventory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Inventory removePage(int index) {
        Preconditions.checkArgument(index >= 0 && index <= pages.size() - 1);
        if (pages.size() - 1 == index && pages.size() > 1) {//Is last page
            Inventory inv = pages.get(pages.size() - 2);
            inv.getItem(InventoryUtil.getNavigationSlot(NavigationType.NEXT, inv.getSize())).setAmount(0);
            disperseViewers(pages.get(pages.size() - 1).getViewers(), pages.size() - 2);
        } else if (index == 0 && pages.size() > 1) {//Is first page
            Inventory inv = pages.get(1);
            inv.getItem(InventoryUtil.getNavigationSlot(NavigationType.PREVIOUS, inv.getSize())).setAmount(0);
            disperseViewers(pages.get(0).getViewers(), 1);
        } else if (pages.size() > 1) {//Is between first and last page
            Inventory inv = pages.get(index);
            disperseViewers(inv.getViewers(), index + 1);
        } else {//Is only page
            Inventory inv = pages.get(0);
            pages.remove(0);
            disperseViewers(inv.getViewers(), null);

            return inv;
        }

        return pages.remove(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removePage(Inventory inventory) {
        int index = pages.indexOf(inventory);

        if (index == -1) {
            return false;
        }

        removePage(index);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, NavigationItem> getNavigation() {
        Map<Integer, NavigationItem> nav = new HashMap<>(9);

        for (int i = 0; i < 9; i++) {
            NavigationItem navigationItem = navigationRow.get(i);

            if (navigationItem != null)
                nav.put(i, navigationItem);
        }

        return nav;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NavigationRow getNavigationRow() {
        return navigationRow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NavigationItem getNavigationItem(int slot) {
        return navigationRow.get(slot);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNavigationItem(NavigationItem navigationItem) {
        for (int i = 0; i < 9; i++) {
            if (navigationItem.equals(navigationRow.get(i)))
                return i;
        }

        return -1;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setNavigation(Integer slot, NavigationItem navigationItem) {
        navigationRow.set(slot, navigationItem);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<UUID, Integer> getViewers() {
        Map<UUID, Integer> viewers = new HashMap<>();

        for (int i = 0; i < pages.size(); i++) {
            Inventory page = pages.get(i);

            for (HumanEntity viewer : page.getViewers()) {
                viewers.put(viewer.getUniqueId(), i);
            }
        }

        return viewers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Inventory> getPages() {
        return pages;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSize() {
        return pages.size();
    }

    private void disperseViewers(List<HumanEntity> viewers, Integer fallbackIndex) {
        viewers = new ArrayList<>(viewers);
        if (viewers.isEmpty())
            return;

        if (fallbackIndex == null) {
            viewers.forEach(viewer -> {
                PagedInventoryCloseHandler.CloseHandler closeHandler = new PagedInventoryCloseHandler.CloseHandler(this, viewer.getOpenInventory(), ((Player) viewer));
                viewer.closeInventory();
                registrar.callGlobalCloseHandlers(closeHandler);
                callCloseHandlers(closeHandler);
            });

            return;
        }

        viewers.forEach(viewer -> open((Player) viewer, fallbackIndex, true));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!obj.getClass().equals(PagedInventory.class))
            return false;

        PagedInventory inv = (PagedInventory) obj;

        return registrar.equals(inv.registrar)
                && pages.equals(inv.pages)
                && navigationRow.equals(inv.navigationRow)
                && clickHandlers.equals(inv.clickHandlers)
                && closeHandlers.equals(inv.closeHandlers)
                && switchHandlers.equals(inv.switchHandlers);
    }

    private static NavigationRow getFromMap(Map<Integer, NavigationItem> navigation) {
        NextNavigationItem nextItem = null;
        PreviousNavigationItem previousItem = null;
        CloseNavigationItem closeItem = null;
        List<NavigationItem> navigationItems = new ArrayList<>(9);

        for (Map.Entry<Integer, NavigationItem> entry : navigation.entrySet()) {
            switch (entry.getValue().getNavigationType()) {
                case NEXT:
                    nextItem = (NextNavigationItem) entry.getValue();
                    break;
                case PREVIOUS:
                    previousItem = (PreviousNavigationItem) entry.getValue();
                    break;
                case CLOSE:
                    closeItem = (CloseNavigationItem) entry.getValue();
                default:
                    navigationItems.add(entry.getValue());
            }
        }

        return new NavigationRow(nextItem, previousItem, closeItem, navigationItems.toArray(new NavigationItem[0]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectMap<Player, SchedulerTask> getPlayerSchedulerFramesMap() {
        return playerSchedulerFramesMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectMap<Player, SchedulerTask> getPlayerSchedulerAnimatedMap() {
        return playerSchedulerAnimatedMap;
    }
}
