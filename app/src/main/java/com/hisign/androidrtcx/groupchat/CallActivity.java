package com.hisign.androidrtcx.groupchat;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hisign.androidrtcx.R;
import com.hisign.androidrtcx.common.EventBusManager;
import com.hisign.broadcastx.pj.Stuff;
import com.hisign.rtcx.AppRTCAudioManager;
import com.hisign.rtcx.client.CallClient;
import com.hisign.rtcx.client.RtcClient;
import com.hisign.rtcx.exception.RTCXException;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.SurfaceViewRenderer;

import java.util.Set;

import static com.hisign.androidrtcx.groupchat.Constant.CALLREQUEST;
import static com.hisign.broadcastx.pj.StuffEvent.HANGUP_EVENT;

public class CallActivity extends Activity implements RtcClient.CallBack {
    private static final String TAG = CallActivity.class.getSimpleName();
    private CallClient callClient;
    private Toast logToast;

    public static void startAction(Activity activity, String roomId) {
        Intent intent = new Intent(activity, CallActivity.class);
        intent.putExtra("roomId", roomId);
        activity.startActivityForResult(intent, CALLREQUEST);
    }

    private boolean swappedFeeds = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(final Stuff stuff) throws InterruptedException {
        switch (stuff.getEventName()) {
            case HANGUP_EVENT:
                RtcClient.release();
                setResult(RESULT_OK);
                finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        EventBusManager.getEventBus(HANGUP_EVENT.getEventName()).register(this);
        Intent intent = this.getIntent();
        final String roomId = intent.getStringExtra("roomId");
        SurfaceViewRenderer fullscreenView = findViewById(R.id.fullscreen_video_view);
        SurfaceViewRenderer smallscreenView = findViewById(R.id.small_video_view);
        smallscreenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callClient.setSwappedFeeds(swappedFeeds);
                swappedFeeds = !swappedFeeds;
            }
        });
        callClient = RtcClient.call(roomId, CallActivity.this, fullscreenView, smallscreenView);
        callClient.toggleMic();
        CallFragment callFragment = new CallFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.call_fragment_container, callFragment);
        ft.commit();
    }

    @Override
    protected void onDestroy() {
        RtcClient.release();
        super.onDestroy();
        EventBusManager.getEventBus(HANGUP_EVENT.getEventName()).unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        callClient.stopCall();
    }

    @Override
    protected void onStart() {
        super.onStart();
        callClient.call();
    }

    public CallClient getCallClient() {
        return callClient;
    }

    @Override
    public void onCallMsg(Object msg) {
        if (msg.getClass() == long.class) {
            long delayMs = (long) msg;
            if (delayMs > 5000) {
                if (logToast != null) {
                    logToast.cancel();
                }
                logToast = Toast.makeText(this, "网络延时超过五秒：" + delayMs, Toast.LENGTH_SHORT);
                logToast.show();
            }
        }
    }

    @Override
    public void onCallError(RTCXException error) {
        Log.e(TAG, error.getMessage());
        setResult(RESULT_CANCELED, new Intent(error.getMessage()));
        finish();
    }

    @Override
    public void onCallConnected(final SurfaceViewRenderer localRenderer) {

    }

    @Override
    public void onCallJoin(final SurfaceViewRenderer remoteRenderer) {

    }

    @Override
    public void onJoinCallBye(String clientId) {
        setResult(RESULT_OK, new Intent("join call say bye!"));
        finish();
    }

    @Override
    public void onAudioDevicesChanged(AppRTCAudioManager.AudioDevice device, Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.i(TAG, "audio changed");
    }
}
