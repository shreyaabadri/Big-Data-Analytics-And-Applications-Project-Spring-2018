package com.example.geovanni.savemylife;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class History extends AppCompatActivity {
    Button btnAddFood;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        btnAddFood = (Button) findViewById(R.id.btnAddFood);
        btnAddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
        textView = (TextView) findViewById(R.id.showContents);
        load();
    }

    public static String[] Load(File file){
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String test;
        int anzahl=0;
        try{
            while ((test=br.readLine()) != null){
                anzahl++;
            }
        }
        catch (IOException e) {e.printStackTrace();}

        try{
            fis.getChannel().position(0);
        }
        catch (IOException e) {e.printStackTrace();}

        String[] array = new String[anzahl];

        String line;
        int i = 0;
        try{
            while((line=br.readLine())!=null){
                array[i] = line;
                i++;
            }
        }
        catch (IOException e) {e.printStackTrace();}
        return array;
    }

    public void load() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyHistory";
        String[] loadtext = Load(new File(path + "/history.txt"));

        String finaltext = "";

        for (int i = 0; i < loadtext.length; i++) {
            finaltext += loadtext[i] + System.getProperty("line.separator");
        }

        textView.setText(finaltext);
    }
}
