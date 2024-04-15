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

import com.example.messageme.R;
import com.example.messageme.data.User;
import com.example.messageme.databinding.FragmentSignUpBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SignupFragment extends Fragment {
    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentSignUpBinding binding;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore mStorage = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonCancel.setOnClickListener(v -> mListener.login());

        binding.buttonSignup.setOnClickListener(v -> {
            String name = binding.editTextName.getText().toString();
            String email = binding.editTextEmail.getText().toString();
            String password = binding.editTextPassword.getText().toString();

            if(name.isEmpty()){
                Toast.makeText(getActivity(), "Enter valid name!", Toast.LENGTH_SHORT).show();
            } else if(email.isEmpty()){
                Toast.makeText(getActivity(), "Enter valid email!", Toast.LENGTH_SHORT).show();
            } else if (password.isEmpty()){
                Toast.makeText(getActivity(), "Enter valid password!", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            mAuth.getCurrentUser().updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    Toast.makeText(getActivity(), "Account created successfully!", Toast.LENGTH_SHORT).show();

                                    User u = new User(mAuth.getCurrentUser().getUid(), email, name);
                                    u.setBlocked(new ArrayList<>());
                                    addUserToDirectory(u);
                                } else {
                                    Toast.makeText(getActivity(), "Error: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        });

        getActivity().setTitle(R.string.create_account_label);

    }

    void addUserToDirectory(User u) {
        mStorage.collection("directory")
            .document(u.getUid())
            .set(u)
            .addOnSuccessListener(documentReference -> {
                Log.d("demo", "Sign in: Created User in Directory");
                mListener.authCompleted();
            }).addOnFailureListener(e -> {
                Log.d("demo", "Sign in: User in Directory Failed! " + e.getMessage());
            });
    }

    SignUpListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (SignUpListener) context;
    }

    public interface SignUpListener {
        void login();
        void authCompleted();
    }
}