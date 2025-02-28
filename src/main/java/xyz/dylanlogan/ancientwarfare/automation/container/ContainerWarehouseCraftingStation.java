package xyz.dylanlogan.ancientwarfare.automation.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import xyz.dylanlogan.ancientwarfare.automation.tile.warehouse2.TileWarehouseBase;
import xyz.dylanlogan.ancientwarfare.automation.tile.warehouse2.TileWarehouseCraftingStation;
import xyz.dylanlogan.ancientwarfare.core.api.AWItems;
import xyz.dylanlogan.ancientwarfare.core.config.AWLog;
import xyz.dylanlogan.ancientwarfare.core.container.ContainerTileBase;
import xyz.dylanlogan.ancientwarfare.core.inventory.ItemQuantityMap;
import xyz.dylanlogan.ancientwarfare.core.inventory.ItemQuantityMap.ItemHashEntry;
import xyz.dylanlogan.ancientwarfare.core.item.ItemResearchBook;

public class ContainerWarehouseCraftingStation extends ContainerTileBase<TileWarehouseCraftingStation> {

    private ItemQuantityMap itemMap = new ItemQuantityMap();
    private final ItemQuantityMap cache = new ItemQuantityMap();
    private boolean shouldUpdate = true;

    public ContainerWarehouseCraftingStation(final EntityPlayer player, int x, int y, int z) {
        super(player, x, y, z);
        IInventory inventory = tileEntity.layoutMatrix;

        Slot slot = new SlotCrafting(player, inventory, tileEntity.result, 0, 3 * 18 + 3 * 18 + 8 + 18, 1 * 18 + 8) {
            @Override
            public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack) {
                tileEntity.preItemCrafted();
                super.onPickupFromSlot(par1EntityPlayer, par2ItemStack);
                tileEntity.onItemCrafted();
            }
        };
        addSlotToContainer(slot);

        slot = new Slot(tileEntity.bookInventory, 0, 8, 18 + 8) {
            @Override
            public boolean isItemValid(ItemStack par1ItemStack) {
                return ItemResearchBook.getResearcherName(par1ItemStack) != null;
            }
        };
        addSlotToContainer(slot);

        int x2, y2, slotNum = 0;
        for (int y1 = 0; y1 < 3; y1++) {
            y2 = y1 * 18 + 8;
            for (int x1 = 0; x1 < 3; x1++) {
                x2 = x1 * 18 + 8 + 3 * 18;
                slotNum = y1 * 3 + x1;
                slot = new Slot(inventory, slotNum, x2, y2);
                addSlotToContainer(slot);
            }
        }

        int y1 = 8 + 3 * 18 + 8;
        y1 = this.addPlayerSlots(y1);
        TileWarehouseBase warehouse = tileEntity.getWarehouse();
        if (warehouse != null) {
            warehouse.addCraftingViewer(this);
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer par1EntityPlayer) {
        TileWarehouseBase warehouse = tileEntity.getWarehouse();
        if (warehouse != null) {
            warehouse.removeCraftingViewer(this);
        }
        super.onContainerClosed(par1EntityPlayer);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotClickedIndex) {
        ItemStack slotStackCopy = null;
        Slot theSlot = this.getSlot(slotClickedIndex);
        if (theSlot != null && theSlot.getHasStack()) {
            ItemStack slotStack = theSlot.getStack();
            slotStackCopy = slotStack.copy();
            int playerSlotStart = 2 + tileEntity.layoutMatrix.getSizeInventory();
            if (slotClickedIndex < playerSlotStart)//result slot, book slot
            {
                if (!this.mergeItemStack(slotStack, playerSlotStart, playerSlotStart + playerSlots, false))//merge into player inventory
                {
                    return null;
                }
            }
            if (slotStack.stackSize == 0) {
                theSlot.putStack(null);
            } else {
                theSlot.onSlotChanged();
            }
            if (slotStack.stackSize == slotStackCopy.stackSize) {
                return null;
            }
            theSlot.onPickupFromSlot(par1EntityPlayer, slotStack);
        }
        return slotStackCopy;
    }

    @Override
    public void handlePacketData(NBTTagCompound tag) {
        if (tag.hasKey("changeList")) {
            AWLog.logDebug("rec. warehouse item map..");
            handleChangeList(tag.getTagList("changeList", Constants.NBT.TAG_COMPOUND));
        }
        refreshGui();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (shouldUpdate) {
            synchItemMaps();
            shouldUpdate = false;
        }
    }

    private void handleChangeList(NBTTagList changeList) {
        NBTTagCompound tag;
        int qty;
        ItemHashEntry wrap;
        for (int i = 0; i < changeList.tagCount(); i++) {
            tag = changeList.getCompoundTagAt(i);
            wrap = ItemHashEntry.readFromNBT(tag);
            qty = tag.getInteger("qty");
            if (qty == 0) {
                itemMap.remove(wrap);
            } else if(wrap != null){
                itemMap.put(wrap, qty);
            }
        }
        TileWarehouseBase warehouse = tileEntity.getWarehouse();
        if (warehouse != null) {
            warehouse.clearItemCache();
            warehouse.addItemsToCache(itemMap);
        }
    }

    private void synchItemMaps() {
        /**
         * need to loop through this.itemMap and compare quantities to warehouse.itemMap
         *    add any changes to change-list
         * need to loop through warehouse.itemMap and find new entries
         *    add any new entries to change-list
         */

        cache.clear();
        TileWarehouseBase warehouse = tileEntity.getWarehouse();
        if (warehouse != null) {
            warehouse.getItems(cache);
        }
        ItemQuantityMap warehouseItemMap = cache;
        int qty;
        NBTTagList changeList = new NBTTagList();
        NBTTagCompound tag;
        for (ItemHashEntry wrap : this.itemMap.keySet()) {
            qty = this.itemMap.getCount(wrap);
            if (qty != warehouseItemMap.getCount(wrap)) {
                qty = warehouseItemMap.getCount(wrap);
                tag = wrap.writeToNBT(new NBTTagCompound());
                tag.setInteger("qty", qty);
                changeList.appendTag(tag);
                this.itemMap.put(wrap, qty);
            }
        }
        for (ItemHashEntry entry : warehouseItemMap.keySet()) {
            if (!itemMap.contains(entry)) {
                qty = warehouseItemMap.getCount(entry);
                tag = ItemHashEntry.writeToNBT(entry, new NBTTagCompound());
                tag.setInteger("qty", qty);
                changeList.appendTag(tag);
                this.itemMap.put(entry, qty);
            }
        }
        if (changeList.tagCount() > 0) {
            tag = new NBTTagCompound();
            tag.setTag("changeList", changeList);
            sendDataToClient(tag);
        }
    }

    public void onWarehouseInventoryUpdated() {
        AWLog.logDebug("update callback from warehouse...");
        shouldUpdate = true;
    }

}
