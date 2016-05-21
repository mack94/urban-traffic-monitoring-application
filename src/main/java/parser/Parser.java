package main.java.parser;

import main.java.input.Input;

import java.io.*;

/**
 * Created by Dawid on 2016-05-20.
 */
public class Parser {
    private File file;
    private BufferedReader br;
    private String buffer;

    public Parser(File file){
        this.file = file;
    }
    public void parse(Input input){
        try {
            this.br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            buffer = br.readLine();
            while(buffer!=null) {
                input.addLine(buffer);
                buffer = br.readLine();
            }
            input.persist();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
