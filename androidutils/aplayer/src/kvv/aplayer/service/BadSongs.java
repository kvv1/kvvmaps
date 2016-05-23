package kvv.aplayer.service;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BadSongs {
	private final SQLiteOpenHelper db;
	private static List<String> badSongs;

	public List<String> getBadSongs() {
		return new ArrayList<String>(badSongs);
	}
	
	public BadSongs(Context context) {
		db = new SQLiteOpenHelper(context, "db", null, 1) {

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion,
					int newVersion) {
				db.execSQL("DROP TABLE IF EXISTS badsongs");
				onCreate(db);
			}

			@Override
			public void onCreate(SQLiteDatabase db) {
				db.execSQL("create table badsongs (id integer primary key, path text)");
			}
		};

		badSongs = _getAllBadSongs();
	}

	private List<String> _getAllBadSongs() {
		List<String> array_list = new ArrayList<String>();

		SQLiteDatabase db = this.db.getReadableDatabase();
		Cursor res = db.rawQuery("select * from badsongs", null);
		res.moveToFirst();

		while (res.isAfterLast() == false) {
			array_list.add(res.getString(res.getColumnIndex("path")));
			res.moveToNext();
		}
		return array_list;
	}

	public void addBadSong(String path) {
		SQLiteDatabase db = this.db.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("path", path);
		db.insert("badsongs", null, contentValues);
		badSongs = _getAllBadSongs();
	}

	public void delBadSong(String path) {
		SQLiteDatabase db = this.db.getWritableDatabase();
		db.delete("badsongs", "path = ? ", new String[] { path });
		badSongs = _getAllBadSongs();
	}
}
