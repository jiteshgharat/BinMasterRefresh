package com.aci.BinMasterCSM;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.aci.BinMasterCSM.SourceCode.BinMasterServiceImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EntityScan("com.aci.BinMasterCSM.Entity")
@EnableJpaRepositories(basePackages = "com.aci.BinMasterCSM.Repo")
public class BinMasterFinalCsmApplication {
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(BinMasterFinalCsmApplication.class, args);
        BinMasterServiceImpl binMasterServiceImpl = applicationContext.getBean(BinMasterServiceImpl.class);
        binMasterServiceImpl.automateBinMaster();
        SpringApplication.exit(applicationContext);
    }
}