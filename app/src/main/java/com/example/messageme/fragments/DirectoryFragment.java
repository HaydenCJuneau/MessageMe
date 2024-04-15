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

import com.example.messageme.data.Reply;
import com.example.messageme.data.User;
import com.example.messageme.databinding.DirectoryListItemBinding;
import com.example.messageme.databinding.FragmentDirectoryBinding;
import com.example.messageme.databinding.ReplyListItemBinding;
import com.example.messageme.fragments.ViewMessageFragment;
import com.example.messageme.singletons.Directory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class DirectoryFragment extends Fragment {
    private static final String TAG = "demo";

    public DirectoryFragment() {
        // Required empty public constructor
    }

    FragmentDirectoryBinding binding;
    DirectoryAdapter adapter;
    DirectoryListener listener;
    ArrayList<User> mDirectoryUsers = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDirectoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DirectoryListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement MyGradesListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (directoryListenerRef != null) directoryListenerRef.remove();
    }

    ListenerRegistration directoryListenerRef;
    FirebaseFirestore mStorage;
    FirebaseUser mUser;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Directory");

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new DirectoryAdapter();
        binding.recyclerView.setAdapter(adapter);

        mStorage = FirebaseFirestore.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        binding.buttonCancel.setOnClickListener(v -> listener.gotoSendMessage());

        getUsers();
    }

    void getUsers() {
        if (directoryListenerRef != null) directoryListenerRef.remove();

        directoryListenerRef = mStorage.collection("directory")
            .addSnapshotListener((value, error) -> {

                if (error != null) {
                    Log.d(TAG, "getUsers: Failed to get Users" + error.getMessage());
                }

                mDirectoryUsers.clear();

                for (QueryDocumentSnapshot document: value) {
                    User nUser = document.toObject(User.class);
                    mDirectoryUsers.add(nUser);
                }

                adapter.notifyDataSetChanged();
            });
    }

    // Adapter
    class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.DirectoryViewHolder> {
        @NonNull
        @Override
        public DirectoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DirectoryListItemBinding itemBinding
                = DirectoryListItemBinding.inflate(getLayoutInflater(), parent, false);
            return new DirectoryViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull DirectoryViewHolder holder, int position) {
            holder.setupUI(mDirectoryUsers.get(position));
        }

        @Override
        public int getItemCount() {
            return mDirectoryUsers.size();
        }

        class DirectoryViewHolder extends RecyclerView.ViewHolder {
            DirectoryListItemBinding itemBinding;
            User mDirectoryUser;
            public DirectoryViewHolder(DirectoryListItemBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
            }

            public void setupUI(User dUser) {
                mDirectoryUser = dUser;

                itemBinding.getRoot().setOnClickListener(v -> listener.userSelected(dUser));

                itemBinding.textViewName.setText(dUser.getDisplayName());
                itemBinding.textViewEmail.setText(dUser.getEmail());

                if (dUser.getUid().equals(mUser.getUid())) {
                    itemBinding.buttonBlock.setVisibility(View.INVISIBLE);
                } else {
                    itemBinding.buttonBlock.setVisibility(View.VISIBLE);
                    User activeUser
                        = Directory.directory.get(mUser.getUid());
                    // Set callbacks
                    if (activeUser.getBlocked().contains(dUser.getUid())) {
                        // If this user is in the active users block list
                        itemBinding.buttonBlock.setOnClickListener(v -> removeFromBlockList(dUser.getUid()));
                        itemBinding.buttonBlock.setText("Unblock");
                    } else {
                        itemBinding.buttonBlock.setOnClickListener(v -> addToBlockList(dUser.getUid()));
                        itemBinding.buttonBlock.setText("Block");
                    }
                }
            }

            private void addToBlockList(String blockUid) {
                CollectionReference dirCollection =
                    mStorage.collection("directory");
                dirCollection.whereEqualTo("uid", mUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc: queryDocumentSnapshots) {
                            dirCollection.document(doc.getId())
                                .update("blocked", FieldValue.arrayUnion(blockUid))
                                .addOnFailureListener(e -> Log.d(TAG, "addToBlockList: Failed!" + e.getMessage()));
                        }
                        adapter.notifyDataSetChanged();
                    });
            }

            private void removeFromBlockList(String unblockUid) {
                CollectionReference dirCollection =
                        mStorage.collection("directory");
                dirCollection.whereEqualTo("uid", mUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc: queryDocumentSnapshots) {
                            dirCollection.document(doc.getId())
                                .update("blocked", FieldValue.arrayRemove(unblockUid))
                                .addOnFailureListener(e -> Log.d(TAG, "removeFromBlockList: Failed!" + e.getMessage()));
                        }
                        adapter.notifyDataSetChanged();
                    });
            }
        }
    }

    public interface DirectoryListener {
        void userSelected(User u);
        void gotoSendMessage();
    }
}