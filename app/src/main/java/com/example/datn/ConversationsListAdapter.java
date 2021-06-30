package com.example.datn;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ConversationsListAdapter extends RecyclerView.Adapter<ConversationsListViewHolder> {

    private static final String THREAD_ID = "thread_id";
    private static final String ADDRESS = "address";

    ArrayList<Conversation> list_Conversation = new ArrayList<>();
    Context context;

    public ConversationsListAdapter(Context context) {
        this.context = context;
    }

    public void updateConversationList(ArrayList<Conversation> list) {
        list_Conversation = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConversationsListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.conversation_list_item, parent, false);
        return new ConversationsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationsListViewHolder holder, int position) {
        holder.bindView(list_Conversation.get(position));
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ConversationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(THREAD_ID, list_Conversation.get(position).getThread_id());
            intent.putExtra(ADDRESS, list_Conversation.get(position).getAddress());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list_Conversation.size();
    }
}

class ConversationsListViewHolder extends RecyclerView.ViewHolder {

    TextView title, body;

    public ConversationsListViewHolder(@NonNull View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.title);
        body = itemView.findViewById(R.id.body);
    }

    void bindView(Conversation conversation) {
        title.setText(conversation.getAddress());
        body.setText(conversation.getBody());
    }
}
