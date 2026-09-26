package com.retroglow;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RetroGlowSound {

    private static final int SAMPLE_RATE = 44100;

    private static final ExecutorService AUDIO_EXECUTOR =
            Executors.newSingleThreadExecutor();

    private RetroGlowSound() {
    }

    public static void playDetent() {

        AUDIO_EXECUTOR.execute(() -> {

            short[] sound = createDetent();

            play(sound);
        });
    }

    private static short[] createDetent() {

        final double duration = 0.115;

        final int samples =
                (int) (SAMPLE_RATE * duration);

        short[] output =
                new short[samples];

        for (int i = 0; i < samples; i++) {

            double t =
                    i / (double) SAMPLE_RATE;

            double body =
                    Math.sin(
                            2.0
                                    * Math.PI
                                    * 118.0
                                    * t
                    );

            double metal =
                    Math.sin(
                            2.0
                                    * Math.PI
                                    * 840.0
                                    * t
                    );

            double texture =
                    Math.sin(
                            2.0
                                    * Math.PI
                                    * 2310.0
                                    * t
                    );

            double resonance =
                    Math.exp(
                            -t * 16.0
                    );

            double impact =
                    Math.exp(
                            -t * 42.0
                    );

            double signal =
                    body
                            * 0.58
                            * resonance
                            + metal
                            * 0.22
                            * impact
                            + texture
                            * 0.08
                            * impact;

            if (t < 0.012) {

                double transientNoise =
                        Math.random() * 2.0 - 1.0;

                signal +=
                        transientNoise
                                * 0.18
                                * Math.exp(
                                        -t * 180.0
                                );
            }

            signal *=
                    Math.exp(
                            -t * 8.0
                    );

            signal *= 0.52;

            signal =
                    Math.max(
                            -1.0,
                            Math.min(
                                    1.0,
                                    signal
                            )
                    );

            output[i] =
                    (short)
                            (signal * 32767.0);
        }

        return output;
    }

    private static void play(
            short[] samples) {

        AudioTrack track = null;

        try {

            AudioAttributes attributes =
                    new AudioAttributes.Builder()
                            .setUsage(
                                    AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
                            )
                            .setContentType(
                                    AudioAttributes.CONTENT_TYPE_SONIFICATION
                            )
                            .build();

            AudioFormat format =
                    new AudioFormat.Builder()
                            .setEncoding(
                                    AudioFormat.ENCODING_PCM_16BIT
                            )
                            .setSampleRate(
                                    SAMPLE_RATE
                            )
                            .setChannelMask(
                                    AudioFormat.CHANNEL_OUT_MONO
                            )
                            .build();

            track =
                    new AudioTrack(
                            attributes,
                            format,
                            samples.length * 2,
                            AudioTrack.MODE_STATIC,
                            AudioManager.AUDIO_SESSION_ID_GENERATE
                    );

            track.write(
                    samples,
                    0,
                    samples.length
            );

            track.play();

            try {

                Thread.sleep(140);

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
            }

        } catch (Exception ignored) {

        } finally {

            if (track != null) {

                try {
                    track.stop();
                } catch (Exception ignored) {
                }

                track.release();
            }
        }
    }
}
