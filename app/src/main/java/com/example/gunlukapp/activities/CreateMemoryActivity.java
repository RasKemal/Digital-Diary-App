package com.example.gunlukapp.activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gunlukapp.R;
import com.example.gunlukapp.database.AppDatabase;
import com.example.gunlukapp.entities.Memory;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateMemoryActivity extends AppCompatActivity {

    ImageView backBtn;
    EditText inputTitle, inputMemory, inputPassword;
    TextView textDate, locationText;
    Button saveBtn,deleteBtn;
    LinearLayout addImg, pickLocationButton, btnPdf, shareContent;
    ImageView resultImg;
    String imgPath;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private static final int REQUEST_CODE = 1000;
    public static final double INVALID_COORDINATE = 91;

    private Memory alreadyAvailableMem;
    private AlertDialog deleteDialog,passwordDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_memory);
        backBtn = findViewById(R.id.back_btn);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });



        inputTitle = findViewById(R.id.inputTitle);
        inputMemory = findViewById(R.id.inputMemory);
        inputPassword = findViewById(R.id.inputPassword);
        textDate = findViewById(R.id.dateTime);
        addImg = findViewById(R.id.addImage);
        resultImg= findViewById(R.id.resultImg);
        pickLocationButton = findViewById(R.id.addLocation);
        locationText = findViewById(R.id.location_text);
        deleteBtn = findViewById(R.id.deletebtn);
        btnPdf = findViewById(R.id.createPdf);
        shareContent = findViewById(R.id.shareContent);

        textDate.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(new Date())
        );

        pickLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMap();
            }
        });

        saveBtn = findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadMemory();
            }
        });

        imgPath = "";

        if(getIntent().getBooleanExtra("isViewOrUpdate",false)){
            alreadyAvailableMem = (Memory) getIntent().getSerializableExtra("memory");
            setViewOrUpdateMemory();
        }




        addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateMemoryActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else{
                    selectImg();
                }
            }
        });

        shareContent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String content = alreadyAvailableMem.getTitle() + "\n\n" + alreadyAvailableMem.getDate()
                        + "\n\n" + alreadyAvailableMem.getMemory() + "\n\n" + alreadyAvailableMem.getLocation();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,content);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, null));
            }
        });



        if(alreadyAvailableMem != null){
            deleteBtn.setVisibility(View.VISIBLE);
            btnPdf.setVisibility(View.VISIBLE);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popUpDelete();
                }
            });

            btnPdf.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String pdfTitle = inputTitle.getText().toString();
                    String pdfDate = textDate.getText().toString();
                    String pdfMemory = inputMemory.getText().toString();
                    String pdfLocation = locationText.getText().toString();

                    if(ContextCompat.checkSelfPermission(
                            getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(
                                CreateMemoryActivity.this,
                                new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_STORAGE_PERMISSION
                        );
                    }else{
                        try{
                            createPdf(pdfTitle,pdfDate,pdfMemory,pdfLocation,imgPath);
                        } catch(FileNotFoundException | MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        }

    }


    private void popUpPassword(){
        if(passwordDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateMemoryActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.password_confirmation,
                    (ViewGroup) findViewById(R.id.passwordConf)
            );
            builder.setView(view);
            builder.setCancelable(false);
            passwordDialog = builder.create();
            if(passwordDialog.getWindow() != null){
                passwordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            Intent intent = new Intent(this, MainActivity.class);
            EditText inputPass = (EditText) view.findViewById(R.id.passInput);
            view.findViewById(R.id.passOk).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!inputPass.getText().toString().equals(alreadyAvailableMem.getPassword())){
                        startActivity(intent);
                        Toast.makeText(CreateMemoryActivity.this, "Password is wrong!!", Toast.LENGTH_LONG).show();
                    }else{
                        passwordDialog.dismiss();
                    }
                }
            });
            view.findViewById(R.id.passCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    passwordDialog.dismiss();
                    startActivity(intent);
                }
            });

        }

        passwordDialog.show();
    }

    private void popUpDelete(){
        if(deleteDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateMemoryActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.delete_confirmation,
                    (ViewGroup) findViewById(R.id.deleteConf)
            );
            builder.setView(view);
            deleteDialog = builder.create();
            if(deleteDialog.getWindow() != null){
                deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.deleteMemory).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    @SuppressLint("StaticFieldLeak")
                    class DeleteMemoryTask extends AsyncTask<Void, Void, Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            AppDatabase.getDatabase(getApplicationContext()).memDao().deleteMemory(alreadyAvailableMem);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent = new Intent();
                            intent.putExtra("isMemoryDeleted",true);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }

                    new DeleteMemoryTask().execute();
                }
            });
            view.findViewById(R.id.cancelDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteDialog.dismiss();
                }
            });

        }

        deleteDialog.show();
    }

    @SuppressWarnings("deprecation")
    private void goToMap() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void createPdf(String title, String date, String memory, String location, String imagePath) throws FileNotFoundException, MalformedURLException {
        String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(pdfPath, "MEMORY_PDF_"+ getCurrentTime() + "_" + getTodaysDate() + ".pdf");
        OutputStream outputStream = new FileOutputStream(file);

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        Paragraph paraTitle = new Paragraph(title).setBold().setFontSize(35).setTextAlignment(TextAlignment.CENTER);
        Paragraph paraDate = new Paragraph(date).setFontSize(19).setTextAlignment(TextAlignment.CENTER);
        Paragraph paraLocation = new Paragraph(location).setItalic().setFontSize(16).setTextAlignment(TextAlignment.CENTER);

        Paragraph paraMemory = new Paragraph(memory).setFontSize(17).setTextAlignment(TextAlignment.CENTER);

        ImageData data = ImageDataFactory.create(imagePath);

        Image img = new Image(data);




        document.add(paraTitle);
        document.add(paraDate);
        document.add(paraLocation);
        document.add(paraMemory);
        document.add(img);

        document.close();
        Toast.makeText(this, "PDF document has been saved to your phone.", Toast.LENGTH_LONG).show();

    }

    private String getCurrentTime(){
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    private String getTodaysDate(){
        return new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());
    }


    @SuppressLint("SetTextI18n")
    private void setViewOrUpdateMemory(){
        inputTitle.setText(alreadyAvailableMem.getTitle());
        inputMemory.setText(alreadyAvailableMem.getMemory());
        textDate.setText(alreadyAvailableMem.getDate());
        if(alreadyAvailableMem.getImgPath() != null){
            resultImg.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableMem.getImgPath()));
            resultImg.setVisibility(View.VISIBLE);
            imgPath = alreadyAvailableMem.getImgPath();
        }else{
            resultImg.setVisibility(View.GONE);
        }

        if(!alreadyAvailableMem.getPassword().equals("")){
            popUpPassword();
        }
        locationText.setText(alreadyAvailableMem.getLocation());
        saveBtn.setText("Update Memory");
        inputPassword.setText(alreadyAvailableMem.getPassword());
        shareContent.setVisibility(View.VISIBLE);
    }


    private void uploadMemory(){
        if(inputTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Title field is empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(inputMemory.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Memory field is empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        final Memory mem = new Memory();
        mem.setTitle(inputTitle.getText().toString());
        mem.setMemory(inputMemory.getText().toString());
        mem.setDate(textDate.getText().toString());
        mem.setImgPath(imgPath);
        mem.setLocation(locationText.getText().toString());
        mem.setPassword(inputPassword.getText().toString());

        if(alreadyAvailableMem != null){
            mem.setId(alreadyAvailableMem.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class uploadTask extends AsyncTask<Void, Void, Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                AppDatabase.getDatabase(getApplicationContext()).memDao().insertMemory(mem);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                if(alreadyAvailableMem != null){
                    Toast.makeText(CreateMemoryActivity.this, "Your memory has been UPDATED.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(CreateMemoryActivity.this, "Your memory has been saved to the diary.", Toast.LENGTH_SHORT).show();
                }

                finish();
            }
        }

        new uploadTask().execute();

        Intent inte = new Intent(this, MainActivity.class);
        startActivity(inte);

    }

    private void selectImg(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }


    public static String getAddressFromCoordinates(double lat, double lng, Context context) {
        String addressStr = null;
        try {
            Geocoder myLocation = new Geocoder(context, Locale.getDefault());
            List<Address> myList = myLocation.getFromLocation(lat, lng, 1);
            Address address = (Address) myList.get(0);
            addressStr = address.getAddressLine(0);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Bir hata oluştu!", Toast.LENGTH_SHORT).show();
        } finally { return addressStr; }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImg();
            }else{
                Toast.makeText(this, "You Don't Have Permission.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri resultImgUri = data.getData();
                if(resultImgUri != null){
                    try{
                        InputStream inputStream = getContentResolver().openInputStream(resultImgUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        resultImg.setImageBitmap(bitmap);
                        resultImg.setVisibility(View.VISIBLE);

                        imgPath = getPathFromUri(resultImgUri);
                    }catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if(resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
                double lat = data.getDoubleExtra("latitude", INVALID_COORDINATE);
                double lng = data.getDoubleExtra("longitude", INVALID_COORDINATE);
                String address = getAddressFromCoordinates(lat, lng, getApplicationContext());
                locationText.setText(address);
            }
        }
    }

    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null,null,null);

        if(cursor ==null){
            filePath = contentUri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
}

