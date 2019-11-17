package com.changgou.test;

import com.changgou.util.IdWorker;

public class IdTest {
    public static void main(String[] args) {

        for (int i = 0; i < 100; i++) {
            IdWorker idWorker = new IdWorker();
            long id = idWorker.nextId();
            System.out.println(id);
        }

    }

}
