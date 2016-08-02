package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.LoginAndSignup.College;
import com.linute.linute.LoginAndSignup.SignUpFragments.CollegeSearch.SignUpCollegeSearch;
import com.linute.linute.R;


/**
 * Created by QiFeng on 7/28/16.
 */
public class SignUpCollegeFragment extends Fragment implements LocationListener, View.OnClickListener {

    public static final String TAG = SignUpCollegeFragment.class.getSimpleName();

    private EditText vEditText;
    private boolean gotResult = false;
    private TextView vNearby;

    private SignUpInfo mSignUpInfo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_up_college, container, false);

        mSignUpInfo = ((SignUpParentFragment)getParentFragment()).getSignUpInfo();
        vNearby = (TextView) root.findViewById(R.id.nearby);
        vNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useLocationSearch();
            }
        });
        vEditText = (EditText) root.findViewById(R.id.search);
        vEditText.setOnClickListener(this);

        root.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSignUpInfo.getCollege() != null) {
                    collegeSelected(mSignUpInfo.getCollege());
                } else {
                    vEditText.setError("Please select your college");
                }
            }
        });


        if (mSignUpInfo.getCollege() != null) {
            vEditText.setText(mSignUpInfo.getCollege().getCollegeName());
        }

        return root;
    }


    private void setCollege(College college) {
        vEditText.setError(null);
        mSignUpInfo.setCollege(college);
        vEditText.setText(college.getCollegeName());
    }

    private static final int REQUEST = 90;

    private Handler mHandler = new Handler();

    public void useLocationSearch() {
        if (getContext() == null) return;
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLocationAndStartSearch();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST);
        }
    }

    private void startFragment(Location location) {

        SignUpParentFragment fragment = (SignUpParentFragment) getParentFragment();
        if (fragment != null) {
            SignUpCollegeSearch search = SignUpCollegeSearch.newInstance(location.getLongitude(), location.getLatitude());
            search.setCollegeSelected(new SignUpCollegeSearch.CollegeSelected() {
                @Override
                public void onCollegeSelected(College college) {
                    setCollege(college);
                }
            });

            fragment.addToTop(search, SignUpCollegeSearch.TAG);
        }
    }


    private LocationManager mLocationManager;

    @SuppressWarnings({"MissingPermission"})
    private void getLocationAndStartSearch() {
        if (getContext() == null) return;
        mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null)
            startFragment(location);
        else if (!isLocationEnabled(getContext()))
            Toast.makeText(getContext(), "Please turn on your location", Toast.LENGTH_SHORT).show();
        else {
            vEditText.setOnClickListener(null);
            vNearby.setText("Retrieving location...");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Failed to retrieve location", Toast.LENGTH_SHORT).show();
                    mLocationManager.removeUpdates(SignUpCollegeFragment.this);
                    vNearby.setText("Nearby colleges");
                    vEditText.setOnClickListener(SignUpCollegeFragment.this);
                }
            }, 15000);

            mLocationManager.requestSingleUpdate(new Criteria(), this, null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (gotResult) {
            gotResult = false;
            getLocationAndStartSearch();
        }
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
            mLocationManager = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gotResult = true;
            } else
                Toast.makeText(getContext(), "Error retrieving your location", Toast.LENGTH_SHORT).show();
        }
    }

    private void collegeSelected(final College college) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Confirm")
                .setMessage("Are you sure you want to set " + college.getCollegeName() + " as your school?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SignUpParentFragment fragment = (SignUpParentFragment) getParentFragment();

                        if (fragment != null) {
                            fragment.addFragment(SignUpEmailFragment.newInstance(college), SignUpEmailFragment.TAG);
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onLocationChanged(Location location) {
        startFragment(location);
        mLocationManager.removeUpdates(this);
        mHandler.removeCallbacksAndMessages(null);
        vEditText.setOnClickListener(this);
        vNearby.setText("Nearby colleges");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    @Override
    public void onClick(View v) {
        SignUpParentFragment fragment = (SignUpParentFragment) getParentFragment();
        if (fragment != null) {
            SignUpCollegeSearch frag = new SignUpCollegeSearch();
            frag.setCollegeSelected(new SignUpCollegeSearch.CollegeSelected() {
                @Override
                public void onCollegeSelected(College college) {
                    setCollege(college);
                }
            });
            fragment.addToTop(frag, SignUpCollegeSearch.TAG);
        }
    }
}
