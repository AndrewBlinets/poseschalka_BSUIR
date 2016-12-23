package com.urban.basicsample;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Andrew on 18.12.2016.
 */

public class MyFileClass  {


    private  String FILENAME;

   /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
*/
    public MyFileClass() {
        FILENAME = Environment.getExternalStorageDirectory().toString() + "/Log_file.txt";
    }

   public void writeFile_M(String str) {
       StringBuilder stringBuilder = new StringBuilder();
              try {
            SimpleDateFormat format = new SimpleDateFormat();
            format.applyPattern("dd.MM.yyyy HH:mm");

                  File myFile = new File(FILENAME);
                  try {
                      FileInputStream inputStream = new FileInputStream(myFile);
                      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                      String line;
                      try {
                          while ((line = bufferedReader.readLine()) != null){
                              stringBuilder.append(line);
                              stringBuilder.append("\n");
                          }
                      } catch (IOException e) {
                          e.printStackTrace();
                      }
                  } catch (FileNotFoundException e) {
                      e.printStackTrace();
                  }



            FileOutputStream st = new FileOutputStream(FILENAME);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(st));
                  stringBuilder.append("___________________________________________________");
                  stringBuilder.append("\n");
                  stringBuilder.append("\n");
                  stringBuilder.append("___________________________________________________");
                  stringBuilder.append("\n");
                  stringBuilder.append("\n");
                  stringBuilder.append("___________________________________________________");
                  stringBuilder.append("\n");
                  stringBuilder.append("\n");
                  stringBuilder.append("          " + format.format(new Date()) +" " + str);
                  stringBuilder.append("\n");
                  bw.write(stringBuilder + "");
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFile( String str) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            SimpleDateFormat format = new SimpleDateFormat();
            format.applyPattern("dd.MM.yyyy HH:mm");
            File myFile = new File(FILENAME);
            try {
                FileInputStream inputStream = new FileInputStream(myFile);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null){
                        stringBuilder.append(line);
                        stringBuilder.append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            FileOutputStream st = new FileOutputStream
                    (FILENAME);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter
                            (st));
            stringBuilder.append("          " + format.format(new Date()) +" " + str);
            stringBuilder.append("\n");
            bw.write(stringBuilder + "");
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
