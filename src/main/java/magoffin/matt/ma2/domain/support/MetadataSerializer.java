/* ===================================================================
 * MetadataSerializer.java
 * 
 * Created Feb 4, 2015 5:28:17 PM
 * 
 * Copyright (c) 2015 Matt Magoffin.
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

package magoffin.matt.ma2.domain.support;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import magoffin.matt.ma2.domain.Metadata;

/**
 * JSON serializer for {@link Metadata} objects.
 *
 * @author matt
 * @version 1.1
 */
public class MetadataSerializer extends StdScalarSerializer<Metadata> {

	private static final long serialVersionUID = 2007074971687971289L;

	/**
	 * Default constructor.
	 */
	public MetadataSerializer() {
		super(Metadata.class);
	}

	@Override
	public void serialize(Metadata meta, JsonGenerator generator, SerializerProvider provider)
			throws IOException, JsonGenerationException {
		generator.writeStartObject();
		generator.writeStringField("key", meta.getKey());
		generator.writeStringField("value", meta.getValue());
		generator.writeEndObject();
	}

}
