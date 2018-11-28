package com.visenergy.iec104.rabbitTask;

import com.rabbitmq.client.*;
import com.visenergy.iec104.socket.Socketer;
import com.visenergy.iec104.util.ChangeUtils;
import com.visenergy.iec104.util.RabbitMq;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Fuxudong on 2018/5/9.
 * 监听总召命令
 */
public class ConsumerZzTask implements Runnable {
    private Socketer socketer;
    private Log log = LogFactory.getLog(ConsumerZzTask.class);
    private Connection conn = null;
    private Channel channel = null;
    private RabbitMq mq;

    public ConsumerZzTask(Socketer socketer, RabbitMq mq) {
        this.socketer = socketer;
        this.mq = mq;
    }

    /**
     * rabbitmq消费者端的通道断线重连
     */
    @Override
    public void run() {
        //消息服务器断开连接标志，如果断开，每隔5秒重新连接
        boolean success = false;
        while (!success) {
            channel = getChannel();
            if (channel != null) {
                try {
                    Consumer consumer = initConsumer();
                    channel.basicConsume("PV_CALL", true, consumer);
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                    success = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    close();
                    success = false;
                }
            } else {
                success = false;
                try {
                    Thread.sleep(5 * 1000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 消费者，监听页面是否发送总召请求
     *
     * @return
     */
    private Consumer initConsumer() {
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                log.debug("有新客户端连接，发送总召命令");
                try {
                    socketer.sendConfirmCommand("680E0000000064010600010000000014");
                } catch (Exception e) {
                    log.error("消费总召请求时，发送总召命令到数据采集服务器端出错", e);
                    socketer.cleanCurrentSocket();
                }
            }
        };
        return consumer;
    }

    private Channel getChannel() {
        try {
            if (conn == null) {
                conn = mq.newConnection();
            }
            if (conn != null && channel == null) {
                channel = conn.createChannel();
            }
        } catch (IOException e) {
            e.printStackTrace();
            close();
        } catch (TimeoutException e) {
            e.printStackTrace();
            close();
        } catch (Exception e) {
            e.printStackTrace();
            close();
        }

        return channel;
    }

    private void close() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (TimeoutException e1) {
                e1.printStackTrace();
            }
            channel = null;
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            conn = null;
        }
    }
}
