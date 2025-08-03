package com.aci.BinMasterCSM.Entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@Table(name = "binmaster")
public class BinMaster {
    @Id
    @Column(name = "binno")
    private String binno;
    @Column(name = "card_type")
    private String card_type;
}