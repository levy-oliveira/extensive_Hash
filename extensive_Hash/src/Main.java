import models.Directory;
import models.dto.OutFileLine;

import java.io.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final String inputPath = "./extensive_Hash/src/externalInfo/in.txt";
        final String outputPath = "./extensive_Hash/src/externalInfo/out.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            String firstLine = reader.readLine();
            int closeIconPosition = firstLine.indexOf("/");
            String globalDepth = firstLine.substring(closeIconPosition + 1);

            Directory directory = new Directory(Integer.parseInt(globalDepth));
            writer.write(String.format("PG/%s%n", globalDepth));

            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line, directory, writer);
            }

            writer.write(String.format("P:/%d", directory.getGL()));
        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
        }
    }

    private static void processLine(String line, Directory directory, BufferedWriter writer) throws IOException {
        String command = line.substring(0, 3);
        int key = command.equals("BUS") ? Integer.parseInt(line.substring(5)) : Integer.parseInt(line.substring(4));

        switch (command) {
            case "INC":
                List<OutFileLine> insertResults = directory.insert(key, writer);
                writer.write("INC:" + line.substring(4) + "/" + insertResults.get(0).getDepths()[0] + "," + insertResults.get(0).getDepths()[1] + "\n");
                if (insertResults.get(0).duplicated) {
                    writer.write("DUP_DIR:" + insertResults.get(0).getDepths()[0] + "," + insertResults.get(0).getDepths()[1] + "\n");
                }
                break;
            case "REM":
                int[] removeResults = directory.remove(key);
                writer.write(String.format("REM:%d/%d,%d,%d%n", key, removeResults[0], removeResults[1], removeResults[2]));
                break;
            case "BUS":
                long searchResult = directory.search(key);
                writer.write(String.format("BUS:%d/%d%n", key, searchResult));
                break;
            default:
                System.err.println("Invalid command: " + command);
                break;
        }
    }
}