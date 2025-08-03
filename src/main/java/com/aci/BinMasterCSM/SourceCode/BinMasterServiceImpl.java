package com.aci.BinMasterCSM.SourceCode;


import com.aci.BinMasterCSM.Entity.BinMaster;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Service
public class BinMasterServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(BinMasterServiceImpl.class);
    @Value("${resourceFile.inputPath}")
    private String inputPath;
    @Value("${resourceFile.prefix}")
    private String prefix;
    @Value("${resourceFile.suffix}")
    private String suffix;
    @Value("${resourceFile.donePrefix}")
    private String donePrefix;
    @Value("${error.threashold}")
    private int errorThreashold;
    @Value(("${resourceFile.runningPrefix}"))
    private String runningPrefix;
    @Value(("${resourceFile.errorPrefix}"))
    private String errorPrefix;
    @Value(("${cardType.acceptanceValue}"))
    private String acceptanceValue;
    @Autowired
    private SessionFactory sessionFactory;

    @Transactional
    public ResponseEntity<String> automateBinMaster() {
        final int[] sqlCodeHolder = new int[1];
        final String[] sqlMessageHolder = new String[1];
        int processedFileCount = 0;
        int errorFileCount = 0;
        try {
            BinMaster binMaster = new BinMaster();
            File path = new File(inputPath);
            List<String> listOfFiles = getListofFilesToBeProcessed(path);
            if (listOfFiles.isEmpty()) {
                logger.info("\nNo new files to process.\n");
                return ResponseEntity.ok("No new files to process.");
            } else {
                for (String filePath : listOfFiles) {
                    int errorRows = 0;
                    int processedRows = 0;
                    int rows = 0;
                    File file = new File(filePath);
                    File runningName = new File(filePath.replaceAll(prefix, runningPrefix));
                    file.renameTo(runningName);
                    String runningFilePath = runningName.getPath();
                    try (BufferedReader lineReader = new BufferedReader(new InputStreamReader(new FileInputStream(runningFilePath), "UTF-8"))) {
                        String sqlMessage = null;
                        String lineText;
                        while ((lineText = lineReader.readLine()) != null) {
                            rows++;
                            int sqlCode = 1;
                            String[] data = lineText.split(",");
                            binMaster.setBinno(data[0].trim());
                            binMaster.setCard_type(data[1].trim());
                            logger.info("{}", isValidBinMaster(binMaster));
                            if (rows == 1 && !isValidBinMaster(binMaster)) {
                                logger.info("Error occur for first row binNo : {} and cardType : {}", binMaster.getBinno(), binMaster.getCard_type());
                                break;
                            } else if (isValidBinMaster(binMaster)) {
                                Session session = sessionFactory.getCurrentSession();
                                session.doWork(connection1 -> {
                                    try (CallableStatement callableStatement = connection1.prepareCall("{ CALL pp_csm_update_bin_master_new(?,?,?,?) }");) {
                                        callableStatement.setString(1, binMaster.getBinno());
                                        callableStatement.setString(2, binMaster.getCard_type());
                                        callableStatement.registerOutParameter(3, Types.INTEGER);
                                        callableStatement.registerOutParameter(4, Types.VARCHAR);
                                        callableStatement.execute();
                                        sqlCodeHolder[0] = callableStatement.getInt(3);
                                        sqlMessageHolder[0] = callableStatement.getString(4);
                                    } catch (SQLException sqlException) {
                                        sqlException.printStackTrace();
                                        sqlMessageHolder[0] = "Error executing procedure: " + sqlException.getMessage();
                                        sqlCodeHolder[0] = -1;
                                    }
                                });
                                sqlCode = sqlCodeHolder[0];
                                sqlMessage = sqlMessageHolder[0];
                            }
                            if (sqlCode == 0) {
                                processedRows++;
                            } else {
                                logger.info("Error occur for binNo: {} and cardType: {}\nSQLCode: {}\nSQLMessage: {}", binMaster.getBinno(), binMaster.getCard_type(), sqlCode, sqlMessage);
                                errorRows++;
                            }
                            if (errorRows == errorThreashold) {
                                break;
                            }
                        }
                    }
                    if (errorRows < errorThreashold && rows != 1) {
                        File doneName = new File(runningFilePath.replaceAll(runningPrefix, donePrefix));
                        boolean isProcessed = runningName.renameTo(doneName);
                        if (isProcessed) {
                            processedFileCount++;
                            logger.info("\nExisting File: {}\nNew File: {}\nTotal rows processed: {}\n", runningFilePath, doneName, processedRows);
                        }
                    } else {
                        File errorName = new File(runningFilePath.replaceAll(runningPrefix, errorPrefix));
                        boolean isError = runningName.renameTo(errorName);
                        if (isError) {
                            errorFileCount++;
                            logger.info("\nExisting File: {}\nNew File: {}\n", runningFilePath, errorName);
                        }
                    }
                }
                logger.info("Total Processed Files: {}\nTotal Error Files: {}", processedFileCount, errorFileCount);
                return ResponseEntity.ok("Files processed successfully: " + processedFileCount);
            }
        } catch (Exception e) {
            logger.info("\nError occurred while inserting data: {}\n", e.getMessage());
            return ResponseEntity.ok("Error occurred while inserting data");
        }
    }

    private boolean isValidBinMaster(BinMaster binMaster) {
        return (binMaster.getBinno().length() >= 6 && binMaster.getBinno().length() <= 8 && acceptanceValue.contains(binMaster.getCard_type()));
    }

    private List<String> getListofFilesToBeProcessed(File path) {
        List<String> listPath = new ArrayList<>();
        File[] firstLevelFiles = path.listFiles();
        if (firstLevelFiles != null) {
            for (File file : firstLevelFiles) {
                if (!file.isDirectory() && file.toString().toLowerCase().contains(prefix) && file.toString().toLowerCase().endsWith(suffix)) {
                    listPath.add(file.getAbsolutePath());
                }
            }
        }
        return listPath;
    }
}
