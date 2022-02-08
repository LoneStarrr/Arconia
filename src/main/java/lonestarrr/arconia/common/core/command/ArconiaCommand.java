package lonestarrr.arconia.common.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import lonestarrr.arconia.common.Arconia;
import lonestarrr.arconia.common.item.ColoredRoot;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.World;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Commands for arconia
 */
public class ArconiaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Command structure:
        //   /arconia <subcommand> <subcommand args>
        dispatcher.register(
                Commands.literal("arconia").then(
                        Commands.literal("enchant_root").then(
                                Commands.argument("item_id", ItemArgument.item()).then(
                                        Commands.argument("item_count", IntegerArgumentType.integer(1, 8)).then(
                                                Commands.argument("generation_interval", IntegerArgumentType.integer(1, 100)).then(
                                                        Commands.argument("coin_cost", IntegerArgumentType.integer(1, 512))
                                                            .executes(ctx -> enchantRoot(
                                                                    ctx,
                                                                    ItemArgument.getItem(ctx, "item_id"),
                                                                    IntegerArgumentType.getInteger(ctx, "item_count"),
                                                                    IntegerArgumentType.getInteger(ctx, "generation_interval"),
                                                                    IntegerArgumentType.getInteger(ctx, "coin_cost")
                                                            ))
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int enchantRoot(
            CommandContext<CommandSourceStack> ctx, ItemInput itemInput, int itemCount, int generationInterval, int coinCost) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        Item resourceItem = itemInput.getItem();

        // Enchant resourceItem in player's hand
        ItemStack rootItem = player.inventory.getSelected();
        if (rootItem.isEmpty() || !(rootItem.getItem() instanceof ColoredRoot)) {
            player.sendMessage(new TextComponent("A colored root is expected in your active hotbar slot for this to work"), Util.NIL_UUID);
            return Command.SINGLE_SUCCESS;
        }

        ColoredRoot.setResourceItem(rootItem, resourceItem, generationInterval, itemCount, coinCost);
        player.sendMessage(new TextComponent("Enchanted the colored root with resourceItem " + resourceItem.getRegistryName()), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }
}