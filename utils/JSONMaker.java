import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JSONMaker
{
    public static void main(String[] args) throws IOException
    {
        File origin = new File("/home/ubuntu/archiveLogs");

        String unzipped = "/home/drzuby/unzippedLogs";
        File unzippedDir = new File(unzipped);
        unzippedDir.mkdirs();

        String parsed = "/home/ubuntu/parsedLogs";
        File parsedDir = new File(parsed);
        parsedDir.mkdirs();

        String zippedAgain = "/home/ubuntu/zippedAgain";
        String aggregated = zippedAgain + File.separator + "aggregated";
        new File(aggregated).mkdirs();

        System.out.print("UNZIPPING: ");
        unzip(origin, unzipped);
        System.out.println("DONE");
        System.out.print("FORMATTING: ");
        parseFiles(unzippedDir, parsed);
        System.out.println("DONE");
        System.out.print("ZIPPING: ");
        zipAgain(parsedDir, zippedAgain);
        System.out.println("DONE");
    }

    private static void parseFiles(File dir, String relativePath) throws IOException {
        for (File originFile : dir.listFiles()) {
            BufferedReader br = null;
            FileOutputStream out = null;
            try {
                if (originFile.isDirectory()) {
                    String newFilePath = relativePath + "/" + originFile.getName();
                    new File(newFilePath).mkdir();
                    parseFiles(originFile, newFilePath);
                } else {
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

    private static String parseLine(String logLine) {
        try {
            String bezKonca = logLine.substring(0, logLine.length() - 2);
            String bezPoczatku = bezKonca.substring(27, bezKonca.length());
            String chybaJson = bezPoczatku.substring(2, bezPoczatku.length() - 2);

            String id = chybaJson.substring(2, 4);
            if (id.endsWith("\"")) {
                id = id.substring(0, 1);
            }
            String timeStamp = logLine.substring(1, 24);
            if (chybaJson.endsWith("]}")) {
                chybaJson += "}";
            }

            JSONObject json = new JSONObject(chybaJson);
            json.remove(id);
            json.put("id", id);
            json.put("timeStamp", timeStamp);

            JSONObject waypoints = json.getJSONObject("me_result");
            String duration = waypoints.getString("Duration");
            waypoints.remove("Duration");
            json.put("duration", duration);
            String durationInTraffic = waypoints.getString("DurationInTraffic");
            waypoints.remove("DurationInTraffic");
            json.put("durationInTraffic", durationInTraffic);
            String distance = waypoints.getString("Distance");
            waypoints.remove("Distance");
            json.put("distance", distance);
            waypoints.remove("Status");

            json.remove("me_result");
            JSONArray waypointsArray = waypoints.getJSONArray("Waypoints");
            json.put("waypoints", waypointsArray);
            return json.toString();
        } catch (Exception e) {
            System.out.println("------" + logLine);
            return null;
        }
    }

    private static void unzip(File dir, String outputDir) throws IOException
    {
        for (File file : dir.listFiles((dir1, name) -> name.endsWith(".zip") || dir1.isDirectory()))
        {
            if(file.isDirectory()) {
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

    private static void zipAgain(File dir, String output) throws IOException
    {
        for (File file : dir.listFiles(pathname -> pathname.isDirectory()))
        {
            if(file.getName().equals("aggregated")) {
                zipAgain(file, output + File.separator + "aggregated");
            }
            else {
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
}