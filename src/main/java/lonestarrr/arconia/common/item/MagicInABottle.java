package lonestarrr.arconia.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.components.ModDataComponents;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Time in a bottle? What's that? Never heard of that. No, this is *magic* in a bottle. It generates a random resource from a loot table at a slow rate.
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
//            RainbowColor newTier = RainbowColor.byTier((tierNum >= RainbowColor.values().length - 1 ? 0: tierNum + 1));
            RainbowColor newTier = currentTier.getNextTier();
            if (newTier == null) {
                newTier = RainbowColor.RED; // Cycle around
            }
            setTier(itemStack, newTier);
            player.displayClientMessage(Component.literal("Taste the " + newTier.getTierName() + " rainbow!"), true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(
            @NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> toolTips, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, toolTips, flag);
        long ticksElapsed = getTicksElapsed(stack);
        int ticksBetweenLoot = getTicksBetweenLoot(stack);
        int pct = (int)Math.min(100, (int)(ticksElapsed * 100d / ticksBetweenLoot));
        toolTips.add(Component.translatable(stack.getDescriptionId() + ".tooltip", pct).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
    }

    public static long getTicksElapsed(ItemStack stack) {
        return getData(stack).ticksElapsed;
    }

    public static void setTicksElapsed(ItemStack stack, long ticks) {
        MagicInABottleData data = getData(stack);
        data = new MagicInABottleData(data.tier(), ticks);
        stack.set(ModDataComponents.MAGIC_IN_A_BOTTLE_DATA, data);
    }

    private static @Nonnull  MagicInABottleData getData(@Nonnull ItemStack stack) {
        MagicInABottleData data = stack.get(ModDataComponents.MAGIC_IN_A_BOTTLE_DATA);
        if (data == null) {
            return new MagicInABottleData(RainbowColor.RED.getTier(), 0);
        }
        return data;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotIn, boolean selected) {
        if (world.isClientSide() || !(entity instanceof ServerPlayer)) {
            return;
        }
        final int tickEvalInterval = 20;

        long gameTime = world.getGameTime();

        if (gameTime % tickEvalInterval == 0) {
            long ticksElapsed = getTicksElapsed(stack);
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

                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    ItemStack otherStack = player.getInventory().getItem(slot);
                    if (otherStack.getItem() == this && otherStack != stack) {
                        long otherTicksElapsed = getTicksElapsed(otherStack);
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

        long ticks = getTicksElapsed(stack);
        if (ticks < getTicksBetweenLoot(stack)) {
            return InteractionResultHolder.fail(stack);
        }

        List<ItemStack> lootCollection = getLoot(stack, world);
        BlockPos spawnPos = playerEntity.blockPosition();
        spawnPos.offset((int)playerEntity.getLookAngle().x, (int)playerEntity.getLookAngle().y, (int)playerEntity.getLookAngle().z); // somewhat in front of player?
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

    public static @Nonnull RainbowColor getTier(ItemStack stack) {
        RainbowColor tier = RainbowColor.byTier(getData(stack).tier());
        if (tier == null) {
            return RainbowColor.RED;
        }
        return tier;
    }

    private void setTier(ItemStack stack, RainbowColor tier) {
        MagicInABottleData data = getData(stack);
        // TODO I really think this should be done with multiple data components. E.g. what is this thing gained more
        // fields, it would be awkward to update it like this.
        stack.set(ModDataComponents.MAGIC_IN_A_BOTTLE_DATA, new MagicInABottleData(tier.getTier(), data.ticksElapsed()));
    }

    protected List<ItemStack> getLoot(ItemStack stack, Level world) {
        RainbowColor tier = getTier(stack);

        final ResourceLocation lootResource = new ResourceLocation(Arconia.MOD_ID, "magic_in_a_bottle_" + tier.getTierName());

        LootTable lootTable = ((ServerLevel) world).getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootResource));
        LootParams params = (new LootParams.Builder((ServerLevel)world)).create(LootContextParamSets.EMPTY);
        return lootTable.getRandomItems(params);
    }

    @OnlyIn(Dist.CLIENT)
    public static float getFilledPercentage(ItemStack stack, ClientLevel world, LivingEntity entity, int seed) {
        // Used to register ItemProperty, used to render model based on filled %
        return Math.min(100f, (float)getTicksElapsed(stack) / getTicksBetweenLoot(stack));
    }

    public record MagicInABottleData(int tier, long ticksElapsed) {
        public static final Codec<MagicInABottleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("tier").forGetter(MagicInABottleData::tier),
                Codec.LONG.fieldOf("ticksElapsed").forGetter(MagicInABottleData::ticksElapsed)
        ).apply(instance, MagicInABottleData::new));

        public static final StreamCodec<ByteBuf, MagicInABottleData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, MagicInABottleData::tier,
                ByteBufCodecs.VAR_LONG, MagicInABottleData::ticksElapsed,
                MagicInABottleData::new
        );
    }
}
