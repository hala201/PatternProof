// this is for manual testing

public class ConcreteVisitor implements IVisitor {
    public Number visit(ElementA element) {
        return ((ElementA) element).getFromA();
    }
    public Number visit(ElementB element) {
        return ((ElementB) element).getFromA();
    }
}
