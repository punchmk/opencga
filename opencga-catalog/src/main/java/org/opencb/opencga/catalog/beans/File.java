package org.opencb.opencga.catalog.beans;

import org.opencb.opencga.lib.common.TimeUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by jacobo on 11/09/14.
 */
public class File {

    private int id;
    /**
     * File name.
     */
    private String name;
    /**
     * Formats: file, folder
     */
    private String type;
    /**
     * Formats: txt, executable, image, ...
     */
    private String format;
    /**
     * BAM, VCF, ...
     */
    private String bioformat;
    /**
     * file://, hdfs://
     */
    private String uriScheme;
    private String path;
    private String creatorId;
    private String creationDate;
    private String description;

    private String status;
    private long diskUsage;

    //private int studyId;
    private int experimentId;
    private List<Integer> sampleIds;
    /**
     * This field values -1 when file has been uploaded.
     */
    private int jobId;
    private List<Acl> acl;


    private Map<String, Object> stats;
    private Map<String, Object> attributes;

    /* Status */
    public static final String UPLOADING = "uploading";
    public static final String UPLOADED = "uploaded";
    public static final String READY = "ready";

    /* Type */
    public static final String FOLDER = "folder";
    public static final String FILE = "file";

    /* Formats */
    public static final String PLAIN = "plain";
    public static final String GZIP = "gzip";
    public static final String EXECUTABLE = "executable";
    public static final String IMAGE = "image";

    /**
     * To think:
     * ACL, url,  responsible,  extended source ??
     */

    public File() {
    }

    public File(String name, String type, String format, String bioformat, String uriScheme, String path, String creatorId,
                String description, String status, long diskUsage) {
        this(-1, name, type, format, bioformat, uriScheme, path, creatorId, TimeUtils.getTime(), description, status, diskUsage,
                -1, new LinkedList<Integer>(), -1, new LinkedList<Acl>(), new HashMap<String, Object>(), new HashMap<String, Object>());
    }

    public File(String name, String type, String format, String bioformat, String uriScheme, String path, String creatorId,
                String creationDate, String description, String status, long diskUsage) {
        this(-1, name, type, format, bioformat, uriScheme, path, creatorId, creationDate, description, status, diskUsage,
                -1, new LinkedList<Integer>(), -1, new LinkedList<Acl>(), new HashMap<String, Object>(), new HashMap<String, Object>());
    }

    public File(int id, String name, String type, String format, String bioformat, String uriScheme, String path,
                String creatorId, String creationDate, String description, String status, long diskUsage, int experimentId,
                List<Integer> sampleIds, int jobId, List<Acl> acl, Map<String, Object> stats, Map<String, Object> attributes) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.format = format;
        this.bioformat = bioformat;
        this.uriScheme = uriScheme;
        this.path = path;
        this.creatorId = creatorId;
        this.creationDate = creationDate;
        this.description = description;
        this.status = status;
        this.diskUsage = diskUsage;
        this.experimentId = experimentId;
        this.sampleIds = sampleIds;
        this.jobId = jobId;
        this.acl = acl;
        this.stats = stats;
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return "File{" + "\n\t" +
                "id=" + id + "\n\t" +
                ", name='" + name + '\'' + "\n\t" +
                ", type='" + type + '\'' + "\n\t" +
                ", format='" + format + '\'' + "\n\t" +
                ", bioformat='" + bioformat + '\'' + "\n\t" +
                ", uriScheme='" + uriScheme + '\'' + "\n\t" +
                ", path='" + path + '\'' + "\n\t" +
                ", creatorId='" + creatorId + '\'' + "\n\t" +
                ", creationDate='" + creationDate + '\'' + "\n\t" +
                ", description='" + description + '\'' + "\n\t" +
                ", status='" + status + '\'' + "\n\t" +
                ", diskUsage=" + diskUsage + "\n\t" +
                ", experimentId=" + experimentId + "\n\t" +
                ", sampleIds=" + sampleIds + "\n\t" +
                ", jobId=" + jobId + "\n\t" +
                ", acl=" + acl + "\n\t" +
                ", stats=" + stats + "\n\t" +
                ", attributes=" + attributes + "\n" +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getBioformat() {
        return bioformat;
    }

    public void setBioformat(String bioformat) {
        this.bioformat = bioformat;
    }

    public String getUriScheme() {
        return uriScheme;
    }

    public void setUriScheme(String uriScheme) {
        this.uriScheme = uriScheme;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(long diskUsage) {
        this.diskUsage = diskUsage;
    }

    public int getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(int experimentId) {
        this.experimentId = experimentId;
    }

    public List<Integer> getSampleIds() {
        return sampleIds;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public List<Acl> getAcl() {
        return acl;
    }

    public void setAcl(List<Acl> acl) {
        this.acl = acl;
    }

    public void setSampleIds(List<Integer> sampleIds) {
        this.sampleIds = sampleIds;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }
}