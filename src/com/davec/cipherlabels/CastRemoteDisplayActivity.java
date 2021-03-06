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
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.common.api.Status;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;


/**
 * <h3>CastRemoteDisplayActivity</h3>
 * <p>
 * This code shows how to create an activity that renders some content on a
 * Cast device using a {@link com.google.android.gms.cast.CastPresentation}.
 * </p>
 * <p>
 * The activity uses the {@link MediaRouter} API to select a cast route
 * using a menu item.
 * When a presentation display is available, we stop
 * showing content in the main activity and instead start a {@link CastRemoteDisplayLocalService}
 * that will create a {@link com.google.android.gms.cast.CastPresentation} to render content on the
 * cast remote display. When the cast remote display is removed, we revert to showing content in
 * the main activity. We also write information about displays and display-related events
 * to the Android log which you can read using <code>adb logcat</code>.
 * </p>
 */
public class CastRemoteDisplayActivity extends AppCompatActivity {

    private final String TAG = "CastRDisplayActivity";

    private final int NUMWORDS = 25;

    // Second screen
    private Toolbar mToolbar;

    // MediaRouter
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;

    private CastDevice mCastDevice;

    // Init things that need to be stored
    private HashMap       mButtonId2Idx = new HashMap(NUMWORDS); //FIXME, change NUMWORDS to be an xml param
    // private HashMap STATE_mButtonId2Idx = new HashMap(NUMWORDS);
    private String[]       mWordValues = new String[NUMWORDS];
    // private String[] STATE_mWordValues = new String[NUMWORDS];
    private int[]       mWordColors = new int[NUMWORDS];
    // private int[] STATE_mWordColors = new int[NUMWORDS];
    private int[]       mWordStates = new int[NUMWORDS];
    // private int[] STATE_mWordStates = new int[NUMWORDS];
    private int mTeamColor1, mTeamColor2, mNeutralColor, mBombColor;
    // private int STATE_mTeamColor1, STATE_mTeamColor2, STATE_mNeutralColor, STATE_mBombColor;
    private ArrayList<Integer> mTeamCards1, mTeamCards2, mNeutralCards, mBombCards;
    // private ArrayList<Integer> STATE_mTeamCards1, STATE_mTeamCards2, STATE_mNeutralCards, STATE_mBombCards;
    private boolean mWordClickEnable = false;
    private int mWordSelected = -1;

    private Button[]       mWordButtons = new Button[NUMWORDS];
    // private Button[] STATE_mWordButtons = new Button[NUMWORDS];

    private int mGameStateInitialized = 0;

    private int[] mButtonIds = {
            R.id.ssButton01,
            R.id.ssButton02,
            R.id.ssButton03,
            R.id.ssButton04,
            R.id.ssButton05,
            R.id.ssButton06,
            R.id.ssButton07,
            R.id.ssButton08,
            R.id.ssButton09,
            R.id.ssButton10,
            R.id.ssButton11,
            R.id.ssButton12,
            R.id.ssButton13,
            R.id.ssButton14,
            R.id.ssButton15,
            R.id.ssButton16,
            R.id.ssButton17,
            R.id.ssButton18,
            R.id.ssButton19,
            R.id.ssButton20,
            R.id.ssButton21,
            R.id.ssButton22,
            R.id.ssButton23,
            R.id.ssButton24,
            R.id.ssButton25
    };
    // private int[] STATE_mButtonIds;

