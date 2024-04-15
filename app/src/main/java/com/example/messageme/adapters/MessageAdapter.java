package com.example.messageme.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messageme.R;
import com.example.messageme.data.Message;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    // Listeners
    IMessageAdapterListener mListener;
    // Data
    ArrayList<Message> messages;
    FirebaseUser mUser;

    public MessageAdapter(ArrayList<Message> messages, FirebaseUser currentUser, IMessageAdapterListener mListener) {
        this.mUser = currentUser;
        this.messages = messages;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_list_item, parent, false);
        MessageViewHolder messageViewHolder = new MessageViewHolder(view, mListener);
        return messageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.mMessage = message;

        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String createdAt = sfd.format(message.getCreatedAt().toDate());

        // Use a string builder to create the to/from field
        StringBuilder builder = new StringBuilder();
        builder.append("From: ");
        if (message.getCreatedById().equals(mUser.getUid())) {
            builder.append("Me ");
        } else {
            builder.append(message.getCreatedByName()).append(" ");
        }

        builder.append("To: ");
        if (message.getRecipientId().equals(mUser.getUid())) {
            builder.append("Me");
        } else {
            builder.append(message.getRecipientName());
        }

        // Get colors
        int color_unread = ContextCompat.getColor(holder.itemView.getContext(), R.color.color_unread);
        int color_read = ContextCompat.getColor(holder.itemView.getContext(), R.color.color_read);

        // Set Texts
        holder.textViewSenderReceiver.setText(builder.toString());
        holder.textViewDate.setText(createdAt);
        holder.textViewTitle.setText(message.getMessageTitle());

        // Set color
        if (message.getRecipientId().equals(mUser.getUid()) && !message.isRecipientOpened()) {
            holder.textViewTitle.setTextColor(color_unread);
        } else {
            holder.textViewTitle.setTextColor(color_read);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        Message mMessage;
        IMessageAdapterListener mListener;

        View itemView;
        TextView textViewSenderReceiver;
        TextView textViewDate;
        TextView textViewTitle;
        ImageView imageViewDelete;

        public MessageViewHolder(@NonNull View itemView, IMessageAdapterListener mListener) {
            super(itemView);
            this.mListener = mListener;

            textViewSenderReceiver = itemView.findViewById(R.id.textViewSenderReciever);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            imageViewDelete = itemView.findViewById(R.id.imageViewDelete);
            this.itemView = itemView;

            itemView.setOnClickListener(v -> mListener.viewMessage(mMessage));
            imageViewDelete.setOnClickListener(v -> mListener.removeMessage(mMessage));
        }
    }

    public interface IMessageAdapterListener {
        void removeMessage(Message m);
        void viewMessage(Message m);
    }
}
