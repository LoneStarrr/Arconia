package lonestarrr.arconia.common.block.tile;

import com.mojang.datafixers.types.templates.CompoundList;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import lonestarrr.arconia.common.core.helper.InventoryHelper;
import lonestarrr.arconia.common.item.ModItems;
import lonestarrr.arconia.common.lib.tile.BaseTileEntity;
import lonestarrr.arconia.common.network.ModPackets;
import lonestarrr.arconia.common.network.OrbLaserPacket;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for pulling in nearby item entities of a specific type, like a magnet. If an inventory is below the tile entity, it will attempt to
 * store them in there.  Has no buffer of its own.
 */
public class OrbTileEntity extends BaseTileEntity implements TickableBlockEntity {
    private List<ItemStack> itemsToPull = new ArrayList<>();
    private static final int MAX_ITEMS = 9;
    public static final String TAG_ITEM = "item";
    public static final int TICK_INTERVAL = 10;
    private static final int RANGE = 10;
    private int ticksElapsed;

    public OrbTileEntity() {
        super(ModTiles.ORB);
    }

    public boolean addItem(ItemStack item) {
        if (item.isEmpty()) {
            return false;
        }

        if (itemsToPull.size() < MAX_ITEMS) {
            itemsToPull.add(item.copy());
            setChanged();
            updateClient();
            return true;
        }

        return false;
    }

    @Nonnull
    public ItemStack popItem() {
        if (itemsToPull.size() > 0) {
            ItemStack stack = itemsToPull.remove(itemsToPull.size() - 1);
            setChanged();
            updateClient();
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    public final List<ItemStack> getItems() {
        return itemsToPull;
    }

    @Override
    public void writePacketNBT(CompoundTag tag) {
        ListTag list = new ListTag();
        for (ItemStack item: itemsToPull) {
            list.add(item.serializeNBT());
        }
        tag.put(TAG_ITEM, list);
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
        if (tag.contains(TAG_ITEM)) {
            ListTag list = tag.getList(TAG_ITEM, Constants.NBT.TAG_COMPOUND);
            List<ItemStack> items = new ArrayList(list.size());
            for (int i = 0; i < list.size(); i++) {
                CompoundTag data = list.getCompound(i);
                ItemStack item = ItemStack.of(data);
                items.add(item);
            }
            itemsToPull = items;
        }
    }

    @Override
    public void tick() {
        if (++ticksElapsed < TICK_INTERVAL) {
            return;
        }
        ticksElapsed = 0;

        double x = worldPosition.getX() + 0.5;
        double y = worldPosition.getY() + 0.5;
        double z = worldPosition.getZ() + 0.5;

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(x - RANGE, y - RANGE, z - RANGE, x + RANGE, y + RANGE, z + RANGE));
        IItemHandler inv = InventoryHelper.getInventory(getLevel(), worldPosition.below(), Direction.UP);

        final int maxHandled = 64;
        int handled = 0;

        for (ItemEntity item: items) {
            if (++handled > maxHandled) {
                break;
            }

            if (!canPullItem(item)) {
                continue;
            }

            if (inv != null) {
                ItemStack before = item.getItem();
                int beforeCount = before.getCount();
                if (!before.isEmpty()) {
                    if (!level.isClientSide()) {
                        ItemStack left = InventoryHelper.insertItem(inv, item.getItem(), false);
                        item.setItem(left);
                        // Send to client to render visualization of item being captured
                        // TODO: bunch up into a list to send
                        if (left.isEmpty() || left.getCount() < beforeCount) {
                            OrbLaserPacket packet = new OrbLaserPacket(worldPosition, item.blockPosition(), before);
                            ModPackets.sendToNearby(level, worldPosition, packet);
                        }
                    }

                }
            }
        }
    }

    private boolean canPullItem(ItemEntity item) {
        // For PreventRemoteMovement, see: https://www.curseforge.com/minecraft/mc-mods/demagnetize
        if (!item.isAlive() || item.getPersistentData().getBoolean("PreventRemoteMovement")) {
            return false;
        }

        ItemStack pulled = item.getItem();

        for (ItemStack toPull: itemsToPull) {
            if (pulled.sameItemStackIgnoreDurability(toPull)) {
                return true;
            }
        }

        return false;
    }
}
