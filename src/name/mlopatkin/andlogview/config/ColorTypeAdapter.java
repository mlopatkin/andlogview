package name.mlopatkin.andlogview.config;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.awt.Color;
import java.io.IOException;

class ColorTypeAdapter extends TypeAdapter<Color> {
    @Override
    public void write(JsonWriter out, Color value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginObject();
        out.name("value").value(value.getRGB());
        out.endObject();
    }

    @Override
    @Nullable
    public Color read(JsonReader in) throws IOException {
        JsonToken nextToken = in.peek();
        return switch (nextToken) {
            case NULL -> null;
            case BEGIN_OBJECT -> parseObject(in);
            default -> throw new IOException("Unexpected value: " + nextToken);
        };
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
