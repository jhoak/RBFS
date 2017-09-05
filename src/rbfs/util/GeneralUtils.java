package rbfs.util;

/**
 * Contains general utility functions.
 * @author James Hoak
 * @version 1.0
 */
public final class GeneralUtils {

    /**
     * Returns true if any of the given objects are null.
     * @param objects The objects to check against null.
     * @return True if any of the given objects are null, and false otherwise.
     */
    public static boolean anyNull(Object... objects) {
        for (Object o : objects)
            if (o == null)
                return true;
        return false;
    }

    /**
     * From the given collection of argument names and their values, returns the name of the first
     * arg whose value is null. Should generally be used for null checking parameters in methods
     * (unless these methods have like 1 argument). Both arguments (arrays) must be non-null and
     * must have the same length.
     * @param argNames The names of the arguments.
     * @param argValues The corresponding values for these arguments.
     * @return The name of the first arg whose value is null, or null if there are no such args.
     * @throws IllegalArgumentException If any arg name (not value) is null.
     */
    public static String firstNullArg(String[] argNames, Object[] argValues)
    throws IllegalArgumentException {
        if (anyNull(argNames))
            throw new IllegalArgumentException("Null value passed as arg name");

        Pair<String, Object>[] args = Pair.zip(argNames, argValues, true);
        for (Pair<String, Object> p : args) {
            if (p.getSecond() == null) {
                return p.getFirst();
            }
        }
        return null;
    }

    /**
     * Represents a pair of values.
     * @param <S> The type of the first value.
     * @param <T> The type of the second value.
     */
    public static class Pair<S, T> {
        private S first;
        private T second;

        /**
         * Constructs a new Pair.
         * @param first The first value.
         * @param second The second value.
         */
        public Pair(S first, T second) {
            this.first = first;
            this.second = second;
        }

        /**
         * Returns the first value.
         * @return The first value of the pair
         */
        public S getFirst() { return first; }

        /**
         * Sets the first value of the pair to a new value.
         * @param first The new first value of the pair.
         */
        public void setFirst(S first) { this.first = first; }

        /**
         * Returns the second value.
         * @return The second value of the pair
         */
        public T getSecond() { return second; }

        /**
         * Sets the second value of the pair to a new value.
         * @param second The new second value of the pair.
         */
        public void setSecond(T second) { this.second = second; }

        /**
         * Matches two arrays together to create an array of pairs. Every item of the first array,
         * si, is matched with the item, ti, at the same index in the other array. The resulting
         * array is only as long as the smaller of the two given arrays.
         * Example: zip([1,2,3], [4,5,6]) yields [[1,4], [2,5], [3,6]].
         * @param sArr The first array.
         * @param tArr The second array.
         * @param requireSameLength If true, the arrays must be of the same length.
         * @param <S> The type of items in the first array.
         * @param <T> The type of items in the second array.
         * @return An array of Pairs mapping each si to its respective ti at the same index.
         * @throws IllegalArgumentException If either array is null, or if requireSameLength is
         * true but the arrays are not of the same length.
         */
        @SuppressWarnings("unchecked")
        public static <S, T> Pair<S, T>[] zip(S[] sArr, T[] tArr, boolean requireSameLength)
        throws IllegalArgumentException {
            if (sArr == null)
                throw new IllegalArgumentException("Null passed as param value of sArr");
            else if (tArr == null)
                throw new IllegalArgumentException("Null passed as param value of tArr");
            else if (sArr.length != tArr.length && requireSameLength) {
                String err = String.format(
                        "Cannot zip list of length %d with list of length %d",
                        sArr.length,
                        tArr.length
                );
                throw new IllegalArgumentException(err);
            }
            int len = (sArr.length < tArr.length) ? sArr.length : tArr.length;
            Object[] pairs = new Object[len];
            for (int i = 0; i < len; i++)
                pairs[i] = new Pair<>(sArr[i], tArr[i]);

            return (Pair<S, T>[])pairs;
        }
    }
}
