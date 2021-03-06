package com.google.android.exoplayer.extractor.flv;

import android.util.Pair;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.extractor.flv.TagPayloadReader.UnsupportedFormatException;
import com.google.android.exoplayer.util.CodecSpecificDataUtil;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.ParsableBitArray;
import com.google.android.exoplayer.util.ParsableByteArray;
import java.util.Collections;

final class AudioTagPayloadReader extends TagPayloadReader {
    private static final int AAC_PACKET_TYPE_AAC_RAW = 1;
    private static final int AAC_PACKET_TYPE_SEQUENCE_HEADER = 0;
    private static final int AUDIO_FORMAT_AAC = 10;
    private static final int[] AUDIO_SAMPLING_RATE_TABLE = new int[]{5500, 11000, 22000, 44000};
    private boolean hasOutputFormat;
    private boolean hasParsedAudioDataHeader;

    public AudioTagPayloadReader(TrackOutput output) {
        super(output);
    }

    public void seek() {
    }

    protected boolean parseHeader(ParsableByteArray data) throws UnsupportedFormatException {
        if (this.hasParsedAudioDataHeader) {
            data.skipBytes(1);
        } else {
            int header = data.readUnsignedByte();
            int audioFormat = (header >> 4) & 15;
            int sampleRateIndex = (header >> 2) & 3;
            if (sampleRateIndex < 0 || sampleRateIndex >= AUDIO_SAMPLING_RATE_TABLE.length) {
                throw new UnsupportedFormatException("Invalid sample rate index: " + sampleRateIndex);
            } else if (audioFormat != 10) {
                throw new UnsupportedFormatException("Audio format not supported: " + audioFormat);
            } else {
                this.hasParsedAudioDataHeader = true;
            }
        }
        return true;
    }

    protected void parsePayload(ParsableByteArray data, long timeUs) {
        int packetType = data.readUnsignedByte();
        if (packetType == 0 && !this.hasOutputFormat) {
            ParsableBitArray adtsScratch = new ParsableBitArray(new byte[data.bytesLeft()]);
            data.readBytes(adtsScratch.data, 0, data.bytesLeft());
            byte[] audioSpecificConfig = CodecSpecificDataUtil.buildAacAudioSpecificConfig(adtsScratch.readBits(5), adtsScratch.readBits(4), adtsScratch.readBits(4));
            Pair<Integer, Integer> audioParams = CodecSpecificDataUtil.parseAacAudioSpecificConfig(audioSpecificConfig);
            this.output.format(MediaFormat.createAudioFormat(null, MimeTypes.AUDIO_AAC, -1, -1, getDurationUs(), ((Integer) audioParams.second).intValue(), ((Integer) audioParams.first).intValue(), Collections.singletonList(audioSpecificConfig), null));
            this.hasOutputFormat = true;
        } else if (packetType == 1) {
            int bytesToWrite = data.bytesLeft();
            this.output.sampleData(data, bytesToWrite);
            this.output.sampleMetadata(timeUs, 1, bytesToWrite, 0, null);
        }
    }
}
