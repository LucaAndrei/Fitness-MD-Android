package com.master.aluca.fitnessmd.auth;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.master.aluca.fitnessmd.Constants;
import com.master.aluca.fitnessmd.MainActivity;
import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.util.SharedPreferencesManager;
import com.master.aluca.fitnessmd.webserver.WebserverManager;


public class LoginActivity extends Activity{

    public static final String LOG_TAG = "Fitness_LoginActivity";

    EditText _emailText, _passwordText;
    Button _loginButton;
    TextView _signupLink;

    private static final int REQUEST_SIGNUP = 0;



    private static boolean animPlayed = false;


    Dialog reset;

    private WebserverManager mWebserverManager;

    private SharedPreferencesManager sharedPreferencesManager;

    ProgressDialog progressDialog = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(LOG_TAG, "onCreate");

        _emailText = (EditText) findViewById(R.id.input_email_login);
        _passwordText = (EditText) findViewById(R.id.input_password_login);
        _loginButton = (Button) findViewById(R.id.btn_login);
        _signupLink = (TextView) findViewById(R.id.link_signup);

        progressDialog = new ProgressDialog(LoginActivity.this);

        sharedPreferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());
        mWebserverManager =  WebserverManager.getInstance(this);


        IntentFilter filter = new IntentFilter();
        filter.addAction("login_result");
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        boolean isLoggedIn = sharedPreferencesManager.getIsUserLoggedIn();

        if(isLoggedIn) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else {
            if (!animPlayed) {
                startAnimations();
                animPlayed = true;
            }
            setListeners();
        }


    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LOG_TAG, "action : " + action.toString());
            if (action.equals("login_result")) {
                if(intent.getBooleanExtra("login_success", false)) {
                    progressDialog.dismiss();
                    _loginButton.setEnabled(false);

                    Log.d(LOG_TAG, "Login sucess");

                    sharedPreferencesManager.setLoggedIn(true);
                    String email = _emailText.getText().toString();
                    sharedPreferencesManager.setUserName(email, sharedPreferencesManager.getUserNameByEmail(email));
                    sharedPreferencesManager.setEmail(email);
                    Intent intentMainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intentMainActivity);
                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);


                    progressDialog.dismiss();


                } else {
                    _loginButton.setEnabled(true);
                    Log.d(LOG_TAG, "Login error");
                    Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();


                }


            }
        }

    };

    private void startAnimations() {
        Log.d(LOG_TAG, "StartAnimations");
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        LinearLayout l = (LinearLayout) findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(anim);



        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
        anim.reset();
        ImageView iv = (ImageView) findViewById(R.id.logo);
        iv.clearAnimation();
        iv.startAnimation(anim);




        anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        anim.setStartOffset(1000);

        _emailText.clearAnimation();
        _emailText.startAnimation(anim);

        _passwordText.clearAnimation();
        _passwordText.startAnimation(anim);

        _loginButton.clearAnimation();
        _loginButton.startAnimation(anim);

        _signupLink.clearAnimation();
        _signupLink.startAnimation(anim);
    }

    private void setListeners() {
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG,"_loginButton onClick");
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Log.d(LOG_TAG,"_signupLink onClick");
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                //finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        Log.d(LOG_TAG, "Login");

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Connecting...");
        progressDialog.show();
        _loginButton.setEnabled(false);
        mWebserverManager.requestLogin(_emailText, _passwordText);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult : " + requestCode);
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                Log.d(LOG_TAG, "onActivityResult RESULT_OK");
                sharedPreferencesManager.setLoggedIn(true);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed LoginActivity");
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }


}