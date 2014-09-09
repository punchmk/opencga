package org.opencb.opencga.account.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.opencb.opencga.lib.common.ArrayUtils;
import org.opencb.opencga.lib.common.IOUtils;
import org.opencb.opencga.lib.common.ListUtils;
import org.opencb.opencga.lib.common.XObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JobFileIOUtils implements IOManager {

    protected static Logger logger = LoggerFactory.getLogger(JobFileIOUtils.class);

    protected static ObjectMapper jsonObjectMapper;
    protected static ObjectWriter jsonObjectWriter;

    public static String getSenchaTable(Path jobFile, String filename, String start, String limit, String colNames,
                                        String colVisibility, String sort) throws IOManagementException, IOException {


        jsonObjectMapper = new ObjectMapper();
        jsonObjectWriter = jsonObjectMapper.writer();

        int first = Integer.parseInt(start);
        int end = first + Integer.parseInt(limit);
        String[] colnamesArray = colNames.split(",");
        String[] colvisibilityArray = colVisibility.split(",");

        if (!Files.exists(jobFile)) {
            throw new IOManagementException("getFileTableFromJob(): the file '" + jobFile.toAbsolutePath()
                    + "' not exists");
        }

        String name = filename.replace("..", "").replace("/", "");
        List<String> avoidingFiles = getAvoidingFiles();
        if (avoidingFiles.contains(name)) {
            throw new IOManagementException("getFileTableFromJob(): No permission to use the file '"
                    + jobFile.toAbsolutePath() + "'");
        }

        XObject jsonObject = new XObject();

        int totalCount = -1;
        List<String> headLines;
        try {
            headLines = IOUtils.head(jobFile, 30);
        } catch (IOException e) {
            throw new IOManagementException("getFileTableFromJob(): could not head the file '"
                    + jobFile.toAbsolutePath() + "'");
        }

        Iterator<String> headIterator = headLines.iterator();
        while (headIterator.hasNext()) {
            String line = headIterator.next();
            if (line.startsWith("#NUMBER_FEATURES")) {
                totalCount = Integer.parseInt(line.split("\t")[1]);
                break;
            }
        }

        logger.debug("totalCount ---after read head lines ---------> " + totalCount);

        if (totalCount == -1) {
            logger.debug("totalCount ---need to count all lines and prepend it---------> " + totalCount);

            int numFeatures = 0;
            BufferedReader br = Files.newBufferedReader(jobFile, Charset.defaultCharset());
            String line = null;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    numFeatures++;
                }
            }
            br.close();
            totalCount = numFeatures;
            String text = "#NUMBER_FEATURES	" + numFeatures;
            IOUtils.prependString(jobFile, text);
        }

//      sort string example: [{"property":"Term size","direction":"ASC"}]
        if (!sort.isEmpty()) {
            //Column Index
            Map<String, Integer> columnIndex = new HashMap<String, Integer>();
            for (int i = 0; i < colnamesArray.length; i++) {
                columnIndex.put(colnamesArray[i], i);
            }

            ArrayNode sortArrayNode = (ArrayNode) jsonObjectMapper.readTree(sort);
            JsonNode sortNode = sortArrayNode.get(0);


            int numColumn = columnIndex.get(sortNode.get("property").asText());
            String direction = sortNode.get("direction").asText();

            boolean decreasing = false;
            if (direction.equals("DESC")) {
                decreasing = true;
            }

            List<String> dataFile = IOUtils.grep(jobFile, "^[^#].*");

            double[] numbers = ListUtils.toDoubleArray(IOUtils.column(jobFile, numColumn, "\t", "^[^#].*"));

            int[] orderedRowIndices = ArrayUtils.order(numbers, decreasing);

            String[] fields;
            jsonObject.put("total", totalCount);
            List<XObject> items = new ArrayList<>();
            for (int j = 0; j < orderedRowIndices.length; j++) {
                if (j >= first && j < end) {
                    fields = dataFile.get(orderedRowIndices[j]).split("\t");
                    XObject item = new XObject();
                    for (int i = 0; i < fields.length; i++) {
                        if (Integer.parseInt(colvisibilityArray[i].toString()) == 1) {
                            item.put(colnamesArray[i], fields[i]);
                        }
                    }
                    items.add(item);
                } else {
                    if (j >= end) {
                        break;
                    }
                }
            }
            jsonObject.put("items", items);

        } else {// END SORT

            int numLine = 0;
            String line;
            String[] fields;
            BufferedReader br = Files.newBufferedReader(jobFile, Charset.defaultCharset());
            jsonObject.put("total", totalCount);
            List<XObject> items = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    if (numLine >= first && numLine < end) {
                        fields = line.split("\t");
                        XObject item = new XObject();
                        for (int i = 0; i < fields.length; i++) {
                            if (Integer.parseInt(colvisibilityArray[i]) == 1) {
                                item.put(colnamesArray[i], fields[i]);
                            }
                        }
                        items.add(item);
                    } else {
                        if (numLine >= end) {
                            break;
                        }
                    }
                    numLine++;
                }
            }
            br.close();
            jsonObject.put("items", items);
        }

        return jsonObjectWriter.writeValueAsString(jsonObject);
    }

    private static List<String> getAvoidingFiles() {
        List<String> avoidingFiles = new ArrayList<String>();
        avoidingFiles.add("result.xml");
        avoidingFiles.add("cli.txt");
        avoidingFiles.add("form.txt");
        avoidingFiles.add("input_params.txt");
        avoidingFiles.add("job.log");
        avoidingFiles.add("jobzip.zip");
        return avoidingFiles;
    }

    private class TableSort {
        private String property;
        private String direction;

        public TableSort() {
        }

        public TableSort(String prop, String direct) {
            this.setProperty(prop);
            this.setDirection(direct);
        }

        @Override
        public String toString() {
            return property + "::" + direction;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

    }
}
