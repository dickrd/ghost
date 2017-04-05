package storage;

import content.Record;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Dick Zhou on 3/28/2017.
 * Stores content in a CSV file.
 */
public class CsvFileStorage {

    private BufferedWriter bufferedWriter;

    public CsvFileStorage() throws IOException {
        this("content.csv");
    }

    public CsvFileStorage(String fileName) throws IOException {
        bufferedWriter = new BufferedWriter(new FileWriter(fileName, true));
    }

    public void store(Record aRecord) throws IOException {
        bufferedWriter.write(aRecord.toString());
        bufferedWriter.flush();
    }

    public void close() throws IOException {
        bufferedWriter.close();
    }
}
