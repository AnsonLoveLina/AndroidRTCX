package com.hisign.androidrtcx.groupchat;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.hisign.androidrtcx.R;
import com.hisign.rtcx.AppRTCAudioManager;
import com.hisign.rtcx.client.CallClient;
import com.hisign.rtcx.client.RtcClient;

import org.webrtc.SurfaceViewRenderer;

import java.util.Set;

public class CallActivity extends Activity implements RtcClient.CallBack {
    private static final String TAG = CallActivity.class.getSimpleName();
    private FrameLayout surfaceViewContainer;
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
        SurfaceViewRenderer fullscreenView = findViewById(R.id.fullscreen_video_view);
        SurfaceViewRenderer pipscreenView = findViewById(R.id.pip_video_view);
        callClient = RtcClient.call(roomId, CallActivity.this, fullscreenView, pipscreenView);
        CallFragment callFragment = new CallFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.call_fragment_container, callFragment);
        ft.commit();
    }

    @Override
    protected void onDestroy() {
        RtcClient.release(callClient.getRoomId());
        super.onDestroy();
    }

    public CallClient getCallClient() {
        return callClient;
    }

    @Override
    public void onCallError(String error) {
        Log.e(TAG, error);
    }

    @Override
    public void onCallConnected(final SurfaceViewRenderer localRenderer) {
        localRenderer.setZOrderOnTop(false);
    }

    @Override
    public void onCallJoin(final SurfaceViewRenderer remoteRenderer) {
        remoteRenderer.setZOrderMediaOverlay(true);
    }

    @Override
    public void onAudioDevicesChanged(AppRTCAudioManager.AudioDevice device, Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.i(TAG, "audio changed");
    }
}
