package fna.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;

import java.sql.CallableStatement;
import org.apache.log4j.Logger;

import fna.parsing.ApplicationUtilities;

public class SetupDerbyDB {

	private static final Logger LOGGER = Logger.getLogger(SetupDerbyDB.class);
	public static Connection conn;
	public static Hashtable<String,Integer> table_list = new Hashtable<String,Integer>();//0 indicates new, 1 indicates old
	public String tablenames[] = {"fishglossaryfixed","uniquespatialterms","brown_wordfreq"};
	public static Hashtable<String,String> table_create = new Hashtable<String,String>();
	
	public SetupDerbyDB()
	{
		getDBConnection();
		collectListOfTables();
		populateCreateScripts();
		populateDefaultTables();
	}
	

	private static void getDBConnection() {
		Boolean create= true;
		try {			
			Class.forName(ApplicationUtilities.getProperty("database.driverPath"));
			conn = DriverManager.getConnection(ApplicationUtilities.getProperty("database.url"));
		} catch (Exception e) {
			LOGGER.error("Couldn't create a DB connection" + e);
			create=false;
			e.printStackTrace();
		}
		if(create==false)
		{
			try {
				conn = DriverManager.getConnection(ApplicationUtilities.getProperty("database.backup.url"));
			} catch (SQLException e) {
				LOGGER.error("Couldn't restore the database" + e);
				e.printStackTrace();
			}
		}
	}
	
	 public void shutdown()
	{
         try
         {
        	 File current_path = new File(".");
        	 String backupdir = current_path.getAbsoluteFile().getParentFile().toString();
        	 System.out.println("backupdir"+backupdir);
        	System.out.println(backupdir+"\\"+ApplicationUtilities.getProperty("Database.backup")+"\\");
        	 java.sql.CallableStatement statement = conn.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
        	 statement.setString(1, backupdir+"\\"+ApplicationUtilities.getProperty("Database.backup")+"\\");
        	 statement.execute();
        	 conn.commit();
        	 conn.close();
        	 System.out.println(ApplicationUtilities.getProperty("database.shutdown"));
        	 DriverManager.getConnection(ApplicationUtilities.getProperty("database.shutdown"));
         }
         catch (SQLException se)
         {
             if (( (se.getErrorCode() == 45000)
                     && ("08006".equals(se.getSQLState()) ))) {
                 System.out.println("Derby shut down normally");
             } else {
                 System.err.println("Derby did not shut down normally "+se.getErrorCode() +"message===="+se.getMessage());
             }
           //  se.printStackTrace();
         }
		
	}
	private void populateCreateScripts() {

		if(table_list.get("fishglossaryfixed")==null)
		{
		String sql = "CREATE TABLE fishglossaryfixed (term varchar(100) NOT NULL, category varchar(200) NOT NULL, last_updated_by varchar(45) NOT NULL, id int NOT NULL, PRIMARY KEY (id))";
		table_create.put("fishglossaryfixed", sql);
		table_list.put("fishglossaryfixed",0);
		}
		if(table_list.get("uniquespatialterms")==null)
		{
		String sql = "CREATE TABLE uniquespatialterms (TERM VARCHAR(200),ID INT)";
		table_create.put("uniquespatialterms", sql);
		table_list.put("uniquespatialterms",0);
		}
		if(table_list.get("brown_wordfreq")==null)
		{
			String sql = "CREATE TABLE brown_wordfreq (WORD VARCHAR(200),FREQ INT)";
			table_create.put("brown_wordfreq",sql);
			table_list.put("brown_wordfreq", 0);
		}
	}

	private void populateDefaultTables() {
		
		Statement stmt =null;
		try {
			
			stmt = conn.createStatement();
			for(String tablename:tablenames)
			{
				//The first time the application runs, all the tables should be populated else not.
				if((table_list.get(tablename)!=null)&&(table_list.get(tablename)==1))
				{
					continue;
				}
				
				String sql = table_create.get(tablename);
				String query;
				stmt.execute(sql);
				File script = new File(ApplicationUtilities.getProperty("database.prepopulate")+tablename+".sql");
				Scanner sc = new Scanner(script);
				int count=0;
				while(sc.hasNext())
				{
					query = sc.nextLine();
					if((query.startsWith("/*")== false)&&(query.startsWith("*/")== false)&&(query.startsWith("--")== false))
					{
						stmt.executeUpdate(query.replaceAll("`", "").replaceAll(";$", ""));
						count++;
					}
				}
				System.out.println(count);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		
	}
	public static void collectListOfTables() {
	//	getDBConnection();
		Statement stmt=null;
		String sql = "SELECT TABLENAME FROM SYS.SYSTABLES WHERE SCHEMAID = (SELECT SCHEMAID FROM SYS.SYSSCHEMAS WHERE SCHEMANAME = '"+ ApplicationUtilities.getProperty("database.username").toUpperCase()+"')";
		ResultSet rset;
		table_list.clear();
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery(sql);
			
			while(rset.next())
			{
				table_list.put(rset.getString(1).toLowerCase(),1);
			}
			rset.close();
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		
	}

	public static void main(String[] args) {

	}

}
