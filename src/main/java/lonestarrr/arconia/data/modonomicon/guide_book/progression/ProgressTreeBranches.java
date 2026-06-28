package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.world.item.crafting.Ingredient;

public class ProgressTreeBranches extends EntryProvider {

  public ProgressTreeBranches(CategoryProviderBase parent) {
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
    this.pageTitle("Tree Branches");
    this.pageText(
        """
        I'm going to have to look for red trees with bright sparkling leaves growing in forested areas.
        When chopping down these trees, the occasional branch falls out. I'll need a lot of these to perform
        the next steps towards a life of peaceful luxury.
        """);
    this.page(
        "page2",
        () ->
            BookSpotlightPageModel.create()
                .withItem(Ingredient.of(ModItems.getColoredBranch(RainbowColor.RED))));
  }

  @Override
  protected String entryName() {
    return "Tree Branches";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModItems.getColoredBranch(RainbowColor.RED));
  }

  @Override
  protected String entryId() {
    return "progress_tree_branches";
  }
}
