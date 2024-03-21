// this is for manual testing

public class ElementB implements IElement {
    public Number accept(IVisitor visitor) {
        return visitor.visit(this);
    }

    public Number getFromA() {
        return 2000;
    }
}
