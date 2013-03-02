#include "ruby.h"

VALUE SWCuda = Qnil;

void Init_SWCuda();

VALUE method_test1(VALUE self, VALUE y);

void Init_SWCuda() {
  SWCuda = rb_define_module("SWCuda");
  rb_define_method(SWCuda, "test1", method_test1, 1);
}

VALUE method_test1(VALUE self, VALUE y) {
  int x = y + 1;
  return INT2NUM(x);
}