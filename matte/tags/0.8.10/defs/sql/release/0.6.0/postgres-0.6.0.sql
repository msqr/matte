/* SQL for PostgreSQL to update from 0.5 to 0.6 release.
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
 * $Id: postgres-0.6.0.sql,v 1.1 2007/03/31 08:29:11 matt Exp $
 * ===================================================================
 */

begin;
alter table Album_Album drop constraint FK4513485F9C576E97;
alter table Album_Album drop constraint FK4513485FD399B7F;
drop table Album_Album;

alter table album add column Album_Album_Hjid int8;
alter table album add column Album_Album_Hjindex int4;
commit;
