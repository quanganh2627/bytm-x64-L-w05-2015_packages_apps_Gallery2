/*
 * Copyright (C) 2007 The Android Open Source Project
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
 *
 * This file was modified by Dolby Laboratories, Inc. The portions of the
 * code that are surrounded by "DOLBY..." are copyrighted and
 * licensed separately, as follows:
 *
 * (C) 2011-2012 Dolby Laboratories, Inc.
 * All rights reserved.
 *
 * This program is protected under international and U.S. Copyright laws as
 * an unpublished work. This program is confidential and proprietary to the
 * copyright owners. Reproduction or disclosure, in whole or in part, or the
 * production of derivative works therefrom without the express permission of
 * the copyright owners is prohibited.
 *
 */

package com.android.gallery3d.app;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
// DOLBY_DAP_GUI
import android.dolby.DsClient;
import android.dolby.IDsClientEvents;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
// DOLBY_DAP_GUI END
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ShareActionProvider;

import com.android.gallery3d.R;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;

/**
 * This activity plays a video from a specified URI.
 *
 * The client of this activity can pass a logo bitmap in the intent
 * (KEY_LOGO_BITMAP) to set the action bar logo so the playback
 * process looks more seamlessly integrated with the original
 * activity.
 */
public class MovieActivity extends Activity
/* DOLBY_DAP_GUI INLINE */
            implements CompoundButton.OnCheckedChangeListener
