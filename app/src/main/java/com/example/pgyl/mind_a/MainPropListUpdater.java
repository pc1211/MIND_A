package com.example.pgyl.mind_a;

import android.widget.ListView;

import com.example.pgyl.pekislib_a.SymbolButtonView.SymbolButtonViewColorBox;

public class MainPropListUpdater {
    //region Variables
    private MainPropListItemAdapter mainPropListItemAdapter;
    private ListView mainPropListView;
    private PropRecordsHandler propRecordsHandler;
    private boolean needScrollBar;
    private Runnable runnableCheckNeedScrollBar;
    //endregion

    public MainPropListUpdater(ListView mainPropListView, PropRecordsHandler propRecordsHandler) {
        super();

        this.mainPropListView = mainPropListView;
        this.propRecordsHandler = propRecordsHandler;
        init();
    }

    private void init() {
        setupRunnables();
        needScrollBar = false;
        setScrollBar(needScrollBar);
        setupMainPropListAdapter();
    }

    public void close() {
        mainPropListView.removeCallbacks(runnableCheckNeedScrollBar);
        runnableCheckNeedScrollBar = null;
        mainPropListItemAdapter = null;
        mainPropListView = null;
        propRecordsHandler = null;
    }

    public void rebuild() {
        propRecordsHandler.sortPropRecords();
        mainPropListItemAdapter.setItems(propRecordsHandler.getPropRecords());
        mainPropListItemAdapter.notifyDataSetChanged();
        mainPropListView.setAdapter(mainPropListItemAdapter);
        mainPropListView.post(runnableCheckNeedScrollBar);
    }

    public void repaint() {
        if (mainPropListView.getChildCount() > 0) {
            int firstVisiblePos = mainPropListView.getFirstVisiblePosition();
            int lastVisiblePos = mainPropListView.getLastVisiblePosition();
            for (int i = firstVisiblePos; i <= lastVisiblePos; i = i + 1) {
                mainPropListItemAdapter.paintView(mainPropListView.getChildAt(i - firstVisiblePos), i);
            }
        }
    }

    public void repaintAtPosAtPegIndex(int position, int pegIndex, SymbolButtonViewColorBox symbolButtonViewColorBox) {
        if (mainPropListView.getChildCount() > 0) {
            int firstVisiblePos = mainPropListView.getFirstVisiblePosition();
            int lastVisiblePos = mainPropListView.getLastVisiblePosition();
            for (int i = firstVisiblePos; i <= lastVisiblePos; i = i + 1) {
                if (i == position) {
                    mainPropListItemAdapter.paintViewAtPegIndex(mainPropListView.getChildAt(i - firstVisiblePos), pegIndex, symbolButtonViewColorBox);
                }
            }
        }
    }

    public void checkNeedScrollBar() {
        if (mainPropListView.getChildCount() > 0) {
            int firstVisiblePos = mainPropListView.getFirstVisiblePosition();
            int lastVisiblePos = mainPropListView.getLastVisiblePosition();
            int firstFullVisiblePos = firstVisiblePos;
            int lastFullVisiblePos = lastVisiblePos;
            if (mainPropListView.getChildAt(0).getTop() < 0) {   //  Le 1er item visible ne l'est que partiellement
                firstFullVisiblePos = firstFullVisiblePos + 1;
            }
            if (mainPropListView.getChildAt(lastVisiblePos - firstVisiblePos).getBottom() > mainPropListView.getHeight()) {   //  Le dernier item visible ne l'est que partiellement
                lastFullVisiblePos = lastFullVisiblePos - 1;
            }
            boolean b = (((firstFullVisiblePos == 0) && (lastFullVisiblePos == (mainPropListView.getCount() - 1))) ? false : true);  // false si toute la liste est entièrement visible
            if (b != needScrollBar) {
                needScrollBar = b;
                setScrollBar(needScrollBar);
            }
        }
    }

    private void setScrollBar(boolean enabled) {
        mainPropListView.setFastScrollEnabled(enabled);
        mainPropListView.setFastScrollAlwaysVisible(enabled);
    }

    private void setupMainPropListAdapter() {
        mainPropListItemAdapter = (MainPropListItemAdapter) mainPropListView.getAdapter();
    }

    private void setupRunnables() {
        runnableCheckNeedScrollBar = new Runnable() {
            @Override
            public void run() {
                checkNeedScrollBar();
            }
        };
    }

}
