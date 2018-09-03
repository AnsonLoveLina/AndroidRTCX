package com.hisign.androidrtcx;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.hisign.rtcx.AppRTCAudioManager;
import com.hisign.rtcx.Constant;
import com.hisign.rtcx.client.CallClient;
import com.hisign.rtcx.client.RtcClient;

import org.webrtc.SurfaceViewRenderer;

import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class RTCActivity extends AppCompatActivity implements RtcClient.CallBack, EasyPermissions.PermissionCallbacks {
    private static final String TAG = RTCActivity.class.getSimpleName();
    private LinearLayout surfaceViewContainer;
    private int PERMISSION_REQUEST_CODE = 13;
    private EditText editText;
    private CallClient callClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtc);
        editText = findViewById(R.id.edit_roomid);
        Button callButton = findViewById(R.id.button_call);
        Button discallButton = findViewById(R.id.button_discall);
        Button switchButton = findViewById(R.id.button_camera_switch);
        Button pauseMicButton = findViewById(R.id.button_toggle_mic);
        LinearLayout contentLayout = findViewById(R.id.rtc_content_layout);
        surfaceViewContainer = contentLayout.findViewById(R.id.surface_container);
        callButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                EasyPermissions.requestPermissions(
                        new PermissionRequest.Builder(RTCActivity.this, PERMISSION_REQUEST_CODE, Constant.MANDATORY_PERMISSIONS)
                                .build());
            }
        });
        discallButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String roomId = editText.getText().toString();
                RtcClient.release(roomId);
                surfaceViewContainer.removeAllViews();
            }
        });
        switchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (callClient!=null){
                    callClient.switchCamera();
                }
            }
        });
        pauseMicButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (callClient!=null){
                    callClient.toggleMic();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        String roomId = editText.getText().toString();
        callClient = RtcClient.call(roomId,  RTCActivity.this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        finish();
    }

    @Override
    public void onCallError(String error) {
        Log.e(TAG, error);
    }

    @Override
    public void onCallConnected(final SurfaceViewRenderer localRenderer) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        localRenderer.setLayoutParams(layoutParams);
        surfaceViewContainer.addView(localRenderer);
    }

    @Override
    public void onCallJoin(final SurfaceViewRenderer remoteRenderer) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        remoteRenderer.setLayoutParams(layoutParams);
        surfaceViewContainer.addView(remoteRenderer);
    }

    @Override
    public void onAudioDevicesChanged(AppRTCAudioManager.AudioDevice device, Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.i(TAG, "audio changed");
    }
}
