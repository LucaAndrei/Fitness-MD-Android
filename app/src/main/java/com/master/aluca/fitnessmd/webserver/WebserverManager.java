package com.master.aluca.fitnessmd.webserver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.EditText;

import com.master.aluca.fitnessmd.Constants;
import com.master.aluca.fitnessmd.auth.AuthenticationLogic;
import com.master.aluca.fitnessmd.datatypes.WeightDayReport;
import com.master.aluca.fitnessmd.util.SharedPreferencesManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;
import im.delight.android.ddp.db.memory.InMemoryDatabase;

/**
 * Created by aluca on 11/15/16.
 */

public class WebserverManager implements MeteorCallback {
    private static final String LOG_TAG = "Fitness_WebManager";
    private ServerRequest serverRequest;
    AuthenticationLogic mAuthLogicInstance;

    private Context mContext;

    List<NameValuePair> params;
    private SharedPreferencesManager sharedPreferencesManager;

    private AtomicBoolean isMeteorClientConnected = new AtomicBoolean(false);

    private static WebserverManager sWebserverManager = null;

    public static WebserverManager getInstance(Context context) {
        if (sWebserverManager == null) {
            sWebserverManager = new WebserverManager(context);
        }
        return sWebserverManager;
    }

    private WebserverManager(Context context) {
        serverRequest = new ServerRequest();
        mAuthLogicInstance = AuthenticationLogic.getInstance();
        sharedPreferencesManager = SharedPreferencesManager.getInstance(context);
        mContext = context;

        // create a new instance
        MeteorSingleton.createInstance(context, "ws://128.224.108.234:3000/websocket", new InMemoryDatabase());
        // register the callback that will handle events and receive messages

        MeteorSingleton.getInstance().addCallback(this);

        // establish the connection
        if (!MeteorSingleton.getInstance().isConnected()) {
            MeteorSingleton.getInstance().connect();
        }
    }

    public void requestLogin(final EditText _emailText, final EditText _passwordText) {
        Log.d(LOG_TAG, "requestLogin");
        if (!mAuthLogicInstance.isInputValid(null, _emailText, _passwordText)) {
            Log.d(LOG_TAG, "Login failed");
            Intent intent = new Intent("login_result");
            intent.putExtra("login_success",false);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        } else {
            if (isMeteorClientConnected.get()) {
                Log.d(LOG_TAG, "Meteor client connected");
                if (MeteorSingleton.getInstance().isLoggedIn()) {
                    Log.d(LOG_TAG, "Meteor user already logged in. Logging out ...");
                    MeteorSingleton.getInstance().logout();
                } else {
                    Log.d(LOG_TAG, "Meteor user NOT logged in");
                }
                MeteorSingleton.getInstance().loginWithEmail(_emailText.getText().toString(), _passwordText.getText().toString(),
                        new ResultListener() {
                            @Override
                            public void onSuccess(String s) {
                                Log.d(LOG_TAG, "Meteor Login SUCCESS");
                                Intent intent = new Intent("login_result");
                                intent.putExtra("login_success",true);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                            }

                            @Override
                            public void onError(String s, String s1, String s2) {
                                Log.d(LOG_TAG, "Meteor Login ERROR");
                                Intent intent = new Intent("login_result");
                                intent.putExtra("login_success",false);
                                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                            }
                        });

            } else {
                Log.d(LOG_TAG, "Meteor client disconnected");
            }
        }
    }


