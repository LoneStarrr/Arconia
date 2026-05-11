package lonestarrr.arconia.common.block.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.crafting.ModRecipeTypes;
import lonestarrr.arconia.common.crafting.PedestalInput;
import lonestarrr.arconia.common.crafting.PedestalRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.NotNull;

/** Block Entity responsible for processing pedestal crafting rituals */
public class CenterPedestalBlockEntity extends BasePedestalBlockEntity {
  private boolean ritualOngoing = false; // persisted
  private float ritualTicksElapsed = 0; // persisted
  public long ritualStartTime = 0; // not persisted, used only client side to track animation
  public long lastParticleDisplayTime = 0; // not persisted, purely client side
  private Identifier currentRecipeID; // persisted
  // Cached so the client renderer doesn't need a server-only RecipeManager lookup to compute
  // progress.
  private int currentRecipeDuration = 0; // persisted
  private long lastTickTime = 0; // Time since last invocation of tick - not persisted
  private static final String TAG_RECIPE = "currentRecipe";
  private static final String TAG_RECIPE_DURATION = "currentRecipeDuration";
  private static final String TAG_ONGOING = "ritualOngoing";
  private static final String TAG_ELAPSED = "ritualTicksElapsed";
  private static final String TAG_RITUAL_START_TIME = "ritualStartTime";
  private static final long TICK_UPDATE_INTERVAL = 4; // How often to do work in tick()

