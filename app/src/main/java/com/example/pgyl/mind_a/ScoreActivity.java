package com.example.pgyl.mind_a;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.pgyl.pekislib_a.HelpActivity;
import com.example.pgyl.pekislib_a.MainActivity;
import com.example.pgyl.pekislib_a.StringDB;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.pgyl.mind_a.Constants.MAX_PEGS;
import static com.example.pgyl.mind_a.Constants.MIND_ACTIVITIES;
import static com.example.pgyl.mind_a.MainActivity.MIND_SHP_KEY_NAMES;
import static com.example.pgyl.mind_a.MainActivity.pegs;
import static com.example.pgyl.pekislib_a.Constants.COLOR_PREFIX;
import static com.example.pgyl.pekislib_a.Constants.PEKISLIB_ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.Constants.SHP_FILE_NAME_SUFFIX;
import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_EXTRA_KEYS;
import static com.example.pgyl.pekislib_a.HelpActivity.HELP_ACTIVITY_TITLE;
import static com.example.pgyl.pekislib_a.R.menu.menu_help_only;
import static com.example.pgyl.pekislib_a.StringDBTables.ACTIVITY_START_STATUS;
import static com.example.pgyl.pekislib_a.StringDBUtils.isColdStartStatusOfActivity;
import static com.example.pgyl.pekislib_a.StringDBUtils.setStartStatusOfActivity;

public class ScoreActivity extends Activity {
    //region Constantes

    private enum COMMANDS {
        CANCEL("Cancel"), OK("OK");

        private String valueText;

        COMMANDS(String valueText) {
            this.valueText = valueText;
        }

        public String TEXT() {
            return valueText;
        }

        public int INDEX() {
            return ordinal();
        }
    }

    //endregion
    //region Variables
    private int blacks;
    private int whites;
    private Button[] buttons;
    private RadioButton[] blackRadioButtons;
    private RadioButton[] whiteRadioButtons;
    private TextView lblDisplay;
    private boolean onStartUp;
    private StringDB stringDB;
    private String shpFileName;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().setTitle("Score");
        setContentView(R.layout.score);
        setupCommandButtons();
        setupBlackRadioButtons();
        setupWhiteRadioButtons();
        setupLblDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();

        savePreferences();
        stringDB.close();
        stringDB = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        onStartUp = true;
        setupStringDB();

        shpFileName = getPackageName() + "." + SHP_FILE_NAME_SUFFIX;   //  getClass().getSimpleName() non inclus car fichier partagé avec MainActivity
        int score = Integer.parseInt(getSHPScore());
        if (score == UNDEFINED) {
            score = 0;
        }
        blacks = score / 10;
        whites = score % 10;
        updateDisplayRadioButtons();
        setupBlackRadioButtonsVisibility();
        setupWhiteRadioButtonsVisibility();
        updateDisplayText();

