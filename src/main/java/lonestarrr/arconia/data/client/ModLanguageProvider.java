package lonestarrr.arconia.data.client;

import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * Data gen some default en_US translations, mostly for rainbow-color blocks and items
 */
public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(DataGenerator gen, String locale) {
        super(gen, Arconia.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        registerRainbowBlocks();
        registerRainbowItems();
        registerModBlocks();
        registerModItems();
        registerAdvancements();
        registerMisc();
    }

    private void registerRainbowBlocks() {
        // Also register items for anything that has both a block and item representation
        for (RainbowColor color : RainbowColor.values()) {
            String colorName = color.getUnlocalizedName();
            add(ModBlocks.getArconiumBlock(color).get(), colorName + " Arconium Block");
            add(ModBlocks.getInfiniteGoldArconiumBlock(color).get(), colorName + " Infinite Gold Arconium Block");
            add(ModBlocks.getArconiumTreeLeaves(color).get(), colorName + " Arconium Tree Leaves");
            add(ModBlocks.getArconiumTreeSapling(color).get(), colorName + " Arconium Tree Sapling");
        }
    }

    private void registerRainbowItems() {
        for (RainbowColor color : RainbowColor.values()) {
            String colorName = color.getUnlocalizedName();
            add(ModItems.getArconiumEssence(color).get(), colorName + " Arconium Essence");
            add(ModItems.getArconiumIngot(color).get(), colorName + " Arconium Ingot");
            add(ModItems.getColoredRoot(color).get(), colorName + " Tree Root");
            add(ModItems.getArconiumSickle(color).get(), colorName + " Arconium Sickle");
        }
    }

    private void registerModBlocks() {
        add(ModBlocks.clover.get(), "Clover Plant");
        add(ModBlocks.hat.get(), "Leprechaun Hat");
        add(ModBlocks.worldBuilder.get(), "World Builder");
        add(ModBlocks.pedestal.get(), "Pedestal");
        add(ModBlocks.centerPedestal.get(), "Center Pedestal");
    }

    private void registerModItems() {
        add(ModItems.threeLeafClover.get(), "Three Leaf Clover");
        add(ModItems.fourLeafClover.get(), "Four Leaf Clover");
        add(ModItems.goldCoin.get(), "Gold Coin");
        add(ModItems.cloverStaff.get(), "Clover Staff");
        add(ModItems.magicInABottle.get(), "Magic In A Bottle");
        add(ModItems.magicInABottle.get().getDescriptionId() + ".tooltip", "The Magic Luck-o-Meter indicates it's at %d%%");
    }

    private void registerAdvancements() {
        Stream.of(new String[][] {
                { "advancement.arconia.main.root.title", "Arconia" },
                { "advancement.arconia.main.root.desc", "Dig up dirt and find some memoirs" },
                { "advancement.arconia.main.four_leaf_clover.title", "Get lucky" },
                { "advancement.arconia.main.four_leaf_clover.desc", "Find a four-leaf clover" },
                { "advancement.arconia.main.red_tree_root.title", "Arconium Tree Root" },
                { "advancement.arconia.main.red_tree_root.desc", "Harvest arconium trees for some roots" },
                { "advancement.arconia.main.clover_staff.title", "You're a wizard now" },
                { "advancement.arconia.main.clover_staff.desc", "Make a clover staff" },
                { "advancement.arconia.main.red_arconium_sickle.title", "Leaf breaker 9000" },
                { "advancement.arconia.main.red_arconium_sickle.desc", "Craft a red arconium sickle" },
                { "advancement.arconia.main.pedestal.title", "All mods need these" },
                { "advancement.arconia.main.pedestal.desc", "Craft some pedestals" },
                { "advancement.arconia.main.center_pedestal.title", "Let the rituals begin" },
                { "advancement.arconia.main.center_pedestal.desc", "Craft a center pedestal to perform rituals" },
                { "advancement.arconia.main.red_root_of_essence.title", "Arconium?" },
                { "advancement.arconia.main.red_root_of_essence.desc", "Imbue magical properties onto a tree root using the altar" },
                { "advancement.arconia.main.pot_of_gold.title", "This has pot-ential" },
                { "advancement.arconia.main.pot_of_gold.desc", "Build a pot of gold" },
                { "advancement.arconia.main.hat.title", "I've hat it with these puns" },
                { "advancement.arconia.main.hat.desc", "Craft a leprechaun hat" },
                { "advancement.arconia.main.red_arconium_essence.title", "Arconium Essence" },
                { "advancement.arconia.main.red_arconium_essence.desc", "Get your first red arconium essence" },
                { "advancement.arconia.main.red_arconium_ingot.title", "Arconium Ingots" },
                { "advancement.arconia.main.red_arconium_ingot.desc", "Craft your first arconium ingot" },
                { "advancement.arconia.main.red_infinite_gold_arconium_block.title", "Arconium Gold" },
                { "advancement.arconia.main.red_infinite_gold_arconium_block.desc", "Get your first arconium infused gold" },
                { "advancement.arconia.main.orange_arconium_tree_sapling.title", "Orange you glad" },
                { "advancement.arconia.main.orange_arconium_tree_sapling.desc", "Harvest your first orange arconium sapling" },
                { "advancement.arconia.main.orange_arconium_ingot.title", "Not so juicy" },
                { "advancement.arconia.main.orange_arconium_ingot.desc", "Orange arconium ingots!" },
                { "advancement.arconia.main.yellow_arconium_ingot.title", "Nautical Nonsense" },
                { "advancement.arconia.main.yellow_arconium_ingot.desc", "Yellow arconium ingots!" },
                { "advancement.arconia.main.green_arconium_ingot.title", "It's not easy being green" },
                { "advancement.arconia.main.green_arconium_ingot.desc", "Green arconium ingots!" },
                { "advancement.arconia.main.light_blue_arconium_ingot.title", "You smurfed another one!" },
                { "advancement.arconia.main.light_blue_arconium_ingot.desc", "Light blue arconium ingots!" },
                { "advancement.arconia.main.blue_arconium_ingot.title", "Da Ba Dee" },
                { "advancement.arconia.main.blue_arconium_ingot.desc", "Blue arconium ingots!" },
                { "advancement.arconia.main.purple_arconium_ingot.title", "ROYGBIV" },
                { "advancement.arconia.main.purple_arconium_ingot.desc", "Obtain the last, purple arconium ingot!" },
        }).forEach(adv -> add(adv[0], adv[1]));
    }

    private void registerMisc() {
        add("jei.arconia.recipe_category.altar", "Pedestal Ritual");
        // clover staff messages
        add("arconia.item.cloverstaff.linkhat.invalidpot", "The selected pot of gold appears to be invalid or missing");
        add("arconia.item.cloverstaff.linkhat.linked", "Linked hat");
        add("arconia.item.cloverstaff.linkhat.toofar", "The hat is too far away to link");
        add("arconia.item.cloverstaff.linkhat.notfound", "Invalid hat");
        add("arconia.item.cloverstaff.linkhat.toomanyhats", "Too many hats have been linked already");
        add("arconia.item.cloverstaff.linkhat.alreadylinked", "The hat's already linked");
        add("arconia.item.cloverstaff.linkhat.linked_other_pot", "The hat's already linked to another pot");
        add("arconia.item.cloverstaff.linkhat.unlinked", "Unlinked hat");
        add("arconia.item.cloverstaff.linkhat.unlink_failed", "Hat was not linked");
        add("arconia.item.cloverstaff.selectpot.success", "Stored coordinate of the pot of gold at %s");
        add("arconia.item.cloverstaff.selectpot.failed", "Invalid pot of gold multiblock structure?");
        // hat messages
        add("arconia.block.hat.not_linked_to_pot", "The hat is not linked to a pot of gold yet");
        add("arconia.block.hat.resource_already_set", "The hat is already configured with a resource. Crouch with an empty hand to clear the undo this first");
        add("arconia.block.hat.resource_tier_too_high", "The linked pot of gold does not have a high enough tier for this resource");
        add("arconia.block.hat.resource_empty", "The root is not configured with a resource to generate");
        add("arconia.block.hat.resource_set", "The hat is now setup to receive %s items");
        add("arconia.block.hat.resource_unset", "The hat will now no longer receive resources");
        add("arconia.block.hat.info_resource_empty", "The hat is not configured to receive resources");
        add("arconia.block.hat.info_resource", "The hat is configured to receive %s items, for tier: ");
        add("arconia.block.hat.info_linked", "The hat is linked to a pot of gold at %s");
        add("arconia.block.hat.info_unlinked", "The hat is not linked to a pot of gold");

        // pedestal ritual messages
        add("arconia.block.center_pedestal.ritual_start_failed", "Failed to start ritual. Perhaps some items are missing?");
        // Pot multiblock
        add("block.arconia.pot_multiblock_secondary", "Pot of Gold");
        add("block.arconia.pot_multiblock_primary", "Pot of Gold");
        add("arconia.block.pot_multiblock.coin_count.none", "There are no coins");
        add("arconia.block.pot_multiblock.coin_count.few", "There are a few coins");
        add("arconia.block.pot_multiblock.coin_count.tens", "There are tens of coins");
        add("arconia.block.pot_multiblock.coin_count.hundreds", "There are hundreds of coins");
        add("arconia.block.pot_multiblock.coin_count.thousands", "There are thousands of coins");
        add("arconia.block.pot_multiblock.coin_count.ludicrous", "There is an insane amount of coins");
        add("arconia.block.pot_multiblock.coin_count.absolute", "The pot contains %s gold coins");
        // world builder
        add("arconia.block.world_builder.in_progress", "The world builder is a little busy right now, please try again later");
        add("arconia.block.world_builder.start_build", "Starting the builder");
        add("arconia.block.world_builder.no_blocks_found", "The builder didn't find any blocks to convert");

        // Guide book
        add("arconia.guide_book.landing_text", "A collection of notes on my discovery of the secrets to the Leprechaun's wealth, and how to help myself to some of that.");
        add("arconia.guide_book.name", "Arconia Notebook");
    }
}
