package com.cmpt276.parentapp.serializer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Store some helper functions that are useful in dealing with images
 * */
public class ImageOperations {
	//Encodes bitmap into a String
	public static String encodeBitmap(Bitmap image) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
		byte[] bytes = outputStream.toByteArray();
		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

	//Decodes String into bitmap
	public static Bitmap decodeBitmap(String encodedString) {
		byte[] decodedByte = Base64.decode(encodedString, 0);
		return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
	}
}
