package lonestarrr.arconia.common.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lonestarrr.arconia.common.item.ColoredBranch;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Commands for arconia
 */
public class ArconiaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        // Command structure:
        //   /arconia <subcommand> <subcommand args>
        dispatcher.register(
                Commands.literal("arconia")
                        .requires(source ->
                                source.hasPermission(Commands.LEVEL_ADMINS) || // op level 4 bypasses
                                        (source.isPlayer() && source.getPlayer() != null && source.getPlayer().gameMode.isCreative())
                        )
                        .then(
                        Commands.literal("imbue_branch").then(
                                Commands.argument("item_id", ItemArgument.item(context))
                                            .executes(ctx -> imbueBranch(
                                                            ctx,
                                                            ItemArgument.getItem(ctx, "item_id")
                                                    )
                                            )
                        )
                )
        );
    }

    private static int imbueBranch(
            CommandContext<CommandSourceStack> ctx, ItemInput itemInput) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        Item resourceItem = itemInput.getItem();

        // Enchant resourceItem in player's hand
        ItemStack branchItem = player.getInventory().getSelected();
        if (branchItem.isEmpty() || !(branchItem.getItem() instanceof ColoredBranch)) {
            player.sendSystemMessage(Component.literal("A colored branch is expected in your active hotbar slot for this to work"));
            return Command.SINGLE_SUCCESS;
        }

        ColoredBranch.setResourceItem(branchItem, new ItemStack(resourceItem));
        player.sendSystemMessage(Component.literal("Imbued the colored branch with resourceItem " + BuiltInRegistries.ITEM.getKey(resourceItem).toString()));
        return Command.SINGLE_SUCCESS;
    }
}