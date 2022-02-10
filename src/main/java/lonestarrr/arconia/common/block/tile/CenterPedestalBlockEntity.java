package lonestarrr.arconia.common.block.tile;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.crafting.IPedestalRecipe;
import lonestarrr.arconia.common.crafting.ModRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Tile Entity responsible for processing pedestal crafting rituals
 */
public class CenterPedestalBlockEntity extends BasePedestalTileEntity {
    private boolean ritualOngoing = false; // persisted
    private float ritualTicksElapsed = 0; // persisted
    private ResourceLocation currentRecipeID; // persisted
    private long lastTickTime = 0; // Time since last invocation of tick - not persisted
    private final static String TAG_RECIPE = "currentRecipe";
    private static final String TAG_ONGOING = "ritualOngoing";
    private static final String TAG_ELAPSED = "ritualTicksElapsed";
    private static final long TICK_UPDATE_INTERVAL = 20; // How often to do work in tick()

    public CenterPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CENTER_PEDESTAL, pos, state);
    }

    private IPedestalRecipe getCurrentRecipe() {
        if (currentRecipeID == null) {
            return null;
        }
        Optional<? extends Recipe> recipe = level.getRecipeManager().byKey(currentRecipeID);
        if (recipe.isPresent() && recipe.get() instanceof IPedestalRecipe) {
            return (IPedestalRecipe)recipe.get();
        }
        return null;
    }

    @Override
    public void writePacketNBT(CompoundTag tag) {
        super.writePacketNBT(tag);
        if (currentRecipeID != null) {
            tag.putString(TAG_RECIPE, currentRecipeID.toString());
        }
        tag.putBoolean(TAG_ONGOING, ritualOngoing);
        tag.putFloat(TAG_ELAPSED, ritualTicksElapsed);
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
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
        if (level.isClientSide()) {
            return false;
        }

        if (isRitualOngoing() || !getItemOnDisplay().isEmpty()) {
            return false;
        }

        List<PedestalBlockEntity> pedestals = findPedestals();
        Arconia.logger.debug("Found " + pedestals.size() + " ritual pedestal(s)");
        SimpleContainer inv = getPedestalItems(pedestals);

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
        setChanged();
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
        if (level.isClientSide()) {
            return;
        }

        IPedestalRecipe currentRecipe = getCurrentRecipe();
        if (!isRitualOngoing() || !getItemOnDisplay().isEmpty() || currentRecipe == null) {
            Arconia.logger.warn("Ritual completion attempted but conditions were not met - resetting ritual");
            resetRitual();
            return;
        }

        List<PedestalBlockEntity> pedestals = findPedestals();
        SimpleContainer inv = getPedestalItems(pedestals);

        if (currentRecipe.matches(inv, level)) {
            produceRecipeOutput();
            consumePedestalItems(pedestals);
            level.playSound(null, worldPosition, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.AMBIENT, 1, 1);
        }
        resetRitual();
    }

    private void consumePedestalItems(List<PedestalBlockEntity> pedestals) {
        pedestals.forEach(pedestal->pedestal.removeItem());
    }

    private void produceRecipeOutput() {
        IPedestalRecipe currentRecipe = getCurrentRecipe();
        if (currentRecipe == null || !getItemOnDisplay().isEmpty()) {
            return;
        }

        ItemStack output = currentRecipe.getResultItem().copy();
        this.putItem(output);
    }

    private void resetRitual() {
        currentRecipeID = null;
        ritualOngoing = false;
        ritualTicksElapsed = 0;
        lastTickTime = 0;
        setChanged();
        updateClient();
    }

    // Find nearby pedestals
    private List<PedestalBlockEntity> findPedestals() {
        List<PedestalBlockEntity> pedestals = new ArrayList<>();

        for (BlockPos posNear: BlockPos.betweenClosed(worldPosition.west(3).north(3), worldPosition.east(3).south(3))) {
            BlockEntity te = level.getBlockEntity(posNear);
            if (te == null) {
                continue;
            }
            if (!(te instanceof PedestalBlockEntity)) {
                continue;
            }
            PedestalBlockEntity pte = (PedestalBlockEntity) te;
            // TODO pedestals should link to 1 specific center pedestal, done when placing down, but I did not build that yet. So yeeeeah you can cheat the
            // system by sharing a single pedestal with MULTIPLE crafting setups! :)
            pedestals.add(pte);
        }
        return pedestals;
    }

    private SimpleContainer getPedestalItems(List<PedestalBlockEntity> pedestals) {
        List<ItemStack> stacks = new ArrayList<>(pedestals.size());
        for (PedestalBlockEntity entity: pedestals) {
            if (!entity.getItemOnDisplay().isEmpty()) {
                stacks.add(entity.getItemOnDisplay());
            }
        }

        SimpleContainer inv = new SimpleContainer(stacks.size());
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack ingredient = stacks.get(i);
            inv.setItem(i, ingredient);
        }

        return inv;
    }

    private IPedestalRecipe findRecipe(SimpleContainer inv) {
        Optional<IPedestalRecipe> hasRecipe = level.getRecipeManager().getRecipeFor(ModRecipeTypes.PEDESTAL_TYPE, inv, level);
        if (hasRecipe.isPresent()) {
            return hasRecipe.get();
        }

        return null;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CenterPedestalBlockEntity blockEntity) {
        blockEntity.tickInternal(level, pos, state);
    }

    private void tickInternal(Level level, BlockPos pos, BlockState state) {
        if (!isRitualOngoing()) {
            return;
        }

        IPedestalRecipe currentRecipe = getCurrentRecipe();
        if (currentRecipe == null) {
            Arconia.logger.warn("Ongoing pedestal without a recipe, this should not happen!");
            resetRitual();
            return;
        }

        if (lastTickTime == 0) {
            lastTickTime = level.getGameTime();
            return;
        }

        long now = level.getGameTime();
        long ticksElapsed = now - lastTickTime;
        if (ticksElapsed < TICK_UPDATE_INTERVAL)
            return;

        ritualTicksElapsed += ticksElapsed;
        lastTickTime = now;
        if (ritualTicksElapsed > currentRecipe.getDurationTicks()) {
            completeRitual();
        } else {
            setChanged(); // unloading the entity should not reset the ritual entirely
            updateClient(); // TODO should probably only send to nearby players!
        }
    }
}