    /**
     * Initialization of the Activity after it is first created. Must at least
     * call {@link android.app.Activity#setContentView setContentView()} to
     * describe what is to be displayed in the screen.
     */
    @Override
    protected void onCreate(Bundle saveState) {
        super.onCreate(saveState);

        setContentView(R.layout.second_screen_layout);
        setFullScreen();
        setupActionBar();

        // Local UI
        // final Button button = (Button) findViewById(R.id.button);
        // button.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        // Change the remote display animation color when the button is clicked
        //        PresentationService presentationService
        //                = (PresentationService) CastRemoteDisplayLocalService.getInstance();
        //        if (presentationService != null) {
        //            presentationService.updateButton();
        //        }
        //    }
        // });

//        this.enableWordButtons(mWordClickEnable);


        if( 0 == mGameStateInitialized ) {
            //initialize game state
            this.initializeGameState();
        }

        if( null != saveState ) {
            //restore game state
            this.restore(saveState);
        }



        View.OnClickListener clickWordListener = new View.OnClickListener() {
            public void onClick(View v) {
                int theId = v.getId();
                int theIdx = (int)mButtonId2Idx.get(theId);
                int theColor = mWordColors[theIdx];

                String tmpstr = String.format("onClickWord: btnId=%4d, btnIdx=%4d, btnColor=%s\n",
                        theId, theIdx, theColor);
                Log.d(TAG, tmpstr);
                if( theIdx == mWordSelected ) {
                    mWordStates[theIdx] = 1;
                    mWordButtons[theIdx].setText("");
                    PresentationService presentationService
                            = (PresentationService) CastRemoteDisplayLocalService.getInstance();
                    if (presentationService != null) {
                        presentationService.updateButton(theIdx, theColor, "");
                    }
                }

                // reset all the local colors
                for(int btnLoopIdx=0; btnLoopIdx<NUMWORDS; btnLoopIdx++) {
                    // assign correct colors to mWordColors
                    mWordButtons[btnLoopIdx].setTextColor(0xFFFFFFFF);
                    mWordButtons[btnLoopIdx].setBackgroundColor(mWordColors[btnLoopIdx]);
                }
                mWordSelected = -1;
                return;
            }
        };

        View.OnLongClickListener longClickWordListener = new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                int theId = v.getId();
                int theIdx = (int)mButtonId2Idx.get(theId);
                for(int btnLoopIdx=0; btnLoopIdx<NUMWORDS; btnLoopIdx++) {
                    // 'black out' all other buttons
                    if( theIdx != btnLoopIdx ) {
                        mWordButtons[btnLoopIdx].setTextColor(0xFF000000);
                        mWordButtons[btnLoopIdx].setBackgroundColor(0xFF000000);
                        // enableWordButtons(mWordClickEnable);
                    }
                }
                mWordSelected = theIdx;
                return true;
            }
        };

        for(int btnIdx=0; btnIdx<NUMWORDS; btnIdx++) {
             String tmpstr = String.format("would like to set %d to %s\n",mButtonIds[btnIdx],mWordValues[btnIdx]);
             Log.d(TAG, tmpstr);

            Button tmpBtn = (Button) findViewById(mButtonIds[btnIdx]);
            mButtonId2Idx.put(mButtonIds[btnIdx], btnIdx);
            mWordButtons[btnIdx] = tmpBtn;
            if(0==mWordStates[btnIdx]){
                mWordButtons[btnIdx].setText(mWordValues[btnIdx]);
            }
            else{
                mWordButtons[btnIdx].setText("");
            }
            mWordButtons[btnIdx].setBackgroundColor(mWordColors[btnIdx]);
            mWordButtons[btnIdx].setOnClickListener(clickWordListener);
            mWordButtons[btnIdx].setOnLongClickListener(longClickWordListener);
        }

        View.OnClickListener clickManualUpdateListener = new View.OnClickListener() {
            public void onClick(View v) {
                PresentationService presentationService
                        = (PresentationService) CastRemoteDisplayLocalService.getInstance();
                for( int btnIdx=0; btnIdx<NUMWORDS; btnIdx++ ) {
                    if (presentationService != null) {
                        if( 0==mWordStates[btnIdx]) {
                            presentationService.setButtonText(btnIdx, mWordValues[btnIdx]);
                        }
                        else{
                            presentationService.setButtonText(btnIdx, " ");
                        }
                    }
                }
            }
        };
        Button tmpManualUpdateBtn = (Button) findViewById(R.id.manualUpdate);
        tmpManualUpdateBtn.setOnClickListener(clickManualUpdateListener);





        // BEGIN boiler plate cast code
        // BEGIN boiler plate cast code
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(getString(R.string.app_id)))
                .build();
        if (isRemoteDisplaying()) {
            // The Activity has been recreated and we have an active remote display session,
            // so we need to set the selected device instance
            CastDevice castDevice = CastDevice
                    .getFromBundle(mMediaRouter.getSelectedRoute().getExtras());
            mCastDevice = castDevice;
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mCastDevice = extras.getParcelable(MainActivity.INTENT_EXTRA_CAST_DEVICE);
            }
        }
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        // END boiler plate cast code
        // END boiler plate cast code
    } // END onCreate for CastRemoteDisplayActivity

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        // Save the user's current game state
        saveState.putSerializable("mButtonId2Idx" , mButtonId2Idx );
        saveState.putSerializable("mWordValues"   , mWordValues   );
        saveState.putSerializable("mWordColors"   , mWordColors   );
        saveState.putSerializable("mWordStates"   , mWordStates   );
        saveState.putSerializable("mTeamColor1"   , mTeamColor1   );
        saveState.putSerializable("mTeamColor2"   , mTeamColor2   );
        saveState.putSerializable("mNeutralColor" , mNeutralColor );
        saveState.putSerializable("mBombColor"    , mBombColor    );
        saveState.putSerializable("mTeamCards1"   , mTeamCards1   );
        saveState.putSerializable("mTeamCards2"   , mTeamCards2   );
        saveState.putSerializable("mNeutralCards" , mNeutralCards );
        saveState.putSerializable("mBombCards"    , mBombCards    );

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(saveState);
    }

    @Override
    public void onRestoreInstanceState(Bundle saveState) {
        this.restore(saveState);
    }

    public void restore(Bundle saveState) {
        if (saveState != null) {
            // places = (ArrayList<HashMap<String,String>>) saveState.getSerializable("places");
            mButtonId2Idx = (HashMap)            saveState.getSerializable("mButtonId2Idx");
            mWordValues   = (String[])           saveState.getSerializable("mWordValues");
            mWordColors   = (int[])              saveState.getSerializable("mWordColors");
            mWordStates   = (int[])              saveState.getSerializable("mWordStates");
            mTeamColor1   = (int)                saveState.getSerializable("mTeamColor1");
            mTeamColor2   = (int)                saveState.getSerializable("mTeamColor2");
            mNeutralColor = (int)                saveState.getSerializable("mNeutralColor");
            mBombColor    = (int)                saveState.getSerializable("mBombColor");
            mTeamCards1   = (ArrayList<Integer>) saveState.getSerializable("mTeamCards1");
            mTeamCards2   = (ArrayList<Integer>) saveState.getSerializable("mTeamCards2");
            mNeutralCards = (ArrayList<Integer>) saveState.getSerializable("mNeutralCards");
            mBombCards    = (ArrayList<Integer>) saveState.getSerializable("mBombCards");
        }
        // Always call the superclass so it can save the view hierarchy state
        super.onRestoreInstanceState(saveState);
    }

    private String[] getRandomWordValues() {
        Resources res = getResources();
        List<String> thewords = Arrays.asList(res.getStringArray(R.array.LargeWordsArray));
        String[] retWords = new String[NUMWORDS]; //FIXME, change NUMWORDS to be xml param
        Collections.shuffle(thewords);
        for( int idx=0; idx<NUMWORDS; idx++) {retWords[idx]=thewords.get(idx);}
        return  retWords;
    }

    private void initializeGameState() {
        Random rand = new Random();
        int blue  = 0xFF0000B5;
        int red   = 0xFFB50000;
        int tan   = 0xFF8D9900;
        int black = 0xFF000000;
        mWordValues = getRandomWordValues();

        // Create list of 0 - 24, shuffled
        List<Integer> theIdxes = new ArrayList<Integer>();
        for( int tidx=0; tidx<NUMWORDS; tidx++) {theIdxes.add(tidx);}
        Collections.shuffle(theIdxes);

        int coinflip = rand.nextInt(2);
        mTeamColor1   = (0==coinflip)      ? blue : red;
        mTeamColor2   = (red==mTeamColor1) ? blue : red;
        mNeutralColor = tan;
        mBombColor    = black;

        mTeamCards1   = new ArrayList<Integer>(theIdxes.subList( 0, 9)); // 9 long
        mTeamCards2   = new ArrayList<Integer>(theIdxes.subList( 9,17)); // 8 long
        mNeutralCards = new ArrayList<Integer>(theIdxes.subList(17,24)); // 7 long
        mBombCards    = new ArrayList<Integer>(theIdxes.subList(24,25)); // 1 long

        // assign correct colors to mWordColors
        for (Integer tmpidx: mTeamCards1)   {mWordColors[tmpidx] = mTeamColor1;}
        for (Integer tmpidx: mTeamCards2)   {mWordColors[tmpidx] = mTeamColor2;}
        for (Integer tmpidx: mNeutralCards) {mWordColors[tmpidx] = mNeutralColor;}
        for (Integer tmpidx: mBombCards)    {mWordColors[tmpidx] = mBombColor;}

        for( int tmpidx=0; tmpidx<NUMWORDS; tmpidx++){mWordStates[tmpidx] = 0;}

    }


