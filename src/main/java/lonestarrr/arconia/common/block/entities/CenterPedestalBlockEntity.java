package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.crafting.ModRecipeTypes;
import lonestarrr.arconia.common.crafting.PedestalRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Block Entity responsible for processing pedestal crafting rituals
 */
public class CenterPedestalBlockEntity extends BasePedestalBlockEntity {
    private boolean ritualOngoing = false; // persisted
    private float ritualTicksElapsed = 0; // persisted
    public long ritualStartTime = 0; // not persisted, used only client side to track animation
    public long lastParticleDisplayTime = 0; // not persisted, purely client side
    private ResourceLocation currentRecipeID; // persisted
    private long lastTickTime = 0; // Time since last invocation of tick - not persisted
    private final static String TAG_RECIPE = "currentRecipe";
    private static final String TAG_ONGOING = "ritualOngoing";
    private static final String TAG_ELAPSED = "ritualTicksElapsed";
    private static final String TAG_RITUAL_START_TIME = "ritualStartTime";
    private static final long TICK_UPDATE_INTERVAL = 4; // How often to do work in tick()

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            updateClient();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false; // Output only - this blocks insertion
        }
    };

    public CenterPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CENTER_PEDESTAL.get(), pos, state);
    }

    @Override
    protected ItemStackHandler getInventory() {
        return inventory;
    }

    private PedestalRecipe getCurrentRecipe() {
        if (currentRecipeID == null) {
            return null;
        }
        Optional<RecipeHolder<?>> recipe = level.getRecipeManager().byKey(currentRecipeID);
        if (recipe.isPresent() && recipe.get().value() instanceof PedestalRecipe) {
            return (PedestalRecipe)recipe.get().value();
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
            if (this.level != null && this.level.isClientSide) {
                // Track this for animating the ritual client side
                ritualStartTime = this.level.getGameTime() - (long) ritualTicksElapsed;
            }
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

        ResourceLocation recipeId = findRecipe(inv);

        if (recipeId == null) {
            return false;
        }

        currentRecipeID = recipeId;
        ritualOngoing = true;
        ritualTicksElapsed = 0;
        setChanged();
        updateClient();
        return true;
    }

    /**
     * Cient-side only method (TODO should mark it as such?)
     * @return A non-precise elapsed % of the ongoing ritual
     */
    public float getRitualProgressPercentage() {
        PedestalRecipe recipe = getCurrentRecipe();
        if (!isRitualOngoing() || recipe == null) {
            return 0;
        }

        // TODO this also won't work across game restarts
        return (this.level.getGameTime() - this.ritualStartTime) / (float)recipe.getDurationTicks() * 100f;
        //return Math.min(100f, this.ritualTicksElapsed / (float)recipe.getDurationTicks() * 100f);
    }

    public void completeRitual() {
        if (level.isClientSide()) {
            return;
        }

        PedestalRecipe currentRecipe = getCurrentRecipe();
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
        PedestalRecipe currentRecipe = getCurrentRecipe();
        if (currentRecipe == null || !getItemOnDisplay().isEmpty()) {
            return;
        }

        ItemStack output = currentRecipe.getResultItem(Minecraft.getInstance().level.registryAccess()).copy();
        this.putItem(output);
    }

    private void resetRitual() {
        currentRecipeID = null;
        ritualOngoing = false;
        ritualTicksElapsed = 0;
        ritualStartTime = 0;
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

    private ResourceLocation findRecipe(SimpleContainer inv) {
        Optional<RecipeHolder<PedestalRecipe>> hasRecipe = level.getRecipeManager().getRecipeFor(ModRecipeTypes.PEDESTAL_TYPE.get(), inv, level);
        return hasRecipe.map(RecipeHolder::id).orElse(null);

    }

    public static void tick(Level level, BlockPos pos, BlockState state, CenterPedestalBlockEntity blockEntity) {
        blockEntity.tickInternal(level, pos, state);
    }

    private void tickInternal(Level level, BlockPos pos, BlockState state) {
        if (!isRitualOngoing()) {
            return;
        }

        PedestalRecipe currentRecipe = getCurrentRecipe();
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