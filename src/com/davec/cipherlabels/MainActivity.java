/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.davec.cipherlabels;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Collections;
import java.util.List;



/**
 * Main activity for application that displays a button to allow the user to select a Cast device
 * for the Remote Display API.
 */
public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    protected static final String INTENT_EXTRA_CAST_DEVICE = "CastDevice";

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouteButton mMediaRouteButton;
    private int mRouteCount = 0;
    private MediaRouterButtonView mMediaRouterButtonView;
    private String[] mWordValues = new String[25]; //FIXME, change 25 to be an xml param
    // wordcolors = { "blue" , "red" , "yellow" , "black" }
    private String[] mWordColors = new String[25]; //FIXME, change 25 to be an xml param
    private boolean[] mWordStates = new boolean[25]; //FIXME, change 25 to be an xml param
    private String mTeamColor1, mTeamColor2, mNeutralColor, mBombColor;
    private List<Integer> mTeamCards1, mTeamCards2, mNeutralCards, mBombCards;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkGooglePlayServices();

        //initialize game state
        this.initializeGameState();

        setContentView(R.layout.main_layout);
        setFullScreen();

        // example of how to "grab" or use all 25 buttons on second screen
        for(int btnIdx=0; btnIdx<25; btnIdx++) {
            String drawableStrId = String.format("ssButton%02d", btnIdx);
            int drawableIntId = this.getResources().getIdentifier(drawableStrId, "id",
                                                                  this.getPackageName());

            String tmpstr = String.format("would like to set %d to %s\n",drawableIntId,mWordValues[btnIdx]);
            Log.d(TAG,tmpstr);

//            Button tmpBtn = (Button) findViewById(drawableIntId);
            // tmpBtn.setText(mWordValues[btnIdx]);

        }

        TextView titleTextView = (TextView) findViewById(R.id.title);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
//        titleTextView.setTypeface(typeface);

        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(getString(R.string.app_id)))
                .build();
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());

        // Set the MediaRouteButton selector for device discovery.
        mMediaRouterButtonView = (MediaRouterButtonView) findViewById(R.id.media_route_button_view);
        if (mMediaRouterButtonView != null) {
            mMediaRouteButton = mMediaRouterButtonView.getMediaRouteButton();
            mMediaRouteButton.setRouteSelector(mMediaRouteSelector);
        }
    }

    private String[] getRandomWordValues() {
        Resources res = getResources();
        List<String> thewords = Arrays.asList(res.getStringArray(R.array.LargeWordsArray));
        String[] retWords = new String[25]; //FIXME, change 25 to be xml param
        Collections.shuffle(thewords);
        for( int idx=0; idx<25; idx++) {retWords[idx]=thewords.get(idx);}
        return  retWords;
    }

    private void initializeGameState() {
        Random rand = new Random();
        mWordValues = getRandomWordValues();

        // Create list of 0 - 24, shuffled
        List<Integer> theIdxes = new ArrayList<Integer>();
        for( int tidx=0; tidx<25; tidx++) {theIdxes.add(tidx);}
        Collections.shuffle(theIdxes);

        int coinflip = rand.nextInt(2);
        mTeamColor1   = (0==coinflip)        ? "blue" : "red";
        mTeamColor2   = ("red"==mTeamColor1) ? "blue" : "red";
        mNeutralColor = "tan";
        mBombColor    = "black";

        mTeamCards1   = theIdxes.subList( 0, 9); // 9 long
        mTeamCards2   = theIdxes.subList( 9,17); // 8 long
        mNeutralCards = theIdxes.subList(17,24); // 7 long
        mBombCards    = theIdxes.subList(24,25); // 1 long

        // assign correct colors to mWordColors
        for (Integer tmpidx: mTeamCards1)   {mWordColors[tmpidx] = mTeamColor1;}
        for (Integer tmpidx: mTeamCards2)   {mWordColors[tmpidx] = mTeamColor2;}
        for (Integer tmpidx: mNeutralCards) {mWordColors[tmpidx] = mNeutralColor;}
        for (Integer tmpidx: mBombCards)    {mWordColors[tmpidx] = mBombColor;}

    }




    public void onClickWord(View v) {
        int btnId = v.getId();
        String tmpstr = String.format("btnId = %4d\n", btnId);
        Log.d(TAG, tmpstr);
    }

    private void setFullScreen() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaRouter.removeCallback(mMediaRouterCallback);
    }

    private final MediaRouter.Callback mMediaRouterCallback =
            new MediaRouter.Callback() {
                @Override
                public void onRouteAdded(MediaRouter router, RouteInfo route) {
                    if (++mRouteCount == 1) {
                        // Show the button when a device is discovered.
                        if (mMediaRouterButtonView != null) {
                            mMediaRouterButtonView.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onRouteRemoved(MediaRouter router, RouteInfo route) {
                    if (--mRouteCount == 0) {
                        // Hide the button if there are no devices discovered.
                        if (mMediaRouterButtonView != null) {
                            mMediaRouterButtonView.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onRouteSelected(MediaRouter router, RouteInfo info) {
                    Log.d(TAG, "onRouteSelected");
                    CastDevice castDevice = CastDevice.getFromBundle(info.getExtras());
                    if (castDevice != null) {
                        Intent intent = new Intent(MainActivity.this,
                                CastRemoteDisplayActivity.class);
                        intent.putExtra(INTENT_EXTRA_CAST_DEVICE, castDevice);
                        startActivity(intent);
                    }
                }

                @Override
                public void onRouteUnselected(MediaRouter router, RouteInfo info) {
                }
            };

    /**
     * A utility method to validate that the appropriate version of the Google Play Services is
     * available on the device. If not, it will open a dialog to address the issue. The dialog
     * displays a localized message about the error and upon user confirmation (by tapping on
     * dialog) will direct them to the Play Store if Google Play services is out of date or
     * missing, or to system settings if Google Play services is disabled on the device.
     */
    private boolean checkGooglePlayServices() {
        int googlePlayServicesCheck = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (googlePlayServicesCheck == ConnectionResult.SUCCESS) {
            return true;
        }
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googlePlayServicesCheck, this, 0);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });
        dialog.show();
        return false;
    }

}
