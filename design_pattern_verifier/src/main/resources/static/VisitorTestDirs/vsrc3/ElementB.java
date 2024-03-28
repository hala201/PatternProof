// this is for manual testing

public class ElementB implements IElement {
    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }

    public String exclusiveMethodOfElementB() {
        return "ElementB";
    }
}
