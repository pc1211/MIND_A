package com.example.pgyl.mind_a;

import android.content.Context;

import com.example.pgyl.pekislib_a.StringDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.example.pgyl.mind_a.PropRecord.CURRENT_PROP_ID;
import static com.example.pgyl.mind_a.StringDBTables.propRecordsToPropRows;
import static com.example.pgyl.mind_a.StringDBTables.propRowToPropRecord;
import static com.example.pgyl.mind_a.StringDBTables.propRowsToPropRecords;
import static com.example.pgyl.mind_a.StringDBUtils.getDBPropById;
import static com.example.pgyl.mind_a.StringDBUtils.getDBProps;
import static com.example.pgyl.mind_a.StringDBUtils.saveDBProps;
import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;

public class PropRecordsHandler {

    //region Variables
    private Context context;
    private ArrayList<PropRecord> propRecords;
    private PropRecord currentPropRecord;
    private StringDB stringDB;
    //endregion

    public PropRecordsHandler(Context context, StringDB stringDB) {
        this.context = context;
        this.stringDB = stringDB;
        setupPropRecords();
        init();
    }

    private void init() {
    }

    public void saveAndClose() {
        propRecords.add(currentPropRecord);   //  Réincorporer le currentPropRecord dans propRecords avant de sauver en DB
        saveDBProps(stringDB, propRecordsToPropRows(propRecords));
        stringDB = null;
        removeProps();
        propRecords = null;
        currentPropRecord = null;
        context = null;
    }

    public void removeProps() {   // Sauf currentPropRecord
        propRecords.clear();
    }

    public ArrayList<PropRecord> getProps() {
        return propRecords;
    }   //  Props sans CurrentProp

    public PropRecord getCurrentProp() {
        return currentPropRecord;
    }

    public int createProp() {
        PropRecord propRecord = new PropRecord();
        int idProp = getMaxId() + 1;
        propRecord.setId(idProp);
        propRecords.add(propRecord);
        return propRecord.getId();
    }

    public void remove(int id) {
        propRecords.remove(id);
    }

    public int getMaxId() {
        int ret = UNDEFINED;
        if (!propRecords.isEmpty()) {
            for (int i = 0; i <= (propRecords.size() - 1); i = i + 1) {
                if (propRecords.get(i).getId() > ret) {
                    ret = propRecords.get(i).getId();
                }
            }
        }
        return ret;
    }

    public void sortPropRecords() {
        if (!propRecords.isEmpty()) {
            if (propRecords.size() >= 2) {
                Collections.sort(propRecords, new Comparator<PropRecord>() {
                    public int compare(PropRecord propRecord1, PropRecord propRecord2) {
                        int idProp1 = propRecord1.getId();
                        int idProp2 = propRecord2.getId();
                        int sortResult = ((idProp1 == idProp2) ? 0 : ((idProp1 < idProp2) ? 1 : -1));   //  Tri par n° idProp DESC
                        return sortResult;
                    }
                });
            }
        }
    }

    private void setupPropRecords() {
        int currentPropIndex = 0;

        propRecords = propRowsToPropRecords(getDBProps(stringDB));
        currentPropRecord = propRowToPropRecord(getDBPropById(stringDB, CURRENT_PROP_ID));
        for (int i = 0; i <= (propRecords.size() - 1); i = i + 1) {    //  Identifier dans propRecords le PropRecord correspondant au currentPropRecord
            if (propRecords.get(i).getId() == CURRENT_PROP_ID) {
                currentPropIndex = i;
                break;
            }
        }
        propRecords.remove(currentPropIndex);   //  Retirer le currentPropRecord de PropRecords
    }
}
