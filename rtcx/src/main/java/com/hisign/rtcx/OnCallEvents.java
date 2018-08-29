package com.hisign.rtcx;


import org.webrtc.RendererCommon;

/**
 * Call control interface for container activity.
 */
public interface OnCallEvents {
    void onCallHangUp();
    void onCameraSwitch();
    void onVideoScalingSwitch(RendererCommon.ScalingType scalingType);
    void onCaptureFormatChange(int width, int height, int framerate);
    boolean onToggleMic();
}
