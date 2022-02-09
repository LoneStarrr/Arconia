package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.core.RainbowColor;
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

import javax.annotation.Nullable;
import java.util.List;

/**
 * Time in a bottle? What's that? Never heard of that. No, this is *magic* in a bottle. It generates a random resource from a loot table at a slow rate.
 * These resources can be used to kickstart a resource generator (of a resource tree), which will then generate that resource at a much faster rate.
 * Mainly intended for use in skyblocks where these items are not necessarily available, in a way that is not grindy but just takes time.
 */
public class MagicInABottle extends Item {
    private static final String TAG_TICKS_ELAPSED = "ticksElapsed";

    public MagicInABottle(Item.Properties builder) {
        super(builder.stacksTo(1));
    }


    @Override
    public boolean isFoil(ItemStack stack) {
        // for that visual enchanted effect only
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos(); // TODO this is pos player is AT, not looking at. That is a client-side thing..
        ItemStack itemStack = player.getItemInHand(context.getHand());

        // For testing purposes, just cycle through the tiers for now when it's used on anything
        if (!world.isClientSide()) {
            RainbowColor currentTier = getTier(itemStack);
            int tierNum = currentTier.ordinal();
            tierNum = (tierNum >= RainbowColor.values().length - 1 ? 0: tierNum + 1);
            RainbowColor newTier = RainbowColor.values()[tierNum];
            CompoundTag tag = itemStack.getTag();
            if (tag == null) {
                tag = new CompoundTag();
                itemStack.setTag(tag);
            }
            tag.putString("tier", newTier.name());
            player.displayClientMessage(new TextComponent("Taste the " + newTier.getTierName() + " rainbow!"), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(
            ItemStack stack, @Nullable Level world, List<Component> toolTips, TooltipFlag flag) {
        super.appendHoverText(stack, world, toolTips, flag);
        int ticksElapsed = getTicksElapsed(stack);
        int ticksBetweenLoot = getTicksBetweenLoot(stack);
        int pct = (int)Math.min(100, (int)(ticksElapsed * 100d / ticksBetweenLoot));
        toolTips.add(new TranslatableComponent(stack.getDescriptionId() + ".tooltip", pct).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
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

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotIn, boolean selected) {
        if (world.isClientSide() || !(entity instanceof ServerPlayer)) {
            return;
        }
        final int tickEvalInterval = 20;

        long gameTime = world.getGameTime();

        if (gameTime % tickEvalInterval == 0) {
            int ticksElapsed = getTicksElapsed(stack);
            int ticksNextLoot = getTicksBetweenLoot(stack);
            if (ticksElapsed < ticksNextLoot) {
                ticksElapsed += tickEvalInterval;
                setTicksElapsed(stack, ticksElapsed);
                if (ticksElapsed >= ticksNextLoot) {
                    world.playSound(null, entity.blockPosition(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1, 1);
                }
            }


            if (gameTime % (tickEvalInterval * 3) == 0){
                ServerPlayer player = (ServerPlayer) entity;

                for (int slot = 0; slot < player.inventory.getContainerSize(); slot++) {
                    ItemStack otherStack = player.inventory.getItem(slot);
                    if (otherStack.getItem() == this && otherStack != stack) {
                        int otherTicksElapsed = getTicksElapsed(otherStack);
                        if (otherTicksElapsed < ticksElapsed) {
                            setTicksElapsed(otherStack, 0);
                        }
                    }
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

        int ticks = getTicksElapsed(stack);
        if (ticks < getTicksBetweenLoot(stack)) {
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

        setTicksElapsed(stack, 0);
        return InteractionResultHolder.success(stack);
    }

    public static int getTicksBetweenLoot(ItemStack stack) {
        RainbowColor tier = getTier(stack);
        return (int)(60 * 20 * Math.pow(0.8, tier.ordinal()));
    }

    public static RainbowColor getTier(ItemStack stack) {
        RainbowColor tier = RainbowColor.RED;

        if (stack.isEmpty() || stack.getItem() != ModItems.magicInABottle) {
            return tier;
        }

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            String tierStr = tag.getString("tier");
            try {
                tier = RainbowColor.valueOf(tierStr);
            } catch (IllegalArgumentException e) {}
        }

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
    public static float getFilledPercentage(ItemStack stack, ClientLevel world, LivingEntity entity) {
        // Used to register ItemProperty, used to render model based on filled %
        return Math.min(100f, (float)getTicksElapsed(stack) / getTicksBetweenLoot(stack));
    }
}
