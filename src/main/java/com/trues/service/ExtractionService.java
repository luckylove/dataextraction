package com.trues.service;

import com.trues.config.model.Env;
import com.trues.config.model.Report;
import com.trues.util.TrueUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: son.nguyen
 * Date: 10/27/13
 * Time: 9:16 AM
 */
public class ExtractionService {

    private Logger logger = LoggerFactory.getLogger(ExtractionService.class);

    private Env config;

    public ExtractionService(Env config) {
        this.config = config;
    }


    public void doExtractText() throws Exception {
        List<Report> reports = config.getReports();
        for (Report report : reports) {
            logger.info(" ---------- extrac for report {} ----------------", report.getName());
            File checkFileName = checkFileName(report);
            if (checkFileName != null) {
                logger.info(" query type {} {}", report.getQueryType(), report.getQuery());
                if ("SQL".equals(report.getQueryType())) {
                    Map<String, Object> namedParams = new HashMap<String, Object>(2);
                    namedParams.put("startDate", TrueUtils.getDate(config.getStarDate()));
                    namedParams.put("endDate", TrueUtils.getDate(config.getEndDate()));
                    NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(config.getJdbcTemplate());
                    List<String[]> results = template.query(report.getQuery(), namedParams, parseResult());
                    //write to csv
                    TrueUtils.export2Csv(checkFileName, results);
                } else {

                    final List<String[]> results = new ArrayList<String[]>();
                    SimpleJdbcCall simpleJdbcCall1 = new SimpleJdbcCall(config.getJdbcTemplate())
                            .useInParameterNames(
                                    "IN_STARTDATE",
                                    "IN_ENDDATE"
                            ).returningResultSet("c_dbuser", new RowMapper() {
                                @Override
                                public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    String[] strings = {};
                                    ResultSetMetaData metaData = rs.getMetaData();
                                    int columnCount = metaData.getColumnCount();
                                    List<String> ts = new ArrayList<String>(columnCount);
                                    if (rowNum == 1) {
                                        for (int i = 1; i <= columnCount; i++) {
                                            logger.info("colums: {}", metaData.getColumnName(i));
                                            ts.add(metaData.getColumnName(i));
                                        }
                                        results.add(ts.toArray(strings));
                                    }
                                    for (int i = 1; i <= columnCount; i++) {
                                        if (rs.getObject(i) != null) {
                                            String data = rs.getObject(i).toString();
                                            ts.add(data);
                                        } else {
                                            ts.add("null");
                                        }
                                    }
                                    results.add(ts.toArray(strings));
                                    return null;
                                }
                            }).withProcedureName(report.getQuery());
                    SqlParameterSource in = new MapSqlParameterSource().addValue("IN_STARTDATE", TrueUtils.getDate(config.getStarDate())).addValue("IN_ENDDATE", TrueUtils.getDate(config.getEndDate()));
                    simpleJdbcCall1.execute(in);
                    logger.info("parse rows : {}", results.size() - 1);
                    //write to csv
                    TrueUtils.export2Csv(checkFileName, results);
                }
            } else {
                logger.info(" file {} exits, so we ignore it  ", config.getStarDate());
            }
            logger.info(" ---------- done extrac for report {} ----------------", report.getName());
        }

    }



    private File checkFileName(Report report) throws IOException {
        File folder = new File(report.getDirectory());
        FileUtils.forceMkdir(folder);
        String fileName = report.getFileName();
        fileName = fileName.replaceAll("#yyyymmdd#", config.getStarDate());
        File file = new File(folder, fileName);
        if (!file.exists()) {
            return file;
        }
        return null;
    }

    private ResultSetExtractor<List<String[]>> parseResult() {
        return new ResultSetExtractor<List<String[]>>() {

            @Override
            public List<String[]> extractData(ResultSet rs) throws SQLException, DataAccessException {

                List<String[]> results = new ArrayList<String[]>();
                String[] strings = {};
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                List<String> ts = new ArrayList<String>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    logger.info("colums: {}", metaData.getColumnName(i));
                    ts.add(metaData.getColumnName(i));
                }
                results.add(ts.toArray(strings));

                //get content
                while(rs.next()){
                    ts = new ArrayList<String>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        if (rs.getObject(i) != null) {
                            String data = rs.getObject(i).toString();
                            ts.add(data);
                        } else {
                            ts.add("null");
                        }
                    }
                    results.add(ts.toArray(strings));
                }
                logger.info("parse rows : {}", results.size() - 1);
                return results;
            }
        };
    }



}
