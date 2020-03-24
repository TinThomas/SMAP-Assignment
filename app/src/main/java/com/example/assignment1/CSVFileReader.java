package com.example.assignment1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/*
The following class, and information on how to use it, is taken from
https://javapapers.com/android/android-read-csv-file/ on 24/02-20
 */

public class CSVFileReader {
    InputStream inputStream;

    public CSVFileReader(InputStream inputStream){
        this.inputStream = inputStream;
    }

    public ArrayList read(){
        ArrayList resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try{
            String CSVLine;
            while((CSVLine = reader.readLine()) != null){
                String[] row = CSVLine.split(";");
                resultList.add(row);
            }
        }
        catch (IOException ex){
            throw new RuntimeException("Error in reading csv file: "+ex);
        }
        finally {
            try{
                inputStream.close();
            }
            catch (IOException ex){
                throw new RuntimeException("Error while closing input stream: "+ex);
            }
        }
        return resultList;
    }
}
