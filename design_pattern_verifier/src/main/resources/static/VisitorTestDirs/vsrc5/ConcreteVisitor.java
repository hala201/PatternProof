// this is for manual testing

public class ConcreteVisitor implements IVisitor {
    public void visit(IElement element) {
        if (element instanceof ElementA) {
            System.out.println("Visiting " + ((ElementA) element).exclusiveMethodOfElementA());
        } else {
            System.out.println("Visiting an unknown element type.");
        }
    }
}
