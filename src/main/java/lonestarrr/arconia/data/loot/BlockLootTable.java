package lonestarrr.arconia.data.loot;

import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashSet;
import java.util.Set;

public class BlockLootTable extends BlockLoot {
    private final Set<Block> knownBlocks = new HashSet<>();

    @Override
    protected void add(Block p_124166_, LootTable.Builder p_124167_) {
        super.add(p_124166_, p_124167_);
        // Required to do this or else datagen will complain about missing blocks
        knownBlocks.add(p_124166_);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return knownBlocks;
    }

    @Override
    protected void addTables() {
        this.dropSelf(ModBlocks.centerPedestal);
        this.dropSelf(ModBlocks.pedestal);
        this.dropSelf(ModBlocks.hat); // TODO probably should not destroy the enchanted root!
        this.dropSelf(ModBlocks.orb);
        this.dropSelf(ModBlocks.pot);
        RainbowColor.stream().forEach(color -> this.dropSelf(ModBlocks.getArconiumBlock(color)));
        RainbowColor.stream().forEach(color -> this.dropSelf(ModBlocks.getArconiumTreeRootBlocks(color)));
        RainbowColor.stream().forEach(color -> this.dropSelf(ModBlocks.getArconiumTreeSapling(color)));
        // TODO the other blocks still in json-only form
    }

}
