package com.example.aves.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aves.R;


public class MainActivity extends AppCompatActivity {
  private static final int PICKFILE_RESULT_OUT = 2;

  private EditText keyView, nonceView;

  private Uri mUri;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initializeViews();
  }

  private void initializeViews() {
    keyView = findViewById(R.id.key);
    nonceView = findViewById(R.id.nonce);
  }

  public void encrypted_file(View view) {
    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
    chooseFile.setType("*/*");
    chooseFile = Intent.createChooser(chooseFile, "Choose a file");
    startActivityForResult(chooseFile, PICKFILE_RESULT_OUT);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case PICKFILE_RESULT_OUT:
        if (resultCode == -1) {
          mUri = data.getData();
        }
        break;
    }
  }

  public void playVideo(View view) {
    if(mUri != null && keyView.getText().toString().length() > 0 && nonceView.getText().toString().length() > 0) {
      Intent player = new Intent(this, ActivityPlayer.class);
      player.putExtra("key", keyView.getText().toString());
      player.putExtra("nonce", nonceView.getText().toString());
      player.putExtra("uri", mUri.toString());
      startActivity(player);
    } else {
      Toast.makeText(this, "no key, no nonce or no content provided", Toast.LENGTH_SHORT).show();
    }
  }

}
