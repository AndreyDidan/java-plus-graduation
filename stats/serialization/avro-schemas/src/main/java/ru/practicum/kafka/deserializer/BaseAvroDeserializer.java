package ru.practicum.kafka.deserializer;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class BaseAvroDeserializer <T extends SpecificRecordBase> implements Deserializer<T> {
    private final Schema schema;
    private final DatumReader<T> reader;
    private final DecoderFactory decoderFactory;

    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.schema = schema;
        this.reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            if (data == null || data.length == 0) {
                log.warn("Десериализация: пустой payload из топика {}", topic);
                return null;
            }

            // Логируем длину и первые несколько байтов для диагностики
            log.debug("Десериализация: длина данных = {}, первые байты = {}",
                    data.length, Arrays.toString(Arrays.copyOf(data, Math.min(data.length, 10))));

            Decoder decoder = decoderFactory.binaryDecoder(data, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            log.error("Ошибка при десериализации Avro для топика {}: {}", topic, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Ошибка при десериализации Avro для топика {}: {}", topic, e.getMessage(), e);
            return null;
        }
    }
}