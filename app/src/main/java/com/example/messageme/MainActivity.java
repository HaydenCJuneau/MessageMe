package com.example.messageme;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.messageme.data.Message;
import com.example.messageme.data.User;
import com.example.messageme.fragments.DirectoryFragment;
import com.example.messageme.fragments.InboxFragment;
import com.example.messageme.fragments.LoginFragment;
import com.example.messageme.fragments.NewMessageFragment;
import com.example.messageme.fragments.ReplyFragment;
import com.example.messageme.fragments.SignupFragment;
import com.example.messageme.fragments.ViewMessageFragment;
import com.example.messageme.singletons.Directory;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity
    implements LoginFragment.LoginListener, SignupFragment.SignUpListener,
        InboxFragment.InboxListener, NewMessageFragment.NewMessageListener,
        ViewMessageFragment.ViewMessageListener, ReplyFragment.ReplyListener,
        DirectoryFragment.DirectoryListener {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Directory.GetDirectory();
        setContentView(R.layout.activity_main);
        if(mAuth.getCurrentUser() == null){
            login();
        } else {
            gotoInbox();
        }
    }

    @Override
    public void createNewAccount() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerView, new SignupFragment())
            .commit();
    }

    @Override
    public void authCompleted() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerView, new InboxFragment())
            .commit();
    }

    @Override
    public void login() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerView, new LoginFragment())
            .commit();
    }

    @Override
    public void gotoMessage(Message m) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerView, ViewMessageFragment.newInstance(m))
            .commit();
    }

    @Override
    public void userSelected(User u) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerView, NewMessageFragment.newInstance(u))
            .commit();
    }

    @Override
    public void gotoSendMessage() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerView, new NewMessageFragment())
            .commit();
    }

    @Override
    public void logout() {
        mAuth.signOut();
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerView, new LoginFragment())
            .commit();
    }

    @Override
    public void gotoInbox() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerView, new InboxFragment())
            .commit();
    }

    @Override
    public void gotoDirectory() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerView, new DirectoryFragment())
            .commit();
    }

    @Override
    public void gotoReply(Message m) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerView, ReplyFragment.newInstance(m))
            .commit();
    }
}