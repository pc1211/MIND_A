package com.example.pgyl.mind_a;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.pgyl.pekislib_a.ColorPickerActivity;
import com.example.pgyl.pekislib_a.CustomImageButton;
import com.example.pgyl.pekislib_a.DotMatrixDisplayView;
import com.example.pgyl.pekislib_a.HelpActivity;
import com.example.pgyl.pekislib_a.InputButtonsActivity;
import com.example.pgyl.pekislib_a.StringDB;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.pgyl.mind_a.Constants.MAX_COLORS;
import static com.example.pgyl.mind_a.Constants.MAX_PEGS;
import static com.example.pgyl.mind_a.Constants.MIND_ACTIVITIES;
import static com.example.pgyl.mind_a.Constants.MIND_ACTIVITIES_REQUEST_CODE_MULTIPLIER;
import static com.example.pgyl.mind_a.StringDBTables.getInputParamsColorsIndex;
import static com.example.pgyl.mind_a.StringDBTables.getInputParamsPegsIndex;
import static com.example.pgyl.mind_a.StringDBTables.getInputParamsScoreIndex;
import static com.example.pgyl.mind_a.StringDBTables.getInputParamsTableName;
import static com.example.pgyl.mind_a.StringDBTables.getPaletteColorsAtIndex;
import static com.example.pgyl.mind_a.StringDBTables.getPaletteColorsTableName;
import static com.example.pgyl.mind_a.StringDBTables.getPropsTableName;
import static com.example.pgyl.mind_a.StringDBUtils.createMindTableIfNotExists;
import static com.example.pgyl.mind_a.StringDBUtils.initializeTableInputParams;
import static com.example.pgyl.mind_a.StringDBUtils.initializeTablePaletteColors;
import static com.example.pgyl.pekislib_a.ColorUtils.ButtonColorBox;
import static com.example.pgyl.pekislib_a.Constants.ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;
import static com.example.pgyl.pekislib_a.Constants.PEKISLIB_ACTIVITIES;
import static com.example.pgyl.pekislib_a.Constants.SHP_FILE_NAME_SUFFIX;
import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_TITLE;
import static com.example.pgyl.pekislib_a.MiscUtils.msgBox;
import static com.example.pgyl.pekislib_a.StringDBTables.ACTIVITY_START_STATUS;
import static com.example.pgyl.pekislib_a.StringDBTables.TABLE_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.StringDBTables.getActivityInfosTableName;
import static com.example.pgyl.pekislib_a.StringDBUtils.createPekislibTableIfNotExists;
import static com.example.pgyl.pekislib_a.StringDBUtils.createPresetWithDefaultValues;
import static com.example.pgyl.pekislib_a.StringDBUtils.getCurrentFromActivity;
import static com.example.pgyl.pekislib_a.StringDBUtils.getCurrentsFromActivity;
import static com.example.pgyl.pekislib_a.StringDBUtils.getDefaults;
import static com.example.pgyl.pekislib_a.StringDBUtils.getLabels;
import static com.example.pgyl.pekislib_a.StringDBUtils.setCurrentForActivity;
import static com.example.pgyl.pekislib_a.StringDBUtils.setCurrentsForActivity;
import static com.example.pgyl.pekislib_a.StringDBUtils.setStartStatusOfActivity;

//  MainActivity fait appel à PropRecordShandler pour la gestion des PropRecord (création, suppression, tri, ...)
//  MainPropListUpdater maintient la liste de MainActivity (rafraîchissement, scrollbar, ...), fait appel à MainPropListAdapter (pour gérer chaque item) et également à PropRecordShandler (pour leur mise à jour)
//  MainPropListItemAdapter reçoit ses items (PropRecord) de la part de MainPropListUpdater et gère chaque item de la liste

public class MainActivity extends Activity {
    public static int pegs;   //  Static pour que PropRecordsHandler, PropRecord, CandsRecordHandler, CandRecord, ScoreActivity, ... puissent y accéder automatiquement en cas de changement de pegs ou colors
    public static int colors;

    //region Constantes
    private enum COMMANDS {
        CLEAR("Clear"), DELETE_LAST("Delete Last"), NEW_GAME("New Game"), CHEAT("Cheat");

        private String label;

        COMMANDS(String label) {
            this.label = label;
        }

        public String LABEL() {
            return label;
        }

        public int INDEX() {
            return ordinal();
        }
    }

    private enum GUESS_MODES {
        USER("User guesses"), ANDROID("Android guesses");

        private String label;

        GUESS_MODES(String label) {
            this.label = label;
        }

        public String LABEL() {
            return label;
        }

        public int INDEX() {
            return ordinal();
        }
    }

    private enum COLOR_OBJECTS {PALETTE, CURRENT_PROP, ITEM_PROP, NONE}

    private enum COLOR_MODES {NORMAL, INVERSE}

    public enum MIND_SHP_KEY_NAMES {KEEP_SCREEN, GUESS_MODE, INPUT_PARAMS_INDEX}

    public final int DISK_PNG_ID = R.drawable.disk;
    //endregion

