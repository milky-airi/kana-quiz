@RestController
public class RootController {

    @GetMapping("/")
    public String root() {
        return "OK";
    }

    @GetMapping("/health")
    public String health() {
        return "UP";
    }
}
