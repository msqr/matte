/* ===================================================================
 * Test926007.java
 * 
 * Created Mar 21, 2007 2:27:48 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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

package java926007;

import java.io.File;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.test.annotation.IfProfileValue;

import junit.framework.TestCase;

/**
 * Test case for the Java incident 926007.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version 1.0
 */
@IfProfileValue(name = "Test926007", value = "true")
public class Test926007 extends TestCase {

	/**
	 * Test able to transform.
	 * 
	 * <p>
	 * This works in Java 5 as well as Java 6
	 * </p>
	 * 
	 * @throws Exception if an error occurs
	 */
	public void test926007_1() throws Exception {
		TransformerFactory factory = TransformerFactory.newInstance();
		File f = new File(getClass().getResource("logon.xsl").getPath());
		Templates t = factory.newTemplates(new StreamSource(f));
		Transformer transformer = t.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		transformer.transform(new StreamSource(getClass().getResourceAsStream("src.xml")),
				new StreamResult(System.out));
	}

	/**
	 * Test able to transform.
	 * 
	 * <p>
	 * This works in Java 5, but fails in Java 6 with a NullPointerException.
	 * </p>
	 * 
	 * @throws Exception if an error occurs
	 */
	public void test926007_2() throws Exception {
		TransformerFactory factory = TransformerFactory.newInstance();
		File f = new File(getClass().getResource("home.xsl").getPath());
		Templates t = factory.newTemplates(new StreamSource(f));
		Transformer transformer = t.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		transformer.transform(new StreamSource(getClass().getResourceAsStream("src.xml")),
				new StreamResult(System.out));
	}

	/**
	 * Test able to transform.
	 * 
	 * <p>
	 * This works in Java 5, but fails in Java 6 with a NoSuchFieldError.
	 * </p>
	 * 
	 * @throws Exception if an error occurs
	 */
	public void test926007_3() throws Exception {
		TransformerFactory factory = TransformerFactory.newInstance();
		File f = new File(getClass().getResource("upload-media.xsl").getPath());
		Templates t = factory.newTemplates(new StreamSource(f));
		Transformer transformer = t.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		transformer.transform(new StreamSource(getClass().getResourceAsStream("src.xml")),
				new StreamResult(System.out));
	}

}
