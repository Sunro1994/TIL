package testDynamicProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TimeInvocationHandler implements InvocationHandler {
    private final Object object;

    public TimeInvocationHandler(Object object) {
        this.object = object;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Proxy 수행");
        long startTime = System.currentTimeMillis();
        Object reuslt = method.invoke(object, args);
        long endTime = System.currentTimeMillis();
        System.out.println("Proxy 수행 시간 ={ "+ (endTime- startTime) +"}");
        return endTime-startTime;
    }
}
