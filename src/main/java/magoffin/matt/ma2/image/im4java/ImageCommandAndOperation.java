/* ===================================================================
 * ImageCommandAndOperation.java
 * 
 * Created Oct 28, 2010 12:59:07 PM
 * 
 * Copyright (c) 2010 Matt Magoffin.
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
 */

package magoffin.matt.ma2.image.im4java;

import org.im4java.core.IMOperation;
import org.im4java.core.ImageCommand;

/**
 * An ImageCommand paired with an IMOperation to run.
 *
 * @author matt
 * @version 1.0
 */
public class ImageCommandAndOperation {

	private ImageCommand command;
	private IMOperation op;
	
	/**
	 * Constructor.
	 * 
	 * @param command the command
	 * @param op the operation
	 */
	public ImageCommandAndOperation(ImageCommand command, IMOperation op) {
		this.command = command;
		this.op = op;
	}
	
	/**
	 * @return the command
	 */
	public ImageCommand getCommand() {
		return command;
	}
	/**
	 * @return the op
	 */
	public IMOperation getOp() {
		return op;
	}
	
}
