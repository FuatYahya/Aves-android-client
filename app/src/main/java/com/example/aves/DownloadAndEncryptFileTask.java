package com.example.aves;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

public class DownloadAndEncryptFileTask extends AsyncTask<Void, Void, Void> {

  private File inFile;
  private File outFile;
  private Cipher mCipher;

  public DownloadAndEncryptFileTask(File inFile, File outFile, Cipher cipher) {

    this.inFile = inFile;
    this.outFile = outFile;
    mCipher = cipher;
  }

  private void downloadAndEncrypt() throws Exception {

    FileInputStream inputStream = new FileInputStream(inFile);
    FileOutputStream fileOutputStream = new FileOutputStream(outFile);
    CipherOutputStream cipherOutputStream = new CipherOutputStream(fileOutputStream, mCipher);

    byte buffer[] = new byte[1024 * 1024];
    int bytesRead;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      cipherOutputStream.write(buffer, 0, bytesRead);
    }

    inputStream.close();
    cipherOutputStream.close();
    Log.e("enc", "finished");
  }

  @Override
  protected Void doInBackground(Void... params) {
    try {
      downloadAndEncrypt();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    Log.d(getClass().getCanonicalName(), "done");
  }
}
