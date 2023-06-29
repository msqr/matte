/* ===================================================================
 * ProcessingException.java
 * 
 * Created Oct 1, 2004 8:07:17 AM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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

package magoffin.matt.ma2;

/**
 * Runtime exception thrown to signify a process completed, but some
 * error still occured.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
public class ProcessingException extends RuntimeException {
	
	private static final long serialVersionUID = 5161055217682338432L;

	private Object result;
	
	/**
	 * Construct with nested exception.
	 * @param thr exception
	 */
	public ProcessingException(Throwable thr) {
		this(null,null,thr);
	}

	/**
	 * Construct with a result and nested exception.
	 * @param result result
	 * @param thr exception
	 */
	public ProcessingException(Object result, Throwable thr) {
		this(result,null,thr);
	}

	/**
	 * Constrcut with a result and message.
	 * @param result result
	 * @param msg message
	 */
	public ProcessingException(Object result, String msg) {
		this(result,msg,null);
	}

	/**
	 * Construct with a result, message, and nested exception.
	 * @param result result
	 * @param msg message
	 * @param thr exception
	 */
	public ProcessingException(Object result, String msg, Throwable thr) {
		super(msg,thr);
		this.result = result;
	}

	/**
	 * Get the processing result.
	 * @return result
	 */
	public Object getProcessResult() {
		return result;
	}

}
