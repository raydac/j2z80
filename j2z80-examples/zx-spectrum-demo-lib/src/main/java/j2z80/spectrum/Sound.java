package j2z80.spectrum;

public class Sound {

  private Sound() {
  }

  public static native void tone(final int duration, final int pitch);
}
