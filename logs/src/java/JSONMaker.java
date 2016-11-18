import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JSONMaker {
    private String[] waypoints = new String[56];
    private Map<Integer, String> daysOfWeek = new HashMap<>();

    public static void main(String[] args) throws IOException, ParseException {
        new JSONMaker().doIt();
    }

    private void doIt() throws IOException, ParseException {
        setDaysOfWeek();
        setWaypoints();
        File origin = new File("/home/drzuby/archiveLogs");

        String unzipped = "/home/drzuby/unzippedLogs";
        File unzippedDir = new File(unzipped);
        unzippedDir.mkdirs();

        String parsed = "/home/drzuby/parsedLogs";
        File parsedDir = new File(parsed);
        parsedDir.mkdirs();

        String zippedAgain = "/home/drzuby/zippedAgain";
        String aggregated = zippedAgain + File.separator + "aggregated";
        new File(aggregated).mkdirs();

//        System.out.print("UNZIPPING: ");
//        unzip(origin, unzipped);
//        System.out.println("DONE");
        System.out.print("RENAMING: ");
        renameFiles(unzippedDir);
        System.out.println("DONE");
        System.out.print("FORMATTING: ");
        parseFiles(unzippedDir, parsed);
        System.out.println("DONE");
        System.out.print("ZIPPING: ");
        zipAgain(parsedDir, zippedAgain);
        System.out.println("DONE");
    }

    private void renameFiles(File dir) throws ParseException {
        Pattern pattern = Pattern.compile("TrafficLog_(\\d+?)_(\\d+?)___([a-zA-Z]*)[_]*(.*?)[.]log");
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                renameFiles(file);
            } else if (file.getName().endsWith(".log")) {
                String filename = file.getName();
                Matcher matcher = pattern.matcher(filename);
                matcher.find();
                String begin = matcher.group(1);
                String end = matcher.group(2);
                String day = matcher.group(3);
                String date = matcher.group(4);
                if ("".equals(day)) {
                    day = getDayOfWeek(date);
                    String newName = new StringBuilder(file.getParentFile().getAbsolutePath())
                            .append(File.separator)
                            .append("TrafficLog_")
                            .append(begin)
                            .append("_")
                            .append(end)
                            .append("___")
                            .append(day)
                            .append("_")
                            .append(date)
                            .append(".log")
                            .toString();
                    file.renameTo(new File(newName));
                }
            }
        }
    }

    private String getDayOfWeek(String date) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
        Date date1 = dateFormat.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        return daysOfWeek.get(calendar.get(Calendar.DAY_OF_WEEK));
    }

    private void parseFiles(File dir, String relativePath) throws IOException {
        for (File originFile : dir.listFiles()) {
            BufferedReader br = null;
            FileOutputStream out = null;
            try {
                if (originFile.isDirectory()) {
                    String newFilePath = relativePath + "/" + originFile.getName();
                    new File(newFilePath).mkdir();
                    parseFiles(originFile, newFilePath);
                } else {
                    if (!originFile.getName().endsWith(".log")) {
                        continue;
                    }
                    File newFile = new File(relativePath + "/" + originFile.getName());
                    br = new BufferedReader(new FileReader(originFile));
                    out = new FileOutputStream(newFile);
                    String line;
                    while ((line = br.readLine()) != null) {
                        String output = parseLine(line);
                        if (output != null) {
                            out.write((output + "\n").getBytes());
                        }
                    }
                }
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String parseLine(String logLine) {
        try {
            JSONObject json = new JSONObject(logLine);
            json.remove("isAnomaly");
            json.put("anomalyId", "");
            return json.toString();
        } catch (Exception e) {
            System.out.println("------" + logLine);
            return null;
        }
    }

    private static void unzip(File dir, String outputDir) throws IOException {
        for (File file : dir.listFiles((dir1, name) -> name.endsWith(".zip") || dir1.isDirectory())) {
            if (file.isDirectory()) {
                unzip(file, outputDir + File.separator + file.getName());
            } else {
                ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
                ZipEntry entry;
                String fileDir = file.getName().substring(0, file.getName().length() - 4);

                while ((entry = zis.getNextEntry()) != null) {
                    String filename = entry.getName();
                    File newFile = new File(outputDir + File.separator + fileDir + File.separator + filename);
                    new File(newFile.getParent()).mkdirs();

                    FileOutputStream fos = new FileOutputStream(newFile);
                    byte[] buffer = new byte[1024];

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    fos.close();
                }
                zis.closeEntry();
                zis.close();
            }
        }
    }

    private void zipAgain(File dir, String output) throws IOException {
        for (File file : dir.listFiles(pathname -> pathname.isDirectory())) {
            if (file.getName().equals("aggregated")) {
                zipAgain(file, output + File.separator + "aggregated");
            } else {
                File[] logFiles = file.listFiles((dir1, name) -> name.endsWith(".log"));
                FileOutputStream fos = new FileOutputStream(output + File.separator + file.getName() + ".zip");
                ZipOutputStream zos = new ZipOutputStream(fos);
                for (File logFile : logFiles) {
                    ZipEntry entry = new ZipEntry(logFile.getName());
                    zos.putNextEntry(entry);

                    FileInputStream in = new FileInputStream(logFile);
                    byte[] buffer = new byte[1024];

                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    in.close();
                }
                zos.closeEntry();
                zos.close();
            }
        }
    }

    private void setWaypoints() throws IOException {
        InputStream inputStream = System.class.getResourceAsStream("/routes.json");

        String jsonTxt = IOUtils.toString(inputStream);
        JSONObject jsonObject = new JSONObject(jsonTxt);

        JSONArray array = jsonObject.getJSONArray("routes");

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            int id = object.getInt("id");
            waypoints[id - 1] = object.getString("coords");
        }
    }

    private void setDaysOfWeek() {
        daysOfWeek.put(1, "Sun");
        daysOfWeek.put(2, "Mon");
        daysOfWeek.put(3, "Tue");
        daysOfWeek.put(4, "Wed");
        daysOfWeek.put(5, "Thu");
        daysOfWeek.put(6, "Fri");
        daysOfWeek.put(7, "Sat");
    }
}