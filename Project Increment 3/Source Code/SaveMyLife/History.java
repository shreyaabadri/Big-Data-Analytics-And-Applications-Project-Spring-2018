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

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;


public class History extends AppCompatActivity {
    Button btnAddFood;
    TextView textViewDate;
    TextView textViewFood;
    TextView textViewCalories;

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
        textViewDate = (TextView) findViewById(R.id.showDate);
        textViewFood = (TextView) findViewById(R.id.showFood);
        textViewCalories = (TextView) findViewById(R.id.showCalories);
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
        int count=0;
        try{
            while ((test=br.readLine()) != null){
                count++;
            }
        }
        catch (IOException e) {e.printStackTrace();}

        try{
            fis.getChannel().position(0);
        }
        catch (IOException e) {e.printStackTrace();}

        String[] array = new String[count];

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
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(new Date());
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyHistory";
        String[] loadtext = Load(new File(path + "/history.txt"));

        String finaltext = "";

        for (int i = 0; i < loadtext.length; i++) {
            finaltext += loadtext[i] + System.getProperty("line.separator");
        }
        finaltext = finaltext.substring(1, finaltext.length() - 2);
        String[] records = finaltext.split("\\}\\{");
        String[] record;
        String finaltextD = "";
        String finaltextF = "";
        String finaltextC = "";
        System.out.println();
        for (int x = 0; x < records.length; x++){
            record = records[x].split(",");
            String part1 = record[0].split(":")[1];
            part1 = part1.substring(2, part1.length() - 1);
            String part2 = record[1].split(":")[1];
            finaltextD += date + "\n";
            finaltextF += part1 + "\n";
            finaltextC += part2 + "\n";
        }
        textViewDate.setText(finaltextD);
        textViewFood.setText(finaltextF);
        textViewCalories.setText(finaltextC);
    }
}
