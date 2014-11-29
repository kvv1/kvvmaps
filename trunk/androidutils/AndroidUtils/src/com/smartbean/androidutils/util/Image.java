package com.smartbean.androidutils.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

public class Image {

	// Loads an image from disk at screen resolution and saves it back out at
	// the same size.
	public static void resizeImage(Context context, String imageFilePath,
			String newImageFilePath, int width, int height) throws IOException {

		// Load up the image's dimensions not the image itself
		Options bmpFactoryOptions = new Options();
		bmpFactoryOptions.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions);
		int heightRatio = (int) Math.ceil(bmpFactoryOptions.outHeight
				/ (float) height);
		int widthRatio = (int) Math.ceil(bmpFactoryOptions.outWidth
				/ (float) width);
		// println("HEIGHTRATIO: " + heightRatio);
		// println("WIDTHRATIO: " + widthRatio);
		// If both of the ratios are greater than 1, one of the sides of //
		// the image is greater than the screen
		if (heightRatio > 1 && widthRatio > 1) {
			if (heightRatio > widthRatio) {
				// Height ratio is larger, scale according to it
				bmpFactoryOptions.inSampleSize = heightRatio;
			} else {
				// Width ratio is larger, scale according to it
				bmpFactoryOptions.inSampleSize = widthRatio;
			}
		}
		// Decode it for real
		bmpFactoryOptions.inJustDecodeBounds = false;
		bmp = BitmapFactory.decodeFile(imageFilePath, bmpFactoryOptions);

		File newImageFile = new File(newImageFilePath);
		Uri newImageUri = Uri.fromFile(newImageFile);
		OutputStream imageFileOS = context.getContentResolver()
				.openOutputStream(newImageUri);

		ExifInterface oldExif = new ExifInterface(imageFilePath);
		int exifOrientation = oldExif.getAttributeInt(
				ExifInterface.TAG_ORIENTATION, -1);

		int degree = 0;
		switch (exifOrientation) {
		case ExifInterface.ORIENTATION_ROTATE_90:
			degree = 90;
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			degree = 180;
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			degree = 270;
			break;
		}

		if (degree != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(degree);
			Bitmap bmp1 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), matrix, true);
			bmp.recycle();
			bmp = bmp1;
		}

		bmp.compress(Bitmap.CompressFormat.JPEG, 90, imageFileOS);

		imageFileOS.close();

		//bmp.recycle();
		
		System.out.println("Resized image, wrote out to: "
				+ newImageFile.getAbsolutePath());
	}

}
