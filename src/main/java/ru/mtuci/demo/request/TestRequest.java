package ru.mtuci.demo.request;

public class TestRequest {
    String name;
    int number;

    public TestRequest(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public TestRequest() {
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(int number) {
        this.number = number;
    }

}