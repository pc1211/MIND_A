package com.example.pgyl.mind_a;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.example.pgyl.pekislib_a.CustomButton;
import com.example.pgyl.pekislib_a.DotMatrixDisplayView;
import com.example.pgyl.pekislib_a.HelpActivity;
import com.example.pgyl.pekislib_a.StringDB;
import com.example.pgyl.pekislib_a.SymbolButtonView;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.pgyl.mind_a.StringDBTables.getPropsTableName;
import static com.example.pgyl.mind_a.StringDBUtils.createMindTableIfNotExists;
import static com.example.pgyl.mind_a.StringDBUtils.initializeTableProps;
import static com.example.pgyl.pekislib_a.Constants.ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.Constants.SHP_FILE_NAME_SUFFIX;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_TITLE;
import static com.example.pgyl.pekislib_a.MiscUtils.msgBox;
import static com.example.pgyl.pekislib_a.StringDBTables.getActivityInfosTableName;
import static com.example.pgyl.pekislib_a.StringDBTables.getTempTableName;
import static com.example.pgyl.pekislib_a.StringDBUtils.createPekislibTableIfNotExists;

//  MainActivity fait appel à PropRecordShandler pour la gestion des PropRecord (création, suppression, tri, ...)
//  MainPropListUpdater maintient la liste de MainActivity (rafraîchissement, scrollbar, ...), fait appel à MainPropListAdapter (pour gérer chaque item) et également à PropRecordShandler (pour leur mise à jour)
//  MainPropListItemAdapter reçoit ses items (PropRecord) de la part de MainPropListUpdater et gère chaque item de la liste (avec ses boutons)

public class MainActivity extends Activity {
    //region Constantes
    public enum PALETTE_COLORS {
        RED("FF0000"), GRREN("00FF00"), BLUE("0000FF"), YELLOW("FFFF00"), BROWN("A47449"), CYAN("00FFFF"), ORANGE("FF7F00"), FUCHSIA("FF00FF"), BLACK("000000"), WHITE("FFFFFF");

        private String color;

        PALETTE_COLORS(String color) {
            this.color = color;
        }

        public String COLOR() {
            return color;
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

    private enum CURRENT_PROP_PEGS {
        PROP_0, PROP_1, PROP_2, PROP_3, PROP_4, PROP_5, PROP_6, PROP_7, PROP_8, PROP_9;

        CURRENT_PROP_PEGS() {
        }

        public int INDEX() {
            return ordinal();
        }
    }

    private enum FLOWS {
        SUBMIT, CLEAR, CLEAR_PREV, NEW;

        FLOWS() {
        }

        public int INDEX() {
            return ordinal();
        }
    }

    private enum EDIT_MODES {PALETTE, CURRENT_PROP, NONE}

    private enum COLOR_MODES {NORMAL, INVERSE}

    public enum MIND_SHP_KEY_NAMES {KEEP_SCREEN, PEGS, COLORS}

    public int COLOR_BUTTON_SVG_ID = R.raw.disk;
    //endregion

    //region Variables
    private CustomButton[] flowButtons;
    private SymbolButtonView[] paletteButtons;
    private SymbolButtonView[] currentPropButtons;
    private DotMatrixDisplayView dotMatrixDisplayView;
    private MainDotMatrixDisplayUpdater dotMatrixDisplayUpdater;
    private EDIT_MODES editMode;
    private int editIndex;
    private Menu menu;
    private MenuItem barMenuItemKeepScreen;
    private PropRecordsHandler propRecordsHandler;
    private PropRecord currentPropRecord;
    private int[] currentPropColorNums;
    private MainPropListUpdater mainPropListUpdater;
    private boolean keepScreen;
    private int pegs;
    private int colors;
    private ListView mainPropListView;
    private MainPropListItemAdapter mainPropListItemAdapter;
    private StringDB stringDB;
    private String shpFileName;

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
    }

