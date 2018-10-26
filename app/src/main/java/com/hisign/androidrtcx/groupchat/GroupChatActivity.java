package com.hisign.androidrtcx.groupchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.hisign.androidrtcx.R;
import com.hisign.rtcx.internet.SocketIOClient;
import com.hisign.androidrtcx.groupchat.pj.Stuff;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jodd.util.StringUtil;

public class GroupChatActivity extends Activity {
    private static final String TAG = "GroupChatActivity";

    private static final int CONNECTION_REQUEST = 1;

    private List<Stuff> stuffs = new ArrayList<>();

    private EditText inputText;

    private Button send;

    private RecyclerView stuffRecyclerView;

    private StuffAdapter stuffAdapter;

    private final String userId = Integer.toString((new Random()).nextInt(100000000));

    public static final String EVENT_STUFF = "STUFF";

    public static final String EVENT_STUFF_HISTORY = "STUFF_HISTORY";

    public static final String EVENT_CALL_USER = "CALLUSER";
    public static final String EVENT_HANGUP_USER = "HANGUPUSER";

    private void sendStuff(final Stuff stuff) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (SocketIOClient.Customer group : SocketIOClientUtil.getGroups()) {
                    SocketIOClientUtil.getInstance(null).send(EVENT_STUFF, group.getCustomerId(), JSON.toJSONString(stuff));
                }
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_chat);
        SocketService.startService(GroupChatActivity.this);
        setTitle(userId + "的群聊");
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
                    Stuff stuff = new Stuff(content, Stuff.StuffType.TYPE_SEND, SocketIOClientUtil.getUser());
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
        SocketService.stopService(GroupChatActivity.this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(final Stuff stuff) throws InterruptedException {
        switch (stuff.getType()) {
            case TYPE_CALL:
                //延时，避免同时发起访问，造成连通问题
                Thread.sleep(500);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("通话来电").setMessage(stuff.getUserCustomer().getCustomerId() + " 来电了！")
                        .setPositiveButton("跪舔", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CallActivity.startAction(GroupChatActivity.this, stuff.getContent() == null ? userId : stuff.getContent());
                            }
                        }).setNegativeButton("丑拒", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final Stuff outStuff = new Stuff("丑拒！", Stuff.StuffType.TYPE_HANGUP, SocketIOClientUtil.getUser());
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SocketIOClientUtil.getInstance(null).send(EVENT_HANGUP_USER, stuff.getUserCustomer().getCustomerId(), JSON.toJSONString(outStuff));
                                    }
                                }).start();
                            }
                        }).create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                break;
            default:
                addStuff(stuff);
                break;
        }
    }

//    public void addStuff(Stuff sourceStuff) {
//        addStuff(sourceStuff, stuffs, stuffAdapter);
//    }

    public void addStuff(Stuff sourceStuff) {
        stuffs.add(sourceStuff);
        stuffAdapter.notifyItemInserted(stuffs.size() - 1);
        stuffRecyclerView.scrollToPosition(stuffs.size() - 1);
    }
}
