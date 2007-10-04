/* SQL for PostgreSQL to update from 0.7 to 0.8 release.
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
 * $Id: postgres-0.8.0.sql,v 1.1 2007/07/15 10:19:02 matt Exp $
 * ===================================================================
 */

begin;
ALTER TABLE album RENAME comment  TO comment_;
ALTER TABLE album RENAME name  TO name_;
ALTER TABLE album RENAME owner  TO owner_;

ALTER TABLE collection RENAME comment  TO comment_;
ALTER TABLE collection RENAME name  TO name_;
ALTER TABLE collection RENAME owner  TO owner_;
ALTER TABLE collection RENAME path  TO path_;

ALTER TABLE media_item RENAME name  TO name_;
ALTER TABLE media_item RENAME path  TO path_;

ALTER TABLE metadata RENAME value TO value_;
ALTER TABLE metadata RENAME user_metadata_hjid  TO user__metadata_hjid;
ALTER TABLE metadata RENAME user_metadata_hjindex  TO user__metadata_hjindex;

ALTER TABLE theme RENAME name  TO name_;
ALTER TABLE theme RENAME owner  TO owner_;

ALTER TABLE time_zone RENAME name  TO name_;

ALTER TABLE users RENAME language TO language_;
ALTER TABLE users RENAME name  TO name_;
ALTER TABLE users RENAME password TO password_;
ALTER TABLE users RENAME ThumbnailSetting_Size TO ThumbnailSetting_Size_;
ALTER TABLE users RENAME ViewSetting_Size TO ViewSetting_Size_;

ALTER TABLE user_comment RENAME comment  TO comment_;
commit;
