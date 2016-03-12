package net.chiragaggarwal.android.popflix.models;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;

public class VideoTest {
    @Test
    public void shouldBeEqualToOtherIfHasSameAttributesAsOther() {
        Video thisVideo = new Video("1", "en", "US", "7jIBCiYg58k", "example movie", "Youtube", "Trailer");
        Video thatVideo = new Video("1", "en", "US", "7jIBCiYg58k", "example movie", "Youtube", "Trailer");
        assert (thisVideo.equals(thatVideo));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        Video thisVideo = new Video("1", "en", "US", "7jIBCiYg58k", "example movie", "Youtube", "Trailer");
        assertFalse(thisVideo.equals(null));
    }

    @Test
    public void shouldNotBeEqualToAnythingOtherThanAVideo() {
        Video thisVideo = new Video("1", "en", "US", "7jIBCiYg58k", "example movie", "Youtube", "Trailer");
        assertFalse(thisVideo.equals(new Object()));
    }

    @Test
    public void shouldNotBeEqualIfHasDifferentAttributes() {
        Video thisVideo = new Video("1", "en", "US", "7jIBCiYg58k", "example movie", "Youtube", "Trailer");
        Video thatVideo = new Video("2", "fr", "US", "748sjd246r5", "second example movie", "Youtube", "Trailer");
        assertFalse(thisVideo.equals(thatVideo));
    }

    @Test
    public void shouldBeEqualToItself() {
        Video thisVideo = new Video("1", "en", "US", "7jIBCiYg58k", "example movie", "Youtube", "Trailer");
        assert (thisVideo.equals(thisVideo));
    }

    @Test
    public void shouldBeEqualCommutatively() {
        Video thisVideo = new Video("1", "en", "US", "7jIBCiYg58k", "example movie", "Youtube", "Trailer");
        Video thatVideo = new Video("1", "en", "US", "7jIBCiYg58k", "example movie", "Youtube", "Trailer");
        assert (thatVideo.equals(thisVideo));
    }

    @Test
    public void shouldHaveSameHashCodeIfBothVideoAreSame() {
        Video thisVideo = new Video("1", "en", "US", "7jIBCiYg58k", "example movie", "Youtube", "Trailer");
        Video thatVideo = new Video("1", "en", "US", "7jIBCiYg58k", "example movie", "Youtube", "Trailer");
        assertEquals(thisVideo.hashCode(), thatVideo.hashCode());
    }
}