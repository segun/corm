/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.idempotent.corm;

import com.codename1.db.Database;
import java.io.IOException;

/**
 *
 * @author aardvocate
 */
public class DBConnector {
    Database db;
    String dbName;
    
    /**
     * 
     * @param dbName The name of the database
     * @param silent If true, exception is not thrown if error occurred while opening database.
     * @return true or false depending on if the connection succeeded or failed.
     * @throws IOException 
     */
    public boolean connect(String dbName, boolean silent) throws IOException {
        try {
            db = Database.openOrCreate(dbName);
            this.dbName = dbName;
            System.err.println(Database.getDatabasePath(dbName));
            return true;
        } catch (IOException ex) {
            if(silent) {
                return false;
            } else {
                throw ex;
            }
        }
    }
    
    /**
     * 
     * @param dbName The name of the database
     * @param silent If true, exception is not thrown if error occurred while opening database.
     * @return true or false depending on if the delete succeeded or failed.
     * @throws IOException 
     */
    public boolean delete(String dbName, boolean silent) throws IOException {
        try {
            Database.delete(dbName);
            return true;
        } catch(IOException ex) {
            if(silent) {
                return false;
            } else {
                throw ex;
            }            
        }
    }
    
    public boolean close(boolean silent) throws IOException {
        try {
            db.close();
            return true;
        } catch(IOException ex) {
            if(silent) {
                return false;
            } else {
                throw ex;
            }                        
        }
    }
}
