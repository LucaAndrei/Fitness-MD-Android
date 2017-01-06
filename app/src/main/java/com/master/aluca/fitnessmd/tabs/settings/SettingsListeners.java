package com.master.aluca.fitnessmd.tabs.settings;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.master.aluca.fitnessmd.Constants;
import com.master.aluca.fitnessmd.MainActivity;
import com.master.aluca.fitnessmd.PairDeviceActivity;
import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.util.SharedPreferencesManager;
import com.master.aluca.fitnessmd.webserver.WebserverManager;

/**
 * Created by aluca on 11/3/16.
 */
public class SettingsListeners {

    private static final String LOG_TAG = "Fitness_SettingsLsnr";

    private static SettingsListeners mInstance = null;


    private Context mContext = null;
    private MainActivity mMainActivity;
    private SharedPreferencesManager sharedPreferencesManager;
    private WebserverManager webserverManager;
    Button btnGender;
    Button btnYoB;
    Button btnWeight;
    Button btnHeight;
    Button btnPairDevice;
    Button btnSyncNow;
    Button btnEraseData;
    Button btnLogout;

    /*public static SettingsListeners getInstance(Context context, MainActivity mainActivity) {
        if (mInstance == null) {
            mInstance = new SettingsListeners(context, mainActivity);
        }
        return mInstance;
    }*/

    public SettingsListeners(Context context, MainActivity mainActivity) {
        Log.d(LOG_TAG,"SettingsListeners");
        mContext = context;
        mMainActivity = mainActivity;
        sharedPreferencesManager = SharedPreferencesManager.getInstance(context);
        webserverManager = WebserverManager.getInstance(context);
    }

    public void setup() {
        btnGender = (Button) mMainActivity.findViewById(R.id.btnGender);
        btnYoB = (Button) mMainActivity.findViewById(R.id.btnYoB);
        btnWeight = (Button) mMainActivity.findViewById(R.id.btnWeight);
        btnHeight = (Button) mMainActivity.findViewById(R.id.btnHeight);
        btnPairDevice = (Button) mMainActivity.findViewById(R.id.btnPairDevice);
        btnSyncNow = (Button) mMainActivity.findViewById(R.id.btnSyncNow);
        btnEraseData = (Button) mMainActivity.findViewById(R.id.btnEraseData);
        btnLogout = (Button) mMainActivity.findViewById(R.id.btnLogout);
        initUserData();
        setListeners(mMainActivity);
    }

