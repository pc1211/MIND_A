package com.example.pgyl.mind_a;

import android.graphics.Rect;
import android.graphics.RectF;

import com.example.pgyl.pekislib_a.ButtonColorBox;
import com.example.pgyl.pekislib_a.DotMatrixDisplayView;
import com.example.pgyl.pekislib_a.DotMatrixFont;
import com.example.pgyl.pekislib_a.DotMatrixFontDefault;

import static com.example.pgyl.pekislib_a.ButtonColorBox.COLOR_TYPES;
import static com.example.pgyl.pekislib_a.DotMatrixFontUtils.getFontTextDimensions;
import static com.example.pgyl.pekislib_a.MiscUtils.BiDimensions;
import static com.example.pgyl.pekislib_a.PointRectUtils.ALIGN_LEFT_HEIGHT;

public class DotMatrixDisplayUpdater {
    //region Variables
    private DotMatrixDisplayView dotMatrixDisplayView;
    private DotMatrixFont defaultFont;
    private Rect margins;
    private Rect displayRect;
    //endregion

    public DotMatrixDisplayUpdater(DotMatrixDisplayView dotMatrixDisplayView) {
        super();

        this.dotMatrixDisplayView = dotMatrixDisplayView;
        init();
    }

    private void init() {
        setupDefaultFont();
        setupBackColor();
        setupMargins();
        setupDimensions();
    }

    public void close() {
        dotMatrixDisplayView = null;
        defaultFont.close();
        defaultFont = null;
    }

    public void displayScore(PropRecord propRecord) {
        final String UNPRESSED_FRONT_COLOR_FOR_BLACKS = "000000";
        final String UNPRESSED_FRONT_COLOR_FOR_WHITES = "FFFFFF";
        final String UNPRESSED_BACK_COLOR = "808080";
        final String PRESSED_BACK_COLOR = "FF9A22";

        ButtonColorBox colorBox = dotMatrixDisplayView.getColorBox();

        colorBox.setColor(COLOR_TYPES.UNPRESSED_BACK_COLOR, UNPRESSED_BACK_COLOR);
        colorBox.setColor(COLOR_TYPES.PRESSED_BACK_COLOR, PRESSED_BACK_COLOR);
        dotMatrixDisplayView.drawBackRect(displayRect);
        dotMatrixDisplayView.setSymbolPos(displayRect.left + margins.left, displayRect.top + margins.top);

        colorBox.setColor(COLOR_TYPES.UNPRESSED_FRONT_COLOR, UNPRESSED_FRONT_COLOR_FOR_BLACKS);
        colorBox.setColor(COLOR_TYPES.PRESSED_FRONT_COLOR, UNPRESSED_FRONT_COLOR_FOR_BLACKS);
        String text = String.valueOf(propRecord.getScoreBlacks()) + " ";
        dotMatrixDisplayView.drawFrontText(text, null, defaultFont);

        colorBox.setColor(COLOR_TYPES.UNPRESSED_FRONT_COLOR, UNPRESSED_FRONT_COLOR_FOR_WHITES);
        text = String.valueOf(propRecord.getScoreWhites());
        dotMatrixDisplayView.drawFrontText(text, null, defaultFont);

        dotMatrixDisplayView.updateDisplay();
    }

    public void setupBackColor() {
        final String BACK_COLOR = "000000";

        dotMatrixDisplayView.setBackColor(BACK_COLOR);
    }

    public void setupDefaultFont() {
        defaultFont = new DotMatrixFontDefault();
    }

    public void setupMargins() {    // Marges (en nombre de carrés autour de l'affichage proprement dit)
        margins = new Rect(1, 1, 1, 1);
    }

    private void setupDimensions() {
        final RectF INTERNAL_MARGIN_SIZE_COEFFS = new RectF(0, 0, 0, 0);   //  Marge autour de l'affichage proprement dit (% de largeur)
        final String MAX_SIZE_SCORE_DISPLAY_TEXT = "9 9";

        BiDimensions textDimensions = getFontTextDimensions(MAX_SIZE_SCORE_DISPLAY_TEXT, null, defaultFont);

        int displayRectWidth = margins.left + textDimensions.width - defaultFont.getRightMargin() + margins.right;   //   margins.right remplace la dernière marge droite
        int displayRectHeight = margins.top + textDimensions.height + margins.bottom;

        Rect gridRect = new Rect(0, 0, displayRectWidth, displayRectHeight);
        displayRect = new Rect(gridRect.left, gridRect.top, displayRectWidth, displayRectHeight);  //  Affichage au début de la grille

        dotMatrixDisplayView.setInternalMarginCoeffs(INTERNAL_MARGIN_SIZE_COEFFS);
        dotMatrixDisplayView.setExternalMarginCoeffs(ALIGN_LEFT_HEIGHT);
        dotMatrixDisplayView.setGridRect(gridRect);
        dotMatrixDisplayView.setDisplayRect(displayRect);
    }
}