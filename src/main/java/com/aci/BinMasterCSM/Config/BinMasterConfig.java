package com.aci.BinMasterCSM.Config;

import com.aci.BinMasterCSM.init.AppConstants;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.security.GeneralSecurityException;

@Configuration
@EnableTransactionManagement
public class BinMasterConfig {
    @Value("${database.dataSourceUrl}")
    private String dataSourceUrl;
    @Value("${database.driverClassName}")
    private String driverClass;
    @Value("${database.username}")
    private String username;
    @Value("${database.password}")
    private String encryptedPassword;
    @Value("${jpa.database-platform}")
    private String hibernateDialect;

    private static Properties appConfigProps = new Properties();
    private SecretKeySpec key;
    byte[] salt = "1234567".getBytes();
    int iterationCount = 40000;
    int keyLength = 128;
    private BufferedReader reader;
    private ArrayList<String> encPwdList;
    private Console console;
    private static String secret;

    public static String getSecret() {
        try {
            appConfigProps.load(new FileInputStream(AppConstants.CONFIG_PATH + "config.properties"));
            Set<String> propKey = appConfigProps.stringPropertyNames();
            TreeSet<String> treeSet = new TreeSet(propKey);
            secret = ((String) treeSet.iterator().next()).substring(1);
        } catch (FileNotFoundException var2) {
            var2.printStackTrace();
        } catch (IOException var3) {
            var3.printStackTrace();
        }
        return secret;
    }

    public static BinMasterConfig newInstance(String configurationId) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return new BinMasterConfig(configurationId);
    }

    public BinMasterConfig(String configurationId) throws NoSuchAlgorithmException, InvalidKeySpecException {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.encPwdList = new ArrayList();
        this.console = System.console();
        this.key = this.createSecretKey(configurationId.toCharArray(), this.salt, this.iterationCount, this.keyLength);
    }

    private SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        PBEKeySpec keySpec = new PBEKeySpec(password, salt, iterationCount, keyLength);
        SecretKey keyTmp = keyFactory.generateSecret(keySpec);
        return new SecretKeySpec(keyTmp.getEncoded(), "AES");
    }

    public String decrypt(String encryptedPassword) throws GeneralSecurityException, IOException {
        Cipher pbeCipher = Cipher.getInstance(AppConstants.AES + AppConstants.SEP + AppConstants.ECB + AppConstants.SEP + AppConstants.PKCS5PADDING);
        pbeCipher.init(Cipher.DECRYPT_MODE, key);
        return new String(pbeCipher.doFinal(base64Decode(encryptedPassword)), StandardCharsets.UTF_8);
    }

    private byte[] base64Decode(String property) {
        return Base64.getDecoder().decode(property);
    }

    @Bean
    public DataSource dataSource() throws Exception {
        BinMasterConfig binMasterConfig = new BinMasterConfig(getSecret());
        String decryptedPassword = binMasterConfig.decrypt(encryptedPassword);
        return DataSourceBuilder.create().url(dataSourceUrl).username(username).password(decryptedPassword).driverClassName(driverClass).build();
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();
        localSessionFactoryBean.setDataSource(dataSource);
        localSessionFactoryBean.setPackagesToScan("com.aci.BinMasterFinalCSM");
        localSessionFactoryBean.setHibernateProperties(hibernateProperties());
        return localSessionFactoryBean;
    }

    @Bean
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) throws Exception {
        return new HibernateTransactionManager(sessionFactory);
    }

    public Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", hibernateDialect);
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", true);
        return properties;
    }
}