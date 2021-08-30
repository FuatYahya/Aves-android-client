package com.example.aves;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.File;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptedFileDataSourceFactory implements DataSource.Factory {

  private Cipher mCipher;
  private SecretKeySpec mSecretKeySpec;
  private IvParameterSpec mIvParameterSpec;
  private TransferListener mTransferListener;
  private File mFile;

  public EncryptedFileDataSourceFactory(Cipher cipher, SecretKeySpec secretKeySpec, IvParameterSpec ivParameterSpec, TransferListener listener, File file) {
    mCipher = cipher;
    mSecretKeySpec = secretKeySpec;
    mIvParameterSpec = ivParameterSpec;
    mTransferListener = listener;
    mFile = file;
  }

  @Override
  public EncryptedFileDataSource createDataSource() {
    return new EncryptedFileDataSource(mCipher, mSecretKeySpec, mIvParameterSpec, mTransferListener, mFile);
  }
}