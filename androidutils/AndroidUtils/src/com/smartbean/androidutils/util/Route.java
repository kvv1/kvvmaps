package com.smartbean.androidutils.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;

public class Route {

	public static class LatLng {
		public LatLng(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}

		public double lat;
		public double lon;
	}

	public static class Loc {
		public final String text;
		public final LatLng latLng;

		public Loc(String text, LatLng latLng) {
			this.text = text;
			this.latLng = latLng;
		}
	}

	public static List<Loc> find(String addr) throws JSONException, IOException {
		HttpURLConnection conn = null;
		try {
			String url = "http://maps.googleapis.com/maps/api/geocode/json?language=ru"
					+ "&address="
					+ URLEncoder.encode(addr, "UTF-8")
					+ "&sensor=false";

			conn = (HttpURLConnection) new URL(url).openConnection();

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));

			String json = "";

			String l;
			while ((l = rd.readLine()) != null)
				json += l + "\n";

			System.out.println(json);
			rd.close();

			JSONObject jObj = new JSONObject(json);
			JSONArray results = jObj.getJSONArray("results");

			List<Loc> res = new ArrayList<Route.Loc>();

			for (int i = 0; i < results.length(); i++) {
				JSONObject result = results.getJSONObject(i);
				String formatted_address = result
						.getString("formatted_address");
				JSONObject location = result.getJSONObject("geometry")
						.getJSONObject("location");
				LatLng ll = new LatLng(location.getDouble("lat"),
						location.getDouble("lng"));
				res.add(new Loc(formatted_address, ll));
			}

			return res;
		} finally {
			conn.disconnect();
		}
	}

	public static void find(String addr, final AsyncCallback<List<Loc>> res) {
		new AsyncTask<String, Void, List<Loc>>() {
			private Exception e;

			@Override
			protected List<Loc> doInBackground(String... dest) {
				try {
					return Route.find(dest[0]);
				} catch (Exception e) {
					this.e = e;
					return null;
				}
			}

			@Override
			protected void onPostExecute(List<Loc> result) {
				if (e != null)
					res.onFailure(e);
				else
					res.onSuccess(result);
			}

			@Override
			protected void onCancelled() {
				res.onFailure(e);
			}
		}.execute(addr);
	}

	public static void googleMap(Context context, double lat, double lon) {
		String d = lat + "," + lon;
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse("http://maps.google.com/maps?daddr=" + d + ""));
		context.startActivity(intent);
	}

	public static void yandexNavi(Context context, double lat, double lon) {
		// Создаем интент для построения маршрута
		Intent intent = new Intent(
				"ru.yandex.yandexnavi.action.BUILD_ROUTE_ON_MAP");
		intent.setPackage("ru.yandex.yandexnavi");

		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);

		// Проверяем, установлен ли Яндекс.Навигатор
		if (infos == null || infos.size() == 0) {
			// Если нет - будем открывать страничку Навигатора в Google Play
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri
					.parse("market://details?id=ru.yandex.yandexnavi"));
		} else {
			intent.putExtra("lat_to", lat);
			intent.putExtra("lon_to", lon);
		}

		// Запускаем нужную Activity
		context.startActivity(intent);
	}

	public static void yandexMap(Context context, double lat, double lon) {
		// Создаем интент для построения маршрута
		Intent intent = new Intent(
				"ru.yandex.yandexmaps.action.BUILD_ROUTE_ON_MAP");
		intent.setPackage("ru.yandex.yandexmaps");

		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);

		// Проверяем, установлены ли Яндекс.Карты
		if (infos == null || infos.size() == 0) {
			// Если нет - будем открывать страничку МЯК в Google Play
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri
					.parse("market://details?id=ru.yandex.yandexmaps"));
		} else {
			intent.putExtra("lat_to", lat);
			intent.putExtra("lon_to", lon);
		}

		// Запускаем нужную Activity
		context.startActivity(intent);
	}

}
