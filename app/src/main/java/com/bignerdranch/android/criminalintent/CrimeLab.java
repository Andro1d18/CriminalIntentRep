package com.bignerdranch.android.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import database.CrimeBaseHelper;
import database.CrimeCursorWrapper;
import database.CrimeDBSchema.CrimeTable;

import static android.R.attr.value;

/**
 * Created by andro1d on 21.01.2017.
 */

public class CrimeLab {

    private static CrimeLab sCrimeLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;


    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
//        for (int i = 0; i < 100; i++) {
//            Crime crime = new Crime();
//            crime.setTitle("Crime #" + i);
//            crime.setSolved(i % 2 == 0);
//            mCrimes.add(crime);
//        }
    }

    public List<Crime> getCrimes() {

        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return crimes;
    }


    public Crime getCrime(UUID id) {

        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                new String[]{id.toString()}
        );

        try {
            if (cursor.getCount() ==  0){
                return null;
            }
            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }


    public File getPhotoFile (Crime crime) {
        File externalFileDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (externalFileDir == null){
            return null;
        }
        return new File(externalFileDir, crime.getPhotoFilename());
    }


    public void updateCrime(Crime crime){
        String uuidStrin = crime.getId().toString();
        ContentValues values = getContentValues(crime);
        mDatabase.update(CrimeTable.NAME, values,
                CrimeTable.Cols.UUID + " = ?",
                new String[]{uuidStrin});
    }

    private static ContentValues getContentValues(Crime crime){
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());

        return values;
    }

    public void deleteCrime(Crime deleteCrime){
//        for (Crime crime :
//                mCrimes) {
//            if (crime.getId().equals(deleteCrime.getId())){
//                mCrimes.remove(crime);
//                return;
//            }
//        }
        String uuidString = deleteCrime.getId().toString();
        mDatabase.delete(CrimeTable.NAME, CrimeTable.Cols.UUID + " = ?",
                new String[]{uuidString});
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new CrimeCursorWrapper(cursor);
    }

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null){
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public void addCrime (Crime crime){
        ContentValues values = getContentValues(crime);
        mDatabase.insert(CrimeTable.NAME, null, values);
    }

    //для упражнения в 10 главе
    public int getPositionInCrimes(Crime crime){
//        for (int i = 0; i < mCrimes.size(); i++) {
//            if (crime.getId().equals(mCrimes.get(i).getId())){
//                return i;
//            }
//        }
        return -1;
    }
}
