package testDynamicProxy;

public class TempAImpl implements TempAInterface{
    @Override
    public String call() {
        System.out.println("TempAImpl call");
        return "data";
    }
}
