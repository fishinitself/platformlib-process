import java.nio.file.Paths

//Print current directory
static void main(String... args) {
    System.out.println(Paths.get(".").toAbsolutePath().normalize())
}
