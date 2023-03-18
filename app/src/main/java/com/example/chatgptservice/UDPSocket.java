package com.example.chatgptservice;


import android.content.Context;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicReference;


public class UDPSocket {
    private static final String TAG = "UDPSocket";

    //指令连续发送次数,防止信号不好收不到
    public static final int UDP_CONTINUE_SEND_COUNT = 1;//UDP发送有延时，发送次数多了导致1秒多还在收这条指令

    private static final int CAMERA_RECEIVE_BUFFER_LENGTH = 300 * 1024;//camera接收BUF长度
    private static final int LEPTON_RECEIVE_BUFFER_LENGTH = 100 * 1024;//lepton接收BUF长度
    private static final int CONTROL_DATA_RECEIVE_BUFFER_LENGTH = 2 * 1024;//控制数据通道接收BUF长度

    private int mRecBufLen = 0;
    private byte[] mReadBuffer = null;

    private int mReceivePort = -1;
    private DatagramPacket mDatagramReceivePacket = null;

    //native socket
    private int mNativeSocketFd = -1;

    private AtomicReference<DatagramSocket> socketRef = new AtomicReference<>();

    public void startUDPSocket(int recPort) {
        DatagramSocket socket = socketRef.get();
        if (socket != null) return;
        try {
            mReceivePort = recPort;

            socket = new DatagramSocket(recPort);
            socket.setReuseAddress(true);
            //这个时间要小于200ms,最好离200毫秒多点空间，因为外部线程停止前是等待200ms
            socket.setSoTimeout(100);//超时时间

            mRecBufLen = CONTROL_DATA_RECEIVE_BUFFER_LENGTH;

            mReadBuffer = new byte[mRecBufLen];

            socketRef.set(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopUDPSocket() {
        try {
            mDatagramReceivePacket = null;

            final DatagramSocket socket = socketRef.get();

            if (socket != null) {
                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //连续发多次，防止信号不好指令丢失
    public void sendMessageWithTimes(Context context, int sendPort, byte[] data, int len, int sendTimes/*发送次数*/) {
        for (int i = 0; i < sendTimes; i++)
            sendMessage(context, sendPort, data, len);
    }

    public void sendMessageX(Context context, int sendPort, byte[] data, int len) {
        try {
            final DatagramSocket socket = socketRef.get();
            if (socket == null || data == null) {
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Context context, int sendPort, byte[] data, int len) {
        try {
            final DatagramSocket socket = socketRef.get();
            if (socket == null || data == null) {
                return;
            }

            DatagramPacket packet = new DatagramPacket(data, len, InetAddress.getByName("192.168.1.3"), sendPort);
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据
     */
    public void receiveMessage() {
        try {
            final DatagramSocket socket = socketRef.get();
            if (socket == null) {
                return;
            }

            if (mDatagramReceivePacket == null) {
                mDatagramReceivePacket = new DatagramPacket(mReadBuffer, mRecBufLen, InetAddress.getByName("192.168.1.112"), mReceivePort);
            } else {
                mDatagramReceivePacket.setData(mReadBuffer);
            }

            socket.receive(mDatagramReceivePacket);

            if (mDatagramReceivePacket == null || mDatagramReceivePacket.getLength() == 0) {
                return;
            }

            if (mDatagramReceivePacket != null) {
                if (mReceiveListener != null) {
                    String ip = mDatagramReceivePacket.getAddress().getHostAddress();
                    mReceiveListener.receiveData(ip, mReceivePort, mDatagramReceivePacket.getData(), mDatagramReceivePacket.getLength());
                }
                mDatagramReceivePacket.setLength(mRecBufLen);//每次接收完UDP数据后，重置长度。否则可能会导致下次收到数据包被截断。
            }

        } catch (Exception e) {
            //e.printStackTrace();
        }
    }


    //接收监听器
    private UDPReceiveListener mReceiveListener = null;

    public void registerReceiveListener(UDPReceiveListener l) {
        mReceiveListener = l;
    }

    public void unRegisterReceiveListener() {
        mReceiveListener = null;
    }
}

