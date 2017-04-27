package com.amsu.test.wifiTramit.uilt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by HP on 2017/4/27.
 */

public class WriteReadDataToBinaryFile implements WriteReadDataToFileStrategy {
    @Override
    public boolean writeDataToFile(List<Integer> integerList, String fileName) {
        DataOutputStream dataOutputStream = null;
        boolean isWriteSuccess = false;
        try {
            dataOutputStream = new DataOutputStream(new FileOutputStream(fileName));
            ByteBuffer byteBuffer = ByteBuffer.allocate(2);
            for (int anInt : integerList) {
                byteBuffer.clear();
                byteBuffer.putShort((short) anInt);
                dataOutputStream.writeByte(byteBuffer.get(1));
                dataOutputStream.writeByte(byteBuffer.get(0));
            }
            dataOutputStream.flush();
            isWriteSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeIOStream(dataOutputStream);
        }
        return isWriteSuccess;
    }

    @Override
    public List<Integer> readDataFromFile(String fileName) {
        List<Integer> integerList = new ArrayList<>();
        DataInputStream dataInputStream = null;
        try {
            dataInputStream= new DataInputStream(new FileInputStream(fileName));
            byte[] bytes = new byte[2];
            ByteBuffer byteBuffer=  ByteBuffer.wrap(bytes);
            while(dataInputStream.available() >0){
                bytes[1] = dataInputStream.readByte();
                bytes[0] = dataInputStream.readByte();
                short readCsharpInt = byteBuffer.getShort();
                byteBuffer.clear();
                integerList.add((int) readCsharpInt);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IOUtil.closeIOStream(dataInputStream);
        }
        return integerList;
    }
}
