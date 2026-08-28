package util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * TypeAdapter para serializar/deserializar objetos LocalTime do Java 8+ 
 * para strings no formato ISO 8601 (HH:mm:ss), resolvendo o erro de reflexão.
 */
public class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {

    // Formato ISO Padrão para hora (HH:mm:ss)
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

    @Override
    public JsonElement serialize(LocalTime localTime, Type typeOfSrc, JsonSerializationContext context) {
        // Se a hora for nula, serializa como null no JSON
        if (localTime == null) {
            return null;
        }
        // Converte LocalTime para String no formato ISO
        return new JsonPrimitive(localTime.format(formatter));
    }

    @Override
    public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Se for nulo ou string vazia, retorna null
        if (json.isJsonNull() || json.getAsString().isEmpty()) {
            return null;
        }
        try {
            // Converte String JSON de volta para LocalTime
            return LocalTime.parse(json.getAsString(), formatter);
        } catch (Exception e) {
            // Trata erro de parsing
            throw new JsonParseException("Failed to parse LocalTime: " + json.getAsString(), e);
        }
    }
}
