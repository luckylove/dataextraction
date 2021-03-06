package com.trues.config.model;


import org.apache.commons.lang.StringUtils;

public class Report {

    private String name;
    private String directory;
    private String fileName;
    private String queryType;
    private String query;
    private String decode;

    public String getDecode() {
        return decode;
    }

    public void setDecode(String decode) {
        this.decode = decode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getQuery() {
        if (StringUtils.isNotEmpty(query)) {
            return query.trim();
        }
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
