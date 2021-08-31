package com.example.aves.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Bundle;

import com.example.aves.Helper.EncryptedFileDataSourceFactory;
import com.example.aves.Helper.FileChooser;
import com.example.aves.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ActivityPlayer extends AppCompatActivity {
    private PlayerView playerView;

    public static final String AES_ALGORITHM = "AES";
    public static final String AES_TRANSFORMATION = "AES/CTR/NoPadding";
    private Cipher mCipher;
    private SecretKeySpec mSecretKeySpec;
    private IvParameterSpec mIvParameterSpec;

    private DefaultBandwidthMeter bandwidthMeter;
    private LoadControl loadControl ;
    private RenderersFactory renderersFactory;
    private ExtractorsFactory extractorsFactory;
    private SimpleExoPlayer player;

    private File mFile;
    private Uri mUri;

    String key = "abebe beso bela.";
    String nonce = "chala ha";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Bundle bundle = getIntent().getExtras();
        mUri = Uri.parse(bundle.getString("uri"));

        initializeViews();
    }

    private void initializeViews() {
        playerView = findViewById(R.id.playerView);
    }

    public void getCipher(String key, String nonce){
        byte[] iv = new byte[128/8];
        System.arraycopy(nonce.getBytes(StandardCharsets.UTF_8), 0, iv, 0, nonce.length());
        mSecretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
        mIvParameterSpec = new IvParameterSpec(iv);

        try {
            mCipher = Cipher.getInstance(AES_TRANSFORMATION);
            mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mIvParameterSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializePlayer() {
        bandwidthMeter = new DefaultBandwidthMeter.Builder(getApplicationContext()).build();
        loadControl = new DefaultLoadControl();
        renderersFactory = new DefaultRenderersFactory(this);
        extractorsFactory = new DefaultExtractorsFactory();
        player = new SimpleExoPlayer.Builder(this, renderersFactory, extractorsFactory)
                .setBandwidthMeter(bandwidthMeter)
                .setLoadControl(loadControl).build();

        playerView.setPlayer(player);

        getCipher(key, nonce);

        try {
            MediaSource mediaSource = buildMediaSource(mUri);
            player.setMediaSource(mediaSource);
            player.prepare();
            player.setPlayWhenReady(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        mFile = new File(FileChooser.getPath(getApplicationContext(), mUri));
        DataSource.Factory dataSourceFactory = new EncryptedFileDataSourceFactory(mCipher, mSecretKeySpec, mIvParameterSpec, bandwidthMeter, mFile);
        MediaItem media = MediaItem.fromUri(uri);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory).createMediaSource(media);
        return mediaSource;
    }

    private void releasePlayer(){
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT < 24 || player == null) {
            initializePlayer();
        }
    }

    @Override
    protected void onPause() {
        if(Util.SDK_INT<24){
            releasePlayer();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if(Util.SDK_INT>=24){
            releasePlayer();
        }
        super.onStop();
    }
}