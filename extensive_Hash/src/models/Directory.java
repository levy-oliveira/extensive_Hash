package models;

import csv.CsvReader;
import hashFunction.Hasher;
import models.dto.OutFileLine;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Directory {
    private int GL;
    private final List<Line> lines = new ArrayList<>();
    Logger logger = Logger.getLogger(getClass().getName());

    public Directory(int GL) {
        this.GL = GL;
        List<String> binaryNumbers = generateBinaryNumbers(GL);
        binaryNumbers.forEach(binary -> {
            Bucket bucket = new Bucket(binary);
            Line line = new Line(binary, bucket, GL);
            lines.add(line);
        });
        logger.info("Directory created with global depth: " + GL);
    }

    public int getGL() {
        return GL;
    }

    public void setGL(int GL) {
        this.GL = GL;
    }

    public List<Line> getLines() {
        return lines;
    }

    public class Dephts {
        private int global;
        private int local;

        public void setDephts(int global, int local) {
            this.global = global;
            this.local = local;
        }
        public void setGlobal(int global) {
            this.global = global;
        }

        public void setLocal(int local) {
            this.local = local;
        }


        public int getGlobal() {
            return global;
        }

        public int getLocal() {
            return local;
        }
    }
    public int[] retornaDeph(int global, int local) {
        int[] valores = new int[2];
        valores[0] = local;
        valores[1] = global;
        return valores;
    }
    private static List<String> generateBinaryNumbers(int depth) {
        List<String> binaryNumbers = new ArrayList<>();
        int totalNumbers = (int) Math.pow(2, depth);
        for (int i = 0; i < totalNumbers; i++) {
            StringBuilder binary = new StringBuilder(Integer.toBinaryString(i));
            while (binary.length() < depth) {
                binary.insert(0, "0");
            }
            binaryNumbers.add(binary.toString());
        }
        return binaryNumbers;
    }

    public Line searchByIndex(String index) {
        return lines.stream()
                .filter(line -> Objects.equals(line.getIndex(), index))
                .findFirst()
                .orElse(null);
    }

    public long search(int key) {
        String bucketIndex = Hasher.hash(key, GL);
        List<Line> matchingLines = lines.stream()
                .filter(line -> Objects.equals(line.getIndex(), bucketIndex))
                .collect(Collectors.toList());

        return matchingLines.stream()
                .filter(line -> line.getBucket() != null && line.getBucket().getInData().stream().anyMatch(shopping -> shopping.getYear() == key))
                .count();
    }

    public List<OutFileLine> insert(int key, BufferedWriter writer) {
        List<Shopping> shoppings = CsvReader.readCsv();
        return shoppings.stream()
                .filter(shopping -> shopping.getYear() == key)
                .map(s -> insertIndividual(key, s, writer))
                .collect(Collectors.toList());
    }

    public OutFileLine insertIndividual(int key, Shopping shoppingToBeAdded, BufferedWriter writer) {
        String bucketName = Hasher.hash(key, GL);
        List<Line> matchingLines = lines.stream()
                .filter(line -> Objects.equals(line.getIndex(), bucketName))
                .toList();

        Dephts depths =  new Dephts();

        Bucket bucket = matchingLines.get(0).getBucket();
        OutFileLine outFileLine = new OutFileLine();

        if (bucket.getInData().size() <= 2) {
            bucket.getInData().add(shoppingToBeAdded);
            logger.info("Shopping item with key " + key + " inserted in bucket " + bucket.getName());
        } else { // bucket is full
            logger.info("Bucket " + bucket.getName() + " is full. Creating a new bucket.");
            Bucket newBucket = new Bucket();
            if (matchingLines.size() > 1) {
                if (bucket.getInData().size() == 2) {
                    matchingLines.get(1).setBucket(newBucket);
                    divideBucket(matchingLines.get(0), matchingLines.get(1), GL, shoppingToBeAdded);
                } else { // bucket is not full
                    bucket.getInData().add(shoppingToBeAdded);
                }
            } else { // localDepth == globalDepth
                if (bucket.getInData().size() < 2) {
                    bucket.getInData().add(shoppingToBeAdded);
                } else { // bucket is full
                    String newIndex = "1" + matchingLines.get(0).getIndex();
                    duplicateDirectory();
                    Line line = searchByIndex(newIndex);
                    divideBucket(matchingLines.get(0), line, GL, shoppingToBeAdded);
                    insertIndividual(key, shoppingToBeAdded, writer);
                    depths.setLocal(line.getLocalDepth());
                    outFileLine.setDuplicated(true);
                }
            }
        }

        depths.setGlobal(GL);
        if (depths.getLocal() == 0) {
            depths.setLocal(matchingLines.get(0).getLocalDepth());
        }
        int[] retorno = retornaDeph(depths.getGlobal(),depths.getLocal());
        outFileLine.setDepths(retorno);
        return outFileLine;
    }

    private void divideBucket(Line oldLine, Line newLine, int depth, Shopping newValue){
        oldLine.getBucket().getInData().add(newValue);
        List<Shopping> oldBucketData = new ArrayList<>(oldLine.getBucket().getInData());

        Bucket newBucket = new Bucket(oldLine.getBucket().getName() + "k");
        newLine.setBucket(newBucket);

        oldBucketData.forEach(key -> {
            String newBucketName = Hasher.hash(key.getYear(), depth);
            if (newBucketName.equals(newLine.getIndex())) {
                newLine.getBucket().getInData().add(key);
                oldLine.getBucket().getInData().remove(key);
            }
        });

        oldLine.setLocalDepth(depth);
        newLine.setLocalDepth(depth);
    }

    private void duplicateDirectory() {
        GL++;
        int size = lines.size();
        for (int i = 0; i < size; i++) {
            Line line = lines.get(i);
            Line newLine = new Line();

            newLine.setIndex("1" + line.getIndex());
            line.setIndex("0" + line.getIndex());
            newLine.setBucket(line.getBucket());

            newLine.setLocalDepth(line.getLocalDepth());
            lines.add(newLine);
        }

        logger.info("Directory duplicated. New global depth: " + GL);
    }

    public int[] remove(int key) {
        String bucketIndex = Hasher.hash(key, GL);
        Line line = lines.stream()
                .filter(DLine -> Objects.equals(DLine.getIndex(), bucketIndex))
                .findFirst()
                .orElse(null);

        int[] tuplesRemoved = new int[3];

        if (line != null) {
            Bucket bucket = line.getBucket();
            List<Shopping> shoppingToBeDeleted = bucket.getInData().stream().filter(s -> s.getYear() == key).toList();
            bucket.getInData().removeAll(shoppingToBeDeleted);
            tuplesRemoved[0] = shoppingToBeDeleted.size();
            tuplesRemoved[1] = GL;
            tuplesRemoved[2] = line.getLocalDepth();
            logger.info("Removed " + tuplesRemoved[0] + " shopping items with key " + key + " from bucket " + bucket.getName());
        }

        return tuplesRemoved;
    }
}