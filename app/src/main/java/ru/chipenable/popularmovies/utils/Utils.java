package ru.chipenable.popularmovies.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import ru.chipenable.popularmovies.R;

/**
 * Created by Pashgan on 02.10.2015.
 */
public class Utils {

    private static final String TAG = "Utils";

    /*This function is used to calculate image sizes*/
    public static Point getScreenSize(Activity activity){

        /*get full size of the display*/
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(screenSize);
        } else {
            display.getSize(screenSize);
        }

        int displayDiv = activity.getResources().getInteger(R.integer.display_div);
        screenSize.x /= displayDiv;

        /*get height of StatusBar*/
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = (resourceId > 0)? activity.getResources().getDimensionPixelSize(resourceId) : 0;
        Log.d(TAG, "height of status bar: " + Integer.toString(statusBarHeight));

        //get a height of ActionBar
        TypedArray styledAttributes = activity.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        int actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        Log.d(TAG, "height of action bar: " + Integer.toString(actionBarHeight));

        //calculate available screen size
        screenSize.y -= (statusBarHeight + actionBarHeight);
        Log.d(TAG, "display size: " + Integer.toString(screenSize.x) + "x" + Integer.toString(screenSize.y));
        return screenSize;
    }

    public static Uri downloadImage(Context context, String url, String name) {
        try {
            if (!isExternalStorageWritable()) {
                Log.d(TAG, "external storage is not writable");
                return null;
            }

            // Input stream.
            InputStream inputStream;

            // Filename that we're downloading (or opening).
            String filename;

            // Download the contents at the URL, which should
            // reference an image.
            inputStream = (InputStream) new URL(url).getContent();
            filename = name;

            // Decode the InputStream into a Bitmap image.
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Bail out of we get an invalid bitmap.
            if (bitmap == null) {
                return null;
            }
            else {
                // Create an output file and save the image into it.
                return Utils.createDirectoryAndSaveFile(context, bitmap, filename);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while downloading. Returning null.");
            Log.e(TAG, e.toString());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Decode an InputStream into a Bitmap and store it in a file on
     * the device.
     *
     * @param context  the context in which to write the file.
     * @param image    the image to save
     * @param fileName name of the file.
     * @return the absolute path to the downloaded image file on the file system.
     */
    private static Uri createDirectoryAndSaveFile(Context context,
                                                  Bitmap image,
                                                  String fileName) {
        File directory = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DCIM)
                + "/PopularMovies");

        if (!directory.exists()) {
            File newDirectory = new File(directory.getAbsolutePath());
            newDirectory.mkdirs();
        }

        File file = new File(directory, fileName);
        if (file.exists()) {
            file.delete();
        }

        FileOutputStream outputStream = null;
        try  {
            outputStream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            // Indicate a failure.
            return null;
        }
        finally {
           if (outputStream != null) {
               try {
                   outputStream.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
        }

        // Get the absolute path of the image.
        String absolutePathToImage = file.getAbsolutePath();

        // Provide metadata so the downloaded image is viewable in the Gallery.
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, fileName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));
        values.put("_data", absolutePathToImage);

        ContentResolver cr = context.getContentResolver();

        // Store the metadata for the image into the Gallery.
        Uri imageUri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Log.d(TAG, "absolute path to image file is " + absolutePathToImage);

        if (imageUri != null) {
            Log.d(TAG, "image uri: " + imageUri.toString());
        }

        return Uri.parse(absolutePathToImage);
    }


    /**
     * This method checks if we can write image to external storage
     *
     * @return true if an image can be written, and false otherwise
     */
    private static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals
                (Environment.getExternalStorageState());
    }

    public static boolean deleteFile(Context context, String fileName){
        boolean result = false;

        File directory = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DCIM) + "/PopularMovies");

        File file = new File(directory, fileName);
        if (file.exists()) {
            result = file.delete();
        }

        // delete data from the gallery
        ContentResolver cr = context.getContentResolver();
        int rowDeleted = cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.TITLE + "=?", new String[]{fileName});

        Log.d(TAG, "rowDeleted: " + Integer.toString(rowDeleted));
        return result;
    }

}
