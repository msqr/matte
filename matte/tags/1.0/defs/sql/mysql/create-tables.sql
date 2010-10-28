alter table Album_Item drop foreign key FK444EB323939793BB;
alter table Album_Item drop foreign key FK444EB3239A392D7B;
alter table album drop foreign key FK5897E6FE62B7365;
alter table album drop foreign key FK5897E6FD399B7F;
alter table album drop foreign key FK5897E6F760730B7;
alter table album drop foreign key FK5897E6F919301A4;
alter table collection drop foreign key FK9835AE9E919301A4;
alter table media_item drop foreign key FK739B128E1A6ACDEC;
alter table media_item drop foreign key FK739B128E2E6186E2;
alter table media_item drop foreign key FK739B128EA032A193;
alter table media_item drop foreign key FK739B128EC9E5DEE2;
alter table media_item_rating drop foreign key FK3599406E4866CE76;
alter table media_item_rating drop foreign key FK3599406E4A192480;
alter table metadata drop foreign key FKE52D7B2FFCE2A6CF;
alter table metadata drop foreign key FKE52D7B2FE1CA8FD4;
alter table metadata drop foreign key FKE52D7B2F122A473A;
alter table theme drop foreign key FK69375C9919301A4;
alter table user_comment drop foreign key FK939585EBD9705906;
alter table user_comment drop foreign key FK939585EB55B8CC7A;
alter table user_tag drop foreign key FKF022FD26BC04F398;
alter table user_tag drop foreign key FKF022FD264F77499F;
alter table users drop foreign key FK6A68E08B9F85DC4;
alter table users drop foreign key FK6A68E081A6ACDEC;
alter table users drop foreign key FK6A68E08B18A531B;
drop table if exists Album_Item;
drop table if exists Locale;
drop table if exists album;
drop table if exists collection;
drop table if exists media_item;
drop table if exists media_item_rating;
drop table if exists media_item_type;
drop table if exists metadata;
drop table if exists theme;
drop table if exists time_zone;
drop table if exists user_comment;
drop table if exists user_tag;
drop table if exists users;
create table Album_Item (Album_Item_Hjid bigint not null, MediaItem_Item_Hjchildid bigint not null, Album_Item_Hjindex integer not null, primary key (Album_Item_Hjid, Album_Item_Hjindex));
create table Locale (Hjid bigint not null auto_increment, Hjtype varchar(255) not null, Ordering integer, Code varchar(255), Name_ varchar(255), primary key (Hjid));
create table album (AlbumId bigint not null auto_increment, Hjtype varchar(255) not null, Theme bigint, Owner_ bigint, Comment_ text, AllowFeed bit not null, Poster bigint, AllowAnonymous bit not null, AnonymousKey varchar(96), AllowOriginal bit not null, AlbumDate datetime, SortMode integer, Name_ varchar(255) not null, AllowBrowse bit not null, ModifyDate datetime, CreationDate datetime not null, Album_Album_Hjid bigint, Album_Album_Hjindex integer, primary key (AlbumId));
create table collection (CollectionId bigint not null auto_increment, Hjtype varchar(255) not null, Owner_ bigint, Comment_ text, Path_ varchar(255) not null, Name_ varchar(128) not null, ModifyDate datetime, CreationDate datetime not null, primary key (CollectionId));
create table media_item (ItemId bigint not null auto_increment, Hjtype varchar(255) not null, Hits integer not null, Height integer not null, TzDisplay varchar(40) not null, MediaType bigint, Description text, Tz varchar(40) not null, Width integer not null, Path_ varchar(255) not null, Mime varchar(64) not null, DisplayOrder integer not null, Name_ varchar(128) not null, UseIcon bit not null, ModifyDate datetime, CreationDate datetime not null, FileSize bigint not null, ItemDate datetime, Collection_Item_Hjid bigint, Collection_Item_Hjindex integer, primary key (ItemId));
create table media_item_rating (RatingId bigint not null auto_increment, Hjtype varchar(255) not null, RatingUser bigint, Rating smallint not null, CreationDate datetime not null, MediaItem_UserRating_Hjid bigint, MediaItem_UserRating_Hjindex integer, primary key (RatingId));
create table media_item_type (TypeId bigint not null auto_increment, Hjtype varchar(255) not null, name varchar(64) not null, primary key (TypeId));
create table metadata (Hjid bigint not null auto_increment, Hjtype varchar(255) not null, mkey varchar(64) not null, Value_ varchar(255), MediaItem_Metadata_Hjid bigint, MediaItem_Metadata_Hjindex integer, MediaItemType_Field_Hjid bigint, MediaItemType_Field_Hjindex integer, User__Metadata_Hjid bigint, User__Metadata_Hjindex integer, primary key (Hjid));
create table theme (ThemeId bigint not null auto_increment, Hjtype varchar(255) not null, Owner_ bigint, BasePath varchar(255) not null, Description text, Author varchar(64), Name_ varchar(128) not null unique, ModifyDate datetime, AuthorEmail varchar(128), CreationDate datetime not null, primary key (ThemeId));
create table time_zone (Code varchar(40) not null, Hjtype varchar(255) not null, Ordering integer not null, TimeOffset integer not null, Name_ varchar(40), primary key (Code));
create table user_comment (CommentId bigint not null auto_increment, Hjtype varchar(255) not null, CommentingUser bigint, Comment_ text not null, Approved bit, CreationDate datetime not null, Commenter varchar(255), MediaItem_UserComment_Hjid bigint, MediaItem_UserComment_Hjindex integer, primary key (CommentId));
create table user_tag (TagId bigint not null auto_increment, Hjtype varchar(255) not null, Tag text not null, TaggingUser bigint not null, CreationDate datetime not null, MediaItem_UserTag_Hjid bigint, MediaItem_UserTag_Hjindex integer, primary key (TagId));
create table users (UserId bigint not null auto_increment, Hjtype varchar(255) not null, Quota integer, DiskUsage bigint, Language_ varchar(2) not null, Login varchar(128) not null unique, ThumbnailSetting_Quality varchar(255), ThumbnailSetting_Size_ varchar(255), Password_ varchar(128) not null, Email varchar(255) not null unique, Tz varchar(40) not null, AnonymousKey varchar(64), DefaultTheme bigint, BrowseTheme bigint, ViewSetting_Quality varchar(255), ViewSetting_Size_ varchar(255), AccessLevel integer, Country varchar(3) not null, Name_ varchar(128) not null, ModifyDate datetime, CreationDate datetime not null, primary key (UserId));
alter table Album_Item add index FK444EB323939793BB (Album_Item_Hjid), add constraint FK444EB323939793BB foreign key (Album_Item_Hjid) references album (AlbumId);
alter table Album_Item add index FK444EB3239A392D7B (MediaItem_Item_Hjchildid), add constraint FK444EB3239A392D7B foreign key (MediaItem_Item_Hjchildid) references media_item (ItemId);
alter table album add index FK5897E6FE62B7365 (Theme), add constraint FK5897E6FE62B7365 foreign key (Theme) references theme (ThemeId);
alter table album add index FK5897E6FD399B7F (Album_Album_Hjid), add constraint FK5897E6FD399B7F foreign key (Album_Album_Hjid) references album (AlbumId);
alter table album add index FK5897E6F760730B7 (Poster), add constraint FK5897E6F760730B7 foreign key (Poster) references media_item (ItemId);
alter table album add index FK5897E6F919301A4 (Owner_), add constraint FK5897E6F919301A4 foreign key (Owner_) references users (UserId);
alter table collection add index FK9835AE9E919301A4 (Owner_), add constraint FK9835AE9E919301A4 foreign key (Owner_) references users (UserId);
alter table media_item add index FK739B128E1A6ACDEC (Tz), add constraint FK739B128E1A6ACDEC foreign key (Tz) references time_zone (Code);
alter table media_item add index FK739B128E2E6186E2 (TzDisplay), add constraint FK739B128E2E6186E2 foreign key (TzDisplay) references time_zone (Code);
alter table media_item add index FK739B128EA032A193 (Collection_Item_Hjid), add constraint FK739B128EA032A193 foreign key (Collection_Item_Hjid) references collection (CollectionId);
alter table media_item add index FK739B128EC9E5DEE2 (MediaType), add constraint FK739B128EC9E5DEE2 foreign key (MediaType) references media_item_type (TypeId);
alter table media_item_rating add index FK3599406E4866CE76 (MediaItem_UserRating_Hjid), add constraint FK3599406E4866CE76 foreign key (MediaItem_UserRating_Hjid) references media_item (ItemId);
alter table media_item_rating add index FK3599406E4A192480 (RatingUser), add constraint FK3599406E4A192480 foreign key (RatingUser) references users (UserId);
alter table metadata add index FKE52D7B2FFCE2A6CF (MediaItem_Metadata_Hjid), add constraint FKE52D7B2FFCE2A6CF foreign key (MediaItem_Metadata_Hjid) references media_item (ItemId);
alter table metadata add index FKE52D7B2FE1CA8FD4 (MediaItemType_Field_Hjid), add constraint FKE52D7B2FE1CA8FD4 foreign key (MediaItemType_Field_Hjid) references media_item_type (TypeId);
alter table metadata add index FKE52D7B2F122A473A (User__Metadata_Hjid), add constraint FKE52D7B2F122A473A foreign key (User__Metadata_Hjid) references users (UserId);
alter table theme add index FK69375C9919301A4 (Owner_), add constraint FK69375C9919301A4 foreign key (Owner_) references users (UserId);
alter table user_comment add index FK939585EBD9705906 (CommentingUser), add constraint FK939585EBD9705906 foreign key (CommentingUser) references users (UserId);
alter table user_comment add index FK939585EB55B8CC7A (MediaItem_UserComment_Hjid), add constraint FK939585EB55B8CC7A foreign key (MediaItem_UserComment_Hjid) references media_item (ItemId);
alter table user_tag add index FKF022FD26BC04F398 (TaggingUser), add constraint FKF022FD26BC04F398 foreign key (TaggingUser) references users (UserId);
alter table user_tag add index FKF022FD264F77499F (MediaItem_UserTag_Hjid), add constraint FKF022FD264F77499F foreign key (MediaItem_UserTag_Hjid) references media_item (ItemId);
alter table users add index FK6A68E08B9F85DC4 (DefaultTheme), add constraint FK6A68E08B9F85DC4 foreign key (DefaultTheme) references theme (ThemeId);
alter table users add index FK6A68E081A6ACDEC (Tz), add constraint FK6A68E081A6ACDEC foreign key (Tz) references time_zone (Code);
alter table users add index FK6A68E08B18A531B (BrowseTheme), add constraint FK6A68E08B18A531B foreign key (BrowseTheme) references theme (ThemeId);