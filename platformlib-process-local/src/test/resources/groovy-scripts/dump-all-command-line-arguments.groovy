//Dump all command line arguments escaped by # to stdout
static void main(String... args) {
    args.each {println("#$it#")}
}
