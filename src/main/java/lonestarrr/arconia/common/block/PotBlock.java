package lonestarrr.arconia.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

public class PotBlock extends Block {
    public PotBlock() {
        super(Block.Properties.of(Material.METAL, MaterialColor.STONE).strength(2.0F).noOcclusion());
    }
}
