package com.hisign.androidrtcx;

import android.content.Context;
import android.content.Intent;
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

import static com.hisign.androidrtcx.Constant.CALLREQUEST;

public class CallActivity extends AppCompatActivity implements RtcClient.CallBack {
    private static final String TAG = CallActivity.class.getSimpleName();
    private LinearLayout surfaceViewContainer;
    private int PERMISSION_REQUEST_CODE = 13;
    private EditText editText;
    private CallClient callClient;

    public static void startAction(Context context, String roomId) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra("roomId", roomId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        Intent intent = this.getIntent();
        final String roomId = intent.getStringExtra("roomId");
        editText = findViewById(R.id.edit_roomid);
        Button discallButton = findViewById(R.id.button_discall);
        Button switchButton = findViewById(R.id.button_camera_switch);
        Button pauseMicButton = findViewById(R.id.button_toggle_mic);
        LinearLayout contentLayout = findViewById(R.id.rtc_content_layout);
        surfaceViewContainer = contentLayout.findViewById(R.id.surface_container);
        callClient = RtcClient.call(roomId, CallActivity.this);
        discallButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                RtcClient.release(roomId);
                surfaceViewContainer.removeAllViews();
            }
        });
        switchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (callClient != null) {
                    callClient.switchCamera();
                }
            }
        });
        pauseMicButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (callClient != null) {
                    callClient.toggleMic();
                }
            }
        });
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
