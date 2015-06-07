package nyc.c4q.rosmaryfc.meme_ify_me;


import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class MainActivity extends ActionBarActivity {
    private static String logtag = "CameraApp8";
    private static int TAKE_PICTURE = 1;
    private static final int PICK_PICTURE = 2;
    private static final int SAVE_PICTURE = 3;
    private Uri imageUri;
    private Bitmap bitmap;
    protected ImageView imageview;
    private String selectedImagePath;
    private Button editMemeButton;
    Bitmap returnedBitmap;
    private ImageButton cameraButton;
    private ImageButton fromGalleryButton;
    private RadioButton vanillaRadioButton;
    private RadioButton demotivationalRadBtn;
    private Intent vanillaMemeIntent;
    private Intent demotivationalMemeIntent;

    private Bitmap bitmapImage;

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString("SelectedImagePath", selectedImagePath);
        vanillaMemeIntent.putExtra("SelectedImagePath", selectedImagePath);
        demotivationalMemeIntent.putExtra("SelectedImagePath", selectedImagePath);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (  (savedInstanceState != null)) {
            selectedImagePath = savedInstanceState.getString("SelectedImagePath");
        }

        imageview = (ImageView) findViewById(R.id.image);

        cameraButton = (ImageButton) findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(cameraListener);

        fromGalleryButton = (ImageButton) findViewById(R.id.pic_from_gallery_button);
        fromGalleryButton.setOnClickListener(GalleryListener);

        vanillaRadioButton = (RadioButton) findViewById(R.id.vanilla_memes_radBtn);
        demotivationalRadBtn = (RadioButton) findViewById(R.id.demotivational_posters_radBtn);

        vanillaMemeIntent = new Intent(this, VanillaMemeEdit.class);
        demotivationalMemeIntent = new Intent(this, DemotivationalMemeEdit.class);

        editMemeButton = (Button)findViewById(R.id.edit_meme_button);
        editMemeButton.setOnClickListener(editMemeListener);

    }

    private View.OnClickListener cameraListener = new View.OnClickListener() {
        // restore canvas to default

        @Override
        public void onClick(View v) {
            takePhoto(v);
        }
    };

    private View.OnClickListener GalleryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            pickPhoto(v);
        }
    };

    private View.OnClickListener editMemeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(vanillaRadioButton.isChecked()){
                bitmap = decodePhoto(selectedImagePath);
                convertImage(bitmap, vanillaMemeIntent);
                startActivity(vanillaMemeIntent);
            }else if(demotivationalRadBtn.isChecked()) {
                bitmap = decodePhoto(selectedImagePath);
                convertImage(bitmap, demotivationalMemeIntent);
                startActivity(demotivationalMemeIntent);
            } else {
                Toast.makeText(getApplicationContext(),"Select a Meme Type", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //method for requesting image from gallery/camera roll
    public void pickPhoto(View v) {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("image/*");
    startActivityForResult(intent, PICK_PICTURE);

}

    //method for requesting camera to capture image and save it under a new file
    public void takePhoto (View v){
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "picture.jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    //todo: still working on transferring image to next activity
    public void convertImage(Bitmap bitmap, Intent currentIntent){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
        byte[] byteArray = stream.toByteArray();
        currentIntent.putExtra("picture", byteArray);
        startActivity(currentIntent);
    }

            //method for gathering intent information from takePhoto and pickPhoto methods
            // and setting the imageview with correct bitmap, and saving
            @Override
            protected void onActivityResult ( int requestCode, int resultCode, Intent data){
                if (resultCode == RESULT_OK) {
                    if (requestCode == PICK_PICTURE) {
                        selectedImagePath = String.valueOf(data.getData());
                        imageview.setImageBitmap(decodePhoto(selectedImagePath));
                    } else if (requestCode == TAKE_PICTURE) {
                        selectedImagePath = imageUri.toString();
                        imageview.setImageBitmap(decodePhoto(selectedImagePath));
                    }
                    else {
                        super.onActivityResult(requestCode, resultCode, data);
                    }
//                    demotivationalMemeIntent.putExtra("meme_dir", selectedImagePath);
//                    vanillaMemeIntent.putExtra("vanilla_meme_dir", selectedImagePath);
                }

            }

            //requesting image's file path and converting into url and calling the
            // ContentResolver to retrieve image and set it inside a bitmap
        public Bitmap decodePhoto (String path){
            Uri selectedImageUri = Uri.parse(path);
            getContentResolver().notifyChange(selectedImageUri, null);
            ContentResolver cr = getContentResolver();
            bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(cr, selectedImageUri);
                bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true); //added this to make image smaller to pass to next activity

                //show image file path to user
                Toast.makeText(MainActivity.this, selectedImageUri.toString(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(logtag, e.toString());
            }
            return bitmap;
        }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected (MenuItem item){
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }

    public Bitmap drawMeme(View v){
        LinearLayout layout = (LinearLayout) findViewById(R.id.meme_preview);
        layout.setDrawingCacheEnabled(true);
        Bitmap memeBitMap = layout.getDrawingCache();
        Bitmap meme = memeBitMap.copy(Bitmap.Config.ARGB_8888, false);
        layout.buildDrawingCache();
        layout.destroyDrawingCache();
        return meme;
    }

    public void onRadioButtonClicked (View view){
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.vanilla_memes_radBtn:
                if (checked)
                    // load vanilla_memes layout
                    //imageview.setImageResource(R.drawable.vanillapreview);
                    break;
            case R.id.demotivational_posters_radBtn:
                if (checked)
                    // load demotivational_posters layout
                    //imageview.setImageResource(R.drawable.demotpreview);
                    break;
        }
    }


 }

//    public void saveMeme (View v) {    //if there is time, fix this so it can be generalized for both types of memes
//        Bitmap meme = drawMeme(v);
//        try {
//            meme.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)));
//
//
//
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//
//        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "memeFile.jpg");
//        imageUri = Uri.fromFile(photo);
//        Toast.makeText(getApplicationContext(), "File saved to :" + imageUri.toString(), Toast.LENGTH_LONG).show();
//
//        File f = new File("memeFile");
//
//
//
//        MediaStore.Images.Media.insertImage(getContentResolver(), meme, "Meme _", "New meme");
//        //return returnedBitmap;
//    }


        public void onRadioButtonClicked (View view){
            // Is the button now checked?
            boolean checked = ((RadioButton) view).isChecked();
            boolean imageSelected = (selectedImagePath != null);

            // Check which radio button was clicked
            switch (view.getId()) {
                case R.id.vanilla_memes_radBtn:
                    if (checked && !imageSelected) {
                        // load vanilla_memes layout
                        //todo: this is where code will go to change sample image to sample vanilla meme image
                        imageview.setImageResource(R.drawable.vanillapreview);
                    } else {
                        // set background behind image to background of vanilla preview
                    }
                        break;
                case R.id.demotivational_posters_radBtn:
                    if (checked && !imageSelected) {
                        // load demotivational_posters layout
                        //todo: this is where code will go to change sample image to sample demotivational poster image
                        imageview.setImageResource(R.drawable.demotpreview);
                    } else {
                        //set background image to demotivational looking

                    }
                        break;
            }
        }





        //todo future work
        public void exportMeme (View v){

        }




 }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK) {
//
//            try {
//
//                Uri selectedImage = imageUri;
//
//
//                getContentResolver().notifyChange(selectedImage, null);
//
//                ImageView imageview = (ImageView) findViewById(R.id.image);
//                ContentResolver cr = getContentResolver();
//                Bitmap bitmap;
//
//
//                bitmap = MediaStore.Images.Media.getBitmap(cr, selectedImage); //don't store in memor card by default
//                imageview.setImageBitmap(bitmap);
//                Toast.makeText(MainActivity.this, selectedImage.toString(), Toast.LENGTH_LONG).show();
//            }