/* DOLBY_DAP_GUI INLINE END */ {
    @SuppressWarnings("unused")
    private static final String TAG = "MovieActivity";
    public static final String KEY_LOGO_BITMAP = "logo-bitmap";
    public static final String KEY_TREAT_UP_AS_BACK = "treat-up-as-back";

    private MoviePlayer mPlayer;
    private boolean mFinishOnCompletion;
    private Uri mUri;
    private boolean mTreatUpAsBack;
// DOLBY_DAP_GUI
    private static final boolean DOLBY_USE_ONOFF_BUTTON = true;
    private ToggleButton mDsOn;
    private DsClient mDsClient;
    private boolean mDsClientConnected;

    private final IDsClientEvents mDsListener = new IDsClientEvents ()
    {

        public void onClientConnected()
        {
            try
            {
                // Test whether the DsClient APIs are OK
                // If we can get the version without exception, the APIs work
                String version = mDsClient.getDsVersion();
                mDsClientConnected = true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                unbindDsClient();
            }
            updateDolbyStateUI();
            mDsOn.setOnCheckedChangeListener(MovieActivity.this);
        }

        public void onClientDisconnected()
        {
            mDsClientConnected = false;
            updateDolbyStateUI();
            mDsOn.setOnCheckedChangeListener(null);
        }

        public void onDsOn(boolean on)
        {
            updateDolbyStateUI();
        }

        public void onProfileSelected(int profile)
        {
        }

        public void onProfileSettingsChanged(int profile)
        {
        }

        public void onProfileNameChanged(int profile, String name)
        {
        }

        public void onVisualizerUpdated()
        {
        }

        public void onEqSettingsChanged(int profile, int preset)
        {
        }

    };
// DOLBY_DAP_GUI END

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setSystemUiVisibility(View rootView) {
        if (ApiHelper.HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.movie_view);
        View rootView = findViewById(R.id.movie_view_root);

        setSystemUiVisibility(rootView);

        Intent intent = getIntent();
        initializeActionBar(intent);
        mFinishOnCompletion = intent.getBooleanExtra(
                MediaStore.EXTRA_FINISH_ON_COMPLETION, true);
        mTreatUpAsBack = intent.getBooleanExtra(KEY_TREAT_UP_AS_BACK, false);
        mPlayer = new MoviePlayer(rootView, this, intent.getData(), savedInstanceState,
                !mFinishOnCompletion) {
            @Override
            public void onCompletion() {
                if (mFinishOnCompletion) {
                    finish();
                }
            }
        };
        if (intent.hasExtra(MediaStore.EXTRA_SCREEN_ORIENTATION)) {
            int orientation = intent.getIntExtra(
                    MediaStore.EXTRA_SCREEN_ORIENTATION,
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            if (orientation != getRequestedOrientation()) {
                setRequestedOrientation(orientation);
            }
        }
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
        winParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        win.setAttributes(winParams);

        // We set the background in the theme to have the launching animation.
        // But for the performance (and battery), we remove the background here.
        win.setBackgroundDrawable(null);
// DOLBY_DAP_GUI
        mDsOn = (ToggleButton) findViewById(R.id.dolby_ds_on);
        if (DOLBY_USE_ONOFF_BUTTON) {
            mDsOn.setEnabled(false);
            mDsClient = new DsClient();
            mDsClient.setEventListener(mDsListener);
            mDsClient.bindDsService(this);
        } else {
            mDsOn.setVisibility(View.GONE);
        }
// DOLBY_DAP_GUI END
    }

    private void setActionBarLogoFromIntent(Intent intent) {
        Bitmap logo = intent.getParcelableExtra(KEY_LOGO_BITMAP);
        if (logo != null) {
            getActionBar().setLogo(
                    new BitmapDrawable(getResources(), logo));
        }
    }
// DOLBY_DAP_GUI
    private void updateDolbyStateUI()
    {
        if (mDsClient != null && mDsClientConnected)
        {
            try {
                mDsOn.setChecked(mDsClient.getDsOn());
                mDsOn.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
                unbindDsClient();
            }
        }
        else
        {
            mDsOn.setEnabled(false);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mDsOn == buttonView) {
            if (mDsClient != null && mDsClientConnected)
            {
                try {
                    if (isChecked != mDsClient.getDsOn())
                    {
                        mDsClient.setDsOn(isChecked);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    unbindDsClient();
                }
            }
        }
    }
// DOLBY_DAP_GUI END

    private void initializeActionBar(Intent intent) {
        mUri = intent.getData();
        final ActionBar actionBar = getActionBar();
        setActionBarLogoFromIntent(intent);
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);

        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        if (title != null) {
            actionBar.setTitle(title);
        } else {
            // Displays the filename as title, reading the filename from the
            // interface: {@link android.provider.OpenableColumns#DISPLAY_NAME}.
            AsyncQueryHandler queryHandler =
                    new AsyncQueryHandler(getContentResolver()) {
                @Override
                protected void onQueryComplete(int token, Object cookie,
                        Cursor cursor) {
                    try {
                        if ((cursor != null) && cursor.moveToFirst()) {
                            String displayName = cursor.getString(0);

                            // Just show empty title if other apps don't set
                            // DISPLAY_NAME
                            actionBar.setTitle((displayName == null) ? "" :
                                    displayName);
                        }
                    } finally {
                        Utils.closeSilently(cursor);
                    }
                }
            };
            queryHandler.startQuery(0, null, mUri,
                    new String[] {OpenableColumns.DISPLAY_NAME}, null, null,
                    null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.movie, menu);

        // Document says EXTRA_STREAM should be a content: Uri
        // So, we only share the video if it's "content:".
        MenuItem shareItem = menu.findItem(R.id.action_share);
        if (ContentResolver.SCHEME_CONTENT.equals(mUri.getScheme())) {
            shareItem.setVisible(true);
            ((ShareActionProvider) shareItem.getActionProvider())
                    .setShareIntent(createShareIntent());
        } else {
            shareItem.setVisible(false);
        }
        return true;
    }

    private Intent createShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, mUri);
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (mTreatUpAsBack) {
                finish();
            } else {
                startActivity(new Intent(this, Gallery.class));
                finish();
            }
            return true;
        } else if (id == R.id.action_share) {
            startActivity(Intent.createChooser(createShareIntent(),
                    getString(R.string.share)));
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        super.onStart();
    }

    @Override
    protected void onStop() {
        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .abandonAudioFocus(null);
        super.onStop();
    }

    @Override
    public void onPause() {
        mPlayer.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        mPlayer.onResume();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPlayer.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        mPlayer.onDestroy();
// DOLBY_DAP_GUI
        unbindDsClient();
// DOLBY_DAP_GUI END
        super.onDestroy();
    }
// DOLBY_DAP_GUI
    private void unbindDsClient() {
        if (mDsClient != null && mDsClientConnected) {
            mDsClientConnected = false;
            if (!isFinishing()) {
                updateDolbyStateUI();
            }
            mDsClient.unBindDsService(this);
        }
    }
// DOLBY_DAP_GUI END

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mPlayer.onKeyDown(keyCode, event)
                || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mPlayer.onKeyUp(keyCode, event)
                || super.onKeyUp(keyCode, event);
    }
}
