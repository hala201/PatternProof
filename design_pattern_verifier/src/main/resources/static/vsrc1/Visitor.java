// this is for manual testing

import java.util.ArrayList;
import java.util.List;

interface Element {
    void accept(Visitor visitor);
}

class Book implements Element {
    private double price;
    private String isbnNumber;

    public Book(double price, String isbn) {
        this.price = price;
        this.isbnNumber = isbn;
    }

    public double getPrice() {
        return this.price;
    }

    public String getIsbnNumber() {
        return this.isbnNumber;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

class Clothing implements Element {
    private double price;
    private String size;

    public Clothing(double price, String size) {
        this.price = price;
        this.size = size;
    }

    public double getPrice() {
        return this.price;
    }

    public String getSize() {
        return this.size;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}

interface Visitor {
    void visit(Book book);
    void visit(Clothing clothing);
}

class PriceVisitor implements Visitor {
    private double totalPrice = 0;

    @Override
    public void visit(Book book) {
        this.totalPrice += book.getPrice();
        System.out.println("Book ISBN: " + book.getIsbnNumber() + " Price: " + book.getPrice());
    }

    @Override
    public void visit(Clothing clothing) {
        this.totalPrice += clothing.getPrice();
        System.out.println("Clothing Size: " + clothing.getSize() + " Price: " + clothing.getPrice());
    }

    public double getTotalPrice() {
        return this.totalPrice;
    }
}

public class Visitor1 {
    public static void main(String[] args) {
        List<Element> items = new ArrayList<>();
        items.add(new Book(20.5, "1234X"));
        items.add(new Clothing(45.75, "L"));

        PriceVisitor priceVisitor = new PriceVisitor();

        for(Element item : items) {
            item.accept(priceVisitor);
        }

        System.out.println("Total Price: " + priceVisitor.getTotalPrice());
    }
}
