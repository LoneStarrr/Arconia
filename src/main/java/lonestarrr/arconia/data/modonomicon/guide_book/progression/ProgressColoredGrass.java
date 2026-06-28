package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import net.minecraft.util.Util;
import net.minecraft.world.item.crafting.Ingredient;

public class ProgressColoredGrass extends EntryProvider {

  public ProgressColoredGrass(CategoryProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generatePages() {
    this.page(
        "page1",
        () ->
            BookTextPageModel.create()
                .withTitle(this.context().pageTitle())
                .withText(this.context().pageText()));
    this.pageTitle("Colored Grass");
    this.pageText(
        """
                        The colorful branches can be used to turn regular grass into a colored version that matches
                        the color of the branch. A nifty little trick that we will need later!
                        Changing the color of grass this way will however consume the branch,
                        so let's gather a good amount of these.
                        """);
    this.page(
        "page2",
        () ->
            BookSpotlightPageModel.create()
                .withItem(Ingredient.of(ModBlocks.getRainbowGrassBlock(RainbowColor.RED))));
  }

  @Override
  protected String entryName() {
    return "Colored Grass";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModBlocks.getRainbowGrassBlock(RainbowColor.RED).asItem());
  }

  @Override
  protected String entryId() {
    return "progress_colored_grass";
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    var advancementCondition =
        this.condition().advancement(ResourceLocationHelper.prefix("red_tree_branch"));
    this.lang()
        .add(
            Util.makeDescriptionId("advancement", advancementCondition.getAdvancementId())
                + ".title",
            "Find a red tree branch");

    return entry.withCondition(advancementCondition);
  }
}
