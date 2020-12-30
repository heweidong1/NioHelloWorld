package com.luban.nio;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Dome2 {
    public static void main(String[] args) throws Exception {
        FileInputStream fileInputStream=new FileInputStream("dome2.txt");
        FileChannel channel = fileInputStream.getChannel();
        ByteBuffer byteBuffer=ByteBuffer.allocate(1024);
        channel.read(byteBuffer);
        //byteBuffer.flip(); 翻转，将写 换为读  并且position指向0
        byteBuffer.flip();
        while (byteBuffer.remaining()>0){
            System.out.println((char)byteBuffer.get());
        }
        fileInputStream.close();
    }
}
