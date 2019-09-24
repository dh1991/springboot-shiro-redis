package com.dh.shiroredis.redis;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.Assert;

public class ObjectRedisSerializer implements RedisSerializer<Object> {
	private final Converter<Object, byte[]> serializer;
	private final Converter<byte[], Object> deserializer;

	public ObjectRedisSerializer() {
		this(new SerializingConverter(), new DeserializingConverter());
	}
	public ObjectRedisSerializer(ClassLoader classLoader) {
		this(new SerializingConverter(), new DeserializingConverter(classLoader));
	}

	public ObjectRedisSerializer(Converter<Object, byte[]> serializer, Converter<byte[], Object> deserializer) {
		Assert.notNull(serializer,"Serializer must not be null!");
		Assert.notNull(deserializer,"Deserializer must not be null!");
		this.serializer = serializer;
		this.deserializer = deserializer;
	}
	@Override
	public Object deserialize(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}

		try {
			return deserializer.convert(bytes);
		} catch (Exception ex) {
			throw new SerializationException("Cannot deserialize", ex);
		}
	}
	@Override
	public byte[] serialize(Object object) {
		if (object == null) {
			return new byte[0];
		}
		try {
			return serializer.convert(object);
		} catch (Exception ex) {
			throw new SerializationException("Cannot serialize", ex);
		}
	}

}
