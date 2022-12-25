package lonestarrr.arconia.common.block.entities;

import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.handler.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gold Arconium blocks are used as a gold source for the pot of gold multiblock. This entity tracks the available gold before it is depleted, and manages
 * gold coin collection interval / counts.
 */
public class GoldArconiumBlockEntity extends BaseBlockEntity {
    public static final String TAG_COINS = "coins"; //nbt tag
    public static final String TAG_INFINITE = "infinite"; //nbt tag
    public static final String TAG_COINS_TO_SEND = "coinsToSend";
    public static final String TAG_COIN_SEND_INTERVAL = "coinSendInterval";
    public static final String TAG_INITIAL_COIN_CAPACITY = "initialCoinCapacity";

    private RainbowColor tier;
    private long coins; // number of coins left to produce, if storage is not infinite
    private long initialCoinCapacity;
    private long coinsToSend; // number of coins to send per 'pot tick'
    private int coinSendInterval; // Send coins at an interval of this many 'pot ticks'
    private boolean infinite; // If set, internal coin store is never depleted and unlimited coins are generated

    public GoldArconiumBlockEntity(RainbowColor tier, BlockPos pos, BlockState state) {
        super(ModBlockEntities.getInfiniteGoldArconiumBlockEntityType(tier), pos, state);
        this.tier = tier;
        this.infinite = ConfigHandler.COMMON.goldArconiumIsInfinite.get(tier).get();
        this.coinsToSend = ConfigHandler.COMMON.goldArconiumCoinCounts.get(tier).get();
        this.initialCoinCapacity = ConfigHandler.COMMON.goldArconiumCoinCapacity.get(tier).get(); // if finite
        this.coinSendInterval = ConfigHandler.COMMON.goldArconiumCoinInterval.get(tier).get();
        this.coins = initialCoinCapacity;
    }

    public final RainbowColor getTier() {
        return this.tier;
    }

    public boolean isInfinite() {
        return this.infinite;
    }

    /**
     * @return Whether the internal gold store has been depleted
     */
    public boolean isDepleted() {
        return !this.infinite && this.coins == 0;
    }

    /**
     * @return How many coins' worth the gold store has left to collect
     */
    public long coinsLeft() {
        return coins;
    }

    public int coinsLeftAsPercentage() {
        return this.infinite? 100: (int)(coins * 100 / initialCoinCapacity);
    }

    /**
     * @return How frequently coins can be collected - interval length determined by collector (pot of gold)
     */
    public int getCoinGenerationInterval() {
        return this.coinSendInterval;
    }

    /**
     * @return The number of coins collected. This depletes an internal 'gold store' if the block is configured to have a limited supply.
     */
    public int collectCoins() {
        long coinCount = coinsToSend;

        if (!infinite) {
            coinCount = coinsToSend > coins ? coins : coinsToSend;
            this.coins -= coinCount;
            setChanged();
        }
        return (int)coinCount;
    }

    @Override
    public void writePacketNBT(CompoundTag tag) {
        tag.putLong(TAG_COINS, this.coins);
        tag.putBoolean(TAG_INFINITE, this.infinite);
        tag.putInt(TAG_COIN_SEND_INTERVAL, this.coinSendInterval);
        tag.putLong(TAG_INITIAL_COIN_CAPACITY, this.initialCoinCapacity);
        tag.putLong(TAG_COINS_TO_SEND, this.coinsToSend);
    }

    @Override
    public void readPacketNBT(CompoundTag tag) {
        this.coins = tag.getLong(TAG_COINS);
        this.infinite = tag.getBoolean(TAG_INFINITE);
        this.coinsToSend = tag.getLong(TAG_COINS_TO_SEND);
        this.initialCoinCapacity = tag.getLong(TAG_INITIAL_COIN_CAPACITY);
        this.coinSendInterval = tag.getInt(TAG_COIN_SEND_INTERVAL);
    }
}