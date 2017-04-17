package kraev.com.skbchat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import kraev.com.skbchat.model.ChatMessage;
import kraev.com.skbchat.model.User;
import kraev.com.skbchat.utils.NetworkUtils;
import kraev.com.skbchat.utils.UserPreferencesUtils;

public class ChatActivity extends AppCompatActivity {

    public static final String TAG = "chat activity";

    public static final int REQUEST_CODE_PHOTO_PICKER = 19;
    private static final String ANONYMOUS = "Аноним";

    @BindView(R.id.msg_recycler_view)
    RecyclerView mMessagesRecyclerView;
    @BindView(R.id.msg_edit_text)
    EditText mMessageEditText;
    @BindView(R.id.msg_send_button)
    ImageButton mSendButton;
    @BindView(R.id.chat_progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.photo_picker_button)
    ImageButton mPhotoPickerButton;
    @BindView(R.id.chat_drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.chat_nav_view)
    NavigationView mNavigationView;
    @BindView(R.id.chat_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.online_counter_text_view)
    TextView mCounterTextView;
    @BindView(R.id.people_in_chat_image)
    ImageView mOnlineIcon;

    private Context mContext;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference mUserOnlineDatabaseReference;
    //облачное хранение фотографий
    private FirebaseStorage mFirebasePhotoStorage;
    private StorageReference mPhotoStorageReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentFirebaseUser;
    private String mCurrentUserUid;
    private String mCurrentUserEmail;
    //листенер изменений состояния узла базы данных с сообщениями
    private ChildEventListener mMessageEventListener;
    //листенер авторизированных в данный момент пользоваетелей
    private ValueEventListener mUsersOnlineEventListener;

    private String mUsername = ANONYMOUS;

    private List<ChatMessage> mChatMessageList;
    private MessageRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebasePhotoStorage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentFirebaseUser = mAuth.getCurrentUser();
        mContext = this;

        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        mCurrentUserUid = mCurrentFirebaseUser.getUid();
        mCurrentUserEmail = mCurrentFirebaseUser.getEmail();

        setSupportActionBar(mToolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.toolbar_menu);
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);

        //ссылка на корневой узел базы данных с сообщениями
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("messages");
        //ссылка на узел с залогинеными пользователями
        mUserOnlineDatabaseReference = mFirebaseDatabase.getReference().child("users_online");
        //ссылка на удаленное хранилище с фотографиями
        mPhotoStorageReference = mFirebasePhotoStorage.getReference().child("chat_photos");

        mChatMessageList = new ArrayList<>();
        mAdapter = new MessageRecyclerAdapter(mChatMessageList, this);
        mMessagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mMessagesRecyclerView.setAdapter(mAdapter);

        setupDrawerContent();
        setupNickname();
        setupEventListeners();

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtils.isNetworkAvailableAndConnected(mContext)) {
                    Toast.makeText(ChatActivity.this, R.string.network_unavailible_toast, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                //Cоздаем и пушим сообщение в базу данных
                ChatMessage message = new ChatMessage(
                        mMessageEditText.getText().toString(), mUsername, null, null, mCurrentUserUid);
                mMessagesDatabaseReference.push().setValue(message);

                mMessageEditText.setText("");
            }
        });

        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                Intent result = Intent.createChooser(intent, "Где ваша фотография?");
                startActivityForResult(result, REQUEST_CODE_PHOTO_PICKER);
            }
        });

        mOnlineIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, OnlineListActivity.class);
                startActivity(intent);
                mChatMessageList.clear();
            }
        });

        //листенер для отслеживания открытия клавиатуры
        //чтобы пользователь мог видеть последние сообщения
        mMessagesRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mMessagesRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMessagesRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                        }
                    }, 100);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUserAsOnline();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        if (mMessagesDatabaseReference != null
                && mMessageEventListener != null) {
            mMessagesDatabaseReference.addChildEventListener(mMessageEventListener);
        }
        if (mUserOnlineDatabaseReference != null
                && mUsersOnlineEventListener != null) {
            mUserOnlineDatabaseReference.addValueEventListener(mUsersOnlineEventListener);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMessagesDatabaseReference != null
                && mMessageEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mMessageEventListener);
        }
        if (mUserOnlineDatabaseReference != null
                && mUsersOnlineEventListener != null) {
            mUserOnlineDatabaseReference.removeEventListener(mUsersOnlineEventListener);
        }
        mChatMessageList.clear();
    }

   
    @Override
    protected void onDestroy() {
        super.onDestroy();
        setUserOffline();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PHOTO_PICKER &&
                resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            try {
                //сжимаем фотографию перед загрузкой
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] byteArr = baos.toByteArray();
                //загрузка фотографии в удаленное хранилище
                StorageReference photoReference =
                        mPhotoStorageReference.child(selectedImageUri.getLastPathSegment());
                UploadTask uploadPhotoTask = photoReference.putBytes(byteArr);
                uploadPhotoTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //получаем ссылку для загрузки фотографии с сервера
                        //и сохраняем ее в базу данных
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        if (downloadUrl != null) {
                            ChatMessage message = new ChatMessage(null, mUsername, downloadUrl.toString(), null, mCurrentUserUid);
                            mMessagesDatabaseReference.push().setValue(message);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.download_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupDrawerContent() {
        View header = mNavigationView.getHeaderView(0);
        TextView name = (TextView) header.findViewById(R.id.drawer_nickname_text_view);
        name.setText(mCurrentFirebaseUser.getEmail());
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.logout_nav_menu:
                        mAuth.signOut();
                        setUserOffline();
                        Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    default:
                        item.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                }
            }
        });
    }

    private void setupEventListeners() {
        mMessageEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mProgressBar.setVisibility(ProgressBar.GONE);
                ChatMessage dbChatMessage = dataSnapshot.getValue(ChatMessage.class);
                mChatMessageList.add(dbChatMessage);
                mAdapter.notifyDataSetChanged();
                mMessagesRecyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
            }
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            public void onCancelled(DatabaseError databaseError) {}
        };
        //при изменении списка авторизированных пользователей
        //обновляется UI
        mUsersOnlineEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCounterTextView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }
            public void onCancelled(DatabaseError databaseError) {}
        };
    }

    //метод сделан с расчетом возможного расширения функционала
    //и добавления возможности изменения ника пользователя
    private void setupNickname() {
        //устанавливаем текущего пользователя
        UserPreferencesUtils.setCurrentUserUid(this, mCurrentUserUid);
        DatabaseReference ref = mFirebaseDatabase.getReference().child("users").child(mCurrentUserUid);
        //возвращаем установленный ник текущего пользователя из базы данных
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                mUsername = user.getName();
                setUserAsOnline();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    private void setUserAsOnline() {
        if (mCurrentFirebaseUser != null) {
            User user = new User(mUsername, mCurrentUserUid, mCurrentUserEmail);
            mUserOnlineDatabaseReference.child(mCurrentUserUid).setValue(user);
        }
    }


    private void setUserOffline() {
        mUserOnlineDatabaseReference.child(mCurrentUserUid).removeValue();
    }


}

