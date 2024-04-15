package com.example.messageme.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.messageme.R;
import com.example.messageme.adapters.MessageAdapter;
import com.example.messageme.data.Message;
import com.example.messageme.databinding.FragmentInboxBinding;
import com.example.messageme.singletons.Directory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class InboxFragment extends Fragment
    implements MessageAdapter.IMessageAdapterListener {
    private static final String TAG = "demo";

    public InboxFragment() {
        // Required empty public constructor
    }

    InboxListener mListener;
    ListenerRegistration messageListenerRef;
    FragmentInboxBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInboxBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //try catch block
        try {
            mListener = (InboxListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement MyGradesListener");
        }
    }

    MessageAdapter adapter;
    ArrayList<Message> mMessages = new ArrayList<>();
    FirebaseFirestore mStorage;
    FirebaseUser mUser;

    String messageQuery = "";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("My Messages");

        mStorage = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MessageAdapter(mMessages, mUser, this);
        binding.recyclerView.setAdapter(adapter);

        binding.buttonLogout.setOnClickListener(v -> mListener.logout());
        binding.buttonNewMessage.setOnClickListener(v -> mListener.gotoSendMessage());
        binding.buttonSearch.setOnClickListener(v -> {
            messageQuery = binding.editTextSearch.getText().toString().trim();
            getMessages();
        });

        getMessages();
    }

    // Firebase Methods
    void setMessages(QuerySnapshot value, FirebaseFirestoreException error) {
        mMessages.clear();
        String userUid = mUser.getUid();

        for (QueryDocumentSnapshot document: value) {
            Message nMessage = document.toObject(Message.class);
            if ((nMessage.getCreatedById().equals(userUid) && !nMessage.isSenderDeleted())
                    || (nMessage.getRecipientId().equals(userUid)  && !nMessage.isRecipientDeleted())) {
                // Set Sender and Receiver names
                String recipientName =
                        Directory.directory.get(nMessage.getRecipientId()).getDisplayName();
                String senderName =
                        Directory.directory.get(nMessage.getCreatedById()).getDisplayName();
                nMessage.setRecipientName(recipientName);
                nMessage.setCreatedByName(senderName);

                mMessages.add(nMessage);
            }
        }

        adapter.notifyDataSetChanged();
    }

    void getMessages() {

        CollectionReference collection = mStorage.collection("messages");

        if (messageListenerRef != null) {
            messageListenerRef.remove();
        }

        if (messageQuery.length() > 0) {
            messageListenerRef = collection
                .whereGreaterThanOrEqualTo("messageTitle", messageQuery)
                .whereLessThanOrEqualTo("messageTitle", messageQuery+ '\uf8ff')
                .addSnapshotListener(this::setMessages);
        } else {
            messageListenerRef = collection.addSnapshotListener(this::setMessages);
        }
    }

    // Listener Methods
    @Override
    public void removeMessage(Message m) {
        DocumentReference d =
            mStorage.collection("messages").document(m.getDocumentId());
        if (mUser.getUid().equals(m.getCreatedById())) {
            d.update("senderDeleted", true);
        } else if (mUser.getUid().equals(m.getRecipientId())) {
            d.update("recipientDeleted", true);
        }

        mMessages.remove(m);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void viewMessage(Message m) {
        mListener.gotoMessage(m);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (messageListenerRef != null) {
            messageListenerRef.remove();
        }
    }

    public interface InboxListener {
        void gotoMessage(Message m);
        void gotoSendMessage();
        void logout();
    }
}