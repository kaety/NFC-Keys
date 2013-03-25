package kandidat.nfc.nfcapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_TABLE_NAME = "LockMappings";
    private static final String DATABASE_NAME = "UnlockApp.db";
    public static final String COLUMN_1 = "lockID";
    public static final String COLUMN_2 = "unlockID";
    //Database creation SQL statement
    private static final String DATABASE_TABLE_CREATE =
                "CREATE TABLE " + DATABASE_TABLE_NAME + " (" +
                COLUMN_1 + " TEXT PRIMARY KEY, " +
                COLUMN_2 + " TEXT);";
    
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_TABLE_CREATE);
    }


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
}
