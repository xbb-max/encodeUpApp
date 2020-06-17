package cn.yunting.utils;

public class EncodeAAC {
    static {
        System.loadLibrary("faac");
        // System.loadLibrary("mp3dec");
    }
    public native int encodeAAc(byte[] inputFilePath,int inputLen,byte[] outputFilePath,int outputLen);
}
