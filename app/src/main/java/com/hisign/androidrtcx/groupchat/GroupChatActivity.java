package com.hisign.androidrtcx.groupchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.hisign.androidrtcx.R;
import com.hisign.androidrtcx.common.EventBusManager;
import com.hisign.androidrtcx.groupchat.http.IMServiceManager;
import com.hisign.broadcastx.CustomerType;
import com.hisign.broadcastx.socket.SocketIOClient;
import com.hisign.broadcastx.socket.SocketIOClientUtil;
import com.hisign.rtcx.Constant;
import com.hisign.broadcastx.pj.Stuff;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jodd.util.StringUtil;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static com.hisign.broadcastx.pj.StuffEvent.TYPE_HANGUP;
import static com.hisign.broadcastx.pj.StuffEvent.TYPE_TEXT;

public class GroupChatActivity extends Activity {
    private static final String TAG = "GroupChatActivity";

    private List<Stuff> stuffs = new ArrayList<>();

    private EditText inputText;

    private Button send;

    private RecyclerView stuffRecyclerView;

    private StuffAdapter stuffAdapter;

    private String userId;

    private SocketIOClient socketIOClient;

    private void sendStuff(final Stuff stuff) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (SocketIOClient.Customer group : SocketIOClientUtil.getGroups()) {
                    SocketIOClientUtil.getInstance(null).send(TYPE_TEXT.getEventName(), group.getCustomerId(), JSON.toJSONString(stuff));
                }
            }
        }).start();
    }

    private int PERMISSION_REQUEST_CODE = 13;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_chat);

        EasyPermissions.requestPermissions(
                new PermissionRequest.Builder(GroupChatActivity.this, PERMISSION_REQUEST_CODE, Constant.MANDATORY_PERMISSIONS)
                        .build());

        Map<String,String> params = Maps.newHashMap();
        params.put("roomName","110");
        params.put("eventName",TYPE_TEXT.getEventName());
        IMServiceManager.getInstance().stuffHistory(params);

        EventBusManager.getEventBus().register(this);

        socketIOClient = SocketIOClientUtil.getInstance(null);
        Log.i(TAG, "getUserStart");
        userId = SocketIOClientUtil.getUser().getCustomerId();
        //发送给群组
        final String groupName = SocketIOClientUtil.getGroups().iterator().next().getCustomerId();
        setTitle(userId + "的群：" + groupName);
        stuffRecyclerView = (RecyclerView) findViewById(R.id.stuff_recycler_view);
        //设置layoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        stuffRecyclerView.setLayoutManager(layoutManager);
        //设置adapter
        stuffAdapter = new StuffAdapter(stuffs, SocketIOClientUtil.getInstance(null), GroupChatActivity.this);
        stuffRecyclerView.setAdapter(stuffAdapter);
        //设置发送按钮
        send = (Button) findViewById(R.id.button);
        inputText = (EditText) findViewById(R.id.input_text_msg);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = inputText.getText().toString();
                if (StringUtil.isNotBlank(content)) {
                    Stuff stuff = new Stuff(SocketIOClientUtil.getUser().getCustomerId(), groupName, CustomerType.GROUP, groupName, TYPE_TEXT, content);
                    addStuff(stuff);
                    sendStuff(stuff);
                    inputText.setText("");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusManager.getEventBus().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(final Stuff stuff) throws InterruptedException {
        switch (stuff.getEventName()) {
            case TYPE_CALL:
                //延时，避免同时发起访问，造成连通问题
                Thread.sleep(500);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("通话来电").setMessage(stuff.getSource() + " 来电了！")
                        .setPositiveButton("跪舔", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CallActivity.startAction(GroupChatActivity.this, stuff.getSource() == null ? userId : stuff.getSource());
                            }
                        }).setNegativeButton("丑拒", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final Stuff outStuff = new Stuff(SocketIOClientUtil.getUser().getCustomerId(), stuff.getSource(), CustomerType.USER, stuff.getSource(), TYPE_HANGUP, "丑拒！");
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        socketIOClient.send(TYPE_HANGUP.getEventName(), stuff.getSource(), JSON.toJSONString(outStuff));
                                    }
                                }).start();
                            }
                        }).create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                break;
            case TYPE_TEXT:
                addStuff(stuff);
                break;
        }
    }

//    public void addStuff(Stuff sourceStuff) {
//        addStuff(sourceStuff, stuffs, stuffAdapter);
//    }

    public void addStuff(Stuff sourceStuff) {
        stuffs.add(sourceStuff);
        if (stuffAdapter != null) {
            stuffAdapter.notifyItemInserted(stuffs.size() - 1);
            stuffRecyclerView.scrollToPosition(stuffs.size() - 1);
        }
    }
}
