package com.mongoplus.manager;

import com.mongodb.MongoNamespace;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.BsonArray;
import org.bson.Document;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * 验证分页备份产生的每个 JSON 文件均可独立解析。
 */
public class BackupManagerPaginationTest {

    @Test
    public void pagedBackupEntriesShouldBeValidJsonArrays() throws IOException {
        assertBackupEntries(3, 2, 2);
    }

    @Test
    public void fullSinglePageBackupEntryShouldBeValidJsonArray() throws IOException {
        assertBackupEntries(2, 2, 1);
    }

    @Test
    public void singleDocumentBackupEntryShouldBeValidJsonArray() throws IOException {
        assertBackupEntries(1, 2, 1);
    }

    @Test
    public void fullFinalPageBackupEntriesShouldBeValidJsonArrays() throws IOException {
        assertBackupEntries(4, 2, 2);
    }

    @Test
    public void partialFinalPageBackupEntriesShouldBeValidJsonArrays() throws IOException {
        assertBackupEntries(5, 2, 3);
    }

    @Test
    public void emptyCollectionShouldKeepExistingNoBackupBehavior() throws IOException {
        Path backupPath = Files.createTempDirectory("mongo-plus-backup-");
        try {
            BackupManager manager = new BackupManager(backupPath.toString(), null);
            manager.setLimit(2);
            assertNull(manager.backupCollectionToJSON(collection(new ArrayList<>())));
        } finally {
            deleteRecursively(backupPath);
        }
    }

    private void assertBackupEntries(int documentCount, int limit, int expectedEntryCount) throws IOException {
        Path backupPath = Files.createTempDirectory("mongo-plus-backup-");
        try {
            BackupManager manager = new BackupManager(backupPath.toString(), null);
            manager.setLimit(limit);
            List<Document> documents = new ArrayList<>();
            for (int index = 1; index <= documentCount; index++) {
                documents.add(new Document("_id", index));
            }
            String zipPath = manager.backupCollectionToJSON(collection(documents));
            int entryCount = 0;
            try (ZipInputStream input = new ZipInputStream(Files.newInputStream(Path.of(zipPath)))) {
                ZipEntry entry;
                while ((entry = input.getNextEntry()) != null) {
                    String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                    BsonArray array = BsonArray.parse(json);
                    assertFalse("backup entry must not leave a trailing comma before its closing array", json.contains(",\n\n]"));
                    int documentsInEntry = Math.min(limit, documentCount - entryCount * limit);
                    assertEquals(documentsInEntry + 1, array.size());
                    input.closeEntry();
                    entryCount++;
                }
            }
            assertEquals(expectedEntryCount, entryCount);
        } finally {
            deleteRecursively(backupPath);
        }
    }

    @SuppressWarnings("unchecked")
    private MongoCollection<Document> collection(List<Document> documents) {
        return (MongoCollection<Document>) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{MongoCollection.class}, (proxy, method, args) -> {
                    if ("getNamespace".equals(method.getName())) {
                        return new MongoNamespace("backup", "records");
                    }
                    if ("estimatedDocumentCount".equals(method.getName())) {
                        return (long) documents.size();
                    }
                    if ("find".equals(method.getName())) {
                        return pagedIterable(documents);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    @SuppressWarnings("unchecked")
    private FindIterable<Document> pagedIterable(List<Document> documents) {
        final int[] skip = {0};
        final int[] limit = {documents.size()};
        return (FindIterable<Document>) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{FindIterable.class}, (proxy, method, args) -> {
                    if ("skip".equals(method.getName())) {
                        skip[0] = (Integer) args[0];
                        return proxy;
                    }
                    if ("limit".equals(method.getName())) {
                        limit[0] = (Integer) args[0];
                        return proxy;
                    }
                    if ("batchSize".equals(method.getName())) {
                        return proxy;
                    }
                    if ("iterator".equals(method.getName())) {
                        int end = Math.min(skip[0] + limit[0], documents.size());
                        return cursor(documents.subList(skip[0], end));
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    @SuppressWarnings("unchecked")
    private MongoCursor<Document> cursor(List<Document> documents) {
        final int[] index = {0};
        return (MongoCursor<Document>) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class[]{MongoCursor.class}, (proxy, method, args) -> {
                    if ("hasNext".equals(method.getName())) {
                        return index[0] < documents.size();
                    }
                    if ("next".equals(method.getName())) {
                        return documents.get(index[0]++);
                    }
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            paths.sorted((left, right) -> right.compareTo(left))
                    .forEach(current -> {
                        try {
                            Files.delete(current);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }
}