    public boolean sendPedometerData(long day, int steps, long timeActive) {
        int Hours = (int) (timeActive/(1000 * 60 * 60));
        int Mins = (int) (timeActive/(1000*60)) % 60;
        String diffTimeActive = Hours + ":" + Mins;
        Log.d(LOG_TAG, "sendPedometerData day: " + (new Date(day)) + " >>> steps : " + steps + " >>> timeActive : " + diffTimeActive);
        boolean oRet = false;
        if (day < 1 || steps < 0)
            return oRet;

       /* params = new ArrayList<>();
        params.add(new BasicNameValuePair("day", ""+day));
        params.add(new BasicNameValuePair("steps", "" + steps));
        params.add(new BasicNameValuePair("timeActive", "" + timeActive));
        JSONObject json = serverRequest.putPedometerData("http://" + Constants.LOCALHOST_IP_ADDRESS
                        + ":"
                        + Constants.LOCALHOST_NODEJS_PORT
                        + Constants.NODEJS_PUT_PEDOMETER_ROUTE,
                        params);
        if(json != null){
            try{
                Log.d(LOG_TAG, "try get string response");
                Log.d(LOG_TAG, "json : " + json.toString());
                String jsonstr = json.getString("message");
                Log.d(LOG_TAG, "jsonstr : " + jsonstr);
                if(json.getBoolean("res")){
                    oRet = true;
                    Log.d(LOG_TAG, "json.getBoolean");
                } else {
                    int resultCode = json.getInt("message");
                    if (resultCode == 400) {
                        Log.d(LOG_TAG, "resultCode 400");
                    } else if (resultCode == 401) {
                        Log.d(LOG_TAG, "resultCode 401");
                    } else if (resultCode == 404) {
                        Log.d(LOG_TAG, "resultCode 404");
                    } else {
                        Log.d(LOG_TAG, "other result code");
                    }
                    Log.d(LOG_TAG, "json get boolean else");
                }

            }catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(LOG_TAG, "json is null");
        }*/
        oRet = true;
        Log.d(LOG_TAG, "put pedometer data success");
        Date date = new Date(day);
        Log.d(LOG_TAG, "date : " + date);
        return oRet;
    }

    public boolean requestSignup(EditText nameText, EditText emailText, EditText passwordText) {
        boolean oRet = false;
        if (!mAuthLogicInstance.isInputValid(nameText, emailText, passwordText)) {
            Log.d(LOG_TAG, "signup failed");
            return oRet;
        } else {
//            params = new ArrayList<>();
//            params.add(new BasicNameValuePair("name", nameText.getText().toString()));
//            params.add(new BasicNameValuePair("email", emailText.getText().toString()));
//            params.add(new BasicNameValuePair("password", passwordText.getText().toString()));
//            JSONObject json = serverRequest.requestLogin("http://" + Constants.LOCALHOST_IP_ADDRESS
//                            + ":"
//                            + Constants.LOCALHOST_NODEJS_PORT
//                            + Constants.NODEJS_SIGNUP_ROUTE,
//                    params);
//            if(json != null){
//                try{
//                    Log.d(LOG_TAG, "try get string response");
//                    Log.d(LOG_TAG, "json : " + json.toString());
//                    String jsonstr = json.getString("message");
//                    Log.d(LOG_TAG, "jsonstr : " + jsonstr);
//                    if(json.getBoolean("res")){
//                    /*String token = json.getString("token");
//                    String grav = json.getString("grav");
//                    SharedPreferences.Editor edit = pref.edit();
//                    //Storing Data using SharedPreferences
//                    edit.putString("token", token);
//                    edit.putString("grav", grav);
//                    edit.commit();*/
//                        oRet = true;
//                        Log.d(LOG_TAG, "json.getBoolean");
//                    } else {
//                        Log.d(LOG_TAG, "json get boolean else");
//                    }
//
//                }catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                Log.d(LOG_TAG, "json is null");
//            }
            oRet = true;
            Log.d(LOG_TAG, "signup success");
        }
        return oRet;
    }


