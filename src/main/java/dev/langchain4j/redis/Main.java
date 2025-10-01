package dev.langchain4j.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.redis.vectorsets.RedisVectorSetEmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Main {

    public record Sentence(String name, String text, int age){ }


    private static String acceptFromStdin(Scanner scanner) {
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        } else {
            System.out.println("Could not read. Exiting.");
            return null;
        }
    }

    public static void main(String[] args) {
        try (
            final Scanner scanner = new Scanner(System.in);
            final Jedis client = new Jedis("localhost", 6379)
        ) {
            var objectMapper = new ObjectMapper();
            var embeddingModel = new AllMiniLmL6V2EmbeddingModel();

            final RedisVectorSetEmbeddingStore store = new RedisVectorSetEmbeddingStore(client, "sentences:test");

            System.out.print("Please enter your name: ");
            var name = acceptFromStdin(scanner);
            System.out.print("Please enter your age: ");
            var age = Optional.ofNullable(acceptFromStdin(scanner))
                        .map(Integer::parseInt)
                        .orElse(0);
            System.out.println("\nHello, " + name + "!");
            System.out.println("Enter your sentence!");
            System.out.println("Press Ctrl+C at any time to terminate the program.");

            if (name == null) {
                System.out.println("Quitting.. bye!");
            }

            BiFunction<String, Filter, Optional<String>> query = (i, filter) -> Optional.ofNullable(i)
                    .map(s -> s.replaceFirst(":q", ""))
                    .map(embeddingModel::embed)
                    .map(Response::content)
                    .map(s -> EmbeddingSearchRequest.builder()
                            .queryEmbedding(s)
                            .filter(filter)
                            .maxResults(10)
                            .build())
                    .map(store::search)
                    .map(EmbeddingSearchResult::matches)
                    .map(Collection::stream)
                    .map(matches -> matches
                            .map(e -> String.format("[%s] %s", e.score(), e.embedded().text()))
                            .reduce("", (a, b) -> String.format("%s\n%s", a, b)));

            Function<String, Optional<String>> addToStore = i -> Optional.of(i)
                    .map(e -> new Sentence(name, e, age))
                    .map(s -> {
                        var content = embeddingModel.embed(i).content();
                        try {
                            store.add(content, TextSegment.from(objectMapper.writeValueAsString(s)));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }

                        return s;
                    })
                    .map(s -> String.format("[%s] Says: %s", s.name(), s.text()));

            while (true) {
                System.out.print("> ");

                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();

                    var command = input.substring(0, 2);


                    Function<String, Optional<String>> action = switch (command) {
                        case ":q" -> e -> query.apply(e, ignore -> true);
                        case ":w" -> e -> Optional.ofNullable(acceptFromStdin(scanner))
                                .map(n -> new IsEqualTo("name", n))
                                .flatMap(f -> query.apply(e, f));
                        case ":a" -> e -> Optional.ofNullable(acceptFromStdin(scanner))
                                .map(Integer::parseInt)
                                .map(n -> new IsEqualTo("age", n))
                                .flatMap(f -> query.apply(e, f));
                        default -> addToStore;
                    };


                    action.apply(input)
                            .ifPresent(System.out::println);
                }
            }
        } finally {
            System.out.println("\nProgram terminated gracefully. Goodbye.");
        }
    }
}