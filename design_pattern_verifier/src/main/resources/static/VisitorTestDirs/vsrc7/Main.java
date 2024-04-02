// this is for manual testing

public interface IElement {
    void accept(IVisitor visitor);
} 

public class ElementA implements IElement {
    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }

    public String exclusiveMethodOfElementA() {
        return "ElementA";
    }
}

public class ElementB implements IElement {
    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }

    public String exclusiveMethodOfElementB() {
        return "ElementB";
    }
}

public class ElementC implements IElement {
    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }

    public String exclusiveMethodOfElementB() {
        return "ElementC";
    }
}

public interface IVisitor {
    void visit(IElement element);
} 

public class ConcreteVisitor implements IVisitor {
    public void visit(IElement element) {
        if (element instanceof ElementA) {
            System.out.println("Visiting " + ((ElementA) element).exclusiveMethodOfElementA());
        } else if (element instanceof ElementB) {
            System.out.println("Visiting " + ((ElementB) element).exclusiveMethodOfElementB());
        }
    }
}

public class Main {
    public static void main(String[] args) {
        IElement[] elements = {new ElementA(), new ElementB(), new ElementC()};
        IVisitor visitor = new ConcreteVisitor();
        for (IElement element : elements) {
            element.accept(visitor);
        }
    }
}