    @Override
    protected void onPause() {
        super.onPause();

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

        shpFileName = getPackageName() + SHP_FILE_NAME_SUFFIX;   //  Sans nom d'activité car sera partagé avec CtDisplayActivity
        keepScreen = getSHPKeepScreen();
        pegs = getSHPPegs();
        colors = getSHPColors();
        editMode = EDIT_MODES.NONE;
        setupStringDB();
        setupPropRecords();
        setupMainPropList();
        setupMainPropListUpdater();
        setupDotMatrixDisplayUpdater();
        setupFlowButtonColors();

        updateDisplayKeepScreen();
        updateDisplayPaletteButtonColors();
        updateDisplayCurrentPropButtonColors();
        mainPropListUpdater.reload();
        mainPropListUpdater.repaint();
        invalidateOptionsMenu();
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
        EDIT_MODES oldEditMode = editMode;
        int oldEditIndex = editIndex;
        editMode = EDIT_MODES.PALETTE;
        editIndex = index;
        if (oldEditMode.equals(EDIT_MODES.PALETTE)) {   //  Click Palette puis Palette
            if (editIndex != oldEditIndex) {
                updateDisplayPaletteButtonColor(editIndex, COLOR_MODES.INVERSE);
            } else {
                updateDisplayPaletteButtonColor(oldEditIndex, COLOR_MODES.NORMAL);
            }
        }
        if (oldEditMode.equals(EDIT_MODES.CURRENT_PROP)) {   //  Click Current Prop puis Palette
            updateDisplayPaletteButtonColor(editIndex, COLOR_MODES.NORMAL);
            currentPropColorNums[oldEditIndex] = editIndex;
            updateDisplayCurrentPropButtonColor(oldEditIndex, COLOR_MODES.NORMAL);
            editMode = EDIT_MODES.NONE;
        }
        if (oldEditMode.equals(EDIT_MODES.NONE)) {   //  Rien puis Click Palette
            updateDisplayPaletteButtonColor(editIndex, COLOR_MODES.INVERSE);
        }
    }

    private void onCurrentPropButtonClick(int index) {
        EDIT_MODES oldEditMode = editMode;
        int oldEditIndex = editIndex;
        editMode = EDIT_MODES.CURRENT_PROP;
        editIndex = index;
        if (oldEditMode.equals(EDIT_MODES.CURRENT_PROP)) {   //  Click Current Prop puis Current Prop
            if (editIndex != oldEditIndex) {
                updateDisplayCurrentPropButtonColor(editIndex, COLOR_MODES.INVERSE);
            } else {
                updateDisplayCurrentPropButtonColor(oldEditIndex, COLOR_MODES.NORMAL);
            }
        }
        if (oldEditMode.equals(EDIT_MODES.PALETTE)) {   //  Click Palette puis Current Prop
            updateDisplayCurrentPropButtonColor(editIndex, COLOR_MODES.NORMAL);
            currentPropColorNums[editIndex] = oldEditIndex;
            updateDisplayPaletteButtonColor(oldEditIndex, COLOR_MODES.NORMAL);
            editMode = EDIT_MODES.NONE;
        }
        if (oldEditMode.equals(EDIT_MODES.NONE)) {   //  Rien puis Click Current Prop
            updateDisplayCurrentPropButtonColor(editIndex, COLOR_MODES.INVERSE);
        }
    }

    private void onFlowButtonClick(FLOWS command) {
        if (command.equals(FLOWS.SUBMIT)) {
            onButtonClickSubmit();
        }
        if (command.equals(FLOWS.CLEAR)) {
            onButtonClickClear();
        }
        if (command.equals(FLOWS.CLEAR_PREV)) {
            onButtonClickClearPrevious();
        }
        if (command.equals(FLOWS.NEW)) {
            onButtonClickNew();
        }
    }

    private void onButtonClickSubmit() {

    }

    private void onButtonClickClear() {

    }

    private void onButtonClickClearPrevious() {

    }

    private void onButtonClickNew() {

    }

    private void updateDisplayPaletteButtonColors() {
        for (PALETTE_COLORS pc : PALETTE_COLORS.values()) {
            updateDisplayPaletteButtonColor(pc.INDEX(), COLOR_MODES.NORMAL);
        }
    }

    private void updateDisplayCurrentPropButtonColors() {
        for (CURRENT_PROP_PEGS cpp : CURRENT_PROP_PEGS.values()) {
            updateDisplayCurrentPropButtonColor(cpp.INDEX(), COLOR_MODES.NORMAL);
        }
    }

    private void updateDisplayPaletteButtonColor(int index, COLOR_MODES colorMode) {
        final String COLOR_NEUTRAL = "C0C0C0";

        String color = PALETTE_COLORS.getByIndex(index).COLOR();
        if (colorMode.equals(COLOR_MODES.NORMAL)) {
            paletteButtons[index].setColors(color, COLOR_NEUTRAL, COLOR_NEUTRAL, color);
        } else {   // Inverse
            paletteButtons[index].setColors(COLOR_NEUTRAL, color, color, COLOR_NEUTRAL);
        }
    }

    private void updateDisplayCurrentPropButtonColor(int index, COLOR_MODES colorMode) {
        final String COLOR_NEUTRAL = "C0C0C0";

        String color = PALETTE_COLORS.getByIndex(currentPropColorNums[index]).COLOR();
        if (colorMode.equals(COLOR_MODES.NORMAL)) {
            currentPropButtons[index].setColors(color, COLOR_NEUTRAL, COLOR_NEUTRAL, color);
        } else {   // Inverse
            currentPropButtons[index].setColors(COLOR_NEUTRAL, color, color, COLOR_NEUTRAL);
        }
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

    private void savePreferences() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        SharedPreferences.Editor shpEditor = shp.edit();
        shpEditor.putBoolean(MIND_SHP_KEY_NAMES.KEEP_SCREEN.toString(), keepScreen);
        shpEditor.putInt(MIND_SHP_KEY_NAMES.PEGS.toString(), pegs);
        shpEditor.putInt(MIND_SHP_KEY_NAMES.COLORS.toString(), colors);
        shpEditor.commit();
    }

