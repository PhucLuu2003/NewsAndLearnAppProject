package com.example.newsandlearn.Utils;

import android.util.Log;

import com.example.newsandlearn.Model.UserVocabulary;
import com.example.newsandlearn.Model.Vocabulary;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

/**
 * VocabularyHelper - Utility class for vocabulary operations
 * Handles adding vocabulary to the new two-collection structure
 */
public class VocabularyHelper {

    private static final String TAG = "VocabularyHelper";
    private static VocabularyHelper instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private VocabularyHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized VocabularyHelper getInstance() {
        if (instance == null) {
            instance = new VocabularyHelper();
        }
        return instance;
    }

    /**
     * Add vocabulary from article
     * Checks if word exists in public collection, if not creates it
     * Then adds to user's vocabulary
     */
    public Task<String> addVocabularyFromArticle(String word, String translation, 
                                                   String articleId, String level) {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        String userId = auth.getCurrentUser().getUid();
        
        // Step 1: Check if word already exists in public vocabularies
        return db.collection("vocabularies")
                .whereEqualTo("word", word.toLowerCase().trim())
                .limit(1)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    QuerySnapshot querySnapshot = task.getResult();
                    
                    if (!querySnapshot.isEmpty()) {
                        // Word exists, use existing vocabulary
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String vocabId = doc.getId();
                        return addToUserVocabulary(userId, vocabId, articleId, "article");
                    } else {
                        // Word doesn't exist, create new public vocabulary
                        return createNewVocabulary(word, translation, level, userId)
                                .continueWithTask(createTask -> {
                                    if (!createTask.isSuccessful()) {
                                        throw createTask.getException();
                                    }
                                    String vocabId = createTask.getResult();
                                    return addToUserVocabulary(userId, vocabId, articleId, "article");
                                });
                    }
                });
    }

    /**
     * Add vocabulary manually (from dialog)
     */
    public Task<String> addVocabularyManually(Vocabulary vocabulary) {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        String userId = auth.getCurrentUser().getUid();
        vocabulary.setCreatedBy(userId);
        vocabulary.setCreatedAt(new Date());
        vocabulary.setPublic(false); // User-created words are private by default

        // Step 1: Add to public vocabularies collection
        return db.collection("vocabularies")
                .add(vocabulary)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    
                    DocumentReference docRef = task.getResult();
                    String vocabId = docRef.getId();
                    
                    // Update the vocabulary with its ID
                    docRef.update("id", vocabId);
                    
                    // Step 2: Add to user's vocabulary
                    return addToUserVocabulary(userId, vocabId, null, "manual");
                });
    }

    /**
     * Create new vocabulary in public collection
     */
    private Task<String> createNewVocabulary(String word, String translation, 
                                             String level, String userId) {
        Vocabulary vocab = new Vocabulary();
        vocab.setWord(word.trim());
        vocab.setTranslation(translation.trim());
        vocab.setLevel(level != null ? level : "B1");
        vocab.setCreatedBy(userId);
        vocab.setCreatedAt(new Date());
        vocab.setPublic(false); // User-created words are private by default

        return db.collection("vocabularies")
                .add(vocab)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    
                    DocumentReference docRef = task.getResult();
                    String vocabId = docRef.getId();
                    
                    // Update the vocabulary with its ID
                    docRef.update("id", vocabId);
                    
                    return vocabId;
                });
    }

    /**
     * Add vocabulary to user's collection
     */
    private Task<String> addToUserVocabulary(String userId, String vocabId, 
                                             String sourceArticleId, String sourceType) {
        // Check if user already has this vocabulary
        return db.collection("users").document(userId)
                .collection("user_vocabulary")
                .document(vocabId)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    DocumentSnapshot doc = task.getResult();
                    
                    if (doc.exists()) {
                        // User already has this word
                        return Tasks.forResult("Word already in your vocabulary");
                    }

                    // Create new user vocabulary entry
                    UserVocabulary userVocab = new UserVocabulary(vocabId);
                    userVocab.setSourceArticleId(sourceArticleId);
                    userVocab.setSourceType(sourceType);
                    userVocab.calculateNextReview();

                    return db.collection("users").document(userId)
                            .collection("user_vocabulary")
                            .document(vocabId)
                            .set(userVocab)
                            .continueWith(setTask -> {
                                if (!setTask.isSuccessful()) {
                                    throw setTask.getException();
                                }
                                return "Vocabulary added successfully";
                            });
                });
    }

    /**
     * Download vocabulary set
     * Adds all vocabularies from a set to user's collection
     */
    public Task<String> downloadVocabularySet(String setId) {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        String userId = auth.getCurrentUser().getUid();

        // Step 1: Get set details
        return db.collection("vocabulary_sets").document(setId)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    DocumentSnapshot doc = task.getResult();
                    if (!doc.exists()) {
                        throw new Exception("Vocabulary set not found");
                    }

                    // Get vocabulary IDs from set
                    Object vocabIdsObj = doc.get("vocabularyIds");
                    if (!(vocabIdsObj instanceof java.util.List)) {
                        throw new Exception("Invalid vocabulary set data");
                    }

                    @SuppressWarnings("unchecked")
                    java.util.List<String> vocabIds = (java.util.List<String>) vocabIdsObj;

                    if (vocabIds == null || vocabIds.isEmpty()) {
                        throw new Exception("Vocabulary set is empty");
                    }

                    // Step 2: Add all vocabularies to user's collection using batch
                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                    for (String vocabId : vocabIds) {
                        UserVocabulary userVocab = new UserVocabulary(vocabId);
                        userVocab.setSourceType("set");
                        userVocab.calculateNextReview();

                        DocumentReference docRef = db.collection("users")
                                .document(userId)
                                .collection("user_vocabulary")
                                .document(vocabId);

                        batch.set(docRef, userVocab);
                    }

                    // Step 3: Add set to user's sets
                    com.example.newsandlearn.Model.UserSet userSet = 
                            new com.example.newsandlearn.Model.UserSet(setId);
                    userSet.setTotalWords(vocabIds.size());

                    DocumentReference setRef = db.collection("users")
                            .document(userId)
                            .collection("user_sets")
                            .document(setId);

                    batch.set(setRef, userSet);

                    // Commit batch
                    return batch.commit().continueWith(commitTask -> {
                        if (!commitTask.isSuccessful()) {
                            throw commitTask.getException();
                        }

                        // Update download count
                        db.collection("vocabulary_sets").document(setId)
                                .update("downloadCount", com.google.firebase.firestore.FieldValue.increment(1));

                        return "Vocabulary set downloaded successfully";
                    });
                });
    }

    /**
     * Update vocabulary progress (mastery, review count, etc.)
     */
    public Task<Void> updateVocabularyProgress(String vocabId, UserVocabulary userVocab) {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        String userId = auth.getCurrentUser().getUid();

        return db.collection("users").document(userId)
                .collection("user_vocabulary")
                .document(vocabId)
                .set(userVocab);
    }

    /**
     * Mark vocabulary as correct (for flashcard/quiz)
     */
    public Task<Void> markCorrect(String vocabId) {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        String userId = auth.getCurrentUser().getUid();

        return db.collection("users").document(userId)
                .collection("user_vocabulary")
                .document(vocabId)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    DocumentSnapshot doc = task.getResult();
                    if (!doc.exists()) {
                        throw new Exception("Vocabulary not found");
                    }

                    UserVocabulary userVocab = doc.toObject(UserVocabulary.class);
                    if (userVocab != null) {
                        userVocab.markCorrect();
                        return updateVocabularyProgress(vocabId, userVocab);
                    }

                    throw new Exception("Failed to parse vocabulary data");
                });
    }

    /**
     * Mark vocabulary as incorrect (for flashcard/quiz)
     */
    public Task<Void> markIncorrect(String vocabId) {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        String userId = auth.getCurrentUser().getUid();

        return db.collection("users").document(userId)
                .collection("user_vocabulary")
                .document(vocabId)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    DocumentSnapshot doc = task.getResult();
                    if (!doc.exists()) {
                        throw new Exception("Vocabulary not found");
                    }

                    UserVocabulary userVocab = doc.toObject(UserVocabulary.class);
                    if (userVocab != null) {
                        userVocab.markIncorrect();
                        return updateVocabularyProgress(vocabId, userVocab);
                    }

                    throw new Exception("Failed to parse vocabulary data");
                });
    }

    /**
     * Delete vocabulary from user's collection
     */
    public Task<Void> deleteUserVocabulary(String vocabId) {
        if (auth.getCurrentUser() == null) {
            return Tasks.forException(new Exception("User not authenticated"));
        }

        String userId = auth.getCurrentUser().getUid();

        return db.collection("users").document(userId)
                .collection("user_vocabulary")
                .document(vocabId)
                .delete();
    }
}
