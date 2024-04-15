package com.example.messageme.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.messageme.data.Message;
import com.example.messageme.data.Reply;
import com.example.messageme.databinding.FragmentReplyBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReplyFragment extends Fragment {

    private static final String TAG = "demo";

    private static final String ARG_PARAM_MESSAGE = "ARG_PARAM_MESSAGE";
    private Message mMessage;

    public ReplyFragment() {
        // Required empty public constructor
    }

    ReplyListener listener;
    FragmentReplyBinding binding;

    public static ReplyFragment newInstance(Message m) {
        ReplyFragment fragment = new ReplyFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_MESSAGE, m);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMessage = (Message) getArguments().getSerializable(ARG_PARAM_MESSAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentReplyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ReplyListener) context;
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

        mStorage = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        binding.buttonCancel.setOnClickListener(v -> listener.gotoMessage(mMessage));
        binding.buttonReply.setOnClickListener(v -> sendReply());

    }

    void sendReply() {
        String message = binding.editTextText.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(getActivity(), "Please add text to your reply!", Toast.LENGTH_SHORT).show();
            return;
        }

        Reply nReply = new Reply();

        nReply.setMessageText(message);
        nReply.setCreatedById(mUser.getUid());
        nReply.setCreatedByName(mUser.getDisplayName());
        nReply.setCreatedAt(Timestamp.now());
        nReply.setMessageId(mMessage.getDocumentId());

        mStorage.collection("messages")
            .document(mMessage.getDocumentId())
            .collection("replies")
            .add(nReply)
            .addOnSuccessListener(documentReference -> {
               documentReference.update("documentId", documentReference.getId());
               listener.gotoMessage(mMessage);
            });
    }

    public interface ReplyListener {
        void gotoMessage(Message m);
    }
}