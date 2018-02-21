package com.example;

import java.io.InputStream;
import org.pitest.util.IsolationUtils;

public class LoadsResourcesFromClassPath {
   public static boolean loadResource() {
      InputStream stream = IsolationUtils.getContextClassLoader().getResourceAsStream("resource folder with spaces/text in folder with spaces.txt");
      boolean result = stream != null;
      return result;
   }
}
