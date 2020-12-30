package com.luban.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Dome8 {
    public static void main(String[] args) throws Exception {
        FileOutputStream fileOutputStream=new FileOutputStream("dome8write.txt");
        FileInputStream fileInputStream=new FileInputStream("dome8read.txt");

        FileChannel channelRead = fileInputStream.getChannel();
        FileChannel channelWrite = fileOutputStream.getChannel();
        //allocateDirect 创建堆外内存
        //HeapByteBuffer 创建堆内内存
        /*
        * 这两个内存相比较：
        *   堆内内存  是直接在堆内 创建baty[] 如果想将数据进行操作，需要进行一下操作：
        *       先将堆内内存中数据  复制到堆外内存中，在进行IO操作
        *   堆外内存  是在  allocateDirect 中存在一个地址变量，这个变量  指向堆外内存
        *   中的数据
        *
        *   这两个内存相比较：堆外内存比堆内内存 更快  更好，，毕竟堆内内存  有复制操作
        *   netty  都是用的堆外内存
        * */
        ByteBuffer byteBuffer=ByteBuffer.allocateDirect(100);
       while (true){
           byteBuffer.clear();
           int readNumber = channelRead.read(byteBuffer);
           System.out.println(readNumber);
           if(readNumber==-1){
               break;
           }
           byteBuffer.flip();
           channelWrite.write(byteBuffer);
       }
       fileOutputStream.close();
       fileInputStream.close();
    }
}
