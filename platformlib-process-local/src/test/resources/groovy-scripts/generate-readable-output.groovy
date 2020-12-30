//
/**
 * Generate and put data into stdout and stderr
 * @param args command line parameters, first - num of stdout lines, seconds num of stderr lines
 */
static void main(String... args) {
    if (args.length != 2) {
        throw new IllegalStateException("Missing parameters")
    }
    int stdOutSize = Integer.valueOf(args[0])
    int stdErrSize = Integer.valueOf(args[1])
    for (int i = 0; i < Math.max(stdOutSize, stdErrSize); i++) {
        if (i < stdOutSize) {
            if (i > 0) {
                System.out.print('\n')
            }
            System.out.print("#$i#")
        }
        if (i < stdErrSize) {
            if (i > 0) {
                System.err.print('\n')
            }
            System.err.print("*$i*")
        }
    }
}
