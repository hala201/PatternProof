// this is for manual testing

public class ElementA implements IElement {
    public Number accept(IVisitor visitor) {
        return visitor.visit(this);
    }

    public Number getFromA() {
        return 1000;
    }
}
