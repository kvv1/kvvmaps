package kvv.kvvmap.dlg;

import kvv.kvvmap.R;
import kvv.kvvmap.adapter.LocationX;
import kvv.kvvmap.placemark.Path;
import kvv.kvvmap.placemark.Paths;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PathDlg extends Dialog {

	public PathDlg(final Context context, final Path path, final LocationX pm,
			final Paths paths) {
		super(context);

		setTitle("Редактирование пути");

		setContentView(R.layout.path);
		final EditText name = (EditText) findViewById(R.id.Name);

		name.setText(path.getName());

		Button bOk = (Button) findViewById(R.id.OK);
		bOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String name1 = name.getText().toString();
				if (!name1.equals(path.getName())) {
					if (name1.length() > 0) {
						boolean res = paths.rename(path, name1);
						if (!res) {
							new AlertDialog.Builder(context).setMessage(
									"Такое имя уже существует").show();
							return;
						}
					}
				}
				dismiss();
			}
		});

		Button bDel = (Button) findViewById(R.id.Delete);
		bDel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				AlertDialog dlg = new AlertDialog.Builder(PathDlg.this
						.getContext()).create();
				dlg.setTitle("Удалить путь?");
				dlg.setButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						paths.remove(path);
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

		Button bSplit = (Button) findViewById(R.id.Split);
		bSplit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				AlertDialog dlg = new AlertDialog.Builder(PathDlg.this
						.getContext()).create();
				dlg.setTitle("Разделить путь?");
				dlg.setButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						paths.split(path, pm);
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

		Button bHide = (Button) findViewById(R.id.Hide);
		bHide.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				AlertDialog dlg = new AlertDialog.Builder(PathDlg.this
						.getContext()).create();
				dlg.setTitle("Скрыть путь?");
				dlg.setButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						path.setEnabled(false);
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

		Button bCAncel = (Button) findViewById(R.id.Cancel);
		bCAncel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				dismiss();
			}
		});
	}
}
