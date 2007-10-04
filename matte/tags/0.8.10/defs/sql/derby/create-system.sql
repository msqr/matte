/* SQL for PostgreSQL to create Matte system tables.
 * ===================================================================
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
 * $Id: create-system.sql,v 1.1 2007/01/28 01:41:15 matt Exp $
 * ===================================================================
 */

create table settings (
	skey 		varchar(128) not null, 
	svalue 		varchar(255), 
	primary key (skey)
);

CREATE FUNCTION SA.bitwise_and( parm1 INTEGER, param2 INTEGER )
    RETURNS INTEGER
    LANGUAGE JAVA
    PARAMETER STYLE JAVA
    NO SQL
    EXTERNAL NAME 'magoffin.matt.ma2.dao.hbm.DerbyBitwiseAndSQLFunction.bitwiseAnd';
