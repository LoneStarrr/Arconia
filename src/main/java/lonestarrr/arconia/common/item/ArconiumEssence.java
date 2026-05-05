package lonestarrr.arconia.common.item;

import lonestarrr.arconia.common.advancements.ModCriteriaTriggers;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.block.RainbowGrassBlock;
import lonestarrr.arconia.common.core.RainbowColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ArconiumEssence extends Item {
    private RainbowColor tier;

    public ArconiumEssence(Item.Properties builder, RainbowColor tier) {
        super(builder);
        this.tier = tier;
    }
}
