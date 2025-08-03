# BinMasterRefresh
Purpose and Functionality of BinMasterRefresh:

1. Primary Objective:
The primary purpose of BinMasterRefresh is to import and update BINs (Bank Identification Numbers) and card types from an Excel sheet into the database.

2. Performance Efficiency:
The system is capable of processing and saving 20,000 BIN records in 28 milliseconds.

3. Data Validation:
Each BIN and associated card type undergoes a validation process prior to being saved.
Records with invalid BINs or card types are excluded from the database insert operation.

4. Error Logging:
All invalid records are logged into a designated log file for further review and auditing purposes.

5. Threshold-Based Rollback:
If the number of invalid records exceeds a predefined threshold, none of the data (including valid rows) will be saved to the database.
This ensures data integrity and prevents partial data uploads.

Technologies Used - Java, SpringBoot, PostgreSQL.
