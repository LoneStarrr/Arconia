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

public class ProgressArconiumEssence extends EntryProvider {

  public ProgressArconiumEssence(CategoryProviderBase parent) {
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
    this.pageTitle("Arconium Essence");
    this.pageText(
        """
        Now that I have my pot of gold setup and rituals ready to go, let's extract our first piece of treasure: 
        arconium essence!
        I observed in detail one of the Leprechauns performing a ritual to imbue a red tree branch with this magical 
        essence. Once imbued, he used it on the pot of gold, which then displayed an apparition of the item above it.
        Soon after, that very same item popped out of the pot as if created out of thin air! With a chest placed
        nearby, the item was captured automatically.
        """);
    this.page(
            "page2",
            () ->
                    BookSpotlightPageModel.create()
                            .withItem(Ingredient.of(ModItems.getArconiumEssence(RainbowColor.RED))));

    this.page(
            "page3",
            () ->
                    BookTextPageModel.create()
                            .withText(this.context().pageText()));
    this.pageText(
        """
        I wrote down the exact ingredients needed to perform the ritual, and they can be found by looking up the
        recipe for red arconium essence.
        There are more items the red branch can be imbued with once we have our essence in hand. To find
        out which, look up recipes using the red branch. I can even lookup any and all rituals by looking up
        recipes using the center pedestal!
        """);
  }

  @Override
  protected String entryName() {
    return "Arconium Essence";
  }

  @Override
  protected GuiSprite entryBackground() {
    return EntryBackground.DEFAULT;
  }

  @Override
  protected BookIconModel entryIcon() {
    return BookIconModel.create(ModItems.getArconiumEssence(RainbowColor.RED));
  }

  @Override
  protected String entryId() {
    return "progress_arconium_essence";
  }

  @Override
  protected BookEntryModel additionalSetup(BookEntryModel entry) {
    var advancementCondition = this.condition().advancement(ResourceLocationHelper.prefix("pot_of_gold"));
    var advancementCondition2 = this.condition().advancement(ResourceLocationHelper.prefix("center_pedestal"));
    return entry.withCondition(advancementCondition).withCondition(advancementCondition2);
  }

}
