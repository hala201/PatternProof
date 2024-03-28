// this is for manual testing

import java.util.ArrayList;
import java.util.List;

interface A {
    void foo(B b);
}

class A1 implements A {

    public double get() {
        return 0;
    }

    @Override
    public void foo(B b) {
        b.bee(this);
    }
}


interface B {
    void bee(A1 book);
}

class B1 implements B {

    @Override
    public void bee(A1 a) {
        a.get();
    }
}

public class Visitor1 {
    public static void main(String[] args) {
        List<A> as = new ArrayList<>();
        as.add(new A1());

        B1 b = new B1();

        for(A a : as) {
            a.foo(b);
        }
    }
}
