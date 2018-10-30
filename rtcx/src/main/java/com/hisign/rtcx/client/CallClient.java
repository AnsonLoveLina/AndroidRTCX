package com.hisign.rtcx.client;

import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import com.google.common.collect.Lists;
import com.hisign.rtcx.AppRTCAudioManager;
import com.hisign.rtcx.AppRTCClient;
import com.hisign.rtcx.Constant;
import com.hisign.rtcx.PeerConnectionClient;
import com.hisign.rtcx.WebSocketRTCClient;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.ContextUtils;
import org.webrtc.EglBase;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.webrtc.ContextUtils.getApplicationContext;

public class CallClient {
    private static final String TAG = CallClient.class.getSimpleName();
    final Handler mHandler = new Handler();
    private Thread mUiThread;

    private String roomId;
    private ProxyRenderer localProxyRenderer;
    private ProxyRenderer remoteProxyRenderer;
    private RtcClient.CallBack callBack;
    private WebSocketRTCClient appRtcClient;
    private AppRTCClient.RoomConnectionParameters roomConnectionParameters;
    private PeerConnectionClient peerConnectionClient;
    private long callStartedTimeMs = 0;
    private AppRTCAudioManager audioManager;
    private boolean activityRunning;
    //    private boolean screencaptureEnabled;
    private boolean micEnabled = true;
    private AppRTCClient.SignalingParameters signalingParameters;
    private EglBase rootEglBase;
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private boolean iceConnected;
    private boolean isError;
    private SurfaceViewRenderer localRenderer;
    private SurfaceViewRenderer remoteRenderer;
    //    private boolean screencaptureEnabled = false;
    private String videoFileAsCamera = null;

    public String getRoomId() {
        return roomId;
    }

    public static class ProxyRenderer implements VideoRenderer.Callbacks {
        private VideoRenderer.Callbacks target;

        synchronized public void renderFrame(VideoRenderer.I420Frame frame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.");
                VideoRenderer.renderFrameDone(frame);
                return;
            }

            target.renderFrame(frame);
        }

