package testDynamicProxy;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

public class JDKProxyTest {
    @Test
    void test1(){
        TempAInterface target1 = new TempAImpl();
        TimeInvocationHandler handler = new TimeInvocationHandler(target1);

        TempAInterface proxy = (TempAInterface) Proxy.newProxyInstance(TempAInterface.class.getClassLoader(), new Class[]{TempAInterface.class},handler );
        proxy.call();
        System.out.println(target1.getClass().getName());
        System.out.println(proxy.getClass().getName());
    }

}
