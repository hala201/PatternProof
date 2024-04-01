package com.example.design_pattern_verifier.ChainOfResponsibility.CorrectCor2;

interface DispenseChain {

    void setNextChain(DispenseChain next);

    void dispense(int c);
}

class Dispense50 implements DispenseChain {
    private DispenseChain next;

    @Override
    public void setNextChain(DispenseChain next) {
        this.next = next;
    }

    @Override
    public void dispense(int c) {
        if (c >= 50) {
            int num = c / 50;
            int remainder = c % 50;
            System.out.println("Dispensing " + num + " 50$");
            if (remainder !=0) {
                this.next.dispense(remainder);
            }
        } else {
            this.next.dispense(c);
        }
    }
}

class Dispense20 implements DispenseChain {
    private DispenseChain next;

    @Override
    public void setNextChain(DispenseChain next) {
        this.next = next;
    }

    @Override
    public void dispense(int c) {
        if (c >= 20) {
            int num = c / 20;
            int remainder = c % 20;
            System.out.println("Dispensing " + num + " 20$");
            if (remainder !=0) {
                this.next.dispense(remainder);
            }
        } else {
            this.next.dispense(c);
        }
    }
}

class Dispense5 implements DispenseChain {
    private DispenseChain next;

    @Override
    public void setNextChain(DispenseChain next) {
        this.next = next;
    }

    @Override
    public void dispense(int c) {
        if (c >= 5) {
            int num = c / 5;
            int remainder = c % 5;
            System.out.println("Dispensing " + num + " 5$");
            if (remainder !=0) {
                this.next.dispense(remainder);
            }
        } else {
            this.next.dispense(c);
        }
    }
}

class Dispense10 implements DispenseChain {
    private DispenseChain next;

    @Override
    public void setNextChain(DispenseChain next) {
        this.next = next;
    }

    @Override
    public void dispense(int c) {
        if (c >= 10) {
            int num = c / 10;
            int remainder = c % 10;
            System.out.println("Dispensing " + num + " 10$");
            if (remainder !=0) {
                this.next.dispense(remainder);
            }
        } else {
            this.next.dispense(c);
        }
    }
}

class Dispense1 implements DispenseChain {
    private DispenseChain next;

    @Override
    public void setNextChain(DispenseChain next) {
        this.next = next;
    }

    @Override
    public void dispense(int c) {
        if (c >= 1) {
            System.out.println("Dispensing " + c + " 1$");
        } else {
            System.out.println("ERROR");
        }
    }
}

public class ChainOfResponsibilityExample2 {
    private DispenseChain chain50;
    public ChainOfResponsibilityExample2() {
        // Creating chain components
        chain50 = new Dispense50();
        Dispense20 chain20;
        chain20 = new Dispense20();
        DispenseChain chain10 = new Dispense10();
        Dispense5 chain5 = new Dispense5();
        Dispense1 chain1 = new Dispense1();

        // Setting chain link
        chain50.setNextChain(chain20);
        chain20.setNextChain(chain10);
        chain10.setNextChain(chain5);
        chain5.setNextChain(chain1);

        chain10 = new Dispense50();
    }

    public static void main(String[] args) {
        ChainOfResponsibilityExample2 atm = new ChainOfResponsibilityExample2();

        int amount = 198;
        atm.chain50.dispense(amount);
    }
}