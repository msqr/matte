/* SQL for PostgreSQL to update from 0.4 to 0.5 release.
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

alter table media_item drop column customdate;
alter table media_item drop column iconwidth;
alter table media_item drop column iconheight;
alter table media_item add column ModifyDate timestamp with time zone;
alter table media_item add column ItemDate timestamp with time zone;

create table user_tag (TagId int8 not null, Hjtype varchar(255) not null, Tag varchar(2048) not null, TaggingUser int8 not null, CreationDate timestamp with time zone not null, MediaItem_UserTag_Hjid int8, MediaItem_UserTag_Hjindex int4, primary key (TagId));
alter table user_tag add constraint FKF022FD26BC04F398 foreign key (TaggingUser) references users;
alter table user_tag add constraint FKF022FD264F77499F foreign key (MediaItem_UserTag_Hjid) references media_item;