        if (isColdStartStatusOfActivity(stringDB, MIND_ACTIVITIES.SCORE.toString())) {
            setStartStatusOfActivity(stringDB, MIND_ACTIVITIES.SCORE.toString(), ACTIVITY_START_STATUS.HOT);
        }
        onStartUp = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(menu_help_only, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.HELP) {
            launchHelpActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onCommandButtonClick(COMMANDS command) {
        if (command.equals(COMMANDS.CANCEL)) {
            onButtonClickCancel();
        }
        if (command.equals(COMMANDS.OK)) {
            onButtonClickOK();
        }
    }

    private void onButtonClickOK() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private void onButtonClickCancel() {
        finish();
    }

    private void onBlackRadioChanged(int checkedId) {
        if (!onStartUp) {   //  Ne pas réagir aux manipulations de radioButtons dans le onResume
            RadioButton rb = findViewById(checkedId);
            blacks = Integer.parseInt((String) rb.getText());
            updateDisplayText();
        }
    }

    private void onWhiteRadioChanged(int checkedId) {
        if (!onStartUp) {   //  Ne pas réagir aux manipulations de radioButtons dans le onResume
            RadioButton rb = findViewById(checkedId);
            whites = Integer.parseInt((String) rb.getText());
            updateDisplayText();
        }
    }

    private void updateDisplayText() {
        lblDisplay.setText(blacks + " black" + (blacks == 1 ? "" : "s") + " and " + whites + " white" + (whites == 1 ? "" : "s"));
    }

    private void updateDisplayRadioButtons() {
        blackRadioButtons[blacks].setChecked(true);
        whiteRadioButtons[whites].setChecked(true);
    }

    private void savePreferences() {
        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        SharedPreferences.Editor shpEditor = shp.edit();
        shpEditor.putString(MIND_SHP_KEY_NAMES.SCORE.toString(), String.valueOf(10 * blacks + whites));   // Le score est sauvé et sera repris par MainActivity
        shpEditor.commit();
    }

    private String getSHPScore() {
        final String SCORE_DEFAULT_VALUE = "0";

        SharedPreferences shp = getSharedPreferences(shpFileName, MODE_PRIVATE);
        return shp.getString(MIND_SHP_KEY_NAMES.SCORE.toString(), SCORE_DEFAULT_VALUE);
    }

    private void setupCommandButtons() {
        final String BUTTON_XML_PREFIX = "BTN_";

        buttons = new Button[COMMANDS.values().length];
        Class rid = R.id.class;
        for (COMMANDS command : COMMANDS.values()) {
            try {
                buttons[command.INDEX()] = findViewById(rid.getField(BUTTON_XML_PREFIX + command.toString()).getInt(rid));
                buttons[command.INDEX()].setText(command.TEXT());
                final COMMANDS fcommand = command;
                buttons[command.INDEX()].setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onCommandButtonClick(fcommand);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setupBlackRadioButtons() {
        final String TXT_COLOR = "000000";
        final String BUTTON_XML_PREFIX = "BTN_B_";

        blackRadioButtons = new RadioButton[1 + MAX_PEGS];   //  p.ex si pegs = 4, il faut pouvoir sélectionner 0..4, cad 5 radioButtons
        Class rid = R.id.class;
        for (int i = 0; i <= (blackRadioButtons.length - 1); i = i + 1) {
            try {
                blackRadioButtons[i] = findViewById(rid.getField(BUTTON_XML_PREFIX + i).getInt(rid));
                blackRadioButtons[i].setTextColor(Color.parseColor(COLOR_PREFIX + TXT_COLOR));
                blackRadioButtons[i].setTypeface(null, Typeface.BOLD);
                blackRadioButtons[i].setPadding(20, 0, 0, 0);   //  Décaler le texte un peu vers la droite
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        RadioGroup grpg = findViewById(R.id.GROUP_BLACKS);
        grpg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onBlackRadioChanged(checkedId);
            }
        });
    }

    private void setupBlackRadioButtonsVisibility() {
        for (int i = 0; i <= (blackRadioButtons.length - 1); i = i + 1) {
            blackRadioButtons[i].setVisibility((i < pegs) ? View.VISIBLE : View.GONE);   //  Empêcher d'entrer un nombre de noirs égal à pegs car signifie victoire
        }
    }

    private void setupWhiteRadioButtons() {
        final String TXT_COLOR = "000000";
        final String BUTTON_XML_PREFIX = "BTN_W_";

        whiteRadioButtons = new RadioButton[1 + MAX_PEGS];   //  p.ex si pegs = 4, il faut pouvoir sélectionner 0..4, cad 5 radioButtons
        Class rid = R.id.class;
        for (int i = 0; i <= (whiteRadioButtons.length - 1); i = i + 1) {
            try {
                whiteRadioButtons[i] = findViewById(rid.getField(BUTTON_XML_PREFIX + i).getInt(rid));
                whiteRadioButtons[i].setTextColor(Color.parseColor(COLOR_PREFIX + TXT_COLOR));
                whiteRadioButtons[i].setTypeface(null, Typeface.BOLD);
                whiteRadioButtons[i].setPadding(20, 0, 0, 0);   //  Décaler le texte un peu vers la droite

            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        RadioGroup grpg = findViewById(R.id.GROUP_WHITES);
        grpg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onWhiteRadioChanged(checkedId);
            }
        });
    }

    private void setupWhiteRadioButtonsVisibility() {
        for (int i = 0; i <= (whiteRadioButtons.length - 1); i = i + 1) {   //  Autoriser un nombre de blancs au maximum égal à pegs
            whiteRadioButtons[i].setVisibility((i <= pegs) ? View.VISIBLE : View.GONE);
        }
    }

    private void setupLblDisplay() {
        lblDisplay = findViewById(R.id.LBL_DISPLAY);
    }

    private void setupStringDB() {
        stringDB = new StringDB(this);
        stringDB.open();
    }

    private void launchHelpActivity() {
        Intent callingIntent = new Intent(this, HelpActivity.class);
        callingIntent.putExtra(PEKISLIB_ACTIVITY_EXTRA_KEYS.TITLE.toString(), HELP_ACTIVITY_TITLE);
        callingIntent.putExtra(HELP_ACTIVITY_EXTRA_KEYS.HTML_ID.toString(), R.raw.helpscoreactivity);
        startActivity(callingIntent);
    }
}

