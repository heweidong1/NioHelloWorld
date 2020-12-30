package com.luban.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//聊天室服务端
public class ChatService {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    private long timeout=2000;


    public ChatService(){
        try {
            //服务端channel
            //开启服务端  相当于BIO中的服务器
            serverSocketChannel=ServerSocketChannel.open();

            //开启选择器对象
            selector=Selector.open();

            //绑定端口
            serverSocketChannel.bind(new InetSocketAddress(9090));

            //设置非阻塞式  必须设置
            serverSocketChannel.configureBlocking(false);

            //把ServerSocketChannel注册给Selector  第二个参数  对什么事件感兴趣【现在是对于 客户端连接 事件】
            SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);//监听连接

            System.out.println("服务端准备就绪");

            start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void start() throws Exception{
        int count=0;
        long start=System.nanoTime();
        //干活
        while (true){
            //监控客户端
//            if(selector.select(timeout)==0){
//                System.out.println("2秒内没有客户端来连接我");
//                continue;
//            }
            //2秒 【时间设置】  判断是否有自己感兴趣的事件发生
            //在selector 中只注册了一个serverSocketChannel 而且只对连接事件感兴趣
            selector.select(timeout);
//            System.out.println("2秒了");
//            long end=System.nanoTime();
//            if(end-start>= TimeUnit.MILLISECONDS.toNanos(timeout)){
//                count=1;
//            }else{
//                count++;
//            }
//
//            if(count>=10){
//                System.out.println("有可能发生空轮询"+count+"次");
//                rebuildSelector();
//                count=0;
//                selector.selectNow();
//                continue;
//            }
            //得到SelectionKey对象，判断事件
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey=iterator.next();
                if(selectionKey.isAcceptable()){     //连接事件
                    //获取网络通道
                    SocketChannel accept = serverSocketChannel.accept();
                    //设置非阻塞式
                    accept.configureBlocking(false);
                    //连接上了  注册读取事件
                    accept.register(selector,SelectionKey.OP_READ);
                    System.out.println(accept.getRemoteAddress().toString()+"上线了");
                }
                if(selectionKey.isReadable()){     //读取客户端数据事件
                    //读取客户端发来的数据
                    readClientData(selectionKey);
                }
                //手动从当前集合将本次运行完的对象删除
                //将处理完的连接事件删除   【已经处理完了】
                iterator.remove();
            }
        }
    }

    private void rebuildSelector() throws IOException {
        Selector newSelector=Selector.open();
        Selector oldSelect=selector;
        for (SelectionKey selectionKey : oldSelect.keys()) {
            //拿到当前这个key  感兴趣的事件  【int】
            int i = selectionKey.interestOps();
            selectionKey.cancel();
            //将这个channel 和 感兴趣的事件  注册到新的Selector上
            selectionKey.channel().register(newSelector,i);
        }
        selector=newSelector;
        oldSelect.close();
    }

    //读取客户端发来的数据
    private void readClientData(SelectionKey selectionKey) throws IOException {
        System.out.println("aaaaaaaaaaaa");
        //通过key  拿到socketChannel
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int read = socketChannel.read(byteBuffer);
        byteBuffer.flip();
        if(read>0){
            byte[] bytes=new byte[read];
            byteBuffer.get(bytes,0,read);
            //读取了数据  广播
            String s = new String(bytes,"utf-8");
            writeClientData(socketChannel,s);
        }
    }

    //广播  将读取的数据群发
    private void writeClientData(SocketChannel socketChannel,String s) throws IOException {
        //拿到所有的channel
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey key : keys) {
            //是否有效
            if(key.isValid()){
                //SelectableChannel 是是ServerSocketChannel和是SocketChannel的基类
                SelectableChannel channel = key.channel();
                //除去服务器的channel   服务器的channel 是ServerSocketChannel
                //客户端的channel 是SocketChannel
                if(channel instanceof  SocketChannel){
                    SocketChannel socketChannel1= (SocketChannel) channel;
                    //不发送给自己
                    if(channel!=socketChannel){
                        ByteBuffer wrap = ByteBuffer.wrap(s.getBytes());
                        socketChannel1.write(wrap);
                    }
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
        new ChatService().start();
    }


}
