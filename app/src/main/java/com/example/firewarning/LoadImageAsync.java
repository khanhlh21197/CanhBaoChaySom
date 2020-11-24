package com.example.firewarning;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

public class LoadImageAsync extends AsyncTask<String, Void, Bitmap> {
    OnImageSuccess onImageSuccess;

    public void setOnImageSuccess(OnImageSuccess onImageSuccess) {
        this.onImageSuccess = onImageSuccess;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        onImageSuccess.onImageSuccess(bitmap);
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        Bitmap bitmap = null;
        bitmap = base64ToBitmap(strings[0]);
        return bitmap;
    }

    private Bitmap base64ToBitmap(String base64) {
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }
}
