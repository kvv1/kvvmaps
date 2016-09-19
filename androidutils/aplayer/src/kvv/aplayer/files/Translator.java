package kvv.aplayer.files;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

import com.smartbean.androidutils.util.AsyncCallback;

public class Translator {
	private final SQLiteOpenHelper db;

	public Translator(Context context) {
		db = new SQLiteOpenHelper(context, "transl", null, 1) {

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion,
					int newVersion) {
				db.execSQL("DROP TABLE IF EXISTS translations");
				onCreate(db);
			}

			@Override
			public void onCreate(SQLiteDatabase db) {
				db.execSQL("create table translations (textFrom text primary key, textTo text)");
			}
		};
	}

	public void translate(final String text,
			final AsyncCallback<String> callback) {
		SQLiteDatabase db = this.db.getReadableDatabase();
		Cursor res = db.rawQuery("select * from translations where textFrom=?",
				new String[] { text });
		res.moveToFirst();

		if (res.isAfterLast() == false) {
			System.out.println("translation found");
			callback.onSuccess(res.getString(res.getColumnIndex("textTo")));
			return;
		}

		new AsyncTask<String, Void, String>() {

			@Override
			protected String doInBackground(String... params) {
				try {
					return translate1(params[0]);
				} catch (IOException e) {
					return e.getMessage();
				}
			}

			@Override
			protected void onPostExecute(String result) {
				SQLiteDatabase db = Translator.this.db.getWritableDatabase();
				ContentValues contentValues = new ContentValues();
				contentValues.put("textFrom", text);
				contentValues.put("textTo", result);
				db.insert("translations", null, contentValues);
				callback.onSuccess(result);
			}
		}.execute(text);
	}

	private static String translate1(String text) throws IOException {
		String url = "https://translate.yandex.net/api/v1.5/tr.json/translate"
				+ "?key=trnsl.1.1.20160915T110302Z.765d9f266bc455af.3339c06bc26030c406ce0cd7b1978896c3639520"
				+ "&text=" + URLEncoder.encode(text, "utf8") + ""
				+ "&lang=fr-ru";

		HttpURLConnection conn = (HttpURLConnection) new URL(url)
				.openConnection();

		conn.connect();

		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream(), "utf8"));

		char[] buffer = new char[1024];
		int len = in.read(buffer);

		conn.disconnect();

		String json = String.valueOf(buffer, 0, len);

		return substr(json, "\"text\":[\"", "\"]");
	}

	private static String substr(String str, String pattern1, String pattern2) {
		int idx1 = str.indexOf(pattern1);
		if (idx1 == -1)
			return null;

		idx1 += pattern1.length();

		int idx2 = str.indexOf("\"]", idx1);
		if (idx2 == -1)
			return null;

		return str.substring(idx1, idx2);
	}

}
