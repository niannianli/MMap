package li.com.mmap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "rowsDatabase";
    static final int DATABASE_VERSION = 1;

    static final String TABLE_NAME = "ROWS";
    static final String UID = "Id";
    static final String NAME = "Name";
    static final String LAT = "Lat";
    static final String LNG = "Lng";
    static final String ADDRESS = "Address";

    private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME
            +"("+UID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +NAME+" VARCHAR(225), "
            +LAT+" VARCHAR(225), "
            +LNG+" VARCHAR(225), "
            +ADDRESS+" VARCHAR(500) )";

    Context context;

    public SQLHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
