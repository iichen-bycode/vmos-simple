package com.vlite.app.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.vlite.app.R;


public abstract class DialogAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected Dialog mProgressDialog;
    private TextView mMessageView;
    private Context context;

    public DialogAsyncTask(Context context) {
        this.context = context;
    }

    protected void showProgressDialog(String message) {
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_material_loading, null);
        mMessageView = view.findViewById(android.R.id.message);
        mProgressDialog = new AlertDialog.Builder(context, R.style.Theme_Vmos_Dialog)
                .setView(view)
                .setCancelable(false)
                .show();
        updateProgressDialog(message);
    }

    protected void updateProgressDialog(String message) {
        if (!TextUtils.isEmpty(message)) mMessageView.setText(message);
    }

    @Override
    protected void onPostExecute(Result result) {
        mProgressDialog.cancel();
    }
}