    protected void setListeners(final MainActivity mainActivity) {
        Log.d(LOG_TAG, "setListeners");


        btnGender.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "btnGender onClick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, AlertDialog.THEME_HOLO_LIGHT);
                builder.setTitle("Gender");
                builder.setSingleChoiceItems(Constants.GENDERS, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sharedPreferencesManager.setGender(Constants.GENDERS[which].toString());
                        btnGender.setText(Html.fromHtml("Gender<br /><small><small>" + Constants.GENDERS[which] + "</small></small>"));
                        Intent genderChangedIntent = new Intent(Constants.GENDER_CHANGED_INTENT);
                        genderChangedIntent.putExtra(Constants.GENDER_CHANGED_INTENT_BUNDLE_KEY, Constants.GENDERS[which]);
                        mContext.sendBroadcast(genderChangedIntent);
                        dialog.dismiss();
                    }
                });
                AlertDialog levelDialog = builder.create();
                levelDialog.show();
            }
        });

        btnHeight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "btnHeight onClick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, AlertDialog.THEME_HOLO_LIGHT);
                LayoutInflater inflater = mainActivity.getLayoutInflater();
                View theView = inflater.inflate(R.layout.layout_single_number_picker, null, false);
                final NumberPicker height_picker = (NumberPicker) theView.findViewById(R.id.single_number_picker);
                final TextView unitsOfMeasurement = (TextView) theView.findViewById(R.id.unitsOfMeasurement);

                Constants.setNumberPickerTextColor(height_picker, mainActivity.getResources().getColor(R.color.tab_menu_background));

                int height = sharedPreferencesManager.getHeight();
                height_picker.setMinValue(Constants.HEIGHT_MIN_VALUE);
                height_picker.setMaxValue(Constants.HEIGHT_MAX_VALUE);
                height_picker.setValue(height);
                unitsOfMeasurement.setText("cm");

                builder.setView(theView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(LOG_TAG, "height OK click");
                                int height = height_picker.getValue();
                                sharedPreferencesManager.setHeight(height);
                                btnHeight.setText(Html.fromHtml("Height<br /><small>" + height + " " + unitsOfMeasurement.getText().toString() + "</small>"));
                                Intent heightChangedIntent = new Intent(Constants.HEIGHT_CHANGED_INTENT);
                                heightChangedIntent .putExtra(Constants.HEIGHT_CHANGED_INTENT_BUNDLE_KEY, height);
                                mContext.sendBroadcast(heightChangedIntent);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOG_TAG, "height Cancel click");
                    }
                });
                AlertDialog levelDialog = builder.create();
                levelDialog.show();
            }
        });

        btnWeight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "btnWeight onClick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, AlertDialog.THEME_HOLO_LIGHT);
                LayoutInflater inflater = mainActivity.getLayoutInflater();
                View theView = inflater.inflate(R.layout.layout_weight_picker, null, false);
                final NumberPicker kg_picker = (NumberPicker) theView.findViewById(R.id.kgPicker);
                final NumberPicker g_picker = (NumberPicker) theView.findViewById(R.id.gPicker);

                Constants.setNumberPickerTextColor(kg_picker, mainActivity.getResources().getColor(R.color.tab_menu_background));
                Constants.setNumberPickerTextColor(g_picker, mainActivity.getResources().getColor(R.color.tab_menu_background));

                float weight = sharedPreferencesManager.getWeight();
                int weight_decimal_part = Math.round(weight%1 * 10);

                kg_picker.setMinValue(Constants.WEIGHT_KG_MIN_VALUE);
                kg_picker.setMaxValue(Constants.WEIGHT_KG_MAX_VALUE);
                g_picker.setMinValue(Constants.WEIGHT_G_MIN_VALUE);
                g_picker.setMaxValue(Constants.WEIGHT_G_MAX_VALUE);

                kg_picker.setValue((int)weight/1);
                g_picker.setValue(weight_decimal_part);


                builder.setView(theView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(LOG_TAG, "weight OK click");
                                float weight = kg_picker.getValue() + g_picker.getValue()/10f;
                                sharedPreferencesManager.setWeight(weight);
                                btnWeight.setText(Html.fromHtml("Weight<br /><small>" + weight + " kg</small>"));

                                Intent weightChangedIntent = new Intent(Constants.WEIGHT_CHANGED_INTENT);
                                weightChangedIntent.putExtra(Constants.WEIGHT_CHANGED_INTENT_BUNDLE_KEY, weight);
                                mContext.sendBroadcast(weightChangedIntent );

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOG_TAG, "weight Cancel click");
                    }
                });
                AlertDialog levelDialog = builder.create();
                levelDialog.show();
            }
        });

        btnYoB.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "btnYoB onClick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, AlertDialog.THEME_HOLO_LIGHT);
                LayoutInflater inflater = mainActivity.getLayoutInflater();
                View theView = inflater.inflate(R.layout.layout_single_number_picker, null, false);
                final NumberPicker yob_picker = (NumberPicker) theView.findViewById(R.id.single_number_picker);

                Constants.setNumberPickerTextColor(yob_picker, mainActivity.getResources().getColor(R.color.tab_menu_background));

                final TextView unitsOfMeasurement = (TextView) theView.findViewById(R.id.unitsOfMeasurement);

                int yob = sharedPreferencesManager.getYearOfBirth();
                yob_picker.setMinValue(Constants.YOB_MIN_VALUE);
                yob_picker.setMaxValue(Constants.YOB_MAX_VALUE);
                yob_picker.setValue(yob);
                unitsOfMeasurement.setText("");

                builder.setView(theView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(LOG_TAG, "yob OK click");
                                int yob = yob_picker.getValue();
                                sharedPreferencesManager.setYearOfBirth(yob);
                                btnYoB.setText(Html.fromHtml("Year of birth<br /><small>" + yob + " " + unitsOfMeasurement.getText().toString() + "</small>"));

                                Intent yobChangedIntent = new Intent(Constants.YOB_CHANGED_INTENT);
                                yobChangedIntent   .putExtra(Constants.YOB_CHANGED_INTENT_BUNDLE_KEY, yob);
                                mContext.sendBroadcast(yobChangedIntent );
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOG_TAG, "yob Cancel click");
                    }
                });
                AlertDialog levelDialog = builder.create();
                levelDialog.show();
            }
        });


        btnPairDevice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "btnPairDevice onClick");
                Intent intent = new Intent(mContext, PairDeviceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "btnLogout onClick");
                webserverManager.requestLogout();
                sharedPreferencesManager.setLoggedIn(false);
                mMainActivity.finish();
            }
        });

        btnSyncNow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "btnSyncNow onClick");
                ProgressDialog mProgressDialog = new ProgressDialog(mainActivity);
                mProgressDialog.setMessage("Receiving data from server");
                mProgressDialog.show();
                if (mMainActivity.getService().getWeightFromServer()) {
                    Constants.displayToastMessage(mContext, "Success getting weight from server");
                    mProgressDialog.dismiss();
                } else {
                    mProgressDialog.dismiss();
                    Constants.displayToastMessage(mContext, "Error getting weight from server");
                }
            }
        });

        btnEraseData.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "btnEraseData onClick");
                final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, AlertDialog.THEME_HOLO_LIGHT);
                builder.setMessage(Html.fromHtml("This action cannot be undone. Are you sure you want to erase all data?"));
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOG_TAG, "btnEraseData OK click");
                        mMainActivity.getService().eraseAllData();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOG_TAG, "btnEraseData Cancel click");
                    }
                });
                AlertDialog levelDialog = builder.create();
                levelDialog.show();


            }
        });
    }

    private void initUserData() {
        Log.d(LOG_TAG,"initUserData");

        String gender = sharedPreferencesManager.getGender();
        btnGender.setText(Html.fromHtml("Gender<br /><small><small>" + gender + "</small></small>"));

        int height = sharedPreferencesManager.getHeight();
        Log.d(LOG_TAG, "height : " + height);
        btnHeight.setText(Html.fromHtml("Height<br /><small>" + height + " cm</small>"));

        float weight = sharedPreferencesManager.getWeight();
        Log.d(LOG_TAG, "weight : " + weight);
        btnWeight.setText(Html.fromHtml("Weight<br /><small>" + weight + " kg</small>"));

        int yob = sharedPreferencesManager.getYearOfBirth();
        Log.d(LOG_TAG, "yob : " + yob);
        btnYoB.setText(Html.fromHtml("Year of birth<br /><small>" + yob + "</small>"));
    }




}
