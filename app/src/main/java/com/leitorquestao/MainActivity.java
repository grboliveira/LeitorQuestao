package com.leitorquestao;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity  extends AppCompatActivity {

    Button btnTakePic;
    Button btnCinza;
    Button btnLim;
    Button btnGallery;
    ImageView imageView;
    String pathToFile;
    public static final int PICK_IMAGE = 1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.main_activity);
        btnTakePic = findViewById(R.id.btnTakePic);
        btnCinza = findViewById(R.id.btnCinza);
        btnLim = findViewById(R.id.btnLim);
        if(Build.VERSION.SDK_INT >=24){
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }
        btnTakePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dispatchPictureTakerAction();
            }
        });
        /*btnGallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getImage();
            }
        });
        */
         
        imageView = findViewById(R.id.image);
        btnCinza.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getGreyScale();
            }
        });
        btnLim.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getLim();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == 1){
                Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);
                imageView.setImageBitmap(bitmap);
            }
        }
    }



    private void dispatchPictureTakerAction(){
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getPackageManager())!=null){
            File photoFile = null;
            photoFile = createPhotoFile();
            pathToFile = photoFile.getAbsolutePath();
            System.out.println("\nPath To File:"+pathToFile);
            Uri photoURI = FileProvider.getUriForFile(MainActivity.this,"com.leitorquestao.MainActivity", photoFile);
            takePic.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
            startActivityForResult(takePic,1);
        }
    }

    private File createPhotoFile() {
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM.concat("/Pictures/"));
        File image = null;
        try {
            image = File.createTempFile(name, ".jpg", storageDir);
        } catch (IOException e) {
            Log.d("myLog", "Excep : " + e.toString());
        }
        return image;
    }

    private void getGreyScale(){
        if(pathToFile != null){
            Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);
            Mat imgToProcess = new Mat();
            Utils.bitmapToMat(bitmap,imgToProcess);

            for(int i=0;i<imgToProcess.height();i++){
                for(int j=0;j<imgToProcess.width();j++){
                    double y = 0.3 * imgToProcess.get(i, j)[0] + 0.59 * imgToProcess.get(i, j)[1] + 0.11 * imgToProcess.get(i, j)[2];
                    imgToProcess.put(i, j, new double[]{y, y, y, 255});
                }
            }


            //Imgproc.cvtColor(imgToProcess, imgToProcess, Imgproc.COLOR_BGR2GRAY);
            //Imgproc.cvtColor(imgToProcess, imgToProcess, Imgproc.COLOR_GRAY2RGBA, 4);
            Bitmap bmpOut = Bitmap.createBitmap(imgToProcess.cols(), imgToProcess.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(imgToProcess, bmpOut);
            imageView.setImageBitmap(bmpOut);
        }

    }

    private void getLim() {
        if (pathToFile != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);
            Mat src = new Mat();
            Utils.bitmapToMat(bitmap,src);
            // Creating an empty matrix to store the result
            Mat dst = new Mat();

            Imgproc.threshold(src,dst,127,255,Imgproc.THRESH_BINARY);

            Bitmap bmpOut = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(dst, bmpOut);
            imageView.setImageBitmap(bmpOut);

        }
    }
    /*
    private void getImage(){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }
    */

}
