package com.example.pgyl.mind_a;

import android.content.Context;

import com.example.pgyl.pekislib_a.StringDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.example.pgyl.mind_a.StringDBTables.CURRENT_PROP_ID;
import static com.example.pgyl.mind_a.StringDBTables.SECR_PROP_ID;
import static com.example.pgyl.mind_a.StringDBTables.propRecordsToPropRows;
import static com.example.pgyl.mind_a.StringDBTables.propRowsToPropRecords;
import static com.example.pgyl.mind_a.StringDBUtils.getDBProps;
import static com.example.pgyl.mind_a.StringDBUtils.saveDBProps;
import static com.example.pgyl.pekislib_a.Constants.UNDEFINED;

public class PropRecordsHandler {

    //region Variables
    private Context context;
    private ArrayList<PropRecord> propRecords;
    private PropRecord currentPropRecord;
    private PropRecord secrPropRecord;
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
        propRecords.add(currentPropRecord);   //  Réintégrer currentPropRecord et secrPropRecord dans propRecords avant de sauver en DB
        propRecords.add(secrPropRecord);
        saveDBProps(stringDB, propRecordsToPropRows(propRecords));
        stringDB = null;
        removePropRecords();
        propRecords = null;
        currentPropRecord = null;
        context = null;
    }

    public void removePropRecords() {   // Sauf currentPropRecord
        propRecords.clear();
    }

    public ArrayList<PropRecord> getPropRecords() {
        return propRecords;
    }   //  PropRecords sans currentPropRecord ni secrPropRecord

    public PropRecord getCurrentPropRecord() {
        return currentPropRecord;
    }

    public PropRecord getSecrPropRecord() {
        return secrPropRecord;
    }

    public PropRecord createPropRecordWithId(int id) {
        PropRecord propRecord = new PropRecord();
        propRecord.setId(id);
        propRecords.add(propRecord);
        return propRecord;
    }

    public void removePropRecordAtId(int id) {
        propRecords.remove(getPropRecordIndexOfId(id));
    }

    public int getPropRecordIndexOfId(int id) {
        int index = UNDEFINED;

        for (int i = 0; i <= (propRecords.size() - 1); i = i + 1) {
            if (propRecords.get(i).getId() == id) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int getPropRecordMaxId() {   //  Hors currentPropRecord et scrPropRecord
        int maxId = SECR_PROP_ID;   //  Pour ne pas attribuer des id déjà attribués à currentPropRecord et secrPropRecord
        if (!propRecords.isEmpty()) {
            for (int i = 0; i <= (propRecords.size() - 1); i = i + 1) {
                if (propRecords.get(i).getId() > maxId) {
                    maxId = propRecords.get(i).getId();
                }
            }
        }
        return maxId;
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

    private PropRecord extractPropRecordAtId(int id) {
        PropRecord propRecordSrc = propRecords.get(getPropRecordIndexOfId(id));
        PropRecord propRecordDest = new PropRecord();
        propRecordDest.setId(id);   //  Copier propRecordSrc dans propRecordDest
        propRecordDest.setComb(propRecordSrc.getComb());
        propRecordDest.setScore(propRecordSrc.getScore());
        propRecords.remove(getPropRecordIndexOfId(id));   //  Retirer le PropRecord de PropRecords
        return propRecordDest;
    }

    private void setupPropRecords() {
        propRecords = propRowsToPropRecords(getDBProps(stringDB));
        currentPropRecord = extractPropRecordAtId(CURRENT_PROP_ID);   //  Retirer currentPropRecord et secrPropRecord de propRecords
        secrPropRecord = extractPropRecordAtId(SECR_PROP_ID);
    }
}
