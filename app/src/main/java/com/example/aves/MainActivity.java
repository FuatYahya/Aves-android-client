package com.example.aves;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
//import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
//import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.io.File;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

  public static final String AES_ALGORITHM = "AES";
  public static final String AES_TRANSFORMATION = "AES/CTR/NoPadding";

  private static final String ENCRYPTED_FILE_NAME = "encrypted.mp4";
  private static final int MY_PERMISSION_REQUEST_CODE = 1001;
  private static final int PICKFILE_RESULT_IN = 1;
  private static final int PICKFILE_RESULT_OUT = 2;

  private Cipher mCipher;
  private SecretKeySpec mSecretKeySpec;
  private IvParameterSpec mIvParameterSpec;

  private File mEncryptedFile;

  private PlayerView mExoPlayerView;
  private Uri uri;
  private File in, out;

  DefaultBandwidthMeter bandwidthMeter;
//  TrackSelection.Factory videoTrackSelectionFactory;
  TrackSelector trackSelector ;
  LoadControl loadControl ;
  RenderersFactory renderersFactory;
  SimpleExoPlayer player;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mExoPlayerView = findViewById(R.id.exoplayerview);

    bandwidthMeter = new DefaultBandwidthMeter.Builder(getApplicationContext()).build();
//    videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
//    trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
    loadControl = new DefaultLoadControl();
    renderersFactory = new DefaultRenderersFactory(this);
    player = new SimpleExoPlayer.Builder(getApplicationContext())
            .setBandwidthMeter(bandwidthMeter)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build();

    mExoPlayerView.setPlayer(player);


    final byte[] iv = { 65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45 };
    final byte[] key = { 0, 42, 2, 54, 4, 45, 6, 7, 65, 9, 54, 11, 12, 13, 60, 15 };

    mSecretKeySpec = new SecretKeySpec(key, AES_ALGORITHM);
    mIvParameterSpec = new IvParameterSpec(iv);

    try {
      mCipher = Cipher.getInstance(AES_TRANSFORMATION);
      mCipher.init(Cipher.DECRYPT_MODE, mSecretKeySpec, mIvParameterSpec);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean checkPermission(){
    if(ActivityCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED){
      return false;
    }
    return true;
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
    player.release();
  }

  private boolean hasFile() {
    return mEncryptedFile != null
        && mEncryptedFile.exists()
        && mEncryptedFile.length() > 0;
  }

  public void normal_file(View view) {
    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
    chooseFile.setType("*/*");
    chooseFile = Intent.createChooser(chooseFile, "Choose a file");
    startActivityForResult(chooseFile, PICKFILE_RESULT_IN);
  }

  public void encrypted_file(View view) {
    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
    chooseFile.setType("*/*");
    chooseFile = Intent.createChooser(chooseFile, "Choose a file");
    startActivityForResult(chooseFile, PICKFILE_RESULT_OUT);
  }

  public void encryptVideo(View view) {
    try {
      Cipher encryptionCipher = Cipher.getInstance(AES_TRANSFORMATION);
      encryptionCipher.init(Cipher.ENCRYPT_MODE, mSecretKeySpec, mIvParameterSpec);
      // TODO:
      // you need to encrypt a video somehow with the same key and iv...  you can do that yourself and update
      // the ciphers, key and iv used in this demo, or to see it from top to bottom,
      // supply a url to a remote unencrypted file - this method will download and encrypt it
      // this first argument needs to be that url, not null or empty...
      new DownloadAndEncryptFileTask(in, out, encryptionCipher).execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case PICKFILE_RESULT_IN:
        if (resultCode == -1) {
          in = new File(FileChooser.getPath(getApplicationContext(), data.getData()));
        }
        break;
      case PICKFILE_RESULT_OUT:
        if (resultCode == -1) {
          uri = data.getData();
          out = new File(FileChooser.getPath(getApplicationContext(), data.getData()));
        }
        break;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == MY_PERMISSION_REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        DataSource.Factory dataSourceFactory = new EncryptedFileDataSourceFactory(mCipher, mSecretKeySpec, mIvParameterSpec, bandwidthMeter, out);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        try {
//            Uri uri = Uri.fromFile(mEncryptedFile);
          MediaItem media = MediaItem.fromUri(uri);
          MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory).createMediaSource(media);
          player.setMediaSource(videoSource);
          player.prepare();
          player.setPlayWhenReady(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        Toast.makeText(this, "not", Toast.LENGTH_SHORT).show();
      }
    }
  }

  public void playVideo(View view) {
    ActivityCompat.requestPermissions(this, new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    }, MY_PERMISSION_REQUEST_CODE);
  }

}
