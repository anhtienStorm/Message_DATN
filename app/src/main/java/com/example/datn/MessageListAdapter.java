package com.example.datn;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListViewHolder>{

    ArrayList<Message> list_message;
    Context context;

    public MessageListAdapter(Context context){
        this.context = context;
    }

    public void updateListMessage(ArrayList<Message> list){
        list_message = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.message_list_item, parent, false);
        return new MessageListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageListViewHolder holder, int position) {
        holder.bindView(list_message.get(position));
        if ("2".equals(list_message.get(position).getType())){
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.itemLayout.getLayoutParams();
            params.gravity = Gravity.END;
            holder.itemLayout.setLayoutParams(params);

            params = (LinearLayout.LayoutParams) holder.content.getLayoutParams();
            params.gravity = Gravity.END;
            holder.content.setLayoutParams(params);
            holder.content.setBackground(context.getDrawable(R.drawable.background_message_sender));
            holder.content.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.itemLayout.getLayoutParams();
            params.gravity = Gravity.START;
            holder.itemLayout.setLayoutParams(params);

            params = (LinearLayout.LayoutParams) holder.content.getLayoutParams();
            params.gravity = Gravity.START;
            holder.content.setLayoutParams(params);
            holder.content.setBackground(context.getDrawable(R.drawable.background_message));
            holder.content.setTextColor(context.getResources().getColor(R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return list_message.size();
    }
}

class MessageListViewHolder extends RecyclerView.ViewHolder{

    LinearLayout itemLayout;
    TextView content, dateTime;

    public MessageListViewHolder(@NonNull View itemView) {
        super(itemView);

        itemLayout = itemView.findViewById(R.id.item_layout);
        content = itemView.findViewById(R.id.content_message);
        dateTime = itemView.findViewById(R.id.date_time);
    }

    void bindView(Message message){
        content.setText(message.getBody());
//        dateTime.setText(message.getDate_sent());
    }
}