  private final ItemStacksResourceHandler inventory =
      new ItemStacksResourceHandler(1) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
          setChanged();
          updateClient();
        }

        @Override
        protected int getCapacity(int index, ItemResource resource) {
          return 1;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
          return false; // Output only - this blocks insertion
        }
      };

  public CenterPedestalBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.CENTER_PEDESTAL.get(), pos, state);
  }

  @Override
  protected ItemStacksResourceHandler getInventory() {
    return inventory;
  }

  private PedestalRecipe getCurrentRecipe() {
    if (currentRecipeID == null) {
      return null;
    }
    Optional<RecipeHolder<?>> recipe =
        level
            .getServer()
            .getRecipeManager()
            .byKey(ResourceKey.create(Registries.RECIPE, currentRecipeID));
    if (recipe.isPresent() && recipe.get().value() instanceof PedestalRecipe) {
      return (PedestalRecipe) recipe.get().value();
    }
    return null;
  }

  @Override
  public void writePacketNBT(@NotNull ValueOutput output) {
    super.writePacketNBT(output);
    if (currentRecipeID != null) {
      output.putString(TAG_RECIPE, currentRecipeID.toString());
    }
    output.putInt(TAG_RECIPE_DURATION, currentRecipeDuration);
    output.putBoolean(TAG_ONGOING, ritualOngoing);
    output.putFloat(TAG_ELAPSED, ritualTicksElapsed);
  }

  @Override
  public void readPacketNBT(@NotNull ValueInput input) {
    super.readPacketNBT(input);
    input.getString(TAG_RECIPE).ifPresent(s -> currentRecipeID = Identifier.parse(s));
    input.getInt(TAG_RECIPE_DURATION).ifPresent(v -> currentRecipeDuration = v);
    ritualTicksElapsed = input.getFloatOr(TAG_ELAPSED, 0f);
    if (ritualTicksElapsed >= 0 && this.level != null && this.level.isClientSide()) {
      // Track this for animating the ritual client side
      ritualStartTime = this.level.getGameTime() - (long) ritualTicksElapsed;
    }
    ritualOngoing = input.getBooleanOr(TAG_ONGOING, false);
  }

  public boolean isRitualOngoing() {
    return ritualOngoing;
  }

  public boolean startRitual() {
    if (level.isClientSide()) {
      return false;
    }

    if (isRitualOngoing() || !getItemOnDisplay().isEmpty()) {
      return false;
    }

    List<PedestalBlockEntity> pedestals = findPedestals();
    Arconia.logger.debug("Found " + pedestals.size() + " ritual pedestal(s)");
    PedestalInput inv = getPedestalItems(pedestals);

    if (inv.isEmpty()) {
      return false;
    }

    Identifier recipeId = findRecipe(inv);

    if (recipeId == null) {
      return false;
    }

    currentRecipeID = recipeId;
    PedestalRecipe recipe = getCurrentRecipe();
    if (recipe == null) {
      currentRecipeID = null;
      return false;
    }
    currentRecipeDuration = recipe.getDurationTicks();
    ritualOngoing = true;
    ritualTicksElapsed = 0;
    setChanged();
    updateClient();
    return true;
  }

  /**
   * Client-side method used by the renderer. Reads from the synced duration cache rather than the
   * RecipeManager (which is server-only).
   *
   * @return A non-precise elapsed % of the ongoing ritual
   */
  public float getRitualProgressPercentage() {
    if (!isRitualOngoing() || currentRecipeDuration <= 0) {
      return 0;
    }

    // TODO this also won't work across game restarts
    return (this.level.getGameTime() - this.ritualStartTime) / (float) currentRecipeDuration * 100f;
    // return Math.min(100f, this.ritualTicksElapsed / (float) currentRecipeDuration * 100f);
  }

  public void completeRitual() {
    if (level.isClientSide()) {
      return;
    }

    PedestalRecipe currentRecipe = getCurrentRecipe();
    if (!isRitualOngoing() || !getItemOnDisplay().isEmpty() || currentRecipe == null) {
      Arconia.logger.warn(
          "Ritual completion attempted but conditions were not met - resetting ritual");
      resetRitual();
      return;
    }

    List<PedestalBlockEntity> pedestals = findPedestals();
    PedestalInput inv = getPedestalItems(pedestals);

    if (currentRecipe.matches(inv, level)) {
      produceRecipeOutput();
      consumePedestalItems(pedestals);
      level.playSound(
          null, worldPosition, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.AMBIENT, 1, 1);
    }
    resetRitual();
  }

  private void consumePedestalItems(List<PedestalBlockEntity> pedestals) {
    pedestals.forEach(pedestal -> pedestal.removeItem());
  }

  private void produceRecipeOutput() {
    PedestalRecipe currentRecipe = getCurrentRecipe();
    if (currentRecipe == null || !getItemOnDisplay().isEmpty()) {
      return;
    }

    ItemStack output = currentRecipe.getOutput().copy();
    this.putItem(output);
  }

  private void resetRitual() {
    ritualOngoing = false;
    currentRecipeID = null;
    currentRecipeDuration = 0;
    ritualTicksElapsed = 0;
    ritualStartTime = 0;
    lastTickTime = 0;
    setChanged();
    updateClient();
  }

  // Find nearby pedestals
  private List<PedestalBlockEntity> findPedestals() {
    List<PedestalBlockEntity> pedestals = new ArrayList<>();

    for (BlockPos posNear :
        BlockPos.betweenClosed(worldPosition.west(3).north(3), worldPosition.east(3).south(3))) {
      BlockEntity te = level.getBlockEntity(posNear);
      if (te == null) {
        continue;
      }
      if (!(te instanceof PedestalBlockEntity)) {
        continue;
      }
      PedestalBlockEntity pte = (PedestalBlockEntity) te;
      // TODO pedestals should link to 1 specific center pedestal, done when placing down, but I did
      // not build that yet. So yeeeeah you can cheat the
      // system by sharing a single pedestal with MULTIPLE crafting setups! :)
      pedestals.add(pte);
    }
    return pedestals;
  }

  private PedestalInput getPedestalItems(List<PedestalBlockEntity> pedestals) {
    List<ItemStack> stacks = new ArrayList<>(pedestals.size());
    for (PedestalBlockEntity entity : pedestals) {
      if (!entity.getItemOnDisplay().isEmpty()) {
        stacks.add(entity.getItemOnDisplay());
      }
    }

    return new PedestalInput(stacks);
  }

  private Identifier findRecipe(PedestalInput inv) {
    Optional<RecipeHolder<PedestalRecipe>> hasRecipe =
        level
            .getServer()
            .getRecipeManager()
            .getRecipeFor(ModRecipeTypes.PEDESTAL_TYPE.get(), inv, level);
    return hasRecipe.map(h -> h.id().identifier()).orElse(null);
  }

  public static void tick(
      Level level, BlockPos pos, BlockState state, CenterPedestalBlockEntity blockEntity) {
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
    if (ticksElapsed < TICK_UPDATE_INTERVAL) return;

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
