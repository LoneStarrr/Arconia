package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookEntryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.core.RainbowColor;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.world.item.crafting.Ingredient;

public class ProgressGreenArconiumIngot extends EntryProvider {

  public ProgressGreenArconiumIngot(CategoryProviderBase parent) {
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
    this.pageTitle("Green arconium ingot");
    this.pageText(
        """
        I know the drill by now. Sickle, sapling, essence, new ingot!!
        """);
    this.page(
            "page2",
            () ->
                    BookSpotlightPageModel.create()
                            .withItem(Ingredient.of(ModItems.getArconiumIngot(RainbowColor.GREEN))));

  }

  @Override
  protected String entryName() {
    return "Green arconium ingot";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModItems.getArconiumIngot(RainbowColor.GREEN));
  }

  @Override
  protected String entryId() {
    return "progress_green_arconium_ingot";
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    var advancementCondition = this.condition().advancement(ResourceLocationHelper.prefix("yellow_arconium_ingot"));
    return entry.withCondition(advancementCondition);
  }

}
