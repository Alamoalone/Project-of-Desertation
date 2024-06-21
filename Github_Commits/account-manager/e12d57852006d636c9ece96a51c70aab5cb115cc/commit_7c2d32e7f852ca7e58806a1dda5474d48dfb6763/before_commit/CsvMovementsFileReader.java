package org.sgitario.accountmanager.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.sgitario.accountmanager.entities.Movement;
import org.sgitario.accountmanager.entities.Profile;
import org.sgitario.accountmanager.exceptions.UnsupportedDateFormatException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class CsvMovementsFileReader implements MovementsFileReader {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private final Reader reader;
    private final CSVParser csvParser;
    private final Profile profile;
    private final Iterator<CSVRecord> records;

    public CsvMovementsFileReader(File file, Profile profile) throws IOException {
        this.reader = new FileReader(file);
        this.csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        this.profile = profile;
        this.records = csvParser.iterator();
        // skip headers
        if (records.hasNext()) {
            records.next();
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
        csvParser.close();
    }

    @Override
    public boolean hasNext() {
        return records.hasNext();
    }

    @Override
    public Movement next() {
        var row = records.next();
        String subject = row.get(profile.columnSubject);
        Movement movement = new Movement();
        movement.subject = subject;
        movement.accountingDate = parseDate(row.get(profile.columnAccountingDate));
        movement.valueDate = parseDate(row.get(profile.columnValueDate));
        movement.quantity = Double.parseDouble(row.get(profile.columnQuantity));
        movement.profile = profile;
        return movement;
    }

    private Date parseDate(String value) {
        try {
            return DATE_FORMATTER.parse(value);
        } catch (ParseException e) {
            throw new UnsupportedDateFormatException("Could not parse date '" + value + "'");
        }
    }
}
