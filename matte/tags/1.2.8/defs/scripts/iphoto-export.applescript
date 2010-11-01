-- Crude script to export an album from iPhoto into a directory with a generated metadata.xml file suitable for 
-- zipping into an archive and loading into Matte.
-- 
-- Requires the "XML Tools" scripting addition (version 2),  available from 
-- http://www.latenightsw.com/freeware/XMLTools2/
-- 
-- ===================================================================
-- This program is free software; you can redistribute it and/or 
-- modify it under the terms of the GNU General Public License as 
-- published by the Free Software Foundation; either version 2 of 
-- the License, or (at your option) any later version.
-- 
-- This program is distributed in the hope that it will be useful, 
-- but WITHOUT ANY WARRANTY; without even the implied warranty of 
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
-- General Public License for more details.
-- 
-- You should have received a copy of the GNU General Public License 
-- along with this program; if not, write to the Free Software 
-- Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
-- 02111-1307 USA
-- ===================================================================
-- $Id: iphoto-export.applescript,v 1.1 2007/02/15 04:18:20 matt Exp $
-- ===================================================================


on NewCollectionExport()
	script CollectionExport
		-- list of AlbumExport objects
		property albums : {}
		
		on pushAlbum(theName, theComments, theSortMode)
			set newAlbum to NewAlbumExport(theName, theComments, theSortMode)
			addAlbum(newAlbum)
		end pushAlbum
		
		on addAlbum(theAlbum)
			set end of albums to theAlbum
		end addAlbum
		
		on getAlbumCount()
			return count of albums
		end getAlbumCount
		
		on getAlbumList()
			return albums
		end getAlbumList
		
		on exportXML(theFolder)
			
			set theXML to {class:XML element, XML tag:"collection-import", XML attributes:{xmlns:"http://msqr.us/xsd/matte"}, XML contents:{}}
			
			repeat with i from 1 to number of items in albums
				set oneAlbum to item i of albums
				set albumXML to {class:XML element, XML tag:"album", XML attributes:{|name|:(albumName of oneAlbum), sort:(sortMode of oneAlbum)}, XML contents:{}}
				set end of XML contents of albumXML to {class:XML element, XML tag:"comment", XML contents:albumComments of oneAlbum}
				
				set photoList to getPhotoList() of oneAlbum
				repeat with p from 1 to number of items in photoList
					set onePhoto to item p of photoList
					set archivePath to albumName of oneAlbum & "/" & (name of (info for (POSIX file (photoPath of onePhoto))))
					set photoXML to {class:XML element, XML tag:"item", XML attributes:{|name|:(photoName of onePhoto), |archive-path|:archivePath}, XML contents:{}}
					set end of XML contents of photoXML to {class:XML element, XML tag:"comment", XML contents:photoComments of onePhoto}
					
					set keywordList to getKeywords() of onePhoto
					if number of keywordList > 0 then
						set keywordStr to ""
						repeat with k from 1 to count of keywordList
							if (k > 1) then
								set keywordStr to keywordStr & ", "
							end if
							set keywordStr to keywordStr & (item k of keywordList)
						end repeat
						
						set keywordXML to {class:XML element, XML tag:"keywords", XML contents:{(keywordStr)}}
						set end of XML contents of photoXML to keywordXML
					end if
					
					
					set end of XML contents of albumXML to photoXML
				end repeat
				
				
				set end of XML contents of theXML to albumXML
			end repeat
			
			
			set exportFile to ((theFolder as text) & "metadata.xml")
			set nref to open for access file exportFile with write permission
			set eof nref to 0
			write (generate XML theXML) to file exportFile
			close access nref
		end exportXML
		
	end script
	return CollectionExport
end NewCollectionExport

on NewAlbumExport(theName, theComments, theSortMode)
	script AlbumExport
		property albumName : theName
		property albumComments : theComments
		property sortMode : theSortMode
		
		property photos : {}
		
		on addPhoto(thePhoto)
			set end of photos to thePhoto
		end addPhoto
		
		on getPhotoCount()
			return count of photos
		end getPhotoCount
		
		on getPhotoList()
			return photos
		end getPhotoList
		
	end script
	
	return AlbumExport
end NewAlbumExport

on NewPhotoExport(theName, theComments, thePath, theKeywords)
	script PhotoExport
		property photoName : theName
		property photoComments : theComments
		property photoPath : thePath
		property photoKeywords : theKeywords
		property photoMetadata : {}
		
		on getKeywords()
			return photoKeywords
		end getKeywords
	end script
	
	return PhotoExport
end NewPhotoExport

on run
	set export to NewCollectionExport()
	tell application "iPhoto"
		set theSelection to selection
		
		repeat with i from 1 to count of theSelection
			if class of item i of theSelection is album then
				set oneAlbum to item i of theSelection
				set oneAlbumExport to my NewAlbumExport(name of oneAlbum, "", "date")
				tell export to addAlbum(oneAlbumExport)
				repeat with p from 1 to count of oneAlbum's photos
					set onePhoto to item p of oneAlbum's photos
					set keywordList to {}
					repeat with k from 1 to number of keywords of onePhoto
						set end of keywordList to name of (item k of keywords of onePhoto)
					end repeat
					set onePhotoExport to my NewPhotoExport(title of onePhoto, Â
						comment of onePhoto, (image path of onePhoto), keywordList)
					tell oneAlbumExport to addPhoto(onePhotoExport)
				end repeat
			end if
		end repeat
	end tell
	activate
	set exportFolder to choose folder with prompt "Pick the first storage folder"
	set albumList to getAlbumList() of export
	repeat with i from 1 to count of albumList
		set oneAlbum to item i of albumList
		--display dialog "Exporting album [" & (albumName of oneAlbum) & Â
		--	"]; " & (getPhotoCount() of oneAlbum) & "photos"
		set photoList to getPhotoList() of oneAlbum
		repeat with i from 1 to count of photoList
			set onePhoto to item i of photoList
			set photoFile to POSIX file (photoPath of onePhoto)
			tell application "Finder"
				if not (exists folder (albumName of oneAlbum) of exportFolder) then
					make new folder at exportFolder with properties {name:(albumName of oneAlbum)}
				end if
				duplicate photoFile to folder (albumName of oneAlbum) of exportFolder with replacing
			end tell
		end repeat
	end repeat
	tell export to exportXML(exportFolder)
end run
