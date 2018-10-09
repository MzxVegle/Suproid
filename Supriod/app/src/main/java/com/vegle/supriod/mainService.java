package com.vegle.supriod;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class mainService extends Service {
    Info info;
   Thread sendthread;
    Thread recvthread;
    DatagramSocket breathSocket;
    Notification.Builder notification;
    NotificationManager notificationManager;
    BroadcastReceiver broadcastReceiver;
    DatagramSocket recvsocket = null;
    public int breathcount=0;
    static boolean alive = false;
    Intent intentmsg ;

     boolean threadlife = true;
    static int index = 0x01000000;
    PowerManager.WakeLock wakeLock = null;
    private void acquireWakeLock(){
        if (wakeLock == null){
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"wakelock");
        }
        if(null != wakeLock){
            wakeLock.acquire();
        }
    }
    private void releaseWakeLock(){
        if(null != wakeLock){
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //load_udpSocket();

        acquireWakeLock();
    }

    public int onStartCommand(final Intent intent, int flags, int startId) {

        serviceBrocast();
        alive=true;

        intentmsg = new Intent();
        intentmsg.setAction("action");
        info = new Info();
        intentmsg.putExtra("alive",alive);
        sendBroadcast(intentmsg);
        //创建一个Notification
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE) ;
        notification  = new Notification.Builder(getApplicationContext());
        //Intent intent1 = new Intent();
        //PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent1,PendingIntent.FLAG_CANCEL_CURRENT);
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setContentTitle("正在执行呼吸");
        notification.setOngoing(true);
       // notification.setContentIntent(contentIntent);
        startForeground(100,notification.build());
    new Thread(new Runnable() {
        @Override
        public void run() {
            disconnected();
        }
    }).start();
        //呼吸发送线程
    sendthread = new Thread(new Runnable() {
            @Override
            public void run() {
                intentmsg.putExtra("btnenflag",1);
                sendBroadcast(intentmsg);
                try {

                    breath_send(info.session);
                } catch (InterruptedException e) {
                   new CatchError("挂起错误",e);
                    Log.d("---------->", "sendthread被挂起");
                    if(breathSocket != null){
                        breathSocket.close();
                    }
                    e.printStackTrace();
                }


            }
        });
        sendthread.start();
      recvthread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    breath_reciver();
                } catch (InterruptedException e) {
                    Log.d("---------->", "recvthread被挂起");

                    if(recvsocket != null){
                        recvsocket.close();
                    }
                    e.printStackTrace();
                }
            }
        });
        recvthread.start();

         return super.onStartCommand(intent,START_FLAG_REDELIVERY, startId);
    }
    /*
    *
    *
    *           初始化套接字
    *
    *
    * */

    private void load_udpSocket(){
        try {
            if(breathSocket == null) {
                breathSocket = new DatagramSocket(null);
                breathSocket.setReuseAddress(true);
                breathSocket.setSoTimeout(50000);
                breathSocket.bind(new InetSocketAddress(3848));
            }
        }catch (SocketException e){

            e.printStackTrace();

        }
    }
    /*
    *
    *
    *
    *               关闭套接字
    *
    *
    *
    * */
    private  void close_udpSocket(){
        if(breathSocket != null || breathSocket.isClosed()) {
            breathSocket.close();
            breathSocket = null;
        }
    }
    public void breath_send(byte[] session) throws InterruptedException {
            Thread.sleep(10*1000);
            InetAddress ip = null;
            try {
                ip = InetAddress.getByName(info.getSeverIp());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    breathSocket = new DatagramSocket(null);
                    breathSocket.setReuseAddress(true);
                    //breathSocket.setSoTimeout(40000);
                    breathSocket.bind(new InetSocketAddress(3848));
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                byte[] breath = breathPacket(session);
                        DatagramPacket dp = new DatagramPacket(breath, breath.length, ip, 3848);
                        notification.setContentInfo("正在发送....");
                        startForeground(100, notification.build());
                        try {
                        breathSocket.send(dp);
                        } catch (Exception e) {
                            new CatchError("发送心跳IOException:",e);
                            //e.printStackTrace();
                            notification.setContentInfo(e.toString() + e.getMessage());
                            startForeground(100, notification.build());
                            intentmsg.putExtra("tag", "心跳错误："+ e.getMessage());
                            intentmsg.putExtra("btnenflag", 0);
                            sendBroadcast(intentmsg);
                            breathSocket.close();
                            break;
                        }
                        notification.setContentInfo("发送完成！");
                        startForeground(100, notification.build());
                        breathSocket.close();
                        breathSocket = null;
                        Thread.sleep(10*1000);
                    }
            }

    public void breath_reciver() throws InterruptedException {
        //Thread.sleep(9000);
        while(true){
                notification.setContentInfo("正在接收...");
                startForeground(100, notification.build());

            try {
                recvsocket = new DatagramSocket(null);
                recvsocket.setReuseAddress(true);
                //recvsocket.setSoTimeout(40000);
                recvsocket.bind(new InetSocketAddress(3848));
            } catch (SocketException e) {
                e.printStackTrace();
            }
            try {

                    byte[] buffer = new byte[4096];
                    DatagramPacket recvdata = new DatagramPacket(buffer, 0, buffer.length);
                recvsocket.receive(recvdata);
                    notification.setContentInfo("接收完成！");
                    startForeground(100, notification.build());
                    int bufferSize = recvdata.getLength();
                    byte[] recvpacket = decrypt(Arrays.copyOf(buffer, bufferSize));
                    byte[] recvmd5 = new byte[16];
                    for (int i = 2; i < 18; i++) {
                        recvmd5[i - 2] = recvpacket[i];
                        recvpacket[i] = 0;
                    }
                    if (compare_Md5(recvmd5, getMd5(recvpacket))) {
                        info.setStatus(recvpacket[20]);
                        if (recvpacket[20] == 1) {
                            index += 3;
                            breathcount++;
                            intentmsg.putExtra("tag", "保持上线成功！次数：" + Integer.toString(breathcount));
                            notification.setContentText("保持上线成功！次数:" + breathcount);
                            startForeground(100, notification.build());
                            intentmsg.putExtra("btnenflag", 1);
                            sendBroadcast(intentmsg);
                        } else {
                            intentmsg.putExtra("tag", "保持上线失败！" + Integer.toString(breathcount));
                            notification.setContentText("第" + breathcount + "保持失败");
                            startForeground(100, notification.build());
                            intentmsg.putExtra("btnenflag", 0);
                            sendBroadcast(intentmsg);
                            recvsocket.close();
                            recvsocket = null;
                            sendthread.interrupt();
                            recvthread.interrupt();
                            break;
                        }
                    }
                }catch (Exception e) {
                new CatchError("接收心跳IOException:",e);
                        intentmsg.putExtra("tag", "心跳错误：" + e.getMessage());
                        intentmsg.putExtra("btnenflag", 0);
                        sendBroadcast(intentmsg);
                        e.printStackTrace();
                recvsocket.close();
                recvsocket = null;
                    break;

                }
                    recvsocket.close();
                    recvsocket = null;
                    //Thread.sleep(9000);
                }


            }

    /*
    *
    *
    *       创建呼吸包
    *
    *
    * */
    public byte[] breathPacket(byte[] session){
        int packetLen = session.length+88;
        byte[] breathpacket = new byte[packetLen];
        byte i=-1;
        breathpacket[++i] = 0x03;
        breathpacket[++i] = (byte)breathpacket.length;
        for(;i<17;){					//md5储存位置
            breathpacket[++i] = 0;
        }
        breathpacket[++i] = 0x08;
        breathpacket[++i] = (byte) (session.length+2);
        for(byte b:session){
            breathpacket[++i] = b;
        }
        breathpacket[++i] = 0x09;
        breathpacket[++i] = 0x12;
        byte[] locIp = info.getLocalIP().getBytes();
        for(byte b:locIp){
            breathpacket[++i] = b;
        }
        for(int z = 0;z<16-locIp.length;z++){
            breathpacket[++i] = 0;
        }
        breathpacket[++i] = 0x07;
        breathpacket[++i] = 0x08;
        String[] macs = info.getMacadd().split(":");
        for (String str : macs) {
            breathpacket[++i] = (byte) Integer.parseInt(str, 16);
        }
        breathpacket[++i] = 0x14;
        breathpacket[++i] = 0x06;
        String indexHex = String.format("%x", index);
        int indexLen = indexHex.length();
        breathpacket[++i] = (byte)(Integer.parseInt(indexHex.substring(0, indexLen - 6) , 16));
        breathpacket[++i] = (byte)(Integer.parseInt(indexHex.substring(indexLen - 6, indexLen - 4) , 16));
        breathpacket[++i] = (byte)(Integer.parseInt(indexHex.substring(indexLen-4, indexLen-2) ,16));
        breathpacket[++i] = (byte)(Integer.parseInt(indexHex.substring(indexLen-2, indexLen-0) , 16));
        for(byte b: info.getBlock()){
            breathpacket[++i] = b;
        }
        byte[] md5 = getMd5(breathpacket);
        for(int n = 0;n<md5.length;n++){
            breathpacket[n+2] = md5[n];
        }
        breathpacket = encrypt(breathpacket);
        return breathpacket;
    }
    /*创建下线包*/
    public byte[] downPacket(byte[] session){
        int packetLen = session.length+88;
        byte[] downpacket = new byte[packetLen];
        byte i=-1;
        downpacket[++i] = 0x05;
        downpacket[++i] = (byte)packetLen;
        for(;i<17;){					//md5储存位置
            downpacket[++i] = 0;
        }
        downpacket[++i] = 0x08;
        downpacket[++i] = (byte) (session.length+2);
        for(byte b:session){
            downpacket[++i] = b;
        }
        downpacket[++i] = 0x09;
        downpacket[++i] = 0x12;
        byte[] locIp = info.localIp.getBytes();
        for(byte b:locIp){
            downpacket[++i] = b;
        }
        for(int z = 0;z<16-locIp.length;z++){
            downpacket[++i] = 0;
        }
        downpacket[++i] = 0x07;
        downpacket[++i] = 0x08;
        String[] macs = info.getMacadd().split(":");
        for (String str : macs) {
            downpacket[++i] = (byte) Integer.parseInt(str, 16);
        }
        downpacket[++i] = 0x14;
        downpacket[++i] = 0x06;
        String indexHex = String.format("%x", index);
        int indexLen = indexHex.length();
        downpacket[++i] = (byte)(Integer.parseInt(indexHex.substring(0, indexLen - 6) , 16));
        downpacket[++i] = (byte)(Integer.parseInt(indexHex.substring(indexLen - 6, indexLen - 4) , 16));
        downpacket[++i] = (byte)(Integer.parseInt(indexHex.substring(indexLen-4, indexLen-2) ,16));
        downpacket[++i] = (byte)(Integer.parseInt(indexHex.substring(indexLen-2, indexLen-0) , 16));
        for(byte b: info.getBlock()){
            downpacket[++i] = b;
        }
        byte[] md5 = getMd5(downpacket);
        for(int n = 0;n<md5.length;n++){
            downpacket[n+2] = md5[n];
        }
        downpacket = encrypt(downpacket);
        return downpacket;
    }

    /*
    *
    *       下线操作
    * */
    public void download(){
        sendthread.interrupt();
        recvthread.interrupt();
    index += 3;
    byte[] downpacket = downPacket(info.session);
    InetAddress address = null;
    try {
        address = InetAddress.getByName(info.getSeverIp());
    } catch (UnknownHostException e) {
        e.printStackTrace();
    }
    try {
        DatagramSocket ds = new DatagramSocket(null);
        ds.setReuseAddress(true);
        ds.bind(new InetSocketAddress(3848));
        DatagramPacket dp = new DatagramPacket(downpacket,downpacket.length,address,3848);
        ds.send(dp);
        byte[] buffer = new byte[4096];
        DatagramPacket recvdp = new DatagramPacket(buffer,buffer.length);
        ds.receive(recvdp);
        intentmsg.putExtra("tag", "下线成功");
        intentmsg.putExtra("btnenflag",0);
        sendBroadcast(intentmsg);
        ds.close();
        stopSelf();
    } catch (IOException e) {
        intentmsg.putExtra("tag","下线失败，请检查网络是否已断开");
        intentmsg.putExtra("btnenflag",0);
        sendBroadcast(intentmsg);
        e.printStackTrace();
    }
}
/*
*
*           获取MD5
* */
private static byte[] getMd5(byte[] md5){
    MessageDigest md5text = null;
    try {
        md5text = MessageDigest.getInstance("MD5");

    }catch (NoSuchAlgorithmException e){
        e.printStackTrace();
    }
    byte[] md5_packet = md5text.digest(md5);
    return md5_packet;
}
/*
 *
 *
 *
 * 				加密Packet
 *
 *
 * */
    private static byte[] encrypt(byte[] packet) {
        byte[] encrypt_packet = new byte[packet.length];
        int i = 0;
        for (byte b : packet) {
            encrypt_packet[i++] = (byte) ((b & 0x80) >> 6 | (b & 0x40) >> 4 | (b & 0x20) >> 2 | (b & 0x10) << 2
                    | (b & 0x08) << 2 | (b & 0x04) << 2 | (b & 0x02) >> 1 | (b & 0x01) << 7);
        }
        return encrypt_packet;
    }

    /*
     *
     * 				解密算法
     *
     *
     * */
    private static byte[] decrypt(byte[] packet) {
        byte[] decrypt_packet = new byte[packet.length];
        int i = 0;
        for (byte b : packet) {
            decrypt_packet[i++] = (byte) ((b & 0x80) >> 7 | (b & 0x40) >> 2 | (b & 0x20) >> 2 | (b & 0x10) >> 2
                    | (b & 0x08) << 2 | (b & 0x04) << 4 | (b & 0x02) << 6 | (b & 0x01) << 1);
        }
        return decrypt_packet;
    }
    /*
	 *
	 *
	 * 				MD5比较算法
	 * 				比较两个md5的位数是否相同
	 * 				相同return true
	 * 				不同return false
	 *
	 *
	 * */
    private static boolean compare_Md5(byte[] n1,byte[] n2){
        boolean flag = true;
        for(int i = 0;i<n1.length;i++)
        {
            if(n1[i]!=n2[i]){
                flag = false;
                break;
            }
        }
        return flag;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override

    public void onDestroy() {
        alive=false;
        releaseWakeLock();

        //stopForeground(false);
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        intentmsg.putExtra("alive",alive);
        sendBroadcast(intentmsg);
    }
    public void serviceBrocast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("notiService");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                threadlife = intent.getBooleanExtra("thread",true);
                if(!threadlife){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                                download();
                            intentmsg.putExtra("clean",true);
                            sendBroadcast(intentmsg);
                        }
                    }).start();

                }
            }
        };
        registerReceiver(broadcastReceiver,intentFilter);
    }
    public void disconnected(){
        try {
            DatagramSocket listendisconet = new DatagramSocket(4999);
            byte[] disconet = new byte[4096];
            DatagramPacket dp = new DatagramPacket(disconet,disconet.length);

            intentmsg.putExtra("tag","正在监听4999端口");
            sendBroadcast(intentmsg);
            listendisconet.receive(dp);
            final int len = dp.getLength();
            //byte[] data = decrypt(Arrays.copyOf(disconet,len));
            intentmsg.putExtra("tag","获取到的长度:"+len);
            sendBroadcast(intentmsg);
            listendisconet.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
