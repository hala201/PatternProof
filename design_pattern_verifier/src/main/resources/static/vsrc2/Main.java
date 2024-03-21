// this is for manual testing

public class Main {
    public static void main(String[] args) {
        IElement[] elements = {new ElementA(), new ElementB()};
        IVisitor visitor = new ConcreteVisitor();
        Number result = 0;
        for (IElement element : elements) {
            result = result.doubleValue() + element.accept(visitor).doubleValue();
        }
        System.out.println(result);
    }
}
