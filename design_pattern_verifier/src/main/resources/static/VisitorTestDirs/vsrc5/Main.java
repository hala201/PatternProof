// this is for manual testing

public class Main {
    public static void main(String[] args) {
        IElement[] elements = {new ElementA(), new ElementB()};
        IVisitor visitor = new ConcreteVisitor();
        for (IElement element : elements) {
            element.accept(visitor);
        }
    }
}
