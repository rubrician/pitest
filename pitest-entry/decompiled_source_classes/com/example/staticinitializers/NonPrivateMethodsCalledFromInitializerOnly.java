package com.example.staticinitializers;

public class NonPrivateMethodsCalledFromInitializerOnly {
   static void mutateDefault() {
      System.out.println("mutate me");
   }

   protected static void mutateProtected() {
      System.out.println("mutate me");
   }

   public static void mutatePublic() {
      System.out.println("mutate me");
   }

   static {
      mutateDefault();
      mutateProtected();
      mutatePublic();
   }
}
