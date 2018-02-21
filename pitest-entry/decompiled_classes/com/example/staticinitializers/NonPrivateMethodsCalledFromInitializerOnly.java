package com.example.staticinitializers;

import java.io.PrintStream;

public class NonPrivateMethodsCalledFromInitializerOnly {
   static void mutateDefault() {
      System.out.println("mutate me");
   }

   protected static void mutateProtected() {
      System.out.println("mutate me");
   }

   public static void mutatePublic() {
      PrintStream var10000 = System.out;
      String var10001 = "mutate me";
   }

   static {
      mutateDefault();
      mutateProtected();
      mutatePublic();
   }
}
