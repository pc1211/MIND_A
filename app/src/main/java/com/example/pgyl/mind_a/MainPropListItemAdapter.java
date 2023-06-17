package com.example.pgyl.mind_a;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.pgyl.pekislib_a.ColorUtils.ButtonColorBox;
import com.example.pgyl.pekislib_a.CustomImageButton;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.example.pgyl.mind_a.Constants.MAX_PEGS;
import static com.example.pgyl.mind_a.MainActivity.pegs;
import static com.example.pgyl.mind_a.StringDBTables.getPaletteColorsAtIndex;
import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;

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
    private ButtonColorBox buttonColorBox;
    private MainPropListItemViewHolder viewHolder;
    //endregion

    public MainPropListItemAdapter(Context context, String[] paletteColors) {
        super();

        this.context = context;
        this.paletteColors = paletteColors;
        init();
    }

    private void init() {
        propRecords = null;
        buttonColorBox = new ButtonColorBox();
    }

    public void close() {
        propRecords = null;
        buttonColorBox = null;
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
        setupViewHolder();
        rowView.setTag(viewHolder);

        setupViewHolderButtons(rowView, position);
        setupViewHolderDotMatrixDisplay(rowView);
        setupViewHolderDotMatrixDisplayUpdater();
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
                buttonColorBox.unpressedFrontColor = color;
                buttonColorBox.unpressedBackColor = BACK_COLOR_NORMAL;
                buttonColorBox.pressedFrontColor = color;
                buttonColorBox.pressedBackColor = BACK_COLOR_INVERSE;
                viewHolder.buttonColors[i].setColors(buttonColorBox);
            } else {  //  Ne rendre visibles que <pegs> boutons de couleur
                viewHolder.buttonColors[i].setVisibility(View.GONE);
            }
        }
        viewHolder.dotMatrixDisplayUpdater.displayText(propRecord.getDecoratedScore());
    }

    public void paintViewAtPegIndex(View rowView, int pegIndex, ButtonColorBox buttonColorBox) {
        MainPropListItemViewHolder viewHolder = (MainPropListItemViewHolder) rowView.getTag();
        viewHolder.buttonColors[pegIndex].setColors(buttonColorBox);
    }

    private void setupViewHolderButtons(View rowView, int position) {
        final String BUTTON_XML_NAME_PREFIX = "BTN_ITEM_COMB_";
        final long BUTTON_MIN_CLICK_TIME_INTERVAL_MS = 500;

        viewHolder.buttonColors = new CustomImageButton[MAX_PEGS];
        Class rid = R.id.class;
        for (int i = 0; i <= (MAX_PEGS - 1); i = i + 1) {
            try {
                viewHolder.buttonColors[i] = rowView.findViewById(rid.getField(BUTTON_XML_NAME_PREFIX + i).getInt(rid));
                viewHolder.buttonColors[i].setImageResource(R.drawable.disk);
                viewHolder.buttonColors[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
                viewHolder.buttonColors[i].setAdjustViewBounds(true);
                viewHolder.buttonColors[i].setMinClickTimeInterval(BUTTON_MIN_CLICK_TIME_INTERVAL_MS);
                final int pegIndex = i;
                viewHolder.buttonColors[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onButtonClick(position, pegIndex);
                    }
                });
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void setupViewHolderDotMatrixDisplay(View rowView) {
        viewHolder.dotMatrixDisplayScore = rowView.findViewById(R.id.BTN_ITEM_DOT_MATRIX_SCORE);
    }

    private void setupViewHolderDotMatrixDisplayUpdater() {
        viewHolder.dotMatrixDisplayUpdater = new DotMatrixDisplayUpdater(viewHolder.dotMatrixDisplayScore);
    }

    private void setupViewHolder() {
        viewHolder = new MainPropListItemViewHolder();
    }
}
