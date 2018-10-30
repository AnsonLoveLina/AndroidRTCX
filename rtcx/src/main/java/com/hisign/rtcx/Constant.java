package com.hisign.rtcx;

public class Constant {

    // Peer connection statistics callback period in ms.
    public static final int STAT_CALLBACK_PERIOD = 1000;

    public static final String ROOM_SERVER_URL_DEFAULT = "http://211.157.146.7:8080";

    // List of mandatory application permissions.
    public static final String[] MANDATORY_PERMISSIONS = {"android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET","android.permission.CAMERA","android.permission.WRITE_EXTERNAL_STORAGE"};

    public static final boolean TRACING_DEFAULT = false;
    public static final boolean ENABLE_DATACHANNEL_DEFAULT = false;
    public static final boolean ORDERED_DEFAULT = true;
    public static final int MAX_RETRANSMITS_DEFAULT = -1;
    public static final String DATA_PROTOCOL_DEFAULT = "";
    public static final boolean NEGOTIATED_DEFAULT = false;
    public static final int DATA_ID_DEFAULT = -1;
    public static final boolean VIDEOCALL_DEFAULT = true;
    public static final int STARTAUDIOBITRATEVALUE_DEFAULT = 32;
    public static final String VIDEOCODEC_DEFAULT = "VP8";
    public static final boolean HWCODEC_DEFAULT = true;
    public static final boolean FLEXFEC_DEFAULT = false;
    public static final String AUDIOCODEC_DEFAULT = "OPUS";
    public static final boolean NOAUDIOPROCESSING_DEFAULT = false;
    public static final boolean AECDUMP_DEFAULT = false;
    public static final boolean OPENSLES_DEFAULT = false;
    public static final boolean DISABLE_BUILT_IN_AEC_DEFAULT = false;
    public static final boolean DISABLE_BUILT_IN_AGC_DEFAULT = false;
    public static final boolean DISABLE_BUILT_IN_NS_DEFAULT = false;
    public static final boolean ENABLE_LEVEL_CONTROL_DEFAULT = false;
    public static final boolean DISABLE_WEBRTC_AGC_DEFAULT = false;
    public static final boolean CAPTURETOTEXTURE_DEFAULT = true;
    public static final String SPEAKERPHONE_DEFAULT = "auto";
    public static final String CAMERA2_TEXTURE_ONLY_ERROR = "Camera2 only supports capturing to texture. Either disable Camera2 or enable capturing to texture in the options.";
    public static final int EXTRA_VIDEO_BITRATE_DEFAULT = 97;
}
