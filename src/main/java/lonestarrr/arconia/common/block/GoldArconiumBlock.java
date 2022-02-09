package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.tile.GoldArconiumTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.compat.theoneprobe.TOPDriver;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.styles.StyleManager;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nullable;

public class GoldArconiumBlock extends BaseEntityBlock implements BlockColor, TOPDriver {
    private final RainbowColor tier;
    private final IProgressStyle progressStyleTOP = (new StyleManager()).progressStyleDefault().backgroundColor(Color.rgb(0, 0, 0))
            .filledColor(Color.rgb(249, 189, 35))
            .alternateFilledColor(Color.rgb(249, 189, 35))
            .borderColor(Color.rgb(99, 97, 97)).showText(false);

    public GoldArconiumBlock(RainbowColor tier) {
        super(Block.Properties.of(Material.METAL).requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.METAL));
        this.tier = tier;
    }

    public RainbowColor getTier() {
        return tier;
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable BlockAndTintGetter iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }

    @Override
    public void addProbeInfo(
            ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
        // The One Probe extra block information
        BlockEntity te = world.getBlockEntity(data.getPos());
        if (te == null || !(te instanceof GoldArconiumTileEntity)) {
            return;
        }
        GoldArconiumTileEntity goldTE = (GoldArconiumTileEntity)te;
        int pct = goldTE.coinsLeftAsPercentage();
        probeInfo.progress(pct, 100, progressStyleTOP);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GoldArconiumTileEntity(tier, false, pos, state);
    }
}