    /*
        This method will provide data from the webserver.
        It wil create a WeightDayReport object
            weight - the received weight from the webserver
            day - the day the weight measurement was taken
      */
    public WeightDayReport getWeightFromServer() {
        // TODO - for testing purposes I set oRet to default weight value
        // in production, initialize with -1 as an error code;
        WeightDayReport weightDayReport = new WeightDayReport();

        String connectedUserEmail = sharedPreferencesManager.getEmail();
        Log.d(LOG_TAG, "getWeightFromServer email : " + connectedUserEmail);

        /*params = new ArrayList<>();
        params.add(new BasicNameValuePair("email", connectedUserEmail));
        JSONObject json = serverRequest.requestWeight("http://" + Constants.LOCALHOST_IP_ADDRESS
                        + ":"
                        + Constants.LOCALHOST_NODEJS_PORT
                        + Constants.NODEJS_GET_WEIGHT_ROUTE,
                        params);
        if(json != null){
            try{
                Log.d(LOG_TAG, "try get string response");
                Log.d(LOG_TAG, "json : " + json.toString());
                String jsonstr = json.getString("message");
                Log.d(LOG_TAG, "jsonstr : " + jsonstr);
                if(json.getBoolean("res")){
                    oRet = Constants.WEIGHT_DEFAULT_VALUE;
                    Log.d(LOG_TAG, "json.getBoolean");
                } else {
                    int resultCode = json.getInt("message");
                    if (resultCode == 400) {
                        Log.d(LOG_TAG, "resultCode 400");
                    } else if (resultCode == 401) {
                        Log.d(LOG_TAG, "resultCode 401");
                    } else if (resultCode == 404) {
                        Log.d(LOG_TAG, "resultCode 404");
                    } else {
                        Log.d(LOG_TAG, "other result code");
                    }
                    Log.d(LOG_TAG, "json get boolean else");
                }

            }catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(LOG_TAG, "json is null");
        }
        oRet = Constants.WEIGHT_DEFAULT_VALUE;
        Log.d(LOG_TAG, "getWeightFromServer success");*/
        return weightDayReport;
    }


    public void requestLogout() {
        Log.d(LOG_TAG, "requestLogout");
        if (isMeteorClientConnected.get()) {
            Log.d(LOG_TAG, "Meteor client connected");
            if (MeteorSingleton.getInstance().isLoggedIn()) {
                Log.d(LOG_TAG, "Meteor user already logged in. Logging out ...");
                MeteorSingleton.getInstance().logout();
            } else {
                Log.d(LOG_TAG, "Meteor user NOT logged in");
            }
        }
    }

    @Override
    public void onConnect(boolean signedInAutomatically) {
        Log.d(LOG_TAG, "Meteor DDP onConnect signedInAutomatically : " + signedInAutomatically);
        isMeteorClientConnected.set(true);
    }

    @Override
    public void onDisconnect() {
        Log.d(LOG_TAG, "Meteor DDP onDisconnect");
        isMeteorClientConnected.set(false);

    }

    @Override
    public void onException(Exception e) {
        Log.d(LOG_TAG, "Meteor DDP onException : ");
        e.printStackTrace();

    }

    @Override
    public void onDataAdded(String collectionName, String documentID, String fieldsJson) {
        Log.d(LOG_TAG, "Meteor DDP onDataAdded "
                + "Data added to <" + collectionName + "> in document <" + documentID + ">\n"
                + "    Added: " + fieldsJson);

    }

    @Override
    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        Log.d(LOG_TAG, "Meteor DDP onDataAdded onDataChanged Data changed in <"
                + collectionName + "> in document <" + documentID + "> \n"
                + "    Updated: " + updatedValuesJson
                + "\n    Removed: " + removedValuesJson);
    }

    @Override
    public void onDataRemoved(String collectionName, String documentID) {
        Log.d(LOG_TAG, "Meteor DDP onDataRemoved Data removed from <" + collectionName + "> in document <" + documentID + ">");

    }

    public void destroyMeteor() {
        Log.d(LOG_TAG, "destroyMeteor");
        MeteorSingleton.getInstance().disconnect();
        MeteorSingleton.getInstance().removeCallback(this);
    }
}
