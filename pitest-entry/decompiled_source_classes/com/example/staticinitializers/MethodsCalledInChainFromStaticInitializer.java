package com.example.staticinitializers;

public class MethodsCalledInChainFromStaticInitializer {
   private static void a() {
      System.out.println("don\'t mutate");
      b();
   }

   private static void b() {
      System.out.println("don\'t mutate");
   }

   static {
      a();
   }
}
