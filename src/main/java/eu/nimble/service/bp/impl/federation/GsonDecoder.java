package eu.nimble.service.bp.impl.federation;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import feign.Response;
import feign.Util;
import feign.codec.Decoder;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;

import static feign.Util.ensureClosed;

public class GsonDecoder implements Decoder {

    private final Gson gson;

    public GsonDecoder(Iterable<TypeAdapter<?>> adapters) {
        this(GsonFactory.create(adapters));
    }

    public GsonDecoder() {
        this(Collections.<TypeAdapter<?>>emptyList());
    }

    public GsonDecoder(Gson gson) {
        this.gson = gson;
    }

    public Object decode(Response response, Type type) throws IOException {
        if (response.status() == 404)
            return Util.emptyValueOf(type);
        if (response.body() == null)
            return null;
        Reader reader = response.body().asReader();
        try {
            return gson.fromJson(reader, type);
        } catch (JsonIOException e) {
            if (e.getCause() != null && e.getCause() instanceof IOException) {
                throw IOException.class.cast(e.getCause());
            }
            throw e;
        } finally {
            ensureClosed(reader);
        }
    }
}