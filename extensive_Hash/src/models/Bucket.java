package models;

import java.util.ArrayList;
import java.util.List;

public class Bucket {
    private String name;
    private List<Shopping> inData = new ArrayList<>();

    public Bucket(String name, List<Shopping> inData) {
        this.setName(name);
        this.setInData(inData);
    }

    public Bucket(String name) {
        this.setName(name);
    }

    public Bucket(List<Shopping> inData) {
        setInData(inData);
    }

    public Bucket(){
        inData = new ArrayList<Shopping>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Shopping> getInData() {
        return inData;
    }

    public void setInData(List<Shopping> inData) {
        this.inData = inData;
    }

}