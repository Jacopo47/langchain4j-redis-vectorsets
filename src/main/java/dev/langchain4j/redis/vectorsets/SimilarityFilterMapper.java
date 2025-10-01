package dev.langchain4j.redis.vectorsets;

import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.*;
import dev.langchain4j.store.embedding.filter.logical.And;
import dev.langchain4j.store.embedding.filter.logical.Not;
import dev.langchain4j.store.embedding.filter.logical.Or;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Collection;
import java.util.function.Function;

public class SimilarityFilterMapper {

    private static final Logger log = LoggerFactory.getLogger(SimilarityFilterMapper.class);

    public SimilarityFilterMapper() {}

    Optional<String> from(Filter filter) {
        if (filter == null) return Optional.empty();

        return switch(filter) {
            case IsEqualTo e -> format(".%s==%s", e.key(), e.comparisonValue());
            case IsNotEqualTo e -> format(".%s!=%s", e.key(), e.comparisonValue());
            case IsGreaterThanOrEqualTo e -> format(".%s>=%s", e.key(), e.comparisonValue());
            case IsGreaterThan e -> format(".%s>%s", e.key(), e.comparisonValue());
            case IsLessThan e -> format(".%s<%s", e.key(), e.comparisonValue());
            case IsLessThanOrEqualTo e -> format(".%s<=%s", e.key(), e.comparisonValue());
            case IsIn e -> format(".%s in %s", e.key(), e.comparisonValues());
            case IsNotIn e -> format(".%s not (in %s)", e.key(), e.comparisonValues());
            case And e -> from(e.left())
                    .flatMap(left ->
                            from(e.right())
                            .map(right -> String.format("(%s and %s)", left, right)));
            case Or e -> from(e.left())
                    .flatMap(left ->
                            from(e.right())
                            .map(right -> String.format("(%s or %s)", left, right)));
            case Not e -> from(e.expression()).map(not -> String.format("(not %s)", not));
            default -> {
                log.warn("Type {} not supported.", filter);
                yield Optional.empty();
            }
        };
    }

    private Optional<String> format(String format, String key, Object value) {
        return Optional.of(String.format(format, key, toString(value)));
    }

    private String toString(Object input) {
        if (input == null) return null;

        Function<Collection<?>, String> collectIntoArray = i -> {
            List<String> values = i
                    .stream()
                    .map(e -> switch (e) {
                        case String s -> toString(s);
                        case null -> null;
                        default -> e.toString();
                    })
                    .filter(Objects::nonNull)
                    .toList();
            var joined = String.join(",", values);

            return String.format("[%s]", joined);
        };

        return switch (input) {
            case String e -> String.format("\"%s\"", e);
            case Collection<?> e -> collectIntoArray.apply(e);
            default -> input.toString();
        };
    }

}
