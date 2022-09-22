package ch.epfl.tchu;

/**
 * @author Tom Nonnenmacher
 * @author Théo Ducrey
 */
public final class Preconditions {

    /**
     * Private constructor so that it is impossible to instantiate a Preconditions class
     */
    private Preconditions() {}

    /**
     * @param shouldBeTrue : argument to be checked if true
     * @throws IllegalArgumentException if the argument is false
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if(!shouldBeTrue) {
            throw new IllegalArgumentException();
        }
    }
}

