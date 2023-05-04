package com.example.pgyl.mind_a;

import android.graphics.Rect;
import android.graphics.RectF;

import com.example.pgyl.pekislib_a.DotMatrixDisplayView;
import com.example.pgyl.pekislib_a.DotMatrixFont;
import com.example.pgyl.pekislib_a.DotMatrixFontDefault;

import static com.example.pgyl.mind_a.PropRecord.MAX_SCORE_DISPLAY_SIZE;
import static com.example.pgyl.pekislib_a.DotMatrixFontUtils.getFontTextDimensions;
import static com.example.pgyl.pekislib_a.MiscUtils.BiDimensions;
import static com.example.pgyl.pekislib_a.PointRectUtils.ALIGN_LEFT_HEIGHT;

public class MainPropListItemDotMatrixDisplayUpdater {
    //region Variables
    private DotMatrixFont defaultFont;
    private Rect margins;
    private Rect gridRect;
    private Rect displayRect;
    private Rect labelDisplayRect;
    //endregion

    public MainPropListItemDotMatrixDisplayUpdater() {    //  Général pour tous les MainPropListItems
        super();

        init();
    }

    private void init() {
        setupDefaultFont();
        setupMargins();
    }

    public void close() {
        defaultFont.close();
        defaultFont = null;
    }

    public void displayScore(DotMatrixDisplayView dotMatrixDisplayView, PropRecord propRecord) {
        final String LABEL_ON_COLOR = "FF9A22";
        final String OFF_COLOR = "404040";

        dotMatrixDisplayView.fillRect(displayRect, LABEL_ON_COLOR, OFF_COLOR);    //  Pressed=ON  Unpressed=OFF
        dotMatrixDisplayView.setSymbolPos(labelDisplayRect.left + margins.left, labelDisplayRect.top + margins.top);
        dotMatrixDisplayView.writeText(propRecord.getStringScore(), LABEL_ON_COLOR, defaultFont);
        dotMatrixDisplayView.updateDisplay();
    }

    public void setupDimensions(DotMatrixDisplayView dotMatrixDisplayView) {       //  La grille (gridRect) contient le score
        final RectF INTERNAL_MARGIN_SIZE_COEFFS = new RectF(0, 0, 0, 0);   //  Marge autour de l'affichage proprement dit (% de largeur)
        int displayRectWidth;
        int displayRectHeight;

        BiDimensions fillerLabelTextDimensions = getFontTextDimensions(MAX_SCORE_DISPLAY_SIZE, defaultFont);

        displayRectWidth = margins.left + fillerLabelTextDimensions.width - defaultFont.getRightMargin() + margins.right;   //   Affichage sur la largeur du label maximum; margins.right remplace la dernière marge droite
        displayRectHeight = margins.top + fillerLabelTextDimensions.height + margins.bottom;

        gridRect = new Rect(0, 0, displayRectWidth, displayRectHeight);
        displayRect = new Rect(gridRect.left, gridRect.top, gridRect.width(), gridRect.height());  //  Affichage au début de la grille
        labelDisplayRect = displayRect;

        dotMatrixDisplayView.setInternalMarginCoeffs(INTERNAL_MARGIN_SIZE_COEFFS);
        dotMatrixDisplayView.setExternalMarginCoeffs(ALIGN_LEFT_HEIGHT);
        dotMatrixDisplayView.setGridRect(gridRect);
        dotMatrixDisplayView.setDisplayRect(displayRect);
    }

    public void setupBackColor(DotMatrixDisplayView dotMatrixDisplayView) {
        final String BACK_COLOR = "000000";

        dotMatrixDisplayView.setBackColor(BACK_COLOR);
    }

    private void setupDefaultFont() {
        defaultFont = new DotMatrixFontDefault();
    }

    private void setupMargins() {    // Marges autour de l'affichage proprement dit
        final int MARGIN_LEFT = 1;
        final int MARGIN_RIGHT = 1;
        final int MARGIN_TOP = 1;
        final int MARGIN_BOTTOM = 1;

        margins = new Rect(MARGIN_LEFT, MARGIN_TOP, MARGIN_RIGHT, MARGIN_BOTTOM);
    }

}