        synchronized public void setTarget(VideoRenderer.Callbacks target) {
            this.target = target;
        }
    }

    CallClient(String roomId, RtcClient.CallBack callBack, SurfaceViewRenderer... screenViews) {
        this.roomId = roomId;
        this.callBack = callBack;
        if (screenViews != null && screenViews.length == 2) {
            init(screenViews[0], screenViews[1]);
        } else {
            init(null, null);
        }
        mUiThread = Thread.currentThread();
    }

    private void init(SurfaceViewRenderer fullscreenView, SurfaceViewRenderer pipscreenView) {

        final CallClient.ProxyRenderer remoteProxyRenderer = new CallClient.ProxyRenderer();
        final CallClient.ProxyRenderer localProxyRenderer = new CallClient.ProxyRenderer();
        this.localProxyRenderer = localProxyRenderer;
        this.remoteProxyRenderer = remoteProxyRenderer;

        rootEglBase = EglBase.create();
        if (fullscreenView != null) {
            localRenderer = fullscreenView;
        } else {
            localRenderer = new SurfaceViewRenderer(getApplicationContext());
        }
        if (pipscreenView != null) {
            remoteRenderer = pipscreenView;
        } else {
            remoteRenderer = new SurfaceViewRenderer(getApplicationContext());
        }

        remoteRenderer.init(rootEglBase.getEglBaseContext(), null);
        remoteRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        localRenderer.init(rootEglBase.getEglBaseContext(), null);
        localRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        localRenderer.setEnableHardwareScaler(true /* enabled */);
        remoteRenderer.setEnableHardwareScaler(true /* enabled */);
        remoteRenderer.setZOrderMediaOverlay(true);

        localProxyRenderer.setTarget(localRenderer);
        remoteProxyRenderer.setTarget(remoteRenderer);
        peerConnectionParameters = RtcClient.getPeerConnectionParameters();
        String roomUrl = RtcClient.getRoomUrl();
        String urlParameters = RtcClient.getUrlParameters();
        appRtcClient = new WebSocketRTCClient(signalingEvents);
        roomConnectionParameters =
                new AppRTCClient.RoomConnectionParameters(roomUrl, roomId, urlParameters);

        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.createPeerConnectionFactory(
                getApplicationContext(), peerConnectionParameters, peerConnectionEvents);
        startCall();
        onCallConnected();
    }

    PeerConnectionClient.PeerConnectionEvents peerConnectionEvents = new PeerConnectionClient.PeerConnectionEvents() {

        //PeerConnectionEvents
        @Override
        public void onLocalDescription(final SessionDescription sdp) {
            final long delta = System.currentTimeMillis() - callStartedTimeMs;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appRtcClient != null) {
                        Log.i(TAG, "Sending " + sdp.type + ", delay=" + delta + "ms");
                        if (signalingParameters.initiator) {
                            appRtcClient.sendOfferSdp(sdp);
                        } else {
                            appRtcClient.sendAnswerSdp(sdp);
                        }
                    }
                    if (peerConnectionParameters.videoMaxBitrate > 0) {
                        Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                        peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                    }
                }
            });
        }

        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appRtcClient != null) {
                        appRtcClient.sendLocalIceCandidate(candidate);
                    }
                }
            });
        }

        @Override
        public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (appRtcClient != null) {
                        appRtcClient.sendLocalIceCandidateRemovals(candidates);
                    }
                }
            });
        }

        @Override
        public void onIceConnected() {
            final long delta = System.currentTimeMillis() - callStartedTimeMs;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "ICE connected, delay=" + delta + "ms");
                    iceConnected = true;
                }
            });
        }

        @Override
        public void onAddRemoteStream() {
            callConnected();
        }

        @Override
        public void onIceDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "ICE disconnected");
                    iceConnected = false;
                    disconnect();
                }
            });
        }

        @Override
        public void onPeerConnectionClosed() {

        }

        @Override
        public void onPeerConnectionStatsReady(final StatsReport[] reports) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isError && iceConnected) {
//          hudFragment.updateEncoderStatistics(reports);
                        for (StatsReport report : reports) {
                            Log.i(TAG, report.toString());
                        }
                    }
                }
            });
        }

        @Override
        public void onPeerConnectionError(String description) {
            onCallError(description);
        }
    };

    AppRTCClient.SignalingEvents signalingEvents = new AppRTCClient.SignalingEvents() {

        //SignalingEvents
        @Override
        public void onConnectedToRoom(final AppRTCClient.SignalingParameters params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onConnectedToRoomInternal(params);
                }
            });
        }

        @Override
        public void onRemoteDescription(final SessionDescription sdp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final long delta = System.currentTimeMillis() - callStartedTimeMs;
                    if (peerConnectionClient == null) {
                        Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                        return;
                    }
                    Log.i(TAG, "Received remote " + sdp.type + ", delay=" + delta + "ms");
                    peerConnectionClient.setRemoteDescription(sdp);
                    if (!signalingParameters.initiator) {
                        Log.i(TAG, "Creating ANSWER...");
                        // Create answer. Answer SDP will be sent to offering client in
                        // PeerConnectionEvents.onLocalDescription event.
                        peerConnectionClient.createAnswer();
                    }
                }
            });
        }

        @Override
        public void onRemoteIceCandidate(final IceCandidate candidate) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (peerConnectionClient == null) {
                        Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                        return;
                    }
                    peerConnectionClient.addRemoteIceCandidate(candidate);
                }
            });
        }

        @Override
        public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (peerConnectionClient == null) {
                        Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                        return;
                    }
                    peerConnectionClient.removeRemoteIceCandidates(candidates);
                }
            });
        }

        @Override
        public void onChannelClose() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Remote end hung up; dropping PeerConnection");
                    disconnect();
                }
            });
        }

        @Override
        public void onChannelError(String description) {
            onCallError(description);
        }
    };

    /**
     * 开启链接
     */
    public void call() {
        this.activityRunning = true;
        // Video is not paused for screencapture. See onPause.
//        if (peerConnectionClient != null && !screencaptureEnabled) {
        if (peerConnectionClient != null) {
            peerConnectionClient.startVideoSource();
        }
    }

    /**
     * 终止本次链接
     */
    public void stopCall() {
        this.activityRunning = false;
        // Video is not paused for screencapture. See onPause.
//        if (peerConnectionClient != null && !screencaptureEnabled) {
        if (peerConnectionClient != null) {
            peerConnectionClient.stopVideoSource();
        }
    }

    private void startCall() {
        if (appRtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }
        callStartedTimeMs = System.currentTimeMillis();

        // Start room connection.
        appRtcClient.connectToRoom(roomConnectionParameters);

        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(getApplicationContext());
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(
                    AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
            }
        });
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
        callBack.onAudioDevicesChanged(device, availableDevices);
    }

    /**
     * 关闭链接释放资源
     */
    public void disconnect() {
        activityRunning = false;
        remoteProxyRenderer.setTarget(null);
        localProxyRenderer.setTarget(null);
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (remoteRenderer != null) {
            remoteRenderer.release();
            remoteRenderer = null;
        }
        if (localRenderer != null) {
            localRenderer.release();
            localRenderer = null;
        }
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        if (rootEglBase.hasSurface()) {
            rootEglBase.release();
        }
    }

