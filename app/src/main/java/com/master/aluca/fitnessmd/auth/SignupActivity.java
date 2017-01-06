package com.master.aluca.fitnessmd.auth;

/**
 * Created by andrei on 10/29/2016.
 */
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.master.aluca.fitnessmd.MainActivity;
import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.util.SharedPreferencesManager;
import com.master.aluca.fitnessmd.webserver.WebserverManager;

import butterknife.ButterKnife;
import butterknife.Bind;

public class SignupActivity extends Activity {
    private static final String LOG_TAG = "Fitness_Signup";

    @Bind(R.id.input_name) EditText _nameText;
    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_signup) Button _signupButton;
    @Bind(R.id.link_login) TextView _loginLink;

    private WebserverManager mWebserverManager;
    private SharedPreferencesManager sharedPreferencesManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Log.d(LOG_TAG, "onCreate");
        ButterKnife.bind(this);

        sharedPreferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());
        mWebserverManager = WebserverManager.getInstance(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "_signupButton onClick");
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"_loginLink onClick");
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });


    }

    public void signup() {
        Log.d(LOG_TAG, "Signup");
        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        if (!mWebserverManager.requestSignup(_nameText, _emailText, _passwordText)) {
            Log.d(LOG_TAG, "onSignupFailed");
            Toast.makeText(getBaseContext(), "signup failed", Toast.LENGTH_LONG).show();
            _signupButton.setEnabled(true);
            progressDialog.dismiss();
            return;
        } else {
            _signupButton.setEnabled(false);
            Log.d(LOG_TAG, "signup success");
            sharedPreferencesManager.setUserName(_emailText.getText().toString(), _nameText.getText().toString());
            sharedPreferencesManager.setEmail(_emailText.getText().toString());
            setResult(RESULT_OK, null);
            progressDialog.dismiss();
            finish();
        }

    }

}