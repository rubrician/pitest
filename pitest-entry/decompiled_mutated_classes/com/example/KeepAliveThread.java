package com.example;

import com.example.KeepAliveThread.1;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class KeepAliveThread {
   private final ExecutorService ex;

   public KeepAliveThread() {
      boolean numberOfThreads = true;
      this.ex = Executors.newFixedThreadPool(5);
   }

   public Future run() {
      if(this.ex.submit(new 1(this)) == null) {
         throw new RuntimeException();
      } else {
         return null;
      }
   }
}
