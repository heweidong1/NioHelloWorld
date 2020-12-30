主从 模式 相比较 多线程模式【重点看Acceptor和TCPSubReactor两个类】
又增加了多个select 服务器一个   根据cpu核心数  创建多个select  用来存储客户端

当程序启动  服务器段初始化  并且会根据 核心数初始化 用来存储客户端得select
当客户端连接  会轮询客户端select  从而将 事件注册进去

服务器段逻辑  Acceptor中：
public Acceptor(ServerSocketChannel ssc) throws IOException {
            this.ssc = ssc;
            // 創建多個selector以及多個subReactor線程
            for (int i = 0; i < cores; i++) {
                selectors[i] = Selector.open();
                r[i] = new TCPSubReactor(selectors[i], ssc, i);
                t[i] = new Thread(r[i]);
                t[i].start();
            }

        }


TCPSubReactor线程类  ，让多个select 跑起来
在初始化服务器得时候  也初始化了 存储 客户端得select  selectors[i]
TCPSubReactor 类是用来处理 客户端select 逻辑的，要与服务端分开
如果有8个存储客户端得select  那么这个类就有8个，一个类处理一个select
public TCPSubReactor(Selector selector, ServerSocketChannel ssc, int num) {
            this.selector = selector;
            this.num = num;
        }


服务端  Acceptor附加对象run方法 基本逻辑[注册客户端]
因为项目启动   都已经初始化完毕【假设有8个存储客户端得select在等待事件触发】，基本逻辑为：
当有客户端进来的时候 会轮询【随机】其中一个TCPSubReactor对象，将这个客户端注册到该select中
该方法也设计了开关得 逻辑【可要可不要都行】 如果不要 直接注册到select中 就行
 @Override
        public synchronized void run() {
            try {
                SocketChannel sc = ssc.accept(); // 接受client連線請求
                System.out.println(sc.socket().getRemoteSocketAddress().toString()
                        + " is connected.");

                if (sc != null) {
                    sc.configureBlocking(false); // 設置為非阻塞
                    r[selIdx].setRestart(true); // 暫停線程
                    selectors[selIdx].wakeup(); // 使一個阻塞住的selector操作立即返回
                    SelectionKey sk = sc.register(selectors[selIdx],
                            SelectionKey.OP_READ); // SocketChannel向selector[selIdx]註冊一個OP_READ事件，然後返回該通道的key
                    selectors[selIdx].wakeup(); // 使一個阻塞住的selector操作立即返回
                    r[selIdx].setRestart(false); // 重啟線程
                    sk.attach(new TCPHandler(sk, sc)); // 給定key一個附加的TCPHandler對象
                    if (++selIdx == selectors.length)
                        selIdx = 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }