# Android RTCX

## DEMO
详见app模块，roomId一致则可以互相通讯，目前房间人数限制只能两人
<pre><code>
        //初始化
        RtcClient.init(getApplicationContext());
        //发起视频聊天，参数2：RtcClient.CallBack
        //CallClient callClient = RtcClient.call(roomId,  RTCActivity.this,fullscreenView, pipscreenView);
        CallClient callClient = RtcClient.call(roomId,  RTCActivity.this);
        //结束视频聊天，并且释放资源
        RtcClient.release(roomId);
</code></pre>

## RtcClient静态方法

- RtcClient.init(context)

    初始化，默认参数
    
- CallClient callClient = RtcClient.call(String roomId, CallBack callBack, fullscreenView, pipscreenView)

    发起进入房间的动作    
    fullscreenView和pipscreenView可以不填，则内部会new一个SurfaceViewRenderer给你，填了则用你的SurfaceViewRenderer
    
- release(String roomId)

    退出房间，并且释放链接。这里可以用
    callClient.stopCall();
    callClient.disconnect();
    callClient = null;
    RtcClient.releaseRtcClient(roomId);

## CallClient公开方法
- call()：

    开启本地视频流输出
    
- stopCall()：

    终止本地视频流输出

- disconnect()

    关闭点对点链接，释放资源

- switchCamera()

    转换本地摄像头（假如需要使用外接设备摄像头，需要扩展SDK）

- toggleMic()

    本地开闭麦，开启/关闭本地声音流输出，返回布尔类型的值（表示这次调用后是否声音流是否输出）

- changeCaptureFormat(int width, int height, int framerate)

    修改摄像头输出视频流的规格和参数，最后一个为FPS参数。注：若无特殊需求慎用


## RtcClient.CallBack

- void onCallError(String error)

    在运行过程中发生严重错误导致运行失败调用
    
- void onCallConnected(SurfaceViewRenderer localRenderer)

    链接成功后，本地视频以及语音的输出SurfaceView

- void onCallJoin(SurfaceViewRenderer remoteRenderer)

    有其他人访问，并且加入成功，远程视频以及语音的输出SurfaceView
    
- void onAudioDevicesChanged(AppRTCAudioManager.AudioDevice device, Set<AppRTCAudioManager.AudioDevice> availableDevices)

    当声音设备更换了，触发（比如，用外接话筒等）
   


参数都在Constant里面，可以调整
    

rtcServer搭建ByDocker：[This link](https://github.com/AnsonLoveLina/rtcServerDockerfile)

rtcServerWeb客户端：[This link](https://211.157.146.7:9000/)