//    public void onCallHangUp() {
//        disconnect();
//    }

    /**
     * 转换摄像头
     */
    public void switchCamera() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
        }
    }

    /**
     * 调整摄像头对应输出流参数
     *
     * @param width
     * @param height
     * @param framerate
     */
    public void changeCaptureFormat(int width, int height, int framerate) {
        if (peerConnectionClient != null) {
            peerConnectionClient.changeCaptureFormat(width, height, framerate);
        }
    }

    /**
     * 闭麦
     *
     * @return
     */
    public boolean toggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(micEnabled);
        }
        return micEnabled;
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    private void onConnectedToRoomInternal(final AppRTCClient.SignalingParameters params) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;

        signalingParameters = params;
        Log.i(TAG, "Creating peer connection, delay=" + delta + "ms");
        VideoCapturer videoCapturer = null;
        if (peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createLocalVideoCapturer();
        }
        peerConnectionClient.createPeerConnection(rootEglBase.getEglBaseContext(), localProxyRenderer,
                Lists.<VideoRenderer.Callbacks>newArrayList(remoteProxyRenderer), videoCapturer, signalingParameters);

        if (signalingParameters.initiator) {
            Log.i(TAG, "Creating OFFER...");
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                Log.i(TAG, "Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
    }

    private final void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }

    public void onVideoScalingSwitch(RendererCommon.ScalingType scalingType) {
        localRenderer.setScalingType(scalingType);
    }

    public void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        localProxyRenderer.setTarget(isSwappedFeeds ? localRenderer : remoteRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? remoteRenderer : localRenderer);
        localRenderer.setMirror(isSwappedFeeds);
        remoteRenderer.setMirror(!isSwappedFeeds);
    }

    private VideoCapturer createLocalVideoCapturer() {
        VideoCapturer videoCapturer = null;
        if (videoFileAsCamera != null) {
            try {
                videoCapturer = new FileVideoCapturer(videoFileAsCamera);
            } catch (IOException e) {
                Log.e(TAG, "Failed to open video file for emulated camera");
                return null;
            }
//        } else if (screencaptureEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            return createScreenCapturer();
        } else if (useCamera2()) {
            if (!captureToTexture()) {
                onCallError(Constant.CAMERA2_TEXTURE_ONLY_ERROR);
                return null;
            }

            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(ContextUtils.getApplicationContext()));
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            Log.e(TAG, "Failed to open camera");
            return null;
        }
        return videoCapturer;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(ContextUtils.getApplicationContext());
    }

    private boolean captureToTexture() {
        return RtcClient.isCapturetoTextureEnable();
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private void onCallError(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    isError = true;
                    callBack.onCallError(error);
                }
            }
        });
    }

    private void onCallConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callBack.onCallConnected(localRenderer);
            }
        });
    }

    private void onCallJoin() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callBack.onCallJoin(remoteRenderer);
            }
        });
    }

    // Should be called from UI thread
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, Constant.STAT_CALLBACK_PERIOD);
        onCallJoin();
    }
}
