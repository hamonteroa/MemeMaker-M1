package com.hamonteroa.mememaker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.widget.Space;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private final static String LOG_TAG = "MainActivity";

    private final static int RC_SELECT_PICTURE_FROM_GALLERY = 0;
    private final static int RC_SELECT_PICTURE_FROM_CAMERA = 1;
    private final static String CONST_IS_GENERATING_MEME = "isGeneratingMeme";
    private final static String CONST_MEME_IMAGE = "memeImage";

    private boolean mIsGeneratingMeme = false;

    private Button saveActionBarButton;

    private ImageView mMemeImageView;

    private EditText mTopEditText;
    private EditText mBottomEditText;

    private Toolbar mPictureSourceToolbar;
    private Space mNr1SpaceToolbar;
    private Space mNr2SpaceToolbar;
    private Space mNr3SpaceToolbar;
    private ImageButton mCameraImageButtonToolbar;
    private ImageButton mGalleryImageButtonToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mMemeImageView = (ImageView) findViewById(R.id.meme_imageView);

        this.mTopEditText = (EditText) findViewById(R.id.top_editText);
        this.mBottomEditText = (EditText) findViewById(R.id.bottom_editText);

        this.mPictureSourceToolbar = (Toolbar) findViewById(R.id.picture_source_toolbar);
        this.mCameraImageButtonToolbar = (ImageButton) findViewById(R.id.camera_imageButton_toolbar);
        this.mGalleryImageButtonToolbar = (ImageButton) findViewById(R.id.gallery_imageButton_toolbar);
        this.mNr1SpaceToolbar = (Space) findViewById(R.id.nr1_space_toolbar);
        this.mNr2SpaceToolbar = (Space) findViewById(R.id.nr2_space_toolbar);
        this.mNr3SpaceToolbar = (Space) findViewById(R.id.nr3_space_toolbar);

        if(savedInstanceState != null) {
            this.mMemeImageView.setImageBitmap((Bitmap) savedInstanceState.getParcelable(CONST_MEME_IMAGE));
            this.mIsGeneratingMeme = savedInstanceState.getBoolean(CONST_IS_GENERATING_MEME);
        }

        this.mGalleryImageButtonToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromGallery();
            }
        });

        this.mCameraImageButtonToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromCamera();
            }
        });

        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            changeToolbarButtonAvailability(this.mCameraImageButtonToolbar, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.mNr3SpaceToolbar.post(new Runnable() {
            @Override
            public void run() {
                setToolbarEvenDistribution();
            }
        });

        setViewGeneratingMeme(mIsGeneratingMeme);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (this.mIsGeneratingMeme) {
            outState.putParcelable(CONST_MEME_IMAGE, ((BitmapDrawable) this.mMemeImageView.getDrawable()).getBitmap());
            outState.putBoolean(CONST_IS_GENERATING_MEME, this.mIsGeneratingMeme);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meme_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.share_menuButton:
                shareMeme();
                return true;

            case R.id.save_menuButton:
                takeScreenshot();
                return true;

            case R.id.cancel_menuButton:
                setViewGeneratingMeme(false);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareMeme() {
        View view = this.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);

        Bitmap bmap = view.getDrawingCache();

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int statusBarHeight= rectgle.top;
        int contentViewTop = this.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop(); /* skip status bar in screenshot */
        Bitmap bitmap = Bitmap.createBitmap(bmap, 0, contentViewTop + statusBarHeight, bmap.getWidth(), bmap.getHeight() - contentViewTop - statusBarHeight, null, true);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
        try {
            file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
        startActivity(Intent.createChooser(share, "Share Image"));

    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            String imagePath = Environment.getExternalStorageDirectory().toString() + File.separator + "Download" + File.separator + now + ".jpg";

            View view = this.getWindow().getDecorView();
            view.setDrawingCacheEnabled(true);

            Bitmap bmap = view.getDrawingCache();

            Rect rectgle= new Rect();
            Window window= getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
            int statusBarHeight= rectgle.top;
            int contentViewTop = this.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop(); /* skip status bar in screenshot */
            Bitmap bitmap = Bitmap.createBitmap(bmap, 0, contentViewTop + statusBarHeight, bmap.getWidth(), bmap.getHeight() - contentViewTop - statusBarHeight, null, true);

            File imageFile = new File(imagePath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            view.setDrawingCacheEnabled(false);

        } catch (Throwable e) {
            Log.v(LOG_TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void getImageFromCamera() {
        startActivityForResult(new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE), RC_SELECT_PICTURE_FROM_CAMERA);
    }

    private void getImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RC_SELECT_PICTURE_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            setViewGeneratingMeme(true);

            if (requestCode == RC_SELECT_PICTURE_FROM_GALLERY) {
                try {
                    Uri selectedImageUri = data.getData();
                    ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(selectedImageUri, "r");
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    this.mMemeImageView.setImageBitmap(BitmapFactory.decodeFileDescriptor(fileDescriptor));
                    parcelFileDescriptor.close();



                } catch (IOException ioe) {
                    Log.v(LOG_TAG, "Exception: " + ioe.getMessage());
                }

            } else if (requestCode == RC_SELECT_PICTURE_FROM_CAMERA) {
                this.mMemeImageView.setImageBitmap((Bitmap) data.getExtras().get("data"));
            }

        } else {
            Toast.makeText(this, getString(R.string.problem_getting_image), Toast.LENGTH_SHORT).show();
        }
    }

    private void setToolbarEvenDistribution() {
        int cameraIconWidth = this.mCameraImageButtonToolbar.getWidth();
        int albumIconWidth = this.mGalleryImageButtonToolbar.getWidth();
        int toolbarWidth = this.mPictureSourceToolbar.getWidth();
        int spaceWidth = (toolbarWidth - cameraIconWidth - albumIconWidth) / 3;

        Log.v(LOG_TAG, "setToolbarEvenDistribution, cameraIconWidth: " + cameraIconWidth
                + ", albumIconWidth: " + albumIconWidth
                + ", toolbarWidth: " + toolbarWidth
                + ", spaceWidth: " + spaceWidth
        );

        this.mNr1SpaceToolbar.setMinimumWidth(spaceWidth);
        this.mNr2SpaceToolbar.setMinimumWidth(spaceWidth);
        this.mNr3SpaceToolbar.setMinimumWidth(spaceWidth);
    }

    private void changeToolbarButtonAvailability(ImageButton imageButton, boolean isEnabled) {
        imageButton.setEnabled(isEnabled);

        if (isEnabled) {
            imageButton.clearColorFilter();
        } else {
            imageButton.setColorFilter(0x77000000, PorterDuff.Mode.SRC_IN);
        }
    }

    private void setViewGeneratingMeme(boolean isEnabled) {
        this.mIsGeneratingMeme = isEnabled;

//        ((Button)findViewById(R.id.share_menuButton)).setEnabled(isEnabled);
//        ((Button)findViewById(R.id.save_menuButton)).setEnabled(isEnabled);
//        ((Button)findViewById(R.id.cancel_menuButton)).setEnabled(isEnabled);
        this.mTopEditText.setEnabled(isEnabled);
        this.mBottomEditText.setEnabled(isEnabled);
        this.mPictureSourceToolbar.setEnabled(!isEnabled);

        if (isEnabled) {
            this.mTopEditText.setVisibility(View.VISIBLE);
            this.mBottomEditText.setVisibility(View.VISIBLE);
            this.mPictureSourceToolbar.setVisibility(View.INVISIBLE);

        } else {
            this.mMemeImageView.setImageBitmap(null);
            this.mTopEditText.setVisibility(View.INVISIBLE);
            this.mBottomEditText.setVisibility(View.INVISIBLE);
            this.mPictureSourceToolbar.setVisibility(View.VISIBLE);
        }
    }
}
