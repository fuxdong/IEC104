package com.visenergy.iec104.socket;

import com.visenergy.iec104.util.ChangeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Fuxudong on 2018/5/9.
 */
public class Socketer {
    public Socket socket = null;
    private boolean connSuccess = false;
    private static Socketer socketer = null;
    private String host = null;
    private int port = 0;
    private final Log log = LogFactory.getLog(Socketer.class);

    private Socketer() {
        socket = null;
        connSuccess = false;
    }

    public static Socketer init(String host, int port) {
        if (socketer == null) {
            socketer = new Socketer();
            socketer.host = host;
            socketer.port = port;
            socketer.socketConnection();
        }

        return socketer;
    }

    /**
     * 发送总召命令
     */
    public void sendZzCommand() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //判断连接状态和socket是否为空，为空则将connSuccess置为false，然后定时重连
                    if (socketer.connSuccess && socketer.socket != null) {
                        //捕获到异常os置为null，并进行socket重连
                        OutputStream os = null;
                        try {
                            os = socketer.socket.getOutputStream();
                            log.info("发送总召命令");
                            os.write(ChangeUtils.hexStringToBytes("680E0000000064010600010000000014"));
                            os.flush();
                            try {
                                Thread.sleep(1000 * 180l);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            log.error("往服务器端发送数据出错：", e);
                            cleanCurrentSocket();
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e1) {
                                    log.error("关闭os输出流出错", e1);
                                }
                            }
                        }
                    } else {
                        cleanCurrentSocket();
                    }
                }
            }
        }).start();
    }

    /**
     * 发送确认命令
     */
    public void sendConfirmCommand(String command) {
        //判断连接状态和socket是否为空，为空则将connSuccess置为false，然后定时重连
        if (socketer.connSuccess && socketer.socket != null) {
            //捕获到异常os置为null，并进行socket重连
            OutputStream os = null;
            try {
                os = socketer.socket.getOutputStream();
                os.write(ChangeUtils.hexStringToBytes(command));
                os.flush();
                if ("68040B000000".equals(command)) {
                    log.debug("发送确认启动消息，U类型");
                } else if ("680423000000".equals(command)) {
                    log.debug("发送确认停止消息，U类型");
                } else if ("680483000000".equals(command)) {
                    log.debug("发送确认测试消息，U类型");
                } else if ("680E0000000064010600010000000014".equals(command)) {
                    log.debug("消费到总召请求时，发送总召命令");
                } else {
                    log.debug("确认I帧消息，S类型");
                }
            } catch (Exception e) {
                log.error("往服务器端发送确认出错：", e);
                cleanCurrentSocket();
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e1) {
                        log.error("关闭os输出流出错", e1);
                    }
                }
            }
        } else {
            cleanCurrentSocket();
        }
    }

    public InputStream getSocketInputStream() {
        if (socketer.connSuccess) {
            InputStream is = null;
            try {
                if (socketer.socket != null) {
                    is = socketer.socket.getInputStream();
                    return is;
                } else {
                    log.info("获取输出流出错...");
                    cleanCurrentSocket();
                    return null;
                }
            } catch (IOException e) {
                log.error("读输入流数据出错", e);
                cleanCurrentSocket();
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                return null;
            }
        } else {
            log.info("socket未连接，不能开始接收数据，稍后重试...");
            return null;
        }
    }

    /**
     * 异常时，关闭socket
     */
    public void cleanCurrentSocket() {
        socketer.connSuccess = false;
        if (socketer.socket != null) {
            try {
                socketer.socket.close();
            } catch (IOException e1) {
                log.error("关闭socket出错", e1);
            }
            socketer.socket = null;
        }
    }

    /**
     * 定时检查socket连接状态
     */
    private void socketConnection() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if ((!socketer.connSuccess)||(socketer.socket==null)) {
                        try {
                            socketer.socket = new Socket(socketer.host, socketer.port);
                            log.info("已经连接到服务器，ip：" + socketer.host);
                            socketer.connSuccess = true;
                        } catch (IOException e) {
                            log.error("重新连接服务器出错，稍后重试...");
                            cleanCurrentSocket();
                        }
                    } else {
                        try {
                            log.info("socket已连接，ip：" + socketer.host);
                            Thread.sleep(60l * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        Thread t = new Thread(runnable);
        t.start();
    }
}
