package lonestarrr.arconia.common.block.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.crafting.IPedestalRecipe;
import lonestarrr.arconia.common.crafting.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Tile Entity responsible for processing pedestal crafting rituals
 */
public class CenterPedestalTileEntity extends BasePedestalTileEntity implements ITickableTileEntity {
    private boolean ritualOngoing = false; // persisted
    private float ritualTicksElapsed = 0; // persisted
    private ResourceLocation currentRecipeID; // persisted
    private long lastTickTime = 0; // Time since last invocation of tick - not persisted
    private final static String TAG_RECIPE = "currentRecipe";
    private static final String TAG_ONGOING = "ritualOngoing";
    private static final String TAG_ELAPSED = "ritualTicksElapsed";
    private static final long TICK_UPDATE_INTERVAL = 20; // How often to do work in tick()

    public CenterPedestalTileEntity() {
        super(ModTiles.CENTER_PEDESTAL);
    }

    private IPedestalRecipe getCurrentRecipe() {
        if (currentRecipeID == null) {
            return null;
        }
        Optional<? extends IRecipe> recipe = world.getRecipeManager().getRecipe(currentRecipeID);
        if (recipe.isPresent() && recipe.get() instanceof IPedestalRecipe) {
            return (IPedestalRecipe)recipe.get();
        }
        return null;
    }

    @Override
    public void writePacketNBT(CompoundNBT tag) {
        super.writePacketNBT(tag);
        if (currentRecipeID != null) {
            tag.putString(TAG_RECIPE, currentRecipeID.toString());
        }
        tag.putBoolean(TAG_ONGOING, ritualOngoing);
        tag.putFloat(TAG_ELAPSED, ritualTicksElapsed);
    }

    @Override
    public void readPacketNBT(CompoundNBT tag) {
        super.readPacketNBT(tag);
        if (tag.contains(TAG_RECIPE)) {
            currentRecipeID = new ResourceLocation(tag.getString(TAG_RECIPE));
        }
        if (tag.contains(TAG_ONGOING)) {
            ritualOngoing = tag.getBoolean(TAG_ONGOING);
        }
        if (tag.contains(TAG_ELAPSED)) {
            ritualTicksElapsed = tag.getFloat(TAG_ELAPSED);
        }
    }

    public boolean isRitualOngoing() { return ritualOngoing; }

    public boolean startRitual() {
        if (world.isRemote()) {
            return false;
        }

        if (isRitualOngoing() || !getItemOnDisplay().isEmpty()) {
            return false;
        }

        List<PedestalTileEntity> pedestals = findPedestals();
        Arconia.logger.debug("Found " + pedestals.size() + " ritual pedestal(s)");
        Inventory inv = getPedestalItems(pedestals);

        if (inv.isEmpty()) {
            return false;
        }

        IPedestalRecipe recipe = findRecipe(inv);

        // TODO actually start a nice visual ritual. For now, just produce the output immediately, then iterate.
        if (recipe == null) {
            return false;
        }

        Arconia.logger.debug("Found a recipe for the ritual: " + recipe);

        currentRecipeID = recipe.getId();
        ritualOngoing = true;
        ritualTicksElapsed = 0;
        markDirty();
        return true;
    }

    public int getRitualProgressPercentage() {
        IPedestalRecipe recipe = getCurrentRecipe();
        if (!isRitualOngoing() || recipe == null) {
            return 0;
        }
        return Math.min(100, Math.round(this.ritualTicksElapsed * 100 / recipe.getDurationTicks()));
    }

    public void completeRitual() {
        if (world.isRemote()) {
            return;
        }

        IPedestalRecipe currentRecipe = getCurrentRecipe();
        if (!isRitualOngoing() || !getItemOnDisplay().isEmpty() || currentRecipe == null) {
            Arconia.logger.warn("Ritual completion attempted but conditions were not met - resetting ritual");
            resetRitual();
            return;
        }

        List<PedestalTileEntity> pedestals = findPedestals();
        Inventory inv = getPedestalItems(pedestals);

        if (currentRecipe.matches(inv, world)) {
            produceRecipeOutput();
            consumePedestalItems(pedestals);
            world.playSound(null, pos, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, 1, 1);
        }
        resetRitual();
    }

    private void consumePedestalItems(List<PedestalTileEntity> pedestals) {
        pedestals.forEach(pedestal->pedestal.removeItem());
    }

    private void produceRecipeOutput() {
        IPedestalRecipe currentRecipe = getCurrentRecipe();
        if (currentRecipe == null || !getItemOnDisplay().isEmpty()) {
            return;
        }

        ItemStack output = currentRecipe.getRecipeOutput().copy();
        this.putItem(output);
    }

    private void resetRitual() {
        currentRecipeID = null;
        ritualOngoing = false;
        ritualTicksElapsed = 0;
        lastTickTime = 0;
        markDirty();
        updateClient();
    }

    // Find nearby pedestals
    private List<PedestalTileEntity> findPedestals() {
        List<PedestalTileEntity> pedestals = new ArrayList<>();

        for (BlockPos posNear: BlockPos.getAllInBoxMutable(pos.west(3).north(3), pos.east(3).south(3))) {
            TileEntity te = world.getTileEntity(posNear);
            if (te == null) {
                continue;
            }
            if (!(te instanceof PedestalTileEntity)) {
                continue;
            }
            PedestalTileEntity pte = (PedestalTileEntity) te;
            // TODO pedestals should link to 1 specific center pedestal, done when placing down, but I did not build that yet. So yeeeeah you can cheat the
            // system by sharing a single pedestal with MULTIPLE crafting setups! :)
            pedestals.add(pte);
        }
        return pedestals;
    }

    private Inventory getPedestalItems(List<PedestalTileEntity> pedestals) {
        List<ItemStack> stacks = new ArrayList<>(pedestals.size());
        for (PedestalTileEntity entity: pedestals) {
            if (!entity.getItemOnDisplay().isEmpty()) {
                stacks.add(entity.getItemOnDisplay());
            }
        }

        Inventory inv = new Inventory(stacks.size());
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack ingredient = stacks.get(i);
            inv.setInventorySlotContents(i, ingredient);
        }

        return inv;
    }

    private IPedestalRecipe findRecipe(Inventory inv) {
        Optional<IPedestalRecipe> hasRecipe = world.getRecipeManager().getRecipe(ModRecipeTypes.PEDESTAL_TYPE, inv, world);
        if (hasRecipe.isPresent()) {
            return hasRecipe.get();
        }

        return null;
    }

    @Override
    public void tick() {
        if (world.isRemote() || !isRitualOngoing()) {
            return;
        }

        IPedestalRecipe currentRecipe = getCurrentRecipe();
        if (currentRecipe == null) {
            Arconia.logger.warn("Ongoing pedestal without a recipe, this should not happen!");
            resetRitual();
            return;
        }

        if (lastTickTime == 0) {
            lastTickTime = world.getGameTime();
            return;
        }

        long now = world.getGameTime();
        long ticksElapsed = now - lastTickTime;
        if (ticksElapsed < TICK_UPDATE_INTERVAL)
            return;

        ritualTicksElapsed += ticksElapsed;
        lastTickTime = now;
        if (ritualTicksElapsed > currentRecipe.getDurationTicks()) {
            completeRitual();
        } else {
            markDirty(); // unloading the entity should not reset the ritual entirely
            updateClient(); // TODO should probably only send to nearby players!
        }
    }
}