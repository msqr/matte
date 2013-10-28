/* ===================================================================
 * DataSourceTest.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ===================================================================
 * $Id: DataSourceTest.java,v 1.1 2006/06/03 22:26:19 matt Exp $
 * ===================================================================
 */

package magoffin.matt.ma.test;

import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class DataSourceTest {

  String foo = "Not Connected";
  int bar = -1;
    
public void init() {
	try{
		Context ctx = new InitialContext();
		if(ctx == null ) throw new Exception("Boom - No Context");
	
		Context envCtx = 
			(Context)ctx.lookup("java:comp/env");
		DataSource ds = 
            (DataSource)envCtx.lookup("jdbc/test");


		if (ds != null) {
			System.out.println("Got DataSource object: "
				+ds.getClass().getName());
			Connection conn = ds.getConnection();
              
        if(conn != null)  {
            foo = "Got Connection "+conn.toString();
            Statement stmt = conn.createStatement();
            ResultSet rst = 
                stmt.executeQuery(
                  "select uid,data_value from test");
            if(rst.next()) {
               foo=rst.getString(2);
               bar=rst.getInt(1);
            }
            conn.close();
        }
      }
    }catch(Exception e) {
      e.printStackTrace();
    }
 }

 public String getFoo() { return foo; }
 public int getBar() { return bar;}
}


