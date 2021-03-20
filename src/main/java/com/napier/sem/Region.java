package com.napier.sem;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Class for Region
 */

public class Region {
    // Placeholder for SQL connection
    private Connection con = null;
    // Region Name
    private String regionName;
    // Region Population
    private long regionPopulation;

    public Region(){

    }

    public Connection getCon() {
        return con;
    }

    public void setCon(Connection con) {
        this.con = con;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    // Returns sum population of a region
    public long getRegionPopulation() {
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "select Region, sum(Population) as Population from country where Region = '"+regionName+"'";
            ResultSet rset = stmt.executeQuery(strSelect);
            if(rset.next()){
                regionPopulation = rset.getLong("Population");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return regionPopulation;
    }

    public void setRegionPopulation(long regionPopulation) {
        this.regionPopulation = regionPopulation;
    }
}