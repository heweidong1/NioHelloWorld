package com.luban.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Dome4 {
    public static void main(String[] args) throws Exception {
        FileOutputStream fileOutputStream=new FileOutputStream("dome4write.txt");
        FileInputStream fileInputStream=new FileInputStream("dome4read.txt");

        FileChannel channelRead = fileInputStream.getChannel();
        FileChannel channelWrite = fileOutputStream.getChannel();
        ByteBuffer byteBuffer=ByteBuffer.allocate(100);
       while (true){
           //相当于 将byteBuffer 中的数据清空
           //当limit = position  是写不就数据的  会返回0
           byteBuffer.clear();
           System.out.println(byteBuffer.position());
           int readNumber = channelRead.read(byteBuffer);
           System.out.println(readNumber);
           if(-1==readNumber){
               break;
           }
           byteBuffer.flip();
           channelWrite.write(byteBuffer);
       }
       fileOutputStream.close();
       fileInputStream.close();
    }
}
