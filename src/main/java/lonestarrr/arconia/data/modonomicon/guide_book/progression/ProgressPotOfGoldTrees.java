package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.block.ModBlocks;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import net.minecraft.world.item.Items;

public class ProgressPotOfGoldTrees extends EntryProvider {

  public ProgressPotOfGoldTrees(CategoryProviderBase parent) {
    super(parent);
  }

  @Override
  protected void generatePages() {
    this.page(
            "page1",
            () ->
                    BookTextPageModel.create()
                            .withText(this.context().pageText()))
                            .withTitle(this.context.pageTitle());
    this.pageTitle("Tree Setup");
    this.pageText(
            """
            Great! Now that I have successfully built the pot, I'll setup the rest so I can get filthy rich.
            As a first step, I need something to store all the loot I'll be ehm.. borrowing. 
            A chest will do, placed close to the pot. 
            Next, use an imbued tree branch (remember from my earlier instructions?) on the pot.
            With that done, I should see an item appear over the pot. This is the item the pot will
            extract from the treasure pool.
            """);
    this.page("page2", () ->
            BookImagePageModel.create()
                    .withImages(
                            ResourceLocationHelper.prefix("textures/gui/guide_book/potofgold_trees_positions.png")
                    )
    );
    this.page(
            "page3",
            () ->
                    BookTextPageModel.create()
                            .withText(this.context().pageText()));
    this.pageText(
            """
            The pot will only extract treasure if certain trees are placed nearby at very specific locations.
            The red trees I have obtained the branches from earlier will work for now.
            I've included a drawing that shows all 8 possible locations in a 15 by 15 area around the pot where
            the trees can be planted. Each location is a 3 by 3 area with red colored grass (see my previous notes)
            on the outsides and regular grass or dirt in the center. The base of the pot itself needs to be on the
            same Y level as the base of the trees, or one block higher or lower.
            """);
    this.page("page4", () ->
                    BookImagePageModel.create()
                            .withImages(
                                    ResourceLocationHelper.prefix("textures/gui/guide_book/potofgold_trees_1_red.png")
                            )
    );
    this.page(
            "page5",
            () ->
                    BookTextPageModel.create()
                            .withText(this.context().pageText()));
    this.pageText(
            """
            I only need to plant a single tree to get started, but I could plant more if I want to. Once a fully
            grown tree is in the right place, the pot of gold will be activated and start consuming its leaves.
            If that does not happen, look at the pot. It will indicate if something's missing before it will work.
            A chest icon indicates it couldn't find a place nearby to store the loot. A leaf block indicates it could
            not find a tree at any of the expected locations (remember to surround it with the red grass!). And of
            course it needs to be activated with one of the imbued branches to produce anything.
            """);
  }

  @Override
  protected String entryName() {
    return "Pot of Gold Tree Setup";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModBlocks.getArconiumTreeSapling(RainbowColor.RED).asItem());
  }

  @Override
  protected String entryId() {
    return "progress_pot_of_gold_trees";
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    var advancementCondition = this.condition().advancement(ResourceLocationHelper.prefix("pot_of_gold"));
    return entry.withCondition(advancementCondition);
    // TODO remove advancement titles in the book? Already datagenned elsewhere
  }

}
