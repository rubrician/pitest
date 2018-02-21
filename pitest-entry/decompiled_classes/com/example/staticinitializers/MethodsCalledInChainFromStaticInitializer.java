package com.example.staticinitializers;

import java.io.PrintStream;

public class MethodsCalledInChainFromStaticInitializer {
   private static void a() {
      System.out.println("don\'t mutate");
      b();
   }

   private static void b() {
      PrintStream var10000 = System.out;
      String var10001 = "don\'t mutate";
   }

   static {
      a();
   }
}
