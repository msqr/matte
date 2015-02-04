/* ===================================================================
 * UserSerializer.java
 * 
 * Created Feb 4, 2015 4:27:12 PM
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
import magoffin.matt.ma2.biz.BizContext;
import magoffin.matt.ma2.domain.User;
import magoffin.matt.ma2.util.BizContextUtil;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

/**
 * JSON serializer for User objects.
 *
 * @author matt
 * @version 1.0
 */
public class UserSerializer extends StdScalarSerializer<User> {

	/**
	 * Default constructor.
	 */
	public UserSerializer() {
		super(User.class);
	}

	@Override
	public void serialize(User user, JsonGenerator generator, SerializerProvider provider)
			throws IOException, JsonGenerationException {
		generator.writeStartObject();
		generator.writeStringField("name", user.getName());
		generator.writeNumberField("userId", user.getUserId());
		generator.writeObjectField("country", user.getCountry());
		generator.writeObjectField("anonymousKey", user.getAnonymousKey());
		generator.writeObjectField("tz", user.getTz());

		BizContext context = BizContextUtil.getBizContext();
		if ( context != null ) {
			User actor = context.getActingUser();
			if ( actor != null && actor.getUserId() != null
					&& actor.getUserId().equals(user.getUserId()) ) {
				// allow viewing own details
				generator.writeStringField("email", user.getEmail());
				generator.writeStringField("login", user.getLogin());
				generator.writeObjectField("thumbnailSetting", user.getThumbnailSetting());
				generator.writeObjectField("viewSetting", user.getViewSetting());
			}
		}
		generator.writeEndObject();
	}

}
