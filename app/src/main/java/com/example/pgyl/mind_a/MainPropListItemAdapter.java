package com.example.pgyl.mind_a;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.pgyl.mind_a.MainActivity.PALETTE_COLORS;
import com.example.pgyl.pekislib_a.StringDB;
import com.example.pgyl.pekislib_a.SymbolButtonView;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.pgyl.mind_a.Constants.MAX_PEGS;
import static com.example.pgyl.mind_a.PropRecord.COLOR_NUM_EMPTY;

public class MainPropListItemAdapter extends BaseAdapter {

    public interface onCheckBoxClickListener {
        void onCheckBoxClick();
    }

    public void setOnItemCheckBoxClick(onCheckBoxClickListener listener) {
        mOnCheckBoxClickListener = listener;
    }

    private onCheckBoxClickListener mOnCheckBoxClickListener;

    public interface onStartStopResetClickListener {
        void onStartStopResetClick(long nowm, long timeAcc);
    }

    public void setOnItemStartStopResetClick(onStartStopResetClickListener listener) {
        mOnStartStopResetClickListener = listener;
    }

    private onStartStopResetClickListener mOnStartStopResetClickListener;

    //region Variables
    private Context context;
    private ArrayList<PropRecord> propRecords;
    private StringDB stringDB;
    private int pegs;
    private boolean showExpirationTime;
    private boolean setClockAppAlarmOnStartTimer;
    private MainPropListItemDotMatrixDisplayUpdater mainPropListItemDotMatrixDisplayUpdater;
    //endregion

    public MainPropListItemAdapter(Context context, StringDB stringDB, int pegs) {
        super();

        this.context = context;
        this.stringDB = stringDB;
        this.pegs = pegs;
        init();
    }

    private void init() {
        mOnCheckBoxClickListener = null;
        propRecords = null;
        setupMainPropListItemDotMatrixDisplayUpdater();
    }

    public void close() {
        mainPropListItemDotMatrixDisplayUpdater.close();
        mainPropListItemDotMatrixDisplayUpdater = null;
        propRecords = null;
        stringDB = null;
        context = null;
    }

    public void setItems(ArrayList<PropRecord> propRecords) {
        this.propRecords = propRecords;
    }

    @Override
    public int getCount() {
        return (propRecords != null) ? propRecords.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View rowView, ViewGroup parent) {   //  Viewholder pattern non utilisé à cause de la custom view DotMatrixDisplayView (ses variables globales ne sont pas récupérées par un getTag())
        LayoutInflater inflater = LayoutInflater.from(context);
        rowView = inflater.inflate(R.layout.mainproplistitem, null);
        MainPropListItemViewHolder viewHolder = buildViewHolder(rowView);
        rowView.setTag(viewHolder);

        setupViewHolder(viewHolder, rowView, position);
        paintView(rowView, position);
        return rowView;
    }

    public void paintView(View rowView, int position) {    //  Décoration proprement dite du getView
        final String EMPTY_COLOR = "C0C0C0";
        final String BACK_COLOR = "707070";

        int pos = position;
        MainPropListItemViewHolder viewHolder = (MainPropListItemViewHolder) rowView.getTag();

        int[] colorNums = propRecords.get(pos).getColorNums();
        for (int i = 0; i <= (MAX_PEGS - 1); i = i + 1) {
            if (i <= (pegs - 1)) {
                String frontColor = ((colorNums[i] == COLOR_NUM_EMPTY) ? EMPTY_COLOR : PALETTE_COLORS.  getByIndex(colorNums[i]).COLOR());
                viewHolder.buttonColors[pos].setColors(frontColor, BACK_COLOR, BACK_COLOR, frontColor);
            } else {  //  Ne rendre visibles que <pegs> boutons de couleur
                viewHolder.buttonColors[pos].setVisibility(View.GONE);
            }
        }

        mainPropListItemDotMatrixDisplayUpdater.displayScore(viewHolder.buttonDotMatrixDisplayScore, propRecords.get(pos));
    }

    private MainPropListItemViewHolder buildViewHolder(View rowView) {
        final String BUTTON_XML_NAME_PREFIX = "BTN_ITEM_COL_";
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        MainPropListItemViewHolder viewHolder = new MainPropListItemViewHolder();

        viewHolder.buttonColors = new SymbolButtonView[MAX_PEGS];
        Class rid = R.id.class;
        for (int i = 0; i <= (MAX_PEGS - 1); i = i + 1) {
            try {
                viewHolder.buttonColors[i] = rowView.findViewById(rid.getField(BUTTON_XML_NAME_PREFIX + i).getInt(rid));
                viewHolder.buttonColors[i].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        viewHolder.buttonDotMatrixDisplayScore = rowView.findViewById(R.id.BTN_ITEM_DOT_MATRIX_SCORE);
        return viewHolder;
    }

    private void setupViewHolder(MainPropListItemViewHolder viewHolder, View rowView, int position) {
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;
        final float STATE_BUTTON_SYMBOL_SIZE_COEFF = 0.75f;   //  Pour que le symbole ne frôle pas les bords de sa View

        for (int i = 0; i <= (pegs - 1); i = i + 1) {
            viewHolder.buttonColors[i].setSVGImageResource(R.raw.disk);
            viewHolder.buttonColors[i].setSymbolSizeCoeff(STATE_BUTTON_SYMBOL_SIZE_COEFF);
            viewHolder.buttonColors[i].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
        }
    }

    private void setupMainPropListItemDotMatrixDisplayUpdater() {
        mainPropListItemDotMatrixDisplayUpdater = new MainPropListItemDotMatrixDisplayUpdater();
    }

}
