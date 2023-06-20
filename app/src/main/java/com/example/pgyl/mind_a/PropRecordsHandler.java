package com.example.pgyl.mind_a;

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
    private StringDB stringDB;
    private ArrayList<PropRecord> propRecords;
    private PropRecord currentPropRecord;
    private PropRecord secrPropRecord;
    //endregion

    public PropRecordsHandler(StringDB stringDB) {
        this.stringDB = stringDB;
        propRecords = propRowsToPropRecords(getDBProps(stringDB));   //  Créer propRecords via la DB
        init();
    }

    private void init() {
        setupCurrentAndSecrPropRecords();
    }

    public void saveAndClose() {
        propRecords.add(currentPropRecord);   //  Réintégrer currentPropRecord et secrPropRecord dans propRecords avant de sauver en DB
        propRecords.add(secrPropRecord);
        saveDBProps(stringDB, propRecordsToPropRows(propRecords));
        stringDB = null;
        clearPropRecords();
        propRecords = null;
    }

    public void clearPropRecords() {
        currentPropRecord = null;
        secrPropRecord = null;
        propRecords.clear();
    }

    public ArrayList<PropRecord> getPropRecords() {   //  PropRecords sans currentPropRecord ni secrPropRecord
        return propRecords;
    }

    public int getPropRecordsCount() {   //  PropRecords sans currentPropRecord ni secrPropRecord
        return propRecords.size();
    }

    public void addPropRecord(PropRecord propRecord) {
        propRecords.add(propRecord);
    }

    public PropRecord getCurrentPropRecord() {
        return currentPropRecord;
    }

    public PropRecord getSecrPropRecord() {
        return secrPropRecord;
    }

    public PropRecord createPropRecordWithNewId() {
        return createPropRecordWithId(getPropRecordMaxId() + 1);
    }

    public void removePropRecordAtMaxId() {
        removePropRecordAtId(getPropRecordMaxId());
    }

    public int getPropRecordMaxId() {   //  Hors currentPropRecord et scrPropRecord
        int maxId = SECR_PROP_ID;   //  Pour ne pas attribuer des id déjà attribués à currentPropRecord (0) et secrPropRecord (1)
        if (!propRecords.isEmpty()) {
            for (int i = 0; i <= (propRecords.size() - 1); i = i + 1) {
                if (propRecords.get(i).getId() > maxId) {
                    maxId = propRecords.get(i).getId();
                }
            }
        }
        return maxId;
    }

    public PropRecord getPropRecordAtIndex(int index) {
        return propRecords.get(index);
    }

    private PropRecord getPropRecordAtId(int id) {
        PropRecord propRecord = null;
        int index = getPropRecordIndexOfId(id);
        if (index != UNDEFINED) {
            propRecord = propRecords.get(index);
        }
        return propRecord;
    }

    private PropRecord createPropRecordWithId(int id) {
        PropRecord propRecord = new PropRecord();
        propRecord.setId(id);
        propRecord.resetComb();
        propRecord.resetScore();
        return propRecord;
    }

    private void removePropRecordAtId(int id) {
        int index = getPropRecordIndexOfId(id);
        if (index != UNDEFINED) {
            propRecords.remove(index);
        }
    }

    private int getPropRecordIndexOfId(int id) {
        int index = UNDEFINED;
        if (!propRecords.isEmpty()) {
            for (int i = 0; i <= (propRecords.size() - 1); i = i + 1) {
                if (propRecords.get(i).getId() == id) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public void sortPropRecords() {
        if (!propRecords.isEmpty()) {
            if (propRecords.size() >= 2) {
                Collections.sort(propRecords, new Comparator<PropRecord>() {
                    public int compare(PropRecord propRecord1, PropRecord propRecord2) {
                        int idProp1 = propRecord1.getId();
                        int idProp2 = propRecord2.getId();
                        int sortResult = ((idProp1 == idProp2) ? 0 : ((idProp1 < idProp2) ? 1 : -1));   //  Tri par n° id DESC
                        return sortResult;
                    }
                });
            }
        }
    }

    private PropRecord copyPropRecord(int id) {   //  Crée une vraie copie (pas uniquement un accès à l'original)
        PropRecord propRecordSrc = getPropRecordAtId(id);
        PropRecord propRecordDest = null;
        if (propRecordSrc != null) {
            propRecordDest = new PropRecord();
            propRecordDest.setId(id);   //  Copier le contenu de propRecordSrc dans propRecordDest
            propRecordDest.setComb(propRecordSrc.getComb());
            propRecordDest.setScore(propRecordSrc.getScore());
        }
        return propRecordDest;
    }

    public void setupCurrentAndSecrPropRecords() {
        if (getPropRecordIndexOfId(CURRENT_PROP_ID) == UNDEFINED) {   //  N'existe pas
            currentPropRecord = createPropRecordWithId(CURRENT_PROP_ID);
        } else {    //  Existe déjà
            currentPropRecord = copyPropRecord(CURRENT_PROP_ID);
            removePropRecordAtId(CURRENT_PROP_ID);   //  Retirer currentPropRecord de propRecords
        }

        if (getPropRecordIndexOfId(SECR_PROP_ID) == UNDEFINED) {   //  N'existe pas
            secrPropRecord = createPropRecordWithId(SECR_PROP_ID);
            secrPropRecord.setRandomComb();
        } else {    //  Existe déjà
            secrPropRecord = copyPropRecord(SECR_PROP_ID);
            removePropRecordAtId(SECR_PROP_ID);   //  Retirer secrPropRecord de propRecords
        }
    }
}