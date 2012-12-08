package kvv.picturelist;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ThumbLayout extends LinearLayout {
	
	private static final int IMG_SIZE = 64;
	private Bitmap bm;
	

	public ThumbLayout(Context context, File file) {
		super(context);
		setOrientation(LinearLayout.VERTICAL);

		set(file);
	}

	public void set(File file) {
		removeAllViews();

		if (bm != null)
			bm.recycle();

		String path = file.getAbsolutePath();

		BitmapFactory.Options opts = new Options();
		
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);

		opts.inJustDecodeBounds = false;
		opts.inSampleSize = Math.max(opts.outWidth / IMG_SIZE, opts.outHeight / IMG_SIZE);
		bm = BitmapFactory.decodeFile(path, opts);

		ImageView iv = new ImageView(getContext());
		iv.setImageBitmap(bm);
		addView(iv, new LinearLayout.LayoutParams(IMG_SIZE, IMG_SIZE));

		TextView tv = new TextView(getContext());
		tv.setText(file.getName());
		addView(tv);
	}
}
