package util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * TypeAdapter para serializar/deserializar objetos LocalDate do Java 8+ 
 * para strings no formato ISO 8601 (YYYY-MM-DD), evitando erros de reflexão (InaccessibleObjectException).
 */
public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    // Formato ISO Padrão para data (YYYY-MM-DD)
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public JsonElement serialize(LocalDate localDate, Type typeOfSrc, JsonSerializationContext context) {
        // Converte LocalDate para String no formato ISO
        return new JsonPrimitive(localDate.format(formatter));
    }

    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull() || json.getAsString().isEmpty()) {
            return null;
        }
        try {
            // Converte String JSON de volta para LocalDate
            return LocalDate.parse(json.getAsString(), formatter);
        } catch (Exception e) {
            // Trata erro de parsing
            throw new JsonParseException("Failed to parse LocalDate: " + json.getAsString(), e);
        }
    }
}