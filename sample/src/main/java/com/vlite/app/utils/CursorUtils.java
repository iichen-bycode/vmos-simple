package com.vlite.app.utils;

import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONObject;

public class CursorUtils {
    public static JSONObject toJSONObject(Cursor cursor) {
        JSONObject respItem = new JSONObject();
        // 赋值每列
        final String[] columnNames = cursor.getColumnNames();
        for (String columnName : columnNames) {
            final int columnIndex = cursor.getColumnIndex(columnName);
            if (columnIndex != -1) {
                final int type = cursor.getType(columnIndex);
                try {
                    if (Cursor.FIELD_TYPE_STRING == type) {
                        respItem.putOpt(columnName, cursor.getString(columnIndex));
                    } else if (Cursor.FIELD_TYPE_INTEGER == type) {
                        respItem.putOpt(columnName, cursor.getLong(columnIndex));
                    } else if (Cursor.FIELD_TYPE_FLOAT == type) {
                        respItem.putOpt(columnName, cursor.getFloat(columnIndex));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return respItem;
    }

    public static JSONArray toJsonArray(Cursor cursor) {
        try {
            if (cursor != null) {
                JSONArray jsonArray = new JSONArray();
                while (cursor.moveToNext()) {
                    jsonArray.put(toJSONObject(cursor));
                }
                return jsonArray;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
