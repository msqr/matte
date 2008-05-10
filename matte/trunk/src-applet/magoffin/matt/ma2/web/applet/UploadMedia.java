/* ===================================================================
 * UploadMedia.java
 * 
 * Created May 10, 2008 1:41:24 PM
 * 
 * Copyright (c) 2008 Matt Magoffin.
 * 
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

package magoffin.matt.ma2.web.applet;

import java.awt.Graphics;

import javax.swing.JApplet;

/**
 * Applet for uploading media.
 * 
 * <p>
 * TODO
 * </p>
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class UploadMedia extends JApplet {

	@Override
	public void paint(Graphics g) {
		g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
		g.drawString("Hello world!", 5, 15);
	}
}
