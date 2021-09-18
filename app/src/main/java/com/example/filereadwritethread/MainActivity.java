package com.example.filereadwritethread;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    Button btnSave, btnLoad;
    EditText etInput;
    TextView tvLoad;
    String filename = "";
    String filepath = "";
    String fileContent = "";

    Thread t, t2= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSave = findViewById(R.id.btnSave);
        btnLoad = findViewById(R.id.btnLoad);
        etInput = findViewById(R.id.etInput);
        tvLoad = findViewById(R.id.tvLoad);
        filename = "myFile.txt";
        filepath = "myFileDir";

        if(!isExternalStorageAvailableForRE()){
            btnSave.setEnabled(false);
        }
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readThread();
            }
        });

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeThread();
            }
        });
    }

    public void readThread(){
        tvLoad.setText("");
        fileContent = etInput.getText().toString().trim();
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(isStoragePermissionGranted()){
                            if(!fileContent.equals("")){
                                File myExternalFile = new File(getExternalFilesDir(filepath),filename);
                                FileOutputStream fos = null;
                                try{
                                    fos = new FileOutputStream(myExternalFile);
                                    fos.write(fileContent.getBytes());
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                etInput.setText("");
                                Toast.makeText(MainActivity.this, "Information saved to SD Card", Toast.LENGTH_SHORT);
                            }else{
                                Toast.makeText(MainActivity.this, "Text field can not be empty", Toast.LENGTH_SHORT);
                            }
                        }
                    }
                });
            }
        });
    }

    public void writeThread(){
        final FileReader[] fr = {null};
        File myExternalFile = new File(getExternalFilesDir(filepath), filename);
        StringBuilder stringBuilder = new StringBuilder();
        t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            fr[0] = new FileReader(myExternalFile);
                            BufferedReader br = new BufferedReader(fr[0]);
                            String line = br.readLine();
                            while (line != null){
                                stringBuilder.append(line).append('\n');
                                line = br.readLine();
                            }
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            String fileContents = "File contents\n" + stringBuilder.toString();
                            tvLoad.setText(fileContents);
                        }
                    }
                });
            }
        });
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //Permission is granted
                return true;
            } else {
                //Permission is revoked
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            //permission is automatically granted on sdk<23 upon installation
            //Permission is granted
            return true;
        }
    }

    private boolean isExternalStorageAvailableForRE() {
        String extStorageState = Environment.getExternalStorageState();
        if(extStorageState.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }
        return false;
    }
}