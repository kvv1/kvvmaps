package kvv.kvvmap.dlg;

import java.util.Locale;

import kvv.kvvmap.R;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.common.pacemark.PlaceMarks;
import kvv.kvvmap.view.MapView;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

interface LonLatEditor {
	void set(double val);

	double get();
}

class DoubleLonLatEditor extends EditText implements LonLatEditor {
	public DoubleLonLatEditor(Context context, int id) {
		super(context);
		setId(id);
	}

	@Override
	public void set(double val) {
		setText(String.format(Locale.ENGLISH, "%.6f", val));
	}

	@Override
	public double get() {
		return Double.parseDouble(getText().toString());
	}
}

class GMLonLatEditor extends LinearLayout implements LonLatEditor {

	private EditText degrees;
	private EditText minutes;

	public GMLonLatEditor(Context context, int id) {
		super(context);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		setLayoutParams(params);
		degrees = new EditText(context);
		addView(degrees);
		TextView label = new TextView(context);
		label.setText("\u00B0");
		addView(label);
		minutes = new EditText(context);
		addView(minutes);
		label = new TextView(context);
		label.setText("\u0027");
		addView(label);
	}

	@Override
	public void set(double val) {
		String[] ss = CoordinateFormat.formatDM(val);
		degrees.setText(ss[0]);
		minutes.setText(ss[1]);
	}

	@Override
	public double get() {
		return CoordinateFormat.parseDM(degrees.getText().toString(), minutes
				.getText().toString());
	}

}

public class PlaceMarkDlg extends Dialog {

	public enum Type {
		ADD, EDIT
	}

	private LonLatEditor lonEditor;
	private LonLatEditor latEditor;

	public PlaceMarkDlg(final Context context, final MapView view,
			final LocationX pm, final PlaceMarks placemarks,
			final PlaceMarkDlg.Type type) {
		super(context);

		if (type == Type.ADD)
			setTitle("Новая точка");
		else if (type == Type.EDIT)
			setTitle("Редактирование точки");

		setContentView(R.layout.placemark);

		final EditText name = (EditText) findViewById(R.id.Name);

		lonEditor = new DoubleLonLatEditor(context, R.id.lon);
		latEditor = new DoubleLonLatEditor(context, R.id.lat);

		lonEditor.set(pm.getLongitude());
		latEditor.set(pm.getLatitude());

		FrameLayout l = (FrameLayout) findViewById(R.id.lonContainer);
		l.removeAllViews();
		l.addView((View) lonEditor);

		l = (FrameLayout) findViewById(R.id.latContainer);
		l.removeAllViews();
		l.addView((View) latEditor);

		if (pm.name != null)
			name.setText(pm.name);

		Button bOk = (Button) findViewById(R.id.OK);
		bOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view1) {
				LocationX res = edit(context, pm, placemarks, type);
				if (res != null) {
					view.animateTo(res);
					dismiss();
				}
			}
		});

		Button bDel = (Button) findViewById(R.id.Delete);
		bDel.setEnabled(type == Type.EDIT);

		bDel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				AlertDialog dlg = new AlertDialog.Builder(PlaceMarkDlg.this
						.getContext()).create();
				dlg.setTitle("Удалить точку?");
				dlg.setButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						placemarks.remove(pm);
						dismiss();
					}
				});
				dlg.setButton2("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				dlg.show();
			}
		});

		Button bCancel = (Button) findViewById(R.id.Cancel);
		bCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				dismiss();
			}
		});

		final CheckBox bTarget = (CheckBox) findViewById(R.id.target);
		bTarget.setChecked(placemarks.getTarget() == pm);
	}

	private LocationX edit(Context context, LocationX pm,
			final PlaceMarks placemarks, final PlaceMarkDlg.Type type) {

		final EditText name = (EditText) findViewById(R.id.Name);
		// final EditText lon = (EditText) findViewById(R.id.lon);
		// final EditText lat = (EditText) findViewById(R.id.lat);

		String name1 = name.getText().toString();
		if (name1.length() > 0)
			pm.name = name1;
		else
			pm.name = null;

		double lon1 = pm.getLongitude();
		double lat1 = pm.getLatitude();
		try {
			lon1 = lonEditor.get();
			lat1 = latEditor.get();

			// lon1 = Double.parseDouble(lon.getText().toString());
			// lat1 = Double.parseDouble(lat.getText().toString());
		} catch (Exception e) {
			new AlertDialog.Builder(context).setMessage(
					"Ошибка ввода координат").show();
			return null;
		}

		LocationX pm1 = new LocationX(lon1, lat1, pm.getAltitude(),
				pm.getAccuracy(), pm.getSpeed(), pm.getTime());
		pm1.name = pm.name;

		placemarks.replace(pm, pm1);

		boolean target = placemarks.getTarget() == pm1;
		final CheckBox bTarget = (CheckBox) findViewById(R.id.target);
		if (!target && bTarget.isChecked())
			placemarks.setTarget(pm1);
		else if (target && !bTarget.isChecked())
			placemarks.setTarget(null);

		return pm1;
	}
}
