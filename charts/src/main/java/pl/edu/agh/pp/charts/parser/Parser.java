package pl.edu.agh.pp.charts.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.pp.charts.input.Input;

import java.io.*;

/**
 * Created by Dawid on 2016-05-20.
 */
public class Parser {

    private final Logger logger = (Logger) LoggerFactory.getLogger(Parser.class);

    private File file = null;
    private BufferedReader br;
    private String buffer;

    public Parser(File file) {
        this.file = file;
    }

    public Parser() {
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void parse(Input input) {
        if (file == null) {
            return;
        }
        try {
            this.br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            buffer = br.readLine();
            while (buffer != null) {
                input.addLine(buffer);
                buffer = br.readLine();
            }
            input.persist();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
