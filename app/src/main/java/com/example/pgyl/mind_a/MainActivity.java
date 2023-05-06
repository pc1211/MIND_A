package com.example.pgyl.mind_a;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.pgyl.pekislib_a.CustomButton;
import com.example.pgyl.pekislib_a.DotMatrixDisplayView;
import com.example.pgyl.pekislib_a.HelpActivity;
import com.example.pgyl.pekislib_a.InputButtonsActivity;
import com.example.pgyl.pekislib_a.StringDB;
import com.example.pgyl.pekislib_a.SymbolButtonView;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.pgyl.mind_a.Constants.MIND_ACTIVITIES;
import static com.example.pgyl.mind_a.PropRecord.COLOR_NUM_EMPTY;
import static com.example.pgyl.mind_a.StringDBTables.getInputParamsColorsIndex;
import static com.example.pgyl.mind_a.StringDBTables.getInputParamsPegsIndex;
import static com.example.pgyl.mind_a.StringDBTables.getInputParamsScoreIndex;
import static com.example.pgyl.mind_a.StringDBTables.getInputParamsTableName;
import static com.example.pgyl.mind_a.StringDBTables.getPropsTableName;
import static com.example.pgyl.mind_a.StringDBUtils.createMindTableIfNotExists;
import static com.example.pgyl.mind_a.StringDBUtils.initializeTableInputParams;
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
    //region Constantes
    public enum PALETTE_COLORS {   //  Autant de valeurs que dans main.xml (BTN_PAL_...), avec un maximum de 10 (index de 0 à 9)
        RED("FF0000"), GRREN("00FF00"), BLUE("0000FF"), YELLOW("FFFF00"), BROWN("A47449"), ORANGE("FF7F00"), FUCHSIA("FF00FF"), CYAN("00FFFF"), BLACK("000000"), WHITE("FFFFFF");

        private String rgb;

        PALETTE_COLORS(String rgb) {
            this.rgb = rgb;
        }

        public String RGB() {
            return rgb;
        }

        public int INDEX() {
            return ordinal();
        }

        public static PALETTE_COLORS getByIndex(int index) {
            for (PALETTE_COLORS c : PALETTE_COLORS.values()) {
                if (c.INDEX() == index) {
                    return c;
                }
            }
            return null;
        }
    }

    public enum CURRENT_PROP_PEGS {   //  Autant de valeurs que dans main.xml (BTN_PROP_...), avec un maximum de 9 (score avec max 9 dizaines)
        CUR_PROP_0, CUR_PROP_1, CUR_PROP_2, CUR_PROP_3, CUR_PROP_4, CUR_PROP_5, CUR_PROP_6, CUR_PROP_7, CUR_PROP_8;

        public int INDEX() {
            return ordinal();
        }
    }

    private enum FLOWS {
        SUBMIT, CLEAR, DELETE_LAST, NEW, CHEAT;

        public int INDEX() {
            return ordinal();
        }
    }

    private enum EDIT_MODES {PALETTE, CURRENT_PROP, NONE}

    private enum COLOR_MODES {NORMAL, INVERSE}

    public enum MIND_SHP_KEY_NAMES {KEEP_SCREEN, USER_GUESS, INPUT_PARAMS_INDEX}

    public int COLOR_BUTTON_SVG_ID = R.raw.disk;
    //endregion

    //region Variables
    private boolean onStartUp;
    private String[] inputParams;
    private int inputParamsIndex;
    private CustomButton[] flowButtons;
    private SymbolButtonView[] paletteButtons;
    private SymbolButtonView[] currentPropButtons;
    private DotMatrixDisplayView currentPropDotMatrixDisplayScore;
    private MainDotMatrixDisplayUpdater dotMatrixDisplayUpdater;
    private RadioButton radioUserGuess;
    private RadioButton radioAndroidGuess;
    private EDIT_MODES editMode;
    private int editIndex;
    private Menu menu;
    private MenuItem barMenuItemKeepScreen;
    private CandRecordsHandler candRecordsHandler;
    private PropRecordsHandler propRecordsHandler;
    private PropRecord currentPropRecord;
    private PropRecord secrPropRecord;
    private MainPropListUpdater mainPropListUpdater;
    private boolean keepScreen;
    private boolean userGuess;
    private int pegs;
    private int colors;
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
        setupPaletteButtons();
        setupCurrentPropButtons();
        setupFlowButtons();
        setupDotMatrixDisplay();
        setupRadioButtons();
        setupTextViews();
        validReturnFromCalledActivity = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        setCurrentsForActivity(stringDB, MIND_ACTIVITIES.MAIN.toString(), getInputParamsTableName(), inputParams);
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
        menu = null;
        savePreferences();
    }
    //endregion

    @Override
    protected void onResume() {
        super.onResume();

        onStartUp = true;
        shpFileName = getPackageName() + "." + getClass().getSimpleName() + SHP_FILE_NAME_SUFFIX;
        keepScreen = getSHPKeepScreen();
        userGuess = getSHPUserGuess();
        editMode = EDIT_MODES.NONE;
        setupStringDB();
        inputParams = getCurrentsFromActivity(stringDB, MIND_ACTIVITIES.MAIN.toString(), getInputParamsTableName());
        pegs = Integer.valueOf(inputParams[getInputParamsPegsIndex()]);
        colors = Integer.valueOf(inputParams[getInputParamsColorsIndex()]);
        boolean newScore = false;

        inputParamsIndex = getSHPInputParamsIndex();
        if (validReturnFromCalledActivity) {
            validReturnFromCalledActivity = false;
            if (calledActivityName.equals(PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString())) {
                String value = getCurrentFromActivity(stringDB, PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString(), getInputParamsTableName(), inputParamsIndex);
                int v = Integer.valueOf(value);
                if (inputParamsIndex == getInputParamsPegsIndex()) {
                    inputParams[inputParamsIndex] = value;
                    pegs = v;
                }
                if (inputParamsIndex == getInputParamsColorsIndex()) {
                    inputParams[inputParamsIndex] = value;
                    colors = v;
                }
                if (inputParamsIndex == getInputParamsScoreIndex()) {
                    int n = v / 10;   //  Noirs
                    int b = v % 10;   //  Blancs
                    if ((n <= pegs) && (b <= pegs) && (v <= (10 * pegs)) && (v != (10 * (pegs - 1) + 1))) {
                        inputParams[inputParamsIndex] = value;
                        newScore = true;
                    } else {   //  Bad guy
                        msgBox("Invalid score: " + value, this);
                    }
                }
            }
        }

        setupPropRecords();
        setupMainPropList();
        setupMainPropListUpdater();
        setupDotMatrixDisplayUpdater();
        setupCandRecords();
        setupFlowButtonColors();
        setupPaletteButtonsVisibility();
        setupCurrentPropButtonsVisibility();
        updateCurrentPropRecord(newScore, inputParamsIndex);
        updateDisplayKeepScreen();
        updateDisplayUserGuess();
        updateDisplayPaletteButtonColors();
        updateDisplayCurrentPropButtonColors();
        updateDisplayCurrentPropDotMatrixDisplayScore();
        mainPropListUpdater.reload();
        mainPropListUpdater.repaint();
        invalidateOptionsMenu();
        onStartUp = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        validReturnFromCalledActivity = false;
        if (requestCode == PEKISLIB_ACTIVITIES.INPUT_BUTTONS.INDEX()) {
            calledActivityName = PEKISLIB_ACTIVITIES.INPUT_BUTTONS.toString();
            if (resultCode == RESULT_OK) {
                validReturnFromCalledActivity = true;
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

    private void onPaletteButtonClick(int index) {
        if (userGuess) {
            EDIT_MODES oldEditMode = editMode;
            int oldEditIndex = editIndex;
            editMode = EDIT_MODES.PALETTE;
            editIndex = index;
            if (oldEditMode.equals(EDIT_MODES.PALETTE)) {   //  Click Palette puis Palette
                if (editIndex != oldEditIndex) {
                    updateDisplayPaletteButtonColor(editIndex, COLOR_MODES.INVERSE);
                } else {   //  Même bouton => L'édition en cours est annulée
                    editMode = EDIT_MODES.NONE;
                }
                updateDisplayPaletteButtonColor(oldEditIndex, COLOR_MODES.NORMAL);
            }
            if (oldEditMode.equals(EDIT_MODES.CURRENT_PROP)) {   //  Click Current Prop puis Palette
                currentPropRecord.setCombAtIndex(oldEditIndex, editIndex);
                updateDisplayPaletteButtonColor(editIndex, COLOR_MODES.NORMAL);
                updateDisplayCurrentPropButtonColor(oldEditIndex, COLOR_MODES.NORMAL);
                editMode = EDIT_MODES.NONE;
            }
            if (oldEditMode.equals(EDIT_MODES.NONE)) {   //  Rien puis Click Palette
                updateDisplayPaletteButtonColor(editIndex, COLOR_MODES.INVERSE);
            }
        }
    }

    private void onCurrentPropButtonClick(int index) {
        if (userGuess) {
            EDIT_MODES oldEditMode = editMode;
            int oldEditIndex = editIndex;
            editMode = EDIT_MODES.CURRENT_PROP;
            editIndex = index;
            if (oldEditMode.equals(EDIT_MODES.CURRENT_PROP)) {   //  Click Current Prop puis Current Prop
                if (editIndex != oldEditIndex) {
                    updateDisplayCurrentPropButtonColor(editIndex, COLOR_MODES.INVERSE);
                } else {   //  Même bouton => L'édition en cours est annulée
                    editMode = EDIT_MODES.NONE;
                }
                updateDisplayCurrentPropButtonColor(oldEditIndex, COLOR_MODES.NORMAL);
            }
            if (oldEditMode.equals(EDIT_MODES.PALETTE)) {   //  Click Palette puis Current Prop
                currentPropRecord.setCombAtIndex(editIndex, oldEditIndex);
                updateDisplayCurrentPropButtonColor(editIndex, COLOR_MODES.NORMAL);
                updateDisplayPaletteButtonColor(oldEditIndex, COLOR_MODES.NORMAL);
                editMode = EDIT_MODES.NONE;
            }
            if (oldEditMode.equals(EDIT_MODES.NONE)) {   //  Rien puis Click Current Prop
                updateDisplayCurrentPropButtonColor(editIndex, COLOR_MODES.INVERSE);
            }
        }
    }

    private void onDotMatrixDisplayScoreCustomClick() {
        if (!userGuess) {
            inputParamsIndex = getInputParamsScoreIndex();
            launchInputButtonsActivity(inputParamsIndex);
        }
    }

    private void onRadioButtonGuessChanged(int checkedId) {
        if (!onStartUp) {   //  Ne pas réagir aux manipulations de radioButtons dans le onResume
            if (checkedId == R.id.RADIO_USER_GUESS) {
                onRadioUserGuessClick();
            }
            if (checkedId == R.id.RADIO_ANDROID_GUESS) {
                onRadioAndroidGuessClick();
            }
        }
    }

    private void onRadioUserGuessClick() {
        userGuess = true;
        onButtonClickNew();
    }

    private void onRadioAndroidGuessClick() {
        userGuess = false;
        onButtonClickNew();
    }

    private void onFlowButtonClick(FLOWS command) {
        if (command.equals(FLOWS.SUBMIT)) {
            onButtonClickSubmit();
        }
        if (command.equals(FLOWS.CLEAR)) {
            onButtonClickClear();
        }
        if (command.equals(FLOWS.DELETE_LAST)) {
            onButtonClickDeleteLast();
        }
        if (command.equals(FLOWS.NEW)) {
            onButtonClickNew();
        }
        if (command.equals(FLOWS.CHEAT)) {
            onButtonClickCheat();
        }
    }

    private void onButtonClickSubmit() {
        if (currentPropRecord.hasValidComb()) {   //  Aucune couleur COLOR_NUM_EMPTY
            PropRecord newPropRecord = propRecordsHandler.createPropRecordWithId(propRecordsHandler.getPropRecordMaxId() + 1);
            propRecordsHandler.addPropRecord(newPropRecord);
            newPropRecord.setComb(currentPropRecord.getComb());
            if (userGuess) {
                newPropRecord.setScore(candRecordsHandler.getScore(currentPropRecord.getComb(), secrPropRecord.getComb()));
                currentPropRecord.resetComb();
                currentPropRecord.setScore(0);
            } else {   //  Android Guess
                newPropRecord.setScore(currentPropRecord.getScore());
                int solIndex = candRecordsHandler.getSolutionCandRecordsIndex(currentPropRecord.getComb(), currentPropRecord.getScore());
                if (solIndex == UNDEFINED) {   //  Encore plusieurs solutions possibles
                    currentPropRecord.setComb(candRecordsHandler.getGuessComb());
                    currentPropRecord.setScore(0);
                } else {   //  Trouvé !
                    newPropRecord = propRecordsHandler.createPropRecordWithId(propRecordsHandler.getPropRecordMaxId() + 1);
                    propRecordsHandler.addPropRecord(newPropRecord);
                    newPropRecord.setComb(candRecordsHandler.getCombAtIndex(solIndex));
                    newPropRecord.setScore(10 * pegs);
                    currentPropRecord.resetComb();
                    currentPropRecord.setScore(0);
                }
            }
            mainPropListUpdater.reload();
            mainPropListUpdater.repaint();
            updateDisplayCurrentPropButtonColors();
            updateDisplayCurrentPropDotMatrixDisplayScore();
        } else {
            msgBox("Invalid proposal", this);
        }
    }

    private void onButtonClickClear() {
        if (userGuess) {
            currentPropRecord.resetComb();
        }
        currentPropRecord.setScore(0);
        updateDisplayCurrentPropButtonColors();
        updateDisplayCurrentPropDotMatrixDisplayScore();
    }

    private void onButtonClickDeleteLast() {
        if (!propRecordsHandler.getPropRecords().isEmpty()) {
            propRecordsHandler.removePropRecordAtId(propRecordsHandler.getPropRecordMaxId());   //  Enlever le dernier PropRecord (cad avec le id le plus élevé)
        }
        if (!userGuess) {   //  Android Guess
            candRecordsHandler.updateCandRecordsToPropRecords(propRecordsHandler.getPropRecords());
            currentPropRecord.setComb(candRecordsHandler.getGuessComb());
            currentPropRecord.setScore(0);
            updateDisplayCurrentPropButtonColors();
            updateDisplayCurrentPropDotMatrixDisplayScore();
        }
        mainPropListUpdater.reload();
        mainPropListUpdater.repaint();
    }

    private void onButtonClickNew() {
        propRecordsHandler.clearPropRecords();   //  Sauf currentPropRecord et secrPropRecord
        if (!userGuess) {
            candRecordsHandler.selectAll();
            currentPropRecord.setComb(candRecordsHandler.getGuessComb());
        } else {   //  User Guess
            currentPropRecord.resetComb();
            currentPropRecord.setScore(0);
            secrPropRecord.setRandomComb();
        }
        currentPropRecord.setScore(0);
        updateDisplayCurrentPropButtonColors();
        updateDisplayCurrentPropDotMatrixDisplayScore();
        mainPropListUpdater.reload();
        mainPropListUpdater.repaint();
    }

    private void onButtonClickCheat() {
        if (userGuess) {
            currentPropRecord.setComb(secrPropRecord.getComb());
            currentPropRecord.setScore(10 * pegs);
            updateDisplayCurrentPropButtonColors();
            updateDisplayCurrentPropDotMatrixDisplayScore();
        } else {
            msgBox("I cannot read human memory", this);
        }
    }

    private void updateDisplayPaletteButtonColors() {
        for (int i = 0; i <= (colors - 1); i = i + 1) {
            updateDisplayPaletteButtonColor(i, COLOR_MODES.NORMAL);
        }
    }

    private void updateDisplayCurrentPropButtonColors() {
        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            updateDisplayCurrentPropButtonColor(i, COLOR_MODES.NORMAL);
        }
    }

    private void updateDisplayPaletteButtonColor(int index, COLOR_MODES colorMode) {
        final String BACK_COLOR_NORMAL = "000000";
        final String BACK_COLOR_INVERSE = "FFFFFF";

        String color = PALETTE_COLORS.getByIndex(index).RGB();
        if (colorMode.equals(COLOR_MODES.NORMAL)) {
            paletteButtons[index].setColors(color, BACK_COLOR_NORMAL, color, BACK_COLOR_INVERSE);
        } else {   // Inverse
            paletteButtons[index].setColors(color, BACK_COLOR_INVERSE, color, BACK_COLOR_NORMAL);
        }
    }

    private void updateDisplayCurrentPropButtonColor(int index, COLOR_MODES colorMode) {
        final String BACK_COLOR_NORMAL = "000000";
        final String BACK_COLOR_INVERSE = "FFFFFF";
        final String EMPTY_COLOR = "808080";

        String color = (currentPropRecord.getCombAtIndex(index) != COLOR_NUM_EMPTY) ? PALETTE_COLORS.getByIndex(currentPropRecord.getCombAtIndex(index)).RGB() : EMPTY_COLOR;
        if (colorMode.equals(COLOR_MODES.NORMAL)) {
            currentPropButtons[index].setColors(color, BACK_COLOR_NORMAL, color, BACK_COLOR_INVERSE);
        } else {   // Inverse
            currentPropButtons[index].setColors(color, BACK_COLOR_INVERSE, color, BACK_COLOR_NORMAL);
        }
    }

    private void updateDisplayCurrentPropDotMatrixDisplayScore() {
        dotMatrixDisplayUpdater.displayText(currentPropRecord.getStringScore());
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

    private void updateDisplayUserGuess() {
        if (userGuess) {
            radioUserGuess.setChecked(true);
        } else {   //  Android Guesses
            radioAndroidGuess.setChecked(true);
        }
    }

    private void savePreferences() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        SharedPreferences.Editor shpEditor = shp.edit();
        shpEditor.putBoolean(MIND_SHP_KEY_NAMES.KEEP_SCREEN.toString(), keepScreen);
        shpEditor.putBoolean(MIND_SHP_KEY_NAMES.USER_GUESS.toString(), userGuess);
        shpEditor.putInt(MIND_SHP_KEY_NAMES.INPUT_PARAMS_INDEX.toString(), inputParamsIndex);
        shpEditor.commit();
    }

    private boolean getSHPKeepScreen() {
        final boolean KEEP_SCREEN_DEFAULT_VALUE = false;

        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getBoolean(MIND_SHP_KEY_NAMES.KEEP_SCREEN.toString(), KEEP_SCREEN_DEFAULT_VALUE);
    }

    private boolean getSHPUserGuess() {
        final boolean USER_GUESS_DEFAULT_VALUE = true;

        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getBoolean(MIND_SHP_KEY_NAMES.USER_GUESS.toString(), USER_GUESS_DEFAULT_VALUE);
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

    private void setupRadioButtons() {
        final String TXT_COLOR = "C0C0C0";

        RadioGroup grpg = findViewById(R.id.GROUP_GUESS);
        grpg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onRadioButtonGuessChanged(checkedId);
            }
        });
        radioUserGuess = findViewById(R.id.RADIO_USER_GUESS);
        radioAndroidGuess = findViewById(R.id.RADIO_ANDROID_GUESS);
        radioUserGuess.setTextColor(Color.parseColor(COLOR_PREFIX + TXT_COLOR));
        radioAndroidGuess.setTextColor(Color.parseColor(COLOR_PREFIX + TXT_COLOR));
    }

    private void setupPaletteButtons() {
        final String BUTTON_XML_PREFIX = "BTN_PAL_";
        final float BUTTON_SYMBOL_SIZE_COEFF = 0.75f;   //  Pour que le symbole ne frôle pas les bords de sa View
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        paletteButtons = new SymbolButtonView[PALETTE_COLORS.values().length];
        Class rid = R.id.class;
        for (PALETTE_COLORS pc : PALETTE_COLORS.values())
            try {
                paletteButtons[pc.INDEX()] = findViewById(rid.getField(BUTTON_XML_PREFIX + pc.INDEX()).getInt(rid));
                paletteButtons[pc.INDEX()].setSymbolSizeCoeff(BUTTON_SYMBOL_SIZE_COEFF);
                paletteButtons[pc.INDEX()].setSVGImageResource(COLOR_BUTTON_SVG_ID);
                paletteButtons[pc.INDEX()].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
                final int index = pc.INDEX();
                paletteButtons[pc.INDEX()].setCustomOnClickListener(new SymbolButtonView.onCustomClickListener() {
                    @Override
                    public void onCustomClick() {
                        onPaletteButtonClick(index);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    private void setupPaletteButtonsVisibility() {
        for (int i = 0; i <= (PALETTE_COLORS.values().length - 1); i = i + 1) {
            paletteButtons[i].setVisibility((i < colors) ? View.VISIBLE : View.GONE);
        }
    }

    private void setupCurrentPropButtons() {
        final String BUTTON_XML_PREFIX = "BTN_CUR_PROP_";
        final float BUTTON_SYMBOL_SIZE_COEFF = 0.75f;   //  Pour que le symbole ne frôle pas les bords de sa View
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        currentPropButtons = new SymbolButtonView[CURRENT_PROP_PEGS.values().length];
        Class rid = R.id.class;
        for (CURRENT_PROP_PEGS cpp : CURRENT_PROP_PEGS.values())
            try {
                currentPropButtons[cpp.INDEX()] = findViewById(rid.getField(BUTTON_XML_PREFIX + cpp.INDEX()).getInt(rid));
                currentPropButtons[cpp.INDEX()].setSymbolSizeCoeff(BUTTON_SYMBOL_SIZE_COEFF);
                currentPropButtons[cpp.INDEX()].setSVGImageResource(COLOR_BUTTON_SVG_ID);
                currentPropButtons[cpp.INDEX()].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
                final int index = cpp.INDEX();
                currentPropButtons[cpp.INDEX()].setCustomOnClickListener(new SymbolButtonView.onCustomClickListener() {
                    @Override
                    public void onCustomClick() {
                        onCurrentPropButtonClick(index);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException |
                    SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    private void setupCurrentPropButtonsVisibility() {
        for (int i = 0; i <= (CURRENT_PROP_PEGS.values().length - 1); i = i + 1) {
            currentPropButtons[i].setVisibility((i < pegs) ? View.VISIBLE : View.GONE);
        }
    }

    private void setupFlowButtons() {
        final String BUTTON_COMMAND_XML_PREFIX = "BTN_";
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        flowButtons = new CustomButton[FLOWS.values().length];
        Class rid = R.id.class;
        for (FLOWS f : FLOWS.values())
            try {
                flowButtons[f.INDEX()] = findViewById(rid.getField(BUTTON_COMMAND_XML_PREFIX + f.toString()).getInt(rid));
                flowButtons[f.INDEX()].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
                final FLOWS ff = f;
                flowButtons[f.INDEX()].setOnClickListener(new CustomButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onFlowButtonClick(ff);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    private void setupFlowButtonColors() {
        final String COLOR_PRESSED = "FF9A22";

        for (FLOWS f : FLOWS.values()) {
            flowButtons[f.INDEX()].setColors(COLOR_PRESSED, null);
        }
    }

    private void setupDotMatrixDisplay() {
        currentPropDotMatrixDisplayScore = findViewById(R.id.BTN_DOT_MATRIX_SCORE);
        currentPropDotMatrixDisplayScore.setOnCustomClickListener(new DotMatrixDisplayView.onCustomClickListener() {
            @Override
            public void onCustomClick() {
                onDotMatrixDisplayScoreCustomClick();
            }
        });
    }

    private void setupDotMatrixDisplayUpdater() {
        dotMatrixDisplayUpdater = new MainDotMatrixDisplayUpdater(currentPropDotMatrixDisplayScore);
    }

    private void updateCurrentPropRecord(boolean newScore, int inputParamsIndex) {
        if (newScore) {
            currentPropRecord.setScore(Integer.valueOf(inputParams[inputParamsIndex]));
        }
    }

    private void setupPropRecords() {
        propRecordsHandler = new PropRecordsHandler(stringDB, pegs, colors);
        currentPropRecord = propRecordsHandler.getCurrentPropRecord();
        secrPropRecord = propRecordsHandler.getSecrPropRecord();
    }

    private void setupCandRecords() {
        candRecordsHandler = new CandRecordsHandler(pegs, colors);
        candRecordsHandler.updateCandRecordsToPropRecords(propRecordsHandler.getPropRecords());
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
    }

    private void setupMainPropList() {
        mainPropListItemAdapter = new MainPropListItemAdapter(this, stringDB, pegs);
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

    private void launchHelpActivity() {
        Intent callingIntent = new Intent(this, HelpActivity.class);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), HELP_ACTIVITY_TITLE);
        callingIntent.putExtra(HELP_ACTIVITY_EXTRA_KEYS.HTML_ID.toString(), R.raw.helpmainactivity);
        startActivity(callingIntent);
    }
}