//    private void onClickWord(View v) {
//        int btnId = v.getId();
//        int btnIdx = (int)mButtonId2Idx.get(btnId);
//        int btnColor = mWordColors[btnIdx];
//        String tmpstr = String.format("onClickWord: btnId=%4d, btnIdx=%4d, btnColor=%s\n",
//                btnId, btnIdx, btnColor);
//        Log.d(TAG, tmpstr);
//        PresentationService presentationService
//                = (PresentationService) CastRemoteDisplayLocalService.getInstance();
//        if (presentationService != null) {
//            presentationService.updateButton(btnIdx,btnColor, "");
//        }
////        mWordClickEnable = false;
////        this.enableWordButtons(mWordClickEnable);
//    }

//    private void enableWordButtons(boolean enabled){
//        for(int bidx=0; bidx<mButtonIds.length; bidx++){
//            Button tmpBtn = (Button) findViewById(mButtonIds[bidx]);
//            tmpBtn.setEnabled(enabled);
//            tmpBtn.setTextColor(0xFFF0F0F0);
//        }
//    }

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
    }

    private void setFullScreen() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * Create the toolbar menu with the cast button.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        // Return true to show the menu.
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isRemoteDisplaying()) {
            if (mCastDevice != null) {
                startCastService(mCastDevice);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaRouter.removeCallback(mMediaRouterCallback);
    }

    private boolean isRemoteDisplaying() {
        return CastRemoteDisplayLocalService.getInstance() != null;
    }

    private void initError() {
        Toast toast = Toast.makeText(
                getApplicationContext(), R.string.init_error, Toast.LENGTH_SHORT);
        mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
        toast.show();
    }

    /**
     * Utility method to identify if the route information corresponds to the currently
     * selected device.
     *
     * @param info The route information
     * @return Whether the route information corresponds to the currently selected device.
     */
    private boolean isCurrentDevice(RouteInfo info) {
        if (mCastDevice == null) {
            // No device selected
            return false;
        }
        CastDevice device = CastDevice.getFromBundle(info.getExtras());
        if (!device.getDeviceId().equals(mCastDevice.getDeviceId())) {
            // The callback is for a different device
            return false;
        }
        return true;
    }

    private final MediaRouter.Callback mMediaRouterCallback =
            new MediaRouter.Callback() {
                @Override
                public void onRouteSelected(MediaRouter router, RouteInfo info) {
                    // Should not happen since this activity will be closed if there
                    // is no selected route
                }

                @Override
                public void onRouteUnselected(MediaRouter router, RouteInfo info) {
                    if (isRemoteDisplaying()) {
                        CastRemoteDisplayLocalService.stopService();
                    }
                    mCastDevice = null;
                    CastRemoteDisplayActivity.this.finish();
                }
            };

    private void startCastService(CastDevice castDevice) {
        Intent intent = new Intent(CastRemoteDisplayActivity.this,
                CastRemoteDisplayActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                CastRemoteDisplayActivity.this, 0, intent, 0);

        CastRemoteDisplayLocalService.NotificationSettings settings =
                new CastRemoteDisplayLocalService.NotificationSettings.Builder()
                        .setNotificationPendingIntent(notificationPendingIntent).build();

        CastRemoteDisplayLocalService.startService(CastRemoteDisplayActivity.this,
                PresentationService.class, getString(R.string.app_id),
                castDevice, settings,
                new CastRemoteDisplayLocalService.Callbacks() {
                    @Override
                    public void onServiceCreated(
                            CastRemoteDisplayLocalService service) {
                        Log.d(TAG, "onServiceCreated");
                    }

                    @Override
                    public void onRemoteDisplaySessionStarted(
                            CastRemoteDisplayLocalService service) {
                        Log.d(TAG, "onServiceStarted");
                        // PresentationService presentationService
                        //         = (PresentationService) CastRemoteDisplayLocalService.getInstance();
                        // for(int btnIdx=0; btnIdx<NUMWORDS; btnIdx++){
                        //     if( null != presentationService ) {
                        //         presentationService.setButtonText(btnIdx, mWordValues[btnIdx]);
                        //     }
                        // }
                    }

                    @Override
                    public void onRemoteDisplaySessionError(Status errorReason) {
                        int code = errorReason.getStatusCode();
                        Log.d(TAG, "onServiceError: " + errorReason.getStatusCode());
                        initError();

                        mCastDevice = null;
                        CastRemoteDisplayActivity.this.finish();
                    }
                });
    }

}
