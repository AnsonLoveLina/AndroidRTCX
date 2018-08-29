package com.hisign.androidrtcx;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.hisign.rtcx.AppRTCAudioManager;
import com.hisign.rtcx.client.RtcClient;

import org.webrtc.SurfaceViewRenderer;

import java.util.Set;

public class RTCActivity extends AppCompatActivity implements RtcClient.CallBack {
    private static final String TAG = RTCActivity.class.getSimpleName();
    private LinearLayout surfaceViewContainer;
    private int permissionRequestCode = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtc);
        final EditText editText = findViewById(R.id.edit_roomid);
        Button callButton = findViewById(R.id.button_call);
        Button discallButton = findViewById(R.id.button_discall);
        LinearLayout contentLayout = findViewById(R.id.rtc_content_layout);
        surfaceViewContainer = contentLayout.findViewById(R.id.surface_container);
        callButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String roomId = editText.getText().toString();
                RtcClient.call(roomId, null, RTCActivity.this);
            }
        });
        discallButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String roomId = editText.getText().toString();
                RtcClient.release(roomId);
            }
        });
    }

    @Override
    public void onPermissionNotGranted(String[] requiredPermissions) {
        try {
            ActivityCompat.requestPermissions(this, requiredPermissions, permissionRequestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCallError(String error) {
        Log.e(TAG, error);
    }

    @Override
    public void onCallConnected(final SurfaceViewRenderer localRenderer) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        localRenderer.setLayoutParams(layoutParams);
        surfaceViewContainer.addView(localRenderer);
    }

    @Override
    public void onCallJoin(final SurfaceViewRenderer remoteRenderer) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        remoteRenderer.setLayoutParams(layoutParams);
        surfaceViewContainer.addView(remoteRenderer);
    }

    @Override
    public void onAudioDevicesChanged(AppRTCAudioManager.AudioDevice device, Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.i(TAG, "audio changed");
    }
}
