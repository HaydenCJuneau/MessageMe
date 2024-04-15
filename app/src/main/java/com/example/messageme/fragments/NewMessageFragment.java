package com.example.messageme.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.messageme.data.Message;
import com.example.messageme.data.User;
import com.example.messageme.databinding.FragmentNewMessageBinding;
import com.example.messageme.singletons.Directory;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewMessageFragment extends Fragment {
    private static final String TAG = "demo";

    private static final String ARG_PARAM_RECIPIENT = "ARG_PARAM_RECIPIENT";

    public NewMessageFragment() {
        // Required empty public constructor
    }

    NewMessageListener listener;
    FragmentNewMessageBinding binding;
    User chosenUser;

    public static NewMessageFragment newInstance(User u) {
        NewMessageFragment fragment = new NewMessageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_RECIPIENT, u);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chosenUser = (User) getArguments().getSerializable(ARG_PARAM_RECIPIENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNewMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //try catch block
        try {
            listener = (NewMessageListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement MyGradesListener");
        }
    }

    FirebaseFirestore mStorage;
    FirebaseUser mUser;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("New Message");

        mStorage = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        binding.buttonCancel.setOnClickListener(v -> listener.gotoInbox());
        binding.buttonSend.setOnClickListener(v -> sendMessage());
        binding.buttonDirectory.setOnClickListener(v -> listener.gotoDirectory());

        if (chosenUser != null) {
            binding.editTextRecipient.setText(chosenUser.getEmail());
        }
    }

    void sendMessage() {
        String title = binding.editTextTitle.getText().toString().trim();
        String message = binding.editTextMessage.getText().toString().trim();
        String recipientEmail = binding.editTextRecipient.getText().toString().trim();
        
        if (title.isEmpty() || message.isEmpty() || recipientEmail.isEmpty()) {
            Toast.makeText(getActivity(), "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        User recipient = checkRecipient(recipientEmail);
        if (recipient == null) {
            Toast.makeText(getActivity(), "User does not exist or has blocked you!", Toast.LENGTH_SHORT).show();
            return;
        }

        Message nMessage = new Message();
        nMessage.setCreatedByName(mUser.getDisplayName());
        nMessage.setCreatedById(mUser.getUid());

        nMessage.setRecipientName(recipient.getDisplayName());
        nMessage.setRecipientId(recipient.getUid());

        nMessage.setMessageText(message);
        nMessage.setMessageTitle(title);
        nMessage.setCreatedAt(Timestamp.now());

        nMessage.setRecipientDeleted(false);
        nMessage.setSenderDeleted(false);
        nMessage.setRecipientOpened(false);

        mStorage.collection("messages")
            .add(nMessage)
            .addOnSuccessListener(documentReference -> {
                Log.d("demo", "sendMessage: Created Message");
                documentReference.update("documentId", documentReference.getId());
                listener.gotoInbox();
            })
            .addOnFailureListener(e -> {
                Log.d("demo", "Post Message Failed: " + e.getMessage());
                Toast.makeText(getActivity(), "New message failed to process", Toast.LENGTH_SHORT).show();
            });
    }

    User checkRecipient(String email) {
        for (User u: Directory.directory.values()) {
            if (u.getEmail().equals(email)) {
                // Check to see if this user has blocked them
                if (u.getBlocked().contains(mUser.getUid())) return null;
                return u;
            }
        }
        return null;
    }

    public interface NewMessageListener {
        void gotoInbox();
        void gotoDirectory();
    }
}