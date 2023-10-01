package lonestarrr.arconia.common.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lonestarrr.arconia.common.item.ColoredRoot;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Commands for arconia
 */
public class ArconiaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        // Command structure:
        //   /arconia <subcommand> <subcommand args>
        dispatcher.register(
                Commands.literal("arconia").then(
                        Commands.literal("enchant_root").then(
                                Commands.argument("item_id", ItemArgument.item(context)).then(
                                        Commands.argument("item_count", IntegerArgumentType.integer(1, 8))
                                                .executes(ctx -> enchantRoot(
                                                                ctx,
                                                                ItemArgument.getItem(ctx, "item_id"),
                                                                IntegerArgumentType.getInteger(ctx, "item_count")
                                                        )
                                                )
                                )
                        )
                )
        );
    }

    private static int enchantRoot(
            CommandContext<CommandSourceStack> ctx, ItemInput itemInput, int itemCount) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        Item resourceItem = itemInput.getItem();

        // Enchant resourceItem in player's hand
        ItemStack rootItem = player.getInventory().getSelected();
        if (rootItem.isEmpty() || !(rootItem.getItem() instanceof ColoredRoot)) {
            player.sendSystemMessage(Component.literal("A colored root is expected in your active hotbar slot for this to work"));
            return Command.SINGLE_SUCCESS;
        }

        ColoredRoot.setResourceItem(rootItem, resourceItem, itemCount);
        player.sendSystemMessage(Component.literal("Enchanted the colored root with resourceItem " + ForgeRegistries.ITEMS.getKey(resourceItem).toString()));
        return Command.SINGLE_SUCCESS;
    }
}