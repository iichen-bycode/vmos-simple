package com.vlite.app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

public class UriUtils {

    public static File getFileFromUri(Context context, Uri uri) {
        final String scheme = uri.getScheme();
        if (scheme != null) {
            if (scheme.equals("content")) {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                return getFileFromCursor(cursor);
            } else if (scheme.equals("file")) {
                return new File(uri.getPath());
            }
        }
        return null;
    }

    public static File getFileFromCursor(Cursor cursor) {
        final ContentValues contentValues = new ContentValues();
        try {
            if (cursor != null && cursor.moveToFirst()) {
                final int columnCount = cursor.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    final int type = cursor.getType(i);
                    if (Cursor.FIELD_TYPE_INTEGER == type) {
                        contentValues.put(cursor.getColumnName(i), cursor.getLong(i));
                    } else if (Cursor.FIELD_TYPE_FLOAT == type) {
                        contentValues.put(cursor.getColumnName(i), cursor.getFloat(i));
                    } else if (Cursor.FIELD_TYPE_STRING == type) {
                        contentValues.put(cursor.getColumnName(i), cursor.getString(i));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
        }
        final String data = contentValues.getAsString(MediaStore.Files.FileColumns.DATA);
        if (TextUtils.isEmpty(data)) {

        } else {
            return new File(data);
        }
        return null;
    }

}
