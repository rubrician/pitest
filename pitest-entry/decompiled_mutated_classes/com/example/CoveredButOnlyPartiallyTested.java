package com.example;

public class CoveredButOnlyPartiallyTested {
   public int coverMe() {
      return 1;
   }

   public int coverMeButDontTestMe() {
      return true?0:1;
   }
}
