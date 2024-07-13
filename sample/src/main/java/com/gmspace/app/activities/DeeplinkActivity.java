package com.gmspace.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gmspace.sdk.proxy.GmSpaceUtils;

/**
 * @author liyanguo
 * @date 2023/6/6
 * 用于处理Deeplink跳转的分发
 */
public class DeeplinkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Intent intent = getIntent();
            if (intent != null){
                Uri uri =  intent.getData();
                if (intent.getAction().equals(Intent.ACTION_VIEW) && (uri != null && "zoomus".equals(uri.getScheme()))){
                    Intent newIntent = new Intent(intent);
                    newIntent.setComponent(null);
                    newIntent.setPackage("us.zoom.videomeetings");
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    GmSpaceUtils.startActivity(newIntent);
                    finish();
                }
            }
        }catch (Exception e){
        }

    }
}
