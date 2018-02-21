package org.pitest.mutationtest.build;

import java.util.logging.Logger;

class MutationDiscoveryTest$HasLogger {
   private static Logger log = Logger.getLogger(MutationDiscoveryTest$HasLogger.class.getName());

   public void call(int i) {
      log.info("foo " + i);
   }
}
