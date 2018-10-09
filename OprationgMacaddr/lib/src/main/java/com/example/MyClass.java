package com.example;

public class MyClass {
    public static void main(String[] args) {
        Funtion a = new Encrypt();
        a.encrypt();
    }
}
abstract class  Funtion{
    void encrypt(){}
    void discrypt(){}
}
class Encrypt extends Funtion{

    public void encrypt(String macaddr) {
        int len = macaddr.length();
        byte[] encrpack = new byte[len];
        System.out.println("show:"+Integer.parseInt(macaddr,16));
    }
}
