package com.hisign.rtcx.client;

public interface OnCallEvents {
    public boolean onToggleMic();
    public void onCaptureFormatChange(int width, int height, int framerate);
    public void onCameraSwitch();
    public void onCallHangUp();
}
