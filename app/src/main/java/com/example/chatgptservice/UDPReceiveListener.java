package com.example.chatgptservice;


public interface UDPReceiveListener {
    void receiveData(String ip, int port, byte[] data, int len);
}