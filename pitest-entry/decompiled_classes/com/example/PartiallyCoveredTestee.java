package com.example;

public class PartiallyCoveredTestee {
   public int coverMe() {
      return 1;
   }

   public int dontCoverMe() {
      return true?0:1;
   }
}
