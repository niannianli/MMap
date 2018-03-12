package li.com.mmap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class SQLiteDatabaseManager {

SQLHelper helper;

public SQLiteDatabaseManager(Context context){
    helper = new SQLHelper(context);
}

public long insertData(String name, String lat, String lng, String address){

  SQLiteDatabase db = helper.getWritableDatabase();

  ContentValues contentValues = new ContentValues();
    contentValues.put(SQLHelper.NAME, name);
    contentValues.put(SQLHelper.LAT, lat);
    contentValues.put(SQLHelper.LNG, lng);
    contentValues.put(SQLHelper.ADDRESS, address);

    long id = db.insertWithOnConflict(SQLHelper.TABLE_NAME, null, contentValues,SQLiteDatabase.CONFLICT_REPLACE);

    ContentValues contentValues1 = new ContentValues();
    contentValues1.put(SQLHelper.NAME, "test");
    contentValues1.put(SQLHelper.LAT, 39.8097343);
    contentValues1.put(SQLHelper.LNG, -98.5556199);
    contentValues1.put(SQLHelper.ADDRESS, "test");

    //insert dumb data for test
    long id1 = db.insertWithOnConflict(SQLHelper.TABLE_NAME, null, contentValues1,SQLiteDatabase.CONFLICT_REPLACE);

    db.close();

    return id1;

}

public List<Row> getData(){

    SQLiteDatabase db = helper.getReadableDatabase();

    Cursor cursor = db.query(SQLHelper.TABLE_NAME, null, null, null, null, null, null);

    List<Row> list = new ArrayList<>();

    while(cursor.moveToNext()){

        int indexName = cursor.getColumnIndex(SQLHelper.NAME);
        int indexLat = cursor.getColumnIndex(SQLHelper.LAT);
        int indexLng = cursor.getColumnIndex(SQLHelper.LNG);
        int indexAddress = cursor.getColumnIndex(SQLHelper.ADDRESS);

        String name = cursor.getString(indexName);
        String lat =  cursor.getString(indexLat);
        String lng = cursor.getString(indexLng);
        String address = cursor.getString(indexAddress);

        LatLng geo = new LatLng(new Double(lat), new Double(lng));

        Row row = new Row(R.raw.food, name, geo, address);

        list.add(row);
    }

    db.close();

    return list;

}

}
