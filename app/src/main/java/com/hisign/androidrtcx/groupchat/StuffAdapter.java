package com.hisign.androidrtcx.groupchat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.hisign.androidrtcx.R;
import com.hisign.rtcx.internet.SocketIOClient;
import com.hisign.androidrtcx.groupchat.pj.Stuff;

import java.util.List;

public class StuffAdapter extends RecyclerView.Adapter<StuffAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftText;
        TextView rightText;
        ImageView leftImage;
        ImageView rightImage;

        public ViewHolder(View itemView) {
            super(itemView);
            leftLayout = (LinearLayout) itemView.findViewById(R.id.left_layout);
            rightLayout = (LinearLayout) itemView.findViewById(R.id.right_layout);
            leftText = (TextView) itemView.findViewById(R.id.stuff_left_text);
            rightText = (TextView) itemView.findViewById(R.id.stuff_right_text);
            leftImage = (ImageView) itemView.findViewById(R.id.user_left_image);
            rightImage = (ImageView) itemView.findViewById(R.id.user_right_image);
        }
    }

    private List<Stuff> sutffs;
    private SocketIOClient socketIOClient;
    private final Context context;

    public StuffAdapter(List<Stuff> sutffs, SocketIOClient socketIOClient, Context context) {
        this.sutffs = sutffs;
        this.socketIOClient = socketIOClient;
        this.context = context;
    }

    @Override
    public StuffAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View stuffView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stuff_view, parent, false);
        return new ViewHolder(stuffView);
    }

    @Override
    public void onBindViewHolder(StuffAdapter.ViewHolder holder, int position) {
        final Stuff stuff = sutffs.get(position);
        if (stuff.getType() == Stuff.StuffType.TYPE_RECEIVE) {
            holder.leftImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Stuff outStuff = new Stuff(stuff.getUserCustomer().getCustomerId(), Stuff.StuffType.TYPE_CALL, socketIOClient.getUserCustomer());
                            socketIOClient.send(GroupChatActivity.EVENT_CALL_USER, stuff.getUserCustomer().getCustomerId(), JSON.toJSONString(outStuff));
                        }
                    }).start();

                    CallActivity.startAction(context, stuff.getUserCustomer().getCustomerId());
                }
            });
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftText.setText(stuff.getContent());
        } else if (stuff.getType() == Stuff.StuffType.TYPE_SEND) {
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.rightText.setText(stuff.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return sutffs.size();
    }
}
