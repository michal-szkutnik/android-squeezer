/*
 * Copyright (c) 2009 Google Inc.  All Rights Reserved.
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

package uk.org.ngo.squeezer;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import java.util.ArrayList;
import java.util.List;

import uk.org.ngo.squeezer.dialog.ChangeLogDialog;
import uk.org.ngo.squeezer.dialog.TipsDialog;
import uk.org.ngo.squeezer.framework.BaseActivity;
import uk.org.ngo.squeezer.itemlist.AlbumListActivity;
import uk.org.ngo.squeezer.itemlist.ApplicationListActivity;
import uk.org.ngo.squeezer.itemlist.ArtistListActivity;
import uk.org.ngo.squeezer.itemlist.FavoriteListActivity;
import uk.org.ngo.squeezer.itemlist.GenreListActivity;
import uk.org.ngo.squeezer.itemlist.MusicFolderListActivity;
import uk.org.ngo.squeezer.itemlist.PlaylistsActivity;
import uk.org.ngo.squeezer.itemlist.RadioListActivity;
import uk.org.ngo.squeezer.itemlist.YearListActivity;
import uk.org.ngo.squeezer.itemlist.dialog.AlbumViewDialog;
import uk.org.ngo.squeezer.model.ContributorRole;
import uk.org.ngo.squeezer.model.ContributorRoles;
import uk.org.ngo.squeezer.service.ServerVersion;
import uk.org.ngo.squeezer.service.event.HandshakeComplete;

public class HomeActivity extends BaseActivity {

    private static final ContributorRoles ALBUM_ARTISTS = new ContributorRoles(ContributorRole.ALBUMARTIST);
    private static final ServerVersion ARTIST_ROLE_ID_MIN_VERSION = new ServerVersion("7.9.0");

    private final String TAG = "HomeActivity";

    private IconRowAdapter.IconRow albumArtistsRow;

    private IconRowAdapter.IconRow artistsRow;

    private IconRowAdapter.IconRow albumsRow;

    private IconRowAdapter.IconRow songsRow;

    private IconRowAdapter.IconRow genresRow;

    private IconRowAdapter.IconRow yearsRow;

    private IconRowAdapter.IconRow newMusicRow;

    private IconRowAdapter.IconRow musicFolderRow;

    private IconRowAdapter.IconRow randomMixRow;

    private IconRowAdapter.IconRow playlistsRow;

    private IconRowAdapter.IconRow internetRadioRow;

    private IconRowAdapter.IconRow favoritesRow;

    private IconRowAdapter.IconRow myAppsRow;

    private boolean mCanFavorites = false;

    private boolean mCanMusicfolder = false;

    private boolean mCanMyApps = false;

    private boolean mCanRandomplay = false;

    private boolean mCanArtistRoleId;

    private ListView listView;

    private GoogleAnalyticsTracker tracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.item_list);
        listView = (ListView) findViewById(R.id.item_list);

        // Turn off the home icon.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        final SharedPreferences preferences = getSharedPreferences(Preferences.NAME, 0);

        // Enable Analytics if the option is on, and we're not running in debug
        // mode so that debug tests don't pollute the stats.
        if ((!BuildConfig.DEBUG) && preferences.getBoolean(Preferences.KEY_ANALYTICS_ENABLED, true)) {
            Log.v("NowPlayingActivity", "Tracking page view 'HomeActivity");
            // Start the tracker in manual dispatch mode...
            tracker = GoogleAnalyticsTracker.getInstance();
            tracker.startNewSession("UA-26457780-1", this);
            tracker.trackPageView("HomeActivity");
        }

        // Show the change log if necessary.
        ChangeLogDialog changeLog = new ChangeLogDialog(this);
        if (changeLog.isFirstRun()) {
            if (changeLog.isFirstRunEver()) {
                changeLog.skipLogDialog();
            } else {
                changeLog.getThemedLogDialog().show();
            }
        }

        initializeActions();
    }

    private void initializeActions() {
        int id = 0;
        albumArtistsRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_album_artists), R.drawable.ic_artists,
                new Runnable() {
                    public void run() {
                        ArtistListActivity.show(HomeActivity.this, ALBUM_ARTISTS);
                    }
                });
        artistsRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_artists), R.drawable.ic_artists,
                new Runnable() {
                    public void run() {
                        ArtistListActivity.show(HomeActivity.this);
                    }
                });
        albumsRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_albums), R.drawable.ic_albums,
                new Runnable() {
                    public void run() {
                        AlbumListActivity.show(HomeActivity.this);
                    }
                });
        songsRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_songs), R.drawable.ic_songs,
                new Runnable() {
                    public void run() {
                        AlbumListActivity.show(HomeActivity.this);
                    }
                });
        genresRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_genres), R.drawable.ic_genres,
                new Runnable() {
                    public void run() {
                        GenreListActivity.show(HomeActivity.this);
                    }
                });
        yearsRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_years), R.drawable.ic_years,
                new Runnable() {
                    public void run() {
                        YearListActivity.show(HomeActivity.this);
                    }
                });
        newMusicRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_new_music), R.drawable.ic_new_music,
                new Runnable() {
                    public void run() {
                        AlbumListActivity.show(HomeActivity.this,
                                AlbumViewDialog.AlbumsSortOrder.__new);
                    }
                });
        musicFolderRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_music_folder), R.drawable.ic_music_folder,
                new Runnable() {
                    public void run() {
                        MusicFolderListActivity.show(HomeActivity.this);
                    }
                });
        randomMixRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_random_mix), R.drawable.ic_random,
                new Runnable() {
                    public void run() {
                        RandomplayActivity.show(HomeActivity.this);
                    }
                });
        playlistsRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_playlists), R.drawable.ic_playlists,
                new Runnable() {
                    public void run() {
                        PlaylistsActivity.show(HomeActivity.this);
                    }
                });
        internetRadioRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_radios), R.drawable.ic_internet_radio,
                new Runnable() {
                    public void run() {
                        // Uncomment these next two lines as an easy way to check
                        // crash reporting functionality.
                        //String sCrashString = null;
                        //Log.e("MyApp", sCrashString);
                        RadioListActivity.show(HomeActivity.this);
                    }
                });
        favoritesRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_favorites), R.drawable.ic_favorites,
                new Runnable() {
                    public void run() {
                        FavoriteListActivity.show(HomeActivity.this);
                    }
                });
        myAppsRow = new IconRowAdapter.IconRow(id++, getString(R.string.home_item_my_apps), R.drawable.ic_my_apps,
                new Runnable() {
                    public void run() {
                        ApplicationListActivity.show(HomeActivity.this);
                    }
                });
    }

    @MainThread
    public void onEventMainThread(HandshakeComplete event) {
        if (getService() != null) {
            mCanArtistRoleId = event.version.compareTo(ARTIST_ROLE_ID_MIN_VERSION) >= 0;
            mCanFavorites = event.canFavourites;
            mCanMusicfolder = event.canMusicFolders;
            mCanMyApps = event.canMyApps;
            mCanRandomplay = event.canRandomPlay;
        }

        List<IconRowAdapter.IconRow> rows = new ArrayList<>();
        if (mCanArtistRoleId) {
            rows.add(albumArtistsRow);
        }
        rows.add(artistsRow);
        rows.add(albumsRow);
        rows.add(songsRow);
        rows.add(genresRow);
        rows.add(yearsRow);
        rows.add(newMusicRow);
        if (mCanMusicfolder) {
            rows.add(musicFolderRow);
        }
        if (mCanRandomplay) {
            rows.add(randomMixRow);
        }
        rows.add(playlistsRow);
        rows.add(internetRadioRow);
        if (mCanFavorites) {
            rows.add(favoritesRow);
        }
        if (mCanMyApps) {
            rows.add(myAppsRow);
        }

        listView.setAdapter(new IconRowAdapter(this, rows));
        listView.setOnItemClickListener(onHomeItemClick);

        // Show a tip about volume controls, if this is the first time this app
        // has run. TODO: Add more robust and general 'tips' functionality.
        PackageInfo pInfo;
        try {
            final SharedPreferences preferences = getSharedPreferences(Preferences.NAME,
                    0);

            pInfo = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
            if (preferences.getLong("lastRunVersionCode", 0) == 0) {
                new TipsDialog().show(getSupportFragmentManager(), "TipsDialog");
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong("lastRunVersionCode", pInfo.versionCode);
                editor.commit();
            }
        } catch (PackageManager.NameNotFoundException e) {
            // Nothing to do, don't crash.
        }
    }

    private final OnItemClickListener onHomeItemClick = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final IconRowAdapter.IconRow iconRow = (IconRowAdapter.IconRow) parent.getItemAtPosition(position);
            if (iconRow.getData() instanceof Runnable) {
                ((Runnable) iconRow.getData()).run();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Send analytics stats (if enabled).
        if (tracker != null) {
            tracker.dispatch();
            tracker.stopSession();
        }
    }

    public static void show(Context context) {
        final Intent intent = new Intent(context, HomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

}