    //region Variables
    private String[] inputParams;
    private int inputParamsIndex;
    private String[] paletteColors;
    private CustomImageButton[] commandButtons;
    private CustomImageButton[] paletteButtons;
    private CustomImageButton[] currentPropPegButtons;
    private DotMatrixDisplayView currentPropDotMatrixDisplayScore;
    private DotMatrixDisplayUpdater dotMatrixDisplayUpdater;
    private RadioButton[] guessModeRadios;
    private COLOR_OBJECTS colorObject;
    private int colorObjectPegIndex;
    private int colorObjectListPosition;
    private ButtonColorBox buttonColorBox;
    private GUESS_MODES guessMode;
    private Menu menu;
    private MenuItem barMenuItemKeepScreen;
    private CandRecordsHandler candRecordsHandler;
    private PropRecordsHandler propRecordsHandler;
    private PropRecord currentPropRecord;
    private PropRecord secrPropRecord;
    private MainPropListUpdater mainPropListUpdater;
    private boolean keepScreen;
    private ListView mainPropListView;
    private MainPropListItemAdapter mainPropListItemAdapter;
    private StringDB stringDB;
    private String shpFileName;
    private boolean validReturnFromCalledActivity;
    private String calledActivityName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String ACTIVITY_TITLE = "MasterMind";

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().setTitle(ACTIVITY_TITLE);
        setContentView(R.layout.main);
        validReturnFromCalledActivity = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        setCurrentsForActivity(stringDB, MIND_ACTIVITIES.MAIN.toString(), getInputParamsTableName(), inputParams);
        setCurrentsForActivity(stringDB, MIND_ACTIVITIES.MAIN.toString(), getPaletteColorsTableName(), paletteColors);
        candRecordsHandler.close();
        candRecordsHandler = null;
        mainPropListUpdater.close();
        mainPropListUpdater = null;
        mainPropListItemAdapter.close();
        mainPropListItemAdapter = null;
        propRecordsHandler.saveAndClose();
        propRecordsHandler = null;
        dotMatrixDisplayUpdater.close();
        dotMatrixDisplayUpdater = null;
        stringDB.close();
        stringDB = null;
        inputParams = null;
        paletteColors = null;
        buttonColorBox = null;
        menu = null;
        savePreferences();
    }
    //endregion

    @Override
    protected void onResume() {
        super.onResume();

        setupPaletteButtons();
        setupCurrentPropPegButtons();
        setupCommandButtons();
        setupGuessModeRadioButtons();
        setupTextViews();
        setupDotMatrixDisplay();   //  Ces setup... ont été déplacés du onCreate au onResume pour éviter crash intermittent

        shpFileName = getPackageName() + "." + getClass().getSimpleName() + SHP_FILE_NAME_SUFFIX;
        keepScreen = getSHPKeepScreen();
        guessMode = getSHPGuessMode();
        inputParamsIndex = getSHPInputParamsIndex();
        colorObject = COLOR_OBJECTS.NONE;
        setupStringDB();
        inputParams = getCurrentsFromActivity(stringDB, MIND_ACTIVITIES.MAIN.toString(), getInputParamsTableName());
        paletteColors = getCurrentsFromActivity(stringDB, MIND_ACTIVITIES.MAIN.toString(), getPaletteColorsTableName());
        pegs = Integer.parseInt(inputParams[getInputParamsPegsIndex()]);
        colors = Integer.parseInt(inputParams[getInputParamsColorsIndex()]);
        buttonColorBox = new ButtonColorBox();
        setupPropRecords();   //  Charger à partir de la DB
        setupCandRecords();

        String newParamValue = null;
        if (validReturnFromCalledActivity) {
            validReturnFromCalledActivity = false;
            if (calledActivityName.equals(PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString())) {   //  Pour Entrée de pegs ou colors
                newParamValue = getCurrentFromActivity(stringDB, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), getInputParamsTableName(), inputParamsIndex);
            }
            if (calledActivityName.equals(MIND_ACTIVITIES.SCORE.toString())) {   //  Pour Entrée du score si Android Guess
                newParamValue = getCurrentFromActivity(stringDB, MIND_ACTIVITIES.SCORE.toString(), getInputParamsTableName(), inputParamsIndex);
            }
            if (calledActivityName.equals(PEKISLIB_ACTIVITIES.COLOR_PICKER.toString())) {   //  Pour Edition de la palette de couleurs
                paletteColors = getCurrentsFromActivity(stringDB, PEKISLIB_ACTIVITIES.COLOR_PICKER.toString(), getPaletteColorsTableName());
            }
        }

        handleActivityReturn(newParamValue);
        setupMainPropList();
        setupMainPropListUpdater();
        setupDotMatrixDisplayUpdater();
        setupPaletteButtonsVisibility();
        setupCurrentPropPegButtonsVisibility();
        updateDisplayKeepScreen();
        updateDisplay();
        invalidateOptionsMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        validReturnFromCalledActivity = false;
        if (requestCode == PEKISLIB_ACTIVITIES.INPUT_BUTTONS.INDEX()) {   //  Pour entrer le nombre de pions ou de couleurs
            calledActivityName = PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString();
            if (resultCode == RESULT_OK) {
                validReturnFromCalledActivity = true;
            }
        }
        if (requestCode == PEKISLIB_ACTIVITIES.COLOR_PICKER.INDEX()) {   //  Pour éditer la palette de couleurs
            calledActivityName = PEKISLIB_ACTIVITIES.COLOR_PICKER.toString();
            if (resultCode == RESULT_OK) {
                validReturnFromCalledActivity = true;
            }
        }
        if (requestCode == (MIND_ACTIVITIES_REQUEST_CODE_MULTIPLIER * MIND_ACTIVITIES.SCORE.INDEX())) {   //  Pour entrer le score  (si Android guess)
            calledActivityName = MIND_ACTIVITIES.SCORE.toString();
            if (resultCode == RESULT_OK) {
                validReturnFromCalledActivity = true;
            }
        }
    }

    private void handleActivityReturn(String sValue) {
        if (sValue != null) {
            int value = Integer.parseInt(sValue);
            if (inputParamsIndex == getInputParamsPegsIndex()) {
                inputParams[inputParamsIndex] = sValue;
                pegs = value;
                resetPropsAndCands();
            }
            if (inputParamsIndex == getInputParamsColorsIndex()) {
                inputParams[inputParamsIndex] = sValue;
                colors = value;
                resetPropsAndCands();
            }
            if (inputParamsIndex == getInputParamsScoreIndex()) {
                int score = value;
                int blacks = score / 10;   //  Noirs
                int whites = score % 10;   //  Blancs
                if ((blacks <= pegs) && (whites <= pegs) && ((blacks + whites) <= pegs) && (score <= (10 * pegs)) && (score != (10 * (pegs - 1) + 1))) {
                    inputParams[inputParamsIndex] = sValue;
                    currentPropRecord.setScore(score);
                    PropRecord newPropRecord = propRecordsHandler.createPropRecordWithNewId();
                    newPropRecord.setComb(currentPropRecord.getComb());
                    newPropRecord.setScore(currentPropRecord.getScore());
                    propRecordsHandler.addPropRecord(newPropRecord);
                    int solIndex = candRecordsHandler.getSolutionCandRecordsIndex(currentPropRecord.getComb(), currentPropRecord.getScore());
                    if (solIndex == UNDEFINED) {   //  Encore plusieurs solutions possibles
                        currentPropRecord.setComb(candRecordsHandler.getGuessComb());
                    } else {   //  Trouvé !
                        newPropRecord = propRecordsHandler.createPropRecordWithNewId();
                        newPropRecord.setComb(candRecordsHandler.getCombAtIndex(solIndex));
                        newPropRecord.setScore(10 * pegs);
                        propRecordsHandler.addPropRecord(newPropRecord);
                        currentPropRecord.resetComb();
                    }
                    currentPropRecord.resetScore();
                } else {   //  Bad guy
                    msgBox("Invalid score: " + blacks + " black" + (blacks > 1 ? "s" : "") + " and " + whites + " white" + (whites > 1 ? "s" : ""), this);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {  //  Non appelé après changement d'orientation
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        setupBarMenuItems();
        updateDisplayKeepScreenBarMenuItemIcon(keepScreen);
        return true;
    }
    //endregion

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {  // appelé par invalideOptionsMenu après changement d'orientation
        updateDisplayKeepScreenBarMenuItemIcon(keepScreen);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.MENU_ITEM_PEGS) {
            inputParamsIndex = getInputParamsPegsIndex();
            launchInputButtonsActivity(inputParamsIndex);
            return true;
        }
        if (item.getItemId() == R.id.MENU_ITEM_COLORS) {
            inputParamsIndex = getInputParamsColorsIndex();
            launchInputButtonsActivity(inputParamsIndex);
            return true;
        }
        if (item.getItemId() == R.id.MENU_ITEM_EDIT_PALETTE) {
            launchColorPickerActivity();
            return true;
        }
        if (item.getItemId() == R.id.HELP) {
            launchHelpActivity();
            return true;
        }
        if (item.getItemId() == R.id.ABOUT) {
            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String version = pInfo.versionName;   //  Version Name
            int verCode = pInfo.versionCode;      //  Version Code

            msgBox("Version: " + version, this);
            return true;
        }
        if (item.getItemId() == R.id.BAR_MENU_ITEM_KEEP_SCREEN) {
            keepScreen = !keepScreen;
            updateDisplayKeepScreen();
            updateDisplayKeepScreenBarMenuItemIcon(keepScreen);
        }
        return super.onOptionsItemSelected(item);
    }

    private void onGuessModeRadioClick(GUESS_MODES guessMode) {
        GUESS_MODES oldGuessMode = this.guessMode;
        if (!guessMode.equals(oldGuessMode)) {
            guessModeRadios[1 - guessMode.INDEX()].setChecked(false);   //  L'autre bouton Radio
            confirm(guessMode.LABEL());
        }
    }

    private void onGuessModeRadioChange(GUESS_MODES guessMode) {   //  Appelé par confirm()
        this.guessMode = guessMode;
        resetPropsAndCands();
    }

    private void onPaletteButtonClick(int pegIndex) {
        if (guessMode.equals(GUESS_MODES.USER)) {
            selectColor(COLOR_OBJECTS.PALETTE, pegIndex, UNDEFINED);
        }
    }

    private void onCurrentPropPegButtonClick(int pegIndex) {
        if (guessMode.equals(GUESS_MODES.USER)) {
            selectColor(COLOR_OBJECTS.CURRENT_PROP, pegIndex, UNDEFINED);
        }
    }

    private void onCurrentPropDotMatrixDisplayScoreCustomClick() {
        if (guessMode.equals(GUESS_MODES.ANDROID)) {
            inputParamsIndex = getInputParamsScoreIndex();
            inputParams[inputParamsIndex] = String.valueOf(currentPropRecord.getScore());
            launchScoreActivity(inputParamsIndex);
        } else {   //  USER
            onButtonClickSubmit();
            colorObject = COLOR_OBJECTS.NONE;
            updateDisplay();
        }
    }

    private void onCommandButtonClick(COMMANDS command) {
        if (command.equals(COMMANDS.CLEAR)) {
            if (guessMode.equals(GUESS_MODES.USER)) {
                onButtonClickClear();
            }
            colorObject = COLOR_OBJECTS.NONE;
            updateDisplay();
        } else {   //  Del Last, New Game, Cheat: A confirmer Yes/No, avec updateDisplay() et colorObject NONE
            if (guessMode.equals(GUESS_MODES.USER)) {
                confirm(command.LABEL());
            } else {   //  Android
                if (!command.equals(COMMANDS.CHEAT)) {
                    confirm(command.LABEL());
                } else {   //  Cheat
                    colorObject = COLOR_OBJECTS.NONE;   //  Ne rien faire
                    updateDisplay();
                }
            }
        }
    }

    private void onButtonClickClear() {    //  Appelé par onCommandButtonClick()
        if (colorObject.equals(COLOR_OBJECTS.CURRENT_PROP)) {
            currentPropRecord.resetCombAtIndex(colorObjectPegIndex);
        }
        if (colorObject.equals(COLOR_OBJECTS.NONE)) {
            currentPropRecord.resetComb();
        }
        currentPropRecord.resetScore();
    }

    private void onButtonClickDeleteLast() {   //  Appelé par confirm()
        propRecordsHandler.removePropRecordAtMaxId();   //  Enlever le dernier PropRecord (cad avec le id le plus élevé)
        if (guessMode.equals(GUESS_MODES.ANDROID)) {
            candRecordsHandler.updateCandRecordsToPropRecords(propRecordsHandler);
            currentPropRecord.setComb(candRecordsHandler.getGuessComb());
            currentPropRecord.resetScore();
        }
    }

    private void onButtonClickNewGame() {
        resetPropsAndCands();
    }   //  Appelé par confirm()

    private void onButtonClickCheat() {   //  Appelé par confirm()
        currentPropRecord.setComb(secrPropRecord.getComb());
        currentPropRecord.setScore(10 * pegs);
    }

    private void onButtonClickSubmit() {   //  Appelé par onCurrentPropDotMatrixDisplayScoreCustomClick()
        if (currentPropRecord.hasValidComb()) {   //  Valide si aucune couleur UNDEFINED
            PropRecord newPropRecord = propRecordsHandler.createPropRecordWithNewId();
            propRecordsHandler.addPropRecord(newPropRecord);
            newPropRecord.setComb(currentPropRecord.getComb());
            newPropRecord.setScore(candRecordsHandler.getScoreByComparing(currentPropRecord.getComb(), secrPropRecord.getComb()));
            currentPropRecord.resetComb();
            currentPropRecord.resetScore();
        } else {
            msgBox("Invalid proposal", this);
        }
    }

    private void onItemPropClick(int position, int pegIndex) {
        if (guessMode.equals(GUESS_MODES.USER)) {
            selectColor(COLOR_OBJECTS.ITEM_PROP, pegIndex, position);
        }
    }

    private void updateDisplayGuessModeRadios() {
        guessModeRadios[guessMode.INDEX()].setChecked(true);
        guessModeRadios[1 - guessMode.INDEX()].setChecked(false);   //  L'autre bouton Radio
    }

    private void updateDisplayPaletteButtonColors() {
        for (int i = 0; i <= (colors - 1); i = i + 1) {
            updateDisplayPaletteButtonColor(i, COLOR_MODES.NORMAL);
        }
    }

    private void updateDisplayPaletteButtonColor(int index, COLOR_MODES colorMode) {
        final String BACK_COLOR_NORMAL = "000000";
        final String BACK_COLOR_INVERSE = "FFFFFF";

        String color = paletteColors[getPaletteColorsAtIndex(index)];
        buttonColorBox.unpressedFrontColor = color;
        buttonColorBox.unpressedBackColor = colorMode.equals(COLOR_MODES.NORMAL) ? BACK_COLOR_NORMAL : BACK_COLOR_INVERSE;
        buttonColorBox.pressedFrontColor = color;
        buttonColorBox.pressedBackColor = BACK_COLOR_NORMAL;
        paletteButtons[index].setColors(buttonColorBox);
    }

    private void updateDisplayCurrentPropButtonColors() {
        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            updateDisplayCurrentPropButtonColor(i, COLOR_MODES.NORMAL);
        }
    }

    private void updateDisplayCurrentPropButtonColor(int index, COLOR_MODES colorMode) {
        final String BACK_COLOR_NORMAL = "000000";
        final String BACK_COLOR_INVERSE = "FFFFFF";
        final String EMPTY_COLOR = "808080";

        String frontColor = EMPTY_COLOR;
        String backColor = EMPTY_COLOR;
        int colorIndex = currentPropRecord.getCombAtIndex(index);
        if (colorIndex != UNDEFINED) {
            frontColor = paletteColors[getPaletteColorsAtIndex(colorIndex)];
            backColor = BACK_COLOR_NORMAL;
        }
        buttonColorBox.unpressedFrontColor = frontColor;
        buttonColorBox.unpressedBackColor = colorMode.equals(COLOR_MODES.NORMAL) ? backColor : BACK_COLOR_INVERSE;
        buttonColorBox.pressedFrontColor = frontColor;
        buttonColorBox.pressedBackColor = colorMode.equals(COLOR_MODES.NORMAL) ? BACK_COLOR_INVERSE : backColor;
        currentPropPegButtons[index].setColors(buttonColorBox);
    }

    private void updateDisplayCurrentPropScore() {
        dotMatrixDisplayUpdater.displayText(currentPropRecord.getDecoratedScore());   //  "B-W" (En attente d'entrée du score)
    }

    private void updateDisplayCommandButtonColors() {
        final String ACTIVE_COLOR = "000000";
        final String INACTIVE_COLOR = "B0B0B0";
        final String COLOR_PRESSED = "FF9A22";

        buttonColorBox.unpressedFrontColor = ACTIVE_COLOR;
        buttonColorBox.unpressedBackColor = null;
        buttonColorBox.pressedFrontColor = ACTIVE_COLOR;
        buttonColorBox.pressedBackColor = COLOR_PRESSED;
        commandButtons[COMMANDS.DELETE_LAST.INDEX()].setColors(buttonColorBox);
        commandButtons[COMMANDS.NEW_GAME.INDEX()].setColors(buttonColorBox);

        buttonColorBox.unpressedFrontColor = guessMode.equals(GUESS_MODES.USER) ? ACTIVE_COLOR : INACTIVE_COLOR;
        buttonColorBox.pressedFrontColor = guessMode.equals(GUESS_MODES.USER) ? ACTIVE_COLOR : INACTIVE_COLOR;
        commandButtons[COMMANDS.CLEAR.INDEX()].setColors(buttonColorBox);
        commandButtons[COMMANDS.CHEAT.INDEX()].setColors(buttonColorBox);
    }

    private void updateDisplayItemPropButtonColor(int position, int pegIndex, COLOR_MODES colorMode) {
        final String BACK_COLOR_NORMAL = "000000";
        final String BACK_COLOR_INVERSE = "FFFFFF";

        PropRecord itemPropRecord = propRecordsHandler.getPropRecordAtIndex(position);
        int colorIndex = itemPropRecord.getCombAtIndex(pegIndex);
        String color = paletteColors[getPaletteColorsAtIndex(colorIndex)];
        buttonColorBox.unpressedFrontColor = color;
        buttonColorBox.unpressedBackColor = colorMode.equals(COLOR_MODES.NORMAL) ? BACK_COLOR_NORMAL : BACK_COLOR_INVERSE;
        buttonColorBox.pressedFrontColor = color;
        buttonColorBox.pressedBackColor = colorMode.equals(COLOR_MODES.NORMAL) ? BACK_COLOR_INVERSE : BACK_COLOR_NORMAL;
        mainPropListUpdater.repaintAtPosAtPegIndex(position, pegIndex, buttonColorBox);
    }

    private void updateDisplay() {
        updateDisplayGuessModeRadios();
        updateDisplayPaletteButtonColors();
        updateDisplayCurrentPropButtonColors();
        updateDisplayCurrentPropScore();
        updateDisplayCommandButtonColors();
        mainPropListUpdater.rebuild();
        mainPropListUpdater.repaint();
    }

    private void updateDisplayKeepScreenBarMenuItemIcon(boolean keepScreen) {
        barMenuItemKeepScreen.setIcon((keepScreen ? R.drawable.main_light_on : R.drawable.main_light_off));
    }

    private void updateDisplayKeepScreen() {
        if (keepScreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void resetPropsAndCands() {
        try {
            propRecordsHandler.clearPropRecords();                 //  Vider propRecords et nuller currentPropRecord et secrPropRecord
            propRecordsHandler.setupCurrentAndSecrPropRecords();   //  Reconstruire currentPropRecord et secrPropRecord
            currentPropRecord = propRecordsHandler.getCurrentPropRecord();
            secrPropRecord = propRecordsHandler.getSecrPropRecord();
            candRecordsHandler = new CandRecordsHandler();   // Reconstruire tous les candidats
            if (guessMode.equals(GUESS_MODES.ANDROID)) {
                currentPropRecord.setComb(candRecordsHandler.getGuessComb());
            }
        } catch (OutOfMemoryError e) {    // Pas assez de RAM
            candRecordsHandler.close();   //  Libérer ce qui fâche
            candRecordsHandler = null;
            propRecordsHandler.clearPropRecords();
            propRecordsHandler = null;
            msgBox("Out of Memory Error. Now Trying to go back to saved state", this);
            inputParams = getCurrentsFromActivity(stringDB, MIND_ACTIVITIES.MAIN.toString(), getInputParamsTableName());   //  Restaurer les anciennes valeurs de pegs et colors
            pegs = Integer.parseInt(inputParams[getInputParamsPegsIndex()]);
            colors = Integer.parseInt(inputParams[getInputParamsColorsIndex()]);
            setupPropRecords();   //  Restaurer à partir de la DB
            setupCandRecords();   //  Reconstruire les candidats
        }
    }

    private void confirm(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage("Are you sure ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int id) {
                if (title.equals(COMMANDS.DELETE_LAST.LABEL())) {
                    onButtonClickDeleteLast();
                }
                if (title.equals(COMMANDS.NEW_GAME.LABEL())) {
                    onButtonClickNewGame();
                }
                if (title.equals(COMMANDS.CHEAT.LABEL())) {
                    onButtonClickCheat();
                }
                if ((title.equals(GUESS_MODES.USER.LABEL()) || (title.equals(GUESS_MODES.ANDROID.LABEL())))) {
                    onGuessModeRadioChange(title.equals(GUESS_MODES.USER.LABEL()) ? GUESS_MODES.USER : GUESS_MODES.ANDROID);
                }
            }
        });
        builder.setNegativeButton("No", null);
        Dialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {   // OK pour modifier UI sous-jacente à la boîte de dialogue
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                colorObject = COLOR_OBJECTS.NONE;
                updateDisplay();
            }
        });
        dialog.show();
    }

    private void selectColor(COLOR_OBJECTS newColorObject, int newColorObjectPegIndex, int newColorObjectListPosition) {
        COLOR_OBJECTS oldColorObject = colorObject;
        int oldColorObjectPegIndex = colorObjectPegIndex;
        int oldColorObjectListPosition = colorObjectListPosition;
        colorObject = newColorObject;
        colorObjectPegIndex = newColorObjectPegIndex;
        colorObjectListPosition = newColorObjectListPosition;

        switch (oldColorObject) {
            case PALETTE:
                switch (newColorObject) {
                    case PALETTE:
                        if (newColorObjectPegIndex != oldColorObjectPegIndex) {
                            updateDisplayPaletteButtonColor(newColorObjectPegIndex, COLOR_MODES.INVERSE);
                        } else {   //  Même index
                            colorObject = COLOR_OBJECTS.NONE;
                        }
                        break;
                    case CURRENT_PROP:
                        currentPropRecord.setCombAtIndex(newColorObjectPegIndex, oldColorObjectPegIndex);   //  Les valeurs de PropRecord.comb sont des n° de couleur (cad leur index dans la palette de couleurs)
                        updateDisplayCurrentPropButtonColor(newColorObjectPegIndex, COLOR_MODES.NORMAL);
                        colorObject = COLOR_OBJECTS.NONE;
                        break;
                    case ITEM_PROP:
                        updateDisplayItemPropButtonColor(newColorObjectListPosition, newColorObjectPegIndex, COLOR_MODES.INVERSE);
                        break;
                }
                updateDisplayPaletteButtonColor(oldColorObjectPegIndex, COLOR_MODES.NORMAL);
                break;

            case CURRENT_PROP:
                switch (newColorObject) {
                    case PALETTE:
                        currentPropRecord.setCombAtIndex(oldColorObjectPegIndex, newColorObjectPegIndex);
                        updateDisplayPaletteButtonColor(newColorObjectPegIndex, COLOR_MODES.NORMAL);
                        break;
                    case CURRENT_PROP:
                        if (newColorObjectPegIndex != oldColorObjectPegIndex) {
                            currentPropRecord.setCombAtIndex(newColorObjectPegIndex, currentPropRecord.getCombAtIndex(oldColorObjectPegIndex));
                            updateDisplayCurrentPropButtonColor(newColorObjectPegIndex, COLOR_MODES.NORMAL);
                        }
                        break;
                    case ITEM_PROP:
                        PropRecord itemPropRecord = propRecordsHandler.getPropRecordAtIndex(newColorObjectListPosition);
                        currentPropRecord.setCombAtIndex(oldColorObjectPegIndex, itemPropRecord.getCombAtIndex(newColorObjectPegIndex));
                        break;
                }
                colorObject = COLOR_OBJECTS.NONE;
                updateDisplayCurrentPropButtonColor(oldColorObjectPegIndex, COLOR_MODES.NORMAL);
                break;

            case ITEM_PROP:
                switch (newColorObject) {
                    case PALETTE:
                        updateDisplayPaletteButtonColor(newColorObjectPegIndex, COLOR_MODES.INVERSE);
                        break;
                    case CURRENT_PROP:
                        PropRecord itemPropRecord = propRecordsHandler.getPropRecordAtIndex(oldColorObjectListPosition);
                        currentPropRecord.setCombAtIndex(newColorObjectPegIndex, itemPropRecord.getCombAtIndex(oldColorObjectPegIndex));
                        updateDisplayCurrentPropButtonColor(newColorObjectPegIndex, COLOR_MODES.NORMAL);
                        colorObject = COLOR_OBJECTS.NONE;
                        break;
                    case ITEM_PROP:
                        if (newColorObjectListPosition != oldColorObjectListPosition) {  //  Sur un itemProp différent
                            updateDisplayItemPropButtonColor(newColorObjectListPosition, newColorObjectPegIndex, COLOR_MODES.INVERSE);
                        } else {   //  Même itemProp
                            if (newColorObjectPegIndex != oldColorObjectPegIndex) {
                                updateDisplayItemPropButtonColor(newColorObjectListPosition, newColorObjectPegIndex, COLOR_MODES.INVERSE);
                            } else {   //  Même index
                                colorObject = COLOR_OBJECTS.NONE;
                            }
                        }
                        break;
                }
                updateDisplayItemPropButtonColor(oldColorObjectListPosition, oldColorObjectPegIndex, COLOR_MODES.NORMAL);
                break;

            case NONE:
                switch (newColorObject) {
                    case PALETTE:
                        updateDisplayPaletteButtonColor(newColorObjectPegIndex, COLOR_MODES.INVERSE);
                        break;
                    case CURRENT_PROP:
                        updateDisplayCurrentPropButtonColor(newColorObjectPegIndex, COLOR_MODES.INVERSE);
                        break;
                    case ITEM_PROP:
                        updateDisplayItemPropButtonColor(newColorObjectListPosition, newColorObjectPegIndex, COLOR_MODES.INVERSE);
                        break;
                }
                break;
        }
    }

    private void savePreferences() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        SharedPreferences.Editor shpEditor = shp.edit();
        shpEditor.putBoolean(MIND_SHP_KEY_NAMES.KEEP_SCREEN.toString(), keepScreen);
        shpEditor.putString(MIND_SHP_KEY_NAMES.GUESS_MODE.toString(), guessMode.toString());
        shpEditor.putInt(MIND_SHP_KEY_NAMES.INPUT_PARAMS_INDEX.toString(), inputParamsIndex);
        shpEditor.commit();
    }

    private boolean getSHPKeepScreen() {
        final boolean KEEP_SCREEN_DEFAULT_VALUE = false;

        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getBoolean(MIND_SHP_KEY_NAMES.KEEP_SCREEN.toString(), KEEP_SCREEN_DEFAULT_VALUE);
    }

    private GUESS_MODES getSHPGuessMode() {
        final String GUESS_MODE_DEFAULT_VALUE = GUESS_MODES.USER.toString();

        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return GUESS_MODES.valueOf(shp.getString(MIND_SHP_KEY_NAMES.GUESS_MODE.toString(), GUESS_MODE_DEFAULT_VALUE));
    }

    private int getSHPInputParamsIndex() {
        final int INPUT_PARAMS_INDEX_DEFAULT_VALUE = 0;

        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getInt(MIND_SHP_KEY_NAMES.INPUT_PARAMS_INDEX.toString(), INPUT_PARAMS_INDEX_DEFAULT_VALUE);
    }

    private void setupTextViews() {
        final String TXT_COLOR = "C0C0C0";

        TextView tg = findViewById(R.id.TXT_GUESS);
        TextView tcp = findViewById(R.id.TXT_CURRENT_PROP);
        tg.setTextColor(Color.parseColor(COLOR_PREFIX + TXT_COLOR));
        tcp.setTextColor(Color.parseColor(COLOR_PREFIX + TXT_COLOR));
    }

    private void setupGuessModeRadioButtons() {
        final String TXT_COLOR = "C0C0C0";
        final String BUTTON_XML_PREFIX = "RADIO_GUESS_";

        guessModeRadios = new RadioButton[GUESS_MODES.values().length];
        Class rid = R.id.class;
        for (GUESS_MODES gm : GUESS_MODES.values()) {
            try {
                guessModeRadios[gm.INDEX()] = findViewById(rid.getField(BUTTON_XML_PREFIX + gm.toString()).getInt(rid));
                guessModeRadios[gm.INDEX()].setTextColor(Color.parseColor(COLOR_PREFIX + TXT_COLOR));
                guessModeRadios[gm.INDEX()].setPadding(16, 0, 0, 0);   //  Décaler le texte un peu vers la droite
                guessModeRadios[gm.INDEX()].setOnClickListener(new View.OnClickListener() {   // Radiogroup non utilisé
                    @Override
                    public void onClick(View view) {
                        onGuessModeRadioClick(gm);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setupPaletteButtons() {
        final String BUTTON_XML_PREFIX = "BTN_PAL_";
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        paletteButtons = new CustomImageButton[MAX_COLORS];
        Class rid = R.id.class;
        for (int i = 0; i <= (paletteButtons.length - 1); i = i + 1) {
            try {
                paletteButtons[i] = findViewById(rid.getField(BUTTON_XML_PREFIX + i).getInt(rid));
                paletteButtons[i].setImageResource(DISK_PNG_ID);
                paletteButtons[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
                paletteButtons[i].setAdjustViewBounds(true);
                paletteButtons[i].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
                final int index = i;
                paletteButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPaletteButtonClick(index);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setupPaletteButtonsVisibility() {
        for (int i = 0; i <= (paletteButtons.length - 1); i = i + 1) {
            paletteButtons[i].setVisibility((i < colors) ? View.VISIBLE : View.GONE);
        }
    }

    private void setupCurrentPropPegButtons() {
        final String BUTTON_XML_PREFIX = "BTN_CUR_PROP_";
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        currentPropPegButtons = new CustomImageButton[MAX_PEGS];
        Class rid = R.id.class;
        for (int i = 0; i <= (currentPropPegButtons.length - 1); i = i + 1) {
            try {
                currentPropPegButtons[i] = findViewById(rid.getField(BUTTON_XML_PREFIX + i).getInt(rid));
                currentPropPegButtons[i].setImageResource(DISK_PNG_ID);
                currentPropPegButtons[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
                currentPropPegButtons[i].setAdjustViewBounds(true);
                currentPropPegButtons[i].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
                final int index = i;
                currentPropPegButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCurrentPropPegButtonClick(index);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException |
                    SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setupCurrentPropPegButtonsVisibility() {
        for (int i = 0; i <= (currentPropPegButtons.length - 1); i = i + 1) {
            currentPropPegButtons[i].setVisibility((i < pegs) ? View.VISIBLE : View.GONE);
        }
    }

    private void setupCommandButtons() {
        final String BUTTON_COMMAND_XML_PREFIX = "BTN_";
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        commandButtons = new CustomImageButton[COMMANDS.values().length];
        Class rid = R.id.class;
        for (COMMANDS c : COMMANDS.values())
            try {
                commandButtons[c.INDEX()] = findViewById(rid.getField(BUTTON_COMMAND_XML_PREFIX + c.toString()).getInt(rid));
                commandButtons[c.INDEX()].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
                final COMMANDS cc = c;
                commandButtons[c.INDEX()].setOnClickListener(new CustomImageButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onCommandButtonClick(cc);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    private void setupDotMatrixDisplay() {
        currentPropDotMatrixDisplayScore = findViewById(R.id.BTN_DOT_MATRIX_SCORE);
        currentPropDotMatrixDisplayScore.setOnCustomClickListener(new DotMatrixDisplayView.onCustomClickListener() {
            @Override
            public void onCustomClick() {
                onCurrentPropDotMatrixDisplayScoreCustomClick();
            }
        });
    }

    private void setupDotMatrixDisplayUpdater() {
        dotMatrixDisplayUpdater = new DotMatrixDisplayUpdater(currentPropDotMatrixDisplayScore);
    }

    private void setupPropRecords() {
        propRecordsHandler = new PropRecordsHandler(stringDB);
        currentPropRecord = propRecordsHandler.getCurrentPropRecord();
        secrPropRecord = propRecordsHandler.getSecrPropRecord();
    }

    private void setupCandRecords() {
        candRecordsHandler = new CandRecordsHandler();
        candRecordsHandler.updateCandRecordsToPropRecords(propRecordsHandler);
    }

    private void setupStringDB() {
        stringDB = new StringDB(this);
        stringDB.open();

        if (!stringDB.tableExists(getActivityInfosTableName())) {
            createPekislibTableIfNotExists(stringDB, getActivityInfosTableName());
        }
        if (!stringDB.tableExists(getPropsTableName())) {
            createMindTableIfNotExists(stringDB, getPropsTableName());
        }
        if (!stringDB.tableExists(getInputParamsTableName())) {
            createMindTableIfNotExists(stringDB, getInputParamsTableName());
            initializeTableInputParams(stringDB);
            String[] defaults = getDefaults(stringDB, getInputParamsTableName());
            setCurrentsForActivity(stringDB, MIND_ACTIVITIES.MAIN.toString(), getInputParamsTableName(), defaults);
        }
        if (!stringDB.tableExists(getPaletteColorsTableName())) {
            createMindTableIfNotExists(stringDB, getPaletteColorsTableName());
            initializeTablePaletteColors(stringDB);
            String[] defaults = getDefaults(stringDB, getPaletteColorsTableName());
            setCurrentsForActivity(stringDB, MIND_ACTIVITIES.MAIN.toString(), getPaletteColorsTableName(), defaults);
            createPresetWithDefaultValues(stringDB, getPaletteColorsTableName(), defaults);
        }
    }

    private void setupMainPropList() {
        mainPropListItemAdapter = new MainPropListItemAdapter(this, paletteColors);
        mainPropListItemAdapter.setOnButtonClick(new MainPropListItemAdapter.onButtonClickListener() {
            @Override
            public void onButtonClick(int position, int pegIndex) {
                onItemPropClick(position, pegIndex);
            }
        });
        mainPropListView = findViewById(R.id.PROP_LIST);
        mainPropListView.setAdapter(mainPropListItemAdapter);
    }

    private void setupMainPropListUpdater() {
        mainPropListUpdater = new MainPropListUpdater(mainPropListView, propRecordsHandler);
    }

    private void setupBarMenuItems() {
        final String BAR_MENU_ITEM_KEEP_SCREEN_NAME = "BAR_MENU_ITEM_KEEP_SCREEN";

        Class rid = R.id.class;
        try {
            barMenuItemKeepScreen = menu.findItem(rid.getField(BAR_MENU_ITEM_KEEP_SCREEN_NAME).getInt(rid));
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void launchInputButtonsActivity(int inputParamsColumnIndex) {   //  Pour pegs, colors ou score
        setCurrentForActivity(stringDB, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), getInputParamsTableName(), inputParamsColumnIndex, inputParams[inputParamsColumnIndex]);
        setStartStatusOfActivity(stringDB, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), ACTIVITY_START_STATUS.COLD);
        Intent callingIntent = new Intent(this, InputButtonsActivity.class);
        callingIntent.putExtra(TABLE_EXTRA_KEYS.TABLE.toString(), getInputParamsTableName());
        callingIntent.putExtra(TABLE_EXTRA_KEYS.INDEX.toString(), inputParamsColumnIndex);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), getLabels(stringDB, getInputParamsTableName())[inputParamsColumnIndex]);
        startActivityForResult(callingIntent, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.INDEX());
    }

    private void launchScoreActivity(int inputParamsColumnIndex) {
        setCurrentForActivity(stringDB, MIND_ACTIVITIES.SCORE.toString(), getInputParamsTableName(), inputParamsColumnIndex, inputParams[inputParamsColumnIndex]);
        setStartStatusOfActivity(stringDB, MIND_ACTIVITIES.SCORE.toString(), ACTIVITY_START_STATUS.COLD);
        Intent callingIntent = new Intent(this, ScoreActivity.class);
        startActivityForResult(callingIntent, MIND_ACTIVITIES_REQUEST_CODE_MULTIPLIER * MIND_ACTIVITIES.SCORE.INDEX());
    }

    private void launchColorPickerActivity() {
        setCurrentsForActivity(stringDB, PEKISLIB_ACTIVITIES.COLOR_PICKER.toString(), getPaletteColorsTableName(), paletteColors);
        setStartStatusOfActivity(stringDB, PEKISLIB_ACTIVITIES.COLOR_PICKER.toString(), ACTIVITY_START_STATUS.COLD);
        Intent callingIntent = new Intent(this, ColorPickerActivity.class);
        callingIntent.putExtra(TABLE_EXTRA_KEYS.TABLE.toString(), getPaletteColorsTableName());
        startActivityForResult(callingIntent, PEKISLIB_ACTIVITIES.COLOR_PICKER.INDEX());
    }

    private void launchHelpActivity() {
        Intent callingIntent = new Intent(this, HelpActivity.class);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), HELP_ACTIVITY_TITLE);
        callingIntent.putExtra(HELP_ACTIVITY_EXTRA_KEYS.HTML_ID.toString(), R.raw.helpmainactivity);
        startActivity(callingIntent);
    }
}