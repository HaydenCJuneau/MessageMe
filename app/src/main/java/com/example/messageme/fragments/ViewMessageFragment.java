package com.example.messageme.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.messageme.R;
import com.example.messageme.data.Message;
import com.example.messageme.data.Reply;
import com.example.messageme.databinding.FragmentViewMessageBinding;
import com.example.messageme.databinding.ReplyListItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ViewMessageFragment extends Fragment {
    private static final String TAG = "demo";

    private static final String ARG_PARAM_MESSAGE = "ARG_PARAM_MESSAGE";

    private Message mMessage;
    private ArrayList<Reply> mReplies = new ArrayList<>();

    public ViewMessageFragment() {
        // Required empty public constructor
    }

    ViewMessageListener listener;
    ListenerRegistration repliesListenerReg;
    ReplyAdapter adapter;
    FragmentViewMessageBinding binding;

    public static ViewMessageFragment newInstance(Message m) {
        ViewMessageFragment fragment = new ViewMessageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_MESSAGE, m);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMessage = (Message) getArguments().getSerializable(ARG_PARAM_MESSAGE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (repliesListenerReg != null) {
            repliesListenerReg.remove();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ViewMessageListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement MyGradesListener");
        }
    }

    FirebaseFirestore mStorage;
    FirebaseUser mUser;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("View Message");

        binding.replyRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ReplyAdapter();
        binding.replyRecyclerView.setAdapter(adapter);

        mStorage = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        binding.buttonCancel.setOnClickListener(v -> listener.gotoInbox());
        binding.buttonReply.setOnClickListener(v -> listener.gotoReply(mMessage));

        markRead();
        setupFields();
        getReplies();
    }

    void getReplies() {
        repliesListenerReg = mStorage.collection("messages")
            .document(mMessage.getDocumentId())
            .collection("replies")
            .addSnapshotListener((value, error) -> {
                if (error != null || value == null) { return; }
                mReplies.clear();
                for (QueryDocumentSnapshot d: value) {
                    Reply r = d.toObject(Reply.class);
                    mReplies.add(r);
                }
                adapter.notifyDataSetChanged();
            });
    }

    void markRead() {
        if (mMessage.getRecipientId().equals(mUser.getUid())) {
            mMessage.setRecipientOpened(true);
            mStorage.collection("messages")
                .document(mMessage.getDocumentId())
                .update("recipientOpened", true)
                    .addOnSuccessListener(unused -> Log.d(TAG, "markRead: success"))
                    .addOnFailureListener(e -> Log.d(TAG, "markRead: failed!" + e.getMessage()));
        }
    }

    void setupFields() {
        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String createdAt = sfd.format(mMessage.getCreatedAt().toDate());

        // Use a string builder to create the to/from field
        StringBuilder builder = new StringBuilder();
        builder.append("From: ");
        if (mMessage.getCreatedById().equals(mUser.getUid())) {
            builder.append("Me ");
        } else {
            builder.append(mMessage.getCreatedByName()).append(" ");
        }

        builder.append("To: ");
        if (mMessage.getRecipientId().equals(mUser.getUid())) {
            builder.append("Me");
        } else {
            builder.append(mMessage.getRecipientName());
        }

        // Set Texts
        binding.textViewSenderReceiver.setText(builder.toString());
        binding.textViewCreatedAt.setText(createdAt);
        binding.textViewTitle.setText(mMessage.getMessageTitle());
        binding.textViewEditMessage.setText(mMessage.getMessageText());
    }

    // Adapter
    class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {
        @NonNull
        @Override
        public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ReplyListItemBinding itemBinding = ReplyListItemBinding.inflate(getLayoutInflater(), parent, false);
            return new ReplyViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
            holder.setupUI(mReplies.get(position));
        }

        @Override
        public int getItemCount() {
            return mReplies.size();
        }

        class ReplyViewHolder extends RecyclerView.ViewHolder {
            ReplyListItemBinding itemBinding;
            Reply mReply;
            public ReplyViewHolder(ReplyListItemBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }

            public void setupUI(Reply reply){
                mReply = reply;
                itemBinding.textViewSenderReciever.setText(reply.getCreatedByName() + ":");
                itemBinding.textViewMessage.setText(reply.getMessageText());
            }
        }
    }

    public interface ViewMessageListener {
        void gotoInbox();
        void gotoReply(Message m);
    }
}