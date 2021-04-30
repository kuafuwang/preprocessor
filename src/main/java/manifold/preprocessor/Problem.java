package manifold.preprocessor;

public class Problem {
    public static final int Ignore = 0; // during handling only
    public  static final int Warning = 1; // during handling only
    public  static final int Error = 2; // when bit is set: problem is error, if not it is a warning
    public Problem(
            String message,
            int severity,
            int startPosition,
            int endPosition)
    {
             this.message = message;
             this.startPosition = startPosition;
             this.endPosition = endPosition;
    }
    public Problem(
            String message,
            int startPosition,
            int endPosition)
    {
       this(message,Error,startPosition,endPosition);
    }
        public String message;
    public int startPosition=0, endPosition=0;
    /**
     * Answer the end position of the problem (inclusive), or -1 if unknown.
     * @return int
     */
    public int getSourceEnd() {

        return endPosition;
    }


    /**
     * Answer the start position of the problem (inclusive), or -1 if unknown.
     * @return int
     */
    public int getSourceStart() {

        return startPosition;
    }

    /**
     * Answer a localized, human-readable message string which describes the problem.
     * @return java.lang.String
     */
    public String getMessage() {

        return message;
    }
}
