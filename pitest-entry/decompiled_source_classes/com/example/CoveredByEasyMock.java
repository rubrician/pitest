package com.example;

import com.example.CoveredByEasyMock.AnInterface;

public class CoveredByEasyMock {
   public void doStuff(AnInterface ai) {
      for(int i = 0; i != 2; ++i) {
         ai.callMe();
      }

   }
}
