/* SQL for Derby to update from 0.8 to 0.9 release.
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
 * $Id$
 * ===================================================================
 */

ALTER TABLE user_comment ADD COLUMN Commenter varchar(255);
ALTER TABLE user_comment ADD COLUMN Approved smallint NOT NULL WITH DEFAULT 1;
create table Locale (Hjid bigint not null, Hjtype varchar(255) not null, Ordering integer, Code varchar(255), Name_ varchar(255), primary key (Hjid));
