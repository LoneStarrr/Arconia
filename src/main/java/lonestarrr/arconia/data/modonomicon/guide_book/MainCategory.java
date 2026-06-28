// SPDX-FileCopyrightText: 2024 klikli-dev
//
// SPDX-License-Identifier: MIT

package lonestarrr.arconia.data.modonomicon.guide_book;

import com.klikli_dev.modonomicon.api.datagen.CategoryProvider;
import com.klikli_dev.modonomicon.api.datagen.SingleBookSubProvider;
import com.klikli_dev.modonomicon.api.datagen.book.BookCategoryModel;
import com.klikli_dev.modonomicon.api.datagen.book.BookIconModel;
import lonestarrr.arconia.common.item.ModItems;
import lonestarrr.arconia.data.modonomicon.guide_book.diary.*;
import lonestarrr.arconia.data.modonomicon.guide_book.progression.*;

public class MainCategory extends CategoryProvider {
    public static final String ID = "main";

    public MainCategory(SingleBookSubProvider parent) {
        super(parent);
    }

    @Override
    protected String[] generateEntryMap() {
        // The entry map allows to define where entries are in relation to each other.
        // It is recommended to use a single character per entry
        // (if you think you are running out of characters .. any unicode character works.)
        // Alternatively the format used for multiblock below can be used.
        return new String[]{
                "_____________________",
                "__d_1_2_3_4_5_6______",
                "_________________G___",
                "____c_s_ℙ___ⓟ___Y_B__",
                "_______________O___I_",
                "__p_____ṕ_T_e_R_____V",
                "_______________$_____",
                "____t_g_______S______",
        };
    }

    @Override
    protected void generateEntries() {
        // Diary
        var diaryIntro = this.add(new DiaryIntro(this).generate('d'));
        var chapter1 =
                this.add(
                                new DiaryChapter1(this)
                                        .generate('1')
                                        .withCondition(this.condition().entryRead(diaryIntro)))
                        .withParent(this.parent(diaryIntro).withLineEnabled(true));
        var chapter2 =
                this.add(
                                new DiaryChapter2(this)
                                        .generate('2')
                                        .withCondition(this.condition().entryRead(chapter1)))
                        .withParent(this.parent(chapter1).withLineEnabled(true));
        var chapter3 =
                this.add(
                                new DiaryChapter3(this)
                                        .generate('3')
                                        .withCondition(this.condition().entryRead(chapter2)))
                        .withParent(this.parent(chapter2).withLineEnabled(true));
        var chapter4 =
                this.add(
                                new DiaryChapter4(this)
                                        .generate('4')
                                        .withCondition(this.condition().entryRead(chapter3)))
                        .withParent(this.parent(chapter3).withLineEnabled(true));
        var chapter5 =
                this.add(
                                new DiaryChapter5(this)
                                        .generate('5')
                                        .withCondition(this.condition().entryRead(chapter4)))
                        .withParent(this.parent(chapter4).withLineEnabled(true));
        var chapter6 =
                this.add(
                                new DiaryChapter6(this)
                                        .generate('6')
                                        .withCondition(this.condition().entryRead(chapter5)))
                        .withParent(this.parent(chapter5).withLineEnabled(true));

        // Main mod progress. Combined into a single category because I find the tabs on the side of the book a little
        // obscure (only because I managed to somehow miss those a few times while playing Thaumcraft!)
        var progressIntro = this.add(new ProgressIntro(this).generate('p'));
        var clovers =
                this.add(
                                new ProgressClovers(this)
                                        .generate('c')
                        )
                        .withParent(this.parent(progressIntro).withLineReversed(true));
        var tree_branches =
                this.add(
                                new ProgressTreeBranches(this)
                                        .generate('t')
                        )
                        .withParent(this.parent(progressIntro).withLineReversed(true));
        var colored_grass =
                this.add(
                                new ProgressColoredGrass(this)
                                        .generate('g')
                        )
                        .withParent(this.parent(tree_branches).withLineReversed(true));

        var clover_staff =
                this.add(
                                new ProgressStaff(this)
                                        .generate('s')
                        )
                        .withParent(this.parent(clovers).withLineReversed(true));

        var pedestals =
                this.add(
                                new ProgressPedestals(this)
                                        .generate('ℙ')
                        )
                        .withParent(this.parent(clover_staff).withLineReversed(true));

        var center_pedestal =
                this.add(
                                new ProgressCenterPedestal(this)
                                        .generate('ⓟ')
                        )
                        .withParent(this.parent(pedestals).withLineReversed(true));

        var pot_of_gold =
                this.add(
                                new ProgressPotOfGold(this)
                                        .generate('ṕ')
                        )
                        .withParent(this.parent(colored_grass).withLineReversed(true))
                        .withParent(this.parent(clover_staff).withLineReversed(true));
        var pot_of_gold_trees =
                this.add(
                                new ProgressPotOfGoldTrees(this)
                                        .generate('T')
                        )
                        .withParent(this.parent(pot_of_gold).withLineReversed(true));

        var arconium_essence =
                this.add(
                                new ProgressArconiumEssence(this)
                                        .generate('e')
                        )
                        .withParent(this.parent(pot_of_gold_trees).withLineReversed(true))
                        .withParent(this.parent(center_pedestal).withLineReversed(true));

        var red_arconium_ingot =
                this.add(
                                new ProgressRedArconiumIngot(this)
                                        .generate('R')
                        )
                        .withParent(this.parent(arconium_essence).withLineReversed(true));

        var red_arconium_sickle =
                this.add(
                                new ProgressRedSickle(this)
                                        .generate('S')
                        )
                        .withParent(this.parent(red_arconium_ingot).withLineReversed(true));

        var orange_sapling =
                this.add(
                                new ProgressOrangeSapling(this)
                                        .generate('$')
                        )
                        .withParent(this.parent(red_arconium_sickle).withLineReversed(true));

        var orange_arconium_ingot =
                this.add(
                                new ProgressOrangeArconiumIngot(this)
                                        .generate('O')
                        )
                        .withParent(this.parent(orange_sapling).withLineReversed(true));

        var yellow_arconium_ingot =
                this.add(
                                new ProgressYellowArconiumIngot(this)
                                        .generate('Y')
                        )
                        .withParent(this.parent(orange_arconium_ingot).withLineReversed(true));
        var green_arconium_ingot =
                this.add(
                                new ProgressGreenArconiumIngot(this)
                                        .generate('G')
                        )
                        .withParent(this.parent(yellow_arconium_ingot).withLineReversed(true));
        var light_blue_arconium_ingot =
                this.add(
                                new ProgressLightBlueArconiumIngot(this)
                                        .generate('B')
                        )
                        .withParent(this.parent(green_arconium_ingot).withLineReversed(true));
        var blue_arconium_ingot =
                this.add(
                                new ProgressBlueArconiumIngot(this)
                                        .generate('I')
                        )
                        .withParent(this.parent(light_blue_arconium_ingot).withLineReversed(true));
        var purple_arconium_ingot =
                this.add(
                                new ProgressPurpleArconiumIngot(this)
                                        .generate('V')
                        )
                        .withParent(this.parent(blue_arconium_ingot).withLineReversed(true));

    }

    @Override
    protected BookCategoryModel additionalSetup(BookCategoryModel category) {
        return category;
    }

    @Override
    protected String categoryName() {
        return "Main";
    }

    @Override
    protected BookIconModel categoryIcon() {
        return BookIconModel.create(ModItems.cloverStaff);
    }

    @Override
    public String categoryId() {
        return ID;
    }
}
