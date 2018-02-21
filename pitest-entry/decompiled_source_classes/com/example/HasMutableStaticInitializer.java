package com.example;

public class HasMutableStaticInitializer {
   public static int i;
   public static int j;

   public static void noticeMe() {
   }

   static {
      int a = Integer.valueOf(100).intValue();
      i = a;
      j = i + 1;
   }
}
