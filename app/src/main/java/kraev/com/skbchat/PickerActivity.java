package kraev.com.skbchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import kraev.com.skbchat.model.User;
import kraev.com.skbchat.utils.NetworkUtils;

/**
 * Created by qbai on 14.04.2017.
 */

public class PickerActivity extends AppCompatActivity {
    private static final String TAG = "picker activity 123";
    @BindView(R.id.choose_avatar_image)
    ImageView mChooseImageView;
    @BindView(R.id.picker_nick_edit_text)
    EditText mChooseNicknameEditText;
    @BindView(R.id.accept_picker_button)
    Button mAcceptButton;
    Context mContext;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private String mNickname;
    private String mCurrentUserUid;
    private String mCurrentUserEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mContext = this;

        setContentView(R.layout.activity_picker);
        ButterKnife.bind(this);

        mCurrentUserUid = mFirebaseUser.getUid();
        mCurrentUserEmail = mFirebaseUser.getEmail();
        //создаем узел в базе данных с уникальным идентификатором
        //равным Uid залогиненного пользователя
        mUserDatabaseReference = mFirebaseDatabase.getReference().child("users").child(mFirebaseUser.getUid());

        mChooseNicknameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    mNickname = s.toString().trim();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mAcceptButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!NetworkUtils.isNetworkAvailableAndConnected(mContext)) {
                    Toast.makeText(PickerActivity.this, R.string.network_unavailible_toast, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (mFirebaseUser == null) {
                    Toast.makeText(PickerActivity.this, "Ошибка регистрации попробуйте снова", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    if (mNickname.length() > 12) {
                        Toast.makeText(PickerActivity.this, R.string.too_long_nick_toast, Toast.LENGTH_SHORT).show();
                    } else {
                        User user = new User(mNickname, mCurrentUserUid, mCurrentUserEmail);
                        mUserDatabaseReference.setValue(user);

                        Intent intent = new Intent(PickerActivity.this, ChatActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
