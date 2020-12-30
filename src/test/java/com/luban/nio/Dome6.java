package com.luban.nio;

import java.nio.ByteBuffer;

public class Dome6 {
    public static void main(String[] args) {
        ByteBuffer byteBuffer=ByteBuffer.allocate(10);
        for(int i=0;i<byteBuffer.capacity();++i){
            byteBuffer.put((byte)i);
        }
        byteBuffer.position(2);
        byteBuffer.limit(8);
        //将byteBuffer 中 2-8的数据  复制到了resetBuffer 同时这两个buffer之间的数据共享

        ByteBuffer resetBuffer = byteBuffer.slice();
        /*   保存标记的作用
        resetBuffer.mark();
        resetBuffer.reset();
           使position回到mark 刚才标记的位置
        */
        for(int i=0;i<resetBuffer.capacity();i++){
            byte anInt = resetBuffer.get();
            resetBuffer.put(i, (byte) (anInt*2));
        }

        byteBuffer.position(0);
        byteBuffer.limit(byteBuffer.capacity());
        while (byteBuffer.hasRemaining()){
            System.out.println(byteBuffer.get());
        }

    }
}
