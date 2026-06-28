package lonestarrr.arconia.data.modonomicon.guide_book.progression;

import com.klikli_dev.modonomicon.api.datagen.CategoryProviderBase;
import com.klikli_dev.modonomicon.api.datagen.EntryBackground;
import com.klikli_dev.modonomicon.api.datagen.EntryProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookImagePageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookSpotlightPageModel;
import com.klikli_dev.modonomicon.api.datagen.book.page.BookTextPageModel;
import com.klikli_dev.modonomicon.client.gui.book.theme.GuiSprite;
import lonestarrr.arconia.common.core.helper.ResourceLocationHelper;
import lonestarrr.arconia.common.item.ModItems;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class ProgressIntro extends EntryProvider {

    public ProgressIntro(CategoryProviderBase parent) {
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
        this.pageTitle("Treasure");
        this.pageText(
                """
                        I have written about my encounters with a group of Leprechauns in my diary. 
                        In this section, I have carefully documented all of the rituals I witnessed them perform. 
                        These rituals will allow me to collect a great amount of treasure, 
                        once I have retraced my steps back into the civilized world. 
                        This will end my days of wandering in rags for good. Maybe I'll even share some of it 
                        with others ... in exchange for services and other niceties of course! 
                        """);
        this.page("page2", () ->
                BookImagePageModel.create()
                        .withImages(
                                ResourceLocationHelper.prefix("textures/gui/guide_book/potofgold_example_setup.png")
                        )
        );
    }

    @Override
    protected String entryName() {
        return "Infinite riches";
    }

    @Override
    protected GuiSprite entryBackground() {
        return EntryBackground.CATEGORY_START;
    }

    @Override
    protected BookIconModel entryIcon() {
        return BookIconModel.create(Items.DIAMOND);
    }

    @Override
    protected String entryId() {
        return "infinite_riches";
    }
}
