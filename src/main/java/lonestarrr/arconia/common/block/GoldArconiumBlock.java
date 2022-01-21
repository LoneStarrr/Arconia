package lonestarrr.arconia.common.block;

import lonestarrr.arconia.common.block.tile.GoldArconiumTileEntity;
import lonestarrr.arconia.common.block.tile.HatTileEntity;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.LanguageHelper;
import lonestarrr.arconia.compat.theoneprobe.TOPDriver;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProgressStyle;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.styles.ProgressStyle;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class GoldArconiumBlock extends Block implements IBlockColor, TOPDriver {
    private final RainbowColor tier;
    private final IProgressStyle progressStyleTOP = IProgressStyle.createDefault().backgroundColor(Color.BLACK).filledColor(Color.decode("#f9bd23")).alternateFilledColor(Color.decode("#f9bd23")).borderColor(Color.decode("#636161")).showText(false);

    public GoldArconiumBlock(RainbowColor tier) {
        super(Block.Properties.create(Material.IRON).setRequiresTool().hardnessAndResistance(5.0f, 6.0f).sound(SoundType.METAL));
        this.tier = tier;
    }

    public RainbowColor getTier() {
        return tier;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new GoldArconiumTileEntity(tier, false);
    }

    @Override
    public int getColor(
            BlockState blockState, @Nullable IBlockDisplayReader iBlockDisplayReader, @Nullable BlockPos blockPos, int tintIndex) {
        // Colors are not dependent on tint index, but on rainbow tier (though may use tintIndex later for less saturated versions)
        return tier.getColorValue();
    }

    @Override
    public void addProbeInfo(
            ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        // The One Probe extra block information
        TileEntity te = world.getTileEntity(data.getPos());
        if (te == null || !(te instanceof GoldArconiumTileEntity)) {
            return;
        }
        GoldArconiumTileEntity goldTE = (GoldArconiumTileEntity)te;
        int pct = goldTE.coinsLeftAsPercentage();
        probeInfo.progress(pct, 100, progressStyleTOP);
    }
}