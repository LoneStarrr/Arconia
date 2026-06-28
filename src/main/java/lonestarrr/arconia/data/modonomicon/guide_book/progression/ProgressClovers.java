package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

public class ProgressClovers extends EntryProvider {

  public ProgressClovers(CategoryProviderBase parent) {
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
    this.pageTitle("Clovers");
    this.pageText(
        """
        While observing the leprechauns, I noticed they were obsessed with four leaf clovers, and for good reason.
        While punching clovers found in the wild with my fists, I would occasionally get one. Hitting them
        with a staff crafted using a four leaf clover however is more rewarding. Even more so if the staff is further
        blessed with good fortune.
        """);
    this.page(
            "page2",
            () ->
                    BookSpotlightPageModel.create()
                            .withItem(Ingredient.of(ModItems.fourLeafClover)));

  }

  @Override
  protected String entryName() {
    return "Clovers";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModItems.fourLeafClover);
  }

  @Override
  protected String entryId() {
    return "progress_clovers";
  }
}
