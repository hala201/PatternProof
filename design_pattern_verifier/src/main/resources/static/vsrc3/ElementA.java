// this is for manual testing

public class ElementA implements IElement {
    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }

    public String exclusiveMethodOfElementA() {
        return "ElementA";
    }
}
