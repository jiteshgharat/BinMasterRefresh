package com.aci.BinMasterCSM.Repo;


import com.aci.BinMasterCSM.Entity.BinMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BinMasterRepo extends JpaRepository<BinMaster, String> {
}