    private boolean getSHPKeepScreen() {
        final boolean KEEP_SCREEN_DEFAULT_VALUE = false;

        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getBoolean(MIND_SHP_KEY_NAMES.KEEP_SCREEN.toString(), KEEP_SCREEN_DEFAULT_VALUE);
    }

    private int getSHPPegs() {
        final int PEGS_DEFAULT_VALUE = 4;

        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getInt(MIND_SHP_KEY_NAMES.PEGS.toString(), PEGS_DEFAULT_VALUE);
    }

    private int getSHPColors() {
        final int COLORS_DEFAULT_VALUE = 6;

        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getInt(MIND_SHP_KEY_NAMES.COLORS.toString(), COLORS_DEFAULT_VALUE);
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

    private void setupCurrentPropButtons() {
        final String BUTTON_COMMAND_XML_PREFIX = "BTN_PROP_";
        final float BUTTON_SYMBOL_SIZE_COEFF = 0.75f;   //  Pour que le symbole ne frôle pas les bords de sa View
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        paletteButtons = new SymbolButtonView[PALETTE_COLORS.values().length];
        Class rid = R.id.class;
        for (CURRENT_PROP_PEGS cpp : CURRENT_PROP_PEGS.values())
            try {
                paletteButtons[cpp.INDEX()] = findViewById(rid.getField(BUTTON_COMMAND_XML_PREFIX + cpp.INDEX()).getInt(rid));
                paletteButtons[cpp.INDEX()].setSymbolSizeCoeff(BUTTON_SYMBOL_SIZE_COEFF);
                paletteButtons[cpp.INDEX()].setSVGImageResource(COLOR_BUTTON_SVG_ID);
                paletteButtons[cpp.INDEX()].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
                final int index = cpp.INDEX();
                paletteButtons[cpp.INDEX()].setCustomOnClickListener(new SymbolButtonView.onCustomClickListener() {
                    @Override
                    public void onCustomClick() {
                        onCurrentPropButtonClick(index);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    private void setupFlowButtons() {
        final String BUTTON_COMMAND_XML_PREFIX = "BTN_";
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        flowButtons = new CustomButton[FLOWS.values().length];
        Class rid = R.id.class;
        for (FLOWS fb : FLOWS.values())
            try {
                flowButtons[fb.INDEX()] = findViewById(rid.getField(BUTTON_COMMAND_XML_PREFIX + fb.toString()).getInt(rid));
                flowButtons[fb.INDEX()].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
                final FLOWS fcommand = fb;
                flowButtons[fb.INDEX()].setOnClickListener(new CustomButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onFlowButtonClick(fcommand);
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
        dotMatrixDisplayView = findViewById(R.id.DOT_MATRIX_DISPLAY);
    }

    private void setupDotMatrixDisplayUpdater() {
        dotMatrixDisplayUpdater = new MainDotMatrixDisplayUpdater(dotMatrixDisplayView);
    }

    private void setupPropRecords() {
        propRecordsHandler = new PropRecordsHandler(this, stringDB);
        currentPropRecord = propRecordsHandler.getCurrentProp();
        currentPropColorNums = currentPropRecord.getColorNums();
    }

    private void setupStringDB() {
        stringDB = new StringDB(this);
        stringDB.open();

        if (!stringDB.tableExists(getActivityInfosTableName())) {
            createPekislibTableIfNotExists(stringDB, getActivityInfosTableName());
        }
        if (!stringDB.tableExists(getPropsTableName())) {
            createMindTableIfNotExists(stringDB, getPropsTableName());
            initializeTableProps(stringDB);
        }

        if (!stringDB.tableExists(getTempTableName())) {
            createPekislibTableIfNotExists(stringDB, getTempTableName());   //  Initialize sera fait lors de l'appel à InputButtons Activity, pour le nombre de couleurs/places ou le score
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

    private void launchHelpActivity() {
        Intent callingIntent = new Intent(this, HelpActivity.class);
        callingIntent.putExtra(ACTIVITY_EXTRA_KEYS.TITLE.toString(), HELP_ACTIVITY_TITLE);
        callingIntent.putExtra(HELP_ACTIVITY_EXTRA_KEYS.HTML_ID.toString(), R.raw.helpmainactivity);
        startActivity(callingIntent);
    }
}