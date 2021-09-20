package com.gknsvs.artbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.gknsvs.artbook.databinding.ActivityArtBinding;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLData;

public class ArtActivity extends AppCompatActivity {
    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase sqlData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityArtBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);
        registerLauncher();
        sqlData=openOrCreateDatabase("Arts",MODE_PRIVATE,null);
        Intent intent=getIntent();
        String intentInfo = intent.getStringExtra("intentInfo");
        if (intentInfo.matches("addList")){
            newArtActivity();
        }else {
            chosenArtActivity(intent.getIntExtra("id",-1));
        }
    }

    private void newArtActivity() {
        binding.imgArt.setImageResource(R.drawable.select);
        binding.txtName.setText("");
        binding.txtInfo.setText("");
        binding.txtYear.setText("");
        binding.btnSave.setVisibility(View.VISIBLE);

    }

    private void chosenArtActivity(int id) {
        if(id<0)
            newArtActivity();
        else{
            binding.btnSave.setVisibility(View.INVISIBLE);
            getDB(id);
        }

    }

    private void getDB(int id) {

        try {
            Cursor cursor=sqlData.rawQuery("SELECT * FROM arts WHERE id = ?",new String[] {String.valueOf(id)});
            int nameIx=cursor.getColumnIndex("name");
            int infoIx=cursor.getColumnIndex("info");
            int yearIx=cursor.getColumnIndex("year");
            int imgIx=cursor.getColumnIndex("image");
            while (cursor.moveToNext()){
                binding.txtName.setText(cursor.getString(nameIx));
                binding.txtInfo.setText(cursor.getString(infoIx));
                binding.txtYear.setText(cursor.getString(yearIx));
                byte[] bytes=cursor.getBlob(imgIx);
                binding.imgArt.setImageBitmap(BitmapFactory.decodeByteArray(bytes,0,bytes.length));
            }
            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void save(View view){
        String name=binding.txtName.getText().toString();
        String info=binding.txtInfo.getText().toString();
        int year=Integer.parseInt(binding.txtYear.getText().toString());

        Bitmap smallImg=makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        smallImg.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] bytes=outputStream.toByteArray();

        saveToDB(name,info,String.valueOf(year),bytes);

        Intent intent=new Intent(ArtActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void saveToDB(String name, String info, String year, byte[] bytes) {
        try {

                sqlData.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY,name VARCHAR,info VARCHAR,year VARCHAR,image BLOB)");
                String sqlString="INSERT INTO arts(name,info,year,image) VALUES(?,?,?,?)";
            SQLiteStatement sqLiteStatement=sqlData.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,info);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,bytes);
            sqLiteStatement.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Bitmap makeSmallerImage(Bitmap img,int maxSize){
        int witdh,height;
        witdh=img.getWidth();
        height=img.getHeight();
        float bitmapRatio=(float)witdh/(float)height;
        if(bitmapRatio>1)
        {//landscape
            witdh=maxSize;
            height= (int) (witdh/bitmapRatio);
        }
        else{//potrait
            height=maxSize;
            witdh= (int) (height*bitmapRatio);
        }
        return Bitmap.createScaledBitmap(img,witdh,height,true);
    }

    public void imgOpen(View view){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                Snackbar.make(view,"Permission needed for Gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }else{
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        else
        {
            Intent intentToGallery=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }

    }

    private void registerLauncher(){

        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()==RESULT_OK)
                {
                    Intent intentFromResult=result.getData();
                    if(intentFromResult.getData()!=null)
                    {
                        Uri imageData=intentFromResult.getData();
                        //binding.imgArt.setImageURI(imageData);
                        try {//convert to bitmap
                            if(Build.VERSION.SDK_INT>=28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageData);
                                selectedImage=ImageDecoder.decodeBitmap(source);
                            }
                            else
                            {
                                selectedImage=MediaStore.Images.Media.getBitmap(getContentResolver(),imageData);
                            }
                            binding.imgArt.setImageBitmap(selectedImage);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });



        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result)
                {
                    Intent intentToGallery= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                }else
                {
                    Toast.makeText(ArtActivity.this,"Permission needed",Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}