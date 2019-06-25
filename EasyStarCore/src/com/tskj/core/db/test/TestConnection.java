package com.tskj.core.db.test;

import com.tskj.core.db.DbUtility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class TestConnection implements Runnable {

    private int index;

    public TestConnection(int index) {
        this.index = index;
    }

    @Override
    public void run() {
        List<Map<String, Object>> list = DbUtility.execSQL("SELECT top 100 * FROM DOCUMENT2999D17160F3467A");
        System.out.println(index + " = " + list.size());
    }

//    public static void main(String[] args) {
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//        try {
//            do {
//                for (int i = 0; i < 20; i++) {
//                    new Thread(new TestConnection(i + 1)).start();
//                }
//                System.out.print("按任意键继续，输入quit退出");
//            } while (!br.readLine().toLowerCase().equals("quit"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}

