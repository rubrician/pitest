package org.pitest.mutationtest.build;

import org.pitest.mutationtest.engine.gregor.CoverageIgnore;
import org.pitest.mutationtest.engine.gregor.DoNotMutate;
import org.pitest.mutationtest.engine.gregor.Generated;

public class MutationDiscoveryTest$AnnotatedToAvoidMethod {
   public int a() {
      return 1;
   }

   @Generated
   public int b() {
      return 1;
   }

   @DoNotMutate
   public int c() {
      return 1;
   }

   @CoverageIgnore
   public int d() {
      return 1;
   }

   public int e() {
      return 1;
   }
}
