/*
 * Copyright 2015-2017 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.opencga.storage.core.variant.io.avro;

import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAvro;
import org.opencb.commons.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created on 01/10/15.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class VariantAvroWriter implements DataWriter<Variant> {

    private final String codecName;
    private final Schema schema;
    private final OutputStream outputStream;
    private final DataFileWriter<VariantAvro> writer;
    private final DatumWriter<VariantAvro> datumWriter;
    private int numWrites = 0;

    protected Logger logger = LoggerFactory.getLogger(this.getClass().toString());

    public VariantAvroWriter(Schema schema, String codecName, OutputStream outputStream) {
        this.schema = schema;
        this.outputStream = outputStream;
        if (codecName == null || codecName.isEmpty()) {
            codecName = "null";
        } else {
            codecName = codecName.replace("gzip", "deflate");
        }
        this.codecName = codecName;

        datumWriter = new SpecificDatumWriter<>();
        writer = new DataFileWriter<>(datumWriter);
        writer.setCodec(CodecFactory.fromString(this.codecName));
//        writer.setCodec(CodecFactory.deflateCodec(CodecFactory.DEFAULT_DEFLATE_LEVEL));
    }

    @Override
    public boolean open() {
        try {
            writer.create(schema, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    @Override
    public boolean write(List<Variant> batch) {
        for (Variant variant : batch) {
            if (numWrites++ % 1000 == 0) {
                logger.debug("Written {} elements", numWrites);
            }
            try {
                writer.append(variant.getImpl());
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        logger.debug("Written " + batch.size());
        return true;
    }

    @Override
    public boolean close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
