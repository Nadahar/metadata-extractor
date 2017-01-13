/*
 * Copyright 2002-2016 Drew Noakes
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://drewnoakes.com/code/exif/
 *    https://github.com/drewnoakes/metadata-extractor
 */
package com.drew.imaging.jpeg;

import com.drew.lang.StreamReader;
import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.Metadata;
import com.drew.metadata.adobe.AdobeJpegReader;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.file.FileMetadataReader;
import com.drew.metadata.filter.MetadataFilter;
import com.drew.metadata.icc.IccReader;
import com.drew.metadata.iptc.IptcReader;
import com.drew.metadata.jfif.JfifReader;
import com.drew.metadata.jfxx.JfxxReader;
import com.drew.metadata.jpeg.JpegCommentReader;
import com.drew.metadata.jpeg.JpegReader;
import com.drew.metadata.photoshop.DuckyReader;
import com.drew.metadata.photoshop.PhotoshopReader;
import com.drew.metadata.xmp.XmpReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Obtains all available metadata from JPEG formatted files.
 *
 * @author Drew Noakes https://drewnoakes.com
 */
public class JpegMetadataReader
{
    public static final Iterable<JpegSegmentMetadataReader> ALL_READERS = Arrays.asList(
            new JpegReader(),
            new JpegCommentReader(),
            new JfifReader(),
            new JfxxReader(),
            new ExifReader(),
            new XmpReader(),
            new IccReader(),
            new PhotoshopReader(),
            new DuckyReader(),
            new IptcReader(),
            new AdobeJpegReader()
    );

    @NotNull
    public static Metadata readMetadata(@NotNull InputStream inputStream, @Nullable Iterable<JpegSegmentMetadataReader> readers) throws JpegProcessingException, IOException
    {
        return readMetadata(inputStream, readers, null);
    }

    @NotNull
    public static Metadata readMetadata(@NotNull InputStream inputStream, @Nullable Iterable<JpegSegmentMetadataReader> readers, @Nullable final MetadataFilter filter) throws JpegProcessingException, IOException
    {
        Metadata metadata = new Metadata();
        process(metadata, inputStream, readers, filter);
        return metadata;
    }

    @NotNull
    public static Metadata readMetadata(@NotNull InputStream inputStream) throws JpegProcessingException, IOException
    {
        return readMetadata(inputStream, null, null);
    }

    @NotNull
    public static Metadata readMetadata(@NotNull InputStream inputStream, @Nullable final MetadataFilter filter) throws JpegProcessingException, IOException
    {
        return readMetadata(inputStream, null, filter);
    }

    @NotNull
    public static Metadata readMetadata(@NotNull File file, @Nullable Iterable<JpegSegmentMetadataReader> readers) throws JpegProcessingException, IOException
    {
        return readMetadata(file, readers, null);
    }

    @NotNull
    public static Metadata readMetadata(@NotNull File file, @Nullable Iterable<JpegSegmentMetadataReader> readers, @Nullable final MetadataFilter filter) throws JpegProcessingException, IOException
    {
        InputStream inputStream = new FileInputStream(file);
        Metadata metadata;
        try {
            metadata = readMetadata(inputStream, readers, filter);
        } finally {
            inputStream.close();
        }
        new FileMetadataReader().read(file, metadata, filter);
        return metadata;
    }

    @NotNull
    public static Metadata readMetadata(@NotNull File file) throws JpegProcessingException, IOException
    {
        return readMetadata(file, null, null);
    }

    @NotNull
    public static Metadata readMetadata(@NotNull File file, @Nullable final MetadataFilter filter) throws JpegProcessingException, IOException
    {
        return readMetadata(file, null, filter);
    }

    public static void process(@NotNull Metadata metadata, @NotNull InputStream inputStream) throws JpegProcessingException, IOException
    {
        process(metadata, inputStream, null, null);
    }

    public static void process(@NotNull Metadata metadata, @NotNull InputStream inputStream, @Nullable final MetadataFilter filter) throws JpegProcessingException, IOException
    {
        process(metadata, inputStream, null, filter);
    }

    public static void process(@NotNull Metadata metadata, @NotNull InputStream inputStream, @Nullable Iterable<JpegSegmentMetadataReader> readers) throws JpegProcessingException, IOException
    {
        process(metadata, inputStream, readers, null);
    }

    public static void process(@NotNull Metadata metadata, @NotNull InputStream inputStream, @Nullable Iterable<JpegSegmentMetadataReader> readers, @Nullable final MetadataFilter filter) throws JpegProcessingException, IOException
    {
        if (readers == null)
            readers = ALL_READERS;

        Set<JpegSegmentType> segmentTypes = new HashSet<JpegSegmentType>();
        for (JpegSegmentMetadataReader reader : readers) {
            for (JpegSegmentType type : reader.getSegmentTypes()) {
                segmentTypes.add(type);
            }
        }

        JpegSegmentData segmentData = JpegSegmentReader.readSegments(new StreamReader(inputStream), segmentTypes);

        processJpegSegmentData(metadata, readers, segmentData, filter);
    }

    public static void processJpegSegmentData(Metadata metadata, Iterable<JpegSegmentMetadataReader> readers, JpegSegmentData segmentData)
    {
        processJpegSegmentData(metadata, readers, segmentData, null);
    }

    public static void processJpegSegmentData(Metadata metadata, Iterable<JpegSegmentMetadataReader> readers, JpegSegmentData segmentData, @Nullable final MetadataFilter filter)
    {
        // Pass the appropriate byte arrays to each reader.
        for (JpegSegmentMetadataReader reader : readers) {
            for (JpegSegmentType segmentType : reader.getSegmentTypes()) {
                reader.readJpegSegments(segmentData.getSegments(segmentType), metadata, segmentType, filter);
            }
        }
    }

    private JpegMetadataReader() throws Exception
    {
        throw new Exception("Not intended for instantiation");
    }
}
