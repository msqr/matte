/* JavaScript Effect.Center support in Scriptaculous.
 * Add to effects.js. Courtesy of Bhavesh Ramburn.
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
 * $Id: effect.center.js,v 1.1 2007/05/24 05:21:26 matt Exp $
 * ===================================================================
 */

Effect.Center = function(element)
{
    try
    {
        element = $(element);
    }
    catch(e)
    {
        return;
    }

    var my_width  = 0;
    var my_height = 0;

    if ( typeof( window.innerWidth ) == 'number' )
    {

        my_width  = window.innerWidth;
        my_height = window.innerHeight;
    }
    else if ( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) )
    {

        my_width  = document.documentElement.clientWidth;
        my_height = document.documentElement.clientHeight;
    }
    else if ( document.body && ( document.body.clientWidth || document.body.clientHeight ) )
    {

        my_width  = document.body.clientWidth;
        my_height = document.body.clientHeight;
    }

    element.style.position = 'absolute';
    element.style.display  = 'block';
    element.style.zIndex   = 99;

    var scrollY = 0;

    if ( document.documentElement && document.documentElement.scrollTop )
    {
        scrollY = document.documentElement.scrollTop;
    }
    else if ( document.body && document.body.scrollTop )
    {
        scrollY = document.body.scrollTop;
    }
    else if ( window.pageYOffset )
    {
        scrollY = window.pageYOffset;
    }
    else if ( window.scrollY )
    {
        scrollY = window.scrollY;
    }

    var elementDimensions = Element.getDimensions(element);

    var setX = ( my_width  - elementDimensions.width  ) / 2;
    var setY = ( my_height - elementDimensions.height ) / 2 + scrollY;

    setX = ( setX < 0 ) ? 0 : setX;
    setY = ( setY < 0 ) ? 0 : setY;

    element.style.left = setX + "px";
    element.style.top  = setY + "px";

}
