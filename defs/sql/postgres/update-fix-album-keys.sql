/* Remove URL-unfriendly characters from album anonymous keys, for Matte 1.7 or earlier */
update album set  anonymouskey = translate(anonymouskey, '/', '')
where anonymouskey is not null
	and position('/' in anonymouskey) > 0
