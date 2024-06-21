package com.ehsanhaidary.arefmorady;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment implements ServiceConnection, ActionPlaying {

    static final int REQUEST_CODE = 123;
    int MEDIA_PLAYER = 0;
    AudioService audioService;

    //initializing variables
    TextView playerPosition1, playerDuration1, playerPosition2, playerDuration2,
            playerPosition3, playerDuration3, playerPosition4, playerDuration4,
            playerPosition5, playerDuration5, playerPosition6, playerDuration6,
            playerPosition7, playerDuration7, playerPosition8, playerDuration8,
            playerPosition9, playerDuration9, playerPosition10, playerDuration10;
    SeekBar seekBar1, seekBar2, seekBar3, seekBar4, seekBar5,
            seekBar6, seekBar7, seekBar8, seekBar9, seekBar10;
    ImageView playButton1, pauseButton1, favoriteButton1, shareButton1,
            playButton2, pauseButton2, favoriteButton2, shareButton2,
            playButton3, pauseButton3, favoriteButton3, shareButton3,
            playButton4, pauseButton4, favoriteButton4, shareButton4,
            playButton5, pauseButton5, favoriteButton5, shareButton5,
            playButton6, pauseButton6, favoriteButton6, shareButton6,
            playButton7, pauseButton7, favoriteButton7, shareButton7,
            playButton8, pauseButton8, favoriteButton8, shareButton8,
            playButton9, pauseButton9, favoriteButton9, shareButton9,
            playButton10, pauseButton10, favoriteButton10, shareButton10;


    Handler handler1 = new Handler();
    Handler handler2 = new Handler();
    Handler handler3 = new Handler();
    Handler handler4 = new Handler();
    Handler handler5 = new Handler();
    Handler handler6 = new Handler();
    Handler handler7 = new Handler();
    Handler handler8 = new Handler();
    Handler handler9 = new Handler();
    Handler handler10 = new Handler();

    Runnable runnable1, runnable2, runnable3, runnable4, runnable5,
            runnable6, runnable7, runnable8, runnable9, runnable10;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Assign variables
        playerPosition1 = view.findViewById(R.id.player_position);
        playerPosition2 = view.findViewById(R.id.player_position_2);
        playerPosition3 = view.findViewById(R.id.player_position_3);
        playerPosition4 = view.findViewById(R.id.player_position_4);
        playerPosition5 = view.findViewById(R.id.player_position_5);
        playerPosition6 = view.findViewById(R.id.player_position_6);
        playerPosition7 = view.findViewById(R.id.player_position_7);
        playerPosition8 = view.findViewById(R.id.player_position_8);
        playerPosition9 = view.findViewById(R.id.player_position_9);
        playerPosition10 = view.findViewById(R.id.player_position_10);


        playerDuration1 = view.findViewById(R.id.player_duration);
        playerDuration2 = view.findViewById(R.id.player_duration_2);
        playerDuration3 = view.findViewById(R.id.player_duration_3);
        playerDuration4 = view.findViewById(R.id.player_duration_4);
        playerDuration5 = view.findViewById(R.id.player_duration_5);
        playerDuration6 = view.findViewById(R.id.player_duration_6);
        playerDuration7 = view.findViewById(R.id.player_duration_7);
        playerDuration8 = view.findViewById(R.id.player_duration_8);
        playerDuration9 = view.findViewById(R.id.player_duration_9);
        playerDuration10 = view.findViewById(R.id.player_duration_10);


        seekBar1 = view.findViewById(R.id.seek_bar);
        seekBar2 = view.findViewById(R.id.seek_bar_2);
        seekBar3 = view.findViewById(R.id.seek_bar_3);
        seekBar4 = view.findViewById(R.id.seek_bar_4);
        seekBar5 = view.findViewById(R.id.seek_bar_5);
        seekBar6 = view.findViewById(R.id.seek_bar_6);
        seekBar7 = view.findViewById(R.id.seek_bar_7);
        seekBar8 = view.findViewById(R.id.seek_bar_8);
        seekBar9 = view.findViewById(R.id.seek_bar_9);
        seekBar10 = view.findViewById(R.id.seek_bar_10);


        playButton1 = view.findViewById(R.id.play_button);
        playButton2 = view.findViewById(R.id.play_button_2);
        playButton3 = view.findViewById(R.id.play_button_3);
        playButton4 = view.findViewById(R.id.play_button_4);
        playButton5 = view.findViewById(R.id.play_button_5);
        playButton6 = view.findViewById(R.id.play_button_6);
        playButton7 = view.findViewById(R.id.play_button_7);
        playButton8 = view.findViewById(R.id.play_button_8);
        playButton9 = view.findViewById(R.id.play_button_9);
        playButton10 = view.findViewById(R.id.play_button_10);


        pauseButton1 = view.findViewById(R.id.pause_button);
        pauseButton2 = view.findViewById(R.id.pause_button_2);
        pauseButton3 = view.findViewById(R.id.pause_button_3);
        pauseButton4 = view.findViewById(R.id.pause_button_4);
        pauseButton5 = view.findViewById(R.id.pause_button_5);
        pauseButton6 = view.findViewById(R.id.pause_button_6);
        pauseButton7 = view.findViewById(R.id.pause_button_7);
        pauseButton8 = view.findViewById(R.id.pause_button_8);
        pauseButton9 = view.findViewById(R.id.pause_button_9);
        pauseButton10 = view.findViewById(R.id.pause_button_10);


        favoriteButton1 = view.findViewById(R.id.favorite_button);
        favoriteButton2 = view.findViewById(R.id.favorite_button_2);
        favoriteButton3 = view.findViewById(R.id.favorite_button_3);
        favoriteButton4 = view.findViewById(R.id.favorite_button_4);
        favoriteButton5 = view.findViewById(R.id.favorite_button_5);
        favoriteButton6 = view.findViewById(R.id.favorite_button_6);
        favoriteButton7 = view.findViewById(R.id.favorite_button_7);
        favoriteButton8 = view.findViewById(R.id.favorite_button_8);
        favoriteButton9 = view.findViewById(R.id.favorite_button_9);
        favoriteButton10 = view.findViewById(R.id.favorite_button_10);


        shareButton1 = view.findViewById(R.id.share_button);
        shareButton2 = view.findViewById(R.id.share_button_2);
        shareButton3 = view.findViewById(R.id.share_button_3);
        shareButton4 = view.findViewById(R.id.share_button_4);
        shareButton5 = view.findViewById(R.id.share_button_5);
        shareButton6 = view.findViewById(R.id.share_button_6);
        shareButton7 = view.findViewById(R.id.share_button_7);
        shareButton8 = view.findViewById(R.id.share_button_8);
        shareButton9 = view.findViewById(R.id.share_button_9);
        shareButton10 = view.findViewById(R.id.share_button_10);


        try {
            if (Global.getStat(getActivity(), "favorite_state1").equals(null)) {
            }
        } catch (Exception e) {
            Global.saveState(getActivity(), "favorite_state1", "border");
            Global.saveState(getActivity(), "favorite_state2", "border");
            Global.saveState(getActivity(), "favorite_state3", "border");
            Global.saveState(getActivity(), "favorite_state4", "border");
            Global.saveState(getActivity(), "favorite_state5", "border");
            Global.saveState(getActivity(), "favorite_state6", "border");
            Global.saveState(getActivity(), "favorite_state7", "border");
            Global.saveState(getActivity(), "favorite_state8", "border");
            Global.saveState(getActivity(), "favorite_state9", "border");
            Global.saveState(getActivity(), "favorite_state10", "border");

        }


        //get duration of player
        setMediaPlayerDurations();


        //all play buttons
        playButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MEDIA_PLAYER != 1) {
                    setMaxToZero();
                    MEDIA_PLAYER = 1;
                    audioService.createMediaPlayer(MEDIA_PLAYER);
                }
                playAudio();
                setSeekBar();
            }
        });

        playButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MEDIA_PLAYER != 2) {
                    setMaxToZero();
                    MEDIA_PLAYER = 2;
                    audioService.createMediaPlayer(MEDIA_PLAYER);
                }
                playAudio();
                setSeekBar();
            }
        });

        playButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MEDIA_PLAYER != 3) {
                    setMaxToZero();
                    MEDIA_PLAYER = 3;
                    audioService.createMediaPlayer(MEDIA_PLAYER);
                }
                playAudio();
                setSeekBar();
            }
        });


        playButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MEDIA_PLAYER != 4) {
                    setMaxToZero();
                    MEDIA_PLAYER = 4;
                    audioService.createMediaPlayer(MEDIA_PLAYER);
                }
                playAudio();
                setSeekBar();
            }
        });

        playButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MEDIA_PLAYER != 5) {
                    setMaxToZero();
                    MEDIA_PLAYER = 5;
                    audioService.createMediaPlayer(MEDIA_PLAYER);
                }
                playAudio();
                setSeekBar();
            }
        });

        playButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MEDIA_PLAYER != 6) {
                    setMaxToZero();
                    MEDIA_PLAYER = 6;
                    audioService.createMediaPlayer(MEDIA_PLAYER);
                }
                playAudio();
                setSeekBar();
            }
        });

        playButton7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MEDIA_PLAYER != 7) {
                    setMaxToZero();
                    MEDIA_PLAYER = 7;
                    audioService.createMediaPlayer(MEDIA_PLAYER);
                }
                playAudio();
                setSeekBar();
            }
        });

        playButton8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MEDIA_PLAYER != 8) {
                    setMaxToZero();
                    MEDIA_PLAYER = 8;
                    audioService.createMediaPlayer(MEDIA_PLAYER);
                }
                playAudio();
                setSeekBar();
            }
        });

        playButton9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MEDIA_PLAYER != 9) {
                    setMaxToZero();
                    MEDIA_PLAYER = 9;
                    audioService.createMediaPlayer(MEDIA_PLAYER);
                }
                playAudio();
                setSeekBar();
            }
        });

        playButton10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MEDIA_PLAYER != 10) {
                    setMaxToZero();
                    MEDIA_PLAYER = 10;
                    audioService.createMediaPlayer(MEDIA_PLAYER);
                }
                playAudio();
                setSeekBar();
            }
        });


        // all pause buttons
        pauseButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioService.showNotification(R.drawable.play_arrow_24, true);
                pauseButton1.setVisibility(View.GONE);
                playButton1.setVisibility(View.VISIBLE);
                audioService.pause();
                handler1.removeCallbacks(runnable1);
            }
        });
        pauseButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioService.showNotification(R.drawable.play_arrow_24, true);
                pauseButton2.setVisibility(View.GONE);
                playButton2.setVisibility(View.VISIBLE);
                audioService.pause();
                handler2.removeCallbacks(runnable2);
            }
        });
        pauseButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioService.showNotification(R.drawable.play_arrow_24, true);
                pauseButton3.setVisibility(View.GONE);
                playButton3.setVisibility(View.VISIBLE);
                audioService.pause();
                handler3.removeCallbacks(runnable3);
            }
        });
        pauseButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioService.showNotification(R.drawable.play_arrow_24, true);
                pauseButton4.setVisibility(View.GONE);
                playButton4.setVisibility(View.VISIBLE);
                audioService.pause();
                handler4.removeCallbacks(runnable4);
            }
        });
        pauseButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioService.showNotification(R.drawable.play_arrow_24, true);
                pauseButton5.setVisibility(View.GONE);
                playButton5.setVisibility(View.VISIBLE);
                audioService.pause();
                handler5.removeCallbacks(runnable5);
            }
        });
        pauseButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioService.showNotification(R.drawable.play_arrow_24, true);
                pauseButton6.setVisibility(View.GONE);
                playButton6.setVisibility(View.VISIBLE);
                audioService.pause();
                handler6.removeCallbacks(runnable6);
            }
        });
        pauseButton7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioService.showNotification(R.drawable.play_arrow_24, true);
                pauseButton7.setVisibility(View.GONE);
                playButton7.setVisibility(View.VISIBLE);
                audioService.pause();
                handler7.removeCallbacks(runnable7);
            }
        });
        pauseButton8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioService.showNotification(R.drawable.play_arrow_24, true);
                pauseButton8.setVisibility(View.GONE);
                playButton8.setVisibility(View.VISIBLE);
                audioService.pause();
                handler8.removeCallbacks(runnable8);
            }
        });
        pauseButton9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioService.showNotification(R.drawable.play_arrow_24, true);
                pauseButton9.setVisibility(View.GONE);
                playButton9.setVisibility(View.VISIBLE);
                audioService.pause();
                handler9.removeCallbacks(runnable9);
            }
        });
        pauseButton10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioService.showNotification(R.drawable.play_arrow_24, true);
                pauseButton10.setVisibility(View.GONE);
                playButton10.setVisibility(View.VISIBLE);
                audioService.pause();
                handler10.removeCallbacks(runnable10);
            }
        });


        //all share buttons
        shareButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermissions();
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    copyFileToExternalStorage(R.raw.aref, "aref.mp3");
                    shareAudios("aref", 1);
                }


            }

        });

        shareButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermissions();
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    copyFileToExternalStorage(R.raw.aref_1, "aref_1.mp3");
                    shareAudios("aref_1", 2);
                }

            }
        });

        shareButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermissions();
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    copyFileToExternalStorage(R.raw.aref_2, "aref_2.mp3");
                    shareAudios("aref_2", 3);
                }
            }
        });
        shareButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermissions();
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    copyFileToExternalStorage(R.raw.aref_3, "aref_3.mp3");
                    shareAudios("aref_3", 4);
                }
            }
        });
        shareButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermissions();
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    copyFileToExternalStorage(R.raw.aref_4, "aref_4.mp3");
                    shareAudios("aref_4", 5);
                }

            }
        });
        shareButton6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermissions();
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    copyFileToExternalStorage(R.raw.aref_5, "aref_5.mp3");
                    shareAudios("aref_5", 6);
                }

            }
        });
        shareButton7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermissions();
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    copyFileToExternalStorage(R.raw.aref_6, "aref_6.mp3");
                    shareAudios("aref_6", 7);
                }

            }
        });
        shareButton8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermissions();
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    copyFileToExternalStorage(R.raw.aref_7, "aref_7.mp3");
                    shareAudios("aref_7", 8);
                }

            }
        });
        shareButton9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermissions();
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    copyFileToExternalStorage(R.raw.aref_8, "aref_8.mp3");
                    shareAudios("aref_8", 9);
                }

            }
        });
        shareButton10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                grantPermissions();
                if (ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    copyFileToExternalStorage(R.raw.aref_9, "aref_9.mp3");
                    shareAudios("aref_9", 10);
                }
            }
        });

        // all favorite buttons
        favoriteButton1.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                String state = null;
                try {
                    state = Global.getStat(getActivity(), "favorite_state1");
                    if (state == null) {
                        favoriteButton1.setImageResource(R.drawable.ic_baseline_favorite_24);
                        Global.saveState(getActivity(), "favorite_state1", "fill");
                        return;
                    }

                } catch (Exception ignored) {

                }
                if (state.equals("fill")) {
                    favoriteButton1.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    Global.saveState(getActivity(), "favorite_state1", "border");
                    return;
                } else if (state.equals("border")) {
                    favoriteButton1.setImageResource(R.drawable.ic_baseline_favorite_24);
                    Global.saveState(getActivity(), "favorite_state1", "fill");

                }

            }
        });

        favoriteButton2.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                String state = null;
                try {
                    state = Global.getStat(getActivity(), "favorite_state2");
                    if (state == null) {
                        favoriteButton2.setImageResource(R.drawable.ic_baseline_favorite_24);
                        Global.saveState(getActivity(), "favorite_state2", "fill");
                        return;
                    }

                } catch (Exception ignored) {

                }
                if (state.equals("fill")) {
                    favoriteButton2.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    Global.saveState(getActivity(), "favorite_state2", "border");
                    return;
                } else if (state.equals("border")) {
                    favoriteButton2.setImageResource(R.drawable.ic_baseline_favorite_24);
                    Global.saveState(getActivity(), "favorite_state2", "fill");

                }

            }
        });
        favoriteButton3.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                String state = null;
                try {
                    state = Global.getStat(getActivity(), "favorite_state3");
                    if (state == null) {
                        favoriteButton3.setImageResource(R.drawable.ic_baseline_favorite_24);
                        Global.saveState(getActivity(), "favorite_state3", "fill");
                        return;
                    }

                } catch (Exception ignored) {

                }
                if (state.equals("fill")) {
                    favoriteButton3.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    Global.saveState(getActivity(), "favorite_state3", "border");
                    return;
                } else if (state.equals("border")) {
                    favoriteButton3.setImageResource(R.drawable.ic_baseline_favorite_24);
                    Global.saveState(getActivity(), "favorite_state3", "fill");

                }

            }
        });
        favoriteButton4.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                String state = null;
                try {
                    state = Global.getStat(getActivity(), "favorite_state4");
                    if (state == null) {
                        favoriteButton4.setImageResource(R.drawable.ic_baseline_favorite_24);
                        Global.saveState(getActivity(), "favorite_state4", "fill");
                        return;
                    }

                } catch (Exception ignored) {

                }
                if (state.equals("fill")) {
                    favoriteButton4.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    Global.saveState(getActivity(), "favorite_state4", "border");
                    return;
                } else if (state.equals("border")) {
                    favoriteButton4.setImageResource(R.drawable.ic_baseline_favorite_24);
                    Global.saveState(getActivity(), "favorite_state4", "fill");

                }

            }
        });
        favoriteButton5.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                String state = null;
                try {
                    state = Global.getStat(getActivity(), "favorite_state5");
                    if (state == null) {
                        favoriteButton5.setImageResource(R.drawable.ic_baseline_favorite_24);
                        Global.saveState(getActivity(), "favorite_state5", "fill");
                        return;
                    }

                } catch (Exception ignored) {

                }
                if (state.equals("fill")) {
                    favoriteButton5.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    Global.saveState(getActivity(), "favorite_state5", "border");
                    return;
                } else if (state.equals("border")) {
                    favoriteButton5.setImageResource(R.drawable.ic_baseline_favorite_24);
                    Global.saveState(getActivity(), "favorite_state5", "fill");

                }

            }
        });
        favoriteButton6.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                String state = null;
                try {
                    state = Global.getStat(getActivity(), "favorite_state6");
                    if (state == null) {
                        favoriteButton6.setImageResource(R.drawable.ic_baseline_favorite_24);
                        Global.saveState(getActivity(), "favorite_state6", "fill");
                        return;
                    }

                } catch (Exception ignored) {

                }
                if (state.equals("fill")) {
                    favoriteButton6.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    Global.saveState(getActivity(), "favorite_state6", "border");
                    return;
                } else if (state.equals("border")) {
                    favoriteButton6.setImageResource(R.drawable.ic_baseline_favorite_24);
                    Global.saveState(getActivity(), "favorite_state6", "fill");

                }

            }
        });
        favoriteButton7.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                String state = null;
                try {
                    state = Global.getStat(getActivity(), "favorite_state7");
                    if (state == null) {
                        favoriteButton7.setImageResource(R.drawable.ic_baseline_favorite_24);
                        Global.saveState(getActivity(), "favorite_state7", "fill");
                        return;
                    }

                } catch (Exception ignored) {

                }
                if (state.equals("fill")) {
                    favoriteButton7.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    Global.saveState(getActivity(), "favorite_state7", "border");
                    return;
                } else if (state.equals("border")) {
                    favoriteButton7.setImageResource(R.drawable.ic_baseline_favorite_24);
                    Global.saveState(getActivity(), "favorite_state7", "fill");

                }

            }
        });
        favoriteButton8.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                String state = null;
                try {
                    state = Global.getStat(getActivity(), "favorite_state8");
                    if (state == null) {
                        favoriteButton8.setImageResource(R.drawable.ic_baseline_favorite_24);
                        Global.saveState(getActivity(), "favorite_state8", "fill");
                        return;
                    }

                } catch (Exception ignored) {

                }
                if (state.equals("fill")) {
                    favoriteButton8.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    Global.saveState(getActivity(), "favorite_state8", "border");
                    return;
                } else if (state.equals("border")) {
                    favoriteButton8.setImageResource(R.drawable.ic_baseline_favorite_24);
                    Global.saveState(getActivity(), "favorite_state8", "fill");

                }

            }
        });
        favoriteButton9.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                String state = null;
                try {
                    state = Global.getStat(getActivity(), "favorite_state9");
                    if (state == null) {
                        favoriteButton9.setImageResource(R.drawable.ic_baseline_favorite_24);
                        Global.saveState(getActivity(), "favorite_state9", "fill");
                        return;
                    }

                } catch (Exception ignored) {

                }
                if (state.equals("fill")) {
                    favoriteButton9.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    Global.saveState(getActivity(), "favorite_state9", "border");
                    return;
                } else if (state.equals("border")) {
                    favoriteButton9.setImageResource(R.drawable.ic_baseline_favorite_24);
                    Global.saveState(getActivity(), "favorite_state9", "fill");

                }

            }
        });
        favoriteButton10.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {

                String state = null;
                try {
                    state = Global.getStat(getActivity(), "favorite_state10");
                    if (state == null) {
                        favoriteButton10.setImageResource(R.drawable.ic_baseline_favorite_24);
                        Global.saveState(getActivity(), "favorite_state10", "fill");
                        return;
                    }

                } catch (Exception ignored) {

                }
                if (state.equals("fill")) {
                    favoriteButton10.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    Global.saveState(getActivity(), "favorite_state10", "border");
                    return;
                } else if (state.equals("border")) {
                    favoriteButton10.setImageResource(R.drawable.ic_baseline_favorite_24);
                    Global.saveState(getActivity(), "favorite_state10", "fill");

                }

            }
        });


        // this try catch sets the red heart at start up
        try {
            {
                String state = Global.getStat(getActivity(), "favorite_state1");
                if (state.equals("fill")) {
                    favoriteButton1.setImageResource(R.drawable.ic_baseline_favorite_24);
                } else if (state.equals("border")) {
                    favoriteButton1.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }
            {
                String state = Global.getStat(getActivity(), "favorite_state2");
                if (state.equals("fill")) {
                    favoriteButton2.setImageResource(R.drawable.ic_baseline_favorite_24);
                } else if (state.equals("border")) {
                    favoriteButton2.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }
            {
                String state = Global.getStat(getActivity(), "favorite_state3");
                if (state.equals("fill")) {
                    favoriteButton3.setImageResource(R.drawable.ic_baseline_favorite_24);
                } else if (state.equals("border")) {
                    favoriteButton3.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }
            {
                String state = Global.getStat(getActivity(), "favorite_state4");
                if (state.equals("fill")) {
                    favoriteButton4.setImageResource(R.drawable.ic_baseline_favorite_24);
                } else if (state.equals("border")) {
                    favoriteButton4.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }
            {
                String state = Global.getStat(getActivity(), "favorite_state5");
                if (state.equals("fill")) {
                    favoriteButton5.setImageResource(R.drawable.ic_baseline_favorite_24);
                } else if (state.equals("border")) {
                    favoriteButton5.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }
            {
                String state = Global.getStat(getActivity(), "favorite_state6");
                if (state.equals("fill")) {
                    favoriteButton6.setImageResource(R.drawable.ic_baseline_favorite_24);
                } else if (state.equals("border")) {
                    favoriteButton6.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }
            {
                String state = Global.getStat(getActivity(), "favorite_state7");
                if (state.equals("fill")) {
                    favoriteButton7.setImageResource(R.drawable.ic_baseline_favorite_24);
                } else if (state.equals("border")) {
                    favoriteButton7.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }
            {
                String state = Global.getStat(getActivity(), "favorite_state8");
                if (state.equals("fill")) {
                    favoriteButton8.setImageResource(R.drawable.ic_baseline_favorite_24);
                } else if (state.equals("border")) {
                    favoriteButton8.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }
            {
                String state = Global.getStat(getActivity(), "favorite_state9");
                if (state.equals("fill")) {
                    favoriteButton9.setImageResource(R.drawable.ic_baseline_favorite_24);
                } else if (state.equals("border")) {
                    favoriteButton9.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }
            {
                String state = Global.getStat(getActivity(), "favorite_state10");
                if (state.equals("fill")) {
                    favoriteButton10.setImageResource(R.drawable.ic_baseline_favorite_24);
                } else if (state.equals("border")) {
                    favoriteButton10.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }

        } catch (Exception e) {

        }

// this changes the seekbar while its playing
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioService.seekTo(progress);
                }
                playerPosition1.setText(convertFormat(audioService.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioService.seekTo(progress);
                }
                playerPosition2.setText(convertFormat(audioService.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioService.seekTo(progress);
                }
                playerPosition3.setText(convertFormat(audioService.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioService.seekTo(progress);
                }
                playerPosition4.setText(convertFormat(audioService.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar5.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioService.seekTo(progress);
                }
                playerPosition5.setText(convertFormat(audioService.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar6.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioService.seekTo(progress);
                }
                playerPosition6.setText(convertFormat(audioService.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar7.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioService.seekTo(progress);
                }
                playerPosition7.setText(convertFormat(audioService.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar8.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioService.seekTo(progress);
                }
                playerPosition8.setText(convertFormat(audioService.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar9.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioService.seekTo(progress);
                }
                playerPosition9.setText(convertFormat(audioService.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar10.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioService.seekTo(progress);
                }
                playerPosition10.setText(convertFormat(audioService.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }


    @SuppressLint("DefaultLocale")
    private String convertFormat(int duration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

       /* outState.putInt("media_player", MEDIA_PLAYER);
        if (audioService.isPlaying()) {
            outState.putInt("saveInt1", audioService.getCurrentPosition());
            outState.putString("saveString1", "playing");
        } else if (!audioService.isPlaying()) {
            outState.putString("saveString1", "notPlaying");
            if (MEDIA_PLAYER > 0) {
                if (audioService.getCurrentPosition() > 0) {
                    outState.putInt("saveInt1", audioService.getCurrentPosition());
                }
            }
        }

        ifAudioIsPlaying();*/

    }


    //sharing audios
    private void shareAudios(String name, int whichAudio) {

        File filePath = Environment.getExternalStorageDirectory();
        File dir = new File(filePath.getAbsolutePath() + "/IslamicAudio/" + name + ".mp3");

        final Uri data = FileProvider.getUriForFile(getContext(),
                "com.ehsanhaidary.arefmorady.provider", dir);
        getContext().grantUriPermission(getContext().getPackageName(), data,
                Intent.FLAG_GRANT_READ_URI_PERMISSION);
        final Intent intent = new Intent(Intent.ACTION_SEND).
                putExtra(Intent.EXTRA_STREAM, data).
                setType("audio/*").
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        switch (whichAudio) {
            case 1:
                intent.putExtra(Intent.EXTRA_TEXT, "جوابات و احکام: نوشتن 786، تقدیر، احتلام، اعتکاف و گروی.");
                break;
            case 2:
                intent.putExtra(Intent.EXTRA_TEXT, "جوابات و احکام: آذان، اذکار وضو و نماز، آرایش زن، ازدواج و تراویح.");
                break;
            case 3:
                intent.putExtra(Intent.EXTRA_TEXT, "جوابات و احکام: تعویذ، گروی، شلوار تنگ، مال دزدی...");
                break;
            case 4:
                intent.putExtra(Intent.EXTRA_TEXT, "جوابات و احکام: استفراغ در رمضان، اسفند، صدقه، تلویزیون، طریقت.");
                break;
            case 5:
                intent.putExtra(Intent.EXTRA_TEXT, "جوابات و احکام: صیغه، عقیقه، عکاسی، فاتحه خوانی، کشتن جادوگر.");
                break;
            case 6:
                intent.putExtra(Intent.EXTRA_TEXT, "جوابات و احکام: انواح وحی، مال دزدی، بد ترین نامحرم، نخ ابرو.");
                break;
            case 7:
                intent.putExtra(Intent.EXTRA_TEXT, "جوابات و احکام: رفع یدین، سرودملی بوسیدن قرآن، بوسیدن دست وپا.");
                break;
            case 8:
                intent.putExtra(Intent.EXTRA_TEXT, "جوابات و احکام: بیع وفا، پوشیدن یخن، سیگار، پول دادن به قاری.");
                break;
            case 9:
                intent.putExtra(Intent.EXTRA_TEXT, "جوابات و احکام: ترک جماعت در کرونا، جهاد، زکات، حقوق زن بر مرد.");
                break;
            case 10:
                intent.putExtra(Intent.EXTRA_TEXT, "جوابات و احکام: جماعت تبلیغ، زنا، کارتونی، سریال قیام، موسیقی.");
                break;
        }
        getContext().startActivity(Intent.createChooser(intent, "ارسال توسط:"));

    }


    private void grantPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("اجازه دسترسی به حافظه");
                builder.setMessage("برنامه برای اشتراک گذاری نیاز دارد تا فایل ها را روی حافظه ذخیره کند");
                builder.setPositiveButton("بلی", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(
                                getActivity(),
                                new String[]{
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                },
                                REQUEST_CODE
                        );
                    }
                });
                builder.setNegativeButton("نخیر", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getContext(), "برای اشتراک گذاری دسترسی به حافظه نیاز است.", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                ActivityCompat.requestPermissions(
                        getActivity(),
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        REQUEST_CODE
                );
            }

        }
    }

    private String copyFileToExternalStorage(int resorceId, String resourceName) {
        File filePath = Environment.getExternalStorageDirectory();
        File dir = new File(filePath.getAbsolutePath() + "/IslamicAudio/");
        if (!(dir.exists())) {
            dir.mkdir();
        }

        String pathSDCard = Environment.getExternalStorageDirectory() + "/IslamicAudio/" + resourceName;
        try {
            InputStream in = getResources().openRawResource(resorceId);
            FileOutputStream out = null;
            out = new FileOutputStream(pathSDCard);
            byte[] buff = new byte[1024];
            int read = 0;
            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathSDCard;


    }

    private void copyToExternalStorage() {
        File filePath = Environment.getExternalStorageDirectory();
        File dir = new File(filePath.getAbsolutePath() + "/Demo/");
        dir.mkdir();
        File file = new File(dir, "aref.mp3");
        try {
            OutputStream outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /*private void playAudio(int audiosCouner){
        ifAudioIsPlaying();
        playButton4.setVisibility(View.GONE);
        pauseButton4.setVisibility(View.VISIBLE);
        mediaPlayer4.start();
        seekBar4.setMax(mediaPlayer4.getDuration());
        handler4.postDelayed(runnable4, 0);

    }*/


    private void setPlayerDuration() {
        int duration1 = audioService.getDuration();
        String sDuration1 = convertFormat(duration1);
        playerDuration1.setText(sDuration1);
    }

    private void setRunnableNull() {
        if (runnable1 != null) {
            runnable1 = null;
            audioService.stop();
            audioService.release();
        }
    }

   /* private void setJustOneRunnable(final int whichAudio) {
        runnable1 = new Runnable() {
            @Override
            public void run() {

                switch (whichAudio) {
                    case 1:
                        //set progress on seekBar
                        seekBar1.setProgress(audioService.getCurrentPosition());
                        // handler post delay for 0.5s
                        handler1.postDelayed(this, 500);
                        break;
                    case 2:
                        //set progress on seekBar
                        seekBar2.setProgress(audioService.getCurrentPosition());
                        // handler post delay for 0.5s
                        handler2.postDelayed(this, 500);
                        break;
                }
               *//* //set progress on seekBar
                seekBar1.setProgress(audioService.getCurrentPosition());

                // handler post delay for 0.5s
                handler1.postDelayed(this, 500);*//*
            }
        };
    }*/

    private void setRunnable() {

        switch (MEDIA_PLAYER) {
            case 1:
                runnable1 = new Runnable() {
                    @Override
                    public void run() {
                        //set progress on seekBar
                        seekBar1.setProgress(audioService.getCurrentPosition());

                        // handler post delay for 0.5s
                        handler1.postDelayed(this, 500);
                    }
                };
                break;
            case 2:
                runnable2 = new Runnable() {
                    @Override
                    public void run() {
                        //set progress on seekBar
                        seekBar2.setProgress(audioService.getCurrentPosition());

                        // handler post delay for 0.5s
                        handler2.postDelayed(this, 500);
                    }
                };
                break;
            case 3:
                runnable3 = new Runnable() {
                    @Override
                    public void run() {
                        //set progress on seekBar
                        seekBar3.setProgress(audioService.getCurrentPosition());

                        // handler post delay for 0.5s
                        handler3.postDelayed(this, 500);
                    }
                };
                break;
            case 4:
                runnable4 = new Runnable() {
                    @Override
                    public void run() {
                        //set progress on seekBar
                        seekBar4.setProgress(audioService.getCurrentPosition());

                        // handler post delay for 0.5s
                        handler4.postDelayed(this, 500);
                    }
                };
                break;
            case 5:
                runnable5 = new Runnable() {
                    @Override
                    public void run() {
                        //set progress on seekBar
                        seekBar5.setProgress(audioService.getCurrentPosition());

                        // handler post delay for 0.5s
                        handler5.postDelayed(this, 500);
                    }
                };
                break;
            case 6:
                runnable6 = new Runnable() {
                    @Override
                    public void run() {
                        //set progress on seekBar
                        seekBar6.setProgress(audioService.getCurrentPosition());

                        // handler post delay for 0.5s
                        handler6.postDelayed(this, 500);
                    }
                };
                break;
            case 7:
                runnable7 = new Runnable() {
                    @Override
                    public void run() {
                        //set progress on seekBar
                        seekBar7.setProgress(audioService.getCurrentPosition());

                        // handler post delay for 0.5s
                        handler7.postDelayed(this, 500);
                    }
                };
                break;
            case 8:
                runnable8 = new Runnable() {
                    @Override
                    public void run() {
                        //set progress on seekBar
                        seekBar8.setProgress(audioService.getCurrentPosition());

                        // handler post delay for 0.5s
                        handler8.postDelayed(this, 500);
                    }
                };
                break;
            case 9:
                runnable9 = new Runnable() {
                    @Override
                    public void run() {
                        //set progress on seekBar
                        seekBar9.setProgress(audioService.getCurrentPosition());

                        // handler post delay for 0.5s
                        handler9.postDelayed(this, 500);
                    }
                };
                break;
            case 10:
                runnable10 = new Runnable() {
                    @Override
                    public void run() {
                        //set progress on seekBar
                        seekBar10.setProgress(audioService.getCurrentPosition());

                        // handler post delay for 0.5s
                        handler10.postDelayed(this, 500);
                    }
                };
                break;
        }

    }

    private void setMaxToZero() {
        setPlayButtonVisible();
        switch (MEDIA_PLAYER) {
            case 1:
                audioService.stop();
                seekBar1.setProgress(0);
                handler1.removeCallbacks(runnable1);
//                seekBar1.setMax(0);
                playerPosition1.setText("00:00");
                break;
            case 2:
                audioService.stop();
                seekBar2.setMax(0);
                seekBar2.setProgress(0);
//                handler2.removeCallbacks(runnable2);
                playerPosition2.setText("00:00");
                break;
            case 3:
                audioService.stop();
//                seekBar3.setMax(0);

                seekBar3.setProgress(0);
                handler3.removeCallbacks(runnable3);
                playerPosition3.setText("00:00");
                break;
            case 4:
                audioService.stop();
//                seekBar4.setMax(0);
                seekBar4.setProgress(0);
                handler4.removeCallbacks(runnable4);
                playerPosition4.setText("00:00");
                break;
            case 5:
                audioService.stop();
//                seekBar5.setMax(0);
                seekBar5.setProgress(0);
                handler5.removeCallbacks(runnable5);
                playerPosition5.setText("00:00");
                break;
            case 6:
                audioService.stop();
//                seekBar6.setMax(0);
                seekBar6.setProgress(0);
                handler6.removeCallbacks(runnable6);
                playerPosition6.setText("00:00");
                break;
            case 7:
                audioService.stop();
//                seekBar7.setMax(0);
                seekBar7.setProgress(0);
                handler7.removeCallbacks(runnable7);
                playerPosition7.setText("00:00");
                break;
            case 8:
                audioService.stop();
//                seekBar8.setMax(0);
                seekBar8.setProgress(0);
                handler8.removeCallbacks(runnable8);
                playerPosition8.setText("00:00");
                break;
            case 9:
                audioService.stop();
//                seekBar9.setMax(0);
                seekBar9.setProgress(0);
                handler9.removeCallbacks(runnable9);
                playerPosition9.setText("00:00");
                break;
            case 10:
                audioService.stop();
//                seekBar10.setMax(0);
                seekBar10.setProgress(0);
                handler10.removeCallbacks(runnable10);
                playerPosition10.setText("00:00");
                break;
        }
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return super.getLifecycle();
    }


    // Background Code
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        AudioService.MyBinder myBinder = (AudioService.MyBinder) service;
        audioService = myBinder.getService();
        audioService.setCallBack(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        audioService = null;
    }

    @Override
    public void onResume() {
        Intent intent = new Intent(getContext(), AudioService.class);
        getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unbindService(this);
    }


    void setMediaPlayerDurations() {
        //get duration of player
        playerDuration1.setText("18:37");
        playerDuration2.setText("14:45");
        playerDuration3.setText("06:09");
        playerDuration4.setText("13:12");
        playerDuration5.setText("12:43");
        playerDuration6.setText("13:07");
        playerDuration7.setText("16:15");
        playerDuration8.setText("15:23");
        playerDuration9.setText("14:42");
        playerDuration10.setText("14:10");
    }

    private void startService() {
        Intent audioServiceIntent = new Intent(getContext(), AudioService.class);
//        audioServiceIntent.putExtra("numberOfAudio", numberOfAudio);
        getActivity().startService(audioServiceIntent);
//        audioService.onCompleted();

        audioService.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(audioService, "com", Toast.LENGTH_SHORT).show();
                audioService.onCompletion(mp);
//                setMaxToZero();
//                setRunnableNull();
//                setMax();
//                ifAudioIsPlaying();
                audioCompleted();
            }
        });
    }

    private void audioCompleted() {


        switch (MEDIA_PLAYER) {

            case 1:
                pauseButton1.setVisibility(View.GONE);
                playButton1.setVisibility(View.VISIBLE);
                audioService.setMediaPlayerToZero();
//                seekBar1.setMax(0);
                break;
            case 2:
                pauseButton2.setVisibility(View.GONE);
                playButton2.setVisibility(View.VISIBLE);
                audioService.setMediaPlayerToZero();
//                seekBar1.setMax(0);
                break;
            case 3:
                pauseButton3.setVisibility(View.GONE);
                playButton3.setVisibility(View.VISIBLE);
//                seekBar1.setMax(0);
                audioService.setMediaPlayerToZero();
                break;
            case 4:
                pauseButton4.setVisibility(View.GONE);
                playButton4.setVisibility(View.VISIBLE);
//                seekBar1.setMax(0);
                audioService.setMediaPlayerToZero();
                break;
            case 5:
                pauseButton5.setVisibility(View.GONE);
                playButton5.setVisibility(View.VISIBLE);
//                seekBar1.setMax(0);
                audioService.setMediaPlayerToZero();
                break;
            case 6:
                pauseButton6.setVisibility(View.GONE);
                playButton6.setVisibility(View.VISIBLE);
//                seekBar1.setMax(0);
                audioService.setMediaPlayerToZero();
                break;
            case 7:
                pauseButton7.setVisibility(View.GONE);
                playButton7.setVisibility(View.VISIBLE);
//                seekBar1.setMax(0);
                audioService.setMediaPlayerToZero();
                break;
            case 8:
                pauseButton8.setVisibility(View.GONE);
                playButton8.setVisibility(View.VISIBLE);
//                seekBar1.setMax(0);
                audioService.setMediaPlayerToZero();
                break;
            case 9:
                pauseButton9.setVisibility(View.GONE);
                playButton9.setVisibility(View.VISIBLE);
//                seekBar1.setMax(0);
                audioService.setMediaPlayerToZero();
                break;
            case 10:
                pauseButton10.setVisibility(View.GONE);
                playButton10.setVisibility(View.VISIBLE);
//                seekBar1.setMax(0);
                audioService.setMediaPlayerToZero();
                break;
        }
    }


    private void setPauseButtonVisible() {
        switch (MEDIA_PLAYER) {
            case 1:
                pauseButton1.setVisibility(View.VISIBLE);
                playButton1.setVisibility(View.GONE);
                break;
            case 2:
                pauseButton2.setVisibility(View.VISIBLE);
                playButton2.setVisibility(View.GONE);
                break;
            case 3:
                pauseButton3.setVisibility(View.VISIBLE);
                playButton3.setVisibility(View.GONE);
                break;
            case 4:
                pauseButton4.setVisibility(View.VISIBLE);
                playButton4.setVisibility(View.GONE);
                break;
            case 5:
                pauseButton5.setVisibility(View.VISIBLE);
                playButton5.setVisibility(View.GONE);
                break;
            case 6:
                pauseButton6.setVisibility(View.VISIBLE);
                playButton6.setVisibility(View.GONE);
                break;
            case 7:
                pauseButton7.setVisibility(View.VISIBLE);
                playButton7.setVisibility(View.GONE);
                break;
            case 8:
                pauseButton8.setVisibility(View.VISIBLE);
                playButton8.setVisibility(View.GONE);
                break;
            case 9:
                pauseButton9.setVisibility(View.VISIBLE);
                playButton9.setVisibility(View.GONE);
                break;
            case 10:
                pauseButton10.setVisibility(View.VISIBLE);
                playButton10.setVisibility(View.GONE);
                break;
        }
    }


    private void setPlayButtonVisible() {
        switch (MEDIA_PLAYER) {
            case 1:
                pauseButton1.setVisibility(View.GONE);
                playButton1.setVisibility(View.VISIBLE);
                break;
            case 2:
                pauseButton2.setVisibility(View.GONE);
                playButton2.setVisibility(View.VISIBLE);
                break;
            case 3:
                pauseButton3.setVisibility(View.GONE);
                playButton3.setVisibility(View.VISIBLE);
                break;
            case 4:
                pauseButton4.setVisibility(View.GONE);
                playButton4.setVisibility(View.VISIBLE);
                break;
            case 5:
                pauseButton5.setVisibility(View.GONE);
                playButton5.setVisibility(View.VISIBLE);
                break;
            case 6:
                pauseButton6.setVisibility(View.GONE);
                playButton6.setVisibility(View.VISIBLE);
                break;
            case 7:
                pauseButton7.setVisibility(View.GONE);
                playButton7.setVisibility(View.VISIBLE);
                break;
            case 8:
                pauseButton8.setVisibility(View.GONE);
                playButton8.setVisibility(View.VISIBLE);
                break;
            case 9:
                pauseButton9.setVisibility(View.GONE);
                playButton9.setVisibility(View.VISIBLE);
                break;
            case 10:
                pauseButton10.setVisibility(View.GONE);
                playButton10.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    public void btn_playPauseClicked() {
        if (audioService.isPlaying()) {
            audioService.showNotification(R.drawable.play_arrow_24, true);
            audioService.pause();
            setPlayButtonVisible();
            handler1.removeCallbacks(runnable1);
            handler2.removeCallbacks(runnable2);
            handler3.removeCallbacks(runnable3);
        } else {
            playAudio();
        }
    }

    @Override
    public void btn_prevClicked() {
        if (MEDIA_PLAYER > 1) {
            setMaxToZero();
            MEDIA_PLAYER -= 1;
            audioService.createMediaPlayer(MEDIA_PLAYER);
            playAudio();
            setSeekBar();
        }
    }

    @Override
    public void btn_clearClicked() {
        if (audioService.isPlaying()) {
            audioService.pause();
            setPlayButtonVisible();
        }
        Toast.makeText(audioService, "clear", Toast.LENGTH_SHORT).show();
        audioService.showNotification(R.drawable.pause_24, false);
    }

    @Override
    public void btn_nextClicked() {
        if (MEDIA_PLAYER < 10) {
            setMaxToZero();
            MEDIA_PLAYER += 1;
            audioService.createMediaPlayer(MEDIA_PLAYER);
            playAudio();
            setSeekBar();
        }
    }


    void setSeekBar() {
        switch (MEDIA_PLAYER) {
            case 1:
                seekBar1.setMax(audioService.getDuration());
                handler1.postDelayed(runnable1, 0);
            case 2:
                seekBar2.setMax(audioService.getDuration());
                handler2.postDelayed(runnable2, 0);
            case 3:
                seekBar3.setMax(audioService.getDuration());
                handler3.postDelayed(runnable3, 0);
            case 4:
                seekBar4.setMax(audioService.getDuration());
                handler4.postDelayed(runnable4, 0);
            case 5:
                seekBar5.setMax(audioService.getDuration());
                handler5.postDelayed(runnable5, 0);
            case 6:
                seekBar6.setMax(audioService.getDuration());
                handler6.postDelayed(runnable6, 0);
            case 7:
                seekBar7.setMax(audioService.getDuration());
                handler7.postDelayed(runnable7, 0);
            case 8:
                seekBar8.setMax(audioService.getDuration());
                handler8.postDelayed(runnable8, 0);
            case 9:
                seekBar9.setMax(audioService.getDuration());
                handler9.postDelayed(runnable9, 0);
            case 10:
                seekBar10.setMax(audioService.getDuration());
                handler10.postDelayed(runnable10, 0);
        }
    }

    void playAudio() {
        audioService.showNotification(R.drawable.pause_24, true);
        setRunnable();
        setPauseButtonVisible();
        startService();
        audioService.start();
    }


    public void removeNotification() {
        if (audioService.isPlaying()) {
            setPlayButtonVisible();
            audioService.stop();
        }
        getActivity().stopService(new Intent(getContext(), AudioService.class));
    }

}