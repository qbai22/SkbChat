package kraev.com.skbchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import kraev.com.skbchat.utils.NetworkUtils;

/**
 * Created by qbai on 14.04.2017.
 */

public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.sign_up_button)
    Button mSignUpButton;
    @BindView(R.id.sign_in_button)
    Button mSignInButton;
    @BindView(R.id.email)
    EditText mEmailEditText;
    @BindView(R.id.password)
    EditText mPasswordEditText;
    @BindView(R.id.signup_progressbar)
    ProgressBar mProgressBar;
    @BindView(R.id.btn_reset_password)
    Button mResetButton;

    private Context mContext;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mContext = this;

        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mEmailEditText.getText().toString().trim();
                String password = mPasswordEditText.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), R.string.empty_email_toast, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), R.string.empty_password_toast, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), R.string.password_too_short_toast, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                //создаем нового пользователя
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                mProgressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    if(!NetworkUtils.isNetworkAvailableAndConnected(mContext)){
                                        Toast.makeText(SignupActivity.this, R.string.network_unavailible_toast, Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        Toast.makeText(SignupActivity.this, getString(R.string.unsuccessful_auth_toast) + task.getException(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    startActivity(new Intent(SignupActivity.this, PickerActivity.class));
                                    finish();
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
    }
}
