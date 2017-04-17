package kraev.com.skbchat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kraev.com.skbchat.model.User;

/**
 * Created by qbai on 17.04.2017.
 */

//aктивити которая показывает список авторизованных в данный момент людей

public class OnlineListActivity extends AppCompatActivity {
    @BindView(R.id.online_list_recycler_view)
    RecyclerView mUsersRecyclerView;
    @BindView(R.id.online_list_progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.online_toolbar)
    Toolbar mToolbar;

    FirebaseAuth mAuth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mOnlineUsersReference;
    ChildEventListener mOnlineUsersEventListener;

    private List<User> mUserList;
    private OnlineUsersAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        setContentView(R.layout.activity_online_list);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        mOnlineUsersReference = mFirebaseDatabase.getReference().child("users_online");

        mUserList = new ArrayList<>();
        mAdapter = new OnlineUsersAdapter(mUserList);
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUsersRecyclerView.setAdapter(mAdapter);
        setupEventListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOnlineUsersReference.addChildEventListener(mOnlineUsersEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOnlineUsersReference.removeEventListener(mOnlineUsersEventListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupEventListener() {
        mOnlineUsersEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mProgressBar.setVisibility(ProgressBar.GONE);
                User user = dataSnapshot.getValue(User.class);
                mUserList.add(user);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    class OnlineUsersAdapter extends RecyclerView.Adapter<OnlineUsersAdapter.OnlineViewHolder> {

        List<User> users;

        public OnlineUsersAdapter(List<User> data) {
            users = data;
        }

        @Override
        public OnlineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.online_list_item, parent, false);

            return new OnlineViewHolder(v);
        }

        @Override
        public void onBindViewHolder(OnlineViewHolder holder, int position) {
            User user = users.get(position);
            holder.emailTextView.setText(user.getEmail());
            holder.nickNameTextView.setText(user.getName());
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class OnlineViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.item_email_text_view)
            TextView emailTextView;
            @BindView(R.id.item_online_nick_text_view)
            TextView nickNameTextView;

            OnlineViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
