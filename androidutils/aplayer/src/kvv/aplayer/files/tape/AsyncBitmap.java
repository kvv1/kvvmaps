package kvv.aplayer.files.tape;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public abstract class AsyncBitmap<Params> {

	private Bitmap bmp;

	void create(final int w, final int h, Params params) {
		AsyncTask<Params, Void, Bitmap> asyncTask = new AsyncTask<Params, Void, Bitmap>() {
			@Override
			protected Bitmap doInBackground(Params... params) {
				if (bmp == null || w != bmp.getWidth() || h != bmp.getHeight()) {
					if (bmp != null)
						bmp.recycle();
					bmp = null;
				}
				_draw(bmp, w, h, params[0]);
				return bmp;
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				bmp = ready(result);
			}
		};
		asyncTask.execute(params);
	}

	protected abstract Bitmap ready(Bitmap bmp);

	protected abstract Bitmap _draw(Bitmap bmp, int w, int h, Params params);

}
