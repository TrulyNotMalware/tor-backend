package com.hackerton.tor.torback.recommender.model;

import com.hackerton.tor.torback.common.Utils;
import com.mysql.cj.jdbc.MysqlDataSource;
import jdk.jshell.execution.Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;

@Slf4j
public class DataModels {

    public DataModel getDataModel(){
        MysqlDataSource dataSource = Utils.getMySQLDataSource();
        //Join Table lists.
        JDBCDataModel jdbcDataModel = new MySQLJDBCDataModel(
                dataSource,
                "product_preferences",
                "userNumber",
                "productId",
                "preference",
                null);
        try{
            log.debug("Return New");
            return new ReloadFromJDBCDataModel(jdbcDataModel);
        } catch (TasteException e){
            e.printStackTrace();
            return jdbcDataModel;
        }
//        return jdbcDataModel;
    }

    public DataModel getPresetDataModel(){
        MysqlDataSource dataSource = Utils.getMySQLDataSource();
        JDBCDataModel jdbcDataModel = new MySQLJDBCDataModel(
                dataSource,
                "preset_preferences",
                "userNumber",
                "presetId",
                "preference",
                null);
        try{
            log.debug("Return New");
            return new ReloadFromJDBCDataModel(jdbcDataModel);
        } catch (TasteException e){
            e.printStackTrace();
            return jdbcDataModel;
        }
    }
}
