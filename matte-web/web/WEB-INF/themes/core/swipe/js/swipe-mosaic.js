/*jslint browser: true */
/**
 * @require jQuery 2.1
 */

(function() {
'use strict';

if ( 'matte' in window === false ) {
	window.matte = {};
}

function defaultMediaSizes() {
	var sizes = {};
	sizes.BIGGEST =			{ width: 1600,	height: 1200 }; 
	sizes.BIGGER =			{ width: 1024,	height: 768 }; 
	sizes.BIG =				{ width: 800,	height: 600 }; 
	sizes.NORMAL =			{ width: 640,	height: 480 }; 
	sizes.SMALL =			{ width: 480,	height: 320 }; 
	sizes.TINY =			{ width: 320,	height: 240 }; 
	sizes.THUMB_BIGGER =	{ width: 240,	height: 180 }; 
	sizes.THUMB_BIG =		{ width: 180,	height: 135 }; 
	sizes.THUMB_NORMAL =	{ width: 120,	height: 90 };
	sizes.THUMB_SMALL =		{ width: 64,	height: 48 };
	return sizes;
}

matte.imageMosaic = function(container, imageSizes) {
	var self = {
		version : '1.0.0'
	};
	
	var root = $(container),
		mediaSizes = (imageSizes || defaultMediaSizes()),
		boxes = [],
		imageURLs = [],
		testMergeData,
		coords, // copy of testMergeData updated during rendering
		gridCols, 
		maxLength, 
		numMerge, 
		tileSize,
		eyeCatcherPeriod = 5000, 
		eyeCatcherTimer,
		tileClickHandlerFn;

	function sizeNameForTile(box, width, height) {
		var size, spec, result = 'BIGGEST', resultSpec = mediaSizes.BIGGEST;
		if ( resultSpec === undefined ) {
			result = 'NORMAL';
			resultSpec = defaultMediaSizes().NORMAL;
		}
		for ( size in mediaSizes ) {
			if ( mediaSizes.hasOwnProperty(size) ) {
				spec = mediaSizes[size];
				if ( spec.height >= height && spec.height < resultSpec.height
				 		&& spec.width >= width && spec.width < resultSpec.width ) {
					result = size;
					resultSpec = spec;
				}
			}
		}
		console.log('%s (%dx%d) image size for box %dx%d', result, resultSpec.width, resultSpec.height, width, height);
		return result;
	}
	
	function imageURLForTile(box, width, height, url) {
		var imgSize = sizeNameForTile(box, width, height);
		if ( imgSize ) {
			url = url.replace(/size=\w+/, 'size=' + imgSize);
		}
		return url;
	}

	function nextCoordinate(cols, rows) {
		var result;
		if ( Array.isArray(coords) && coords.length > 0 ) {
			result = coords[0];
			coords.splice(0, 1);
		} else {
			result = [ 
				Math.floor(Math.random() * cols), 
				Math.floor(Math.random() * rows),
				Math.floor(Math.random() * 4) // 0 = up, 1 = right, 2 = down, 3 = left
			];
		}
		return result;
	}
	
	function generateBoxes(numMerge, cols, rows, maxMergedCols, maxMergedRows) {
		var i, j, coord, len, box, dir, nx, ny, obox, numTries = numMerge * numMerge;
		var grid = [], result = [];
		for ( i = 0; i < rows; i += 1 ) {
			grid[i] = [];
			for ( j = 0; j < cols; j += 1 ) {
				grid[i][j] = { x : i, y : j, w : 1, h : 1 };
			}
		}
		MERGE:
		while ( numMerge > 0 && numTries > 0 ) {
			numTries -= 1;
			coord = nextCoordinate(cols, rows);
			box = grid[coord[0]][coord[1]];
			dir = coord[2]; // 0 = up, 1 = right, 2 = down, 3 = left
			if ( dir === 0 ) { // up
				ny = box.y - 1;
				if ( ny < 0 || (maxMergedRows > 0 && box.h >= maxMergedRows) ) {
					continue;
				}
				for ( i = box.x, len = box.x + box.w; i < len; i += 1 ) {
					obox = grid[i][ny];
					if ( obox.w > 1 || obox.h > 1 ) {
						// other box already merged, cowardly abort this merge
						continue MERGE;
					}
				}
				console.log('Merging %d,%d %dx%d up', box.x, box.y, box.w, box.h);
				box.y = ny;
				box.h += 1;
				for ( i = box.x, len = box.x + box.w; i < len; i += 1 ) {
					grid[i][ny] = box;
				}
			} else if ( dir === 1 ) { // right
				nx = box.x + box.w;
				if ( nx >= cols || (maxMergedCols > 0 && box.w >= maxMergedCols) ) {
					continue;
				}
				for ( j = box.y, len = box.y + box.h; j < len; j += 1 ) {
					obox = grid[nx][j];
					if ( obox.w > 1 || obox.h > 1 ) {
						// other box already merged, cowardly abort this merge
						continue MERGE;
					}
				}
				console.log('Merging %d,%d %dx%d ri', box.x, box.y, box.w, box.h);
				box.w += 1;
				for ( j = box.y, len = box.y + box.h; j < len; j += 1 ) {
					grid[nx][j] = box;
				}
			} else if ( dir === 2 ) { // down
				ny = box.y + box.h;
				if ( ny >= rows || (maxMergedRows > 0 && box.h >= maxMergedRows) ) {
					continue;
				}
				for ( i = box.x, len = box.x + box.w; i < len; i += 1 ) {
					obox = grid[i][ny];
					if ( obox.w > 1 || obox.h > 1 ) {
						// other box already merged, cowardly abort this merge
						continue MERGE;
					}
				}
				console.log('Merging %d,%d %dx%d do', box.x, box.y, box.w, box.h);
				box.h += 1;
				for ( i = box.x, len = box.x + box.w; i < len; i += 1 ) {
					grid[i][ny] = box;
				}
			} else { // left
				nx = box.x - 1;
				if ( nx < 0 || (maxMergedCols > 0 && box.w >= maxMergedCols) ) {
					continue;
				}
				for ( j = box.y, len = box.y + box.h; j < len; j += 1 ) {
					obox = grid[nx][j];
					if ( obox.w > 1 || obox.h > 1 ) {
						// other box already merged, cowardly abort this merge
						continue MERGE;
					}
				}
				console.log('Merging %d,%d %dx%d le', box.x, box.y, box.w, box.h);
				box.x = nx;
				box.w += 1;
				for ( j = box.y, len = box.y + box.h; j < len; j += 1 ) {
					grid[nx][j] = box;
				}
			}
			numMerge -= 1;
		}
		
		// extract unique boxes out of the grid
		for ( j = 0; j < rows; j += 1 ) {
			for ( i = 0; i < cols; i += 1 ) {
				box = grid[i][j];
				if ( result.indexOf(box) === -1 ) {
					result.push(box);
				}
			}
		}
		
		return result;
	}
	
	function renderBoxes(callback) {
		var i, len, box, t, l, w, h, stop = false, 
			mosaicHeight = root.height(),
			mosaicWidth = root.width();
		for ( i = 0, len = boxes.length; i < len && !stop; i += 1 ) {
			box = boxes[i];
			t = box.y * tileSize;
			l = box.x * tileSize;
			w = box.w * tileSize;
			h = box.h * tileSize;
			var div = $('<div class="tile"/>').css({
				'top' : String(t) + 'px',
				'left' : String(l) + 'px',
				'width' : String(w) + 'px',
				'height' : String(h) + 'px'
			});
			if ( tileClickHandlerFn ) {
				div.addClass('clickable');
			}
			if ( callback ) {
				stop = callback.call(div, i, box, w, h);
			}
			root.append(div);
			if ( t + h > mosaicHeight ) {
				root.css('height', String(t + h) + 'px');
			}
			if ( l + w > mosaicWidth ) {
				root.css('width', String(l + w) + 'px');
			}
		}
	}
	
	function tileImageLoaded(event) {
		var data = event.data,
			img = $(this),
			tileWidth = data.tileSize * data.box.w,
			tileHeight = data.tileSize * data.box.h,
			imgDimensions = { w : img.width(), h : img.height() };
		console.log('Loaded box %d,%d image %s', data.box.x, data.box.y, this.src);
		
		// aspect-fill scale image to tile
		if ( imgDimensions.h < tileHeight ) {
			img.css('height', '100%');
		} else if ( imgDimensions.w < tileWidth ) {
			img.css('width', '100%');
		}
		
		// re-get dimensions after (possible) resize
		imgDimensions = { w : img.width(), h : img.height() };
		
		// center image
		if ( imgDimensions.w > tileWidth ) {
			img.css('margin-left', String(-Math.floor((imgDimensions.w - tileWidth) / 2)) +'px');
		}
		if ( imgDimensions.h > tileHeight ) {
			img.css('margin-top', String(-Math.floor((imgDimensions.h - tileHeight) / 2)) +'px');
		}
		img.removeClass('new');
		
		// save box details as image data
		img.data(data);
		
		if ( data.callback ) {
			data.callback.call(this);
		}
	}
	
	function nextEyeCatcherDelay() {
		var result = Math.round(eyeCatcherPeriod * 0.8 + (Math.random() * eyeCatcherPeriod * 0.3));
		console.log('Next eye catcher in %dms', result);
		return result;
	}
	
	function eyeCatcher() {
		if ( Array.isArray(boxes) === false || boxes.length < 1 ) {
			return;
		}
		var tiles = root.children('.tile'),
			boxIndex = 0,
			imageIndex = 0,
			box,
			tile,
			flipped,
			oldClip,
			newImage,
			newClip;
		if ( tiles.length < 1 || imageURLs.length < 1 ) {
			return;
		}
		while ( boxIndex === imageIndex ) {
			boxIndex = Math.floor(Math.random() * tiles.length);
			imageIndex = Math.floor(Math.random() * imageURLs.length);
		}
		box = boxes[boxIndex];
		tile = tiles.eq(boxIndex);
		flipped = tile.hasClass('flipped');
		oldClip = tile.find('.clip').eq(flipped ? 1 : 0);
		newClip = tile.find('.clip').eq(flipped ? 0 : 1);

		newImage = $('<img/>').on('load', {index:imageIndex, box:box, tileSize:tileSize, callback:function() {
			tile.toggleClass('flipped');
			if ( eyeCatcherTimer ) {
				eyeCatcherTimer = setTimeout(eyeCatcher, nextEyeCatcherDelay());
			}
		}}, tileImageLoaded);
		newImage.attr('src', imageURLForTile(box, (tileSize * box.w), (tileSize * box.h), imageURLs[imageIndex]));
		if ( newClip.length < 1 ) {
			newClip = $('<div class="clip back"/>');
			tile.append(newClip);
		} else {
			newClip.empty();
		}
		newClip.append(newImage);
	}
	
	function handleImageTap(event) {
		var tile = $(this),
			flipped = tile.hasClass('flipped'),
			img = tile.find('.clip').eq(flipped ? 1 : 0).children('img'),
			data = img.data();
		if ( tileClickHandlerFn ) {
			tileClickHandlerFn.call(self, event, { index : data.index, image : img });
		} else {
			console.log('Tapped on image %d', data.index);
		}
	}
	
	/**
	 * Render the mosaic based on the current size of the container.
	 * 
	 * @return this object
	 * @memberOf matte.imageMosaic
	 */
	self.render = function() {
		root.empty();
		coords = testMergeData; // reset test data
		tileSize = Math.floor((root.width() - 20) / gridCols); // TODO: make right margin propery
		boxes = generateBoxes(numMerge, gridCols, gridCols, maxLength, maxLength);
		renderBoxes(function(index, box, width, height) {
			var img, url, container;
			console.log('Rendering box %d %dx%d', (index + 1), width, height);
			img = $('<img class="new"/>').on('load', {index:index, box:box, tileSize:tileSize}, tileImageLoaded);
			url = imageURLForTile(box, width, height, imageURLs[index]);
			img.attr('src', url);
			container = $('<div class="clip"/>');
			container.append(img).appendTo(this);
			return (index + 1 >= imageURLs.length);
		});
		return self;
	};

	/**
	 * Get or set the image URLs.
	 * 
	 * @param {Array} [value] An array of string image URLs to use for the mosaic tiles.
	 * @return When used as a getter, the current image URLs, otherwise this object.
	 * @memberOf matte.imageMosaic
	 */
	self.images = function(value) {
		if ( !arguments.length ) return imageURLs;
		if ( Array.isArray(value) ) {
			imageURLs = value;
		} else {
			imageURLs = [];
		}
		return self;
	};

	/**
	 * Get or set the grid column count.
	 * 
	 * @param {Number} [value] the grid column count to set.
	 * @return When used as a getter, the current grid column count value, otherwise this object.
	 * @memberOf matte.imageMosaic
	 */
	self.gridColumnCount = function(value) {
		if ( !arguments.length ) return gridCols;
		if ( value > 0 ) {
			gridCols = value;
			maxLength = gridCols / 2;
			numMerge = (gridCols / 2) * Math.ceil(Math.random() * gridCols);
		}
		return self;
	};

	/**
	 * Get or set the maximum tile length, in grid box count.
	 * 
	 * @param {Number} [value] the maximum tile length to set.
	 * @return When used as a getter, the current grid column count value, otherwise this object.
	 * @memberOf matte.imageMosaic
	 */
	self.tileMaximumLength = function(value) {
		if ( !arguments.length ) return maxLength;
		maxLength = value;
		return self;
	};

	/**
	 * Get or set the test merge instructions. The data consists of an array of arrays. Each sub-array
	 * must have 3 numbers: grid X index, grid Y index, and merge direction. The merge direction
	 * must be 0 (up), 1 (right), 2 (down), or 3 (left). The grid origin is the upper-left of the 
	 * container, and extends to the right and down.
	 * 
	 * @param {Array} [value] the test merge instructions to set.
	 * @return When used as a getter, the current test merge instructions, otherwise this object.
	 * @memberOf matte.imageMosaic
	 */
	self.testMergeData = function(value) {
		if ( !arguments.length ) return testMergeData;
		if ( Array.isArray(value) ) {
			testMergeData = value;
			numMerge = testMergeData.length;
		}
		return self;
	};

	/**
	 * Test if the eye catcher timer is running.
	 * 
	 * @return True if the eye catcher is running, false otherwise.
	 * @memberOf matte.imageMosaic
	 */
	self.isEyeCatcherEnabled = function() {
		return (eyeCatcherTimer !== undefined);
	};
	
	/**
	 * Start periodically animating tiles.
	 * 
	 * @return this object
	 * @memberOf matte.imageMosaic
	 */
	self.startEyeCatcher = function() {
		if ( eyeCatcherTimer === undefined ) {
			eyeCatcherTimer = setTimeout(eyeCatcher, nextEyeCatcherDelay());
		}
		return self;
	};
	
	/**
	 * Stop automatically updating the status of the configured control.
	 * 
	 * @return this object
	 * @memberOf sn.util.controlToggler
	 */
	self.stopEyeCatcher = function() {
		if ( eyeCatcherTimer !== undefined ) {
			clearTimeout(eyeCatcherTimer);
			eyeCatcherTimer = undefined;
		}
		return self;
	};

	/**
	 * Get or set the eye catcher approximate period, in milliseconds.
	 * 
	 * @param {Number} [value] The millisecond value to set.
	 * @return When used as a getter, the current eye catcher approximate period, otherwise this object.
	 * @memberOf matte.imageMosaic
	 */
	self.eyeCatcherPeriod = function(value) {
		if ( !arguments.length ) return eyeCatcherPeriod;
		if ( typeof value === 'number' && value > 0 ) {
			eyeCatcherPeriod = value;
		}
		return self;
	};
	
	/**
	 * Get or set a tile click callback function. The function will be passed the event and a 
	 * data object with an <code>index</code> property with the image index and <code>image</code>
	 * property with the &lt;img&gt; element itself. The <code>this</code> value will be set to
	 * this object.
	 * 
	 * @param {Function} [value] The click callback function to set.
	 * @return When used as a getter, the current click callback function, otherwise this object.
	 * @memberOf matte.imageMosaic
	 */
	self.tileClickHandler = function(value) {
		if ( !arguments.length ) return tileClickHandlerFn;
		if ( typeof value === 'function' ) {
			tileClickHandlerFn = value;
		} else {
			tileClickHandlerFn = undefined;
		}
		return self;
	};
	
	Object.defineProperties(self, {
		container				: { value : root },
		imageSizes 				: { value : mediaSizes }
	});
	
	self.gridColumnCount(6); // default column count
	root.on('click', 'div.tile', handleImageTap);
	return self;
};

}());
