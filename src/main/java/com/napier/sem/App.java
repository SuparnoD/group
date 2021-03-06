package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;

/**
 * Main Interaction Class for program
 * @author Suparno Deb, Alexandru Pintea, Stephen Messer, Vassilis Papadodimas
 */
public class App {
    private Connection con = null;

    public static void main(String[] args) {
        // Create new Application
        App app = new App();

        // Establishing SQL connection for objects
        if (args.length < 1)
        {
            app.connect("db:3306");
        }
        else
        {
            app.connect(args[0]);
        }
        City city = new City();
        Country country = new Country();
        Continent continent = new Continent();
        continent.setContinentName("Europe");
        country.setCountryCode("IND");
        city.setCityID(4079);


        System.out.println("The population of Europe: " + app.continentPopulation(continent));
        app.generateCityReport(city);
        app.generateCountryReport(country);
        app.printCityList(app.generateCityTopN(3));
        app.printCountryList(app.generateCountryTopN(3));
        app.disconnect();
    }

    /**
     * Connect to the MySQL database.
     */
    public void connect(String location)
    {
        try
        {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i)
        {
            System.out.println("Connecting to database...");
            try
            {
                // Wait a bit for db to start
                Thread.sleep(30000);
                // Connect to database
                con = DriverManager.getConnection("jdbc:mysql://" + location + "/world?allowPublicKeyRetrieval=true&useSSL=false", "root", "example");
                System.out.println("Successfully connected");
                break;
            }
            catch (SQLException sqle)
            {
                System.out.println("Failed to connect to database attempt " + Integer.toString(i));
                System.out.println(sqle.getMessage());
            }
            catch (InterruptedException ie)
            {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect() {
        if (con != null) {
            try {
                // Close connection
                con.close();
            } catch (Exception e) {
                System.out.println("Error closing connection to database");
            }
        }
    }

    /**
     *******************************************************************************************************************
     ********************************************** METHODS FOR CITY ***************************************************
     *******************************************************************************************************************
     */

    /**
     Generates a full report of a city where ID is specified, in the following format
     City ID: {cityID}
     City Name: {cityName}
     Country Code: {countryCode}
     City District: {cityDistrict}
     City Population: {cityPopulation}
     */
    public void generateCityReport(City city){
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "select * from world.city where ID = '" + city.getCityID() + "'";
            ResultSet rset = stmt.executeQuery(strSelect);
            if(rset.next()){
                city.setCityID(rset.getInt("ID"));
                city.setCityName(rset.getString("Name"));
                city.setCountryCode(rset.getString("CountryCode"));
                city.setCityDistrict(rset.getString("District"));
                city.setCityPopulation(rset.getLong("Population"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("City ID: " + city.getCityID());
        System.out.println("City Name: " + city.getCityName());
        System.out.println("Country Code: " + city.getCountryCode());
        System.out.println("City District: " + city.getCityDistrict());
        System.out.println("City Population: " + city.getCityPopulation());
    }


    /**
     * Generates a list of all cities (ordered from population largest - smallest)
     */
    public ArrayList<City> generateCityLargestToSmallest(){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name, country.Name AS Country, city.District, city.Population from world.city inner join country on city.CountryCode = country.Code order by city.Population desc";
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()){
                City city = new City();
                city.setCityName(rset.getString("Name"));
                city.setCountryName(rset.getString("Country"));
                city.setCityDistrict(rset.getString("District"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cityList;
    }

    /**
     * Generates a list of the top N populated cities (where N is specified by the user)
     * @param number
     */
    public ArrayList<City> generateCityTopN(int number){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name, country.Name AS Country, city.District, city.Population from world.city inner join country on city.CountryCode = country.Code order by city.Population desc limit " + number;
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()){
                City city = new City();
                city.setCityName(rset.getString("Name"));
                city.setCountryName(rset.getString("Country"));
                city.setCityDistrict(rset.getString("District"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cityList;
    }

    /**
     * Generates a list of the cities in a country organised by largest population to smallest
     * @param country
     */
    public ArrayList<City> generateCityPopulation(String country){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name, country.Name AS Country, city.District, city.Population from world.city inner join country on city.CountryCode = country.Code where country.Name = '" + country + "' order by Population desc";
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()){
                City city = new City();
                city.setCityName(rset.getString("Name"));
                city.setCountryName(rset.getString("Country"));
                city.setCityDistrict(rset.getString("District"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return cityList;
    }

    /**
     * Generates a list on all the cities in a continent organised by largest population to smallest
     * @param continent
     */
    public ArrayList<City> generateCityPopulationInContinent(String continent){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name, country.Name AS Country, city.District, city.Population from world.city inner join country on city.CountryCode = country.Code where country.Continent = '"+continent+"' order by city.Population desc";
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()){
                City city = new City();
                city.setCityName(rset.getString("Name"));
                city.setCountryName(rset.getString("Country"));
                city.setCityDistrict(rset.getString("District"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return cityList;
    }

    /**
     * Generates a list of all the capital cities in a continent organised by largest population to smallest
     * @param continent
     */
    public ArrayList<City> generateCapitalPopulationInContinent(String continent){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name AS 'CapitalCity', country.Name AS 'Country', city.Population from city inner join country on city.Id = country.capital where Continent = '"+continent+"' order by city.Population desc";
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()){
                City city = new City();
                city.setCityName(rset.getString("CapitalCity"));
                city.setCountryName(rset.getString("Country"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return cityList;
    }

    /**
     * Generates a list on the top N populated capital cities in a continent where N is provided by the user
     * @param continent
     * @param number
     */
    public ArrayList<City> generateTopNCapitalPopulationInContinent(String continent, int number){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name AS 'CapitalCity', country.Name AS 'Country', city.Population from city inner join country on city.Id = country.capital where Continent = '"+continent+"' order by city.Population desc limit " + number;
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()){
                City city = new City();
                city.setCityName(rset.getString("CapitalCity"));
                city.setCountryName(rset.getString("Country"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return cityList;
    }

    /**
     * Generate a list of the top N populated capital cities in the world where N is provided by the user
     * @param number
     */
    public ArrayList<City> generateTopNCapitalCities(int number){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name AS 'CapitalCity', country.Name AS 'Country', city.Population from city inner join country on city.Id = country.capital order by city.Population desc limit " + number;
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()) {
                City city = new City();
                city.setCityName(rset.getString("CapitalCity"));
                city.setCountryName(rset.getString("Country"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return cityList;
    }

    /**
     * Prints the ArrayList containing information regarding City to console
     * @param cityList
     */
    public void printCityList(ArrayList<City> cityList){
        if((cityList == null)){
            System.out.println("Empty Set");
            return;
        }
        System.out.println(String.format("%-10s %-15s %-20s %-15s", "City", "Country", "District", "Population"));
        for(City city : cityList){
            if(city == null)
                continue;
            String cityString =
                    String.format("%-15s %-15s %-20s %-15s",
                            city.getCityName(), city.getCountryName(), city.getCityDistrict(), city.getCityPopulation());
            System.out.println(cityString);
        }
    }

    /**
     * Generates a list of the top N populated capital cities in a region where N is provided by the user
     * @param region
     * @param number
     * @return
     */
    public ArrayList<City> generateTopNCapitalPopulationInRegion(String region, int number){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name AS 'CapitalCity', country.Name AS 'Country', city.Population from city inner join country on city.Id = country.capital where Region = '"+region+"' order by city.Population desc limit " +number;
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()) {
                City city = new City();
                city.setCityName(rset.getString("CapitalCity"));
                city.setCountryName(rset.getString("Country"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return cityList;
    }

    /**
     * Generates a list of the top N populated cities in a region where N is provided by the user
     * @param region
     * @param number
     * @return
     */
    public ArrayList<City> generateTopNCityPopulationInRegion(String region, int number){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name, country.Name AS Country, city.District, city.Population from world.city inner join country on city.CountryCode = country.Code where country.Region = '"+region+"' order by city.Population desc limit " + number;
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()) {
                City city = new City();
                city.setCityName(rset.getString("Name"));
                city.setCountryName(rset.getString("Country"));
                city.setCityDistrict(rset.getString("District"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return cityList;
    }

    /**
     * Generates a list of the top N populated cities in a country where N is provided by the user
     * @param country
     * @param number
     * @return
     */
    public ArrayList<City> generateTopNCityPopulationInCountry(String country, int number){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name, country.Name AS Country, city.District, city.Population from world.city inner join country on city.CountryCode = country.Code where country.Name = '"+country+"' order by city.Population desc limit " + number;
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()) {
                City city = new City();
                city.setCityName(rset.getString("Name"));
                city.setCountryName(rset.getString("Country"));
                city.setCityDistrict(rset.getString("District"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return cityList;
    }

    /**
     * Generates a list of the top N populated cities in a continent where N is provided by the user
     * @param continent
     * @param number
     * @return
     */
    public ArrayList<City> generateTopNCityPopulationInContinent(String continent, int number){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name, country.Name AS Country, city.District, city.Population from world.city inner join country on city.CountryCode = country.Code where country.Continent = '"+continent+"' order by city.Population desc limit " + number;
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()) {
                City city = new City();
                city.setCityName(rset.getString("Name"));
                city.setCountryName(rset.getString("Country"));
                city.setCityDistrict(rset.getString("District"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return cityList;
    }

    /**
     * Generates a list of the top N populated cities in a district where N is provided by the user
     * @param district
     * @param number
     * @return
     */
    public ArrayList<City> generateTopNCityPopulationInDistrict(String district, int number){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name, country.Name AS Country, city.District, city.Population from world.city inner join country on city.CountryCode = country.Code where city.District = '"+district+"' order by city.Population desc limit " + number;
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()) {
                City city = new City();
                city.setCityName(rset.getString("Name"));
                city.setCountryName(rset.getString("Country"));
                city.setCityDistrict(rset.getString("District"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return cityList;
    }

    /**
     * Generates a list of all the cities in a region organised by largest population to smallest
     * @param region
     * @return
     */
    public ArrayList<City> generateCityPopulationInRegionLargestToSmallest(String region){
        ArrayList<City> cityList = new ArrayList<City>();
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name, country.Name AS Country, city.District, city.Population from world.city inner join country on city.CountryCode = country.Code where country.Region = '" + region + "' order by city.Population desc";
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()) {
                City city = new City();
                city.setCityName(rset.getString("Name"));
                city.setCountryName(rset.getString("Country"));
                city.setCityDistrict(rset.getString("District"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cityList;
    }

    /**
     * Generates a list of all the cities in a district organised by largest population to smallest
     * @param district
     * @return
     */
    public ArrayList<City> generateCityPopulationInDistrictLargestToSmallest(String district){
        ArrayList<City> cityList = new ArrayList<City>();
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name, country.Name AS Country, city.District, city.Population from world.city inner join country on city.CountryCode = country.Code where city.District = '"+district+"' order by city.Population desc";
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()) {
                City city = new City();
                city.setCityName(rset.getString("Name"));
                city.setCountryName(rset.getString("Country"));
                city.setCityDistrict(rset.getString("District"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cityList;
    }

    /**
     * Generates a list of all the capital cities in the world organised by largest population to smallest
     * @return
     */
    public ArrayList<City> generateCapitalPopulationInWorldLargestToSmallest(){
        ArrayList<City> cityList = new ArrayList<City>();
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name AS 'CapitalCity', country.Name AS 'Country', city.Population from city inner join country on city.Id = country.capital order by city.Population desc";
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()) {
                City city = new City();
                city.setCityName(rset.getString("CapitalCity"));
                city.setCountryName(rset.getString("Country"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cityList;
    }

    /**
     * Generates a list of all the capital cities in a region organised by largest population to smallest
     * @param region
     * @return
     */
    public ArrayList<City> generateCapitalPopulationInRegionLargestToSmallest(String region){
        ArrayList<City> cityList = new ArrayList<City>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select city.Name AS 'CapitalCity', country.Name AS 'Country', city.Population from city inner join country on city.Id = country.capital where Region = '"+region+"' order by city.Population desc";
            ResultSet rset = stmt.executeQuery(strSelect);
            while (rset.next()) {
                City city = new City();
                city.setCityName(rset.getString("CapitalCity"));
                city.setCountryName(rset.getString("Country"));
                city.setCityPopulation(rset.getLong("Population"));
                cityList.add(city);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return cityList;
    }

    /**
     * Print out the contents of cityList for Capital Cities to console
     * @param cityList
     */
    public void printCapitalList(ArrayList<City> cityList){
        if((cityList == null)){
            System.out.println("Empty Set");
            return;
        }
        System.out.println(String.format("%-25s %-25s %-25s", "City", "Country", "Population"));
        for(City city : cityList){
            if(city == null)
                continue;
            String cityString =
                    String.format("%-25s %-25s %-25s",
                            city.getCityName(), city.getCountryName(), city.getCityPopulation());
            System.out.println(cityString);
        }
    }

    /**
     * Generates the population of a city
     */
    public long getCityPopulation(String city) {
        long population = 0;
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "select Population from world.city where Name = '" + city + "'";
            ResultSet rset = stmt.executeQuery(strSelect);
            if (rset.next()) {
                population = rset.getLong("Population");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return population;
    }

    /**
     *******************************************************************************************************************
     ********************************************** END OF METHODS FOR CITY ********************************************
     *******************************************************************************************************************
     */


    /**
     *******************************************************************************************************************
     ********************************************** METHODS FOR COUNTRY ************************************************
     *******************************************************************************************************************
     */

    /**
     * Generates the population of a country
     */
    public long generateCountryPopulation(String country) {
        long population = 0;
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "select Population from world.country where Name = '" + country + "'";
            ResultSet rset = stmt.executeQuery(strSelect);
            if (rset.next()) {
                population = rset.getLong("Population");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return population;
    }

    /**
     Generates a full report of a country where code is specified, in the following format
     Country Code: {countryCode}
     Country Name: {countryName}
     Country Continent: {countryContinent}
     Country Region: {countryRegion}
     Country Population: {countryPopulation}
     Country Capital: {countryCapital}
     */
    public void generateCountryReport(Country country){
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "select country.Code AS 'Code', country.Name AS 'Name', country.Continent AS 'Continent', country.Region AS 'Region', country.Population AS 'Population', city.Name AS 'Capital' from country inner join city on city.Id = country.Capital where Code = '" + country.getCountryCode() + "'";
            ResultSet rset = stmt.executeQuery(strSelect);
            if(rset.next()){
                country.setCountryCode(rset.getString("Code"));
                country.setCountryName(rset.getString("Name"));
                country.setCountryContinent(rset.getString("Continent"));
                country.setCountryRegion(rset.getString("Region"));
                country.setCountryPopulation(rset.getLong("Population"));
                country.setCountryCapital(rset.getString("Capital"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Country Code: " + country.getCountryCode());
        System.out.println("Country Name: " + country.getCountryName());
        System.out.println("Country Continent: " + country.getCountryContinent());
        System.out.println("Country Region: " + country.getCountryRegion());
        System.out.println("Country Population: " + country.getCountryPopulation());
        System.out.println("Country Capital: " + country.getCountryCapital());
    }

    /**
     * Generates a list of all countries (ordered from population largest - smallest)
     */
    public ArrayList<Country> generateCountryLargestToSmallest(){
        ArrayList<Country> countryList = new ArrayList<Country>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select country.Code AS 'Code', country.Name AS 'Name', country.Continent AS 'Continent', country.Region AS 'Region', country.Population AS 'Population', city.Name AS 'Capital' from country inner join city on city.Id = country.Capital order by country.Population desc";
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()){
                Country ctry = new Country();
                ctry.setCountryCode(rset.getString("Code"));
                ctry.setCountryName(rset.getString("Name"));
                ctry.setCountryContinent(rset.getString("Continent"));
                ctry.setCountryRegion(rset.getString("Region"));
                ctry.setCountryPopulation(rset.getLong("Population"));
                ctry.setCountryCapital(rset.getString("Capital"));
                countryList.add(ctry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return countryList;
    }

    /**
     * Generates a list of the top N populated countries (where N is specified by the user)
     * @param number
     */
    public ArrayList<Country> generateCountryTopN(int number){
        ArrayList<Country> countryList = new ArrayList<Country>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select country.Code AS 'Code', country.Name AS 'Name', country.Continent AS 'Continent', country.Region AS 'Region', country.Population AS 'Population', city.Name AS 'Capital' from country inner join city on city.Id = country.Capital order by Population desc limit " + number;
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()){
                Country ctry = new Country();
                ctry.setCountryCode(rset.getString("Code"));
                ctry.setCountryName(rset.getString("Name"));
                ctry.setCountryContinent(rset.getString("Continent"));
                ctry.setCountryRegion(rset.getString("Region"));
                ctry.setCountryPopulation(rset.getLong("Population"));
                ctry.setCountryCapital(rset.getString("Capital"));
                countryList.add(ctry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return countryList;
    }

    /**
     * Prints the ArrayList containing information regarding Country to console
     * @param countryList
     */
    public void printCountryList(ArrayList<Country> countryList){
        if((countryList == null) || (countryList.isEmpty())){
            System.out.println("Empty");
            return;
        }
        System.out.println(String.format("%-10s %-15s %-20s", "Code", "Name", "Population"));
        for(Country ctry: countryList){
            if(ctry == null)
                continue;
            String cityString =
                    String.format("%-10s %-15s %-20s",
                            ctry.getCountryCode(), ctry.getCountryName(), ctry.getCountryPopulation());
            System.out.println(cityString);
        }
    }

    /**
     * Generates a list of countries in a continent organised by largest population to smallest
     * @param continent
     * @return
     */
    public ArrayList<Country> generateCountryPopulationInContinentLargestToSmallest(String continent){
        ArrayList<Country> countryList = new ArrayList<Country>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select country.Code AS 'Code', country.Name AS 'Name', country.Continent AS 'Continent', country.Region AS 'Region', country.Population AS 'Population', city.Name AS 'Capital' from country inner join city on city.Id = country.Capital where Continent = '"+continent+"' order by Population desc";
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()){
                Country ctry = new Country();
                ctry.setCountryCode(rset.getString("Code"));
                ctry.setCountryName(rset.getString("Name"));
                ctry.setCountryContinent(rset.getString("Continent"));
                ctry.setCountryRegion(rset.getString("Region"));
                ctry.setCountryPopulation(rset.getLong("Population"));
                ctry.setCountryCapital(rset.getString("Capital"));
                countryList.add(ctry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return countryList;
    }

    /**
     * Generates a list of countries in a region organised by largest population to smallest
     * @param region
     * @return
     */
    public ArrayList<Country> generateCountryPopulationInRegionLargestToSmallest(String region){
        ArrayList<Country> countryList = new ArrayList<Country>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select country.Code AS 'Code', country.Name AS 'Name', country.Continent AS 'Continent', country.Region AS 'Region', country.Population AS 'Population', city.Name AS 'Capital' from country inner join city on city.Id = country.Capital where Region = '"+region+"' order by Population desc";
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()){
                Country ctry = new Country();
                ctry.setCountryCode(rset.getString("Code"));
                ctry.setCountryName(rset.getString("Name"));
                ctry.setCountryContinent(rset.getString("Continent"));
                ctry.setCountryRegion(rset.getString("Region"));
                ctry.setCountryPopulation(rset.getLong("Population"));
                ctry.setCountryCapital(rset.getString("Capital"));
                countryList.add(ctry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return countryList;
    }

    /**
     * Generates a list of top N populated countries in a continent where N is provided by the user
     * @param continent
     * @param number
     * @return
     */
    public ArrayList<Country> generateTopNCountryInContinent(String continent, int number){
        ArrayList<Country> countryList = new ArrayList<Country>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select country.Code AS 'Code', country.Name AS 'Name', country.Continent AS 'Continent', country.Region AS 'Region', country.Population AS 'Population', city.Name AS 'Capital' from country inner join city on city.Id = country.Capital where Continent = '"+continent+"' order by Population desc limit " + number;
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()){
                Country ctry = new Country();
                ctry.setCountryCode(rset.getString("Code"));
                ctry.setCountryName(rset.getString("Name"));
                ctry.setCountryContinent(rset.getString("Continent"));
                ctry.setCountryRegion(rset.getString("Region"));
                ctry.setCountryPopulation(rset.getLong("Population"));
                ctry.setCountryCapital(rset.getString("Capital"));
                countryList.add(ctry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return countryList;
    }

    /**
     * Generates a list of top N populated countries in a region where N is provided by the user
     * @param region
     * @param number
     * @return
     */
    public ArrayList<Country> generateTopNCountryInRegion(String region, int number){
        ArrayList<Country> countryList = new ArrayList<Country>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select country.Code AS 'Code', country.Name AS 'Name', country.Continent AS 'Continent', country.Region AS 'Region', country.Population AS 'Population', city.Name AS 'Capital' from country inner join city on city.Id = country.Capital where Region = '"+region+"' order by Population desc limit " + number;
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()){
                Country ctry = new Country();
                ctry.setCountryCode(rset.getString("Code"));
                ctry.setCountryName(rset.getString("Name"));
                ctry.setCountryContinent(rset.getString("Continent"));
                ctry.setCountryRegion(rset.getString("Region"));
                ctry.setCountryPopulation(rset.getLong("Population"));
                ctry.setCountryCapital(rset.getString("Capital"));
                countryList.add(ctry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return countryList;
    }

    /**
     * Generates report on the population of people, people living in cities and people not living in cities in a specified country code
     * @param countryCode
     * @return
     */
    public ArrayList<InOrOutCity> generateLivingInCityStatsInCountry(String countryCode){
        ArrayList<InOrOutCity> statsList = new ArrayList<InOrOutCity>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "SELECT co.name AS 'Country', co.population AS 'Population', SUM(ci.Population) AS 'InCitiesPopulation', (SUM(ci.Population) * 100 / co.Population) AS 'InCitiesPercentage', (co.Population - SUM(ci.Population)) AS 'OutCitiesPopulation',((co.Population - SUM(ci.Population)) * 100 / co.Population) AS 'OutCitiesPercentage' FROM country co, city ci WHERE co.code = '"+countryCode+"' AND co.Code = ci.CountryCode LIMIT 1";
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()){
                InOrOutCity iooc = new InOrOutCity();
                iooc.setPlace(rset.getString("Country"));
                iooc.setPopulation(rset.getLong("Population"));
                iooc.setInCityPopulation(rset.getLong("InCitiesPopulation"));
                iooc.setInCityPercentage(rset.getFloat("InCitiesPercentage"));
                iooc.setOutCityPopulation(rset.getLong("OutCitiesPopulation"));
                iooc.setOutCityPercentage(rset.getFloat("OutCitiesPercentage"));
                statsList.add(iooc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return statsList;
    }

    /**
     *******************************************************************************************************************
     ********************************************** END OF METHODS FOR COUNTRY *****************************************
     *******************************************************************************************************************
     */


    /**
     *******************************************************************************************************************
     ********************************************** METHODS FOR CONTINENT **********************************************
     *******************************************************************************************************************
     */

    /**
     * Returns the population of a specified continent
     * @param continent
     * @return continentPopulation
     */
    public long continentPopulation(Continent continent){
        long continentPopulation = 0;
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "select Continent, sum(Population) as Population from country where Continent = '"+continent.getContinentName()+"'";
            ResultSet rset = stmt.executeQuery(strSelect);
            if(rset.next()){
                continentPopulation = rset.getLong("Population");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return continentPopulation;
    }

    /**
     * Generates report on the population of people, people living in cities and people not living in cities in each continent
     * @param continent
     * @return
     */
    public ArrayList<InOrOutCity> generateLivingInCityStatsInContinent(String continent){
        ArrayList<InOrOutCity> statsList = new ArrayList<InOrOutCity>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "SELECT co.Continent, continentPop.Population, cityPop.Population AS 'InCitiesPopulation', (cityPop.Population * 100 / continentPop.Population) AS 'InCitiesPercentage', (continentPop.Population - cityPop.Population) AS 'OutCitiesPopulation', ((continentPop.Population - cityPop.Population) * 100 / continentPop.Population) AS 'OutCitiesPercentage' FROM country co, city ci, (SELECT SUM(ci.population) AS Population FROM country co, city ci WHERE co.continent = '"+continent+"' AND co.Code = ci.CountryCode) AS cityPop, (SELECT SUM(population) AS Population FROM country WHERE continent = '"+continent+"') AS continentPop WHERE co.Continent = '"+continent+"' AND co.Code = ci.CountryCode LIMIT 1";
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()){
                InOrOutCity iooc = new InOrOutCity();
                iooc.setPlace(rset.getString("Continent"));
                iooc.setPopulation(rset.getLong("Population"));
                iooc.setInCityPopulation(rset.getLong("InCitiesPopulation"));
                iooc.setInCityPercentage(rset.getFloat("InCitiesPercentage"));
                iooc.setOutCityPopulation(rset.getLong("OutCitiesPopulation"));
                iooc.setOutCityPercentage(rset.getFloat("OutCitiesPercentage"));
                statsList.add(iooc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return statsList;
    }

    /**
     *******************************************************************************************************************
     ********************************************** END OF METHODS FOR CONTINENT ***************************************
     *******************************************************************************************************************
     */


    /**
     *******************************************************************************************************************
     ********************************************** METHODS FOR DISTRICT ***********************************************
     *******************************************************************************************************************
     */

    /**
     * Returns the population of a specified district
     * @param district
     * @return districtPopulation
     */
    public long districtPopulation(District district){
        long districtPopulation = 0;
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "select District, sum(Population) as Population from city where District = '"+district.getDistrictName()+"'";
            ResultSet rset = stmt.executeQuery(strSelect);
            if(rset.next()){
                districtPopulation = rset.getLong("Population");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return districtPopulation;
    }

    /**
     *******************************************************************************************************************
     ********************************************** END OF METHODS FOR DISTRICT ****************************************
     *******************************************************************************************************************
     */


    /**
     *******************************************************************************************************************
     ********************************************** METHODS FOR REGION *************************************************
     *******************************************************************************************************************
     */

    /**
     * Returns the population of a specified region
     * @param region
     * @return regionPopulation
     */
    public long regionPopulation(Region region){
        long regionPopulation = 0;
        try {
            Statement stmt = con.createStatement();
            String strSelect =
                    "select Region, sum(Population) as Population from country where Region = '"+region.getRegionName()+"'";
            ResultSet rset = stmt.executeQuery(strSelect);
            if(rset.next()){
                regionPopulation = rset.getLong("Population");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return regionPopulation;
    }

    /**
     * Generates report on the population of people, people living in cities and people not living in cities in a specified region
     * @param region
     * @return
     */
    public ArrayList<InOrOutCity> generateLivingInCityStatsInRegion(String region){
        ArrayList<InOrOutCity> statsList = new ArrayList<InOrOutCity>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "SELECT co.Region, regionPop.Population, cityPop.Population AS 'InCitiesPopulation', (cityPop.Population * 100 / regionPop.Population) AS 'InCitiesPercentage', (regionPop.Population - cityPop.Population) AS 'OutCitiesPopulation', ((regionPop.Population - cityPop.Population) * 100 / regionPop.Population) AS 'OutCitiesPercentage' FROM country co, city ci, (SELECT SUM(ci.population) AS Population FROM country co, city ci WHERE co.region = '"+region+"' AND co.Code = ci.CountryCode) AS cityPop, (SELECT SUM(population) AS Population FROM country WHERE region = '"+region+"' ) AS regionPop WHERE co.Region = '"+region+"' AND co.Code = ci.CountryCode LIMIT 1";
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()){
                InOrOutCity iooc = new InOrOutCity();
                iooc.setPlace(rset.getString("Region"));
                iooc.setPopulation(rset.getLong("Population"));
                iooc.setInCityPopulation(rset.getLong("InCitiesPopulation"));
                iooc.setInCityPercentage(rset.getFloat("InCitiesPercentage"));
                iooc.setOutCityPopulation(rset.getLong("OutCitiesPopulation"));
                iooc.setOutCityPercentage(rset.getFloat("OutCitiesPercentage"));
                statsList.add(iooc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return statsList;
    }

    /**
     *******************************************************************************************************************
     ********************************************** END OF METHODS FOR REGION ******************************************
     *******************************************************************************************************************
     */


    /**
     *******************************************************************************************************************
     ********************************************** METHODS FOR WORLD **************************************************
     *******************************************************************************************************************
     */

    /**
     * Returns the population of the world
     * @return worldPopulation
     */
    public long worldPopulation(){
        long worldPopulation = 0;
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "select sum(Population) from(select population from world.city union all select population from world.country) as population";
            ResultSet rset = stmt.executeQuery(strSelect);
            if(rset.next()){
                worldPopulation = rset.getLong("sum(Population)");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return worldPopulation;
    }

    /**
     *******************************************************************************************************************
     ********************************************** END OF METHODS FOR WORLD *******************************************
     *******************************************************************************************************************
     */

    /**
     *******************************************************************************************************************
     ********************************************** METHODS FOR LANGUAGE ***********************************************
     *******************************************************************************************************************
     */

    public ArrayList<Language> generateLanguageReport(){
        ArrayList<Language> languageList = new ArrayList<Language>();
        try{
            Statement stmt = con.createStatement();
            String strSelect =
                    "SELECT cl.Language, ROUND(SUM((c.Population * cl.Percentage) / 100)) AS 'Population', (((ROUND(SUM((c.Population * cl.Percentage) / 100))) * 100) / (SELECT SUM(country.Population) FROM country)) AS 'TotalPercentage'FROM countrylanguage cl, country c WHERE (cl.Language = 'Chinese' OR cl.Language = 'English' OR cl.Language = 'Hindi' OR cl.Language = 'Spanish' OR cl.Language = 'Arabic') AND cl.CountryCode = c.Code GROUP BY cl.Language ORDER BY Population DESC";
            ResultSet rset = stmt.executeQuery(strSelect);
            while(rset.next()){
                Language lng = new Language();
                lng.setLanguage(rset.getString("Language"));
                lng.setPopulation(rset.getLong("Population"));
                lng.setPercentage(rset.getFloat("TotalPercentage"));
                languageList.add(lng);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return languageList;
    }

    /**
     *******************************************************************************************************************
     ********************************************** END OF METHODS FOR LANGUAGR ****************************************
     *******************************************************************************************************************
     */

    /**
     *******************************************************************************************************************
     ********************************************** METHODS FOR MISC ***************************************************
     *******************************************************************************************************************
     */

    /**
     * Prints the ArrayList containing information regarding InOrOutCity to console
     * @param ioocList
     */
    public void printIOOCList(ArrayList<InOrOutCity> ioocList){
        if((ioocList == null) || (ioocList.isEmpty())){
            System.out.println("Empty");
            return;
        }
        System.out.println(String.format("%-10s %-15s %-20s %-20s %-20s %-20s", "Place", "Population", "InCitiesPop", "InCities(%)", "OutCitiesPop", "OutCities(%)"));
        for(InOrOutCity iooc: ioocList){
            if(iooc == null)
                continue;
            String str =
                    String.format("%-10s %-15s %-20s %-20s %-20s %-20s",
                            iooc.getPlace(), iooc.getPopulation(), iooc.getInCityPopulation(), iooc.getInCityPercentage(), iooc.getOutCityPopulation(), iooc.getOutCityPercentage());
            System.out.println(str);
        }
    }

    /**
     * Prints the ArrayList containing information regarding Language to console
     * @param languageList
     */
    public void printLanguageList(ArrayList<Language> languageList){
        if((languageList == null) || (languageList.isEmpty())){
            System.out.println("Empty");
            return;
        }
        System.out.println(String.format("%-15s %-15s %-20s", "Language", "Population", "Total(%)"));
        for(Language lng: languageList){
            if(lng == null)
                continue;
            String str =
                    String.format("%-15s %-15s %-20s",
                            lng.getLanguage(), lng.getPopulation(), lng.getPercentage());
            System.out.println(str);
        }
    }
}
