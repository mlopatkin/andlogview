package name.mlopatkin.andlogview.config;

import com.google.common.base.CharMatcher;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.io.IOException;

class ColorTypeAdapter extends TypeAdapter<Color> {
    @Override
    public void write(JsonWriter out, @Nullable Color value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        if (value.getAlpha() == 0xFF) {
            // write RGB value
            out.value(String.format(
                    "#%02X%02X%02X", value.getRed(), value.getGreen(), value.getBlue())
            );
        } else {
            // write ARGB value
            out.value(String.format(
                    "#%02X%02X%02X%02X", value.getAlpha(), value.getRed(), value.getGreen(), value.getBlue()
            ));
        }
    }

    @Override
    public @Nullable Color read(JsonReader in) throws IOException {
        JsonToken nextToken = in.peek();
        return switch (nextToken) {
            case NULL -> {
                in.nextNull();
                yield null;
            }
            case BEGIN_OBJECT -> parseObject(in);
            case STRING -> parseColorString(in);
            default -> throw new JsonParseException("Unexpected token for Color: " + nextToken);
        };
    }

    private Color parseColorString(JsonReader in) throws IOException {
        var jsonString = CharMatcher.whitespace().trimFrom(in.nextString());
        if (!jsonString.startsWith("#")) {
            throw parseError(jsonString, "expecting to start with #", null);
        }
        var hasAlpha = switch (jsonString.length()) {
            case 7 -> false; // RGB color '#RRGGBB'
            case 9 -> true;  // ARGB color '#AARRGGBB'
            default -> throw parseError(jsonString, "unexpected color length, must be RRGGBB or AARRGGBB", null);
        };

        try {
            var rgb = Integer.parseUnsignedInt(jsonString.substring(1), 16);
            return new Color(rgb, hasAlpha);
        } catch (NumberFormatException e) {
            throw parseError(jsonString, "can't parse it as hexadecimal string", e);
        }
    }

    private JsonParseException parseError(String jsonString, String message, @Nullable Throwable cause) {
        throw new JsonParseException(String.format("Invalid Color value string `%s`: %s", jsonString, message), cause);
    }

    private Color parseObject(JsonReader in) throws IOException {
        Color result = null;
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if ("value".equals(name)) {
                if (result != null) {
                    throw new IOException("Duplicate 'value' in object");
                }
                result = new Color(in.nextInt());
            } else {
                in.skipValue();
            }
        }
        in.endObject();
        if (result == null) {
            throw new IOException("No 'value' in object");
        }
        return result;
    }
}
