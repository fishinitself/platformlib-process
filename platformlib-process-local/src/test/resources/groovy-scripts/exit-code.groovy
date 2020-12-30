/**
 * Exit with specified exit code.
 * @param args command line parameters, first - exit code
 */
static void main(String... args) {
    if (args.length != 1) {
        throw new IllegalStateException("Missing parameters")
    }
    System.exit(Integer.valueOf(args[0]))
}
