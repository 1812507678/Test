package com.amsu.test.wifiTramit.uilt;

import android.os.Environment;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 2017/4/27.
 * 将数据写道文本文件中，以空格分隔
 */

public class WriteReadDataToTextFile implements WriteReadDataToFileStrategy {

    private static final String TAG = "TestActivity";

    @Override
    public boolean writeDataToFile(List<Integer> integerList, String fileName) {
        FileWriter fileWriter = null;
        boolean isWriteSuccess = false;
        try {
            fileWriter = new FileWriter(fileName);
            for(int i:integerList){
                fileWriter.write(i+" ");
            }
            fileWriter.flush();
            isWriteSuccess =  true;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IOUtil.closeIOStream(fileWriter);
        }
        return isWriteSuccess;
    }

    @Override
    public List<Integer> readDataFromFile(String fileName) {
        FileReader fileReader = null;
        List<Integer> integerList = new ArrayList<>();
        try {
            fileReader = new FileReader(fileName);
            char[] cbuf = new char[1024*1024];
            int length;
            String allTextString ="";
            while ((length = fileReader.read(cbuf))>0) {
                allTextString += String.valueOf(cbuf,0,length);
            }
            parStringDataToList(allTextString,integerList);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IOUtil.closeIOStream(fileReader);
        }
        return integerList;
    }

    @Override
    public boolean writeByteDataToFile(byte[] bytes, String fileName) {

        return false;
    }

    private void parStringDataToList(String fileStringData,List<Integer> integerList) {
        String[] split = fileStringData.split(" ");
        for (String s:split){
            integerList.add(Integer.parseInt(s));
        }
    }

}
