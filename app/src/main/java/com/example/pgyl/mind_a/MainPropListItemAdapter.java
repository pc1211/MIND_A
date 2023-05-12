package com.example.pgyl.mind_a;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.pgyl.pekislib_a.SymbolButtonView;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.pgyl.mind_a.MainActivity.pegs;
import static com.example.pgyl.mind_a.PropRecord.COLOR_NUM_EMPTY;
import static com.example.pgyl.mind_a.StringDBTables.getPaletteColorsAtIndex;
import static com.example.pgyl.mind_a.StringDBTables.getPegsCount;

public class MainPropListItemAdapter extends BaseAdapter {
    //region Variables
    private Context context;
    private ArrayList<PropRecord> propRecords;
    private String[] paletteColors;
    private MainPropListItemViewHolder viewHolder;
    private MainPropListItemDotMatrixDisplayUpdater mainPropListItemDotMatrixDisplayUpdater;
    //endregion

    public MainPropListItemAdapter(Context context, String[] paletteColors) {
        super();

        this.context = context;
        this.paletteColors = paletteColors;
        init();
    }

    private void init() {
        propRecords = null;
        setupMainPropListItemDotMatrixDisplayUpdater();
    }

    public void close() {
        mainPropListItemDotMatrixDisplayUpdater.close();
        mainPropListItemDotMatrixDisplayUpdater = null;
        propRecords = null;
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
        setupViewHolder(rowView);
        rowView.setTag(viewHolder);

        setupViewHolderButtonAttributes();
        setupViewHolderDotMatrixDisplayAttributes();
        paintView(rowView, position);
        return rowView;
    }

    public void paintView(View rowView, int position) {    //  Décoration proprement dite du getView
        final String BACK_COLOR_NORMAL = "000000";
        final String BACK_COLOR_INVERSE = "FFFFFF";
        final String EMPTY_COLOR = "808080";

        int pos = position;
        MainPropListItemViewHolder viewHolder = (MainPropListItemViewHolder) rowView.getTag();

        int[] comb = propRecords.get(pos).getComb();
        for (int i = 0; i <= (getPegsCount() - 1); i = i + 1) {
            if (i <= (pegs - 1)) {
                String color = ((comb[i] != COLOR_NUM_EMPTY) ? paletteColors[getPaletteColorsAtIndex(comb[i])] : EMPTY_COLOR);
                viewHolder.buttonColors[i].setColors(color, BACK_COLOR_NORMAL, color, BACK_COLOR_INVERSE);
            } else {  //  Ne rendre visibles que <pegs> boutons de couleur
                viewHolder.buttonColors[i].setVisibility(View.GONE);
            }
        }

        mainPropListItemDotMatrixDisplayUpdater.displayText(viewHolder.buttonDotMatrixDisplayScore, propRecords.get(pos));
    }

    private void setupViewHolderButtonAttributes() {
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;
        final float STATE_BUTTON_SYMBOL_SIZE_COEFF = 0.75f;   //  Pour que le symbole ne frôle pas les bords de sa View

        for (int i = 0; i <= (getPegsCount() - 1); i = i + 1) {
            viewHolder.buttonColors[i].setSVGImageResource(R.raw.disk);
            viewHolder.buttonColors[i].setSymbolSizeCoeff(STATE_BUTTON_SYMBOL_SIZE_COEFF);
            viewHolder.buttonColors[i].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
        }
    }

    private void setupViewHolderDotMatrixDisplayAttributes() {
        mainPropListItemDotMatrixDisplayUpdater.setupDimensions(viewHolder.buttonDotMatrixDisplayScore);
        mainPropListItemDotMatrixDisplayUpdater.setupBackColor(viewHolder.buttonDotMatrixDisplayScore);
    }

    private void setupViewHolder(View rowView) {
        final String BUTTON_XML_NAME_PREFIX = "BTN_ITEM_COMB_";
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        viewHolder = new MainPropListItemViewHolder();

        viewHolder.buttonColors = new SymbolButtonView[getPegsCount()];
        Class rid = R.id.class;
        for (int i = 0; i <= (getPegsCount() - 1); i = i + 1) {
            try {
                viewHolder.buttonColors[i] = rowView.findViewById(rid.getField(BUTTON_XML_NAME_PREFIX + i).getInt(rid));
                viewHolder.buttonColors[i].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        viewHolder.buttonDotMatrixDisplayScore = rowView.findViewById(R.id.BTN_ITEM_DOT_MATRIX_SCORE);
    }

    private void setupMainPropListItemDotMatrixDisplayUpdater() {
        mainPropListItemDotMatrixDisplayUpdater = new MainPropListItemDotMatrixDisplayUpdater();
    }

}
