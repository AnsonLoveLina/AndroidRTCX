package com.hisign.rtcx.client;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.Maps;
import com.hisign.rtcx.AppRTCAudioManager;
import com.hisign.rtcx.Constant;
import com.hisign.rtcx.PeerConnectionClient;

import org.webrtc.ContextUtils;
import org.webrtc.SurfaceViewRenderer;

import java.util.Map;
import java.util.Set;


public class RtcClient {
    private static final String TAG = RtcClient.class.getSimpleName();
    private static boolean capturetoTextureEnable = true;

    private static PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private static String roomUrl;
    private static String urlParameters;

    public static boolean isCapturetoTextureEnable() {
        return capturetoTextureEnable;
    }

    public static PeerConnectionClient.PeerConnectionParameters getPeerConnectionParameters() {
        return peerConnectionParameters;
    }

    public static String getRoomUrl() {
        return roomUrl;
    }

    public static String getUrlParameters() {
        return urlParameters;
    }

    public static void init(Context context) {
        ContextUtils.initialize(context);
        setDefParams();
    }

    public interface CallBack {
        void onCallError(String error);

        void onCallConnected(SurfaceViewRenderer localRenderer);

        void onCallJoin(SurfaceViewRenderer remoteRenderer);

        void onAudioDevicesChanged(AppRTCAudioManager.AudioDevice device, Set<AppRTCAudioManager.AudioDevice> availableDevices);
    }

    private static Map<String, CallClient> callClientMap = Maps.newHashMap();

    public static CallClient call(String roomId, CallBack callBack, SurfaceViewRenderer... screenViews) {
        CallClient callClient = new CallClient(roomId, callBack, screenViews);
        callClient.call();
        callClientMap.put(roomId, callClient);
        return callClient;
    }

    public static void release(String roomId) {
        CallClient callClient = callClientMap.get(roomId);
        if (callClient != null) {
            callClient.stopCall();
            callClient.disconnect();
            callClient = null;
            releaseRtcClient(roomId);
        }
    }

    public static void releaseRtcClient(String roomId) {
        callClientMap.remove(roomId);
    }

    private static void setDefParams() {
        roomUrl = Constant.ROOM_SERVER_URL_DEFAULT;
        if (roomUrl == null) {
            Log.e(TAG, "Incorrect room ID in conf!");
            throw new RuntimeException("Incorrect room ID in conf");
        }

        boolean tracing = Constant.TRACING_DEFAULT;

        int videoWidth = 0;
        int videoHeight = 0;

        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;
        if (Constant.ENABLE_DATACHANNEL_DEFAULT) {
            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(Constant.ORDERED_DEFAULT,
                    Constant.MAX_RETRANSMITS_DEFAULT,
                    Constant.MAX_RETRANSMITS_DEFAULT, Constant.DATA_PROTOCOL_DEFAULT,
                    Constant.NEGOTIATED_DEFAULT, Constant.DATA_ID_DEFAULT);
        }
        peerConnectionParameters =
                new PeerConnectionClient.PeerConnectionParameters(Constant.VIDEOCALL_DEFAULT,
                        tracing, videoWidth, videoHeight, 0,/*getIntParam(Constant.EXTRA_VIDEO_FPS, 0),*/
                        Constant.STARTAUDIOBITRATEVALUE_DEFAULT, Constant.VIDEOCODEC_DEFAULT,
                        Constant.HWCODEC_DEFAULT,
                        Constant.FLEXFEC_DEFAULT,
                        Constant.STARTAUDIOBITRATEVALUE_DEFAULT, Constant.AUDIOCODEC_DEFAULT,
                        Constant.NOAUDIOPROCESSING_DEFAULT,
                        Constant.AECDUMP_DEFAULT,
                        Constant.OPENSLES_DEFAULT,
                        Constant.DISABLE_BUILT_IN_AEC_DEFAULT,
                        Constant.DISABLE_BUILT_IN_AGC_DEFAULT,
                        Constant.DISABLE_BUILT_IN_NS_DEFAULT,
                        Constant.ENABLE_LEVEL_CONTROL_DEFAULT,
                        Constant.DISABLE_WEBRTC_AGC_DEFAULT, dataChannelParameters);

        capturetoTextureEnable = Constant.CAPTURETOTEXTURE_DEFAULT;

        // Create connection parameters.
        urlParameters = null;//getStrParam(RoomUtil.EXTRA_URLPARAMETERS, null);
    }
}
