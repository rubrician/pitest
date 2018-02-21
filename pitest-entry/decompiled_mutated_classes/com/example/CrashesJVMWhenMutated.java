package com.example;

public class CrashesJVMWhenMutated {
   public static void crashJVM(int i) {
      if(i != 0) {
         crashJVM();
      }

   }

   private static void crashJVM() {
      Runtime.getRuntime().halt(2);
   }
}
