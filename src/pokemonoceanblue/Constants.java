package pokemonoceanblue;

import java.util.Set;

public class Constants {
  // list of sound-based moves that are negated by SOUNDPROOF
  public static final Set<Integer> SOUND_MOVES = Set.of(45, 47, 48, 103, 173, 195, 253, 304, 319, 405);
  // list of explosion moves prevented by DAMP
  public static final Set<Integer> EXPLOSION_MOVES = Set.of(120, 153);
}
