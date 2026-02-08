package lonestarrr.arconia.data.loot;

import com.mojang.datafixers.kinds.Const;
import lonestarrr.arconia.common.block.ArconiumTreeLeaves;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ArconiaBlockLootSubProvider extends BlockLootSubProvider {
    private static final float[] COLORED_STICK_CHANCES = new float[]{0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};
    private static final LootItemCondition.Builder HAS_CLOVER_STAFF = MatchTool.toolMatches(ItemPredicate.Builder.item().of(ModItems.cloverStaff.value()));

    public ArconiaBlockLootSubProvider(HolderLookup.Provider registries) {
        // The first parameter is a set of blocks we are creating loot tables for. Instead of hardcoding,
        // we use our block registry and just pass an empty set here.
        // The second parameter is the feature flag set, this will be the default flags
        // unless you are adding custom flags (which is beyond the scope of this article).
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }
    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        // Add all of this mod's blocks here.
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(e -> (Block)e.value()).toList();
    }

    @Override
    protected void generate() {
        // See VanillaBlockLoot.java as an example. This will fail if not all known blocks from our mod are covered.
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);


        // No drops
        this.add(ModBlocks.potMultiBlockPrimary.value(), noDrop());
        this.add(ModBlocks.potMultiBlockSecondary.value(), noDrop());

        // Basic blocks
        this.dropSelf(ModBlocks.pedestal.value());
        this.dropSelf(ModBlocks.centerPedestal.value());
        this.dropSelf(ModBlocks.hat.value());
        this.dropSelf(ModBlocks.worldBuilder.value());

        // Colored blocks
        for (RainbowColor color: RainbowColor.values()) {
            // Basic self droppers
            this.dropSelf(ModBlocks.getArconiumTreeSapling(color).value());
            this.dropSelf(ModBlocks.getArconiumBlock(color).value());

            this.add(ModBlocks.getRainbowGrassBlock(color).value(), b -> this.createSingleItemTableWithSilkTouch(b, Blocks.DIRT));

            this.add(ModBlocks.getArconiumTreeLeaves(color).value(), this::createArconiumLeavesDrops);
        }

        // Clover, which gets four leaf clover drops when hit with a staff
        this.add(ModBlocks.clover.value(), b ->
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                                        .when(HAS_CLOVER_STAFF)
                                        .add(
                                                LootItem.lootTableItem(ModItems.fourLeafClover)
                                                    .when(BonusLevelTableCondition.bonusLevelFlatChance(registrylookup.getOrThrow(Enchantments.FORTUNE), 0.3F, 0.5F, 0.65F, 0.8F))
                                                    .otherwise(LootItem.lootTableItem(ModItems.threeLeafClover))
                                        )
                        ).withPool(
                                LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                                        .when(HAS_CLOVER_STAFF.invert())
                                        .add(
                                                LootItem.lootTableItem(ModItems.fourLeafClover)
                                                        .when(LootItemRandomChanceCondition.randomChance(0.15F))
                                                        .otherwise(LootItem.lootTableItem(ModItems.threeLeafClover))
                                        )
                        )
        );
    }

    private LootTable.Builder createArconiumLeavesDrops(Block pLeaves) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);

        ArconiumTreeLeaves leaves = (ArconiumTreeLeaves) pLeaves;
        RainbowColor nextTier = leaves.getTier().getNextTier();
        if (nextTier == null) nextTier = RainbowColor.PURPLE; // Maybe add some cool super special drop when mining the last tier tree?

        //  Arconium leaves have a chance to drop leaves of the next tier's tree, IF mined with an appropriately colored sickle
        return this.createLeavesDrops(leaves, ModBlocks.getArconiumTreeSapling(leaves.getTier()).value(), NORMAL_LEAVES_SAPLING_CHANCES)
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(hasSickle((leaves.getTier())))
                                .add(
                                        ((LootPoolSingletonContainer.Builder)this.applyExplosionCondition(pLeaves, LootItem.lootTableItem(ModBlocks.getArconiumTreeSapling(nextTier).asItem())))
                                                .when(BonusLevelTableCondition.bonusLevelFlatChance(registrylookup.getOrThrow(Enchantments.FORTUNE), NORMAL_LEAVES_SAPLING_CHANCES))
                                )
                )
                .withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(HAS_SHEARS.or(this.hasSilkTouch()).invert())
                                .add(
                                        ((LootPoolSingletonContainer.Builder)this.applyExplosionCondition(pLeaves, LootItem.lootTableItem(ModItems.getColoredRoot(leaves.getTier()))))
                                                .when(BonusLevelTableCondition.bonusLevelFlatChance(registrylookup.getOrThrow(Enchantments.FORTUNE), COLORED_STICK_CHANCES))
                                )
                );
    }

    private LootItemCondition.Builder hasSickle(RainbowColor color) {
        return MatchTool.toolMatches(ItemPredicate.Builder.item().of(ModItems.getArconiumSickle(color)));
    }
}
