package com.master.aluca.fitnessmd.tabs.profile;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.master.aluca.fitnessmd.Constants;
import com.master.aluca.fitnessmd.MainActivity;
import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.bluetooth.DBHelper;
import com.master.aluca.fitnessmd.datatypes.StepsDayReport;
import com.master.aluca.fitnessmd.datatypes.WeightDayReport;
import com.master.aluca.fitnessmd.util.IDataRefreshCallback;
import com.master.aluca.fitnessmd.util.ProfilePhotoUtils;
import com.master.aluca.fitnessmd.util.SharedPreferencesManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;

/**
 * Created by aluca on 11/3/16.
 */
public class Profile {

    private static final String LOG_TAG = "Fitness_Profile";

    private Context mContext = null;
    private static MainActivity mMainActivity;
    private SharedPreferencesManager sharedPreferencesManager;
    static TextView tvName, tvGender, tvAge, tvHeight;
    TextView tvPersonalBestSteps, tvPersonalBestWeight;
    TextView tvPersonalBestStepsDate, tvPersonalBestWeightDate;
    TextView tvAverageSteps, tvAverageWeight;
    ImageView imageViewProfile;

    private static Bitmap image = null;
    private static Bitmap rotateImage = null;


    private DBHelper mDB;

    int personalBestSteps, personalBestWeight;
    int averageSteps, averageWeight;
    private Bitmap profilePicture;

    public Profile(Context context, MainActivity mainActivity) {
        Log.d(LOG_TAG,"Profile");
        mContext = context;
        mMainActivity = mainActivity;
        sharedPreferencesManager = SharedPreferencesManager.getInstance(context);
        sharedPreferencesManager.registerCallback(mCallback);
        if(mDB == null) {
            mDB = new DBHelper(mContext).openWritable();
        }
    }

    /*
        When the users sets Gender, Year of birth or Height from the Settings menu
        the Profile tab does not get updated unless this callback is called.
     */
    private IDataRefreshCallback mCallback = new IDataRefreshCallback() {
        @Override
        public void onDataChanged(String changedDataKey) {
            Log.d(LOG_TAG,"onDataChanged : " + changedDataKey);
            switch(changedDataKey) {
                case Constants.SHARED_PREFS_GENDER_KEY:
                    updateGenderTextView();
                    break;
                case Constants.SHARED_PREFS_YOB_KEY:
                    updateAgeTextView();
                    break;
                case Constants.SHARED_PREFS_HEIGHT_KEY:
                    updateHeightTextView();
                    break;
            }
        }
    };

    private void updateHeightTextView() {
        int height = sharedPreferencesManager.getHeight();
        Log.d(LOG_TAG, "height : " + height);
        tvHeight.setText(height + " cm");
    }

    private void updateAgeTextView() {
        int yob = sharedPreferencesManager.getYearOfBirth();
        Log.d(LOG_TAG, "yob : " + yob);
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        Log.d(LOG_TAG, "currentYear : " + currentYear);
        int age = currentYear - yob;
        Log.d(LOG_TAG, "age : " + age);
        tvAge.setText(age + " yrs");
    }

    private void updateGenderTextView() {
        String gender = sharedPreferencesManager.getGender();
        if (gender != null) {
            Log.d(LOG_TAG, "gender : " + gender.toString());
            tvGender.setText(gender);
        } else {
            tvGender.setText("Gender Not set");
        }
    }

