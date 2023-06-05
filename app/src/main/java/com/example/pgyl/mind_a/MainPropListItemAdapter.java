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

import static com.example.pgyl.mind_a.Constants.MAX_PEGS;
import static com.example.pgyl.mind_a.MainActivity.pegs;
import static com.example.pgyl.mind_a.StringDBTables.getPaletteColorsAtIndex;
import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;
import static com.example.pgyl.pekislib_a.SymbolButtonView.SymbolButtonViewColorBox;

public class MainPropListItemAdapter extends BaseAdapter {
    public interface onButtonClickListener {
        void onButtonClick(int position, int pegIndex);
    }

    public void setOnButtonClick(onButtonClickListener listener) {
        mOnButtonClickListener = listener;
    }

    private onButtonClickListener mOnButtonClickListener;

    //region Variables
    private Context context;
    private ArrayList<PropRecord> propRecords;
    private String[] paletteColors;
    private SymbolButtonViewColorBox symbolButtonViewColorBox;
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
        symbolButtonViewColorBox = new SymbolButtonViewColorBox();
    }

    public void close() {
        mainPropListItemDotMatrixDisplayUpdater.close();
        mainPropListItemDotMatrixDisplayUpdater = null;
        propRecords = null;
        symbolButtonViewColorBox = null;
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

    private void onButtonClick(int position, int pegIndex) {
        if (mOnButtonClickListener != null) {
            mOnButtonClickListener.onButtonClick(position, pegIndex);
        }
    }

    @Override
    public View getView(final int position, View rowView, ViewGroup parent) {   //  Viewholder pattern non utilisé à cause de la custom view DotMatrixDisplayView (ses variables globales ne sont pas récupérées par un getTag())
        LayoutInflater inflater = LayoutInflater.from(context);
        rowView = inflater.inflate(R.layout.mainproplistitem, null);
        setupViewHolder(rowView, position);
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

        MainPropListItemViewHolder viewHolder = (MainPropListItemViewHolder) rowView.getTag();

        PropRecord propRecord = propRecords.get(position);
        int[] comb = propRecord.getComb();
        for (int i = 0; i <= (MAX_PEGS - 1); i = i + 1) {
            if (i <= (pegs - 1)) {
                String color = ((comb[i] != UNDEFINED) ? paletteColors[getPaletteColorsAtIndex(comb[i])] : EMPTY_COLOR);
                symbolButtonViewColorBox.unpressedFrontColor = color;
                symbolButtonViewColorBox.unpressedBackColor = BACK_COLOR_NORMAL;
                symbolButtonViewColorBox.pressedFrontColor = color;
                symbolButtonViewColorBox.pressedBackColor = BACK_COLOR_INVERSE;
                viewHolder.buttonColors[i].setColors(symbolButtonViewColorBox);
            } else {  //  Ne rendre visibles que <pegs> boutons de couleur
                viewHolder.buttonColors[i].setVisibility(View.GONE);
            }
        }
        mainPropListItemDotMatrixDisplayUpdater.displayText(viewHolder.buttonDotMatrixDisplayScore, propRecord);
    }

    public void paintViewAtPegIndex(View rowView, int pegIndex, SymbolButtonViewColorBox symbolButtonViewColorBox) {
        MainPropListItemViewHolder viewHolder = (MainPropListItemViewHolder) rowView.getTag();
        viewHolder.buttonColors[pegIndex].setColors(symbolButtonViewColorBox);
    }

    private void setupViewHolderButtonAttributes() {
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;
        final float STATE_BUTTON_SYMBOL_SIZE_COEFF = 0.75f;   //  Pour que le symbole ne frôle pas les bords de sa View

        for (int i = 0; i <= (MAX_PEGS - 1); i = i + 1) {
            viewHolder.buttonColors[i].setSVGImageResource(R.raw.disk);
            viewHolder.buttonColors[i].setSymbolSizeCoeff(STATE_BUTTON_SYMBOL_SIZE_COEFF);
            viewHolder.buttonColors[i].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
        }
    }

    private void setupViewHolderDotMatrixDisplayAttributes() {
        mainPropListItemDotMatrixDisplayUpdater.setupDimensions(viewHolder.buttonDotMatrixDisplayScore);
        mainPropListItemDotMatrixDisplayUpdater.setupBackColor(viewHolder.buttonDotMatrixDisplayScore);
    }

    private void setupViewHolder(View rowView, int position) {
        final String BUTTON_XML_NAME_PREFIX = "BTN_ITEM_COMB_";
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        viewHolder = new MainPropListItemViewHolder();

        viewHolder.buttonColors = new SymbolButtonView[MAX_PEGS];
        Class rid = R.id.class;
        for (int i = 0; i <= (MAX_PEGS - 1); i = i + 1) {
            try {
                viewHolder.buttonColors[i] = rowView.findViewById(rid.getField(BUTTON_XML_NAME_PREFIX + i).getInt(rid));
                viewHolder.buttonColors[i].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
                final int pegIndex = i;
                viewHolder.buttonColors[i].setCustomOnClickListener(new SymbolButtonView.onCustomClickListener() {
                    @Override
                    public void onCustomClick() {
                        onButtonClick(position, pegIndex);
                    }
                });
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
