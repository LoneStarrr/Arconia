package lonestarrr.arconia.data.modonomicon;

import com.klikli_dev.modonomicon.api.datagen.MultiblockProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;

public class MultiBlockProvider extends MultiblockProvider {
  public MultiBlockProvider(PackOutput packOutput, String modid) {
    super(packOutput, modid);
  }

  @Override
  public void buildMultiblocks() {
    // looks like there must always be a '0' to indicate center of structure which is what
    // visualization
    // centers on when placing.
    // Layers are top to bottom
    this.add(
        this.modLoc("pot_of_gold"),
        new DenseMultiblockBuilder()
            .layer("CCC", "CGC", "CCC")
            .layer("CCC", "C0C", "CCC")
            .block('C', () -> Blocks.CAULDRON)
            .block('0', () -> Blocks.CAULDRON)
            .block('G', () -> Blocks.GOLD_BLOCK));
  }
}
