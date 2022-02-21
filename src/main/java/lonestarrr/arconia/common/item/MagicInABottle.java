package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.PlayerInventoryHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Time in a bottle? What's that? Never heard of that. No, this is *magic* in a bottle. It generates a random resource from a loot table at a slow rate.
 * These resources can be used to kickstart a resource generator (of a resource tree), which will then generate that resource at a much faster rate.
 * Mainly intended for use in skyblocks where these items are not necessarily available, in a way that is not grindy but just takes time.
 */
public class MagicInABottle extends Item {
    private static final String TAG_TICKS_ELAPSED = "ticksElapsed";
    private static final String TAG_FILL_PERCENTAGE = "fillPercentage";
    private static final String TAG_COLOR = "color";
    private static final int fillPercentageTimeBased = 10; // Percentage of bottle to fill based on just time progression (mod cfg TODO)

    public MagicInABottle(Item.Properties builder) {
        super(builder.stacksTo(1));
    }

    public static ItemStack getBottleForTier(RainbowColor tier) {
        ItemStack stack = new ItemStack(ModItems.magicInABottle);
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_COLOR, tier.getTierName());
        stack.setTag(tag);
        return stack;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // for that visual enchanted effect only
        return false;
    }

    public static int getTicksElapsed(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != ModItems.magicInABottle) {
            return 0;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(TAG_TICKS_ELAPSED);
    }

    public static void setTicksElapsed(ItemStack stack, int ticks) {
        if (!stack.isEmpty() && stack.getItem() == ModItems.magicInABottle) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt(TAG_TICKS_ELAPSED, ticks);
        }
    }

    // Fills up the bottle by a partial amount, if it's not full already. Alerts the player if it just filled up
    private static int fill(Level world, ServerPlayer player, ItemStack stack, int percent) {
        int fillPct = getFillPercentage(stack);
        if (fillPct < 100) {
            fillPct = Math.min(100, fillPct + percent);
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt(TAG_FILL_PERCENTAGE, fillPct);
            if (fillPct == 100) {
                player.playSound(SoundEvents.BREWING_STAND_BREW, 1, 1);
            }
        }

        return fillPct;
    }

    private static void resetFillPercentage(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_FILL_PERCENTAGE, 0);
    }

    private static int getFillPercentage(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(TAG_FILL_PERCENTAGE);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotIn, boolean selected) {
        // Time passing will slowly fill up the bottle
        if (world.isClientSide() || !(entity instanceof ServerPlayer)) {
            return;
        }
        final int tickEvalInterval = 20;
        ServerPlayer player = (ServerPlayer)entity;

        long gameTime = world.getGameTime();

        if (gameTime % tickEvalInterval == 0) {
            int ticksElapsed = getTicksElapsed(stack) + tickEvalInterval;
            int ticksNextFill = getTicksPerFill(stack);

            if (ticksElapsed >= ticksNextFill) {
                fill(world, player, stack, fillPercentageTimeBased);
                setTicksElapsed(stack, 0);
            } else {
                setTicksElapsed(stack, ticksElapsed);
            }

            if (gameTime % (tickEvalInterval * 3) == 0){
                resetOtherBottles(player, stack);
            }
        }
    }

    /**
     * Jumping with the bottle in the hotbar will speed up filling the bottle
     * @param living
     */
    public static void onPlayerJump(LivingEntity living) {
        if (living instanceof ServerPlayer && !(living instanceof FakePlayer)) {
            ServerPlayer player = (ServerPlayer)living;
            int hotbarSlot = PlayerInventoryHelper.findSlotMatchingItem(player.getInventory(), new ItemStack(ModItems.magicInABottle, 1), false, PlayerInventoryHelper.SLOT_HOTBAR);
            if (hotbarSlot != -1) {
                ItemStack bottle = player.getInventory().getItem(hotbarSlot);
                fill(player.level, player, bottle, getFillPercentagePerJump(bottle));
            }
        }
    }

    // Prevents multiple bottles in a player's inv from getting ticked by resetting all counts but one. This works because they all get ticked and the
    // highest will 'win'
    private static void resetOtherBottles(ServerPlayer player, ItemStack stack) {
        int filledPct = getFillPercentage(stack);

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack otherStack = player.getInventory().getItem(slot);
            if (otherStack.getItem() == stack.getItem() && otherStack != stack) {
                int otherFilledPct = getFillPercentage(otherStack);
                if (otherFilledPct > 0 && otherFilledPct < filledPct) {
                    resetFillPercentage(otherStack);
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player playerEntity, InteractionHand hand) {
        ItemStack stack = playerEntity.getItemInHand(hand);
        if (stack.isEmpty() || stack.getItem() != this) {
            return InteractionResultHolder.pass(stack);
        }

        // Without syncing ticks to client, can't really know on the client when time has elapsed because it will desync. So just consume the click.
        if (world.isClientSide()) {
            return InteractionResultHolder.consume(stack);
        }

        int filledPct = getFillPercentage(stack);
        if (filledPct < 100) {
            return InteractionResultHolder.fail(stack);
        }

        List<ItemStack> lootCollection = getLoot(stack, world);
        BlockPos spawnPos = playerEntity.blockPosition();
        spawnPos.offset(playerEntity.getLookAngle().x, playerEntity.getLookAngle().y, playerEntity.getLookAngle().z); // somewhat in front of player?
        for (ItemStack loot: lootCollection) {
            ItemEntity entity = new ItemEntity(world, spawnPos.getX(), spawnPos.getY(),
                    spawnPos.getZ() ,
                    loot);
            entity.setDeltaMovement(0D, 0.0D, 0D);
            entity.setNoPickUpDelay();
            entity.lifespan = 200;
            world.addFreshEntity(entity);
            world.playSound(null, spawnPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1, 1);
        }

        resetFillPercentage(stack);
        return InteractionResultHolder.success(stack);
    }

    /**
     * How many ticks have to elapse before the bottle gets automatically filled up by a partial amount?
     * @param stack
     * @return
     */
    public static int getTicksPerFill(ItemStack stack) {
        RainbowColor tier = getTier(stack);
        return (int)(30 * 20 * Math.pow(0.8, tier.getTier()));
    }

    /**
     * @param stack
     * @return Percentage of the bottle will be filled per player jump
     */
    public static int getFillPercentagePerJump(ItemStack stack) {
        RainbowColor tier = getTier(stack);
        return 5; // TODO modconfig
    }

    public static RainbowColor getTier(ItemStack stack) {
        RainbowColor tier = RainbowColor.RED;

        if (stack.isEmpty() || stack.getItem() != ModItems.magicInABottle) {
            return tier;
        }

        CompoundTag tag = stack.getOrCreateTag();
        String tierStr = tag.getString(TAG_COLOR);
        try {
            tier = RainbowColor.valueOf(tierStr);
        } catch (IllegalArgumentException e) {}
        return tier;
    }

    protected List<ItemStack> getLoot(ItemStack stack, Level world) {
        RainbowColor tier = getTier(stack);

        final ResourceLocation lootResource = new ResourceLocation(Arconia.MOD_ID, "magic_in_a_bottle_" + tier.getTierName());
        LootTable lootTable = ((ServerLevel) world).getServer().getLootTables().get(lootResource);
        LootContext ctx = new LootContext.Builder((ServerLevel) world).create(LootContextParamSets.EMPTY);
        List<ItemStack> stacks = lootTable.getRandomItems(ctx);
        return stacks;
    }

    @OnlyIn(Dist.CLIENT)
    public static float getFilledItemProperty(ItemStack stack, ClientLevel world, LivingEntity entity, int seed) {
        // Used to register ItemProperty, used to render model based on filled %
        return getFillPercentage(stack) / 100f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(
            ItemStack stack, @Nullable Level world, List<Component> toolTips, TooltipFlag flag) {
        super.appendHoverText(stack, world, toolTips, flag);
        int pct = getFillPercentage(stack);
        toolTips.add(new TranslatableComponent(stack.getDescriptionId() + ".tooltip", pct).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
    }
}
