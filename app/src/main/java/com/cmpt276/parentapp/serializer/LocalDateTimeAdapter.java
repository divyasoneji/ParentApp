package com.cmpt276.parentapp.serializer;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Allows LocalDateTime to be serialized and deserialized with com.google.gson library.
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
	@Override
	public void write(final JsonWriter jsonWriter, final LocalDateTime localDate) throws IOException {
		if (localDate == null) {
			jsonWriter.nullValue();
		}
		else {
			jsonWriter.value(localDate.toString());
		}
	}

	@Override
	public LocalDateTime read(final JsonReader jsonReader) throws IOException {
		if (jsonReader.peek() == JsonToken.NULL) {
			jsonReader.nextNull();
			return null;
		}
		else {
			return LocalDateTime.parse(jsonReader.nextString());
		}
	}

	public static String getTimeFormatted(LocalDateTime time) {
		String pattern = "uuuu-MMM-dd hh:mm a";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		return time.format(formatter);
	}
}