    public void setup() {
        tvName =  (TextView) mMainActivity.findViewById(R.id.tvName);
        tvGender =  (TextView) mMainActivity.findViewById(R.id.tvGender);
        tvAge =  (TextView) mMainActivity.findViewById(R.id.tvAge);
        tvHeight =  (TextView) mMainActivity.findViewById(R.id.tvHeight);

        tvPersonalBestSteps =  (TextView) mMainActivity.findViewById(R.id.tvPersonalBestSteps);
        tvPersonalBestWeight =  (TextView) mMainActivity.findViewById(R.id.tvPersonalBestWeight);

        tvPersonalBestStepsDate = (TextView) mMainActivity.findViewById(R.id.tvPersonalBestStepsDate);
        tvPersonalBestWeightDate = (TextView) mMainActivity.findViewById(R.id.tvPersonalBestWeightDate);

        tvAverageSteps =  (TextView) mMainActivity.findViewById(R.id.tvAverageSteps);
        tvAverageWeight =  (TextView) mMainActivity.findViewById(R.id.tvAverageWeight);

        imageViewProfile = (ImageView) mMainActivity.findViewById(R.id.imageViewProfile);
        imageViewProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "imageView on click");
                final AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity, AlertDialog.THEME_HOLO_LIGHT);
                builder.setTitle("Profile picture");
                builder.setPositiveButton("Choose photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOG_TAG, "Choose from gallery");
                        imageViewProfile.setImageBitmap(null);
                        if (image != null) {
                            image.recycle();
                        }
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                        mMainActivity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.GET_GALLERY_IMAGE);
                    }
                }).setNegativeButton("Take photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(LOG_TAG, "Take photo click");
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        mMainActivity.startActivityForResult(cameraIntent, Constants.TAKE_PHOTO);
                    }
                });
                AlertDialog levelDialog = builder.create();
                levelDialog.show();
            }
        });

        initUserData();
    }



    private void initUserData() {
        Log.d(LOG_TAG,"initUserData");

        String name = sharedPreferencesManager.getUserName();
        if (name != null) {
            Log.d(LOG_TAG, "name : " + name.toString());
            tvName.setText(name);
        } else {
            tvName.setText("Name Not set.Error");
        }

        updateGenderTextView();

        updateAgeTextView();

        updateHeightTextView();

        updateProfilePicture();
    }

    private void updateProfilePicture() {
        Log.d(LOG_TAG, "updateProfilePicture");

        String profilePictureUri = sharedPreferencesManager.getProfilePictureURI();
        Log.d(LOG_TAG, "profilePictureUri : " + profilePictureUri);
        if (profilePictureUri != null) {
            Bitmap profilePhoto = ProfilePhotoUtils.getProfilePicFromGallery(mContext.getContentResolver(), Uri.parse(profilePictureUri));
            setProfilePicture(profilePhoto);
        } else {
            setDefaultProfilePicture();
        }
    }

    /*
        Whenever this tab is selected, the UI must update with the latest achievements.
     */
    public void syncData() {
        SimpleDateFormat s = new SimpleDateFormat("d MMMM yyyy");
        String day = null;
        StepsDayReport personalBestReport = mDB.getBestSteps();
        int steps = personalBestReport.getSteps();
        tvPersonalBestSteps.setText(String.valueOf(steps) + " steps");

        day = s.format(new Date(personalBestReport.getDay()));
        tvPersonalBestStepsDate.setText(day);


        WeightDayReport weightBestReport = mDB.getBestWeight();
        float weight = weightBestReport.getWeight();
        tvPersonalBestWeight.setText(String.valueOf(weight) + " kg");

        day = s.format(new Date(weightBestReport.getDay()));
        Log.d(LOG_TAG,"day : " + day);
        tvPersonalBestWeightDate.setText(day);

        StepsDayReport averageStepsRaport = mDB.getAverageSteps();
        int averageSteps = averageStepsRaport.getSteps();
        tvAverageSteps.setText(String.valueOf(averageSteps) + " steps");

        WeightDayReport averageWeightRaport = mDB.getAverageWeight();
        float averageWeight = averageWeightRaport.getWeight();
        tvAverageWeight.setText(String.valueOf(averageWeight) + " kg");

    }

    public void setProfilePicture(Bitmap profilePicture) {
        imageViewProfile.setImageBitmap(profilePicture);
    }

    public void setDefaultProfilePicture() {
        imageViewProfile.setImageDrawable(mContext.getResources().getDrawable(R.drawable.user));
        //imageViewProfile.setImageResource(mContext.getResources().getDrawable(R.drawable.user));
    }
}
