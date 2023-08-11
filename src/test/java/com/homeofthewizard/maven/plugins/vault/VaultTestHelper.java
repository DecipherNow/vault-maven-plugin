package com.homeofthewizard.maven.plugins.vault;

import com.homeofthewizard.maven.plugins.vault.config.Mapping;
import com.homeofthewizard.maven.plugins.vault.config.Path;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VaultTestHelper {

    public static Mapping randomMapping() {
        return new Mapping(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }

    public static List<Mapping> randomMappings(int count) {
        return IntStream.range(0, count).mapToObj(i -> randomMapping()).collect(Collectors.toList());
    }

    public static Path randomPath(int mappingCount) {
        return new Path(String.format("secret/%s", UUID.randomUUID()), randomMappings(mappingCount));
    }

    public static List<Path> randomPaths(int pathCount, int mappingCount) {
        return IntStream.range(0, pathCount).mapToObj(i -> randomPath(mappingCount)).collect(Collectors.toList());
    }

    public static Map<String, String> secretsFromPaths(List<Path> paths){
        return paths.stream()
                .map(Path::getMappings)
                .flatMap(List::stream)
                .collect(Collectors.toMap(Mapping::getKey, Mapping::getKey));
    }

    public static Map<String, String> propertiesFromPaths(List<Path> paths){
        return paths.stream()
                .map(Path::getMappings)
                .flatMap(List::stream)
                .collect(Collectors.toMap(Mapping::getProperty, Mapping::getKey));
    }
}
