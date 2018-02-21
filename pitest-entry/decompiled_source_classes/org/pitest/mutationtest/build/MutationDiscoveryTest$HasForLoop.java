package org.pitest.mutationtest.build;

class MutationDiscoveryTest$HasForLoop {
   public void foo() {
      for(int i = 0; i != 10; ++i) {
         System.out.println(i);
      }

   }
}
