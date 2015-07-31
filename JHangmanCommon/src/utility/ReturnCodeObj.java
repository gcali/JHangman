package utility;

public class ReturnCodeObj<E extends Enum<E>, T> {

    private final E code;
    private final T obj;

    public ReturnCodeObj(E code, T obj) {
        this.code = code;
        this.obj = obj;
    }
    
    public E getCode() {
        return this.code;
    }
    
    public T getObj() {
        return this.obj;
    } 
}