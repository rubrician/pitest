package com.example;

import com.example.MockitoFoo;

class MockitoCallFoo {
   final MockitoFoo foo;

   public MockitoCallFoo(MockitoFoo foo) {
      this.foo = foo;
   }

   public void call() {
      this.foo.foo();
   }
}
